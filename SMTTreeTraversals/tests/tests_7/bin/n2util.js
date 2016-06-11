/*
   Copyright 2015 Software Reliability Lab, ETH Zurich

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */



(function(root, mod) {
  if (typeof exports == "object" && typeof module == "object") // CommonJS
    return mod(exports, require("./acorn"), require("./util/walk"));
  if (typeof define == "function" && define.amd) // AMD
    return define([ "exports", "./acorn", "./util/walk" ], mod);
  mod(root.tern || (root.tern = {}), acorn, acorn.walk); // Plain browser env
})(this, function(exports, acorn, walk) {

  /////////////////////////////////////////////////
  // AST Traversal
  /////////////////////////////////////////////////
  exports.addAstTraversalInfo = function(ast) {
    var stack = [];

    var capitalize = function (input) {
      return input.charAt(0).toUpperCase() + input.slice(1);
    }
    
    var walker = walk.make({
      VariableDeclaration : function(node, st, c) {
        for (var i = 0; i < node.declarations.length; ++i) {
          c(node.declarations[i], st);
        }
      },
      VariableDeclarator : function(node, st, c) {
        if (node.init)
          c(node.init, st, "Expression");
      },
      MemberExpression : function(node, st, c) {
        c(node.object, st, "Expression");
        //If computed === true, the node corresponds to a computed e1[e2] expression and property is an Expression. 
        //If computed === false, the node corresponds to a static e1.x expression and property is an Identifier.
        if (node.computed)
          c(node.property, st, "Expression");
        else
          c(node.property, st);
      },
      FunctionDeclaration : function(node, st, c) {
        c(node.id, st);
        for (var i = 0; i < node.params.length; ++i) {
          c(node.params[i], st);
        }
        c(node.body);
      },
      FunctionExpression : function(node, st, c) {
        for (var i = 0; i < node.params.length; ++i) {
          c(node.params[i], st);
        }
        c(node.body);
      },    
      DebuggerStatement : function(node, st, c){
        // Simply ignoring it may break the program. E.g., if it's inside an 'if' without the scope
        throw "'debugger;' statement not supported";
      },
      WithStatement : function(node, st, c) {
        throw "'with' construct not supported";
      },
      ForOfStatement : function(node, st, c) {
        throw "'for...of' construct not supported";
      },
      ForStatement : function(node, st, c) {
        if (node.init) {
          c(node.init, st);
        } else {
          empty_node = new acorn.Node();
          empty_node.type = "EmptyStatement";
          c(empty_node, st); 
        }
        if (node.test) {
          c(node.test, st);
        } else {
          empty_node = new acorn.Node();
          empty_node.type = "EmptyStatement";
          c(empty_node, st); 
        }
        if (node.update) {
          c(node.update, st);  
        } else {
          empty_node = new acorn.Node();
          empty_node.type = "EmptyStatement";
          c(empty_node, st); 
        }
        c(node.body, st);
      },
      CatchClause : function(node, st, c) {                
        c(node.param, st);          
        c(node.body, st);
      },
      TryStatement : function(node, st, c) {
        c(node.block, st, "Statement");
        if (node.handler) {          
          c(node.handler, st);
        } else {
          empty_node = new acorn.Node();
          empty_node.type = "EmptyStatement";
          c(empty_node, st); 
        }
        if (node.finalizer) {
          c(node.finalizer, st, "Statement");
        } else {
          empty_node = new acorn.Node();
          empty_node.type = "EmptyStatement";
          c(empty_node, st); 
        }
      },
      SwitchCase : function(node, st, c) {
        // node.test === null if and only if it's a default case
        if (node.test) {
          c(node.test, st, "Expression");
        } else {
          empty_node = new acorn.Node();
          empty_node.type = "EmptyStatement";
          c(empty_node, st); 
        }
        
        body_node = new acorn.Node();
        body_node.type = "BlockStatement";
        body_node.body = node.consequent;
        c(body_node, st);
      },
      SwitchStatement : function(node, st, c) {
        c(node.discriminant, st, "Expression");
        for (var i = 0; i < node.cases.length; ++i) {          
          var cs = node.cases[i];
          c(cs, st);          
        }
      },
      Literal : function(node, st, c) {
//      value: string | boolean | null | number | RegExp;
        if (node.value === null) {
          node.type = "LiteralNull";
          node.value = "null";
        } else if (node.value instanceof RegExp) {
          node.type = "LiteralRegExp";          
        } else {
          node.type = "Literal" + capitalize(typeof(node.value));
        }
        
        if (typeof node.value !== 'string') {
          node.value = node.value.toString();
        }
      }
    }, walk.base);

    function traverse(node, st, override) {
//      if (node.type == "DebuggerStatement"){
//        console.error("Ignoring '" + node.type + "'");
//        return; 
//      } 
      
      if (override) {
        walker[override](node, st, traverse);
      } else {
        node.hole = undefined;

        if (stack.length > 0) {
          var top = stack[stack.length - 1];
          node.ast_parent = top;
          if (!top.ast_children) {
            top.ast_children = [];
          }
          node.ast_parent = top;
          top.ast_children.push(node);
        }

        node.ast_children = undefined;
        stack.push(node);
        if (node.type)
          walker[node.type](node, st, traverse);
        stack.pop();
      }
    }
    traverse(ast, null);
  }
  
  var isNodeHole = exports.isNodeHole = function(node) {
    return (node.type == "Identifier" && node.name == "✖");
  }
  
  var astMarkHole = exports.astMarkHole = function(ast) {
    if (isNodeHole(ast)) {
      ast.hole = "HOLE";
      return true;
    }
    if (ast.ast_children) {
      for (var i in ast.ast_children) {
        if (astMarkHole(ast.ast_children[i])) return true;
      }
    }
    return false;
  }

  
  var global_nodejs_scope = { "module" : true, 
                               "exports" : true,
                               "setTimeout" : true,
                               "clearTimeout" : true,
                               "setInterval" : true,
                               "clearInterval" : true,
                               "global" : true,
                               "process" : true,
                               "console" : true,
                               "require" : true,
                                "define": true
                              }
    
	var global_value_props = {
    	"Infinity": true,
    	"NaN": true,
    	"undefined": true
	}
	
	var global_func_props = {
    	"eval": true,
    	"uneval(": true,
    	"isFinite": true,
    	"isNaN": true,
    	"parseFloat": true,
    	"parseInt": true,
    	"decodeURI": true,
    	"decodeURIComponent": true,
    	"encodeURI": true,
    	"encodeURIComponent": true,
    	"escape(": true,
    	"unescape(": true
	}
	
	var global_objects = {
    	"Object": true,
    	"Function": true,
    	"Boolean": true,
    	"Symbol ": true,
    	"Error": true,
    	"EvalError": true,
    	"InternalError": true,
    	"RangeError": true,
    	"ReferenceError": true,
    	"SyntaxError": true,
    	"TypeError": true,
    	"URIError": true,
    	"Number": true,
    	"Math": true,
    	"Date": true,
    	"String": true,
    	"RegExp": true,
    	"Array": true,
    	"Int8Array": true,
    	"Uint8Array": true,
    	"Uint8ClampedArray": true,
    	"Int16Array": true,
    	"Uint16Array": true,
    	"Int32Array": true,
    	"Uint32Array": true,
    	"Float32Array": true,
    	"Float64Array": true,
    	"ArrayBuffer": true,
    	"DataView": true,
    	"JSON": true,
    	"Intl": true,
    	"Intl.Collator": true,
    	"Intl.DateTimeFormat": true,
    	"Intl.NumberFormat": true,
      "document": true,
      "window": true,
      "navigator": true,
      "$": true,
      "jQuery": true
	}
  
  function nodeValue(node) {
      if (node.hole) {
        return "?";
      }
      if (node.type.substr(0, "Literal".length )  == "Literal") {
        return node.value;
      }
      if (node.type == "Identifier") {
        return node.name;
      }
      if (node.type == "UnaryExpression" || node.type == "BinaryExpression") {
        return node.operator;
      }
      if (node.type == "VariableDeclarator") {
        return node.id.name;
      }
      if (node.type == "Property") {
        return node.key.name ? node.key.name : node.key.value;
      }
      if (node.type == "UpdateExpression") {
        if (node.prefix)
          return node.operator + '?';
        else 
          return '?' + node.operator;
      }
      if (node.type == "LogicalExpression") {
        return node.operator;
      }
    
      if (node.type == "LabeledStatement") {        
        return node.label.name;
      }
    
      if (node.type == "ContinueStatement" || node.type == "BreakStatement") {
        if (node.label !== null)
          return node.label.name;
      }
    
      return undefined;
    }

    function nodeType(node) {
      if (node.hole) {
        return "????";
      }
      
      if (node.type == "Identifier" && node.ast_parent
          && node.ast_parent.property == node) {
        return "Property";
      }
      
      if (node.type == "MemberExpression" && node.computed === true) {
        return "ArrayAccess"; 
      }
      return node.type;
    }   
  
  exports.findUnresolvedIdentifiers = function(ast, cx) {
    var unresolved_identifiers = [];        
                         
    var scopes = [ cx.topScope ];     

    function traverse(node) {
      if (node.scope) {
        scopes.push(node.scope);
      }
              
      if (nodeType(node) == "Identifier" && !(node.ast_parent && (node.ast_parent.type == "FunctionDeclaration" || node.ast_parent.type == "FunctionExpression"))){        
        var value = nodeValue(node);
        found = false
        found = value in global_nodejs_scope || value in global_func_props || value in global_value_props || value in global_objects
        for (var i = 0; i < scopes.length; i++) {
          if (value in scopes[i].props) {
            found = true 
          }
        }
        if (!found) {
          unresolved_identifiers.push(nodeValue(node));
        }
      }
      
      if (node.ast_children) {
        for ( var cid in node.ast_children) {
          traverse(node.ast_children[cid]);
        }
      }

      if (node.scope) {
        scopes.pop();
      }
    }
        
    traverse(ast);
    
    return unresolved_identifiers;
  }
  
  /////////////////////////////////////////////////
  // Minified Checker
  /////////////////////////////////////////////////  
  
  var EXPECTED_MAX_NODES_PER_NONOBFUSACATED_LINE = 25;
  var MAX_RATIO_SHORT_NAMES = 0.45;
  var NUM_NUMBERED_LOCALS = 5;

  exports.isMinified = function(ast, cx, code) {    
    var numLines = code.split(/\r\n|\r|\n/).length;
    var numStatements = 0;
    var numNames = 0;
  	var numShortNames = 0;
  	var numNumberedNames = 0;
    
    function traverse(node) {
      numStatements++;
      if (node.type == "Identifier") {
        numNames++;
  			if (node.name.length <= 2 && node.name != "el" && node.name != "$") {
  				numShortNames++;
  			}
  			if (node.name.length >= 2 && node.name[0] == '_') {
  				var c2 = node.name[1];
  				if (c2 >= '0' && c2 <= '9') ++numNumberedNames;
  			}
      }
      
      if (node.ast_children) {
        for (var cid in node.ast_children) {
          traverse(node.ast_children[cid]);
        }
      }      
    }
    
    traverse(ast);
      	
    return (EXPECTED_MAX_NODES_PER_NONOBFUSACATED_LINE * numLines <= numStatements) ||
        (numShortNames > numNames * MAX_RATIO_SHORT_NAMES) ||
        numNumberedNames == numNames ||
        numNumberedNames >= NUM_NUMBERED_LOCALS;
}
  
  
  /////////////////////////////////////////////////
  // Scoped visitor
  /////////////////////////////////////////////////
  exports.findNode = function(ast, cx, id) {
    var result;
    function traverse(node) {
      if (node.ast_node_id == id) {
        result = node;
        return true;
      }      
      
      if (node.ast_children) {
        for ( var cid in node.ast_children) {
          if (traverse(node.ast_children[cid]))
            break;
        }      
      }
    }
    
    traverse(ast);
    return result;
  }

  exports.outputAst = function(ast, cx) {
    var result = "";
    result += "[";
                               
    var scopes = [ cx.topScope ];      
    
    function numbernodes(node) {
      node.ast_node_id = node_id;
      node_id++;
      if (node.ast_children) {
        for (var cid in node.ast_children) {
          numbernodes(node.ast_children[cid]);
        }
      }      
    }

    function traverse(node) {

      result += " { \"id\":" + node.ast_node_id;
      result += ", \"type\":\"" + nodeType(node) + "\"";

      var value = nodeValue(node);
      if (value !== undefined) {
        result += ", \"value\":" + JSON.stringify(value);
      }                 

      if (node.ast_children) {
        result += ", \"children\":[";
        var first = true;
        for ( var cid in node.ast_children) {
          result += (first ? "" : ",") + node.ast_children[cid].ast_node_id;
          first = false;
        }
        result += "]";
      }

      result += " },";
      if (node.ast_children) {
        for ( var cid in node.ast_children) {
          traverse(node.ast_children[cid]);
        }
      }

    }
    
    var node_id = 0;
    numbernodes(ast);
    traverse(ast);
    result += " 0]";
    return result;
  }
});
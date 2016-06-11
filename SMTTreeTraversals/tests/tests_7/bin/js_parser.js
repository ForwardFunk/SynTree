#!/usr/bin/env nodejs

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


var acorn = require("./acorn");
var acorn_walk = require("./util/walk");
//var tern = require("../lib/tern");
var infer = require("./infer");
var n2util = require("./n2util");
var fs = require("fs"), path = require("path"), url = require("url");

var infile = null;
for (var i = 2; i < process.argv.length; ++i) {
	  var arg = process.argv[i];
	  if (arg[0] != "-" && !infile) infile = arg;
}

if (infile === null) {
	console.log("Error! No file to analyze.");
  console.log("Usage:");
  console.log("  ./js_parser.js file.js");
	process.exit(0);
}

var code = fs.readFileSync(infile, "utf8");
var ast;
var cx = new infer.Context();

infer.withContext(cx, function() {
  var options = {allowHashBang: true};
  try { ast = acorn.parse(code, options); }
  catch(e) {
    // uncomment to enable error tolerant parsing
      //ast = acorn_loose.parse_dammit(code, options);
    console.error("Unable to parse '" + infile + "': " + e);
    process.exit(1);
  }

	infer.analyze(ast, infile);
});

try {
  n2util.addAstTraversalInfo(ast);
} catch (ex) {
  console.error("Skipping '" + infile + "': " + ex);
  process.exit(1);
}

function printobj(o) {
	var str = "";
	for (var k in o) {
		if (str != "") str += ", ";
		var v = o[k];
		try {
			str += k + "=" + v;
		} catch (e) {
			str += k + "=?";
		}
	}
	console.log(str);
}

// if (n2util.isMinified(ast, cx, code)){
//   console.error("Skipping minified file '" + infile);
//   process.exit(1);
// }

try {
  console.log(n2util.outputAst(ast, cx));
} catch (ex) {
  console.error("Skipping '" + infile + "': " + ex);
  process.exit(1);
}

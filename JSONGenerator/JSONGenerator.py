# -*- coding: utf-8 -*-
import simplejson
import json
import sys

class ASTNode:
    def to_json(self):
        return json.dumps(self, default=lambda o: o.__dict__,
                            sort_keys=True, indent=4)

class Program:
    def to_json(self):
        return json.dumps(self, default=lambda o: o.__dict__,
                            sort_keys=True, indent=4)           


def postprocess(nodes):
    
    # set parents for nodes
    for key in nodes.keys():
        currNode = nodes[key]
        if currNode.children != "":
            children = [int(i.lstrip()) for i in currNode.children[1:-1].split(",")]
            #print(children)
            for child in children:
                nodes[child].parent = currNode.id
    
    # set previous node with same value
    for curr in sorted(nodes.keys(), reverse=True):
        if nodes[curr].value == "":
            nodes[curr].previous_id = ""
            continue
        for search in reversed(range(0,curr)):
            if nodes[search].value == nodes[curr].value:
                #print(str(search) + " " + str(curr))
                nodes[curr].previous_id = search
                break
            nodes[curr].previous_id = ""
            
    # set previous node with same type
    '''
    for curr in sorted(nodes.keys(), reverse=True):
        if nodes[curr].type == "":
            nodes[curr].previous_id_type = ""
            continue
        for search in reversed(range(0,curr)):
            if nodes[search].type == nodes[curr].type:
                #print(str(search) + " " + str(curr))
                nodes[curr].previous_id_type = search
                break
            nodes[curr].previous_id_type = ""
    '''
    # set left, right
    for curr in nodes.keys():
        currNode = nodes[curr];
        if currNode.parent == "":
            nodes[curr].left = ""
            nodes[curr].right = ""
            continue
        
        #children = nodes[currNode.parent].children[1:-1].split(',')
        #children_int = [int(i.lstrip()) for i in children]
        
        
        children = [int(i.lstrip()) for i in nodes[currNode.parent].children[1:-1].split(",")]
        
        ind_curr = children.index(curr)
        
        left = ind_curr-1
        if left < 0:
            nodes[curr].left = ""
        else:
            nodes[curr].left = children[left]
            
        right = ind_curr+1
        if right >= len(children):
            nodes[curr].right = ""  
        else:
            nodes[curr].right = children[right] 
            
    # set prev_leaf  
    for curr in sorted(nodes.keys(),reverse=True):
        for search in reversed(range(0,curr)):
            if nodes[search].children == "":
                nodes[curr].prev_leaf = search
                break
        nodes[curr].prev_leaf = ""
    
    # set next leaf
    for curr in nodes.keys():
        for search in range(curr+1,len(nodes)):
            if nodes[search].children == "":
                nodes[curr].next_leaf = search
                break
        nodes[curr].next_leaf = ""
    
    return nodes

if __name__ == '__main__':
    programs = ["programs1.json", "programs2.json", "programs3.json", "programs4.json", "programs5.json"]
    for program in programs:
        with open(program, "r") as f:
            nodes = {}
            data = simplejson.loads(f.read())
            for element in data:
                if isinstance(element, int):
                    prog = Program()
                    prog.program_id = str(element)
                    continue
	            
                node = ASTNode()
                if 'id' in element:
                    node.id = int(element['id'])
                    
                if 'value' in element: 
                    node.value = str(element['value'])
                else:
                    node.value = ""
                             
                if 'type' in element:
                    node.type = str(element['type'])
                else:
                    node.type = ""
                        
                if 'children' in element:
                    node.children = str(element['children'])
                else:
                    node.children = ""
                
                node.parent = ""
                nodes[node.id] = node
            
            # Postprocess node data (insert parent, previous_id, previous_leaf, next_leaf,
            #                           left, right)

            nodes = postprocess(nodes)
            nodes[0] = prog
            
            lst_nodes = list()
            for key in sorted(nodes.keys()):
                lst_nodes.append(nodes[key])
                #lst_nodes.append(json.dumps(nodes[key].__dict__))
            
            json_output = json.dumps([el.__dict__ for el in lst_nodes])
            
            out_file = open(program[:-5]+"_augmented.json", "w")
            out_file.write(json_output)
            out_file.close()

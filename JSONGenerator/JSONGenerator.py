# -*- coding: utf-8 -*-
import simplejson
import json
import sys
import csv
def put(data, filename):
	try:
		jsondata = simplejson.dumps(data, indent=4, skipkeys=True, sort_keys=True)
		fd = open(filename, 'w')
		fd.write(jsondata)
		fd.close()
	except:
		print 'ERROR writing', filename
		pass

def get(filename):
	returndata = {}
	try:
		fd = open(filename, 'r')
		text = fd.read()
		fd.close()
		returndata = json.read(text)
		# Hm.  this returns unicode keys...
		#returndata = simplejson.loads(text)
	except: 
		print 'COULD NOT LOAD:', filename
	return returndata

if __name__ == '__main__':
        # get("hello.txt")
    with open("programs.json", "r") as f:
        jsonoutput = list()
           
        #data = [ { 'a' : 1, 'b' : 2, 'c' : 3, 'd' : 4, 'e' : 5 } ]
        jsonoutput.append("\"nodes\": [")
        L = list()
        kids = list()
        kiddies = 0
        index = 0
        data = simplejson.loads(f.read())
        for element in data:
            if isinstance(element, int):
	            jsonoutput[:0]="{\"program_id\": {\"value\": \"" + str(element)+ "\"},"
	            continue
            jsonoutput.append("{ ")
	        
            if 'id' in element:
                jsonoutput.append("\"id\":" +  str(element['id'])+ ",")
                
            if 'value' in element: 
                jsonoutput.append("\"value\":" + "\"" + element['value'] + "\",")
                currentid = int(element['id'])
                cnt = len(L)                            
                   
                while cnt > 0:
                    if (str(L[cnt-1]) == str(element['value'])):
                        jsonoutput.append("\"previous_id\":" + "\"" + str(cnt-1) + "\",")                                    
                        break
                    cnt = cnt-1
                    
                    if cnt == 0:
                        jsonoutput.append("\"previous_id\": \"\",")     

                L.insert(index, element['value'])
                index = index + 1 
                         
            if 'value' not in element:   
               L.insert(index, "")
               jsonoutput.append("\"value\":" + "\"\",")
               jsonoutput.append("\"previous_id\":" + "\"\",")
               index = index + 1 
                         
            if 'type' in element:
                print "Type: " + str(element['type'])
                jsonoutput.append("\"type\":" + "\"" + str(element['type'])+ "\",")
            if 'type' not in element:
                jsonoutput.append("\"type\":" + "\"" + "\"\",")
                    
            if 'children' in element:
                print "Children: " + str(element['children'])
                jsonoutput.append("\"children\":" + "\"" + str(element['children'])+ "\",")
                
                kids.insert(kiddies, element['children']) 
                kiddies = kiddies + 1
            if 'children' not in element:
                jsonoutput.append("\"children\":" + "\"\",")
                kids.insert(kiddies, "") 
                kiddies = kiddies + 1
            success = 0
            kidindex = 0
            for kiddata in kids:
                for kidarray in kiddata:
                    
                    if (int(element['id'])==int(kidarray)) and kiddata!="":
                        print "Parent: " + str(kidindex)
                        success = 1
                        jsonoutput.append("\"parent\":"  + str(kidindex))
                kidindex = kidindex + 1
                
            if success == 0:
                jsonoutput.append("\"parent\":"  + "\"\"")
                
            jsonoutput.append(" }, ")
        #jsonoutput.append("{}")
        #jsonoutput.append("]}")
        strJsonoutput = ''.join(jsonoutput).replace('\n','')
        strJsonoutput = strJsonoutput[:-2]
        strJsonoutput += "]}"
        text_file = open("programs_augmented.json", "w")
        text_file.write(strJsonoutput)
        text_file.close()
  
        with open('some.csv', 'wb') as f:
            writer = csv.writer(f)

 # [ { "id":0, "type":"Program", "children":[1,6,11,16,28,40,42,52,72] }, { "id":1, "type":"VariableDeclaration", "children":[2] }, 
   #put("data","a.json")

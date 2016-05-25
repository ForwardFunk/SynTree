# -*- coding: utf-8 -*-
import simplejson
import json
import sys
if __name__ == '__main__':
	  # get("hello.txt")
	   with open("JSONPAS.txt", "r") as f:
	        jsonoutput = list()
	        data = simplejson.loads(f.read())
	        for element in data:
	            if 'previous_id' in element:
	               print element['previous_id']
	        
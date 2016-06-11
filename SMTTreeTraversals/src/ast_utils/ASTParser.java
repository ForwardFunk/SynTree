package ast_utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class ASTParser {

	private static final String SPLIT_BY = ",";
	private static final String DEFAULT_FILE_LOC = "programs_augmented.json";
	
	private static final String program_id = "program_id";
	private static final String value = "value";
	private static final String nodes = "nodes";
	private static final String nodeIdx = "id";
	private static final String parentIdx = "parent";
	private static final String prevLeafIdx = "prev_leaf";
	private static final String nextLeafIdx = "next_leaf";
	private static final String leftIdx = "left";
	private static final String rightIdx = "right";
	private static final String prevNodeValueIdx = "previous_id";
	//private static final String prevNodeTypeIdx = "previous_id_type";
	private static final String childrenIdx = "children";
	private static final String type = "type";
	
	
	HashMap<Integer, HashMap<Integer, ASTNode>> parse(String fileLoc) {
		if (fileLoc.equals("")) {
			fileLoc = DEFAULT_FILE_LOC;
		}

		HashMap<Integer, HashMap<Integer, ASTNode>> astStore = new HashMap<>();
		BufferedReader jsonReader = null;
		try {
			jsonReader = new BufferedReader(new FileReader(fileLoc));
		    
			String jsonLine = jsonReader.readLine();
			if (!jsonLine.equals("")) {
				// if JSON file wasn't empty, parse data
				JsonParser parser = new JsonParser();

				
				JsonElement jElement = parser.parse(jsonLine);
				JsonArray mainArray = jElement.getAsJsonArray();
				JsonObject jTreeIdx = mainArray.get(0).getAsJsonObject();
				int currTreeIdx = jTreeIdx.get(program_id).getAsInt();
				astStore.put(currTreeIdx, new HashMap<Integer, ASTNode>());
				
				for (int i = 1; i < mainArray.size(); i++) {
					JsonObject ndObject = mainArray.get(i).getAsJsonObject();
					
					Integer ndIdx = ndObject.get(nodeIdx).getAsInt();
					
					String ndParentIdxStr = ndObject.get(parentIdx).getAsString();
					Integer ndParentIdx = ndParentIdxStr.equals("") ? -1 : Integer.parseInt(ndParentIdxStr);
					
					String ndPrevValueIdxStr = ndObject.get(prevNodeValueIdx).getAsString();
					Integer ndPrevValueIdx = ndPrevValueIdxStr.equals("") ? -1 : Integer.parseInt(ndPrevValueIdxStr);
					
					/*String ndPrevTypeIdxStr = ndObject.get(prevNodeTypeIdx).getAsString();
					Integer ndPrevTypeIdx = ndPrevTypeIdxStr.equals("") ? -1 : Integer.parseInt(ndPrevTypeIdxStr);*/
					
					String ndPrevLeafIdxStr = ndObject.get(prevLeafIdx).getAsString();
					Integer ndPrevLeafIdx = ndPrevLeafIdxStr.equals("") ? -1 : Integer.parseInt(ndPrevValueIdxStr);
					
					String ndNextLeafIdxStr = ndObject.get(nextLeafIdx).getAsString();
					Integer ndNextLeafIdx = ndNextLeafIdxStr.equals("") ? -1 : Integer.parseInt(ndNextLeafIdxStr);
					
					String ndLeftIdxStr = ndObject.get(leftIdx).getAsString();
					Integer ndLeftIdx = ndLeftIdxStr.equals("") ? -1 : Integer.parseInt(ndLeftIdxStr);
					
					String ndRightIdxStr = ndObject.get(rightIdx).getAsString();
					Integer ndRightIdx = ndRightIdxStr.equals("") ? -1 : Integer.parseInt(ndRightIdxStr);
					
					String ndValue = ndObject.get(value).getAsString();
					
					String ndType = ndObject.get(type).getAsString();
					
					String ndChildrenStr = ndObject.get(childrenIdx).getAsString();
					Integer[] ndChildren = null;
					if (!ndChildrenStr.equals("")) {
						String[] ndChildrenStrSplit = ndChildrenStr.substring(1,ndChildrenStr.length()-1).replaceAll("\\s+","").split(",");
						ndChildren = new Integer[ndChildrenStrSplit.length];	
						for (int j = 0; j < ndChildrenStrSplit.length; j++) {
							ndChildren[j] = Integer.parseInt(ndChildrenStrSplit[j]);
						}	
					} else {
						ndChildren = new Integer[0];
					}
					
					astStore.get(currTreeIdx).put(ndIdx, new ASTNode(ndIdx, ndParentIdx, ndPrevLeafIdx, ndNextLeafIdx, ndLeftIdx, ndRightIdx, ndPrevValueIdx, /*ndPrevTypeIdx,*/ ndChildren, ndType, ndValue));
				}
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (jsonReader != null) {
				try {
					jsonReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return astStore;	
	}
}

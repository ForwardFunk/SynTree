package ast_utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ASTStore {

	private HashMap<Integer, HashMap<Integer, ASTNode>> store;
	private int treeIdx;
	
	public ASTStore(String fileLoc) {
		store = new HashMap<>();
		init(fileLoc);
	}
	
	public void init(String fileLoc) {
		ASTParser parser = new ASTParser();
		store = parser.parse(fileLoc);
	}
	
	public void setTreeIdx(int treeIdx) {
		this.treeIdx = treeIdx;
	}

	public Integer getNdIdx(Integer ndIdx) {
		ASTNode res = store.get(treeIdx).get(ndIdx);
		if (res == null)
			return -1;
		else
			return res.ndIdx;
	}
	
	public Integer getNdParentIdx(Integer ndIdx) {
		ASTNode res = store.get(treeIdx).get(ndIdx);
		if (res == null)
			return -1;
		else
			return res.ndParentIdx;
	}
	
	public Integer getNdPrevLeafIdx(Integer ndIdx) {
		ASTNode res = store.get(treeIdx).get(ndIdx);
		if (res == null)
			return -1;
		else
			return res.ndPrevLeaf;
	}
	public Integer getNdNextLeafIdx(Integer ndIdx) {
		ASTNode res = store.get(treeIdx).get(ndIdx);
		if (res == null)
			return -1;
		else
			return res.ndNextLeaf;
	}
	public Integer getNdLeftIdx(Integer ndIdx) {
		ASTNode res = store.get(treeIdx).get(ndIdx);
		if (res == null)
			return -1;
		else
		return res.ndLeft;
	}
	public Integer getNdRightIdx(Integer ndIdx) {
		ASTNode res = store.get(treeIdx).get(ndIdx);
		if (res == null)
			return -1;
		else
		return res.ndRight;
	}
	
	public Integer[] getNdChildrenIdx(Integer ndIdx) {
		ASTNode res = store.get(treeIdx).get(ndIdx);
		if (res == null)
			return new Integer[0];
		else
			return res.ndChildrenIdx;
	}
	
	public Integer getNdDownFirstIdx(Integer ndIdx) {
		Integer[] children = store.get(treeIdx).get(ndIdx).ndChildrenIdx;
		if (children.length == 0)
			return -1;
		else
			return children[0];
	}
	
	public Integer getNdDownLastIdx(Integer ndIdx) {
		Integer[] children = store.get(treeIdx).get(ndIdx).ndChildrenIdx;
		if (children.length == 0)
			return -1;
		else
			return children[children.length-1];
	}
	
	
	public String getNdType(Integer ndIdx) {
		ASTNode res = store.get(treeIdx).get(ndIdx);
		if (res == null)
			return "";
		else
			return res.ndType;
	}
	
	public String getNdValue(Integer ndIdx) {
		ASTNode res = store.get(treeIdx).get(ndIdx);
		if (res == null)
			return "";
		else
			return res.ndValue;
	}
	
	public Integer getNdPrevValue(Integer ndIdx) {
		ASTNode res = store.get(treeIdx).get(ndIdx);
		if (res == null)
			return -1;
		else
			return res.ndPrevValueIdx;
	}
	
	/*public Integer getNdPrevType(Integer ndIdx) {
		ASTNode res = store.get(treeIdx).get(ndIdx);
		if (res == null)
			return -1;
		else
			return res.ndPrevTypeIdx;
	}*/

	public Iterator<Map.Entry<Integer, ASTNode>> getNdIterator() {
		return store.get(treeIdx).entrySet().iterator();
	}
}

package ast_utils;

import java.util.HashMap;
import java.util.Iterator;

public class ASTStore {

	private HashMap<Integer, ASTNode> store;
	
	public ASTStore(String fileLoc, int treeIdx) {
		store = new HashMap<Integer, ASTNode>();
		init(fileLoc, treeIdx);
	}
	
	public void init(String fileLoc, int treeIdx) {
		ASTParser parser = new ASTParser();
		store = parser.parse(fileLoc, treeIdx);
	}
	

	public Integer getNdIdx(Integer ndIdx) {
		return store.get(ndIdx).ndIdx;
	}
	
	public Integer getNdParentIdx(Integer ndIdx) {
		return store.get(ndIdx).ndParentIdx;
	}
	
	public Integer getNdPrevLeafIdx(Integer ndIdx) {
		return store.get(ndIdx).ndPrevLeaf;
	}
	public Integer getNdNextLeafIdx(Integer ndIdx) {
		return store.get(ndIdx).ndNextLeaf;
	}
	public Integer getNdLeftIdx(Integer ndIdx) {
		return store.get(ndIdx).ndLeft;
	}
	public Integer getNdRightIdx(Integer ndIdx) {
		return store.get(ndIdx).ndRight;
	}
	
	public Integer[] getNdChildrenIdx(Integer ndIdx) {
		return store.get(ndIdx).ndChildrenIdx;
	}
	
	public Integer getNdDownFirstIdx(Integer ndIdx) {
		Integer[] children = store.get(ndIdx).ndChildrenIdx;
		if (children.length == 0)
			return -1;
		else
			return children[0];
	}
	
	public Integer getNdDownLastIdx(Integer ndIdx) {
		Integer[] children = store.get(ndIdx).ndChildrenIdx;
		if (children.length == 0)
			return -1;
		else
			return children[children.length-1];
	}
	
	
	public String getNdType(Integer ndIdx) {
		return store.get(ndIdx).ndType;
	}
	
	public String getNdValue(Integer ndIdx) {
		return store.get(ndIdx).ndValue;
	}
	
	public Integer getNdPrevValue(Integer ndIdx) {
		return store.get(ndIdx).ndPrevValueIdx;
	}
	
	public Iterator getNdIterator() {
		return store.entrySet().iterator();
	}
}

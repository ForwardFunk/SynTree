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
	
	public Integer getNdParentIdx(Integer ndIdx) {
		return store.get(ndIdx).ndParentIdx;
	}
	
	public Integer[] getNdChildrenIdx(Integer ndIdx) {
		return store.get(ndIdx).ndChildrenIdx;
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
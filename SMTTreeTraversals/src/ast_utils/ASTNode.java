package ast_utils;

class ASTNode {
	public Integer ndIdx;
	public Integer ndParentIdx;
	public Integer ndPrevValueIdx;
	public Integer[] ndChildrenIdx;
	public String ndType;
	public String ndValue;
	
	ASTNode(Integer ndIdx, Integer ndParentIdx, Integer ndPrevValueIdx, Integer[] ndChildrenIdx, String ndType, String ndValue) {
		this.ndIdx = ndIdx;
		this.ndParentIdx = ndParentIdx;
		this.ndPrevValueIdx = ndPrevValueIdx;
		this.ndChildrenIdx = ndChildrenIdx;
		this.ndType = ndType;
		this.ndValue = ndValue;
	}		
}

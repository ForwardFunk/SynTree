package ast_utils;

class ASTNode {
	public Integer ndIdx;
	public Integer ndParentIdx;
	public Integer ndPrevLeaf;
	public Integer ndNextLeaf;
	public Integer ndLeft;
	public Integer ndRight;
	public Integer ndPrevValueIdx;
	//public Integer ndPrevTypeIdx;
	public Integer[] ndChildrenIdx;
	public String ndType;
	public String ndValue;
	
	ASTNode(Integer ndIdx, Integer ndParentIdx, Integer ndPrevLeaf,
			Integer ndNextLeaf, Integer ndLeft, Integer ndRight,
			Integer ndPrevValueIdx, /*Integer ndPrevTypeIdx, */Integer[] ndChildrenIdx, String ndType,
			String ndValue) {
		this.ndIdx = ndIdx;
		this.ndParentIdx = ndParentIdx;
		this.ndPrevLeaf = ndPrevLeaf;
		this.ndNextLeaf = ndNextLeaf;
		this.ndLeft = ndLeft;
		this.ndRight = ndRight;
		this.ndPrevValueIdx = ndPrevValueIdx;
		this.ndChildrenIdx = ndChildrenIdx;
		this.ndType = ndType;
		this.ndValue = ndValue;
		//this.ndPrevTypeIdx = ndPrevTypeIdx;
	}

}

package syn_core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ast_utils.ASTStore;

import com.microsoft.z3.*;

/*
 * NOTE: On every add of a new DSL operation:
 * 	- update opCnt
 * 	- add definition as a private method,
 *  - add case to getDSLOp and update op code constants
 */

public class DSLHelper {

	public static final int OP_CNT = 9;
	
	public static final int OP_UP = 0;
	public static final int OP_DOWN_FIRST = 1;
	public static final int OP_DOWN_LAST = 2;
	public static final int OP_PREV_NODE_VAL = 3;
	public static final int OP_LEFT = 4;	
	public static final int OP_RIGHT = 5;	
	public static final int OP_NOP = 6;
	public static final int OP_PREV_LEAF = 7;	
	public static final int OP_NEXT_LEAF = 8;	
	//public static final int OP_PREV_NODE_TYPE = 9;
	
	private static ArrayExpr arrUp;
	private static ArrayExpr arrDownFirst;
	private static ArrayExpr arrDownLast;
	private static ArrayExpr arrPrevNodeVal;
	private static ArrayExpr arrPrevLeaf;
	private static ArrayExpr arrNextLeaf;
	private static ArrayExpr arrLeft;
	private static ArrayExpr arrRight;
	//private static ArrayExpr arrPrevNodeType;
	
	
	// Will be called to get operation definitions,
	// during generation of the Synthesis formula
	public static Expr getDSLOp(int opIdx, IntExpr srcNdIdx, IntExpr dstNdIdx, ASTStore astStore, Context z3Ctx, boolean efficientLookup) {
		Expr expr = null;
		switch (opIdx) {
		case OP_NOP:
			expr = nop(srcNdIdx, dstNdIdx, z3Ctx);
			break;
		default:
			if (!efficientLookup) // code DSL invocations as array accesses
				expr = (Expr) mkNestedITE(opIdx, astStore, srcNdIdx, dstNdIdx, astStore.getNdIterator(), z3Ctx);
			else // display a HashMap using ITE for every DSL op invocation
				expr = (Expr) mkArrayedLookup(opIdx, astStore, srcNdIdx, dstNdIdx, z3Ctx);
				//expr = mkMacroLookup(opIdx, astStore, srcNdIdx, dstNdIdx, z3Ctx);
			break;
		}
		 
		return expr;
	}
	
	public static BoolExpr initDSLArrays(ASTStore astStore, Context z3Ctx) {
		Sort intType = z3Ctx.mkIntSort();
		
		arrUp = z3Ctx.mkArrayConst("arrUp", intType, intType);
		arrDownFirst = z3Ctx.mkArrayConst("arrDownFirst", intType, intType);
		arrDownLast = z3Ctx.mkArrayConst("arrDownLast", intType, intType);
		arrPrevNodeVal = z3Ctx.mkArrayConst("arrPrevNodeVal", intType, intType);
		//arrPrevNodeType = z3Ctx.mkArrayConst("arrPrevNodeType", intType, intType);
		arrPrevLeaf = z3Ctx.mkArrayConst("arrPrevLeaf", intType, intType);
		arrNextLeaf = z3Ctx.mkArrayConst("arrNextLeaf", intType, intType);
		arrLeft = z3Ctx.mkArrayConst("arrLeft", intType, intType);
		arrRight = z3Ctx.mkArrayConst("arrRight", intType, intType);
		
		Iterator it = astStore.getNdIterator();		
		
		
		BoolExpr result = null;
		
		//ArrayList<BoolExpr> result = new ArrayList<>();
		while (it.hasNext()) {

			Map.Entry entry = (Map.Entry)it.next();		
			Integer srcNd = (Integer) entry.getKey();

			BoolExpr stArrUp = z3Ctx.mkEq(z3Ctx.mkSelect(arrUp, z3Ctx.mkInt(srcNd)), z3Ctx.mkInt(astStore.getNdParentIdx(srcNd)));
			BoolExpr stArrDownFirst = z3Ctx.mkEq(z3Ctx.mkSelect(arrDownFirst, z3Ctx.mkInt(srcNd)), z3Ctx.mkInt(astStore.getNdDownFirstIdx(srcNd)));
			BoolExpr stArrDownLast = z3Ctx.mkEq(z3Ctx.mkSelect(arrDownLast, z3Ctx.mkInt(srcNd)), z3Ctx.mkInt(astStore.getNdDownLastIdx(srcNd)));
			BoolExpr stArrPrevNodeVal = z3Ctx.mkEq(z3Ctx.mkSelect(arrPrevNodeVal, z3Ctx.mkInt(srcNd)), z3Ctx.mkInt(astStore.getNdPrevValue(srcNd)));
			//BoolExpr stArrPrevNodeType = z3Ctx.mkEq(z3Ctx.mkSelect(arrPrevNodeType, z3Ctx.mkInt(srcNd)), z3Ctx.mkInt(astStore.getNdPrevType(srcNd)));
			BoolExpr stArrPrevLeaf = z3Ctx.mkEq(z3Ctx.mkSelect(arrPrevLeaf, z3Ctx.mkInt(srcNd)), z3Ctx.mkInt(astStore.getNdPrevLeafIdx(srcNd)));
			BoolExpr stArrNextLeaf = z3Ctx.mkEq(z3Ctx.mkSelect(arrNextLeaf, z3Ctx.mkInt(srcNd)), z3Ctx.mkInt(astStore.getNdNextLeafIdx(srcNd)));
			BoolExpr stArrLeft = z3Ctx.mkEq(z3Ctx.mkSelect(arrLeft, z3Ctx.mkInt(srcNd)), z3Ctx.mkInt(astStore.getNdLeftIdx(srcNd)));
			BoolExpr stArrRight = z3Ctx.mkEq(z3Ctx.mkSelect(arrRight, z3Ctx.mkInt(srcNd)), z3Ctx.mkInt(astStore.getNdRightIdx(srcNd)));
			
			if (result == null)
				result = z3Ctx.mkAnd((BoolExpr) stArrUp, (BoolExpr) stArrDownFirst, (BoolExpr) stArrDownLast, (BoolExpr) stArrPrevNodeVal, /*(BoolExpr) stArrPrevNodeType, */
						(BoolExpr) stArrPrevLeaf, (BoolExpr) stArrNextLeaf, (BoolExpr) stArrLeft, (BoolExpr) stArrRight);
			else
				result = z3Ctx.mkAnd(result, (BoolExpr) stArrUp, (BoolExpr) stArrDownFirst, (BoolExpr) stArrDownLast, (BoolExpr) stArrPrevNodeVal, /*(BoolExpr) stArrPrevNodeType,*/
					(BoolExpr) stArrPrevLeaf, (BoolExpr) stArrNextLeaf, (BoolExpr) stArrLeft, (BoolExpr) stArrRight);
		}
		return result;
	}
	
	private static Expr mkDSLOpITE(Context z3Ctx, int opCode, Expr opVar, Iterator it, ASTStore astStore) {
		HashMap.Entry pair = (HashMap.Entry) it.next();
		Integer srcNdIdx = (Integer) pair.getKey();
		BoolExpr cond = z3Ctx.mkEq(opVar, z3Ctx.mkInt(srcNdIdx));
		
		Expr tBranch = null;
		switch (opCode) {
		case OP_UP:
			tBranch = z3Ctx.mkInt(astStore.getNdParentIdx(srcNdIdx));
			break;
		case OP_DOWN_FIRST:
			tBranch = z3Ctx.mkInt(astStore.getNdDownFirstIdx(srcNdIdx));
			break;
		case OP_DOWN_LAST:
			tBranch = z3Ctx.mkInt(astStore.getNdDownLastIdx(srcNdIdx));
			break;
		case OP_PREV_NODE_VAL:
			tBranch = z3Ctx.mkInt(astStore.getNdPrevValue(srcNdIdx));
			break;
		case OP_PREV_LEAF:
			tBranch = z3Ctx.mkInt(astStore.getNdPrevLeafIdx(srcNdIdx));
			break;
		case OP_NEXT_LEAF:
			tBranch = z3Ctx.mkInt(astStore.getNdNextLeafIdx(srcNdIdx));
			break;
		case OP_LEFT:
			tBranch = z3Ctx.mkInt(astStore.getNdLeftIdx(srcNdIdx));
			break;
		case OP_RIGHT:
			tBranch = z3Ctx.mkInt(astStore.getNdRightIdx(srcNdIdx));
			break;
		case OP_NOP:
			tBranch = z3Ctx.mkInt(srcNdIdx);
		/*case OP_PREV_NODE_TYPE:
			tBranch = z3Ctx.mkInt(astStore.getNdPrevType(srcNdIdx));
			break;*/
		}
		
		if (!it.hasNext()) {
			Expr fBranch = z3Ctx.mkInt(astStore.getNdRightIdx(srcNdIdx));
			return z3Ctx.mkITE(cond, tBranch, fBranch);
		} else {
			Expr fBranch = mkDSLOpITE(z3Ctx, opCode, opVar, it, astStore);
			return z3Ctx.mkITE(cond, tBranch, fBranch);
		}
		
	}
	
	public static String decodeDSLOp(int opCode) {
		switch(opCode) {
		case OP_UP:
			return "Up";
		case OP_DOWN_FIRST:
			return "DownFirst";
		case OP_DOWN_LAST:
			return "DownLast";
		case OP_PREV_NODE_VAL:
			return "PrevNodeVal";
		/*case OP_PREV_NODE_TYPE:
			return "PrevNodeType";*/
		case OP_PREV_LEAF:
			return "PrevLeaf";
		case OP_NEXT_LEAF:
			return "NextLeaf";
		case OP_LEFT:
			return "Left";
		case OP_RIGHT:
			return "Right";
		case OP_NOP:
			return "Nop";
		default:
			return "UNKNOWN_DSL_OP";
		}
	}
	
	private static Expr mkArrayedLookup(int opCode, ASTStore astStore, IntExpr srcVar, IntExpr dstVar, Context z3Ctx) {
		Expr selExpr = null;
		switch(opCode) {
		case OP_UP:
			selExpr = z3Ctx.mkSelect(arrUp, srcVar); 
			break;
		case OP_DOWN_FIRST:
			selExpr = z3Ctx.mkSelect(arrDownFirst, srcVar);
			break;
		case OP_DOWN_LAST:
			selExpr = z3Ctx.mkSelect(arrDownLast, srcVar);
			break;
		case OP_PREV_NODE_VAL:
			selExpr = z3Ctx.mkSelect(arrPrevNodeVal, srcVar);
			break;
		/*case OP_PREV_NODE_TYPE:
			selExpr = z3Ctx.mkSelect(arrPrevNodeType, srcVar);
			break;*/
		case OP_PREV_LEAF:
			selExpr = z3Ctx.mkSelect(arrPrevLeaf, srcVar);
			break;
		case OP_NEXT_LEAF:
			selExpr = z3Ctx.mkSelect(arrNextLeaf, srcVar);
			break;
		case OP_LEFT:
			selExpr = z3Ctx.mkSelect(arrLeft, srcVar);
			break;
		case OP_RIGHT:
			selExpr = z3Ctx.mkSelect(arrRight, srcVar);
			break;
		}
		
		return z3Ctx.mkITE(z3Ctx.mkEq(srcVar, z3Ctx.mkInt(-1)), z3Ctx.mkEq(dstVar, z3Ctx.mkInt(-1)), z3Ctx.mkEq(dstVar, selExpr));
	}
	
	/*private static Expr mkMacroLookup(int opCode, ASTStore astStore, IntExpr srcVar, IntExpr dstVar, Context z3Ctx) {
		Expr selExpr = null;
		switch(opCode) {
		case OP_UP:
			selExpr = z3Ctx.mkApp(funUp, srcVar); 
			break;
		case OP_DOWN_FIRST:
			selExpr = z3Ctx.mkApp(funDownFirst, srcVar);
			break;
		case OP_DOWN_LAST:
			selExpr = z3Ctx.mkApp(funDownLast, srcVar);
			break;
		case OP_PREV_NODE_VAL:
			selExpr = z3Ctx.mkApp(funPrevNodeVal, srcVar);
			break;
		case OP_PREV_LEAF:
			selExpr = z3Ctx.mkApp(funPrevLeaf, srcVar);
			break;
		case OP_NEXT_LEAF:
			selExpr = z3Ctx.mkApp(funNextLeaf, srcVar);
			break;
		case OP_LEFT:
			selExpr = z3Ctx.mkApp(funLeft, srcVar);
			break;
		case OP_RIGHT:
			selExpr = z3Ctx.mkApp(funRight, srcVar);
			break;
		}
		
		return z3Ctx.mkEq(dstVar, selExpr);
	}*/
	
	private static Expr nop(IntExpr srcVar, IntExpr dstVar, Context z3Ctx) {
		return z3Ctx.mkEq(dstVar, srcVar);
	}
	
	private static Expr mkNestedITE(int opCode, ASTStore astStore, IntExpr srcVar, IntExpr dstVar, Iterator it, Context z3Ctx) {
		
		HashMap.Entry pair = (HashMap.Entry) it.next();
		Integer srcNdIdx = (Integer) pair.getKey();
		BoolExpr cond = z3Ctx.mkEq(srcVar, z3Ctx.mkInt(srcNdIdx));
		
		int dstNdVal = -1;
		switch (opCode) {
		case OP_UP:
			dstNdVal = astStore.getNdParentIdx(srcNdIdx);
			break;
		case OP_DOWN_FIRST:
			/*Integer[] children1 = astStore.getNdChildrenIdx(srcNdIdx);
			if (children1.length == 0) {
				dstNdVal = -1;
			} else {
				dstNdVal = children1[0];
			}*/
			dstNdVal = astStore.getNdDownFirstIdx(srcNdIdx);
			break;
		case OP_DOWN_LAST:
			/*Integer[] children2 = astStore.getNdChildrenIdx(srcNdIdx);
			if (children2.length == 0) {
				dstNdVal = -1;
			} else {
				dstNdVal = children2[children2.length-1];
			}*/
			dstNdVal = astStore.getNdDownLastIdx(srcNdIdx);
			break;
		case OP_PREV_NODE_VAL:
			dstNdVal = astStore.getNdPrevValue(srcNdIdx);
			break;
		/*case OP_PREV_NODE_TYPE:
			dstNdVal = astStore.getNdPrevType(srcNdIdx);
			break;*/
		case OP_PREV_LEAF:
			dstNdVal = astStore.getNdPrevLeafIdx(srcNdIdx);
			break;
		case OP_NEXT_LEAF:
			dstNdVal = astStore.getNdNextLeafIdx(srcNdIdx);
			break;
		case OP_LEFT:
			dstNdVal = astStore.getNdLeftIdx(srcNdIdx);
			break;
		case OP_RIGHT:
			dstNdVal = astStore.getNdRightIdx(srcNdIdx);
			break;
		case OP_NOP:
			dstNdVal = srcNdIdx;
			break;
		}
		
		Expr tBranch = z3Ctx.mkEq(dstVar, z3Ctx.mkInt(dstNdVal));
		if (!it.hasNext()) {
			//it.remove();
			Expr fBranch = z3Ctx.mkEq(dstVar, z3Ctx.mkInt(-1));
			return z3Ctx.mkITE(cond, tBranch, fBranch);
		} else {
			//it.remove();
			Expr fBranch = mkNestedITE(opCode, astStore, srcVar, dstVar, it, z3Ctx);
			return z3Ctx.mkITE(cond, tBranch, fBranch);
		}
	}
	
	public static Integer applyDSLOp(Integer srcNdIdx, Integer opInd, ASTStore astStore) {
		switch (opInd) {
		case OP_UP:
			return astStore.getNdParentIdx(srcNdIdx);
		case OP_DOWN_FIRST:
			Integer[] children1 = astStore.getNdChildrenIdx(srcNdIdx);
			if (children1.length == 0) {
				return -1;
			} else {
				return children1[0];
			}
		case OP_DOWN_LAST:
			Integer[] children2 = astStore.getNdChildrenIdx(srcNdIdx);
			if (children2.length == 0) {
				return -1;
			} else {
				return children2[children2.length-1];
			}
		case OP_PREV_NODE_VAL:
			return astStore.getNdPrevValue(srcNdIdx);
		/*case OP_PREV_NODE_TYPE:
			return astStore.getNdPrevType(srcNdIdx);*/
		case OP_PREV_LEAF:
			return astStore.getNdPrevLeafIdx(srcNdIdx);
		case OP_NEXT_LEAF:
			return astStore.getNdNextLeafIdx(srcNdIdx);
		case OP_LEFT:
			return astStore.getNdLeftIdx(srcNdIdx);
		case OP_RIGHT:
			return astStore.getNdRightIdx(srcNdIdx);
		case OP_NOP:
			return srcNdIdx;	
		default:
			return -1;
		}
	}
	
	public static Integer applyDSLSequence(Integer srcNdIdx, ASTStore astStore, Integer... opSequence) {
		Integer currNd = srcNdIdx;
		for (int i = 0; i < opSequence.length; i++) {
			currNd = applyDSLOp(currNd, opSequence[i], astStore);
			if (currNd == -1)
				return -1;
		}
		return currNd;
	}
	
}

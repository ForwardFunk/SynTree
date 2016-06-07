package syn_core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import main.SynMain;
import utils.Pair;
import ast_utils.ASTStore;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

public class SynEngineSmt {

	public static boolean trainProgramSmt(int treeIdx, ASTStore astStore, boolean efficientLookup, boolean branchedTraining, Pair<Integer, Object> branchCondValue, ArrayList<Pair<Integer, Integer>> srcDstPairs) {
		// Toggle model generation on in Z3 solver
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");
		SynContext ctx = new SynContext(cfg, astStore, efficientLookup);
		for (Pair<Integer, Integer> pair : srcDstPairs) {
			ctx.addSrcDstPair(pair);
		}
		
		int opNum = SynMain.startOpNum;
		boolean programFound = false;
		while (!programFound && opNum <= SynMain.maxOpNum) {
			ctx.setOpNum(opNum);
			System.out.println("Training" + (branchedTraining ? " single branch: " : " program: ") + " checking synthesis satisfiability with max op. count: " + opNum);
			try {
				BoolExpr synFormula = ctx.mkSynthesisFormula();
				Solver solve = ctx.mkSolver();
				solve.add(synFormula);
				Status stat = solve.check();
				if (stat == Status.SATISFIABLE) {
					Model mod = solve.getModel();
					if (branchedTraining) {
						SynEngine.branchedModelInterpretation.put(branchCondValue, eliminateDeadCode(ctx.mkModelInterpretation(mod)));
					}
					else {
						SynEngine.modelInterpretation = eliminateDeadCode(ctx.mkModelInterpretation(mod));
					}
					programFound = true;
				} else {		
					opNum+=1;			
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("======");
		ctx.dispose();
		return programFound;
	}
	
	private static TreeMap<Integer, Integer> eliminateDeadCode(TreeMap<Integer, Integer> program) {
		Iterator it = program.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry en = (Entry) it.next();
			Integer dslOp = (Integer) en.getValue();
			if (dslOp == DSLHelper.OP_NOP) {
				it.remove();
			} 
		}
		return program;
	}

}

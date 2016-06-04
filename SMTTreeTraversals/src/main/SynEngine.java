package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import syn_core.BranchClassifier;
import syn_core.DSLHelper;
import syn_core.SynContext;
import utils.Pair;

import ast_utils.ASTStore;

import com.microsoft.z3.*;

public class SynEngine {


	public static TreeMap<Integer, Integer> modelInterpretation = null;
	public static HashMap<Pair<Integer, Object>, TreeMap<Integer, Integer>> branchedModelInterpretation = null;
	
	public static boolean trainProgram(int treeIdx, ASTStore astStore, boolean efficientLookup, boolean branchedTraining, Pair<Integer, Object> branchCondValue, ArrayList<Pair<Integer, Integer>> srcDstPairs) {
		// Toggle model generation on in Z3 solver
		HashMap<String, String> cfg = new HashMap<String, String>();
		cfg.put("model", "true");

		SynContext ctx = new SynContext(cfg, astStore, efficientLookup);
		for (Pair<Integer, Integer> pair : srcDstPairs) {
			ctx.addSrcDstPair(pair);
		}
		
		int opNum = 4;
		boolean programFound = false;
		while (!programFound && opNum <= SynMain.maxDslOpNum) {
			ctx.setOpNum(opNum);
			try {
				BoolExpr synFormula = ctx.mkSynthesisFormula();
				Solver solve = ctx.mkSolver();
				solve.add(synFormula);
				System.out.println("Training phase: Checking SMT of synthesis formula with max DSL op count: " + opNum + "...");
				Status stat = solve.check();
				if (stat == Status.SATISFIABLE) {
					Model mod = solve.getModel();
					if (branchedTraining) {
						branchedModelInterpretation.put(branchCondValue, ctx.mkModelInterpretation(mod));
					}
					else {
						modelInterpretation = ctx.mkModelInterpretation(mod);
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

		ctx.dispose();
		return programFound;
	}
	
	public static boolean trainBranchedProgram(int treeIdx, BranchClassifier bc, ASTStore astStore, boolean efficientLookup, ArrayList<Pair<Integer, Integer>> srcDstPairs) {
		bc.classifyTestPairs();
		branchedModelInterpretation = new HashMap<>();
		
		boolean synAll = true;
		for(int i = 0; i < bc.criteriaCnt; i++) {
			if (bc.isFakeCriterion(i))
				continue;
			ArrayList<ArrayList<Pair<Integer, Integer>>> currSrcDstPairs = bc.getClassification(i);
			for (ArrayList<Pair<Integer, Integer>> branchPairs : currSrcDstPairs) {
				System.out.println(branchPairs);
				synAll = synAll && trainProgram(treeIdx, astStore, efficientLookup, true, new Pair(bc.getCriterion(i), bc.getBranchCondValue(i, branchPairs)), branchPairs);
				if (!synAll) 
					break;
			}
			if (!synAll)
				continue;

			break;
		}
		return synAll;
	}
	/*public static void main(String[] args) {
	Global.ToggleWarningMessages(true);
	
	// Toggle model generation on in Z3 solver
	HashMap<String, String> cfg = new HashMap<String, String>();
	cfg.put("model", "true");
	
	// Parse AST nodes from an augmented JSON file to a store
	String fileLoc = program4;
	int treeIdx = 0;
	boolean efficientLookup = true;
	
	ASTStore store = new ASTStore(fileLoc, treeIdx);
	// Initialize synthesis context (which is also a wrapper for the Z3 context)
	SynContext ctx = new SynContext(cfg, store, efficientLookup);
	int opNum = 5;
	ctx.setOpNum(opNum);
	test4(ctx, store);
	try {
		BoolExpr synFormula = ctx.mkSynthesisFormula();
		
		Solver solve = ctx.mkSolver();//(ctx.mkTactic("macro-finder"));
		//System.out.println(synFormula.toString());
		solve.add(synFormula);
		System.out.println("main: Checking SMT of synthesis formula with max DSL op count: " + opNum + "...");
		Status stat = solve.check();
		if (stat == Status.SATISFIABLE) {
			System.out.println("Following program found:");
			Model mod = solve.getModel();
			//System.out.println(mod.toString());
			TreeMap<Integer, Integer> interp = ctx.mkModelInterpretation(mod);
			Iterator<Map.Entry<Integer, Integer>> it = interp.entrySet().iterator();
			Integer[] opSequence = new Integer[interp.size()];
			int i = 0;
			while (it.hasNext()) {
				Map.Entry<Integer, Integer> curr = it.next();
				System.out.println(curr.getKey().toString() + ": " + DSLHelper.decodeDSLOp((Integer) curr.getValue()));
				opSequence[i] = (Integer) curr.getValue();
				i++;
			}
			System.out.println("Dst="+DSLHelper.applyDSLSequence(24, store, opSequence).toString());
			System.out.println("Dst="+DSLHelper.applyDSLSequence(27, store, opSequence).toString());
			System.out.println("Dst="+DSLHelper.applyDSLSequence(355, store, opSequence).toString());
			System.out.println("Dst="+DSLHelper.applyDSLSequence(381, store, opSequence).toString());
			System.out.println("Dst="+DSLHelper.applyDSLSequence(218, store, opSequence).toString());
			System.out.println("Dst="+DSLHelper.applyDSLSequence(240, store, opSequence).toString());
			System.out.println("Dst="+DSLHelper.applyDSLSequence(286, store, opSequence).toString());
		} else {		
			System.out.println("Cannot find a program that satisfies all given src/dst pairs. Requested DSL op. number: " + opNum);				
		}
		
	} catch (Exception ex) {
		// TODO Auto-generated catch block
		ex.printStackTrace();
	}
	
	
}*/

	/*private static void testSimple(SynContext ctx) {
		ctx.addSrcDstPair(3, 1);
	}
	
	private static void test1(SynContext ctx) {
		ctx.addSrcDstPair(353, 330);
		ctx.addSrcDstPair(379, 353);
		ctx.addSrcDstPair(330, 309);	// different	
	}
	
	private static void test2(SynContext ctx, ASTStore astStore) {
		ArrayList<Pair<Integer, Integer>> sdList = new ArrayList<>();
		sdList.add(new Pair(353,330));
		sdList.add(new Pair(379,353));
		sdList.add(new Pair(330,309));
		sdList.add(new Pair(311,290));
		sdList.add(new Pair(332,311));
		sdList.add(new Pair(355,332));
		sdList.add(new Pair(381,355));
		BranchClassifier bc = new BranchClassifier(astStore, sdList);
		bc.classifyTestPairs();
		
	}
	
	private static void test3(SynContext ctx) {
		ctx.addSrcDstPair(194, 119);
		ctx.addSrcDstPair(284, 194);
		ctx.addSrcDstPair(362, 284);		
	}
	
	private static void test4(SynContext ctx, ASTStore astStore) {
		ArrayList<Pair<Integer, Integer>> sdList = new ArrayList<>();
		sdList.add(new Pair(210,202));
		sdList.add(new Pair(232,224));
		sdList.add(new Pair(286,246));
		sdList.add(new Pair(451,440));
		sdList.add(new Pair(467,456));
		BranchClassifier bc = new BranchClassifier(astStore, sdList);
		HashMap<Pair<Integer, Object>, ArrayList<Pair<Integer, Integer>>> result = bc.classifyTestPairs();
		int a  = 5;

		//easy
		ctx.addSrcDstPair(210, 202);
		ctx.addSrcDstPair(232, 224);
		ctx.addSrcDstPair(286, 246);
		
		// hard
		ctx.addSrcDstPair(451, 440);
		ctx.addSrcDstPair(467, 456);
		
	}
	
	private static void test5(SynContext ctx) {
		// easy
		ctx.addSrcDstPair(24, 21);
		ctx.addSrcDstPair(43, 40);
		ctx.addSrcDstPair(66, 63);
		ctx.addSrcDstPair(91, 88);
		
		// harder (doesnt involve these above)
		// even harder (involves above)

		ctx.addSrcDstPair(27, 24);
		ctx.addSrcDstPair(46, 43);
		ctx.addSrcDstPair(49, 46);
		ctx.addSrcDstPair(69, 66);
		ctx.addSrcDstPair(72, 69);
		ctx.addSrcDstPair(75, 72);
		ctx.addSrcDstPair(94, 91);
		
	}*/

}

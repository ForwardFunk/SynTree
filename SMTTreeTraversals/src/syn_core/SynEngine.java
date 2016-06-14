package syn_core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import main.SynMain;

import utils.Pair;

import ast_utils.ASTStore;

import com.microsoft.z3.*;

public class SynEngine {

	
	public static TreeMap<Integer, Integer> modelInterpretation = null;
	public static HashMap<Pair<Integer, Object>, TreeMap<Integer, Integer>> branchedModelInterpretation = null;
	private static boolean baseline = false;
	

	public static boolean trainProgram(int treeIdx, ASTStore astStore, boolean efficientLookup, boolean branchedTraining, Pair<Integer, Object> branchCondValue, ArrayList<Pair<Integer, Integer>> srcDstPairs) {
		if (baseline) {
			return SynEngineBaseline.trainProgramBaseline(treeIdx, astStore, branchedTraining, branchCondValue, srcDstPairs);
		} else {
			return SynEngineSmt.trainProgramSmt(treeIdx, astStore, efficientLookup, branchedTraining, branchCondValue, srcDstPairs);
			
		}
	}
	
	public static boolean trainBranchedProgram(int treeIdx, BranchClassifier bc, ASTStore astStore, boolean efficientLookup, ArrayList<Pair<Integer, Integer>> srcDstPairs) {
		bc.classifyTestPairs();
		branchedModelInterpretation = new HashMap<>();
		
		boolean synAll = true;
		for(int i = 0; i < bc.criteriaCnt; i++) {
			if (!SynMain.statsOnly && !SynMain.resultsOnly) {
				System.out.println("Training branched: Getting classification according to criteria -> " + bc.getCriterionName(i));
			}
			if (bc.isFakeCriterion(i)) {
				if (!SynMain.statsOnly && !SynMain.resultsOnly) {
					System.out.println("Training branched: " + bc.getCriterionName(i) + " is false criterion... (all pairs are within one branch)");
					System.out.println("======");
				}
				// no criteria can be used
				if (i == bc.criteriaCnt-1)
					return false;
				
				continue;
			}

			if (!SynMain.statsOnly && !SynMain.resultsOnly) {
				System.out.println("Training branched: " + bc.getCriterionName(i) + " classification successful!");
				System.out.println();
			}
			ArrayList<ArrayList<Pair<Integer, Integer>>> currSrcDstPairs = bc.getClassification(i);
			for (ArrayList<Pair<Integer, Integer>> branchPairs : currSrcDstPairs) {
				boolean canTrainProgram = false;
				if (baseline)
					canTrainProgram = SynEngineBaseline.trainProgramBaseline(treeIdx, astStore, true, new Pair(bc.getCriterion(i), bc.getBranchCondValue(i, branchPairs)), branchPairs);
				else
					canTrainProgram = SynEngineSmt.trainProgramSmt(treeIdx, astStore, efficientLookup, true, new Pair(bc.getCriterion(i), bc.getBranchCondValue(i, branchPairs)), branchPairs);
				synAll = synAll && canTrainProgram;
				if (!synAll) 
					break;
			}
			if (!synAll)
				continue;

			break;
		}
		return synAll;
	}
	
	public static void setBaselineMode(boolean isOn) {
		baseline = isOn;
	}
	
	public static TreeMap<Integer, Integer> eliminateDeadCode(TreeMap<Integer, Integer> program) {
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
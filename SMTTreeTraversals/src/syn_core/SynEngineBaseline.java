package syn_core;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import main.SynMain;
import utils.Pair;
import ast_utils.ASTStore;

public class SynEngineBaseline {

	public static boolean trainProgramBaseline(int treeIdx, ASTStore astStore, boolean branchedTraining, Pair<Integer, Object> branchCondValue, ArrayList<Pair<Integer, Integer>> srcDstPairs) {
		boolean programFound = false;
		ArrayList<Integer> dslOps = new ArrayList<>();
		for (int i = 0; i < DSLHelper.OP_CNT; i++) {
			dslOps.add(i);
		}
		for (int opNum = SynMain.startOpNum; opNum <= SynMain.maxOpNum && !programFound; opNum++) {
			if (!SynMain.statsOnly) {
				System.out.println("Training" + (branchedTraining ? " single branch: " : " program: ") + " checking synthesis satisfiability with max op. count: " + opNum);
			}
				
			final String chars = "0123456789abcdefghijklmnopqrstuvwxyz";
			
	        double powVal = Math.pow(opNum, dslOps.size());
	        for (int i = 0; i < powVal; i++) {
	        	ArrayList<Integer> program = new ArrayList<>();
	            String str = Integer.toString(i, dslOps.size());

	            while (dslOps.size() + str.length() < opNum) {
	            	//System.out.println(dslOps.get(0));
	                program.add(dslOps.get(0));
	            }
	            for (char c : str.toCharArray()) {
	            	//System.out.println(chars.indexOf(c));
	               	program.add(dslOps.get(chars.indexOf(c)));
	            }
	            //if (program.toString().equals("[0, 0, 0, 0, 0, 4, 1]"))
	            // Add trailing zeros
	            for (int j = program.size(); j <= opNum; j++ ) {
		            //System.out.println(program);
					int k = 0;
					Integer[] arrProgram = new Integer[program.size()];
					for (Integer op : program) {
						arrProgram[k++] = op;
					}
					boolean programOk = true;
					for (Pair<Integer, Integer> pair : srcDstPairs) {
						int src = pair.first;
						int dst = pair.second;
						
						int dstComputed = DSLHelper.applyDSLSequence(src, astStore, arrProgram);
						
						programOk = programOk && (dst == dstComputed);
						
						if (!programOk)
							break;
					}
					if (programOk) {
						programFound = true;
						if (branchedTraining) {
							SynEngine.branchedModelInterpretation.put(branchCondValue, SynEngine.eliminateDeadCode(cvtToTreeMap(arrProgram)));
						}
						else {
							SynEngine.modelInterpretation = SynEngine.eliminateDeadCode(cvtToTreeMap(arrProgram));
						}
						break;
					}
					program.add(0, 0);
				}
	        }
			
		}
		if (!SynMain.statsOnly)
			System.out.println();
		return programFound;
	}
	
	private static TreeMap<Integer, Integer> cvtToTreeMap(Integer[] program) {
		TreeMap<Integer, Integer> result = new TreeMap();
		for (int i = 0; i < program.length; i++) {
			result.put(i, program[i]);
		}
		return result;
	}
	
	private static ArrayList<ArrayList<Integer>> combinations(ArrayList<Integer> values, int maxLen) {

        final String chars = "0123456789abcdefghijklmnopqrstuvwxyz";
        ArrayList<ArrayList<Integer>> finalResult = new ArrayList();
        double powVal = Math.pow(maxLen, values.size());
        for (int i = 0; i < powVal; i++) {

            String str = Integer.toString(i, values.size());

            ArrayList<Integer> result = new ArrayList();
            while (values.size() + str.length() < maxLen)
                result.add(values.get(0));

            for (char c : str.toCharArray())
                result.add(values.get(chars.indexOf(c)));
            
            finalResult.add(result);            
        }
        return finalResult;
	}
	
	/*private static ArrayList<ArrayList<Integer>> combinations(ArrayList<Integer> values, int maxLen) {
		return combinations(values, maxLen, new ArrayList(), new ArrayList());
	}
	
	private static ArrayList<ArrayList<Integer>> combinations(ArrayList<Integer> values, int length, ArrayList<Integer> current, ArrayList<ArrayList<Integer>> result) {
		if (length == 0) {
			ArrayList<ArrayList<Integer>> newResult = new ArrayList<>(result);
			newResult.add(current);
			return newResult;
		}
		
		ArrayList<ArrayList<ArrayList<Integer>>> res3 = new ArrayList<>();
		for(Integer i : values) {
			ArrayList<Integer> newCurrent = new ArrayList<>(current);
			newCurrent.add(i);
			res3.add(combinations(values, length-1, newCurrent, result));
		}
		
		ArrayList<ArrayList<Integer>> res2 = new ArrayList<>();
		for (ArrayList<ArrayList<Integer>> list : res3) {
			res2.addAll(list);
		}
		return res2;
	}*/
}

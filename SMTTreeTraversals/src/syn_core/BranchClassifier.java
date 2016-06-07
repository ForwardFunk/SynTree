package syn_core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ast_utils.ASTStore;

import utils.Pair;


public class BranchClassifier {

	public static final int CRIT_SRC_DST_VALUE= 0;
	public static final int CRIT_SRC_IS_IN_CALL_EXPRESSION = 1;
	public static final int CRIT_ALL = 2;
	public static int criteriaCnt;
	
	private ASTStore astStore;
	private ArrayList<Pair<Integer, Integer>> srcDstPairs;
	private HashMap<Pair<Integer, Object>, ArrayList<Pair<Integer, Integer>>> srcDstPairsClassified;
	private HashMap<Integer, ArrayList<Object>> criteriaValues;
	private ArrayList<Integer> criteria;
	
	
	public BranchClassifier(ASTStore astStore, ArrayList<Pair<Integer, Integer>> srcDstPairs) {
		this.astStore = astStore;
		this.srcDstPairs = srcDstPairs;
		
		criteria = new ArrayList<>();
		criteria.add(CRIT_SRC_DST_VALUE);
		criteria.add(CRIT_SRC_IS_IN_CALL_EXPRESSION);	
		criteriaCnt = criteria.size();
		
		criteriaValues = new HashMap<>();
	}
	
	// returns a hashmap mapping the criteria type, their satisfiability to the pairs that satisfy/do not satisfy the criteria
	public void classifyTestPairs() {
	    srcDstPairsClassified = new HashMap<>();
		for (Integer criterion : criteria) {
			srcDstPairsClassified.putAll(applyCriterion(criterion));
		}
		//print(srcDstPairsClassified);
	}
	
	public ArrayList<ArrayList<Pair<Integer, Integer>>> getClassification(int criterionIdx) {
		ArrayList<ArrayList<Pair<Integer, Integer>>> result = new ArrayList<>();
		ArrayList<Object> valList = criteriaValues.get(criterionIdx);
		for (Object val : valList) {
			Pair<Integer, Object> key = new Pair(criteria.get(criterionIdx), val);
			result.add(srcDstPairsClassified.get(key));
		}
		return result;
	}
	
	private HashMap<Pair<Integer, Object>, ArrayList<Pair<Integer, Integer>>> applyCriterion(Integer criterion) {
		HashMap<Pair<Integer, Object>, ArrayList<Pair<Integer, Integer>>> result = new HashMap<Pair<Integer, Object>, ArrayList<Pair<Integer, Integer>>>();
		for (Pair<Integer, Integer> pair : srcDstPairs) {
			Object keyData = new Object();
			switch(criterion) {
				case CRIT_SRC_DST_VALUE:
					String ndValue1 = astStore.getNdValue(pair.first);
					String ndValue2 = astStore.getNdValue(pair.second);
					if (ndValue1.equals(ndValue2)) {
						keyData = ndValue1;
					} else {
						keyData = "";
					//	criterion = CRIT_ALL;
					}
				break;
				case CRIT_SRC_IS_IN_CALL_EXPRESSION:
					Integer grandparent = astStore.getNdParentIdx(astStore.getNdParentIdx(pair.first));
					String gpValue = astStore.getNdType(grandparent);
					if (gpValue.equals("CallExpression")) {
						keyData = Boolean.TRUE;
					} else {
						keyData = Boolean.FALSE;
					//	criterion = CRIT_ALL;
					}
				break;
			}

			if (!criteriaValues.containsKey(criterion))
				criteriaValues.put(criterion, new ArrayList<>());
			else if (!criteriaValues.get(criterion).contains(keyData))
				criteriaValues.get(criterion).add(keyData);
			
			Pair<Integer, Object> key = new Pair(criterion, keyData);
			if (result.containsKey(key)) {
				result.get(key).add(pair);
			} else  {
				ArrayList<Pair<Integer, Integer>> list = new ArrayList<>();
				list.add(pair);
				result.put(key, list);
			}				
		}
		return result;
		
	}
	
	public boolean isFakeCriterion(int criterionIdx) {
		int criterion = criteria.get(criterionIdx);
		for (Object val : criteriaValues.get(criterion)) {
			Pair<Integer, Object> key = new Pair(criterion, val);
			if (srcDstPairsClassified.get(key).size() == srcDstPairs.size()) {
				return true;
			}
		}
		return false;
	}
	
	public Integer getCriterion(int criterionIdx) {
		return criteria.get(criterionIdx);
	}
	
	public Object getBranchCondValue(int criterionIdx, ArrayList<Pair<Integer, Integer>> branchPairs) {
		Iterator it = srcDstPairsClassified.entrySet().iterator();
		while (it.hasNext()) {
			Entry en = (Entry) it.next();
			if (en.getValue() == branchPairs) {
				return ((Pair<Integer, Object>)en.getKey()).second;
			}
		}
		return null;
	}
	
	private void print(HashMap<Pair<Integer, Object>, ArrayList<Pair<Integer, Integer>>> result) {
		Iterator it = result.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry en = (Map.Entry) it.next();
			Pair<Integer, Object> key = (Pair<Integer, Object>) en.getKey();
			ArrayList<Pair<Integer, Integer>> pairs = (ArrayList<Pair<Integer, Integer>>) en.getValue();
			
			System.out.println(key.first.toString() + " " + key.second.toString());
			for (Pair<Integer, Integer> p : pairs) {
				System.out.print("("+p.first+","+p.second+") ");
			}
			System.out.println();
		}
	}

	public static String parseBranchCondition(Pair<Integer, Object> branchCond) {
		String type = "";
		switch(branchCond.first) {
		case CRIT_SRC_DST_VALUE:
			type = "(SrcVal.and.DstVal)==";
			break;
		case CRIT_SRC_IS_IN_CALL_EXPRESSION:
			type = "(Src.in.CallExpression)==";
			break;
		default:
			type = "(Else)";
			branchCond.second = "";
			break;
		}
		return "if$"+type+branchCond.second.toString();
	}

	public boolean classifySrcNode(Integer srcNdIdx, Pair<Integer, Object> branchCond) {
		int criterion = branchCond.first;
		switch (criterion) {
		case CRIT_SRC_DST_VALUE:
			if (astStore.getNdValue(srcNdIdx).equals(branchCond.second)) {
				return true;
			} else {
				return false;
			}
		case CRIT_SRC_IS_IN_CALL_EXPRESSION:
			Integer grandparent = astStore.getNdParentIdx(astStore.getNdParentIdx(srcNdIdx));
			String gpValue = astStore.getNdType(grandparent);
			if (gpValue.equals("CallExpression") == (Boolean) branchCond.second) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public String getCriterionName(int criterionIdx) {
		switch (criteria.get(criterionIdx)) {
		case CRIT_SRC_DST_VALUE:
			return "SRC_DST_HAVE_SAME_VALUE";
		case CRIT_SRC_IS_IN_CALL_EXPRESSION:
			return "SRC_IN_CALL_EXPRESSION";
		default:
			return "unknown_criteria";
		}
	}
}

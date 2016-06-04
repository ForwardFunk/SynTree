package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import ast_utils.ASTStore;

import syn_core.BranchClassifier;
import syn_core.DSLHelper;
import utils.Pair;

public class SynMain {

	public static int maxDslOpNum = 0;
	public static String fNameAst = "";
	public static String fNameTrain = "";
	public static String fNameTest = "";
	public static boolean efficientLookup = false;
	public static HashMap<Integer, ArrayList<Pair<Integer, Integer>>> trainSrcDstPairs;
	public static HashMap<Integer, ArrayList<Integer>> trainSrcVals;
	public static HashMap<Integer, ArrayList<Pair<Integer,Integer>>> testSrcDstPairs;
	public static ASTStore astStore;
	
	
	private static BranchClassifier bc;
	private static HashMap<Pair<Integer, Object>, TreeMap<Integer, Integer>> program;
	
	public static void main(String[] argv) {
		
		fNameAst = "programs_augmented4.json";
		fNameTrain = "../tests/tests_4/harder/train";
		fNameTest = "../tests/tests_4/harder/test";
		efficientLookup = true;
		maxDslOpNum = 7;
		
		// parse augmented AST from JSON
		astStore = new ASTStore(fNameAst);
		
		parseTrainingData();
		parseTestData();
		
		Iterator it = trainSrcDstPairs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry en = (Entry) it.next();
			Integer treeIdx = (Integer) en.getKey();
			ArrayList<Pair<Integer, Integer>> srcDstPairs = (ArrayList<Pair<Integer, Integer>>) en.getValue();
			astStore.setTreeIdx(treeIdx);
			
			if (SynEngine.trainProgram(treeIdx, astStore, efficientLookup, false, null, srcDstPairs)) {
				System.out.println(SynEngine.modelInterpretation);
				testProgram(false);
			} else if (SynEngine.trainBranchedProgram(treeIdx, (bc=new BranchClassifier(astStore, srcDstPairs)), astStore, efficientLookup, srcDstPairs)) {
				testProgram(true);				
			} else {
				System.out.println("Couldn't find a satisfying program...");
			}
			
		}
		
	}
	
	private static void testProgram(boolean branched) {
		testSrcDstPairs = new HashMap<>();
		Iterator it = trainSrcVals.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry en = (Entry) it.next();
			Integer treeIdx = (Integer) en.getKey();
			ArrayList<Integer> srcNds = (ArrayList<Integer>) en.getValue();
			for (int srcNdIdx : srcNds) {
				TreeMap<Integer, Integer> program = null;
				if (branched) {
					Iterator itPrograms = SynEngine.branchedModelInterpretation.entrySet().iterator();
					TreeMap<Integer, Integer> defaultProgram = null;	
					while (itPrograms.hasNext()) {
						Map.Entry enPrograms = (Entry) itPrograms.next();
						Pair<Integer, Object> branchCond = (Pair<Integer, Object>) enPrograms.getKey();
						TreeMap<Integer, Integer> currProgram = (TreeMap<Integer, Integer>) enPrograms.getValue();
						if (branchCond.second.toString().equals("")) {
							defaultProgram = currProgram;
						}
						else if (bc.classifySrcNode(srcNdIdx, branchCond)) {
							program = currProgram;
							break;
						}
					}
					if (program == null) {
						program = defaultProgram;
					}
				} else {
					program = SynEngine.modelInterpretation;
				}
				System.out.println(program);
				Integer[] programDslSequence = new Integer[program.size()];
				Iterator itProgram = program.entrySet().iterator();
				int i = 0;
				while (itProgram.hasNext()) {
					Map.Entry enProgram = (Entry) itProgram.next();
					programDslSequence[i++]=(Integer) enProgram.getValue();
				}
				if (!testSrcDstPairs.containsKey(treeIdx))
					testSrcDstPairs.put(treeIdx, new ArrayList<Pair<Integer, Integer>>());
				testSrcDstPairs.get(treeIdx).add(new Pair(srcNdIdx, DSLHelper.applyDSLSequence(srcNdIdx, astStore, programDslSequence)));
			}
		}
		System.out.println(testSrcDstPairs);
	}
	
	private static void parseTrainingData() {
		trainSrcDstPairs = new HashMap<>();
		
		BufferedReader rdr = null;
		try {
			rdr = new BufferedReader(new FileReader(fNameTrain));
			
			String line;
			while ((line = rdr.readLine()) != null) {
				if (!line.equals("")) {
					String[] val = line.split("\\s+");
					int treeIdx = Integer.parseInt(val[0]);
					int srcIdx = Integer.parseInt(val[1]);
					int dstIdx = Integer.parseInt(val[2]);
					if (!trainSrcDstPairs.containsKey(treeIdx))
						trainSrcDstPairs.put(treeIdx, new ArrayList<Pair<Integer, Integer>>());
					
					trainSrcDstPairs.get(treeIdx).add(new Pair(srcIdx, dstIdx));			
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rdr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void parseTestData() {
		trainSrcVals = new HashMap<>();
		
		BufferedReader rdr = null;
		try {
			rdr = new BufferedReader(new FileReader(fNameTest));
			String line;
			while ((line = rdr.readLine()) != null) {
				if (!line.equals("")) {
					String[] val = line.split("\\s+");
					int treeIdx = Integer.parseInt(val[0]);
					int srcIdx = Integer.parseInt(val[1]);
					if (!trainSrcVals.containsKey(treeIdx))
						trainSrcVals.put(treeIdx, new ArrayList<Integer>());
					
					trainSrcVals.get(treeIdx).add(srcIdx);			
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				rdr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}

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
import syn_core.SynEngine;
import utils.Pair;
import org.apache.commons.cli.*;

public class SynMain {
	
	private static String dirNameAugmented = "/asts_augmented/";

	public static int maxOpNum = 10;
	public static int startOpNum = 10;
	private static boolean baselineMode = false;
	private static String dirName = "";
	private static String fNameMain = "";
	private static String fNameAst = "";
	private static String fNameTrain = "";
	private static String fNameTest = "";
	private static String fNameCheck = "";
	
	private static final String optDirTest = "dir-test";
	private static final String optOpMin = "opmin";
	private static final String optOpMax = "opmax";
	private static final String optFNameAst = "f-ast";
	private static final String optFNameTrain = "f-train";
	private static final String optFNameTest = "f-test";
	private static final String optFNameCheck = "f-check";
	private static final String optDirAug = "dir-aug";
	private static final String optStrBaseline = "baseline";
	
	
	private static boolean efficientLookup = false;
	private static HashMap<Integer, ArrayList<Pair<Integer, Integer>>> trainSrcDstPairs;
	private static HashMap<Integer, ArrayList<Integer>> trainSrcVals;
	private static HashMap<Integer, ArrayList<Pair<Integer,Integer>>> testSrcDstPairs;
	private static HashMap<Integer, ArrayList<Pair<Integer,Integer>>> checkSrcDstPairs;
	private static ASTStore astStore;
	
	
	private static BranchClassifier bc;
	private static HashMap<Pair<Integer, Object>, TreeMap<Integer, Integer>> program;
	
	@SuppressWarnings("deprecation")
	public static void main(String[] argv) {
		
		
		// Parse CLI options
		parseArguments(argv);
		
		// Start the timer
		long startTime = System.currentTimeMillis();
		
		// Augment the json from the testdir with extra information about nodes (previous_id, prev_leaf...)
		String command = "python ./python/JSONGenerator.py" + " " +fNameMain + " " + dirNameAugmented;
		try {
			int p = Runtime.getRuntime().exec(command).waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Parse augmented AST from JSON
		astStore = new ASTStore(fNameAst);
		
		// Parse the <src,dst> pairs from training, test, and validate files within the test dir
		parseTrainingData();
		parseTestData();
		parseCheckData();
		
		// For every JavaScript program in AST, find a SynTree program which satisfies all the <src,dst> pairs
		Iterator it = trainSrcDstPairs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry en = (Entry) it.next();
			Integer treeIdx = (Integer) en.getKey();
			ArrayList<Pair<Integer, Integer>> srcDstPairs = (ArrayList<Pair<Integer, Integer>>) en.getValue();
			astStore.setTreeIdx(treeIdx);
			
			System.out.println("Training: trying to synthesize a straightline program.");
			if (SynEngine.trainProgram(treeIdx, astStore, efficientLookup, false, null, srcDstPairs)) {
				System.out.println("Training: found straightline program!");
				System.out.println("=====");
				testProgram(false);
			} else {
				System.out.println("Training: straightline program not found... trying to synthesize branched program.");
				if (SynEngine.trainBranchedProgram(treeIdx, (bc=new BranchClassifier(astStore, srcDstPairs)), astStore, efficientLookup, srcDstPairs)) {
					System.out.println("Training: branched program found!");
					System.out.println("=====");
					testProgram(true);
				} else {
					System.out.println("Couldn't find a satisfying program...");
				}
			}
			long stopTime = System.currentTimeMillis();
		    long elapsedTime = stopTime - startTime;
		    System.out.println((double)elapsedTime/1000 + "s");
		}
		
	}
	
	private static void parseArguments(String[] argv) {
		Option optTestDir = OptionBuilder.hasArg().create(optDirTest);
		Option optNameProg = OptionBuilder.hasArg().create(optFNameAst);
		Option optNameTrain = OptionBuilder.hasArg().create(optFNameTrain);
		Option optNameTest = OptionBuilder.hasArg().create(optFNameTest);
		Option optNameCheck = OptionBuilder.hasArg().create(optFNameCheck);
		Option optAugDir = OptionBuilder.hasArg().create(optDirAug);
		Option optMin = OptionBuilder.hasArg().create(optOpMin);
		Option optMax = OptionBuilder.hasArg().create(optOpMax);
		Option optBaseline = new Option(optStrBaseline, false, "Enable baseline mode.");
		
		Options options = new Options();
		CommandLineParser parser = new GnuParser();
		
		options.addOption(optTestDir);
		options.addOption(optNameProg);
		options.addOption(optNameTrain);
		options.addOption(optNameTest);
		options.addOption(optNameCheck);
		options.addOption(optAugDir);
		options.addOption(optBaseline);
		options.addOption(optMin);
		options.addOption(optMax);
		try 
		{
			CommandLine cl = parser.parse(options, argv);
			if (cl.hasOption(optDirTest)) {
				dirName = cl.getOptionValue(optDirTest);
			} else {
				dirName = "../tests/tests_4/harder/";
			}
			
			if (cl.hasOption(optFNameAst)) {
				fNameMain = cl.getOptionValue(optFNameAst);
			} else {
				fNameMain = "programs.json";
			}	
			
			if (cl.hasOption(optFNameTrain)) {
				fNameTrain = cl.getOptionValue(optFNameTrain);
			} else {
				fNameTrain = "train";
			}
			
			if (cl.hasOption(optFNameTest)) {
				fNameTest = cl.getOptionValue(optFNameTest);
			} else {
				fNameTest = "test";
			}
			
			if (cl.hasOption(optFNameCheck)) {
				fNameCheck = cl.getOptionValue(optFNameCheck);
			} else {
				fNameCheck = "expected";	
			}	
			
			if (cl.hasOption(optDirAug)) {
				dirNameAugmented = cl.getOptionValue(optDirAug);
			} else {
				dirNameAugmented = "./asts_augmented/";
			}
			
			if (cl.hasOption(optStrBaseline)) {
				baselineMode = true;
			} else {
				baselineMode = false;
			}
			
			if (cl.hasOption(optOpMin)) {
				startOpNum = Integer.valueOf(cl.getOptionValue(optOpMin));
			} else {
				startOpNum = 4;
			}
			
			if (cl.hasOption(optOpMax)) {
				maxOpNum = Integer.valueOf(cl.getOptionValue(optOpMax));
			} else {
				maxOpNum = 10;
			}
			
		} 
		catch (ParseException exception) {
            System.out.print("Parse error: ");
            System.out.println(exception.getMessage());
        }
		
		fNameAst = "programs_augmented.json";
		
		fNameMain = dirName + fNameMain;

	 
		fNameAst = dirNameAugmented + fNameAst;
		fNameTrain = dirName + fNameTrain;
		fNameTest = dirName + fNameTest;
		fNameCheck = dirName + fNameCheck;		
		efficientLookup = true;		
		SynEngine.setBaselineMode(baselineMode);  
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
		validateProgram();
	}
	
	private static void validateProgram() {
		Iterator itTest = testSrcDstPairs.entrySet().iterator();
		Iterator itCheck = checkSrcDstPairs.entrySet().iterator();
		
		System.out.println("Synthesis validation: (c - computed, e - expected)");
		while (itTest.hasNext() && itCheck.hasNext()) {
			Map.Entry enTest = (Entry) itTest.next();
			Map.Entry enCheck = (Entry) itCheck.next();
			
			Integer treeIdxTest = (Integer) enTest.getKey();
			Integer treeIdxCheck = (Integer) enCheck.getKey();
			assert(treeIdxTest == treeIdxCheck);
			
			ArrayList<Pair<Integer, Integer>> pairsTest = (ArrayList<Pair<Integer,Integer>>) enTest.getValue();
			ArrayList<Pair<Integer, Integer>> pairsCheck = (ArrayList<Pair<Integer,Integer>>) enCheck.getValue();
			
			assert(pairsTest.size() == pairsCheck.size());
			for (int i = 0; i < pairsTest.size(); i++) {
				Pair<Integer, Integer> pairTest = pairsTest.get(i);
				Pair<Integer, Integer> pairCheck = pairsCheck.get(i);
				
				// Src node indices have to match in both pairs
				assert (pairTest.first.equals(pairCheck.first));
				String test = pairTest.second.equals(pairCheck.second) ? "OK!" : "Invalid!";
				System.out.println(treeIdxTest.toString() + " " + pairTest.first.toString()  + " " + "c:"+pairTest.second + "/e:" + pairCheck.second + " " + test);
			}
		}
	}
	
	private static void parseCheckData() {
		checkSrcDstPairs = new HashMap<>();
		
		BufferedReader rdr = null;
		try {
			rdr = new BufferedReader(new FileReader(fNameCheck));
			
			String line;
			while ((line = rdr.readLine()) != null) {
				if (!line.equals("")) {
					String[] val = line.split("\\s+");
					int treeIdx = Integer.parseInt(val[0]);
					int srcIdx = Integer.parseInt(val[1]);
					int dstIdx = Integer.parseInt(val[2]);
					if (!checkSrcDstPairs.containsKey(treeIdx))
						checkSrcDstPairs.put(treeIdx, new ArrayList<Pair<Integer, Integer>>());
					
					checkSrcDstPairs.get(treeIdx).add(new Pair(srcIdx, dstIdx));			
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

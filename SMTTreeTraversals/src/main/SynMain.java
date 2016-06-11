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
	
	private static String dirNameAugmented = "./asts_augmented/";

	public static int maxOpNum = 10;
	public static int startOpNum = 10;
	
	private static boolean baselineMode = false;
	private static boolean efficientLookup = true;
	
	public static boolean statsOnly = true;
	private static String dirName ="./tests/tests_4/harder/";
	private static String altDirName ="";
	private static String fNameMain = "programs.json";
	private static String fNameAst = "programs_augmented.json";
	private static String fNameTrain = "train";
	private static String fNameTest = "test";
	private static String fNameCheck = "expected";
	
	private static final String optDirTest = "dirtest";
	private static final String optAltDirTest = "altdirtest";
	private static final String optOpMin = "opmin";
	private static final String optOpMax = "opmax";
	private static final String optFNameAst = "fast";
	private static final String optFNameTrain = "ftrain";
	private static final String optFNameTest = "ftest";
	private static final String optFNameCheck = "fcheck";
	private static final String optDirAug = "diraug";
	private static final String optStrBaseline = "baseline";
	private static final String optStrSlowLookup = "slowlookup";
	private static final String optStrStatsOnly = "statsonly";

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
		String command = "python ./python/JSONGenerator.py" + " " + fNameMain + " " + dirNameAugmented;
		try {
			Runtime.getRuntime().exec(command).waitFor();
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
			
			System.out.printf("Training program (baseline=%b, opmin=%d, opmax=%d, efficient (makes sense if smt)=%b, testdir=%s...\n\n", 
							baselineMode, startOpNum, maxOpNum, efficientLookup, dirName);
			if (SynEngine.trainProgram(treeIdx, astStore, efficientLookup, false, null, srcDstPairs)) {
				testProgram(false);
				if (!altDirName.equals("")) {
					testProgramWithNewAst(false);
				}
			} else {
				if (SynEngine.trainBranchedProgram(treeIdx, (bc=new BranchClassifier(astStore, srcDstPairs)), astStore, efficientLookup, srcDstPairs)) {
					testProgram(true);
					if (!altDirName.equals("")) {
						testProgramWithNewAst(true);
					}
				} else {
					System.out.println("Couldn't find a satisfying program...");
				}
			}
			
			printStats(startTime);
		}
		
	}
	
	private static void printStats(long startTime) {

		// print time
		if (SynEngine.modelInterpretation != null) {
			System.out.printf("No. of instr.: %d\n",SynEngine.modelInterpretation.size());
		} else if (SynEngine.branchedModelInterpretation.size() > 0) {
			Iterator it = SynEngine.branchedModelInterpretation.entrySet().iterator();
			ArrayList<Integer> instrCnt = new ArrayList();
			while (it.hasNext()) {
				Map.Entry en = (Map.Entry) it.next();
				TreeMap<Integer, Integer> val = (TreeMap<Integer, Integer>) en.getValue();
				instrCnt.add(val.size());
			}
			System.out.printf("No. of instr: %s\n", instrCnt.toString());
		}
		
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.printf("Time: %fs\n", (double)elapsedTime/1000);
	    
	    
	    Runtime rt = Runtime.getRuntime();
	    rt.gc();
	   
	    long mem = rt.totalMemory() - rt.freeMemory();
	    System.out.printf("Mem. usage: %d\n", mem);
	    System.out.println("=================");
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
		Option optAltTestDir = OptionBuilder.hasArg().create(optAltDirTest);
		Option optBaseline = new Option(optStrBaseline, false, "Enable baseline mode.");
		Option optSlowLookup = new Option(optStrSlowLookup, false, "Enable slow DSL function application in SMT.");
		Option optStatsOnly = new Option(optStrStatsOnly, false, "Only display statistics of runtime.");
		
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
		options.addOption(optSlowLookup);
		options.addOption(optStatsOnly);
		options.addOption(optAltTestDir);
		try 
		{
			CommandLine cl = parser.parse(options, argv);
			if (cl.hasOption(optDirTest)) {
				dirName = cl.getOptionValue(optDirTest);
			} 
			
			if (cl.hasOption(optAltDirTest)) {
				altDirName = cl.getOptionValue(optAltDirTest);
			} 
			
			if (cl.hasOption(optFNameAst)) {
				fNameMain = cl.getOptionValue(optFNameAst);
			} 
			
			if (cl.hasOption(optFNameTrain)) {
				fNameTrain = cl.getOptionValue(optFNameTrain);
			} 
			
			if (cl.hasOption(optFNameTest)) {
				fNameTest = cl.getOptionValue(optFNameTest);
			} 
			
			if (cl.hasOption(optFNameCheck)) {
				fNameCheck = cl.getOptionValue(optFNameCheck);
			} 
			
			if (cl.hasOption(optDirAug)) {
				dirNameAugmented = cl.getOptionValue(optDirAug);
			} 
			
			//baselineMode = cl.hasOption(optStrBaseline);
			//efficientLookup = !cl.hasOption(optStrSlowLookup);
			statsOnly = cl.hasOption(optStrStatsOnly); 
			
			if (cl.hasOption(optOpMin)) {
				startOpNum = Integer.valueOf(cl.getOptionValue(optOpMin));
			} else {
				startOpNum = 10;
			}
			
			if (cl.hasOption(optOpMax)) {
				maxOpNum = Integer.valueOf(cl.getOptionValue(optOpMax));
			} 
			
		} 
		catch (ParseException exception) {
            System.out.print("Parse error: ");
            System.out.println(exception.getMessage());
        }
		
		
		fNameMain = dirName + fNameMain;

	 
		fNameAst = dirNameAugmented + fNameAst;
		fNameTrain = dirName + fNameTrain;
		fNameTest = dirName + fNameTest;
		fNameCheck = dirName + fNameCheck;		
		SynEngine.setBaselineMode(baselineMode);  
	}
	
	@SuppressWarnings("unchecked")
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
							//break;
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
		validateAndShowProgram();
	}
	
	private static void testProgramWithNewAst(boolean branched) {
		String command = "python ./python/JSONGenerator.py" + " " + (altDirName + "programs.json") + " " + dirNameAugmented;
		try {
			Runtime.getRuntime().exec(command).waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		astStore = new ASTStore(fNameAst);
		testProgram(branched);
	}
	
	private static void validateAndShowProgram() {
		Iterator itTest = testSrcDstPairs.entrySet().iterator();
		Iterator itCheck = checkSrcDstPairs.entrySet().iterator();
		
		if (!statsOnly)
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
			boolean validates = true;
			for (int i = 0; i < pairsTest.size(); i++) {
				Pair<Integer, Integer> pairTest = pairsTest.get(i);
				Pair<Integer, Integer> pairCheck = pairsCheck.get(i);
				
				// Src node indices have to match in both pairs
				assert (pairTest.first.equals(pairCheck.first));
				validates = validates && pairTest.second.equals(pairCheck.second);
				String test = pairTest.second.equals(pairCheck.second) ? "OK!" : "Invalid!";
				if (!statsOnly)
					System.out.println(treeIdxTest.toString() + " " + pairTest.first.toString()  + " " + "c:"+pairTest.second + "/e:" + pairCheck.second + " " + test);
			}
			if (validates)
				System.out.println("Program validates!\n");
			else
				System.out.println("Program doesn't validate, please check!\n");
				
		}
		
		// Show program
		if (SynEngine.modelInterpretation != null) {
			System.out.println(DSLHelper.programToString(SynEngine.modelInterpretation));		
		} else if (SynEngine.branchedModelInterpretation.size() > 0) {
			Iterator it = SynEngine.branchedModelInterpretation.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry en = (Entry) it.next();
				TreeMap<Integer, Integer> branch = (TreeMap<Integer, Integer>) en.getValue();
				
				System.out.println(BranchClassifier.parseBranchCondition((Pair<Integer, Object>)en.getKey()));
				System.out.println(DSLHelper.programToString(branch));
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

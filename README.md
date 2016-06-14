#SynTree - SMT Tree Traversal Synthesizer

Copyright (C) 2016  Sevgi Kaya, Djordjevic Pavle

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.    

What is it?
----------------
SynTree draws instructions from  a specific DSL (domain specific language) in
order to synthesize programs which traverse from a source to a destination node 
where nodes are given by their indices in the tree (i.e. src/dst pairs). 
The trees in question are abstract syntax trees of programs written in JavaScript. 
SynTree tries to synthesize programs which satisfy all src/dst pairs, and if it cannot
find a straight line program which satisfies all the pairs, it will try to synthesize
a branched program, where branch bodies are applied to those pairs which satisfy 
the respective branch conditions. Possible branch conditions have been previously 
inferred from the training data.

SynTree is implemented in three different variants, which are presented in the 
report:
    + Baseline
    + SMT with inefficient lookup (HashMap maps to Z3 ITE structures)
    + SMT with efficient lookup (HashMap maps to Z3 arrays)

SynTree source code
-----------------
You can investigate the source code by opening the project from SMTTreeTraversals 
folder in Eclipse, or starting Eclipse from the Launcher on the left side (the
project should already be opened on start)


Running SynTree
-----------------
SynTree can be run with "java -jar <vm_args> ./tests/SynTree.jar <synthesis_args> " 
from the Terminal. The synthesis options which don't have a default value
(indicated below), need to be supplied.

The possible arguments/flags for running the synthesizer are:
      
      -Xmx2g - Set max Java VM memory size to 2GB.*
      
      -opmin <val> - Minimum number of DSL instructions for the program the 
      synthesizer is trying to synthesize. (default=4)
      
      -opmax <val> - Maximum number of DSL instructions that the synthesizer will 
      explore (default=7)
      
      -fast <file_name> - Location of the program AST in JSON format* 
      
      -ftrain <file_name> - Location of src/dst pairs for training*
      
      -ftest <file_name> - Location of src values for testing*
      
      -fcheck <file_name> - Location of src/dst pairs for validation
      (won't execute validation if --resultsonly)
      
      -diraug <dir_name> - Directory where the python preprocessor will store 
      the augmented JSON file of the AST (default = "../asts_augmented")
      
      --baseline - Run the naive baseline implementation of SynTree (default=false)
      
      --slowlookup - Run the slow lookup variant of SMT implementation of SynTree 
      (default=false).
      
      --statsonly - Display only runtime information (time, memory usage, program 
      synthesized) (default=false)
      
      --resultsonly - Apply the synthesized program to values of src in "test" file, 
      and display results. No other information displayed. (default=false). 
      IMPORTANT! If not using --resultsonly, -fcheck path has to be specified
      (the "expected" file in the tests folders) 
      
      
      (*) non-optional parameters
      
      
Replicating experiments
----------------
There are in total 2 script files for running the experiments presented in the 
report.

    + tests/run.sh - default script provided with the project. Need to supply
    -fast, -ftrain and -ftest paths. 
    
    + tests/run_measurements.sh - script which runs the synthesizer and displays 
    runtime information (time, memory usage, program synthesized), for all tests
    (1, 2, 3, 4-simple, 4-harder, 5-simple, 5-simple2, 5-harder)
    
    
Authors
--------------
Sevgi Kaya (skaya@student.ethz.ch)
Pavle Djordjevic (dpavle@student.ethz.ch)




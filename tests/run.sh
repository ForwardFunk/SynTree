#!/bin/bash

if [[ $# -ne 3 ]]; then
	echo "Error: Expected exactly 3 arguments"
	echo "$0 programs train test"
	echo "    programs: file containing JavaScript AST in JSON format (programs.json)"
	echo "    train   : file containing trainig examples"
	echo "    test    : file containing testing examples"
	echo ""
	echo "For example:"
	echo "$0 tests_1/programs.json tests_1/train tests_1/test"
	echo ""
	echo "Training file defines a set of training examples (one on each line) in the following format:"
	echo "    <tree_id> <source_node_id> <target_node_id>"
	echo "  where:"
	echo "    <tree_id> is a index (zero indexed) of the AST tree in the 'programs' argument"
	echo "    <source_node_id> is a integer denoting position of the source node"
	echo "    <target_node_id> is a integer denoting position of the target node"
	echo "  An example file can be found in ??"
	echo ""
	echo "Test file defines a set of testing examples (one on each line) in the following format:"
	echo "    <tree_id> <source_node_id>"
	echo "  where your goal is to run the synthesised program (learned from training examples) on each such example and produce the <target_node_id>"
	echo ""
	echo ""
	echo "Output format should be the same as the format of training file. If for some reason you can not produce <target_node_id> for a given example you should output -1 instead."
	echo "All the output should be produced to stdout. Any debugging logs should go to stderr."
	exit -1
fi


# Run your analysis and produce output as defined above

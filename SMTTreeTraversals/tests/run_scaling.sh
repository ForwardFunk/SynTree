b=--baseline
sl=--slowlookup
t61="./tests/tests_6/1/"
t63="./tests/tests_6/3/"
t65="./tests/tests_6/5/"
t67="./tests/tests_6/7/"
t69="./tests/tests_6/9/"

vm_args=-Xmx2g
syn_args="-opmin 4 -opmax 10 --statsonly -dirtest "
 

java -jar $vm_args SynTree.jar $syn_args $t61
java -jar $vm_args SynTree.jar $syn_args $t61 $sl
java -jar $vm_args SynTree.jar $syn_args $t61 $b
java -jar $vm_args SynTree.jar $syn_args $t63
java -jar $vm_args SynTree.jar $syn_args $t63 $sl
java -jar $vm_args SynTree.jar $syn_args $t63 $b
java -jar $vm_args SynTree.jar $syn_args $t65
java -jar $vm_args SynTree.jar $syn_args $t65 $sl
java -jar $vm_args SynTree.jar $syn_args $t65 $b
java -jar $vm_args SynTree.jar $syn_args $t67
java -jar $vm_args SynTree.jar $syn_args $t67 $sl
java -jar $vm_args SynTree.jar $syn_args $t67 $b
java -jar $vm_args SynTree.jar $syn_args $t69
java -jar $vm_args SynTree.jar $syn_args $t69 $sl
java -jar $vm_args SynTree.jar $syn_args $t69 $b

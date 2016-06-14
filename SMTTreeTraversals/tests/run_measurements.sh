vm_args=-Xmx2g
b=--baseline
sl=--slowlookup
t1="-fast ./tests_1/programs.json -ftrain ./tests_1/train -ftest ./tests_1/test -fcheck ./tests_1/expected"
t2="-fast ./tests_2/programs.json -ftrain ./tests_2/train -ftest ./tests_2/test -fcheck ./tests_2/expected"
t3="-fast ./tests_3/programs.json -ftrain ./tests_3/train -ftest ./tests_3/test -fcheck ./tests_3/expected"
t4s="-fast ./tests_4/simple/programs.json -ftrain ./tests_4/simple/train -ftest ./tests_4/simple/test -fcheck ./tests_4/simple/expected"
t4s="-fast ./tests_4/harder/programs.json -ftrain ./tests_4/harder/train -ftest ./tests_4/harder/test -fcheck ./tests_4/harder/expected"
t5s1="-fast ./tests_5/simple/programs.json -ftrain ./tests_5/simple/train -ftest ./tests_5/simple/test -fcheck ./tests_5/simple/expected"
t5s2="-fast ./tests_5/simple2/programs.json -ftrain ./tests_5/simple2/train -ftest ./tests_5/simple2/test -fcheck ./tests_5/simple2/expected"
t5h="-fast ./tests_5/harder/programs.json -ftrain ./tests_5/harder/train -ftest ./tests_5/harder/test -fcheck ./tests_5/harder/expected"

syn_args="-opmin 4 -opmax 10 --statsonly "
alt_dir=" -altdirtest ./tests/tests_5/alt_simple/"


java -jar $vm_args SynTree.jar $syn_args $t1
java -jar $vm_args SynTree.jar $syn_args $t1 $sl
java -jar $vm_args SynTree.jar $syn_args $t1 $b
java -jar $vm_args SynTree.jar $syn_args $t2
java -jar $vm_args SynTree.jar $syn_args $t2 $sl
java -jar $vm_args SynTree.jar $syn_args $t2 $b
java -jar $vm_args SynTree.jar $syn_args $t3
java -jar $vm_args SynTree.jar $syn_args $t3 $sl
java -jar $vm_args SynTree.jar $syn_args $t3 $b
java -jar $vm_args SynTree.jar $syn_args $t4s
java -jar $vm_args SynTree.jar $syn_args $t4s $sl
java -jar $vm_args SynTree.jar $syn_args $t4s $b
java -jar $vm_args SynTree.jar $syn_args $t4h
java -jar $vm_args SynTree.jar $syn_args $t4h $sl
java -jar $vm_args SynTree.jar $syn_args $t4h $b
java -jar $vm_args SynTree.jar $syn_args $t5s1
java -jar $vm_args SynTree.jar $syn_args $t5s1 $sl
java -jar $vm_args SynTree.jar $syn_args $t5s1 $b
java -jar $vm_args SynTree.jar $syn_args $t5s2
java -jar $vm_args SynTree.jar $syn_args $t5s2 $sl
java -jar $vm_args SynTree.jar $syn_args $t5s2 $b
java -jar $vm_args SynTree.jar $syn_args $t5h
java -jar $vm_args SynTree.jar $syn_args $t5h $sl
java -jar $vm_args SynTree.jar $syn_args $t5h $b

#java -jar $vm_args SynTree.jar $syn_args $t6 
#java -jar $vm_args SynTree.jar $syn_args $t6 $b

#java -jar $vm_args SynTree.jar $syn_args $t5h $alt_dir

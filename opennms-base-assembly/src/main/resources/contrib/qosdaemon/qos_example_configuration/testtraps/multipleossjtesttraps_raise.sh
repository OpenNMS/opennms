#!/bin/sh
# OpenNMS OSSJ Test Traps Generator
# this script sends multiple ossj test alarm raise traps 
# from different devices in the inventory defined in oss_test_db
     
# OSSJ_TEST_ALARM_1 raise

# openoss2
./trapgen v1 -d 127.0.0.1:162 -i 192.168.30.80 -c public -o 1.3.6.1.4.1.5813.3 -g 6 -s 1  

# openoss3
./trapgen v1 -d 127.0.0.1:162 -i 192.168.30.81 -c public -o 1.3.6.1.4.1.5813.3 -g 6 -s 1  

# openoss4
./trapgen v1 -d 127.0.0.1:162 -i 192.168.30.82 -c public -o 1.3.6.1.4.1.5813.3 -g 6 -s 1  


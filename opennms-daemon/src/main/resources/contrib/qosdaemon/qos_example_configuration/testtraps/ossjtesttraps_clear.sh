#!/bin/sh
# OpenNMS OSSJ Test Traps Generator
     
# OSSJ_TEST_ALARM_1 clear
./trapgen v1 -d 127.0.0.1:162 -i 127.0.0.1 -c public -o 1.3.6.1.4.1.5813.3 -g 6 -s 2       


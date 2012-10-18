#!/usr/bin/perl
# Copyright 2009-2012 Juniper Networks, Inc. All rights reserved.
use strict;
use warnings;
#use lib ("/usr/nma/lib");
#use PostgresReplication;
use IO::Handle;

my $log = '/opt/opennms/logs/failover.log';
open MYLOG, '>>', $log;
STDOUT->fdopen( \*MYLOG, 'w' );
STDERR->fdopen( \*MYLOG, 'w' );

my $isVIP = `ifconfig | grep eth0:0`;
my $snmpTargetReset = "/var/cache/jboss/opennms/snmpTargetReset";
my $ear = "/usr/local/jboss/server/all/deploy/011/opennms.ear";
print "starting opennms failover data recovery\n";
system("sh /opt/opennms/contrib/failover/scripts/failover.sh >> $log 2>&1");
print "wait until opennms is fully up...";
my $isRunning = isServiceRunning();
if ($isRunning == 0) {	
  	if (isDevIpConfigured()){
		# if there is no dev management IP,
		# VIP is the snmp target, so no need to resync 
               	print "trigger snmp target reset\n";
              	system("touch $snmpTargetReset");
	}
	sendTrap();
	setSnmpd();
	# touch to redeploy opennms.ear to sync opennms users
	system("touch $ear");
}else {
	print "ERROR: service opennms failed to run\n";
}
#when do we do this:   system("rm $snmpTargetReset"); # jmp-opennms stop will take care of this for now
#when do we do this:	unsetSnmpd();  # OK to do this in jmp-opennms stop

sub isServiceRunning {
	my $count = 0;
	my $done = 0;
	while ($count < 30 && $done == 0) {
        	my $return =  system("service opennms status");
        	if ($return != 0) {
                	my $status = system("service opennms status | grep running");
                	if ($status == 0 ) {
                        	print "partially running, give 15 seconds\n";
                       	 	sleep 15;
                        	$count++;
                	}else {
                        	print "ERROR: installation issue\n";
                        	last;
                		}
        	}else {
                	$done = 1;
       		}
	}
	if ($done == 1) {
        	print "\n## service runing, move on";
        	return 0;
	}else {
        	print "\n##ERROR: waited 100 seconds, failed faileover\n";
        	return 1;
	}
}

sub isDevIpConfigured {
    my $str = `ifconfig eth3 | grep 'inet addr'`;
    if ($str eq '') {
	return 0;	
    }
    else {
	return 1;
    }
}

sub setSnmpd {
	  # set min java process count to 2 in snmpd.conf
	  system("sed -i 's/proc java 10 1/proc java 10 2/g' /etc/snmp/snmpd.conf");
	  system("service snmpd reload");
}

sub unsetSnmpd {
	# set min java process count to 1 in snmpd.conf
	system("sed -i 's/proc java 10 2/proc java 10 1/g' /etc/snmp/snmpd.conf");
        system("service snmpd reload");
}
sub sendTrap {
	#send trap to opennms, triggering failover event, Opennms will then send a Space restarted trap out
	my $vip = `grep jmp-CLUSTER /etc/hosts| cut -f1`;
	my $ip = `hostname -i`;
	my $date = `date +%s`;
	chomp($ip);
	chomp($date);
	system("snmptrap -v 2c -c public $ip $date 1.3.6.1.4.1.2636.1.3.1.1.1");
}

#!/usr/bin/perl -w
use strict;

# This is a script for creating outages in random.
# Hard coded values are the month, serviceid which begins at 41.
# Ipaddress is hardcoded to 192.168.100


my $num_of_outages=30;
my $num_of_nodes=1;
my $num_of_services=1;
my $start_day=12;
my $num_of_days=7;

for (1..$num_of_outages) {
	my $node=int(rand()*$num_of_nodes)+1;
	my $service=int(rand()*$num_of_services+41);
	my $day=int($_/(($num_of_outages+1)/$num_of_days))+1;
	my $hour=int(($_-(($num_of_outages/$num_of_days)*($day-1)))/(($num_of_outages/$num_of_days)/20));
	$hour=~s/^([0-9])$/0$1/;
	my $minute=int(rand()*20)+10;
	my $xminute=$minute+int(rand()*20)+1;
	my $second=int(rand()*50)+10;
	my $xsecond=int(rand()*50)+10;
	print "insert into \"outages\" values (";
	print "$node,'192.168.100", ".", (int($node/$num_of_outages)+$node) % $num_of_outages;
	print "',$service,'", ($day + $start_day), "-Nov-2001 $hour:$minute:$second'";
	print ",'", ($day + $start_day), "-Nov-2001 $hour:$xminute:$xsecond')\;";
	print "", "\n";
}

#!/usr/bin/perl
#
# replace the above line with the path to your perl
#
use Getopt::Long;
#
# Get the host and timeout
#
my $host = "";
my $timeout = 0;
GetOptions
	("H|hostname=s" => \$host,
	"t|timeout=i"  => \$timeout);
# get the current time
#
$t = localtime();
#
# Put the IP addresses that you want to "pass" the test in the
# following list variable
#
@goodones = qw(
		10.0.0.12
		192.168.20.44
		192.168.31.2
	);
#
# uncomment the following line to increase the response time
# by 10 seconds (or whatever number you want). Use this to test timeouts.
#
#sleep(10);
#
$isit = 0;
foreach $i (@goodones) {
  if ($i eq $host) {
    $isit = 1;
  }
}
if($isit != 0) {
  print "gptest ran successfully";
}
else {
  print "gptest.pl failed";
}
#
# Comment out the next 3 lines if you don't want logging,
# or change the location and/or message of the log if you
# want something different.
#
open(LOG,">>/tmp/gplog");
print LOG "$t: $host: timeout = $timeout @ARGV result = $isit\n";
close(LOG);
#
# Uncomment one of the following lines to create an error message
# or return a non-zero exit code.
#
#print STDERR "this is the error msg";
#exit 5;

#!/usr/bin/perl
#
# replace the above line with the path to your perl
#
$t = localtime();
#
# Comment out the next 3 lines if you don't want logging,
# or change the location of the log if you want it
# somewhere other than /tmp/gplog
#
open(LOG,">>/tmp/gplog");
print LOG "$t: @ARGV\n";
close(LOG);
#
# Put the IP addresses that you want to "pass" the test in the
# following list variable
#
@goodones = qw(
		128.193.128.202
		128.193.128.17
		128.193.4.20
	);
#
# uncomment the following line to increase the response time
# by 10 seconds (or whatever number you want). Use this to test timeouts.
#
#sleep(10);
#
$isit = 0;
foreach $i (@goodones) {
  if ($i eq $ARGV[0]) {
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
# Uncomment one of the following lines to create an error message
# or return a non-zero exit code.
#
#print STDERR "this is the error msg";
#exit 5;

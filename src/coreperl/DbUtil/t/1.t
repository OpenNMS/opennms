# Before `make install' is performed this script should be runnable with
# `make test'. After `make install' it should work as `perl 1.t'

#########################

# change 'tests => 1' to 'tests => last_test_to_print';

use Test::More tests => 1;
BEGIN { use_ok('OpenNMS::DbUtil') };

#########################
use DBI;
# Insert your test code below, the Test::More module is use()ed here so read
# its man page ( perldoc Test::More ) for help writing this test script.
my $nodetype = 'A';

#        my $dbname = 'opennms';
#        my $dbhostname = 'localhost';
#        my $dbport = 5432;
#        my $dbuser = 'opennms' ;
#        my $dbpass = 'opennms' ;

my $dbh = OpenNMS::DbUtil->connect();
#$dbh = DBI->connect("dbi:Pg:dbname=$dbname;host=$dbhostname;port=$dbport","$dbuser","$dbpass")
#                or die "Error Connecting: $DBI::errstr";
#exit;
my $sth = $dbh->prepare('SELECT ipaddr from ipinterface') or die "Couldn't prepare statement: " . $dbh->errstr;

$sth->execute() or die "get_nodes - Couldn't execute statement: " . $sth->errstr;

my $nn = 0;

while ( my @nnid = $sth->fetchrow_array() ) {
	print " $nnid[0] \n";

	$nn++;
}

	$sth->finish;
	$dbh->disconnect;




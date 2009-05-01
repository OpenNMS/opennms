#!${install.perl.bin}


use Net::Nessus::Client;
use Net::Nessus::Message;
use DBI;
use Getopt::Std;

my $fatal_err=0;
our ( $opt_d, $opt_n );

unless (getopts ("dn")) {
	print "Invalid arguments!  Bailing!\n";
	exit 0;
}
# Before we do anything else, process args
my $debug = $opt_d;
my $no_commit = $opt_n;

# -d turns on debugging
if ( !defined($debug) ) {
	$debug = 0;
}
# This is mostly for debugging.  If no_commit is non-zero, we will not
# write to the database
if ( !defined($no_commit) ) {
	$no_commit = 0;
}

# These should be pretty obvious, but set your db_name, username, and password
my $db_name="opennms";
my $db_user="opennms";
my $db_pass="opennms";

if ($debug) {
	print "Using database name: $db_name\n";
	print "Using database username: $db_name\n";
	print "Using database password: $db_name\n";
}


if ($debug) {
	if (!$no_commit) {
		print "Plugins will be written to database\n";
	} else {
		print "Plugins will NOT be written to database\n";
	}
}

# Go ahead and make the database connection
my $dbh = DBI->connect("DBI:Pg:dbname=$db_name", $db_user, $db_pass, {
		PrintError => 0,	## Don't error via warn()
		RaiseError => 0		## Do error via die()
	});

if ($debug) {
	print "Database connection succeded\n";
}

my $sth=$dbh->prepare ("
	INSERT INTO vulnPlugins
	VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
	");

my $client = Net::Nessus::Client->new('host' => 'localhost',
				'user' => 'admin',
				'password' => 'password',
				'ntp_proto' => '1.2',
				);
if ($debug) {
	print "Connected to Nessus server\n";
	print "Requesting plugin list\n";
}

my $plugins = $client->Plugins();

if ($debug) {
	print "Received plugin list\n";
	print "Processing plugins\n";
	if (!$no_commit) {
		"Inserting plugins into database now\n";
	} else {
		print "Plugins will be printed to stdout.  ";
		print "This will be spammy!\n" 
	}
}

# iterate over elements of array in $ARRAYREF
foreach my $plugin (@$plugins) {
		if (!$no_commit) {
			$sth->execute(	$plugin->{'id'},
					"0",
					$plugin->{'name'},
					$plugin->{'category'},
					$plugin->{'copyright'},
					$plugin->{'description'},
					$plugin->{'summary'},
					$plugin->{'family'},
					$plugin->{'version'},
					$plugin->{'cve'},
					''
				#) || print "Insert failed: $DBI::errstr\n";
				) || $fatal_err++;
			if ($fatal_err) { #Fatal error, bail!
				print "We're bailin'!\n";
				print "Insert failed: $DBI::errstr\n";
				last;
			}
			if ($debug) {
				print "Inserted plugin id: $plugin->{'id'}\n";
			}
		} else {
			foreach my $key(keys %$plugin) {
				print "$key: $plugin->{$key}\n";
			}
		}
}

if ($fatal_err) {
	print "Something went wrong.  See above.  Closing database ";
	print "and exiting!\n";
} elsif ($debug) {
	print "All done.  Disconnecting from database.  Have a nice day.\n";
}

$dbh->disconnect or warn "Disconnection failed: $DBI::errstr\n";

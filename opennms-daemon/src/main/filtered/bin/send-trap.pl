#!${install.perl.bin}
#
# a quick hack of a script to send traps
# by Ben Reed (ben@opennms.org)
#
# whee!

$|++;

use lib '.';
use Net::SNMP;
use Getopt::Mixed qw(nextOption);
use Time::HiRes qw(usleep time);

use vars qw(
  $BURST
  $COMMUNITY
  $DEBUG
  $DESTINATION
  $INTERVAL
  $NUMBER
  $OID
  $PORT
  $STRING
  $VERBOSE
);

$BURST       = 0;
$COMMUNITY   = 'public';
$DEBUG       = 0;
$DESTINATION = '127.0.0.1';
$INTERVAL    = 1000;
$NUMBER      = 10;
$OID         = '.1.3.6.1.4.1.7001';
$PORT        = 162;
$STRING      = 'OpenNMS Rules!';
$VERBOSE     = 0;

Getopt::Mixed::init('d=s dest>d i=i interval>i n=i number>n b burst>b c=s community>c o=s oid>o p=i port>p s=s string>s v verbose>v h help>h');

while (my ($option, $value) = nextOption()) {

  $BURST++              if ($option eq 'b');
  $COMMUNITY   = $value if ($option eq 'c');
  $DESTINATION = $value if ($option eq 'd');
  $INTERVAL    = $value if ($option eq 'i');
  $NUMBER      = $value if ($option eq 'n');
  $OID         = $value if ($option eq 'o');
  $PORT        = $value if ($option eq 'p');
  $STRING      = $value if ($option eq 's');
  $VERBOSE++            if ($option eq 'v');

  if ($option eq 'h') {
    print_help();
    exit;
  }
}

Getopt::Mixed::cleanup();

if ($VERBOSE > 1) {

  $DEBUG = 1;

  print <<END_INFO;
burst?       $BURST
verbose?     $VERBOSE
community:   $COMMUNITY
destination: $DESTINATION
interval:    $INTERVAL
number:      $NUMBER
oid:         $OID
port:        $PORT
string:      $STRING
END_INFO

}

my $session = Net::SNMP->session(
  -hostname    => $DESTINATION,
  -community   => $COMMUNITY,
#  -nonblocking => 1,
  -timeout     => 1,
  -retries     => 1,
  -debug       => $DEBUG,
  -port        => $PORT,
);


my $start_time = time();
my $counter = 1;
while (1) {

  $session->trap(
#    -enterprise   => '1.3.6.1.4.1.5813',
     -enterprise   => $OID,
    -generictrap  => 6,
    -specifictrap => 0,
    -varbindlist  => [$OID.1, OCTET_STRING, $STRING . " ($counter)"],
  ) or die "error: $!";

  usleep($INTERVAL * 1000) unless $BURST;
  $counter++;
  last if ($counter > $NUMBER);
}
my $end_time = time();

my $traps  = ($counter / ($end_time - $start_time)) if ($counter != 0);
printf("- sent %0.2f traps per second.\n", $traps);

sub print_help {

  print <<END_HELP;
usage:

  $0 [-b] [-d <dest_addr>] [-o <oid>] [-i <interval>] [-n <num_traps>] [-h] [-v]

-b / --burst       try to flood the SNMP agent (default: no)
-c / --community   the community string to use (default: public)
-d / --dest        destination address of the agent (default: 127.0.0.1)
-p / --port        the port to send to (default: 162)
-i / --interval    interval (in milliseconds) between sends (default: 1000)
                   ignored if -b
-n / --number      number of traps to send (default: 10)
-h / --help        this help
-v / --verbose     verbose output (multiple -v's are allowed)

other options:

-o / --oid         the enterprise OID of generated traps
-s / --string      the string to send in the varbinds

END_HELP

  return 1;

}


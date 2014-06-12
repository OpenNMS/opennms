#!${install.perl.bin}

use strict;
use Getopt::Long;
use IO::Socket;
use POSIX qw(strftime);
use Sys::Hostname;

use vars qw(
	$VERSION

	$DESCR
	$LOGMSG
	$HOSTNAME
	$INTERFACE
	$IFINDEX
	$NODEID
	$SERVICE
	$SEVERITY
	$SOURCE
	$UEI
	$UUID
	$VERBOSE
	$ZONE
	$OPERINSTR
	@PARMS

	@SEVERITIES
	$HOST_TO
	$PORT_TO
);

$VERSION = '0.3';
$VERBOSE = 0;
$ZONE    = 'GMT';

@SEVERITIES = ( undef, 'Indeterminate', 'Cleared', 'Normal', 'Warning', 'Minor', 'Major', 'Critical' );
	
my $help = 0;
my $version = 0;
my $result = GetOptions("help|h" => \$help,
                        "descr|d=s"     => \$DESCR,
                        "logmsg|l=s"    => \$LOGMSG,
                        "interface|i=s" => \$INTERFACE,
                        "ifindex|f=i"   => \$IFINDEX,
                        "nodeid|n=i"    => \$NODEID,
                        "parm|p=s"      => \@PARMS,
                        "service|s=s"   => \$SERVICE,
                        "uuid|U=i"      => \$UUID,
                        "version|V"     => \$version,
                        "verbose|v"     => \$VERBOSE,
                        "severity|x=i"  => \$SEVERITY,
                        "operinstr|o=s" => \$OPERINSTR);
if (! $result) { print get_help(); exit; }
if ($version)  { print "$0 version $VERSION\n"; exit; }
if ($help)     { print get_help(); exit; }

# parm array is numerically referenced in OpenNMS' templates
@PARMS = reverse map { parse_parm($_) } @PARMS;

my $hostname = hostname;

## we'll try turning the hostname into an IP and back, to see if we can get a canonical FQDN
my @addr = gethostbyname($hostname);
$HOSTNAME = gethostbyaddr($addr[4], $addr[2]);

if (not defined $HOSTNAME or $HOSTNAME eq "") {
	# if we can't look it back up by the IP address, just use what `hostname` printed
	$HOSTNAME = $hostname;
}

$SOURCE   = 'perl_send_event';
$UEI      = $ARGV[0];
$HOST_TO  = $ARGV[1];
$PORT_TO  = 5817;

#### bounds-checking on various inputs

# UEI
if (defined $UEI) {
	unless (grep(m#uei#, $UEI)) {
		print "*** \"$UEI\" does not appear to be a valid UEI\n\n";
		print get_help();
		exit 1;
	}
} else {
	print get_banner(), "the UEI is a required field!\n";
	print get_help();
	exit 1;
}

if (defined $HOST_TO) {
	my ($host, $port) = split(/:/, $HOST_TO);
	if ($port =~ /^\d+$/ and $port > 0) {
		$PORT_TO = $port;
	}
	if ($host ne "") {
		$HOST_TO = $host;
	}
} else {
	$HOST_TO = 'localhost';
}

if (defined $SEVERITY) {
	my $SEVERITY_OK = 0;
	if ($SEVERITY !~ /^\d+$/) {
		$SEVERITY = ucfirst(lc($SEVERITY));
		for my $index (0..$#SEVERITIES) {
			if ($SEVERITY eq $SEVERITIES[$index]) {
				$SEVERITY_OK = 1;
				last;
			}
		}
		unless ($SEVERITY_OK) {
			print "*** $SEVERITY does not appear to be a valid severity level\n\n";
			print get_help();
			exit 1;
		}
	} else {
		if (defined $SEVERITIES[$SEVERITY]) {
			$SEVERITY = $SEVERITIES[$SEVERITY];
		} else {
			print "*** $SEVERITY does not appear to be a valid severity level\n\n";
			print get_help();
			exit 1;
		}
	}
}

if (defined $INTERFACE) {
	my $ok = 0;
	# Check for IPv4 Address
	if (4 == grep($_ <= 255, $INTERFACE =~ /^(\d+)\.(\d+)\.(\d+)\.(\d+)$/)) {
		$ok = 1;
	}
	if (not $ok) {
		# hahahahahahaha!
		# *cough* um, I mean, Check for IPv6 Address
		# http://forums.dartware.com/viewtopic.php?t=452
		if ($INTERFACE =~ /^\s*((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)(\.(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)){3}))|:)))(%.+)?\s*$/) {
			$ok = 1;
		}
	}

	if (not $ok) {
		print "*** \"$INTERFACE\" does not appear to be a valid IP address\n\n";
		print get_help();
		exit 1;
	}
}

if (defined $DESCR) {
	($DESCR) = simple_parse($DESCR);
}

if (defined $LOGMSG) {
	($LOGMSG) = simple_parse($LOGMSG);
}

if (defined $SERVICE) {
	($SERVICE) = simple_parse($SERVICE);
}

my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = gmtime(time);
$year += 1900;
my $month = $mon;
$min   = sprintf("%02d", $min);
$sec   = sprintf("%02d", $sec);
my @week = ('Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday');
my @month = ('January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December');

my $uuidattribute;
if (defined $UUID) {
	$uuidattribute = "uuid=\"$UUID\"";
} else {
	$uuidattribute = "";
}


my $event = <<END;
<log>
 <events>
  <event $uuidattribute>
   <uei>$UEI</uei>
   <source>$SOURCE</source>
END

$event .= "   <nodeid>$NODEID</nodeid>\n"          if (defined $NODEID);

$event .= <<END;
   <time>$week[$wday], $mday $month[$month] $year $hour:$min:$sec o'clock $ZONE</time>
   <host>$HOSTNAME</host>
END

$event .= "   <interface>$INTERFACE</interface>\n" if (defined $INTERFACE);
$event .= "   <service>$SERVICE</service>\n"       if (defined $SERVICE);
$event .= "   <ifIndex>$IFINDEX</ifIndex>\n"     if (defined $IFINDEX);

if (@PARMS) {
  $event .= "   <parms>\n";
  for my $parm (@PARMS) {
    $event .= <<END;
    <parm>
     <parmName><![CDATA[$parm->{'name'}]]></parmName>
     <value type="string" encoding="text"><![CDATA[$parm->{'value'}]]></value>
    </parm>
END
  }
  $event .= "   </parms>\n";
}

$event .= "   <descr>$DESCR</descr>\n"             if (defined $DESCR);
$event .= "   <logmsg>$LOGMSG</logmsg>\n"             if (defined $LOGMSG);
$event .= "   <severity>$SEVERITY</severity>\n"    if (defined $SEVERITY);
$event .= "   <operinstruct>$OPERINSTR</operinstruct>\n" if (defined $OPERINSTR);
$event .= <<END;
  </event>
 </events>
</log>
END

print "- sending to $HOST_TO on port $PORT_TO...\n" if ($VERBOSE);

my $socket = IO::Socket::INET->new(PeerAddr => $HOST_TO, PeerPort => $PORT_TO, Proto => "tcp", Type => SOCK_STREAM)
	or die "Couldn't connect to $HOST_TO:$PORT_TO - $@\n";

print "$event" if ($VERBOSE);
print $socket $event;
$socket->close();

sub parse_parm {
  my $parm = shift;

  my ($name, $value) = split(/\s+/, $parm, 2);
  return ({ name => $name, value => $value });
}

sub get_banner {
	return <<END;
Usage: $0 <UEI> [host] [options]

END
}

sub simple_parse {
	for (@_) {
		s#\&#\&amp;#gs;
		s#\<#\&lt;#gs;
		s#\>#\&gt;#gs;
		s#\'#\&apos;#gs;
		s#\"#\&quot;#gs;
	}
	return @_;
}

sub get_help {
	return (get_banner, <<END);

Options:

         <UEI>             the universal event identifier (URI)
         [host[:port]]     a hostname to send the event to (default: localhost)
         --version, -V     print version and exit successfully
         --verbose, -v     print the raw XML that's generated
         --help, -h        this help message

         --timezone, -t    the time zone you are in
         --service, -s     service name 
         --nodeid, -n      node identifier (numeric)
         --interface, -i   IP address of the interface
         --ifindex, -f     IfIndex of the interface
         --descr, -d       a description for the event browser
         --logmsg, -l      a logmsg for the event browser (secure field by default)
         --severity, -x    the severity of the event (numeric or name)
                           1 = Indeterminate
                           2 = Cleared (unimplemented at this time)
                           3 = Normal
                           4 = Warning
                           5 = Minor
                           6 = Major
                           7 = Critical
	--parm, -p         an event parameter (ie:
                           --parm 'url http://www.google.com/')
	--uuid, -U         a UUID to pass with the event

Example: Force discovery of a node:

        send-event.pl \\
                --interface 172.16.1.1 \\
                uei.opennms.org/internal/discovery/newSuspect

END
}

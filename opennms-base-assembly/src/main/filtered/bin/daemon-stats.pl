#!/usr/bin/perl -w

=head1 NAME

daemon-stats.pl - Command-line interface to list OpenNMS daemon statistics

=head1 SYNOPSIS

daemon-stats.pl [options] [daemon]...

=cut

use warnings;

use Carp;
use Getopt::Long;
use LWP;
use LWP::UserAgent;
use Pod::Usage;
use XML::Twig;

use vars qw(
	$BUILD
	$BROWSER
	$XML
	$DAEMONNAME
	$daemon
	$status
);

$BUILD = (qw$LastChangedRevision 1 $)[-1];
$XML = XML::Twig->new();
# set defaults
#
$url_root = 'http://localhost:8181';
$username = 'manager';
$password = 'manager';

=head1 OPTIONS

=over 8

=item B<--help>

Print a brief help message and exit.

=item B<--version>

Print the version and exit.

=item B<--username>

The username to use when connecting to the MX4J HTTP adaptor.

Defaults to 'manager'.

=item B<--password>

The password associated with the MX4J username specified
in B<-username>.

Defaults to 'manager'.

=item B<--url>

The URL of the OpenNMS MX4J HTTP adaptor.  Defaults to
'http://localhost:8181'.

=back

=cut

Getopt::Long::Configure( "require_order" );
my $result = GetOptions(
	"help|h"     => \$print_help,
	"longhelp|l" => \$print_longhelp,
	"version|v"  => \&print_version,

	"username|u=s" => \$username,
	"password|p=s" => \$password,

	"url=s"        => \$url_root,
);

$DAEMONNAME = shift;

pod2usage(1) if $print_help;
pod2usage(-exitstatus => 0, -verbose => 2) if $print_longhelp;

set_up_environment();

=head1 ARGUMENTS

If a daemon name is specified, statistics for that daemon will be listed.

When run with no arguments, statistics for all daemons will be listed.

=cut

$status = 1;
foreach $daemon( get_all_daemon_names() ) {
	if ( (! defined $DAEMONNAME) || (lc $DAEMONNAME eq lc $daemon) ) {
		print_daemon_stats( $daemon );
		$status = 0;
	}
}
if ($status != 0) {
	print "No daemon found with name ${DAEMONNAME}. Run with no arguments to see stats for all daemons.\n";
}
exit $status;


sub set_up_environment {
	$BROWSER = LWP::UserAgent->new(agent => "daemon-status.pl/$BUILD");

	my $uri = URI->new($url_root);

	$BROWSER->credentials(
		$uri->host_port(),
		'MX4J',
		$username => $password,
	);
}

sub get_all_daemon_names {
	my @daemons = ();
	my $response = $BROWSER->get( $url_root . '/');
	
	if (! $response->is_success) {
		croak($response->status_line);
	}
	$XML->parse($response->content);
	my $root = $XML->root;
	for my $child ($root->children('MBean')) {
		if ( ( $child->{att}->{objectname} ) =~ /^OpenNMS:Name=(.*)$/ ) {
			push @daemons, ( $1 );
		}
	}
	return @daemons;
}

sub print_daemon_stats {
	my $daemonName = shift;
	my $objname = URI::Escape::uri_escape_utf8("OpenNMS:Name=${daemonName}");
	my $response = $BROWSER->get( $url_root . "/mbean?objectname=${objname}" );
	
	if (! $response->is_success) {
		croak($response->status_line);
	}
	$XML->parse($response->content);
	my $root = $XML->root;
	for my $child ($root->children('Attribute')) {
		print "\t${daemonName}." . $child->{att}->{name} . " = " . $child->{att}->{value} . "\n" unless ($child->{att}->{name} =~ /^(Status(Text)?)|(LoggingPrefix)|(SpringContext)$/);
	}
}

sub print_version {
	printf("%s build %d\n", (split('/', $0))[-1], $BUILD);
	exit 0;
}

=head1 DESCRIPTION

B<This program> provides an interface to the daemon statistics
exposed via JMX, available in OpenNMS 1.12 and higher. It uses
the MX4J HTTP adaptor which provides an HTTP-JMX bridge.

=head1 AUTHOR

Jeff Gehlbach <jeffg@opennms.org>

=head1 BUGS

Please submit bug reports at http://issues.opennms.org/

=head1 COPYRIGHT AND DISCLAIMER

Copyright 2014, The OpenNMS Group, Inc.  All rights reserved.

OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

For more information contact:

	OpenNMS Licensing <license@opennms.org>

=cut

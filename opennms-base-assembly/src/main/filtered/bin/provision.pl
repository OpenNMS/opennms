#!/usr/bin/perl -w

=head1 NAME

provision.pl - Command-line interface to the provisioner

=head1 SYNOPSIS

provision.pl [options] command [arguments ...]

=cut

use warnings;

BEGIN {
	eval("use HTTP::Cookies; use HTTP::Request; use LWP; use Pod::Usage; use XML::Twig;");
	if ($@) {
		print <<END;
!!!! WARNING !!!!

provision.pl requires a couple of perl modules that may not be provided by
your perl installation.  Please make sure LWP and XML::Twig are installed.

If you are on an RPM-based system, you can run:

	yum install 'perl(LWP)' 'perl(XML::Twig)'

If you are on a Debian-based system, run:

	apt-get install libwww-perl libxml-twig-perl

Otherwise, you can use CPAN directly:

	cpan LWP XML::Twig

END
		exit(1);
	}
}

use Carp;
use Data::Dumper;
use File::Path;
use Getopt::Long;
use HTTP::Cookies;
use HTTP::Request;
use LWP;
use LWP::UserAgent;
use Pod::Usage;
use URI;
use URI::Escape;
use XML::Twig;

use vars qw(
	$BUILD
	$BROWSER
	$XML

	$print_help
	$print_longhelp

	$username
	$password

	$url_root
	$user_config_file
);

$BUILD = (qw$LastChangedRevision 1 $)[-1];
$XML = XML::Twig->new();

# set defaults
$url_root = 'http://localhost:8980/opennms/rest';
$username = 'admin';
$password = 'admin';

$user_config_file = ($^O eq "MSWin32") ? $ENV{LOCALAPPDATA} . "\\OpenNMS\\provision.plrc" : $ENV{HOME} . "/.opennms/provision.plrc";

# load user-overridden defaults if present
load_user_config();

=head1 OPTIONS

=over 8

=item B<--help>

Print a brief help message and exit.

=item B<--version>

Print the version and exit.

=item B<--username>

The username to use when connecting to the RESTful API.  This
user must have administrative privileges in the OpenNMS web UI.

Defaults to 'admin'.

May be overridden by setting $username in $HOME/.opennms/provision.plrc
(UNIX) or %LOCALAPPDATA%\OpenNMS\provision.plrc (Windows).

=item B<--password>

The password associated with the administrative username specified
in B<-username>.

Defaults to 'admin'.

May be overridden by setting $password in $HOME/.opennms/provision.plrc
(UNIX) or %LOCALAPPDATA%\OpenNMS\provision.plrc (Windows).

=item B<--url>

The URL of the OpenNMS REST interface.  Defaults to
'http://localhost:8980/opennms/rest'.

May be overridden by setting $url_root in $HOME/.opennms/provision.plrc
(UNIX) or %LOCALAPPDATA%\OpenNMS\provision.plrc (Windows).

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

pod2usage(1) if $print_help;
pod2usage(-exitstatus => 0, -verbose => 2) if $print_longhelp;

set_up_environment();

=head1 ARGUMENTS

=over 8

=cut

my $command = shift(@ARGV);

if (not defined $command) {
	pod2usage(-exitval => 1);
} elsif (exists &{"cmd_$command"}) {
	&{"cmd_$command"}(@ARGV);
} else {
	pod2usage(-exitval => 1, -message => "Unknown command: $command");
}

### commands ###

=item B<list>

=over 8

=item B<list>

List the available requisition foreign sources.

=item B<list E<lt>foreign-sourceE<gt>>

List the requisition with the given foreign source.

=back

=cut

sub cmd_list {
	my @args = @_;

	my $path = shift @args || '';

	my $response = get($path);
	dump_xml($response->content);
}

=item B<requisition>

=over 8

=item B<requisition list E<lt>foreign-sourceE<gt>>

List the requisition with the given foreign source.

=item B<requisition add E<lt>foreign-sourceE<gt>>

Add a requisition with the given foreign source.

=item B<requisition remove E<lt>foreign-sourceE<gt> [deployed]>

Remove the requisition with the given foreign source.

If the optional argument "B<deployed>" is specified, it will remove
the already-imported foreign source configuration.

=item B<requisition import E<lt>foreign-sourceE<gt>> [rescanExisting] [value]

Import the requisition with the given foreign source.

If the optional argument "B<rescanExisting>" is specified, the value
must be one of the following:

=over 4

=item * true, to update the database and execute the scan phase 

=item * false, to add/delete nodes on the DB sckipping the scan phase

=item * dbonly, to add/detete/update nodes on the DB sckipping the scan phase

=back

=back

=cut

sub cmd_requisition {
	my @args = @_;

	my $command        = shift @args;
	my $foreign_source = shift @args;

	if (not defined $foreign_source or $foreign_source eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify a foreign source!", -verbose => 0);
	}

	if ($command eq 'list') {
		cmd_list($foreign_source);
	} elsif (is_add($command)) {
		if ($foreign_source =~ /[\/\\?:&*'"]/) {
			pod2usage(-exitval => 1, -message => "Error: foreign source cannot contain :, /, \\, ?, &, *, ', \"", -verbose => 0);
		}
		my $xml = get_element('model-import');
		my $root = $xml->root;
		$root->{'att'}->{'foreign-source'} = $foreign_source;
		post('', $root);
	} elsif (is_remove($command)) {
		my $deployed = shift @args;
		if (defined $deployed and ($deployed eq 'deployed' or $deployed eq 'active')) {
			remove('deployed/' . $foreign_source);
		} else {
			remove($foreign_source);
		}
	} elsif ($command eq 'import' or $command eq 'deploy') {
		my $key   = shift @args || '';
		my $value = shift @args || '';
		if ($key eq 'rescanExisting' and $value !~ /^(true|false|dbonly)$/i) {
			pod2usage(-exitval => 1, -message => "Error: You must specify a valid value for rescanExisting (true, false, dbonly)!", -verbose => 0);
		}
		my $query = URI::Escape::uri_escape_utf8($key) . "=" . URI::Escape::uri_escape_utf8($value) if $key eq 'rescanExisting';
		put_simple(URI::Escape::uri_escape_utf8($foreign_source) . '/import', $query);
	} else {
		pod2usage(-exitval => 1, -message => "Unknown command: requisition $command", -verbose => 0);
	}
}

=item B<node>

=over 8

=item B<node add E<lt>foreign-sourceE<gt> E<lt>foreign-idE<gt> E<lt>node-labelE<gt>>

Add a node to the requisition identified by the given foreign source.

=item B<node remove E<lt>foreign-sourceE<gt> E<lt>foreign-idE<gt>>

Remove a node from the requisition identified by the given foreign source and foreign ID.

=item B<node set E<lt>foreign-sourceE<gt> E<lt>foreign-idE<gt> E<lt>keyE<gt> [value]>

Set a property on a node, given the foreign source and foreign id.  Valid properties are:

=over 4

=item * building

=item * city

=item * node-label

=item * parent-foreign-source

=item * parent-foreign-id

=item * parent-node-label

=back

=back

=cut

sub cmd_node {
	my @args = @_;

	my $command        = shift @args;
	my $foreign_source = shift @args;
	my $foreign_id     = shift @args;

	if (not defined $foreign_source or $foreign_source eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify a foreign source!", -verbose => 0);
	}
	if (not defined $foreign_id or $foreign_id eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify a foreign id!", -verbose => 0);
	}

	if (is_add($command)) {
		if ($foreign_id =~ /[\/\\?:&*'"]/) {
			pod2usage(-exitval => 1, -message => "Error: foreign id cannot contain :, /, \\, ?, &, *, ', \"", -verbose => 0);
		}
		my $node_label = shift @args;
		if (not defined $node_label or $node_label eq "") {
			pod2usage(-exitval => 1, -message => "Error: You must specify a node label!", -verbose => 0);
		}
		my $xml = get_element('node');
		my $root = $xml->root;
		$root->{'att'}->{'foreign-id'} = $foreign_id;
		$root->{'att'}->{'node-label'} = $node_label;
		post(URI::Escape::uri_escape_utf8($foreign_source) . "/nodes", $root);
	} elsif (is_remove($command)) {
		remove($foreign_source . '/nodes/' . $foreign_id);
	} elsif (is_set($command)) {
		my $key   = shift @args;
		my $value = shift @args;

		if (not defined $key or $key eq "") {
			pod2usage(-exitval => 1, -message => "Error: You must specify a key!", -verbose => 0);
		}

		$key   = URI::Escape::uri_escape_utf8($key);
		$value = URI::Escape::uri_escape_utf8($value);
		put(URI::Escape::uri_escape_utf8($foreign_source) . '/nodes/' . URI::Escape::uri_escape_utf8($foreign_id), URI::Escape::uri_escape_utf8($key) . "=" . URI::Escape::uri_escape_utf8($value));
	} else {
		pod2usage(-exitval => 1, -message => "Unknown command: node $command", -verbose => 0);
	}
}

=item B<interface>

=over 8

=item B<interface add E<lt>foreign-sourceE<gt> E<lt>foreign-idE<gt> E<lt>ip-addressE<gt>>

Add an interface to the requisition identified by the given foreign source and node foreign ID.

=item B<interface remove E<lt>foreign-sourceE<gt> E<lt>foreign-idE<gt> E<lt>ip-addressE<gt>>

Remove an interface from the requisition identified by the given foreign source, foreign ID, and IP address.

=item B<interface set E<lt>foreign-sourceE<gt> E<lt>foreign-idE<gt> E<lt>ip-addressE<gt> E<lt>keyE<gt> [value]>

Set a property on an interface, given the foreign source, foreign id, and IP address.  Valid properties are:

=over 4

=item * descr - the interface description

=item * snmp-primary - P (primary), S (secondary), N (not eligible)

=item * status - 1 for managed, 3 for unmanaged (yes, I know)

=back

=back

=cut

sub cmd_interface {
	my @args = @_;

	my $command        = shift @args;
	my $foreign_source = shift @args;
	my $foreign_id     = shift @args;
	my $ip             = shift @args;

	if (not defined $foreign_source or $foreign_source eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify a foreign source!", -verbose => 0);
	}
	if (not defined $foreign_id or $foreign_id eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify a foreign id!", -verbose => 0);
	}
	if (not defined $ip or $ip !~ /^\d+\.\d+\.\d+\.\d+$/) {
		pod2usage(-exitval => 1, -message => "Error: You must specify a valid IP address!", -verbose => 0);
	}

	if (is_add($command)) {
		my $xml = get_element('interface');
		my $root = $xml->root;
		$root->{'att'}->{'ip-addr'} = $ip;
		post(URI::Escape::uri_escape_utf8($foreign_source) . "/nodes/ " . URI::Escape::uri_escape_utf8($foreign_id) . "/interfaces", $root);
	} elsif (is_remove($command)) {
		remove($foreign_source . '/nodes/' . $foreign_id . '/interfaces/' . $ip);
	} elsif (is_set($command)) {
		my $key     = shift @args;
		my $value   = shift @args;
		my $version = shift @args || 'v2c';

		if (not defined $key or $key eq "") {
			pod2usage(-exitval => 1, -message => "Error: You must specify a key!", -verbose => 0);
		}

		$key   = URI::Escape::uri_escape_utf8($key);
		$value = URI::Escape::uri_escape_utf8($value);

		put(URI::Escape::uri_escape_utf8($foreign_source) . '/nodes/' . URI::Escape::uri_escape_utf8($foreign_id) . '/interfaces/' . URI::Escape::uri_escape_utf8($ip), URI::Escape::uri_escape_utf8($key) . "=" . URI::Escape::uri_escape_utf8($value));
	} else {
		pod2usage(-exitval => 1, -message => "Unknown command: interface $command", -verbose => 0);
	}
}

=item B<service>

=over 8

=item B<service add E<lt>foreign-sourceE<gt> E<lt>foreign-idE<gt> E<lt>ip-addressE<gt> E<lt>service-nameE<gt>>

Add a service to the interface identified by the given foreign source, node ID, and IP address.

=item B<service remove E<lt>foreign-sourceE<gt> E<lt>foreign-idE<gt> E<lt>ip-addressE<gt> E<lt>service-nameE<gt>>

Remove a service from the interface identified by the given foreign source, node ID, and IP address.

=back

=cut

sub cmd_service {
	my @args = @_;

	my $command        = shift @args;
	my $foreign_source = shift @args;
	my $foreign_id     = shift @args;
	my $ip             = shift @args;
	my $service        = shift @args;

	if (not defined $foreign_source or $foreign_source eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify a foreign source!", -verbose => 0);
	}
	if (not defined $foreign_id or $foreign_id eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify a foreign id!", -verbose => 0);
	}
	if (not defined $ip or $ip !~ /^\d+\.\d+\.\d+\.\d+$/) {
		pod2usage(-exitval => 1, -message => "Error: You must specify a valid IP address!", -verbose => 0);
	}
	if (not defined $service or $service eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify a service!", -verbose => 0);
	}

	if (is_add($command)) {
		my $xml = get_element('monitored-service');
		my $root = $xml->root;
		$root->{'att'}->{'service-name'} = $service;
		post(URI::Escape::uri_escape_utf8($foreign_source) . "/nodes/" . URI::Escape::uri_escape_utf8($foreign_id) . "/interfaces/" . URI::Escape::uri_escape_utf8($ip) . "/services", $root);
	} elsif (is_remove($command)) {
		remove($foreign_source . '/nodes/' . $foreign_id . '/interfaces/' . $ip . '/services/' . $service);
	} else {
		pod2usage(-exitval => 1, -message => "Unknown command: service $command", -verbose => 0);
	}
}

=item B<category>

=over 8

=item B<category add E<lt>foreign-sourceE<gt> E<lt>foreign-idE<gt> E<lt>category-nameE<gt>>

Add a category to the node identified by the given foreign source and node foreign ID.

=item B<category remove E<lt>foreign-sourceE<gt> E<lt>foreign-idE<gt> E<lt>category-nameE<gt>>

Remove a category from the node identified by the given foreign source and node foreign ID.

=back

=cut

sub cmd_category {
	my @args = @_;

	my $command        = shift @args;
	my $foreign_source = shift @args;
	my $foreign_id     = shift @args;
	my $category       = shift @args;

	if (not defined $foreign_source or $foreign_source eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify a foreign source!", -verbose => 0);
	}
	if (not defined $foreign_id or $foreign_id eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify a foreign id!", -verbose => 0);
	}
	if (not defined $category or $category eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify a category!", -verbose => 0);
	}

	if (is_add($command) or is_set($command)) {
		my $xml = get_element('category');
		my $root = $xml->root;
		$root->{'att'}->{'name'} = $category;
		post(URI::Escape::uri_escape_utf8($foreign_source) . "/nodes/" . URI::Escape::uri_escape_utf8($foreign_id) . "/categories", $root);
	} elsif (is_remove($command)) {
		remove("$foreign_source/nodes/$foreign_id/categories/$category");
	} else {
		pod2usage(-exitval => 1, -message => "Unknown command: category $command", -verbose => 0);
	}
}

=item B<asset>

=over 8

=item B<asset add E<lt>foreign-sourceE<gt> E<lt>foreign-idE<gt> E<lt>keyE<gt> [value]>

Add an asset to the node identified by the given foreign source and node foreign ID.

=item B<asset remove E<lt>foreign-sourceE<gt> E<lt>foreign-idE<gt> E<lt>keyE<gt>>

Remove an asset from the node identified by the given foreign source and node foreign ID.

=item B<asset set E<lt>foreign-sourceE<gt> E<lt>foreign-idE<gt> E<lt>keyE<gt> E<lt>valueE<gt>>

Set an asset value given the node foreign source, foreign ID, and asset key.

=back

=cut

sub cmd_asset {
	my @args = @_;

	my $command        = shift @args;
	my $foreign_source = shift @args;
	my $foreign_id     = shift @args;
	my $key            = shift @args;

	if (not defined $foreign_source or $foreign_source eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify a foreign source!", -verbose => 0);
	}
	if (not defined $foreign_id or $foreign_id eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify a foreign id!", -verbose => 0);
	}
	if (not defined $key or $key eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify an asset key!", -verbose => 0);
	}

	if (is_add($command) or is_set($command)) {
		my $value = shift @args;

		my $xml = get_element('asset');
		my $root = $xml->root;
		$root->{'att'}->{'name'}  = $key;
		$root->{'att'}->{'value'} = $value;
		post(URI::Escape::uri_escape_utf8($foreign_source) . "/nodes/" . URI::Escape::uri_escape_utf8($foreign_id) . "/assets", $root);
	} elsif (is_remove($command)) {
		remove("$foreign_source/nodes/$foreign_id/assets/$key");
	} else {
		pod2usage(-exitval => 1, -message => "Unknown command: asset $command", -verbose => 0);
	}
}

=item B<snmp>

=over 8

=item B<snmp get E<lt>ip-addressE<gt>>

Get the SNMP configuration for the given IP address.

=item B<snmp netsnmp E<lt>ip-addressE<gt>>

Get the SNMP configuration for the given IP address, formatted for use in
shell backticks with the utilities from the Net-SNMP package.

=item B<snmp set E<lt>ip-addressE<gt> E<lt>communityE<gt> [options...]>

Set the SNMP community (and, optionally, version) for the given IP address.

Optionally, you can set additional options as key=value pairs.  For example:

	snmp set 192.168.0.1 public version=v1 timeout=1000

Valid options are:

=over 4

=item * version: v1 or v2c or v3

=item * port: the port of the SNMP agent

=item * timeout: the timeout, in milliseconds

=item * retries: the number of retries before giving up

=item * max-repetitions: maximum repetitions (defaults to 2)

=item * max-vars-per-pdu: maximum variables per PDU (defaults to 10)

=back

SNMPv3 options:

=over 4

=item * security-name: the USM name

=item * security-level: 1, 2, 3

=over 4

=item * 1: noAuthNoPriv (default)

=item * 2: authNoPriv

=item * 3: authPriv

=back

=item * priv-protocol: DES, AES, AES192, AES256

=item * priv-pass-phrase: the password for privacy protocol

=item * auth-protocol: MD5, SHA

=item * auth-pass-phrase: the password for the authentication protocol

=item * engine-id: the unique engine ID of the SNMP agent

=item * context-engine-id: the context ending ID

=item * context-name: the context name

=item * enterprise-id: the enterprise ID

=back

=back

=cut

sub cmd_snmp {
	my @args = @_;

	my $command   = shift @args;
	my $ip        = shift @args;

	if (not defined $ip or $ip eq "") {
		pod2usage(-exitval => 1, -message => "Error: You must specify an IP address!", -verbose => 0);
	}

	if ($command eq 'get' or $command eq 'list') {
		my $response = get(URI::Escape::uri_escape_utf8($ip), '/snmpConfig');
		$XML->parse($response->content);
		my $root = $XML->root;
		print "SNMP Configuration for $ip:\n";
		for my $child ($root->children) {
			print "* ", $child->tag, ": ", $child->text, "\n";
		}
	} elsif ($command eq 'netsnmp') {
		my $response = get(URI::Escape::uri_escape_utf8($ip), '/snmpConfig');
		$XML->parse($response->content);
		my $root = $XML->root;
		my $versionSwitch = "-" . $root->first_child('version')->text;
		my $communitySwitch = "-c " . $root->first_child('community')->text;
		my $timeoutSwitch = "-t " . int($root->first_child('timeout')->text / 1000 + 0.5);
		my $retrySwitch = "-r " . $root->first_child('retries')->text;
		print "${versionSwitch} ${communitySwitch} ${timeoutSwitch} ${retrySwitch}"
	} elsif (is_set($command)) {
		my $community = shift @args;
		if (not defined $community or $community eq "") {
			pod2usage(-exitval => 1, -message => "Error: You must specify an SNMP community string!", -verbose => 0);
		}
		my $arguments = "community=" . URI::Escape::uri_escape_utf8($community);

		for my $arg (@args) {
			my ($key, $value) = split(/=/, $arg);
			$arguments .= "&" . URI::Escape::uri_escape_utf8($key) . "=" . URI::Escape::uri_escape_utf8($value);
		}
		put(URI::Escape::uri_escape_utf8($ip), $arguments, '/snmpConfig');
	} else {
		pod2usage(-exitval => 1, -message => "Unknown command: snmp $command", -verbose => 0);
	}
}

sub is_add {
	my $command = shift;
	return ($command eq 'add' or $command eq 'create');
}

sub is_remove {
	my $command = shift;
	return ($command eq 'remove' or $command eq 'delete' or $command eq 'del' or $command eq 'rm');
}

sub is_set {
	my $command = shift;
	return ($command eq 'set' or $command eq 'put');
}

sub get_element {
	my $root = shift;

	my $xml = XML::Twig->new();
	$xml->set_root(XML::Twig::Elt->new($root));
	return $xml;
}

sub set_up_environment {
	$BROWSER = LWP::UserAgent->new(agent => "provision.pl/$BUILD");

	mkpath($ENV{'HOME'} . '/.opennms');
	$BROWSER->cookie_jar(
		HTTP::Cookies->new(
			file     => $ENV{'HOME'} . '/.opennms/rest-cookies.txt',
			autosave => 1,
		)
	);

	my $uri = URI->new($url_root);

	$BROWSER->credentials(
		$uri->host_port(),
		'OpenNMS Realm',
		$username => $password,
	);
}

sub get {
	my $path = shift;
	my $base = shift || '/requisitions';

	my $response = $BROWSER->get( $url_root . $base . '/' . $path );
	if ($response->is_success) {
		return $response;
	}
	croak($response->status_line);
}

sub put {
	my $path = shift;
	my $arguments = shift;
	my $base      = shift || '/requisitions';

	my $put = HTTP::Request->new(PUT => $url_root . $base . '/' . $path );
	$put->content_type('application/x-www-form-urlencoded');
	$put->content($arguments);
	my $response = $BROWSER->request($put);
	if ($response->is_redirect && $response->header('Location')) {
		return $response;
	}
	if ($response->is_success) {
		return $response;
	}
	croak($response->status_line);
}

sub put_simple {
	my $path = shift;
	my $args = shift;
	my $base = shift || '/requisitions';

	my $put = HTTP::Request->new(PUT => $url_root . $base . '/' . $path . (defined $args and $args ne '' ? '?' . $args : ''));
	my $response = $BROWSER->request($put);
	if ($response->is_redirect && $response->header('Location')) {
		return $response;
	}
	if ($response->is_success) {
		return $response;
	}
	croak($response->status_line);
}

sub remove {
	my $path = shift;
	my $base = shift || '/requisitions';

	my $delete = HTTP::Request->new(DELETE => $url_root . $base . '/' . $path );
	my $response = $BROWSER->request($delete);
	if ($response->is_redirect && $response->header('Location')) {
		return $response;
	}
	if ($response->is_success) {
		return $response;
	}
	croak($response->status_line);
}

sub post {
	my $path      = shift;
	my $twig      = shift;
	my $base      = shift || '/requisitions';
	my $namespace = shift || 'http://xmlns.opennms.org/xsd/config/model-import';

	$twig->{'att'}->{'xmlns'} = $namespace;
	my $post = HTTP::Request->new(POST => $url_root . $base . '/' . $path );
	$post->content_type('application/xml');
	$post->content($twig->sprint);
	my $response = $BROWSER->request($post);
	if ($response->is_redirect && $response->header('Location')) {
		return $response;
	}
	if ($response->is_success) {
		return $response;
	}
	croak($response->status_line);
}

sub dump_xml {
	my $content = shift;

	$XML->parse($content);
	if ($content =~ m/[<]requisitions/) {
		dump_requisitions($XML->root);
	} else {
		dump_requisition($XML->root);
	}
	#$XML->flush;
}

sub dump_requisitions {
	my $xml = shift;

	for my $child ($xml->children) {
		dump_requisition($child);
	}
}

sub dump_requisition {
	my $xml = shift;

	print "* ", $xml->{'att'}->{'foreign-source'}, " (last updated: ", $xml->{'att'}->{'date-stamp'}, ")\n";
	print "  * nodes:\n" if ($xml->children);
	for my $node ($xml->children) {
		dump_node($node);
	}
}

sub dump_node {
	my $node = shift;

	my @parent_info;
	push (@parent_info, "foreign Label: " . $node->{'att'}->{'parent-node-label'}) if $node->{'att'}->{'parent-node-label'};
	push (@parent_info, "foreign ID: " . $node->{'att'}->{'parent-foreign-id'}) if $node->{'att'}->{'parent-foreign-id'};
	push (@parent_info, "foreign Source: " . $node->{'att'}->{'parent-foreign-source'}) if $node->{'att'}->{'parent-foreign-source'};
	print ("    * ", $node->{'att'}->{'node-label'}, " (foreign ID: ", $node->{'att'}->{'foreign-id'}, ")\n");
	print ("      * parent: (", join(", ", @parent_info), ")\n") if ($node->{'att'}->{'parent-node-label'} or $node->{'att'}->{'parent-foreign-id'});
	print ("      * city: ", $node->{'att'}->{'city'}, "\n") if ($node->{'att'}->{'city'});
	print ("      * building: ", $node->{'att'}->{'building'}, "\n") if ($node->{'att'}->{'building'});
	print ("      * assets:\n") if ($node->descendants('asset'));
	for my $asset ($node->descendants('asset')) {
		dump_asset($asset);
	}
	print ("      * categories:\n") if ($node->descendants('category'));
	for my $category ($node->descendants('category')) {
		dump_category($category);
	}
	print ("      * interfaces:\n") if ($node->descendants('interface'));
	for my $interface ($node->descendants('interface')) {
		dump_interface($interface);
	}
}

sub dump_asset {
	my $asset = shift;
	print "        * ", $asset->{'att'}->{'name'}, "=", $asset->{'att'}->{'value'}, "\n";
}

sub dump_category {
	my $category = shift;
	print "        * ", $category->{'att'}->{'name'}, "\n";
}

sub dump_interface {
	my $interface = shift;

	print("        * ", $interface->{'att'}->{'ip-addr'});
	print(" (", $interface->{'att'}->{'descr'}, ")") if ($interface->{'att'}->{'descr'});
	print "\n";
	print("          * services:\n") if ($interface->descendants('monitored-service'));
	for my $service ($interface->descendants('monitored-service')) {
		print("            * " . $service->{'att'}->{'service-name'} . "\n");
	}
	print("          * SNMP Primary: ", $interface->{'att'}->{'snmp-primary'}, "\n") if ($interface->{'att'}->{'snmp-primary'});
	print("          * Status: ", $interface->{'att'}->{'status'}, "\n") if ($interface->{'att'}->{'status'});

}

sub print_version {
	printf("%s build %d\n", (split('/', $0))[-1], $BUILD);
	exit 0;
}

sub load_user_config {
	my $have_config = open USERCONFIG, "<${user_config_file}";
	return if (! defined $have_config);
	while (my $configline = <USERCONFIG>) {
		eval $configline;
	}
	close USERCONFIG;
}

=back

=head1 DESCRIPTION

B<This program> provides an interface to the RESTful API of the
provisioner, available in OpenNMS 1.8 and higher.  It mimics the
Requisition editor in the "Manage Provisioning Groups" portion
of the administrative UI.

=head1 AUTHOR

Benjamin Reed <ranger@opennms.org>

=head1 COPYRIGHT AND DISCLAIMER

Copyright 2009, The OpenNMS Group, Inc.  All rights reserved.

OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
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

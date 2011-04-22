#!/usr/bin/env perl

use Cwd qw(abs_path);
use File::Basename qw(dirname);

# include script functions
use vars qw(
	$PREFIX
);
$PREFIX = abs_path(dirname($0));
require($PREFIX . "/functions.pl");

my $git=`which git 2>/dev/null`;
chomp($git);
if ($git eq "" or not -x $git) {
	error("Unable to locate git ($git)");
	exit 1;
}

my @command = ($git, "clean", "-fdx", ".");
info("running:", @command);
handle_errors_and_exit_on_failure(system(@command));

my $hostname = `/bin/hostname`;
chomp($hostname);

if ($hostname eq "nen") {
	unshift(@ARGS, "-DaltDeploymentRepository=opennms-snapshot::default::file:///var/www/sites/opennms.org/site/repo/snapshots");
}

@command = ($MVN, @ARGS);
info("running:", @command);
handle_errors_and_exit(system(@command));

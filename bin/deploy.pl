#!/usr/bin/env perl

use Cwd qw(abs_path);
use File::Basename qw(dirname);

# include script functions
use vars qw(
	$PREFIX
	$TESTS
);
$TESTS = 1;
$PREFIX = abs_path(dirname($0));
require($PREFIX . "/functions.pl");

if (not defined $GIT) {
	exit 1;
}

clean_git() unless (exists $ENV{'SKIP_CLEAN'});

my $hostname = `hostname 2>/dev/null`;
chomp($hostname);
if ($hostname eq "repo.opennms.org") {
	# special case, on nen we use the local repo
	unshift(@ARGS, "-DaltDeploymentRepository=opennms-snapshot::default::file:///var/www/sites/opennms.org/site/repo/snapshots");
}

my @command = ($MVN, '-Dmaven.test.skip.exec=true', @ARGS, 'deploy');
info("running:", @command);
handle_errors_and_exit_on_failure(system(@command));

chdir($PREFIX . '/opennms-assemblies');
@command = ($MVN, '-Dmaven.test.skip.exec=true', '-N', @ARGS, 'deploy');
info("running:", @command);
handle_errors_and_exit_on_failure(system(@command));

clean_git() unless (exists $ENV{'SKIP_CLEAN'});

exit 0;

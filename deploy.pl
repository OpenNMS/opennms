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

clean_git();

for my $module (@ARGS) {
	my @other_args = ();

	my $hostname = `hostname 2>/dev/null`;
	chomp($hostname);
	if ($hostname eq "nen") {
		# special case, on nen we use the local repo
		push(@other_args, "-DaltDeploymentRepository=opennms-snapshot::default::file:///var/www/sites/opennms.org/site/repo/snapshots");
	}

	if ($module =~ /^-/) {
		push(@other_args, $module);
		next;
	}

	my $moduledir = $PREFIX . "/" . $module;
	if (-d $moduledir) {
		my $deps = get_dependencies($module);
	
		my %realdeps = ('org.opennms:opennms' => 1);
	
		for my $project (keys %$deps) {
			debug("project = $project");
			for my $dep (@{$deps->{$project}}) {
				debug("dep = $dep");
				$realdeps{$dep}++;
			}
		}

		chdir($PREFIX);

		# first, we install the deps
		my @command = ($MVN, '--projects', join(',', sort(keys(%realdeps))), '-Dmaven.test.skip.exec=true', @other_args, 'install');
		info("running:", @command);
		handle_errors_and_exit_on_failure(system(@command));

		# then, we deploy
		chdir($moduledir);
		@command = ($MVN, '-Dmaven.test.skip.exec=true', @other_args, 'deploy');
		info("running:", @command);
		handle_errors_and_exit_on_failure(system(@command));
	} else {
		error("directory $module does not exist in $PREFIX!");
		exit 1;
	}
}

clean_git();

exit 0;

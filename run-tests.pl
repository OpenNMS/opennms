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

my @command = ($MVN, '-Dmaven.test.skip.exec=true', '-N', 'install');
info("running:", @command);
handle_errors_and_exit_on_failure(system(@command));

my @other_args = ();
for my $module (@ARGS) {
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

		if ($module =~ /assembl/) {
			my @command = ($MVN, '-Dmaven.test.skip.exec=true', @other_args, 'install');
			info("running:", @command);
			handle_errors_and_exit_on_failure(system(@command));
			chdir($PREFIX . "/opennms-full-assembly");
			my @command = ($MVN, '-Dmaven.test.skip.exec=true', @other_args, 'install');
			info("running:", @command);
			handle_errors_and_exit_on_failure(system(@command));
		} else {
			my @command = ($MVN, '--projects', join(',', sort(keys(%realdeps))), '-Dmaven.test.skip.exec=true', @other_args, 'install');
			info("running:", @command);
			handle_errors_and_exit_on_failure(system(@command));
		}

		chdir($moduledir);
		my @command = ($MVN, @other_args, 'install');
		info("running:", @command);
		handle_errors_and_exit_on_failure(system(@command));
	} else {
		error("directory $module does not exist in $PREFIX!");
		exit 1;
	}
}

exit 0;

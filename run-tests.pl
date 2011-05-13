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

my $working_dir = $PREFIX;
if (open (FILEIN, "$PREFIX/pom.xml")) {
	my @modules = ();
	while (my $line = <FILEIN>) {
		if ($line =~ /<module>\s*([^<]+?)\s*<\/module>/) {
			push(@modules, $1);
		}
	}
	close(FILEIN) or warning("unable to close pom.xml: $!");

	for my $module (@ARGS) {
		next if ($module =~ /^-/);
		if (not grep { $_ eq $module } @modules) {
			$working_dir = "opennms-full-assembly";
			debug("$module is not in the main build, assuming we should pre-build the assembly instead of top-level.");
			last;
		}
	}
} else {
	error("unable to read from pom.xml: $!");
	exit 1;
}

chdir($working_dir);
my @command = ($MVN, '-Dmaven.test.skip.exec=true', '-P!jspc', 'install');
info("running:", @command);
handle_errors_and_exit_on_failure(system(@command));
chdir($PREFIX);

my @other_args = ();
for my $module (@ARGS) {
	if ($module =~ /^-/) {
		push(@other_args, $module);
		next;
	}

	my $moduledir = $PREFIX . "/" . $module;
	if (-d $moduledir) {
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

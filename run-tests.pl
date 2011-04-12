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

my $git=`which git 2>/dev/null`;
chomp($git);
if ($git eq "" or not -x $git) {
	error("Unable to locate git ($git)");
	exit 1;
}

my @command = ($git, "clean", "-fdx", ".");
info("running:", @command);
handle_errors_and_exit_on_failure(system(@command));

my @other_args = ();

@SKIP = qw(
	org\.opennms\:jicmp-api
	org\.opennms\:jrrd-api
	org\.opennms\:rancid-api
	org\.opennms\.lib
	org\.opennms\.smslib\:smslib
);

for my $module (@ARGS) {
	if ($module =~ /^-/) {
		push(@other_args, $module);
		next;
	}
	my $moduledir = $PREFIX . "/" . $module;
	if (-d $moduledir) {
		my %deps = ('org.opennms:opennms' => 1);
		my $in_module_list = 0;
		chdir($moduledir);
		open(MVNRUN, "$MVN dependency:list |") or die "unable to run $MVN dependency:list in $moduledir: $!";
		LIST: while (my $line = <MVNRUN>) {
			chomp($line);
			$line =~ s/^\[[^\]]*\]\s*//;
			if ($in_module_list) {
				if ($line =~ /^$/) {
					$in_module_list = 0;
				} elsif ($line !~ /opennms/) {
					# skip non-opennms dependencies
				} else {
					for my $skip (@SKIP) {
						if ($line =~ /$skip/) {
							next LIST;
						}
					}
					my ($dep) = $line =~ /^\s*([^\:]*\:[^\:]*)/;
					$deps{$dep} = 1;
				}
			} else {
				if ($line =~ /The following files have been resolved/) {
					$in_module_list = 1;
				}
			}
		}
		close(MVNRUN);
		chdir($PREFIX);

		if (keys(%deps) == 0) {
			info("no dependencies found, skipping compile");
		} else {
			if ($module =~ /assembl/) {
				my @command = ($MVN, '-Dmaven.test.skip.exec=true', @other_args, 'install');
				info("running:", @command);
				handle_errors_and_exit_on_failure(system(@command));
				chdir($PREFIX . "/opennms-full-assembly");
				my @command = ($MVN, '-Dmaven.test.skip.exec=true', @other_args, 'install');
				info("running:", @command);
				handle_errors_and_exit_on_failure(system(@command));
			} else {
				my @command = ($MVN, '--projects', join(',', sort(keys(%deps))), '-Dmaven.test.skip.exec=true', @other_args, 'install');
				info("running:", @command);
				handle_errors_and_exit_on_failure(system(@command));
			}
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

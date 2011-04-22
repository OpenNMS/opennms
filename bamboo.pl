#!/usr/bin/env perl

use Cwd qw(abs_path);
use File::Basename qw(dirname);

# include script functions
use vars qw(
	$PREFIX
);
$PREFIX = abs_path(dirname($0));
require($PREFIX . "/functions.pl");

if (not defined $GIT) {
	exit 1;
}

my @command = ($GIT, "clean", "-fdx", ".");
info("running:", @command);
handle_errors_and_exit_on_failure(system(@command));

@command = ($MVN, @ARGS);
info("running:", @command);
handle_errors_and_exit(system(@command));

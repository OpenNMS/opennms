#!/usr/bin/env perl

use Cwd qw(abs_path);
use File::Basename qw(dirname);

# include script functions
use vars qw(
	$PREFIX
);
$PREFIX = abs_path(dirname($0));
require($PREFIX . "/functions.pl");


if (not grep { $_ =~ /^[^-]/ } @ARGS) {
	debug("no maven targets specified, adding 'install' to the command-line");
	push(@ARGS, "install");
}

my @command = ($MVN, @ARGS);
info("JAVA_HOME = $JAVA_HOME");
info("MAVEN_OPTS = $MAVEN_OPTS");
info("running:", @command);
system(@command);
handle_errors_and_exit($?);

#!/usr/bin/env perl

use Cwd qw(abs_path);
use File::Basename qw(dirname);

# include script functions
use vars qw(
	$PREFIX
);
$PREFIX = abs_path(dirname($0));
require($PREFIX . "/bin/functions.pl");


my @command = ($MVN, 'clean');
info("running in $PREFIX:", @command);
my $exit = handle_errors(system(@command));

if ($exit != 0) {
	exit ($exit >> 8);
}

chdir($PREFIX . "/opennms-full-assembly");
info("running in $PREFIX/opennms-full-assembly:", @command);
handle_errors_and_exit(system(@command));

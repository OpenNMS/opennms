#!/usr/bin/env perl

use Cwd qw(abs_path);
use File::Basename qw(dirname);

# include script functions
use vars qw(
	$PREFIX
);
$PREFIX = abs_path(dirname($0));
require($PREFIX . "/bin/functions.pl");


my @command = ($MVN, '-Passemblies', 'clean');
info("running in $PREFIX:", @command);
handle_errors_and_exit(system(@command));

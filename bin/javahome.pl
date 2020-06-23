#!/usr/bin/env perl

use Cwd qw(abs_path);
use File::Basename qw(dirname);

# include script functions
use vars qw(
	$LOGLEVEL
	$PREFIX
);
$LOGLEVEL = 'error';
$PREFIX = abs_path(dirname($0));

require($PREFIX . "/functions.pl");

print STDOUT $JAVA_HOME, "\n";

exit 0;

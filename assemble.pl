#!/usr/bin/env perl

use Cwd qw(abs_path);
use File::Basename qw(dirname);
use File::Spec;

# include script functions
use vars qw(
	$PREFIX
);
$PREFIX = abs_path(dirname($0));
require(File::Spec->catfile($PREFIX, 'bin', 'functions.pl'));

@profiles = ('default', 'full', 'dir');
my $assembly = File::Spec->catdir($PREFIX, 'opennms-full-assembly');
my $pomfile = File::Spec->catfile($assembly, 'pom.xml');
if (-f $pomfile) {
	if (open (FILEIN, $pomfile)) {
		@profiles = ();
		my $lastline = "";
		while (my $line = <FILEIN>) {
			chomp($line);
			if ($lastline =~ /<name>build.profile<\/name>/) {
				if ($line =~ /<value>(.*?)<\/value>/) {
					push(@profiles, $1);
				}
			}
			$lastline = $line;
		}
		close(FILEIN);
	} else {
		warning("unable to read from $pomfile: $!");
	}
}

if (not grep { $_ =~ /^-Dbuild.profile=/ } @ARGS) {
	info("No build profile set, using the default.  Possible profiles are: " . join(", ", @profiles));
	push(@ARGS, '-Dbuild.profile=default');
}

if (not grep { $_ =~ /^[^-]/ } @ARGS) {
	debug("no maven targets specified, adding 'install' to the command-line");
	push(@ARGS, "install");
}

my @command = ($MVN, @ARGS);
info("changing working directory to $assembly");
chdir($assembly);
info("running:", @command);
handle_errors_and_exit(system(@command));

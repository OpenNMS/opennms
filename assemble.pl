#!/usr/bin/env perl

use Cwd qw(abs_path);
use File::Basename qw(dirname);

# include script functions
use vars qw(
	$PREFIX
);
$PREFIX = abs_path(dirname($0));
require($PREFIX . "/bin/functions.pl");

@profiles = ('default', 'full', 'dir');
if (-f $PREFIX . "/opennms-full-assembly/pom.xml") {
	if (open (FILEIN, $PREFIX . "/opennms-full-assembly/pom.xml")) {
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
		warning("unable to read from $PREFIX/opennms-full-assembly/pom.xml: $!");
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
info("changing working directory to $PREFIX/opennms-full-assembly");
chdir($PREFIX . "/opennms-full-assembly");
info("running:", @command);
handle_errors_and_exit(system(@command));

#!/usr/bin/perl -w

use 5.008008;
use strict;
use warnings;

use Carp;

use OpenNMS::Config;
use OpenNMS::Config::Git;

our $OPENNMS_HOME = shift @ARGV;
our $PACKAGE      = shift @ARGV;
our $VERSION      = shift @ARGV;

if (not defined $PACKAGE) {
	croak "usage: $0 <\$OPENNMS_HOME> <rpm_package_name> <rpm_package_version>\n";
}

my $config      = OpenNMS::Config->new($OPENNMS_HOME);
my $version     = $config->existing_version($PACKAGE);
my $pristinedir = $config->pristine_dir();
my $etcdir      = $config->etc_dir();

print STDERR "=" x 80, "\n";
print STDERR "$0 $OPENNMS_HOME $PACKAGE $VERSION\n";
system('rpm', '--verify', $PACKAGE);
print STDERR "=" x 80, "\n";

my $git = OpenNMS::Config::Git->new($etcdir);
$git->author('OpenNMS Git Auto-Upgrade <' . $0 . '>');

my $current_branch = $git->get_branch_name();

if ($current_branch eq $config->runtime_branch()) {
	my @modifications = $git->get_modifications();
	if (@modifications > 0) {
		print STDERR "We are on the " . $config->runtime_branch() . " branch, but there are unexpected modifications! Something went wrong.";
		for my $mod (@modifications) {
			print STDERR "  modified: ", $mod->file(), "\n";
		}
		print STDERR "\n";
		exit 1;
	}
	print "Already on runtime branch. Assuming it is safe to exit.";
	exit 0;
}

if ($current_branch ne $config->pristine_branch()) {
	croak "Expected " . $config->pristine_branch() . ' branch, but current branch is ' . $current_branch . '. Bailing.';
}

$git->commit_modifications("Pristine configuration for $PACKAGE $VERSION");
$git->tag($config->get_tag_name("pristine-modifications-$PACKAGE-$VERSION"));

$git->checkout($config->runtime_branch());

$git->merge($config->pristine_branch());

$git->tag($config->get_tag_name("merged-modifications-$PACKAGE-$VERSION"));

exit 0;
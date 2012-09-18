#!/usr/bin/perl -w

use 5.008008;
use strict;
use warnings;

use Carp;

use OpenNMS::Config;
use OpenNMS::Config::Git;

our ($config, $version, $pristinedir, $etcdir, $rpm_name, $rpm_version) = OpenNMS::Config->setup($0, @ARGV);

my $git = OpenNMS::Config::Git->new($etcdir);
$git->author('OpenNMS Git Auto-Upgrade <' . $0 . '>');

my $current_branch = $git->get_branch_name();

if ($current_branch eq $config->pristine_branch()) {
	my @modifications = $git->get_modifications();
	if (@modifications > 0) {
		print STDERR "We are on the " . $config->pristine_branch() . " branch, but there are unexpected modifications! Something went wrong.";
		for my $mod (@modifications) {
			print STDERR "  modified: ", $mod->file(), "\n";
		}
		print STDERR "\n";
		exit 1;
	}
	print "Already on pristine branch. Assuming it is safe to exit.";
	exit 0;
}

if ($current_branch ne $config->runtime_branch()) {
	croak "Expected " . $config->runtime_branch() . ' branch, but current branch is ' . $current_branch . '. Bailing.';
}

$git->commit_modifications("user modifications to $rpm_name, version $version");

$git->tag($config->get_tag_name("pre-$rpm_name-$rpm_version"));
$git->checkout($config->pristine_branch());

exit 0;
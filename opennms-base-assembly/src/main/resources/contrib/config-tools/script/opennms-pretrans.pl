#!/usr/bin/perl -w

use 5.008008;
use strict;
use warnings;

use Carp;

use OpenNMS::Config;
use OpenNMS::Config::Git;

our ($config, $version, $pristinedir, $etcdir, $rpm_name, $rpm_version) = OpenNMS::Config->setup($0, @ARGV);

my $git = OpenNMS::Config::Git->new($etcdir);
$git->author_name('OpenNMS Git Auto-Upgrade');
$git->author_email($0);

my $current_branch = $git->get_branch_name();
$config->log('checking current branch (', $current_branch, ')');

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
	croak "Expected " . $config->runtime_branch() . ' branch, but current branch is ' . $current_branch . '.  Bailing.';
}

if ($config->is_conflicted() and $git->get_modifications() > 0) {
	croak "We are in a conflicted mode, but there are uncommitted modifications.  Bailing.";
}

$config->log('committing user modifications (', $rpm_name, '-', $version, ')');
my $mods = $git->commit_modifications("user modifications to $rpm_name, version $version");
if ($mods == 0) {
	$config->log('(no changes to commit)');
}

my $revert_tag_runtime = $config->runtime_revert_tag($rpm_version);
if (not $git->tag_exists($revert_tag_runtime)) {
	$git->tag($revert_tag_runtime);
}

$config->log('tagging pre-upgrade');
$git->tag($config->get_tag_name("pre-$rpm_name-$rpm_version"));

$config->log('checking out ', $config->pristine_branch());
$git->checkout($config->pristine_branch());

my $revert_tag_pristine = $config->pristine_revert_tag($rpm_version);
if (not $git->tag_exists($revert_tag_pristine)) {
	$git->tag($revert_tag_pristine);
}

exit 0;

#!/usr/bin/perl -w

use 5.008008;
use strict;
use warnings;

use Carp;
use Error qw(:try);

use OpenNMS::Config;
use OpenNMS::Config::Git;

our ($config, $version, $pristinedir, $etcdir, $rpm_name, $rpm_version) = OpenNMS::Config->setup($0, @ARGV);

my $git = OpenNMS::Config::Git->new($etcdir);
$git->author_name('OpenNMS Git Auto-Upgrade');
$git->author_email($0);

my $current_branch = $git->get_branch_name();
$config->log('checking current branch (', $current_branch, ')');

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

$config->log('committing modifications to the pristine branch (', $rpm_name, '-', $rpm_version, ')');
my $mods = $git->commit_modifications("Pristine configuration for $rpm_name $rpm_version");
if ($mods == 0) {
	$config->log('(no changes to commit)');
}

$config->log('tagging pristine modifications');
$git->tag($config->get_tag_name("pristine-modifications-$rpm_name-$rpm_version"));

$config->log('checking out ', $config->runtime_branch());
$git->checkout($config->runtime_branch());

$config->log('merging ', $config->pristine_branch());
eval {
	$git->merge_or_fail($config->pristine_branch());
	$config->log('tagging merged modifications');
	$git->tag($config->get_tag_name("merged-modifications-$rpm_name-$rpm_version"));
};
if ($@) {
	$config->log($@);
	$config->log('reverting to pristine and saving user modifications');
	$git->reset($config->runtime_branch());

	my $runtime_branch      = $config->runtime_branch();
	my $pristine_branch     = $config->pristine_branch();
	my $compare_from        = $runtime_branch;
	my $compare_to          = $pristine_branch;

	my $revert_tag_pristine = $config->pristine_revert_tag($rpm_version);
	my $revert_tag_runtime  = $config->runtime_revert_tag($rpm_version);

	if ($git->tag_exists($revert_tag_pristine)) {
		$compare_to = $revert_tag_pristine;
	}
	if ($git->tag_exists($revert_tag_runtime)) {
		$compare_from = $revert_tag_runtime;
	}

	$git->save_changes_between($compare_from, $compare_to, $pristine_branch);

	$config->log('committing pristine changes back to the user branch');
	my $mods = $git->commit_modifications("Pristine revert for $rpm_name $rpm_version");
	if ($mods == 0) {
		$config->log('(no changes to commit)');
	}

	$config->create_conflicted();
};

exit 0;

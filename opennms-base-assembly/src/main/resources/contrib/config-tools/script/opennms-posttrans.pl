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

if ($current_branch ne $config->runtime_branch()) {
	croak "Expected " . $config->runtime_branch() . ' branch, but current branch is ' . $current_branch . '. Bailing.';
}

my @runtime_modifications = $git->get_modifications();

$git->checkout($config->pristine_branch());
my @modifications = $git->get_modifications();
my $do_commit = 0;
for my $modification (@modifications) {
	if ($modification->isa('OpenNMS::Config::Git::Remove')) {
		$modification->exec();
		$do_commit++;
	}
}
if ($do_commit) {
	$git->commit("Pristine deletes for $rpm_name $rpm_version");
}

$git->tag($config->get_tag_name("pristine-$rpm_name-$rpm_version"));
$git->checkout($config->runtime_branch());

for my $runmod (@runtime_modifications) {
	if ($runmod->isa('OpenNMS::Config::Git::Remove')) {
		unlink(File::Spec->catfile($etcdir, $runmod->file()));
	}
}

$git->commit_modifications("Post-transaction runtime changes for $rpm_name-$rpm_version");

$git->merge($config->pristine_branch());

$git->tag($config->get_tag_name("merged-$rpm_name-$rpm_version"));

exit 0;
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
	$git->commit("Pristine deletes for $PACKAGE $VERSION");
}

$git->tag($config->get_tag_name("pristine-$PACKAGE-$VERSION"));
$git->checkout($config->runtime_branch());

for my $runmod (@runtime_modifications) {
	if ($runmod->isa('OpenNMS::Config::Git::Remove')) {
		unlink(File::Spec->catfile($etcdir, $runmod->file()));
	}
}

$git->commit_modifications("Post-transaction runtime changes for $PACKAGE-$VERSION");

$git->merge($config->pristine_branch());

$git->tag($config->get_tag_name("merged-$PACKAGE-$VERSION"));

exit 0;
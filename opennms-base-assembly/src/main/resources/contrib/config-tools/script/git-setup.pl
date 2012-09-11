#!/usr/bin/perl -w

use strict;
use warnings;

use Carp;
use File::Copy;
use File::Path;
use File::Spec;
use IO::Handle;

use OpenNMS::Config::Git;

our $OPENNMS_HOME    = shift @ARGV;
our $PRISTINE_BRANCH = 'pristine';
our $CONFIG_BRANCH   = 'master';

our $EXISTING_VERSION = `rpm -q --queryformat='\%{version}-\%{release}' opennms-core 2>/dev/null`;
chomp($EXISTING_VERSION);
$EXISTING_VERSION = "0.0-0" if ($EXISTING_VERSION eq '');

our $pristinedir = File::Spec->catdir($OPENNMS_HOME, "share", "etc-pristine");
our $etcdir      = File::Spec->catdir($OPENNMS_HOME, "etc");

# add other checks to be sure we really made this
if (-d File::Spec->catdir($etcdir, '.git')) {
	print STDERR "git is already set up\n";
	exit 0;
}

mkpath($pristinedir);
mkpath($etcdir);

our $git = OpenNMS::Config::Git->new($pristinedir);
$git->author('OpenNMS Git Setup <' . $0 . '>');
$git->init(branch_name => $PRISTINE_BRANCH);

# create .gitignore and commit to the pristine branch
my $gitignore = IO::Handle->new();
open ($gitignore, '>', File::Spec->catfile($pristinedir, '.gitignore')) or croak "unable to write to .gitignore in $etcdir: $!";
print $gitignore "*.jasper\n";
print $gitignore "*.rpmnew\n";
print $gitignore "*.rpmorig\n";
print $gitignore "*.rpmsave\n";
print $gitignore "examples\n";
print $gitignore "configured\n";
print $gitignore "libraries.properties\n";
close($gitignore) or croak "unable to close filehandle for .gitignore: $!";

$git->add('.gitignore')->commit(".gitignore for $PRISTINE_BRANCH branch");
if ($git->get_modified_files()) {
	$git->add('.')->commit("pristine configuration for OpenNMS $EXISTING_VERSION");
}
$git->create_branch($CONFIG_BRANCH, $PRISTINE_BRANCH);
$git->tag("pristine-$EXISTING_VERSION");
$git->checkout($CONFIG_BRANCH);

move(File::Spec->catfile($pristinedir, '.gitignore'), File::Spec->catfile($etcdir, '.gitignore'));
move(File::Spec->catdir($pristinedir, '.git'), File::Spec->catdir($etcdir, '.git'));

exit 0;
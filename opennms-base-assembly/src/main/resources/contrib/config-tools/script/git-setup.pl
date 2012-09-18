#!/usr/bin/perl -w

use 5.008008;
use strict;
use warnings;

use Carp;
use File::Copy;
use File::Path;
use File::Spec;
use IO::Handle;

use OpenNMS::Config;
use OpenNMS::Config::Git;

our ($config, $version, $pristinedir, $etcdir) = OpenNMS::Config->setup($0, @ARGV);

# add other checks to be sure we really made this
# TODO: this should do more detailed validation of existing .git directories
if (-d File::Spec->catdir($etcdir, '.git')) {
	print STDERR "git is already set up\n";
	exit 0;
}

mkpath($pristinedir);
mkpath($etcdir);

our $git = OpenNMS::Config::Git->new($pristinedir);
$git->author('OpenNMS Git Auto-Upgrade <' . $0 . '>');
$git->init(branch_name => $config->pristine_branch());

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

$git->add('.gitignore')->commit("initial commit for " . $config->pristine_branch() . " branch");
$git->commit_modifications("pristine configuration for OpenNMS $version");
$git->create_branch($config->runtime_branch(), $config->pristine_branch());
$git->tag($config->get_tag_name("pristine-$version"));
$git->checkout($config->runtime_branch());

move(File::Spec->catfile($pristinedir, '.gitignore'), File::Spec->catfile($etcdir, '.gitignore'));
move(File::Spec->catdir($pristinedir, '.git'), File::Spec->catdir($etcdir, '.git'));

exit 0;

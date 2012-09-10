#!/usr/bin/perl -w

use strict;
use warnings;

use Carp;
use File::Path;
use File::Spec;
use IO::Handle;

use Git;

our $OPENNMS_HOME    = shift @ARGV;
our $PRISTINE_BRANCH = 'pristine';
our $CONFIG_BRANCH   = 'master';

our $gitdir = File::Spec->catdir($OPENNMS_HOME, "etc");

git_cmd_try {
	Git::command_oneline('init', $gitdir);
} "Error \%d while running git init: \%s";

our $git = Git->repository(Directory => $gitdir);

# set up config
_git('init', $git, 'config', 'user.name', 'OpenNMS Git Config');
_git('init', $git, 'config', 'user.email', 'opennms@git');

# create empty pristine branch
_git('init', $git, 'symbolic-ref', 'HEAD', 'refs/heads/' . $PRISTINE_BRANCH);

# create .gitignore and commit to the pristine branch
my $gitignore = IO::Handle->new();
open ($gitignore, '>', File::Spec->catfile($gitdir, '.gitignore')) or croak "unable to write to .gitignore in $gitdir: $!";
print $gitignore "*.jasper\n";
print $gitignore "*.rpmnew\n";
print $gitignore "*.rpmorig\n";
print $gitignore "*.rpmsave\n";
print $gitignore "examples\n";
print $gitignore "configured\n";
print $gitignore "libraries.properties\n";
close($gitignore) or croak "unable to close filehandle for .gitignore: $!";

_git('init', $git, 'add', '.gitignore');
_git('init', $git, 'commit', '-m', ".gitignore for $PRISTINE_BRANCH branch");

# create a branch for the user's configuration
_git('init', $git, 'checkout', '-b', $CONFIG_BRANCH);
	
sub _git {
	my $source = shift;
	my $git    = shift;
	my @args   = @_;

	my $command_text = "git '" . join("' '", @args) . "'";
	debug($source . ':', "running: $command_text");
	my $retval = undef;
	git_cmd_try {
		$retval = $git->command(@args);
		debug($retval);
	} "Error \%d while running command: \%s";

	return $retval;
}

sub _output {
	my $level = shift;
	for my $line (split(/\r?\n/, join(' ', @_))) {
		print STDERR $level, ' ', $line, "\n";
	}
}

sub debug   { _output("[DEBUG]", @_); }
sub info    { _output("[INFO] ", @_); }
sub warning { _output("[WARN] ", @_); }
sub error   { _output("[ERROR]", @_); }

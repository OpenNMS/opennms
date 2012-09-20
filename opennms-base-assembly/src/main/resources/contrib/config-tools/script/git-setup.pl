#!/usr/bin/perl -w

use 5.008008;
use strict;
use warnings;

use Carp;
use File::Basename;
use File::Copy;
use File::Find;
use File::Path;
use File::Slurp;
use File::Spec;
use IO::Handle;

use OpenNMS::Config;
use OpenNMS::Config::Git;

our ($config, $version, $pristinedir, $etcdir, $rpm_name, $rpm_version) = OpenNMS::Config->setup($0, @ARGV);

our $etc_pretrans = File::Spec->catdir($config->home(), '.etc-pretrans');

if (-d $etc_pretrans) {
	$config->log('Found an ', $etc_pretrans, ' directory.  Moving its contents back to ', $etcdir);
	mkpath($etcdir);
	find({
		wanted => sub {
			return unless (-e $File::Find::name);
			my $relative = File::Spec->abs2rel($File::Find::name, $etc_pretrans);
			return unless (defined $relative and $relative ne "");
			my $targetdir = dirname($relative);
			my $target;
			mkpath(File::Spec->catdir($etcdir, $targetdir));
			if (-d $File::Find::name) {
				$target = File::Spec->catdir($etcdir, $relative);
			} else {
				$target = File::Spec->catfile($etcdir, $relative);
			}
			$config->log("moving ", $File::Find::name, " -> ", $target);
			move($File::Find::name, $target);
		}
	}, $etc_pretrans);
	rmtree($etc_pretrans);
}


my $version_override_file = File::Spec->catfile('/tmp', 'git-setup.' . $rpm_name);
if (-f $version_override_file and -s $version_override_file) {
	$version = $config->existing_version($rpm_name, read_file($version_override_file));
	unlink($version_override_file);
}

# add other checks to be sure we really made this
# TODO: this should do more detailed validation of existing .git directories
$config->log('checking if .git is already set up');
if (-d File::Spec->catdir($etcdir, '.git')) {
	print STDERR "git is already set up\n";
	exit 0;
}
$config->log("(it's not)");

if (not -d $pristinedir) {
	$pristinedir = File::Spec->catdir('/tmp', 'git-setup-empty-pristine');
}

$config->log('creating directories if necessary');
mkpath($pristinedir);
mkpath($etcdir);

$config->log('initializing git');
our $git = OpenNMS::Config::Git->new($pristinedir);
$git->author('OpenNMS Git Auto-Upgrade <' . $0 . '>');
$git->init(branch_name => $config->pristine_branch());

# create .gitignore and commit to the pristine branch
$config->log('creating .gitignore');
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

$config->log('committing pristine configuration');
$git->commit_modifications("pristine configuration for version $version");

$config->log('creating ', $config->runtime_branch(), ' branch');
$git->create_branch($config->runtime_branch(), $config->pristine_branch());

$config->log('tagging pristine-', $version);
$git->tag($config->get_tag_name("pristine-$version"));

$config->log('checking out ', $config->runtime_branch());
$git->checkout($config->runtime_branch());

$config->log('moving pristine files to ', $etcdir);
move(File::Spec->catfile($pristinedir, '.gitignore'), File::Spec->catfile($etcdir, '.gitignore'));
move(File::Spec->catdir($pristinedir, '.git'), File::Spec->catdir($etcdir, '.git'));

$git = OpenNMS::Config::Git->new($etcdir);
$git->author('OpenNMS Git Auto-Upgrade <' . $0 . '>');

$config->log('committing any initial user changes (if necessary)');
my $mods = $git->commit_modifications("user modifications to $rpm_name, version $version");
if ($mods == 0) {
	$config->log('(no changes to commit)');
}
$config->log('tagging setup-user-', $version);
$git->tag($config->get_tag_name("setup-user-$version"));

exit 0;

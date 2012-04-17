#!/usr/bin/perl -w

use strict;
use warnings;

use Cwd;
use Data::Dumper;
use File::Basename;
use File::Spec;
use Getopt::Long qw(:config gnu_getopt pass_through);
use Git;

our $OPENNMS_HOME;

sub find_opennms_home($);
sub debug(@);
sub info(@);
sub warn(@);
sub error(@);
sub usage();

### Find $OPENNMS_HOME

if (exists $ENV{'OPENNMS_HOME'}) {
	$OPENNMS_HOME = find_opennms_home($ENV{'OPENNMS_HOME'});
}

if (not defined $OPENNMS_HOME) {
	$OPENNMS_HOME = find_opennms_home(Cwd::abs_path($0));
}

if (not defined $OPENNMS_HOME) {
	error 'Unable to locate $OPENNMS_HOME or $OPENNMS_HOME not set.';
	exit 1;
}

### Process arguments

my $help = 0;

my $results = GetOptions(
	'h|help' => \$help,
);

if ($help) {
	usage();
	exit 1;
}

### Run command, if found in Commands::* namespace

my $command = shift @ARGV;

if (not defined $command) {
	usage();
	exit 1;
}

my $commands = \%Commands::;

if (not exists $commands->{$command}) {
	error 'Unknown command: ' . $command . ". Valid commands are:";
	for (sort keys %$commands) {
		if (defined *{$commands->{$_}}{CODE} and $_ !~ /^_/) {
			error '  ' . $_;
		}
	}
	exit 1;
}

$commands->{$command}(@ARGV);








sub find_opennms_home($) {
	my $topdir = shift;

	my $opennms_properties = File::Spec->catfile($topdir, 'etc', 'opennms.properties');

	if (-f $opennms_properties) {
		return Cwd::abs_path($topdir);
	}

	my $dirname = dirname($topdir);
	if ($dirname eq '/') {
		return undef;
	}

	return find_opennms_home($dirname);
}

sub _output($@) {
	my $level = shift;
	for my $line (split(/\r?\n/, join(' ', @_))) {
		print STDERR $level, ' ', $line, "\n";
	}
}

sub debug(@) { _output("[DEBUG]", @_); }
sub info(@)  { _output("[INFO] ", @_); }
sub warn(@)  { _output("[WARN] ", @_); }
sub error(@) { _output("[ERROR]", @_); }

sub usage() {
	print <<END;
usage: $0 [-h] <command>

  -h, --help      This help.

Valid commands:
* init            Initialize $OPENNMS_HOME/etc as a Git repository.
  * -v, --version The version of the configuration that is being initialized.

* store           Store current configuration changes in $OPENNMS_HOME/etc.

* upgrade         Upgrade the user's configuration using the latest pristine configs.

END
}

package Commands;

use Carp;
use Cwd;
use File::Basename;
use File::Copy;
use File::Path qw(make_path);
use File::Spec;
use File::Temp;
use Getopt::Long qw(:config gnu_getopt pass_through);
use Git;
use IO::Handle;

sub _recursive_copy($$);
sub init(@);


sub _recursive_copy($$) {
	my $source = shift;
	my $target = shift;

	if (basename($source) eq ".git") {
		main::debug '_recursive_copy: skipping .git dir';
		return;
	}

	main::debug '_recursive_copy: ' . $source . ' -> ' . $target;

	if (not -d $source) {
		main::error $source . ' is not a directory!';
		exit 1;
	}
	if (-e $target and not -d $target) {
		main::error $target . ' is not a directory!';
		exit 1;
	}

	make_path($target);

	my $rsync = `which rsync 2>/dev/null`;
	if ($? != 0) {
		main::debug 'Unable to locate `rsync` command.';
		exit 1;
	}
	chomp($rsync);

	system('rsync', '-ar', '--delete', '--exclude=.git', '--exclude=examples', Cwd::abs_path($source) . '/', Cwd::abs_path($target) . '/') == 0
		or croak "rsync failed: $!";
}

sub init(@) {
	my $version = undef;

	my $ret = Getopt::Long::GetOptionsFromArray(\@_,
		'v|version=s' => \$version
	);
	if (not defined $version) {
		main::error '-v version is a required argument when initializing an etc git repository!';
		exit 1;
	}

	### make sure expected paths exist

	my $etcdir      = File::Spec->catdir($OPENNMS_HOME, 'etc');
	my $gitdir      = File::Spec->catdir($etcdir, '.git');
	my $pristinedir = File::Spec->catdir($OPENNMS_HOME, 'share', 'etc-pristine');

	if (-d $gitdir) {
		main::error "'init' called, but $gitdir already exists!";
		exit 1;
	}
	if (! -d $etcdir) {
		main::error "\$OPENNMS_HOME is $OPENNMS_HOME, but $etcdir does not exist!";
		exit 1;
	}
	if (! -d $pristinedir) {
		main::error "\$OPENNMS_HOME is $OPENNMS_HOME, but $pristinedir does not exist!";
		exit 1;
	}

	main::info "Initializing Git in $etcdir.";

	#my $tempdir = File::Temp::tempdir( CLEANUP => 1 );
	my $tempdir = '/tmp/git-config';

	### initialize the (temporary) base git directory, using a copy of etc-pristine

	_recursive_copy($pristinedir, $tempdir);

	git_cmd_try {
		my $retval = Git::command_oneline('init', $tempdir);
		main::debug $retval;
	} "Error \%d while initializing git directory in $tempdir with command: \%s";

	my $git = Git->repository(Directory => $tempdir);

	git_cmd_try {
		my $retval = $git->command('add', '.');
		main::debug $retval;
	} "Error \%d while adding files to git in $tempdir with command: \%s";

	git_cmd_try {
		my $retval = $git->command('commit', '-m', 'initial commit, version: ' . $version);
		main::debug $retval;
	} "Error \%d while committing pristine configuration in $tempdir with command: \%s";

	git_cmd_try {
		my $retval = $git->command('tag', 'opennms-git-config-pristine-' . $version);
		main::debug $retval;
	} "Error \%d while tagging initial pristine configuration (version $version) in $tempdir with command: \%s";

	# create a branch for the user's configuration

	git_cmd_try {
		my $retval = $git->command('checkout', '-b', 'opennms-git-config-local');
		main::debug $retval;
	} "Error \%d while creating opennms-git-config-local branch in $tempdir with command: \%s";

	### copy the existing configuration files from etc into the working tree and commit

	_recursive_copy($etcdir, $tempdir);

	my $gitignore = IO::Handle->new();
	open ($gitignore, '>' . File::Spec->catfile($tempdir, '.gitignore')) or croak "unable to write to .gitignore in $tempdir: $!";
	print $gitignore "configured\n";
	print $gitignore "libraries.properties\n";
	close($gitignore) or croak "unable to close filehandle for .gitignore: $!";

	git_cmd_try {
		my $retval = $git->command('add', '.');
		main::debug $retval;
	} "Error \%d while adding files to git in $tempdir with command: \%s";

	my @delete = ();
	git_cmd_try {
		my ($fh, $ctx) = $git->command_output_pipe('status');
		while (<$fh>) {
			if (/^\#\s+deleted:\s+(.*?)\s*$/) {
				main::info "deleted: $1";
				push(@delete, $1);
			}
		}
		$git->command_close_pipe($fh, $ctx);
	} "Error \%d while getting status in $tempdir with command: \%s";

	for my $delete (@delete) {
		git_cmd_try {
			my $retval = $git->command('rm', '-f', $delete);
			main::debug $retval;
		} "Error \%d while git rm'ing $delete from $tempdir with command: \%s";
	}

	git_cmd_try {
		my $retval = $git->command('commit', '-m', 'user-modified configuration files for OpenNMS, version ' . $version);
		main::debug $retval;
	} "Error \%d while committing user changes to $tempdir with command: \%s";

	git_cmd_try {
		my $retval = $git->command('tag', 'opennms-git-config-user-' . $version);
		main::debug $retval;
	} "Error \%d while tagging initial pristine configuration (version $version) in $tempdir with command: \%s";

}


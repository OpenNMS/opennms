#!/usr/bin/perl -w

use strict;
use warnings;

use Cwd;
use Data::Dumper;
use File::Basename;
use File::Spec;
use Getopt::Long qw(:config gnu_getopt pass_through);
use Git;

our $CONFIG_BRANCH = 'opennms-git-config-local';
our $OPENNMS_HOME;
our $TEMPDIR;

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

### Create a temporary working directory
#$TEMPDIR = File::Temp::tempdir( CLEANUP => 1 );
$TEMPDIR = '/tmp/git-config';

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

info "finished.";






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
  * -v, --version The version of the pristine configuration that is being initialized.

* store           Store current configuration changes in $OPENNMS_HOME/etc.
  * -v, --version The version of the user configuration that is being stored.

* upgrade         Upgrade the user's configuration using the latest pristine configs.
  * -f, --from    The version of the OpenNMS configuration that is being upgraded from.
  * -t, --to      The version of the OpenNMS configuration that is being upgraded to.

END
}

package Commands;

use Carp;
use Cwd;
use File::Basename;
use File::Copy;
use File::Path qw(make_path remove_tree);
use File::Spec;
use File::Temp;
use Getopt::Long qw(:config gnu_getopt pass_through);
use Git;
use IO::Handle;

sub _assert_branch($$);
sub _delete_tempdir();
sub _git_rm_files_in_tempdir();
sub _is_branch_clean($);
sub _recursive_copy($$$);
sub _update_etc_pristine($);
sub init(@);
sub store(@);
sub upgrade(@);

sub _assert_branch($$) {
	main::debug "_assert_branch(@_)";

	my $repository      = shift;
	my $expected_branch = shift;

	my $git = Git->repository(Directory => $TEMPDIR);

	git_cmd_try {
		my $retval = $git->command('status');
		my ($branchname) = $retval =~ /\# On branch\s+(.*?)\s*$/ms;
		if ($branchname ne $expected_branch) {
			main::error "Expected to be on the '$expected_branch' branch, but we're on '$branchname' instead. Please make sure you're on the $expected_branch branch before attempting to store or update configs.";
			exit 1;
		}
	} "Error \%d while getting branch status in $TEMPDIR with command: \%s";
}

sub _delete_tempdir() {
	# we only want to work with .git directories for now
	my $error_list;
	remove_tree($TEMPDIR, { error => \$error_list });

	if (@$error_list) {
		main::error "1 or more errors occurred while cleaning up $TEMPDIR: ", @$error_list;
		exit 1;
	}
}

sub _git_rm_files_in_tempdir() {
	main::debug "_git_rm_files_in_tempdir()";

	my $git = Git->repository(Directory => $TEMPDIR);

	# delete any files the user removed
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
	} "Error \%d while getting status in $TEMPDIR with command: \%s";

	for my $delete (@delete) {
		git_cmd_try {
			my $retval = $git->command('rm', '-f', $delete);
			main::debug $retval;
		} "Error \%d while git rm'ing $delete from $TEMPDIR with command: \%s";
	}

}

sub _is_branch_clean($) {
	main::debug "_is_branch_clean(\$git)";

	my $git = shift;

	my $clean = 0;
	# check if anything has been changed since last commit
	git_cmd_try {
		my $retval = $git->command('status');
		main::debug $retval;
		if ($retval =~ /nothing to commit \(working directory clean\)/ms) {
			$clean = 1;
		}
	} "Error \%d while determining branch status with command: \%s";
	return $clean;
}

sub _recursive_copy($$$) {
	main::debug "_recursive_copy(@_)";
	my $source      = shift;
	my $target      = shift;
	my $exclude_git = shift;

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

	if ($exclude_git) {
		system('rsync', '-ar', '--delete', '--exclude=.git', '--exclude=examples',
			Cwd::abs_path($source) . '/',
			Cwd::abs_path($target) . '/'
		) == 0 or croak "rsync failed: $!";
	} else {
		system('rsync', '-ar', '--delete', '--exclude=examples',
			Cwd::abs_path($source) . '/',
			Cwd::abs_path($target) . '/'
		) == 0 or croak "rsync failed: $!";
	}
}

sub _update_etc_pristine($) {
	main::debug "_update_etc_pristine(@_)";

	my $version = shift;

	my $etcdir      = File::Spec->catdir($OPENNMS_HOME, 'etc');
	my $gitdir      = File::Spec->catdir($etcdir, '.git');
	my $pristinedir = File::Spec->catdir($OPENNMS_HOME, 'share', 'etc-pristine');
	my $tempgitdir  = File::Spec->catdir($TEMPDIR, '.git');

	if (! -d $gitdir) {
		main::error "Attempting to update master with the latest etc-pristine, but $gitdir does not exist!";
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

	# we only want to work with .git directories for now
	_delete_tempdir();
	_recursive_copy($gitdir, $tempgitdir, 0);

	my $git = Git->repository(Directory => $TEMPDIR);

	_assert_branch($TEMPDIR, $CONFIG_BRANCH);

	main::debug "git checkout master";
	git_cmd_try {
		my $retval = $git->command('checkout', 'master');
		main::debug $retval;
	} "Error \%d while checking out the master branch in $TEMPDIR with command: \%s";

	main::debug "git clean -fdx";
	git_cmd_try {
		my $retval = $git->command('clean', '-fdx');
		main::debug $retval;
	} "Error \%d while cleaning the master branch in $TEMPDIR with command: \%s";

	main::debug "git reset --hard master";
	git_cmd_try {
		my $retval = $git->command('reset', '--hard', 'master');
		main::debug $retval;
	} "Error \%d while resetting the master branch in $TEMPDIR with command: \%s";

	# copy etc-pristine, excluding .git
	_recursive_copy($pristinedir, $TEMPDIR, 1);

	# add and commit initial content
	main::debug "git add .";
	git_cmd_try {
		my $retval = $git->command('add', '.');
		main::debug $retval;
	} "Error \%d while adding files to git in $TEMPDIR with command: \%s";

	_git_rm_files_in_tempdir();

	my $clean = _is_branch_clean($git);
	if ($clean) {
		main::debug "skipping commit, $version pristine files have not changed since last commit";
	} else {
		my $commit_message = 'initial commit, version: ' . $version;
		main::debug "git commit -m '$commit_message'";
		git_cmd_try {
			my $retval = $git->command('commit', '-m', $commit_message);
			main::debug $retval;
		} "Error \%d while committing pristine configuration in $TEMPDIR with command: \%s";
	}

	# create a tag with the initial etc pristine
	main::debug "git tag opennms-git-config-pristine-$version";
	git_cmd_try {
		my $retval = $git->command('tag', 'opennms-git-config-pristine-' . $version);
		main::debug $retval;
	} "Error \%d while tagging initial pristine configuration (version $version) in $TEMPDIR with command: \%s";

	# switch back to the user config branch
	main::debug "git checkout $CONFIG_BRANCH";
	git_cmd_try {
		my $retval = $git->command('checkout', $CONFIG_BRANCH);
		main::debug $retval;
	} "Error \%d while checking out $CONFIG_BRANCH in $TEMPDIR with command: \%s";

	# copy the updated .git back to the live tree
	_recursive_copy($tempgitdir, $gitdir, 0);
}

sub _store_user_configs($) {
	main::debug "_store_user_configs(@_)";

	my $version = shift;

	### make sure expected paths exist

	my $etcdir      = File::Spec->catdir($OPENNMS_HOME, 'etc');
	my $gitdir      = File::Spec->catdir($etcdir, '.git');
	my $tempgitdir  = File::Spec->catdir($TEMPDIR, '.git');

	if (! -d $gitdir) {
		main::error "'store' called, but $gitdir does not exist!";
		exit 1;
	}

	main::info "Storing config changes to Git in $etcdir.";

	# make sure the temp directory is clean
	_delete_tempdir();
	make_path($TEMPDIR);

	# set up a pristine copy of the git dir
	_recursive_copy($gitdir, $tempgitdir, 0);

	my $git = Git->repository(Directory => $TEMPDIR);

	git_cmd_try {
		my $retval = $git->command('reset', '--hard', $CONFIG_BRANCH);
		main::debug $retval;
	} "Error \%d while resetting $TEMPDIR to $CONFIG_BRANCH with command: \%s";

	# copy the existing configuration files from etc into the working tree, skipping the .git dir
	_recursive_copy($etcdir, $TEMPDIR, 1);

	# add all new and modified files
	git_cmd_try {
		my $retval = $git->command('add', '.');
		main::debug $retval;
	} "Error \%d while adding files to git in $TEMPDIR with command: \%s";

	# remove any user-deleted files
	_git_rm_files_in_tempdir();

	my $clean = _is_branch_clean($git);

	if ($clean) {
		main::info "_store_user_configs: nothing to do, no changes detected";
		return;
	}

	# commit user configs
	git_cmd_try {
		my $retval = $git->command('commit', '-m', 'user-modified configuration files for OpenNMS, version ' . $version);
		main::debug $retval;
	} "Error \%d while committing user changes to $TEMPDIR with command: \%s";

	# create a tag for this version
	git_cmd_try {
		my $retval = $git->command('tag', 'opennms-git-config-user-' . $version);
		main::debug $retval;
	} "Error \%d while tagging initial pristine configuration (version $version) in $TEMPDIR with command: \%s";

	# copy the updated .git tree back
	_recursive_copy($tempgitdir, $gitdir, 0);
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

	_delete_tempdir();

	### make sure expected paths exist

	my $etcdir      = File::Spec->catdir($OPENNMS_HOME, 'etc');
	my $gitdir      = File::Spec->catdir($etcdir, '.git');
	my $pristinedir = File::Spec->catdir($OPENNMS_HOME, 'share', 'etc-pristine');
	my $tempgitdir  = File::Spec->catdir($TEMPDIR, '.git');

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

	### initialize the (temporary) base git directory

	# initialize .git
	git_cmd_try {
		my $retval = Git::command_oneline('init', $TEMPDIR);
		main::debug $retval;
	} "Error \%d while initializing git directory in $TEMPDIR with command: \%s";

	my $git = Git->repository(Directory => $TEMPDIR);

	# create empty master branch
	git_cmd_try {
		my $retval = $git->command('checkout', '--orphan', 'master');
		main::debug $retval;
	} "Error \%d while checking out empty master branch in $TEMPDIR with command: \%s";

	git_cmd_try {
		my $retval = $git->command('commit', '--allow-empty', '-m', 'initial master branch');
		main::debug $retval;
	} "Error \%d while committing empty master branch in $TEMPDIR with command: \%s";

	# create a branch for the user's configuration
	git_cmd_try {
		my $retval = $git->command('checkout', '-b', $CONFIG_BRANCH);
		main::debug $retval;
	} "Error \%d while creating $CONFIG_BRANCH branch in $TEMPDIR with command: \%s";

	# create .gitignore and commit to the user branch
	my $gitignore = IO::Handle->new();
	open ($gitignore, '>' . File::Spec->catfile($TEMPDIR, '.gitignore')) or croak "unable to write to .gitignore in $TEMPDIR: $!";
	print $gitignore "examples\n";
	print $gitignore "configured\n";
	print $gitignore "libraries.properties\n";
	close($gitignore) or croak "unable to close filehandle for .gitignore: $!";

	git_cmd_try {
		my $retval = $git->command('add', '.gitignore');
		main::debug $retval;
	} "Error \%d while adding files to git in $TEMPDIR with command: \%s";

	git_cmd_try {
		my $retval = $git->command('commit', '-m', ".gitignore for $CONFIG_BRANCH branch");
		main::debug $retval;
	} "Error \%d while committing .gitignore to $TEMPDIR with command: \%s";

	# copy the .git repository back to the live etc directory
	_recursive_copy($tempgitdir, $gitdir, 0);

	# and the .gitignore as well
	copy(File::Spec->catfile($TEMPDIR, '.gitignore'), File::Spec->catfile($etcdir, '.gitignore'));

	### put an initial copy of etc-pristine in the master branch
	_update_etc_pristine($version);

	# make sure the temp directory is clean
	_delete_tempdir();

	make_path($TEMPDIR);

	# make a fresh copy of the git files
	_recursive_copy($gitdir, $tempgitdir, 0);

	_assert_branch($TEMPDIR, $CONFIG_BRANCH);

	# make sure we have all the files
	git_cmd_try {
		my $retval = $git->command('reset', '--hard', $CONFIG_BRANCH);
		main::debug $retval;
	} "Error \%d while resetting $TEMPDIR to $CONFIG_BRANCH with command: \%s";

	# now update the user branch to contain the pristine master configs
	git_cmd_try {
		my $retval = $git->command('merge', 'master');
		main::debug $retval;
	} "Error \%d while merging pristine changes to $CONFIG_BRANCH with command: \%s";

	# now copy this back to the real tree
	_recursive_copy($tempgitdir, $gitdir, 0);

	# now commit the user's changes
	_store_user_configs($version);
}

sub store(@) {
	my $version = undef;

	my $ret = Getopt::Long::GetOptionsFromArray(\@_,
		'v|version=s' => \$version
	);
	if (not defined $version) {
		main::error '-v version is a required argument when storing an etc git repository!';
		exit 1;
	}

	_delete_tempdir();

	# commit the user's changes
	_store_user_configs($version);
}

sub upgrade(@) {
	my $from = undef;
	my $to   = undef;

	my $ret = Getopt::Long::GetOptionsFromArray(\@_,
		'f|from=s' => \$from,
		't|to=s'   => \$to,
	);
	if (not defined $from) {
		main::error '-f is a required argument when upgrading an etc git repository!';
		exit 1;
	}
	if (not defined $to) {
		main::error '-t is a required argument when upgrading an etc git repository!';
		exit 1;
	}

	### make sure expected paths exist

	my $etcdir      = File::Spec->catdir($OPENNMS_HOME, 'etc');
	my $gitdir      = File::Spec->catdir($etcdir, '.git');

	if (! -d $gitdir) {
		main::error "'upgrade' called, but $gitdir does not exist!";
		exit 1;
	}

	main::info "Merging latest configuration changes to Git in $etcdir.";

	_delete_tempdir();

	my $realgit = Git->repository(Directory => $etcdir);
	my $clean = _is_branch_clean($realgit);

	if (not $clean) {
		main::error "You have uncommitted changes in $etcdir, please run $0 store first!";
		exit 1;
	}

	git_cmd_try {
		my $retval = $realgit->command('tag', '-l');
		main::debug $retval;
		if ($retval !~ /^opennms-git-config-pristine-${from}$/m) {
			main::error "Attempting to upgrade from $from to $to, but no opennms-git-config-pristine-$from tag was found.";
			main::error "Make sure you have a pristine copy of the OpenNMS etc directory stored in git before performing an upgrade.";
			exit 1;
		}
	} "Error \%d while resetting $TEMPDIR to $CONFIG_BRANCH with command: \%s";

	### we're clean, update the master branch with etc-pristine, assumed to be version $to
	main::debug "updating master branch to include etc-pristine, assumed to be version $to";
	_update_etc_pristine($to);

	my $git = Git->repository(Directory => $TEMPDIR);

	# make sure we're reset with all user files
	git_cmd_try {
		my $retval = $git->command('reset', '--hard', $CONFIG_BRANCH);
		main::debug $retval;
	} "Error \%d while resetting files to $CONFIG_BRANCH with command: \%s";

	# now update the user branch to contain the pristine master configs
	git_cmd_try {
		my $retval = $git->command('merge', 'master');
		main::debug $retval;
	} "Error \%d while merging pristine changes to $CONFIG_BRANCH with command: \%s";

	# create a tag for this version
	git_cmd_try {
		my $retval = $git->command('tag', 'opennms-git-config-user-' . $to);
		main::debug $retval;
	} "Error \%d while tagging updated user configuration (version $to) in $TEMPDIR with command: \%s";

	# now copy this back to the real tree
	_recursive_copy($TEMPDIR, $etcdir, 0);
}


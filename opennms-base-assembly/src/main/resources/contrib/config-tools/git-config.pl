#!/usr/bin/perl -w

use strict;
use warnings;

use Cwd;
use Data::Dumper;
use File::Basename;
use File::Spec;
use Getopt::Long qw(:config gnu_getopt pass_through);
use Git;

our $PRISTINE_BRANCH = 'opennms-git-config-pristine';
our $CONFIG_BRANCH   = 'opennms-git-config-local';
our $OPENNMS_HOME;
our $TEMPDIR;
our $TOOLDIR;

sub find_opennms_home($);
sub debug(@);
sub info(@);
sub warning(@);
sub error(@);
sub usage();

my $me = Cwd::abs_path($0);
$TOOLDIR = dirname($me);

### Create a temporary working directory
$TEMPDIR = File::Temp::tempdir( CLEANUP => 1 );
#$TEMPDIR = '/tmp/git-config';

### Process arguments

our $OPT_HELP         = 0;
our $OPT_OPENNMS_HOME = undef;
our $OPT_VERSION      = undef;
our $OPT_FROM         = undef;
our $OPT_TO           = undef;

my $results = GetOptions(
	'h|help'           => \$OPT_HELP,
	'o|opennms-home=s' => \$OPT_OPENNMS_HOME,
	'v|version=s'      => \$OPT_VERSION,
	'f|from=s'         => \$OPT_FROM,
	't|to=s'           => \$OPT_TO,
);

if ($OPT_HELP) {
	usage();
	exit 1;
}

if ($OPT_OPENNMS_HOME) {
	$OPENNMS_HOME = $OPT_OPENNMS_HOME;
	mkpath(File::Spec->catdir($OPENNMS_HOME, 'etc'));
}

### Find $OPENNMS_HOME

if (not defined $OPENNMS_HOME and exists $ENV{'OPENNMS_HOME'}) {
	$OPENNMS_HOME = find_opennms_home($ENV{'OPENNMS_HOME'});
}

if (not defined $OPENNMS_HOME) {
	$OPENNMS_HOME = find_opennms_home(Cwd::abs_path($0));
}

if (not defined $OPENNMS_HOME) {
	error 'Unable to locate $OPENNMS_HOME or $OPENNMS_HOME not set.';
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

sub debug(@)   { _output("[DEBUG]", @_); }
sub info(@)    { _output("[INFO] ", @_); }
sub warning(@) { _output("[WARN] ", @_); }
sub error(@)   { _output("[ERROR]", @_); }

sub usage() {
	print <<END;
usage: $0 [-h] <command>

  -h, --help          This help.
  -o, --opennms-home  The OpenNMS root directory.

Valid commands:
* init                Initialize \$OPENNMS_HOME/etc as a Git repository.
  * -v, --version     The version of the pristine configuration that is being initialized.

* storepristine       Update the pristine branch with the changes in \$OPENNMS_HOME/bin/config-tools.
  * -v, --version     The version of the pristine configuration that is being stored.

* storecurrent        Store current configuration changes in \$OPENNMS_HOME/etc.
  * -v, --version     The version of the user configuration that is being stored.

* upgrade             Upgrade the user's configuration using the latest pristine configs.
  * -f, --from        The version of the OpenNMS configuration that is being upgraded from.
  * -t, --to          The version of the OpenNMS configuration that is being upgraded to.

END
}

package Commands;

use strict;
use warnings;

use Carp;
use Cwd;
use File::Basename;
use File::Copy;
use File::Path;
use File::Spec;
use File::Temp;
use Git;
use IO::Handle;

sub _assert_branch($$);
sub _delete_tempdir();
sub _git(@);
sub _git_rm_files_in_tempdir();
sub _get_current_opennms_rpms();
sub _is_branch_clean($);
sub _recursive_copy($$$);
sub _store_user_configs($);
sub _unpack_pristine_tarballs();
sub _update_etc_pristine($$);
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
			main::error '_assert_branch:', "Expected to be on the '$expected_branch' branch, but we're on '$branchname' instead. Please make sure you're on the $expected_branch branch before attempting to store or update configs.";
			exit 1;
		}
	} "Error \%d while getting branch status in $TEMPDIR with command: \%s";
}

sub _delete_tempdir() {
	# we only want to work with .git directories for now
	if (-d $TEMPDIR) {
		my $deleted = rmtree($TEMPDIR);
		if ($deleted == 0) {
			main::error '_delete_tempdir:', "an error occurred while cleaning up $TEMPDIR: ", $!;
		}
	}
}

sub _get_current_opennms_rpms() {
	my $rpms = { 'opennms-core' => 1 };

	my $rpmhandle = IO::Handle->new();
	open($rpmhandle, 'rpm -qa --queryformat=\'%{name}\\n\' | grep -E \'^opennms-\' |') or croak "unable to run: rpm -qa | grep -E '^opennms': $!";
	while (my $rpmname = <$rpmhandle>) {
		chomp($rpmname);
		next if ($rpmname =~ /^opennms-(config-data|repo)/);
		$rpms->{$rpmname}++;
	}
	close($rpmhandle);

	my @rpms = keys %$rpms;
	return \@rpms;
}

sub _git(@) {
	my $source = shift;
	my $git    = shift;
	my @args   = @_;

	my $command_text = "git '" . join("' '", @args) . "'";
	main::debug $source . ':', "running: $command_text";
	my $retval = undef;
	git_cmd_try {
		$retval = $git->command(@args);
		main::debug $retval;
	} "Error \%d while running command: \%s";

	return $retval;
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
				main::info '_git_rm_files_in_tempdir:', "deleted $1";
				push(@delete, $1);
			}
		}
		$git->command_close_pipe($fh, $ctx);
	} "Error \%d while getting status in $TEMPDIR with command: \%s";

	for my $delete (@delete) {
		_git('_git_rm_files_in_tempdir', $git, 'rm', '-f', $delete);
	}

}

sub _is_branch_clean($) {
	main::debug "_is_branch_clean(\$git)";

	my $git = shift;

	# check if anything has been changed since last commit
	my $retval = _git('_is_branch_clean', $git, 'status');
	if ($retval =~ /nothing to commit \(working directory clean\)/ms) {
		return 1;
	}
	return 0;
}

sub _recursive_copy($$$) {
	main::debug "_recursive_copy(@_)";
	my $source      = shift;
	my $target      = shift;
	my $exclude_git = shift;

	if (not -d $source) {
		main::error '_recursive_copy:', $source, 'is not a directory!';
		exit 1;
	}
	if (-e $target and not -d $target) {
		main::error '_recursive_copy:', $target . ' is not a directory!';
		exit 1;
	}

	eval { mkpath($target) };
	if ($@) {
		main::error '_recursive_copy:', "Could not create $target: $@";
		exit 1;
	}

	my $rsync = `which rsync 2>/dev/null`;
	if ($? != 0) {
		main::debug '_recursive_copy', 'Unable to locate `rsync` command.';
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

sub _unpack_pristine_tarballs() {
	main::info '_unpack_pristine_tarballs()';

	# unpack etc-pristine
	my $tar = `which tar 2>/dev/null`;
	if ($? != 0) {
		main::debug '_unpack_pristine_tarballs:', 'Unable to locate the `tar` executable: ' . $!;
		exit 1;
	}
	chomp($tar);
	my $pristinedir = File::Temp::tempdir(CLEANUP => 1);

	my $rpmnames = _get_current_opennms_rpms();

	for my $rpmname (@$rpmnames) {
		my $pristinefile = File::Spec->catfile($TOOLDIR, 'etc-pristine-' . $rpmname . '.tar.gz');
		if (-f $pristinefile) {
			main::debug '_unpack_pristine_tarballs:', 'unpacking pristine file', $pristinefile;
			system($tar, '-C', $pristinedir, '-xzf', $pristinefile) == 0
				or croak "Unable to unpack $pristinefile into $pristinedir: $!";
		} else {
			main::warning '_unpack_pristine_tarballs:', 'no etc-pristine file found for', $pristinefile;
		}
	}

	my $etcdir            = File::Spec->catdir($OPENNMS_HOME, 'etc');
	my $pristinegitignore = File::Spec->catfile($pristinedir, '.gitignore');
	my $realgitignore     = File::Spec->catfile($etcdir, '.gitignore');

	if (-f $realgitignore and not -f $pristinegitignore) {
		copy($realgitignore, $pristinegitignore);
	}

	return $pristinedir;
}

sub _update_etc_pristine($$) {
	main::debug "_update_etc_pristine(@_)";

	my $version     = shift;
	my $pristinedir = shift;
	my $tagname     = 'opennms-git-config-pristine-' . $version;

	my $etcdir      = File::Spec->catdir($OPENNMS_HOME, 'etc');
	my $gitdir      = File::Spec->catdir($etcdir, '.git');
	my $tempgitdir  = File::Spec->catdir($TEMPDIR, '.git');

	if (! -d $gitdir) {
		main::error '_update_etc_pristine:', "Attempting to update $PRISTINE_BRANCH with the latest etc-pristine, but $gitdir does not exist!";
		exit 1;
	}
	if (! -d $etcdir) {
		main::error '_update_etc_pristine:', "\$OPENNMS_HOME is $OPENNMS_HOME, but $etcdir does not exist!";
		exit 1;
	}
	if (! -d $pristinedir) {
		main::error '_update_etc_pristine:', "Attempting to update configs based on etc-pristine from $pristinedir, but it does not exist!";
		exit 1;
	}

	# we only want to work with .git directories for now
	_delete_tempdir();
	_recursive_copy($gitdir, $tempgitdir, 0);

	my $git = Git->repository(Directory => $TEMPDIR);

	my $tag = _git('_update_etc_pristine', $git, 'tag', '-l', $tagname);
	if ($tag =~ /opennms-git-config-pristine/) {
		main::warning "_update_etc_pristine: tag $tagname already exists, skipping etc_pristine update.";
		return;
	}

	_assert_branch($TEMPDIR, $CONFIG_BRANCH);

	_git('_update_etc_pristine', $git, 'checkout', $PRISTINE_BRANCH);
	_git('_update_etc_pristine', $git, 'clean', '-fdx');
	_git('_update_etc_pristine', $git, 'reset', '--hard', $PRISTINE_BRANCH);

	_recursive_copy($pristinedir, $TEMPDIR, 1);

	my $etcgitignore = File::Spec->catfile($etcdir, '.gitignore');
	my $tempgitignore = File::Spec->catfile($TEMPDIR, '.gitignore');

	if (! -f $tempgitignore and -f $etcgitignore) {
		copy($etcgitignore, $tempgitignore);
	}

	# add and commit initial content
	_git('_update_etc_pristine', $git, 'add', '.');

	_git_rm_files_in_tempdir();

	my $clean = _is_branch_clean($git);
	if ($clean) {
		main::debug '_update_etc_pristine:', "skipping commit, $version pristine files have not changed since last commit";
	} else {
		my $commit_message = 'initial commit, version: ' . $version;
		_git('_update_etc_pristine', $git, 'commit', '-m', $commit_message);
	}

	# create a tag with the initial etc pristine
	_git('_update_etc_pristine', $git, 'tag', 'opennms-git-config-pristine-' . $version);

	# switch back to the user config branch
	_git('_update_etc_pristine', $git, 'checkout', $CONFIG_BRANCH);

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
		main::error '_store_user_configs:', "$gitdir does not exist!";
		exit 1;
	}

	main::info "Storing config changes to Git in $etcdir.";

	# make sure the temp directory is clean
	_delete_tempdir();
	eval { mkpath($TEMPDIR) };
	if ($@) {
		main::error '_store_user_configs:', "Could not create $TEMPDIR: $@";
		exit 1;
	}

	# set up a pristine copy of the git dir
	_recursive_copy($gitdir, $tempgitdir, 0);

	my $git = Git->repository(Directory => $TEMPDIR);

	_git('_store_user_configs', $git, 'reset', '--hard', $CONFIG_BRANCH);

	# copy the existing configuration files from etc into the working tree, skipping the .git dir
	_recursive_copy($etcdir, $TEMPDIR, 1);

	# add all new and modified files
	_git('_store_user_configs', $git, 'add', '.');

	# remove any user-deleted files
	_git_rm_files_in_tempdir();

	my $clean = _is_branch_clean($git);

	if ($clean) {
		main::info "_store_user_configs: nothing to do, no changes detected";
		return;
	}

	# commit user configs
	_git('_store_user_configs', $git, 'commit', '-m', 'user-modified configuration files for OpenNMS, version ' . $version);

	# create a tag for this version
	_git('_store_user_configs', $git, 'tag', 'opennms-git-config-user-' . $version);

	# copy the updated .git tree back
	_recursive_copy($tempgitdir, $gitdir, 0);
}

sub init(@) {
	main::debug "init($OPT_VERSION)";

	my $version = $OPT_VERSION;
	if (not defined $version) {
		main::error '-v version is a required argument when initializing an etc git repository!';
		exit 1;
	}

	_delete_tempdir();

	### make sure expected paths exist

	my $etcdir      = File::Spec->catdir($OPENNMS_HOME, 'etc');
	my $gitdir      = File::Spec->catdir($etcdir, '.git');
	my $tempgitdir  = File::Spec->catdir($TEMPDIR, '.git');
	my $pristinedir = File::Spec->catdir($OPENNMS_HOME, 'share', 'etc-pristine');

	if (-d $gitdir) {
		main::error "'init' called, but $gitdir already exists!";
		exit 1;
	}
	if (! -d $etcdir) {
		main::error 'init:', "\$OPENNMS_HOME is $OPENNMS_HOME, but $etcdir does not exist!";
		exit 1;
	}

	main::info "Initializing Git in $etcdir.";

	### initialize the (temporary) base git directory

	# initialize .git
	main::debug 'init:', 'running:', 'git', 'init', $TEMPDIR;
	git_cmd_try {
		my $retval = Git::command_oneline('init', $TEMPDIR);
		main::debug $retval;
	} "Error \%d while initializing git directory in $TEMPDIR with command: \%s";

	my $git = Git->repository(Directory => $TEMPDIR);

	# set up config
	_git('init', $git, 'config', 'user.name', 'OpenNMS Git Config');
	_git('init', $git, 'config', 'user.email', $0);

	# create empty pristine branch
	_git('init', $git, 'symbolic-ref', 'HEAD', 'refs/heads/' . $PRISTINE_BRANCH);

	# create .gitignore and commit to the user branch
	my $gitignore = IO::Handle->new();
	open ($gitignore, '>' . File::Spec->catfile($TEMPDIR, '.gitignore')) or croak "unable to write to .gitignore in $TEMPDIR: $!";
	print $gitignore "*.jasper\n";
	print $gitignore "*.rpmnew\n";
	print $gitignore "*.rpmorig\n";
	print $gitignore "examples\n";
	print $gitignore "configured\n";
	print $gitignore "libraries.properties\n";
	close($gitignore) or croak "unable to close filehandle for .gitignore: $!";

	_git('init', $git, 'add', '.gitignore');
	_git('init', $git, 'commit', '-m', ".gitignore for $PRISTINE_BRANCH branch");

	# create a branch for the user's configuration
	_git('init', $git, 'checkout', '-b', $CONFIG_BRANCH);

	# copy the .git repository back to the live etc directory
	_recursive_copy($tempgitdir, $gitdir, 0);

	# and the .gitignore as well
	copy(File::Spec->catfile($TEMPDIR, '.gitignore'), File::Spec->catfile($etcdir, '.gitignore'));

	### put an initial copy of etc-pristine in the pristine branch
	if (-d $pristinedir) {
		main::info $pristinedir, 'exists, figuring out what to put into the pristine directory';
		$pristinedir = File::Temp::tempdir(CLEANUP => 1);
		my $rpmhandle = IO::Handle->new();
		my $rpmnames = _get_current_opennms_rpms();
		main::debug 'init:', 'found RPMs:', @$rpmnames;
		for my $rpmname (@$rpmnames) {
			my $rpmlist = IO::Handle->new();
			open($rpmlist, 'rpm -ql ' . $rpmname . ' |') or croak "unable to run: rpm -ql $rpmname: $!";
			while (my $file = <$rpmlist>) {
				chomp($file);
				next unless ($file =~ /\/etc-pristine/);
				next unless (-f $file);
				my ($relativefile) = $file =~ /^.*?\/etc-pristine\/(.*)$/;
				my $newfile = File::Spec->catfile($pristinedir, $relativefile);
				main::debug "init: $rpmname: $file -> $newfile";
				mkpath(dirname($newfile));
				copy($file, $newfile) or croak "unable to copy $file to $newfile: $!";
			}
			close($rpmlist);
		}
	} else {
		main::warning $pristinedir, 'does not exist, assuming this is a new install';
		$pristinedir = _unpack_pristine_tarballs();
	}

	# copy .gitignore for this initial sync, so that it looks like it comes with a pristine etc
	copy(File::Spec->catfile($etcdir, '.gitignore'), File::Spec->catfile($pristinedir, '.gitignore'));

	# now put the pristine files we just unpacked into the pristine branch
	_update_etc_pristine($version, $pristinedir);

	# make sure the temp directory is clean
	_delete_tempdir();

	eval { mkpath($TEMPDIR) };
	if ($@) {
		main::error "Could not create $TEMPDIR: $@";
		exit 1;
	}

	# make a fresh copy of the git files
	_recursive_copy($gitdir, $tempgitdir, 0);

	_assert_branch($TEMPDIR, $CONFIG_BRANCH);

	# make sure we have all the files
	_git('init', $git, 'reset', '--hard', $CONFIG_BRANCH);

	# now update the user branch to contain the pristine branch configs
	_git('init', $git, 'merge', $PRISTINE_BRANCH);

	# now copy this back to the real tree
	_recursive_copy($tempgitdir, $gitdir, 0);

	# now commit the user's changes
	_store_user_configs($version);
}

sub storepristine(@) {
	my $version = $OPT_VERSION;
	if (not defined $version) {
		main::error '-v version is a required argument when storing an etc git repository!';
		exit 1;
	}

	_delete_tempdir();

	my $pristinedir = _unpack_pristine_tarballs();

	_update_etc_pristine($version, $pristinedir);
}

sub storecurrent(@) {
	my $version = $OPT_VERSION;
	if (not defined $version) {
		main::error '-v version is a required argument when storing an etc git repository!';
		exit 1;
	}

	_delete_tempdir();

	# commit the user's changes
	_store_user_configs($version);
}

sub upgrade(@) {
	my $from = $OPT_FROM;
	my $to   = $OPT_TO;

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

	my $retval = _git('upgrade', $realgit, 'tag', '-l');
	if ($retval !~ /^opennms-git-config-pristine-${from}$/m) {
		main::error "Attempting to upgrade from $from to $to, but no opennms-git-config-pristine-$from tag was found.";
		main::error "Make sure you have a pristine copy of the OpenNMS etc directory stored in git before performing an upgrade.";
		exit 1;
	}

	my $pristinedir = _unpack_pristine_tarballs();

	### we're clean, update the pristine branch with etc-pristine, assumed to be version $to
	main::debug "updating $PRISTINE_BRANCH branch to include etc-pristine, assumed to be version $to";
	_update_etc_pristine($to, $pristinedir);

	my $git = Git->repository(Directory => $TEMPDIR);

	# make sure we're reset with all user files
	_git('upgrade', $git, 'reset', '--hard', $CONFIG_BRANCH);

	# now update the user branch to contain the pristine branch configs
	_git('upgrade', $git, 'merge', $PRISTINE_BRANCH);

	# create a tag for this version
	_git('upgrade', $git, 'tag', 'opennms-git-config-user-' . $to);

	# now copy this back to the real tree
	_recursive_copy($TEMPDIR, $etcdir, 0);
}


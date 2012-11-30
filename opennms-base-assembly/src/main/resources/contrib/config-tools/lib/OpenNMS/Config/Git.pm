package OpenNMS::Config::Git;

use 5.008008;
use strict;
use warnings;

use Carp;
use Digest::MD5;
use Error qw(:try);
use File::Copy;
use File::Path;
use File::Spec;
use Git;
use IO::Handle;

require Exporter;

our @ISA = qw(Exporter);
our @MERGE_OPTIONS = qw(-Xpatience -Xignore-space-change -Xignore-all-space -Xrenormalize);

our $VERSION = '0.1.0';

=head1 NAME

OpenNMS::Config::Git - OpenNMS git manipulation.

=head1 SYNOPSIS

  use OpenNMS::Config::Git;

=head1 DESCRIPTION

This module is for interacting with an OpenNMS git
directory.

=head1 CONSTRUCTOR

OpenNMS::Config::Git->new($gitroot)

Given a directory, create a Git object.

=cut

sub new {
	my $proto = shift;
	my $class = ref($proto) || $proto;

	my $dir = shift;
	if (not defined $dir) {
		croak "You must pass the git directory!";
	}

	my $self = {
		DIR => $dir,
	};

	bless($self, $class);
	return $self;
}

=head1 METHODS

=cut

sub _git {
	my $self = shift;

	if (exists $self->{GIT}) {
		return $self->{GIT};
	}

	if (-d $self->dir() and -d File::Spec->catdir($self->dir(), ".git")) {
		$self->{GIT} = Git->repository(Directory => $self->dir());
		return $self->{GIT};
	}

	return;
}

=head2 * dir

The directory of this git repository.

=cut

sub dir {
	my $self = shift;
	return $self->{DIR};
}

=head2 * init(%options)

Initialize the git repository.

Options:

=over 2

=item * branch_name

The name of the initial branch.  (Default: master)

=item * author_name

The name of the author of commits used in this Git object.

=item * author_email

The email address of the author of commits used in this Git object.

=back

=cut

sub init {
	my $self = shift;
	my %options = @_;

	if (! -d $self->dir()) {
		mkpath($self->dir());
	}

	if (exists $options{'author_name'}) {
		$self->author_name($options{'author_name'});
	}

	if (exists $options{'author_email'}) {
		$self->author_email($options{'author_email'});
	}

	git_cmd_try {
		Git::command_oneline('init', $self->dir());
	} "Error \%d while initializing " . $self->dir() . " as a git repository: \%s";

	if (exists $options{'branch_name'}) {
		git_cmd_try {
			$self->_git()->command_oneline('symbolic-ref', 'HEAD', 'refs/heads/' . $options{'branch_name'});
		} "Error \%d while setting initial branch name to $options{'branch_name'}: \%s";
	}

	if (defined $self->author_name()) {
		git_cmd_try {
			$self->_git()->command_oneline('config', 'user.name', $self->author_name());
		} "Error \%d while setting user.name to " . $self->author_name() . ": \%s";
	}

	git_cmd_try {
		my $email = $0;
		if (defined $self->author_email()) {
			$email = $self->author_email();
		}
		$self->_git()->command_oneline('config', 'user.email', $self->author_email());
	} "Error \%d while setting user.email to " . $self->author_email() . ": \%s";

	return $self;
}

=head2 * author_name([$author_name])

Returns the author name used when committing changes to the git repository.
If an argument is specified, the author name is set.

=cut

sub author_name {
	my $self = shift;
	my $author_name = shift;
	if (defined $author_name) {
		$self->{AUTHOR_NAME} = $author_name;
	}
	if (not exists $self->{AUTHOR_NAME}) {
		$self->{AUTHOR_NAME} = 'Unknown';
	}
	return $self->{AUTHOR_NAME};
}

=head2 * author_email([$author_email])

Returns the author email used when committing changes to the git repository.
If an argument is specified, the author email is set.

=cut

sub author_email {
	my $self = shift;
	my $author_email = shift;
	if (defined $author_email) {
		$self->{AUTHOR_EMAIL} = $author_email;
	}
	if (not exists $self->{AUTHOR_EMAIL}) {
		$self->{AUTHOR_EMAIL} = 'unknown@opennms-config-git.pl';
	}
	return $self->{AUTHOR_EMAIL};
}

=head2 * get_branch_name()

Get the name of the current branch.

=cut

sub get_branch_name {
	my $self = shift;
	
	my $branch = undef;
	git_cmd_try {
		$branch = $self->_git()->command_oneline('symbolic-ref', 'HEAD');
		$branch =~ s,^refs/heads/,,;
	} "Error \%d while running 'git branch': \%s";

	return $branch;
}

=head2 * get_index_status($filename)

Get the status of the given file in the index.

Valid responses are: unchanged, untracked, new, modified, deleted

=cut

our $STATES = {
	' '   => 'unchanged',
	'?'   => 'untracked',
	'A'   => 'new',
	'M'   => 'modified',
	'D'   => 'deleted',
	'R'   => 'renamed',
	'C'   => 'copied',
	'U'   => 'modified', # technically, "updated"
};

sub get_index_status {
	my $self = shift;
	my $file = shift;

	my $status = ' ';
	git_cmd_try {
		my @ret = $self->_git()->command('status', '--porcelain', $file);
		if (@ret == 1) {
			$status = $ret[0];
			$status =~ s/^(.).*$/$1/;
		}
	} "Error \%d while running git status on $file: \%s";

	return $STATES->{$status};
}

=head2 * get_modifications()

Get a list of OpenNMS::Config::Git::Change objects representing all
modified files in the working tree.

=cut

sub get_modifications {
	my $self = shift;
	
	my @entries;
	git_cmd_try {
		@entries = $self->_git()->command('status', '--porcelain');
	} "Error \%d while running git status on the working tree: \%s";

	my @results;
	for my $entry (@entries) {
		if ($entry =~ /^(.)(.) (.*)$/) {
			my ($index, $working, $filename) = ($1, $2, $3);
			if ($working eq 'D') {
				push(@results, OpenNMS::Config::Git::Remove->new($filename, $self));
			} else {
				push(@results, OpenNMS::Config::Git::Add->new($filename, $self));
			}
		} else {
			print STDERR "unable to parse $entry\n";
		}
	}
	return sort { $a->file() cmp $b->file() } @results;
}

=head2 * get_deleted()

Get a list of files which have been deleted, either from the filesystem, or through 'git rm'.

=cut

sub get_deleted {
	my $self = shift;

	my @entries;
	git_cmd_try {
		@entries = $self->_git()->command('status', '--porcelain');
	} "Error \%d while running git status on the working tree: \%s";

	my @results;
	for my $entry (@entries) {
		if ($entry =~ /^(.)(.) (.*)$/) {
			my ($index, $working, $filename) = ($1, $2, $3);
			if ($index eq 'D' or $working eq 'D') {
				push(@results, $filename);
			}
		} else {
			print STDERR "unable to parse $entry\n";
		}
	}
	return @results;
}

=head2 * add(@files_and_directories)

Add one or more files or directories to the git repository.

=cut

sub add {
	my $self = shift;
	
	my @files = @_;
	
	git_cmd_try {
		$self->_git()->command('add', @files);
	} "Error \%d while adding " . scalar(@files) . " files or directories: \%s";

	return $self;
}

=head2 * rm($file)

Given a file, remove it from the git index.

=cut

sub rm {
	my $self = shift;
	my $file = shift;
	
	if (not defined $file) {
		croak "You must specify a file to remove!";
	}
	
	git_cmd_try {
		$self->_git()->command('rm', $file);
	} "Error \%d while removing $file from the git index: \%s";

	return $self;
}

=head2 * commit($commit_message)

Given a commit message, commit the currently staged files to the git repository.

=cut

sub commit {
	my $self = shift;
	my $message = shift;
	
	if (not defined $message) {
		croak "You must specify a commit message!";
	}

	my @extra_args;
	if (defined $self->author_name()) {
		my $name = $self->author_name();
		my $email = $0;
		if (defined $self->author_email()) {
			$email = $self->author_email();
		}
		push(@extra_args, '--author=' . "$name <$email>");
	}

	git_cmd_try {
		$self->_git()->command('commit', '-m', $message, @extra_args);
	} "Error \%d while committing staged files to the git repository: \%s";
	
	return $self;
}

=head2 * commit_modifications($commit_message)

Given a commit message, add any new or modified files, remove any deleted files,
and then commit the changes to the git repository.

=cut

sub commit_modifications {
	my $self = shift;
	my $message = shift;
	if (not defined $message) {
		croak "You must specify a commit message!";
	}	

	my @modifications = $self->get_modifications();
	if (@modifications == 0) { return 0; }

	my $count = 0;
	for my $change (@modifications) {
		$change->exec();
		$count++;
	}
	$self->commit($message);

	return $count;
}

=head2 * create_branch($new_branch_name, $existing_branch)

Given a new branch name, and an existing branch name, create a new branch
based on the existing branch.

=cut

sub create_branch {
	my $self = shift;
	my $to   = shift;
	my $from = shift;
	
	if (not defined $from or not defined $to) {
		croak "You must specify a branch name, and a source branch name!";
	}
	
	git_cmd_try {
		$self->_git()->command('branch', $to, $from);
	} "Error \%d while attempting to create the '$to' branch from the '$from' branch: \%s";

	return $self;
}

=head2 * checkout($branch_name)

Check out the branch with the given name.

=cut

sub checkout {
	my $self = shift;
	my $branch = shift;
	
	if (not defined $branch) {
		croak "You must specify a branch name!";
	}

	git_cmd_try {
		$self->_git()->command('checkout', $branch);
	} "Error \%d while checking out the '$branch' branch: \%s";

	return $self;
}

=head2 * get_conflicts()

Get the list files in the current working tree that are in conflict.

=cut

sub get_conflicts {
	my $self = shift;

	my @list;
	git_cmd_try {
		@list = $self->_git()->command('diff', '--name-only', '--diff-filter=U');
	} "Error \%d while attempting to list conflicted files: \%s";
	
	return @list;
}
=head2 * merge($branch_name)

Merge the given branch into the current branch.

=cut

sub merge {
	my $self = shift;
	my $branch = shift;
	
	if (not defined $branch) {
		croak "You must specify a branch to merge!";
	}

	git_cmd_try {
		$self->_git()->command('merge', @MERGE_OPTIONS, $branch);
	} "Error \%d while merging the '$branch' branch into the current branch: \%s";

	return $self;
}

=head2 * merge_or_fail($branch_name)

Merge the given branch into the current branch.  Croaks on failure.

=cut

sub merge_or_fail {
	my $self = shift;
	my $branch = shift;

	if (not defined $branch) {
		croak "You must specify a branch to merge!";
	}

	try {
		my $result = $self->_git()->command('merge', @MERGE_OPTIONS, $branch);
	} catch Git::Error::Command with {
		my $E = shift;
		croak "Error while running git merge $branch: " . $E->stringify;
	};

	return $self;
}

=head2 * reset(SHA, tag, or branch)

Reset the working tree to the Git SHA, tag, or branch specified.

Croaks on failure.

=cut

sub reset {
	my $self = shift;
	my $sha  = shift;
	
	if (not defined $sha or $sha eq '') {
		croak "You must specify a git SHA, tag, or branch name!";
	}

	try {
		my $result = $self->_git()->command('reset', '--hard', $sha);
	} catch Git::Error::Command with {
		my $E = shift;
		croak "Error while running git reset --hard $sha: " . $E->stringify;
	};
	return $self;
}

=head2 * save_changes_compared_to(SHA, tag, or branch, extension)

For each file that is new or different from the specified SHA, tag, or branch,
create a copy of the file with the specified extension, and replace the file
with the version in the SHA/tag/branch.

Croaks on failure.

=cut

sub save_changes_compared_to {
	my $self       = shift;
	my $unmodified = shift;
	my $ext        = shift;

	return $self->save_changes_between('HEAD', $unmodified, $unmodified, $ext);
}

=head2 * save_changes_between(modified_files, unmodified_files, pristine_files)

For each file that is new or different from the specified SHA, tag, or branch,
create a copy of the file with the specified extension, and replace the file
with the version in the pristine_files SHA.

Croaks on failure.

=cut

sub save_changes_between {
	my $self       = shift;
	my $modified   = shift;
	my $unmodified = shift;
	my $pristine   = shift;
	my $ext        = shift || '.old';

	if (not defined $pristine or $pristine eq '') {
		croak "You must specify a git SHA, tag, or branch name!";
	}

	my @rename_files;
	my @replace_files;
	try {
		@rename_files = $self->_git()->command('diff', '--name-only', $unmodified, $modified);
	} catch Git::Error::Command with {
		my $E = shift;
		croak "Failed to run git diff --name-only $unmodified $modified: " . $E->stringify;
	};
	try {
		@replace_files = $self->_git()->command('diff', '--name-only', $pristine, $modified);
	} catch Git::Error::Command with {
		my $E = shift;
		croak "Failed to run git diff --name-only $pristine $modified: " . $E->stringify;
	};

#	print STDERR <<END;
#
#modified      = $modified
#unmodified    = $unmodified
#pristine      = $pristine
#
#rename_files  = @rename_files
#replace_files = @replace_files
#
#END

	for my $file (@rename_files) {
		my $from = File::Spec->catfile($self->dir, $file);
		my $to   = $from . $ext;

		if ($from !~ /\b(java|opennms)\.conf$/ and -f $from) {
			OpenNMS::Config->log("- copying $from to $to");
			move($from, $to) or croak "Unable to copy $from to $to: $!";
		}
	}

	my $modified_files = {};
	for my $file ($self->get_deleted(), @replace_files) {
		$modified_files->{$file}++;
	}

	for my $file (sort keys %$modified_files) {
		try {
			# first, we check if the file is even in the pristine branch
			my $result = $self->_git()->command('ls-tree', $pristine, $file);
			if ($result !~ /^[\r\n\s]*$/ms) {
				# if so, check out the pristine version
				OpenNMS::Config->log("- checking out $file from $pristine");
				$result = $self->_git()->command('checkout', $pristine, $file);
			} else {
				OpenNMS::Config->log("- checking out $file from $modified");
				$result = $self->_git()->command('checkout', $modified, $file);
			}
		} catch Git::Error::Command with {
			my $E = shift;
			croak "Failed to checkout $file from $pristine: " . $E->stringify;
		};
	}

	for my $file (@rename_files) {
		my $from = File::Spec->catfile($self->dir, $file);
		my $to   = $from . $ext;

		if (-e $from and -e $to) {
			my $fromdigest = _digest_file($from);
			my $todigest   = _digest_file($to);
			if ($fromdigest eq $todigest) {
				OpenNMS::Config->log("- $from matches $to, removing the $ext file");
				unlink($to);
			}
		}
	}
	
	return $self;
}

sub _digest_file {
	my $file = shift;
	my $digest = Digest::MD5->new();
	my $handle = IO::Handle->new();
	open ($handle, $file) or croak "Can't read from $file: $!";
	binmode($handle);
	my $retval = Digest::MD5->new()->addfile(*$handle)->hexdigest();
	close ($handle) or croak "Can't close filehandle for $file: $!";
	return $retval;
}

=head2 * tag($tag_name)

Create a tag with the given name.

=cut

sub tag {
	my $self = shift;
	my $tag  = shift;
	
	if (not defined $tag) {
		croak "You must specify a tag name!";
	}
	
	git_cmd_try {
		$self->_git()->command('tag', $tag);
	} "Error \%d while creating the '$tag' tag: \%s";

	return $self;
}

=head2 * tag_exists($tag_name)

Check if a tag with the given name exists.

=cut

sub tag_exists {
	my $self = shift;
	my $tag  = shift;

	if (not defined $tag) {
		croak "You must specify a tag name!";
	}

	my $result;

	git_cmd_try {
		$result = $self->_git()->command_oneline('tag', '-l', $tag);
	} "Error \%d while attempting to list tags: \%s";


	if (defined $result) {
		chomp($result);
		return ($result eq $tag);
	}

	return;
}

=head2 * get_latest_runtime_pre_tag()

Get the latest pre-upgrade tag for the runtime branch.

=cut

sub get_latest_runtime_pre_tag {
	my $self = shift;

	my @tags;
	git_cmd_try {
		@tags = sort $self->_git()->command('tag', '-l');
	} "Error \%d while attempting to list tags: \%s";

	my $matching = undef;

	for my $tag (@tags) {
		if ($tag =~ /runtime\/pre-/) {
			$matching = $tag;
		}
	}

	return $matching;
}

1;

package OpenNMS::Config::Git::Change;

use strict;
use warnings;
use Carp;

sub new {
	my $proto = shift;
	my $class = ref($proto) || $proto;

	my $self = {
		FILE => shift,
		GIT  => shift,
	};

	if (not defined $self->{GIT}) {
		croak "You must specify a file and Git object when creating a $class object!";
	}

	bless($self, $class);
	return $self;
}

sub _git {
	my $self = shift;
	return $self->{GIT};
}

sub file {
	my $self = shift;
	return $self->{FILE};
}

sub exec {
	croak "You must implement the exec method in your subclass!"
}

1;

package OpenNMS::Config::Git::Add;

use strict;
use warnings;
use base qw(OpenNMS::Config::Git::Change);

sub exec {
	my $self = shift;
	$self->_git()->add($self->file());
}

1;

package OpenNMS::Config::Git::Remove;

use strict;
use warnings;
use base qw(OpenNMS::Config::Git::Change);

sub exec {
	my $self = shift;
	$self->_git()->rm($self->file());
}

1;

__END__

=head1 AUTHOR

Benjamin Reed E<lt>ranger@opennms.orgE<gt>

=head1 COPYRIGHT AND LICENSE

Copyright (C) 2012 by The OpenNMS Group, Inc.

This library is free software; you can redistribute it and/or modify
it under the same terms as Perl itself, either Perl version 5.8.8 or,
at your option, any later version of Perl 5 you may have available.

=cut


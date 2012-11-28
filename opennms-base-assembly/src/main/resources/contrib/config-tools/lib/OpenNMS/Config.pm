package OpenNMS::Config;

use 5.008008;
use strict;
use warnings;

use Carp;
use File::Basename;
use File::Slurp;
use File::Spec;

our $VERSION = '0.1.0';
our $_OPENNMS_HOME = undef;

=head1 NAME

OpenNMS::Config::Git - OpenNMS git manipulation.

=head1 SYNOPSIS

  use OpenNMS::Config;

=head1 DESCRIPTION

This module defines a set of constants relating to the location of the OpenNMS
installation, branch names, and utility methods useful for the git upgrade scripts.

=head1 CONSTRUCTOR

OpenNMS::Config->new($OPENNMS_HOME)

Given a directory, create a Config object.

=cut

sub new {
	my $proto = shift;
	my $class = ref($proto) || $proto;

	my $dir = shift;
	if (not defined $dir) {
		carp "You must pass the OpenNMS home directory!";
	}

	my $self = {
		DIR => $dir,
	};

	# used when exiting to write a file upon failure, if possible
	$_OPENNMS_HOME = $dir;

	bless($self, $class);
	return $self;
}

sub home {
	my $self = shift;
	return $self->{DIR};
}

sub get_branch_name {
	my $self = shift;
	my $suffix = shift;
	if (not defined $suffix) {
		croak "You must provide a suffix when getting git branch names!";
	}
	return 'opennms-auto-upgrade/' . $suffix;
}

sub get_tag_name {
	my $self = shift;
	my $suffix = shift;
	if (not defined $suffix) {
		croak "You must provide a suffix when getting git tag names!";
	}
	return 'opennms-auto-upgrade/tags/' . $suffix;
}

sub pristine_branch {
	my $self = shift;
	return $self->get_branch_name('pristine');
}

sub runtime_branch {
	my $self = shift;
	return $self->get_branch_name('runtime');
}

sub pristine_revert_tag {
	my $self    = shift;
	my $version = shift;
	if (not defined $version) {
		croak "You must provide a version when getting revert tag names!";
	}
	return $self->get_tag_name('pristine/pre-' . $version);
}

sub runtime_revert_tag {
	my $self    = shift;
	my $version = shift;
	if (not defined $version) {
		croak "You must provide a version when getting revert tag names!";
	}
	return $self->get_tag_name('runtime/pre-' . $version);
}

sub existing_version {
	my $self = shift;
	my $package = shift;
	my $version_string = shift;
	
	if (not defined $package) {
		croak "You must specify a package name to query!\n";
	}

	if (defined $version_string) {
		chomp($version_string);
	}

	if (not defined $version_string or $version_string !~ /^[\d\.]+\-[[:alnum:]\.]+$/) {
		$version_string = `rpm -q --queryformat='\%{version}-\%{release}' $package 2>/dev/null | sort -u | head -n 1`;
		chomp($version_string);
	}
	$version_string = "0.0-0" unless ($version_string =~ /^[\d\.]+\-[[:alnum:]\.]+$/);
	return $version_string;
}

sub pristine_dir {
	my $self = shift;
	return File::Spec->catdir($self->home(), 'share', 'etc-pristine');
}

sub etc_dir {
	my $self = shift;
	return File::Spec->catdir($self->home(), 'etc');
}

sub log {
	my $self = shift;
	my @args = @_;
	my ($file, $line) = (caller)[1,2];

	print STDERR basename($file), sprintf(' % 4d: ', $line), @args, "\n";
	return $self;
}

sub setup {
	my $class = shift;

	my $dollar_zero  = shift;
	my $opennms_home = shift;
	my $rpm_name     = shift;
	my $rpm_version  = shift;

	$_OPENNMS_HOME = $opennms_home;

	if (not defined $rpm_version) {
		croak "usage: $dollar_zero <\$opennms_home> <rpm_package_name> <rpm_package_rpm_version>\n";
	}

	my $config      = $class->new($opennms_home);
	my $version     = $config->existing_version($rpm_name);
	my $pristinedir = $config->pristine_dir();
	my $etcdir      = $config->etc_dir();

	print STDERR "=" x 80, "\n";
	print STDERR "$dollar_zero $opennms_home $rpm_name $rpm_version\n";
	system("rpm --verify $rpm_name | grep $etcdir");
	print STDERR "=" x 80, "\n";

	return ($config, $version, $pristinedir, $etcdir, $rpm_name, $rpm_version);
}

sub get_conflicted_file {
	my $config = shift;
	if (not defined $config and defined $_OPENNMS_HOME) {
		$config = OpenNMS::Config->new($_OPENNMS_HOME);
	}
	if (not defined $config) {
		return;
	}
	return File::Spec->catfile($config->etc_dir(), '.git', 'opennms-conflicted');
}

sub is_conflicted {
	my $self = shift;
	return (-e $self->get_conflicted_file());
}

sub create_conflicted {
	my $conflicted = get_conflicted_file();
	write_file($conflicted, "Failed in $_OPENNMS_HOME\n");
	return $conflicted;
}

END {
	if ($? != 0) {
		my $conflicted = create_conflicted();
		print STDERR "ERROR: exiting non-zero. Creating '$conflicted' file.\n";
	}
};

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


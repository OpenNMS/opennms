package OpenNMS::Config;

use 5.008008;
use strict;
use warnings;

use Carp;
use File::Spec;

our $VERSION = '0.1.0';

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

sub existing_version {
	my $self = shift;
	my $package = shift;
	
	if (not defined $package) {
		croak "You must specify a package name to query!\n";
	}
	my $version = `rpm -q --queryformat='\%{version}-\%{release}' $package 2>/dev/null`;
	chomp($version);
	$version = "0.0-0" if ($version eq '');
	return $version;
}

sub pristine_dir {
	my $self = shift;
	return File::Spec->catdir($self->home(), 'share', 'etc-pristine');
}

sub etc_dir {
	my $self = shift;
	return File::Spec->catdir($self->home(), 'etc');
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


package OpenNMS::Config::RPM;

use 5.008008;
use strict;
use warnings;

use Carp;
use Cwd 'abs_path';

require Exporter;

our @ISA = qw(Exporter);

our $VERSION = '0.1.0';

=head1 NAME

OpenNMS::Config::RPM - Manipulate RPM packages.

=head1 SYNOPSIS

  use OpenNMS::Config::RPM;

=head1 DESCRIPTION

This module can create RPMs to a specification.

=head1 CONSTRUCTOR

OpenNMS::Config::RPM->new($output_directory)

Given an output directory, create an RPM object.

=cut

sub new {
	my $proto = shift;
	my $class = ref($proto) || $proto;

	my $file = shift;
	if (not defined $file) {
		carp "You must pass the RPM file!";
	}

	my $self = {
		RPM => $file,
	};

	bless($self, $class);
	return $self;
}

=head1 STATIC (OBJECT) METHODS

=head2 * install

Install one or more RPMs.

Options:

=over 2

=item * rpms

A reference to an array of OpenNMS::Config::RPM objects.

=item * root

The installation root directory.

=back

=cut

sub install {
	my $self = shift;
	
	my %options = @_;
	
	my $rpms = $options{'rpms'};

	if (not defined $rpms) {
		croak "you must specify a list of RPMs!";
	}

	my @args;

	my $root = $options{'root'};
	if (defined $root) {
		push(@args, '--installroot', abs_path($root));
	}

	# return system('yum', '--nogpgcheck', '-y', 'localinstall', @args, map { $_->file } @$rpms) == 0;
	return system("yum --nogpgcheck -y localinstall @args " . join(' ', map { $_->file } @$rpms) . " 1>\&2") == 0;
}

=head1 METHODS

=head2 * file

The location of the RPM file.

=cut

sub file {
	my $self = shift;
	return $self->{RPM};
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


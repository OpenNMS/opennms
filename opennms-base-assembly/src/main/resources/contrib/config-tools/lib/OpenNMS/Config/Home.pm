package OpenNMS::Home;

use 5.008008;
use strict;
use warnings;

use Carp;

require Exporter;

our @ISA = qw(Exporter);

our $VERSION = '0.1.0';

=head1 NAME

OpenNMS::Home - OpenNMS home directory.

=head1 SYNOPSIS

  use OpenNMS::Home;

=head1 DESCRIPTION

This module represents the OpenNMS home directory.

=head1 CONSTRUCTOR

OpenNMS::Config::Home->new($OPENNMS_HOME)

Given an OpenNMS home directory, create a Home object.

=cut

sub new {
	my $proto = shift;
	my $class = ref($proto) || $proto;

	my $root = shift;
	if (not defined $root) {
		carp "You must pass the root that represents \$OPENNMS_HOME!";
	}

	my $self = {
		ROOT => $root,
	};

	return $self;
}

=head1 METHODS

=head2 * root

The root of this OpenNMS home directory.

=cut

sub root {
	my $self = shift;
	return $self->{ROOT};
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


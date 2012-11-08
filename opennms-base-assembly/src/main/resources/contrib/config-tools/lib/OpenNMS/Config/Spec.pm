package OpenNMS::Config::Spec;

use 5.008008;
use strict;
use warnings;

use Carp;
use Cwd qw(abs_path);
use File::Basename;
use File::Copy;
use File::Path;
use File::Spec;
use IO::Handle;

use OpenNMS::Config::RPM;

require Exporter;

our @ISA = qw(Exporter);

our $VERSION = '0.1.0';

=head1 NAME

OpenNMS::Config::Spec - Manipulate RPM spec files.

=head1 SYNOPSIS

  use OpenNMS::Config::Spec;

=head1 DESCRIPTION

This module can create RPMs to a specification.

=head1 CONSTRUCTOR

OpenNMS::Config::Spec->new(%options)

=head2 OPTIONS

=over 2

=item * topdir

The directory to build the RPM files in.

=back

=cut

sub new {
	my $proto = shift;
	my $class = ref($proto) || $proto;

	my $spec = shift;

	my $self = {
		SPEC => $spec,
	};

	bless($self, $class);
	return $self;
}

=head1 METHODS

=head2 * spec

The path to the RPM spec file.

=cut

sub spec {
	my $self = shift;
	return $self->{SPEC};
}

=head2 * add_source($filename, $copy_from)

Given a target filename in the SOURCES/ directory, and a filename to copy from,
add a source file to the SOURCES/ directory when building.

=cut

sub add_source {
	my $self = shift;
	my $to_filename = shift;
	my $from_filename = shift;
	
	$self->{SOURCES}->{$to_filename} = $from_filename;
}

=head2 * build

Build RPM(s) from this spec file.

Options that can be passed:

=over 2

=item * _topdir

The top directory to build RPMs in.

=back

=cut

sub build {
	my $self = shift;
	my %options = @_;
	
	my $rpmrc = IO::Handle->new();
	
	my $topdir = $options{_topdir};
	if (not defined $topdir) {
		open($rpmrc, '-|', "rpm --showrc") or croak "Unable to run rpm --showrc: $!";
		while (<$rpmrc>) {
			chomp;
			if (/[0-9\-]:\s+_topdir\s+(.*)\s*$/) {
				$topdir = $1;
				last;
			}
		}
		close($rpmrc) or croak "Unable to close filehandle for rpm --showrc: $!";
	}

	mkpath($topdir);
	$topdir = abs_path($topdir);

	$options{_topdir} = $topdir;

	my @args;
	for my $key (keys %options) {
		push(@args, '--define', $key . ' ' . $options{$key});
	}

	for my $subdir ('BUILD', 'RPMS', 'SOURCES', 'SPECS') {
		my $dir = File::Spec->catdir($topdir, $subdir);
		print STDERR "dir = $dir\n";
		if (not -d $dir) {
			mkpath($dir);
		}
	}	

	for my $targetfile (keys %{$self->{SOURCES}}) {
		my $targetdir = dirname($targetfile);
		mkpath($targetdir);
		my $copy_from = $self->{SOURCES}->{$targetfile};
		copy($copy_from, File::Spec->catfile($topdir, 'SOURCES', $targetfile)) or croak "Unable to copy $copy_from to $targetfile";
	}

	my @rpms;
	if (system('rpmbuild', '-v', @args, '-bb', $self->spec) != 0) {
		croak("RPM build failed");
	}
	
	my $rpmdir = File::Spec->catdir($topdir, "RPMS", "noarch");

	my $dir = IO::Handle->new();
	opendir($dir, $rpmdir) or croak "Unable to open $rpmdir directory: $!";
	while (my $entry = readdir($dir)) {
		chomp($entry);
		if ($entry =~ /\.rpm$/) {
			my $rpm = OpenNMS::Config::RPM->new(File::Spec->catfile($rpmdir, $entry));
			push(@rpms, $rpm);
		}
	}
	closedir($dir) or croak "Unable to close filehandle for $rpmdir directory: $!";

	return \@rpms;
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


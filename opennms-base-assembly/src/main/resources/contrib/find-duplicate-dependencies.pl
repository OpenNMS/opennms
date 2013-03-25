#!/usr/bin/env perl -w

use warnings;

use File::Basename;
use File::Find;
use IO::Handle;
use Data::Dumper;
use IPC::Run3;

use vars qw(
	$DIR
	$PEDANTIC
	$WARNINGS
);

$DIR = shift(@ARGV);
$PEDANTIC = shift(@ARGV);
$PEDANTIC = (defined $PEDANTIC and $PEDANTIC eq "-p");

$WARNINGS = 0;

if (not -d $DIR) {
	print STDERR "usage: $0 <\$OPENNMS_HOME/lib path> [-p]\n";
	exit 1;
}

if ($PEDANTIC) {
	print STDERR "[[running in pedantic mode]]\n";
}

my $jars  = {};
my $files = {};

sub warning {
	print $@;
	$WARNINGS++;
}

find({
	wanted => sub {
		my $dir       = $File::Find::dir;
		my $filename  = $File::Find::name;
		my $shortname = basename($filename);
		return unless ($filename =~ /\.jar$/);

		my ($name, $version) = $shortname =~ /^(.*)-(\d+\..*?)\.jar$/;

		if (not defined $name or not defined $version) {
			if ($shortname =~ /^(jtidy|vijava)-(.*)\.jar$/) {
				$jars->{$1}->{$2}++;
			} elsif ($shortname =~ /^(karaf|karaf-client|karaf-jaas-boot|opennms-branding|opennms_bootstrap|opennms_install|opennms_system_report)\.jar$/) {
				$jars->{$1}->{0}++;
			} else {
				warning("WARNING: not sure how to determine version for $shortname!\n");
			}
		} else {
			if ($version =~ /SNAPSHOT-(xsds|liquibase)$/) {
				# ignore these, they're just classifier'd
			} elsif ($shortname =~ /^(karaf|karaf-client|karaf-jaas-boot|opennms-branding|opennms_bootstrap|opennms_install|opennms_system_report)\.jar$/) {
				$jars->{$1}->{0}++;
			} else {
				#print STDERR "dir = $dir, shortname = $shortname, filename = $filename, name = $name, version = $version\n";
				$jars->{$name}->{$version}++;
			}
		}

		my @lines;
		run3 [ 'jar', '-tf', $filename ], \undef, \@lines;
		for my $line (@lines) {
			chomp($line);
			next if ($line =~ /\/$/);
			if ($filename =~/jdtcore-\*.jar/) {
				# special case, skip the compiler jar in jdtcore
				next if ($line =~ /jdtCompilerAdapter.jar/);
			}
			$files->{$line}->{$filename}++;
		}
	},
	follow => 1,
	no_chdir => 1,
}, $DIR);

#print Dumper($files), "\n";
for my $jar (sort keys %$jars) {
	if (keys %{$jars->{$jar}} > 1) {
		warning("WARNING: multiple version of jar $jar: " . join(', ', sort keys %{$jars->{$jar}}) . "\n");
	}
}

# iterate over this twice, once to find jar files inside jars, then again
# to look for duplicate classes between jars

for my $jar (sort keys %$files) {
	next if ($jar !~ /\.jar$/);
	for my $file (sort keys %{$files->{$jar}}) {
		warning("WARNING: $file contains a jar file $jar\n");
	}
}

for my $file (sort keys %$files) {
	next unless ($file =~ /\.class$/);
	if (not $PEDANTIC) {
		next unless ($file =~ /org\/opennms/);
	}

	if (keys %{$files->{$file}} > 1) {
		warning("WARNING: multiple copies of class $file in separate jars: " . join(', ', sort keys %{$files->{$file}}) . "\n");
	}
}

exit($WARNINGS);

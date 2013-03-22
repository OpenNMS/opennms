#!/usr/bin/env perl -w

use warnings;

use File::Find;
use IO::Handle;
use Data::Dumper;
use IPC::Run3;

my $dir = shift(@ARGV);
my $VERBOSE = shift(@ARGV);

$VERBOSE = (defined $VERBOSE and $VERBOSE eq "-v");

if (not -d $dir) {
	print STDERR "usage: $0 <\$OPENNMS_HOME/lib path>\n";
	exit 1;
}

my $jars  = {};
my $files = {};

find({
	wanted => sub {
		my $shortname = $_;
		my $dir       = $File::Find::dir;
		my $filename  = $File::Find::name;
		return unless ($filename =~ /\.jar$/);

		my ($name, $version) = $shortname =~ /^(.*)-(\d+\..*?)\.jar$/;
		if (not defined $name or not defined $version) {
			if ($shortname =~ /^(jtidy|vijava)-(.*)\.jar$/) {
				$jars->{$1}->{$2}++;
			} elsif ($shortname =~ /^(karaf|karaf-client|karaf-jaas-boot|opennms-branding|opennms_bootstrap|opennms_install|opennms_system_report)\.jar$/) {
				$jars->{$1}->{0}++;
			} else {
				print STDERR "WARNING: not sure how to determine version for $shortname!\n";
			}
		} else {
			if ($version =~ /SNAPSHOT-(xsds|liquibase)$/) {
				# ignore these, they're just classifier'd
			} else {
				$jars->{$name}->{$version}++;
			}
		}

		my @lines;
		run3 [ 'jar', '-tf', $filename ], \undef, \@lines;
		for my $line (@lines) {
			chomp($line);
			next if ($line =~ /\/$/);
			$files->{$line}->{$filename}++;
		}
	},
	follow => 1,
	no_chdir => 1,
}, $dir);

#print Dumper($files), "\n";
for my $jar (sort keys %$jars) {
	if (keys %{$jars->{$jar}} > 1) {
		print "WARNING: multiple version of jar $jar: " . join(', ', sort keys %{$jars->{$jar}}) . "\n";
	}
}

# iterate over this twice, once to find jar files inside jars, then again
# to look for duplicate classes between jars

for my $jar (sort keys %$files) {
	next if ($jar !~ /\.jar$/);
	for my $file (sort keys %{$files->{$jar}}) {
		print "WARNING: $file contains a jar file $jar\n";
	}
}

for my $file (sort keys %$files) {
	next unless ($file =~ /\.class$/);
	if (not $VERBOSE) {
		next unless ($file =~ /org\/opennms/);
	}

	if (keys %{$files->{$file}} > 1) {
		print "WARNING: multiple copies of class $file in separate jars: " . join(', ', sort keys %{$files->{$file}}) . "\n";
	}
}

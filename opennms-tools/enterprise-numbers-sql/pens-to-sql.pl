#!/usr/bin/perl

my $PENFILE = "enterprise-numbers";
my $SQLFILE = "enterprise-numbers.sql";

open IFH, "<${PENFILE}" or die "Failed to open ${PENFILE} for reading: $!";
open OFH, ">${SQLFILE}" or die "Failed to open ${SQLFILE} for writing: $!";

my $indata = 0;
my $inrecord = 0;
my $number; undef $number;
my $name; undef $name;

print OFH <<EOT;
SELECT CASE
	WHEN entstats.entnum IS NULL THEN '--'
	ELSE entstats.entnum END AS entnum,
CASE
EOT

sub writeRecord($$) {
	my ($number, $name) = @_;
	$name =~ s/'/''/g;
	print OFH "\tWHEN entnum = '${number}' THEN '${name}' \n";
}

while (my $inline = <IFH>) {
	chomp $inline;
	if (! $indata) {
		$indata = 1 if ( $inline =~ /^0$/ );
	}
	next if (! $indata);

	if (! $inrecord) {
		$inrecord = 1 if ( $inline =~ /^\d+$/ );
	}
	next if (! $inrecord);

	if ( $inrecord && $inline =~ /^\d+$/ ) {
		$number = $inline;
	} elsif ( $inrecord && $inline =~ /^  (\S+.*)$/ ) {
		$name = $1;
		writeRecord( $number, $name );
		$inrecord = 0;
	}
}

print OFH <<EOT;
	WHEN entnum IS NULL THEN '(IETF or Other Non-Enterprise Standards Body)'
	ELSE '(Unknown)'
END AS entname,
entstats.tally AS tally FROM
	(SELECT SUBSTRING(eventsnmp from '^\\.1\\.3\\.6\\.1\\.4\\.1\\.(\\d+)[.,]') AS entnum,
	 COUNT(*) AS tally
	 FROM events
	 WHERE eventtime > NOW() - INTERVAL '1 week'
	 AND eventuei = 'uei.opennms.org/generic/traps/EnterpriseDefault'
	 AND eventsnmp IS NOT NULL
	 GROUP BY entnum
	 ORDER BY tally DESC) AS entstats;
EOT


close;

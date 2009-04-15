#!/usr/bin/perl

# scrub an snmpwalk file to remove potentially sensitive customer information
#
#
# This file is part of the OpenNMS(R) Application.
#
# OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
# OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
#
# For more information contact:
# OpenNMS Licensing       <license@opennms.org>
#     http://www.opennms.org/
#     http://www.opennms.com/

use IO::Handle;

my $infile  = shift @ARGV;
my $outfile = shift @ARGV || $infile . '.anonymized.txt';

if (not -f $infile) {
	die "you must specify a filename!";
}

my $IPS = {};
my $MACS = {};

my $inhandle = IO::Handle->new();
open ($inhandle, $infile) or die "unable to read from $infile: $!\n";

my $outhandle = IO::Handle->new();
open ($outhandle, '>' . $outfile) or die "unable to write to $outfile: $!\n";

my $ifaliascount = 0;
my $line;
while ($line = <$inhandle>) {
	chomp($line);

	my ($oid, $value);
	if (($oid, $value) = $line =~ /^(\.1\.3\.6\.1\.2\.1\.2\.2\.1\.6.*?)\s+\=\s+STRING\:\s+(.*?)\s*$/) {
		if ($value !~ /^\s*$/) {
			my $newmac = get_new_mac($value);
			print $outhandle sprintf("\%s = STRING: \%s\n", $oid, $newmac);
		} else {
			print $outhandle $line, "\n";
		}
	} elsif (($oid, $value) = $line =~ /^(\.1\.3\.6\.1\.2\.1\.4\.20\.1\.1\.).*?\s+\=\s+IpAddress\:\s+(.*?)\s*$/) {
		my $newip = get_new_ip($value);
		$oid .= $newip;
		print $outhandle sprintf("\%s = IpAddress: \%s\n", $oid, $newip);
	} elsif (($oid, $value) = $line =~ /^(\.1\.3\.6\.1\.2\.1\.31\.1\.1\.1\.18\.)(.*?)\s+\=\s+STRING\:.*$/) {
		print $outhandle sprintf("\%s\%d = STRING: (scrubbed ifalias #\%d)\n", $oid, $value, $value);
	} elsif ($line =~ /^\.1\.3\.6\.1\.2\.1\.1\.1\.0/) {
		print $outhandle <<END;
.1.3.6.1.2.1.1.1.0 = STRING: scrubbed.opennms.org
.1.3.6.1.2.1.1.2.0 = OID: .1.3.6.1.4.1.5813.1
.1.3.6.1.2.1.1.3.0 = Timeticks: (727976267) 84 days, 6:09:22.67
.1.3.6.1.2.1.1.4.0 = STRING: Jack McWinkle
.1.3.6.1.2.1.1.5.0 = STRING: scrubbed.opennms.org
.1.3.6.1.2.1.1.6.0 = STRING: Candyland
.1.3.6.1.2.1.1.7.0 = INTEGER: 4
END
	} elsif ($line =~ /^\.1\.3\.6\.1\.2\.1\.1/) {
		# skip, we already wrote that stuff out
	} else {
		# skip translation if this line doesn't look like it has a MAC or IP address
		if ($line =~ /^.*(?:(?:[[:xdigit:]]{1,2}[-:]){5}[[:xdigit:]]{1,2}|\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}).*$/) {
			# my $key likes it!
			my $key;
			for $key (keys %$IPS) {
				$line =~ s/\b$key\b/$IPS->{$key}/g;
			}
			for $key (keys %$MACS) {
				$line =~ s/\b$key\b/$MACS->{$key}/g;
			}
		}
		print $outhandle $line, "\n";
	}
}

sub get_new_ip {
	my $ip = shift;

	if (exists $IPS->{$ip}) {
		return $IPS->{$ip};
	}

	my @newip = (10);
	push(@newip, int(rand(255)));
	push(@newip, int(rand(255)));
	push(@newip, (int(rand(254)) + 1));

	$IPS->{$ip} = join('.', @newip);
	print "- converting IP: $ip to $IPS->{$ip}\n";
	return $IPS->{$ip};
}

sub get_new_mac {
	my $mac = shift;

	if (exists $MACS->{$mac}) {
		return $MACS->{$mac};
	}

	my @newaddr;
	for (1..6) {
		push(@newaddr, int(rand(256)));
	}
	$MACS->{$mac} = sprintf('%x:%x:%x:%x:%x:%x', @newaddr);
	print "- converting MAC: $mac to $MACS->{$mac}\n";
	return $MACS->{$mac};
}

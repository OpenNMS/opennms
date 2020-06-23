#!/usr/bin/perl

use strict;
use File::Copy qw(move);

my $etc = shift or die "Please provide the path of the OpenNMS Configuration Directory (for example, /opt/opennms/etc)\n";
die "Can't find $etc\n" unless -d $etc;

my $templates = "$etc/snmp-graph.properties.d";
die "Can't find $templates\n" unless -d $templates;

opendir DIR, $templates;
my @files = grep '\.properties', readdir DIR;
closedir DIR;

foreach my $file (@files) {
    print "Processing $file\n";
    my $in  = "$templates/$file";
    my $out = "$in.temp";
    open IN, $in or die "Can't open $in\n";
    open OUT, ">$out" or die "Can't write $out\n";
    my $bad = 0;
    while (<IN>) {
       if (m/\w+:.*"[^":\\]*:/) {
           $bad++;
           s/"([^":\\]*):/"$1\\\\:/;
       }
       print OUT $_;
    }
    close IN;
    close OUT;
    if ($bad) {
        print "    PROBLEM FOUND: Fixing $bad lines\n";
        move($out, $in);
    } else {
        unlink $out;
    }
}

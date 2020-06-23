#!/usr/bin/perl

# Created by Alejandro Galue <agalue@opennms.org>

use strict;

my $configFile = shift @ARGV or die "Please specify the JMX file you want to patch\n";

my $isXml = $configFile =~ m/\.xml$/;

open CFG, $configFile or die "Can't read $configFile\n";
while(my $line = <CFG>) {
    if ($isXml) {
        $line = applyFix($line, $1) if $line =~ m/alias="(.+\..+)"/;
    } else {
        $line = applyFix($line, $1) if $line =~ m/ DEF:.+:(.+\..+):/;
        if ($line =~ m/\.columns=(.+)$/) {
            my $columns = $1;
            my @words = split /[,\s]+/, $columns;
            foreach my $w (@words) {
                $line = applyFix($line, $w);
            }
        }
    }
    print $line;
}
close CFG;

sub applyFix {
    my ($line, $word) = @_;
    return $line unless $word;
    my $target = $word;
    if ($target =~ m/.+\..+/) {
        $target =~ s/\.(\w+)/\u$1/;
    } else {
        $target =~ s/\.//;
    }
    $line =~ s/$word/$target/;
    return $line;
}

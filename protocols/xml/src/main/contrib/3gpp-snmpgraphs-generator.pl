#!/usr/bin/perl

use strict;
use XML::Simple;

my $file = shift;
die "Need the 3GPP XML sample file.\n" unless $file;
die "File $file does not exist\n" unless -e $file;

my $ref = XMLin($file, ForceArray => [ 'measInfo', 'measType' ]);

my @report_ids;
my @reports;

my $index = 1;
foreach my $measInfo (@{$ref->{measData}{measInfo}}) {
    my $groupName = getGroupName($measInfo->{measInfoId});
    my $groupType = getGroupType($measInfo->{measInfoId});
    my @tmp;
    for my $measType (@{$measInfo->{measType}}) {
        my $name = "var" . sprintf("%04d", $index++); # To avoid problems with big names.
        my $rpt  = "3gpp.$groupType.$name";
        push @tmp, $rpt;
        push @reports, <<EOF;
report.$rpt.name=3GPP - $measType->{content}
report.$rpt.columns=$name
report.$rpt.propertiesValues=instance
report.$rpt.type=$groupType
report.$rpt.command=--title="{instance}" \\
 DEF:v1={rrd1}:$name:AVERAGE \\
 LINE2:v1#ff0000:"$measType->{content}" \\
 COMMENT:"\\\\n" \\
 GPRINT:v1:AVERAGE:"Avg\\\\: %8.2lf %s" \\
 GPRINT:v1:MIN:"Min\\\\: %8.2lf %s" \\
 GPRINT:v1:MAX:"Max\\\\: %8.2lf %s\\\\n"
EOF
    }
    push @report_ids, join(",", @tmp);
}

print "reports=" . join(", \\\n", @report_ids), "\n\n";
foreach my $rpt (@reports) {
    print "$rpt\n";
}

sub getGroupName($) {
    my $id = shift @_;
    $id =~ s/\|/-/g;
    return $id;
}

sub getGroupType($) {
    my $id = shift @_;
    my @data = split /-/, getGroupName($id);
    for my $i (1..@data-1) {
        $data[$i] = ucfirst($data[$i]);
    }
    return join('', @data);
}


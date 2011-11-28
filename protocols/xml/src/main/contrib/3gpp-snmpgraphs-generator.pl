#!${install.perl.bin}

use strict;
use XML::Simple;

#-----------------------------------------------------------
# COMMON VARIABLES
#-----------------------------------------------------------

my $height = 150;
my $width  = 600;
my $color  = "ff0000";

#-----------------------------------------------------------
# DO NOT CHANGE ANYTHING FROM HERE
#-----------------------------------------------------------

my $file = shift;
die "Need the 3GPP XML sample file.\n" unless $file;
die "File $file does not exist\n" unless -e $file;

print "Parsing XML file $file\n";
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
report.$rpt.propertiesValues=label
report.$rpt.type=$groupType
report.$rpt.height=$height
report.$rpt.width=$width
report.$rpt.command=--title="{label}" \\
 --height $height --width $width \\
 DEF:v1={rrd1}:$name:AVERAGE \\
 LINE2:v1#$color:"$measType->{content}" \\
 COMMENT:"\\\\n" \\
 GPRINT:v1:AVERAGE:"    Avg\\\\: %8.2lf %s" \\
 GPRINT:v1:MIN:"Min\\\\: %8.2lf %s" \\
 GPRINT:v1:MAX:"Max\\\\: %8.2lf %s\\\\n"
EOF
    }
    push @report_ids, join(",", @tmp);
}

my $out = "3gpp.graphs.properties";
print "Generating $out\n";
open XML, ">$out" or die "Can't write $out\n";
print XML "reports=" . join(", \\\n", @report_ids), "\n\n";
foreach my $rpt (@reports) {
    print XML "$rpt\n";
}
close XML;

print "Remember to put $out into \$OPENNMS_HOME/etc/snmp-graph.properties.d/\n";

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


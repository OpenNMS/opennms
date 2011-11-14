#!/usr/bin/perl

use strict;
use XML::Simple;

my $file = shift;
die "Need the 3GPP XML sample file.\n" unless $file;
die "File $file does not exist\n" unless -e $file;

my $ref = XMLin($file, ForceArray => [ 'measInfo', 'measType' ]);

print <<EOF;
<xml-datacollection-config rrdRepository="/opt/opennms/share/rrd/snmp/" xmlns="http://xmlns.opennms.org/xsd/config/xml-datacollection">
    <xml-collection name="3GPP">
        <rrd step="300">
            <rra>RRA:AVERAGE:0.5:1:8928</rra>
            <rra>RRA:AVERAGE:0.5:12:8784</rra>
            <rra>RRA:MIN:0.5:12:8784</rra>
            <rra>RRA:MAX:0.5:12:8784</rra>
        </rrd>
        <xml-source url="sftp.3gpp://opennms:Op3nNMS!\@{ipaddr}/opt/3gpp/data/?step={step}&amp;neId={foreignId}">
EOF

foreach my $measInfo (@{$ref->{measData}{measInfo}}) {
    my $groupName = getGroupName($measInfo->{measInfoId});
    my $groupType = getGroupType($measInfo->{measInfoId});
    print <<EOF;
            <xml-group name="$groupName" resource-type="$groupType"
                key-xpath="\@measObjLdn"
                resource-xpath="/measCollecFile/measData/measInfo[\@measInfoId='$measInfo->{measInfoId}']/measValue"
                timestamp-xpath="/measCollecFile/fileFooter/measCollec/\@endTime"
                timestamp-format="yyyy-MM-dd'T'HH:mm:ssZ">
                <xml-object name="suspect" type="STRING" xpath="suspect" />
EOF
    for my $measType (@{$measInfo->{measType}}) {
        my $idx  = $measType->{p};
        my $name = "var$idx"; # To avoid problems with big names.
        print <<EOF;
                <xml-object name="$name" type="GAUGE" xpath="r[\@p=$idx]" /> <!-- $measType->{content} -->
EOF
    }
    print <<EOF;
            </xml-group>
EOF
}

print <<EOF;
        </xml-source>
    </xml-collection>
 </xml-datacollection-config>
EOF

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

sub getObjectName($) {
    my $id = shift @_;
    $id =~ s/\.U$//;
    my @data = split /[-\.]/, $id;
    return $data[0] . ucfirst($data[@data-1]);
}

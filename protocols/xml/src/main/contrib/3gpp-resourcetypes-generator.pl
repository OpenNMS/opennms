#!/usr/bin/perl

use strict;
use XML::Simple;

my $file = shift;
die "Need the 3GPP XML sample file.\n" unless $file;
die "File $file does not exist\n" unless -e $file;

my $ref = XMLin($file, ForceArray => [ 'measInfo', 'measType' ]);

print <<EOF;
<?xml version="1.0"?>
<datacollection-group name="3GPP">
EOF

foreach my $measInfo (@{$ref->{measData}{measInfo}}) {
    my $groupType = getGroupType($measInfo->{measInfoId});
    print <<EOF;
  <resourceType name="$groupType" label="3GPP $measInfo->{measInfoId}" resourceLabel="\${label}" >
    <persistenceSelectorStrategy class="org.opennms.netmgt.collectd.PersistRegexSelectorStrategy">
      <parameter key="match-expression" value="#suspect=='false'" />
    </persistenceSelectorStrategy>
    <storageStrategy class="org.opennms.protocols.xml.collector.XmlStorageStrategy"/>
  </resourceType>
EOF
}

print <<EOF;
</datacollection-group>
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


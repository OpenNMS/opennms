#!${install.perl.bin}

use strict;
use XML::Simple;

#-----------------------------------------------------------
# DO NOT CHANGE ANYTHING FROM HERE
#-----------------------------------------------------------

my $file = shift;
die "Need the 3GPP XML sample file.\n" unless $file;
die "File $file does not exist\n" unless -e $file;

print "Parsing XML file $file\n";
my $ref = XMLin($file, ForceArray => [ 'measInfo', 'measType' ]);

my $out = "3gpp.types.xml";
print "Generating $out\n";
open XML, ">$out" or die "Can't write $out\n";
print XML <<EOF;
<?xml version="1.0"?>
<datacollection-group name="3GPP">
EOF

foreach my $measInfo (@{$ref->{measData}{measInfo}}) {
    my $groupType = getGroupType($measInfo->{measInfoId});
    print XML <<EOF;
  <resourceType name="$groupType" label="3GPP $measInfo->{measInfoId}" resourceLabel="\${label}" >
    <persistenceSelectorStrategy class="org.opennms.netmgt.collectd.PersistRegexSelectorStrategy">
      <parameter key="match-expression" value="#suspect=='false'" />
    </persistenceSelectorStrategy>
    <storageStrategy class="org.opennms.protocols.xml.collector.XmlStorageStrategy"/>
  </resourceType>
EOF
}

print XML <<EOF;
</datacollection-group>
EOF
close XML;

print "Remember to put $out into \$OPENNMS_HOME/etc/datacollection/\n";

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


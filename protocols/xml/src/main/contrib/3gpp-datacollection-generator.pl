#!${install.perl.bin}

use strict;
use XML::Simple;

#-----------------------------------------------------------
# COMMON VARIABLES
#-----------------------------------------------------------

my $share_dir = "${install.share.dir}";
my $xml_dir   = "/opt/hitachi/cnp/data/pm/reports/3gpp/5";
my $username  = "opennms";
my $password  = "Op3nNMS";

#-----------------------------------------------------------
# DO NOT CHANGE ANYTHING FROM HERE
#-----------------------------------------------------------

my $file      = shift;
die "Need the 3GPP XML sample file.\n" unless $file;
die "File $file does not exist\n" unless -e $file;

print "Parsing XML file $file\n";
my $ref = XMLin($file, ForceArray => [ 'measInfo', 'measType' ]);

my $out = "xml-datacollection-config.xml";
print "Generating $out\n";
open XML, ">$out" or die "Can't write $out\n";
print XML <<EOF;
<xml-datacollection-config rrdRepository="$share_dir/rrd/snmp" xmlns="http://xmlns.opennms.org/xsd/config/xml-datacollection">
    <xml-collection name="3GPP">
        <rrd step="300">
            <rra>RRA:AVERAGE:0.5:1:2016</rra>
            <rra>RRA:AVERAGE:0.5:12:1488</rra>
            <rra>RRA:AVERAGE:0.5:288:366</rra>
            <rra>RRA:MAX:0.5:288:366</rra>
            <rra>RRA:MIN:0.5:288:366</rra>
        </rrd>
        <xml-source url="sftp.3gpp://$username:$password\@{ipaddr}$xml_dir?step={step}&amp;neId={foreignId}">
            <import-groups>xml-datacollection/3gpp.full.xml</import-groups>
        </xml-source>
    </xml-collection>
 </xml-datacollection-config>
EOF
close XML;

my $subdir = "xml-datacollection";
mkdir $subdir unless -e $subdir;
$out = "$subdir/3gpp.full.xml";
print "Generating $out\n";
open XML, ">$out" or die "Can't write $out\n";
print XML <<EOF;
<xml-groups xmlns="http://xmlns.opennms.org/xsd/config/xml-datacollection">
EOF

my $index = 1;
foreach my $measInfo (@{$ref->{measData}{measInfo}}) {
    my $groupName = getGroupName($measInfo->{measInfoId});
    my $groupType = getGroupType($measInfo->{measInfoId});
    print XML <<EOF;
    <xml-group name="$groupName" resource-type="$groupType"
        resource-xpath="/measCollecFile/measData/measInfo[\@measInfoId='$measInfo->{measInfoId}']/measValue"
        key-xpath="\@measObjLdn"
        timestamp-xpath="/measCollecFile/fileFooter/measCollec/\@endTime"
        timestamp-format="yyyy-MM-dd'T'HH:mm:ssZ">
        <xml-object name="suspect" type="STRING" xpath="suspect" />
EOF
    for my $measType (@{$measInfo->{measType}}) {
        my $idx  = $measType->{p};
        my $name = "var" . sprintf("%04d", $index++); # To avoid problems with big names.
        print XML <<EOF;
        <xml-object name="$name" type="GAUGE" xpath="r[\@p=$idx]" /> <!-- $measType->{content} -->
EOF
    }
    print XML <<EOF;
    </xml-group>
EOF
}

print XML <<EOF;
 </xml-groups>
EOF
close XML;

print "Remember to put xml-datacollection* into \$OPENNMS_HOME/etc/\n";

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

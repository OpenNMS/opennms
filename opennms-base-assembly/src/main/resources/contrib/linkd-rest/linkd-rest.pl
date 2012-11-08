#!/usr/bin/perl

use strict;
use warnings;

use XML::Simple;
use Data::Dumper;
use DateTime;

my $CURL = "curl -k -u admin:juniper123"; 
my $REST_PREFIX = '/opennms/rest';
my $source_xml = $ARGV[0];
my $space_server = $ARGV[1];


# Use localhost and port 8980 unless a hostname is passed
if ( defined $space_server ) {
    $space_server .= $REST_PREFIX;
}
else {
    $space_server = 'http://localhost:8980' . $REST_PREFIX;
}


#my $source_xml = '
#<Links>
#<Link>
#        <AEND deviceName="austin-mx80" interface="em0"/>
#        <ZEND deviceName="phoenix-mx80" interface="ge-1/0/0.217"/>
#</Link>
#</Links>
#';
# New format
#<link status="A" source="mysource">
#    <ifIndex>1</ifIndex>
#    <lastPollTime>2012-10-30T16:56:26.271-04:00</lastPollTime>
#    <linkTypeId>-1</linkTypeId>
#    <nodeId>2</nodeId>
#    <nodeParentId>1</nodeParentId>
#    <parentIfIndex>1</parentIfIndex>
#  </link>


#
# Add links
#
if ( defined $source_xml and  $source_xml !~ /-delete/ ) {
    # Read in source link file
    my $ref = XMLin($source_xml, ForceArray => 1);
    add_links( $ref );
}
#
# Delete link
#
elsif ( defined $source_xml and $source_xml =~ /-delete=(\d+)/ ) {
   do_delete( $1 ); 
}
else {
    print_usage();
    exit 1;
}

exit 0;

#
# Subs
#
sub do_post {
    my $linkd_hash_ref = shift;

    # Check if link already exists
    my $parent_id =  $linkd_hash_ref->{'nodeParentId'};
    my $node_id   =  $linkd_hash_ref->{'nodeId'};
    my $parent_ifindex = $linkd_hash_ref->{'parentIfIndex'};
    my $node_ifindex = $linkd_hash_ref->{'ifIndex'};
    my $link_xml = `$CURL -X GET \'$space_server/links?ifIndex=$node_ifindex&parentIfIndex=$parent_ifindex&node.id=$node_id&nodeParentId=$parent_id\' 2>/dev/null`;

    if ( defined $link_xml and $link_xml !~ /count="0"/ ) {
        warn "The following link is already defined, skipping...\n";
        warn "[$link_xml]\n";
        return 1;
    }
    
    my $linkd_xml = XMLout( $linkd_hash_ref, RootName => 'link', NoAttr => '1');
    #print "$linkd_xml\n";
    $linkd_xml =~ s/link/link status="A" source="space"/;
    #print "$linkd_xml\n";
    `$CURL -X POST -o /dev/stdout -H 'Content-type: application/xml' -d \'$linkd_xml\' $space_server/links 2>/dev/null`;
    $link_xml = `$CURL -X GET \'$space_server/links?ifIndex=$node_ifindex&parentIfIndex=$parent_ifindex&node.id=$node_id&nodeParentId=$parent_id\' 2>/dev/null`;
    print "Added link [$link_xml]\n";
     
    
}

sub do_delete {
    my $link_id = shift;

    # Get the link
    my $link_xml = `$CURL -X GET $space_server/links/$link_id 2>/dev/null`;
    if ( not defined $link_xml or $link_xml eq '' ) {
        warn "Unable to find a link with id <$link_id>, skipping...\n";
        return 1;
    } 

    `$CURL -X DELETE $space_server/links/$link_id 2>/dev/null`;

    my $link_xml2 = `$CURL -X GET $space_server/links/$link_id 2>/dev/null`;

    if ( not defined $link_xml2 or $link_xml2 eq '' ) {
        print "The following link has been deleted\n";
        print "[$link_xml]\n";
    }
    else {
        warn "Unable to delete link with id <$link_id>\n";
    }

}

sub add_links {
    my $data_hash_ref = shift;
    my %linkd_hash;

    LINK:
    foreach my $link ( @ { $data_hash_ref->{'Link'} } ) {
        #print Dumper $link;
        my $parent_node_label = $link->{'AEND'}->[0]{'deviceName'};
        my $parent_node_ifdescr = $link->{'AEND'}->[0]{'interface'};
        my $node_label = $link->{'ZEND'}->[0]{'deviceName'};
        my $node_ifdescr = $link->{'ZEND'}->[0]{'interface'};
        # Get Node Ids
        my $parent_id = _get_nodeid( $parent_node_label );
        $linkd_hash{'nodeParentId'} = $parent_id;
        my $node_id = _get_nodeid( $node_label );
        $linkd_hash{'nodeId'} = $node_id;
        if ( not defined $parent_id or $parent_id eq '' or not defined $node_id or $node_id eq '' ) {
                warn "Unable to find matching device(s) for following link, skipping...\n";
                warn Dumper $link;
                next LINK;
        } 
        # Get Interface indexes
        $linkd_hash{'parentIfIndex'} = _get_ifindex( $parent_node_ifdescr, $parent_id );
        $linkd_hash{'ifIndex'} = _get_ifindex( $node_ifdescr, $node_id );
        # Hardcode the link type
        $linkd_hash{'linkTypeId'} = -1;
        # Add poll time
        $linkd_hash{'lastPollTime'} = ''. DateTime->now();
        #print Dumper \%linkd_hash;
        # Add the link
        if ( not grep { not defined $_ } values %linkd_hash ) {
            do_post(\%linkd_hash);
        }
        else {
                warn "Unable to find matching interface(s) for following link, skipping...\n";
                warn Dumper $link;
                next LINK;
        }
    }
}

sub _get_nodeid {
    my $label = shift;
    my $node_xml = `$CURL -X GET $space_server/nodes?label=$label 2>/dev/null`;
    #print "$CURL -X GET $space_server/nodes?label=$label\n";
    #print "$node_xml\n";
    my $node_ref = XMLin($node_xml);
    #print Dumper $node_ref;
    return  $node_ref->{'node'}->{'id'};

}

sub _get_ifindex {
    my $ifdescr = shift;
    my $id = shift;

    my $interface_xml = `$CURL -X GET $space_server/nodes/$id/snmpinterfaces?ifDescr=$ifdescr 2>/dev/null`;

    #print "$interface_xml\n";
    my $interface_ref = XMLin($interface_xml);
    #print Dumper $interface_ref;
    return  $interface_ref->{'snmpInterface'}->{'ifIndex'};
}

sub print_usage {
   warn "Usage: linkd-rest.pl <source_xml>|-delete=<id>  [(http|https)://space_server_ip:port]\n";
}

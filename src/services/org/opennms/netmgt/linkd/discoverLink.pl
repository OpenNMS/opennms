#! /usr/bin/perl
#########################################################################################
#											
# Program	- discoverLink.pl							
# Author	- rssntn67@yahoo.it								
# Version	- 1.2.0									
# Date	- 2004, 30 October
#											
# Comment	- This script is for  package that extends OpenNMS			
#        	- It uses SNMP  to discover mac addresses,stp info, routing info 		
#           - and populate database tables 
#           - "atInterface" "stpNode" "stpInterface" "ipRouteInterface"
#        	- also calculate datalink links and		
#        	- populate database table "dataLinkInterface".					
#    		- To code: event management on data update and insert
#    		- To code: security event generation
#           - Modify datalinkinterface to support multi link on the same port
#
#########################################################################################

use strict;

use OpenNMS::DbUtil;
use Net::SNMP 4.1.0 qw(oid_lex_sort oid_base_match SNMP_VERSION_1 DEBUG_ALL);
use XML::DOM;
use Socket;

use vars qw(
  $dbh
  $sth
  %sysoidmask2vlanoid
  %SNMP
  %nodeid2snmpprimaryip
  %snmpprimaryip2nodeid
  %ipaddr2nodeid
  %nodeid2ipaddrs
  %nodeid_ip2ifindex
  %snmpinterface
  %snmpifmac2nodeid
  %nodeid2sysoid
  %atinterface
  %iprouteinterface
  %nodeid_routenexthop2ifindex
  %stpnode
  %stpinterface
  %bridgenodeid2scalefactor
  %datalinkinterface
  %nodeid_bp2ifindex
  %mac2ip
  %mac2bp
  %bbbp
);

$SIG{TERM} = \&catch_zap;
my $lockfile = '/var/lock/discoverlink.pid';
my $linkconffile = '@install.etc.dir@/linkconf.xml';

die scalar localtime() . " main - file $linkconffile does not exist"
    if ( ! -f $linkconffile);

die scalar localtime() . "main - file $lockfile exist, linkd already running"
    if ( -f $lockfile );
my $pid = getpgrp(0);
open( LOCKFILE, ">$lockfile" )
    or &function_log( 4, "main - cannot open lock file $lockfile" );
print LOCKFILE "$pid\n";
close(LOCKFILE);

my $logfile = '@install.logs.dir@/link.log';    # file dove effettuare il log

open( STDERR, ">>$logfile" )
  or die scalar localtime()
  . " CRITICAL: discoverlink.pl cannot open file $logfile \n";

my $initial_sleep_time = 900000;

if ( $ARGV[0] =~ /\d+/ ) {
  $initial_sleep_time = $ARGV[0];
  shift @ARGV;
}

my $sleep_time     = 30000;
if ( $ARGV[0] =~ /\d+/ ) {
    $sleep_time = $ARGV[0];
    shift @ARGV;
}
# 	Log level
#   0  "DEBUG"
#   1  "INFO"
#   2  "WARN"
#   3 "ERROR"
#   4 "FATAL"
my $LOGLEVEL = 2;
if ( $ARGV[0] =~ /^\w/ ) {
    $_ = $ARGV[0];

    SWITCH: {
        $LOGLEVEL = 0, last SWITCH if /^DEBUG$/;
        $LOGLEVEL = 1, last SWITCH if /^INFO$/;
        $LOGLEVEL = 2, last SWITCH if /^WARN$/;
        $LOGLEVEL = 3, last SWITCH if /^ERROR$/;
        $LOGLEVEL = 4, last SWITCH if /^FATAL$/;
        &function_log( 3,"Main - Not valid value $ARGV[0] for LOG Level,  setting to WARN" );
    }  
}

&function_log( 1, "Main - Setting LOG Level to $LOGLEVEL $ARGV[0]" );

# parse configuration file and populate hash   %sysoidmask2vlanoid
# that returns VLANS OIDS to find vlan as a funtion of sysoid (vendor)
&parse_xml_linkconf($linkconffile);

# open connection to DB

$dbh = OpenNMS::DbUtil->connect();

##########################################
#
#     first of all create data structure
# sql_update_delete
# - update db table atinterface, iprouteinterface, stpnode, stpinterface,
#   datalinkinterface set status = D for node deleted in table node
# sql_select_nodes
# - query db table ipinterface and populate : %nodeid2snmpprimaryip, 
#   %snmpprimaryip2nodeid, %ipaddr2nodeid, %nodeid_ip2ifindex
# - query db table snmpinterface and populate array of hashes %snmpinterface
#   and hash  %snmpifmac2nodeid return nodeid as function of interface
# - query db table node and populate hash %nodeid2sysoid
# sql_update_tables
# - update table atinterface, stpnode, stpinterface, iprouteinterface,
#   setting status = N for data not polled
# sql_update_datalink
# - update table datalinkinterface
#   setting status = N for data not polled
# sql_select_tables
# - query db table atinterfacepopulate populate relative hash and %mac2ip
# - query db table stpnode and populate relative hash
# - query db table stpinterface and populate relative hash
# - query db table iprouteinterface and populate relative hash
# sql_select_datalink
# - query db table datalinkinterface and populate relative hash
##########################################

&sql_select_tables();


&function_log( 1, "main - entering while loop" );
while (1) {
# sleep initial
  
    &function_log( 1, "main - initial sleeping $initial_sleep_time msec" );

    sleep int( $initial_sleep_time / 1000 );
    &sql_select_nodes();
    my $lastpolltime = localtime(time);


##########################################################
#
# second find on snmp nodes to get what is useful
# - find SNMP access parameters from snmp-config.xml and populate hash %SNMP
# - populate db table iprouteinterface, atinterface,stpnode,stpinterface 
# - test which snmp object is also a bridge so can find active vlan on it
# - populate hash %mac2bp return bp as a function of nodeid and macaddr
#
################################################################################
    &function_log(0, "main - start loop on snmp nodes");
    foreach my $ipaddr ( values %nodeid2snmpprimaryip ) {
        &function_log( 1, "main - try to get snmp data from $ipaddr");
        &parse_xml_snmp_config($ipaddr) unless (exists $SNMP{$ipaddr}{COMMUNITY});
        &get_snmp_data($ipaddr);
        &function_log( 1, "main - sleeping $sleep_time msec" );
        sleep int( $sleep_time / 1000 );
    }

    &function_log( 1, "main - updating hash from db");
    &sql_update_tables($lastpolltime);
    &sql_select_tables();
    &sql_select_datalink();

###############################################################
#
# find links among switches
# also populate and update DB table links
# write links for backbone port on switches to DB table links
# also populate hash %bbbp as a function that returns an array
# containing linked inter switch bridge ports
###############################################################


    &function_log(0,
    "main - searching bbbp and links beetween switches: iterating on row in stpinterface");
    
    foreach my $nodeparentid (keys %stpinterface ){
        foreach my $bridgeport ( keys %{ $stpinterface{$nodeparentid} }){
            foreach my $VLAN ( keys %{ $stpinterface{$nodeparentid}{$bridgeport} }){
                &function_log(0,
                    "main - parsing stpinterface: nodeparentid $nodeparentid bp $bridgeport VLAN $VLAN");
                last if
                ($stpinterface{$nodeparentid}{$bridgeport}{$VLAN}{status} ne 'A');
                my $parentifindex = $stpinterface{$nodeparentid}{$bridgeport}{$VLAN}{ifindex};
                &function_log(0,"main - parsing stpinterface: parentifindex $parentifindex");
                my $bridgemacaddr = $stpnode{$nodeparentid}{$VLAN}{baseBridgeAddress};
                &function_log(0,"main - parsing stpinterface: baseBridgeAddress $bridgemacaddr");
                my $stpdesignatedbridge =
                $stpinterface{$nodeparentid}{$bridgeport}{$VLAN}{stpportdesignatedbridge};
                $stpdesignatedbridge =~ s/^[0-f]{4,4}//;
                &function_log(0,"main - parsing stpinterface: stpportdesignatedbridge $stpdesignatedbridge");
                last if ( $bridgemacaddr eq $stpdesignatedbridge );
                last if ( $bridgemacaddr eq "000000000000" );
                last unless (exists $snmpifmac2nodeid{$stpdesignatedbridge});
                my $nodeid = $snmpifmac2nodeid{$stpdesignatedbridge};
                &function_log(0,"main - parsing stpinterface: nodeid $nodeid");
                last if ( $nodeid == $nodeparentid );
                my $stpportdesignatedport =
                $stpinterface{$nodeparentid}{$bridgeport}{$VLAN}{stpportdesignatedport};
                &function_log(0,"main - parsing stpinterface: stpportdesignatedport $stpportdesignatedport");
                &calc_bridge_scalefactor($nodeparentid) unless
                            (exists $bridgenodeid2scalefactor{$nodeparentid} );
                $stpportdesignatedport =
                hex($stpportdesignatedport) - $bridgenodeid2scalefactor{$nodeparentid};
                &function_log(0,"main - parsing stpinterface: normalized stpportdesignatedport $stpportdesignatedport");
                my $ifindex = $nodeid_bp2ifindex{$nodeid}{$stpportdesignatedport};
                if ( $ifindex eq '') {
                    &function_log(2,"main - parsing stpinterface: ifindex is null");
                    next
                } else {
                    &function_log(0,"main - parsing stpinterface: ifindex $ifindex");
                }
                push @{ $bbbp{ $nodeparentid} },$bridgeport;
                push @{ $bbbp{ $nodeid} },$stpportdesignatedport;
                    
                my @nnid;
                push @nnid, $nodeid;
                push @nnid, $ifindex;
                push @nnid, $nodeparentid;
                push @nnid, $parentifindex;
                push @nnid, 'A';
                &function_log( 1, "main - found bridges link: record @nnid");
                next unless ($#nnid == 4);
                if (exists $datalinkinterface{$nodeid}{$ifindex}{status}) {
                    &sql_update_datalinkinterface(@nnid);
                } else {
                    $datalinkinterface{$nodeid}{$ifindex}{status} = 'A';
                    &sql_insert_datalinkinterface(@nnid);
                }
                 
                undef @nnid;        
                push @nnid, $nodeparentid;
                push @nnid, $parentifindex;
                push @nnid, $nodeid;
                push @nnid, $ifindex;
                push @nnid, 'A';
                &function_log( 1, "main - found bridges link: record @nnid");
                next unless ($#nnid == 4);
                if (exists $datalinkinterface{$nodeparentid}{$parentifindex}{status}) {
                    &sql_update_datalinkinterface(@nnid);
                } else {
                    $datalinkinterface{$nodeparentid}{$parentifindex}{status} = 'A';
                    &sql_insert_datalinkinterface(@nnid);
                }
                last;
            }
        }    
    }

###############################################################
#
# find not ethernet like type links beetween routers
# also populate and update DB table datalinkinterface
#
###############################################################
    foreach my $nodeid (keys %nodeid_routenexthop2ifindex ){
        foreach my $ipnexthop ( keys %{ $nodeid_routenexthop2ifindex{$nodeid} }){
            &function_log(1,"main - parsing iproute: nodeid $nodeid nexthop $ipnexthop");
            next if ($ipnexthop eq '0.0.0.0');
            next if ($ipnexthop eq '127.0.0.1');
            next if ($ipnexthop eq $nodeid2snmpprimaryip{$nodeid});
            next if (! exists $ipaddr2nodeid{$ipnexthop});
            my $ifindex = $nodeid_routenexthop2ifindex{$nodeid}{$ipnexthop};
            &function_log( 1, "main - parsing iproute: ifindex $ifindex");
            next if ($snmpinterface{$nodeid}{$ifindex}{snmpiftype} == 6);
            next if ( $ifindex == 0);
            next unless (exists $ipaddr2nodeid{$ipnexthop});
            my $nodenexthopid = $ipaddr2nodeid{$ipnexthop};
            &function_log( 1, "main - parsing iproute: nodenexthopid $nodenexthopid");
            my $nxhopifindex = 0;
            &function_log( 1, "main - parsing iproute: try to find parentifindex");
            foreach my $ipaddr (@{ $nodeid2ipaddrs{$nodeid} }){
                &function_log( 1, "main - parsing iproute: node $nodeid ip $ipaddr");
                next unless (exists $nodeid_routenexthop2ifindex{$nodenexthopid}{$ipaddr});
                $nxhopifindex = $nodeid_routenexthop2ifindex{$nodenexthopid}{$ipaddr};
                &function_log( 1, "main - parsing iproute: nxhopifindex $nxhopifindex");
                last;
            }
            next if ($snmpinterface{$nodenexthopid}{$nxhopifindex}{snmpiftype} == 6);
            my @nnid;
            push @nnid, $nodenexthopid;
            push @nnid, $nxhopifindex;
            push @nnid, $nodeid;
            push @nnid, $ifindex;
            push @nnid, 'A';
            next unless ($#nnid == 4);
            &function_log( 1, "main - found routers link: record @nnid");
            if (exists $datalinkinterface{$nodenexthopid}{$nxhopifindex}{status}) {
                &sql_update_datalinkinterface(@nnid);
            } else {
                $datalinkinterface{$nodenexthopid}{$nxhopifindex}{status} = 'A';
                &sql_insert_datalinkinterface(@nnid);
            }
        }
    }
#
# find bridge mac address table as function of bridge port number and vlan
# insert data that matches values in %mac2ip
# populate hash %mac2bp as a function of l2_ip and mac address, return bridge port

    foreach my $nodeparentid ( keys %mac2bp ) {
        &function_log( 1,"main - parsing macs: bridge node $nodeparentid" );
        foreach my $macaddr ( keys %{ $mac2bp{$nodeparentid} } ) {
            my $bp = $mac2bp{$nodeparentid}{$macaddr};
            &function_log(1,"main - parsing macs: mac $macaddr bp $bp" );
            my @greplist = grep { /^$bp$/ } @{ $bbbp{$nodeparentid} };
            next unless ( $#greplist == -1 );
            &function_log(0,"main - parsing macs: bp $bp not bb port");
            ## find link between aggregated port
            &function_log(0,"main - parsing macs: mac $macaddr is in snmpifmac")
                if (exists $snmpifmac2nodeid{$macaddr}); 
            #
            next unless (exists $mac2ip{$macaddr});
            my $parentifindex = $nodeid_bp2ifindex{$nodeparentid}{$bp};
            &function_log( 1,"main - parsing macs: parentifindex $parentifindex");
            my $nodeid = $ipaddr2nodeid{$mac2ip{$macaddr}};
            &function_log( 1,"main - parsing macs: nodeid $nodeid mac $macaddr");
            my $ifindex = 0;
            $ifindex = $nodeid_ip2ifindex{$nodeid}{$mac2ip{$macaddr}}
            if ( exists $nodeid_ip2ifindex{$nodeid}{$mac2ip{$macaddr}} );
            &function_log( 1,"main - parsing macs: ifindex $ifindex");
            my @nnid;
            push @nnid, $nodeid;
            push @nnid, $ifindex;
            push @nnid, $nodeparentid;
            push @nnid, $parentifindex;
            push @nnid, 'A';
            next unless ($#nnid == 4);
            if (exists $datalinkinterface{$nodeid}{$ifindex}{status}) {
                &sql_update_datalinkinterface(@nnid);
            } else {
                $datalinkinterface{$nodeid}{$ifindex}{status} = 'A';
                &sql_insert_datalinkinterface(@nnid);
            }
            next if (&sql_isparentnode_datalink($nodeid) == 0);
            &function_log( 1,"main - parsing macs: nodeid $nodeid is parentnode");
            undef @nnid;
            push @nnid, $nodeparentid;
            push @nnid, $parentifindex;
            push @nnid, $nodeid;
            push @nnid, $ifindex;
            push @nnid, 'A';
            next unless ($#nnid == 4);
            if (exists $datalinkinterface{$nodeparentid}{$parentifindex}{status}) {
                &sql_update_datalinkinterface(@nnid);
            } else {
                $datalinkinterface{$nodeparentid}{$parentifindex}{status} = 'A';
                &sql_insert_datalinkinterface(@nnid);
            }
        }
    }

    &sql_update_datalink($lastpolltime);
    undef  %snmpifmac2nodeid;
    undef  %bbbp;
    undef  %mac2ip;

    &function_log( 1, "main - restarting loop" );
}    # close of while infinite loop


sub catch_zap {

        my $signame = shift;
		$dbh->disconnect;
		unlink $lockfile;

        &function_log( 4, "somebody sent me a SIG$signame!");
}

sub sql_select_nodes {

    &function_log( 0, "sql_select_nodes - entering in sub" );

    undef  %nodeid2snmpprimaryip;
    undef  %snmpprimaryip2nodeid;
    undef  %ipaddr2nodeid;
    undef  %nodeid2ipaddrs;
    undef  %nodeid_ip2ifindex;
    undef  %snmpinterface;
    undef  %nodeid2sysoid;

    my $nodetype = "A";

    $sth = $dbh->prepare(
        'SELECT ipaddr, nodeid, ifindex, issnmpprimary FROM ipinterface
        WHERE nodeid in (SELECT nodeid from node WHERE nodetype = ? )
        ORDER BY nodeid, ifindex'
    ) or &function_log(3,
        "sql_select_nodes - Couldn't prepare statement: " . $dbh->errstr );

    $sth->execute($nodetype) or &function_log(3,
        "sql_select_nodes - Couldn't execute statement: " . $sth->errstr );


    while ( my @nnid = $sth->fetchrow_array() ) {
    	&function_log(1,"sql_select_nodes - ipinterface parsing row: @nnid");
    	$nodeid2snmpprimaryip{ $nnid[1] } = $nnid[0] if ( $nnid[3] eq 'P' );
	    $snmpprimaryip2nodeid{ $nnid[0] } = $nnid[1] if ( $nnid[3] eq 'P' );
	    $ipaddr2nodeid{ $nnid[0] } = $nnid[1] if ( $nnid[0] ne '0.0.0.0' );
        push @{ $nodeid2ipaddrs{$nnid[1]} }, $nnid[0] if ( $nnid[0] ne '0.0.0.0' );
	    $nodeid_ip2ifindex{ $nnid[1] }{ $nnid[0] } = $nnid[2]
        if ( $nnid[2] ne '' && $nnid[0] ne '0.0.0.0');
    }

    $sth->finish;

    $sth = $dbh->prepare(
        'SELECT nodeid,ipaddr,snmpipadentnetmask,snmpphysaddr,snmpifindex,
        snmpiftype,snmpifadminstatus,snmpifoperstatus FROM snmpinterface 
        WHERE nodeid in (SELECT nodeid from node WHERE nodetype = ? )'
    ) or &function_log(3,
        "sql_select_nodes - Couldn't prepare statement: " . $dbh->errstr );

    $sth->execute($nodetype) or &function_log(3,
        "sql_select_nodes - Couldn't execute statement: " . $sth->errstr );

    while ( my @nnid = $sth->fetchrow_array() ) {
    	&function_log( 1,"sql_select_nodes - snmpinterface parsing row: @nnid");
        next if ($nnid[3] eq '');
        $snmpinterface{ $nnid[0] }{$nnid[4]}{ipaddr}             = $nnid[1];
    	$snmpinterface{ $nnid[0] }{$nnid[4]}{snmpipadentnetmask} = $nnid[2];
    	$snmpinterface{ $nnid[0] }{$nnid[4]}{snmpphysaddr}       = $nnid[3];
    	$snmpinterface{ $nnid[0] }{$nnid[4]}{snmpiftype}         = $nnid[5];
    	$snmpinterface{ $nnid[0] }{$nnid[4]}{snmpifadminstatus}  = $nnid[6];
    	$snmpinterface{ $nnid[0] }{$nnid[4]}{snmpifoperstatus}   = $nnid[7];
    	$snmpifmac2nodeid{ $nnid[3] } = $nnid[0];
    	&function_log(1,"sql_select_nodes - snmpinterface get row: @nnid");
    }

    $sth->finish;

    $sth = $dbh->prepare(
    'SELECT nodeid, nodesysoid from node WHERE nodetype = ? '
    ) or &function_log(3,
        "sql_select_nodes - Couldn't prepare statement: " . $dbh->errstr );

    $sth->execute($nodetype) or &function_log(3,
        "sql_select_nodes - Couldn't execute statement: " . $sth->errstr );

    while ( my @nnid = $sth->fetchrow_array() ) {
        next if ( $nnid[1] eq "" );
        $nodeid2sysoid{ $nnid[0] } = $nnid[1] ;
        &function_log(1,"sql_select_nodes - node get row: @nnid");
    }
    $sth->finish;
    &function_log( 0, "sql_select_nodes - exiting without errors from sub" );
}

sub sql_isparentnode_datalink {

    &function_log( 0, "sql_isparentnode_datalink - entering in sub" );
    my ($nodeparentid) = @_;
    my $nodetype = "A";
    my $rowcount = 0;
    $sth = $dbh->prepare(
    'SELECT count(*) from datalinkinterface
    WHERE nodeparentid = ? AND status = ?'
    ) or &function_log(3,
    "sql_isparentnode_datalink - Couldn't prepare statement: " . $dbh->errstr);

    $sth->execute($nodeparentid,$nodetype)
    or &function_log(3,
    "sql_isparentnode_datalink - Couldn't execute statement: " . $sth->errstr);

    while ( my @nnid = $sth->fetchrow_array() ) {
        $rowcount = $nnid[0];
        last;
    }
   	&function_log(1,
    "sql_isparentnode_datalink - get $rowcount for nodeparentid $nodeparentid");

    &function_log(0,"sql_isparentnode_datalink - exiting without errors from sub");
    return $rowcount;

}

sub sql_select_datalink {    

    &function_log( 0, "sql_select_datalink - entering in sub" );

    undef  %datalinkinterface;

    $sth = $dbh->prepare(
        'SELECT nodeid,ifindex,nodeparentid,parentIfIndex,status
        FROM datalinkinterface'
    ) or &function_log(3,
        "sql_select_datalink - Couldn't prepare statement: " . $dbh->errstr );

    $sth->execute() or &function_log(3,
        "sql_select_datalink - Couldn't execute statement: " . $sth->errstr );

    while ( my @nnid = $sth->fetchrow_array() ) {
        $datalinkinterface{ $nnid[0] }{ $nnid[1] }{nodeparentid}= $nnid[2] ;
        $datalinkinterface{ $nnid[0] }{ $nnid[1] }{parentifindex}= $nnid[3] ;
        $datalinkinterface{ $nnid[0] }{ $nnid[1] }{status}= $nnid[4] ;
    	&function_log( 1,"sql_select_datalink - datalinkinterface get row: @nnid");
    }

    $sth->finish;

    &function_log(0,"sql_select_datalink - exiting without errors from sub" );
}

sub sql_update_datalink {

    &function_log( 0, "sql_update_datalink - entering in sub" );
    my ($lastpolltime) = @_;
    my $nodetype = "D";
    my $status = "N";

    $sth = $dbh->prepare(
        'UPDATE datalinkinterface set status = ? WHERE nodeid IN 
        (SELECT nodeid from node WHERE nodetype = ? )'
    ) or &function_log(3,
        "sql_update_delete - Couldn't prepare statement: " . $dbh->errstr );
    $sth->execute($nodetype,$nodetype) or &function_log(3,
        "sql_update_delete - Couldn't execute statement: " . $sth->errstr );

    $nodetype = "A";    

    $sth = $dbh->prepare(
    'UPDATE datalinkinterface set status = ?
    WHERE lastpolltime < ? AND status = ?'
    ) or &function_log(3,
    "sql_update_datalink - Couldn't prepare statement: " . $dbh->errstr );

    $sth->execute($status,$lastpolltime,$nodetype)
    or &function_log(3,
    "sql_update_datalink - Couldn't execute statement: " . $sth->errstr );

    &function_log(0,"sql_update_datalink - exiting without errors from sub" );
}

sub sql_select_tables {

    &function_log( 0, "sql_select_tables - entering in sub" );
# now populate hashes relative to tables, atinterface, stpnode, stpinterface,
# iprouteinterface, datalinkinterface

    undef  %atinterface;
    undef  %iprouteinterface;
    undef  %nodeid_routenexthop2ifindex;
    undef  %stpnode;
    undef  %stpinterface;
    undef  %nodeid_bp2ifindex;
    undef  %mac2ip;

    $sth = $dbh->prepare(
        'SELECT nodeid,ipaddr,atPhysAddr,status,sourcenodeid,ifindex
        FROM atinterface'
    ) or &function_log(3,
    	"sql_select_tables - Couldn't prepare statement: " . $dbh->errstr );

    $sth->execute() or &function_log(3,
        "sql_select_tables - Couldn't execute statement: " . $sth->errstr );

    while ( my @nnid = $sth->fetchrow_array() ) {
        $atinterface{ $nnid[0] }{ $nnid[1] }{atPhysAddr}= $nnid[2] ;
        $atinterface{ $nnid[0] }{ $nnid[1] }{status}= $nnid[3] ;
     	$atinterface{ $nnid[0] }{ $nnid[1] }{sourcenodeid}= $nnid[4] ;
        $atinterface{ $nnid[0] }{ $nnid[1] }{ifindex}= $nnid[5] ;
        $mac2ip{ $nnid[2] } = $nnid[1] if ($nnid[3] eq "A");
    	&function_log(1,"sql_select_tables - atinterface get row: @nnid");
    }

    $sth->finish;

    $sth = $dbh->prepare(
        'SELECT nodeid,baseBridgeAddress,baseNumPorts,basetype,
            stpProtocolSpecification,stpPriority,stpdesignatedroot,
            stprootcost,stprootport,status,basevlan
        FROM stpnode'
    ) or &function_log(3,
        "sql_select_tables - Couldn't prepare statement: " . $dbh->errstr );

    $sth->execute() or &function_log(3,
        "sql_select_tables - Couldn't execute statement: " . $sth->errstr );

    while ( my @nnid = $sth->fetchrow_array() ) {
    	$stpnode{ $nnid[0] }{ $nnid[10] }{baseBridgeAddress}= $nnid[1] ;
    	$stpnode{ $nnid[0] }{ $nnid[10] }{baseNumPorts}= $nnid[2] ;
    	$stpnode{ $nnid[0] }{ $nnid[10] }{basetype}= $nnid[3] ;
    	$stpnode{ $nnid[0] }{ $nnid[10] }{baseNumPorts}= $nnid[4] ;
    	$stpnode{ $nnid[0] }{ $nnid[10] }{stpProtocolSpecification}= $nnid[5] ;
    	$stpnode{ $nnid[0] }{ $nnid[10] }{stpPriority}= $nnid[6] ;
    	$stpnode{ $nnid[0] }{ $nnid[10] }{stpdesignatedroot}= $nnid[7] ;
    	$stpnode{ $nnid[0] }{ $nnid[10] }{status}= $nnid[8] ;
    	$stpnode{ $nnid[0] }{ $nnid[10] }{baseNumPorts}= $nnid[9] ;
    	$snmpifmac2nodeid{ $nnid[1] } = $nnid[0] if ($nnid[8] eq 'A');
    	&function_log( 1,"sql_select_tables - stpnode get row: @nnid");
    }

    $sth->finish;

    $sth = $dbh->prepare(
        'SELECT nodeid,bridgeport,ifindex,stpportstate,stpportpathcost,
            stpportdesignatedroot,stpportdesignatedcost,stpportdesignatedbridge,
            stpportdesignatedport,status,stpvlan
        FROM stpinterface'
    ) or &function_log(3,
        "sql_select_tables - Couldn't prepare statement: " . $dbh->errstr );

    $sth->execute() or &function_log(3,
        "sql_select_tables - Couldn't execute statement: " . $sth->errstr );

    while ( my @nnid = $sth->fetchrow_array() ) {
    	$stpinterface{ $nnid[0] }{ $nnid[1] }{ $nnid[10] }{ifindex} = $nnid[2] ;
    	$stpinterface{ $nnid[0] }{ $nnid[1] }{ $nnid[10] }{stpportstate}
            = $nnid[3] ;
        $stpinterface{ $nnid[0] }{ $nnid[1] }{ $nnid[10] }{stpportpathcost}
            = $nnid[4] ;
    	$stpinterface{ $nnid[0] }{ $nnid[1] }{ $nnid[10] }{stpportdesignatedroot}
            = $nnid[5] ;
    	$stpinterface{ $nnid[0] }{ $nnid[1] }{ $nnid[10] }{stpportdesignatedcost}
            = $nnid[6] ;
    	$stpinterface{ $nnid[0] }{ $nnid[1] }{ $nnid[10] }{stpportdesignatedbridge}
            = $nnid[7] ;
    	$stpinterface{ $nnid[0] }{ $nnid[1] }{ $nnid[10] }{stpportdesignatedport}
            = $nnid[8] ;
    	$stpinterface{ $nnid[0] }{ $nnid[1] }{ $nnid[10] }{status} = $nnid[9] ;
        $nodeid_bp2ifindex{ $nnid[0] }{ $nnid[1] }
            = $nnid[2] if ($nnid[9] eq "A");
    	&function_log( 1,"sql_select_tables - stpinterface get row: @nnid");
    }

    $sth->finish;

    $sth = $dbh->prepare('
        SELECT nodeid,routeDest,routeMask,routeNextHop,routeifindex,routemetric1,
            routemetric2,routemetric3,routemetric4,routemetric5,routetype,
    		routeproto,status
        FROM iprouteinterface'
    ) or &function_log(3,
        "sql_select_tables - Couldn't prepare statement: " . $dbh->errstr );

    $sth->execute() or &function_log(3,
        "sql_select_tables - Couldn't execute statement: " . $sth->errstr );

    while ( my @nnid = $sth->fetchrow_array() ) {
        $iprouteinterface{ $nnid[0] }{ $nnid[1] }{routeMask}= $nnid[2] ;
        $iprouteinterface{ $nnid[0] }{ $nnid[1] }{routeNextHop}= $nnid[3] ;
        $iprouteinterface{ $nnid[0] }{ $nnid[1] }{routeifindex}= $nnid[4] ;
        $iprouteinterface{ $nnid[0] }{ $nnid[1] }{routemetric1}= $nnid[5] ;
        $iprouteinterface{ $nnid[0] }{ $nnid[1] }{routemetric2}= $nnid[6] ;
        $iprouteinterface{ $nnid[0] }{ $nnid[1] }{routemetric3}= $nnid[7] ;
        $iprouteinterface{ $nnid[0] }{ $nnid[1] }{routemetric4}= $nnid[8] ;
        $iprouteinterface{ $nnid[0] }{ $nnid[1] }{routemetric5}= $nnid[9] ;
        $iprouteinterface{ $nnid[0] }{ $nnid[1] }{routetype}= $nnid[10] ;
        $iprouteinterface{ $nnid[0] }{ $nnid[1] }{routeproto}= $nnid[11] ;
        $iprouteinterface{ $nnid[0] }{ $nnid[1] }{status}= $nnid[12] ;
        $nodeid_routenexthop2ifindex{ $nnid[0] }{ $nnid[3] } = $nnid[4]
            if ( $nnid[12] eq "A" );
    	&function_log( 1,"sql_select_tables - iprouteinterface get row: @nnid");
    }

    $sth->finish;
    &function_log( 0, "sql_select_tables - exiting without errors from sub" );

}

sub sql_update_tables {

# set to D node deleted on tables: atinterface, stpnode, stpinterface,
#                                  iprouteinterface
    
    &function_log(0,"sql_update_tables - entering in sub" );
    my $nodetype = "D";

    $sth = $dbh->prepare(
        'UPDATE atinterface set status = ? WHERE nodeid IN 
        (SELECT nodeid from node WHERE nodetype = ? )'
    ) or &function_log(3,
        "sql_update_tables - Couldn't prepare statement: " . $dbh->errstr );
    $sth->execute($nodetype,$nodetype) or &function_log(3,
        "sql_update_from_tables - Couldn't execute statement: " . $sth->errstr );

    $sth = $dbh->prepare(
        'UPDATE stpnode set status = ? WHERE nodeid IN 
        (SELECT nodeid from node WHERE nodetype = ? )'
    ) or &function_log(3,
        "sql_update_tables - Couldn't prepare statement: " . $dbh->errstr );
    $sth->execute($nodetype,$nodetype) or &function_log(3,
        "sql_update_tables - Couldn't execute statement: " . $sth->errstr );

    $sth = $dbh->prepare(
        'UPDATE stpinterface set status = ? WHERE nodeid IN 
        (SELECT nodeid from node WHERE nodetype = ? )'
    ) or &function_log(3,
        "sql_update_tables - Couldn't prepare statement: " . $dbh->errstr );
    $sth->execute($nodetype,$nodetype) or &function_log(3,
        "sql_update_tables - Couldn't execute statement: " . $sth->errstr );

    $sth = $dbh->prepare(
        'UPDATE iprouteinterface set status = ? WHERE nodeid IN 
        (SELECT nodeid from node WHERE nodetype = ? )'
    ) or &function_log(3,
        "sql_update_tables - Couldn't prepare statement: " . $dbh->errstr );
    $sth->execute($nodetype,$nodetype) or &function_log(3,
        "sql_update_tables - Couldn't execute statement: " . $sth->errstr );

# start update to N row not polled on table atinterface, stpnode, stpinterface
# iprouteinterface

    my ($lastpolltime) = @_;
    $nodetype = "A";
    my $status = "N";

    $sth = $dbh->prepare(
    'UPDATE atinterface set status = ?
    WHERE lastpolltime < ? AND status = ?'
    ) or &function_log(3,
    "sql_update_tables - Couldn't prepare statement: " . $dbh->errstr );

    $sth->execute($status,$lastpolltime,$nodetype)
    or &function_log(3,
    "sql_update_tables - Couldn't execute statement: " . $sth->errstr );

    $sth = $dbh->prepare(
    'UPDATE stpnode set status = ?
    WHERE lastpolltime < ? AND status = ?'
    ) or &function_log(3,
    "sql_update_tables - Couldn't prepare statement: " . $dbh->errstr );

    $sth->execute($status,$lastpolltime,$nodetype)
    or &function_log(3,
    "sql_update_tables - Couldn't execute statement: " . $sth->errstr );

    $sth = $dbh->prepare(
    'UPDATE stpinterface set status = ?
    WHERE lastpolltime < ? AND status = ?'
    ) or &function_log(3,
    "sql_update_tables - Couldn't prepare statement: " . $dbh->errstr );

    $sth->execute($status,$lastpolltime,$nodetype)
    or &function_log(3,
    "sql_update_tables - Couldn't execute statement: " . $sth->errstr );

    $sth = $dbh->prepare(
    'UPDATE iprouteinterface set status = ?
    WHERE lastpolltime < ? AND status = ?'
    ) or &function_log(3,
    "sql_update_tables - Couldn't prepare statement: " . $dbh->errstr );

    $sth->execute($status,$lastpolltime,$nodetype)
    or &function_log(3,
    "sql_update_tables - Couldn't execute statement: " . $sth->errstr );

    &function_log(0,"sql_update_tables - exiting without errors from sub" );

}

sub calc_bridge_scalefactor {

# found scale factor to calculate bridge port on bridge

    my ($nodeid) = @_ ;
    &function_log(0, "calc_bridge_scalefactor - entering in sub");
    &function_log(0, "calc_bridge_scalefactor - getting info for node $nodeid");

    foreach my $bridgeport ( keys %{ $stpinterface{$nodeid} }){
        foreach my $VLAN ( keys %{ $stpinterface{$nodeid}{$bridgeport} }){
            &function_log(0,
            "calc_bridge_scalefactor - row nodeid $nodeid bp $bridgeport VLAN $VLAN");
            next if ($stpinterface{$nodeid}{$bridgeport}{$VLAN}{status} ne 'A');
            my $stpdesignatedbridge =
            $stpinterface{$nodeid}{$bridgeport}{$VLAN}{stpportdesignatedbridge};
            $stpdesignatedbridge =~ s/^[0-f]{4,4}//;
            &function_log(0,
            "calc_bridge_scalefactor - stpdesignatedbridge $stpdesignatedbridge");
            my $nodeparent = $snmpifmac2nodeid{$stpdesignatedbridge};
            $nodeparent = $ipaddr2nodeid{$mac2ip{$stpdesignatedbridge}}
                if ($nodeparent eq '');
            &function_log(0,"calc_bridge_scalefactor - nodeparent $nodeparent");
            my $baseBridgeAddress = $stpnode{$nodeid}{$VLAN}{baseBridgeAddress};
            &function_log(0,"calc_bridge_scalefactor - baseBridgeAddress $baseBridgeAddress");
            if ( $baseBridgeAddress eq $stpdesignatedbridge || $nodeparent eq $nodeid){
                my $stpportdesignatedport =
                $stpinterface{$nodeid}{$bridgeport}{$VLAN}{stpportdesignatedport};
                $stpportdesignatedport = hex($stpportdesignatedport);
                $bridgenodeid2scalefactor{$nodeid} =
                $stpportdesignatedport - $bridgeport;
              	&function_log(0,"calc_bridge_scalefactor - scalefactor for nodeid $nodeid: $bridgenodeid2scalefactor{$nodeid} ");
                &function_log(0,"calc_bridge_scalefactor - exiting without errors from sub" );
                return;
            }
        }
    }
    &function_log( 2,"calc_bridge_scalefactor - no scale factor found for $nodeid" );
    &function_log( 0,"calc_bridge_scalefactor - exiting without errors from sub" );

}

sub get_snmp_data {

    my $oid;
    my @args;

    my $result;
    my $errsess;

    my %tablelines;
    my @nnid;

    my ($IP) = @_;
    my $NODEID = $snmpprimaryip2nodeid{$IP};

    &function_log( 0, "get_snmp_data - entering in sub" );

    $SNMP{$IP}{TIMEOUT} = 1 if ( $SNMP{$IP}{TIMEOUT} < 1 );

    my ( $s, $e ) = Net::SNMP->session(
		-hostname  => $IP,
		-community => $SNMP{$IP}{COMMUNITY},
		-port      => $SNMP{$IP}{PORT},
		-retries   => $SNMP{$IP}{RETRY},
		-timeout   => $SNMP{$IP}{TIMEOUT},
		-version   => $SNMP{$IP}{VERSION}
    );

    if ( !defined($s) ) {
        &function_log( 3,"get_snmp_data - SNMP session failed for $IP: $e" );
    }

# populate atinterface db table

    my $stringacm = '.1.3.6.1.2.1.4.22.1.2.';    # ipNetToMediaPhysAddr OID

    &function_log( 1,
	"get_snmp_data - getting ipNetToMediaIfindex for node $IP" );

    @args = ( -varbindlist => [$stringacm] );

    while ( defined( $result = $s->get_next_request(@args) ) ) {
        $oid = ( $s->var_bind_names )[0];
        if ( !oid_base_match( $stringacm, $oid ) ) { last; }
        @args = ( -varbindlist => [$oid] );
        my $atphysaddr = $result->{$oid};
        &function_log( 0,"get_snmp_data - $IP found mac address $atphysaddr for oid $oid ");
        ( my $a, $atphysaddr) = split( "x", $atphysaddr );
        my $ipaddr = $oid;
        $ipaddr =~ s/$stringacm\d+\.//;
        my $ifindex = $oid;
        $ifindex =~ s/$stringacm//;
        $ifindex =~ s/\.$ipaddr//;
        &function_log( 1,"get_snmp_data - $IP found mac address $atphysaddr for ip $ipaddr ");
        next unless ( exists $ipaddr2nodeid{$ipaddr});
        next unless ( $atphysaddr =~ /^[0-f]{12,12}$/ );
        # devo verificare se on è un proxy arp
        my $T_IP = inet_aton($ipaddr);
        foreach my $localipaddress (@{ $nodeid2ipaddrs{$NODEID} }){
            my $A_IP = inet_aton($localipaddress);
            my $localifindex = $nodeid_ip2ifindex{$NODEID}{$localipaddress};
            my $NETMASK = $snmpinterface{$NODEID}{$localifindex}{snmpipadentnetmask};
            $NETMASK = inet_aton($NETMASK);
            $T_IP = $T_IP & $NETMASK;
            $A_IP = $A_IP & $NETMASK;
            next unless ( $T_IP eq $A_IP );
            push @nnid, $ipaddr2nodeid{$ipaddr};
            push @nnid, $ipaddr;
            push @nnid, $atphysaddr;
            push @nnid, "A";
            push @nnid, $NODEID;
            push @nnid, $ifindex;
            last;
        }
# per ora è sufficiente upgradare se la riga esiste altrimenti insert
# per verificare se è cambiata è sufficiente vedere cosa è cambiato nella hast table
        next unless ($#nnid == 5);
        if (exists $atinterface{$ipaddr2nodeid{$ipaddr}}{$ipaddr}{status}) {
            &sql_update_atinterface(@nnid);
        } else {
            $atinterface{$ipaddr2nodeid{$ipaddr}}{$ipaddr}{status} = 'A';
            &sql_insert_atinterface(@nnid);
        }
        undef @nnid;
    }

    if ( $s->error() ne '' ) {
        $errsess = $s->error;
        &function_log( 2, "get_snmp_data - $errsess" );
    }

# populate iprouteinterface db table

    $stringacm = '.1.3.6.1.2.1.4.21.1.';    # ipRouteEntry OID

    &function_log( 1,"get_snmp_data - getting ipRouteEntry for node $IP" );

    @args = ( -varbindlist => [$stringacm] );

    undef %tablelines;
    my %oldoidsid;
    while ( defined( $result = $s->get_next_request(@args) ) ) {
        $oid = ( $s->var_bind_names )[0];
        if ( !oid_base_match( $stringacm, $oid ) ) { last; }
        @args = ( -varbindlist => [$oid] );
        my $iproutedestResult = $result->{$oid};
        my $iproutedest=$oid;
        $iproutedest=~ s/$stringacm\d+\.//;
        my $oidsid = $oid;
        $oidsid =~ s/$stringacm//;
        $oidsid =~ s/\.$iproutedest//;
        $oldoidsid{$iproutedest} = 1 unless (defined $oldoidsid{$iproutedest});
        &function_log( 1,
        "get_snmp_data - $IP ipRouteEntry: dest $iproutedest Entry $oid value $iproutedestResult");

# aggiungo il nodeid all'array se è vuoto
        push @{ $tablelines{$iproutedest} }, $NODEID
        if ( $#{ $tablelines{$iproutedest} } == -1 ); 
# aggiungo all'array nell'ordine
# routedest routeifindex metric1 metric2 metric3 metric4
# nexthop routeType routeProto routeMask metric5
# some mib2 put metric2 metric3 metric4 and metric5 to null we set to -1

        while ($oidsid > $oldoidsid{$iproutedest}) {
    	    &function_log( 0,
	       	 "get_snmp_data - $IP ipRouteEntry: dest $iproutedest Entry $oidsid int value $oldoidsid{$iproutedest}");
	        $oldoidsid{$iproutedest}++;
	        next if ($oldoidsid{$iproutedest} == 11);
    	    next if ($oldoidsid{$iproutedest} == 14);
	        push @{ $tablelines{$iproutedest} }, '-1';
        }
        next if ($oidsid == 10);
        next if ($oidsid == 13);
        push @{ $tablelines{$iproutedest} }, $iproutedestResult;
		$oldoidsid{$iproutedest} = $oidsid;
		$oldoidsid{$iproutedest}++;
    }

    if ( $s->error() ne '' ) {
        $errsess = $s->error;
        &function_log( 2, "get_snmp_data - $errsess" );
    }
## inserisco i dati in iprouteinterface

    undef @nnid;

    foreach my $iproutedest (keys %tablelines){
        @nnid = @{ $tablelines{$iproutedest} };
        push @nnid, "A";
        &function_log( 0,
	       	 "get_snmp_data - $IP ipRouteEntry: dest $iproutedest try insert row @nnid");
        &function_log( 0,
	       	 "get_snmp_data - $IP ipRouteEntry: dest $iproutedest try insert row with $#nnid columns (should be 12)");
        next unless ( $#nnid == 12 );
        if (exists $iprouteinterface{$ipaddr2nodeid{$IP}}{$iproutedest}{status}) {
            &sql_update_iprouteinterface(@nnid);
        } else {
            $iprouteinterface{$ipaddr2nodeid{$IP}}{$iproutedest}{status} = 'A';
            &sql_insert_iprouteinterface(@nnid);
        }
    }

# continue if is a bridge and support VLAN

    my $sysoid = $nodeid2sysoid{ $NODEID };
    my @vlans;
    &function_log(1,"get_snmp_data - find active vlans on switch $IP");
 
    foreach my $sysoidMask (keys %sysoidmask2vlanoid){
        &function_log(1,"get_snmp_data - matching $sysoidMask and $sysoid");
        if ( $sysoid =~ /^$sysoidMask/ ) {
            $stringacm = $sysoidmask2vlanoid{$sysoidMask} ;
            &function_log(1,"get_snmp_data - matched $sysoidMask and $sysoid: using $stringacm");
            @args = ( -varbindlist => [$stringacm] );
            while ( defined( $result = $s->get_next_request(@args) ) ) {
                $oid = ( $s->var_bind_names )[0];
                if ( !oid_base_match( $stringacm, $oid ) ) { last; }
                @args = ( -varbindlist => [$oid] );
                my $newstring = $stringacm . '.1.';
                $oid =~ s/$newstring//;
                push @vlans, $oid unless ( $oid  =~ /^(1002|1003|1004|1005)$/ );
                &function_log( 1,"get_snmp_data - vlan $oid found on switch $IP");
            } 
            if ( $s->error() ne '' ) {
                $errsess = $s->error;
                &function_log( 3, "get_snmp_data - $errsess" );
            }
            &function_log( 1,"get_snmp_data - vlan @vlans found on switch $IP");
            last;
        }
    }
    $s->close;
    undef $s;
    undef $e;

    push @vlans, 1 if ($#vlans == -1);


# now get data from bridge mib
# first of all test if node is a bridge
# test if was a bridge success else return
# .iso.org.dod.internet.mgmt.mib-2.dot1dBridge.dot1dBase.dot1dBaseBridgeAddress

# loop sulle vlan per supporto MVLAN STP
    my $community = $SNMP{$IP}{COMMUNITY};
    
VLAN:    foreach my $VLAN ( @vlans ) {
        $community = $SNMP{$IP}{COMMUNITY} . '@' . $VLAN unless ($#vlans == 0);
        $SNMP{$IP}{TIMEOUT} = 1 if ( $SNMP{$IP}{TIMEOUT} < 1 );
      
        my ( $s, $e) = Net::SNMP->session(
    		-hostname  => $IP,
    		-community => $community,
    		-port      => $SNMP{$IP}{PORT},
    		-retries   => $SNMP{$IP}{RETRY},
    		-timeout   => $SNMP{$IP}{TIMEOUT},
    		-version   => $SNMP{$IP}{VERSION}
        );
        
        if ( !defined($s) ) {
           &function_log( 3,
           "get_snmp_data - SNMP session failed with $IP and community $community: $e"
			);
            next;
        }
        
        &function_log( 1,
        "get_snmp_data - SNMP session success with $IP VLAN $VLAN community $community"
        );

	    undef @nnid;
# test if support dot1d-bridge
    	$stringacm = '1.3.6.1.2.1.17.1.1.'; 

	    &function_log( 1,"get_snmp_data - getting dot1dBaseBridgeAddress for node $IP community $community" );

	    @args = ( -varbindlist => [$stringacm] );

    	while ( defined( $result = $s->get_next_request(@args) ) ) {
        	$oid = ( $s->var_bind_names )[0];
        	if ( !oid_base_match( $stringacm, $oid ) ) { last; }
        	@args = ( -varbindlist => [$oid] );
        	my $atphysaddr = $result->{$oid};
        	( my $a, $atphysaddr) = split( "x", $atphysaddr );
        	&function_log( 1,"get_snmp_data - $IP dot1dBaseBridgeAddress: found $atphysaddr");
        	push @nnid, $atphysaddr if ($atphysaddr =~ /^[0-f]{12,12}$/ );
        	last;
    	}

	    if ( $s->error() ne '' ) {
    	    $errsess = $s->error;
        	&function_log( 2, "get_snmp_data - $errsess" );
    	}

	    if ($#nnid == -1 ) {
    	    &function_log( 2,"get_snmp_data - $IP VLAN $VLAN is not a bridge - skipping bridge snmp stuff");
        	$s->close;
        	next VLAN;
    	}

        
        undef @nnid;
        undef %tablelines;

        push @nnid, $NODEID;
        $stringacm = '1.3.6.1.2.1.17.1.';   # .iso.org.dod.internet.mgmt.mib-2.dot1dBridge.dot1dBase
        @args = ( -varbindlist => [$stringacm] );
        while ( defined( $result = $s->get_next_request(@args) ) ) {
            $oid = ( $s->var_bind_names )[0];
            if ( !oid_base_match( $stringacm, $oid ) ) { last; }
            @args = ( -varbindlist => [$oid] );
            my $bridgeres = $result->{$oid};
            $bridgeres =~ s/^0x//; # se è esadecimale allora tolgo 0x
            my $stringa=$oid;
            $stringa =~ s/$stringacm//;
            &function_log( 1,
            "get_snmp_data - $IP dot1dBase: oidEntry $stringa value $bridgeres");

            if ( $stringa =~ /^(1|2|3)\.0/ )
            {
                push @nnid, $bridgeres;
                &function_log( 1,
                "get_snmp_data - $IP dot1dBase: oidEntry $stringa value $bridgeres added to nnid");
            } elsif  ( $stringa =~ /^4\.1\.2\./ )
            {
                my $bridgeport = $stringa;
                $bridgeport =~ s/^4\.1\.2\.//;
                push @{ $tablelines{$bridgeport} }, $NODEID; 
                push @{ $tablelines{$bridgeport} }, $bridgeport;
                push @{ $tablelines{$bridgeport} }, $bridgeres;
                &function_log( 1,
                "get_snmp_data - $IP dot1dBase: bridgeport $bridgeport ifindex $bridgeres added to tableline");
            }
        }
        if ( $s->error() ne '' ) {
            $errsess = $s->error;
            &function_log( 2, "get_snmp_data - $errsess" );
            next;
        }
# now get data from bridge spt mib
        $stringacm = '1.3.6.1.2.1.17.2.';

        @args = ( -varbindlist => [$stringacm] );

        while ( defined( $result = $s->get_next_request(@args) ) ) {
            $oid = ( $s->var_bind_names )[0];
            if ( !oid_base_match( $stringacm, $oid ) ) { last; }
            @args = ( -varbindlist => [$oid] );
            my $stringa=$oid;
            my $stpres = $result->{$oid};
# se è esadecimale alora tolgo 0x
            $stpres =~ s/^0x//;
            $stringa =~ s/$stringacm//;
            &function_log( 1,
            "get_snmp_data - $IP dot1dStp: oidEntry $stringa value $stpres");
            if ( $stringa =~ /^(1|2|5|6|7)\.0/ ) {
                push @nnid, $stpres;
                &function_log( 1,
                "get_snmp_data - $IP dot1dStp: oidEntry $stringa value $stpres added to nnid");
            }
            if ( $stringa =~ /^15\.1\.(3|5|6|7|8|9)\./ ) {
                my $bridgeport = $stringa;
                $bridgeport =~ s/^15\.1\.(3|5|6|7|8|9)\.//;
                push @{ $tablelines{$bridgeport} }, $stpres;
                &function_log( 1,
                "get_snmp_data - $IP dot1dstp: bridgeport $bridgeport value $stpres added to tablelines");
            }
        }

        if ( $s->error() ne '' ) {
            $errsess = $s->error;
            &function_log( 2, "get_snmp_data - $errsess" );
            next;
        }

        push @nnid, "A";
        push @nnid, $VLAN;
        next unless ($#nnid == 10);
        if (exists $stpnode{$NODEID}{$VLAN}{status}) {
            &sql_update_stpnode(@nnid);
        } else {
            $stpnode{$NODEID}{$VLAN}{status} = 'A';
            &sql_insert_stpnode(@nnid);
        }

        undef @nnid;
        foreach my $bridgeport (keys %tablelines){
            @nnid = @{ $tablelines{$bridgeport} };
            push @nnid, "A";
            push @nnid, $VLAN;
            next unless ( $#nnid == 10 );
            if (exists $stpinterface{$ipaddr2nodeid{$IP}}{$bridgeport}{$VLAN}{status}) {
                &sql_update_stpinterface(@nnid);
            } else {
                $stpinterface{$ipaddr2nodeid{$IP}}{$bridgeport}{$VLAN}{status} = 'A';
                &sql_insert_stpinterface(@nnid);
            }
        }

# now get data from transparent bridge mib regarding macaddress to bridge port
# dot1dBridge.dot1dTp.dot1dTpFdbTable.dot1dTpFdbEntry.dot1dTpFdbPort

        $stringacm = '1.3.6.1.2.1.17.4.3.1.2';
        @args = ( -varbindlist => [$stringacm] );
        while ( defined( $result = $s->get_next_request(@args) ) ) {
            $oid = ( $s->var_bind_names )[0];
            if ( !oid_base_match( $stringacm, $oid ) ) { last; }
            my $bp = $result->{$oid};
            @args = ( -varbindlist => [$oid] );
            $oid =~ s/$stringacm\.//;
            my $macaddr = '';
            foreach my $a ( split( /\./, $oid ) ) {
                my $b = sprintf "%02x", $a;
                $macaddr .= $b;
            }

            if ( $macaddr =~ /^[0-f]{12,12}$/ ) {
                &function_log(1,
                "get_snmp_data - node $NODEID ip $IP: mac $macaddr found on bridge port $bp"
				);
                $mac2bp{$NODEID}{$macaddr} = $bp;
            }
        }
        $s->close;
        &function_log( 0, "get_snmp_data - exiting data coll on vlan $VLAN" );
    }
    &function_log( 0, "get_snmp_data - exiting without errors from sub" );
}

sub sql_insert_atinterface {

	&function_log( 0, "sql_insert_atinterface - entering in sub" );
	my ( $nodeid, $ipaddr, $atphysaddr, $status, $sourceNodeid, $ifindex ) = @_;
    &function_log( 0, "sql_insert_atinterface - try insert new row @_" );

	if (!($sth = $dbh->prepare(
            'INSERT INTO atinterface (
                nodeid,
                ipaddr,
                atphysaddr,
                status,
                sourceNodeid,
                ifindex,
                lastpolltime) 
            VALUES (?,?,?,?,?,?,(\'now\'))'
	)))
    {
		&function_log( 3,
			"sql_insert_atinterface - Couldn't prepare statement: $dbh->errstr" );
		return 0;
	}
	if (!( $sth->execute(
        $nodeid,$ipaddr,$atphysaddr,$status,$sourceNodeid,$ifindex
    )))
    {
		&function_log( 3,
			"sql_insert_atinterface - Couldn't execute statement: $sth->errstr" );
		return 0;
	}
	$sth->finish;
	&function_log( 0,
		"sql_insert_atinterface - inserted new row @_ in atinterface" );
	&function_log( 0, "sql_insert_atinterface - exiting without errors from sub" );

}

sub sql_update_atinterface {

	&function_log( 0, "sql_update_atinterface - entering in sub" );
	my ($nodeid,$ipaddr,$atphysaddr,$status,$sourceNodeid,$ifindex ) = @_;
    &function_log( 0, "sql_update_atinterface - try update new row @_" );

	if (!($sth = $dbh->prepare(
		'UPDATE atinterface  set 
			atphysaddr=?, 
			status=?, 
			sourcenodeid=?, 
			ifindex =?,
			lastpolltime = (\'now\') 
		WHERE nodeid = ? AND ipaddr = ? '
	)))
    {
		&function_log( 3,"sql_update_atinterface  - Couldn't prepare statement: $dbh->errstr" );
		return 0;
	}
	if (!($sth->execute(
        $atphysaddr,$status,$sourceNodeid,$ifindex,$nodeid,$ipaddr
    )))
    {
		&function_log( 3,
			"sql_update_atinterface - Couldn't execute statement: $sth->errstr");
		return 0;
	}
	$sth->finish;
	&function_log( 0,"sql_update_atinterface - updated row @_ on atinterface");
	&function_log( 0,"sql_update_atinterface - exiting without errors from sub");
}

sub sql_insert_stpnode {

	&function_log( 0, "sql_insert_stpnode - entering in sub" );
	my ( $nodeid, $baseBridgeAddress, $baseNumPorts, $baseType, 
        $stpProtocolSpecification, $stpPriority,$stpDesignatedRoot,
        $stpRootCost, $stpRootPort, $status, $baseVlan) = @_ ;
    &function_log( 0, "sql_insert_stpnode - try insert new row @_" );
    
	if (!( $sth = $dbh->prepare(
        'INSERT INTO stpnode ( nodeid,
            baseBridgeAddress,
    		baseNumPorts,
    		basetype,
    		stpProtocolSpecification,
            stpPriority,
    		stpdesignatedroot,
    		stprootcost,
    		stprootport,
    		status,
    		lastPollTime,
    		basevlan) 
        VALUES (?,?,?,?,?,?,?,?,?,?,(\'now\'),?)'
	)))
	{
		&function_log( 3,
			"sql_insert_stpnode - Couldn't prepare statement: $dbh->errstr" );
		return 0;
	}
	if (!($sth->execute(
        $nodeid, $baseBridgeAddress, $baseNumPorts, $baseType, 
        $stpProtocolSpecification, $stpPriority,$stpDesignatedRoot,
        $stpRootCost, $stpRootPort, $status, $baseVlan
    )))
    {
		&function_log( 3,
			"sql_insert_stpnode - Couldn't execute statement: $sth->errstr" );
		return 0;
	}
	$sth->finish;
	&function_log( 0,"sql_insert_stpnode - inserted new row @_ in stpnode" );
	&function_log( 0, "sql_insert_stpnode - exiting without errors from sub" );

}

sub sql_update_stpnode {

	&function_log( 0, "sql_update_stpnode - entering in sub" );
	my ( $nodeid, $baseBridgeAddress, $baseNumPorts, $baseType, 
        $stpProtocolSpecification, $stpPriority,$stpDesignatedRoot,
        $stpRootCost, $stpRootPort, $status, $baseVlan) = @_ ;
    &function_log( 0, "sql_update_stpnode - try update new row @_" );

	if (!($sth = $dbh->prepare(
		'UPDATE stpnode  set 
            baseBridgeAddress=?,
            baseNumPorts=?,
            basetype=?,
            stpProtocolSpecification=?,
            stpPriority=?,
            stpdesignatedroot=?,
            stprootcost=?,
            stprootport=?,
            status=? ,
            lastPollTime=(\'now\')
		WHERE nodeid = ? AND basevlan = ? '
	)))
	{
		&function_log( 3,
			"sql_update_stpnode  - Couldn't prepare statement: $dbh->errstr" );
		return 0;
	}
	if (!($sth->execute(
        $baseBridgeAddress, $baseNumPorts, $baseType, 
        $stpProtocolSpecification, $stpPriority,$stpDesignatedRoot,
        $stpRootCost, $stpRootPort, $status, $nodeid, $baseVlan) ) ) {
		&function_log( 3,
			"sql_update_stpnode - Couldn't execute statement: $sth->errstr" );
		return 0;
	}
	$sth->finish;
	&function_log( 0,"sql_update_stpnode - updated row @_ on stpnode");
	&function_log( 0, "sql_update_stpnode - exiting without errors from sub" );
}

sub sql_insert_stpinterface {

	&function_log( 0, "sql_insert_stpinterface - entering in sub" );
	my ($nodeid,$bridgeport,$ifindex,$stpportstate,$stpportpathcost,
        $stpportdesignatedroot,$stpportdesignatedcost,$stpportdesignatedbridge,
        $stpportdesignatedport,$status,$stpvlan) = @_ ;
    &function_log( 0, "sql_insert_stpinterface - try insert new row @_" );

	if (!($sth = $dbh->prepare(
        'INSERT INTO stpinterface (
    		nodeid,
    		bridgeport,
    		ifindex,
    		stpportstate,
    		stpportpathcost,
    		stpportdesignatedroot,
    		stpportdesignatedcost,
    		stpportdesignatedbridge,
    		stpportdesignatedport,
    		status,
    		lastPollTime,
    		stpvlan) 
        VALUES (?,?,?,?,?,?,?,?,?,?,(\'now\'),?)'
	)))
	{
		&function_log( 3,
			"sql_insert_stpinterface - Couldn't prepare statement: $dbh->errstr" );
		return 0;
	}
	if (!($sth->execute(
        $nodeid,$bridgeport,$ifindex,$stpportstate,$stpportpathcost,
        $stpportdesignatedroot,$stpportdesignatedcost,$stpportdesignatedbridge,
        $stpportdesignatedport,$status,$stpvlan
    )))
    {
		&function_log( 3,
			"sql_insert_stpinterface - Couldn't execute statement: $sth->errstr" );
		return 0;
	}
	$sth->finish;
	&function_log( 0,"sql_insert_stpinterface - inserted new row @_ in stpinterface");
	&function_log( 0, "sql_insert_stpinterface - exiting without errors from sub" );

}

sub sql_update_stpinterface {

	&function_log( 0, "sql_update_stpinterface - entering in sub" );
	my ( $nodeid,$bridgeport,$ifindex,$stpportstate,$stpportpathcost,
        $stpportdesignatedroot,$stpportdesignatedcost,$stpportdesignatedbridge,
        $stpportdesignatedport,$status,$stpvlan) = @_ ;
    &function_log(0, "sql_update_stpinterface - try update new row @_" );


	if (!($sth = $dbh->prepare(
		'UPDATE stpinterface  set 
            ifindex=?,
            stpportstate=?,
            stpportpathcost=?,
            stpportdesignatedroot=?,
            stpportdesignatedcost=?,
            stpportdesignatedbridge=?,
            stpportdesignatedport=?,
            status=?,
            lastPollTime=(\'now\')
		WHERE nodeid = ? AND bridgeport=? AND stpvlan=? '
	))) {
		&function_log( 3,
			"sql_update_stpinterface  - Couldn't prepare statement: $dbh->errstr" );
		return 0;
	}
	if (!($sth->execute(
        $ifindex, $stpportstate, $stpportpathcost,$stpportdesignatedroot,
        $stpportdesignatedcost,$stpportdesignatedbridge,$stpportdesignatedport,
        $status,$nodeid, $bridgeport,$stpvlan
    ))) {
		&function_log( 3,
			"sql_update_stpinterface - Couldn't execute statement: $sth->errstr" );
		return 0;
	}
	$sth->finish;
	&function_log( 0,"sql_update_stpinterface - updated row @_ on stpinterface");
	&function_log( 0, "sql_update_stpinterface - exiting without errors from sub" );
}

sub sql_insert_iprouteinterface{

	&function_log( 0, "sql_insert_iprouteinterface - entering in sub" );
	my ($nodeid,$routeDest,$routeifindex,$routemetric1,$routemetric2,
        $routemetric3,$routemetric4,$routeNextHop,$routetype,$routeproto,
        $routeMask,$routemetric5,$status) = @_;
	&function_log( 0, "sql_insert_iprouteinterface - try insert new row @_" );

	if (!($sth = $dbh->prepare(
        'INSERT INTO iprouteinterface (
       		nodeid,
            routeDest,
    		routeifindex,
    		routemetric1,
    		routemetric2,
    		routemetric3,
    		routemetric4,
            routeNextHop,
    		routetype,
    		routeproto,
            routeMask,
    		routemetric5,
    		status,
    		lastPollTime
    		) 
        VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,(\'now\'))'
    )))
	{
		&function_log( 3,
			"sql_insert_iprouteinterface - Couldn't prepare statement: $dbh->errstr" );
		return 0;
	}
	if (!($sth->execute(
        $nodeid,$routeDest,$routeifindex,$routemetric1,$routemetric2,
        $routemetric3,$routemetric4,$routeNextHop,$routetype,$routeproto,
        $routeMask,$routemetric5,$status
    )))
    {
		&function_log( 3,
			"sql_insert_atinterface - Couldn't execute statement: $sth->errstr");
		return 0;
	}
	$sth->finish;
	&function_log( 0,"sql_insert_iprouteinterface - inserted new row @_ in iprouteinterface" );
	&function_log( 0, "sql_insert_iprouteinterface - exiting without errors from sub" );
}

sub sql_update_iprouteinterface {

	&function_log( 0, "sql_update_iprouteinterface - entering in sub" );
	my ( $nodeid,$routeDest,$routeifindex,$routemetric1,$routemetric2,
        $routemetric3,$routemetric4,$routeNextHop,$routetype,$routeproto,
        $routeMask,$routemetric5,$status) = @_ ;
    &function_log(0, "sql_update_iprouteinterface - try update new row @_" );


	if (!($sth = $dbh->prepare(
        'UPDATE iprouteinterface  set 
        	routeifindex=?,
        	routemetric1=?,
        	routemetric2=?,
        	routemetric3=?,
        	routemetric4=?,
            routeNextHop=?,
        	routetype=?,
        	routeproto=?,
            routeMask=?,
        	routemetric5=?,
        	status=?,
        	lastpolltime = (\'now\') 
        WHERE nodeid = ? AND routeDest = ? '
	)))
	{
		&function_log( 3,
			"sql_update_iprouteinterface  - Couldn't prepare statement: $dbh->errstr" );
		return 0;
	}
        
	if (!($sth->execute(
        $routeifindex,$routemetric1,$routemetric2,$routemetric3,$routemetric4,
        $routeNextHop,$routetype,$routeproto,$routeMask,$routemetric5,$status,
        $nodeid,$routeDest
    )))
    {
		
        &function_log( 3,
			"sql_update_iprouteinterface - Couldn't execute statement: $sth->errstr" );
		return 0;
	}
	$sth->finish;
	&function_log(0,"sql_update_iprouteinterface - updated row @_ on iprouteinterface");
	&function_log( 0, "sql_update_iprouteinterface - exiting without errors from sub" );
}

sub sql_insert_datalinkinterface{

	&function_log( 0, "sql_insert_datalinkinterface - entering in sub" );
	my ($nodeid,$ifindex,$nodeparentid,$parentifindex,$status) = @_;
    &function_log(0, "sql_insert_datalinkinterface - try insert new row @_" );

	if (!($sth = $dbh->prepare(
        'INSERT INTO datalinkinterface (
           	nodeid,
            ifindex,
            nodeparentid,
            parentIfIndex,
        	status,
        	lastPollTime) 
        VALUES (?,?,?,?,?,(\'now\'))'
    )))
	{
		&function_log( 3,
		"sql_insert_datalinkinterface - Couldn't prepare statement: $dbh->errstr" );
		return 0;
	}
	if (!($sth->execute(
        $nodeid,$ifindex,$nodeparentid,$parentifindex,$status
    )))
    {
		&function_log( 3,
		"sql_insert_datalinkinterface - Couldn't execute statement: $sth->errstr" );
		return 0;
	}
	$sth->finish;
	&function_log(0,"sql_insert_datalinkinterface - inserted new row @_ in datalinkinterface" );
	&function_log( 0, "sql_insert_datalinkinterface - exiting without errors from sub" );

}

sub sql_update_datalinkinterface {

	&function_log( 0, "sql_update_datalinkinterface - entering in sub" );
    my ($nodeid,$ifindex,$nodeparentid,$parentifindex,$status) = @_ ;
	&function_log(0, "sql_update_datalinkinterface - try update new row @_" );

	if (!($sth = $dbh->prepare(
        'UPDATE datalinkinterface  set 
            nodeparentid=?,
            parentifindex=?,
            status=?,
            lastpolltime = (\'now\') 
        WHERE nodeid = ? AND ifindex = ? '
	)))	{
		&function_log( 3,
			"sql_update_datalinkinterface  - Couldn't prepare statement: $dbh->errstr" );
		return 0;
	}
	if (!($sth->execute(
        $nodeparentid,$parentifindex,$status,$nodeid,$ifindex
    )))
    {
		&function_log( 3,
			"sql_update_datalinkinterface - Couldn't execute statement: $sth->errstr" );
		return 0;
	}
	$sth->finish;
	&function_log( 0,"sql_update_datalinkinterface - updated row @_ on datalinkinterface");
	&function_log( 0, "sql_update_datalinkinterface - exiting without errors from sub" );
}

sub parse_xml_snmp_config() {


     my $communitystring = 'public';
     my $port            = 161;
     my $retry           = 2;
     my $timeout         = 800;
     my $version         = "v2c";

     my $file = '@install.etc.dir@/snmp-config.xml';
     &function_log( 0, "parse_xml_snmp_config - entering in sub" );

     &function_log( 4,
 	"parse_xml_snmp_config - conf file $file does not exists" )
     if ( !-f $file );

     my $parser = XML::DOM::Parser->new();

     my ($IP) = @_;

     my @iparray;
     my @iparray1;
     my @iparray2;

     my $doc = $parser->parsefile($file);

     SNMPCONFIG:
     foreach my $snmpconfig ( $doc->getElementsByTagName('snmp-config') ) {
    	if ( defined $snmpconfig->getAttribute('read-community') ) {
	         $communitystring = $snmpconfig->getAttribute('read-community')
	         if ( $snmpconfig->getAttribute('read-community') ne '' );
	     }
     	if ( defined $snmpconfig->getAttribute('port') ) {
	         $port = $snmpconfig->getAttribute('port')
			  if ( $snmpconfig->getAttribute('port') ne '' );
		 }
		if ( defined $snmpconfig->getAttribute('timeout') ) {
			$timeout = $snmpconfig->getAttribute('timeout')
			  if ( $snmpconfig->getAttribute('timeout') ne '' );
		}
		if ( defined $snmpconfig->getAttribute('version') ) {
			$version = $snmpconfig->getAttribute('version')
			  if ( $snmpconfig->getAttribute('version') ne '' );
		}
		if ( defined $snmpconfig->getAttribute('retry') ) {
			$retry = $snmpconfig->getAttribute('retry')
			  if ( $snmpconfig->getAttribute('retry') ne '' );
		}
		foreach my $def ( $snmpconfig->getElementsByTagName('definition') ) {
			if ( defined $def->getElementsByTagName('specific')->item(0) ) {
				foreach my $spec ( $def->getElementsByTagName('specific') ) {
					my $IPADDR = $spec->getFirstChild->getNodeValue;
					if ( $IP eq $IPADDR ) {
						if ( defined $def->getAttribute('read-community') ) {
							$communitystring =
							  $def->getAttribute('read-community')
							  if ( $def->getAttribute('read-community') ne '' );
						}
						if ( defined $def->getAttribute('port') ) {
							$port = $def->getAttribute('port')
							  if ( $def->getAttribute('port') ne '' );
						}
						if ( defined $def->getAttribute('timeout') ) {
							$timeout = $def->getAttribute('timeout')
							  if ( $def->getAttribute('timeout') ne '' );
						}
						if ( defined $def->getAttribute('version') ) {
							$version = $def->getAttribute('version')
							  if ( $def->getAttribute('version') ne '' );
						}
						if ( defined $def->getAttribute('retry') ) {
							$retry = $def->getAttribute('retry')
							  if ( $def->getAttribute('retry') ne '' );
						}
						last SNMPCONFIG;
					}
				}
			}
			if ( defined $def->getElementsByTagName('range')->item(0) ) {
				foreach my $rng ( $def->getElementsByTagName('range') ) {
					my $IP_BEGIN = $rng->getAttribute('begin');
					my $IP_END   = $rng->getAttribute('end');
					@iparray  = split( /\./, $IP );
					@iparray1 = split( /\./, $IP_BEGIN );
					@iparray2 = split( /\./, $IP_END );
					my $ii = 0;
					for ( $ii = 0 ; $ii < 4 ; $ii++ ) {
						last
						  if ( $iparray[$ii] < $iparray1[$ii]
							|| $iparray[$ii] > $iparray2[$ii] );
						next if ( $iparray1[$ii] == $iparray2[$ii] );
						if ( defined $def->getAttribute('read-community') ) {
							$communitystring =
							  $def->getAttribute('read-community')
							  if ( $def->getAttribute('read-community') ne '' );
						}
						if ( defined $def->getAttribute('port') ) {
							$port = $def->getAttribute('port')
							  if ( $def->getAttribute('port') ne '' );
						}
						if ( defined $def->getAttribute('timeout') ) {
							$timeout = $def->getAttribute('timeout')
							  if ( $def->getAttribute('timeout') ne '' );
						}
						if ( defined $def->getAttribute('version') ) {
							$version = $def->getAttribute('version')
							  if ( $def->getAttribute('version') ne '' );
						}
						if ( defined $def->getAttribute('retry') ) {
							$retry = $def->getAttribute('retry')
							  if ( $def->getAttribute('retry') ne '' );
						}
						last SNMPCONFIG;
					}
				}
			}
		}
	}
	$timeout              = $timeout / 1000;
	$SNMP{$IP}{COMMUNITY} = $communitystring;
	&function_log( 1,
		"parse_xml_snmp_config - $IP SNMP_COMMUNITY param $communitystring" );
	$SNMP{$IP}{PORT} = $port;
	&function_log( 1, "parse_xml_snmp_config - $IP SNMP_PORT param $port" );
	$SNMP{$IP}{RETRY} = $retry;
	&function_log( 1,
        "parse_xml_snmp_config - $IP SNMP_RETRY param $retry" );
	$SNMP{$IP}{TIMEOUT} = $timeout;
	&function_log( 1,
		"parse_xml_snmp_config - $IP SNMP_TIMEOUT param $timeout" );
	$SNMP{$IP}{VERSION} = $version;
	&function_log( 1,
		"parse_xml_snmp_config - $IP SNMP_VERSION param $version" );
	&function_log( 0,
		"parse_xml_snmp_config - exiting without errors from sub" );
}

sub parse_xml_linkconf() {

    undef  %sysoidmask2vlanoid;
    my ($file) = @_;

    &function_log(0, "parse_xml_linkconf - entering in sub" );

    my $parser = XML::DOM::Parser->new();
    &function_log( 1, "parse_xml_linkconf - parsing file $file" );

    my $doc = $parser->parsefile($file);

    foreach my $vendor ( $doc->getElementsByTagName('vendor') ) {
        my $sysoidsMask  = -1;
        my $vlanoidsMask = -1;
        foreach my $sysoid ( $vendor->getElementsByTagName('sysoidMask') ) {
            $sysoidsMask = $sysoid->getFirstChild->getNodeValue;
        }
        foreach my $vlanoid ( $vendor->getElementsByTagName('vlanoid') ) {
            $vlanoidsMask = $vlanoid->getFirstChild->getNodeValue;
            $sysoidmask2vlanoid{$sysoidsMask} = $vlanoidsMask;
        }
        &function_log( 1,
        "parse_xml_linkconf - get vlan oid $vlanoidsMask for systema mask oid $sysoidsMask");
    }
    &function_log(0, "parse_xml_linkconf - exiting without errors from sub" );
    return 0;
}

sub function_log {

     my ( $LEVEL, $STRING ) = @_;
     my @LEVELSTRING = ( "DEBUG", "INFO", "WARN", "ERROR", "FATAL" );
     $STRING .= "\n";
     if ( $LEVEL == 4 ) {
  	     die scalar localtime() . " " . $LEVELSTRING[$LEVEL] . " " . $STRING;
     }
     print STDERR scalar localtime() . " " . $LEVELSTRING[$LEVEL] . " " . $STRING
     if ( $LEVEL >= $LOGLEVEL );

}

sub send_event {

	my ( $nodeid, $DESCR, $IPADDR, $SERVICE, $SEVERITY, $UEI, $OPERINSTR ) = @_;
	my $send_event = '@install.bin.dir@/send-event.pl';
`$send_event "$UEI" -s "$SERVICE" -n $nodeid -i "$IPADDR" -x "$SEVERITY" -d "$DESCR" -o "$OPERINSTR" `;

}

--########################################################################
--#
--# author rssntn67@yahoo.it
--# 10/08/04
--# creato il file e le tabelle
--# rev. rssntn67@yahoo.it
--# 18/08/04 
--# eliminato createtime dalle tabelle
--# sufficiente il createtime della tabella node
--#
--########################################################################

drop table atinterface cascade;
drop table stpnode cascade;
drop table stpinterface cascade;
drop table iprouteinterface cascade;
drop table datalinkinterface cascade;
drop table inventory cascade;

--########################################################################
--#
--# atInterface table -- This table maintains a record of ip address to mac 
--#                  address among  interfaces. It reflect information from mib-2
--#                  arp table
--#	at interface is now deprecated .iso.org.dod.internet.mgmt.mib-2.at.atTable.atEntry
--#                  OID: .1.3.6.1.2.1.3.1.1
--#	so support is for .iso.org.dod.internet.mgmt.mib-2.ip.ipNetToMediaTable.ipNetToMediaEntry 
--#                  OID: .1.3.6.1.2.1.4.22.1	
--#					
--# This table provides the following information:
--#
--#  nodeid            : Unique integer identifier of the node
--#  ipAddr            : Ip address identifier of the node
--#  atPhysAddr        : Mac address identifier for the node
--#  status            : Flag indicating the status of the entry.
--#                      'A' - Active
--#                      'N' - Not Active
--#                      'D' - Deleted
--#  sourceNodeid      : The nodeid from which information have been retrivied.
--#  ifindex           : The SNMP ifindex on which this info was recorded.
--#  lastPollTime    : The last time when this information was retrived
--#
--########################################################################

create table atinterface (
    nodeid	   integer not null,
    constraint fk_ar_nodeID1 foreign key (nodeid) references node,
    ipAddr	   varchar(16) not null,
    atPhysAddr	   varchar(12) not null,
    status	   char(1) not null,
    sourceNodeid   integer not null,
    ifindex	   integer not null,
    lastPollTime timestamp not null
);

create index atinterface_node_ipaddr_idx on atinterface(nodeid,ipaddr);
create index atinterface_atphysaddr_idx on atinterface(atphysaddr);

--########################################################################
--#
--# stpNode table -- This table maintains a record of general bridge interface.
--#                  It reflect information from the mib-2 bridge mib 
--# 		         support .iso.org.dod.internet.mgmt.mib-2.dot1dBridge
--#                  OID: .1.3.6.1.2.1.17	
--#					
--# This table provides the following information:
--#
--#  nodeid   	              : Unique integer identifier of the node
--#  baseBridgeAddress        : The MAC address used by this bridge when it must
--#                             be referred to in a unique fashion.
--#  baseNumPorts             : The number of ports controlled by the bridge entity.
--#  baseType				  : Indicates what type of bridging this bridge can
--#                             perform.
--#						        '1' unknown
--#						        '2' transparent-only
--#						        '3' sourceroute-only
--#                             '4' srt
--#  stpProtocolSpecification : An indication of what version of the Spanning
--#                             Tree Protocol is being run. 
--#						        '1' unknown
--#						        '2' decLb100
--#						        '3' ieee8011d
--#  stpPriority              : The value of the write-able portion of the Bridge
--#                             ID, i.e., the first two octets of the (8 octet
--#                             long) Bridge ID. The other (last) 6 octets of the
--#                             Bridge ID are given by the value of dot1dBaseBridgeAddress.
--#  stpDesignatedRoot        : The bridge identifier of the root of the spanning
--#                             tree as determined by the Spanning Tree Protocol
--#                             as executed by this node.
--#  stpRootCost              : The cost of the path to the root as seen from this bridge.
--#  stpRootPort              : The port number of the port which offers the
--#                             lowest cost path from this bridge to the root bridge.
--#  status                   : Flag indicating the status of the entry.
--#                             'A' - Active
--#                             'N' - Not Active
--#                             'D' - Deleted
--#  lastPollTime             : The last time when this information was retrived
--#  baseVlan                 : Unique integer identifier VLAN for which this info is valid
--#
--########################################################################

create table stpnode (
    nodeid		     integer not null,
	constraint fk_ar_nodeID2 foreign key (nodeid) references node,
    baseBridgeAddress	     varchar(12) not null,
    baseNumPorts             integer,
    basetype                 integer,
    stpProtocolSpecification integer,
    stpPriority              integer,
    stpdesignatedroot        varchar(16) not null,
    stprootcost              integer,
    stprootport              integer,
    status		     char(1) not null,
    lastPollTime             timestamp not null,
    basevlan                 integer not null
);

alter table stpnode add constraint pk_stpnode primary key (nodeid,basevlan);
create index stpnode_baseBridgeAddress_idx on stpnode(baseBridgeAddress);
create index stpnode_stpdesignatedroot_idx on stpnode(stpdesignatedroot);

--########################################################################
--#
--# stpInterface table -- This table maintains a record of STP interface.
--#                  It reflect information from mib-2
--#                  bridge mib and subinterface STP table
--#					 support .iso.org.dod.internet.mgmt.mib-2.dot1dBridge
--#                  OID: .1.3.6.1.2.1.17	
--#					
--# This table provides the following information:
--#
--#  nodeid   	              : Unique integer identifier of the node
--#  ifIndex                  : interface ifindex corresponding to bridge port number
--#  bridgePort               : bridge port number identifier
--#  stpPortState             : integer that reflect thestp staus of the bridge port
--#						        '1' disabled
--#						        '2' blocking
--#						        '3' listening
--#						        '4' learning
--#						        '5' forwarding
--#						        '6' broken
--#  stpPortPathCost          : The contribution of this port to the path cost of
--#                             paths towards the spanning tree root which include
--#                             this port.
--#  stpPortDesignatedRoot    : the unique Bridge Identifier of the Bridge
--#                             recorded as the Root in the Configuration BPDUs
--#                             transmitted by the Designated Bridge for the
--#                             segment to which the port is attached.  
--#  stpPortDesignatedCost    : The path cost of the Designated Port of the
--#                             segment connected to this port. This value is
--#                             compared to the Root Path Cost field in received
--#                             bridge PDUs.
--#  stpPortDesignatedBridge  : The Bridge Identifier of the bridge which this
--#                             port considers to be the Designated Bridge for
--#                             this port's segment.
--#  stpPortDesignatedPort    : The Port Identifier of the port on the Designated
--#                             Bridge for this port's segment.
--#  status                   : Flag indicating the status of the entry.
--#                             'A' - Active
--#                             'N' - Not Active
--#                             'D' - Deleted
--#  lastPollTime          : The last time when this information was retrived
--#  stpVlan                  : Unique integer identifier VLAN for which this info is valid
--#                             value '0' means that the port is a trunk port
--#
--########################################################################

create table stpinterface (
    nodeid	            integer not null,
	constraint fk_ar_nodeID3 foreign key (nodeid) references node,
    bridgeport              integer not null,
    ifindex                 integer not null,
    stpportstate            integer,
    stpportpathcost         integer,
    stpportdesignatedroot   varchar(16),
    stpportdesignatedcost   integer,
    stpportdesignatedbridge varchar(16),
    stpportdesignatedport   varchar(4),
    status       	    char(1) not null,
    lastPollTime         timestamp not null,
    stpvlan                 integer not null
);

create index stpinterface_node_ifindex_idx on stpinterface(nodeid,ifindex);
create index stpinterface_node_idx on stpinterface(nodeid);
create index stpinterface_stpvlan_idx on stpinterface(stpvlan);
create index stpinterface_stpdesbridge_idx on stpinterface(stpportdesignatedbridge);

--########################################################################
--#
--# ipRouteInterface table -- This table maintains a record of ip route info on routers.
--#                           It reflect information from mib-2
--#                           ipRouteTable mib 
--#					          support .iso.org.dod.internet.mgmt.mib-2.ip.ipRouteTable.ipRouteEntry
--#                           OID: .1.3.6.1.2.1.4.21.1	
--#					
--# This table provides the following information:
--#
--#  nodeid   	       : Unique integer identifier of the node
--#  routeDest         : The destination IP address of this route. An
--#                      entry with a value of 0.0.0.0 is considered a default route.
--#  routeMask         : Indicate the mask to be logical-ANDed with the
--#                      destination address before being compared to the
--#                      value in the ipRouteDest field.
--#  routeNextHop      : The IP address of the next hop of this route.
--#                      (In the case of a route bound to an interface
--#                      which is realized via a broadcast media, the value
--#                      of this field is the agent's IP address on that
--#                      interface.)
--#  routeifIndex      : The index value which uniquely identifies the
--#                      local interface through which the next hop of this
--#                      route should be reached. 
--#  routeMetric1      : The primary routing metric for this route. The
--#                      semantics of this metric are determined by the
--#                      routing-protocol specified in the route's
--#                      ipRouteProto value. If this metric is not used,
--#                      its value should be set to -1.
--#  routeMetric2      : An alternate routing metric for this route.
--#  routeMetric3      : An alternate routing metric for this route.
--#  routeMetric4      : An alternate routing metric for this route.
--#  routeMetric5      : An alternate routing metric for this route.
--#  routeType         : The type of route.
--#						 '1' other
--#						 '2' invalid
--#						 '3' direct
--#						 '4' indirect
--#  routeProto        : The routing mechanism via which this route was learned. 
--#						 '1' other
--#						 '2' local
--#						 '3' netmgmt
--#						 '4' icmp
--#						 '5' egp
--#						 '6' ggp
--#						 '7' hello
--#						 '8' rip
--#						 '9' is-is
--#						 '10' es-is
--#						 '11' ciscolgrp
--#						 '12' bbnSpfIgp
--#						 '13' ospf
--#						 '14' bgp
--#  status            : Flag indicating the status of the entry.
--#                      'A' - Active
--#                      'N' - Not Active
--#                      'D' - Deleted
--#  lastPollTime      : The last time when this information was retrived
--#
--########################################################################

create table iprouteinterface (
    nodeid		    integer not null,
	constraint fk_ar_nodeID4 foreign key (nodeid) references node,
    routeDest               varchar(16) not null,
    routeMask               varchar(16) not null,
    routeNextHop            varchar(16) not null,
    routeifindex            integer not null,
    routemetric1            integer,
    routemetric2            integer,
    routemetric3            integer,
    routemetric4            integer,
    routemetric5            integer,
    routetype               integer,
    routeproto              integer,
    status		    char(1) not null,
    lastPollTime            timestamp not null
);

alter table iprouteinterface add constraint pk_iprouteinterface primary key (nodeid,routedest);
create index iprouteinterface_node_ifdex_idx on iprouteinterface(nodeid,routeifindex);
create index iprouteinterface_rnh_idx on iprouteinterface(routenexthop);

--########################################################################
--#
--# dataLinkInterface table -- This table maintains a record of data link info 
--#                            among  the interfaces. 
--#                            Data is calculated using info from other tables
--#
--# This table provides the following information:
--#
--#  nodeid            : Unique integer identifier for the linked node 
--#  IfIndex           : SNMP index of interface connected to the link on the node, 
--# 		             is null if it doesn't support SNMP.
--#  nodeparentid      : Unique integer identifier for linking node
--#  parentIfIndex     : SNMP index of interface linked on the parent node.
--#  linkTypeId        : SNMP Link type identification number
--#  status            : Flag indicating the status of the entry.
--#                      'A' - Active
--#                      'N' - Not Active
--#                      'D' - Deleted
--#  lastPollTime      : The last time when this information was retrived
--#
--########################################################################

create table datalinkinterface (
    nodeid	     integer not null,
	constraint fk_ar_nodeID5 foreign key (nodeid) references node,
    ifindex          integer not null,
    nodeparentid     integer not null,
	constraint fk_ar_nodeID6 foreign key (nodeparentid) references node,
    parentIfIndex    integer not null,
    status	     char(1) not null,
    lastPollTime timestamp not null
);

create index dlint_node_idx on datalinkinterface(nodeid);
create index dlint_node_ifindex_idx on datalinkinterface(nodeid,ifindex);
create index dlint_nodeparent_idx on datalinkinterface(nodeparentid);
create index dlint_nodeparent_paifindex_idx on datalinkinterface(nodeparentid,parentifindex);

--########################################################################
--#
--# inventory table -- This table maintains inventories 
--#                  of switch nodes.
--#
--# This table provides the following information:
--#
--#  nodeid            : Unique integer identifier for the linked node. 
--#  name			   : Name that describes the category of the inventory.
--#  createtime        : The timestamp of the creation of the inventory.
--#  lastpolltime      : The timestamp of last download of the inventory.
--#  pathtofile        : The path where the inventory file is stored.
--#  status            : Flag indicating the status of the entry.
--#                      'A' - Active
--#                      'N' - Not Active
--#                      'D' - Deleted: when the status of the node associated 
--# 						   is Deleted
--#
--########################################################################

create table inventory (
        nodeid		integer not null,
				constraint fk_ar_nodeID7 foreign key (nodeID)	references node,
        name 	varchar(30) not null,
        createtime   timestamp not null,
	    lastpolltime   timestamp not null,
        pathtofile varchar(256) not null,
	    status char(1) not null
        );

create index inventory_nodeid_name_idx on inventory(nodeid,name);
create index inventory_lastpolltime_idx on inventory(lastpolltime);
create index inventory_status_idx on inventory(status);
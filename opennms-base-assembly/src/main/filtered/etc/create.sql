--# create.sql -- SQL to build the initial tables for the OpenNMS Project
--#
--# Modifications:
--# 2013 Nov 15: Added protocol field in datalinkinterface table
--# 2009 Sep 29: Added linkTypeId field in datalinkinterface table
--# 2009 Mar 27: Added Users, Groups tables
--# 2009 Jan 28: Added Acks tables - david@opennms.org
--# 2007 Apr 10: Added statistics report tables - dj@opennms.org
--# 2006 Apr 17: Added pathOutage table
--# 2005 Mar 11: Added alarms table
--# 2004 Aug 30: See create.sql.changes
--#
--# Copyright (C) 2005-2006 The OpenNMS Group, Inc., Inc.  All rights reserved.
--# Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
--#
--# This program is free software; you can redistribute it and/or modify
--# it under the terms of the GNU General Public License as published by
--# the Free Software Foundation; either version 2 of the License, or
--# (at your option) any later version.
--#
--# This program is distributed in the hope that it will be useful,
--# but WITHOUT ANY WARRANTY; without even the implied warranty of
--# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--# GNU General Public License for more details.
--#
--# You should have received a copy of the GNU General Public License
--# along with this program; if not, write to the Free Software
--# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
--#
--# For more information contact:
--#      OpenNMS Licensing       <license@opennms.org>
--#      http://www.opennms.org/
--#      http://www.sortova.com/
--#

drop table accessLocks cascade;
drop table accesspoints cascade;
drop table requisitioned_categories cascade;
drop table category_node cascade;
drop table categories cascade;
drop table assets cascade;
drop table usersNotified cascade;
drop table notifications cascade;
drop table outages cascade;
drop table ifServices cascade;
drop table snmpInterface cascade;
drop table ipInterface cascade;
drop table alarms cascade;
drop table memos cascade;
drop table node cascade;
drop table service cascade;
drop table distPoller cascade;
drop table events cascade;
drop table vulnerabilities cascade;
drop table vulnPlugins cascade;
drop table serverMap cascade;
drop table serviceMap cascade;
drop table pathOutage cascade;
drop table demandPolls cascade;
drop table pollResults cascade;
drop table reportLocator cascade;
drop table atinterface cascade;
drop table stpnode cascade;
drop table stpinterface cascade;
drop table iprouteinterface cascade;
drop table datalinkinterface cascade;
drop table inventory cascade;
drop table element cascade;
drop table map cascade;
drop table location_monitors cascade;
drop table location_specific_status_changes cascade;
drop table vlan cascade;
drop table statisticsReportData cascade;
drop table resourceReference cascade;
drop table statisticsReport cascade;
drop table acks cascade;
drop table users cascade;
drop table groups cascade;
drop table group_user cascade;
drop table category_user cascade;
drop table category_group cascade;
drop table filterfavorites cascade;
drop table hwentity cascade;
drop table hwentityattribute cascade;
drop table hwentityattributetype cascade;
drop table minions_properties cascade;
drop table minions cascade;

drop sequence catNxtId;
drop sequence nodeNxtId;
drop sequence serviceNxtId;
drop sequence eventsNxtId;
drop sequence alarmsNxtId;
drop sequence memoNxtId;
drop sequence outageNxtId;
drop sequence notifyNxtId;
drop sequence userNotifNxtId;
drop sequence demandPollNxtId;
drop sequence pollResultNxtId;
drop sequence vulnNxtId;
drop sequence reportNxtId;
drop sequence reportCatalogNxtId;
drop sequence mapNxtId;
drop sequence opennmsNxtId;  --# should be used for all sequences, eventually
drop sequence filternextid;

drop index filternamesidx;

--# Begin quartz persistence 

drop table qrtz_job_listeners;
drop table qrtz_trigger_listeners;
drop table qrtz_fired_triggers;
drop table qrtz_paused_trigger_grps;
drop table qrtz_scheduler_state;
drop table qrtz_locks;
drop table qrtz_simple_triggers;
drop table qrtz_cron_triggers;
drop table qrtz_blob_triggers;
drop table qrtz_triggers;
drop table qrtz_job_details;
drop table qrtz_calendars;

--# End quartz persistence

CREATE FUNCTION plpgsql_call_handler () 
    RETURNS OPAQUE AS '$libdir/plpgsql.so' LANGUAGE 'c';

CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql' 
    HANDLER plpgsql_call_handler LANCOMPILER 'PL/pgSQL';

--##################################################################
--# The following commands set up automatic sequencing functionality
--# for fields which require this.
--#
--# DO NOT forget to add an "install" comment so that the installer
--# knows to fix and renumber the sequences if need be
--##################################################################

--# Sequence for the nodeID column in the aggregate_status_views and the
--# aggregate_status_definitions tables (eventually all tables, perhaps)
--#          sequence, column, table
--# install: opennmsNxtId id   aggregate_status_views
create sequence opennmsNxtId minvalue 1;

--# Sequence for the nodeID column in the node table
--#          sequence, column, table
--# install: nodeNxtId nodeID   node
create sequence nodeNxtId minvalue 1;

--# Sequence for the serviceID column in the service table
--#          sequence,    column,   table
--# install: serviceNxtId serviceID service
create sequence serviceNxtId minvalue 1;

--# Sequence for the eventID column in the events table
--#          sequence,   column, table
--# install: eventsNxtId eventID events
create sequence eventsNxtId minvalue 1;

--# Sequence for the alarmId column in the alarms table
--#          sequence,   column, table
--# install: alarmsNxtId alarmId alarms
create sequence alarmsNxtId minvalue 1;

--# Sequence for the id column in the memos table
--#          sequence,   column, table
--# install: memoNxtId id memos
create sequence memoNxtId minvalue 1;

--# Sequence for the outageID column in the outages table
--#          sequence,   column,  table
--# install: outageNxtId outageID outages
create sequence outageNxtId minvalue 1;

--# Sequence for the notifyID column in the notification table
--#          sequence,   column,  table
--# install: notifyNxtId notifyID notifications
create sequence notifyNxtId minvalue 1;

--# Sequence for the vulnerabilityID column in the vulnerabilities table
--#          sequence, column,         table
--# install: vulnNxtId vulnerabilityID vulnerabilities
create sequence vulnNxtId minvalue 1;

--# Sequence for the id column in the categories table
--#          sequence, column, table
--# install: catNxtId categoryid   categories
create sequence catNxtId minvalue 1;

--# Sequence for the id column in the usersNotified table
--#          sequence, column, table
--# install: userNotifNxtId id   usersNotified
create sequence userNotifNxtId minvalue 1;

--# Sequence for the id column in the demandPolls table
--#          sequence, column, table
--# install: demandPollNxtId id   demandPolls
create sequence demandPollNxtId minvalue 1;

--# Sequence for the id column in the pollResults table
--#          sequence, column, table
--# install: pollResultNxtId id   pollResults
create sequence pollResultNxtId minvalue 1;

--# Sequence for the mapID column in the map table
--#          sequence,   column, table
--# install: mapNxtId mapid map
create sequence mapNxtId minvalue 1;

--# Sequence for the filterid column in the filterfavorites table
--#          sequence, column, table
--# install: filternextid filterid filterfavorites
create sequence filternextid minvalue 1;


--# A table to use to manage upsert access

CREATE TABLE accessLocks (
    lockName varchar(40) not null,
    constraint pk_accessLocks PRIMARY KEY (lockName)
);


--# 


--########################################################################
--# serverMap table - Contains a list of IP Addresses mapped to
--#                   OpenNMS servers
--#
--# This table contains the following fields:
--#
--#  ipAddr      : IP address of the device to be monitored
--#  serverName  : Text field to store the server name
--#
--########################################################################

create table serverMap (
	ipAddr			text not null,
	serverName		varchar(64) not null );

create index server_name_idx on serverMap(serverName);

--########################################################################
--# serviceMap table - Contains a list of IP Addresses mapped to
--#                    OpenNMS services
--#
--# This table contains the following fields:
--#
--#  ipAddr          : IP address of the device to be monitored
--#  serviceName     : Text field to store the service name
--#
--########################################################################

create table serviceMap (
	ipAddr			text not null,
	serviceMapName		varchar(255) not null
);
create index servicemap_name_idx on serviceMap(serviceMapName);
create index serviceMap_ipaddr_idx on serviceMap(ipAddr);

--########################################################################
--# distPoller table - Contains information on Distributed Pollers
--#                    installed in this OpenNMS instance.
--#
--# This table contains the following fields:
--#
--#  dpName      : A human-readable name for each system.  Typically,
--#                the system's hostname (not fully qualified).
--#  dpIP        : IP address of the distributed poller.
--#  dpComment   : Free-form text field
--#  dpDiscLimit : Numeric representation of percentage of interface speed
--#                available to discovery process.  See documentation for
--#                "bandwidth troll"
--#  dpLastNodePull 	: Time of last pull of new nodes from the DP
--#  dpLastEventPull	: Time of last pull of events from the DP
--#  dpLastPackagePush	: Time of last push of Package (config) to the DP
--#  dpAdminState: Reflects desired state for this distributed poller.
--#                1 = Up, 0 = Down
--#  dpRunState  : Reflects the current perceived state of the distributed
--#                poller.  1 = Up, 0 = Down
--#
--########################################################################

create table distPoller (
	dpName			varchar(12) not null,
	dpIP			text not null,
	dpComment		varchar(256),
	dpDiscLimit		numeric(5,2),
	dpLastNodePull		timestamp with time zone,
	dpLastEventPull		timestamp with time zone,
	dpLastPackagePush	timestamp with time zone,
	dpAdminState 		integer,
	dpRunState		integer,

	constraint pk_dpName primary key (dpName)
);

--########################################################################
--# node Table - Contains information on nodes discovered and potentially
--#              managed by OpenNMS.  nodeSys* fields map to SNMP MIB 2
--#              system table information.
--#
--# This table contains the following fields:
--#
--#  nodeID          : Unique identifier for node.  Note that this is the
--#                    enabler for overlapping IP ranges and that uniquity
--#                    is dependent on combination of dpName & IP address
--#  dpName          : Distributed Poller responsible for this node
--#  nodeCreateTime  : Time node was added to the database
--#  nodeParentID    : In the case that the node is virtual or an independent
--#                    device in a chassis that should be reflected as a
--#                    subcomponent or "child", this field reflects the nodeID
--#                    of the chassis/physical node/"parent" device.
--#                    Currently unused.
--#  nodeType        :  Flag indicating status of node
--#			'A' - active
--#  			'D' - deleted
--#  nodeSysOID      : SNMP MIB-2 system.sysObjectID.0
--#  nodeSysName     : SNMP MIB-2 system.sysName.0
--#  nodeSysDescription    : SNMP MIB-2 system.sysDescr.0
--#  nodeSysLocation : SNMP MIB-2 system.sysLocation.0
--#  nodeSysContact  : SNMP MIB-2 system.sysContact.0
--#  nodeLabel	     : User-friendly name associated with the node.
--#  nodeLabelSource : Flag indicating source of nodeLabel
--#                      'U' = user defined
--#                      'H' = IP hostname
--#                      'S' = sysName
--#                      'A' = IP address
--# nodeNetBIOSName  : NetBIOS workstation name associated with the node.
--# nodeDomainName   : NetBIOS damain name associated with the node.
--# operatingSystem  : Operating system running on the node.
--# lastCapsdPoll    : Date and time of last Capsd scan.
--# foreignSource    : When importing nodes this contains the source of the
--#                       nodes, null otherwise
--# foriegnId        : When importing nodes this contains the id of the node
--#                       as known to the foriegn source, null otherwise
--########################################################################

create table node (
	nodeID		integer not null,
	dpName		varchar(12),
	nodeCreateTime	timestamp with time zone not null,
	nodeParentID	integer,
	nodeType	char(1),
	nodeSysOID	varchar(256),
	nodeSysName	varchar(256),
	nodeSysDescription	varchar(256),
	nodeSysLocation	varchar(256),
	nodeSysContact	varchar(256),
	nodeLabel	varchar(256),
	nodeLabelSource	char(1),
	nodeNetBIOSName varchar(16),
	nodeDomainName  varchar(16),
	operatingSystem varchar(64),
	lastCapsdPoll   timestamp with time zone,
	foreignSource	varchar(64),
	foreignId       varchar(64),

	constraint pk_nodeID primary key (nodeID),
	constraint fk_dpName foreign key (dpName) references distPoller
);

create index node_id_type_idx on node(nodeID, nodeType);
create index node_label_idx on node(nodeLabel);
create index node_dpname_idx on node(dpName);
create unique index node_foreign_unique_idx on node(foreignSource, foreignId);

--#########################################################################
--# snmpInterface Table - Augments the ipInterface table with information
--#                       available from IP interfaces which also support
--#                       SNMP.
--#
--# This table provides the following information:
--#
--#  nodeID             : Unique identifier for node to which this if belongs
--#  snmpIpAdEntNetMask : SNMP MIB-2 ipAddrTable.ipAddrEntry.ipAdEntNetMask
--#                       Value is interface's subnet mask
--#  snmpPhysAddr       : SNMP MIB-2 ifTable.ifEntry.ifPhysAddress
--#                       Value is interface's MAC Address
--#  snmpIfIndex        : SNMP MIB-2 ifTable.ifEntry.ifIndex
--#                       Value is interface's arbitrarily assigned index,
--#                       or -100 if we can query the agent, but we can't find
--#                       this IP address in the ifTable.
--#  snmpIfDescr        : SNMP MIB-2 ifTable.ifEntry.ifDescr
--#                       Value is interface's manufacturer/product name/version
--#  snmpIfType         : SNMP MIB-2 ifTable.ifEntry.ifType
--#                       Value is interface's physical/link protocol
--#  snmpIfName		: SNMP MIB-2 ifTable.ifEntry.ifName
--#			  Value is interface's device name
--#  snmpIfSpeed        : SNMP MIB-2 ifTable.ifEntry.ifSpeed
--#                       Value is estimate of interface's data rate
--#  snmpIfAdminStatus  : SNMP MIB-2 ifTable.ifEntry.ifAdminStatus
--#                       Value is interface's desired status
--#                       1 = Up, 2 = Down, 3 = Testing
--#  snmpIfOperStatus   : SNMP MIB-2 ifTable.ifEntry.ifOperStatus
--#                       Value is interface's current operational status
--#                       1 = Up, 2 = Down, 3 = Testing
--#  snmpIfAlias		: SNMP MIB-2 ifXTable.ifXEntry.ifAlias
--#			  Value is interface's device alias
--#  snmpCollect        : 'C' means collect 'N' means don't collect
--#                     : 'UC' means collect 'UN' means don't collect (user override)
--#                       This has been moved from the isSnmpPrimary field in the
--#                         ipinterface table
--#  snmpLastCapsdPoll  : Date and time of last poll by capsd or provisiond
--#  snmpPoll           : 'P' means polled 'N' means not polled (interface admin and oper status)
--#  snmpLastSnmpPoll   : Date and time of last snmp poll 
--#
--# NOTE:  Although not marked as "not null" the snmpIfIndex field
--#        should never be null.  This table is considered to be uniquely
--#        keyed by nodeId and snmpIfIndex.  Eventually ipAddr and
--#        snmpIpAdEntNetMask will be removed and netmask added to
--#        the ipInterface table.
--########################################################################

create table snmpInterface (
    id				INTEGER DEFAULT nextval('opennmsNxtId') NOT NULL,
	nodeID			integer not null,
	snmpIpAdEntNetMask	varchar(45),
	snmpPhysAddr		varchar(32),
	snmpIfIndex		integer not null,
	snmpIfDescr		varchar(256),
	snmpIfType		integer,
	snmpIfName		varchar(96),
	snmpIfSpeed		bigint,
	snmpIfAdminStatus	integer,
	snmpIfOperStatus	integer,
	snmpIfAlias		varchar(256),
    snmpLastCapsdPoll timestamp with time zone,
    snmpCollect     varchar(2) default 'N',
    snmpPoll     varchar(1) default 'N',
    snmpLastSnmpPoll timestamp with time zone,

    CONSTRAINT snmpinterface_pkey primary key (id),
	constraint fk_nodeID2 foreign key (nodeID) references node ON DELETE CASCADE
);

create unique index snmpinterface_nodeid_ifindex_unique_idx on snmpinterface(nodeID, snmpIfIndex);
create index snmpinterface_nodeid_idx on snmpinterface(nodeID);

--########################################################################
--# ipInterface Table - Contains information on interfaces which support
--#                     TCP/IP as well as current status information.
--#                     ipAddr is integer, to support easier filtering.
--#
--# This table contains the following information:
--#
--#  nodeID          : Unique identifier of the node that "owns" this interface
--#  ipAddr          : IP Address associated with this interface
--#  ifIndex	     : SNMP index of interface, used to uniquely identify
--# 		           unnumbered interfaces, or null if there is no mapping to
--#                    snmpInterface table.  Can be -100 if old code added an
--#                    snmpInterface table entry but no SNMP data could be gathered.
--#
--# NOTE: The combination of nodeID, ipAddr, and ifIndex must be unique,
--# and this must be enforced programmatically.
--#
--#  ipHostname      : IP Hostname associated with this interface
--#  isManaged       : Character used as a boolean flag
--#                     'M' - Managed
--#                     'A' - Alias
--#                     'D' - Deleted
--#                     'U' - Unmanaged
--#                     'F' - Forced Unmanaged (via the user interface)
--#                     'N' - Not polled as part of any package
--#                     'X' - Remotely Monitored only
--#  ipStatus        : If interface supports SNMP this field will
--#                    hold a numeric representation of interface's
--#                    operational status (same as 'snmpIfOperStatus'
--#                    field in the snmpInterface table).
--#                      1 = Up, 2 = Down, 3 = Testing
--#  ipLastCapsdPoll : Date and time of last poll by capsd or provisiond
--#  isSnmpPrimary   : Character used as a boolean flag
--#                      'P' - Primary SNMP
--#                      'S' - Secondary SNMP
--#                      'N' - Not eligible (does not support SNMP or
--#                               or has no ifIndex)
--#                     NOTE: 'C' is no longer a valid value for isSnmpPrimary
--#                       this has moved to the snmpinterface table
--#
--########################################################################

create table ipInterface (
    id              INTEGER DEFAULT nextval('opennmsNxtId') NOT NULL,
	nodeID			integer not null,
	ipAddr			text not null,
	ifIndex			integer,
	ipHostname		varchar(256),
	isManaged		char(1),
	ipStatus		integer,
    ipLastCapsdPoll timestamp with time zone,
	isSnmpPrimary   char(1),
	snmpInterfaceId	integer,

	CONSTRAINT ipinterface_pkey PRIMARY KEY (id),
	CONSTRAINT snmpinterface_fkey2 FOREIGN KEY (snmpInterfaceId) REFERENCES snmpInterface (id) ON DELETE SET NULL,
	constraint fk_nodeID1 foreign key (nodeID) references node ON DELETE CASCADE
);

create unique index ipinterface_nodeid_ipaddr_notzero_idx on ipInterface (nodeID, ipAddr) WHERE ipAddr != '0.0.0.0';
create index ipinterface_nodeid_ipaddr_ismanaged_idx on ipInterface (nodeID, ipAddr, isManaged);
create index ipinterface_ipaddr_ismanaged_idx on ipInterface (ipAddr, isManaged);
create index ipinterface_ipaddr_idx on ipInterface (ipAddr);
create index ipinterface_nodeid_ismanaged_idx on ipInterface (ipAddr);
create index ipinterface_nodeid_idx on ipInterface (nodeID);
create index ipinterface_snmpInterfaceId_idx on ipInterface (snmpInterfaceId);

--########################################################################
--# service Table - Contains a name<->number mapping for services
--#                 (e.g., poller packages)
--#
--# This table provides the following information:
--#
--#  serviceID   : Unique integer mapping to service/poller package
--#  serviceName : Name associated with service/poller package
--########################################################################

create table service (
	serviceID		integer default nextval('serviceNxtId') not null,
	serviceName		varchar(255) not null,

	constraint pk_serviceID primary key (serviceID)
);

create unique index service_servicename_key on service (serviceid);

--########################################################################
--# ifServices Table - Contains a mapping of interfaces to services available
--#                    on those interfaces (e.g., FTP, SMTP, DNS, etc.) and
--#                    recent polling status information.
--#
--# This table provides the following information:
--#
--#  nodeID    : Unique integer identifier for node
--#  ipAddr    : IP Address of node's interface
--#  ifIndex   : SNMP ifIndex, if available, null otherwise
--#  serviceID : Unique integer identifier of service/poller package
--#  lastGood  : Date and time of last successful poll by this poller package
--#  lastFail  : Date and time of last failed poll by this poller package
--#  qualifier : Service qualifier.  May be used to distinguish two
--#		         services which have the same serviceID.  For example, in the
--#              case of the HTTP service a qualifier might be the specific
--#              port on which the HTTP server was found.
--#  status    : Flag indicating the status of the service.
--#                'A' - Active
--#                'D' - Deleted
--#                'U' - Unmanaged (per capsd configuration change and CAPSD)
--#                'F' - Forced unmanaged (via user interface)
--#                'N' - Not polled as part of any of the packages that the
--#                      interface belongs to
--#                'X' - service is remotely monitored only
--#  source    : Flag indicating how the service was detected.
--#                'P' - Plugin
--#                'F' - Forced (via CapsdPluginBehavior.conf)
--#  notify    : Flag indicating if this service should be notified on or not
--#                'Y' - to notify
--#                'N' = not to notify
--########################################################################

create table ifServices (
    id				integer default nextval('opennmsNxtId') NOT NULL,
	nodeID			integer not null,
	ipAddr			text not null,
	ifIndex			integer,
	serviceID		integer not null,
	lastGood		timestamp with time zone,
	lastFail		timestamp with time zone,
	qualifier		char(16),
	status         	char(1),
	source			char(1),
	notify          char(1),
	ipInterfaceId	integer not null,

	CONSTRAINT ifservices_pkey PRIMARY KEY (id), 
	CONSTRAINT ipinterface_fkey FOREIGN KEY (ipInterfaceId) REFERENCES ipInterface (id) ON DELETE CASCADE,
	constraint fk_nodeID3 foreign key (nodeID) references node ON DELETE CASCADE,
	constraint fk_serviceID1 foreign key (serviceID) references service ON DELETE CASCADE
);

create unique index ifservices_nodeid_ipaddr_svc_unique on ifservices(nodeID, ipAddr, serviceId);
create index ifservices_nodeid_ipaddr_status on ifservices(nodeID, ipAddr, status);
create index ifservices_nodeid_status on ifservices(nodeid, status);
create index ifservices_nodeid_idx on ifservices(nodeID);
create index ifservices_serviceid_idx on ifservices(serviceID);
create index ifservices_nodeid_serviceid_idx on ifservices(nodeID, serviceID);
create index ifservicves_ipInterfaceId_idx on ifservices(ipInterfaceId);

--##################################################################
--# events Table -- This table provides information on the events
--#                 that are passed into the event subsystem.  It
--#                 contains information defining the event as
--#                 unique, while additional information is stored
--#                 in the eventsDetail table.
--#
--# This table provides the following information:
--#
--#  eventID   		: Unique identifier for the event
--#  eventUei		: Universal Event Identifer (UEI) for this event
--#  eventSnmp		: Contains the eid, eidtext (optionally), specific,
--#			  and generic identifier for the SNMP Trap.  This
--#			  maps directly to the <snmp> element in the
--#			  Event Data Stream DTD.
--#  eventTime		: The <time> element from the Event Data Stream DTD,
--#			  which is the time the event was received by the
--#			  source process.
--#  eventCreateTime 	: Creation time of event in database
--#  eventHost   	: The <host> element from the Event Data Stream DTD
--#  eventSource        : The entity/process which generated the event.
--#  eventSnmphost	: The <snmphost> element from the Event Data Stream DTD
--#  eventDpName	: The dpName of the Dist Poller which received the
--#			  event
--#  eventParms		: The <parms> element from the Event Data Stream DTD
--#  nodeID             : Unique integer identifier for node
--#  ifindex		: The <ifindex> element from the Event Data Stream DTD
--#  ipAddr             : IP Address of node's interface
--#  serviceID          : Unique integer identifier of service/poller package
--#  eventDescr		: Free-form textual description of the event
--#  eventLogmsg	: The log message for the event
--#  eventSeverity	: Severity of event
--#			   1 = Indeterminate
--#			   2 = Cleared (implementation is now in alarms)
--#			   3 = Normal
--#			   4 = Warning
--#			   5 = Minor
--#			   6 = Major
--#			   7 = Critical
--#  eventPathOutage	: Event Path outage information	
--#  eventCorrelation	: The event correlation configured for this event
--#			  (stored as an XML string)
--#  eventSuppressedCount	: The number of times the event was suppressed
--#			  (if event correlation was set for suppression)
--#  eventOperInstruct 	: Operator instruction for event.
--#  eventAutoAction	: Automated Action for event.  Should
--#			  consist of fully-qualfied pathname to
--#			  executable command, with possible variables
--#			  used to reference event-specific data
--#  eventOperAction   	: Operator Action for event.  Should
--#			  consist of fully-qualfied pathname to
--#			  executable command, with possible variables
--#			  used to reference event-specific data
--#  eventOperActionMenuText	: Menu text displayed to Operator, which if
--#			  selected, will invoke action described in
--#			  eventOperAction
--#  eventLoggroup	: Logical group with which to associate event.
--#			  This field provides a means of logically
--#			  grouping related events.
--#  eventNotification  : Notification string.  Should consist of
--#			  a fully-qualfied pathname to an executable
--#			  which invokes the notification software, and
--#			  will likely contain event-specific variables
--#  eventTticket       : Trouble ticket integration string.  Should
--#			  consist of fully-qualfied pathname to
--#			  executable command, with possible variables
--#			  used to reference event-specific data
--#  eventTticketState  : Trouble ticket on/off boolean
--#   				1=on, 0=off
--#  eventForward       : Contains a list of triplets:
--#	  		    Destination,State,Mechanism;Destination,State,Mechanism;
--#			  which reflect the following:
--#			      - State is a boolean flag as to whether the
--#				entry is active or not.  1=on, 0=off.
--#			      - Destination is hostname or IP of system to
--#				forward the event to
--#			      - Method is the means by which it will be
--#				forwarded.  A keyword, e.g., SNMP
--#  eventMouseOverText : Text to be displayed on MouseOver event, if
--#			  the event is displayed in the browser and
--#			  the operator needs additional info.
--#  eventLog		: Flag indicating if the event is to be logged, set
--#			  from the 'dest' attribute on the incoming event
--#                       Y = log, N = do not log
--#  eventDisplay	: Flag indicating if the event is to be displayed, set
--#			  from the 'dest' attribute on the incoming event
--#                       Y = display, N = do not display
--#  eventAckUser	: The user who acknowledged this event.  If
--#			  null, then this event has not been acknowledged.
--#  eventAckTime	: The time this event was acknowledged.
--#  alarmID : If this event is configured for alarmReduction, the alarmId
--#            of the reduced event will set in this column
--#
--##################################################################

create table events (
	eventID			integer not null,
	eventUei		varchar(256) not null,
	nodeID			integer,
	eventTime		timestamp with time zone not null,
	eventHost		varchar(256),
	eventSource		varchar(128) not null,
	ipAddr			text,
	eventDpName		varchar(12) not null,
	eventSnmphost		varchar(256),
	serviceID		integer,
	eventSnmp		varchar(256),
	eventParms		text,
	eventCreateTime		timestamp with time zone not null,
	eventDescr		text,
	eventLoggroup		varchar(32),
	eventLogmsg		text,
	eventSeverity		integer not null,
	eventPathOutage		varchar(1024),
	eventCorrelation	varchar(1024),
	eventSuppressedCount	integer,
	eventOperInstruct	varchar(1024),
	eventAutoAction		varchar(256),
	eventOperAction		varchar(256),
	eventOperActionMenuText	varchar(64),
	eventNotification	varchar(128),
	eventTticket		varchar(128),
	eventTticketState	integer,
	eventForward		varchar(256),
	eventMouseOverText	varchar(64),
	eventLog		char(1) not null,
	eventDisplay		char(1) not null,
    ifIndex             integer,
	eventAckUser		varchar(256),
	eventAckTime		timestamp with time zone,
	alarmID			integer,

	constraint pk_eventID primary key (eventID)
);

create index events_uei_idx on events(eventUei);
create index events_nodeid_idx on events(nodeID);
create index events_ipaddr_idx on events(ipaddr);
create index events_serviceid_idx on events(serviceID);
create index events_time_idx on events(eventTime);
create index events_severity_idx on events(eventSeverity);
create index events_log_idx on events(eventLog);
create index events_display_idx on events(eventDisplay);
create index events_ackuser_idx on events(eventAckUser);
create index events_acktime_idx on events(eventAckTime);
create index events_alarmid_idx on events(alarmID);
create index events_nodeid_display_ackuser on events(nodeid, eventdisplay, eventackuser);

--########################################################################
--#
--# outages table -- This table maintains a record of outage periods for
--#                  given services on specific interfaces.
--#
--# This table provides the following information:
--#
--#  outageID          : Unique integer identifier for the outage
--#  svcLostEventID    : ID of the event that caused the outage. Will be
--#                      a non-null value when a new outage is inserted
--#                      but might be null in case of an opennms upgrade
--#  svcRegainedEventID: ID of the event that cleared the outage
--#  nodeID            : Unique integer identifier for node
--#  ipAddr            : IP Address of node's interface
--#  serviceID         : Unique integer identifier of service/poller package
--#  ifLostService     : Time of lost service event
--#  ifRegainedService : Time of regained service event
--#  suppressTime 	   : Time to suppress the outage
--#  suppressedBy	   : The suppressor
--#
--########################################################################

create table outages (
	outageID		integer not null,
	svcLostEventID		integer,
	svcRegainedEventID	integer,
	nodeID			integer not null,
	ipAddr			text not null,
	serviceID		integer not null,
	ifLostService		timestamp with time zone not null,
	ifRegainedService	timestamp with time zone,
	suppressTime    	timestamp with time zone,
	suppressedBy		varchar(256),
	ifServiceId		INTEGER not null,

	constraint pk_outageID primary key (outageID),
	constraint fk_eventID1 foreign key (svcLostEventID) references events (eventID) ON DELETE CASCADE,
	constraint fk_eventID2 foreign key (svcRegainedEventID) references events (eventID) ON DELETE CASCADE,
	constraint fk_nodeID4 foreign key (nodeID) references node (nodeID) ON DELETE CASCADE,
	constraint fk_serviceID2 foreign key (serviceID) references service (serviceID) ON DELETE CASCADE,
	CONSTRAINT ifServices_fkey1 FOREIGN KEY (nodeId, ipAddr, serviceId) REFERENCES ifServices (nodeId, ipAddr, serviceId) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT ifServices_fkey2 FOREIGN KEY (ifServiceId) REFERENCES ifServices (id) ON DELETE CASCADE
);

create index outages_nodeid_ipaddr_svc_idx on outages(nodeID, ipAddr, serviceId);
create index outages_svclostid_idx on outages(svcLostEventID);
create index outages_svcregainedid_idx on outages(svcRegainedEventID);
create index outages_nodeid_idx on outages(nodeID);
create index outages_serviceid_idx on outages(serviceID);
create index outages_ipaddr_idx on outages(ipaddr);
create index outages_regainedservice_idx on outages(ifRegainedService);
create index outages_ifServivceId_idx on outages(ifServiceId);
create unique index one_outstanding_outage_per_service_idx on outages (ifserviceid) where ifregainedservice is null;

--########################################################################
--#
--# vulnerabilities table -- This table maintains a record of vulnerabilites
--#                          that have been detected on target IP addresses.
--#
--# This table provides the following information:
--#
--#  vulnerabilityID   : Unique integer identifier for the outage
--#  nodeID            : Unique integer identifier for node
--#  ipAddr            : IP Address of node's interface
--#  serviceID         : Unique integer identifier of service/poller package
--#
--#  creationTime      : Initial creation time of the vulnerability
--#  lastAttemptTime   : Last time that an attempt was made to scan for
--#                      this vulnerability
--#  lastScanTime      : Most recent successful scan time
--#  resolvedTime      : Time after which the vulnerability was no longer
--#                      detected
--#
--#  severity          : Severity of the vulnerability (identical to event
--#                      severities
--#  pluginID          : ID number of the plugin that produced the vulnerability
--#  pluginSubID       : Specific vulnerability type generated by the plugin
--#  logmsg            : Terse description of vulnerability (usually
--#                      the plugin name plus short description)
--#  descr             : Verbose description of vulnerability
--#  port              : Port that the vulnerability affects
--#  protocol          : Network protocol of the attack (TCP, UDP, ICMP)
--#
--########################################################################

create table vulnerabilities (
	vulnerabilityID		integer not null,
	nodeID			integer,
	ipAddr			text,
	serviceID		integer,
	creationTime		timestamp with time zone not null,
	lastAttemptTime		timestamp with time zone not null,
	lastScanTime		timestamp with time zone not null,
	resolvedTime		timestamp with time zone,
	severity		integer not null,
	pluginID		integer not null,
	pluginSubID             integer not null,
	logmsg                  text,
	descr			text,
	port			integer,
	protocol		varchar(32),
	cveEntry		varchar(255),

	constraint pk_vulnerabilityID primary key (vulnerabilityID)
);

create index vulnerabilities_nodeid_idx on vulnerabilities(nodeID);
create index vulnerabilities_ipaddr_idx on vulnerabilities(ipAddr);
create index vulnerabilities_severity_idx on vulnerabilities(severity);
create index vulnerabilities_port_idx on vulnerabilities(port);
create index vulnerabilities_protocol_idx on vulnerabilities(protocol);

--########################################################################
--#
--# vulnPlugins table -- This table contains a list of information about
--#                      Nessus plugins that are in use by the nessusd
--#                      daemons that are being used by vulnscand.
--#
--# This table provides the following information:
--#
--#  pluginID          : Plugin ID number (from Nessus)
--#  pluginSubID       : Specific vulnerability type within the plugin
--#  name              : Short name of the plugin
--#  category          : Category of the plugin's behavior (scanner,
--#                      attack, etc)
--#  copyright         : Copyright notice for the plugin
--#  descr             : Verbose description of vulnerability
--#  summary           : Short description of plugin behavior
--#  family            : User-comprehensible type of attack (CGI abuses,
--#                      Backdoors, etc)
--#  version           : Version of the plugin code
--#  cveEntry          : CVE entry associated with the vulnerability
--#                      that this plugin tests
--#  md5               : 128-bit hex MD5 checksum of the plugin that
--#                      can be used to detect changes in the plugin code
--#
--########################################################################

create table vulnPlugins (
        pluginID                integer not null,
        pluginSubID             integer not null,
        name                    varchar(128),
        category                varchar(32),
        copyright               varchar(128),
        descr                   text,
        summary                 varchar(256),
        family                  varchar(32),
        version                 varchar(32),
        cveEntry                varchar(255),
        md5                     varchar(32)
);

--#  This constraint not understood installer
--#        CONSTRAINT pk_vulnplugins PRIMARY KEY (pluginID,pluginSubID));
--#

create unique index vulnplugins_plugin_idx on vulnPlugins(pluginID, pluginSubID);

--########################################################################
--# notification table - Contains information on acknowleged and outstanding
--#                      pages listed by user/groups
--#
--# This table contains the following fields:
--#
--# textMsg     : The message being sent in the page.
--# numericMsg  : The message being sent to a numeric pager
--# notifyID    : The primary key of this row, populated with the value from
--#               the notifyNxtId sequence.
--# pageTime    : A timestamp of when the page was originally sent.
--# respondTime : A timestamp of when the page was acknowleged. A null in this
--#               field means that the page has not been answered yet.
--# answeredBy  : The user id of the user that answered the page, set the same
--#               for all rows with the same groupId field.
--# nodeId      : The id of the node that has the problem
--# interfaceId : The id of the interface on the node that has the problem
--# serviceID   : The id of the service on the interface that has the problem
--# eventID     : The primary key of the event that spawned the notification
--# eventUEI    : The uei of the event that spawned the notification, placed here
--#               for speed of lookup as notifications are processed.
--#
--########################################################################

create table notifications (
       textMsg      text not null,
       subject      text,
       numericMsg   varchar(256),
       notifyID	    integer not null,
       pageTime     timestamp with time zone,
       respondTime  timestamp with time zone,
       answeredBy   varchar(256),
       nodeID	    integer,
       interfaceID  varchar(16),
       serviceID    integer,
       queueID		varchar(256),
       eventID      integer,
       eventUEI     varchar(256) not null,
       notifConfigName	varchar(63),

       constraint pk_notifyID primary key (notifyID),
       constraint fk_nodeID7 foreign key (nodeID) references node (nodeID) ON DELETE CASCADE,
       constraint fk_eventID3 foreign key (eventID) references events (eventID) ON DELETE CASCADE
);

create index notifications_nodeid_idx on notifications(nodeid);
create index notifications_ipaddr_idx on notifications(interfaceID);
create index notifications_serviceid_idx on notifications(serviceID);
create index notifications_eventid_idx on notifications(eventID);
create index notifications_respondtime_idx on notifications(respondTime);
create index notifications_answeredby_idx on notifications(answeredBy);
create index notifications_eventuei_idx on notifications (eventuei);

--########################################################################
--#
--# This table contains the following fields:
--# id			: ID column for the table
--# userID      : The user id of the person being paged, from the users.xml
--#               file.
--# notifyID    : The index of the row from the notification table.
--# notifyTime	: The timestamp of when the notification was sent
--# media       : A string describing the type of contact being made, ie text
--#               page, numeric page, email, etc...
--# contactInfo : A field for storing the information used to contact the user,
--#               e.g. an email address, the phone number and pin of the pager...
--# autonotify	: A character to determine how auto acknowledge is handled for
--#               this entry
--#
--########################################################################

create table usersNotified (
		id				integer not null, 
        userID          varchar(256) not null,
        notifyID        integer,
        notifyTime      timestamp with time zone,
        media           varchar(32),
        contactinfo     varchar(64),
        autonotify      char(1),

	constraint pk_userNotificationID primary key (id),
	constraint fk_notifID2 foreign key (notifyID) references notifications (notifyID) ON DELETE CASCADE
);

create index userid_notifyid_idx on usersNotified(userID, notifyID);

--#################################
--# This table contains memos used by alarms to represent StickyMemos and Journal / ReductionKeyMemos
create table memos (
  id integer NOT NULL,
  created timestamp with time zone,
  updated timestamp with time zone,
  author character varying(256),
  body text,
  reductionkey character varying(256),
  type character varying(64),
  CONSTRAINT memos_pkey PRIMARY KEY (id)
);
--########################################################################
--#
--# This table contains the following fields:
--# alarmID     : The id created from the alarmsNxtId sequence.
--# eventUei    : A reference to the eventUei that created this alarm.
--# nodeID      : A reference to the node represented by this alarm.
--# ipAddr      : IP Address of node's interface
--# serviceID   : A reference to the service represented by the alarm.
--# reductionKey: Used with nodeID and serviceID to match an event and
--#               increment the counter column.  Set by configuring the
--#               optional alarm-data elment in the eventConf.xml file.
--# alarmType   : Customizable column designed for use in automations and
--#               can be set in the eventConf.xml file by configuring the
--#               optional alarm-data element.
--# counter     : Incremented by the AlarmWriter instead of inserting
--#               a new row when matched node, service, and reductionKey
--# severity    : Severity of the Alarm... Initially set by the event
--#               can be changed with SQL update.
--# lastEventID : A reference to the event table with the ID of the last
--#               matching event (typically node, service, reductionkey)
--# firstEventTime: timestamp of the first event matching this alarm
--# lastEventTime: timestamp of the last event matching this alarm
--# description : description from the event
--# logMsg      : the logmsg from the event
--# ifIndex      : the ifindex from the event
--# operInstruct: the operator instructions from the event
--# tticketID   : helpdesk integration field
--# tticketState: helpdesk integration field
--# mouseOverTest: flyOverText for the webUI
--# suppressedUntil: used to suppress display an alarm until
--#                : timestamp time is reached
--# suppressedUser : user that suppressed alarm
--# suppressedTime : time the alarm was suppressed
--# alarmAckUser : user that acknowledged the alarm
--# alarmAckTime : time user Ack'd the alarm
--# stickymemo  : reference to the memo table
--########################################################################

create table alarms (
    alarmID                 INTEGER, CONSTRAINT pk_alarmID PRIMARY KEY (alarmID),
    eventUei                VARCHAR(256) NOT NULL,
    dpName                  VARCHAR(12) NOT NULL,
    nodeID                  INTEGER, CONSTRAINT fk_alarms_nodeid FOREIGN KEY (nodeID) REFERENCES node (nodeID) ON DELETE CASCADE,
    ipaddr                  VARCHAR(39),
    serviceID               INTEGER,
    reductionKey            VARCHAR(256),
    alarmType               INTEGER,
    counter                 INTEGER NOT NULL,
    severity                INTEGER NOT NULL,
    lastEventID             INTEGER, CONSTRAINT fk_eventIDak2 FOREIGN KEY (lastEventID)  REFERENCES events (eventID) ON DELETE CASCADE,
    firstEventTime          timestamp with time zone,
    lastEventTime           timestamp with time zone,
    firstAutomationTime     timestamp with time zone,
    lastAutomationTime      timestamp with time zone,
    description             text,
    logMsg                  text,
    operInstruct            VARCHAR(1024),
    tticketID               VARCHAR(128),
    tticketState            INTEGER,
    mouseOverText           VARCHAR(64),
    suppressedUntil         timestamp with time zone,
    suppressedUser          VARCHAR(256),
    suppressedTime          timestamp with time zone,
    alarmAckUser            VARCHAR(256),
    alarmAckTime            timestamp with time zone,
    managedObjectInstance   VARCHAR(512),
    managedObjectType       VARCHAR(512),
    applicationDN           VARCHAR(512),
    ossPrimaryKey           VARCHAR(512),
    x733AlarmType           VARCHAR(31),
    x733ProbableCause       INTEGER default 0 not null,
    qosAlarmState           VARCHAR(31),
    ifIndex                 INTEGER,
    clearKey                VARCHAR(256),
    eventParms              text,
    stickymemo              INTEGER, CONSTRAINT fk_stickyMemo FOREIGN KEY (stickymemo) REFERENCES memos (id) ON DELETE CASCADE
);

CREATE INDEX alarm_uei_idx ON alarms(eventUei);
CREATE INDEX alarm_nodeid_idx ON alarms(nodeID);
CREATE UNIQUE INDEX alarm_reductionkey_idx ON alarms(reductionKey);
CREATE INDEX alarm_clearkey_idx ON alarms(clearKey);
CREATE INDEX alarm_reduction2_idx ON alarms(alarmID, eventUei, dpName, nodeID, serviceID, reductionKey);
CREATE INDEX alarm_app_dn ON alarms(applicationDN);
CREATE INDEX alarm_oss_primary_key ON alarms(ossPrimaryKey);
CREATE INDEX alarm_eventid_idx ON alarms(lastEventID);
CREATE INDEX alarm_lasteventtime_idx on alarms(lasteventtime);
CREATE INDEX alarm_firstautomationtime_idx on alarms(firstautomationtime);
CREATE INDEX alarm_lastautomationtime_idx on alarms(lastautomationtime);

--########################################################################
--#
--# Use this table to add additional custom data about an alarm... somewhat
--# usefull with automations and will be viewable/editable in the alarm
--# details WebUI page.
--#
--# This table contains the following fields:
--# alarmID     : The id created from the alarmsNxtId sequence.
--# attribute   : The custom attribute name
--# attributeValue : The custom attribute value
--########################################################################

CREATE TABLE alarm_attributes (
    alarmID         INTEGER, CONSTRAINT fk_alarmID1 FOREIGN KEY (alarmID) REFERENCES alarms (alarmID) ON DELETE CASCADE,
    attributeName   VARCHAR(63),
    attributeValue  VARCHAR(255)
);

CREATE INDEX alarm_attributes_idx ON alarm_attributes(alarmID);
CREATE UNIQUE INDEX alarm_attributes_aan_idx ON alarm_attributes(alarmID, attributeName);

--# This constraint not understood by installer
--#        CONSTRAINT pk_usersNotified PRIMARY KEY (userID,notifyID) );
--#
--########################################################################
--# asset table - Contains inventory and other user-entered information
--#                     for nodes
--#
--# This table contains the following fields:
--#
--# nodeID           : The node id for the node this asset information belongs.
--# category         : A broad idea of what this asset does (examples are
--#                    desktop, printer, server, infrastructure, etc.).
--# manufacturer     : Name of the manufacturer of this asset.
--# vendor           : Vendor from whom this asset was purchased.
--# modelNumber      : The model number of this asset.
--# serialNumber     : The serial number of this asset.
--# description      : A free-form description.
--# circuitId        : The electrical/network circuit this asset connects to.
--# assetNumber      : A business-specified asset number.
--# operatingSystem  : The operating system, if any.
--# rack             : For servers, the rack it is installed in.
--# slot             : For servers, the slot in the rack it is installed in.
--# port             : For servers, the port in the slot it is installed in.
--# region           : A broad geographical or organizational area.
--# division         : A broad geographical or organizational area.
--# department       : The department this asset belongs to.
--# address1         : Address of geographical location of asset, line 1.
--# address2         : Address of geographical location of asset, line 2.
--# city             : The city where this asset resides.
--# state            : The state where this asset resides.
--# zip              : The zip code where this asset resides.
--# building         : The building where this asset resides.
--# floor            : The floor of the building where this asset resides.
--# room             : The room where this asset resides.
--# vendorPhone      : A contact number for the vendor.
--# vendorFax        : A fax number for the vendor.
--# vendorAssetNumber: The vendor asset number.
--# username		 : A Username to access the node
--# password		 : The password to access the node
--# enable			 : The privilege password to access the node
--# autoenable		 : If username has privileged access
--#                    - 'A' autoenable true
--# connection		 : Connection protocol used to access the node (telnet, ssh, rsh, ...)
--# userCreated      : The username who created this record.
--# userLastModified : The last user who modified this record.
--# lastModifiedDate : The last time this record was modified.
--# dateInstalled    : The date the asset was installed.
--# lease            : The lease number of this asset.
--# leaseExpires     : The date the lease expires for this asset.
--# supportPhone     : A support phone number for this asset.
--# maintContract    : The maintenance contract number for this asset.
--#
--########################################################################

create table assets (
        id              INTEGER DEFAULT nextval('opennmsNxtId') NOT NULL,
        nodeID          integer,
        category        text not null,
        manufacturer    text,
        vendor          text,
        modelNumber     text,
        serialNumber    text,
        description     text,
        circuitId       text,
        assetNumber     text,
        operatingSystem text,
        rack            text,
        slot            text,
        port            text,
        region          text,
        division        text,
        department      text,
        address1        text,
        address2        text,
        city            text,
        state           text,
        zip             text,
        country         text,
        building        text,
        floor           text,
        room            text,
        vendorPhone     text,
        vendorFax       text,
        vendorAssetNumber text,
        username		text,
        password		text,
        enable			text,
        autoenable		char(1),
        connection		varchar(32),
        userLastModified char(20) not null,
        lastModifiedDate timestamp with time zone not null,
        dateInstalled   varchar(64),
        lease           text,
        leaseExpires    varchar(64),
        supportPhone    text,
        maintContract   text,
        maintContractExpires varchar(64),
        displayCategory   text,
        notifyCategory   text,
        pollerCategory   text,
        thresholdCategory   text,
        comment         text,
        managedObjectInstance text,
        managedObjectType text,
        cpu		text,
        ram		text,
        storagectrl	text,
        hdd1		text,
        hdd2		text,
        hdd3		text,
        hdd4		text,
        hdd5		text,
        hdd6		text,
        numpowersupplies		varchar(1),
        inputpower		varchar(6),
        additionalhardware		text,
        admin		text,
        snmpcommunity		varchar(32),
        rackunitheight		varchar(2),
        longitude		float,
        latitude		float,
        vmwaremanagedobjectid	text,
        vmwaremanagedentitytype	text,
        vmwaremanagementserver	text,
        vmwaretopologyinfo	text,
        vmwarestate	text,

    constraint pk_assetID primary key (id),
	constraint fk_nodeID5 foreign key (nodeID) references node ON DELETE CASCADE
);

create index assets_nodeid_idx on assets(nodeid);
CREATE INDEX assets_an_idx ON assets(assetNumber);

--########################################################################
--# categories table - Contains list of categories
--#                     for nodes, interfaces, and services
--#
--# This table contains the following fields:
--#
--# id           : The category id
--# name         : Textual name of a category
--# description  : Descriptive text about a category.
--########################################################################

create table categories (
		categoryId			integer,
		categoryName			text not null,
		categoryDescription	varchar(256),

	constraint category_pkey primary key (categoryId)
);

CREATE UNIQUE INDEX category_idx ON categories(categoryName);

--##################################################################
--# The following command adds an initial set of categories if there
--# are no categories in the category table
--##################################################################
--# criteria: SELECT count(*) = 0 from categories
insert into categories values (nextVal('catNxtId'), 'Routers', null);
--# criteria: SELECT count(*) = 0 from categories
insert into categories values (nextVal('catNxtId'), 'Switches', null);
--# criteria: SELECT count(*) = 0 from categories
insert into categories values (nextVal('catNxtId'), 'Servers', null);
--# criteria: SELECT count(*) = 0 from categories
insert into categories values (nextVal('catNxtId'), 'Production', null);
--# criteria: SELECT count(*) = 0 from categories
insert into categories values (nextVal('catNxtId'), 'Test', null);
--# criteria: SELECT count(*) = 0 from categories
insert into categories values (nextVal('catNxtId'), 'Development', null);

--########################################################################
--# category_node table - Many-to-Many mapping table of categories to nodes
--#
--# This table contains the following fields:
--#
--# categoryid   : The category id from category table
--# nodeID       : The node id from the node table.
--########################################################################

create table category_node (
                categoryId              integer,
                nodeId                  integer,

                constraint categoryid_fkey1 foreign key (categoryId) references categories (categoryId) ON DELETE CASCADE,
                constraint nodeid_fkey1 foreign key (nodeId) references node ON DELETE CASCADE
);

CREATE INDEX catid_idx on category_node(categoryId);
CREATE INDEX catnode_idx on category_node(nodeId);
CREATE UNIQUE INDEX catenode_unique_idx on category_node(categoryId, nodeId);

--########################################################################
--# requisitioned_categories table - Many-to-Many mapping table of
--# requisition categories to nodes
--#
--# This table contains the following fields:
--#
--# id           : The ID of the association
--# categoryId   : The category ID from categories table
--# nodeId       : The node ID from the node table.
--########################################################################

create table requisitioned_categories (
                id                      integer default nextval('opennmsNxtId') not null,
                categoryId              integer not null,
                nodeId                  integer not null,

                constraint requisitioned_nodeid_fkey foreign key (nodeId) references node ON DELETE CASCADE,
                constraint requisitioned_categoryid_fkey foreign key (categoryId) references categories (categoryId) ON DELETE CASCADE
);

CREATE UNIQUE INDEX requisitioned_category_node_unique_idx on requisitioned_categories(nodeId, categoryId);

--########################################################################
--# pathOutage Table - Contains the critical path IP address and service
--#                    associated with each node for suppressing nodeDown
--#                    notifications
--#
--# This table contains the following information:
--#
--#  nodeID                  : Unique identifier of the node
--#  criticalPathIp          : IP Address associated with the critical element in
--#                            the path between the OpenNMS server and the node
--#  criticalPathServiceName : the service to test on the critical path IP
--#                            address (Assume ICMP in Phase I implementation)
--#
--# NOTE: The nodeID must be unique
--#
--########################################################################

create table pathOutage (
	nodeID			integer,
	criticalPathIp		text not null,
	criticalPathServiceName	varchar(255),

	constraint fk_nodeID8 foreign key (nodeID) references node ON DELETE CASCADE
);

create unique index pathoutage_nodeid on pathOutage(nodeID);
create index pathoutage_criticalpathip on pathOutage(criticalPathIp);
create index pathoutage_criticalpathservicename_idx on pathOutage(criticalPathServiceName);

--########################################################################
--# demandPolls Table - contains a list of requested polls
--#
--# This table contains the following information:
--#
--#  id                      : Unique identifier of the demand poll
--#  requestTime             : the time the user requested the poll
--#  user                    : the user that requested the poll
--#  description             : ?
--#
--########################################################################
create table demandPolls (
	id			integer,
	requestTime	timestamp with time zone,
	username	varchar(32),
	description varchar(128),
	
	constraint demandpoll_pkey primary key (id)
	
);

create index demandpoll_request_time on demandPolls(requestTime);
	
--########################################################################
--# pollResults Table - contains a list of requested polls
--#
--# This table contains the following information:
--#
--#  id			: unique identifier of the demand poll
--#  pollId		: unique identifier of this specific service poll
--#  nodeId		: node id of the polled service
--#  ipAddr		: ip address of the polled service
--#  ifIndex		: ifIndex of the polled service's interface
--#  serviceId		: serviceid of the polled service
--#  statusCode		: status code of the pollstatus returned by the monitor
--#  statusName		: status name of the pollstaus returnd by the monitor
--#  reason		: the reason of the pollstatus returned by the monitor
--#
--########################################################################
create table pollResults (
	id			integer,
	pollId      integer,
	nodeId		integer,
	ipAddr		text,
	ifIndex		integer,
	serviceId	integer,
	statusCode	integer,
	statusName	varchar(32),
	reason		varchar(128),
	
	constraint pollresult_pkey primary key (id),
	constraint fk_demandPollId foreign key (pollID) references demandPolls (id) ON DELETE CASCADE

);

create index pollresults_poll_id on pollResults(pollId);
create index pollresults_service on pollResults(nodeId, ipAddr, ifIndex, serviceId);

--#####################################################
--# locaation_monitors Table - contains a list of monitors in remote
--#                            locations
--#
--# This table contains the following information:
--#
--#  id          : surrogate key generated by sequence 
--#  name        : name of the location monitor
--#  definitionName : used to reference XML configuration
--#
--#
--#####################################################

CREATE TABLE location_monitors (
    id INTEGER,
    status VARCHAR(31) NOT NULL,
    lastCheckInTime timestamp with time zone,
    definitionName VARCHAR(31) NOT NULL,
    
    CONSTRAINT location_monitors_pkey PRIMARY KEY (id)
);

CREATE TABLE location_monitor_details (
    locationMonitorId INTEGER NOT NULL,
    property VARCHAR(255) NOT NULL,
    propertyValue VARCHAR(255),
    
    CONSTRAINT location_monitor_fkey1 FOREIGN KEY (locationMonitorId) REFERENCES location_monitors (id) ON DELETE CASCADE
);

create index location_monitor_details_id on location_monitor_details(locationMonitorId);
create unique index location_monitor_details_id_property on location_monitor_details(locationMonitorId, property);


--#############################################################################
--# location_specific_status_changes Table - contains a list status
--#      changed reported for a service by a monitor in a remote
--#      location.
--#
--# This table contains the following information:
--#
--#  id                : surrogate key generated by a sequence
--#  locationMonitorId : foreign key referencing a specific
--#                      monitor in a remote location
--#  serviceId         : foreign key referencing a specific monitored services
--#  statusTime        : time of reported status from remote location monitor 
--#  reason            : description of status change
--#  responseTime      : data for latency reporting
--#
--#############################################################################
CREATE TABLE location_specific_status_changes (
    id INTEGER,
    locationMonitorId INTEGER NOT NULL,
    ifServiceId INTEGER NOT NULL,
    statusCode INTEGER NOT NULL,
    statusTime timestamp with time zone NOT NULL,
    statusReason VARCHAR(255),
    responseTime DOUBLE PRECISION,

    CONSTRAINT location_specific_status_changes_pkey PRIMARY KEY (id),
    CONSTRAINT location_monitor_fkey2 FOREIGN KEY (locationMonitorId) REFERENCES location_monitors (id) ON DELETE CASCADE,
    CONSTRAINT ifservices_fkey4 FOREIGN KEY (ifServiceId) REFERENCES ifservices (id) ON DELETE CASCADE
);

create index location_specific_status_changes_ifserviceid on location_specific_status_changes(ifserviceid);
create index location_specific_status_changes_locationmonitorid on location_specific_status_changes(locationmonitorid);
create index location_specific_status_changes_locationmonitorid_ifserviceid on location_specific_status_changes(locationmonitorid, ifserviceid);
create index location_specific_status_changes_locationmonitorid_loc_if_time on location_specific_status_changes(locationmonitorid, ifserviceid, statustime);
create index location_specific_status_changes_statustime on location_specific_status_changes(statustime);



--########################################################################
--# applications table - Contains list of applications for services
--#
--# This table contains the following fields:
--#
--# id           : The application id
--# name         : Textual name of a application
--########################################################################

create table applications (
	id			integer,
	name			varchar(32) not null,

	constraint applications_pkey primary key (id)
);

CREATE UNIQUE INDEX applications_name_idx ON applications(name);

--########################################################################
--# application_service_map table - Many-to-Many mapping table of
--# applications to ifServices
--#
--# This table contains the following fields:
--#
--# appId           : The application id from applications table
--# ifServiceId     : The id from the ifServices table.
--########################################################################

create table application_service_map (
	appId		integer,
	ifServiceId	integer,

	constraint applicationid_fkey1 foreign key (appId) references applications (id) ON DELETE CASCADE,
	constraint ifservices_fkey3 foreign key (ifServiceId) references ifServices (id) ON DELETE CASCADE
);

CREATE INDEX appid_idx on application_service_map(appid);
CREATE INDEX ifserviceid_idx on application_service_map(ifserviceid);
CREATE UNIQUE INDEX appid_ifserviceid_idex on application_service_map(appid,ifserviceid);


--##################################################################
--# The following command adds the initial loopback poller entry to
--# the 'distPoller' table.
--##################################################################
--# criteria: SELECT count(*) = 0 from distPoller where dpName = 'localhost'
insert into distPoller (dpName, dpIP, dpComment, dpDiscLimit, dpLastNodePull, dpLastEventPull, dpLastPackagePush, dpAdminState, dpRunState) values ('localhost', '127.0.0.1', 'This is the default poller.', 0.10, null, null, null, 1, 1);

--########################################################################
--#
--# next are Italian Adventures 2 specific tables
--# author rssntn67@yahoo.it
--#
--# 10/08/04
--# creato il file e le tabelle
--# rev. rssntn67@yahoo.it
--#
--# 18/08/04 
--# eliminato createtime dalle tabelle
--# sufficiente il createtime della tabella node
--#
--# 11/07/05
--# modificata la tabella stpnode aggiunto campo vlanname
--# definita primary key
--# per la tabella atinterface, 
--# Modified: 2007-01-09
--# Note: Added vlan table, Modified Stpnode Table
--#
--#
--########################################################################

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
--#                      'K' - Unknown
--#  sourceNodeid      : The nodeid from which information have been retrivied.
--#  ifindex           : The SNMP ifindex on which this info was recorded.
--#  lastPollTime    : The last time when this information was active
--#
--########################################################################

create table atinterface (
    id			integer default nextval('opennmsNxtId') not null,
	nodeid		integer not null,
	ipAddr		text not null,
	atPhysAddr	varchar(32) not null,
	status		char(1) not null,
	sourceNodeid	integer not null,
	ifindex		integer not null,
	lastPollTime	timestamp not null,
    constraint pk_atinterface primary key (nodeid,ipAddr,atPhysAddr),
	constraint fk_ia_nodeID1 foreign key (nodeid) references node on delete cascade
);



create index atinterface_nodeid_idx on atinterface(nodeid);
create index atinterface_node_ipaddr_idx on atinterface(nodeid,ipaddr);
create index atinterface_atphysaddr_idx on atinterface(atphysaddr);

--########################################################################
--#
--# vlan table  --   This table maintains a record of generic vlan table
--#					
--# This table provides the following information:
--#
--#  nodeid   	              : Unique integer identifier of the node
--#  vlanid                   : The vlan identifier to be referred to in a unique fashion.
--#  vlanname                 : the name the vlan 
--#  vlantype           	  : Indicates what type of vlan is this:
--#						        '1' ethernet
--#						        '2' FDDI
--#						        '3' TokenRing
--#                             '4' FDDINet
--#                             '5' TRNet
--#                             '6' Deprecated
--#  vlanstatus               : An indication of what is the Vlan Status: 
--#						        '1' operational
--#						        '2' suspendid
--#						        '3' mtuTooBigForDevice
--#						        '4' mtuTooBigForTrunk
--#  status            : Flag indicating the status of the entry.
--#                      'A' - Active
--#                      'N' - Not Active
--#                      'D' - Deleted
--#                      'K' - Unknown
--#  lastPollTime             : The last time when this information was retrived
--#
--########################################################################

create table vlan (
    id			integer default nextval('opennmsNxtId') not null,
    nodeid		 integer not null,
    vlanid	     integer not null,
    vlanname     varchar(64) not null,
    vlantype     integer,
    vlanstatus   integer,
    status		 char(1) not null,
    lastPollTime timestamp not null,
	constraint pk_vlan primary key (nodeid,vlanid),
	constraint fk_ia_nodeID8 foreign key (nodeid) references node on delete cascade
);

create unique index vlan_id_key on vlan(id);
create index vlan_vlanname_idx on vlan(vlanname);


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
--#  baseType		: Indicates what type of bridging this bridge can
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
--#  status            : Flag indicating the status of the entry.
--#                      'A' - Active
--#                      'N' - Not Active
--#                      'D' - Deleted
--#                      'K' - Unknown
--#  lastPollTime             : The last time when this information was retrived
--#  baseVlan                 : Unique integer identifier VLAN for which this info is valid
--#  baseVlanName             : VLAN name
--#
--########################################################################

create table stpnode (
    id			integer default nextval('opennmsNxtId') not null,
    nodeid		     integer not null,
    baseBridgeAddress	     varchar(12) not null,
    baseNumPorts             integer,
    basetype                 integer,
    stpProtocolSpecification integer,
    stpPriority              integer,
    stpdesignatedroot        varchar(16),
    stprootcost              integer,
    stprootport              integer,
    status		     char(1) not null,
    lastPollTime             timestamp not null,
    basevlan                 integer not null,
    basevlanname			 varchar(32),
    constraint pk_stpnode primary key (nodeid,basevlan),
	constraint fk_ia_nodeID2 foreign key (nodeid) references node on delete cascade
);

create unique index stpnode_id_key on stpnode(id);
create index stpnode_nodeid_idx on stpnode(nodeid);
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
--#  status            : Flag indicating the status of the entry.
--#                      'A' - Active
--#                      'N' - Not Active
--#                      'D' - Deleted
--#                      'K' - Unknown
--#  lastPollTime          : The last time when this information was retrived
--#  stpVlan               : Unique integer identifier VLAN for which this info is valid
--#
--########################################################################

create table stpinterface (
    id			integer default nextval('opennmsNxtId') not null,
    nodeid	            integer not null,
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
    stpvlan                 integer not null,

    constraint pk_stpinterface primary key (nodeid,bridgeport,stpvlan),
    constraint fk_ia_nodeID3 foreign key (nodeid) references node on delete cascade
);

create unique index stpinterface_id_key on stpinterface(id);
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
--#                      'K' - Unknown
--#  lastPollTime      : The last time when this information was retrived
--#
--########################################################################

create table iprouteinterface (
    id			integer default nextval('opennmsNxtId') not null,
    nodeid		    integer not null,
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
    lastPollTime            timestamp not null,

    constraint pk_iprouteinterface primary key (nodeid,routedest),
    constraint fk_ia_nodeID4 foreign key (nodeid) references node on delete cascade
);

create unique index iprouteinterface_id_key on iprouteinterface(id);
create index iprouteinterface_nodeid_idx on iprouteinterface(nodeid);
create index iprouteinterface_node_ifdex_idx on iprouteinterface(nodeid,routeifindex);
create index iprouteinterface_rnh_idx on iprouteinterface(routenexthop);

--########################################################################
--#
--# dataLinkInterface table -- This table maintains a record of data link info 
--#                            among  the interfaces. 
--#
--# This table provides the following information:
--#
--#  nodeid            : Unique integer identifier for the linked node 
--#  IfIndex           : SNMP index of interface connected to the link on the node, 
--# 		             is -1 if it doesn't support SNMP.
--#  nodeparentid      : Unique integer identifier for linking node
--#  parentIfIndex     : SNMP index of interface linked on the parent node.
--#  status            : Flag indicating the status of the entry.
--#                      'A' - Active
--#                      'N' - Not Active
--#                      'D' - Deleted
--#                      'U' - Unknown
--#                      'G' - Good
--#                      'B' - Bad
--#                      'X' - Admin Down
--#  protocol          : the protocol used to discover the link (bridge,iproute,isis,ospf,cdp,lldp)  
--#  linkTypeId        : An Integer (corresponding at iftype for cables links) indicating the type  
--#  lastPollTime      : The last time when this information was retrived
--#  source            : The source of the data link.  Defaults to 'linkd', but can be different
--#                      when created from the ReST interface.
--#
--########################################################################

create table datalinkinterface (
    id               integer default nextval('opennmsNxtId') not null,
    nodeid           integer not null,
    ifindex          integer not null,
    nodeparentid     integer not null,
    parentIfIndex    integer not null,
    status           char(1) not null,
    protocol         varchar(31),
    linkTypeId       integer,
    lastPollTime     timestamp not null,
    source           varchar(64) not null default 'linkd',

    constraint pk_datalinkinterface primary key (id),
    constraint fk_ia_nodeID5 foreign key (nodeid) references node on delete cascade,
    constraint fk_ia_nodeID6 foreign key (nodeparentid) references node (nodeid) ON DELETE CASCADE
);

create index dlint_id_idx on datalinkinterface(id);
create index dlint_node_idx on datalinkinterface(nodeid);
create index dlint_nodeparent_idx on datalinkinterface(nodeparentid);
create index dlint_nodeparent_paifindex_idx on datalinkinterface(nodeparentid,parentifindex);

--########################################################################
--#
--# linkState table -- This table maintains the state of the link. 
--#
--# This table provides the following information:
--#
--#  nodeid            : Unique integer identifier for the linked node 
--#  IfIndex           : SNMP index of interface connected to the link on the node, 
--#                      is -1 if it doesn't support SNMP.
--#  nodeparentid      : Unique integer identifier for linking node
--#  parentIfIndex     : SNMP index of interface linked on the parent node.
--#  status            : Flag indicating the status of the entry.
--#                      'A' - Active
--#                      'N' - Not Active
--#                      'D' - Deleted
--#                      'U' - Unknown
--#                      'G' - Good
--#                      'B' - Bad
--#                      'X' - Admin Down
--#  linkTypeId        : An Integer (corresponding at iftype for cables links) indicating the type  
--#  lastPollTime      : The last time when this information was retrived
--#
--########################################################################

create table linkstate (
    id                      integer default nextval('opennmsNxtId') not null,
    datalinkinterfaceid     integer not null, 
    linkstate               varchar(30) default 'LINK_UP' not null,

    constraint pk_linkstate primary key (id),
    constraint fk_linkstate_datalinkinterface_id foreign key (datalinkinterfaceid) references datalinkinterface (id) on delete cascade
);

create unique index linkstate_datalinkinterfaceid_index on linkstate (datalinkinterfaceid);

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
        name 	varchar(30) not null,
        createtime   timestamp not null,
	    lastpolltime   timestamp not null,
        pathtofile varchar(256) not null,
	    status char(1) not null,

		constraint fk_ia_nodeID7 foreign key (nodeID) references node on delete cascade
        );

create index inventory_nodeid_name_idx on inventory(nodeid,name);
create index inventory_nodeid_idx on inventory(nodeid);
create index inventory_lastpolltime_idx on inventory(lastpolltime);
create index inventory_status_idx on inventory(status);

--########################################################################
--#
--# map table     -- This table maintains a record of map definede in opennms
--#					
--# This table provides the following information:
--#
--#  mapId             : Unique integer identifier of the map
--#  mapName           : Identifier of the map
--#  mapBackGround     : bakground image assocated with map
--#  mapOwner          : user who has the ownership of the map (also the user that created the map)
--#  mapGroup          : group who has the access to the map
--#  mapCreateTime     : The time the map was created
--#  mapAccess         : a 2/4 character sequence rw,ro, rwro to access the map owner/group/all permission
--#  userLastModifies  : the user who last modified the map
--#  lastModifiedTime  : The last time the map was modified
--#  mapScale          : A float scale factor for the map
--#  mapXOffeset       : An Integer representing the offset in Pixel
--#  mapYOffset        : An Integer representing the offset in Pixel
--#  mapType           : Flag indicating the type of the map.
--#                      'A' - Map generated automatically
--#                      'U' - Map generated by user
--#                      'S' - Map Static means that is an Automatic map Saved by a user
--#                      'D' - Map deleted // FOR FUTURE USE
--#  mapWidth		   : Width of the map
--#  mapHeight		   : Height of the map
--########################################################################

create table map (
    mapId	   		 integer default nextval('opennmsNxtId') not null,
    mapName	   		 varchar(63) not null,
    mapBackGround	 varchar(256),
    mapOwner   		 varchar(64) not null,
    mapGroup   		 varchar(64),
    mapCreateTime	 timestamp not null,
    mapAccess		 char(6) not null,
    userLastModifies varchar(64) not null,
    lastModifiedTime timestamp not null,
    mapScale         float8,
    mapXOffset      integer,
	mapYOffset       integer,
	mapType          char(1),
	mapWidth		integer not null,
	mapHeight		integer not null,

	constraint pk_mapID primary key (mapId)
);

--########################################################################
--#
--# element table     -- This table maintains a record of elements beloging to maps
--#					
--# This table provides the following information:
--#
--#  mapId             : Identifier of the parent map
--#  elementId         : Identifier of the element map
--#  elemenType        : Flag indicating the type of the element.
--#                      'M' - Element is a Map 
--#                      'N' - Element is a Node
--#  elementLabel      : element label
--#  elementIcon       : image assocated with element
--#  elementX          : An Integer representing the position in arbitrary units
--#  elementY          : An Integer representing the offset in abitrary units
--#
--########################################################################

create table element (
    id               integer default nextval('opennmsNxtId') not null,
    mapId	   		 integer not null,
    elementId		 integer not null,
	elementType      char(1) not null,
    elementLabel 	 varchar(256) not null,
    elementIcon 	 varchar(256),
    elementX         integer,
	elementY         integer,
	
	constraint pk_element primary key (mapId,elementId,elementType),
	constraint fk_mapID foreign key (mapId) references map on delete cascade
);

create index element_mapid_elementid on element(mapId,elementId);

--# These don't work with installer

--#alter table element add constraint elementid check (elementid <> 0);

--########################################################################
--#
--# reportLocator table     -- This table contains a record of availability
--#                            reports and their location on disk
--#					
--# This table provides the following information:
--#
--#  id                	: Unique integer identifier for the report
--#  categoryName		: Name of the report category
--#  runDate			: Date report sheduled to run
--#  format				: format of the report (calenda etc).
--#  type				: output type of the file (SVG/PDF/HTML)
--#  location			: where on disk we put the report
--#	 Available			: Have we run the report yet or not?
--#
--########################################################################

create table reportLocator (
    reportId	 		integer not null,
    reportCategory		varchar(256) not null,
	reportDate			timestamp with time zone not null,
    reportFormat		varchar(256) not null,
    reportType			varchar(256) not null,
    reportLocation		varchar(256) not null,
	reportAvailable		bool not null
);

--# Sequence for the reportId column in the reportLocator table
--#          sequence,   column, table
--# install: reportNxtId reportId reportLocator
create sequence reportNxtId minvalue 1;

--########################################################################
--#
--# reportcatalog table     -- report catalog data
--#                            reports and their location on disk
--#					
--# This table provides the following information:
--#
--#  id                	: Unique integer identifier for the report
--#  reportId			: Name of the report category
--#  title				: display title
--#  date				: when the report was run
--#  location			: where on disk we put the report
--#
--########################################################################

create table reportCatalog (
    id			 		integer not null,
    reportId			varchar(256) not null,
    title				varchar(256) not null,
	date				timestamp with time zone not null,
    location			varchar(256) not null
);

--# Sequence for the reportId column in the reportLocator table
--#          sequence,   column, table
--# install: reportCatalogNxtId id reportCatalog
create sequence reportCatalogNxtId minvalue 1;


--########################################################################
--#
--# statisticsReport table -- This table contains a record of statistics
--#                           reports
--#					
--# This table provides the following information:
--#
--#  id                	: Unique integer identifier for the report
--#  startDate          : The beginning date for the report (data starting
--#                       at this time stamp is included)
--#  endDate            : The end date for the report (data up to,
--#                       but not including this time stamp is included)
--#  name               : Report name this references a report definition
--#                       in statsd-configuration.xml
--#  description        : User-friendly description for this report
--#  jobStartedDate     : The date when this report run started
--#  jobCompletedDate   : The date when this report run completed
--#  purgeDate          : The date at which this report can be purged
--#
--########################################################################

create table statisticsReport (
	id					integer default nextval('opennmsNxtId') not null,
	startDate			timestamp with time zone not null,
	endDate				timestamp with time zone not null,
	name				varchar(63) not null,
	description			varchar(255) not null,
	jobStartedDate		timestamp with time zone not null,
	jobCompletedDate	timestamp with time zone not null,
	purgeDate			timestamp with time zone not null,

	constraint pk_statisticsReport_id primary key (id)
);

create index statisticsReport_startDate on statisticsReport(startDate);
create index statisticsReport_name on statisticsReport(name);
create index statisticsReport_purgeDate on statisticsReport(purgeDate);


--########################################################################
--#
--# resourceReference table -- This table is a lookup table for string
--#                            resourceIds. This will help keep the relatively
--#                            long (tens of characters) string resource IDs
--#                            out of the statistics table.
--#					
--# This table provides the following information:
--#
--#  id                	: Unique integer identifier for the resource
--#  resourceId         : String resource ID for this resource
--#
--########################################################################

create table resourceReference (
	id					integer default nextval('opennmsNxtId') not null,
	resourceId			varchar(255) not null,

	constraint pk_resourceReference_id primary key (id)
);

create unique index resourceReference_resourceId on resourceReference (resourceId);


--########################################################################
--#
--# statisticsReportData table -- This table contains individual data points
--#                               (aggregated or not) for statistics reports.
--#					
--# This table provides the following information:
--#
--#  id                	: Unique integer identifier for the data
--#  reportId           : Integer ID for the report that created this data
--#  resourceId         : Integer ID for this resource related to this data
--#  value              : Float containing the value for this data point
--#
--########################################################################

create table statisticsReportData (
	id					integer default nextval('opennmsNxtId') not null,
	reportId			integer not null,
	resourceId			integer not null,
	value				float8 not null,
	
	constraint pk_statsData_id primary key (id),
	constraint fk_statsData_reportId foreign key (reportId) references statisticsReport (id) on delete cascade,
	constraint fk_statsData_resourceId foreign key (resourceId) references resourceReference (id) on delete cascade
);

create unique index statsData_unique on statisticsReportData(reportId, resourceId);


--# Begin Acknowledgment persistence table structure

--########################################################################
--#
--# acks table -- This table contains each acknowledgment
--# 
--#  id                 : Unique ID
--#  ackTime            : Time of the Acknowledgment
--#  ackUser            : User ID of the Acknowledgment
--#  ackType            : Enum of Acknowlegable Types in the system (i.e
--#                     : notifications/alarms
--#  ackAction          : Enum of Acknowlegable Actions in the system (i.e.
--#                     : ack,unack,clear,escalate
--#  refId              : Acknowledgable's ID
--########################################################################

CREATE TABLE acks (
    id        integer default nextval('opennmsnxtid') not null,
    ackTime   timestamp with time zone not null default now(),
    ackUser   varchar(64) not null default 'admin',
    ackType   integer not null default 1,
    ackAction integer not null default 1,
    log       varchar(128),
    refId     integer,
    
    constraint pk_acks_id primary key (id)
);

create index ack_time_idx on acks(ackTime);
create index ack_user_idx on acks(ackUser);

--########################################################################
--#
--#  categories to groups mapping table -- This table used for maintaining a many-to-many
--#     relationship between categories and groups
--# 
--#  categoryId       : References foreign key in the groups table
--#  groupId          : References foreign key in the users table
--########################################################################

create table category_group (
    categoryId  integer not null,
    groupId     varchar(16) not null,

    constraint categoryid_fkey2 foreign key (categoryId) references categories ON DELETE CASCADE
);

CREATE INDEX catid_idx3 on category_group(categoryId);
CREATE INDEX catgroup_idx on category_group(groupId);
CREATE UNIQUE INDEX catgroup_unique_idx on category_group(categoryId, groupId);

--########################################################################
--#
--# minions - table for tracking remote minions
--#
--# id           : The ID of the minion
--# location     : The monitoring location associated with the minion
--# status       : The status of the minion
--# last_updated : The last time the minion reported in
--#
--########################################################################

create table minions (
    id           varchar(36) not null,
    location     text not null,
    status       text,
    last_updated timestamp with time zone default now(),

    constraint pk_minions primary key (id)
);

--########################################################################
--#
--# minions_properties - arbitrary properties associated with a minion
--#
--# id        : The unique ID of the property entry
--# minion_id : The ID of the minion
--# key       : The property key
--# value     : The property value
--#
--########################################################################

create table minions_properties (
    id        integer default nextval('opennmsnxtid') not null,
    minion_id varchar(36) not null,
    key       text not null,
    value     text,

    constraint pk_minions_properties_id primary key (id),
    constraint fk_minions_properties foreign key (minion_id) references minions (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX minions_properties_unique_idx ON minions_properties(minion_id, key);

--# Begin enlinkd table
drop table lldpElement cascade;
drop table lldpLink cascade;
drop table cdpElement cascade;
drop table cdpLink cascade;
drop table ospfElement cascade;
drop table ospfLink cascade;
drop table isisElement cascade;
drop table isisLink cascade;
drop table ipNetToMedia cascade;
drop table bridgeElement cascade;
drop table bridgeMacLink cascade;
drop table bridgeBridgeLink cascade;
drop table bridgeStpLink cascade;

create table lldpElement (
      id integer default nextval('opennmsnxtid') not null,
      nodeid          integer not null,
      lldpChassisId text not null,
      lldpChassisIdSubType integer not null,
      lldpSysname text not null,
      lldpNodeCreateTime	timestamp not null,
      lldpNodeLastPollTime	timestamp not null,
      constraint pk_lldpelement_id primary key (id),
      constraint fk_nodeIDlldpelem foreign key (nodeid) references node ON DELETE CASCADE
);

create table lldpLink (
      id integer default nextval('opennmsnxtid') not null,
      nodeid          integer not null,
      lldpLocalPortNum integer not null,
      lldpPortId text not null,
      lldpPortIdSubType integer not null,
      lldpPortDescr text not null,
      lldpPortIfindex integer,
      lldpRemChassisId text not null,
      lldpRemChassisIdSubType integer not null,
      lldpRemSysname text not null,
      lldpRemPortId text not null,
      lldpRemPortIdSubType integer not null,
      lldpRemPortDescr text not null,
      lldpLinkCreateTime	timestamp not null,
      lldpLinkLastPollTime	timestamp not null,
      constraint pk_lldplink_id primary key (id),
      constraint fk_nodeIDlldplink foreign key (nodeid) references node ON DELETE CASCADE
);

create table cdpElement (
      id integer default nextval('opennmsnxtid') not null,
      nodeid          integer not null,
      cdpGlobalRun    integer not null,
      cdpGlobalDeviceId text not null,
      cdpNodeCreateTime	timestamp not null,
      cdpNodeLastPollTime	timestamp not null,
      constraint pk_cdpelement_id primary key (id),
      constraint fk_nodeIDcdpelem foreign key (nodeid) references node ON DELETE CASCADE
);

create table cdpLink (
      id integer default nextval('opennmsnxtid') not null,
      nodeid          integer not null,
      cdpCacheIfIndex integer not null,
      cdpCacheDeviceIndex integer not null,
      cdpInterfaceName text,
      cdpCacheAddressType integer not null,
      cdpCacheAddress text not null,
      cdpCacheVersion text not null,
      cdpCacheDeviceId text not null,
      cdpCacheDevicePort text not null,
      cdpCacheDevicePlatform text not null,
      cdpLinkCreateTime	timestamp not null,
      cdpLinkLastPollTime timestamp not null,
      constraint pk_cdplink_id primary key (id),
      constraint fk_nodeIDcdplink foreign key (nodeid) references node ON DELETE CASCADE
);

create table ospfElement (
      id integer default nextval('opennmsnxtid') not null,
      nodeid          integer not null,
      ospfRouterId varchar(16) not null,
      ospfAdminStat      integer not null,
      ospfVersionNumber  integer not null,
      ospfBdrRtrStatus   integer not null,
      ospfASBdrRtrStatus integer not null,
      ospfRouterIdNetmask varchar(16) not null,
      ospfRouterIdIfindex      integer not null,
      ospfNodeCreateTime	timestamp not null,
      ospfNodeLastPollTime	timestamp not null,
      constraint pk_ospfelement_id primary key (id),
      constraint fk_nodeIDospfelem foreign key (nodeid) references node ON DELETE CASCADE
);

create table ospfLink (
      id integer default nextval('opennmsnxtid') not null,
      nodeid          integer not null,
      ospfIpAddr varchar(16),
      ospfIpMask varchar(16),
      ospfAddressLessIndex integer,
      ospfIfIndex integer,
      ospfRemRouterId varchar(16) not null,
      ospfRemIpAddr varchar(16) not null,
      ospfRemAddressLessIndex integer not null,
      ospfLinkCreateTime	timestamp not null,
      ospfLinkLastPollTime	timestamp not null,
      constraint pk_ospflink_id primary key (id),
      constraint fk_nodeIDospflink foreign key (nodeid) references node ON DELETE CASCADE
);

create table isisElement (
      id integer default nextval('opennmsnxtid') not null,
      nodeid          integer not null,
      isisSysID varchar(32) not null,
      isisSysAdminState integer not null,
      isisNodeCreateTime	timestamp not null,
      isisNodeLastPollTime	timestamp not null,
      constraint pk_isiselement_id primary key (id),
      constraint fk_nodeIDisiselem foreign key (nodeid) references node ON DELETE CASCADE
);

create table isisLink (
      id integer default nextval('opennmsnxtid') not null,
      nodeid          integer not null,
      isisCircIndex   integer not null,
      isisISAdjIndex  integer not null,
      isisCircIfIndex    integer,
      isisCircAdminState integer,
      isisISAdjState  integer not null,
      isisISAdjNeighSNPAAddress varchar(80) not null,
      isisISAdjNeighSysType integer not null,
      isisISAdjNeighSysID varchar(32) not null,
      isisISAdjNbrExtendedCircID integer,
      isisLinkCreateTime	timestamp not null,
      isisLinkLastPollTime	timestamp not null,
      constraint pk_isislink_id primary key (id),
      constraint fk_nodeIDisislink foreign key (nodeid) references node ON DELETE CASCADE
);

create table ipNetToMedia (
    id                      integer default nextval('opennmsNxtId') not null,
    netAddress              text not null,
    physAddress             varchar(32) not null,
    sourceNodeId            integer not null,
    sourceIfIndex           integer not null,
    createTime     timestamp not null,
    lastPollTime   timestamp not null,
    constraint pk_ipnettomedia_id primary key (id),
    constraint fk_sourcenodeid_ipnettomedia foreign key (sourcenodeid) references node (nodeid) 
);

create table bridgeElement (
    id                  integer default nextval('opennmsNxtId') not null,
    nodeid                   integer not null,
    baseBridgeAddress        varchar(12) not null,
    baseNumPorts             integer not null,
    basetype                 integer not null,
    vlan                     integer,
    vlanname                 text,
    stpProtocolSpecification integer,
    stpPriority              integer,
    stpdesignatedroot        varchar(16),
    stprootcost              integer,
    stprootport              integer,
    bridgeNodeCreateTime     timestamp not null,
    bridgeNodeLastPollTime   timestamp not null,
    constraint pk_bridgeelement_id primary key (id),
    constraint fk_nodeIDbridgeelement foreign key (nodeid) references node on delete cascade
);

create table bridgeMacLink (
    id                  integer default nextval('opennmsNxtId') not null,
    nodeid              integer not null,
    bridgePort          integer not null,
    bridgePortIfIndex   integer,
    bridgePortIfName    text,
    vlan                integer,
    macAddress          varchar(12) not null,
    bridgeMacLinkCreateTime     timestamp not null,
    bridgeMacLinkLastPollTime   timestamp not null,
    constraint pk_bridgemaclink_id primary key (id),
    constraint fk_nodeIDbridgemaclink foreign key (nodeid) references node on delete cascade
);

create table bridgeBridgeLink (
    id                      integer default nextval('opennmsNxtId') not null,
    nodeid                  integer not null,
    bridgePort              integer,
    bridgePortIfIndex       integer,
    bridgePortIfName        text,
    vlan                    integer,
    designatedNodeid        integer not null,
    designatedBridgePort    integer,
    designatedBridgePortIfIndex   integer,
    designatedBridgePortIfName    text,
    designatedVlan          integer,
    bridgeBridgeLinkCreateTime     timestamp not null,
    bridgeBridgeLinkLastPollTime   timestamp not null,
    constraint pk_bridgebridgelink_id primary key (id),
    constraint fk_nodeIDbridgebridgelink foreign key (nodeid) references node on delete cascade,
    constraint fk_desnodeIDbridgemaclink foreign key (designatednodeid) references node (nodeid) 
);

create table bridgeStpLink (
    id                   integer default nextval('opennmsNxtId') not null,
    nodeid               integer not null,
    stpPort              integer not null,
    stpPortPriority      integer not null,
    stpPortState         integer not null,
    stpPortEnable        integer not null,
    stpPortPathCost      integer not null,
    stpPortIfIndex       integer,
    stpPortIfName        text,
    vlan                 integer,
    designatedCost       integer not null,
    designatedRoot       varchar(16) not null,
    designatedBridge     varchar(16) not null,
    designatedPort       varchar(4) not null,
    bridgeStpLinkCreateTime     timestamp not null,
    bridgeStpLinkLastPollTime   timestamp not null,
    constraint pk_bridgestplink_id primary key (id),
    constraint fk_nodeIDbridgestplink foreign key (nodeid) references node on delete cascade
);
--# End enlinkd table

--# Begin Quartz persistence tables

CREATE TABLE qrtz_job_details
  (
    JOB_NAME  VARCHAR(80) NOT NULL,
    JOB_GROUP VARCHAR(80) NOT NULL,
    DESCRIPTION VARCHAR(120) NULL,
    JOB_CLASS_NAME   VARCHAR(128) NOT NULL,
    IS_DURABLE BOOL NOT NULL,
    IS_VOLATILE BOOL NOT NULL,
    IS_STATEFUL BOOL NOT NULL,
    REQUESTS_RECOVERY BOOL NOT NULL,
    JOB_DATA BYTEA NOT NULL,

    constraint qrtz_job_details_pkey PRIMARY KEY (JOB_NAME,JOB_GROUP)
);

CREATE TABLE qrtz_job_listeners
  (
    JOB_NAME  VARCHAR(80) NOT NULL,
    JOB_GROUP VARCHAR(80) NOT NULL,
    JOB_LISTENER VARCHAR(80) NOT NULL,

    constraint pk_qrtz_job_listeners PRIMARY KEY (JOB_NAME,JOB_GROUP,JOB_LISTENER),
    constraint fk_qrtz_job_listeners FOREIGN KEY (JOB_NAME,JOB_GROUP)
        REFERENCES QRTZ_JOB_DETAILS (JOB_NAME,JOB_GROUP)
);


CREATE TABLE qrtz_triggers
  (
    TRIGGER_NAME VARCHAR(80) NOT NULL,
    TRIGGER_GROUP VARCHAR(80) NOT NULL,
    JOB_NAME  VARCHAR(80) NOT NULL,
    JOB_GROUP VARCHAR(80) NOT NULL,
    IS_VOLATILE BOOL NOT NULL,
    DESCRIPTION VARCHAR(120),
    NEXT_FIRE_TIME BIGINT,
    PREV_FIRE_TIME BIGINT,
    TRIGGER_STATE VARCHAR(16) NOT NULL,
    TRIGGER_TYPE VARCHAR(8) NOT NULL,
    START_TIME BIGINT NOT NULL,
    END_TIME BIGINT,
    CALENDAR_NAME VARCHAR(80),
    MISFIRE_INSTR SMALLINT,
    JOB_DATA BYTEA,
    PRIORITY INTEGER,

    constraint pk_qrtz_triggers PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    constraint fk_qrtz_triggers FOREIGN KEY (JOB_NAME,JOB_GROUP)
        REFERENCES QRTZ_JOB_DETAILS (JOB_NAME,JOB_GROUP)
);

CREATE TABLE qrtz_simple_triggers
  (
    TRIGGER_NAME VARCHAR(80) NOT NULL,
    TRIGGER_GROUP VARCHAR(80) NOT NULL,
    REPEAT_COUNT BIGINT NOT NULL,
    REPEAT_INTERVAL BIGINT NOT NULL,
    TIMES_TRIGGERED BIGINT NOT NULL,

    constraint pk_qrtz_simple_triggers PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    constraint fk_qrtz_simple_triggers FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS (TRIGGER_NAME,TRIGGER_GROUP)
);


CREATE TABLE qrtz_cron_triggers
  (
    TRIGGER_NAME VARCHAR(80) NOT NULL,
    TRIGGER_GROUP VARCHAR(80) NOT NULL,
    CRON_EXPRESSION VARCHAR(80) NOT NULL,
    TIME_ZONE_ID VARCHAR(80),

    constraint pk_qrtz_cron_triggers PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    constraint fk_qrtz_cron_triggers FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS (TRIGGER_NAME,TRIGGER_GROUP)
);


CREATE TABLE qrtz_blob_triggers
  (
    TRIGGER_NAME VARCHAR(80) NOT NULL,
    TRIGGER_GROUP VARCHAR(80) NOT NULL,
    BLOB_DATA BYTEA,

    constraint pk_qrtz_blob_triggers PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    constraint fk_qrtz_blob_triggers FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS (TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_trigger_listeners
  (
    TRIGGER_NAME  VARCHAR(80) NOT NULL,
    TRIGGER_GROUP VARCHAR(80) NOT NULL,
    TRIGGER_LISTENER VARCHAR(80) NOT NULL,

    constraint pk_qrtz_trigger_listeners PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_LISTENER),
    constraint fk_qrtz_trigger_listeners FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP)
        REFERENCES QRTZ_TRIGGERS (TRIGGER_NAME,TRIGGER_GROUP)
);


CREATE TABLE qrtz_calendars
  (
    CALENDAR_NAME  VARCHAR(80) NOT NULL,
    CALENDAR BYTEA NOT NULL,
    constraint pk_qrtz_calendars PRIMARY KEY (CALENDAR_NAME)
);


CREATE TABLE qrtz_paused_trigger_grps
  (
    TRIGGER_GROUP  VARCHAR(80) NOT NULL,
    constraint pk_qrtz_paused_trigger_grps PRIMARY KEY (TRIGGER_GROUP)
);

CREATE TABLE qrtz_fired_triggers
  (
    ENTRY_ID VARCHAR(95) NOT NULL,
    TRIGGER_NAME VARCHAR(80) NOT NULL,
    TRIGGER_GROUP VARCHAR(80) NOT NULL,
    IS_VOLATILE BOOL NOT NULL,
    INSTANCE_NAME VARCHAR(80) NOT NULL,
    FIRED_TIME BIGINT NOT NULL,
    STATE VARCHAR(16) NOT NULL,
    JOB_NAME VARCHAR(80),
    JOB_GROUP VARCHAR(80),
    IS_STATEFUL BOOL,
    REQUESTS_RECOVERY BOOL,
    PRIORITY INTEGER,
    constraint pk_qrtz_fired_triggers PRIMARY KEY (ENTRY_ID)
);

CREATE TABLE qrtz_scheduler_state
  (
    INSTANCE_NAME VARCHAR(80) NOT NULL,
    LAST_CHECKIN_TIME BIGINT NOT NULL,
    CHECKIN_INTERVAL BIGINT NOT NULL,
    RECOVERER VARCHAR(80),
    constraint pk_qrtz_scheduler_state PRIMARY KEY (INSTANCE_NAME)
);

CREATE TABLE qrtz_locks
  (
    LOCK_NAME  VARCHAR(40) NOT NULL,
    constraint pk_qrtz_locks PRIMARY KEY (LOCK_NAME)
);

--##################################################################
--# The following command should populate the qrtz_locks table
--# are no categories in the category table
--##################################################################
--# criteria: SELECT count(*) = 0 from qrtz_locks
insert into qrtz_locks values('TRIGGER_ACCESS');
--# criteria: SELECT count(*) = 0 from qrtz_locks
insert into qrtz_locks values('JOB_ACCESS');
--# criteria: SELECT count(*) = 0 from qrtz_locks
insert into qrtz_locks values('CALENDAR_ACCESS');
--# criteria: SELECT count(*) = 0 from qrtz_locks
insert into qrtz_locks values('STATE_ACCESS');
--# criteria: SELECT count(*) = 0 from qrtz_locks
insert into qrtz_locks values('MISFIRE_ACCESS');

--# End Quartz persistence tables

create table accesspoints (
  physaddr varchar(32) NOT NULL UNIQUE,
  nodeid integer NOT NULL,
  pollingpackage varchar(256) NOT NULL,
  status integer,
  controlleripaddr varchar(40),

  CONSTRAINT pk_physaddr primary key (physaddr)
);

create index accesspoint_package_idx on accesspoints(pollingpackage);

--##################################################################
--# The following command should populate the filterfavorites table
--##################################################################
CREATE TABLE filterfavorites (
  filterid INTEGER NOT NULL,
  username VARCHAR(50) NOT NULL,
  filtername VARCHAR(50) NOT NULL,
  page VARCHAR(25) NOT NULL,
  filter VARCHAR(255) NOT NULL,

  CONSTRAINT pk_filterid PRIMARY KEY (filterid)
);
CREATE INDEX filternamesidx ON filterfavorites (username, filtername, page);

--##################################################################
--# Hardware Inventory Tables
--##################################################################

create table hwEntity (
    id                      integer default nextval('opennmsNxtId') not null,
    parentId                integer,
    nodeId                  integer,
    entPhysicalIndex        integer not null,
    entPhysicalParentRelPos integer,
    entPhysicalName         varchar(128),
    entPhysicalDescr        varchar(128),
    entPhysicalAlias        varchar(128),
    entPhysicalVendorType   varchar(128),
    entPhysicalClass        varchar(128),
    entPhysicalMfgName      varchar(128),
    entPhysicalModelName    varchar(128),
    entPhysicalHardwareRev  varchar(128),
    entPhysicalFirmwareRev  varchar(128),
    entPhysicalSoftwareRev  varchar(128),
    entPhysicalSerialNum    varchar(128),
    entPhysicalAssetID      varchar(128),
    entPhysicalIsFRU        bool, 
    entPhysicalMfgDate      timestamp,
    entPhysicalUris         varchar(256),
    constraint pk_hwEntity_id primary key (id),
    constraint fk_hwEntity_parent foreign key (parentId) references hwEntity (id) on delete cascade,
    constraint fk_hwEntity_node foreign key (nodeId) references node on delete cascade
);
create index hwEntity_nodeId_idx on hwEntity(nodeid);
create index hwEntity_entPhysicalIndex_idx on hwEntity(entPhysicalIndex);

create table hwEntityAttributeType (
    id          integer default nextval('opennmsNxtId') not null,
    attribName  varchar(128) not null,
    attribOid   varchar(128) not null,
    attribClass varchar(32) not null,
    constraint  pk_hwEntity_attributeType_id primary key (id)
);
create unique index hwEntityAttributeType_unique_name_idx on hwEntityAttributeType(attribName);
create unique index hwEntityAttributeType_unique_oid_idx on hwEntityAttributeType(attribOid);

create table hwEntityAttribute (
    id             integer default nextval('opennmsNxtId') not null,
    hwEntityId     integer not null,
    hwAttribTypeId integer not null,
    attribValue    varchar(256) not null,
    constraint pk_hwEntity_attribute_id primary key (id),
    constraint fk_hwEntity_hwEntityAttribute foreign key (hwEntityId) references hwEntity (id) on delete cascade,
    constraint fk_hwEntityAttribute_hwEntityAttributeType foreign key (hwAttribTypeId) references hwEntityAttributeType (id) on delete cascade
);
create unique index hwEntityAttribute_unique_idx on hwEntityAttribute(hwEntityId,hwAttribTypeId);


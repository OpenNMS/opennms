--# create.sql -- SQL to build the initial tables for the OpenNMS Project
--#
--# Copyright (C) 2005 The OpenNMS Group, Inc., Inc.  All rights reserved.
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
--# Modified: 2004-08-30
--# Note: See create.sql.changes

drop table assets cascade;
drop table usersNotified cascade;
drop table notifications cascade;
drop table outages cascade;
drop table ifServices cascade;
drop table snmpInterface cascade;
drop table ipInterface cascade;
drop table node cascade;
drop table service cascade;
drop table distPoller cascade;
drop table events cascade;
drop table vulnerabilities cascade;
drop table vulnPlugins cascade;
drop table serverMap cascade;
drop table serviceMap cascade;
drop sequence nodeNxtId;
drop sequence serviceNxtId;
drop sequence eventsNxtId;
drop sequence outageNxtId;
drop sequence notifyNxtId;
drop sequence vulnNxtId;

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
	ipAddr			varchar(16) not null,
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
	ipAddr			varchar(16) not null,
	serviceMapName		varchar(32) not null );
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
	dpName			varchar(12),
				constraint pk_dpName primary key (dpName),
	dpIP			varchar(16) not null,
	dpComment		varchar(256),
	dpDiscLimit		numeric(5,2),
	dpLastNodePull		timestamp without time zone,
	dpLastEventPull		timestamp without time zone,
	dpLastPackagePush	timestamp without time zone,
	dpAdminState 		integer,
	dpRunState		integer );

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
--########################################################################

create table node (
	nodeID		integer,
			constraint pk_nodeID primary key (nodeID),
	dpName		varchar(12),
			constraint fk_dpName foreign key (dpName) references distPoller,
	nodeCreateTime	timestamp without time zone not null,
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
	lastCapsdPoll   timestamp without time zone );

create index node_id_type_idx on node(nodeID, nodeType);
create index node_label_idx on node(nodeLabel);

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
--# 		       unnumbered interfaces.
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
--#			'F' - Forced Unmanaged (via the user interface)
--#                     'N' - Not polled as part of any package
--#  ipStatus        : If interface supports SNMP this field will
--#                    hold a numeric representation of interface's
--#                    operational status (same as 'snmpIfOperStatus'
--#                    field in the snmpInterface table).
--#                      1 = Up, 2 = Down, 3 = Testing
--#  ipLastCapsdPoll : Date and time of last poll by capsd
--#  isSnmpPrimary   : Character used as a boolean flag
--#                      'P' - Primary SNMP
--#                      'S' - Secondary SNMP
--#                      'N' - Not eligible (does not support SNMP or
--#                               or has no ifIndex)
--#
--########################################################################

create table ipInterface (
	nodeID			integer,
				constraint fk_nodeID1 foreign key (nodeID) references node ON DELETE CASCADE,
	ipAddr			varchar(16) not null,
	ifIndex			integer,
	ipHostname		varchar(256),
	isManaged		char(1),
	ipStatus		integer,
	ipLastCapsdPoll		timestamp without time zone,
	isSnmpPrimary           char(1) );

create index ipinterface_nodeid_ipaddr_ismanaged_idx on ipInterface(nodeID, ipAddr, isManaged);
create index ipinterface_ipaddr_ismanaged_idx on ipInterface(ipAddr, isManaged);
create index ipinterface_ipaddr_idx on ipInterface(ipAddr);
create index ipinterface_nodeid_ismanaged_idx on ipInterface(ipAddr);
create index ipinterface_nodeid_idx on ipInterface(nodeID);

--#########################################################################
--# snmpInterface Table - Augments the ipInterface table with information
--#                       available from IP interfaces which also support
--#                       SNMP.
--#
--# This table provides the following information:
--#
--#  nodeID             : Unique identifier for node to which this if belongs
--#  ipAddr             : IP Address associated with this interface
--#  snmpIpAdEntNetMask : SNMP MIB-2 ipAddrTable.ipAddrEntry.ipAdEntNetMask
--#                       Value is interface's subnet mask
--#  snmpPhysAddr       : SNMP MIB-2 ifTable.ifEntry.ifPhysAddress
--#                       Value is interface's MAC Address
--#  snmpIfIndex        : SNMP MIB-2 ifTable.ifEntry.ifIndex
--#                       Value is interface's arbitrarily assigned index.
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
--#
--# NOTE:  Although not marked as "not null" the snmpIfIndex field
--#        should never be null.  This table is considered to be uniquely
--#        keyed by nodeId and snmpIfIndex.  Eventually ipAddr and
--#        snmpIpAdEntNetMask will be removed and netmask added to
--#        the ipInterface table.
--########################################################################

create table snmpInterface (
	nodeID			integer,
				constraint fk_nodeID2 foreign key (nodeID) references node ON DELETE CASCADE,
	ipAddr			varchar(16) not null,
	snmpIpAdEntNetMask	varchar(16),
	snmpPhysAddr		char(12),
	snmpIfIndex		integer,
	snmpIfDescr		varchar(256),
	snmpIfType		integer,
	snmpIfName		varchar(32),
	snmpIfSpeed		integer,
	snmpIfAdminStatus	integer,
	snmpIfOperStatus	integer );

create index snmpinterface_nodeid_ifindex_idx on snmpinterface(nodeID, snmpIfIndex);
create index snmpinterface_nodeid_idx on snmpinterface(nodeID);
create index snmpinterface_ipaddr_idx on snmpinterface(ipaddr);

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
	serviceID		integer,
				constraint pk_serviceID primary key (serviceID),
	serviceName		varchar(32) not null );

--########################################################################
--# ifServices Table - Contains a mapping of interfaces to services available
--#                    on those interfaces (e.g., FTP, SMTP, DNS, etc.) and
--#                    recent polling status information.
--#
--# This table provides the following information:
--#
--#  nodeID    : Unique integer identifier for node
--#  ipAddr    : IP Address of node's interface
--#  ifIndex   : SNMP ifIndex, if available
--#  serviceID : Unique integer identifier of service/poller package
--#  lastGood  : Date and time of last successful poll by this poller package
--#  lastFail  : Date and time of last failed poll by this poller package
--#  qualifier : Service qualifier.  May be used to distinguish two
--#		 services which have the same serviceID.  For example, in the
--#              case of the HTTP service a qualifier might be the specific
--#              port on which the HTTP server was found.
--#  status    : Flag indicating the status of the service.
--#                'A' - Active
--#                'D' - Deleted
--#                'U' - Unmanaged (per capsd configuration change and CAPSD)
--#                'F' - Forced unmanaged (via user interface)
--#                'N' - Not polled as part of any of the packages that the
--#                      interface belongs to
--#  source    : Flag indicating how the service was detected.
--#                'P' - Plugin
--#                'F' - Forced (via CapsdPluginBehavior.conf)
--#  notify    : Flag indicating if this service should be notified on or not
--#                'Y' - to notify
--#                'N' = not to notify
--########################################################################

create table ifServices (
	nodeID			integer,
				constraint fk_nodeID3 foreign key (nodeID) references node ON DELETE CASCADE,
	ipAddr			varchar(16) not null,
	ifIndex			integer,
	serviceID		integer,
				constraint fk_serviceID1 foreign key (serviceID) references service ON DELETE CASCADE,
	lastGood		timestamp without time zone,
	lastFail		timestamp without time zone,
	qualifier		char(16),
	status         		char(1),
	source			char(1),
	notify                  char(1) );

create index ifservices_nodeid_ipaddr_status on ifservices(nodeID, ipAddr, status);
create index ifservices_nodeid_status on ifservices(nodeid, status);
create index ifservices_nodeid_idx on ifservices(nodeID);
create index ifservices_serviceid_idx on ifservices(serviceID);
create index ifservices_nodeid_serviceid_idx on ifservices(nodeID, serviceID);

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
--#  ipAddr             : IP Address of node's interface
--#  serviceID          : Unique integer identifier of service/poller package
--#  eventDescr		: Free-form textual description of the event
--#  eventLogmsg	: The log message for the event
--#  eventSeverity	: Severity of event
--#			   1 = Indeterminate
--#			   2 = Cleared (unimplemented at this time)
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
--#
--##################################################################

create table events (
	eventID			integer,
				constraint pk_eventID primary key (eventID),
	eventUei		varchar(256) not null,
	nodeID			integer,
				constraint fk_nodeID6 foreign key (nodeID) references node ON DELETE CASCADE,
	eventTime		timestamp without time zone not null,
	eventHost		varchar(256),
	eventSource		varchar(128) not null,
	ipAddr			varchar(16),
	eventDpName		varchar(12) not null,
	eventSnmphost		varchar(256),
	serviceID		integer,
	eventSnmp		varchar(256),
	eventParms		text,
	eventCreateTime		timestamp without time zone not null,
	eventDescr		varchar(4000),
	eventLoggroup		varchar(32),
	eventLogmsg		varchar(256),
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
	eventAckUser		varchar(256),
	eventAckTime		timestamp without time zone
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
--#
--########################################################################

create table outages (

	outageID		integer,
				constraint pk_outageID primary key (outageID),
	svcLostEventID		integer,
				constraint fk_eventID1 foreign key (svcLostEventID) references events (eventID) ON DELETE CASCADE,
	svcRegainedEventID	integer,
				constraint fk_eventID2 foreign key (svcRegainedEventID) references events (eventID) ON DELETE CASCADE,
	nodeID			integer,
					constraint fk_nodeID4 foreign key (nodeID) references node (nodeID) ON DELETE CASCADE,
	ipAddr			varchar(16) not null,
	serviceID		integer,
				constraint fk_serviceID2 foreign key (serviceID) references service (serviceID) ON DELETE CASCADE,
	ifLostService		timestamp without time zone not null,
	ifRegainedService	timestamp without time zone );

create index outages_svclostid_idx on outages(svcLostEventID);
create index outages_svcregainedid_idx on outages(svcRegainedEventID);
create index outages_nodeid_idx on outages(nodeID);
create index outages_ipaddr_idx on outages(ipaddr);
create index outages_serviceid_idx on outages(serviceID);
create index outages_regainedservice_idx on outages(ifRegainedService);

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
	vulnerabilityID		integer,
				constraint pk_vulnerabilityID primary key (vulnerabilityID),
	nodeID			integer,
	ipAddr			varchar(16),
	serviceID		integer,
	creationTime		timestamp without time zone not null,
	lastAttemptTime		timestamp without time zone not null,
	lastScanTime		timestamp without time zone not null,
	resolvedTime		timestamp without time zone,
	severity		integer not null,
	pluginID		integer not null,
	pluginSubID             integer not null,
	logmsg                  varchar(256),
	descr			text,
	port			integer,
	protocol		varchar(32),
	cveEntry		varchar(14) );

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
        cveEntry                varchar(14),
        md5                     varchar(32)
);


create unique index vulnplugins_pluginid_pluginsubid_idx on vulnPlugins(pluginID, pluginSubID);

--#  This constraint not understood installer
--#        CONSTRAINT pk_vulnplugins PRIMARY KEY (pluginID,pluginSubID));
--#
        

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
       textMsg      varchar(4000) not null,
       numericMsg   varchar(256),
       notifyID	    integer,
       			constraint pk_notifyID primary key (notifyID),
       pageTime     timestamp without time zone,
       respondTime  timestamp without time zone,
       answeredBy   varchar(256),
       nodeID	    integer,
       			constraint fk_nodeID7 foreign key (nodeID) references node (nodeID) ON DELETE CASCADE,
       interfaceID  varchar(16),
       serviceID    integer,
       eventID      integer,
       			constraint fk_eventID3 foreign key (eventID) references events (eventID) ON DELETE CASCADE,
       eventUEI     varchar(256) not null
       );

create index notifications_ipaddr_idx on notifications(interfaceID);
create index notifications_serviceid_idx on notifications(serviceID);
create index notifications_eventid_idx on notifications(eventID);
create index notifications_respondtime_idx on notifications(respondTime);
create index notifications_answeredby_idx on notifications(answeredBy);

--########################################################################
--#
--# This table contains the following fields:
--# userID      : The user id of the person being paged, from the users.xml
--#               file.
--# notifyID    : The index of the row from the notification table.
--# media       : A string describing the type of contact being made, ie text
--#               page, numeric page, email, etc...
--# contactInfo : A field for storing the information used to contact the user,
--#               e.g. an email address, the phone number and pin of the pager...
--#
--########################################################################

create table usersNotified (
        userID          varchar(256) not null,
        notifyID        integer,
			constraint fk_notifID2 foreign key (notifyID) references notifications (notifyID) ON DELETE CASCADE,
        notifyTime      timestamp without time zone,
        media           varchar(32),
        contactinfo     varchar(64)
);

create index userid_notifyid_idx on usersNotified(userID, notifyID);

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
        nodeID          integer,
			constraint fk_nodeID5 foreign key (nodeID) references node (nodeID),
        category        varchar(64) not null,
        manufacturer    varchar(64),
        vendor          varchar(64),
        modelNumber     varchar(64),
        serialNumber    varchar(64),
        description     varchar(128),
        circuitId       varchar(64),
        assetNumber     varchar(64),
        operatingSystem varchar(64),
        rack            varchar(64),
        slot            varchar(64),
        port            varchar(64),
        region          varchar(64),
        division        varchar(64),
        department      varchar(64),
        address1        varchar(256),
        address2        varchar(256),
        city            varchar(64),
        state           varchar(64),
        zip             varchar(64),
        building        varchar(64),
        floor           varchar(64),
        room            varchar(64),
        vendorPhone     varchar(64),
        vendorFax       varchar(64),
        vendorAssetNumber varchar(64),
        userLastModified char(20) not null,
        lastModifiedDate timestamp without time zone not null,
        dateInstalled   varchar(64),
        lease           varchar(64),
        leaseExpires    varchar(64),
        supportPhone    varchar(64),
        maintContract   varchar(64),
        maintContractExpires varchar(64),
        displayCategory   varchar(64),
        notifyCategory   varchar(64),
        pollerCategory   varchar(64),
        thresholdCategory   varchar(64),
        comment         varchar(1024)
       );


--##################################################################
--# The following commands set up automatic sequencing functionality
--# for fields which require this.
--#
--# DO NOT forget to add an "install" comment so that install.pl
--# knows to fix and renumber the sequences if need be
--##################################################################

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

--##################################################################
--# The following command adds the initial loopback poller entry to
--# the 'distPoller' table.
--##################################################################
insert into distPoller (dpName, dpIP, dpComment, dpDiscLimit, dpLastNodePull, dpLastEventPull, dpLastPackagePush, dpAdminState, dpRunState) values ('localhost', '127.0.0.1', 'This is the default poller.', 0.10, null, null, null, 1, 1);


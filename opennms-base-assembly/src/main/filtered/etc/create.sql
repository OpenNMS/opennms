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
--# Copyright (C) 1999-2015 The OpenNMS Group, Inc.
--# OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
--#
--# OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
--#
--# OpenNMS(R) is free software: you can redistribute it and/or modify
--# it under the terms of the GNU Affero General Public License as published
--# by the Free Software Foundation, either version 3 of the License,
--# or (at your option) any later version.
--#
--# OpenNMS(R) is distributed in the hope that it will be useful,
--# but WITHOUT ANY WARRANTY; without even the implied warranty of
--# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--# GNU Affero General Public License for more details.
--#
--# You should have received a copy of the GNU Affero General Public License
--# along with OpenNMS(R).  If not, see:
--#      http://www.gnu.org/licenses/
--#
--# For more information contact:
--#     OpenNMS(R) Licensing <license@opennms.org>
--#     http://www.opennms.org/
--#     http://www.opennms.com/

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
drop table scanreports cascade;
drop table monitoringlocations cascade;
drop table monitoringlocationspollingpackages cascade;
drop table monitoringlocationscollectionpackages cascade;
drop table monitoringlocationstags cascade;
drop table monitoringsystems cascade;
drop table events cascade;
drop table event_parameters cascade;
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
drop sequence reportNxtId;
drop sequence reportCatalogNxtId;
drop sequence mapNxtId;
drop sequence opennmsNxtId;  --# should be used for all sequences, eventually
drop sequence filternextid;

drop index filternamesidx;

--# Begin quartz persistence 

--# Legacy quartz 1.X tables
drop table qrtz_job_listeners;
drop table qrtz_trigger_listeners;

drop table qrtz_fired_triggers;
drop table qrtz_paused_trigger_grps;
drop table qrtz_scheduler_state;
drop table qrtz_locks;
drop table qrtz_simple_triggers;
drop table qrtz_cron_triggers;
drop table qrtz_simprop_triggers;
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


--#####################################################
--# monitoringlocations Table - Contains a list of network locations
--#   that are being monitored by OpenNMS systems in this cluster
--#
--# This table contains the following information:
--#
--# id            : The unique name of the location
--# monitoringarea: The monitoring location associated with the system
--# geolocation   : Address used for geolocating the location
--# coordinates   : Latitude/longitude coordinates determined by geolocating
--#                 the value of 'geolocation'
--# priority      : Integer priority used to layer items in the UI
--#
--#####################################################

CREATE TABLE monitoringlocations (
    id TEXT NOT NULL,
    monitoringarea TEXT NOT NULL,
    geolocation TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    priority INTEGER,

    CONSTRAINT monitoringlocations_pkey PRIMARY KEY (id)
);


CREATE TABLE monitoringlocationspollingpackages (
    monitoringlocationid TEXT NOT NULL,
    packagename TEXT NOT NULL,

    CONSTRAINT monitoringlocationspollingpackages_fkey FOREIGN KEY (monitoringlocationid) REFERENCES monitoringlocations (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX monitoringlocationspollingpackages_id_idx on monitoringlocationspollingpackages(monitoringlocationid);
CREATE UNIQUE INDEX monitoringlocationspollingpackages_id_pkg_idx on monitoringlocationspollingpackages(monitoringlocationid, packagename);


CREATE TABLE monitoringlocationscollectionpackages (
    monitoringlocationid TEXT NOT NULL,
    packagename TEXT NOT NULL,

    CONSTRAINT monitoringlocationscollectionpackages_fkey FOREIGN KEY (monitoringlocationid) REFERENCES monitoringlocations (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX monitoringlocationscollectionpackages_id_idx on monitoringlocationscollectionpackages(monitoringlocationid);
CREATE UNIQUE INDEX monitoringlocationscollectionpackages_id_pkg_idx on monitoringlocationscollectionpackages(monitoringlocationid, packagename);


CREATE TABLE monitoringlocationstags (
    monitoringlocationid TEXT NOT NULL,
    tag TEXT NOT NULL,

    CONSTRAINT monitoringlocationstags_fkey FOREIGN KEY (monitoringlocationid) REFERENCES monitoringlocations (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX monitoringlocationstags_id_idx on monitoringlocationstags(monitoringlocationid);
CREATE UNIQUE INDEX monitoringlocationstags_id_pkg_idx on monitoringlocationstags(monitoringlocationid, tag);

--##################################################################
--# The following command adds the initial 'Default' entry to
--# the 'monitoringlocations' table.
--##################################################################
INSERT INTO monitoringlocations (id, monitoringarea) values ('Default', 'Default');


--#####################################################
--# monitoringsystems Table - Contains a list of OpenNMS systems
--#    that are producing management information for this database
--#
--# This table contains the following information:
--#
--# id           : The UUID of the system
--# label        : Human-readable label for the system
--# location     : The monitoring location associated with the system
--# type         : The type of monitoring system, one of "OpenNMS", 
--#                "Remote Poller" or "Minion"
--# status       : The status of the system
--# last_updated : The last time the system reported in
--#
--#####################################################

CREATE TABLE monitoringsystems (
    id TEXT NOT NULL,
    label TEXT,
    location TEXT NOT NULL,
    type TEXT NOT NULL,
    status TEXT,
    last_updated TIMESTAMP WITH TIME ZONE,

    CONSTRAINT monitoringsystems_pkey PRIMARY KEY (id)
);

CREATE TABLE monitoringsystemsproperties (
    monitoringsystemid TEXT NOT NULL,
    property TEXT NOT NULL,
    propertyvalue TEXT,

    CONSTRAINT monitoringsystemsproperties_fkey FOREIGN KEY (monitoringsystemid) REFERENCES monitoringsystems (id) ON DELETE CASCADE
);

CREATE INDEX monitoringsystemsproperties_id_idx on monitoringsystemsproperties(monitoringsystemid);
CREATE UNIQUE INDEX monitoringsystemsproperties_id_property_idx on monitoringsystemsproperties(monitoringsystemid, property);

--##################################################################
--# The following command adds the initial localhost poller entry to
--# the 'monitoringsystems' table.
--##################################################################
INSERT INTO monitoringsystems (id, label, location, type) values ('00000000-0000-0000-0000-000000000000', 'localhost', 'Default', 'OpenNMS');


--#####################################################
--# scanreports Table - Contains a list of OpenNMS remote poller scan reports
--#
--# This table contains the following information:
--#
--# id               : The UUID of the report
--# location         : The monitoring location name
--# locale           : The locale the scan was run from
--# timestamp        : The start time of the scan
--#
--#####################################################

CREATE TABLE scanreports (
    id TEXT NOT NULL,
    location TEXT NOT NULL,
    locale TEXT,
    timestamp TIMESTAMP WITH TIME ZONE,

    CONSTRAINT scanreports_pkey PRIMARY KEY (id),
    CONSTRAINT scanreports_monitoringlocations_fkey FOREIGN KEY (location) REFERENCES monitoringlocations (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE UNIQUE INDEX scanreports_id_idx on scanreport(id);

--#####################################################
--# scanreportproperties Table - Contains an arbitrary collection of properties associated with a scan report
--#
--# This table contains the following information:
--#
--# scanReportId     : The ID of the scan report
--# property         : The property name/key
--# propertyValue    : The property value
--#
--#####################################################

CREATE TABLE scanreportproperties (
    scanReportId TEXT NOT NULL,
    property TEXT NOT NULL,
    propertyValue TEXT,

    CONSTRAINT scanreportproperties_fkey FOREIGN KEY (scanReportId) REFERENCES scanreports (id) ON DELETE CASCADE
);

CREATE INDEX scanreportproperties_id_idx on scanreportproperties(scanreportid);
CREATE UNIQUE INDEX scanreportproperties_id_property_idx on scanreportproperties(scanreportid, property);

--#####################################################
--# scanreportpollresults Table - Contains the set of poll results (service up/down) associated with a scan report
--#
--# This table contains the following information:
--#
--# id               : The UUID of the result
--# scanReportId     : The ID of the scan report
--# serviceName      : The name of the monitored service
--# serviceId        : The ID of the monitored service
--# nodeLabel        : The node label for display
--# nodeId           : The ID of the node
--# ipaddress        : The IP address of the monitored service
--# statusReason     : A user-displayable description of the response
--# responseTime     : The response time of the poll
--# statusCode       : The response code associated with the poll
--# statusTime       : The timestamp of the poll
--#
--#####################################################

CREATE TABLE scanreportpollresults (
    id TEXT NOT NULL,
    scanReportId TEXT NOT NULL,
    serviceName TEXT NOT NULL,
    serviceId INTEGER NOT NULL,
    nodeLabel TEXT NOT NULL,
    nodeId INTEGER NOT NULL,
    ipaddress TEXT,
    statusReason TEXT,
    responseTime DOUBLE PRECISION,
    statusCode INTEGER NOT NULL,
    statusTime TIMESTAMP WITH TIME ZONE,

    CONSTRAINT scanreportpollresults_pkey PRIMARY KEY (id),
    CONSTRAINT scanreportpollresults_fkey FOREIGN KEY (scanReportId) REFERENCES scanreports (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX scanreportpollresults_id_idx on scanreportpollresults(id);
CREATE UNIQUE INDEX scanreportpollresults_id_scanreportid_idx on scanreportpollresults(id, scanreportid);

--#####################################################
--# scanreportlogs Table - Contains poll logs associated with a scan report
--#
--# This table contains the following information:
--#
--# scanReportId     : The ID of the scan report
--# logText          : The contents of the scan log
--#
--#####################################################

CREATE TABLE scanreportlogs (
    scanReportId TEXT NOT NULL,
    logText TEXT,

    CONSTRAINT scanreportlogs_pkey PRIMARY KEY (scanReportId),
    CONSTRAINT scanreportlogs_fkey FOREIGN KEY (scanReportId) REFERENCES scanreports (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX scanreportlogs_scanReportId_idx on scanreportlogs(scanReportId);

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
	location        text not null,
	hasFlows        boolean not null default false,

	constraint pk_nodeID primary key (nodeID),
	constraint fk_node_location foreign key (location) references monitoringlocations (id) ON UPDATE CASCADE
);

create index node_id_type_idx on node(nodeID, nodeType);
create index node_label_idx on node(nodeLabel);
create unique index node_foreign_unique_idx on node(foreignSource, foreignId);

--#########################################################################
--# snmpInterface Table - Augments the ipInterface table with information
--#                       available from IP interfaces which also support
--#                       SNMP.
--#
--# This table provides the following information:
--#
--#  nodeID             : Unique identifier for node to which this if belongs
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
--#        keyed by nodeId and snmpIfIndex.
--########################################################################

create table snmpInterface (
    id				INTEGER DEFAULT nextval('opennmsNxtId') NOT NULL,
	nodeID			integer not null,
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
	hasFlows        boolean not null default false,

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
--#  netmask         : Netmask associated with this interface
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
	netmask			varchar(45),
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
	constraint fk_serviceID1 foreign key (serviceID) references service ON DELETE CASCADE
);

create unique index ifservices_ipinterfaceid_svc_unique on ifservices(ipInterfaceId, serviceId);
create index ifservices_ipinterfaceid_status on ifservices(ipInterfaceId, status);
create index ifservices_serviceid_idx on ifservices(serviceID);
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
	systemId		TEXT not null,
	eventSnmphost		varchar(256),
	serviceID		integer,
	eventSnmp		varchar(256),
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
create index events_systemid_idx on events(systemId);
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

create table event_parameters (
	eventID			integer not null,
	name        varchar(256) not null,
	value		    text not null,
	type		    varchar(256) not null,

	constraint pk_eventParameters primary key (eventID, name),
	constraint fk_eventParametersEventID foreign key (eventID) references events (eventID) ON DELETE CASCADE
);

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
--#  ifServiceId       : Unique integer identifier of service
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
	ifLostService		timestamp with time zone not null,
	ifRegainedService	timestamp with time zone,
	suppressTime    	timestamp with time zone,
	suppressedBy		varchar(256),
	ifServiceId		INTEGER not null,

	constraint pk_outageID primary key (outageID),
	constraint fk_eventID1 foreign key (svcLostEventID) references events (eventID) ON DELETE CASCADE,
	constraint fk_eventID2 foreign key (svcRegainedEventID) references events (eventID) ON DELETE CASCADE,
	CONSTRAINT ifServices_fkey2 FOREIGN KEY (ifServiceId) REFERENCES ifServices (id) ON DELETE CASCADE
);

create index outages_svclostid_idx on outages(svcLostEventID);
create index outages_svcregainedid_idx on outages(svcRegainedEventID);
create index outages_regainedservice_idx on outages(ifRegainedService);
create index outages_ifServiceId_idx on outages(ifServiceId);
create unique index one_outstanding_outage_per_service_idx on outages (ifserviceid) where ifregainedservice is null;

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
ALTER TABLE memos ADD CONSTRAINT reductionkey_type_unique_constraint UNIQUE (reductionkey, type);

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
    systemId                TEXT NOT NULL, CONSTRAINT fk_alarms_systemid FOREIGN KEY (systemId) REFERENCES monitoringsystems (id) ON DELETE CASCADE,
    nodeID                  INTEGER, CONSTRAINT fk_alarms_nodeid FOREIGN KEY (nodeID) REFERENCES node (nodeID) ON DELETE CASCADE,
    ipaddr                  VARCHAR(39),
    serviceID               INTEGER,
    reductionKey            TEXT,
    alarmType               INTEGER,
    counter                 INTEGER NOT NULL,
    severity                INTEGER NOT NULL,
    lastEventID             INTEGER, CONSTRAINT fk_eventIDak2 FOREIGN KEY (lastEventID) REFERENCES events (eventID) ON DELETE CASCADE,
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
    stickymemo              INTEGER, CONSTRAINT fk_stickyMemo FOREIGN KEY (stickymemo) REFERENCES memos (id) ON DELETE CASCADE
);

CREATE INDEX alarm_uei_idx ON alarms(eventUei);
CREATE INDEX alarm_nodeid_idx ON alarms(nodeID);
CREATE UNIQUE INDEX alarm_reductionkey_idx ON alarms(reductionKey);
CREATE INDEX alarm_clearkey_idx ON alarms(clearKey);
CREATE INDEX alarm_reduction2_idx ON alarms(alarmID, eventUei, systemId, nodeID, serviceID, reductionKey);
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
        userLastModified varchar(20) not null,
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
    systemId TEXT NOT NULL,
    ifServiceId INTEGER NOT NULL,
    statusCode INTEGER NOT NULL,
    statusTime timestamp with time zone NOT NULL,
    statusReason TEXT,
    responseTime DOUBLE PRECISION,

    CONSTRAINT location_specific_status_changes_pkey PRIMARY KEY (id),
    CONSTRAINT location_specific_status_changes_systemid_fkey FOREIGN KEY (systemId) REFERENCES monitoringsystems (id) ON DELETE CASCADE,
    CONSTRAINT ifservices_fkey4 FOREIGN KEY (ifServiceId) REFERENCES ifservices (id) ON DELETE CASCADE
);

create index location_specific_status_changes_ifserviceid on location_specific_status_changes(ifserviceid);
CREATE INDEX location_specific_status_changes_systemid ON location_specific_status_changes(systemId);
CREATE INDEX location_specific_status_changes_systemid_ifserviceid ON location_specific_status_changes(systemId, ifserviceid);
CREATE INDEX location_specific_status_changes_systemid_if_time ON location_specific_status_changes(systemId, ifserviceid, statustime);
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
      cdpGlobalDeviceIdFormat integer,
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
    linkType            integer not null,
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
--# See https://github.com/quartz-scheduler/quartz, file tables_postgres.sql
--# See http://www.quartz-scheduler.org/documentation/quartz-2.x/migration-guide.html

CREATE TABLE qrtz_job_details
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    JOB_NAME  VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    JOB_CLASS_NAME   VARCHAR(250) NOT NULL, 
    IS_DURABLE BOOL NOT NULL,
    IS_NONCONCURRENT BOOL NOT NULL,
    IS_UPDATE_DATA BOOL NOT NULL,
    REQUESTS_RECOVERY BOOL NOT NULL,
    JOB_DATA BYTEA,
    CONSTRAINT pk_qrtz_job_details PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP)
);

CREATE TABLE qrtz_triggers
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    JOB_NAME  VARCHAR(200) NOT NULL, 
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    NEXT_FIRE_TIME BIGINT,
    PREV_FIRE_TIME BIGINT,
    PRIORITY INTEGER,
    TRIGGER_STATE VARCHAR(16) NOT NULL,
    TRIGGER_TYPE VARCHAR(8) NOT NULL,
    START_TIME BIGINT NOT NULL,
    END_TIME BIGINT,
    CALENDAR_NAME VARCHAR(200) NULL,
    MISFIRE_INSTR SMALLINT,
    JOB_DATA BYTEA,
    CONSTRAINT pk_qrtz_triggers PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT fk_qrtz_triggers_job_details FOREIGN KEY (SCHED_NAME,JOB_NAME,JOB_GROUP) 
	REFERENCES QRTZ_JOB_DETAILS (SCHED_NAME,JOB_NAME,JOB_GROUP) 
);

CREATE TABLE qrtz_simple_triggers
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    REPEAT_COUNT BIGINT NOT NULL,
    REPEAT_INTERVAL BIGINT NOT NULL,
    TIMES_TRIGGERED BIGINT NOT NULL,
    CONSTRAINT pk_qrtz_simple_triggers PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT fk_qrtz_simple_triggers_triggers FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES QRTZ_TRIGGERS (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_cron_triggers
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    CRON_EXPRESSION VARCHAR(120) NOT NULL,
    TIME_ZONE_ID VARCHAR(80),
    CONSTRAINT pk_qrtz_cron_triggers PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT fk_qrtz_cron_triggers_triggers FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES QRTZ_TRIGGERS (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_simprop_triggers
  (          
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 INTEGER,
    INT_PROP_2 INTEGER,
    LONG_PROP_1 BIGINT,
    LONG_PROP_2 BIGINT,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 BOOL,
    BOOL_PROP_2 BOOL,
    CONSTRAINT pk_qrtz_simprop_triggers PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT fk_qrtz_simprop_triggers_triggers FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
    REFERENCES QRTZ_TRIGGERS (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_blob_triggers
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    BLOB_DATA BYTEA,
    CONSTRAINT pk_qrtz_blob_triggers PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    CONSTRAINT fk_qrtz_blob_triggers_triggers FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP) 
        REFERENCES QRTZ_TRIGGERS (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_calendars
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    CALENDAR_NAME  VARCHAR(200) NOT NULL, 
    CALENDAR BYTEA NOT NULL,
    CONSTRAINT pk_qrtz_calendars PRIMARY KEY (SCHED_NAME,CALENDAR_NAME)
);


CREATE TABLE qrtz_paused_trigger_grps
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_GROUP  VARCHAR(200) NOT NULL, 
    CONSTRAINT pk_qrtz_paused_trigger_grps PRIMARY KEY (SCHED_NAME,TRIGGER_GROUP)
);

CREATE TABLE qrtz_fired_triggers 
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    ENTRY_ID VARCHAR(95) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    FIRED_TIME BIGINT NOT NULL,
    SCHED_TIME BIGINT NOT NULL,
    PRIORITY INTEGER NOT NULL,
    STATE VARCHAR(16) NOT NULL,
    JOB_NAME VARCHAR(200) NULL,
    JOB_GROUP VARCHAR(200) NULL,
    IS_NONCONCURRENT BOOL,
    REQUESTS_RECOVERY BOOL,
    CONSTRAINT pk_qrtz_fired_triggers PRIMARY KEY (SCHED_NAME,ENTRY_ID)
);

CREATE TABLE qrtz_scheduler_state 
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT NOT NULL,
    CHECKIN_INTERVAL BIGINT NOT NULL,
    CONSTRAINT pk_qrtz_scheduler_state PRIMARY KEY (SCHED_NAME,INSTANCE_NAME)
);

CREATE TABLE qrtz_locks
  (
    SCHED_NAME VARCHAR(120) NOT NULL,
    LOCK_NAME  VARCHAR(40) NOT NULL, 
    CONSTRAINT pk_qrtz_locks PRIMARY KEY (SCHED_NAME,LOCK_NAME)
);

create index idx_qrtz_j_req_recovery on qrtz_job_details(SCHED_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_j_grp on qrtz_job_details(SCHED_NAME,JOB_GROUP);

create index idx_qrtz_t_j on qrtz_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_t_jg on qrtz_triggers(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_t_c on qrtz_triggers(SCHED_NAME,CALENDAR_NAME);
create index idx_qrtz_t_g on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP);
create index idx_qrtz_t_state on qrtz_triggers(SCHED_NAME,TRIGGER_STATE);
create index idx_qrtz_t_n_state on qrtz_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_t_n_g_state on qrtz_triggers(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
create index idx_qrtz_t_next_fire_time on qrtz_triggers(SCHED_NAME,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_st on qrtz_triggers(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME);
create index idx_qrtz_t_nft_st_misfire on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
create index idx_qrtz_t_nft_st_misfire_grp on qrtz_triggers(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);

create index idx_qrtz_ft_trig_inst_name on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME);
create index idx_qrtz_ft_inst_job_req_rcvry on qrtz_fired_triggers(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
create index idx_qrtz_ft_j_g on qrtz_fired_triggers(SCHED_NAME,JOB_NAME,JOB_GROUP);
create index idx_qrtz_ft_jg on qrtz_fired_triggers(SCHED_NAME,JOB_GROUP);
create index idx_qrtz_ft_t_g on qrtz_fired_triggers(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
create index idx_qrtz_ft_tg on qrtz_fired_triggers(SCHED_NAME,TRIGGER_GROUP);

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


--##################################################################
--# NCS component tables
--##################################################################

CREATE TABLE ncscomponent (
    id integer NOT NULL,
    version integer,
    name character varying(255),
    type character varying(255),
    foreignsource character varying(255),
    foreignid character varying(255),
    depsrequired character varying(12),
    nodeforeignsource character varying(64),
    nodeforeignid character varying(64),
    upeventuei character varying(255),
    downeventuei character varying(255)
);

ALTER TABLE ncscomponent ADD CONSTRAINT ncscomponent_type_foreignsource_foreignid_key UNIQUE (type, foreignsource, foreignid);


CREATE TABLE ncs_attributes (
    ncscomponent_id integer NOT NULL,
    key character varying(255) NOT NULL,
    value character varying(255) NOT NULL
);

ALTER TABLE ncs_attributes ADD CONSTRAINT ncs_attributes_pkey PRIMARY KEY (ncscomponent_id, key);


CREATE TABLE subcomponents (
    component_id integer NOT NULL,
    subcomponent_id integer NOT NULL
);

ALTER TABLE subcomponents ADD CONSTRAINT subcomponents_pkey PRIMARY KEY (component_id, subcomponent_id);
ALTER TABLE subcomponents ADD CONSTRAINT subcomponents_component_id_subcomponent_id_key UNIQUE (component_id, subcomponent_id);

--##################################################################
--# Business Service Monitor (BSM) tables
--##################################################################

CREATE TABLE bsm_reduce (
    id integer NOT NULL,
    type character varying(32) NOT NULL,
    threshold float,
    threshold_severity integer,
    base float,
    CONSTRAINT bsm_reduce_pkey PRIMARY KEY (id)
);

CREATE TABLE bsm_map (
    id integer NOT NULL,
    type character varying(32) NOT NULL,
    severity integer,
    CONSTRAINT bsm_map_pkey PRIMARY KEY (id)
);

CREATE TABLE bsm_service (
    id integer NOT NULL,
    name character varying(255) NOT NULL UNIQUE,
    bsm_reduce_id integer NOT NULL,
    CONSTRAINT bsm_services_pkey PRIMARY KEY (id),
    CONSTRAINT fk_bsm_service_reduce_id FOREIGN KEY (bsm_reduce_id) REFERENCES bsm_reduce (id)
);

CREATE TABLE bsm_service_attributes (
    bsm_service_id integer NOT NULL,
    key character varying(255) NOT NULL,
    value TEXT NOT NULL,
    CONSTRAINT bsm_service_attributes_pkey PRIMARY KEY (bsm_service_id, key),
    CONSTRAINT fk_bsm_service_attributes_service_id FOREIGN KEY (bsm_service_id)
    REFERENCES bsm_service (id)
);

CREATE TABLE bsm_service_edge (
    id integer NOT NULL,
    enabled boolean NOT NULL,
    weight integer NOT NULL,
    bsm_map_id integer NOT NULL,
    bsm_service_id integer NOT NULL,
    CONSTRAINT bsm_service_edge_pkey PRIMARY KEY (id),
    CONSTRAINT fk_bsm_service_edge_map_id FOREIGN KEY (bsm_map_id)
    REFERENCES bsm_map (id),
    CONSTRAINT fk_bsm_service_edge_service_id FOREIGN KEY (bsm_service_id)
    REFERENCES bsm_service (id) ON DELETE CASCADE
);

CREATE TABLE bsm_service_ifservices (
    id integer NOT NULL,
    ifserviceid integer NOT NULL,
    friendlyname varchar(255),
    CONSTRAINT bsm_service_ifservices_pkey PRIMARY KEY (id),
    CONSTRAINT fk_bsm_service_ifservices_edge_id FOREIGN KEY (id)
    REFERENCES bsm_service_edge (id) ON DELETE CASCADE,
    CONSTRAINT fk_bsm_service_ifservices_ifserviceid FOREIGN KEY (ifserviceid)
    REFERENCES ifservices (id) ON DELETE CASCADE
);

CREATE TABLE bsm_service_reductionkeys (
    id integer NOT NULL,
    reductionkey TEXT NOT NULL,
    friendlyname varchar(255),
    CONSTRAINT bsm_service_reductionkeys_pkey PRIMARY KEY (id),
    CONSTRAINT fk_bsm_service_reductionkeys_edge_id FOREIGN KEY (id)
    REFERENCES bsm_service_edge (id) ON DELETE CASCADE
);

CREATE TABLE bsm_service_children (
      id integer NOT NULL,
      bsm_service_child_id integer NOT NULL,
      CONSTRAINT bsm_service_children_pkey PRIMARY KEY (id),
      CONSTRAINT fk_bsm_service_children_edge_id FOREIGN KEY (id)
      REFERENCES bsm_service_edge (id) ON DELETE CASCADE,
      CONSTRAINT fk_bsm_service_child_service_id FOREIGN KEY (bsm_service_child_id)
      REFERENCES bsm_service (id) ON DELETE CASCADE
);

--##################################################################
--# Topology tables
--##################################################################

-- Layout table
CREATE TABLE topo_layout (
	id varchar(255) NOT NULL,
	created timestamp NOT NULL,
	creator varchar(255) NOT NULL,
	updated timestamp NOT NULL,
	updator varchar(255) NOT NULL,
	last_used timestamp,
	CONSTRAINT topo_layout_pkey PRIMARY KEY (id)
);

-- Layout coordinates of vertex
CREATE TABLE topo_vertex_position (
	id integer NOT NULL,
	x integer NOT NULL,
	y integer NOT NULL,
	vertex_namespace varchar(255) NULL,
	vertex_id varchar(255) NULL,
	CONSTRAINT topo_vertex_position_pkey PRIMARY KEY (id)
);

-- Relation table (layout -> vertex positions)
CREATE TABLE topo_layout_vertex_positions (
  vertex_position_id integer NOT NULL,
	layout_id varchar(255) NOT NULL,
	CONSTRAINT fk_topo_layout_vertex_positions_layout_id FOREIGN KEY (layout_id)
	REFERENCES topo_layout (id) ON DELETE CASCADE,
	CONSTRAINT fk_topo_layout_vertex_positions_vertex_position_id FOREIGN KEY (vertex_position_id)
	REFERENCES topo_vertex_position (id) ON DELETE CASCADE
);

--##################################################################
--# Status views
--##################################################################

CREATE VIEW node_alarm_status AS SELECT node.nodeid,
  COALESCE(
        (SELECT max(
              CASE
                  WHEN alarms.severity IS NULL OR alarms.severity < 3 THEN 3
                  ELSE alarms.severity
              END)
        FROM alarms
        WHERE alarms.nodeid = node.nodeid), 3) AS max_alarm_severity,
  COALESCE(
        (SELECT max(
              CASE
                  WHEN alarms.severity IS NULL OR alarms.severity < 3 THEN 3
                  ELSE alarms.severity
              END)
         FROM alarms
         WHERE alarms.nodeid = node.nodeid AND alarms.alarmacktime IS NULL), 3) AS max_alarm_severity_unack,
  (SELECT count(alarms.alarmid)
         FROM alarms
        WHERE alarms.nodeid = node.nodeid AND alarms.alarmacktime IS NULL) AS alarm_count_unack,
  (SELECT count(*)
         FROM alarms
        WHERE alarms.nodeid = node.nodeid) AS alarm_count
 FROM node;

CREATE VIEW node_outage_status AS
 SELECT node.nodeid,
      CASE
          WHEN tmp.severity IS NULL OR tmp.severity < 3 THEN 3
          ELSE tmp.severity
      END AS max_outage_severity
 FROM ( SELECT events.nodeid,
          max(events.eventseverity) AS severity
         FROM events
           JOIN outages ON outages.svclosteventid = events.eventid
        WHERE outages.svcregainedeventid IS NULL
        GROUP BY events.nodeid) tmp
 RIGHT JOIN node ON tmp.nodeid = node.nodeid;

--##################################################################
--# Classification tables
--##################################################################
CREATE TABLE classification_groups (
  id integer not null,
  name text not null,
  readonly boolean,
  enabled boolean,
  priority integer not null,
  description text,
  CONSTRAINT classification_groups_pkey PRIMARY KEY (id)
);
ALTER TABLE classification_groups ADD CONSTRAINT classification_groups_name_key UNIQUE (name);

CREATE TABLE classification_rules (
  id integer NOT NULL,
  name TEXT NOT NULL,
  dst_address TEXT,
  dst_port TEXT,
  src_address TEXT,
  src_port TEXT,
  exporter_filter TEXT,
  protocol TEXT,
  position integer not null,
  groupid integer NOT NULL,
  CONSTRAINT classification_rules_pkey PRIMARY KEY (id),
  CONSTRAINT fk_classification_rules_groupid FOREIGN KEY (groupId) REFERENCES classification_groups (id) ON DELETE CASCADE
);
ALTER TABLE classification_rules ADD CONSTRAINT classification_rules_unique_definition_key UNIQUE (dst_address,dst_port,src_address,src_port,protocol,exporter_filter,groupid);

--# Sequence for the id column in classification_rules table
--#          sequence, column, table
--# install: rulenxtid id classification_rules
create sequence rulenxtid minvalue 1;

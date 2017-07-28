/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rest.support;

import static org.opennms.web.rest.support.SearchProperty.SearchPropertyType.FLOAT;
import static org.opennms.web.rest.support.SearchProperty.SearchPropertyType.INTEGER;
import static org.opennms.web.rest.support.SearchProperty.SearchPropertyType.IP_ADDRESS;
import static org.opennms.web.rest.support.SearchProperty.SearchPropertyType.LONG;
import static org.opennms.web.rest.support.SearchProperty.SearchPropertyType.STRING;
import static org.opennms.web.rest.support.SearchProperty.SearchPropertyType.TIMESTAMP;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.opennms.netmgt.model.OnmsSeverity;

/**
 * @author Seth
 */
public abstract class SearchProperties {

	/**
	 * {@link OnmsSeverity} as a {@link Map}.
	 */
	private static final Map<String,String> ONMS_SEVERITIES = Arrays.stream(OnmsSeverity.values()).collect(Collectors.toMap(s -> String.valueOf(s.getId()), OnmsSeverity::getLabel));

	private static final SortedSet<SearchProperty> ALARM_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", STRING),
		new SearchProperty("alarmAckTime", "Acknowledged Time", TIMESTAMP),
		new SearchProperty("alarmAckUser", "Acknowledging User", STRING),
		new SearchProperty("alarmType", "Alarm Type", INTEGER),
		new SearchProperty("applicationDN", "Application DN", STRING),
		new SearchProperty("clearKey", "Clear Key", STRING),
		new SearchProperty("counter", "Event Counter", INTEGER),
		new SearchProperty("description", "Description", STRING),
		new SearchProperty("firstAutomationTime", "First Automation Time", TIMESTAMP),
		new SearchProperty("firstEventTime", "First Event Time", TIMESTAMP),
		new SearchProperty("ifIndex", "SNMP Interface Index", INTEGER),
		new SearchProperty("ipAddr", "IP Address", IP_ADDRESS),
		new SearchProperty("lastAutomationTime", "Last Automation Time", TIMESTAMP),
		new SearchProperty("lastEventTime", "Last Event Time", TIMESTAMP),
		new SearchProperty("logMsg", "Log Message", STRING),
		new SearchProperty("managedObjectInstance", "Managed Object Instance", STRING),
		new SearchProperty("managedObjectType", "Managed Object Type", STRING),
		new SearchProperty("mouseOverText", "Mouseover Text", STRING),
		new SearchProperty("operInstruct", "Operator Instructions", STRING),
		new SearchProperty("ossPrimaryKey", "OSS Primary Key", STRING),
		new SearchProperty("qosAlarmState", "QoS Alarm State", STRING),
		new SearchProperty("reductionKey", "Reduction Key", STRING),
		new SearchProperty("severity", "Severity", INTEGER, ONMS_SEVERITIES),
		new SearchProperty("suppressedTime", "Suppressed Time", TIMESTAMP),
		new SearchProperty("suppressedUntil", "Suppressed Until", TIMESTAMP),
		new SearchProperty("suppressedUser", "Suppressed User", STRING),
		new SearchProperty("uei", "UEI", STRING),
		new SearchProperty("x733AlarmType", "X.733 Alarm Type", STRING),
		new SearchProperty("x733ProbableCause", "X.733 Probable Cause", INTEGER)
	}));

	private static final SortedSet<SearchProperty> ASSET_RECORD_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("additionalhardware", "Additional Hardware", STRING),
		//new SearchProperty("address1", "Address 1", STRING),
		//new SearchProperty("address2", "Address 2", STRING),
		new SearchProperty("admin", "Admin", STRING),
		new SearchProperty("assetNumber", "Asset Number", STRING),
		new SearchProperty("autoenable", "Auto-enable", STRING),
		new SearchProperty("building", "Building", STRING),
		new SearchProperty("category", "Category", STRING),
		new SearchProperty("circuitId", "Circuit ID", STRING),
		//new SearchProperty("city", "City", STRING),
		new SearchProperty("comment", "Comment", STRING),
		new SearchProperty("connection", "Connection", STRING),
		//new SearchProperty("country", "Country", STRING),
		new SearchProperty("cpu", "CPU", STRING),
		new SearchProperty("dateInstalled", "Date Installed", STRING),
		new SearchProperty("department", "Department", STRING),
		new SearchProperty("description", "Description", STRING),
		new SearchProperty("displayCategory", "Display Category", STRING),
		new SearchProperty("division", "Division", STRING),
		new SearchProperty("enable", "Enable", STRING),
		new SearchProperty("floor", "Floor", STRING),
		//new SearchProperty("geolocation", "", ?),
		new SearchProperty("hdd1", "HDD 1", STRING),
		new SearchProperty("hdd2", "HDD 2", STRING),
		new SearchProperty("hdd3", "HDD 3", STRING),
		new SearchProperty("hdd4", "HDD 4", STRING),
		new SearchProperty("hdd5", "HDD 5", STRING),
		new SearchProperty("hdd6", "HDD 6", STRING),
		new SearchProperty("inputpower", "Input Power", STRING),
		new SearchProperty("lastModifiedBy", "Last Modified By", STRING),
		new SearchProperty("lastModifiedDate", "Last Modified Date", TIMESTAMP),
		//new SearchProperty("latitude", "Latitude", FLOAT),
		new SearchProperty("lease", "Lease", STRING),
		new SearchProperty("leaseExpires", "Lease Expires", STRING),
		//new SearchProperty("longitude", "Longitude", FLOAT),
		new SearchProperty("maintcontract", "Maintenance Contract", STRING),
		new SearchProperty("maintContractExpiration", "Maintenance Contract Expiration", STRING),
		new SearchProperty("maintContractNumber", "Maintenance Contract Number", STRING),
		new SearchProperty("managedObjectInstance", "Managed Object Instance", STRING),
		new SearchProperty("managedObjectType", "Managed Object Type", STRING),
		new SearchProperty("manufacturer", "Manufacturer", STRING),
		new SearchProperty("modelNumber", "Model Number", STRING),
		new SearchProperty("notifyCategory", "Notify Category", STRING),
		new SearchProperty("numpowersupplies", "Number of Power Supplies", STRING),
		new SearchProperty("operatingSystem", "Operating System", STRING),
		new SearchProperty("password", "Password", STRING),
		new SearchProperty("pollerCategory", "Poller Category", STRING),
		new SearchProperty("port", "Port", STRING),
		new SearchProperty("rack", "Rack", STRING),
		new SearchProperty("rackunitheight", "Rack Unit Height", STRING),
		new SearchProperty("ram", "RAM", STRING),
		new SearchProperty("region", "Region", STRING),
		new SearchProperty("room", "Room", STRING),
		new SearchProperty("serialNumber", "Serial Number", STRING),
		new SearchProperty("slot", "Slot", STRING),
		new SearchProperty("snmpcommunity", "SNMP Community", STRING),
		//new SearchProperty("state", "State or Province", STRING),
		new SearchProperty("storagectrl", "Storage Controller", STRING),
		new SearchProperty("supportPhone", "Support Phone", STRING),
		new SearchProperty("thresholdCategory", "Threshold Category", STRING),
		new SearchProperty("username", "Username", STRING),
		new SearchProperty("vendor", "Vendor", STRING),
		new SearchProperty("vendorAssetNumber", "Vendor Asset Number", STRING),
		new SearchProperty("vendorFax", "Vendor Fax", STRING),
		new SearchProperty("vendorPhone", "Vendor Phone", STRING),
		new SearchProperty("vmwareManagedEntityType", "VMware Managed Entity Type", STRING),
		new SearchProperty("vmwareManagedObjectId", "VMware Managed Object ID", STRING),
		new SearchProperty("vmwareManagementServer", "VMware Management Server", STRING),
		new SearchProperty("vmwareState", "VMware State", STRING),
		new SearchProperty("vmwareTopologyInfo", "VMware Topology Information", STRING)
		//new SearchProperty("zip", "ZIP or Postal Code", STRING)
	}));

	private static final SortedSet<SearchProperty> CATEGORY_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("description", "Description", STRING),
		new SearchProperty("name", "Name", STRING)
	}));

	private static final SortedSet<SearchProperty> DIST_POLLER_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("label", "Label for the system", STRING),
		//new SearchProperty("lastUpdated", "Last Updated", TIMESTAMP),
		new SearchProperty("location", "ID of the monitoring location that this Minion is assigned to", STRING)
	}));

	private static final SortedSet<SearchProperty> EVENT_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("eventAckTime", "Acknowledged Time", TIMESTAMP),
		new SearchProperty("eventAckUser", "Acknowledging User", STRING),
		new SearchProperty("eventAutoAction", "Autoaction", STRING),
		new SearchProperty("eventCorrelation", "Correlation", STRING),
		new SearchProperty("eventCreateTime", "Creation Time", TIMESTAMP),
		new SearchProperty("eventDescr", "Description", STRING),
		new SearchProperty("eventDisplay", "Display TODO", STRING),
		new SearchProperty("eventForward", "Forward TODO", STRING),
		new SearchProperty("eventHost", "Host", STRING),
		new SearchProperty("eventLog", "Log TODO", STRING),
		new SearchProperty("eventLogGroup", "Log Group", STRING),
		new SearchProperty("eventLogMsg", "Log Message", STRING),
		new SearchProperty("eventMouseOverText", "Mouseover Text", STRING),
		new SearchProperty("eventNotification", "Notification", STRING),
		new SearchProperty("eventOperAction", "Operator Action", STRING),
		new SearchProperty("eventOperActionMenuText", "Operator Action Menu Text", STRING),
		new SearchProperty("eventOperInstruct", "Operator Instructions", STRING),
		new SearchProperty("eventPathOutage", "Path Outage", STRING),
		new SearchProperty("eventSeverity", "Severity (TODO: Enumerate values) ", INTEGER),
		new SearchProperty("eventSnmp", "SNMP TODO", STRING),
		new SearchProperty("eventSnmpHost", "SNMP Host", STRING),
		new SearchProperty("eventSource", "Event Source", STRING),
		new SearchProperty("eventSuppressedCount", "Suppressed Count", INTEGER),
		new SearchProperty("eventTime", "Event Time", TIMESTAMP),
		new SearchProperty("eventTTicket", "Trouble Ticket ID", STRING),
		new SearchProperty("eventTTicketState", "Trouble Ticket State (TODO: Enumerate values) ", INTEGER),
		new SearchProperty("eventUei", "UEI", STRING),
		new SearchProperty("ifIndex", "ifIndex", INTEGER),
		new SearchProperty("ipAddr", "IP Address", IP_ADDRESS)
	}));

	private static final SortedSet<SearchProperty> IF_SERVICE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("lastFail", "Last Failure Time", TIMESTAMP),
		new SearchProperty("lastGood", "Last Good Time", TIMESTAMP),
		new SearchProperty("notify", "? (TODO: Enumerate values)", STRING),
		new SearchProperty("qualifier", "? TODO", STRING),
		new SearchProperty("source", "? (TODO: Enumerate values)", STRING),
		new SearchProperty("status", "Management status of the service (TODO: Enumerate values)", STRING)
	}));

	private static final SortedSet<SearchProperty> IP_INTERFACE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("ipAddress", "IPv4 or IPv6 address of the interface", IP_ADDRESS),
		new SearchProperty("ipHostName", "Hostname", STRING),
		new SearchProperty("ipLastCapsdPoll", "Time of last provisioning scan", TIMESTAMP),
		new SearchProperty("isManaged", "Management status", STRING)
	}));

	private static final SortedSet<SearchProperty> LOCATION_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("locationName", "ID", STRING),
		new SearchProperty("geolocation", "Geographic address of the location", STRING),
		new SearchProperty("latitude", "Latitude", FLOAT),
		new SearchProperty("longitude", "Longitude", FLOAT),
		new SearchProperty("monitoringArea", "Monitoring Area", STRING),
		new SearchProperty("priority", "Priority in UI", INTEGER)
	}));

	private static final SortedSet<SearchProperty> MINION_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("label", "Label for the system", STRING),
		new SearchProperty("lastUpdated", "Timestamp of the last heartbeat communication with the system", TIMESTAMP),
		new SearchProperty("location", "ID of the monitoring location that this Minion is assigned to", STRING),
		new SearchProperty("status", "Status", STRING)
	}));

	private static final SortedSet<SearchProperty> NODE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("createTime", "Creation time for the node", TIMESTAMP),
		new SearchProperty("foreignId", "Foreign ID", STRING),
		new SearchProperty("foreignSource", "Foreign source", STRING),
		new SearchProperty("label", "Node label", STRING),
		new SearchProperty("labelSource", "Source for the label (TODO: Enumerate values)", STRING),
		new SearchProperty("lastCapsdPoll", "Time of last provisioning scan", TIMESTAMP),
		new SearchProperty("netBiosDomain", "Windows domain of the node", STRING),
		new SearchProperty("netBiosName", "Windows name for the node", STRING),
		new SearchProperty("operatingSystem", "Operating system", STRING),
		//new SearchProperty("parent", "?", ?),
		//new SearchProperty("pathElement", "?", ?),
		new SearchProperty("sysContact", "SNMP sysContact field", STRING),
		new SearchProperty("sysDescription", "SNMP sysDescription field", STRING),
		new SearchProperty("sysLocation", "SNMP sysLocation field", STRING),
		new SearchProperty("sysName", "SNMP sysName field", STRING),
		new SearchProperty("sysObjectId", "SNMP sysObjectId", STRING),
		//new SearchProperty("type", "?", ?)
	}));

	private static final SortedSet<SearchProperty> NOTIFICATION_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("notifyId", "ID", INTEGER),
		new SearchProperty("answeredBy", "Answered By", STRING),
		new SearchProperty("ipAddress", "IP Address", IP_ADDRESS),
		new SearchProperty("numericMsg", "Numeric Message", STRING),
		new SearchProperty("pageTime", "Page Time", TIMESTAMP),
		new SearchProperty("queueId", "Queue ID", STRING),
		new SearchProperty("respondTime", "Responded Time", TIMESTAMP),
		new SearchProperty("subject", "Subject", STRING),
		new SearchProperty("textMsg", "Text Message", STRING)
	}));

	private static final SortedSet<SearchProperty> OUTAGE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("ifLostService", "Lost Service Time", TIMESTAMP),
		new SearchProperty("ifRegainedService", "Regained Service Time", TIMESTAMP),
		new SearchProperty("suppressedBy", "Suppressed By User", STRING),
		new SearchProperty("suppressTime", "Suppressed Time", TIMESTAMP)
	}));

	private static final SortedSet<SearchProperty> SCAN_REPORT_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", STRING),
		new SearchProperty("locale", "Locale for the report", STRING),
		new SearchProperty("location", "ID of the monitoring location that this report was generated for", STRING),
		new SearchProperty("timestamp", "Timestamp of the report", TIMESTAMP)
	}));

	private static final SortedSet<SearchProperty> SERVICE_TYPE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("name", "Service name", STRING)
	}));

	private static final SortedSet<SearchProperty> SNMP_INTERFACE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("ifAdminStatus", "Admin status", INTEGER),
		new SearchProperty("ifIndex", "Interface index", INTEGER),
		new SearchProperty("ifOperStatus", "Operational status", INTEGER),
		new SearchProperty("ifSpeed", "Bits-per-second speed for the interface", LONG),
		new SearchProperty("lastCapsdPoll", "Time of last provisioning scan", TIMESTAMP),
		new SearchProperty("lastSnmpPoll", "?", TIMESTAMP),
		new SearchProperty("netMask", "IP address representing the netmask of the interface", IP_ADDRESS)
	}));

	public static final SortedSet<SearchProperty> ALARM_SERVICE_PROPERTIES;

	/**
	 * Prepend a join alias to the property ID for each {@link SearchProperty}.
	 * 
	 * @param alias
	 * @param properties
	 * @return
	 */
	private static final Set<SearchProperty> withAliasPrefix(Aliases alias, Set<SearchProperty> properties) {
		return properties.stream().map(p -> { return new SearchProperty(alias.prop(p.id), p.name, p.type, p.values); }).collect(Collectors.toSet());
	}

	/**
	 * Prepend a join alias to the property ID for each {@link SearchProperty}.
	 * 
	 * @param alias
	 * @param properties
	 * @return
	 */
	private static final Set<SearchProperty> withAliasPrefix(String alias, Set<SearchProperty> properties) {
		return properties.stream().map(p -> { return new SearchProperty(alias + "." + p.id, p.name, p.type, p.values); }).collect(Collectors.toSet());
	}

	static {
		ALARM_SERVICE_PROPERTIES = new TreeSet<>();
		// Root prefix
		ALARM_SERVICE_PROPERTIES.addAll(ALARM_PROPERTIES);
		//ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.alarm, ALARM_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.assetRecord, ASSET_RECORD_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.category, CATEGORY_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.distPoller, DIST_POLLER_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.ipInterface, IP_INTERFACE_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix("lastEvent", EVENT_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.location, LOCATION_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.node, NODE_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.serviceType, SERVICE_TYPE_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.snmpInterface, SNMP_INTERFACE_PROPERTIES));
	}
}

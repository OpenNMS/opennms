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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.OnmsSeverity;

import com.google.common.collect.ImmutableMap;

/**
 * @author Seth
 */
public abstract class SearchProperties {

	/**
	 * {@link OnmsSeverity} as a {@link Map}.
	 */
	private static final Map<String,String> ONMS_SEVERITIES = Arrays.stream(OnmsSeverity.values()).collect(Collectors.toMap(s -> String.valueOf(s.getId()), OnmsSeverity::getLabel));

	static final SortedSet<SearchProperty> ALARM_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("alarmAckTime", "Acknowledged Time", TIMESTAMP),
		new SearchProperty("alarmAckUser", "Acknowledging User", STRING),
		new SearchProperty("alarmType", "Alarm Type", INTEGER, ImmutableMap.<String,String>builder()
			.put(String.valueOf(OnmsAlarm.PROBLEM_TYPE), "Problem")
			.put(String.valueOf(OnmsAlarm.RESOLUTION_TYPE), "Resolution")
			.build()
		),
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

	static final SortedSet<SearchProperty> APPLICATION_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("name", "Name", STRING)
	}));

	static final SortedSet<SearchProperty> ASSET_RECORD_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
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

	static final SortedSet<SearchProperty> CATEGORY_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("description", "Description", STRING),
		new SearchProperty("name", "Name", STRING)
	}));

	static final SortedSet<SearchProperty> DIST_POLLER_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("label", "Label", STRING),
		new SearchProperty("lastUpdated", "Last Updated", TIMESTAMP),
		new SearchProperty("location", "Monitoring Location", STRING)
	}));

	static final SortedSet<SearchProperty> EVENT_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("eventAckTime", "Acknowledged Time", TIMESTAMP),
		new SearchProperty("eventAckUser", "Acknowledging User", STRING),
		new SearchProperty("eventAutoAction", "Autoaction", STRING),
		new SearchProperty("eventCorrelation", "Correlation", STRING),
		new SearchProperty("eventCreateTime", "Creation Time", TIMESTAMP),
		new SearchProperty("eventDescr", "Description", STRING),
		new SearchProperty("eventDisplay", "Display", STRING, ImmutableMap.<String,String>builder()
			.put("Y", "Yes")
			.put("N", "No")
			.build()
		),
		// This field has an unusual format with fields from the eventconf
		new SearchProperty("eventForward", "Forward", STRING),
		new SearchProperty("eventHost", "Host", STRING),
		new SearchProperty("eventLog", "Log", STRING, ImmutableMap.<String,String>builder()
			.put("Y", "Yes")
			.put("N", "No")
			.build()
		),
		new SearchProperty("eventLogGroup", "Log Group", STRING),
		new SearchProperty("eventLogMsg", "Log Message", STRING),
		new SearchProperty("eventMouseOverText", "Mouseover Text", STRING),
		new SearchProperty("eventNotification", "Notification", STRING),
		new SearchProperty("eventOperAction", "Operator Action", STRING),
		new SearchProperty("eventOperActionMenuText", "Operator Action Menu Text", STRING),
		new SearchProperty("eventOperInstruct", "Operator Instructions", STRING),
		new SearchProperty("eventPathOutage", "Path Outage", STRING),
		new SearchProperty("eventSeverity", "Severity", INTEGER, ONMS_SEVERITIES),
		// This field has an unusual format with fields from the eventconf
		new SearchProperty("eventSnmp", "SNMP", STRING),
		new SearchProperty("eventSnmpHost", "SNMP Host", STRING),
		new SearchProperty("eventSource", "Source", STRING),
		new SearchProperty("eventSuppressedCount", "Suppressed Count", INTEGER),
		new SearchProperty("eventTime", "Time", TIMESTAMP),
		new SearchProperty("eventTTicket", "Trouble Ticket ID", STRING),
		new SearchProperty("eventTTicketState", "Trouble Ticket State", INTEGER, ImmutableMap.<String,String>builder()
			// Can also be null, but that's probably not useful for searching
			.put("0", "Off")
			.put("1", "On")
			.build()
		),
		new SearchProperty("eventUei", "UEI", STRING),
		new SearchProperty("ifIndex", "ifIndex", INTEGER),
		new SearchProperty("ipAddr", "IP Address", IP_ADDRESS)
	}));

	static final SortedSet<SearchProperty> IF_SERVICE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("lastFail", "Last Failure Time", TIMESTAMP),
		new SearchProperty("lastGood", "Last Good Time", TIMESTAMP),
		new SearchProperty("notify", "Notify", STRING, ImmutableMap.<String,String>builder()
			.put("Y", "Yes")
			.put("N", "No")
			.build()
		),
		// Unclear if this value is ever used
		new SearchProperty("qualifier", "Qualifier", STRING),
		new SearchProperty("source", "Detection Source", STRING, ImmutableMap.<String,String>builder()
			.put("P", "Plugin")
			.put("F", "Forced")
			.build()
		),
		new SearchProperty("status", "Management Status", STRING, OnmsMonitoredService.STATUS_MAP)
	}));

	static final SortedSet<SearchProperty> IP_INTERFACE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("ipAddress", "IP Address", IP_ADDRESS),
		new SearchProperty("ipHostName", "Hostname", STRING),
		new SearchProperty("ipLastCapsdPoll", "Last Provisioning Scan", TIMESTAMP),
		new SearchProperty("isManaged", "Management Status", STRING)
	}));

	static final SortedSet<SearchProperty> LOCATION_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("locationName", "ID", STRING),
		new SearchProperty("geolocation", "Geographic Address", STRING),
		new SearchProperty("latitude", "Latitude", FLOAT),
		new SearchProperty("longitude", "Longitude", FLOAT),
		new SearchProperty("monitoringArea", "Monitoring Area", STRING),
		new SearchProperty("priority", "UI Priority", INTEGER)
	}));

	static final SortedSet<SearchProperty> MINION_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("label", "Label", STRING),
		new SearchProperty("lastUpdated", "Last Heartbeat Update", TIMESTAMP),
		new SearchProperty("location", "Monitoring Location", STRING),
		new SearchProperty("status", "Status", STRING)
	}));

	static final SortedSet<SearchProperty> NODE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("createTime", "Creation Time", TIMESTAMP),
		new SearchProperty("foreignId", "Foreign ID", STRING),
		new SearchProperty("foreignSource", "Foreign Source", STRING),
		new SearchProperty("label", "Label", STRING),
		new SearchProperty("labelSource", "Label Source", STRING, ImmutableMap.<String,String>builder()
			.put(String.valueOf(NodeLabelSource.ADDRESS.value()), "IP Address")
			.put(String.valueOf(NodeLabelSource.HOSTNAME.value()), "Hostname")
			.put(String.valueOf(NodeLabelSource.NETBIOS.value()), "NetBIOS")
			.put(String.valueOf(NodeLabelSource.SYSNAME.value()), "SNMP sysName")
			.put(String.valueOf(NodeLabelSource.UNKNOWN.value()), "Unknown")
			.put(String.valueOf(NodeLabelSource.USER.value()), "User-Defined")
			.build()
		),
		new SearchProperty("lastCapsdPoll", "Last Provisioning Scan", TIMESTAMP),
		new SearchProperty("netBiosDomain", "Windows NetBIOS Domain", STRING),
		new SearchProperty("netBiosName", "Windows NetBIOS Name", STRING),
		new SearchProperty("operatingSystem", "Operating System", STRING),
		//new SearchProperty("parent", "?", ?),
		//new SearchProperty("pathElement", "?", ?),
		new SearchProperty("sysContact", "SNMP sysContact", STRING),
		new SearchProperty("sysDescription", "SNMP sysDescription", STRING),
		new SearchProperty("sysLocation", "SNMP sysLocation", STRING),
		new SearchProperty("sysName", "SNMP sysName", STRING),
		new SearchProperty("sysObjectId", "SNMP sysObjectId", STRING),
		new SearchProperty("type", "Type", STRING, ImmutableMap.<String,String>builder()
			.put(String.valueOf(NodeType.ACTIVE.value()), "Active")
			.put(String.valueOf(NodeType.DELETED.value()), "Deleted")
			.put(String.valueOf(NodeType.UNKNOWN.value()), "Unknown")
			.build()
		)
	}));

	static final SortedSet<SearchProperty> NOTIFICATION_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
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

	static final SortedSet<SearchProperty> OUTAGE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("ifLostService", "Lost Service Time", TIMESTAMP),
		new SearchProperty("ifRegainedService", "Regained Service Time", TIMESTAMP),
		new SearchProperty("suppressedBy", "Suppressed By User", STRING),
		new SearchProperty("suppressTime", "Suppressed Time", TIMESTAMP)
	}));

	static final SortedSet<SearchProperty> SCAN_REPORT_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", STRING),
		new SearchProperty("locale", "Locale", STRING),
		new SearchProperty("location", "Monitoring Location", STRING),
		new SearchProperty("timestamp", "Timestamp", TIMESTAMP)
	}));

	static final SortedSet<SearchProperty> SERVICE_TYPE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("name", "Service Name", STRING)
	}));

	static final SortedSet<SearchProperty> SNMP_INTERFACE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty("id", "ID", INTEGER),
		new SearchProperty("ifAdminStatus", "Admin Status", INTEGER),
		new SearchProperty("ifIndex", "Interface Index", INTEGER),
		new SearchProperty("ifOperStatus", "Operational Status", INTEGER),
		new SearchProperty("ifSpeed", "Interface Speed (Bits per second)", LONG),
		new SearchProperty("lastCapsdPoll", "Last Provisioning Scan", TIMESTAMP),
		new SearchProperty("lastSnmpPoll", "Last SNMP Interface Poll", TIMESTAMP),
		new SearchProperty("netMask", "Network Mask", IP_ADDRESS)
	}));

	public static final Set<SearchProperty> ALARM_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> APPLICATION_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> EVENT_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> IF_SERVICE_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> MINION_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> LOCATION_SERVICE_PROPERTIES = new LinkedHashSet<>();
	//public static final Set<SearchProperty> NODE_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> NOTIFICATION_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> OUTAGE_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> SCAN_REPORT_SERVICE_PROPERTIES = new LinkedHashSet<>();

	/**
	 * Prepend a join alias to the property ID for each {@link SearchProperty}.
	 * 
	 * @param alias
	 * @param properties
	 * @return
	 */
	static final Set<SearchProperty> withAliasPrefix(Aliases alias, String namePrefix, Set<SearchProperty> properties) {
		return withAliasPrefix(alias, namePrefix, properties, SearchProperty.DEFAULT_ORDER_BY);
	}

	/**
	 * Prepend a join alias to the property ID for each {@link SearchProperty}.
	 * 
	 * @param alias
	 * @param properties
	 * @param orderBy
	 * @return
	 */
	private static final SortedSet<SearchProperty> withAliasPrefix(Aliases alias, String namePrefix, Set<SearchProperty> properties, boolean orderBy) {
		return new TreeSet<>(properties.stream().map(p -> { return new SearchProperty(
			alias.prop(p.id),
			namePrefix == null ? p.name : namePrefix + ": " + p.name,
			p.type,
			orderBy,
			p.values
		); }).collect(Collectors.toSet()));
	}

	/**
	 * Prepend a join alias to the property ID for each {@link SearchProperty}.
	 * 
	 * @param alias
	 * @param properties
	 * @return
	 */
	private static final Set<SearchProperty> withAliasPrefix(String alias, String namePrefix, Set<SearchProperty> properties) {
		return properties.stream().map(p -> { return new SearchProperty(
			alias + "." + p.id,
			namePrefix == null ? p.name : namePrefix + ": " + p.name,
			p.type,
			p.values
		); }).collect(Collectors.toSet());
	}

	static {
		// Root prefix
		ALARM_SERVICE_PROPERTIES.addAll(ALARM_PROPERTIES);
		//ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.alarm, "Alarm", ALARM_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.assetRecord, "Asset", ASSET_RECORD_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.category, "Category", CATEGORY_PROPERTIES, false));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.distPoller, "Monitoring System", DIST_POLLER_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.ipInterface, "IP Interface", IP_INTERFACE_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix("lastEvent", "Last Event", EVENT_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.location, "Location", LOCATION_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.node, "Node", NODE_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.serviceType, "Service", SERVICE_TYPE_PROPERTIES));
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.snmpInterface, "SNMP Interface", SNMP_INTERFACE_PROPERTIES));

		// Root prefix
		APPLICATION_SERVICE_PROPERTIES.addAll(APPLICATION_PROPERTIES);

		// Root prefix
		EVENT_SERVICE_PROPERTIES.addAll(EVENT_PROPERTIES);
		//EVENT_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.event, EVENT_PROPERTIES));
		EVENT_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.alarm, "Alarm", ALARM_PROPERTIES));
		EVENT_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.assetRecord, "Asset", ASSET_RECORD_PROPERTIES));
		EVENT_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.category, "Category", CATEGORY_PROPERTIES, false));
		EVENT_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.distPoller, "Monitoring System", DIST_POLLER_PROPERTIES));
		EVENT_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.ipInterface, "IP Interface", IP_INTERFACE_PROPERTIES));
		EVENT_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.location, "Location", LOCATION_PROPERTIES));
		EVENT_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.node, "Node", NODE_PROPERTIES));
		EVENT_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.serviceType, "Service", SERVICE_TYPE_PROPERTIES));
		EVENT_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.snmpInterface, "SNMP Interface", SNMP_INTERFACE_PROPERTIES));

		// Root prefix
		IF_SERVICE_SERVICE_PROPERTIES.addAll(IF_SERVICE_PROPERTIES);
		//IF_SERVICE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.monitoredService, IF_SERVICE_PROPERTIES));
		IF_SERVICE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.assetRecord, "Asset", ASSET_RECORD_PROPERTIES));
		IF_SERVICE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.ipInterface, "IP Interface", IP_INTERFACE_PROPERTIES));
		IF_SERVICE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.location, "Location", LOCATION_PROPERTIES));
		IF_SERVICE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.node, "Node", NODE_PROPERTIES));
		IF_SERVICE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.serviceType, "Service", SERVICE_TYPE_PROPERTIES));
		IF_SERVICE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.snmpInterface, "SNMP Interface", SNMP_INTERFACE_PROPERTIES));

		// Root prefix
		MINION_SERVICE_PROPERTIES.addAll(MINION_PROPERTIES);

		// Root prefix
		LOCATION_SERVICE_PROPERTIES.addAll(LOCATION_PROPERTIES);

		// Root prefix
		NOTIFICATION_SERVICE_PROPERTIES.addAll(NOTIFICATION_PROPERTIES);
		//NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.notification, NOTIFICATION_PROPERTIES));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.assetRecord, "Asset", ASSET_RECORD_PROPERTIES));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.category, "Category", CATEGORY_PROPERTIES, false));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.distPoller, "Monitoring System", DIST_POLLER_PROPERTIES));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.event, "Event", EVENT_PROPERTIES));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.ipInterface, "IP Interface", IP_INTERFACE_PROPERTIES));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.location, "Location", LOCATION_PROPERTIES));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.node, "Node", NODE_PROPERTIES));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.serviceType, "Service", SERVICE_TYPE_PROPERTIES));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.snmpInterface, "SNMP Interface", SNMP_INTERFACE_PROPERTIES));

		// Root prefix
		OUTAGE_SERVICE_PROPERTIES.addAll(OUTAGE_PROPERTIES);
		//OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.outage, OUTAGE_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.assetRecord, "Asset", ASSET_RECORD_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.distPoller, "Monitoring System", DIST_POLLER_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.ipInterface, "IP Interface", IP_INTERFACE_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.location, "Location", LOCATION_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.node, "Node", NODE_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix("serviceLostEvent", "Service Lost Event", EVENT_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix("serviceRegainedEvent", "Service Regained Event", EVENT_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.serviceType, "Service", SERVICE_TYPE_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.snmpInterface, "SNMP Interface", SNMP_INTERFACE_PROPERTIES));

		// Root prefix
		SCAN_REPORT_SERVICE_PROPERTIES.addAll(SCAN_REPORT_PROPERTIES);
	}
}

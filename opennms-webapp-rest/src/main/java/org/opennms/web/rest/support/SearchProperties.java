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
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ScanReport;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

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
		new SearchProperty(OnmsAlarm.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsAlarm.class, "alarmAckTime", "Acknowledged Time", TIMESTAMP),
		new SearchProperty(OnmsAlarm.class, "alarmAckUser", "Acknowledging User", STRING),
		new SearchProperty(OnmsAlarm.class, "alarmType", "Alarm Type", INTEGER, ImmutableMap.<String,String>builder()
			.put(String.valueOf(OnmsAlarm.PROBLEM_TYPE), "Problem")
			.put(String.valueOf(OnmsAlarm.RESOLUTION_TYPE), "Resolution")
			.build()
		),
		new SearchProperty(OnmsAlarm.class, "applicationDN", "Application DN", STRING),
		new SearchProperty(OnmsAlarm.class, "clearKey", "Clear Key", STRING),
		new SearchProperty(OnmsAlarm.class, "counter", "Event Counter", INTEGER),
		new SearchProperty(OnmsAlarm.class, "description", "Description", STRING),
		new SearchProperty(OnmsAlarm.class, "firstAutomationTime", "First Automation Time", TIMESTAMP),
		new SearchProperty(OnmsAlarm.class, "firstEventTime", "First Event Time", TIMESTAMP),
		new SearchProperty(OnmsAlarm.class, "ifIndex", "SNMP Interface Index", INTEGER),
		new SearchPropertyBuilder().entityClass(OnmsAlarm.class).id("ipAddr").name("IP Address").type(IP_ADDRESS).iplike(true).build(),
		new SearchProperty(OnmsAlarm.class, "lastAutomationTime", "Last Automation Time", TIMESTAMP),
		new SearchProperty(OnmsAlarm.class, "lastEventTime", "Last Event Time", TIMESTAMP),
		new SearchProperty(OnmsAlarm.class, "logMsg", "Log Message", STRING),
		new SearchProperty(OnmsAlarm.class, "managedObjectInstance", "Managed Object Instance", STRING),
		new SearchProperty(OnmsAlarm.class, "managedObjectType", "Managed Object Type", STRING),
		new SearchProperty(OnmsAlarm.class, "mouseOverText", "Mouseover Text", STRING),
		new SearchProperty(OnmsAlarm.class, "operInstruct", "Operator Instructions", STRING),
		new SearchProperty(OnmsAlarm.class, "ossPrimaryKey", "OSS Primary Key", STRING),
		new SearchProperty(OnmsAlarm.class, "qosAlarmState", "QoS Alarm State", STRING),
		new SearchProperty(OnmsAlarm.class, "reductionKey", "Reduction Key", STRING),
		new SearchProperty(OnmsAlarm.class, "severity", "Severity", INTEGER, ONMS_SEVERITIES),
		new SearchProperty(OnmsAlarm.class, "suppressedTime", "Suppressed Time", TIMESTAMP),
		new SearchProperty(OnmsAlarm.class, "suppressedUntil", "Suppressed Until", TIMESTAMP),
		new SearchProperty(OnmsAlarm.class, "suppressedUser", "Suppressed User", STRING),
		new SearchProperty(OnmsAlarm.class, "uei", "UEI", STRING),
		new SearchProperty(OnmsAlarm.class, "x733AlarmType", "X.733 Alarm Type", STRING),
		new SearchProperty(OnmsAlarm.class, "x733ProbableCause", "X.733 Probable Cause", INTEGER)
	}));

	static final SortedSet<SearchProperty> APPLICATION_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsApplication.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsApplication.class, "name", "Name", STRING)
	}));

	static final SortedSet<SearchProperty> ASSET_RECORD_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsAssetRecord.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsAssetRecord.class, "additionalhardware", "Additional Hardware", STRING),
		//new SearchProperty(OnmsAssetRecord.class, "address1", "Address 1", STRING),
		//new SearchProperty(OnmsAssetRecord.class, "address2", "Address 2", STRING),
		new SearchProperty(OnmsAssetRecord.class, "admin", "Admin", STRING),
		new SearchProperty(OnmsAssetRecord.class, "assetNumber", "Asset Number", STRING),
		new SearchProperty(OnmsAssetRecord.class, "autoenable", "Auto-enable", STRING),
		new SearchProperty(OnmsAssetRecord.class, "building", "Building", STRING),
		new SearchProperty(OnmsAssetRecord.class, "category", "Category", STRING),
		new SearchProperty(OnmsAssetRecord.class, "circuitId", "Circuit ID", STRING),
		//new SearchProperty(OnmsAssetRecord.class, "city", "City", STRING),
		new SearchProperty(OnmsAssetRecord.class, "comment", "Comment", STRING),
		new SearchProperty(OnmsAssetRecord.class, "connection", "Connection", STRING),
		//new SearchProperty(OnmsAssetRecord.class, "country", "Country", STRING),
		new SearchProperty(OnmsAssetRecord.class, "cpu", "CPU", STRING),
		new SearchProperty(OnmsAssetRecord.class, "dateInstalled", "Date Installed", STRING),
		new SearchProperty(OnmsAssetRecord.class, "department", "Department", STRING),
		new SearchProperty(OnmsAssetRecord.class, "description", "Description", STRING),
		new SearchProperty(OnmsAssetRecord.class, "displayCategory", "Display Category", STRING),
		new SearchProperty(OnmsAssetRecord.class, "division", "Division", STRING),
		new SearchProperty(OnmsAssetRecord.class, "enable", "Enable", STRING),
		new SearchProperty(OnmsAssetRecord.class, "floor", "Floor", STRING),
		//new SearchProperty(OnmsAssetRecord.class, "geolocation", "", ?),
		new SearchProperty(OnmsAssetRecord.class, "hdd1", "HDD 1", STRING),
		new SearchProperty(OnmsAssetRecord.class, "hdd2", "HDD 2", STRING),
		new SearchProperty(OnmsAssetRecord.class, "hdd3", "HDD 3", STRING),
		new SearchProperty(OnmsAssetRecord.class, "hdd4", "HDD 4", STRING),
		new SearchProperty(OnmsAssetRecord.class, "hdd5", "HDD 5", STRING),
		new SearchProperty(OnmsAssetRecord.class, "hdd6", "HDD 6", STRING),
		new SearchProperty(OnmsAssetRecord.class, "inputpower", "Input Power", STRING),
		new SearchProperty(OnmsAssetRecord.class, "lastModifiedBy", "Last Modified By", STRING),
		new SearchProperty(OnmsAssetRecord.class, "lastModifiedDate", "Last Modified Date", TIMESTAMP),
		//new SearchProperty(OnmsAssetRecord.class, "latitude", "Latitude", FLOAT),
		new SearchProperty(OnmsAssetRecord.class, "lease", "Lease", STRING),
		new SearchProperty(OnmsAssetRecord.class, "leaseExpires", "Lease Expires", STRING),
		//new SearchProperty(OnmsAssetRecord.class, "longitude", "Longitude", FLOAT),
		new SearchProperty(OnmsAssetRecord.class, "maintcontract", "Maintenance Contract", STRING),
		new SearchProperty(OnmsAssetRecord.class, "maintContractExpiration", "Maintenance Contract Expiration", STRING),
		new SearchProperty(OnmsAssetRecord.class, "managedObjectInstance", "Managed Object Instance", STRING),
		new SearchProperty(OnmsAssetRecord.class, "managedObjectType", "Managed Object Type", STRING),
		new SearchProperty(OnmsAssetRecord.class, "manufacturer", "Manufacturer", STRING),
		new SearchProperty(OnmsAssetRecord.class, "modelNumber", "Model Number", STRING),
		new SearchProperty(OnmsAssetRecord.class, "notifyCategory", "Notify Category", STRING),
		new SearchProperty(OnmsAssetRecord.class, "numpowersupplies", "Number of Power Supplies", STRING),
		new SearchProperty(OnmsAssetRecord.class, "operatingSystem", "Operating System", STRING),
		new SearchProperty(OnmsAssetRecord.class, "password", "Password", STRING),
		new SearchProperty(OnmsAssetRecord.class, "pollerCategory", "Poller Category", STRING),
		new SearchProperty(OnmsAssetRecord.class, "port", "Port", STRING),
		new SearchProperty(OnmsAssetRecord.class, "rack", "Rack", STRING),
		new SearchProperty(OnmsAssetRecord.class, "rackunitheight", "Rack Unit Height", STRING),
		new SearchProperty(OnmsAssetRecord.class, "ram", "RAM", STRING),
		new SearchProperty(OnmsAssetRecord.class, "region", "Region", STRING),
		new SearchProperty(OnmsAssetRecord.class, "room", "Room", STRING),
		new SearchProperty(OnmsAssetRecord.class, "serialNumber", "Serial Number", STRING),
		new SearchProperty(OnmsAssetRecord.class, "slot", "Slot", STRING),
		new SearchProperty(OnmsAssetRecord.class, "snmpcommunity", "SNMP Community", STRING),
		//new SearchProperty(OnmsAssetRecord.class, "state", "State or Province", STRING),
		new SearchProperty(OnmsAssetRecord.class, "storagectrl", "Storage Controller", STRING),
		new SearchProperty(OnmsAssetRecord.class, "supportPhone", "Support Phone", STRING),
		new SearchProperty(OnmsAssetRecord.class, "thresholdCategory", "Threshold Category", STRING),
		new SearchProperty(OnmsAssetRecord.class, "username", "Username", STRING),
		new SearchProperty(OnmsAssetRecord.class, "vendor", "Vendor", STRING),
		new SearchProperty(OnmsAssetRecord.class, "vendorAssetNumber", "Vendor Asset Number", STRING),
		new SearchProperty(OnmsAssetRecord.class, "vendorFax", "Vendor Fax", STRING),
		new SearchProperty(OnmsAssetRecord.class, "vendorPhone", "Vendor Phone", STRING),
		new SearchProperty(OnmsAssetRecord.class, "vmwareManagedEntityType", "VMware Managed Entity Type", STRING),
		new SearchProperty(OnmsAssetRecord.class, "vmwareManagedObjectId", "VMware Managed Object ID", STRING),
		new SearchProperty(OnmsAssetRecord.class, "vmwareManagementServer", "VMware Management Server", STRING),
		new SearchProperty(OnmsAssetRecord.class, "vmwareState", "VMware State", STRING),
		new SearchProperty(OnmsAssetRecord.class, "vmwareTopologyInfo", "VMware Topology Information", STRING)
		//new SearchProperty(OnmsAssetRecord.class, "zip", "ZIP or Postal Code", STRING)
	}));

	static final SortedSet<SearchProperty> CATEGORY_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsCategory.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsCategory.class, "description", "Description", STRING),
		new SearchProperty(OnmsCategory.class, "name", "Name", STRING)
	}));

	static final SortedSet<SearchProperty> DIST_POLLER_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsDistPoller.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsDistPoller.class, "label", "Label", STRING),
		new SearchProperty(OnmsDistPoller.class, "lastUpdated", "Last Updated", TIMESTAMP),
		new SearchProperty(OnmsDistPoller.class, "location", "Monitoring Location", STRING)
	}));

	static final SortedSet<SearchProperty> EVENT_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsEvent.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsEvent.class, "eventAckTime", "Acknowledged Time", TIMESTAMP),
		new SearchProperty(OnmsEvent.class, "eventAckUser", "Acknowledging User", STRING),
		new SearchProperty(OnmsEvent.class, "eventAutoAction", "Autoaction", STRING),
		new SearchProperty(OnmsEvent.class, "eventCorrelation", "Correlation", STRING),
		new SearchProperty(OnmsEvent.class, "eventCreateTime", "Creation Time", TIMESTAMP),
		new SearchProperty(OnmsEvent.class, "eventDescr", "Description", STRING),
		new SearchProperty(OnmsEvent.class, "eventDisplay", "Display", STRING, ImmutableMap.<String,String>builder()
			.put("Y", "Yes")
			.put("N", "No")
			.build()
		),
		// This field has an unusual format with fields from the eventconf
		new SearchProperty(OnmsEvent.class, "eventForward", "Forward", STRING),
		new SearchProperty(OnmsEvent.class, "eventHost", "Host", STRING),
		new SearchProperty(OnmsEvent.class, "eventLog", "Log", STRING, ImmutableMap.<String,String>builder()
			.put("Y", "Yes")
			.put("N", "No")
			.build()
		),
		new SearchProperty(OnmsEvent.class, "eventLogGroup", "Log Group", STRING),
		new SearchProperty(OnmsEvent.class, "eventLogMsg", "Log Message", STRING),
		new SearchProperty(OnmsEvent.class, "eventMouseOverText", "Mouseover Text", STRING),
		new SearchProperty(OnmsEvent.class, "eventNotification", "Notification", STRING),
		new SearchProperty(OnmsEvent.class, "eventOperAction", "Operator Action", STRING),
		new SearchProperty(OnmsEvent.class, "eventOperActionMenuText", "Operator Action Menu Text", STRING),
		new SearchProperty(OnmsEvent.class, "eventOperInstruct", "Operator Instructions", STRING),
		new SearchProperty(OnmsEvent.class, "eventPathOutage", "Path Outage", STRING),
		new SearchProperty(OnmsEvent.class, "eventSeverity", "Severity", INTEGER, ONMS_SEVERITIES),
		// This field has an unusual format with fields from the eventconf
		new SearchProperty(OnmsEvent.class, "eventSnmp", "SNMP", STRING),
		new SearchProperty(OnmsEvent.class, "eventSnmpHost", "SNMP Host", STRING),
		new SearchProperty(OnmsEvent.class, "eventSource", "Source", STRING),
		new SearchProperty(OnmsEvent.class, "eventSuppressedCount", "Suppressed Count", INTEGER),
		new SearchProperty(OnmsEvent.class, "eventTime", "Time", TIMESTAMP),
		new SearchProperty(OnmsEvent.class, "eventTTicket", "Trouble Ticket ID", STRING),
		new SearchProperty(OnmsEvent.class, "eventTTicketState", "Trouble Ticket State", INTEGER, ImmutableMap.<String,String>builder()
			// Can also be null, but that's probably not useful for searching
			.put("0", "Off")
			.put("1", "On")
			.build()
		),
		new SearchProperty(OnmsEvent.class, "eventUei", "UEI", STRING),
		new SearchProperty(OnmsEvent.class, "ifIndex", "ifIndex", INTEGER),
		new SearchPropertyBuilder().entityClass(OnmsEvent.class).id("ipAddr").name("IP Address").type(IP_ADDRESS).iplike(true).build(),
	}));

	static final SortedSet<SearchProperty> EVENT_PARAMETER_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		//new SearchProperty(OnmsEventParameter.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsEventParameter.class, "name", "Name", STRING),
		new SearchProperty(OnmsEventParameter.class, "type", "Type", STRING),
		new SearchProperty(OnmsEventParameter.class, "value", "Value", STRING)
	}));

	static final SortedSet<SearchProperty> IF_SERVICE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsMonitoredService.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsMonitoredService.class, "lastFail", "Last Failure Time", TIMESTAMP),
		new SearchProperty(OnmsMonitoredService.class, "lastGood", "Last Good Time", TIMESTAMP),
		new SearchProperty(OnmsMonitoredService.class, "notify", "Notify", STRING, ImmutableMap.<String,String>builder()
			.put("Y", "Yes")
			.put("N", "No")
			.build()
		),
		// Unclear if this value is ever used
		new SearchProperty(OnmsMonitoredService.class, "qualifier", "Qualifier", STRING),
		new SearchProperty(OnmsMonitoredService.class, "source", "Detection Source", STRING, ImmutableMap.<String,String>builder()
			.put("P", "Plugin")
			.put("F", "Forced")
			.build()
		),
		new SearchProperty(OnmsMonitoredService.class, "status", "Management Status", STRING, OnmsMonitoredService.STATUS_MAP)
	}));

	static final SortedSet<SearchProperty> IP_INTERFACE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsIpInterface.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsIpInterface.class, "ipAddress", "IP Address", IP_ADDRESS),
                new SearchProperty(OnmsIpInterface.class, "netMask", "Network Mask", IP_ADDRESS),
		new SearchProperty(OnmsIpInterface.class, "ipHostName", "Hostname", STRING),
		new SearchProperty(OnmsIpInterface.class, "ipLastCapsdPoll", "Last Provisioning Scan", TIMESTAMP),
		new SearchProperty(OnmsIpInterface.class, "isManaged", "Management Status", STRING)
	}));

	static final SortedSet<SearchProperty> LOCATION_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsMonitoringLocation.class, "locationName", "ID", STRING),
		new SearchProperty(OnmsMonitoringLocation.class, "geolocation", "Geographic Address", STRING),
		new SearchProperty(OnmsMonitoringLocation.class, "latitude", "Latitude", FLOAT),
		new SearchProperty(OnmsMonitoringLocation.class, "longitude", "Longitude", FLOAT),
		new SearchProperty(OnmsMonitoringLocation.class, "monitoringArea", "Monitoring Area", STRING),
		new SearchProperty(OnmsMonitoringLocation.class, "priority", "UI Priority", INTEGER)
	}));

	static final SortedSet<SearchProperty> MINION_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsMinion.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsMinion.class, "label", "Label", STRING),
		new SearchProperty(OnmsMinion.class, "lastUpdated", "Last Heartbeat Update", TIMESTAMP),
		new SearchProperty(OnmsMinion.class, "location", "Monitoring Location", STRING),
		new SearchProperty(OnmsMinion.class, "status", "Status", STRING)
	}));

	static final SortedSet<SearchProperty> NODE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsNode.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsNode.class, "createTime", "Creation Time", TIMESTAMP),
		new SearchProperty(OnmsNode.class, "foreignId", "Foreign ID", STRING),
		new SearchProperty(OnmsNode.class, "foreignSource", "Foreign Source", STRING),
		new SearchProperty(OnmsNode.class, "label", "Label", STRING),
		new SearchProperty(OnmsNode.class, "labelSource", "Label Source", STRING, ImmutableMap.<String,String>builder()
			.put(String.valueOf(NodeLabelSource.ADDRESS.value()), "IP Address")
			.put(String.valueOf(NodeLabelSource.HOSTNAME.value()), "Hostname")
			.put(String.valueOf(NodeLabelSource.NETBIOS.value()), "NetBIOS")
			.put(String.valueOf(NodeLabelSource.SYSNAME.value()), "SNMP sysName")
			.put(String.valueOf(NodeLabelSource.UNKNOWN.value()), "Unknown")
			.put(String.valueOf(NodeLabelSource.USER.value()), "User-Defined")
			.build()
		),
		new SearchProperty(OnmsNode.class, "lastCapsdPoll", "Last Provisioning Scan", TIMESTAMP),
		new SearchProperty(OnmsNode.class, "netBiosDomain", "Windows NetBIOS Domain", STRING),
		new SearchProperty(OnmsNode.class, "netBiosName", "Windows NetBIOS Name", STRING),
		new SearchProperty(OnmsNode.class, "operatingSystem", "Operating System", STRING),
		//new SearchProperty(OnmsNode.class, "parent", "?", ?),
		//new SearchProperty(OnmsNode.class, "pathElement", "?", ?),
		new SearchProperty(OnmsNode.class, "sysContact", "SNMP sysContact", STRING),
		new SearchProperty(OnmsNode.class, "sysDescription", "SNMP sysDescription", STRING),
		new SearchProperty(OnmsNode.class, "sysLocation", "SNMP sysLocation", STRING),
		new SearchProperty(OnmsNode.class, "sysName", "SNMP sysName", STRING),
		new SearchProperty(OnmsNode.class, "sysObjectId", "SNMP sysObjectId", STRING),
		new SearchProperty(OnmsNode.class, "type", "Type", STRING, ImmutableMap.<String,String>builder()
			.put(String.valueOf(NodeType.ACTIVE.value()), "Active")
			.put(String.valueOf(NodeType.DELETED.value()), "Deleted")
			.put(String.valueOf(NodeType.UNKNOWN.value()), "Unknown")
			.build()
		)
	}));

	static final SortedSet<SearchProperty> NOTIFICATION_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsNotification.class, "notifyId", "ID", INTEGER),
		new SearchProperty(OnmsNotification.class, "answeredBy", "Answered By", STRING),
		new SearchPropertyBuilder().entityClass(OnmsNotification.class).id("ipAddress").name("IP Address").type(IP_ADDRESS).iplike(true).build(),
		new SearchProperty(OnmsNotification.class, "numericMsg", "Numeric Message", STRING),
		new SearchProperty(OnmsNotification.class, "pageTime", "Page Time", TIMESTAMP),
		new SearchProperty(OnmsNotification.class, "queueId", "Queue ID", STRING),
		new SearchProperty(OnmsNotification.class, "respondTime", "Responded Time", TIMESTAMP),
		new SearchProperty(OnmsNotification.class, "subject", "Subject", STRING),
		new SearchProperty(OnmsNotification.class, "textMsg", "Text Message", STRING)
	}));

	static final SortedSet<SearchProperty> OUTAGE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsOutage.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsOutage.class, "ifLostService", "Lost Service Time", TIMESTAMP),
		new SearchProperty(OnmsOutage.class, "ifRegainedService", "Regained Service Time", TIMESTAMP),
		new SearchProperty(OnmsOutage.class, "suppressedBy", "Suppressed By User", STRING),
		new SearchProperty(OnmsOutage.class, "suppressTime", "Suppressed Time", TIMESTAMP)
	}));

	static final SortedSet<SearchProperty> SCAN_REPORT_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(ScanReport.class, "id", "ID", STRING),
		new SearchProperty(ScanReport.class, "locale", "Locale", STRING),
		new SearchProperty(ScanReport.class, "location", "Monitoring Location", STRING),
		new SearchProperty(ScanReport.class, "timestamp", "Timestamp", TIMESTAMP)
	}));

	static final SortedSet<SearchProperty> SERVICE_TYPE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsServiceType.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsServiceType.class, "name", "Service Name", STRING)
	}));

	static final SortedSet<SearchProperty> SNMP_INTERFACE_PROPERTIES = new TreeSet<>(Arrays.asList(new SearchProperty[] {
		new SearchProperty(OnmsSnmpInterface.class, "id", "ID", INTEGER),
		new SearchProperty(OnmsSnmpInterface.class, "ifAdminStatus", "Admin Status", INTEGER),
		new SearchProperty(OnmsSnmpInterface.class, "ifIndex", "Interface Index", INTEGER),
		new SearchProperty(OnmsSnmpInterface.class, "ifOperStatus", "Operational Status", INTEGER),
		new SearchProperty(OnmsSnmpInterface.class, "ifSpeed", "Interface Speed (Bits per second)", LONG),
		new SearchProperty(OnmsSnmpInterface.class, "lastCapsdPoll", "Last Provisioning Scan", TIMESTAMP),
		new SearchProperty(OnmsSnmpInterface.class, "lastSnmpPoll", "Last SNMP Interface Poll", TIMESTAMP),
	}));

	public static final Set<SearchProperty> ALARM_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> APPLICATION_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> EVENT_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> IF_SERVICE_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> MINION_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> LOCATION_SERVICE_PROPERTIES = new LinkedHashSet<>();
	public static final Set<SearchProperty> NODE_SERVICE_PROPERTIES = new LinkedHashSet<>();
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
			p.entityClass,
			alias.toString(), 
			p.id,
			namePrefix, 
			p.name,
			p.type,
			orderBy,
			// IPLIKE queries are only valid on root aliases
			// so always reset this value to 'false' when adding
			// prefixes
			SearchProperty.DEFAULT_IPLIKE,
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
			p.entityClass,
			alias,
			p.id,
			namePrefix,
			p.name,
			p.type,
			SearchProperty.DEFAULT_ORDER_BY,
			// IPLIKE queries are only valid on root aliases
			// so always reset this value to 'false' when adding
			// prefixes
			SearchProperty.DEFAULT_IPLIKE,
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
		ALARM_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.eventParameter, "Event Parameter", EVENT_PARAMETER_PROPERTIES, false));
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
		EVENT_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.eventParameter, "Event Parameter", EVENT_PARAMETER_PROPERTIES, false));
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
		NODE_SERVICE_PROPERTIES.addAll(NODE_PROPERTIES);
		//NODE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.node, NODE_PROPERTIES));
		NODE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.assetRecord, "Asset", ASSET_RECORD_PROPERTIES));
		NODE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.category, "Category", CATEGORY_PROPERTIES, false));
		NODE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.ipInterface, "IP Interface", IP_INTERFACE_PROPERTIES, false));
		NODE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.location, "Location", LOCATION_PROPERTIES));
		// TODO: Figure out if it makes sense to search/orderBy on 2nd-level and greater JOINed properties
		//NODE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.monitoredService, "Monitored Service", IF_SERVICE_PROPERTIES, false));
		//NODE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.serviceType, "Service", SERVICE_TYPE_PROPERTIES, false));
		NODE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.snmpInterface, "SNMP Interface", SNMP_INTERFACE_PROPERTIES, false));

		// Root prefix
		NOTIFICATION_SERVICE_PROPERTIES.addAll(NOTIFICATION_PROPERTIES);
		//NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.notification, NOTIFICATION_PROPERTIES));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.assetRecord, "Asset", ASSET_RECORD_PROPERTIES));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.category, "Category", CATEGORY_PROPERTIES, false));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.distPoller, "Monitoring System", DIST_POLLER_PROPERTIES));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.event, "Event", EVENT_PROPERTIES));
		NOTIFICATION_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.eventParameter, "Event Parameter", EVENT_PARAMETER_PROPERTIES, false));
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
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.monitoredService, "Monitored Service", IF_SERVICE_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.node, "Node", NODE_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix("serviceLostEvent", "Service Lost Event", EVENT_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix("serviceRegainedEvent", "Service Regained Event", EVENT_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.serviceType, "Service", SERVICE_TYPE_PROPERTIES));
		OUTAGE_SERVICE_PROPERTIES.addAll(withAliasPrefix(Aliases.snmpInterface, "SNMP Interface", SNMP_INTERFACE_PROPERTIES));

		// Root prefix
		SCAN_REPORT_SERVICE_PROPERTIES.addAll(SCAN_REPORT_PROPERTIES);
	}
}

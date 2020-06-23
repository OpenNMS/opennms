/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import static org.opennms.web.rest.support.SearchProperties.ALARM_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.APPLICATION_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.ASSET_RECORD_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.CATEGORY_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.DIST_POLLER_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.EVENT_PARAMETER_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.EVENT_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.IF_SERVICE_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.IP_INTERFACE_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.LOCATION_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.MINION_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.NODE_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.NOTIFICATION_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.OUTAGE_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.SCAN_REPORT_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.SERVICE_TYPE_PROPERTIES;
import static org.opennms.web.rest.support.SearchProperties.SNMP_INTERFACE_PROPERTIES;

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

/**
 * This test can be run manually to generate docs for the search
 * parameters for the RESTv2 services. The docs should be pasted
 * into:
 * 
 * opennms-webapp-rest/src/main/java/org/opennms/web/rest/v2/README.adoc
 */
@Ignore
public class SearchPropertiesToAsciidocTest {

	private static final String HEADER_FORMAT = "[[%sProperties]]\n" +
		".%s Properties\n" +
		"[options=\"header\",width=\"99%%\",cols=\"2m,1,3\"]\n" +
		"|===\n" +
		"| Name | Type | Description";

	private static final String ROW_FORMAT = "| %s | %s | %s";

	private static final String FOOTER_FORMAT = "|===\n";

	@Test
	public void generateAsciidoc() throws Exception {
		System.out.println(String.format(HEADER_FORMAT, "alarm", "Alarm"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.alarm, null, ALARM_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "application", "Application"));
		for (SearchProperty prop : APPLICATION_PROPERTIES) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		// Expand the first column width here to accommodate long property names
		System.out.println(String.format(HEADER_FORMAT, "asset", "Asset").replace("2m,1,3", "3m,1,3"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.assetRecord, null, ASSET_RECORD_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "category", "Category"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.category, null, CATEGORY_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "distPoller", "Distributed Poller"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.distPoller, null, DIST_POLLER_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "event", "Event"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.event, null, EVENT_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "eventParameter", "Event Parameter"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.eventParameter, null, EVENT_PARAMETER_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "ipInterface", "IP Interface"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.ipInterface, null, IP_INTERFACE_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "monitoredService", "Monitored Service"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.monitoredService, null, IF_SERVICE_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "location", "Monitoring Location"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.location, null, LOCATION_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "minion", "Minion"));
		for (SearchProperty prop : MINION_PROPERTIES) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "node", "Node"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.node, null, NODE_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "notification", "Notification"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.notification, null, NOTIFICATION_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "outage", "Outage"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.outage, null, OUTAGE_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "scanReport", "Scan Report"));
		for (SearchProperty prop : SCAN_REPORT_PROPERTIES) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "serviceType", "Service Type"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.serviceType, null, SERVICE_TYPE_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);

		System.out.println(String.format(HEADER_FORMAT, "snmpInterface", "SNMP Interface"));
		for (SearchProperty prop : SearchProperties.withAliasPrefix(Aliases.snmpInterface, null, SNMP_INTERFACE_PROPERTIES)) {
			System.out.println(String.format(ROW_FORMAT, prop.getId(), toPrettyType(prop.type), nameAndValues(prop)));
		}
		System.out.println(FOOTER_FORMAT);
	}

	private static String nameAndValues(SearchProperty prop) {
		StringBuffer retval = new StringBuffer();
		retval.append(prop.name);
		if (prop.values != null) {
			for (Map.Entry<String,String> entry : prop.values.entrySet()) {
				retval.append(String.format("\n\n`%s`: %s", entry.getKey().trim().length() == 0 ? "&blank;" : entry.getKey(), entry.getValue()));
			}
		}
		return retval.toString();
	}

	private static String toPrettyType(SearchProperty.SearchPropertyType type) {
		switch(type) {
		case FLOAT: return "Float";
		case INTEGER: return "Integer";
		case IP_ADDRESS: return "IP Address";
		case LONG: return "Long";
		case STRING: return "String";
		case TIMESTAMP: return "Timestamp";
		default: throw new IllegalArgumentException();
		}
	}
}

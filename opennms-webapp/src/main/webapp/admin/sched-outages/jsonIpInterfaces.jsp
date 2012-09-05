<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"
        contentType="text/html"
        session="true"
        import="java.util.*,
        org.opennms.netmgt.config.*,
        org.opennms.netmgt.config.poller.*,
        org.opennms.core.utils.WebSecurityUtils,
        org.opennms.web.element.*,
        org.opennms.netmgt.EventConstants,
        org.opennms.netmgt.xml.event.Event,
        org.opennms.netmgt.utils.*,
        org.opennms.web.api.Util,
        java.net.*,
        java.io.*,
        java.text.NumberFormat,
        java.text.SimpleDateFormat,
        net.sf.json.JSONSerializer
        "
%>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%!
/**
 * This class will be serialized into JSON format. It needs to correspond to the
 * format that the jQuery autocomplete is expecting.
 */
public static class AutocompleteRecord {
	private String m_label;
	private String m_value;

	public AutocompleteRecord(String label, String value) {
		m_label = label;
		m_value = value;
	}

	public String getLabel() {
		return m_label;
	}

	public String getValue() {
		return m_value;
	}
}
%>
<%
List<org.opennms.web.element.Interface> items = Arrays.asList(NetworkElementFactory.getInstance(getServletContext()).getAllManagedIpInterfaces(false));
%>

<%-- Use this segment to test large numbers of JSON objects
<%!
private static class MyInterface extends org.opennms.web.element.Interface {
	public String ipAddress;
	public String name;
	public String getIpAddress() { return ipAddress; }
	public String getName() { return name; }
}
%>
<%
List<org.opennms.web.element.Interface> items = new ArrayList<org.opennms.web.element.Interface>();
for (int i = 0; i < 50000; i++) {
	MyInterface item = new MyInterface();
	item.name = ("really_super_long_hostname_that_is_longer_than_normal_" + i);
	item.ipAddress = ("192.168." + Integer.valueOf(i / 256) + "." + (i % 256));
	items.add(item);
}
%>
--%>

[
<% 
boolean printedFirst = false;
int recordCounter = 1;
final int recordLimit = 200;
for (org.opennms.web.element.Interface item : items) {
	String autocomplete = request.getParameter("term");
	// Check to see if the interface matches the search term
	if (
		autocomplete == null || 
		"".equals(autocomplete) || 
		item.getName().contains(autocomplete) || 
		item.getIpAddress().contains(autocomplete)
	) {
		String hostnameClause = (
			item.getName() == null || 
			"".equals(item.getName())) || 
			item.getName().equals(item.getIpAddress()
		) ? "" : " (" + item.getName() + ")";

		String label = item.getIpAddress() + hostnameClause;
		if (autocomplete != null && !"".equals(autocomplete)) {
			label = label.replace(autocomplete, "<strong>" + autocomplete + "</strong>");
		}
		// If we've already printed the first item, separate the items with a comma
		if (printedFirst) {
			out.println(",");
		}
		out.println(JSONSerializer.toJSON(new AutocompleteRecord(label, item.getIpAddress())));
		printedFirst = true;
		// Don't print more than a certain number of records to limit the
		// performance impact in the web browser
		if (recordCounter++ >= recordLimit) {
			break;
		}
	}
}
%>
]

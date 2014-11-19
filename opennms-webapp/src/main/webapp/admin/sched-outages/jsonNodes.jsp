<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

--%>

<%@page language="java"
        contentType="text/html"
        session="true"
        import="java.util.*, java.util.regex.*,
        org.opennms.web.element.*,
        org.opennms.netmgt.model.OnmsNode,
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
	private int m_value;

	public AutocompleteRecord(String label, int value) {
		m_label = label;
		m_value = value;
	}

	public String getLabel() {
		return m_label;
	}

	public int getValue() {
		return m_value;
	}
}
%>
<%

%>
[
<% 
boolean printedFirst = false;
int recordCounter = 1;
final int recordLimit = 200;
String autocomplete = request.getParameter("term");

List<OnmsNode> items;
if(autocomplete == null || autocomplete.equals("")){
    items = NetworkElementFactory.getInstance(getServletContext()).getAllNodes();
} else{
    items = NetworkElementFactory.getInstance(getServletContext()).getNodesLike(autocomplete);
}
for (OnmsNode item : items) {
	// Check to see if the item matches the search term

		StringBuffer result = new StringBuffer();

        result.append(item.getLabel());
		result.append(" (Node ID ").append(item.getId()).append(")");
		// If we've already printed the first item, separate the items with a comma
		if (printedFirst) {
			out.println(",");
		}
		out.println(JSONSerializer.toJSON(new AutocompleteRecord(result.toString(), item.getId())));
		printedFirst = true;
		// Don't print more than a certain number of records to limit the
		// performance impact in the web browser
		if (recordCounter++ >= recordLimit) {
			break;
		}
}
%>
]

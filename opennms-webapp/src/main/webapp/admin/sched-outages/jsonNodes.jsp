<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2017 The OpenNMS Group, Inc.
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

--%>

<%@page language="java"
        contentType="text/html"
        session="true"
        import="java.util.*, java.util.regex.*,
        org.opennms.web.element.*,
        org.opennms.netmgt.model.OnmsCategory,
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

%>
[
<% 
boolean printedFirst = false;
boolean listCategories = false;
boolean listForeignSources = false;
int recordCounter = 1;
final int recordLimit = 200;
String autocomplete = request.getParameter("term");
boolean patternMatch = false;
String category = null;
String foreignSource = null;

List<OnmsNode> items;
if(autocomplete == null || autocomplete.equals("") || autocomplete.matches("^[#@%\\$].*$")){
    items = NetworkElementFactory.getInstance(getServletContext()).getAllNodes();
} else{
    items = NetworkElementFactory.getInstance(getServletContext()).getNodesLike(autocomplete);
}

if (autocomplete != null && autocomplete.matches("^[#@%\\$].*$")){
    if (autocomplete.startsWith("#")) {
        category = autocomplete.substring(1);
    } else if (autocomplete.startsWith("@")) {
        category = autocomplete.substring(1);
        listCategories = true;
    } else if (autocomplete.startsWith("%")) {
        foreignSource = autocomplete.substring(1);
    } else if (autocomplete.startsWith("$")) {
        foreignSource = autocomplete.substring(1);
        listForeignSources = true;
    }
}
if (listCategories) {
    Set<OnmsCategory> set = new TreeSet<>();
    for (OnmsNode item : items) {
        set.addAll(item.getCategories());
    }
    for (OnmsCategory cat : set) {
        if (cat.getName().startsWith(category)) {
            if (printedFirst) {
                out.println(",");
            }
            out.println(JSONSerializer.toJSON(new AutocompleteRecord("@"+cat.getName(), "@"+cat.getName())));
            printedFirst = true;
        }
    }

} else if (listForeignSources) {
    Set<String> set = new TreeSet<>();
    for (OnmsNode item : items) {
        if (item.getForeignSource() != null && !"".equals(item.getForeignSource())) {
            set.add(item.getForeignSource());
        }
    }
    for (String fSource : set) {
        if (fSource.startsWith(foreignSource)) {
            if (printedFirst) {
                out.println(",");
            }
            out.println(JSONSerializer.toJSON(new AutocompleteRecord("$"+fSource, "$"+fSource)));
            printedFirst = true;
        }
    }

} else {
    for (OnmsNode item : items) {
        // Check to see if the item matches the search term
        if (category != null) {
            boolean skipItem = true;
            for (OnmsCategory cat : item.getCategories()) {
                if (cat.getName().startsWith(category)) {
                    skipItem = false;
                    break;
                }
            }
            if (skipItem) {
                continue;
            }
        } else if (foreignSource != null) {
            if (item.getForeignSource() != null && !item.getForeignSource().startsWith(foreignSource) && !item.getForeignSource().equals(foreignSource)) {
                continue;
            }
        }

        StringBuffer result = new StringBuffer();

        result.append(item.getLabel());
        result.append(" (Node ID ").append(item.getId()).append(")");
        // If we've already printed the first item, separate the items with a comma
        if (printedFirst) {
            out.println(",");
        }
        out.println(JSONSerializer.toJSON(new AutocompleteRecord(result.toString(), item.getNodeId())));
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

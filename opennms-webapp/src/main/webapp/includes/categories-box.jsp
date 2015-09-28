<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

<%-- 
  This page is included by other JSPs to create a box containing a
  table of categories and their outage and availability status.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true"
	import="org.opennms.web.category.Category,
		org.opennms.web.category.CategoryList,
		org.opennms.web.api.Util,
		java.util.Date,
		java.util.Iterator,
		java.util.List,
		java.util.Map" %>

<%!

    CategoryList m_category_list;

    public void init() throws ServletException {
	m_category_list = new CategoryList();
    }

	// Creates a link to the rtc/category.jsp according to the selected outagesType.
	public String createCategoriesOutageLink(HttpServletResponse response, Category category, String outagesType, String linkTitle, String linkText) {
		if (category.getLastUpdated() != null) {
			if (linkTitle == null) {
				return String.format("<a href=\"%s\">%s</a>",
						response.encodeURL("/opennms/rtc/category.jsp?showoutages=" + outagesType + "&category=" + Util.encode(category.getName())),
						linkText);
			}
			return String.format("<a href=\"%s\" title=\"%s\">%s</a>",
					response.encodeURL("/opennms/rtc/category.jsp?showoutages=" + outagesType + "&category=" + Util.encode(category.getName())),
					linkTitle,
					linkText);
		}
		return linkText;
	}
%>

<%
	Map<String, List<Category>> categoryData = m_category_list.getCategoryData();

	long earliestUpdate = m_category_list.getEarliestUpdate(categoryData);
	boolean opennmsDisconnect = m_category_list.isDisconnected(earliestUpdate);

	String titleName = "Availability Over the Past 24 Hours";
	if (opennmsDisconnect) {
		titleName = "Waiting for availability data. ";
		if (earliestUpdate > 0) {
			titleName += new Date(earliestUpdate).toString();
		} else {
			titleName += "One or more categories have never been updated.";
		}
	}
%>

<div class="panel panel-default fix-subpixel">
  <div class="panel-heading">
    <h3 class="panel-title"><%= titleName %></h3>
  </div>

<table class="table table-condensed severity">
<%
	for (Iterator<String> i = categoryData.keySet().iterator(); i.hasNext(); ) {
	    String sectionName = i.next();
%>
	<thead class="dark">
		<tr>
			<th><%= sectionName %></th>
			<th align="right">Outages</th>
			<th align="right">Availability</th>
		</tr>
	</thead>
<%
 	    List<Category> categories = categoryData.get(sectionName);

	    for (Iterator<Category> j = categories.iterator(); j.hasNext(); ) {
		Category category = j.next();
%>
	<tr>
		<td>
			<%=createCategoriesOutageLink(response, category, "all", category.getTitle(), category.getName())%>
		</td>
		<td class="severity-<%= (opennmsDisconnect ? "indeterminate" : category.getOutageClass().toLowerCase()) %> bright divider"
	        align="right"
		    title="Updated: <%= category.getLastUpdated() %>">
			<%=createCategoriesOutageLink(response, category, "outages", null, category.getOutageText())%>
		</td>
		<td class="severity-<%= (opennmsDisconnect ? "indeterminate" : category.getAvailClass().toLowerCase()) %> bright divider"
		    align="right" 
		    title="Updated: <%= category.getLastUpdated() %>">
			<%=createCategoriesOutageLink(response, category, "avail", null, category.getAvailText())%>
		</td>
	</tr>
	
<%
	    }
	}
%>
</table>
<!-- </div> -->
</div>

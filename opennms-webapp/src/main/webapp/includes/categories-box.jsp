<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

%>

<%
	Map<String, List<Category>> categoryData = m_category_list.getCategoryData();

	long earliestUpdate = m_category_list.getEarliestUpdate(categoryData);
	boolean opennmsDisconnect =
		m_category_list.isDisconnected(earliestUpdate);
%>
<%	if (opennmsDisconnect) { %>
	    <h3 class="o-box">OpenNMS Disconnect -- is the OpenNMS daemon running? - 
		Last update:
<%=		(earliestUpdate > 0 ?
			 new Date(earliestUpdate).toString() :
			 "one or more categories have never been updated.") %>
	      </h3>
<%	} else { %>
	    <h3 class="o-box">Availability Over the Past 24 Hours</h3>
<%	} %>


<table class="o-box onms-table">
<%
	for (Iterator<String> i = categoryData.keySet().iterator(); i.hasNext(); ) {
	    String sectionName = i.next();
%>
	<thead>
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
		String categoryName = category.getName();
%>
	<tr class="CellStatus">
		<td>
          <% if (category.getLastUpdated() != null) { %>
		    <a href="<%= response.encodeURL("rtc/category.jsp?category=" + Util.encode(categoryName)) %>"
		       title="<%= category.getTitle() %>">
              <%= categoryName %>
            </a>
          <% } else { %>
            <%= categoryName %>
          <% } %>
		</td>
		<td class="<%= (opennmsDisconnect ? "Indeterminate" : category.getOutageClass()) %>"
	        align="right"
		    title="Updated: <%= category.getLastUpdated() %>"><%= category.getOutageText() %>
		</td>
		<td class="<%= (opennmsDisconnect ? "Indeterminate" : category.getAvailClass()) %>"
		    align="right" 
		    title="Updated: <%= category.getLastUpdated() %>"><%= category.getAvailText() %>
		</td>
	</tr>
	
<%
	    }
	}
%>
</table>

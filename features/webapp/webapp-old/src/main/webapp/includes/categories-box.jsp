<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 May 10: No longer need to pass the ServletContext to CategoryList. - dj@opennms.org
// 2004 Oct 01: Added a color change when disconnected from OpenNMS.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Oct 24: Added a mouse over for last update times. Bug #517.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

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
		org.opennms.web.Util,
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
	Map categoryData = m_category_list.getCategoryData();

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


<table class="o-box">
<%
	for (Iterator i = categoryData.keySet().iterator(); i.hasNext(); ) {
	    String sectionName = (String) i.next();
%>
	<thead>
		<tr>
			<th><%= sectionName %></th>
			<th align="right">Outages</th>
			<th align="right">Availability</th>
		</tr>
	</thead>
<%
 	    List categories = (List) categoryData.get(sectionName);

	    for (Iterator j = categories.iterator(); j.hasNext(); ) {
		Category category = (Category) j.next();
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



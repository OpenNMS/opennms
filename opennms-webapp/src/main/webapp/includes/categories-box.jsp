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
		java.net.URLEncoder,
		java.util.Date,
		java.util.Iterator,
		java.util.List,
		java.util.Map" %>

<%!

    CategoryList m_category_list;

    public void init() throws ServletException {
	m_category_list = new CategoryList(getServletContext());
    }

%>

<%
	Map categoryData = m_category_list.getCategoryData();

	long earliestUpdate = m_category_list.getEarliestUpdate(categoryData);
	boolean opennmsDisconnect =
		m_category_list.isDisconnected(earliestUpdate);
%>

        <table class="standardfirst">

<%
	for (Iterator i = categoryData.keySet().iterator(); i.hasNext(); ) {
	    String sectionName = (String) i.next();
%>

	    <tr>
	    <td class="standardheader" width="50%"><%= sectionName %></td>
	    <td class="standardheader" width="20%" align="right">Outages</td>
	    <td class="standardheader" width="30%" align="right">24hr Avail</td>
	    </tr>
	    
<%
 	    List categories = (List) categoryData.get(sectionName);

	    for (Iterator j = categories.iterator(); j.hasNext(); ) {
		Category category = (Category) j.next();
		String categoryName = category.getName();
%>

		<tr>

		<td class="standard"><a href="<%= response.encodeURL(
				"rtc/category.jsp?category=" +
			    	URLEncoder.encode(categoryName, "UTF-8")) %>"
		       title="<%= category.getTitle() %>"
		    ><%= categoryName %></a></td>
		<td class="standard" bgcolor="<%= (opennmsDisconnect ? "lightblue" :
			       category.getOutageColor()) %>"
	            align="right"
		    title="Updated: <%= category.getLastUpdated() %>"
		><%= category.getOutageText() %></td>
		<td class="standard" bgcolor="<%= (opennmsDisconnect ? "lightblue" :
			      category.getAvailColor()) %>"
		    align="right" 
		    title="Updated: <%= category.getLastUpdated() %>"
		><b><%= category.getAvailText() %></b></td>
		
		</tr>
<%
	    }
	}
%>
    
	<tr>

<%	if (opennmsDisconnect) { %>
	    <td class="standardheader" colspan="3">
	      <font color="#bb1111">
	        OpenNMS Disconnect -- is the OpenNMS daemon running?
		<br/>
		Last update:
<%=		(earliestUpdate > 0 ?
			 new Date(earliestUpdate).toString() :
			 "one or more categories have never been updated.") %>
	      </font>
            </td>
<%	} else { %>
	    <td class="standardheaderplain" colspan="3">Percentage over last 24 hours</td>
<%	} %>

	</tr>
	</table>



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
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
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
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.asset.*,
		org.opennms.web.MissingParameterException 
	"
%>

<%!
    AssetModel model;

    public void init() throws ServletException {
        this.model = new AssetModel();
    }
%>

<%
    String column = request.getParameter("column");
    String search = request.getParameter("searchvalue");
    String requiredParameters[] = new String[] { "column", "searchvalue" };

    if( column == null ) {
        throw new MissingParameterException("column", requiredParameters);
    }

    if( search == null ) {
        throw new MissingParameterException("searchvalue", requiredParameters);
    }

    AssetModel.MatchingAsset[] assets = model.searchAssets(column, search);
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Asset List" />
  <jsp:param name="headTitle" value="Asset List" />
  <jsp:param name="breadcrumb" value="<a href='asset/index.jsp'>Assets</a>" />
  <jsp:param name="breadcrumb" value="Asset List" />
</jsp:include>

 <% if (request.getParameter("showMessage") != null && request.getParameter("showMessage").equalsIgnoreCase("true")) { %>
 <br />
 <p>
 <span style="font-size: larger;"><%= request.getSession(false).getAttribute("message") %></span>
 </p>
 <% } %>

<h3>Assets</h3>

  <% if( assets.length > 0 ) { %>
      <table class="standard">
        <tr>
          <td class="standardheader">Matching Text</td>
          <td class="standardheader">Asset Link</td>
          <td class="standardheader">Node Link</td>
        </tr>

      <% for( int i=0; i < assets.length; i++ ) { %>
        <tr>
          <td class="standard"><%=assets[i].matchingValue%></td>
          <td class="standard"><a href="asset/modify.jsp?node=<%=assets[i].nodeId%>"><%=assets[i].nodeLabel%></a></td>
	  <td class="standard"><a href="element/node.jsp?node=<%=assets[i].nodeId%>"><%=assets[i].nodeLabel%></a></td>
        </tr>
      <% } %>
      </table>
  <% } else { %>
      None found.
  <% } %>

<jsp:include page="/includes/footer.jsp" flush="false" />

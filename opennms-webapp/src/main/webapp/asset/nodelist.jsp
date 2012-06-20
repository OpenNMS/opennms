<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
	import="org.opennms.web.asset.*,
		org.opennms.web.servlet.MissingParameterException 
	"
%>

<%
    final String ALL_NON_EMPTY = "_allNonEmpty";
    String column = request.getParameter("column");
    String search = request.getParameter("searchvalue");
    String requiredParameters[] = new String[] { "column", "searchvalue" };

    if( column == null ) {
        throw new MissingParameterException("column", requiredParameters);
    }

    if( search == null && ! column.equals(ALL_NON_EMPTY)) {
        throw new MissingParameterException("searchvalue", requiredParameters);
    }

    AssetModel.MatchingAsset[] assets = column.equals(ALL_NON_EMPTY) ? AssetModel.searchNodesWithAssets() : AssetModel.searchAssets(column, search);
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

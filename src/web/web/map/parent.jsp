<!--

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
// Copyright (C) 2003 Networked Knowledge Systems, Inc.  All rights reserved.
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
//      http://www.blast.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*"%>

<%
       Node[] nodes = NetworkElementFactory.getAllNodes();
       String message = (String)session.getAttribute("message");
%>

<html>
<head>
  <title>Map | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<%
   String breadcrumb1 = "<a href='map/index.jsp'>Map</a>";
   String breadcrumb2 = "Set Parent-Child Relationships";
%>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Set Parents" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
  <tr>
    <td>&nbsp;</td>

    <td>
      <h3>Set Parents</h3>          

      <% if(message != null) { %>
      <%= message %>
      <% } %>

      <p>
        <form method="POST" action="map/modifyParent" >
          <select name="parentID">
              <option value="0">- none -</option>
          <% for(int i = 0; i < nodes.length; i++) { %>
              <option value="<%= nodes[i].getNodeId() %>">
                <%= nodes[i].getLabel() %>
              </option>
          <% } %>
          </select><br>
            is parent of <br>
          <select name="childID">
              <option value="0">- none -</option>
          <% for(int i = 0; i < nodes.length; i++) { %>
              <option value="<%= nodes[i].getNodeId() %>">
                <%= nodes[i].getLabel() %>
              </option>
          <% } %>
          </select><br>
          <input type="submit" value="submit">
        </form>      
      </p>
    </td>

    <td>&nbsp;</td>

    <td valign="top" width="60%" >
      <h3>Set Parent&lt;-&gt;Child Relationships</h3>

      <p>
         Set the parent node on the top and the child node on the
         bottom.  Set "-none-" as the parent to remove a relationship.
      </p>

      <p>
         Note that if you set up a circular relationship (e.g. Node 3
         is the parent of Node 4, which is the parent of Node 3) any
         nodes involved in the circular relationship will not appear
         on the map.
      </p>
    </td>

    <td>&nbsp;</td>

  </tr>

</table>                                    

<br />

    <jsp:include page="/includes/footer.jsp" flush="false" />

  </body>
</html>

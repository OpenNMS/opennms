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
//      http://www.opennms.com/
//

-->

<%@page language="java" contentType="text/html" session="true" %>
<%@page import="org.opennms.web.map.DocumentGenerator" %>
<%@page import="org.opennms.web.map.MapNodeFactory" %>
<%
   String type = request.getParameter("type");
   String format = request.getParameter("format");
   String fullscreen = request.getParameter("fullscreen");
   String refresh = request.getParameter("refresh");

   String refreshVal = new Integer( new Integer(refresh).intValue() * 60 ).toString();
%>
<html>
<head>
  <title>Map | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
  <meta http-equiv="refresh" content="<%= refreshVal %>" />
</head>
<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% 
   String breadcrumb1 = "<a href='map/index.jsp'>Map</a>";
   String breadcrumb2 = "View Network Map";

   MapNodeFactory mnf = new MapNodeFactory();
   DocumentGenerator docgen = new DocumentGenerator();
   docgen.setNodes(mnf.getNodes());
   docgen.setServletContext(getServletContext());
   docgen.setUrlBase(org.opennms.web.Util.calculateUrlBase(request));
   docgen.setMapType(type);
   docgen.calculateHostCoordinates();

   // this needs to go into an HttpSession so the SVGTranscoder
   // can find the document
   request.getSession().setAttribute("docgen", docgen);
%>
<% if(fullscreen.equals("n")) { %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Display Map" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>
<% } %>

<br>
<!-- Body -->

<table width="100%" cellspacing="0" cellpadding="0" border="0">
<% if(fullscreen.equals("n")) { %>
  <tr>
    <td>
      <h3>View Network Map</h3>          
    </td>
  </tr>
<% } %>

  <tr>    
    <% if(format.equals("png")) { %>
    <%= docgen.getImageMap("svgmap", "element/node.jsp?node=") %>
    <td><img src="map/SVGTranscoder" border="0" usemap="#svgmap"></td>
    <% } else { %>
    <td><embed src="map/SVGServlet" 
               width="<%= docgen.getDocumentWidth() %>" 
               height="<%= docgen.getDocumentHeight() %>" 
               type="image/svg+xml"
               pluginspage="http://www.adobe.com/svg/viewer/install/"></td>
    <% } %>
  </tr>
</table>                                    

<br />

<% if(fullscreen.equals("n")) { %>
    <jsp:include page="/includes/footer.jsp" flush="false" />
<% } %>

  </body>
</html>

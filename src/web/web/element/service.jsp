<!--

//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//

-->

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.element.*,org.opennms.web.category.*,java.util.*,org.opennms.web.event.*" %>

<%
    String nodeIdString = request.getParameter( "node" );
    String ipAddr = request.getParameter( "intf" );
    String serviceIdString = request.getParameter( "service" );

    if( nodeIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "node", new String[] { "node", "intf", "service" } );
    }

    if( ipAddr == null ) {
        throw new org.opennms.web.MissingParameterException( "intf", new String[] { "node", "intf", "service" } );
    }

    if( serviceIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "service", new String[] { "node", "intf", "service" } );
    }

    int nodeId = -1;
    int serviceId = -1;

    try {
        nodeId = Integer.parseInt( nodeIdString );
        serviceId = Integer.parseInt( serviceIdString );
    }
    catch( NumberFormatException e ) {
        //throw new WrongParameterDataTypeException
        throw new ServletException( "Wrong data type, should be integer", e );
    }

    Service service_db = NetworkElementFactory.getService( nodeId, ipAddr, serviceId );

    if( service_db == null ) {
        //handle this WAY better, very awful
        throw new ServletException( "No such service in database" );
    }

    String eventUrl = "event/list?filter=node%3D" + nodeId + "&filter=interface%3D" + ipAddr + "&filter=service%3D" + serviceId;
%>

<html>
<head>
  <title><%=service_db.getServiceName()%> Service on <%=ipAddr%> | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = java.net.URLEncoder.encode("<a href='element/index.jsp'>Search</a>"); %>
<% String breadcrumb2 = java.net.URLEncoder.encode("<a href='element/node.jsp?node=" + nodeId + "'>Node</a>"); %>
<% String breadcrumb3 = java.net.URLEncoder.encode("<a href='element/interface.jsp?node=" + nodeId + "&intf=" + ipAddr + "'>Interface</a>"); %>
<% String breadcrumb4 = java.net.URLEncoder.encode("Service"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Service" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb4%>" />
</jsp:include>

<br>

<!-- Body -->
<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>

    <td width="100%" valign="top" >
      <h2><%=service_db.getServiceName()%> service on <%=service_db.getIpAddress()%></h2>

      <p><a href="<%=eventUrl%>">View Events</a>
      </p>
 
      <table width="100%" border="0" cellspacing="0" cellpadding="2" >
        <tr>
          <td valign="top" width="48%">
            <!-- general info box -->
            <table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">
              <tr bgcolor="#999999">
                <td colspan="2" ><b>General</b></td> 
              </tr>
              <tr>
                <td>Node</td> 
                <td><a href="element/node.jsp?node=<%=service_db.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(service_db.getNodeId())%></a></td>
              </tr>
              <tr>
                <td>Interface</td> 
                <td><a href="element/interface.jsp?node=<%=service_db.getNodeId()%>&intf=<%=service_db.getIpAddress()%>"><%=service_db.getIpAddress()%></a></td>
              </tr>              
              <tr> 
                <td>Polling Status</td>
                <td><%=ElementUtil.getServiceStatusString(service_db)%></td>
              </tr>
            </table>
            <br>
          
            <!-- Availability box -->
            <jsp:include page="/includes/serviceAvailability-box.jsp" flush="false" />
            
          </td>

          <td>&nbsp;</td>
          
          <td valign="top" width="48%">
            <!-- events list box -->
            <% String eventHeader = java.net.URLEncoder.encode("<a href=\"" + eventUrl + "\">Recent Events</a>"); %>
            <% String moreEventsUrl = java.net.URLEncoder.encode(eventUrl); %>
            <jsp:include page="/includes/eventlist.jsp" flush="false" >
              <jsp:param name="node" value="<%=nodeId%>" />
              <jsp:param name="ipAddr" value="<%=ipAddr%>" />
              <jsp:param name="service" value="<%=serviceId%>" />              
              <jsp:param name="throttle" value="5" />
              <jsp:param name="header" value="<%=eventHeader%>" />
              <jsp:param name="moreUrl" value="<%=moreEventsUrl%>" />
            </jsp:include>            
            <br>
      
            <!-- Recent outages box -->
            <jsp:include page="/includes/serviceOutages-box.jsp" flush="false" />
            <br>          
          </td>
         </tr>
       </table>    
    </td>
    
    <td>&nbsp;</td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>


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
	import="org.opennms.web.element.*,
		org.opennms.web.category.*,
		java.util.*,
		org.opennms.web.event.*
	"
%>

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

<% String headTitle = service_db.getServiceName() + " Service on " + ipAddr; %>
<% String breadcrumb2 = "<a href='element/node.jsp?node=" + nodeId  + "'>Node</a>"; %>
<% String breadcrumb3 = "<a href='element/interface.jsp?node=" + nodeId + "&intf=" + ipAddr  + "'>Interface</a>"; %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Service" />
  <jsp:param name="headTitle" value="<%= headTitle %>" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb3%>" />
  <jsp:param name="breadcrumb" value="Service" />
</jsp:include>
       
<% if (request.isUserInRole("OpenNMS Administrator")) { %>

<script language="Javascript" type="text/javascript" >
function doDelete() {
     if (confirm("Are you sure you want to proceed? This action will permanently delete this service and cannot be undone."))
     {
         document.forms["delete"].submit();
     }
     return false;
}
</script>

<% } %>

      <h2><%=service_db.getServiceName()%> service on <%=service_db.getIpAddress()%></h2>

         <% if (request.isUserInRole("OpenNMS Administrator")) { %>
         <form method="POST" name="delete" action="admin/deleteService">
         <input type="hidden" name="node" value="<%=nodeId%>">
         <input type="hidden" name="intf" value="<%=ipAddr%>">
         <input type="hidden" name="service" value="<%=serviceId%>">
         <% } %>
      <p>
         <a href="<%=eventUrl%>">View Events</a>
         
         <% if (request.isUserInRole("OpenNMS Administrator")) { %>
         &nbsp;&nbsp;&nbsp;<a href="admin/deleteService" onClick="return doDelete()">Delete</a>
         <% } %>
      </p>
 
         <% if (request.isUserInRole("OpenNMS Administrator")) { %>
         </form>
         <% } %>


      <div id="contentleft">
            <!-- general info box -->
            <table class="standardfirst">
              <tr>
                <td class="standardheader" colspan="2">General</td> 
              </tr>
              <tr>
                <td class="standard">Node</td> 
                <td class="standard"><a href="element/node.jsp?node=<%=service_db.getNodeId()%>"><%=NetworkElementFactory.getNodeLabel(service_db.getNodeId())%></a></td>
              </tr>
              <tr>
                <td class="standard">Interface</td> 
                <td class="standard"><a href="element/interface.jsp?node=<%=service_db.getNodeId()%>&intf=<%=service_db.getIpAddress()%>"><%=service_db.getIpAddress()%></a></td>
              </tr>              
              <tr> 
                <td class="standard">Polling Status</td>
                <td class="standard"><%=ElementUtil.getServiceStatusString(service_db)%></td>
              </tr>
            </table>
          
            <!-- Availability box -->
            <jsp:include page="/includes/serviceAvailability-box.jsp" flush="false" />
            
      </div> <!-- id="contentleft" -->

      <div id="contentright">
            <!-- events list box -->
            <% String eventHeader = "<a href='" + eventUrl + "'>Recent Events</a>"; %>
            <% String moreEventsUrl = eventUrl; %>
            <jsp:include page="/includes/eventlist.jsp" flush="false" >
              <jsp:param name="node" value="<%=nodeId%>" />
              <jsp:param name="ipAddr" value="<%=ipAddr%>" />
              <jsp:param name="service" value="<%=serviceId%>" />              
              <jsp:param name="throttle" value="5" />
              <jsp:param name="header" value="<%=eventHeader%>" />
              <jsp:param name="moreUrl" value="<%=moreEventsUrl%>" />
            </jsp:include>            
      
            <!-- Recent outages box -->
            <jsp:include page="/includes/serviceOutages-box.jsp" flush="false" />
      </div> <!-- id="contentright" -->

<jsp:include page="/includes/footer.jsp" flush="false" />

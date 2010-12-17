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
	import="
		org.opennms.web.springframework.security.Authentication,
		org.opennms.web.element.ElementUtil,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.web.element.Service
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    Service service = ElementUtil.getServiceByParams(request, getServletContext());
	
	int nodeId = service.getNodeId();
	String ipAddr = service.getIpAddress();
 	int serviceId = service.getServiceId();
%>
<c:url var="eventUrl" value="event/list.htm">
  <c:param name="filter" value="<%="node=" + nodeId%>"/>
  <c:param name="filter" value="<%="interface=" + ipAddr%>"/>
  <c:param name="filter" value="<%="service=" + serviceId%>"/>
</c:url>


<% String headTitle = service.getServiceName() + " Service on " + ipAddr; %>
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
       
<% if (request.isUserInRole( Authentication.ADMIN_ROLE )) { %>

<script type="text/javascript" >
function doDelete() {
     if (confirm("Are you sure you want to proceed? This action will permanently delete this service and cannot be undone."))
     {
         document.forms["delete"].submit();
     }
     return false;
}
</script>

<% } %>

      <h2><%=service.getServiceName()%> service on <%=service.getIpAddress()%></h2>

         <% if (request.isUserInRole(Authentication.ADMIN_ROLE)) { %>
         <form method="post" name="delete" action="admin/deleteService">
         <input type="hidden" name="node" value="<%=nodeId%>"/>
         <input type="hidden" name="intf" value="<%=ipAddr%>"/>
         <input type="hidden" name="service" value="<%=serviceId%>"/>
         <% } %>
      <p>
         <a href="${eventUrl}">View Events</a>
         
         <% if (request.isUserInRole(Authentication.ADMIN_ROLE)) { %>
         &nbsp;&nbsp;&nbsp;<a href="admin/deleteService" onClick="return doDelete()">Delete</a>
         <% } %>
      </p>
 
         <% if (request.isUserInRole( Authentication.ADMIN_ROLE)) { %>
         </form>
         <% } %>


      <div class="TwoColLeft">
            <!-- general info box -->
            <h3>General</h3>
            <table>
              <tr>
                <c:url var="nodeLink" value="element/node.jsp">
                  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
                </c:url>
                <th>Node</th>
                <td><a href="${nodeLink}"><%=NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(nodeId)%></a></td>
              </tr>
              <tr>
                <c:url var="interfaceLink" value="element/interface.jsp">
                  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
                  <c:param name="intf" value="<%=ipAddr%>"/>
                </c:url>
                <th>Interface</th> 
                <td><a href="${interfaceLink}"><%=ipAddr%></a></td>
              </tr>              
              <tr>
                <th>Polling Status</th>
                <td><%=ElementUtil.getServiceStatusString(service)%></td>
              </tr>
            </table>
          
            <!-- Availability box -->
            <jsp:include page="/includes/serviceAvailability-box.jsp" flush="false" />
            
            <jsp:include page="/includes/serviceApplication-box.htm" flush="false" />
            
      </div> <!-- class="TwoColLeft" -->

      <div class="TwoColRight">
            <!-- events list box -->
            <jsp:include page="/includes/eventlist.jsp" flush="false" >
              <jsp:param name="node" value="<%=nodeId%>" />
              <jsp:param name="ipAddr" value="<%=ipAddr%>" />
              <jsp:param name="service" value="<%=serviceId%>" />
              <jsp:param name="throttle" value="5" />
              <jsp:param name="header" value="<a href='${eventUrl}'>Recent Events</a>" />
              <jsp:param name="moreUrl" value="${eventUrl}" />
            </jsp:include>
      
            <!-- Recent outages box -->
            <jsp:include page="/outage/serviceOutages-box.htm" flush="false" />
      </div> <!-- class="TwoColRight" -->

<jsp:include page="/includes/footer.jsp" flush="false" />

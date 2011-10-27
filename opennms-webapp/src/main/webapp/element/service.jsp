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
	import="
		org.opennms.web.springframework.security.Authentication,
		org.opennms.web.element.ElementUtil,
		org.opennms.web.element.NetworkElementFactory,
		org.opennms.web.element.Service
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

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
<c:url var="nodeLink" value="element/node.jsp">
  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
</c:url>
<c:url var="interfaceLink" value="element/interface.jsp">
  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
  <c:param name="intf" value="<%=ipAddr%>"/>
</c:url>

<% String headTitle = service.getServiceName() + " Service on " + ipAddr; %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Service" />
  <jsp:param name="headTitle" value="<%=headTitle%>" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<a href='${fn:escapeXml(nodeLink)}'>Node</a>" />
  <jsp:param name="breadcrumb" value="<a href='${fn:escapeXml(interfaceLink)}'>Interface</a>" />
  <jsp:param name="breadcrumb" value="Service" />
</jsp:include>
       
<% if (request.isUserInRole( Authentication.ROLE_ADMIN )) { %>

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

      <h2><%=service.getServiceName()%> service on <%=ipAddr%></h2>

         <% if (request.isUserInRole(Authentication.ROLE_ADMIN)) { %>
         <form method="post" name="delete" action="admin/deleteService">
         <input type="hidden" name="node" value="<%=nodeId%>"/>
         <input type="hidden" name="intf" value="<%=ipAddr%>"/>
         <input type="hidden" name="service" value="<%=serviceId%>"/>
         <% } %>
      <p>
         <a href="${eventUrl}">View Events</a>
         
         <% if (request.isUserInRole(Authentication.ROLE_ADMIN)) { %>
         &nbsp;&nbsp;&nbsp;<a href="admin/deleteService" onClick="return doDelete()">Delete</a>
         <% } %>
      </p>
 
         <% if (request.isUserInRole( Authentication.ROLE_ADMIN)) { %>
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
                <td><a href="${fn:escapeXml(nodeLink)}"><%=NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(nodeId)%></a></td>
              </tr>
              <tr>
                <c:url var="interfaceLink" value="element/interface.jsp">
                  <c:param name="node" value="<%=String.valueOf(nodeId)%>"/>
                  <c:param name="intf" value="<%=ipAddr%>"/>
                </c:url>
                <th>Interface</th> 
                <td><a href="${fn:escapeXml(interfaceLink)}"><%=ipAddr%></a></td>
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
              <jsp:param name="header" value="<a href='${fn:escapeXml(eventUrl)}'>Recent Events</a>" />
              <jsp:param name="moreUrl" value="${fn:escapeXml(eventUrl)}" />
            </jsp:include>
      
            <!-- Recent outages box -->
            <jsp:include page="/outage/serviceOutages-box.htm" flush="false" />
      </div> <!-- class="TwoColRight" -->

<jsp:include page="/includes/footer.jsp" flush="false" />

<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"	contentType="text/html"	session="true" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="org.opennms.core.utils.WebSecurityUtils"%>
<%@page import="org.opennms.netmgt.events.api.EventConstants"%>

<%@page import="org.opennms.web.event.AcknowledgeType"%>
<%@page import="org.opennms.web.event.Event"%>
<%@page import="org.opennms.web.servlet.XssRequestWrapper"%>
<%@page import="org.springframework.util.Assert"%>

<%
	XssRequestWrapper req = new XssRequestWrapper(request);
	Event[] events = (Event[])req.getAttribute("events");
	Event event = null;
	String action = null;
    String buttonName=null;
    Map<String, String> parms = new HashMap<String, String>();
	if ( events.length > 0 ) {
		Assert.isTrue(events.length == 1, "event detail filter should match only one event: event found:" + events.length);

    	event = events[0];
    
	    if (event.getAcknowledgeTime()==null)
	    {
	        buttonName = "Acknowledge";
	        action = AcknowledgeType.ACKNOWLEDGED.getShortName();
	    }
	    else
	    {
	        buttonName = "Unacknowledge";
	        action = AcknowledgeType.UNACKNOWLEDGED.getShortName();
	    }

	    parms = event.getParms();
	}

%>

<% boolean provisioned = parms.containsKey(EventConstants.PARM_LOCATION_MONITOR_ID); %>
<% boolean acknowledgeEvent = "true".equals(System.getProperty("opennms.eventlist.acknowledge")); %>
<% boolean canAck = (request.isUserInRole(org.opennms.web.api.Authentication.ROLE_ADMIN) || !request.isUserInRole(org.opennms.web.api.Authentication.ROLE_READONLY)); %>

<c:set var="provisioned" value="<%=provisioned%>"/>
<c:set var="acknowledgeEvent" value="<%=acknowledgeEvent%>"/>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Event Detail" />
  <jsp:param name="headTitle" value="Detail" />
  <jsp:param name="headTtitle" value="Events" />
  <jsp:param name="breadcrumb" value="<a href='event/index'>Events</a>" />
  <jsp:param name="breadcrumb" value='<%="Event " + (event == null? "Not Found" : event.getId()) %>' />
</jsp:include>

<% if (event == null ) { %>
    <p>Event not found in database.</p>
<% } else { %>

    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Event <%=event.getId()%></h3>
      </div>

      <table class="table table-condensed severity">
        <tr class="severity-<%= event.getSeverity().getLabel().toLowerCase() %>">
          <th class="col-md-1">Severity</th>
          <td class="col-md-3 bright"><%= event.getSeverity().getLabel() %></td>
          <th class="col-md-1">Node</th>
          <td ${acknowledgeEvent ? '' : 'colspan="3"'} class="${acknowledgeEvent ? 'col-md-3' : 'col-md-7'}">
            <% if( event.getNodeId() > 0 ) { %>
              <a href="element/node.jsp?node=<%=event.getNodeId()%>"><%=event.getNodeLabel()%></a>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <c:if test="${acknowledgeEvent}">
            <th class="col-md-1">Acknowledged&nbsp;By</th>
            <td class="col-md-3"><%=event.getAcknowledgeUser()!=null ? event.getAcknowledgeUser() : "&nbsp;"%></td>
          </c:if>
        </tr>
          <tr class="severity-<%= event.getSeverity().getLabel().toLowerCase() %>">
              <th class="col-md-1">Event Source Location</th>
              <td class="col-md-3"><%=event.getLocation()%> (<%= event.getSystemId() %>)</td>
              <th class="col-md-1">Node Location</th>
              <td class="col-md-3"><%= event.getNodeLocation() %></td>
          </tr>
          <tr class="severity-<%= event.getSeverity().getLabel().toLowerCase() %>">
          <th class="col-md-1">Time</th>
          <td class="col-md-3"><fmt:formatDate value="<%=event.getTime()%>" type="BOTH" /></td>
          <th class="col-md-1">Interface</th>
          <td ${acknowledgeEvent ? '' : 'colspan="3"'} class="${acknowledgeEvent ? 'col-md-3' : 'col-md-7'}">
            <% if( event.getIpAddress() != null ) { %>
              <% if( event.getNodeId() > 0 ) { %>
                <c:url var="interfaceLink" value="element/interface.jsp">
                  <c:param name="node" value="<%=String.valueOf(event.getNodeId())%>"/>
                  <c:param name="intf" value="<%=event.getIpAddress()%>"/>
                </c:url>
                <a href="${interfaceLink}"><%=event.getIpAddress()%></a>
              <% } else { %>
                <%=event.getIpAddress()%>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <c:if test="${acknowledgeEvent}">
          <th class="col-md-1">Time&nbsp;Acknowledged</th>
          <td class="col-md-3">
          <c:choose>
            <c:when test="<%=event.getAcknowledgeTime() != null%>">
              <fmt:formatDate value="<%=event.getAcknowledgeTime()%>" type="BOTH" />
            </c:when>
            <c:otherwise>
              &nbsp;
            </c:otherwise>
          </c:choose>
          </td>
          </c:if>
        </tr>
        
        <tr class="severity-<%= event.getSeverity().getLabel().toLowerCase() %>">
          <th class="col-md-1">Service</th>
          <!-- If the node is not provisioned, then expand the service row out with colspan 5, col-md-11 -->
          <td ${provisioned ? '' : 'colspan="5"'} class="${provisioned ? 'col-md-3' : 'col-md-11'}">
            <% if( event.getServiceName() != null ) { %>
              <% if( event.getIpAddress() != null && event.getNodeId() > 0 ) { %>
                <c:url var="serviceLink" value="element/service.jsp">
                  <c:param name="node" value="<%=String.valueOf(event.getNodeId())%>"/>
                  <c:param name="intf" value="<%=event.getIpAddress()%>"/>
                  <c:param name="service" value="<%=String.valueOf(event.getServiceId())%>"/>
                </c:url>
                <a href="${serviceLink}"><c:out value="<%=event.getServiceName()%>"/></a>
              <% } else { %>
                <c:out value="<%=event.getServiceName()%>"/>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <c:if test="${provisioned}">
            <th class="col-md-1">Location&nbsp;Monitor&nbsp;ID</th>
            <td colspan="3" class="col-md-7"><a href="distributed/locationMonitorDetails.htm?monitorId=<%= parms.get(EventConstants.PARM_LOCATION_MONITOR_ID)%>"><%= parms.get(EventConstants.PARM_LOCATION_MONITOR_ID) %></a></td>
          </c:if>
        </tr> 
          
        <tr class="severity-<%= event.getSeverity().getLabel().toLowerCase() %>">
          	<th class="col-md-1">UEI</th>
                <td colspan="5" class="col-md-11">
          	<% if( event.getUei() != null ) { %>
          	      <%=event.getUei()%>
          	<% } else {%>
                	&nbsp;
          	<% } %>
                </td>
        </tr>

          <% if (event.getAlarmId() != null && event.getAlarmId().intValue() != 0) { %>
            <tr class="severity-<%= event.getSeverity().getLabel().toLowerCase() %>">
              <th class="col-md-1">Alarm ID</th>
              <td colspan="5" class="col-md-11">
                  <a href="alarm/detail.htm?id=<%=event.getAlarmId()%>"><%=event.getAlarmId()%></a>
              </td>
            </tr>
          <% }%>

      </table>
    </div>

    <div class="panel panel-default severity">
      <div class="panel-heading">
        <h3 class="panel-title">Log&nbsp;Message</h3>
      </div>
      <div class="panel-body severity-<%= event.getSeverity().getLabel().toLowerCase() %>">
        <%=WebSecurityUtils.sanitizeString(event.getLogMessage(), true)%>
      </div>
    </div>

    <div class="panel panel-default severity">
      <div class="panel-heading">
        <h3 class="panel-title">Description</h3>
      </div>
      <div class="panel-body severity-<%= event.getSeverity().getLabel().toLowerCase() %>">
        <%=WebSecurityUtils.sanitizeString(event.getDescription(), true)%>
      </div>
    </div>

    <div class="panel panel-default severity">
      <div class="panel-heading">
        <h3 class="panel-title">Operator&nbsp;Instructions</h3>
      </div>
      <div class="panel-body severity-<%= event.getSeverity().getLabel().toLowerCase() %>">
        <% if (event.getOperatorInstruction()==null) { %>
          No instructions available.
        <% } else { %>
          <%=event.getOperatorInstruction()%>
        <% } %>
      </div>
    </div>
 
    <c:if test="<%=canAck && acknowledgeEvent %>">
      <form method="post" action="event/acknowledge">
        <input type="hidden" name="actionCode" value="<%=action%>" />
        <input type="hidden" name="event" value="<%=event.getId()%>"/>
        <input type="hidden" name="redirect" value="<%= "detail.jsp?" + request.getQueryString()%>" />
        <input type="submit" value="<%=buttonName%>"/>
      </form>
    </c:if>

<% } %>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

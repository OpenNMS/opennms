<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"	contentType="text/html"	session="true" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib uri="/WEB-INF/taglib.tld" prefix="onms" %>

<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashSet"%>
<%@page import="org.opennms.core.utils.WebSecurityUtils"%>
<%@page import="org.opennms.netmgt.events.api.EventConstants"%>

<%@page import="org.opennms.web.event.AcknowledgeType"%>
<%@page import="org.opennms.web.event.Event"%>
<%@page import="org.opennms.web.event.EventIdNotFoundException"%>
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

    if (event == null) {
      throw new EventIdNotFoundException("Event not found in database.", (String) req.getAttribute("eventId"));
    }
%>

<% boolean provisioned = parms.containsKey(EventConstants.PARM_LOCATION_MONITOR_ID); %>
<% boolean acknowledgeEvent = "true".equals(System.getProperty("opennms.eventlist.acknowledge")); %>
<% boolean canAck = (request.isUserInRole(org.opennms.web.api.Authentication.ROLE_ADMIN) || !request.isUserInRole(org.opennms.web.api.Authentication.ROLE_READONLY)); %>
<% 
    boolean showParms = "true".equals(System.getProperty("opennms.eventdetail.showParms")); 
    if (showParms) {
        Set<String> parmRoles = new HashSet<>();
        for (String role : System.getProperty("opennms.eventdetail.parmsRoles", org.opennms.web.api.Authentication.ROLE_USER).split("\\s*,\\s*")) {
            if (org.opennms.web.api.Authentication.isValidRole(role)) {
                parmRoles.add(role);
            }
        }
        showParms = false;
        for (String r : parmRoles) {
            if (request.isUserInRole(r)) {
               showParms = true;
               break;
            }
        }
    }
%>

<c:set var="provisioned" value="<%=provisioned%>"/>
<c:set var="acknowledgeEvent" value="<%=acknowledgeEvent%>"/>
<c:set var="showParms" value="<%=showParms%>"/>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Detail")
          .headTitle("Events")
          .breadcrumb("Events", "event/index")
          .breadcrumb("Event " + (event == null? "Not Found" : event.getId()))
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<% if (event == null ) { %>
    <p>Event not found in database.</p>
<% } else { %>

    <div class="card">
      <div class="card-header">
        <span>Event <%=event.getId()%></span>
      </div>

      <table class="table table-sm severity">
        <tr class="severity-<%= event.getSeverity().getLabel().toLowerCase() %> d-flex">
          <th class="col-1">Severity</th>
          <td class="col-3 bright"><%= event.getSeverity().getLabel() %></td>
          <th class="col-1">Node</th>
          <td ${acknowledgeEvent ? '' : 'colspan="3"'} class="${acknowledgeEvent ? 'col-3' : 'col-7'}">
            <% if( event.getNodeId() > 0 ) { %>
              <a href="element/node.jsp?node=<%=event.getNodeId()%>"><%=WebSecurityUtils.sanitizeString(event.getNodeLabel())%></a>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <c:if test="${acknowledgeEvent}">
            <th class="col-1">Acknowledged&nbsp;By</th>
            <td class="col-4"><%=event.getAcknowledgeUser()!=null ? event.getAcknowledgeUser() : "&nbsp;"%></td>
          </c:if>
        </tr>
          <tr class="severity-<%= event.getSeverity().getLabel().toLowerCase() %> d-flex">
              <th class="col-1">Event Source Location</th>
              <td class="col-3"><%=event.getLocation()%> (<%= event.getSystemId() %>)</td>
              <th class="col-1">Node Location</th>
              <td class="col-3"><%= event.getNodeLocation() %></td>
          </tr>
          <tr class="severity-<%= event.getSeverity().getLabel().toLowerCase() %> d-flex">
          <th class="col-1">Time</th>
          <td class="col-3"><onms:datetime date="<%=event.getTime()%>" /></td>
          <th class="col-1">Interface</th>
          <td ${acknowledgeEvent ? '' : 'colspan="3"'} class="${acknowledgeEvent ? 'col-3' : 'col-7'}">
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
          <th class="col-1">Time&nbsp;Acknowledged</th>
          <td class="col-3">
          <c:choose>
            <c:when test="<%=event.getAcknowledgeTime() != null%>">
              <onms:datetime date="<%=event.getAcknowledgeTime()%>" />
            </c:when>
            <c:otherwise>
              &nbsp;
            </c:otherwise>
          </c:choose>
          </td>
          </c:if>
        </tr>
        <tr class="severity-<%= event.getSeverity().getLabel().toLowerCase() %> d-flex">
          <th class="col-1">Service</th>
          <!-- If the node is not provisioned, then expand the service row out with colspan 5, col-11 -->
          <td ${provisioned ? '' : 'colspan="5"'} class="${provisioned ? 'col-3' : 'col-11'}">
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
            <th class="col-1">Location&nbsp;Monitor&nbsp;ID</th>
            <td colspan="3" class="col-7"><a href="distributed/locationMonitorDetails.htm?monitorId=<%= parms.get(EventConstants.PARM_LOCATION_MONITOR_ID)%>"><%= parms.get(EventConstants.PARM_LOCATION_MONITOR_ID) %></a></td>
          </c:if>
        </tr>

        <tr class="severity-<%= event.getSeverity().getLabel().toLowerCase() %> d-flex">
          	<th class="col-1">UEI</th>
                <td colspan="5" class="col-11">
          	<% if( event.getUei() != null ) { %>
          	      <%=event.getUei()%>
          	<% } else {%>
                	&nbsp;
          	<% } %>
                </td>
        </tr>

          <% if (event.getAlarmId() != null && event.getAlarmId().intValue() != 0) { %>
            <tr class="severity-<%= event.getSeverity().getLabel().toLowerCase() %> d-flex">
              <th class="col-1">Alarm ID</th>
              <td colspan="5" class="col-11">
                  <a href="alarm/detail.htm?id=<%=event.getAlarmId()%>"><%=event.getAlarmId()%></a>
              </td>
            </tr>
          <% }%>

      </table>
    </div>

    <c:if test="${showParms}">
    <div class="card severity">
      <div class="card-header">
        <span>Parameters</span>
      </div>
      <% if (event.getParms() != null && event.getParms().size() > 0) { %>
      <table class="table table-sm severity">
        <% for (Map.Entry<String, String> parm : event.getParms().entrySet()) { %>
        <tr class="severity-<%= event.getSeverity().getLabel().toLowerCase() %> d-flex">
          <th class="col-4"><%=WebSecurityUtils.sanitizeString(parm.getKey(), true)%></th>
          <td class="col"><%=WebSecurityUtils.sanitizeString(parm.getValue(), true)%></td>
        </tr>
        <% } %>
      </table>
      <% } else { %>
      <div class="card-body severity-<%= event.getSeverity().getLabel().toLowerCase() %>">
        No event parameters available.
      </div>
      <% }%>
    </div>
    </c:if>

    <div class="card severity">
      <div class="card-header">
        <span>Log&nbsp;Message</span>
      </div>
      <div class="card-body severity-<%= event.getSeverity().getLabel().toLowerCase() %>">
        <%=WebSecurityUtils.sanitizeString(event.getLogMessage(), true)%>
      </div>
    </div>

    <div class="card severity">
      <div class="card-header">
        <span>Description</span>
      </div>
      <div class="card-body severity-<%= event.getSeverity().getLabel().toLowerCase() %>">
        <%=WebSecurityUtils.sanitizeString(event.getDescription(), true)%>
      </div>
    </div>

    <div class="card severity">
      <div class="card-header">
        <span>Operator&nbsp;Instructions</span>
      </div>
      <div class="card-body severity-<%= event.getSeverity().getLabel().toLowerCase() %>">
        <% if (event.getOperatorInstruction()==null) { %>
          No instructions available.
        <% } else { %>
          <%=event.getOperatorInstruction()%>
        <% } %>
      </div>
    </div>
 
    <c:if test="<%=canAck && acknowledgeEvent %>">
      <form method="post" action="event/acknowledge">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        <input type="hidden" name="actionCode" value="<%=action%>" />
        <input type="hidden" name="event" value="<%=event.getId()%>"/>
        <input type="hidden" name="redirect" value="<%= "detail.jsp?" + request.getQueryString()%>" />
        <input type="submit" value="<%=buttonName%>"/>
      </form>
    </c:if>

<% } %>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

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
// 2002 Jul 18: Added operinstructions to display.
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
		java.util.HashMap,
		java.util.regex.Matcher,
		java.util.regex.Pattern,
		org.opennms.netmgt.EventConstants,
		org.opennms.web.WebSecurityUtils,
		org.opennms.web.event.Event,
		org.opennms.web.event.EventFactory,
		org.opennms.web.event.EventUtil,
		org.opennms.web.Util,
        org.opennms.web.springframework.security.Authentication,
        org.opennms.web.event.AcknowledgeType
        "

%>

<%
    String eventIdString = request.getParameter( "id" );

    if( eventIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "id" );
    }

    int eventId = -1;

    try {
        eventId = WebSecurityUtils.safeParseInt( eventIdString );
    }
    catch( NumberFormatException e ) {
        throw new org.opennms.web.event.EventIdNotFoundException( "The event id must be an integer.", eventIdString );
    }

    Event event = EventFactory.getEvent( eventId );

    if( event == null ) {
        throw new org.opennms.web.event.EventIdNotFoundException( "An event with this id was not found.", String.valueOf(eventId) );
    }
    
    String action = null;
    String buttonName=null;
    
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
    
    Pattern p = Pattern.compile("([^=]+)=(.*)\\((\\w+),(\\w+)\\)");
    
    HashMap<String, String> parms = new HashMap<String, String>();
    if (event.getParms() != null) {
		String[] parmStrings = event.getParms().split(";");
		for (String parmString : parmStrings) {
			Matcher m = p.matcher(parmString);
			if (!m.matches()) {
				log("Could not match event parameter string element '"
					+ parmString + "' in event ID " + event.getId());
				continue;
			}
			
			parms.put(m.group(1), m.group(2));
		}
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Event Detail" />
  <jsp:param name="headTitle" value="Detail" />
  <jsp:param name="headTtitle" value="Events" />
  <jsp:param name="breadcrumb" value="<a href='event/index.jsp'>Events</a>" />
  <jsp:param name="breadcrumb" value="Detail" />
</jsp:include>

      <h3>Event <%=event.getId()%></h3>

      <table>
        <tr class="<%= event.getSeverity().getLabel() %>">
          <th class="divider" width="10%">Severity</th>
          <td class="divider"><%= event.getSeverity().getLabel() %></td>
          <th class="divider" width="10%">Node</th>
          <td class="divider">
            <% if( event.getNodeId() > 0 ) { %>
              <a href="element/node.jsp?node=<%=event.getNodeId()%>"><%=event.getNodeLabel()%></a>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <th class="divider" width="10%">Acknowledged&nbsp;By</th>
          <td class="divider"><%=event.getAcknowledgeUser()!=null ? event.getAcknowledgeUser() : "&nbsp"%></td>
        </tr>
        
        <tr  class="<%= event.getSeverity().getLabel() %>">
          <th>Time</th>
          <td><%=org.opennms.web.Util.formatDateToUIString(event.getTime())%></td>
          <th>Interface</th>
          <td>
            <% if( event.getIpAddress() != null ) { %>
              <% if( event.getNodeId() > 0 ) { %>
                <a href="element/interface.jsp?node=<%=event.getNodeId()%>&intf=<%=event.getIpAddress()%>"><%=event.getIpAddress()%></a>
              <% } else { %>
                <%=event.getIpAddress()%>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <th>Time&nbsp;Acknowledged</th>
          <td><%=event.getAcknowledgeTime()!=null ? org.opennms.web.Util.formatDateToUIString(event.getAcknowledgeTime()) : "&nbsp"%></td>
        </tr>
        
        <tr class="<%= event.getSeverity().getLabel() %>">
          <th>Service</th>
          <td>
            <% if( event.getServiceName() != null ) { %>
              <% if( event.getIpAddress() != null && event.getNodeId() > 0 ) { %>
                <a href="element/service.jsp?node=<%=event.getNodeId()%>&intf=<%=event.getIpAddress()%>&service=<%=event.getServiceId()%>"><%=event.getServiceName()%></a>
              <% } else { %>
                <%=event.getServiceName()%>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <% if (parms.containsKey(EventConstants.PARM_LOCATION_MONITOR_ID)) { %>
            <th>Location Monitor ID</th>
            <td><a href="distributed/locationMonitorDetails.htm?monitorId=<%= parms.get(EventConstants.PARM_LOCATION_MONITOR_ID)%>"><%= parms.get(EventConstants.PARM_LOCATION_MONITOR_ID) %></a></td>
            <td colspan="2">&nbsp;</td>
          <% } else { %>
            <td colspan="4">&nbsp;</td>
          <% } %>
        </tr> 
          
        <tr class="<%= event.getSeverity().getLabel() %>">
          	<th>UEI</th>
                <td>
          	<% if( event.getUei() != null ) { %>
          	      <%=event.getUei()%>
          	<% } else {%>
                	&nbsp;
          	<% } %>
                </td>
                <td colspan="4">&nbsp;</td>
        </tr>
      </table>

      <table>
        <tr class="<%= event.getSeverity().getLabel() %>">
          <th>Log Message</th>
        </tr>
        <tr class="<%= event.getSeverity().getLabel() %>">
          <td><%=event.getLogMessage()%></td>
        </tr>
      </table>

      <table>
        <tr class="<%= event.getSeverity().getLabel() %>">
          <th>Description</th>
        </tr>
        <tr class="<%= event.getSeverity().getLabel() %>">
          <td><%=event.getDescription()%></td>
        </tr>
      </table>
      
      <table>
        <tr class="<%= event.getSeverity().getLabel() %>">
          <th>Operator Instructions</th>
        </tr>
        <tr class="<%= event.getSeverity().getLabel() %>">
          <td>
	    <%if (event.getOperatorInstruction()==null) { %>
              No instructions available
            <% } else { %>
              <%=event.getOperatorInstruction()%>
            <% } %>
	  </td>
        </tr>
      </table>

      <% if( !(request.isUserInRole( Authentication.READONLY_ROLE ))) { %>
        <form method="post" action="event/acknowledge">
          <input type="hidden" name="action" value="<%=action%>" />
          <input type="hidden" name="event" value="<%=event.getId()%>"/>
          <input type="hidden" name="redirect" value="<%=request.getContextPath() + request.getServletPath() + "?" + request.getQueryString()%>" />
          <input type="submit" value="<%=buttonName%>"/>
        </form>
      <% } %>
      
<jsp:include page="/includes/footer.jsp" flush="false" />

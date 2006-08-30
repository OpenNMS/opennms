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
	import="org.opennms.web.alarm.*"
%>

<%
    String alarmIdString = request.getParameter( "id" );

    if( alarmIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "id" );
    }

    int alarmId = -1;

    try {
        alarmId = Integer.parseInt( alarmIdString );
    }
    catch( NumberFormatException e ) {
        throw new org.opennms.web.alarm.AlarmIdNotFoundException( "The alarm id must be an integer.", alarmIdString );
    }

    Alarm alarm = AlarmFactory.getAlarms( alarmId );

    if( alarm == null ) {
        throw new org.opennms.web.alarm.AlarmIdNotFoundException( "An alarm with this id was not found.", String.valueOf(alarmId) );
    }
    
    String action = null;
    String buttonName=null;
    
    if (alarm.getAcknowledgeTime()==null)
    {
        buttonName = "Acknowledge";
        action = AcknowledgeAlarmServlet.ACKNOWLEDGE_ACTION;
    }
    else
    {
        buttonName = "Unacknowledge";
        action = AcknowledgeAlarmServlet.UNACKNOWLEDGE_ACTION;
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Alarm Detail" />
  <jsp:param name="headTitle" value="Detail" />
  <jsp:param name="headTitle" value="Alarms" />
  <jsp:param name="breadcrumb" value="<a href='alarm/index.jsp'>Alarms</a>" />
  <jsp:param name="breadcrumb" value="Detail" />
</jsp:include>

      <h3>Alarm <%=alarm.getId()%></h3>
 
      <table class="standardfirst">
        <tr>
          <td class="standardheader" width="10%">Severity</td>
          <td class="standard" style="background-color: <%=AlarmUtil.getSeverityColor(alarm.getSeverity())%>"><%=AlarmUtil.getSeverityLabel(alarm.getSeverity())%></td>
          <td class="standardheader" width="10%">Node</td>
          <td class="standard">
            <% if( alarm.getNodeId() > 0 ) { %>
              <a href="element/node.jsp?node=<%=alarm.getNodeId()%>"><%=alarm.getNodeLabel()%></a>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <td class="standardheader" width="10%">Acknowledged&nbsp;By</td>
          <td class="standard"><%=alarm.getAcknowledgeUser()!=null ? alarm.getAcknowledgeUser() : "&nbsp"%></td>
        </tr>
        <tr>
          <td class="standardheader">Last Event</td>
          <td class="standard"><%=org.opennms.netmgt.EventConstants.formatToUIString(alarm.getLastEventTime())%></td>
          <td class="standardheader">Interface</td>
          <td class="standard">
            <% if( alarm.getIpAddress() != null ) { %>
              <% if( alarm.getNodeId() > 0 ) { %>
                <a href="element/interface.jsp?node=<%=alarm.getNodeId()%>&intf=<%=alarm.getIpAddress()%>"><%=alarm.getIpAddress()%></a>
              <% } else { %>
                <%=alarm.getIpAddress()%>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <td class="standardheader">Time&nbsp;Acknowledged</td>
          <td class="standard"><%=alarm.getAcknowledgeTime()!=null ? org.opennms.netmgt.EventConstants.formatToUIString(alarm.getAcknowledgeTime()) : "&nbsp"%></td>
        </tr>
        <tr>
          <td class="standardheader">First Event</td>
          <td class="standard"><%=org.opennms.netmgt.EventConstants.formatToUIString(alarm.getFirstEventTime())%></td>
          <td class="standardheader">Service</td>
          <td class="standard" colspan="3">
            <% if( alarm.getServiceName() != null ) { %>
              <% if( alarm.getIpAddress() != null && alarm.getNodeId() > 0 ) { %>
                <a href="element/service.jsp?node=<%=alarm.getNodeId()%>&intf=<%=alarm.getIpAddress()%>&service=<%=alarm.getServiceId()%>"><%=alarm.getServiceName()%></a>
              <% } else { %>
                <%=alarm.getServiceName()%>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          </tr> 
          <tr>
          	<td class="standardheader">Count</td>
	        <td class="standard"><%=alarm.getCount()%></td>
          	<td class="standardheader">UEI</td>
          	<td class="standard" colspan="3">
          	<% if( alarm.getUei() != null ) { %>
          	      <%=alarm.getUei()%>
          	<% } else {%>
                      &nbsp;
          	<% } %>
		</td>
        </tr>
        <tr>
          	<td class="standardheader">Reduct. Key</td>
          	<td class="standard" colspan="5">
          	<% if( alarm.getReductionKey() != null ) { %>
          	      <%=alarm.getReductionKey()%>
          	<% } else {%>
                      &nbsp;
          	<% } %>
		</td>
        </tr>
      </table>

      <table class="standard">
          <td class="standardheader">Log Message</td>
        </tr>
        <tr>
          <td class="standard"><%=alarm.getLogMessage()%></td>
        </tr>
      </table>

      <table class="standard">
        <tr>
          <td class="standardheader">Description</td>
        </tr>
        <tr>
          <td class="standard"><%=alarm.getDescription()%></td>
        </tr>
      </table>
      
      
      <table class="standard">
        <tr>
          <td class="standardheader">Operator Instructions</td>
        </tr>
	
        <tr>
          <td class="standard">
	    <%if (alarm.getOperatorInstruction()==null) { %>
              No instructions available
            <% } else { %>
              <%=alarm.getOperatorInstruction()%>
            <% } %>
	  </td>
        </tr>
      </table>

      <br/>
      
      <form method="post" action="alarm/acknowledge">
        <input type="hidden" name="action" value="<%=action%>" />
        <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
        <input type="hidden" name="redirect" value="<%=request.getContextPath() + request.getServletPath() + "?" + request.getQueryString()%>" />
        <input type="submit" value="<%=buttonName%>" />
      </form>

<jsp:include page="/includes/footer.jsp" flush="false" />

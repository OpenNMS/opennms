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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.event.*" %>

<%
    String eventIdString = request.getParameter( "id" );

    if( eventIdString == null ) {
        throw new org.opennms.web.MissingParameterException( "id" );
    }

    int eventId = -1;

    try {
        eventId = Integer.parseInt( eventIdString );
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
        action = AcknowledgeEventServlet.ACKNOWLEDGE_ACTION;
    }
    else
    {
        buttonName = "Unacknowledge";
        action = AcknowledgeEventServlet.UNACKNOWLEDGE_ACTION;
    }
%>

<html>
<head>
  <title> Detail | Events | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>

<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">

<% String breadcrumb1 = "<a href='" + java.net.URLEncoder.encode("event/index.jsp" + "'>Events</a>"; %>
<% String breadcrumb2 = java.net.URLEncoder.encode("Detail"); %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Event Detail" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>

<br>

<!-- Body -->
<table width="100%" border="0" cellspacing="0" cellpadding="2" >
  <tr>
    <td>&nbsp;</td>

    <td width="100%" valign="top" >
      <h3>Event <%=event.getId()%></h3>
 
      <table width="100%" border="1" cellspacing="0" cellpadding="2" bgcolor="#cccccc" bordercolor="black">
        <tr>
          <td width="10%" bgcolor="#999999"><b>Severity</b></td>
          <td bgcolor="<%=EventUtil.getSeverityColor(event.getSeverity())%>"><%=EventUtil.getSeverityLabel(event.getSeverity())%></td>
          <td width="10%" bgcolor="#999999"><b>Node</b></td>
          <td>
            <% if( event.getNodeId() > 0 ) { %>
              <a href="element/node.jsp?node=<%=event.getNodeId()%>"><%=event.getNodeLabel()%></a>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <td width="10%" bgcolor="#999999"><b>Acknowledged&nbsp;By</b></td>
          <td><%=event.getAcknowledgeUser()!=null ? event.getAcknowledgeUser() : "&nbsp"%></td>
        </tr>
        <tr>
          <td bgcolor="#999999"><b>Time</b></td>
          <td><%=org.opennms.netmgt.EventConstants.formatToUIString(event.getTime())%></td>
          <td bgcolor="#999999"><b>Interface</b></td>
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
          <td bgcolor="#999999"><b>Time&nbsp;Acknowledged</b></td>
          <td><%=event.getAcknowledgeTime()!=null ? org.opennms.netmgt.EventConstants.formatToUIString(event.getAcknowledgeTime()) : "&nbsp"%></td>
        </tr>
        <tr>
          <td colspan="2">&nbsp;</td>
          <td bgcolor="#999999"><b>Service</b></td>
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
          <td colspan="2">&nbsp;</td>
        </tr>
      </table>

      <br>
            
      <table width="100%" border="1" cellspacing="0" cellpadding="2" bgcolor="#cccccc" bordercolor="black">        <tr bgcolor="#999999">
          <td align="left" valign="top">Log Message</td>
        </tr>
        <tr>
          <td align="left" valign="top"><%=event.getLogMessage()%></td>
        </tr>
      </table>

      <br>

      <table width="100%" border="1" cellspacing="0" cellpadding="2" bgcolor="#cccccc" bordercolor="black">        <tr bgcolor="#999999">
          <td align="left" valign="top">Description</td>
        </tr>
        <tr>
          <td align="left" valign="top"><%=event.getDescription()%></td>
        </tr>
      </table>
      
      <br>
      
      <table width="100%" border="1" cellspacing="0" cellpadding="2" bgcolor="#cccccc" bordercolor="black">        <tr bgcolor="#999999">
          <td align="left" valign="top">Operator Instructions</td>
        </tr>
	
        <tr>
          <td align="left" valign="top"> 
	    <%if (event.getOperatorInstruction()==null) { %>
              No instructions available
            <% } else { %>
              <%=event.getOperatorInstruction()%>
            <% } %>
	  </td>
        </tr>
      </table>
      
      <br>

      
      <FORM METHOD="POST" NAME="acknowlegde" ACTION="event/acknowledge">
        <input type="hidden" name="action" value="<%=action%>" />
        <input type="hidden" name="event" value="<%=event.getId()%>"/>
        <input type="hidden" name="redirect" value="<%=request.getContextPath() + request.getServletPath() + "?" + request.getQueryString()%>" />
        <input type="submit" value="<%=buttonName%>"
      </FORM>
      
    </td>
    
    <td>&nbsp;</td>
  </tr>
</table>

<br>

<jsp:include page="/includes/footer.jsp" flush="false" />

</body>
</html>

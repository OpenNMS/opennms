<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.blast.com/
//

-->

<%-- 
  This page is included by other JSPs to create a table containing
  a row for each event passed in.  
  
  This page has one required parameter: node, a node identifier.
  Without this parameter, this page will throw a ServletException.

  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.event.*, org.opennms.web.MissingParameterException" %>

<%
    //required parameter: node
    String nodeIdString = request.getParameter("node");

    if( nodeIdString == null ) {
        throw new MissingParameterException("node");
   }

    int nodeId = Integer.parseInt(nodeIdString);
    
    //optional parameters: ipAddr, service
    String ipAddr = request.getParameter("ipAddr");
    String serviceIdString = request.getParameter("service");
    
    //optional parameter: throttle
   String throttleString = request.getParameter( "throttle" );
    int throttle = 0;  //less than one means no throttle

   if( throttleString != null ) {
        try {
           throttle = Integer.parseInt( throttleString );
        }
        catch( NumberFormatException e ) {}
   }

    //optional parameter: header 
   String header = request.getParameter( "header" );
   if( header == null ) {
        header = "Event List";
   }

   //optional parameter moreUrl, can be null
   String moreUrl = request.getParameter( "moreUrl" );   
    
    Event[] events = new Event[0];
    int eventCount = 0;

    //sort by event id    
    EventFactory.SortStyle sortStyle = EventFactory.SortStyle.ID;
    
    //include only unacknowledged (outstanding) events
    EventFactory.AcknowledgeType ackType = EventFactory.AcknowledgeType.UNACKNOWLEDGED;    
    
    //throttle from the beginning of the result set
    int offset = 0;
    
    if( ipAddr != null ) {
        if( serviceIdString != null ) {
            int serviceId = Integer.parseInt(serviceIdString);
            events = EventFactory.getEventsForService(nodeId, ipAddr, serviceId, sortStyle, ackType, throttle, offset);
            eventCount = EventFactory.getEventCountForService(nodeId, ipAddr, serviceId, ackType);
        }
        else {
            events = EventFactory.getEventsForInterface(nodeId, ipAddr, sortStyle, ackType, throttle, offset);
            eventCount = EventFactory.getEventCountForInterface(nodeId, ipAddr, ackType);            
        }
    }
    else {
        events = EventFactory.getEventsForNode(nodeId, sortStyle, ackType, throttle, offset);            
        eventCount = EventFactory.getEventCountForNode(nodeId, ackType);    
    }
%>

<script language="Javascript" type="text/javascript" >
    
    function submitAck()
    {
        var isChecked = false
        
        if (document.acknowledge_form.event.length)
        {
            for( i = 0; i < document.acknowledge_form.event.length; i++ ) 
            {
              //make sure something is checked before proceeding
              if (document.acknowledge_form.event[i].checked)
              {
                isChecked=true;
              }
            }
            
            if (isChecked)
            {
              document.acknowledge_form.submit();
            }
            else
            {
              alert("Please check the events that you would like to acknowledge.");
            }
        }
        else
        {
            if (document.acknowledge_form.event.checked)
            {
                document.acknowledge_form.submit();
            }
            else
            {
                alert("Please check the events that you would like to acknowledge.");
            }
        }
    }
</script>

<form action="event/acknowledge" method="POST" name="acknowledge_form">
<input type="hidden" name="redirect" value="<%=request.getContextPath() + request.getServletPath() + "?" + request.getQueryString()%>" />
<input type="hidden" name="action" value="<%=org.opennms.web.event.AcknowledgeEventServlet.ACKNOWLEDGE_ACTION%>" />


<table width="100%" border="1" cellspacing="0" cellpadding="2" bordercolor="black" BGCOLOR="#cccccc">
     <tr bgcolor="#999999">
       <td colspan="4"><b><%=header%></b></td>
     </tr>

<%
   for( int i=0; i < events.length; i++ ) {
       int severity = events[i].getSeverity();
%>
     <tr>        
       <td>
         <nobr>
           <input type="checkbox" name="event" value="<%=events[i].getId()%>" />
           <a href="event/detail.jsp?id=<%=events[i].getId()%>"><%=events[i].getId()%></a>
         </nobr>
       </td>
       <td><%=org.opennms.netmgt.EventConstants.formatToUIString(events[i].getTime())%></td>
       <td bgcolor="<%=EventUtil.getSeverityColor(severity)%>"><%=EventUtil.getSeverityLabel(severity)%></td>
       <td><%=events[i].getLogMessage()%></td>
     </tr>
<% } %>

     <tr>
       <td colspan="2">
         <nobr>
           <input type="button" value="Acknowledge" onclick="submitAck()">
           <input TYPE="reset" />
         </nobr>
       </td>

<% if( eventCount > events.length ) { %>
  <% if( moreUrl != null ) { %>     
       <td colspan="2"><a href="<%=moreUrl%>"><%=eventCount-events.length%> more</a></td>
  <% } else { %>
       <td colspan="2"><%=eventCount-events.length%> more</td>
  <% } %>
<% } else { %>
       <td colspan="2"><%=events.length%> events total
<% } %>
     </tr>
      
</table>

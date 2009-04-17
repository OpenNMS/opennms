<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Apr: refactoring to support ACL DAO work
// 2005 Sep 30: Hacked up to use CSS for layout. -- DJ Gregor
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
//      http://www.opennms.com/
//

--%>

<%-- 
  This page is included by other JSPs to create a table containing
  a row for each event passed in.  
  
  This page has one required parameter: node, a node identifier.
  Without this parameter, this page will throw a ServletException.

  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.WebSecurityUtils,
		org.opennms.web.event.*,
		org.opennms.web.springframework.security.Authentication,
		org.opennms.web.MissingParameterException
	"
%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
    //required parameter: node
    String nodeIdString = request.getParameter("node");

    if( nodeIdString == null ) {
        throw new MissingParameterException("node");
   }

    int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
    
    //optional parameters: ipAddr, service
    String ipAddr = request.getParameter("ipAddr");
    String serviceIdString = request.getParameter("service");
    
    //optional parameter: throttle
   String throttleString = request.getParameter( "throttle" );
    int throttle = 0;  //less than one means no throttle

   if( throttleString != null ) {
        try {
           throttle = WebSecurityUtils.safeParseInt( throttleString );
        }
        catch( NumberFormatException e ) {}
   }

   String ifIndexString = request.getParameter("ifIndex");
   int ifIndex = -1;
   if (ifIndexString != null ) {
	   ifIndex= WebSecurityUtils.safeParseInt(ifIndexString);
   }
    //optional parameter: header 
   String header = request.getParameter( "header" );
   if( header == null ) {
        header = "Event List";
   }

   //optional parameter moreUrl, can be null
   String moreUrl = request.getParameter( "moreUrl" );   
    
    Event[] events = new Event[0];

    //sort by event id    
    SortStyle sortStyle = SortStyle.ID;
    
    //include only unacknowledged (outstanding) events
    AcknowledgeType ackType = AcknowledgeType.UNACKNOWLEDGED;    
    
    //throttle from the beginning of the result set
    int offset = 0;
    
    if( ipAddr != null && ! ipAddr.equals("0.0.0.0")) {
        if( serviceIdString != null ) {
            int serviceId = WebSecurityUtils.safeParseInt(serviceIdString);
            events = EventFactory.getEventsForService(nodeId, ipAddr, serviceId, sortStyle, ackType, throttle, offset);
        }
        else {
            events = EventFactory.getEventsForInterface(nodeId, ipAddr, sortStyle, ackType, throttle, offset);
        }
    } else if (ifIndex != -1 ) {
        events = EventFactory.getEventsForInterface(nodeId, ifIndex, sortStyle, ackType, throttle, offset);
    }
    else {
        events = EventFactory.getEventsForNode(nodeId, sortStyle, ackType, throttle, offset);            
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

<div id="include-eventlist">

<% if( !(request.isUserInRole( Authentication.READONLY_ROLE ))) { %>
    <form action="event/acknowledge" method="POST" name="acknowledge_form">
    <input type="hidden" name="redirect" value="<%=request.getContextPath() + request.getServletPath() + "?" + request.getQueryString()%>" />
    <input type="hidden" name="action" value="<%=org.opennms.web.event.AcknowledgeType.ACKNOWLEDGED.getShortName() %>" />
<% } %>

<h3 class="o-box"><%=header%></h3>
<table class="standard o-box">

<%
   for( int i=0; i < events.length; i++ ) {
       Event event = events[i];
       pageContext.setAttribute("event", event);
%>
     <tr class="<%= event.getSeverity().getLabel() %>">
       <% if( !(request.isUserInRole( Authentication.READONLY_ROLE ))) { %>
           <td class="divider">
             <nobr>
               <input type="checkbox" name="event" value="<%=event.getId()%>" />
               <a href="event/detail.jsp?id=<%=event.getId()%>"><%=event.getId()%></a>
             </nobr>
           </td>
       <% } %>
       <td class="divider"><fmt:formatDate value="${event.time}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${event.time}" type="time" pattern="HH:mm:ss"/></td>
       <td class="divider bright"><%= event.getSeverity().getLabel() %></td>
       <td class="divider"><%=event.getLogMessage()%></td>
     </tr>
<% } %>

     <tr>
       <td class="standard" colspan="2">
         <% if( !(request.isUserInRole( Authentication.READONLY_ROLE ))) { %>
           <nobr>
             <input type="button" value="Acknowledge" onclick="submitAck()">
             <input TYPE="reset" />
           </nobr>
         <% } %>
       </td>

  <% if( moreUrl != null ) { %>     
       <td class="standard" colspan="2"><a href="<%=moreUrl%>">More...</a></td>
  <% } %>
     </tr>
      
</table>
</form>

</div>

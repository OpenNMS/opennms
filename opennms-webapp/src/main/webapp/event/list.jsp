<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Aug 14: Sanitize input
// 2005 Sep 30: Hacked up to use CSS for layout. -- DJ Gregor
// 2004 Feb 11: remove the extra 'limit' parameter in the base URL.
// 2003 Sep 04: Added a check to allow for deleted node events to display.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 2002 Nov 10: Removed the "http://" from UEIs and references to bluebird.
// 2002 Nov 09: Removed borders around events.
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

<%@page language="java"	contentType="text/html"	session="true" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>

<%@page import="org.opennms.web.WebSecurityUtils"%>
<%@page import="org.opennms.web.XssRequestWrapper"%>
<%@page import="org.opennms.web.acegisecurity.Authentication"%>

<%@page import="org.opennms.web.admin.notification.noticeWizard.NotificationWizardServlet"%>

<%@page import="org.opennms.web.filter.Filter"%>

<%@page import="org.opennms.web.event.Event"%>
<%@page import="org.opennms.web.event.EventQueryParms"%>
<%@page import="org.opennms.web.event.EventFactory"%>
<%@page import="org.opennms.web.event.AcknowledgeEventServlet"%>
<%@page import="org.opennms.web.event.EventUtil"%>

<%@page import="org.opennms.web.event.filter.SeverityFilter"%>
<%@page import="org.opennms.web.event.filter.NegativeSeverityFilter"%>
<%@page import="org.opennms.web.event.filter.AfterDateFilter"%>
<%@page import="org.opennms.web.event.filter.BeforeDateFilter"%>
<%@page import="org.opennms.web.event.filter.NodeFilter"%>
<%@page import="org.opennms.web.event.filter.NegativeNodeFilter"%>
<%@page import="org.opennms.web.event.filter.InterfaceFilter"%>
<%@page import="org.opennms.web.event.filter.NegativeInterfaceFilter"%>
<%@page import="org.opennms.web.event.filter.ServiceFilter"%>
<%@page import="org.opennms.web.event.filter.NegativeServiceFilter"%>
<%@page import="org.opennms.web.event.filter.AcknowledgedByFilter"%>
<%@page import="org.opennms.web.event.filter.NegativeAcknowledgedByFilter"%>
<%@page import="org.opennms.web.event.filter.ExactUEIFilter"%>
<%@page import="org.opennms.web.event.filter.NegativeExactUEIFilter"%>

<%--
  This page is written to be the display (view) portion of the EventQueryServlet
  at the /event/list URL.  It will not work by itself, as it requires two request
  attributes be set:
  
  1) events: the list of org.opennms.web.element.Event instances to display
  2) parms: an org.opennms.web.event.EventQueryParms object that holds all the 
     parameters used to make this query
--%>

<%
	XssRequestWrapper req = new XssRequestWrapper(request);

    //required attributes
    Event[] events = (Event[])req.getAttribute( "events" );
    EventQueryParms parms = (EventQueryParms)req.getAttribute( "parms" );

    if( events == null || parms == null ) {
	throw new ServletException( "Missing either the events or parms request attribute." );
    }

    String action = null;

    if( parms.ackType == EventFactory.AcknowledgeType.UNACKNOWLEDGED ) {
        action = AcknowledgeEventServlet.ACKNOWLEDGE_ACTION;
    } 
    else if( parms.ackType == EventFactory.AcknowledgeType.ACKNOWLEDGED ) {
        action = AcknowledgeEventServlet.UNACKNOWLEDGE_ACTION;
    }

    int eventCount = EventFactory.getEventCount( parms.ackType, parms.getFilters() );    
    
    //useful constant strings
    String addPositiveFilterString = "[+]";
    String addNegativeFilterString = "[-]";
    String addBeforeDateFilterString = "[&gt;]";
    String addAfterDateFilterString  = "[&lt;]";    
%>



<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Event List" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Events" />
  <jsp:param name="breadcrumb" value="<a href= 'event/index.jsp' title='Events System Page'>Events</a>" />
  <jsp:param name="breadcrumb" value="List" />
</jsp:include>

  <script language="Javascript" type="text/javascript">
    function checkAllCheckboxes() {
       if( document.acknowledge_form.event.length ) {  
         for( i = 0; i < document.acknowledge_form.event.length; i++ ) {
           document.acknowledge_form.event[i].checked = true
         }
       }
       else {
         document.acknowledge_form.event.checked = true
       }
         
    }
    
    function submitForm(anAction)
    {
        var isChecked = false
        var numChecked = 0;
 
        if (document.acknowledge_form.event.length)
        {
            for( i = 0; i < document.acknowledge_form.event.length; i++ ) 
            {
              //make sure something is checked before proceeding
              if (document.acknowledge_form.event[i].checked)
              {
                isChecked=true;
                numChecked+=1;
              }
            }
            
            if (isChecked && document.acknowledge_form.multiple)
            {
              if (numChecked == parseInt(document.acknowledge_form.event.length)) 
              { 
                var newPageNum = parseInt(document.acknowledge_form.multiple.value) - 1;
                var findVal = "multiple=" + document.acknowledge_form.multiple.value;
                var replaceWith = "multiple=" + newPageNum;
                var tmpRedirect = document.acknowledge_form.redirectParms.value;
                document.acknowledge_form.redirectParms.value = tmpRedirect.replace(findVal, replaceWith);
                document.acknowledge_form.submit();
              } 
              else 
              {
                document.acknowledge_form.submit();
              }
            }
            else if (isChecked)
            {
              document.acknowledge_form.submit();
            }
            else
            {
                alert("Please check the events that you would like to " + anAction + ".");
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
                alert("Please check the events that you would like to " + anAction + ".");
            }
        }
    }
    
    function submitNewNotificationForm(uei) {
    	document.getElementById("uei").value=uei;
    	document.add_notification_form.submit();
    }

  </script>


      <!-- menu -->
      <div id="linkbar">
      <ul>
        <li><a href="<%=this.makeLink( parms, new ArrayList<Filter>())%>" title="Remove all search constraints" >View all events</a></li>
        <li><a href="event/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a></li>
        <li><a href="<%=org.opennms.web.Util.calculateUrlBase(req)%>/event/severity.jsp">Severity Legend</a></li>
      
      <% if( !(req.isUserInRole( Authentication.READONLY_ROLE ))) { %>
        <% if( parms.ackType == EventFactory.AcknowledgeType.UNACKNOWLEDGED ) { %> 
        <li><a href="javascript: void document.acknowledge_by_filter_form.submit()" onclick="return confirm('Are you sure you want to acknowledge all events in the current search including those not shown on your screen?  (<%=eventCount%> total events)')" title="Acknowledge all events that match the current search constraints, even those not shown on the screen">Acknowledge entire search</a></li>
        <% } else { %>
        <li><a href="javascript: void document.acknowledge_by_filter_form.submit()" onclick="return confirm('Are you sure you want to unacknowledge all events in the current search including those not shown on your screen)?  (<%=eventCount%> total events)')" title="Unacknowledge all events that match the current search constraints, even those not shown on the screen">Unacknowledge entire search</a></li>
        <% } %>
      <% } %>
      </ul>
      </div>
      <!-- end menu -->

	  <!-- hidden form for adding a new Notification -->
	  <form action="admin/notification/noticeWizard/notificationWizard" method="POST" name="add_notification_form">
	  	<input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_OTHER_WEBUI%>" />
	  	<input type="hidden" name="uei" id="uei" value="" /> <!-- Set by java script -->
	  </form>
	  
      <!-- hidden form for acknowledging the result set --> 
      <form action="event/acknowledgeByFilter" method="POST" name="acknowledge_by_filter_form">    
        <input type="hidden" name="redirectParms" value="<%=req.getQueryString()%>" />
        <input type="hidden" name="action" value="<%=action%>" />
        <%=org.opennms.web.Util.makeHiddenTags(req)%>
      </form>      


      <jsp:include page="/includes/event-querypanel.jsp" flush="false" />
          
            <% if( eventCount > 0 ) { %>
              <% String baseUrl = this.makeLink(parms); %>
              <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
                <jsp:param name="count"    value="<%=eventCount%>" />
                <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                <jsp:param name="limit"    value="<%=parms.limit%>"      />
                <jsp:param name="multiple" value="<%=parms.multiple%>"   />
              </jsp:include>
            <% } %>          

            <% if( parms.filters.size() > 0 || parms.ackType == EventFactory.AcknowledgeType.UNACKNOWLEDGED || parms.ackType == EventFactory.AcknowledgeType.ACKNOWLEDGED ) { %>               <% int length = parms.filters.size(); %>

              <p>Search constraints:
                  <% if( parms.ackType == EventFactory.AcknowledgeType.UNACKNOWLEDGED ) { %>
                    <span class="filter">Event(s) outstanding
					  <a href="<%=this.makeLink(parms, EventFactory.AcknowledgeType.ACKNOWLEDGED)%>" title="Show acknowledged event(s)">[-]</a>
                    </span>
                  <% } else if( parms.ackType == EventFactory.AcknowledgeType.ACKNOWLEDGED ) { %>
                    <span class="filter">Event(s) acknowledged
                      <a href="<%=this.makeLink(parms, EventFactory.AcknowledgeType.UNACKNOWLEDGED)%>" title="Show outstanding event(s)">[-]</a>
                    </span>
                  <% } %>
                  <% for( int i=0; i < length; i++ ) { %>
                    <% Filter filter = (Filter)parms.filters.get(i); %>
                    &nbsp;
                    <span class="filter">
                      <%= WebSecurityUtils.sanitizeString(filter.getTextDescription()) %>
                      <a href="<%=this.makeLink( parms, filter, false)%>" title="Remove filter">[-]</a>
                    </span>
                  <% } %>
              </p>
            <% } %>


    <% if( !(req.isUserInRole( Authentication.READONLY_ROLE ))) { %>
      <form action="event/acknowledge" method="POST" name="acknowledge_form">
        <input type="hidden" name="redirectParms" value="<%=req.getQueryString()%>" />
        <input type="hidden" name="action" value="<%=action%>" />
        <%=org.opennms.web.Util.makeHiddenTags(req)%>
    <% } %>
                <jsp:include page="/includes/key.jsp" flush="false" />

      <table>
        <thead>
        <tr>
          <% if( !(req.isUserInRole( Authentication.READONLY_ROLE ))) { %>
            <% if ( parms.ackType == EventFactory.AcknowledgeType.UNACKNOWLEDGED ) { %>
            <th width="1%">Ack</th>
            <% } else { %>
            <th width="1%">UnAck</th>
            <% } %>
          <% } else { %>
            <th width="1%">&nbsp;</th>
          <% } %>
          <th width="1%"> <%=this.makeSortLink( parms, EventFactory.SortStyle.ID,        EventFactory.SortStyle.REVERSE_ID,        "id",        "ID" )%></th>
          <th width="10%"><%=this.makeSortLink( parms, EventFactory.SortStyle.SEVERITY,  EventFactory.SortStyle.REVERSE_SEVERITY,  "severity",  "Severity"  )%></th>
          <th width="19%"><%=this.makeSortLink( parms, EventFactory.SortStyle.TIME,      EventFactory.SortStyle.REVERSE_TIME,      "time",      "Time"      )%></th>
          <th width="25%"><%=this.makeSortLink( parms, EventFactory.SortStyle.NODE,      EventFactory.SortStyle.REVERSE_NODE,      "node",      "Node"      )%></th>
          <th width="16%"><%=this.makeSortLink( parms, EventFactory.SortStyle.INTERFACE, EventFactory.SortStyle.REVERSE_INTERFACE, "interface", "Interface" )%></th>
          <th width="15%"><%=this.makeSortLink( parms, EventFactory.SortStyle.SERVICE,   EventFactory.SortStyle.REVERSE_SERVICE,   "service",   "Service"   )%></th>
          <th width="10%">Ackd</th>
        </tr>
        </thead>     
      <% for( int i=0; i < events.length; i++ ) {
        Event event = events[i];
      	pageContext.setAttribute("event", event);
      %>
        <tr valign="top" class="<%=EventUtil.getSeverityLabel(events[i].getSeverity())%>">
          <% if( !(req.isUserInRole( Authentication.READONLY_ROLE ))) { %>
          <td valign="top" rowspan="3" class="divider">
                <input type="checkbox" name="event" value="<%=events[i].getId()%>" /> 
            </td>
            <% } else { %>
              <td valign="top" rowspan="3" class="divider">&nbsp;</td>
            <% } %>

          <td valign="top" rowspan="3" class="divider"><a href="event/detail.jsp?id=<%=events[i].getId()%>"><%=events[i].getId()%></a></td>
          
          <td valign="top" rowspan="3" class="divider bright"> 
            <strong><%=EventUtil.getSeverityLabel(events[i].getSeverity())%></strong>
            <% Filter severityFilter = new SeverityFilter(events[i].getSeverity()); %>      
            <% if( !parms.filters.contains( severityFilter )) { %>
              <nobr>
                <a href="<%=this.makeLink( parms, severityFilter, true)%>" class="filterLink" title="Show only events with this severity"><%=addPositiveFilterString%></a>
                <a href="<%=this.makeLink( parms, new NegativeSeverityFilter(events[i].getSeverity()), true)%>" class="filterLink" title="Do not show events with this severity"><%=addNegativeFilterString%></a>
              </nobr>
            <% } %>
          </td>
          <td class="divider">
            <nobr><fmt:formatDate value="${event.time}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${event.time}" type="time" pattern="HH:mm:ss"/></nobr>
            <nobr>
              <a href="<%=this.makeLink( parms, new AfterDateFilter(events[i].getTime()), true)%>"  class="filterLink" title="Only show events occurring after this one"><%=addAfterDateFilterString%></a>            
              <a href="<%=this.makeLink( parms, new BeforeDateFilter(events[i].getTime()), true)%>" class="filterLink" title="Only show events occurring before this one"><%=addBeforeDateFilterString%></a>
            </nobr>
          </td>
          <td class="divider">
	    <% if(events[i].getNodeId() != 0 && events[i].getNodeLabel()!= null ) { %>
              <% Filter nodeFilter = new NodeFilter(events[i].getNodeId()); %>             
              <% String[] labels = this.getNodeLabels( events[i].getNodeLabel() ); %>
              <a href="element/node.jsp?node=<%=events[i].getNodeId()%>" title="<%=labels[1]%>"><%=labels[0]%></a>
                    
              <% if( !parms.filters.contains(nodeFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, nodeFilter, true)%>" class="filterLink" title="Show only events on this node"><%=addPositiveFilterString%></a>
                  <a href="<%=this.makeLink( parms, new NegativeNodeFilter(events[i].getNodeId()), true)%>" class="filterLink" title="Do not show events for this node"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
          <td class="divider">
            <% if(events[i].getIpAddress() != null ) { %>
              <% Filter intfFilter = new InterfaceFilter(events[i].getIpAddress()); %>
              <% if( events[i].getNodeId() != 0 ) { %>
                 <a href="element/interface.jsp?node=<%=events[i].getNodeId()%>&intf=<%=events[i].getIpAddress()%>" title="More info on this interface"><%=events[i].getIpAddress()%></a>
              <% } else { %>
                 <%=events[i].getIpAddress()%>
              <% } %>
              <% if( !parms.filters.contains(intfFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, intfFilter, true)%>" class="filterLink" title="Show only events on this IP address"><%=addPositiveFilterString%></a>
                  <a href="<%=this.makeLink( parms, new NegativeInterfaceFilter(events[i].getIpAddress()), true)%>" class="filterLink" title="Do not show events for this interface"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
          <td class="divider">
            <% if(events[i].getServiceName() != null) { %>
              <% Filter serviceFilter = new ServiceFilter(events[i].getServiceId()); %>
              <% if( events[i].getNodeId() != 0 && events[i].getIpAddress() != null ) { %>
                <a href="element/service.jsp?node=<%=events[i].getNodeId()%>&intf=<%=events[i].getIpAddress()%>&service=<%=events[i].getServiceId()%>" title="More info on this service"><%=events[i].getServiceName()%></a>
              <% } else { %>
                <%=events[i].getServiceName()%>
              <% } %>
              <% if( !parms.filters.contains( serviceFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, serviceFilter, true)%>" class="filterLink" title="Show only events with this service type"><%=addPositiveFilterString%></a>
                  <a href="<%=this.makeLink( parms, new NegativeServiceFilter(events[i].getServiceId()), true)%>" class="filterLink" title="Do not show events for this service"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>                            
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
          <td class="divider">
            <% if (events[i].isAcknowledged()) { %>
              <% Filter acknByFilter = new AcknowledgedByFilter(events[i].getAcknowledgeUser()); %>      
              <%=events[i].getAcknowledgeUser()%>
              <% if( !parms.filters.contains( acknByFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, acknByFilter, true)%>" class="filterLink" title="Show only events with this acknowledged by user"><%=addPositiveFilterString%></a>
                  <a href="<%=this.makeLink( parms, new NegativeAcknowledgedByFilter(events[i].getAcknowledgeUser()), true)%>" class="filterLink" title="Do not show events acknowledgd by this user"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>              
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
        </tr>
        
        <tr valign="top" class="<%=EventUtil.getSeverityLabel(events[i].getSeverity())%>">
          <td colspan="4">
            <% if(events[i].getUei() != null) { %>
              <% Filter exactUEIFilter = new ExactUEIFilter(events[i].getUei()); %>
                <%=events[i].getUei()%>
              <% if( !parms.filters.contains( exactUEIFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, exactUEIFilter, true)%>" class="filterLink" title="Show only events with this UEI"><%=addPositiveFilterString%></a>
                  <a href="<%=this.makeLink( parms, new NegativeExactUEIFilter(events[i].getUei()), true)%>" class="filterLink" title="Do not show events for this UEI"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>
              <% if (req.isUserInRole(Authentication.ADMIN_ROLE)) { %>
               	  <a href="javascript: void submitNewNotificationForm('<%=events[i].getUei()%>');" title="Edit notifications for this Event UEI">Edit notifications for event</a>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
          <td valign="top">
            <% if (events[i].isAcknowledged()) { %>
	      <fmt:formatDate value="${event.acknowledgeTime}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${event.acknowledgeTime}" type="time" pattern="HH:mm:ss"/>
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
        </tr>
       
        <tr valign="top" class="<%=EventUtil.getSeverityLabel(events[i].getSeverity())%>">
          <td colspan="5"><%=events[i].getLogMessage()%></td>
        </tr>
       
      <% } /*end for*/%>
      </table>
        
        <p><%=events.length%> events
          <% if( !(req.isUserInRole( Authentication.READONLY_ROLE ))) { %>
            <% if( parms.ackType == EventFactory.AcknowledgeType.UNACKNOWLEDGED ) { %>
              <input type="button" value="Acknowledge Events" onClick="submitForm('acknowledge')"/>
              <input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
              <input TYPE="reset" />
            <% } else if( parms.ackType == EventFactory.AcknowledgeType.ACKNOWLEDGED ) { %>
              <input type="button" value="Unacknowledge Events" onClick="submitForm('unacknowledge')"/>
              <input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
              <input TYPE="reset" />
            <% } %>
          <% } %>
        </p>
      </form>

      <%--<br>
      <% if(req.isUserInRole(Authentication.ADMIN_ROLE)) { %>
        <a HREF="admin/events.jsp" title="Acknowledge or Unacknowledge All Events">[Acknowledge or Unacknowledge All Events]</a>
      <% } %>--%>


<jsp:include page="/includes/bookmark.jsp" flush="false" />

<jsp:include page="/includes/footer.jsp" flush="false" />


<%!
    String urlBase = "event/list";

    protected String makeSortLink( EventQueryParms parms, EventFactory.SortStyle style, EventFactory.SortStyle revStyle, String sortString, String title ) {
      StringBuffer buffer = new StringBuffer();

      buffer.append( "<nobr>" );
      
      if( parms.sortStyle == style ) {
          buffer.append( "<img src=\"images/arrowdown.gif\" hspace=\"0\" vspace=\"0\" border=\"0\" alt=\"" );
          buffer.append( title );
          buffer.append( " Ascending Sort\"/>" );
          buffer.append( "&nbsp;<a href=\"" );
          buffer.append( this.makeLink( parms, revStyle ));
          buffer.append( "\" title=\"Reverse the sort\">" );
      } else if( parms.sortStyle == revStyle ) {
          buffer.append( "<img src=\"images/arrowup.gif\" hspace=\"0\" vspace=\"0\" border=\"0\" alt=\"" );
          buffer.append( title );
          buffer.append( " Descending Sort\"/>" );
          buffer.append( "&nbsp;<a href=\"" );
          buffer.append( this.makeLink( parms, style )); 
          buffer.append( "\" title=\"Reverse the sort\">" );
      } else {
          buffer.append( "<a href=\"" );
          buffer.append( this.makeLink( parms, style ));
          buffer.append( "\" title=\"Sort by " );
          buffer.append( sortString );
          buffer.append( "\">" );   
      }

      buffer.append( title );
      buffer.append( "</a>" );

      return( buffer.toString() );
    }

    
    public String getFiltersAsString(List<Filter> filters ) {
        StringBuffer buffer = new StringBuffer();
    
        if( filters != null ) {
            for( int i=0; i < filters.size(); i++ ) {
                buffer.append( "&filter=" );
                String filterString = EventUtil.getFilterString((Filter)filters.get(i));
                buffer.append( java.net.URLEncoder.encode(filterString) );
            }
        }      
    
        return( buffer.toString() );
    }

    public String makeLink( EventFactory.SortStyle sortStyle, EventFactory.AcknowledgeType ackType, List<Filter> filters, int limit ) {
      StringBuffer buffer = new StringBuffer( this.urlBase );
      buffer.append( "?sortby=" );
      buffer.append( EventUtil.getSortStyleString(sortStyle) );
      buffer.append( "&acktype=" );
      buffer.append( EventUtil.getAcknowledgeTypeString(ackType) );
      if (limit > 0) {
          buffer.append( "&limit=" ).append(limit);
      }
      buffer.append( this.getFiltersAsString(filters) );

      return( buffer.toString() );
    }


    public String makeLink( EventQueryParms parms ) {
      return( this.makeLink( parms.sortStyle, parms.ackType, parms.filters, parms.limit) );
    }


    public String makeLink( EventQueryParms parms, EventFactory.SortStyle sortStyle ) {
      return( this.makeLink( sortStyle, parms.ackType, parms.filters, parms.limit) );
    }


    public String makeLink( EventQueryParms parms, EventFactory.AcknowledgeType ackType ) {
      return( this.makeLink( parms.sortStyle, ackType, parms.filters, parms.limit) );
    }


    public String makeLink( EventQueryParms parms, List<Filter> filters ) {
      return( this.makeLink( parms.sortStyle, parms.ackType, filters, parms.limit) );
    }


    public String makeLink( EventQueryParms parms, Filter filter, boolean add ) {
      List<Filter> newList = new ArrayList<Filter>( parms.filters );
      if( add ) {
        newList.add( filter );
      }
      else {
        newList.remove( filter );
      }

      return( this.makeLink( parms.sortStyle, parms.ackType, newList, parms.limit ));
    }


    public String[] getNodeLabels( String nodeLabel ) {
        String[] labels = null;

        if( nodeLabel.length() > 32 ) {
            String shortLabel = nodeLabel.substring( 0, 31 ) + "...";                        
            labels = new String[] { shortLabel, nodeLabel };
        }
        else {
            labels = new String[] { nodeLabel, nodeLabel };
        }

        return( labels );
    }

%>



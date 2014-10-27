<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>

<%@page import="org.opennms.core.utils.WebSecurityUtils" %>
<%@page import="org.opennms.web.servlet.XssRequestWrapper" %>
<%@page import="org.opennms.web.api.Authentication" %>
<%@page import="org.opennms.web.api.Util" %>

<%@page import="org.opennms.web.filter.Filter"%>

<%@page import="org.opennms.web.event.AcknowledgeType" %>
<%@page import="org.opennms.web.event.EventUtil"%>
<%@page import="org.opennms.web.event.Event"%>
<%@page import="org.opennms.web.event.EventQueryParms"%>

<%@page import="org.opennms.web.event.filter.ExactUEIFilter"%>
<%@page import="org.opennms.web.event.filter.NegativeExactUEIFilter"%>
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

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

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
    Integer eventCount = (Integer)req.getAttribute( "eventCount" );
    EventQueryParms parms = (EventQueryParms)req.getAttribute( "parms" );

    if( events == null || parms == null ) {
        throw new ServletException( "Missing either the events or parms request attribute." );
    }
    if (eventCount == null) {
	    eventCount = Integer.valueOf(-1);
	}

    String action = null;

    if ( parms.ackType != null ) {
    	action = parms.ackType.getShortName();
    }

    //useful constant strings
    String addPositiveFilterString = "<i class=\"fa fa-plus-square-o\"></i>";
    String addNegativeFilterString = "<i class=\"fa fa-minus-square-o\"></i>";
    String addBeforeDateFilterString = "<i class=\"fa fa-toggle-right\"></i>";
    String addAfterDateFilterString  = "<i class=\"fa fa-toggle-left\"></i>";
%>

<%@page import="org.opennms.web.event.SortStyle"%><jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Event List" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Events" />
  <jsp:param name="breadcrumb" value="<a href= 'event/index' title='Events System Page'>Events</a>" />
  <jsp:param name="breadcrumb" value="List" />
</jsp:include>

<link rel="stylesheet" href="css/font-awesome-4.0.3/css/font-awesome.min.css">

  <script type="text/javascript">
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

  </script>




      <!-- menu -->
      <div id="linkbar">
      <ul>
        <li><a href="<c:out value="<%=this.makeLink( parms, new ArrayList<Filter>())%>"/>" title="Remove all search constraints" >View all events</a></li>
        <li><a href="event/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a></li>
        <li><a onclick="javascript:window.open('<%=Util.calculateUrlBase(req, "event/severity.jsp")%>','event_severity_legend', 'fullscreen=no,toolbar=no,status=no,menubar=no,scrollbars=no,resizable=yes,directories=no,location=no,width=525,height=350');" title="Open a window explaining the event severities">Severity Legend</a></li>
      
      <% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
        <% if( parms.ackType == AcknowledgeType.UNACKNOWLEDGED ) { %> 
          <% if ( eventCount == -1 ) { %>
            <li><a href="javascript: void document.acknowledge_by_filter_form.submit()" onclick="return confirm('Are you sure you want to acknowledge all events in the current search including those not shown on your screen?')" title="Acknowledge all events that match the current search constraints, even those not shown on the screen">Acknowledge entire search</a></li>
          <% } else { %>
            <li><a href="javascript: void document.acknowledge_by_filter_form.submit()" onclick="return confirm('Are you sure you want to acknowledge all events in the current search including those not shown on your screen?  (<%=eventCount%> total events)')" title="Acknowledge all events that match the current search constraints, even those not shown on the screen">Acknowledge entire search</a></li>
          <% } %>
        <% } else { %>
          <% if ( eventCount == -1 ) { %>
            <li><a href="javascript: void document.acknowledge_by_filter_form.submit()" onclick="return confirm('Are you sure you want to unacknowledge all events in the current search including those not shown on your screen)?')" title="Unacknowledge all events that match the current search constraints, even those not shown on the screen">Unacknowledge entire search</a></li>
          <% } else { %>
            <li><a href="javascript: void document.acknowledge_by_filter_form.submit()" onclick="return confirm('Are you sure you want to unacknowledge all events in the current search including those not shown on your screen)?  (<%=eventCount%> total events)')" title="Unacknowledge all events that match the current search constraints, even those not shown on the screen">Unacknowledge entire search</a></li>
          <% } %>
        <% } %>
      <% } %>
      </ul>
      </div>
      <!-- end menu -->


      <!-- hidden form for acknowledging the result set --> 
      <form action="event/acknowledgeByFilter" method="post" name="acknowledge_by_filter_form">    
        <input type="hidden" name="redirectParms" value="<c:out value="<%=req.getQueryString()%>"/>" />
        <input type="hidden" name="action" value="<%=action%>" />
        <%=org.opennms.web.api.Util.makeHiddenTags(req)%>
      </form>      

      <jsp:include page="/includes/event-querypanel.jsp" flush="false" />

            <% if( events.length > 0 ) { %>
              <% String baseUrl = this.makeLink(parms); %>
              <% if ( eventCount == -1 ) { %>
                <jsp:include page="/includes/resultsIndexNoCount.jsp" flush="false" >
                  <jsp:param name="itemCount"    value="<%=events.length%>" />
                  <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                  <jsp:param name="limit"    value="<%=parms.limit%>"      />
                  <jsp:param name="multiple" value="<%=parms.multiple%>"   />
                </jsp:include>
              <% } else { %>
                <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
                  <jsp:param name="count"    value="<%=eventCount%>" />
                  <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                  <jsp:param name="limit"    value="<%=parms.limit%>"      />
                  <jsp:param name="multiple" value="<%=parms.multiple%>"   />
                </jsp:include>
              <% } %>
            <% } %>          

            <% if( parms.filters.size() > 0 || parms.ackType == AcknowledgeType.UNACKNOWLEDGED || parms.ackType == AcknowledgeType.ACKNOWLEDGED ) { %>
              <% int length = parms.filters.size(); %>

              <p>Search constraints: 
                  <% if( parms.ackType == AcknowledgeType.UNACKNOWLEDGED ) { %>
                  <span class="filter">Event(s) outstanding <a href="<c:out value="<%=this.makeLink(parms, AcknowledgeType.ACKNOWLEDGED)%>"/>" title="Show acknowledged event(s)">[-]</a></span>
                  <% } else if( parms.ackType == AcknowledgeType.ACKNOWLEDGED ) { %>
                  <span class="filter">Event(s) acknowledged <a href="<c:out value="<%=this.makeLink(parms, AcknowledgeType.UNACKNOWLEDGED)%>"/>" title="Show outstanding event(s)">[-]</a></span>
                  <% } %>
									<% for( int i=0; i < length; i++ ) { %>
									  <% Filter filter = (Filter)parms.filters.get(i); %>
									    &nbsp; <span class="filter"><%=WebSecurityUtils.sanitizeString(filter.getTextDescription())%> <a href="<c:out value="<%=this.makeLink( parms, filter, false)%>"/>" title="Remove filter">[-]</a></span>
									<% } %>
              </p>           
            <% } %>

    <% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
      <form action="event/acknowledge" method="post" name="acknowledge_form">
        <input type="hidden" name="redirectParms" value="<c:out value="<%=req.getQueryString()%>"/>"/>
        <input type="hidden" name="action" value="<%=action%>" />
        <%=org.opennms.web.api.Util.makeHiddenTags(req)%>
    <% } %>
		<jsp:include page="/includes/key.jsp" flush="false" />
      <table>
        <thead>
        <tr>
					<th></th>
          <th><%=this.makeSortLink( parms, SortStyle.ID,        SortStyle.REVERSE_ID,        "id",        "ID" )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.SEVERITY,  SortStyle.REVERSE_SEVERITY,  "severity",  "Severity"  )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.TIME,      SortStyle.REVERSE_TIME,      "time",      "Time"      )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.NODE,      SortStyle.REVERSE_NODE,      "node",      "Node"      )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.INTERFACE, SortStyle.REVERSE_INTERFACE, "interface", "Interface" )%></th>
          <th><%=this.makeSortLink( parms, SortStyle.SERVICE,   SortStyle.REVERSE_SERVICE,   "service",   "Service"   )%></th>
          <th>Ackd</th>
        </tr>
        </thead>     
      <% for( int i=0; i < events.length; i++ ) {
        Event event = events[i];
        pageContext.setAttribute("event", event);
      %>
        <tr class="<%= events[i].getSeverity().getLabel() %>">
          <% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
          <td><input type="checkbox" name="event" value="<%=events[i].getId()%>" /></td>
            <% } %>
          <td class="noWrap"><a href="event/detail.jsp?id=<%=events[i].getId()%>"><%=events[i].getId()%></a>
						<% if(events[i].getUei() != null) { %>
	            <% Filter exactUEIFilter = new ExactUEIFilter(events[i].getUei()); %>
	              <abbr title="<%=events[i].getUei()%>">UEI</abbr>
	            <% if( !parms.filters.contains( exactUEIFilter )) { %>
	                <a href="<c:out value="<%=this.makeLink( parms, exactUEIFilter, true)%>"/>" class="filterLink" title="Show only events with this UEI"><%=addPositiveFilterString%></a>
	                <a href="<c:out value="<%=this.makeLink( parms, new NegativeExactUEIFilter(events[i].getUei()), true)%>"/>" class="filterLink" title="Do not show events for this UEI"><%=addNegativeFilterString%></a>
	            <% } %>                            
	          <% } %>
						</td>

          <td class="bright noWrap"> 
            <%= events[i].getSeverity().getLabel() %>
            <% Filter severityFilter = new SeverityFilter(events[i].getSeverity()); %>      
            <% if( !parms.filters.contains( severityFilter )) { %>
                <a href="<c:out value="<%=this.makeLink( parms, severityFilter, true)%>"/>" class="filterLink" title="Show only events with this severity"><%=addPositiveFilterString%></a>
                <a href="<c:out value="<%=this.makeLink( parms, new NegativeSeverityFilter(events[i].getSeverity()), true)%>"/>" class="filterLink" title="Do not show events with this severity"><%=addNegativeFilterString%></a>
            <% } %>
          </td>
          <td class="noWrap">
	              <a href="<c:out value="<%=this.makeLink( parms, new AfterDateFilter(events[i].getTime()), true)%>"/>"  class="filterLink" title="Only show events occurring after this one"><%=addAfterDateFilterString%></a> 
								<fmt:formatDate value="${event.time}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${event.time}" type="time" pattern="HH:mm:ss"/>           
              <a href="<c:out value="<%=this.makeLink( parms, new BeforeDateFilter(events[i].getTime()), true)%>"/>" class="filterLink" title="Only show events occurring before this one"><%=addBeforeDateFilterString%></a>
          </td>
          <td class="noWrap">
	    <% if(events[i].getNodeId() != 0 && events[i].getNodeLabel()!= null ) { %>
              <% Filter nodeFilter = new NodeFilter(events[i].getNodeId(), getServletContext()); %>             
              <% String[] labels = this.getNodeLabels( events[i].getNodeLabel() ); %>
              <a href="element/node.jsp?node=<%=events[i].getNodeId()%>" title="<%=labels[1]%>"><c:out value="<%=labels[0]%>"/></a>
              <% if( !parms.filters.contains(nodeFilter) ) { %>
                  <a href="<c:out value="<%=this.makeLink( parms, nodeFilter, true)%>"/>" class="filterLink" title="Show only events on this node"><%=addPositiveFilterString%></a>
                  <a href="<c:out value="<%=this.makeLink( parms, new NegativeNodeFilter(events[i].getNodeId(), getServletContext()), true)%>"/>" class="filterLink" title="Do not show events for this node"><%=addNegativeFilterString%></a>
              <% } %>
            <% } %>
          </td>
          <td class="noWrap">
            <% if(events[i].getIpAddress() != null ) { %>
              <% Filter intfFilter = new InterfaceFilter(events[i].getIpAddress()); %>
              <% if( events[i].getNodeId() != 0 ) { %>
                 <c:url var="interfaceLink" value="element/interface.jsp">
                     <c:param name="node" value="<%=String.valueOf(events[i].getNodeId())%>"/>
                     <c:param name="intf" value="<%=events[i].getIpAddress()%>"/>
                 </c:url>
                 <a href="${interfaceLink}" title="More info on this interface"><%=events[i].getIpAddress()%></a>
              <% } else { %>
                 <%=events[i].getIpAddress()%>
              <% } %>
              <% if( !parms.filters.contains(intfFilter) ) { %>
                  <a href="<c:out value="<%=this.makeLink( parms, intfFilter, true)%>"/>" class="filterLink" title="Show only events on this IP address"><%=addPositiveFilterString%></a>
                  <a href="<c:out value="<%=this.makeLink( parms, new NegativeInterfaceFilter(events[i].getIpAddress()), true)%>"/>" class="filterLink" title="Do not show events for this interface"><%=addNegativeFilterString%></a>
              <% } %>
            <% } %>
          </td>
          <td class="noWrap">
            <% if(events[i].getServiceName() != null) { %>
              <% Filter serviceFilter = new ServiceFilter(events[i].getServiceId(), getServletContext()); %>
              <% if( events[i].getNodeId() != 0 && events[i].getIpAddress() != null ) { %>
                <c:url var="serviceLink" value="element/service.jsp">
                  <c:param name="node" value="<%=String.valueOf(events[i].getNodeId())%>"/>
                  <c:param name="intf" value="<%=events[i].getIpAddress()%>"/>
                  <c:param name="service" value="<%=String.valueOf(events[i].getServiceId())%>"/>
                </c:url>
                <a href="${serviceLink}" title="More info on this service"><c:out value="<%=events[i].getServiceName()%>"/></a>
              <% } else { %>
                <c:out value="<%=events[i].getServiceName()%>"/>
              <% } %>
              <% if( !parms.filters.contains( serviceFilter )) { %>
                  <a href="<c:out value="<%=this.makeLink( parms, serviceFilter, true)%>"/>" class="filterLink" title="Show only events with this service type"><%=addPositiveFilterString%></a>
                  <a href="<c:out value="<%=this.makeLink( parms, new NegativeServiceFilter(events[i].getServiceId(), getServletContext()), true)%>"/>" class="filterLink" title="Do not show events for this service"><%=addNegativeFilterString%></a>
              <% } %>                            
            <% } %>
          </td>         
          <td><div class="clip">
            <% if (events[i].isAcknowledged()) { %>
              <% Filter acknByFilter = new AcknowledgedByFilter(events[i].getAcknowledgeUser()); %>      
              <%=events[i].getAcknowledgeUser()%>
              <% if( !parms.filters.contains( acknByFilter )) { %>
                <nobr>
                  <a href="<c:out value="<%=this.makeLink( parms, acknByFilter, true)%>"/>" class="filterLink" title="Show only events with this acknowledged by user"><%=addPositiveFilterString%></a>
                  <a href="<c:out value="<%=this.makeLink( parms, new NegativeAcknowledgedByFilter(events[i].getAcknowledgeUser()), true)%>"/>" class="filterLink" title="Do not show events acknowledgd by this user"><%=addNegativeFilterString%></a>
                </nobr>
              <% } %>              
            <% } %>
						<% if (events[i].isAcknowledged()) { %>
						  <fmt:formatDate value="${event.acknowledgeTime}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${event.acknowledgeTime}" type="time" pattern="HH:mm:ss"/>
						<% } else { %>
						  &nbsp;
						<% } %>
						</div>
          </td>
        </tr>
       
      <% } /*end for*/%>
      </table>
        <hr />
        <p style="margin-top:10px;"><% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
            <% if( parms.ackType == AcknowledgeType.UNACKNOWLEDGED ) { %>
							<input TYPE="reset" />
							<input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
              <input type="button" value="Acknowledge Events" onClick="submitForm('acknowledge')"/>
            <% } else if( parms.ackType == AcknowledgeType.ACKNOWLEDGED ) { %>
              <input TYPE="reset" />
							<input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
							<input type="button" value="Unacknowledge Events" onClick="submitForm('unacknowledge')"/>
            <% } %>
          <% } %>
        </p>
      </form>

            <% if( events.length > 0 ) { %>
              <% String baseUrl = this.makeLink(parms); %>
              <% if ( eventCount == -1 ) { %>
                <jsp:include page="/includes/resultsIndexNoCount.jsp" flush="false" >
                  <jsp:param name="itemCount"    value="<%=events.length%>" />
                  <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                  <jsp:param name="limit"    value="<%=parms.limit%>"      />
                  <jsp:param name="multiple" value="<%=parms.multiple%>"   />
                </jsp:include>
              <% } else { %>
                <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
                  <jsp:param name="count"    value="<%=eventCount%>" />
                  <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                  <jsp:param name="limit"    value="<%=parms.limit%>"      />
                  <jsp:param name="multiple" value="<%=parms.multiple%>"   />
                </jsp:include>
              <% } %>
            <% } %>          

      <%--<br/>
      <% if(req.isUserInRole(Authentication.ROLE_ADMIN)) { %>
        <a HREF="admin/events.jsp" title="Acknowledge or Unacknowledge All Events">[Acknowledge or Unacknowledge All Events]</a>
      <% } %>--%>

<jsp:include page="/includes/footer.jsp" flush="false" />


<%!
    String urlBase = "event/list";

    protected String makeSortLink( EventQueryParms parms, SortStyle style, SortStyle revStyle, String sortString, String title ) {
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
                String filterString = EventUtil.getFilterString(filters.get(i));
                buffer.append( java.net.URLEncoder.encode(filterString) );
            }
        }      
    
        return( buffer.toString() );
    }

    public String makeLink( SortStyle sortStyle, AcknowledgeType ackType, List<Filter> filters, int limit ) {
      StringBuffer buffer = new StringBuffer( this.urlBase );
      buffer.append( "?sortby=" );
      buffer.append( sortStyle.getShortName() );
      buffer.append( "&acktype=" );
      buffer.append( ackType.getShortName() );
      if (limit > 0) {
          buffer.append( "&limit=" ).append(limit);
      }
      buffer.append( this.getFiltersAsString(filters) );

      return( buffer.toString() );
    }


    public String makeLink( EventQueryParms parms ) {
      return( this.makeLink( parms.sortStyle, parms.ackType, parms.filters, parms.limit) );
    }


    public String makeLink( EventQueryParms parms, SortStyle sortStyle ) {
      return( this.makeLink( sortStyle, parms.ackType, parms.filters, parms.limit) );
    }


    public String makeLink( EventQueryParms parms, AcknowledgeType ackType ) {
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

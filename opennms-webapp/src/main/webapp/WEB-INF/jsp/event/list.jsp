<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

<%@page import="org.opennms.netmgt.model.OnmsFilterFavorite"%>
<%@page import="org.opennms.web.admin.notification.noticeWizard.NotificationWizardServlet"%>
<%@page import="org.opennms.web.api.Authentication"%>
<%@page import="org.opennms.web.event.AcknowledgeType"%>
<%@page import="org.opennms.web.event.Event"%>
<%@page import="org.opennms.web.event.SortStyle"%>
<%@page import="org.opennms.web.event.filter.*"%>
<%@page import="org.opennms.web.filter.Filter"%>
<%@page import="org.opennms.web.filter.NormalizedQueryParameters"%>
<%@page import="org.opennms.web.servlet.XssRequestWrapper"%>
<%@page import="org.opennms.web.tags.filters.EventFilterCallback"%>
<%@page import="org.opennms.web.tags.filters.FilterCallback"%>
<%@page import="org.opennms.web.tags.select.FilterFavoriteSelectTagHandler"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="org.opennms.web.api.Util" %>
<%@page import="org.opennms.web.tags.FavoriteTag" %>
<%@page import="org.opennms.core.utils.WebSecurityUtils"%>
<%@ page import="com.google.common.base.Strings" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="../../taglib.tld" prefix="onms" %>

<%--
  This page is written to be the display (view) portion of the EventFilterController
  at the /event/list.htm URL.  It will not work by itself, as it requires two request
  attributes be set:
  
  1) events: the list of org.opennms.web.element.Event instances to display
  2) parms: an org.opennms.web.event.EventQueryParms object that holds all the 
     parameters used to make this query
--%>

<%
	XssRequestWrapper req = new XssRequestWrapper(request);

    //required attributes
    Event[] events = (Event[])req.getAttribute( "events" );
    int eventCount = req.getAttribute( "eventCount" ) == null ? -1 : (Integer)req.getAttribute( "eventCount" );
    NormalizedQueryParameters parms = (NormalizedQueryParameters)req.getAttribute( "parms" );
    FilterCallback callback = (EventFilterCallback) req.getAttribute("callback");

    if( events == null || parms == null ) {
        throw new ServletException( "Missing either the events or parms request attribute." );
    }

    // optional bookmark
    final OnmsFilterFavorite favorite = (OnmsFilterFavorite) req.getAttribute("favorite");


    // Make 'action' the opposite of the current acknowledgement state
    String action = AcknowledgeType.ACKNOWLEDGED.getShortName();
    if (parms.getAckType() != null && parms.getAckType().equals(AcknowledgeType.ACKNOWLEDGED.toNormalizedAcknowledgeType())) {
    	action = AcknowledgeType.UNACKNOWLEDGED.getShortName();
    }

    pageContext.setAttribute("addPositiveFilter", "<i class=\"fa fa-plus-square-o\"></i>");
    pageContext.setAttribute("addNegativeFilter", "<i class=\"fa fa-minus-square-o\"></i>");
    pageContext.setAttribute("addBeforeFilter", "<i class=\"fa fa-toggle-right\"></i>");
    pageContext.setAttribute("addAfterFilter", "<i class=\"fa fa-toggle-left\"></i>");
    pageContext.setAttribute("filterFavoriteSelectTagHandler", new FilterFavoriteSelectTagHandler("All Events"));
%>



<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Event List" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Events" />
  <jsp:param name="breadcrumb" value="<a href= 'event/index' title='Events System Page'>Events</a>" />
  <jsp:param name="breadcrumb" value="List" />
</jsp:include>

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

    function submitNewNotificationForm(uei) {
    	document.getElementById("uei").value=uei;
    	document.add_notification_form.submit();
    }

    function changeFavorite(selectElement) {
        var selectedOption = selectElement.options[selectElement.selectedIndex];
        var favoriteId = selectedOption.value.split(';')[0];
        var filter = selectedOption.value.split(';')[1];
        changeFavorite(favoriteId, filter);
    }

    function changeFavorite(favoriteId, filter) {
        window.location.href = "<%=req.getContextPath()%>/event/list?favoriteId=" + favoriteId + '&' + filter;
    }
  </script>

	  <!-- hidden form for adding a new Notification -->
	  <form action="admin/notification/noticeWizard/notificationWizard" method="post" name="add_notification_form">
	  	<input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_OTHER_WEBUI%>" />
	  	<input type="hidden" name="uei" id="uei" value="" /> <!-- Set by java script -->
	  </form>

<div id="advancedSearchModal" class="modal fade" tabindex="-1">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-body">
        <jsp:include page="/includes/event-advquerypanel.jsp" flush="false" />
      </div>
    </div>
  </div>
</div>

<div id="severityLegendModal" class="modal fade" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
        <jsp:include page="/event/severity.jsp" flush="false" />
    </div>
  </div>
</div>


<div class="row">
<div class="col-md-12">
  <!-- start menu -->
  <a class="btn btn-default" href="<%=this.makeLink(callback, parms, new ArrayList<Filter>(), favorite)%>">View all events</a>
  <button type="button" class="btn btn-default" onClick="$('#advancedSearchModal').modal()">Search</button>
  <button type="button" class="btn btn-default" onClick="$('#severityLegendModal').modal()">Severity Legend</button>
        <% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
          <% if ( eventCount > 0 ) { %>
              <!-- hidden form for acknowledging the result set -->
              <form style="display:inline" action="event/acknowledgeByFilter" method="post" name="acknowledge_by_filter_form">
                <input type="hidden" name="redirectParms" value="<c:out value="<%=req.getQueryString()%>"/>" />
                <input type="hidden" name="actionCode" value="<%=action%>" />
                <%=org.opennms.web.api.Util.makeHiddenTags(req)%>
              </form>

              <% if( AcknowledgeType.UNACKNOWLEDGED.toNormalizedAcknowledgeType().equals(parms.getAckType()) ) { %>
                <button type="button" class="btn btn-default" onclick="if (confirm('Are you sure you want to acknowledge all events in the current search including those not shown on your screen?  (<%=eventCount%> total events)')) {  document.acknowledge_by_filter_form.submit(); }" title="Acknowledge all events that match the current search constraints, even those not shown on the screen">Acknowledge entire search</button>
              <% } else { %>
                <button type="button" class="btn btn-default" onclick="if (confirm('Are you sure you want to unacknowledge all events in the current search including those not shown on your screen)?  (<%=eventCount%> total events)')) { document.acknowledge_by_filter_form.submit(); }" title="Unacknowledge all events that match the current search constraints, even those not shown on the screen">Unacknowledge entire search</button>
              <% } %>
          <% } %>
        <% } %>
      <!-- end menu -->
</div>
<div class="text-right hidden">
  <jsp:include page="/includes/event-querypanel.jsp" flush="false" />
</div>
</div>

<%-- This tag writes out the createFavorite(), deleteFavorite(), and clearFilters() methods --%>
<onms:favorite
  favorite="${favorite}"
  parameters="${parms}"
  callback="${callback}"
  context="/event/list"
  createFavoriteController="/event/createFavorite"
  deleteFavoriteController="/event/deleteFavorite"
/>

<div class="row">
<br/>
</div>

<div class="row">
  <div class="col-sm-6 col-md-3">
  <div class="input-group">
    <span class="input-group-addon">
      <c:choose>
      <c:when test="${favorite == null}">
      <a onclick="createFavorite()">
        <!-- Star outline -->
        <i class="fa fa-lg fa-star-o"></i>
      </a>
      </c:when>
      <c:otherwise>
      <a onclick="deleteFavorite(${favorite.id})">
        <i class="fa fa-lg fa-star"></i>
      </a>
      </c:otherwise>
      </c:choose>
    </span>
    <!-- Use background-color:white to make it look less disabled -->
    <input type="text" class="form-control" style="background-color:white;" readonly placeholder="Unsaved filter" value="<c:out value="${favorite.name}"/>"/>
    <div class="input-group-btn">
      <div class="dropdown">
        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
          <span class="caret"></span>
        </button>
        <!-- I put margin: 0px here because the margin gap was causing the menu to disappear before you could get the mouse on it -->
        <ul class="dropdown-menu dropdown-menu-right" style="margin: 0px;" role="menu">
          <c:forEach var="fave" items="${favorites}">
            <c:if test="${favorite.id != fave.id}">
              <li>
                <a onclick="changeFavorite(${fave.id}, '${fave.filter}')">
                  <c:out value="${fave.name}"/>
                </a>
              </li>
              <c:set var="showDivider" value="${true}"/>
            </c:if>
          </c:forEach>
          <c:if test="${showDivider}"><li class="divider"/></c:if>
          <li><a onclick="clearFilters()">Clear filters</a></li>
        </ul>
      </div>
    </div>
  </div>
  </div>

            <% if( parms.getFilters().size() > 0 || AcknowledgeType.UNACKNOWLEDGED.toNormalizedAcknowledgeType().equals(parms.getAckType()) || AcknowledgeType.ACKNOWLEDGED.toNormalizedAcknowledgeType().equals(parms.getAckType()) ) { %>
              <div class="col-sm-6 col-md-9">
                    <onms:filters
                            context="/event/list"
                            favorite="${favorite}"
                            parameters="${parms}"
                            showRemoveLink="true"
                            showAcknowledgeFilter="true"
                            acknowledgeFilterPrefix="Event(s)"
                            acknowledgeFilterSuffix="event(s)"
                            callback="${callback}" />
              </div>
            <% } %>
</div>

<div class="row">
<br/>
</div>

            <onms:alert/>

            <% if( events.length > 0 ) { %>
              <% String baseUrl = this.makeLink(callback, parms, favorite); %>
              <% if ( eventCount == -1 ) { %>
                <jsp:include page="/includes/resultsIndexNoCount.jsp" flush="false" >
                  <jsp:param name="itemCount"    value="<%=events.length%>" />
                  <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                  <jsp:param name="limit"    value="<%=parms.getLimit()%>"      />
                  <jsp:param name="multiple" value="<%=parms.getMultiple()%>"   />
                </jsp:include>
              <% } else { %>
                <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
                  <jsp:param name="count"    value="<%=eventCount%>" />
                  <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                  <jsp:param name="limit"    value="<%=parms.getLimit()%>"      />
                  <jsp:param name="multiple" value="<%=parms.getMultiple()%>"   />
                </jsp:include>
              <% } %>
            <% } %>

    <% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
      <form action="event/acknowledge" method="post" name="acknowledge_form">
        <input type="hidden" name="redirectParms" value="<c:out value="<%=req.getQueryString()%>"/>" />
        <input type="hidden" name="actionCode" value="<%=action%>" />
        <%=org.opennms.web.api.Util.makeHiddenTags(req)%>
    <% } %>

    <% String acknowledgeEvent = System.getProperty("opennms.eventlist.acknowledge"); %>

      <table class="table table-condensed severity">
        <thead>
        <tr>
          <% if( "true".equals(acknowledgeEvent) ) { %>
						<% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
							<% if ( AcknowledgeType.UNACKNOWLEDGED.toNormalizedAcknowledgeType().equals(parms.getAckType()) ) { %>
							<th width="1%">Ack</th>
							<% } else { %>
							<th width="1%">UnAck</th>
							<% } %>
						<% } else { %>
							<th width="1%">&nbsp;</th>
						<% } %>
          <% } %>
          <th width="01%"><%=this.makeSortLink(callback, parms, SortStyle.ID,            SortStyle.REVERSE_ID,            "id",           "ID"                  , favorite)%></th>
          <th width="06%"><%=this.makeSortLink(callback, parms, SortStyle.SEVERITY,      SortStyle.REVERSE_SEVERITY,      "severity",     "Severity"            , favorite)%></th>
          <th width="10%"><%=this.makeSortLink(callback, parms, SortStyle.TIME,          SortStyle.REVERSE_TIME,          "time",         "Time"                , favorite)%></th>
          <th width="05%"><%=this.makeSortLink(callback, parms, SortStyle.LOCATION,      SortStyle.REVERSE_LOCATION,      "location",     "Source&nbsp;Location", favorite)%></th>
          <th width="19%"><%=this.makeSortLink(callback, parms, SortStyle.SYSTEMID,      SortStyle.REVERSE_SYSTEMID,      "systemid",     "System-ID"           , favorite)%></th>
          <th width="18%"><%=this.makeSortLink(callback, parms, SortStyle.NODE,          SortStyle.REVERSE_NODE,          "node",         "Node"                , favorite)%></th>
          <th width="05%"><%=this.makeSortLink(callback, parms, SortStyle.NODE_LOCATION, SortStyle.REVERSE_NODE_LOCATION, "nodelocation", "Node&nbsp;Location"  , favorite)%></th>
          <th width="14%"><%=this.makeSortLink(callback, parms, SortStyle.INTERFACE,     SortStyle.REVERSE_INTERFACE,     "interface",    "Interface"           , favorite)%></th>
          <th width="8%"><%=this.makeSortLink(callback, parms, SortStyle.SERVICE,       SortStyle.REVERSE_SERVICE,       "service",      "Service"              , favorite)%></th>
          <th width="5%"><%=this.makeSortLink(callback, parms, SortStyle.ALARMID,       SortStyle.REVERSE_ALARMID,       "alarm",      "Alarm ID"                  , favorite)%></th>
        </tr>
        </thead>     
      <% for( int i=0; i < events.length; i++ ) {
        Event event = events[i];
      	pageContext.setAttribute("event", event);
      %>
      
        <tr valign="top" class="severity-<%=events[i].getSeverity().getLabel()%>">
          <% if( "true".equals(acknowledgeEvent) ) { %>
						<% if( request.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
						<td valign="top" rowspan="3" class="divider">
									<input type="checkbox" name="event" value="<%=events[i].getId()%>" /> 
							</td>
							<% } else { %>
								<td valign="top" rowspan="3" class="divider">&nbsp;</td>
							<% } %>
            <% } %>

          <td valign="top" rowspan="3" class="divider"><a href="event/detail.jsp?id=<%=events[i].getId()%>"><%=events[i].getId()%></a></td>
          
          <td valign="top" rowspan="3" class="divider bright"> 
            <nobr>
            <strong><%= events[i].getSeverity().getLabel() %></strong>
            <% Filter severityFilter = new SeverityFilter(events[i].getSeverity()); %>      
            <% if( !parms.getFilters().contains(severityFilter)) { %>
                <a href="<%=this.makeLink(callback, parms, severityFilter, true, favorite)%>" class="filterLink" title="Show only events with this severity">${addPositiveFilter}</a>
                <a href="<%=this.makeLink(callback, parms, new NegativeSeverityFilter(events[i].getSeverity()), true, favorite)%>" class="filterLink" title="Do not show events with this severity">${addNegativeFilter}</a>
            <% } %>
            </nobr>
          </td>
          <td class="divider">
            <nobr>
              <fmt:formatDate value="${event.time}" type="BOTH" />
              <a href="<%=this.makeLink(callback, parms, new AfterDateFilter(events[i].getTime()), true, favorite)%>"  class="filterLink" title="Only show events occurring after this one">${addAfterFilter}</a>
              <a href="<%=this.makeLink(callback, parms, new BeforeDateFilter(events[i].getTime()), true, favorite)%>" class="filterLink" title="Only show events occurring before this one">${addBeforeFilter}</a>
            </nobr>
          </td>
          <td class="divider">
              <% if(!Strings.isNullOrEmpty(events[i].getLocation())) { %>
              <% Filter locationFilter = new LocationFilter(events[i].getLocation()); %>
              <%=events[i].getLocation()%></a>

              <% if( !parms.getFilters().contains(locationFilter) ) { %>
              <nobr>
                  <a href="<%=this.makeLink(callback, parms, locationFilter, true, favorite)%>" class="filterLink" title="Show only events for this location">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeLocationFilter(events[i].getLocation()), true, favorite)%>" class="filterLink" title="Do not show events for this location">${addNegativeFilter}</a>
              </nobr>
              <% } %>
              <% } else { %>
              &nbsp;
              <% } %>
          </td>
          <td class="divider">
              <% if(!Strings.isNullOrEmpty(events[i].getSystemId())) { %>
              <% Filter systemIdFilter = new SystemIdFilter(events[i].getSystemId()); %>
              <%=events[i].getSystemId()%></a>

              <% if( !parms.getFilters().contains(systemIdFilter) ) { %>
              <nobr>
                  <a href="<%=this.makeLink(callback, parms, systemIdFilter, true, favorite)%>" class="filterLink" title="Show only events for this system Id">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeSystemIdFilter(events[i].getSystemId()), true, favorite)%>" class="filterLink" title="Do not show events for this system Id">${addNegativeFilter}</a>
              </nobr>
              <% } %>
              <% } else { %>
              &nbsp;
              <% } %>
          </td>
          <td class="divider">
	        <% if(events[i].getNodeId() != 0 && events[i].getNodeLabel()!= null ) { %>
              <% Filter nodeFilter = new NodeFilter(events[i].getNodeId(), pageContext.getServletContext()); %>
              <% String[] labels = this.getNodeLabels( events[i].getNodeLabel() ); %>
              <a href="element/node.jsp?node=<%=events[i].getNodeId()%>" title="<%=labels[1]%>"><%=labels[0]%></a>
                    
              <% if( !parms.getFilters().contains(nodeFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, nodeFilter, true, favorite)%>" class="filterLink" title="Show only events on this node">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeNodeFilter(events[i].getNodeId(), pageContext.getServletContext()), true, favorite)%>" class="filterLink" title="Do not show events for this node">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
          <td class="divider">
              <% if(!Strings.isNullOrEmpty(events[i].getNodeLocation())) { %>
              <% Filter nodeLocationFilter = new NodeLocationFilter(events[i].getNodeLocation()); %>
              <%=events[i].getNodeLocation()%></a>

              <% if( !parms.getFilters().contains(nodeLocationFilter) ) { %>
              <nobr>
                  <a href="<%=this.makeLink(callback, parms, nodeLocationFilter, true, favorite)%>" class="filterLink" title="Show only events for this node location">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeNodeLocationFilter(events[i].getNodeLocation()), true, favorite)%>" class="filterLink" title="Do not show events for this node location">${addNegativeFilter}</a>
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
                <c:url var="interfaceLink" value="element/interface.jsp">
                  <c:param name="node" value="<%=String.valueOf(events[i].getNodeId())%>"/>
                  <c:param name="intf" value="<%=events[i].getIpAddress()%>"/>
                </c:url>
                <a href="<c:out value="${interfaceLink}"/>" title="More info on this interface"><%=events[i].getIpAddress()%></a>
              <% } else { %>
                 <%=events[i].getIpAddress()%>
              <% } %>
              <% if( !parms.getFilters().contains(intfFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, intfFilter, true, favorite)%>" class="filterLink" title="Show only events on this IP address">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeInterfaceFilter(events[i].getIpAddress()), true, favorite)%>" class="filterLink" title="Do not show events for this interface">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
          <td class="divider">
            <% if(events[i].getServiceName() != null && events[i].getServiceName() != "") { %>
              <% Filter serviceFilter = new ServiceFilter(events[i].getServiceId(), pageContext.getServletContext()); %>
              <% if( events[i].getNodeId() != 0 && events[i].getIpAddress() != null ) { %>
                <c:url var="serviceLink" value="element/service.jsp">
                  <c:param name="node" value="<%=String.valueOf(events[i].getNodeId())%>"/>
                  <c:param name="intf" value="<%=events[i].getIpAddress()%>"/>
                  <c:param name="service" value="<%=String.valueOf(events[i].getServiceId())%>"/>
                </c:url>
                <a href="<c:out value="${serviceLink}"/>" title="More info on this service"><c:out value="<%=events[i].getServiceName()%>"/></a>
              <% } else { %>
                <c:out value="<%=events[i].getServiceName()%>"/>
              <% } %>
              <% if( !parms.getFilters().contains(serviceFilter)) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, serviceFilter, true, favorite)%>" class="filterLink" title="Show only events with this service type">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeServiceFilter(events[i].getServiceId(), pageContext.getServletContext()), true, favorite)%>" class="filterLink" title="Do not show events for this service">${addNegativeFilter}</a>
                </nobr>
              <% } %>                            
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
          <td>
            <% if (events[i].getAlarmId() != null && events[i].getAlarmId().intValue() != 0) { %>
              <a href="alarm/detail.htm?id=<%=events[i].getAlarmId()%>"><%=events[i].getAlarmId()%></a>
            <% } else { %>
              &nbsp;
            <% }  %>
          </td>
        </tr>
        
        <tr valign="top" class="severity-<%= events[i].getSeverity().getLabel() %>">
          <td colspan="8">
            <% if(events[i].getUei() != null) { %>
              <% Filter exactUEIFilter = new ExactUEIFilter(events[i].getUei()); %>
                <%=events[i].getUei()%>
              <% if( !parms.getFilters().contains(exactUEIFilter)) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, exactUEIFilter, true, favorite)%>" class="filterLink" title="Show only events with this UEI">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeExactUEIFilter(events[i].getUei()), true, favorite)%>" class="filterLink" title="Do not show events for this UEI">${addNegativeFilter}</a>
                </nobr>
              <% } %>
              <% if (req.isUserInRole(Authentication.ROLE_ADMIN)) { %>
               	  <a href="javascript:void()" onclick="submitNewNotificationForm('<%=events[i].getUei()%>');" title="Edit notifications for this Event UEI">Edit notifications for event</a>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          </td>
        </tr>
       
        <tr valign="top" class="severity-<%= events[i].getSeverity().getLabel() %>">
          <td colspan="8"><%=WebSecurityUtils.sanitizeString(events[i].getLogMessage(), true)%></td>
        </tr>
       
      <% } /*end for*/%>
      </table>
        
        <p><%=events.length%> events
          <% 
          if( (req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY )) && "true".equals(acknowledgeEvent)) { %>
            <% if( AcknowledgeType.UNACKNOWLEDGED.toNormalizedAcknowledgeType().equals(parms.getAckType()) ) { %>
              <input type="button" value="Acknowledge Events" onClick="submitForm('<%= AcknowledgeType.UNACKNOWLEDGED.getShortName() %>')"/>
              <input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
              <input TYPE="reset" />
            <% } else if( AcknowledgeType.ACKNOWLEDGED.toNormalizedAcknowledgeType().equals(parms.getAckType()) ) { %>
              <input type="button" value="Unacknowledge Events" onClick="submitForm('<%= AcknowledgeType.ACKNOWLEDGED.getShortName() %>')"/>
              <input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
              <input TYPE="reset" />
            <% } %>
          <% } %>
        </p>
      </form>

            <% if( events.length > 0 ) { %>
              <% String baseUrl = this.makeLink(callback, parms, favorite); %>
              <% if ( eventCount == -1 ) { %>
                <jsp:include page="/includes/resultsIndexNoCount.jsp" flush="false" >
                  <jsp:param name="itemCount"    value="<%=events.length%>" />
                  <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                  <jsp:param name="limit"    value="<%=parms.getLimit()%>"      />
                  <jsp:param name="multiple" value="<%=parms.getMultiple()%>"   />
                </jsp:include>
              <% } else { %>
                <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
                  <jsp:param name="count"    value="<%=eventCount%>" />
                  <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                  <jsp:param name="limit"    value="<%=parms.getLimit()%>"      />
                  <jsp:param name="multiple" value="<%=parms.getMultiple()%>"   />
                </jsp:include>
              <% } %>
            <% } %>          

    <jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<%!
    final String urlBase = "event/list";

    protected String makeSortLink(FilterCallback callback, NormalizedQueryParameters parms, SortStyle style, SortStyle revStyle, String sortString, String title, OnmsFilterFavorite favorite) {
        StringBuffer buffer = new StringBuffer();
        final String styleShortName = style != null ? style.getShortName() : null;
        final String revStyleShortName = revStyle != null ? revStyle.getShortName() : null;

      buffer.append( "<nobr>" );
      
      if( parms.getSortStyleShortName().equals(styleShortName) ) {
          buffer.append( "<img src=\"images/arrowdown.gif\" hspace=\"0\" vspace=\"0\" border=\"0\" alt=\"" );
          buffer.append( title );
          buffer.append( " Ascending Sort\"/>" );
          buffer.append( "&nbsp;<a href=\"" );
          buffer.append( this.makeLink(callback, parms, revStyle, favorite));
          buffer.append( "\" title=\"Reverse the sort\">" );
      } else if( parms.getSortStyleShortName().equals(revStyleShortName)) {
          buffer.append( "<img src=\"images/arrowup.gif\" hspace=\"0\" vspace=\"0\" border=\"0\" alt=\"" );
          buffer.append( title );
          buffer.append( " Descending Sort\"/>" );
          buffer.append( "&nbsp;<a href=\"" );
          buffer.append( this.makeLink(callback, parms, style, favorite ));
          buffer.append( "\" title=\"Reverse the sort\">" );
      } else {
          buffer.append( "<a href=\"" );
          buffer.append( this.makeLink(callback, parms, style, favorite ));
          buffer.append( "\" title=\"Sort by " );
          buffer.append( sortString );
          buffer.append( "\">" );   
      }

      buffer.append( title );
      buffer.append( "</a>" );

      buffer.append( "</nobr>" );

      return( buffer.toString() );
    }

    public String makeLink(FilterCallback callback,  NormalizedQueryParameters parms, OnmsFilterFavorite favorite ) {
      return callback.createLink(urlBase, parms, favorite);
    }


    public String makeLink(FilterCallback callback, final NormalizedQueryParameters parms, SortStyle sortStyle, OnmsFilterFavorite favorite ) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms);
        newParms.setSortStyleShortName(sortStyle.getShortName());
        return this.makeLink(callback, newParms, favorite);
    }


    public String makeLink(FilterCallback callback, NormalizedQueryParameters parms, AcknowledgeType ackType, OnmsFilterFavorite favorite) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms); // clone
        newParms.setAckType(ackType.toNormalizedAcknowledgeType());
        return this.makeLink(callback, newParms, favorite);
    }


    public String makeLink(FilterCallback callback, NormalizedQueryParameters parms, List<Filter> filters, OnmsFilterFavorite favorite) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms); // clone;
        newParms.setFilters(filters);
        return this.makeLink(callback, newParms, favorite);
    }

    public String makeLink(FilterCallback callback, NormalizedQueryParameters parms, Filter filter, boolean add, OnmsFilterFavorite favorite ) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms);
        List<Filter> newList = new ArrayList<Filter>( parms.getFilters());
        if( add ) {
            newList.add( filter );
        } else {
            newList.remove( filter );
        }
        newParms.setFilters(newList);
        return this.makeLink(callback, newParms, favorite);
    }

    public String[] getNodeLabels( String nodeLabel ) {
        nodeLabel = WebSecurityUtils.sanitizeString(nodeLabel);
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

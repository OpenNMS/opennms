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

<%@page language="java" contentType="text/html" session="true" %>
  
<%@page import="org.opennms.core.utils.InetAddressUtils" %>
<%@page import="org.opennms.core.utils.WebSecurityUtils" %>
<%@page import="org.opennms.netmgt.model.OnmsAlarm" %>
<%@page import="org.opennms.netmgt.model.OnmsFilterFavorite"%>
<%@page import="org.opennms.web.alarm.AcknowledgeType" %>
<%@page import="org.opennms.web.alarm.SortStyle" %>
<%@page import="org.opennms.web.alarm.filter.*" %>
<%@page import="org.opennms.web.api.Authentication" %>
<%@page import="org.opennms.web.api.Util" %>
<%@page import="org.opennms.web.controller.alarm.AlarmSeverityChangeController" %>
<%@page import="org.opennms.web.filter.Filter"%>
<%@page import="org.opennms.web.filter.NormalizedQueryParameters" %>
<%@page import="org.opennms.web.servlet.XssRequestWrapper" %>
<%@page import="org.opennms.web.tags.filters.AlarmFilterCallback" %>
<%@page import="org.opennms.web.tags.filters.FilterCallback" %>
<%@page import="org.opennms.web.tags.select.FilterFavoriteSelectTagHandler" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.List" %>
<%@page import="org.opennms.web.tags.FavoriteTag" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="../../taglib.tld" prefix="onms" %>

<%--
  This page is written to be the display (view) portion of the AlarmQueryServlet
  at the /alarm/list.htm URL.  It will not work by itself, as it requires two request
  attributes be set:
  
  1) alarms: the list of {@link OnmsAlarm} instances to display
  2) parms: an org.opennms.web.alarm.AlarmQueryParms object that holds all the 
     parameters used to make this query
--%>

<%
    urlBase = (String) request.getAttribute("relativeRequestPath");

    XssRequestWrapper req = new XssRequestWrapper(request);

    //required attributes
    OnmsAlarm[] alarms = (OnmsAlarm[])req.getAttribute( "alarms" );
    long alarmCount = req.getAttribute("alarmCount") == null ? -1 : (Long)req.getAttribute("alarmCount");
    NormalizedQueryParameters parms = (NormalizedQueryParameters)req.getAttribute( "parms" );
    FilterCallback callback = (AlarmFilterCallback) req.getAttribute("callback");

    if( alarms == null || parms == null ) {
        throw new ServletException( "Missing either the alarms or parms request attribute." );
    }

    // Make 'action' the opposite of the current acknowledgement state
    String action = AcknowledgeType.ACKNOWLEDGED.getShortName();
    if (parms.getAckType() != null && parms.getAckType().equals(AcknowledgeType.ACKNOWLEDGED.toNormalizedAcknowledgeType())) {
        action = AcknowledgeType.UNACKNOWLEDGED.getShortName();
    }

    // optional bookmark
    final OnmsFilterFavorite favorite = (OnmsFilterFavorite) req.getAttribute("favorite");

    pageContext.setAttribute("addPositiveFilter", "<i class=\"fa fa-plus-square-o\"></i>");
    pageContext.setAttribute("addNegativeFilter", "<i class=\"fa fa-minus-square-o\"></i>");
    pageContext.setAttribute("addBeforeFilter", "<i class=\"fa fa-toggle-right\"></i>");
    pageContext.setAttribute("addAfterFilter", "<i class=\"fa fa-toggle-left\"></i>");
    pageContext.setAttribute("filterFavoriteSelectTagHandler", new FilterFavoriteSelectTagHandler("All Alarms"));
%>
<c:set var="baseHref" value="<%=Util.calculateUrlBase(request)%>"/>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Alarm List" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Alarms" />
  <jsp:param name="breadcrumb" value="<a href='${baseHref}alarm/index.htm' title='Alarms System Page'>Alarms</a>" />
  <jsp:param name="breadcrumb" value="List" />
</jsp:include>

<link rel="stylesheet" href="css/font-awesome-4.0.3/css/font-awesome.min.css">

  <script type="text/javascript">
    function checkAllCheckboxes() {
       if( document.alarm_action_form.alarm.length ) {  
         for( i = 0; i < document.alarm_action_form.alarm.length; i++ ) {
           document.alarm_action_form.alarm[i].checked = true
         }
       }
       else {
         document.alarm_action_form.alarm.checked = true
       }
         
    }
    
    function submitForm(anAction)
    {
        var isChecked = false
        var numChecked = 0;
        
        // Decide to which servlet we will submit
        if (anAction == "clear" || anAction == "escalate") {
        	document.alarm_action_form.action = "alarm/changeSeverity";
        } else if (anAction == "acknowledge" || anAction == "unacknowledge") {
        	document.alarm_action_form.action = "alarm/acknowledge";
        }
        
        // Decide what our action should be
        if (anAction == "escalate") {
        	document.alarm_action_form.actionCode.value = "<%=AlarmSeverityChangeController.ESCALATE_ACTION%>";
        } else if (anAction == "clear") {
        	document.alarm_action_form.actionCode.value = "<%=AlarmSeverityChangeController.CLEAR_ACTION%>";
        } else if (anAction == "acknowledge") {
        	document.alarm_action_form.actionCode.value = "<%= AcknowledgeType.ACKNOWLEDGED.getShortName() %>";
        } else if (anAction == "unacknowledge") {
        	document.alarm_action_form.actionCode.value = "<%= AcknowledgeType.UNACKNOWLEDGED.getShortName() %>";
        }
 
        if (document.alarm_action_form.alarm.length)
        {
            for( i = 0; i < document.alarm_action_form.alarm.length; i++ ) 
            {
              //make sure something is checked before proceeding
              if (document.alarm_action_form.alarm[i].checked)
              {
                isChecked=true;
                numChecked+=1;
              }
            }
            
            if (isChecked && document.alarm_action_form.multiple)
            {
              if (numChecked == parseInt(document.alarm_action_form.alarm.length)) 
              { 
                var newPageNum = parseInt(document.alarm_action_form.multiple.value) - 1;
                var findVal = "multiple=" + document.alarm_action_form.multiple.value;
                var replaceWith = "multiple=" + newPageNum;
                var tmpRedirect = document.alarm_action_form.redirectParms.value;
                document.alarm_action_form.redirectParms.value = tmpRedirect.replace(findVal, replaceWith);
                document.alarm_action_form.submit();
              } 
              else 
              {
                document.alarm_action_form.submit();
              }
            }
            else if (isChecked)
            {
              document.alarm_action_form.submit();
            }
            else
            {
                alert("Please check the alarms that you would like to " + anAction + ".");
            }
        }
        else
        {
            if (document.alarm_action_form.alarm.checked)
            {
                document.alarm_action_form.submit();
            }
            else
            {
                alert("Please check the alarms that you would like to " + anAction + ".");
            }
        }
    }

    function changeFavorite(selectElement) {
        var selectedOption = selectElement.options[selectElement.selectedIndex];
        var favoriteId = selectedOption.value.split(';')[0];
        var filter = selectedOption.value.split(';')[1];
        window.location.href = "<%=req.getContextPath()%>/alarm/list?display=<%=parms.getDisplay()%>&favoriteId=" + favoriteId + '&' + filter;
    }

  </script>


      <!-- menu -->
      <div id="linkbar">
      <ul>
      <li><a href="<%=this.makeLink(callback, parms, new ArrayList<Filter>(), favorite)%>" title="Remove all search constraints" >View all alarms</a></li>
      <li><a href="alarm/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a></li>
      <c:choose>
        <c:when test="${param.display == 'long'}">
      <li><a href="<%=this.makeLink(callback, parms, "short", favorite)%>" title="Summary List of Alarms">Short Listing</a></li>
        </c:when>
        <c:otherwise>
      <li><a href="<%=this.makeLink(callback, parms, "long", favorite)%>" title="Detailed List of Alarms">Long Listing</a></li>
        </c:otherwise>
      </c:choose>
      <li><a onclick="javascript:window.open('<%=Util.calculateUrlBase(req, "alarm/severity.jsp")%>','alarm_severity_legend', 'fullscreen=no,toolbar=no,status=no,menubar=no,scrollbars=no,resizable=yes,directories=no,location=no,width=525,height=330')" title="Open a window explaining the alarm severities">Severity Legend</a></li>
      
      <% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
        <% if ( alarmCount > 0 ) { %>
          <li>
            <!-- hidden form for acknowledging the result set -->
            <form style="display:inline" method="post" action="<%= Util.calculateUrlBase(req, "alarm/acknowledgeByFilter") %>" name="acknowledge_by_filter_form">
              <input type="hidden" name="redirectParms" value="<c:out value="<%=req.getQueryString()%>"/>" />
              <input type="hidden" name="actionCode" value="<%=action%>" />
              <%=Util.makeHiddenTags(req)%>
            </form>
            <% if( parms.getAckType().equals(AcknowledgeType.UNACKNOWLEDGED.toNormalizedAcknowledgeType()) ) { %>
              <a href="javascript:void()" onclick="if (confirm('Are you sure you want to acknowledge all alarms in the current search including those not shown on your screen?  (<%=alarmCount%> total alarms)')) { document.acknowledge_by_filter_form.submit(); }" title="Acknowledge all alarms that match the current search constraints, even those not shown on the screen">Acknowledge entire search</a>
            <% } else { %>
              <a href="#javascript:void()" onclick="if (confirm('Are you sure you want to unacknowledge all alarms in the current search including those not shown on your screen)?  (<%=alarmCount%> total alarms)')) { document.acknowledge_by_filter_form.submit(); }" title="Unacknowledge all alarms that match the current search constraints, even those not shown on the screen">Unacknowledge entire search</a>
            <% } %>
          </li>
        <% } %>
      <% } %>
      </ul>
      </div>
      <!-- end menu -->

            <% if( parms.getFilters().size() > 0 || AcknowledgeType.UNACKNOWLEDGED.toNormalizedAcknowledgeType().equals(parms.getAckType()) || AcknowledgeType.ACKNOWLEDGED.toNormalizedAcknowledgeType().equals(parms.getAckType()) ) { %>
                <div>
                <p>
                    Favorites:
                    <onms:select
                            defaultText="All Alarms"
                            elements='${favorites}'
                            selected='${favorite}'
                            handler='${filterFavoriteSelectTagHandler}'
                            onChange='changeFavorite(this)'/>
                </p>
            <% } %>
            <jsp:include page="/includes/alarm-querypanel.jsp" flush="false" />

            <% if( parms.getFilters().size() > 0 || AcknowledgeType.UNACKNOWLEDGED.toNormalizedAcknowledgeType().equals(parms.getAckType()) || AcknowledgeType.ACKNOWLEDGED.toNormalizedAcknowledgeType().equals(parms.getAckType()) ) { %>
                <p>
                    <onms:filters
                            context="/alarm/list"
                            favorite="${favorite}"
                            parameters="${parms}"
                            showRemoveLink="true"
                            showAcknowledgeFilter="true"
                            acknowledgeFilterPrefix="Alarm(s)"
                            acknowledgeFilterSuffix="alarm(s)"
                            callback="${callback}" />

                    <onms:favorite
                            favorite="${favorite}"
                            parameters="${parms}"
                            callback="${callback}"
                            context="/alarm/list"
                            createFavoriteController="/alarm/createFavorite"
                            deleteFavoriteController="/alarm/deleteFavorite"
                            onDeselect="<%=FavoriteTag.Action.CLEAR_FILTERS%>"/>
                </p>
                </div>
            <% } %>
            <onms:alert/>

            <% if( alarmCount > 0 ) { %>
              <% String baseUrl = this.makeLink(callback, parms, favorite); %>
              <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
                <jsp:param name="count"    value="<%=alarmCount%>" />
                <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                <jsp:param name="limit"    value="<%=parms.getLimit()%>"      />
                <jsp:param name="multiple" value="<%=parms.getMultiple()%>"   />
              </jsp:include>
            <% } %>

      <% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
          <form action="<%= Util.calculateUrlBase(request, "alarm/acknowledge") %>" method="post" name="alarm_action_form">
          <input type="hidden" name="redirectParms" value="<c:out value="<%=req.getQueryString()%>"/>" />
          <input type="hidden" name="actionCode" value="<%=action%>" />
          <%=Util.makeHiddenTags(req)%>
      <% } %>
			<jsp:include page="/includes/key.jsp" flush="false" />
      <table>
				<thead>
					<tr>
                                             <% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
						<% if ( parms.getAckType().equals(AcknowledgeType.UNACKNOWLEDGED.toNormalizedAcknowledgeType()) ) { %>
						<th width="1%">Ack</th>
						<% } else if ( parms.getAckType().equals(AcknowledgeType.ACKNOWLEDGED.toNormalizedAcknowledgeType()) ) { %>
						<th width="1%">UnAck</th>
						<% } else if ( parms.getAckType().equals(AcknowledgeType.BOTH.toNormalizedAcknowledgeType()) ) { %>
						<th width="1%">Ack?</th>
						<% } %>
                    <% } else { %>
                        <th width="1%">&nbsp;</th>
                    <% } %>



			<th width="7%">
              <%=this.makeSortLink(callback, parms, SortStyle.ID,        SortStyle.REVERSE_ID,        "id",        "ID" , favorite )%>
              <br />
              <%=this.makeSortLink(callback, parms, SortStyle.SEVERITY,  SortStyle.REVERSE_SEVERITY,  "severity",  "Severity", favorite  )%>
            </th>
			<th width="19%">
              <%=this.makeSortLink(callback, parms, SortStyle.NODE,      SortStyle.REVERSE_NODE,      "node",      "Node", favorite      )%>
              <c:if test="${param.display == 'long'}">
              <br />
              <%=this.makeSortLink(callback, parms, SortStyle.INTERFACE, SortStyle.REVERSE_INTERFACE, "interface", "Interface", favorite )%>
              <br />
              <%=this.makeSortLink(callback, parms, SortStyle.SERVICE,   SortStyle.REVERSE_SERVICE,   "service",   "Service", favorite   )%>
              </c:if>
            </th>
			<th width="3%">
              <%=this.makeSortLink(callback, parms, SortStyle.COUNT,  SortStyle.REVERSE_COUNT,  "count",  "Count", favorite  )%>
            </th>
			<th width="13%">
              <%=this.makeSortLink(callback, parms, SortStyle.LASTEVENTTIME,  SortStyle.REVERSE_LASTEVENTTIME,  "lasteventtime",  "Last Event Time", favorite  )%>
              <c:if test="${param.display == 'long'}">
              <br />
              <%=this.makeSortLink(callback, parms, SortStyle.FIRSTEVENTTIME,  SortStyle.REVERSE_FIRSTEVENTTIME,  "firsteventtime",  "First Event Time", favorite  )%>
              <br />
              <% if ( parms.getAckType().equals(AcknowledgeType.ACKNOWLEDGED.toNormalizedAcknowledgeType()) ) { %>
              <%=this.makeSortLink(callback, parms, SortStyle.ACKUSER,  SortStyle.REVERSE_ACKUSER,  "ackuser",  "Acknowledged By", favorite  )%>
              <% } %>
              </c:if>
            </th>
			<th width="56%">Log Msg</th>
		</tr>
	</thead>

      <% for( int i=0; i < alarms.length; i++ ) { 
      	pageContext.setAttribute("alarm", alarms[i]);
      %> 

        <tr class="<%=alarms[i].getSeverity().getLabel()%>">
          <% if( parms.getAckType().equals(AcknowledgeType.BOTH.toNormalizedAcknowledgeType()) ) { %>
              <td class="divider" valign="middle" rowspan="1">
                <nobr>
                  <input type="checkbox" name="alarm" disabled="disabled" <%=alarms[i].isAcknowledged() ? "checked='true'" : ""%> /> 
                </nobr>
          <% } else if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
              <td class="divider" valign="middle" rowspan="1">
                <nobr>
                  <input type="checkbox" name="alarm" value="<%=alarms[i].getId()%>" /> 
                </nobr>
          <% } else { %>
            <td valign="middle" rowspan="1" class="divider">&nbsp;
          <% } %>
          </td>

          
          <td class="divider bright" valign="middle" rowspan="1">
            
            <a style="vertical-align:middle" href="<%= Util.calculateUrlBase(request, "alarm/detail.htm?id=" + alarms[i].getId()) %>"><%=alarms[i].getId()%></a>
            <c:if test="<%= (alarms[i].getStickyMemo() != null && alarms[i].getStickyMemo().getId() != null) && (alarms[i].getReductionKeyMemo() != null && alarms[i].getReductionKeyMemo().getId() != null) %>">
                <br />
            </c:if>
            <c:if test="<%= alarms[i].getStickyMemo() != null && alarms[i].getStickyMemo().getId() != null%>">
                <img style="vertical-align:middle" src="images/AlarmMemos/StickyMemo.png" width="20" height="20" title="<%=alarms[i].getStickyMemo().getBody() %>"/>
            </c:if>
            <c:if test="<%= alarms[i].getReductionKeyMemo() != null && alarms[i].getReductionKeyMemo().getId() != null%>">
                <img style="vertical-align:middle" src="images/AlarmMemos/JournalMemo.png" width="20" height="20" title="<%=alarms[i].getReductionKeyMemo().getBody() %>"/>
            </c:if>

          <c:if test="${param.display == 'long'}">
            <% if(alarms[i].getUei() != null) { %>
              <% Filter exactUEIFilter = new ExactUEIFilter(alarms[i].getUei()); %>
                <br />UEI
              <% if( !parms.getFilters().contains( exactUEIFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, exactUEIFilter, true, favorite)%>" class="filterLink" title="Show only events with this UEI">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeExactUEIFilter(alarms[i].getUei()), true, favorite)%>" class="filterLink" title="Do not show events for this UEI">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
            <% Filter severityFilter = new SeverityFilter(alarms[i].getSeverity()); %>      
            <% if( !parms.getFilters().contains( severityFilter )) { %>
		<br />Sev.
              <nobr>
                <a href="<%=this.makeLink(callback, parms, severityFilter, true, favorite)%>" class="filterLink" title="Show only alarms with this severity">${addPositiveFilter}</a>
                <a href="<%=this.makeLink(callback, parms, new NegativeSeverityFilter(alarms[i].getSeverity()), true, favorite)%>" class="filterLink" title="Do not show alarms with this severity">${addNegativeFilter}</a>

              </nobr>
            <% } %>
          </c:if>
          </td>
          <td class="divider">
	    <% if(alarms[i].getNodeId() != null && alarms[i].getNodeLabel()!= null ) { %>
              <% Filter nodeFilter = new NodeFilter(alarms[i].getNodeId(), getServletContext()); %>             
              <% String[] labels = this.getNodeLabels( alarms[i].getNodeLabel() ); %>
              <a href="element/node.jsp?node=<%=alarms[i].getNodeId()%>" title="<%=labels[1]%>"><%=labels[0]%></a>
                    
              <% if( !parms.getFilters().contains(nodeFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, nodeFilter, true, favorite)%>" class="filterLink" title="Show only alarms on this node">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeNodeFilter(alarms[i].getNodeId(), getServletContext()), true, favorite)%>" class="filterLink" title="Do not show alarms for this node">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          <c:if test="${param.display == 'long'}">
		<br />
            <% if(alarms[i].getIpAddr() != null ) { %>
              <% Filter intfFilter = new InterfaceFilter(alarms[i].getIpAddr()); %>
              <% if( alarms[i].getNodeId() != null ) { %>
                <c:url var="interfaceLink" value="element/interface.jsp">
                  <c:param name="node" value="<%=String.valueOf(alarms[i].getNodeId())%>"/>
                  <c:param name="intf" value="<%=InetAddressUtils.str(alarms[i].getIpAddr())%>"/>
                </c:url>
                <a href="<c:out value="${interfaceLink}"/>" title="More info on this interface"><%=InetAddressUtils.str(alarms[i].getIpAddr())%></a>
              <% } else { %>
                <%=InetAddressUtils.str(alarms[i].getIpAddr())%>
              <% } %>
              <% if( !parms.getFilters().contains(intfFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, intfFilter, true, favorite)%>" class="filterLink" title="Show only alarms on this IP address">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeInterfaceFilter(alarms[i].getIpAddr()), true, favorite)%>" class="filterLink" title="Do not show alarms for this interface">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          <br />
            <% if(alarms[i].getServiceType() != null && !"".equals(alarms[i].getServiceType().getName())) { %>
              <% Filter serviceFilter = new ServiceFilter(alarms[i].getServiceType().getId(), getServletContext()); %>
              <% if( alarms[i].getNodeId() != null && alarms[i].getIpAddr() != null ) { %>
                <c:url var="serviceLink" value="element/service.jsp">
                  <c:param name="node" value="<%=String.valueOf(alarms[i].getNodeId())%>"/>
                  <c:param name="intf" value="<%=InetAddressUtils.str(alarms[i].getIpAddr())%>"/>
                  <c:param name="service" value="<%=String.valueOf(alarms[i].getServiceType().getId())%>"/>
                </c:url>
                <a href="<c:out value="${serviceLink}"/>" title="More info on this service"><c:out value="<%=alarms[i].getServiceType().getName()%>"/></a>
              <% } else { %>
                <c:out value="<%=alarms[i].getServiceType().getName()%>"/>
              <% } %>
              <% if( !parms.getFilters().contains( serviceFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, serviceFilter, true, favorite)%>" class="filterLink" title="Show only alarms with this service type">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeServiceFilter(alarms[i].getServiceType().getId(), getServletContext()), true, favorite)%>" class="filterLink" title="Do not show alarms for this service">${addNegativeFilter}</a>
                </nobr>
              <% } %>                            
            <% } %>
            </c:if>
          </td>          
          <td class="divider" valign="middle" rowspan="1" >
	    <% if(alarms[i].getId() > 0 ) { %>           
                <nobr>
                  <a href="event/list.htm?sortby=id&amp;acktype=unack&amp;filter=alarm%3d<%=alarms[i].getId()%>"><%=alarms[i].getCounter()%></a>
                </nobr>
            <% } else { %>
            <%=alarms[i].getCounter()%>
            <% } %>
          </td>
          <td class="divider">
            <nobr>
              <% if(alarms[i].getLastEvent() != null) { %><span title="Event <%= alarms[i].getLastEvent().getId()%>"><a href="event/detail.htm?id=<%= alarms[i].getLastEvent().getId()%>"><% } %>
                <fmt:formatDate value="${alarm.lastEventTime}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${alarm.lastEventTime}" type="time" pattern="HH:mm:ss"/>
              <% if(alarms[i].getLastEvent() != null) { %></a></span><% } %>
            </nobr>
            <nobr>
              <a href="<%=this.makeLink(callback, parms, new AfterLastEventTimeFilter(alarms[i].getLastEventTime()), true, favorite)%>"  class="filterLink" title="Only show alarms occurring after this one">${addAfterFilter}</a>
              <a href="<%=this.makeLink(callback, parms, new BeforeLastEventTimeFilter(alarms[i].getLastEventTime()), true, favorite)%>" class="filterLink" title="Only show alarms occurring before this one">${addBeforeFilter}</a>
            </nobr>
          <c:if test="${param.display == 'long'}">
          <br />
            <nobr><fmt:formatDate value="${alarm.firstEventTime}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${alarm.firstEventTime}" type="time" pattern="HH:mm:ss"/></nobr>
            <nobr>
              <a href="<%=this.makeLink(callback, parms, new AfterFirstEventTimeFilter(alarms[i].getFirstEventTime()), true, favorite)%>"  class="filterLink" title="Only show alarms occurring after this one">${addAfterFilter}</a>
              <a href="<%=this.makeLink(callback, parms, new BeforeFirstEventTimeFilter(alarms[i].getFirstEventTime()), true, favorite)%>" class="filterLink" title="Only show alarms occurring before this one">${addBeforeFilter}</a>
            </nobr>
          <br />
              <% if ( parms.getAckType().equals(AcknowledgeType.ACKNOWLEDGED.toNormalizedAcknowledgeType()) ) { %>
			<nobr><%=alarms[i].getAckUser()%></nobr>          
            <nobr>
              <a href="<%=this.makeLink(callback, parms, new AcknowledgedByFilter(alarms[i].getAckUser()), true, favorite)%>"  class="filterLink" title="Only show alarms ack by this user">${addPositiveFilter}</a>
              <a href="<%=this.makeLink(callback, parms, new NegativeAcknowledgedByFilter(alarms[i].getAckUser()), true, favorite)%>" class="filterLink" title="Only show alarms ack by other users">${addNegativeFilter}</a>
            </nobr>
			<% }%>
          </c:if>
          </td>
          <td class="divider"><%=WebSecurityUtils.sanitizeString(alarms[i].getLogMsg(), true)%></td>
        </tr> 
      <% } /*end for*/%>

      </table>
			<hr />
			 <p><%=alarms.length%> alarms &nbsp;
      <% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
          <input TYPE="reset" />
          <input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
          <select name="alarmAction">
        <% if( parms.getAckType().equals(AcknowledgeType.UNACKNOWLEDGED.toNormalizedAcknowledgeType()) ) { %>
          <option value="acknowledge">Acknowledge Alarms</option>
        <% } else if( parms.getAckType().equals(AcknowledgeType.ACKNOWLEDGED.toNormalizedAcknowledgeType()) ) { %>
          <option value="unacknowledge">Unacknowledge Alarms</option>
        <% } %>
          <option value="clear">Clear Alarms</option>
          <option value="escalate">Escalate Alarms</option>
          </select>
          <input type="button" value="Go" onClick="submitForm(document.alarm_action_form.alarmAction.value)" />
      <% } %>
        </p>
      </form>


        <% if( alarmCount > 0 ) { %>
          <% String baseUrl = this.makeLink(callback, parms, favorite); %>
          <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
            <jsp:param name="count"    value="<%=alarmCount%>" />
            <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
            <jsp:param name="limit"    value="<%=parms.getLimit()%>"      />
            <jsp:param name="multiple" value="<%=parms.getMultiple()%>"   />
          </jsp:include>
        <% } %>


<jsp:include page="/includes/bookmark.jsp" flush="false" />
<jsp:include page="/includes/footer.jsp" flush="false" />


<%!
    String urlBase;

    protected String makeSortLink(FilterCallback callback, NormalizedQueryParameters parms, SortStyle style, SortStyle revStyle, String sortString, String title, OnmsFilterFavorite favorite ) {
      StringBuffer buffer = new StringBuffer();
      final String styleShortName = style != null ? style.getShortName() : null;
      final String revStyleShortName = revStyle != null ? revStyle.getShortName() : null;

      buffer.append( "<nobr>" );
      
      if( parms.getSortStyleShortName().equals(styleShortName) ) {
          buffer.append( "<img src=\"images/arrowdown.gif\" hspace=\"0\" vspace=\"0\" border=\"0\" alt=\"" );
          buffer.append( title );
          buffer.append( " Ascending Sort\"/>" );
          buffer.append( "&nbsp;<a href=\"" );
          buffer.append( this.makeLink( callback, parms, revStyle, favorite ));
          buffer.append( "\" title=\"Reverse the sort\">" );
      } else if( parms.getSortStyleShortName().equals(revStyleShortName)) {
          buffer.append( "<img src=\"images/arrowup.gif\" hspace=\"0\" vspace=\"0\" border=\"0\" alt=\"" );
          buffer.append( title );
          buffer.append( " Descending Sort\"/>" );
          buffer.append( "&nbsp;<a href=\"" );
          buffer.append( this.makeLink( callback, parms, style, favorite ));
          buffer.append( "\" title=\"Reverse the sort\">" );
      } else {
          buffer.append( "<a href=\"" );
          buffer.append( this.makeLink( callback, parms, style, favorite ));
          buffer.append( "\" title=\"Sort by " );
          buffer.append( sortString );
          buffer.append( "\">" );   
      }

      buffer.append( title );
      buffer.append( "</a>" );

      buffer.append( "</nobr>" );

      return( buffer.toString() );
    }

    public String makeLink( FilterCallback callback, NormalizedQueryParameters params, OnmsFilterFavorite favorite) {
        return callback.createLink(urlBase, params, favorite);
    }

    public String makeLink(FilterCallback callback, NormalizedQueryParameters parms, String display, OnmsFilterFavorite favorite) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms);
        newParms.setDisplay(display);
        return makeLink(callback, newParms, favorite);
    }

    public String makeLink(FilterCallback callback, NormalizedQueryParameters parms, SortStyle sortStyle, OnmsFilterFavorite favorite ) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms);
        newParms.setSortStyleShortName(sortStyle.getShortName());
        return makeLink(callback, newParms, favorite);
    }


    public String makeLink(FilterCallback callback, NormalizedQueryParameters parms, AcknowledgeType ackType, OnmsFilterFavorite favorite ) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms);
        newParms.setAckType(ackType.toNormalizedAcknowledgeType());
        return makeLink(callback, newParms, favorite);
    }


    public String makeLink(FilterCallback callback, NormalizedQueryParameters parms, List<Filter> filters, OnmsFilterFavorite favorite ) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms); // clone;
        newParms.setFilters(filters);
        return this.makeLink(callback, newParms, favorite);
    }

    public String makeLink(FilterCallback callback, NormalizedQueryParameters parms, Filter filter, boolean add, OnmsFilterFavorite favorite) {
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

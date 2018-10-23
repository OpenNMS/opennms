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

<%@page language="java" contentType="text/html" session="true" %>
  
<%@page import="org.opennms.core.utils.InetAddressUtils" %>
<%@page import="org.opennms.core.utils.WebSecurityUtils" %>
<%@page import="org.opennms.netmgt.model.OnmsAlarm" %>
<%@page import="org.opennms.netmgt.model.OnmsEvent" %>
<%@page import="org.opennms.netmgt.model.OnmsFilterFavorite"%>
<%@page import="org.opennms.web.alarm.AcknowledgeType" %>
<%@page import="org.opennms.web.alarm.SortStyle" %>
<%@page import="org.opennms.web.alarm.filter.*" %>
<%@page import="org.opennms.web.api.Authentication" %>
<%@page import="org.opennms.web.api.Util" %>
<%@page import="org.opennms.web.controller.alarm.AlarmSeverityChangeController" %>
<%@page import="org.opennms.web.filter.Filter"%>
<%@page import="org.opennms.web.filter.NormalizedQueryParameters" %>
<%@page import="org.opennms.web.filter.NormalizedAcknowledgeType" %>
<%@page import="org.opennms.web.servlet.XssRequestWrapper" %>
<%@page import="org.opennms.web.tags.filters.AlarmFilterCallback" %>
<%@page import="org.opennms.web.tags.filters.FilterCallback" %>
<%@page import="org.opennms.web.tags.select.FilterFavoriteSelectTagHandler" %>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.List" %>
<%@page import="org.opennms.web.tags.FavoriteTag" %>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/taglib.tld" prefix="onms" %>

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
    
    // show unacknowledged alarms as flashing alarms
    String unAckFlashStr = System.getProperty("opennms.alarmlist.unackflash");
	boolean unAckFlash = (unAckFlashStr == null) ? false : "true".equals(unAckFlashStr.trim());
	if(unAckFlash) parms.setAckType(NormalizedAcknowledgeType.BOTH);
    
    FilterCallback callback = (AlarmFilterCallback) req.getAttribute("callback");

    if( alarms == null || parms == null ) {
        throw new ServletException( "Missing either the alarms or parms request attribute." );
    }
  
    //Make 'action' the opposite of the current acknowledgement state
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
    
    // get sound constants from session, request or opennms.properties
	String soundEnabledStr = System.getProperty("opennms.alarmlist.sound.enable");
	boolean soundEnabled = (soundEnabledStr == null) ? false : "true".equals(soundEnabledStr.trim());

	boolean soundOn = false;
	boolean soundOnEvent = false;

	String alarmSoundStatusStr = null; // newalarm,newalarmcount,off
	if (soundEnabled) {
		// get request parameter if present, or session parameter if present or system property
		String sessionStatus = (String) session.getAttribute("opennms.alarmlist.STATUS");
		alarmSoundStatusStr = request.getParameter("alarmSoundStatus");
		if (alarmSoundStatusStr != null) {
			if(!alarmSoundStatusStr.equals(sessionStatus)){
			   session.setAttribute("opennms.alarmlist.STATUS",alarmSoundStatusStr);
			   session.setAttribute("opennms.alarmlist.HIGHEST", new Integer(0));
			}
		} else {
			alarmSoundStatusStr = (String) session.getAttribute("opennms.alarmlist.STATUS");
			if (alarmSoundStatusStr == null) {
				alarmSoundStatusStr = System.getProperty("opennms.alarmlist.sound.status");
				if (alarmSoundStatusStr == null) alarmSoundStatusStr = "off";
				session.setAttribute("opennms.alarmlist.STATUS",alarmSoundStatusStr);
			}
		}
		switch (alarmSoundStatusStr) {
		case "newalarm": {
			soundOn = true;
			soundOnEvent = false;
			break;
		}
		case "newalarmcount": {
			soundOn = true;
			soundOnEvent = true;
			break;
		}
		default: { //off 
			break;
		}
		}
	}
%>
<c:set var="baseHref" value="<%=Util.calculateUrlBase(request)%>"/>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Alarm List" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Alarms" />
  <jsp:param name="breadcrumb" value="<a href='${baseHref}alarm/index.htm' title='Alarms System Page'>Alarms</a>" />
  <jsp:param name="breadcrumb" value="List" />
</jsp:include>

<c:url var="alarmListLink" value="alarm/list">
  <c:param name="display" value="<%=parms.getDisplay()%>"/>
</c:url>

<% if(unAckFlash){ //style to make unacknowledged alarms flash %>
<style media="screen" type="text/css">
.blink_text {
    animation:1s blinker linear infinite;
    -webkit-animation:1s blinker linear infinite;
    -moz-animation:1s blinker linear infinite;
    }
    @-moz-keyframes blinker {  
     0% { opacity: 1.0; }
     50% { opacity: 0.0; }
     100% { opacity: 1.0; }
     }
    @-webkit-keyframes blinker {  
     0% { opacity: 1.0; }
     50% { opacity: 0.0; }
     100% { opacity: 1.0; }
     }
    @keyframes blinker {  
     0% { opacity: 1.0; }
     50% { opacity: 0.0; }
     100% { opacity: 1.0; }
     }
</style>
<% } %>

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
        changeFavorite(favoriteId, filter);
    }

    function changeFavorite(favoriteId, filter) {
        window.location.href = "<%=req.getContextPath()%>/${alarmListLink}&favoriteId=" + favoriteId + '&' + filter;
    }
    
    // function to play sound
    var snd = new Audio("sounds/alert.wav"); // loads automatically
    function playSound(){
    	snd.play();
    }

  </script>
  
<div id="severityLegendModal" class="modal fade" tabindex="-1">
  <div class="modal-dialog">
    <div class="modal-content">
        <jsp:include page="/alarm/severity.jsp" flush="false" />
    </div>
  </div>
</div>

      <!-- menu -->
      <div class="row form-inline">
      <div class="col-md-12">
      <a class="btn btn-default" href="<%=this.makeLink(callback, parms, new ArrayList<Filter>(), favorite)%>" title="Remove all search constraints" >View all alarms</a>
      <a class="btn btn-default" href="alarm/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a>
      <c:choose>
        <c:when test="${param.display == 'long'}">
      <a class="btn btn-default" href="<%=this.makeLink(callback, parms, "short", favorite)%>" title="Summary List of Alarms">Short Listing</a>
        </c:when>
        <c:otherwise>
      <a class="btn btn-default" href="<%=this.makeLink(callback, parms, "long", favorite)%>" title="Detailed List of Alarms">Long Listing</a>
        </c:otherwise>
      </c:choose>
      <a class="btn btn-default" onclick="$('#severityLegendModal').modal()">Severity Legend</a>
      
      <% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
        <% if ( alarmCount > 0 ) { %>
            <!-- hidden form for acknowledging the result set -->
            <form style="display:inline" method="post" action="<%= Util.calculateUrlBase(req, "alarm/acknowledgeByFilter") %>" name="acknowledge_by_filter_form">
              <input type="hidden" name="redirectParms" value="<c:out value="<%=req.getQueryString()%>"/>" />
              <input type="hidden" name="actionCode" value="<%=action%>" />
              <%=Util.makeHiddenTags(req)%>
            </form>
         <% if (!unAckFlash) { // global ack or unack only displayed if flashing disabled %>
            <% if( parms.getAckType().equals(AcknowledgeType.UNACKNOWLEDGED.toNormalizedAcknowledgeType()) ) { %>
              <a class="btn btn-default" href="javascript:void()" onclick="if (confirm('Are you sure you want to acknowledge all alarms in the current search including those not shown on your screen?  (<%=alarmCount%> total alarms)')) { document.acknowledge_by_filter_form.submit(); }" title="Acknowledge all alarms that match the current search constraints, even those not shown on the screen">Acknowledge entire search</a>
            <% } else { %>
              <a class="btn btn-default" href="#javascript:void()" onclick="if (confirm('Are you sure you want to unacknowledge all alarms in the current search including those not shown on your screen)?  (<%=alarmCount%> total alarms)')) { document.acknowledge_by_filter_form.submit(); }" title="Unacknowledge all alarms that match the current search constraints, even those not shown on the screen">Unacknowledge entire search</a>
            <% } %>
         <% } %>
        <% } %>
      <% } %>

      <select class="form-control pull-right" onchange="location = this.value;">
          <option value="<%= makeLimitLink(callback, parms, favorite,  10) %>" ${(parms.getLimit() ==  10) ? 'selected' : ''}> 10</option>
          <option value="<%= makeLimitLink(callback, parms, favorite,  20) %>" ${(parms.getLimit() ==  20) ? 'selected' : ''}> 20</option>
          <option value="<%= makeLimitLink(callback, parms, favorite,  50) %>" ${(parms.getLimit() ==  50) ? 'selected' : ''}> 50</option>
          <option value="<%= makeLimitLink(callback, parms, favorite, 100) %>" ${(parms.getLimit() == 100) ? 'selected' : ''}>100</option>
          <option value="<%= makeLimitLink(callback, parms, favorite, 500) %>" ${(parms.getLimit() == 500) ? 'selected' : ''}>500</option>
      </select>
      
<% if(soundEnabled){ %>
      <select class="form-control pull-right" onchange="location = this.value;">
          <option value="<%= makeAlarmSoundLink(callback,  parms, favorite,"off") %>" <% out.write("off".equals(alarmSoundStatusStr) ? "selected" : ""); %>> Sound off</option>
          <option value="<%= makeAlarmSoundLink(callback,  parms, favorite,"newalarm" ) %>" <% out.write("newalarm".equals(alarmSoundStatusStr) ? "selected" : ""); %>> Sound on new alarm</option>
          <option value="<%= makeAlarmSoundLink(callback,  parms, favorite,"newalarmcount" ) %>" <% out.write("newalarmcount".equals(alarmSoundStatusStr) ? "selected" : ""); %>> Sound on alarm event count</option>
      </select>
<% 
}
%>

      </div>
      </div>
      <!-- end menu -->

<div class="hidden">
  <jsp:include page="/includes/alarm-querypanel.jsp" flush="false" />
</div>

<%-- This tag writes out the createFavorite(), deleteFavorite(), and clearFilters() methods --%>
<onms:favorite
  favorite="${favorite}"
  parameters="${parms}"
  callback="${callback}"
  context="/alarm/list"
  createFavoriteController="/alarm/createFavorite"
  deleteFavoriteController="/alarm/deleteFavorite"
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
                            context="/alarm/list"
                            favorite="${favorite}"
                            parameters="${parms}"
                            showRemoveLink="true"
                            showAcknowledgeFilter="true"
                            acknowledgeFilterPrefix="Alarm(s)"
                            acknowledgeFilterSuffix="alarm(s)"
                            callback="${callback}" />
                </div>
            <% } %>
</div>

<div class="row">
  <br/>
</div>

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
          <form class="form-inline" action="<%= Util.calculateUrlBase(request, "alarm/acknowledge") %>" method="post" name="alarm_action_form">
          <input type="hidden" name="redirectParms" value="<c:out value="<%=req.getQueryString()%>"/>" />
          <input type="hidden" name="actionCode" value="<%=action%>" />
          <%=Util.makeHiddenTags(req)%>
      <% } %>

      <table class="table table-condensed severity">
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

			<th width="2%">
              <%=this.makeSortLink(callback, parms, SortStyle.ID,        SortStyle.REVERSE_ID,        "id",        "ID" ,       favorite )%>
            </th>
            <th width="4%">
              <%=this.makeSortLink(callback, parms, SortStyle.SITUATION, SortStyle.REVERSE_SITUATION, "situation", "Situation", favorite )%>
            </th>
            <th width="6%">
              <%=this.makeSortLink(callback, parms, SortStyle.SEVERITY,  SortStyle.REVERSE_SEVERITY,  "severity",  "Severity",  favorite )%>
            </th>
			<th>
              <%=this.makeSortLink(callback, parms, SortStyle.NODE,      SortStyle.REVERSE_NODE,      "node",      "Node",      favorite )%>
              <c:if test="${param.display == 'long'}">
              /
              <%=this.makeSortLink(callback, parms, SortStyle.INTERFACE, SortStyle.REVERSE_INTERFACE, "interface", "Interface", favorite )%>
              </th>
              <th>
              <%=this.makeSortLink(callback, parms, SortStyle.NODE_LOCATION, SortStyle.REVERSE_NODE_LOCATION, "nodelocation", "Node Location", favorite )%>
              </th>
              <th>
              <%=this.makeSortLink(callback, parms, SortStyle.SERVICE,   SortStyle.REVERSE_SERVICE,   "service",   "Service",   favorite )%>
              </c:if>
            </th>
			<th width="3%">
              <%=this.makeSortLink(callback, parms, SortStyle.COUNT,  SortStyle.REVERSE_COUNT,  "count",  "Count", favorite  )%>
            </th>
			<th <% if ("long".equals(request.getParameter("display"))) { %>width="13%"<% } %>>
              <%=this.makeSortLink(callback, parms, SortStyle.LASTEVENTTIME,  SortStyle.REVERSE_LASTEVENTTIME,  "lasteventtime",  "Last", favorite  )%>
              <c:if test="${param.display == 'long'}">
              /
              <%=this.makeSortLink(callback, parms, SortStyle.FIRSTEVENTTIME,  SortStyle.REVERSE_FIRSTEVENTTIME,  "firsteventtime",  "First Event Time", favorite  )%>
              </th>
              <th>
              <%=this.makeSortLink(callback, parms, SortStyle.LOCATION,  SortStyle.REVERSE_LOCATION,  "location",  "Event Source Location", favorite  )%>
              <% if ( parms.getAckType().equals(AcknowledgeType.ACKNOWLEDGED.toNormalizedAcknowledgeType()) ) { %>
              </th>
              <th>
              <%=this.makeSortLink(callback, parms, SortStyle.ACKUSER,  SortStyle.REVERSE_ACKUSER,  "ackuser",  "Acknowledged By", favorite  )%>
              <% } %>
              </c:if>
            </th>
            <c:if test="${param.display != 'long'}">
			<th width="52%">Log Msg</th>
			</c:if>
		</tr>
	</thead>

      <% for( int i=0; i < alarms.length; i++ ) { 
      	pageContext.setAttribute("alarm", alarms[i]);
      %>

      <% if(unAckFlash){ // flash unacknowledged alarms %>
        <tr class="severity-<%=alarms[i].getSeverity().getLabel()%> <%=alarms[i].isAcknowledged() ? "" : "blink_text"%>">
        
          <% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
              <td class="divider" valign="middle" rowspan="<%= ("long".equals(request.getParameter("display"))? 2:1) %>">
                <nobr>
                  <input  type="checkbox" name="alarm" value="<%=alarms[i].getId()%>" /> 
                  <% if(unAckFlash && alarms[i].isAcknowledged() ){ // tick char %>
                  &#10004;
                  <% } %>
                </nobr>
          <% } else { %>
            <td valign="middle" rowspan="<%= ("long".equals(request.getParameter("display"))? 2:1) %>" class="divider">
                  <% if(unAckFlash && alarms[i].isAcknowledged() ){ // tick char %>
                  &#10004;
                  <% } %>
          <% } %>
          </td>
        
      <% } else { // normal behaviour %>
        <tr class="severity-<%=alarms[i].getSeverity().getLabel()%> ">

          <% if( parms.getAckType().equals(AcknowledgeType.BOTH.toNormalizedAcknowledgeType()) ) { %>
              <td class="divider" valign="middle" rowspan="<%= ("long".equals(request.getParameter("display"))? 2:1) %>">
                <nobr>
                  <input type="checkbox" name="alarm" disabled="disabled" <%=alarms[i].isAcknowledged() ? "checked='true'" : ""%> /> 
                </nobr>
          <% } else if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
              <td class="divider" valign="middle" rowspan="<%= ("long".equals(request.getParameter("display"))? 2:1) %>">
                <nobr>
                  <input type="checkbox" name="alarm" value="<%=alarms[i].getId()%>" /> 
                </nobr>
          <% } else { %>
            <td valign="middle" rowspan="<%= ("long".equals(request.getParameter("display"))? 2:1) %>" class="divider">&nbsp;
          <% } %>
          </td>
      <% } %>

          <td class="divider" valign="middle" rowspan="<%= ("long".equals(request.getParameter("display"))? 2:1) %>">

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
                <br />
                <nobr>
                UEI
              <% if( !parms.getFilters().contains( exactUEIFilter )) { %>
                  <a href="<%=this.makeLink(callback, parms, exactUEIFilter, true, favorite)%>" class="filterLink" title="Show only events with this UEI">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeExactUEIFilter(alarms[i].getUei()), true, favorite)%>" class="filterLink" title="Do not show events for this UEI">${addNegativeFilter}</a>              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
            </nobr>
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
          <td class="divider" valign="middle" rowspan="<%= ("long".equals(request.getParameter("display"))? 2:1) %>">
              <%
                  if (alarms[i].isSituation()) {
                      if(parms.getFilters().contains(new SituationFilter(true))) {
              %>
              <i class="fa fa-check-square-o"></i>
              <%
              } else {
              %>
              <a href="<%=this.makeLink(callback, parms, new SituationFilter(true), true, favorite)%>" class="filterLink" title="Show only situations"><i class="fa fa-check-square-o"></i></a>
              <%
                  }
              } else {
                  if(parms.getFilters().contains(new SituationFilter(false))) {
              %>
              <i class="fa fa-square-o"></i>
              <%
              } else {
              %>
              <a href="<%=this.makeLink(callback, parms, new SituationFilter(false), true, favorite)%>" class="filterLink" title="Show only alarms"><i class="fa fa-square-o"></i></a>
              <%
                      }
                  }
              %>
          </td>
          <td class="divider bright" valign="middle" rowspan="<%= ("long".equals(request.getParameter("display"))? 2:1) %>">
            <nobr>
            <strong><%= alarms[i].getSeverity().getLabel() %></strong>
            <% Filter severityFilter = new SeverityFilter(alarms[i].getSeverity()); %>
            <% if( !parms.getFilters().contains(severityFilter)) { %>
              <a href="<%=this.makeLink(callback, parms, severityFilter, true, favorite)%>" class="filterLink" title="Show only events with this severity">${addPositiveFilter}</a>
              <a href="<%=this.makeLink(callback, parms, new NegativeSeverityFilter(alarms[i].getSeverity()), true, favorite)%>" class="filterLink" title="Do not show events with this severity">${addNegativeFilter}</a>
            <% } %>
            </nobr>
          </td>
          <td>
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
            </td>
            <td>
            <% if (alarms[i].getNodeId() != null && alarms[i].getNode() != null && alarms[i].getNode().getLocation() != null) { %>
              <% String location = alarms[i].getNode().getLocation().getLocationName(); %>
              <% Filter locationFilter = new NodeLocationFilter(location); %>
              <a href="element/node.jsp?node=<%=alarms[i].getNodeId()%>"><%= location %></a>
              <% if( !parms.getFilters().contains(locationFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, locationFilter, true, favorite)%>" class="filterLink" title="Show only alarms for this node location">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeNodeLocationFilter(location), true, favorite)%>" class="filterLink" title="Do not show alarms for this node location">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } %>
            </td>
            <td>
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
          <td valign="middle">
	    <% if(alarms[i].getId() > 0 ) { %>           
                <nobr>
                  <a href="event/list.htm?sortby=id&amp;acktype=unack&amp;filter=alarm%3d<%=alarms[i].getId()%>"><%=alarms[i].getCounter()%></a>
                </nobr>
            <% } else { %>
            <%=alarms[i].getCounter()%>
            <% } %>
          </td>
          <td>
            <nobr>
              <% if(alarms[i].getLastEvent() != null) { %><span title="Event <%= alarms[i].getLastEvent().getId()%>"><a href="event/detail.htm?id=<%= alarms[i].getLastEvent().getId()%>"><% } %>
                <onms:datetime date="${alarm.lastEventTime}" />
              <% if(alarms[i].getLastEvent() != null) { %></a></span><% } %>
              <a href="<%=this.makeLink(callback, parms, new AfterLastEventTimeFilter(alarms[i].getLastEventTime()), true, favorite)%>"  class="filterLink" title="Only show alarms occurring after this one">${addAfterFilter}</a>
              <a href="<%=this.makeLink(callback, parms, new BeforeLastEventTimeFilter(alarms[i].getLastEventTime()), true, favorite)%>" class="filterLink" title="Only show alarms occurring before this one">${addBeforeFilter}</a>
            </nobr>
          <c:if test="${param.display == 'long'}">
          <br />
            <nobr>
              <onms:datetime date="${alarm.firstEventTime}" />
              <a href="<%=this.makeLink(callback, parms, new AfterFirstEventTimeFilter(alarms[i].getFirstEventTime()), true, favorite)%>"  class="filterLink" title="Only show alarms occurring after this one">${addAfterFilter}</a>
              <a href="<%=this.makeLink(callback, parms, new BeforeFirstEventTimeFilter(alarms[i].getFirstEventTime()), true, favorite)%>" class="filterLink" title="Only show alarms occurring before this one">${addBeforeFilter}</a>
            </nobr>
          </td>
          <td>
            <% if (alarms[i].getDistPoller() != null && alarms[i].getLastEvent() != null) { %>
              <% String location = alarms[i].getDistPoller().getLocation(); %>
              <% Filter locationFilter = new LocationFilter(location); %>
              <span title="Event source location <%= location %>"><a href="event/detail.htm?id=<%= alarms[i].getLastEvent().getId()%>">
                <%= location %>
              </a></span>
              <% if( !parms.getFilters().contains(locationFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink(callback, parms, locationFilter, true, favorite)%>" class="filterLink" title="Show only alarms for this event source location">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink(callback, parms, new NegativeLocationFilter(location), true, favorite)%>" class="filterLink" title="Do not show alarms for this event source location">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } %>
              <% if ( parms.getAckType().equals(AcknowledgeType.ACKNOWLEDGED.toNormalizedAcknowledgeType()) ) { %>
          </td>
          <td>
			<nobr><%=alarms[i].getAckUser()%></nobr>          
            <nobr>
              <a href="<%=this.makeLink(callback, parms, new AcknowledgedByFilter(alarms[i].getAckUser()), true, favorite)%>"  class="filterLink" title="Only show alarms ack by this user">${addPositiveFilter}</a>
              <a href="<%=this.makeLink(callback, parms, new NegativeAcknowledgedByFilter(alarms[i].getAckUser()), true, favorite)%>" class="filterLink" title="Only show alarms ack by other users">${addNegativeFilter}</a>
            </nobr>
			<% }%>
          </c:if>

          <%-- if sound enabled, write java script to play sound --%>
          <%
          if (soundOn) out.write(this.alarmSound(alarms[i], session, soundOnEvent));
          %>

          </td>
          <c:if test="${param.display != 'long'}">
          <td class="divider"><%=WebSecurityUtils.sanitizeString(alarms[i].getLogMsg(), true)%></td>
          </c:if>
        </tr>
        <c:if test="${param.display == 'long'}">
        <tr class="severity-<%=alarms[i].getSeverity().getLabel()%>">
          <td colspan="7" class="divider" style="border-top: none"><%=WebSecurityUtils.sanitizeString(alarms[i].getLogMsg(), true)%></td>
        </tr> 
        </c:if>
      <% } /*end for*/%>

      </table>
			<hr />
			 <p><%=alarms.length%> alarms &nbsp;
      <% if( req.isUserInRole( Authentication.ROLE_ADMIN ) || !req.isUserInRole( Authentication.ROLE_READONLY ) ) { %>
          <input class="btn btn-default" TYPE="reset" />
          <input class="btn btn-default" TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
          <select class="form-control" name="alarmAction">
          <% if(unAckFlash){ // allow alarms to be acked and unacked %>
              <option value="acknowledge">Acknowledge Alarms</option>
              <option value="unacknowledge">Unacknowledge Alarms</option>
          <% } else { // normal behaviour %>
            <% if( parms.getAckType().equals(AcknowledgeType.UNACKNOWLEDGED.toNormalizedAcknowledgeType()) ) { %>
              <option value="acknowledge">Acknowledge Alarms</option>
            <% } else if( parms.getAckType().equals(AcknowledgeType.ACKNOWLEDGED.toNormalizedAcknowledgeType()) ) { %>
              <option value="unacknowledge">Unacknowledge Alarms</option>
            <% } %>
         <% } %>
              <option value="clear">Clear Alarms</option>
              <option value="escalate">Escalate Alarms</option>
          </select>
          <input class="btn btn-default" type="button" value="Go" onClick="submitForm(document.alarm_action_form.alarmAction.value)" />
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

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />


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

    public String makeLimitLink( FilterCallback callback, NormalizedQueryParameters params, OnmsFilterFavorite favorite, int limit) {
        NormalizedQueryParameters alteredParams = new NormalizedQueryParameters(params);
        alteredParams.setLimit(limit);
        return callback.createLink(urlBase, alteredParams, favorite);
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

    public String makeAlarmSoundLink(FilterCallback callback, NormalizedQueryParameters parms, OnmsFilterFavorite favorite, String alarmSoundStatus ) {
        NormalizedQueryParameters newParms = new NormalizedQueryParameters(parms); // clone;
        String urlStr = this.makeLink(callback, newParms, favorite)+"&alarmSoundStatus="+alarmSoundStatus;
        return urlStr;
    }

    
    public String alarmSound(OnmsAlarm onmsAlarm, HttpSession session, boolean soundOnEvent ){

        // added this section to fire when a new alarm arrives
        // This maintains the highest alarmId or eventId received in the session variable "opennms.alarmlist.HIGHEST"
        // If a new alarm is received the variable is updated
        String soundStr="<script type=\"text/javascript\"> playSound(); </script>";

        Integer highest = (Integer)session.getAttribute("opennms.alarmlist.HIGHEST");
        Integer latest = 0;
        Integer lastId = 0;

        // To have every new unique alarm trigger, use getId.  To have every new
        // alarm and every increment of Count, use last event Id.
        if(soundOnEvent){
            OnmsEvent lastEvent=onmsAlarm.getLastEvent();
            if(lastEvent!=null && lastEvent.getId()!=null) lastId = lastEvent.getId();
        } else {
            lastId=onmsAlarm.getId();
        }

        if(highest==null) {
            if (lastId!=null) {
                highest = new Integer(lastId);
                session.setAttribute("opennms.alarmlist.HIGHEST", new Integer(highest));
                return soundStr;
            }
        } else {
            latest = new Integer(lastId);
            if (latest > highest) {
                highest = latest;
                session.setAttribute("opennms.alarmlist.HIGHEST", highest);
                return soundStr;
            }
        }

        return "<!-- no sound -->";

    }

%>

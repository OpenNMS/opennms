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
// 2008 Sep 26: Use new Severity enum. - dj@opennms.org
// 2008 Aug 31: Copied from /alarm/list.jsp, merged in /alarm/list-long.jsp,
//              cleaned up imports, and added support for the display
//              parameter. - dj@opennms.org
// 2008 Aug 14: Sanitize input
// 2006 Nov 08: Added Read Only Role
// 2004 Feb 11: remove the extra 'limit' parameter in the base URL.
// 2003 Sep 04: Added a check to allow for deleted node alarms to display.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 2002 Nov 10: Removed the "http://" from UEIs and references to bluebird.
// 2002 Nov 09: Removed borders around alarms.
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
	session="true" %>
	
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.List" %>

<%@page import="org.opennms.web.Util"%>
<%@page import="org.opennms.web.WebSecurityUtils" %>
<%@page import="org.opennms.web.XssRequestWrapper" %>
<%@page import="org.opennms.web.springframework.security.Authentication" %>

<%@page import="org.opennms.web.controller.alarm.AcknowledgeAlarmController" %>
<%@page import="org.opennms.web.controller.alarm.AlarmSeverityChangeController" %>

<%@page import="org.opennms.web.filter.Filter" %>
<%@page import="org.opennms.web.alarm.Alarm" %>
<%@page import="org.opennms.web.alarm.AlarmQueryParms" %>
<%@page import="org.opennms.web.alarm.SortStyle" %>
<%@page import="org.opennms.web.alarm.AcknowledgeType" %>
<%@page import="org.opennms.web.alarm.AlarmUtil" %>
<%@page import="org.opennms.web.alarm.filter.ExactUEIFilter" %>
<%@page import="org.opennms.web.alarm.filter.NegativeExactUEIFilter" %>
<%@page import="org.opennms.web.alarm.filter.SeverityFilter" %>
<%@page import="org.opennms.web.alarm.filter.NegativeSeverityFilter" %>
<%@page import="org.opennms.web.alarm.filter.NodeFilter" %>
<%@page import="org.opennms.web.alarm.filter.NegativeNodeFilter" %>
<%@page import="org.opennms.web.alarm.filter.InterfaceFilter" %>
<%@page import="org.opennms.web.alarm.filter.NegativeInterfaceFilter" %>
<%@page import="org.opennms.web.alarm.filter.ServiceFilter" %>
<%@page import="org.opennms.web.alarm.filter.NegativeServiceFilter" %>
<%@page import="org.opennms.web.alarm.filter.AfterLastEventTimeFilter" %>
<%@page import="org.opennms.web.alarm.filter.BeforeLastEventTimeFilter" %>
<%@page import="org.opennms.web.alarm.filter.AfterFirstEventTimeFilter" %>
<%@page import="org.opennms.web.alarm.filter.BeforeFirstEventTimeFilter" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--
  This page is written to be the display (view) portion of the AlarmQueryServlet
  at the /alarm/list.htm URL.  It will not work by itself, as it requires two request
  attributes be set:
  
  1) alarms: the list of org.opennms.web.element.Alarm instances to display
  2) parms: an org.opennms.web.alarm.AlarmQueryParms object that holds all the 
     parameters used to make this query
--%>

<%
    urlBase = (String) request.getAttribute("relativeRequestPath");

    XssRequestWrapper req = new XssRequestWrapper(request);

    //required attributes
    Alarm[] alarms = (Alarm[])req.getAttribute( "alarms" );
    AlarmQueryParms parms = (AlarmQueryParms)req.getAttribute( "parms" );
    int alarmCount = (Integer) req.getAttribute("alarmCount");

    if( alarms == null || parms == null ) {
	throw new ServletException( "Missing either the alarms or parms request attribute." );
    }
    
    pageContext.setAttribute("alarms",alarms);
    pageContext.setAttribute("parms", parms);

    String action = null;

    if( ( req.isUserInRole( Authentication.ADMIN_ROLE ) || !req.isUserInRole( Authentication.READONLY_ROLE ) ) ) {     
    	if ( parms.ackType != null ) {
    		action = parms.ackType.getShortName();
    	}
    } 
    
    pageContext.setAttribute("action", action);
    pageContext.setAttribute("alarmCount", new Integer(alarmCount));
 
    //useful constant strings
    //String addPositiveFilterString = "[+]";
    //String addNegativeFilterString = "[-]";
    //String addBeforeDateFilterString = "[&gt;]";
    //String addAfterDateFilterString  = "[&lt;]"; 
    
    pageContext.setAttribute("addPositiveFilter", "[+]");
    pageContext.setAttribute("addNegativeFilter", "[-]");
    pageContext.setAttribute("addBeforeFilter", "[&gt;]");
    pageContext.setAttribute("addAfterFilter", "[&lt;]");
%>



<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Alarm List" />
  <jsp:param name="headTitle" value="List" />
  <jsp:param name="headTitle" value="Alarms" />
  <jsp:param name="breadcrumb" value="<a href= 'alarm/index.jsp' title='Alarms System Page'>Alarms</a>" />
  <jsp:param name="breadcrumb" value="List" />
</jsp:include>


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

  </script>


      <!-- menu -->
      <div id="linkbar">
      <ul>
      <li><a href="<%=this.makeLink( parms, new ArrayList<Filter>())%>" title="Remove all search constraints" >View all alarms</a></li>
      <li><a href="alarm/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a></li>
      <c:choose>
        <c:when test="${param.display == 'long'}">
      <li><a href="<%=this.makeLink(parms, "short")%>" title="Summary List of Alarms">Short Listing</a></li>
        </c:when>
        <c:otherwise>
      <li><a href="<%=this.makeLink(parms, "long")%>" title="Detailed List of Alarms">Long Listing</a></li>
        </c:otherwise>
      </c:choose>
      <li><a href="javascript: void window.open('<%=Util.calculateUrlBase(req)%>/alarm/severity.jsp','', 'fullscreen=no,toolbar=no,status=no,menubar=no,scrollbars=no,resizable=yes,directories=no,location=no,width=525,height=158')" title="Open a window explaining the alarm severities">Severity Legend</a></li>
      <li>
      <% if( parms.ackType == AcknowledgeType.UNACKNOWLEDGED ) { %> 
        <a href="javascript: void document.acknowledge_by_filter_form.submit()" onclick="return confirm('Are you sure you want to acknowledge all alarms in the current search including those not shown on your screen?  (<%=alarmCount%> total alarms)')" title="Acknowledge all alarms that match the current search constraints, even those not shown on the screen">Acknowledge entire search</a>
      <% } else { %>
        <a href="javascript: void document.acknowledge_by_filter_form.submit()" onclick="return confirm('Are you sure you want to unacknowledge all alarms in the current search including those not shown on your screen)?  (<%=alarmCount%> total alarms)')" title="Unacknowledge all alarms that match the current search constraints, even those not shown on the screen">Unacknowledge entire search</a>               
      <% } %>
      </li>
      </ul>
      </div>
      <!-- end menu -->      

      <!-- hidden form for acknowledging the result set --> 
      <% if( req.isUserInRole( Authentication.ADMIN_ROLE ) || !req.isUserInRole( Authentication.READONLY_ROLE ) ) { %>
          <form method="post" action="alarm/acknowledgeByFilter" name="acknowledge_by_filter_form">    
            <input type="hidden" name="redirectParms" value="<%=req.getQueryString()%>" />
            <input type="hidden" name="actionCode" value="<%=action%>" />
            <%=Util.makeHiddenTags(req)%>
          </form>      
      <% } %>



            <jsp:include page="/includes/alarm-querypanel.jsp" flush="false" />
          
            <% if( alarmCount > 0 ) { %>
              <% String baseUrl = this.makeLink(parms); %>
              <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
                <jsp:param name="count"    value="<%=alarmCount%>" />
                <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                <jsp:param name="limit"    value="<%=parms.limit%>"      />
                <jsp:param name="multiple" value="<%=parms.multiple%>"   />
              </jsp:include>
            <% } %>          


            <% if( parms.filters.size() > 0 || parms.ackType == AcknowledgeType.UNACKNOWLEDGED || parms.ackType == AcknowledgeType.ACKNOWLEDGED ) { %>
              <% int length = parms.filters.size(); %>
              <p>Search constraints:
                  <% if( parms.ackType == AcknowledgeType.UNACKNOWLEDGED ) { %>
                    <span class="filter">alarm is outstanding <a href="<%=this.makeLink(parms, AcknowledgeType.ACKNOWLEDGED)%>" title="Show acknowledged alarms">[-]</a></span>
                  <% } else if( parms.ackType == AcknowledgeType.ACKNOWLEDGED ) { %>
                    <span class="filter">alarm is acknowledged <a href="<%=this.makeLink(parms, AcknowledgeType.UNACKNOWLEDGED)%>" title="Show outstanding alarms">[-]</a></span>
                  <% } %>            
                
                  <% for( int i=0; i < length; i++ ) { %>
                    <% Filter filter = parms.filters.get(i); %>
                    &nbsp; <span class="filter"><%=WebSecurityUtils.sanitizeString(filter.getTextDescription())%> <a href="<%=this.makeLink( parms, filter, false)%>" title="Remove filter">[-]</a></span>
                  <% } %>
              </p>           
            <% } %>

      <% if( req.isUserInRole( Authentication.ADMIN_ROLE ) || !req.isUserInRole( Authentication.READONLY_ROLE ) ) { %>
          <form action="alarm/acknowledge" method="post" name="alarm_action_form">
          <input type="hidden" name="redirectParms" value="<%=req.getQueryString()%>" />
          <input type="hidden" name="actionCode" value="<%=action%>" />
          <%=Util.makeHiddenTags(req)%>
      <% } %>
			<jsp:include page="/includes/key.jsp" flush="false" />
      <table>
				<thead>
					<tr>
                                             <% if( req.isUserInRole( Authentication.ADMIN_ROLE ) || !req.isUserInRole( Authentication.READONLY_ROLE ) ) { %>
						<% if ( parms.ackType == AcknowledgeType.UNACKNOWLEDGED ) { %>
						<th width="1%">Ack</th>
						<% } else { %>
						<th width="1%">UnAck</th>
						<% } %>
                    <% } else { %>
                        <th width="1%">&nbsp;</th>
                    <% } %>



			<th width="8%">
              <%=this.makeSortLink( parms, SortStyle.ID,        SortStyle.REVERSE_ID,        "id",        "ID" )%>
              <br />
              <%=this.makeSortLink( parms, SortStyle.SEVERITY,  SortStyle.REVERSE_SEVERITY,  "severity",  "Severity"  )%>
            </th>
			<th width="19%">
              <%=this.makeSortLink( parms, SortStyle.NODE,      SortStyle.REVERSE_NODE,      "node",      "Node"      )%>
              <c:if test="${param.display == 'long'}">
              <br />
              <%=this.makeSortLink( parms, SortStyle.INTERFACE, SortStyle.REVERSE_INTERFACE, "interface", "Interface" )%>
              <br />
              <%=this.makeSortLink( parms, SortStyle.SERVICE,   SortStyle.REVERSE_SERVICE,   "service",   "Service"   )%>
              </c:if>
            </th>
			<th width="3%">
              <%=this.makeSortLink( parms, SortStyle.COUNT,  SortStyle.REVERSE_COUNT,  "count",  "Count"  )%>
            </th>
			<th width="20%">
              <%=this.makeSortLink( parms, SortStyle.LASTEVENTTIME,  SortStyle.REVERSE_LASTEVENTTIME,  "lasteventtime",  "Last Event Time"  )%>
              <c:if test="${param.display == 'long'}">
              <br />
              <%=this.makeSortLink( parms, SortStyle.FIRSTEVENTTIME,  SortStyle.REVERSE_FIRSTEVENTTIME,  "firsteventtime",  "First Event Time"  )%>
              </c:if>
            </th>
			<th width="48%">Log Msg</th>
		</tr>
	</thead>

      <% for( int i=0; i < alarms.length; i++ ) { 
      	Alarm alarm = alarms[i];
      	pageContext.setAttribute("alarm", alarm);
      %> 

        <tr class="<%=alarms[i].getSeverity().getLabel()%>">
          <% if( req.isUserInRole( Authentication.ADMIN_ROLE ) || !req.isUserInRole( Authentication.READONLY_ROLE ) ) { %>
              <td class="divider" valign="middle" rowspan="1">
                <nobr>
                  <input type="checkbox" name="alarm" value="<%=alarms[i].getId()%>" /> 
                </nobr>
          <% } else { %>
            <td valign="middle" rowspan="1" class="divider">&nbsp;
          <% } %>
          </td>

          
          <td class="divider bright" valign="middle" rowspan="1">
            
            <a href="alarm/detail.jsp?id=<%=alarms[i].getId()%>"><%=alarms[i].getId()%></a>
          <c:if test="${param.display == 'long'}">
            <% if(alarms[i].getUei() != null) { %>
              <% Filter exactUEIFilter = new ExactUEIFilter(alarms[i].getUei()); %>
                <br />UEI
              <% if( !parms.filters.contains( exactUEIFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, exactUEIFilter, true)%>" class="filterLink" title="Show only events with this UEI">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink( parms, new NegativeExactUEIFilter(alarms[i].getUei()), true)%>" class="filterLink" title="Do not show events for this UEI">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
            <% Filter severityFilter = new SeverityFilter(alarms[i].getSeverity()); %>      
            <% if( !parms.filters.contains( severityFilter )) { %>
		<br />Sev.
              <nobr>
                <a href="<%=this.makeLink( parms, severityFilter, true)%>" class="filterLink" title="Show only alarms with this severity">${addPositiveFilter}</a>
                <a href="<%=this.makeLink( parms, new NegativeSeverityFilter(alarms[i].getSeverity()), true)%>" class="filterLink" title="Do not show alarms with this severity">${addNegativeFilter}</a>

              </nobr>
            <% } %>
          </c:if>
          </td>
          <td class="divider">
	    <% if(alarms[i].getNodeId() != 0 && alarms[i].getNodeLabel()!= null ) { %>
              <% Filter nodeFilter = new NodeFilter(alarms[i].getNodeId()); %>             
              <% String[] labels = this.getNodeLabels( alarms[i].getNodeLabel() ); %>
              <a href="element/node.jsp?node=<%=alarms[i].getNodeId()%>" title="<%=labels[1]%>"><%=labels[0]%></a>
                    
              <% if( !parms.filters.contains(nodeFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, nodeFilter, true)%>" class="filterLink" title="Show only alarms on this node">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink( parms, new NegativeNodeFilter(alarms[i].getNodeId()), true)%>" class="filterLink" title="Do not show alarms for this node">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          <c:if test="${param.display == 'long'}">
		<br />
            <% if(alarms[i].getIpAddress() != null ) { %>
              <% Filter intfFilter = new InterfaceFilter(alarms[i].getIpAddress()); %>
              <% if( alarms[i].getNodeId() != 0 ) { %>
                 <a href="element/interface.jsp?node=<%=alarms[i].getNodeId()%>&intf=<%=alarms[i].getIpAddress()%>" title="More info on this interface"><%=alarms[i].getIpAddress()%></a>
              <% } else { %>
                 <%=alarms[i].getIpAddress()%>
              <% } %>
              <% if( !parms.filters.contains(intfFilter) ) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, intfFilter, true)%>" class="filterLink" title="Show only alarms on this IP address">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink( parms, new NegativeInterfaceFilter(alarms[i].getIpAddress()), true)%>" class="filterLink" title="Do not show alarms for this interface">${addNegativeFilter}</a>
                </nobr>
              <% } %>
            <% } else { %>
              &nbsp;
            <% } %>
          <br />
            <% if(alarms[i].getServiceName() != null && alarms[i].getServiceName() != "") { %>
              <% Filter serviceFilter = new ServiceFilter(alarms[i].getServiceId()); %>
              <% if( alarms[i].getNodeId() != 0 && alarms[i].getIpAddress() != null ) { %>
                <a href="element/service.jsp?node=<%=alarms[i].getNodeId()%>&intf=<%=alarms[i].getIpAddress()%>&service=<%=alarms[i].getServiceId()%>" title="More info on this service"><%=alarms[i].getServiceName()%></a>
              <% } else { %>
                <%=alarms[i].getServiceName()%>
              <% } %>
              <% if( !parms.filters.contains( serviceFilter )) { %>
                <nobr>
                  <a href="<%=this.makeLink( parms, serviceFilter, true)%>" class="filterLink" title="Show only alarms with this service type">${addPositiveFilter}</a>
                  <a href="<%=this.makeLink( parms, new NegativeServiceFilter(alarms[i].getServiceId()), true)%>" class="filterLink" title="Do not show alarms for this service">${addNegativeFilter}</a>
                </nobr>
              <% } %>                            
            <% } %>
            </c:if>
          </td>          
          <td class="divider" valign="middle" rowspan="1" >
	    <% if(alarms[i].getId() > 0 ) { %>           
                <nobr>
                  <a href="event/list.htm?sortby=id&acktype=unack&filter=alarm=<%=alarms[i].getId()%>"><%=alarms[i].getCount()%></a>
                </nobr>
            <% } else { %>
            <%=alarms[i].getCount()%>
            <% } %>
          </td>
          <td class="divider">
            <nobr><span title="Event <%= alarms[i].getLastEventID() %>"><a href="event/detail.jsp?id=<%= alarms[i].getLastEventID() %>"><fmt:formatDate value="${alarm.lastEventTime}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${alarm.lastEventTime}" type="time" pattern="HH:mm:ss"/></a></span></nobr>
            <nobr>
              <a href="<%=this.makeLink( parms, new AfterLastEventTimeFilter(alarms[i].getLastEventTime()), true)%>"  class="filterLink" title="Only show alarms occurring after this one">${addAfterFilter}</a>            
              <a href="<%=this.makeLink( parms, new BeforeLastEventTimeFilter(alarms[i].getLastEventTime()), true)%>" class="filterLink" title="Only show alarms occurring before this one">${addBeforeFilter}</a>
            </nobr>
          <c:if test="${param.display == 'long'}">
          <br />
            <nobr><fmt:formatDate value="${alarm.firstEventTime}" type="date" dateStyle="short"/>&nbsp;<fmt:formatDate value="${alarm.firstEventTime}" type="time" pattern="HH:mm:ss"/></nobr>
            <nobr>
              <a href="<%=this.makeLink( parms, new AfterFirstEventTimeFilter(alarms[i].getFirstEventTime()), true)%>"  class="filterLink" title="Only show alarms occurring after this one">${addAfterFilter}</a>            
              <a href="<%=this.makeLink( parms, new BeforeFirstEventTimeFilter(alarms[i].getFirstEventTime()), true)%>" class="filterLink" title="Only show alarms occurring before this one">${addBeforeFilter}</a>
            </nobr>
          </c:if>
          </td>
          <td class="divider"><%=alarms[i].getLogMessage()%></td>
       
      <% } /*end for*/%>

      </table>
			<hr />
			 <p><%=alarms.length%> alarms &nbsp;
      <% if( req.isUserInRole( Authentication.ADMIN_ROLE ) || !req.isUserInRole( Authentication.READONLY_ROLE ) ) { %>
          <input TYPE="reset" />
          <input TYPE="button" VALUE="Select All" onClick="checkAllCheckboxes()"/>
          <select name="alarmAction">
        <% if( parms.ackType == AcknowledgeType.UNACKNOWLEDGED ) { %>
          <option value="acknowledge">Acknowledge Alarms</option>
        <% } else if( parms.ackType == AcknowledgeType.ACKNOWLEDGED ) { %>
          <option value="unacknowledge">Unacknowledge Alarms</option>
        <% } %>
          <option value="clear">Clear Alarms</option>
          <option value="escalate">Escalate Alarms</option>
          </select>
          <input type="button" value="Go" onClick="submitForm(document.alarm_action_form.alarmAction.value)" />
      <% } %>
        </p>
      </form>

      <%--<br/>
      <% if(req.isUserInRole(Authentication.ADMIN_ROLE)) { %>
        <a HREF="admin/alarms.jsp" title="Acknowledge or Unacknowledge All Alarms">[Acknowledge or Unacknowledge All Alarms]</a>
      <% } %>--%>

 <!-- id="eventlist" -->

            <% if( alarmCount > 0 ) { %>
              <% String baseUrl = this.makeLink(parms); %>
              <jsp:include page="/includes/resultsIndex.jsp" flush="false" >
                <jsp:param name="count"    value="<%=alarmCount%>" />
                <jsp:param name="baseurl"  value="<%=baseUrl%>"    />
                <jsp:param name="limit"    value="<%=parms.limit%>"      />
                <jsp:param name="multiple" value="<%=parms.multiple%>"   />
              </jsp:include>
            <% } %>


<jsp:include page="/includes/bookmark.jsp" flush="false" />
<jsp:include page="/includes/footer.jsp" flush="false" />


<%!
    String urlBase;

    protected String makeSortLink( AlarmQueryParms parms, SortStyle style, SortStyle revStyle, String sortString, String title ) {
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
                String filterString = AlarmUtil.getFilterString(filters.get(i));
                buffer.append(Util.encode(filterString));
            }
        }      
    
        return( buffer.toString() );
    }
    
    public String makeLink( SortStyle sortStyle, AcknowledgeType ackType, List<Filter> filters, int limit, String display ) {
      StringBuffer buffer = new StringBuffer( this.urlBase );
      buffer.append( "?sortby=" );
      buffer.append( sortStyle.getShortName() );
      buffer.append( "&acktype=" );
      buffer.append( ackType.getShortName() );
      if (limit > 0) {
          buffer.append( "&limit=" ).append(limit);
      }
      if (display != null) {
          buffer.append( "&display=" ).append(display);
      }
      buffer.append( this.getFiltersAsString(filters) );

      return( buffer.toString() );
    }

    public String eventMakeLink( AlarmQueryParms parms, Filter filter, boolean add ) {
      List<Filter> filters = new ArrayList<Filter>( parms.filters );
      if( add ) {
        filters.add( filter );
      }
      else {
        filters.remove( filter );
      }
      StringBuffer buffer = new StringBuffer( "event/list.htm" );
      buffer.append( "?sortby=" );
      buffer.append( parms.sortStyle.getShortName() );
      buffer.append( "&acktype=" );
      buffer.append( parms.ackType.getShortName() );
      buffer.append( this.getFiltersAsString(filters) );

      return( buffer.toString() );
    }


    public String makeLink( AlarmQueryParms parms ) {
      return( this.makeLink( parms.sortStyle, parms.ackType, parms.filters, parms.limit, parms.display) );
    }

    public String makeLink(AlarmQueryParms parms, String display) {
      return makeLink(parms.sortStyle, parms.ackType, parms.filters, parms.limit, display);
    }

    public String makeLink( AlarmQueryParms parms, SortStyle sortStyle ) {
      return( this.makeLink( sortStyle, parms.ackType, parms.filters, parms.limit, parms.display) );
    }


    public String makeLink( AlarmQueryParms parms, AcknowledgeType ackType ) {
      return( this.makeLink( parms.sortStyle, ackType, parms.filters, parms.limit, parms.display) );
    }


    public String makeLink( AlarmQueryParms parms, List<Filter> filters ) {
      return( this.makeLink( parms.sortStyle, parms.ackType, filters, parms.limit, parms.display) );
    }

    public String makeLink( AlarmQueryParms parms, Filter filter, boolean add ) {
      List<Filter> newList = new ArrayList<Filter>( parms.filters );
      if( add ) {
        newList.add( filter );
      }
      else {
        newList.remove( filter );
      }

      return( this.makeLink( parms.sortStyle, parms.ackType, newList, parms.limit, parms.display ));
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



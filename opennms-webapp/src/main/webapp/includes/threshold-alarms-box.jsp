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
// 2009 Jul 22: Display important events on start page -- r.trommer@open-factory.org
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
        import="org.opennms.core.spring.BeanUtils,
                org.opennms.netmgt.dao.api.AlarmRepository,
                org.opennms.netmgt.model.OnmsAlarm,
                org.opennms.web.alarm.AcknowledgeType,
                org.opennms.web.alarm.AlarmUtil,
                org.opennms.web.alarm.SortStyle,
                org.opennms.web.alarm.filter.AlarmCriteria,
                org.opennms.web.alarm.filter.PartialUEIFilter"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<%
    int throttle = 10;
    String header = "Threshold Alarms (newest " + throttle + ")";
    String ueifilter = "uei.opennms.org/threshold/";
    org.opennms.web.filter.Filter[] filters = new org.opennms.web.filter.Filter[1];
    filters[0] = new PartialUEIFilter(ueifilter);
    int offset = 0;

    //get alarms
    OnmsAlarm[] alarms = new OnmsAlarm[0];
    AlarmCriteria criteria = new AlarmCriteria(filters, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, throttle, 0);
    AlarmRepository repository = BeanUtils.getBean("daoContext", "alarmRepository", AlarmRepository.class);
    if (repository != null) {
        alarms = repository.getMatchingAlarms(AlarmUtil.getOnmsCriteria(criteria));
    }
%>

<c:url var="headingLink" value="alarm/list.htm?sortby=id&acktype=unack&limit=20&display=long&filter=partialUei=uei.opennms.org/threshold"/>
<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title"><a href="${headingLink}">Nodes with Threshold Alarms</a></h3>
    </div>
        <%
        if (alarms.length == 0) {
        %>
        <div class="panel-body">
            <p class="noBottomMargin">
                There are no pending threshold alarms.
            </p>
        </div>
        <%
        } else { %>
        <%
            for (int i = 0; i < alarms.length; i++) {
                OnmsAlarm alarm = alarms[i];
                if (!alarm.getUei().contains("Rearmed")) {
        %>
                    <table class="table table-condensed severity">
                        <tr class="severity-<%=alarm.getSeverity().getLabel()%> nodivider">
                            <% if (alarm.getUei().contains("relativeChangeExceeded") && !alarm.getUei().contains("Rearmed")) { %>
                            <td class="bright"><img src="images/thresh_change.png" />&nbsp;<a href="alarm/detail.htm?id=<%=alarm.getId()%>"title="<c:out value="<%=alarm.getLogMsg()%>"/>"><%=alarm.getNodeLabel()%></a></td>
                            <% } else if (alarm.getUei().contains("absoluteChangeExceeded") && !alarm.getUei().contains("Rearmed")) { %>
                            <td class="bright"><img src="images/thresh_change.png" />&nbsp;<a href="alarm/detail.htm?id=<%=alarm.getId()%>"title="<c:out value="<%=alarm.getLogMsg()%>"/>"><%=alarm.getNodeLabel()%></a></td>
                            <% } else if (alarm.getUei().contains("highThresholdExceeded") && !alarm.getUei().contains("Rearmed")) { %>
                            <td class="bright"><img src="images/thresh_high.png" />&nbsp;<a href="alarm/detail.htm?id=<%=alarm.getId()%>"title="<c:out value="<%=alarm.getLogMsg()%>"/>"><%=alarm.getNodeLabel()%></a></td>
                            <% } else if (alarm.getUei().contains("lowThresholdExceeded") && !alarm.getUei().contains("Rearmed")) { %>
                            <td class="bright"><img src="images/thresh_low.png" />&nbsp;<a href="alarm/detail.htm?id=<%=alarm.getId()%>"title="<c:out value="<%=alarm.getLogMsg()%>"/>"><%=alarm.getNodeLabel()%></a></td>
                            <% } else { %>
                            <td class="bright"><img src="images/thresh_unknown.png" />&nbsp;<a href="alarm/detail.htm?id=<%=alarm.getId()%>"title="<c:out value="<%=alarm.getLogMsg()%>"/>"><%=alarm.getNodeLabel()%></a></td>
                            <% } %>
                        </tr>
                    </table>
        <%
                } // if alarms do not contain "Rearmed"
            } // for i = 0; i < alarms.length
        } // if alarms.length == 0
        %>
    </table>
</div>

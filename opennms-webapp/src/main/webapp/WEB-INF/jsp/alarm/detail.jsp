<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
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

<%@page language="java"
        contentType="text/html"
        session="true"
        import="java.util.List,
        org.opennms.core.resource.Vault,
        org.opennms.core.utils.InetAddressUtils,
        org.opennms.core.utils.WebSecurityUtils,
        org.opennms.web.controller.alarm.*,
        org.opennms.web.alarm.*,
        org.opennms.web.servlet.XssRequestWrapper,
        org.opennms.netmgt.model.OnmsAcknowledgment,
        org.opennms.netmgt.model.OnmsAlarm,
        org.opennms.netmgt.model.OnmsSeverity,
        org.opennms.netmgt.model.TroubleTicketState,
        org.opennms.web.api.Authentication,
        org.apache.commons.configuration.Configuration,
        org.apache.commons.configuration.ConfigurationException,
        org.apache.commons.configuration.PropertiesConfiguration"
%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags/form" prefix="form" %>

<%!
    public String alarmTicketLink(OnmsAlarm alarm) {
        String template = System.getProperty("opennms.alarmTroubleTicketLinkTemplate");
        if (template == null) {
            return alarm.getTTicketId();
        } else {
            template = template.replaceAll("\\$\\{id\\}", alarm.getTTicketId());
            return "<a href=\"" + template + "\">" + alarm.getTTicketId() + "</a>";
        }
    }

%>

<%
    XssRequestWrapper req = new XssRequestWrapper(request);
    OnmsAlarm alarm = (OnmsAlarm) request.getAttribute("alarm");
    final String alarmId = (String)request.getAttribute("alarmId");

    if (alarm == null) {
        throw new AlarmIdNotFoundException("Missing alarm request attribute.", alarmId);
    }

    pageContext.setAttribute("alarm", alarm);

    String action = null;
    String ackButtonName = null;
    boolean showEscalate = false;
    boolean showClear = false;

    if (alarm.getAckTime() == null) {
        ackButtonName = "Acknowledge this alarm";
        action = AcknowledgeType.ACKNOWLEDGED.getShortName();
    } else {
        ackButtonName = "Unacknowledge this alarm";
        action = AcknowledgeType.UNACKNOWLEDGED.getShortName();
    }

    String escalateAction = AlarmSeverityChangeController.ESCALATE_ACTION;
    String clearAction = AlarmSeverityChangeController.CLEAR_ACTION;
    if (alarm.getSeverity() == OnmsSeverity.CLEARED || (alarm.getSeverity().isGreaterThan(OnmsSeverity.NORMAL) && alarm.getSeverity().isLessThan(OnmsSeverity.CRITICAL))) {
        showEscalate = true;
    }
    if (alarm.getSeverity().isGreaterThanOrEqual(OnmsSeverity.NORMAL) && alarm.getSeverity().isLessThanOrEqual(OnmsSeverity.CRITICAL)) {
        showClear = true;
    }
    
    List<OnmsAcknowledgment> acks = (List<OnmsAcknowledgment>) request.getAttribute("acknowledgments");

	String eventLocation = null;
	String nodeLocation = null;

	if (alarm.getLastEvent() != null && alarm.getLastEvent().getDistPoller() != null) {
	    eventLocation = alarm.getLastEvent().getDistPoller().getLocation();
	}
	if (alarm.getNode() != null && alarm.getNode().getLocation() != null) {
	    nodeLocation = alarm.getNode().getLocation().getLocationName();
	}
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
    <jsp:param name="title" value="Alarm Detail" />
    <jsp:param name="headTitle" value="Detail" />
    <jsp:param name="headTitle" value="Alarms" />
    <jsp:param name="breadcrumb" value="<a href='alarm/index.htm'>Alarms</a>" />
    <jsp:param name="breadcrumb" value='<%="Alarm " + alarm.getId()%>' />
</jsp:include>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Alarm <%=alarm.getId()%></h3>
  </div>

<table class="table table-condensed severity">
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%>">
        <th class="col-md-1">Severity</th>
        <td class="col-md3 bright"><%=alarm.getSeverity().getLabel()%></td>
        <th class="col-md-1">Node</th>
        <td class="col-md-3">
            <% if (alarm.getNodeId() != null && alarm.getNodeId() > 0) {%>
            <c:url var="nodeLink" value="element/node.jsp">
                <c:param name="node" value="<%=String.valueOf(alarm.getNodeId())%>"/>
            </c:url>
            <a href="${nodeLink}"><c:out value="<%=alarm.getNodeLabel()%>"/></a>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%>">
        <th class="col-md-1">Last&nbsp;Event</th>
        <td class="col-md-3"><span title="Event <%= alarm.getLastEvent().getId()%>"><a href="event/detail.jsp?id=<%= alarm.getLastEvent().getId()%>"><fmt:formatDate value="<%=alarm.getLastEventTime()%>" type="BOTH" /></a></span></td>
        <th class="col-md-1">Interface</th>
        <td class="col-md-3">
            <% if (alarm.getIpAddr() != null) {%>
            <% if (alarm.getNodeId() != null && alarm.getNodeId() > 0) {%>
            <c:url var="interfaceLink" value="element/interface.jsp">
                <c:param name="node" value="<%=String.valueOf(alarm.getNodeId())%>"/>
                <c:param name="intf" value="<%=InetAddressUtils.str(alarm.getIpAddr())%>"/>
            </c:url>
            <a href="${interfaceLink}"><%=InetAddressUtils.str(alarm.getIpAddr())%></a>
            <% } else {%>
            <%=InetAddressUtils.str(alarm.getIpAddr())%>
            <% }%>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%>">
        <th class="col-md-1">First&nbsp;Event</th>
        <td class="col-md-3"><fmt:formatDate value="<%=alarm.getFirstEventTime()%>" type="BOTH" /></td>
        <th class="col-md-1">Service</th>
        <td class="col-md-3">
            <% if (alarm.getServiceType() != null) {%>
            <% if (alarm.getIpAddr() != null && alarm.getNodeId() > 0) {%>
            <c:url var="serviceLink" value="element/service.jsp">
                <c:param name="node" value="<%=String.valueOf(alarm.getNodeId())%>"/>
                <c:param name="intf" value="<%=InetAddressUtils.str(alarm.getIpAddr())%>"/>
                <c:param name="service" value="<%=String.valueOf(alarm.getServiceType().getId())%>"/>
            </c:url>
            <a href="${serviceLink}"><c:out value="<%=alarm.getServiceType().getName()%>"/></a>
            <% } else {%>
            <c:out value="<%=alarm.getServiceType().getName()%>"/>
            <% }%>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr> 
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%>">
        <th class="col-md-1">Event Source Location</th>
        <td class="col-md-3"><%= eventLocation == null? "&nbsp;" : eventLocation %>
        <th class="col-md-1">Node Location</th>
        <td class="col-md-3"><%= nodeLocation == null? "&nbsp;" : nodeLocation %>
    </tr>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%>">
        <th class="col-md-1">Count</th>
        <td class="col-md-3"><%=alarm.getCounter()%></td>
        <th class="col-md-1">UEI</th>
        <td class="col-md-3">
            <% if (alarm.getUei() != null) {%>
            <%=alarm.getUei()%>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%>">
        <th class="col-md-1">Ticket&nbsp;ID</th>
        <td class="col-md-3"><% if (alarm.getTTicketId() == null) {%>
            &nbsp;
            <% } else {%>
            <%= alarmTicketLink(alarm)%> 
            <% }%>
        </td>
        <th class="col-md-1">Ticket&nbsp;State</th>
        <td class="col-md-3"><% if (alarm.getTTicketState() == null) {%>
            &nbsp;
            <% } else {%>
            <%= alarm.getTTicketState()%> 
            <% }%>
        </td>
    </tr>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%>">
        <th class="col-md-1">Reduction&nbsp;Key</th>
        <td class="col-md-11" colspan="3">
            <% if (alarm.getReductionKey() != null) {%>
            <%=alarm.getReductionKey()%>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr>
</table>
</div>

<div class="panel panel-default severity">
  <div class="panel-heading">
    <h3 class="panel-title">Log&nbsp;Message</h3>
  </div>
  <div class="panel-body severity-<%= alarm.getSeverity().getLabel().toLowerCase() %>">
    <%=WebSecurityUtils.sanitizeString(alarm.getLogMsg(), true)%>
  </div>
</div>

<div class="panel panel-default severity">
  <div class="panel-heading">
    <h3 class="panel-title">Description</h3>
  </div>
  <div class="panel-body severity-<%= alarm.getSeverity().getLabel().toLowerCase() %>">
    <%=WebSecurityUtils.sanitizeString(alarm.getDescription(), true)%>
  </div>
</div>

<% if (acks != null && acks.size() > 0) {%>
<div class="panel panel-default severity">
  <div class="panel-heading">
    <h3 class="panel-title">Acknowledgements</h3>
  </div>
<table class="table table-condensed severity">
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%>">
        <th>Acknowledged&nbsp;By</th>
        <th>Acknowledged&nbsp;Type</th>
        <th>Time&nbsp;Acknowledged</th>
    </tr>
    <% for (OnmsAcknowledgment ack : acks) {%>
    <tr class="severity-<%=alarm.getSeverity().getLabel().toLowerCase()%>">
        <td><%=ack.getAckUser()%></td>
        <td><%=ack.getAckAction()%></td>
        <td><fmt:formatDate value="<%=ack.getAckTime()%>" type="BOTH" /></td>
    </tr>
    <% }%>
</table>
</div>
<% }%>

<div class="row">
<div class="col-md-6">
<div class="panel panel-default severity">
  <div class="panel-heading">
    <h3 class="panel-title">Sticky&nbsp;Memo</h3>
  </div>
  <div class="panel-body severity-<%= alarm.getSeverity().getLabel().toLowerCase() %>">
	         <form class="form" method="post" action="alarm/saveStickyMemo.htm">
				<textarea style="width:100%" name="stickyMemoBody" ><%=(alarm.getStickyMemo() != null && alarm.getStickyMemo().getBody() != null) ? alarm.getStickyMemo().getBody() : ""%></textarea>
				<input type="hidden" name="alarmId" value="<%=alarm.getId() %>"/>
                <div class="btn-group btn-group-sm">
                <form:input class="btn btn-default" type="submit" value="Save" />
                <form:input class="btn btn-default" type="button" value="Delete" onclick="document.getElementById('deleteStickyForm').submit();"/>
                </div>
	         </form>
	         <form id="deleteStickyForm" method="post" action="alarm/removeStickyMemo.htm">
				<input type="hidden" name="alarmId" value="<%=alarm.getId() %>"/>
	         </form>
	         <br/>
        <% if (alarm.getStickyMemo() != null) { %>
	         <div class="row">
        <div class="col-md-4"><strong>Author:</strong><br/><%=(alarm.getStickyMemo().getAuthor() != null) ? alarm.getStickyMemo().getAuthor() : ""%></div>
        <div class="col-md-4"><strong>Updated:</strong><br/>
       		<span style="white-space:nowrap;"><fmt:formatDate value="<%=alarm.getStickyMemo().getUpdated()%>" type="BOTH" /></span>
        </div>
        <div class="col-md-4"><strong>Created:</strong><br/>
       		<span style="white-space:nowrap;"><fmt:formatDate value="<%=alarm.getStickyMemo().getCreated()%>" type="BOTH" /></span>
        </div>
        </div>
        <% } else { %>
        <div class="row">
        <div class="col-md-12">&nbsp;</div>
        <div class="col-md-12">&nbsp;</div>
        </div>
        <% } %>
</div>
</div>
</div>

<div class="col-md-6">
<div class="panel panel-default severity">
  <div class="panel-heading">
    <h3 class="panel-title">Journal&nbsp;Memo</h3>
  </div>
  <div class="panel-body severity-<%= alarm.getSeverity().getLabel().toLowerCase() %>">
            <form class="form" method="post" action="alarm/saveJournalMemo.htm">
                <textarea style="width:100%" name="journalMemoBody" ><%=(alarm.getReductionKeyMemo() != null && alarm.getReductionKeyMemo().getBody() != null) ? alarm.getReductionKeyMemo().getBody() : ""%></textarea>
                <input type="hidden" name="alarmId" value="<%=alarm.getId()%>"/>
                <div class="btn-group btn-group-sm">
                <form:input class="btn btn-default" type="submit" value="Save" />
                <form:input class="btn btn-default" type="button" value="Delete" onclick="document.getElementById('deleteJournalForm').submit();"/>
                </div>
            </form>
            <form id="deleteJournalForm" method="post" action="alarm/removeJournalMemo.htm">
                <input type="hidden" name="alarmId" value="<%=alarm.getId()%>"/>
            </form>
	         <br/>
        <% if (alarm.getReductionKeyMemo() != null) { %>
        <div class="row">
        <div class="col-md-4"><strong>Author:</strong><br/><%=(alarm.getReductionKeyMemo().getAuthor() != null) ? alarm.getReductionKeyMemo().getAuthor() : ""%></div>
        <div class="col-md-4"><strong>Updated:</strong><br/>
       		<span style="white-space:nowrap;"><fmt:formatDate value="<%=alarm.getReductionKeyMemo().getUpdated()%>" type="BOTH" /></span>
        </div>
        <div class="col-md-4"><strong>Created:</strong><br/>
       		<span style="white-space:nowrap;"><fmt:formatDate value="<%=alarm.getReductionKeyMemo().getCreated()%>" type="BOTH" /></span>
        </div>
        </div>
        <% } else { %>
        <div class="row">
        <div class="col-md-12">&nbsp;</div>
        <div class="col-md-12">&nbsp;</div>
        </div>
        <% } %>

</div>
</div>
</div>

</div>

<div class="panel panel-default severity">
  <div class="panel-heading">
    <h3 class="panel-title">Operator&nbsp;Instructions</h3>
  </div>
  <div class="panel-body severity-<%= alarm.getSeverity().getLabel().toLowerCase() %>">
            <%if (alarm.getOperInstruct() == null) {%>
            No instructions available.
            <% } else {%>
            <%=alarm.getOperInstruct()%>
            <% }%>
  </div>
</div>

<% if (request.isUserInRole(Authentication.ROLE_ADMIN) || !request.isUserInRole(Authentication.ROLE_READONLY)) {%>

<div class="row">
<div class="col-md-6">

                <form class="form-inline" method="post" action="alarm/acknowledge">
                    <input type="hidden" name="actionCode" value="<%=action%>" />
                    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
                    <input type="hidden" name="redirect" value="<%= "detail.htm" + "?" + request.getQueryString()%>" />
                    <input class="form-control btn btn-default" type="submit" value="<%=ackButtonName%>" />
                </form>

        <%if (showEscalate || showClear) {%>

                <form class="form-inline" method="post" action="alarm/changeSeverity">
                    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
                    <input type="hidden" name="redirect" value="<%= "detail.htm" + "?" + request.getQueryString()%>" />	  
                    <select class="form-control" name="actionCode">
                        <%if (showEscalate) {%>
                        <option value="<%=escalateAction%>">Escalate this alarm</option>
                        <% }%>
                        <%if (showClear) {%>
                        <option value="<%=clearAction%>">Clear this alarm</option>
                        <% }%>
                    </select>
                    <input class="form-control btn btn-default" type="submit" value="Go"/>
                </form>

        <% } // showEscalate || showClear %>

<% if ("true".equalsIgnoreCase(Vault.getProperty("opennms.alarmTroubleTicketEnabled"))) {%>

</div>
<div class="col-md-6">

<form class="form-inline" method="post" action="alarm/ticket/create.htm">
    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
    <input type="hidden" name="redirect" value="<%="/alarm/detail.htm" + "?" + request.getQueryString()%>" />
    <form:input class="form-control btn btn-default" type="submit" value="Create Ticket" disabled="<%=((alarm.getTTicketState() != null) && (alarm.getTTicketState() != TroubleTicketState.CREATE_FAILED)) ? true : false %>" />
    <%-- Remedy Specific TroubleTicket - Start --%>
    <% if ("org.opennms.netmgt.ticketer.remedy.RemedyTicketerPlugin".equalsIgnoreCase(Vault.getProperty("opennms.ticketer.plugin")) && (alarm.getTTicketState() == null || alarm.getTTicketState().toString().equals("CREATE_FAILED") )) { %>
      <input type="hidden" name="nodelabel" value="<%=alarm.getNodeLabel()%>"/>
      <input class="form-control" type="text" name="remedy.user.comment" value="Add a Comment here"/>
      <select class="form-control" name="remedy.urgency">
        <option value="1-Critical">1-Critical</option>
        <option value="2-High">2-High</option>
        <option value="3-Medium">3-Medium</option>
        <option value="4-Low" selected="selected">4-Low</option>
      </select>
      <select class="form-control" name="remedy.assignedgroup">
        <% String propsFile = new String(Vault.getProperty("opennms.home") + "/etc/remedy.properties");
           Configuration remedyConfig = null;
           try {
             remedyConfig = new PropertiesConfiguration(propsFile);
           } catch (final ConfigurationException e) {}
           for (String group: remedyConfig.getString("remedy.targetgroups").split(":")) { %>
             <option value="<%=group%>"><%=group%></option>
        <% }  %>
      </select>
    <% } %>
    <%-- Remedy Specific TroubleTicket - End --%>
</form>

<form class="form-inline" method="post" action="alarm/ticket/update.htm">
    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
    <input type="hidden" name="redirect" value="<%="/alarm/detail.htm" + "?" + request.getQueryString()%>" />
    <form:input class="form-control btn btn-default" type="submit" value="Update Ticket" disabled="<%=(alarm.getTTicketState() == null || alarm.getTTicketId() == null) %>"/>
</form>

<form class="form-inline" method="post" action="alarm/ticket/close.htm">
    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
    <input type="hidden" name="redirect" value="<%="/alarm/detail.htm" + "?" + request.getQueryString()%>" />
    <form:input class="form-control btn btn-default" type="submit" value="Close Ticket" disabled="<%=((alarm.getTTicketState() == null) || ((alarm.getTTicketState() != TroubleTicketState.OPEN) && (alarm.getTTicketState() != TroubleTicketState.CLOSE_FAILED))) ? true : false %>" />
</form>

</div>
</div>
<br/>

<% } // alarmTroubleTicketEnabled %>
<% } // isUserInRole %>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

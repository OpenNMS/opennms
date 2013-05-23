<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
        org.opennms.web.springframework.security.Authentication"
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
            return template.replaceAll("\\$\\{id\\}", alarm.getTTicketId());
        }
    }

%>

<%
    XssRequestWrapper req = new XssRequestWrapper(request);
    OnmsAlarm alarm = (OnmsAlarm) request.getAttribute("alarm");

    if (alarm == null) {
        throw new ServletException("Missing alarm request attribute.");
    }

    pageContext.setAttribute("alarm", alarm);

    String action = null;
    String ackButtonName = null;
    boolean showEscalate = false;
    boolean showClear = false;

    if (alarm.getAckTime() == null) {
        ackButtonName = "Acknowledge";
        action = AcknowledgeType.ACKNOWLEDGED.getShortName();
    } else {
        ackButtonName = "Unacknowledge";
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
%>

<jsp:include page="/includes/header.jsp" flush="false" >
    <jsp:param name="title" value="Alarm Detail" />
    <jsp:param name="headTitle" value="Detail" />
    <jsp:param name="headTitle" value="Alarms" />
    <jsp:param name="breadcrumb" value="<a href='alarm/index.jsp'>Alarms</a>" />
    <jsp:param name="breadcrumb" value="Detail" />
</jsp:include>

<h3>Alarm <%=alarm.getId()%></h3>

<table>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th width="100em">Severity</th>
        <td class="divider" width="28%"><%=alarm.getSeverity().getLabel()%></td>
        <th width="100em">Node</th>
        <td class="divider" width="28%">
            <% if (alarm.getNodeId() > 0) {%>
            <c:url var="nodeLink" value="element/node.jsp">
                <c:param name="node" value="<%=String.valueOf(alarm.getNodeId())%>"/>
            </c:url>
            <a href="${nodeLink}"><c:out value="<%=alarm.getNodeLabel()%>"/></a>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th>Last&nbsp;Event</th>
        <td><span title="Event <%= alarm.getLastEvent().getId()%>"><a href="event/detail.jsp?id=<%= alarm.getLastEvent().getId()%>"><fmt:formatDate value="<%=alarm.getLastEventTime()%>" type="BOTH" /></a></span></td>
        <th>Interface</th>
        <td>
            <% if (alarm.getIpAddr() != null) {%>
            <% if (alarm.getNodeId() > 0) {%>
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
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th>First&nbsp;Event</th>
        <td><fmt:formatDate value="<%=alarm.getFirstEventTime()%>" type="BOTH" /></td>
        <th>Service</th>
        <td>
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
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th>Count</th>
        <td><%=alarm.getCounter()%></td>
        <th>UEI</th>
        <td>
            <% if (alarm.getUei() != null) {%>
            <%=alarm.getUei()%>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th>Ticket&nbsp;ID</th>
        <td><% if (alarm.getTTicketId() == null) {%>
            &nbsp;
            <% } else {%>
            <%= alarmTicketLink(alarm)%> 
            <% }%>
        </td>
        <th>Ticket&nbsp;State</th>
        <td><% if (alarm.getTTicketState() == null) {%>
            &nbsp;
            <% } else {%>
            <%= alarm.getTTicketState()%> 
            <% }%>
        </td>
    </tr>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th>Reduct.&nbsp;Key</th>
        <td colspan="3">
            <% if (alarm.getReductionKey() != null) {%>
            <%=alarm.getReductionKey()%>
            <% } else {%>
            &nbsp;
            <% }%>
        </td>
    </tr>
</table>

<table>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th>Log&nbsp;Message</th>
    </tr>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <td><%=alarm.getLogMsg()%></td>
    </tr>
</table>

<% if (acks != null) {%>
<table>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th>Acknowledged&nbsp;By</th>
        <th>Acknowledged&nbsp;Type</th>
        <th>Time&nbsp;Acknowledged</th>
    </tr>
    <% for (OnmsAcknowledgment ack : acks) {%>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <td><%=ack.getAckUser()%></td>
        <td><%=ack.getAckAction()%></td>
        <td><%=ack.getAckTime()%></td>
    </tr>
    <% }%>
</table>
<% }%>

<table>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th>Description</th>
    </tr>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <td><%=alarm.getDescription()%></td>
    </tr>
</table>

<table>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th colspan="3" width="50%">Sticky Memo</th>
        <th colspan="3" width="50%">Journal Memo</th>
    </tr>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <td colspan="3">
	         <form method="post" action="alarm/saveStickyMemo.htm">        
				<textarea style="width:99%" name="stickyMemoBody" ><%=(alarm.getStickyMemo() != null && alarm.getStickyMemo().getBody() != null) ? alarm.getStickyMemo().getBody() : ""%></textarea>
	            <br/>
				<input type="hidden" name="alarmId" value="<%=alarm.getId() %>"/>   
	            <form:input type="submit" value="Save" />
	         </form>
	         <form method="post" action="alarm/removeStickyMemo.htm">        
				<input type="hidden" name="alarmId" value="<%=alarm.getId() %>"/>   
	            <form:input type="submit" value="Delete" />
	         </form>
        </td>

        <td colspan="3"> 
            <form method="post" action="alarm/saveJournalMemo.htm">        
                <textarea style="width:99%" name="journalMemoBody" ><%=(alarm.getReductionKeyMemo() != null && alarm.getReductionKeyMemo().getBody() != null) ? alarm.getReductionKeyMemo().getBody() : ""%></textarea>
                <br/>
                <input type="hidden" name="alarmId" value="<%=alarm.getId()%>"/>
                <form:input type="submit" value="Save" />    
            </form>
            <form method="post" action="alarm/removeJournalMemo.htm">
                <input type="hidden" name="alarmId" value="<%=alarm.getId()%>"/>
                <form:input type="submit" value="Delete" />    
            </form>
        </td>
    </tr>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <td><strong>Author:</strong>&nbsp;<%=(alarm.getStickyMemo() != null && alarm.getStickyMemo().getAuthor() != null) ? alarm.getStickyMemo().getAuthor() : ""%></td>
        <td><strong>Updated:</strong>&nbsp;
        	<%if (alarm.getStickyMemo() != null) { %>
        		<fmt:formatDate value="<%=alarm.getStickyMemo().getUpdated()%>" type="BOTH" />
        	<%}%>
        </td>
        <td><strong>Created:</strong>&nbsp;
        	<%if (alarm.getStickyMemo() != null) { %>
        		<fmt:formatDate value="<%=alarm.getStickyMemo().getCreated()%>" type="BOTH" />
        	<%}%>
        </td>
        
        <td><strong>Author:&nbsp;</strong><%=(alarm.getReductionKeyMemo() != null && alarm.getReductionKeyMemo().getAuthor() != null) ? alarm.getReductionKeyMemo().getAuthor() : ""%></td>
        <td><strong>Updated:</strong>&nbsp;
        	<%if (alarm.getReductionKeyMemo() != null) {%>
        		<fmt:formatDate value="<%=alarm.getReductionKeyMemo().getUpdated()%>" type="BOTH" />
        	<%}%>
        </td>
        <td><strong>Created:</strong>&nbsp;
        	<%if (alarm.getReductionKeyMemo() != null) {%>
        		<fmt:formatDate value="<%=alarm.getReductionKeyMemo().getCreated()%>" type="BOTH" />
        	<%}%>
        </td>
    </tr>
</table>

<table>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th>Operator&nbsp;Instructions</th>
    </tr>

    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <td>
            <%if (alarm.getOperInstruct() == null) {%>
            No instructions available
            <% } else {%>
            <%=alarm.getOperInstruct()%>
            <% }%>
        </td>
    </tr>
</table>

<% if (request.isUserInRole(Authentication.ROLE_ADMIN) || !request.isUserInRole(Authentication.ROLE_READONLY)) {%>
<table>
    <tbody>
        <tr class="<%=alarm.getSeverity().getLabel()%>">
            <th colspan="2">Acknowledgment&nbsp;and&nbsp;Severity&nbsp;Actions</th>
        </tr>
        <tr class="<%=alarm.getSeverity().getLabel()%>">
            <td>
                <form method="post" action="alarm/acknowledge">
                    <input type="hidden" name="actionCode" value="<%=action%>" />
                    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
                    <input type="hidden" name="redirect" value="<%= "detail.htm" + "?" + request.getQueryString()%>" />
                    <input type="submit" value="<%=ackButtonName%>" />
                </form>
            </td>
            <td><%=ackButtonName%> this alarm</td>
        </tr>

        <%if (showEscalate || showClear) {%>
        <tr class="<%=alarm.getSeverity().getLabel()%>">
            <td>
                <form method="post" action="alarm/changeSeverity">
                    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
                    <input type="hidden" name="redirect" value="<%= "detail.htm" + "?" + request.getQueryString()%>" />	  
                    <select name="actionCode">
                        <%if (showEscalate) {%>
                        <option value="<%=escalateAction%>">Escalate</option>
                        <% }%>
                        <%if (showClear) {%>
                        <option value="<%=clearAction%>">Clear</option>
                        <% }%>
                    </select>
                    <input type="submit" value="Go"/>
                </form>
            </td>
            <td>
                <%if (showEscalate) {%>
                Escalate
                <% }%>
                <%if (showEscalate && showClear) {%>
                or
                <% }%>
                <%if (showClear) {%>
                Clear
                <% }%>
                this alarm
            </td>
        </tr>
        <% } // showEscalate || showClear %>      
    </tbody>
</table>

<br/>

<% if ("true".equalsIgnoreCase(Vault.getProperty("opennms.alarmTroubleTicketEnabled"))) {%>

<form method="post" action="alarm/ticket/create.htm">
    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
    <input type="hidden" name="redirect" value="<%="/alarm/detail.htm" + "?" + request.getQueryString()%>" />
    <form:input type="submit" value="Create Ticket" disabled="${(!empty alarm.troubleTicketState) && (alarm.troubleTicketState != 'CREATE_FAILED')}" />
</form>

<form method="post" action="alarm/ticket/update.htm">
    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
    <input type="hidden" name="redirect" value="<%="/alarm/detail.htm" + "?" + request.getQueryString()%>" />
    <form:input type="submit" value="Update Ticket" disabled="${(empty alarm.troubleTicket)}"/>
</form>

<form method="post" action="alarm/ticket/close.htm">
    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
    <input type="hidden" name="redirect" value="<%="/alarm/detail.htm" + "?" + request.getQueryString()%>" />
    <form:input type="submit" value="Close Ticket" disabled="${(empty alarm.troubleTicketState) || ((alarm.troubleTicketState != 'OPEN') && (alarm.troubleTicketState != 'CLOSE_FAILED')) }" />
</form>

<% } // alarmTroubleTicketEnabled %>
<% } // isUserInRole%>

<jsp:include page="/includes/footer.jsp" flush="false" />

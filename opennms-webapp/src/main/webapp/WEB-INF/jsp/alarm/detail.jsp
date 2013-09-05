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
        import="java.util.*,
	org.opennms.core.resource.Vault,
	org.opennms.core.utils.InetAddressUtils,
	java.text.SimpleDateFormat,
	org.opennms.web.filter.Filter,
        org.opennms.core.utils.WebSecurityUtils,
        org.opennms.web.controller.alarm.*,
        org.opennms.web.alarm.*,
	org.opennms.web.event.SortStyle,
	org.opennms.web.event.Event,
	org.opennms.web.event.EventQueryParms,
	org.opennms.web.event.EventUtil,
	org.opennms.netmgt.EventConstants,
	org.opennms.web.event.AcknowledgeType,
        org.opennms.netmgt.model.OnmsAcknowledgment,
        org.opennms.netmgt.model.OnmsAlarm,
        org.opennms.netmgt.model.OnmsSeverity,
	org.opennms.web.servlet.XssRequestWrapper,
        org.opennms.web.api.Authentication"
%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:include page="/includes/header.jsp" flush="false" >
    <jsp:param name="title" value="Alarm Detail" />
    <jsp:param name="headTitle" value="Detail" />
    <jsp:param name="headTitle" value="Alarms" />
    <jsp:param name="breadcrumb" value="<a href='alarm/index.jsp'>Alarms</a>" />
    <jsp:param name="breadcrumb" value="Detail" />
</jsp:include>

<script type="text/javascript" src="<c:url value="/js/jquery/jquery.js"/>"></script>
<script type="text/javascript">
	function submitForm() {
		// Decide to which servlet we will submit
		document.alarm_action_form.action = "alarm/alarmExport";
		document.alarm_action_form.actionCode.value = "<%=AlarmReportController.EXPORT_ACTION%>";
		showPopup('Are you sure you want to export an alarm?');
	}
</script>
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
    
    //Get the required attributes value
    XssRequestWrapper req = new XssRequestWrapper(request);
    
    String alarmIdString = req.getParameter( "id" );
    
    OnmsAlarm alarm = (OnmsAlarm) request.getAttribute("alarm");
    final String alarmId = (String)request.getAttribute("alarmId");

    EventQueryParms parms = (EventQueryParms)req.getAttribute( "parms" );
    pageContext.setAttribute("parms", parms);
    
    Event[] events = (Event[])req.getAttribute("events");
    pageContext.setAttribute("events", events);

    int eventCount = req.getAttribute( "eventCount" ) == null ? -1 : (Integer)req.getAttribute( "eventCount" );

    HashMap<Integer, List<OnmsAcknowledgment>> alarmsAcknowledgments = (HashMap<Integer, List<OnmsAcknowledgment>>)req.getAttribute("alarmsAcknowledgments"); 
    pageContext.setAttribute("alarmsAcknowledgments", alarmsAcknowledgments);
    
    //Date format for an alarm events
    SimpleDateFormat formater = new SimpleDateFormat("MM/dd/yy hh:mm:ss aaa",Locale.ENGLISH);
		
    if(alarm == null){%>
	<h3>An alarm with this id [ <%=alarmIdString%> ] was not found in Database</h3>
    <%}else{
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
	    
	    List<OnmsAcknowledgment> acks = (List<OnmsAcknowledgment>) req.getAttribute("acknowledgments");
    %>
	<h3> Alarm <%=alarm .getId()%></h3>
	
	<!-- Table for Alarm Details -->
<table>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th width="100em">Severity</th>
        <td class="divider" width="28%"><%=alarm.getSeverity().getLabel()%></td>
        <th width="100em">Node</th>
        <td class="divider" width="28%">
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
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th>Last&nbsp;Event</th>
        <td><span title="Event <%= alarm.getLastEvent().getId()%>"><a href="event/detail.jsp?id=<%= alarm.getLastEvent().getId()%>"><fmt:formatDate value="<%=alarm.getLastEventTime()%>" type="BOTH" /></a></span></td>
        <th>Interface</th>
        <td>
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
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th>First&nbsp;Event</th>
        <td><fmt:formatDate value="<%=alarm.getFirstEventTime()%>" type="BOTH" /></td>
        <th>Service</th>
        <td>
            <% if (alarm.getServiceType() != null) {%>
            <% if (alarm.getIpAddr() != null && alarm.getNodeId() != null) {%>
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

	<!-- Table for Log Message  Details-->
<table>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th>Log&nbsp;Message</th>
    </tr>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <td><%=alarm.getLogMsg()%></td>
    </tr>
</table>

	<!--  Table for Acknowledged Details-->
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
			<td><%=org.opennms.web.api.Util.formatDateToUIString(ack.getAckTime())%></td>
    </tr>
    <% }%>
</table>
<% }%>

	<!-- Table for Description details -->
<table>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th>Description</th>
    </tr>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <td><%=alarm.getDescription()%></td>
    </tr>
</table>
	
	<!--  START : Table for Alarm events details -->
	<table>
		<tr>
			<th colspan="7">Alarm History</th>
		</tr>
		<%if(events.length>0){%>
		<tr>
			<th class="divider" width="50em">Event ID</th>
			<th class="divider" width="50em">Alarm ID</th>
			<th>Creation Time</th>
			<th class="divider" width="100em">Severity</th>
			<th>Operation Time</th>
			<th>User</th>
			<th>Operation</th>
		</tr>
		<% for( int i=0; i < events.length; i++ ) {
			
			//Get an event from eventList
			Event event = events[i];
			pageContext.setAttribute("event", event);
			
			//Get the alarm acknowledgment size
			int alarmAckSize = 0;
			int eventAlarmId = event.getAlarmId();
			List<OnmsAcknowledgment> currAlarmAcknowledgment = alarmsAcknowledgments.get(eventAlarmId);
			if(currAlarmAcknowledgment!=null){
				alarmAckSize = currAlarmAcknowledgment.size();
			}
		
			//Find the duplicate Alarm Id for the events
			boolean isAlarmsWithSameId = true;
			for(int j=0;j<i;j++){
				Event preEvent = events[j];
				int preEventAlarmId = preEvent.getAlarmId();
				if(eventAlarmId == preEventAlarmId){
					isAlarmsWithSameId = false;
				}
			}
			
			if(alarmAckSize>0){
				
				int rowSpanCount = 0;
				int firstRow = 0;
				Calendar eventCreatTime = this.getDateFormat(formater.parse(org.opennms.web.api.Util.formatDateToUIString(event.getCreateTime())));
				
				if(isAlarmsWithSameId){
				
					//Find the row span count to draw the row of table
					for (OnmsAcknowledgment ack : currAlarmAcknowledgment) {
						Calendar ackTime = this.getDateFormat(formater.parse(org.opennms.web.api.Util.formatDateToUIString(ack.getAckTime())));
						if((eventCreatTime.compareTo(ackTime)) < 0){
							rowSpanCount++;
						}
					}
					
					if(rowSpanCount == 0){%>
						<tr class="<%= event.getSeverity().getLabel() %>">
							<td class="divider">
							    <% if( event.getId() > 0 ) { %>
								<%=event.getId()%>
							    <% } else {%>
							      -
							    <% } %>
							</td>
							<td class="divider" >
								<% if( eventAlarmId > 0 ) { %>
									<%=eventAlarmId%>
								<% } else {%>
								-
								<% } %>
							</td>
							<td class="divider" ><%=org.opennms.web.api.Util.formatDateToUIString(event.getCreateTime())%></td>
							<td class="divider" ><%= event.getSeverity().getLabel() %></td>
							<td class="divider" > - </td>
							<td class="divider" > - </td>
							<td class="divider" > - </td>
						</tr>
					<%
					}else{
						for (int ackIterator=alarmAckSize-1; ackIterator >=0 ; ackIterator--)  {
						
							//Get the acknowledgment time
							OnmsAcknowledgment ack = currAlarmAcknowledgment.get(ackIterator);
							Calendar ackTime = this.getDateFormat(formater.parse(org.opennms.web.api.Util.formatDateToUIString(ack.getAckTime())));
						
							//Comparison of event creation time with acknowledgment time
							if((eventCreatTime.compareTo(ackTime)) < 0){
								if(firstRow == 0){
									firstRow++;
								%>
									<tr class="<%= event.getSeverity().getLabel() %>">
										<td rowspan="<%=rowSpanCount%>" class="divider">
										    <% if( event.getId() > 0 ) { %>
											<%=event.getId()%>
										    <% } else {%>
										      -
										    <% } %>
										</td>
										<td rowspan="<%=rowSpanCount%>" class="divider" >
											<% if( eventAlarmId > 0 ) { %>
												<%=eventAlarmId%>
											<% } else {%>
											-
											<% } %>
										</td>
										<td rowspan="<%=rowSpanCount%>" class="divider" ><%=org.opennms.web.api.Util.formatDateToUIString(event.getCreateTime())%></td>
										<td rowspan="<%=rowSpanCount%>" class="divider" ><%= event.getSeverity().getLabel() %></td>
										<td class="divider" ><%=org.opennms.web.api.Util.formatDateToUIString(ack.getAckTime())%></td>
										<td class="divider" ><%=ack.getAckUser()%></td>
										<td class="divider" ><%=ack.getAckAction()%></td>
									</tr>
								<%}else{%>
									<tr class="<%= event.getSeverity().getLabel() %>">
										<td class="divider" ><%=org.opennms.web.api.Util.formatDateToUIString(ack.getAckTime())%></td>
										<td class="divider" ><%=ack.getAckUser()%></td>
										<td class="divider" ><%=ack.getAckAction()%></td>
									</tr>
								<%}
							}
						}
					}%><%
				}else{
					
					//Get the previous event creation time
					Event previousEvent = events[i-1];
					Calendar preEventCreatTime = this.getDateFormat(formater.parse(org.opennms.web.api.Util.formatDateToUIString(previousEvent.getCreateTime())));
					
					//Find the row span count to draw the row of table
					for (OnmsAcknowledgment ack : currAlarmAcknowledgment) {
						Calendar ackTime = this.getDateFormat(formater.parse(org.opennms.web.api.Util.formatDateToUIString(ack.getAckTime())));
						if((((eventCreatTime.compareTo(ackTime)) < 0) && ((preEventCreatTime.compareTo(ackTime)) > 0))){
							rowSpanCount++;
						}
					}
					
					if(rowSpanCount == 0){%>
						<tr class="<%= event.getSeverity().getLabel() %>">
							<td class="divider">
							    <% if( event.getId() > 0 ) { %>
								<%=event.getId()%>
							    <% } else {%>
							      -
							    <% } %>
							</td>
							<td class="divider" >
								<% if( eventAlarmId > 0 ) { %>
									<%=eventAlarmId%>
								<% } else {%>
									-
								<% } %>
							</td>
							<td class="divider" ><%=org.opennms.web.api.Util.formatDateToUIString(event.getCreateTime())%></td>
							<td class="divider" ><%= event.getSeverity().getLabel() %></td>
							<td class="divider" > - </td>
							<td class="divider" > - </td>
							<td class="divider" > - </td>
						</tr>
					<%
					}else{
						for (int ackIterator=alarmAckSize-1; ackIterator >=0 ; ackIterator--)  {
							
							//Get the acknowledgment time
							OnmsAcknowledgment ack = currAlarmAcknowledgment.get(ackIterator);
							Calendar ackTime = this.getDateFormat(formater.parse(org.opennms.web.api.Util.formatDateToUIString(ack.getAckTime())));
							
							//Comparison of event creation time with acknowledgment time
							if((((eventCreatTime.compareTo(ackTime)) < 0) && ((preEventCreatTime.compareTo(ackTime)) > 0))){
								if(firstRow == 0){
									firstRow++;%>
									<tr class="<%= event.getSeverity().getLabel() %>">
										<td rowspan="<%=rowSpanCount%>" class="divider">
										    <% if( event.getId() > 0 ) { %>
											<%=event.getId()%>
										    <% } else {%>
										      -
										    <% } %>
										</td>
										<td rowspan="<%=rowSpanCount%>" class="divider" >
											<% if( eventAlarmId > 0 ) { %>
												<%=eventAlarmId%>
											<% } else {%>
											-
											<% } %>
										</td>
										<td rowspan="<%=rowSpanCount%>" class="divider" ><%=org.opennms.web.api.Util.formatDateToUIString(event.getCreateTime())%></td>
										<td rowspan="<%=rowSpanCount%>" class="divider" ><%= event.getSeverity().getLabel() %></td>
										<td class="divider" ><%=org.opennms.web.api.Util.formatDateToUIString(ack.getAckTime())%></td>
										<td class="divider" ><%=ack.getAckUser()%></td>
										<td class="divider" ><%=ack.getAckAction()%></td>
									</tr><%
								}else{%>
									<tr class="<%= event.getSeverity().getLabel() %>">
										<td class="divider" ><%=org.opennms.web.api.Util.formatDateToUIString(ack.getAckTime())%></td>
										<td class="divider" ><%=ack.getAckUser()%></td>
										<td class="divider" ><%=ack.getAckAction()%></td>
									</tr><%
								}
							}
						}
					}%><%
				}//isDuplicateAlarmId%><%
			}else{%>
				<tr class="<%= event.getSeverity().getLabel() %>">
					<td class="divider">
					    <% if( event.getId() > 0 ) { %>
						<%=event.getId()%>
					    <% } else {%>
					      -
					    <% } %>
					</td>
					<td class="divider" >
						<% if( eventAlarmId > 0 ) { %>
							<%=eventAlarmId%>
						<% } else {%>
							-
						<% } %>
					</td>
					<td class="divider" ><%=org.opennms.web.api.Util.formatDateToUIString(event.getCreateTime())%></td>
					<td class="divider" ><%= event.getSeverity().getLabel() %></td>
					<td class="divider" > - </td>
					<td class="divider" > - </td>
					<td class="divider" > - </td>
				</tr><%
			} //isAcknowledgmentAvailable%><%
		}%>
		<!-- PageNavigation Row-->
		<tr>
			<td colspan="7">
				<div>
					<div style="float:left;">
						<% 
							if( events.length > 0 ) { 
						%>
						      <% String baseUrl = this.makeLink(parms,alarm); %>
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
					</div>
					<div style="float:right;"><p>Number of events  in this page : <%=events.length%></p></div>
				</div>
			</td>
		</tr><%
		}else{%>
		<tr class="<%=alarm.getSeverity().getLabel()%>">
			<td colspan="7">There is no events for this alarm</td>
		</tr>
		<%}%>
	</table>
	<!--  END : Table for Alarm events details -->
	
	<!-- Table for Sticky Memo Details -->
<table>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <th colspan="3" width="50%">Sticky Memo</th>
        <th colspan="3" width="50%">Journal Memo</th>
    </tr>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <td colspan="3">
            <form method="post" action="alarm/saveSticky.htm">        
                <textarea style="width:99%" name="stickyMemoBody" ><%=alarm.getStickyMemo() == null? "" : (alarm.getStickyMemo().getBody() != null ? alarm.getStickyMemo().getBody() : "")%></textarea>
                <br/>
                <input type="hidden" name="alarmId" value="<%=alarm.getId()%>"/>
                <input type="submit" value="Save" />    
            </form>
            <form method="post" action="alarm/clearSticky.htm">
                 <input type="hidden" name="alarmId" value="<%=alarm.getId()%>"/>
                 <input type="submit" value="Clear" />
            </form>
        </td>

        <td colspan="3"> 
            <form method="post" action="alarm/saveJournal.htm">        
                <textarea style="width:99%" name="journalMemoBody" ><%=alarm.getReductionKeyMemo() == null? "" : (alarm.getReductionKeyMemo().getBody() != null ? alarm.getReductionKeyMemo().getBody() : "")%></textarea>
                <br/>
                <input type="hidden" name="alarmId" value="<%=alarm.getId()%>"/>
                <input type="submit" value="Save" />    
            </form>
            <form method="post" action="alarm/removeJournalMemo.htm">
                <input type="hidden" name="alarmId" value="<%=alarm.getId()%>"/>
                <input type="submit" value="Clear" />    
            </form>
        </td>
    </tr>
    <tr class="<%=alarm.getSeverity().getLabel()%>">
        <td><strong>Author:</strong>&nbsp;<%=alarm.getStickyMemo() == null? "" : (alarm.getStickyMemo().getAuthor() != null ? alarm.getStickyMemo().getAuthor() : "")%></td>
        <td><strong>Updated:</strong>&nbsp;<%if (alarm.getStickyMemo() != null) { %><fmt:formatDate value="<%=alarm.getStickyMemo().getUpdated()%>" type="BOTH" /><% } %></td>
        <td><strong>Created:</strong>&nbsp;<%if (alarm.getStickyMemo() != null) { %><fmt:formatDate value="<%=alarm.getStickyMemo().getCreated()%>" type="BOTH" /><% } %></td>
        
        <td><strong>Author:&nbsp;</strong><%=alarm.getReductionKeyMemo() == null? "" : (alarm.getReductionKeyMemo().getAuthor() != null ? alarm.getReductionKeyMemo().getAuthor() : "")%></td>
        <td><strong>Updated:</strong>&nbsp;<%if (alarm.getReductionKeyMemo() != null) { %><fmt:formatDate value="<%=alarm.getReductionKeyMemo().getUpdated()%>" type="BOTH" /><% } %></td>
        <td><strong>Created:</strong>&nbsp;<%if (alarm.getReductionKeyMemo() != null) { %><fmt:formatDate value="<%=alarm.getReductionKeyMemo().getCreated()%>" type="BOTH" /><% } %></td>
    </tr>
</table>

	<!-- Table for Operator Instructions Details-->
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

	<!-- Table for Acknowledgment and Severity Actions Details -->
	<% if (req.isUserInRole(Authentication.ROLE_ADMIN) || !request.isUserInRole(Authentication.ROLE_READONLY)) {%>
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
			    <input type="hidden" name="redirect" value="<%= this.getLink(alarm.getId(), parms.filters)%>" />
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
			    <input type="hidden" name="redirect" value="<%= this.getLink(alarm.getId(), parms.filters)%>" />
			    
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
	<!-- 
		<tr class="<%=alarm.getSeverity().getLabel()%>">
		    <td>
			<form method="post" name="alarm_action_form">
				<div class="exportConfirmation" id="exportConfirmation" style="font-size:120%;display:none" >
					<center>
						<div id="alertText">&nbsp;</div><br>
						Select your file format : 
						<input type="radio" name="format" value="PDF" checked="checked">PDF
						<input type="radio" name="format" value="HTML">HTML
						<input type="radio" name="format" value="CSV">CSV<br><br>
						<input type="button" onclick="javascript:callExportAction();" value="Ok" />
						<input type="button" onclick="javascript:hideTransBackground();" value="Cancel"/>
					</center>
				</div>
				
				<input type="hidden" name="nodeid" value="node=" />
				<input type="hidden" name="exactuei" value="exactUei=" />
				<input type="hidden" name="ipaddress" value="interface=" />
				<input type="hidden" name="reportId" value="local_alarm-report" />
				<div id="progressBar" class="jquery-ui-like"><div><center>Action in progress, Please wait...</center></div></div>
				<div id="backgroundPopup"></div><body/>
				<input type="hidden" name="actionCode"/>
				<input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
				<input type="button" value="Export" onClick="submitForm()"/>
			</form>
		    </td>
		    <td>Export this alarm</td>
		</tr>
	-->
    </tbody>
</table>

<br/>

<% if ("true".equalsIgnoreCase(Vault.getProperty("opennms.alarmTroubleTicketEnabled"))) {%>

<form method="post" action="alarm/ticket/create.htm">
    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
    <input type="hidden" name="redirect" value="<%="/alarm/detail.htm" + "?" + request.getQueryString()%>" />
    <input type="submit" value="Create Ticket" disabled="${(!empty alarm.troubleTicketState) && (alarm.troubleTicketState != 'CREATE_FAILED')}" />
</form>

<form method="post" action="alarm/ticket/update.htm">
    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
    <input type="hidden" name="redirect" value="<%="/alarm/detail.htm" + "?" + request.getQueryString()%>" />
    <input type="submit" value="Update Ticket" disabled="${(empty alarm.troubleTicket)}"/>
</form>

<form method="post" action="alarm/ticket/close.htm">
    <input type="hidden" name="alarm" value="<%=alarm.getId()%>"/>
    <input type="hidden" name="redirect" value="<%="/alarm/detail.htm" + "?" + request.getQueryString()%>" />
    <input type="submit" value="Close Ticket" disabled="${(empty alarm.troubleTicketState) || ((alarm.troubleTicketState != 'OPEN') && (alarm.troubleTicketState != 'CLOSE_FAILED')) }" />
</form>

<% } // alarmTroubleTicketEnabled %>
<% } // isUserInRole%><%
   }//isValidAlarmId%>
    
<jsp:include page="/includes/footer.jsp" flush="false" />

<%!
    String urlBase = "alarm/detail.htm";
    
    public String getFiltersAsString(List<Filter> filters ) {
    
        StringBuffer buffer = new StringBuffer();

        if( filters != null ) {
            for( int i=0; i < filters.size(); i++ ) {
                buffer.append( "&amp;filter=" );
                String filterString = EventUtil.getFilterString((Filter)filters.get(i));
                buffer.append( java.net.URLEncoder.encode(filterString) );
            }
        }      
    
        return( buffer.toString() );
    }
    
    public String makeLink( int alarm, List<Filter> filters) {
    
      StringBuffer buffer = new StringBuffer( this.urlBase );
      buffer.append( "?id=" );
      buffer.append( alarm );
      buffer.append( this.getFiltersAsString(filters) );
      
      return( buffer.toString() );
      
    }
    
    public String makeLink( EventQueryParms parms , OnmsAlarm alarm) {
      return( this.makeLink( alarm.getId(), parms.filters) );
    }
    
   public String getLink( int alarm, List<Filter> filters) {
    
      String urlDetail = "detail.htm";
      StringBuffer buffer = new StringBuffer( urlDetail );
      buffer.append( "?id=" );
      buffer.append( alarm );
      buffer.append( this.getFiltersAsString(filters) );
      
      return( buffer.toString() );
      
    }
    
    public Calendar getDateFormat(Date date){
    
	Calendar calendar = Calendar.getInstance();  
	calendar.setTime(date);
	
	Calendar calDate = new GregorianCalendar(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),calendar.get(Calendar.SECOND));
	
	return calDate;
    }

%>

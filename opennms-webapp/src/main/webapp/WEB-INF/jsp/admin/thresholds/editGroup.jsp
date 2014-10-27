<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.admin.notification.noticeWizard.*,
	org.opennms.web.api.Util"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Threshold Group" />
	<jsp:param name="headTitle" value="Edit Group" />
	<jsp:param name="headTitle" value="Thresholds" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
    <jsp:param name="breadcrumb" value="<a href='admin/thresholds/index.jsp'>Threshold Groups</a>" />
    <jsp:param name="breadcrumb" value="Edit Group" />
</jsp:include>

<script type="text/javascript">
    function submitNewNotificationForm(uei) {
    	document.getElementById("uei").value=uei;
    	document.add_notification_form.submit();
    }
</script>

	  <!-- hidden form for adding a new Notification -->
	  <form action="admin/notification/noticeWizard/notificationWizard" method="post" name="add_notification_form">
	  	<input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_OTHER_WEBUI%>" />
	  	<input type="hidden" id="uei" name="uei" value="" /> <!-- Set by java script -->
	  	<input type="hidden" name="returnPage" value="<%=Util.calculateUrlBase(request)%>admin/thresholds/index.htm?groupName=${group.name}&editGroup" />
	  </form>

<h3>Edit group ${group.name}</h3>

<form action="admin/thresholds/index.htm" method="post">
  <h2>Basic Thresholds</h2>
  <table class="normal">
    <tr>
        <th class="standardheader">Type</th>
        <th class="standardheader">Description</th>
        <th class="standardheader">Datasource</th>
        <th class="standardheader">Datasource type</th>
        <th class="standardheader">Datasource label</th>
        <th class="standardheader">Value</th>
        <th class="standardheader">Re-arm</th>
        <th class="standardheader">Trigger</th>
        <th class="standardheader">Triggered UEI</th>
        <th class="standardheader">Re-armed UEI</th>
        <th class="standardheader">&nbsp;</th>
        <th class="standardheader">&nbsp;</th>
    </tr>
    <c:forEach items="${group.threshold}" varStatus="thresholdIndex" var="threshold">
        <tr>
			<td class="standard">${threshold.type}</td>
			<td class="standard">${threshold.description}</td>
			<td class="standard">${threshold.dsName}</td>
			<td class="standard">${threshold.dsType}</td>
			<td class="standard">${threshold.dsLabel}</td>
			<td class="standard">${threshold.value}</td>
			<td class="standard">${threshold.rearm}</td>
			<td class="standard">${threshold.trigger}</td>
			<td class="standard"><a href="javascript: void submitNewNotificationForm('${threshold.triggeredUEI}');" title="Edit notifications for this uei">${threshold.triggeredUEI}</a></td>
			<td class="standard"><a href="javascript: void submitNewNotificationForm('${threshold.rearmedUEI}');" title="Edit notifications for this uei">${threshold.rearmedUEI}</a></td>
			<td class="standard"><a href="admin/thresholds/index.htm?groupName=${group.name}&thresholdIndex=${thresholdIndex.index}&editThreshold">Edit</a></td>
			<td class="standard"><a href="admin/thresholds/index.htm?groupName=${group.name}&thresholdIndex=${thresholdIndex.index}&deleteThreshold">Delete</a></td>
        </tr>
    </c:forEach>
  </table>
  <a href="admin/thresholds/index.htm?groupName=${group.name}&newThreshold">Create New Threshold</a>
  <br/><br/>
  <h2>Expression-based Thresholds</h2>
  <table class="normal">
    <tr>
        <th class="standardheader">Type</th>
        <th class="standardheader">Description</th>
        <th class="standardheader">Expression</th>
        <th class="standardheader">Datasource type</th>
        <th class="standardheader">Datasource label</th>
        <th class="standardheader">Value</th>
        <th class="standardheader">Re-arm</th>
        <th class="standardheader">Trigger</th>
		<th class="standardheader">Triggered UEI</th>
        <th class="standardheader">Re-armed UEI</th>
        <th class="standardheader">&nbsp;</th>
        <th class="standardheader">&nbsp;</th>
    </tr>
      <c:forEach items="${group.expression}" varStatus="expressionIndex" var="expression">
        <tr>
			<td class="standard">${expression.type}</td>
			<td class="standard">${expression.description}</td>
			<td class="standard">${expression.expression}</td>
			<td class="standard">${expression.dsType}</td>
			<td class="standard">${expression.dsLabel}</td>
			<td class="standard">${expression.value}</td>
			<td class="standard">${expression.rearm}</td>
			<td class="standard">${expression.trigger}</td>
			<td class="standard"><a href="javascript: void submitNewNotificationForm('${expression.triggeredUEI}');" title="Edit notifications for this uei">${expression.triggeredUEI}</a></td>
			<td class="standard"><a href="javascript: void submitNewNotificationForm('${expression.rearmedUEI}');" title="Edit notifications for this uei">${expression.rearmedUEI}</a></td>
			<td class="standard"><a href="admin/thresholds/index.htm?groupName=${group.name}&expressionIndex=${expressionIndex.index}&editExpression">Edit</a></td>
			<td class="standard"><a href="admin/thresholds/index.htm?groupName=${group.name}&expressionIndex=${expressionIndex.index}&deleteExpression">Delete</a></td>
        </tr>
    </c:forEach>
    </table>
    <a href="admin/thresholds/index.htm?groupName=${group.name}&newExpression">Create New Expression-based Threshold</a>
</form>
<h3>Help</h3>
<p>
The upper section is Basic Thresholds (thresholds on a  single datasource).  The threshold details are displayed to edit the threshold, click on the "Edit" link on same line as the threshold line.  
To delete the threshold, click on "Delete" on the same line as the threshold you want to delete.<br/>
To create a new threshold, click on the "Create New Threshold" link<br/>
The lower section is for Expression-based Thresholds, where the value being checked is a mathematical expression including one or more data sources.  Functionality is identical to that for the Basic Thresholds section
<br/>
If you have a custom UEI for triggering or re-arming the threshold, then it will be a hyperlink.  Clicking on that link takes you to the notifications wizard for that UEI, allowing you to see existing notifications for that UEI, and possibly create a new notification for that UEI.
</p>
<jsp:include page="/includes/footer.jsp" flush="false"/>

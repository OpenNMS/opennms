<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
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

<div class="row">
  <div class="col-md-12">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Basic Thresholds</h3>
      </div>
      <table class="table table-condensed table-striped edit-group-basic-thresholds">
        <tr>
            <th>Type</th>
            <th>Description</th>
            <th>Datasource</th>
            <th>Datasource type</th>
            <th>Datasource label</th>
            <th>Value</th>
            <th>Re-arm</th>
            <th>Trigger</th>
            <th>Triggered UEI</th>
            <th>Re-armed UEI</th>
            <th>&nbsp;</th>
            <th>&nbsp;</th>
        </tr>
        <c:forEach items="${group.thresholds}" varStatus="thresholdIndex" var="threshold">
            <tr name="threshold.${thresholdIndex.index}">
              <td name="threshold.${thresholdIndex.index}.type"><c:out value="${threshold.type.enumName}"/></td>
              <td name="threshold.${thresholdIndex.index}.description"><c:out value="${threshold.description.orElse(null)}"/></td>
              <td name="threshold.${thresholdIndex.index}.dsName"><c:out value="${threshold.dsName}"/></td>
              <td name="threshold.${thresholdIndex.index}.dsType"><c:out value="${threshold.dsType}"/></td>
              <td name="threshold.${thresholdIndex.index}.dsLabel"><c:out value="${threshold.dsLabel.orElse(null)}"/></td>
              <td name="threshold.${thresholdIndex.index}.value"><c:out value="${threshold.value}"/></td>
              <td name="threshold.${thresholdIndex.index}.rearm"><c:out value="${threshold.rearm}"/></td>
              <td name="threshold.${thresholdIndex.index}.trigger"><c:out value="${threshold.trigger}"/></td>
              <td name="threshold.${thresholdIndex.index}.triggeredUEI"><a href="javascript: void submitNewNotificationForm('${e:forJavaScript(threshold.triggeredUEI.orElse(null))}');" title="Edit notifications for this uei"><c:out value="${threshold.triggeredUEI.orElse(null)}"/></a></td>
              <td name="threshold.${thresholdIndex.index}.rearmedUEI"><a href="javascript: void submitNewNotificationForm('${e:forJavaScript(threshold.rearmedUEI.orElse(null))}');" title="Edit notifications for this uei"><c:out value="${threshold.rearmedUEI.orElse(null)}"/></a></td>
              <td><a href="admin/thresholds/index.htm?groupName=${group.name}&thresholdIndex=${thresholdIndex.index}&editThreshold">Edit</a></td>
              <td><a href="admin/thresholds/index.htm?groupName=${group.name}&thresholdIndex=${thresholdIndex.index}&deleteThreshold">Delete</a></td>
            </tr>
        </c:forEach>
      </table>
      <div class="panel-footer">
        <a href="admin/thresholds/index.htm?groupName=${group.name}&newThreshold">Create New Threshold</a>
      </div> <!-- panel-footer -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-md-12">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Expression-based Thresholds</h3>
      </div>
      <table class="table table-condensed table-striped edit-group-expression-based-thresholds">
        <tr>
            <th>Type</th>
            <th>Description</th>
            <th>Expression</th>
            <th>Datasource type</th>
            <th>Datasource label</th>
            <th>Value</th>
            <th>Re-arm</th>
            <th>Trigger</th>
            <th>Triggered UEI</th>
            <th>Re-armed UEI</th>
            <th>&nbsp;</th>
            <th>&nbsp;</th>
        </tr>
          <c:forEach items="${group.expressions}" varStatus="expressionIndex" var="expression">
            <tr name="expression.0">
              <td name="expression.${expressionIndex.index}.type"><c:out value="${expression.type.enumName}"/></td>
              <td name="expression.${expressionIndex.index}.description"><c:out value="${expression.description.orElse(null)}"/></td>
              <td name="expression.${expressionIndex.index}.expression"><c:out value="${expression.expression}"/></td>
              <td name="expression.${expressionIndex.index}.dsType"><c:out value="${expression.dsType}"/></td>
              <td name="expression.${expressionIndex.index}.dsLabel"><c:out value="${expression.dsLabel.orElse(null)}"/></td>
              <td name="expression.${expressionIndex.index}.value"><c:out value="${expression.value}"/></td>
              <td name="expression.${expressionIndex.index}.rearm"><c:out value="${expression.rearm}"/></td>
              <td name="expression.${expressionIndex.index}.trigger"><c:out value="${expression.trigger}"/></td>
              <td name="expression.${expressionIndex.index}.triggeredUEI"><a href="javascript: void submitNewNotificationForm('${e:forJavaScript(expression.triggeredUEI.orElse(null))}');" title="Edit notifications for this uei"><c:out value="${expression.triggeredUEI.orElse(null)}"/></a></td>
              <td name="expression.${expressionIndex.index}.rearmedUEI"><a href="javascript: void submitNewNotificationForm('${e:forJavaScript(expression.rearmedUEI.orElse(null))}');" title="Edit notifications for this uei"><c:out value="${expression.rearmedUEI.orElse(null)}"/></a></td>
              <td><a href="admin/thresholds/index.htm?groupName=${group.name}&expressionIndex=${expressionIndex.index}&editExpression">Edit</a></td>
              <td><a href="admin/thresholds/index.htm?groupName=${group.name}&expressionIndex=${expressionIndex.index}&deleteExpression">Delete</a></td>
            </tr>
        </c:forEach>
      </table>
      <div class="panel-footer">
        <a href="admin/thresholds/index.htm?groupName=${group.name}&newExpression">Create New Expression-based Threshold</a>
      </div> <!-- panel-footer -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

</form>

<div class="row">
  <div class="col-md-12">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Help</h3>
      </div>
      <div class="panel-body">
        <p>
        The upper section is Basic Thresholds (thresholds on a  single datasource).  The threshold details are displayed to edit the threshold, click on the "Edit" link on same line as the threshold line.  
        To delete the threshold, click on "Delete" on the same line as the threshold you want to delete.<br/>
        To create a new threshold, click on the "Create New Threshold" link<br/>
        The lower section is for Expression-based Thresholds, where the value being checked is a mathematical expression including one or more data sources.  Functionality is identical to that for the Basic Thresholds section
        <br/>
        If you have a custom UEI for triggering or re-arming the threshold, then it will be a hyperlink.  Clicking on that link takes you to the notifications wizard for that UEI, allowing you to see existing notifications for that UEI, and possibly create a new notification for that UEI.
        </p>
      </div> <!-- panel-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

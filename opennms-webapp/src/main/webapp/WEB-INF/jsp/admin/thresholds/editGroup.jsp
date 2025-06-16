<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.admin.notification.noticeWizard.*,
	org.opennms.web.api.Util"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e"%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Edit Group")
          .headTitle("Thresholds")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Threshold Groups", "admin/thresholds/index.jsp")
          .breadcrumb("Edit Group")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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
    <div class="card">
      <div class="card-header">
        <span>Basic Thresholds</span>
      </div>
      <table class="table table-sm table-striped edit-group-basic-thresholds">
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
      <div class="card-footer">
        <a href="admin/thresholds/index.htm?groupName=${group.name}&newThreshold">Create New Threshold</a>
      </div> <!-- card-footer -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<div class="row">
  <div class="col-md-12">
    <div class="card">
      <div class="card-header">
        <span>Expression-based Thresholds</span>
      </div>
      <table class="table table-sm table-striped edit-group-expression-based-thresholds">
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
      <div class="card-footer">
        <a href="admin/thresholds/index.htm?groupName=${group.name}&newExpression">Create New Expression-based Threshold</a>
      </div> <!-- card-footer -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

</form>

<div class="row">
  <div class="col-md-12">
    <div class="card">
      <div class="card-header">
        <span>Help</span>
      </div>
      <div class="card-body">
        <p>
        The upper section is Basic Thresholds (thresholds on a  single datasource).  The threshold details are displayed to edit the threshold, click on the "Edit" link on same line as the threshold line.  
        To delete the threshold, click on "Delete" on the same line as the threshold you want to delete.<br/>
        To create a new threshold, click on the "Create New Threshold" link<br/>
        The lower section is for Expression-based Thresholds, where the value being checked is a mathematical expression including one or more data sources.  Functionality is identical to that for the Basic Thresholds section
        <br/>
        If you have a custom UEI for triggering or re-arming the threshold, then it will be a hyperlink.  Clicking on that link takes you to the notifications wizard for that UEI, allowing you to see existing notifications for that UEI, and possibly create a new notification for that UEI.
        </p>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

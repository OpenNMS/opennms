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
	import="org.opennms.web.admin.notification.noticeWizard.*"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" prefix="e"%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Event Notifications")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Configure Notifications", "admin/notification/index.jsp")
          .breadcrumb("Event Notifications")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<script type="text/javascript" >

    function editNotice(name)
    {
        document.notices.userAction.value="edit";
        document.notices.notice.value=name;
        document.notices.submit();
    }
    
    function deleteNotice(name)
    {
        if (confirm("Are you sure you want to delete the notification " + name + "?"))
        {
          document.notices.userAction.value="delete";
          document.notices.notice.value=name;
          document.notices.submit();
        }
    }
    
    function setStatus(name, status)
    {
        document.notices.userAction.value=status;
        document.notices.notice.value=name;
        document.notices.submit();
    }
    
    function newNotice()
    {
        document.notices.userAction.value="new";
        document.notices.submit();
    }
    
</script>

<form method="post" name="notices" action="admin/notification/noticeWizard/notificationWizard">
<input type="hidden" name="userAction" value=""/>
<input type="hidden" name="notice" value=""/>
<input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_NOTICES%>"/>

<div class="card">
  <div class="card-header">
    <h4 class="pull-left">Event Notifications</h4>
    <button class="pull-right btn btn-secondary" onclick="javascript:newNotice()"><i class="fa fa-plus"></i> Add New Event Notification</button>
  </div>
  <div class="card-body">
        <table class="table table-sm table-striped">
          <tr>
            <th colspan="3">
              Actions
            </th>
            <th>
              Notification
            </th>
            <th>
              Event
            </th>
            <th>
              UEI
            </th>
          </tr>
          <c:forEach items="${notifications}" var="notification">
          <tr>
            <td>
              <input type="button" class="btn btn-secondary" value="Edit" onclick="javascript:editNotice('${e:forJavaScript(notification.name)}')"/>
            </td>
            <td>
              <input type="button" class="btn btn-secondary" value="Delete"  onclick="javascript:deleteNotice('${e:forJavaScript(notification.name)}')"/>
            </td>
            <td>
            <c:choose>
              <c:when test="${notification.isOn}">
                <input type="radio" value="Off" onclick="javascript:setStatus('${e:forJavaScript(notification.name)}','off')"/>Off
                <input type="radio" value="On" CHECKED onclick="javascript:setStatus('${e:forJavaScript(notification.name)}','on')"/>On
              </c:when>
              <c:otherwise>
                <input type="radio" value="Off" CHECKED onclick="javascript:setStatus('${e:forJavaScript(notification.name)}','off')"/>Off
                <input type="radio" value="On" onclick="javascript:setStatus('${e:forJavaScript(notification.name)}','on')"/>On
              </c:otherwise>
            </c:choose>
            </td>
            <td>
            ${fn:escapeXml(notification.name)}
            </td>
            <td>
              ${fn:escapeXml(notification.eventLabel)}
            </td>
            <td>
              ${fn:escapeXml(notification.displayUei)}
            </td>
          </tr>
          </c:forEach>
        </table>
  </div>
</div> <!-- card -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

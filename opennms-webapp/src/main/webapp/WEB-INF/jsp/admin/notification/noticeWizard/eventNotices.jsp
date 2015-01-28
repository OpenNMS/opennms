<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
	import="org.opennms.web.admin.notification.noticeWizard.*"
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Event Notifications" />
  <jsp:param name="headTitle" value="Event Notifications" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/notification/index.jsp'>Configure Notifications</a>" />
  <jsp:param name="breadcrumb" value="Event Notifications" />
</jsp:include>

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

<h2>Event Notifications</h2>

<form method="post" name="notices" action="admin/notification/noticeWizard/notificationWizard">
<input type="hidden" name="userAction" value=""/>
<input type="hidden" name="notice" value=""/>
<input type="hidden" name="sourcePage" value="<%=NotificationWizardServlet.SOURCE_PAGE_NOTICES%>"/>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Add a notification to an event or edit an existing event notification</h3>
  </div>
  <table class="table table-condensed">
    <tr>
      <td> <input type="button" class="btn btn-default" value="Add New Event Notification" onclick="javascript:newNotice()"/>
    </tr>
    <tr>
      <td valign="top">
        <h4>Event Notifications</h4>
        <table class="table table-condensed table-striped">
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
              <input type="button" class="btn btn-default" value="Edit" onclick="javascript:editNotice('${notification.escapedName}')"/>
            </td>
            <td>
              <input type="button" class="btn btn-default" value="Delete"  onclick="javascript:deleteNotice('${notification.escapedName}')"/>
            </td>
            <td>
            <c:choose>
              <c:when test="${notification.isOn}">
                <input type="radio" value="Off" onclick="javascript:setStatus('${notification.escapedName}','off')"/>Off
                <input type="radio" value="On" CHECKED onclick="javascript:setStatus('${notification.escapedName}','on')"/>On
              </c:when>
              <c:otherwise>
                <input type="radio" value="Off" CHECKED onclick="javascript:setStatus('${notification.escapedName}','off')"/>Off
                <input type="radio" value="On" onclick="javascript:setStatus('${notification.escapedName}','on')"/>On
              </c:otherwise>
            </c:choose>
            </td>
            <td>
              ${notification.name}
            </td>
            <td>
              ${notification.eventLabel}
            </td>
            <td>
              ${notification.displayUei}
            </td>
          </tr>
          </c:forEach>
        </table>
      </td>
    </tr>
  </table>
</div> <!-- panel -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

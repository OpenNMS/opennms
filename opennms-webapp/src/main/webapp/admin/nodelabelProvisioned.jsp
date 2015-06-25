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
	import="org.opennms.web.servlet.MissingParameterException"
%>

<%
    String nodeId = request.getParameter("node");
    if (nodeId == null) {
        throw new MissingParameterException("node");
    }
    String foreignSource = request.getParameter("foreignSource");
    if (foreignSource == null) {
        throw new MissingParameterException("foreignSource");
    }
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Node Label Changed in Requisition" />
  <jsp:param name="headTitle" value="Node Label Changed in Requisition" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Node Label Changed in Requisition" />
  <jsp:param name="script" value="<script type='text/javascript' src='js/provisioningGroups.js'></script>" />
</jsp:include>

<form action="admin/provisioningGroups.htm" name="takeAction" method="post">
  <input type="hidden" name="groupName" size="20"/>
  <input type="hidden" name="action" value="addGroup" />
  <input type="hidden" name="actionTarget" value="" />
</form>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Node Label Changed in Requisition</h3>
  </div>
  <div class="panel-body">
    <p>
      This node was created as part of a requisition.
      The requested change to the node's label has been made and will be reflected immediately in the requisition, but will not take effect in the database until the next time the node or requisition is rescanned.
    </p>
    <p>
      If you want to re-import (or synchronize) the <strong><%=foreignSource%></strong> requisition, click on the "Synchronize" button. That will redirect you to the Requisitions page, after requesting re-synchronization of the requisition.
    </p>
    <p>
      <br/>
      <input type="button" class="btn btn-default" value="Synchronize" onclick="doAction('<%=foreignSource%>', 'import')" />
      <input type="button" class="btn btn-default" value="Return to Node Page" onclick="location='element/node.jsp?node=<%=nodeId%>'" />
    </p>
  </div> <!-- panel-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

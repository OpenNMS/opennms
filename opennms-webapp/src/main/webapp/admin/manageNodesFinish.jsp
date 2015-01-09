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
	import="org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.element.*,
		org.opennms.netmgt.model.OnmsNode
	"
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Manage/Unmanage Interfaces Finish" />
  <jsp:param name="headTitle" value="Manage Interfaces" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Manage/Unmanage Interfaces Finish" />
</jsp:include>

<%
	OnmsNode node = null;
	String nodeIdString = request.getParameter("node");
	if (nodeIdString != null) {
		try {
			int nodeId = WebSecurityUtils.safeParseInt(nodeIdString);
			node = NetworkElementFactory.getInstance(getServletContext()).getNode(nodeId);
		} catch (NumberFormatException e) {
			// ignore this, we just won't put a link if it fails
		}
	}
%>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title">Database Update Complete After Management Changes</h3>
  </div>
  <div class="panel-body">
    <p>
      These changes take effect immediately. OpenNMS does not need to be restarted.
    </p>

    <p>
      Changes for a specific node will become effective upon execution of
      a forced rescan on that node. The node must be up when rescanned for the
      inventory information to be updated.
    </p>

    <% if (node != null) { %>
    <p>
      <a href="element/rescan.jsp?node=<%= node.getId() %>">Rescan this node</a>
    </p>
    <p>
      <a href="element/node.jsp?node=<%= node.getId() %>">Return to node page</a>
    </p>
    <% } %>
  </div> <!-- panel-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>

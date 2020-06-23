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
	isErrorPage="true"
	import="org.opennms.web.admin.nodeManagement.*, org.opennms.web.utils.ExceptionUtils"
%>

<%
    NoManagedInterfacesException nmie = ExceptionUtils.getRootCause(exception, NoManagedInterfacesException.class);
%>


<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="headTitle" value="No Managed Interfaces for Node" />
  <jsp:param name="headTitle" value="Error" />
  <jsp:param name="breadcrumb" value="Error" />
</jsp:include>

<h1>No Managed Interfaces for Node</h1>

<p>
  The selected node has no interfaces that are included in an OpenNMS polling package. Therefore no interfaces or services on this node can be
  managed or unmanaged. Your OpenNMS administrator can add interfaces to a polling package.</p>

  <% if (nmie.getNodeListUri() != null) { %>
  <p>
  To select a different node, you can browse the
  <a href="<%=nmie.getNodeListUri()%>">node list</a>.
  </p>
  <% } %>
</p>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

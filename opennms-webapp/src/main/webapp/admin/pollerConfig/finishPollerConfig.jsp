<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Restart Pollers" />
  <jsp:param name="headTitle" value="Restart Pollers" />
  <jsp:param name="headTitle" value="Configure Pollers" />
  <jsp:param name="headTitle" value="Admin" />
  <jsp:param name="location" value="admin" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='admin/pollerConfig/index.jsp'>Configure Pollers</a>" />
  <jsp:param name="breadcrumb" value="Restart Pollers" />
</jsp:include>

<h3>The Pollers Need to be Restarted for the Changes to Take Effect</h3>

<p>
  Please click the &quot;Restart Pollers&quot; button below to have the
  pollers read the new configuration. If you want to make more poller
  configuration changes, please revisit the
  <a href="admin/pollerConfig/index.jsp">Configure Pollers</a> page.
</p>

<form method="post" action="admin/pollerConfig/finishPollerConfig">
  <input type="submit" value="Restart Pollers"/>
</form>

<jsp:include page="/includes/footer.jsp" flush="true"/>

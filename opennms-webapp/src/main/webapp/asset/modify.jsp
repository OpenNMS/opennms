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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.springframework.security.AclUtils"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
  <jsp:param name="title" value="Modify Asset" />
  <jsp:param name="headTitle" value="Modify" />
  <jsp:param name="headTitle" value="Asset" />
  <jsp:param name="breadcrumb" value="<a href ='asset/index.jsp'>Assets</a>" />
  <jsp:param name="breadcrumb" value="Modify" />
	<jsp:param name="meta">
	  <jsp:attribute name="value">
	    <meta name='gwt:module' content='org.opennms.gwt.web.ui.asset.Asset' />
	  </jsp:attribute>
	</jsp:param>
    <jsp:param name="meta">
	  <jsp:attribute name="value">
        <link media="screen" href="../css/dashboard.css" type="text/css" rel="stylesheet">
	  </jsp:attribute>
	</jsp:param>
	
</jsp:include>

<%
	AclUtils.NodeAccessChecker accessChecker = AclUtils.getNodeAccessChecker(getServletContext());

	Integer nodeId = null;

	try {
	    nodeId = Integer.valueOf(request.getParameter("node"));
	} catch (NumberFormatException e) {
%>
		<h2>Error parsing node parameter.</h2>
<%
	}

	if (nodeId != null) {
		if (accessChecker.isNodeAccessible(nodeId)) {
%>
			<div id="opennms-assetNodePage"></div>
<%
		} else {
%>
			<h2>Access denied.</h2>
<%
		}
	}
%>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

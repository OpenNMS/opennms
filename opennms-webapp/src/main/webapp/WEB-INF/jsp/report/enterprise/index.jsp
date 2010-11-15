<%--

/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Nov 12: Created jonathan@opennms.org
 * 
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

--%>
<%@page language="java"
    contentType="text/html"
    session="true"
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Enterprise Reports" />
  <jsp:param name="headTitle" value="Enterprise Reports" />
    <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
    <jsp:param name="breadcrumb" value="Enterprise" />
</jsp:include>

<opennms:EnterpriseReporting id="addReportBtn"/>
<div class="TwoColLeft" style="height:100%; width:100%;">
    <h3>Enterprise Reports</h3>
    <opennms:EnterpriseReporting id="reportList"/>
</div>

<jsp:include page="/includes/footer.jsp" flush="false"/>
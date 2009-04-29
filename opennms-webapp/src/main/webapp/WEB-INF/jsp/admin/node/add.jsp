<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com///

--%>

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@page import="java.util.Set"%>

<%@page import="org.opennms.netmgt.provision.persist.foreignsource.ForeignSource" %>
<%@page import="org.opennms.web.XssRequestWrapper" %>

<jsp:include page="/includes/header.jsp" flush="false" >
	<jsp:param name="title" value="Provision Node" />
	<jsp:param name="headTitle" value="Provisioning Groups" />
	<jsp:param name="headTitle" value="Add Node" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/provisioningGroups.htm'>Provisioning Groups</a>" />
	<jsp:param name="breadcrumb" value="Node Quick-Add" />
</jsp:include>

<br />

<c:if test="${success}">
	<div style="border: 1px solid black; background-color: #bbffcc; margin: 5px;">
		<h2>Success</h2>
		<p>Your node has been provisioned in the ${foreignSource} foreign source.</p>
	</div>
</c:if>

<c:choose>
<c:when test="${empty requisitions}">
	<h2>Missing Requisition</h2>
	<p>You must first <a href='admin/provisioningGroups.htm'>create and import a provisioning group</a> before using this page.</p>
</c:when>
<c:otherwise>
<h2><span style="font-color: red">Note: this <strong>will</strong> override any unimported modifications made to the provisioning group.</span></h2>
<form action="admin/node/add.htm">
	<input type="hidden" name="actionCode" value="add" />
	<table class="normal">
		<tr>
			<td>Provisioning Group:</td>
			<td>
				<select name="foreignSource">
					<c:forEach var="req" items="${requisitions}">
						<option><c:out value="${req.foreignSource}" /></option>
					</c:forEach>
				</select>
			</td>

			<td>Node Label:</td>
			<td><input type="text" name="nodeLabel" /></td>

			<td>IP Address:</td>
			<td><input type="text" name="ipAddress" /></td>
		</tr>
		<tr>
			<td>Category:</td>
			<td>
				<select name="category">
					<c:forEach var="cat" items="${categories}">
						<option><c:out value="${cat}" /></option>
					</c:forEach>
				</select>
			</td>

			<td>Category:</td>
			<td>
				<select name="category">
					<c:forEach var="cat" items="${categories}">
						<option><c:out value="${cat}" /></option>
					</c:forEach>
				</select>
			</td>

			<td colspan="2">&nbsp;</td>
		</tr>
		<tr>
			<td>Community String:</td>
			<td><input type="text" name="community" /></td>

			<td>Version</td>
			<td><select name="snmpVersion"><option>v1</option><option selected>v2c</option></select></td>

			<td colspan="2">&nbsp;</td>
		</tr>
		<tr>
			<td>Device Username:</td>
			<td><input type="text" name="deviceUsername" /></td>

			<td>Device Password:</td>
			<td><input type="text" name="devicePassword" /></td>
			
			<td>Enable Password:</td>
			<td><input type="text" name="enablePassword" /></td>
		</tr>
        <tr>
            <td>Access Method:</td>
            <td>
              <select name="accessMethod" >
                <option value="rsh">RSH</option>
                <option value="ssh">SSH</option>
                <option value="telnet" selected="true">Telnet</option>
              </select>  
            </td>
            <td>Auto Enable:</td>
            <td>
              <select name="autoEnable" >
                <option selected="true"></option>
                <option value="A">Yes</option>
              </select>
            </td>
        </tr>
	</table>
	<input type="submit" value="Provision" />
	<input type="reset" />
</form>

</c:otherwise>
</c:choose> <!--  empty requisitions -->

<br />

<jsp:include page="/includes/footer.jsp" flush="false" />

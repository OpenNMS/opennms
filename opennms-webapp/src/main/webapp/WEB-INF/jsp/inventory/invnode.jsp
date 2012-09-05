<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
	session="true"%>
<%@page language="java" contentType="text/html" session="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>



<jsp:include page="/includes/header.jsp" flush="false" >
<jsp:param name="title" value="Inventory" />
<jsp:param name="headTitle" value="${model.id}" />
<jsp:param name="headTitle" value="Inventory" />
<jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<a href='element/node.jsp?node=${model.db_id}'>Node</a>" />
  <jsp:param name="breadcrumb" value="<a href='inventory/rancid.htm?node=${model.db_id}'>Rancid</a>" />
<jsp:param name="breadcrumb" value="Inventory" />
</jsp:include>


<div class="TwoColLeft">
	<!-- general info box -->
	<h3>General (Status: ${model.status_general})</h3>
  	<table class="o-box">
  		<tr>
	  		<th>Node</th>
	  		<td><a href="element/node.jsp?node=${model.db_id}">${model.id}</a></td>
	  	</tr>
 		<tr>
	  		<th>Requisition Name</th>
	  		<td>${model.foreignSource}</td>
	  	</tr>
	  	<tr>
	  		<th>RWS status</th>
	  		<td>${model.RWSStatus}</td>
	  	</tr>
	</table>

	<h3>Rancid info</h3>
  <table class="o-box">
	<tr>
		<th>Group Name</th>
		<td>${model.groupname}</td>
	</tr>
	<tr>
		<th>Version</th>
		<td>${model.version}</td>
	</tr>
	<tr>
		<th>Rancid Name</th>
		<td>${model.devicename}</td>
	</tr>
	<tr>
	    <th>Status</th>
	    <td>${model.status}</td>
	</tr>
	<tr>
	    <th>Creation Date</th>
	    <td>${model.creationdate}</td>
	</tr>
   </table>
	
	 <h3>Configuration info</h3>

	 <table class="o-box">
		<tr>
	    <th>Host(version)</th>
		</tr>
	    <tr>
		<td>${model.id}(${model.version})
		<a href="inventory/rancidViewVc.htm?node=${model.db_id}&groupname=${model.groupname}&viewvc=${model.configurationurl}">(configuration)</a>
		</td>
		</tr>
	</table>
</div>
<div class="TwoColRight">
<!-- general info box -->
<h3>Inventory Items</h3>

	<c:forEach items="${model.inventory}" var="invel" varStatus="status">
	<h3>Item ${status.count}</h3>
	<table class="o-box">
		<c:forEach items="${invel.tupleList}" var="tup">
		<tr>
			<th width="50%">${tup.name}</th>
			<td>${tup.description}</td>
		</tr>
		</c:forEach>

		<c:forEach items="${invel.softwareList}" var="sof">
		<tr>
			<th width="50%">Software: ${sof.type}</th>
			<td>Version: ${sof.version}</td>
		</tr>
		</c:forEach>

		<c:forEach items="${invel.memoryList}" var="mem">
		<tr>
			<th width="50%">Memory: ${mem.type}</th>
			<td>Size: ${mem.size}</td>
		</tr>
		</c:forEach>
		</table>
	</c:forEach>
</div>

<jsp:include page="/includes/footer.jsp" flush="false" />

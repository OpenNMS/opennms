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
  <jsp:param name="title" value="Rancid" />
  <jsp:param name="headTitle" value="${model.id}" />
  <jsp:param name="headTitle" value="Rancid" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<a href='element/node.jsp?node=${model.db_id}'>Node</a>" />
  <jsp:param name="breadcrumb" value="Rancid" />
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

	<h3>Rancid Info</h3>
	<table class="o-box">
		<tr>
			<th>Device Name</th>
			<td>${model.id}</td>
		</tr>	
		<tr>
			<th>Device Type</th>
			<td>${model.devicetype}</td>
		</tr>
		<tr>
			<th>Comment</th>
			<td>${model.comment}</td>
		</tr>
		<tr>
			<th>Status</th>
			<td>${model.status}</td>
		</tr>
	</table>

</div>

<div class="TwoColRight">
<!-- Inventory info box -->
	<h3>Inventory Elements</h3>
	
	<table class="o-box">
	<tr>
		<th>Group</th>
		<th>Total Revisions</th>
		<th>Last Version</th>
		<th>Last Update</th>
	</tr>
	<c:forEach items="${model.grouptable}" var="groupelm" begin ="0" end="9">
		<tr>
			<td>${groupelm.group}
			<a href="inventory/rancidViewVc.htm?node=${model.db_id}&groupname=${groupelm.group}&viewvc=${groupelm.rootConfigurationUrl}">(configurations)</a>
			</td>
			<td>${groupelm.totalRevisions} <a href="inventory/rancidList.htm?node=${model.db_id}&groupname=${groupelm.group}">(list)</a></td>
			<td>${groupelm.headRevision}
			<a href="inventory/invnode.htm?node=${model.db_id}&groupname=${groupelm.group}&version=${groupelm.headRevision}">(inventory)</a>
			</td>
			<td>${groupelm.creationDate}</td>
		</tr>
	</c:forEach>
	<tr>
		<th colspan="4" ><a href="inventory/rancidList.htm?node=${model.db_id}&groupname=*">entire group list...</a></th>
	</tr>
	</table>

<!-- Software image box -->
	<h3>Software Images Stored</h3>
	
	<table class="o-box">
	<tr>
		<th>Name</th>
		<th>Size</th>
		<th>Last Modified</th>
	</tr>
	<c:forEach items="${model.bucketitems}" var="swimgelem">
		<tr>
			<td>${swimgelem.name}
<a href="${model.url}/storage/buckets/${model.id}?filename=${swimgelem.name}">(download)</a>
			</td>
			<td>${swimgelem.size}</td>
			<td>${swimgelem.lastModified}</td>
		</tr>
	</c:forEach>
	</table>

</div>
<jsp:include page="/includes/footer.jsp" flush="false" />

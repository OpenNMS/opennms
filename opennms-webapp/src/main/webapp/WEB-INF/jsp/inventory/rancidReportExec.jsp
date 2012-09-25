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
  <jsp:param name="title" value="Creating Inventory Report" />
  <jsp:param name="headTitle" value="Creating Inventory Report " />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='inventory/rancidReport.htm'>Inventory Reports</a>" />
  <jsp:param name="breadcrumb" value="Rancid Reports" />
</jsp:include>

<div class="TwoColLeft">
    <!-- general info box -->
    <h3>Report in progress</h3>
    <table class="o-box">
		<tr> 
		<th>Report Type</th>
        <td>${type}</td>
		</tr>
		<tr> 
		<th>Report Date</th>
        <td>${date}</td>
		</tr>
		<tr> 
		<th>Search field</th>
        <td>${searchfield}</td>
		</tr>
		<tr> 
		<th>File format</th>
        <td>${reportformat}</td>
		</tr>
		
</table>
</div>

  <div class="TwoColRight">
    <h3>Descriptions</h3>
    <div class="boxWrapper">
    <p>
    OpenNMS is processing the report in background because it can take a while.
    An email with the report attached will be send to the specified user when finished. 
    </p>
</div>
</div>


<jsp:include page="/includes/footer.jsp" flush="false" />

<script type="text/javascript">

</script>


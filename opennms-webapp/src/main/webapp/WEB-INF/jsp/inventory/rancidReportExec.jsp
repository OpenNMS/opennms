<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
		<td>Report Type</td>
        <td>${type}</td>
		</tr>
		<tr> 
		<td>Report Date</td>
        <td>${date}</td>
		</tr>
		<tr> 
		<td>Search field</td>
        <td>${searchfield}</td>
		</tr>
		<tr> 
		<td>File format</td>
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

<script language="JavaScript">

</script>


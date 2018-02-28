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
	session="true"%>
<%@page language="java" contentType="text/html" session="true" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Creating Inventory Report" />
  <jsp:param name="headTitle" value="Creating Inventory Report " />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='inventory/rancidReport.htm'>Inventory Reports</a>" />
  <jsp:param name="breadcrumb" value="Rancid Reports" />
</jsp:include>

<div class="row">
    <div class="col-md-6">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">Report in progress</h3>
            </div>
            <table class="table table-condensed table-bordered">
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

    </div>

    <div class="col-md-6">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">Descriptions</h3>
            </div>
            <div class="panel-body">
                <p>
                    OpenNMS is processing the report in background because it can take a while.
                    An email with the report attached will be send to the specified user when finished.
                </p>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

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
  <jsp:param name="title" value="Rancid" />
  <jsp:param name="headTitle" value="${model.id}" />
  <jsp:param name="headTitle" value="Inventory List" />
  <jsp:param name="breadcrumb" value="<a href='element/index.jsp'>Search</a>" />
  <jsp:param name="breadcrumb" value="<a href='element/node.jsp?node=${model.db_id}'>Node</a>" />
  <jsp:param name="breadcrumb" value="<a href='inventory/rancid.htm?node=${model.db_id}'>Rancid</a>" />
  <jsp:param name="breadcrumb" value="Inventory List" />
  
</jsp:include>
<div class="row">
    <div class="col-md-12">
        <h2> Node: ${model.id} </h2>
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">RWS status</h3>
            </div>

            <table class="table table-condensed table-bordered">
                <tr>
                    <td>${model.RWSStatus}</td>
                </tr>
            </table>
        </div>

        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">Associated Elements</h3>
            </div>

            <table class="table table-condensed table-bordered">
                <tr>
                    <th>Group</th>
                    <th>Version</th>
                    <th>Revision Date</th>
                </tr>
                <c:forEach items="${model.grouptable}" var="groupelm">
                    <tr>
                        <td>${groupelm.group}
                        </td>
                        <td>${groupelm.version}
                            <a href="inventory/invnode.htm?node=${model.db_id}&groupname=${groupelm.group}&version=${groupelm.version}">(inventory)</a>
                            <a href="inventory/rancidViewVc.htm?node=${model.db_id}&groupname=${groupelm.group}&viewvc=${groupelm.urlViewVC}">(configuration)</a>
                        </td>
                        <td>${groupelm.date}</td>

                    </tr>
                </c:forEach>
            </table>

        </div>
    </div>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

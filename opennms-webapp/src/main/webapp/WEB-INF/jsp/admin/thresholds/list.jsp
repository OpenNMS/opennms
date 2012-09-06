<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page contentType="text/html;charset=UTF-8" language="java" import="org.opennms.web.api.Util" %>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Thresholds Configuration" />
	<jsp:param name="headTitle" value="List" />
	<jsp:param name="headTitle" value="Thresholds" />
	<jsp:param name="headTitle" value="Admin" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
    <jsp:param name="breadcrumb" value="Threshold Groups" />
</jsp:include>

<h3>Threshold Configuration</h3>

<form method="post" name="allGroups">
<table class="standard">
        <tr>
                <th class="standardheader">Name</th>
                <th class="standardheader">RRD Repository</th>
                <th class="standardheader">&nbsp;</th>
        </tr>
        <c:forEach var="mapEntry" items="${groupMap}">
                <tr>
                        <td class="standard">${mapEntry.key}</td>
                        <td class="standard">${mapEntry.value.rrdRepository}</td>
                        <td class="standard"><a href="<%= Util.calculateUrlBase(request, "admin/thresholds/index.htm") %>?groupName=${mapEntry.key}&editGroup">Edit</a></td>
                </tr>
        </c:forEach>
</table>
</form>
<script type="text/javascript">
function doReload() {
    if (confirm("Are you sure you want to do this?")) {
        document.location = "<%= Util.calculateUrlBase(request, "admin/thresholds/index.htm") %>?reloadThreshdConfig";
    }
}
</script>
<input type="button" onclick="doReload()" value="Request a reload threshold packages configuration"/>
<jsp:include page="/includes/footer.jsp" flush="false" />

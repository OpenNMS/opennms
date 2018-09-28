<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Node List" />
	<jsp:param name="headTitle" value="Node List" />
	<jsp:param name="breadcrumb" value="Node List" />
</jsp:include>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="node-elementList" />
</jsp:include>

<!-- NMS-7099: Add custom javascripts AFTER the header was included -->
<script type="text/javascript">
    function toggleClassDisplay(clazz, displayA, displayB) {
        var targetElems = document.querySelectorAll("." + clazz);
        for (var i = 0; i < targetElems.length; i++) {
            var e = targetElems[i];
            if (e.style.display == displayA) {
                e.style.display = displayB;
            } else {
                e.style.display = displayA;
            }
        }
    }
</script>

<style>
.dropdown-menu {
	padding: 5px;
}

.dropdown-menu > li > a {
	padding: 5px;
}

.dropdown-menu > .active > a
{
	color: white !important;
	background-color: #4c9d29 !important;
}
</style>

<onms-node-list></onms-node-list>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

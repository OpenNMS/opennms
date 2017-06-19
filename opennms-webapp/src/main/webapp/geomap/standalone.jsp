<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

<%@page language="java" contentType="text/html" session="true" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
    <jsp:param name="title" value="Geographcial Map" />
    <jsp:param name="nobreadcrumbs" value="true" />
    <jsp:param name="useionicons" value="true" />
</jsp:include>

<div id="map-container">
    <style>
        /* overwrite styles to make it fullscreen */
        #footer {
            margin-right: 0;
        }
        .container-fluid {
            padding: 0;
        }
    </style>
    <jsp:include page="includes/map.jsp" flush="false" >
        <jsp:param name="height" value="100%" />
        <jsp:param name="mapId" value="map" />
    </jsp:include>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<script type="text/javascript">
    /* apply maximal height in px */
    function refresh() {
        var height = $(window).height() - $("#footer").outerHeight() - $("#header").outerHeight();
        $("#map-container").height(height);
    }

    $(window).resize(function() {
        refresh();
    });
    $(document).ready(function() {
        refresh();
    });
</script>

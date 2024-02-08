<%--

    Licensed to The OpenNMS Group, Inc (TOG) under one or more
    contributor license agreements.  See the LICENSE.md file
    distributed with this work for additional information
    regarding copyright ownership.

    TOG licenses this file to You under the GNU Affero General
    Public License Version 3 (the "License") or (at your option)
    any later version.  You may not use this file except in
    compliance with the License.  You may obtain a copy of the
    License at:

         https://www.gnu.org/licenses/agpl-3.0.txt

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
    either express or implied.  See the License for the specific
    language governing permissions and limitations under the
    License.

--%>
<%@page language="java" contentType="text/html" session="true" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .flags("nobreadcrumbs", "useionicons")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

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

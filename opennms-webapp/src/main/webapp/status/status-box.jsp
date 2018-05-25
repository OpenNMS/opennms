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

<%@ page language="java" contentType="text/html" session="true" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<%
    final String graphs = System.getProperty("org.opennms.statusbox.elements", "business-services,nodes-by-alarms,nodes-by-outages");
    final String[] graphKeys = graphs.split(",");
    final List<String> graphKeyList = new ArrayList<>();
    for (String eachGraphKey : graphKeys) {
        if (eachGraphKey != null && !eachGraphKey.isEmpty()) {
            graphKeyList.add("'" + eachGraphKey + "'");
        }
    }
    String javaScriptArrayContent = String.join(", ", graphKeyList);
%>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="status-box" />
</jsp:include>

<div id="status-overview-box" class="panel panel-default fix-subpixel" style="display: none;">
    <div class="panel-heading">
        <h3 class="panel-title">Status Overview</h3>
    </div>
    <div class="panel-body">
        <div id="chart-content" class="row row-centered">
        </div>
    </div>
</div>

<script type="application/javascript">
document.addEventListener('DOMContentLoaded', function() {
	var graphs = [ <%= javaScriptArrayContent %> ];
	window.renderStatusGraphs(graphs);
});
</script>

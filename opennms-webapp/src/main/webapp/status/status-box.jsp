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

<div id="status-overview-box" class="card fix-subpixel" style="display: none;">
    <div class="card-header">
        <span>Status Overview</span>
    </div>
    <div class="card-body">
        <div id="chart-content" class="row mx-auto">
        </div>
    </div>
</div>

<script type="application/javascript">
document.addEventListener('DOMContentLoaded', function() {
	var graphs = [ <%= javaScriptArrayContent %> ];
	window.renderStatusGraphs(graphs);
});
</script>

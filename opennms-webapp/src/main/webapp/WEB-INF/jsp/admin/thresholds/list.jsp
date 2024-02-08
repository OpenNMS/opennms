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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%@ page contentType="text/html;charset=UTF-8" language="java" import="org.opennms.web.api.Util" %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("List")
          .headTitle("Thresholds")
          .headTitle("Admin")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Threshold Groups")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="card">
  <div class="card-header">
    <span>Threshold Configuration</span>
  </div>
    <form method="post" name="allGroups">
      <table class="table table-sm table-striped table-hover">
        <tr>
                <th>Name</th>
                <th>RRD Repository</th>
                <th>&nbsp;</th>
        </tr>
        <c:forEach var="mapEntry" items="${groupMap}">
                <tr>
                        <td>${mapEntry.key}</td>
                        <td>${mapEntry.value.rrdRepository}</td>
                        <td><a href="<%= Util.calculateUrlBase(request, "admin/thresholds/index.htm") %>?groupName=${mapEntry.key}&editGroup">Edit</a></td>
                </tr>
        </c:forEach>
      </table>
    </form>
</div> <!-- panel -->

<script type="text/javascript">
function doReload() {
    if (confirm("Are you sure you want to do this?")) {
        document.location = "<%= Util.calculateUrlBase(request, "admin/thresholds/index.htm") %>?reloadThreshdConfig";
    }
}
</script>
<button type="button" class="btn btn-secondary mb-4" onclick="doReload()">Reload Threshold Configuration</button>
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

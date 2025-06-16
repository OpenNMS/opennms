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
<%@page language="java"
        contentType="text/html"
        session="true"
%>

<div id="onms-search" ng-app="onms.default.apps">

  <div class="card">
    <div class="card-header">
      <span><a href="graph/index.jsp">Resource Graphs</a></span>
    </div>
    <div class="card-body">
      <onms-search-nodes />
    </div>
  </div>

  <div class="card">
    <div class="card-header">
      <span><a href="KSC/index.jsp">KSC Reports</a></span>
    </div>
    <div class="card-body">
      <onms-search-ksc />
    </div>
  </div>

</div>

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="search" />
</jsp:include>

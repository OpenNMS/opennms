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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Resource Graphs")
          .headTitle("Reports")
          .breadcrumb("Reports", "report/index.jsp")
          .breadcrumb("Resource Graphs")
          .ngApp("onms-resources")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="container-fluid" ng-controller="NodeListCtrl">

  <div growl></div>

  <div class="jumbotron" ng-show="!loaded">
    <div class="container">
      <h1>Loading Resources</h1>
      <p>Please wait while all the resources are loaded into your browser.</p>
      <p>This could take time, depending on the number of resources for this node.</p>
    </div>
  </div>

  <div class="jumbotron" ng-show="loaded && !hasResources">
    <div class="container">
      <h1>There is no collected data</h1>
    </div>
  </div>

  <div class="row" ng-show="hasResources">
    <div class="col-md-6">
      <div class="card">
        <div class="card-header">
          <span>Resources</span>
        </div>
        <div class="card-body">
          <div class="radio">
            <label>
              <input type="radio" name="reportTarget" value="graph/results.htm" ng-model="endUrl"/>
              Standard Resource Performance Reports
            </label>
            <br/>
            <br/>
            <label>
              <input type="radio" name="reportTarget" value="graph/adhoc2.jsp" ng-model="endUrl"/>
              Custom Resource Performance Reports
            </label>
            <br/>
            <br/>
          </div>
          <div class="table-responsive">
            <div class="input-group">
              <div class="input-group-prepend">
                <div class="input-group-text">
                  <i class="fas fa-magnifying-glass"></i>
                </div>

              </div>
              <input class="form-control" type="text" placeholder="Search/Filter Resources" ng-model="resourceFilter"></input>
            </div>

            <table class="table table-striped table-sm table-hover">
              <thead>
                <tr>
                  <th>Resources</th>
                </tr>
              </thead>
              <tbody>
                <tr ng-repeat="resource in filteredResources | startFrom:(currentPage-1)*pageSize | limitTo:pageSize">
                  <td>
                    <a ng-click="goTo(resource.id)" href>{{ resource.label }}</a>
                  </td>
                </tr>
             </tbody>
            </table>
          </div>
          <ul uib-pagination class="pagination-sm"
            total-items="totalItems"
            num-pages="numPages"
            ng-model="currentPage"
            max-size="maxSize"
            boundary-links="true"
            ng-show="filteredResources.length > pageSize"></ul>
        </div>
      </div>
    </div>
    <div class="col-md-6">
      <div class="card">
        <div class="card-header">
          <span>Network Performance Data</span>
        </div>
        <div class="card-body">
          <p>
          The <strong>Standard Performance Reports</strong> provide a stock way to
          easily visualize the critical SNMP data collected from managed nodes
          and interfaces throughout your network.
          </p>
          <p>
          <strong>Custom Performance Reports</strong> can be used to produce a
          single graph that contains the data of your choice from a single
          interface or node.  You can select the timeframe, line colors, line
           styles, and title of the graph.
          </p>
        </div>
      </div>
    </div>
  </div>

</div>

<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="angular-js" />
</jsp:include>
<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="onms-resources" />
</jsp:include>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

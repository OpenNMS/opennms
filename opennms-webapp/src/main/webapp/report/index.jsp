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


<%@page import="org.opennms.core.resource.Vault"%>
<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Reports")
          .breadcrumb("Reports")
          .ngApp("onms.default.apps")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-md-5">
    <div class="card">
        <div class="card-header">
            <span>Reports</span>
        </div>
        <div class="card-body" id="onms-search">
            <div class="row">
                <div class="col-md-12">
                    <div class="pull-right">
                        <form class="form-inline" role="form" name="resourceGraphs">
                            <div class="form-group">
                                <label class="sr-only">Resource Graphs for Node</label>
                                <p class="form-control-static">Resource Graphs for Node</p>
                            </div>
                            <onms-search-nodes />
                        </form>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-md-12">
                </div>
            </div>
        </div>
        <!-- more reports will follow -->
    </div>
  </div>

  <div class="col-md-7">
    <div class="card">
        <div class="card-header">
            <span>Descriptions</span>
        </div>
        <div class="card-body">
            <p><b>Resource Graphs</b> provide an easy way to visualize the critical
                SNMP, response time, and other data collected from managed nodes
                throughout your network.
            </p>
            <p>You may narrow your selection of resources by entering a search
                string in the "Name contains" field. This will invoke a case-insensitive
                substring match on resource names.
            </p>
            <p>You may narrow your selection of resources by entering a search string
                in the "Name contains" field. This will invoke a case-insensitive substring
                match on resource names.
            </p>

            <p><b>Database Reports</b> provide graphical or numeric
                view of your service level metrics for the current
                month-to-date, previous month, and last twelve months by categories.
            </p>

            <p><b>Statistics Reports</b> provide regularly scheduled statistical
                reports on collected numerical data (response time, SNMP performance
                data, etc.).
            </p>
        </div>
    </div>
  </div>
</div>
  <hr />

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="search" />
</jsp:include>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

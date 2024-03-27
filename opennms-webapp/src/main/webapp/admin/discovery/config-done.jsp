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

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Discovery")
          .headTitle("Admin")
          .breadcrumb("Admin","admin/index.jsp")
          .breadcrumb("Discovery", "admin/discovery/index.jsp")
          .breadcrumb("Configuration Updated")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="card">
  <div class="card-header">
    <span>Discovery Configuration Updated</span>
  </div>
  <div class="card-body">
    <p>
      The discovery configuration has been updated successfully and the discovery subsystem has been reloaded. The next scan
      will begin after the configured initial sleep time.
    </p>

    <p><a href='admin/index.jsp'>Return to the admin page</a></p>
    <p><a href='admin/discovery/index.jsp'>Return to the discovery configuration page</a></p>

  </div> <!-- card-body -->
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />


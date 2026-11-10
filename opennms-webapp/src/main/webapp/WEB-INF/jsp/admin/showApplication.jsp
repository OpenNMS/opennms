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
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Application")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Applications", "admin/applications.htm")
          .breadcrumb("Show")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-md-8">
    <div class="card">
      <div class="card-header">
        <span>Application: ${fn:escapeXml(model.application.name)}</span>
      </div>
      <div class="card-body">
        <p>
        Application '${fn:escapeXml(model.application.name)}' has ${fn:length(model.memberServices)} services.
        </p>

        <p>
        <a href="admin/applications.htm?edit=services&applicationid=${model.application.id}">Edit application</a>
        </p>

        <p>
          <h5>Defined service(s):</h5>

          <table class="table table-sm">
            <tr>
              <th>Node</th>
              <th>Interface</th>
              <th>Service</th>
            </tr>
            <c:forEach items="${model.memberServices}" var="service">
              <tr>
                <td><a href="element/node.jsp?node=${service.ipInterface.node.id}">${fn:escapeXml(service.ipInterface.node.label)}</a></td>
                <td><a href="element/interface.jsp?ipinterfaceid=${service.ipInterface.id}">${service.ipInterface.ipAddress.hostAddress}</a></td>
                <td><a href="element/service.jsp?ifserviceid=${service.id}">${fn:escapeXml(service.serviceName)}</a></td>
              </tr>
            </c:forEach>
          </table>
        </p>

        <p>
          <h5>Associated location(s):</h5>

          <table class="table table-sm">
            <tr>
              <th>Location</th>
            </tr>
            <c:forEach items="${model.memberLocations}" var="location">
              <tr>
                <td>${location.locationName}</td>
              </tr>
            </c:forEach>
          </table>
        </p>

      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

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
          .headTitle("Category")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Application", "admin/applications.htm")
          .breadcrumb("Show")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<form action="admin/applications.htm" method="get">
  <input type="hidden" name="ifserviceid" value="${model.service.id}"/>
  <input type="hidden" name="edit" value=""/>

<div class="row">
  <div class="col-md-12">
    <div class="card">
      <div class="card-header">
        <span>Edit applications on ${fn:escapeXml(model.service.serviceName)}</span>
      </div>
      <div class="card-body">
        <p>
        Service <a href="<c:url value='element/service.jsp?ifserviceid=${model.service.id}'/>">${fn:escapeXml(model.service.serviceName)}</a>
        on interface <a href="<c:url value='element/interface.jsp?ipinterfaceid=${model.service.ipInterface.id}'/>">${model.service.ipAddressAsString}</a>
        of node <a href="<c:url value='element/node.jsp?node=${model.service.ipInterface.node.id}'/>">${fn:escapeXml(model.service.ipInterface.node.label)}</a>
        (node ID: ${model.service.ipInterface.node.id}) has ${fn:length(model.service.applications)} applications.
        </p>
        <div class="row">
          <div class="col-md-5">
            <label>Available applications</label>
            <select name="toAdd" size="20" class="form-control" multiple>
              <c:forEach items="${model.applications}" var="application">
                <option value="${application.id}">${fn:escapeXml(application.name)}</option>
              </c:forEach>
            </select>
          </div> <!-- column -->
          <div class="col-md-2 my-auto text-center">
            <input type="submit" name="action" class="btn btn-secondary" value="&#139;&#139; Remove"/>
            <input type="submit" name="action" class="btn btn-secondary" value="Add &#155;&#155;"/>
          </div> <!-- column -->
          <div class="col-md-5">
            <label>Applications on service</label>
            <select name="toDelete" size="20" class="form-control" multiple>
              <c:forEach items="${model.sortedApplications}" var="application">
                <option value="${application.id}">${fn:escapeXml(application.name)}</option>
              </c:forEach>
            </select>
          </div> <!-- column -->
        </div> <!-- row -->
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

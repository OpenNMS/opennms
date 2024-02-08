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
          .breadcrumb("Categories", "admin/categories.htm")
          .breadcrumb("Show")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-md-6">
    <div class="card">
      <div class="card-header">
        <span>Surveillance Category: ${fn:escapeXml(model.category.name)}</span>
      </div>
      <div class="card-body">
        <p>
        Category '${fn:escapeXml(model.category.name)}' has ${fn:length(model.memberNodes)} nodes.
        </p>

        <p>
        <a href="admin/categories.htm?edit&categoryid=${model.category.id}">Edit category</a>
        </p>

        <table class="table table-sm">
          <tr>
            <th>Node</th>
          </tr>
          <c:forEach items="${model.memberNodes}" var="node">
            <tr>
              <td><a href="element/node.jsp?node=${node.id}">${fn:escapeXml(node.label)}</a></td>
            </tr>
          </c:forEach>
        </table>
      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

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
          .headTitle("Applications")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("Applications")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

      <div class="card-header">
        <span>Applications</span>
      </div>
      <table class="table table-sm table-responsive">
        <tr>
          <th>Delete</th>
          <th>Edit</th>
          <th>Application</th>
        </tr>
        <c:forEach items="${applications}" var="app">
        <tr>
          <td><a href="admin/applications.htm?removeApplicationId=${app.id}"><i class="fa fa-trash-o fa-2x"></i></a></td>
          <td><a href="admin/applications.htm?applicationid=${app.id}&edit=services"><i class="fa fa-edit fa-2x"></i></a></td>
          <td><a href="admin/applications.htm?applicationid=${app.id}">${fn:escapeXml(app.name)}</a></td>
        </tr>
        </c:forEach>
        <tr>
          <td colspan="3">
            <form class="form-inline mt-4" action="admin/applications.htm">
              <div class="form-group">
                <input type="textfield" class="form-control" placeholder="Application name" name="newApplicationName" size="40"/>
              </div>
              <button type="submit" name="newApplicationSubmit" class="btn btn-secondary ml-2"><i class="fa fa-plus"></i> Add New Application</button>
            </form>
          </td>
        </tr>
      </table>
    </div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

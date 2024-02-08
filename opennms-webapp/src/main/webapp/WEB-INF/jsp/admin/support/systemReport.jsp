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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("System Reports")
          .breadcrumb("Support", "support/index.htm")
          .breadcrumb("System Reports")
          .breadcrumb("${report.type}")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="card">
  <div class="card-header">
    <span>Plugins</span>
  </div>
  <div class="card-body">
    <p>Choose which plugins to enable:</p>
    <c:forEach items="${report.plugins}" var="plugin">
     <c:out value="using plugin ${plugin.name}" />
    </c:forEach>
  </div>
</div> <!-- panel -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

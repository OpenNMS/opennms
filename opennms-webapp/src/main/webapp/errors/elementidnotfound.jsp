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
	isErrorPage="true"
	import="org.opennms.web.element.*, org.opennms.web.utils.ExceptionUtils"
%>
<%@page import="org.opennms.core.utils.WebSecurityUtils" %>

<%
    ElementIdNotFoundException einfe = ExceptionUtils.getRootCause(exception, ElementIdNotFoundException.class);
%>


<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("ID Not Found for " + einfe.getElemType())
          .headTitle("Error")
          .breadcrumb("Error")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<h1><%=einfe.getElemType(true)%> ID Not Found</h1>

<p>
  The <%=einfe.getElemType()%> ID <%=einfe.getBadID()%> is invalid. <%=WebSecurityUtils.sanitizeString(einfe.getMessage())%>
  <br/>
  <% if (einfe.getDetailUri() != null) { %>
  <p>
  To search again by <%=einfe.getElemType()%> ID, enter the ID here:
  </p>

  <form role="form" method="get" action="<%=einfe.getDetailUri()%>" class="form">
    <div class="row">
      <div class="form-group col-md-2">
        <label for="input_text">Get&nbsp;details&nbsp;for&nbsp;<%=einfe.getElemType()%>&nbsp;ID</label>
        <input type="text" class="form-control" id="input_text" name="<%=einfe.getDetailParam()%>"/>
      </div>
    </div>
    <button type="submit" class="btn btn-secondary">Search</button>
  </form>
  <% } %>
  
  <% if (einfe.getBrowseUri() != null) { %>
  <p>
  To find the <%=einfe.getElemType()%> you are looking for, you can
  browse the <a href="<%=einfe.getBrowseUri()%>"><%=einfe.getElemType()%> list</a>.
  </p>
  <% } %>
</p>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

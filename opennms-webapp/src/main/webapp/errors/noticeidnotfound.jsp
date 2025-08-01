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
	import="org.opennms.web.notification.*, org.opennms.web.utils.ExceptionUtils"
%>

<%
    NoticeIdNotFoundException ninfe = ExceptionUtils.getRootCause(exception, NoticeIdNotFoundException.class);
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Notice ID Not Found")
          .headTitle("Error")
          .breadcrumb("Error")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<h1>Notice ID Not Found</h1>

<p>
  The notice ID <%=ninfe.getBadID()%> is invalid. <%=ninfe.getMessage()%>
  <br/>
  You can re-enter it here or
  <a href="notification/browse?acktype=unack">browse all of the notices</a>
  to find the notice you are looking for.
</p>

<form role="form" method="get" action="notification/detail.jsp" class="form mb-4">
  <div class="row">
    <div class="form-group col-md-2">
      <label for="input_notice">Get&nbsp;details&nbsp;for&nbsp;Notice&nbsp;ID</label>
      <input type="text" class="form-control" id="input_notice" name="notice"/>
    </div>
  </div>
  <button type="submit" class="btn btn-secondary">Search</button>
</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

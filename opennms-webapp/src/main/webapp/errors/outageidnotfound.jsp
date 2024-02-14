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
	import="org.opennms.web.outage.*, org.opennms.web.utils.ExceptionUtils"
%>

<%
     OutageIdNotFoundException einfe = ExceptionUtils.getRootCause(exception, OutageIdNotFoundException.class);
%>


<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Outage ID Not Found")
          .headTitle("Error")
          .breadcrumb("Error")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<h1>Outage ID Not Found</h1>

<p>
  The outage ID <%=einfe.getBadID()%> is invalid. <%=einfe.getMessage()%>
  <br/>
  You can re-enter it here or <a href="outage/list.htm">browse all of the
  outages</a> to find the outage you are looking for.
</p>

<form role="form" method="get" action="outage/detail.htm" class="form mb-4">
  <div class="row">
    <div class="form-group col-md-2">
      <label for="input_id">Get&nbsp;details&nbsp;for&nbsp;Outage&nbsp;ID</label>
      <input type="text" class="form-control" id="input_id" name="id"/>
    </div>
  </div>
  <button type="submit" class="btn btn-secondary">Search</button>
</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

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
	import="org.opennms.web.alarm.*, org.opennms.web.utils.ExceptionUtils"
%>

<%
    AlarmIdNotFoundException einfe = ExceptionUtils.getRootCause(exception, AlarmIdNotFoundException.class);
%>


<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Alarm ID Not Found")
          .headTitle("Error")
          .breadcrumb("Error")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<h1>Alarm Cleared or Not Found</h1>

<p>
  <%=einfe.getMessage()%>. The alarm has been cleared or has an invalid alarm ID.
  <br/>
  Re-enter the alarm ID below or <a href="alarm/list.htm?acktyp=unack">browse all
   alarms</a> to find the alarm you are looking for.
   <br /> If you get the same error message,
   you can assume that the alarm has been cleared.
</p>

<form role="form" method="post" action="alarm/detail.htm" class="form mb-4">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
  <div class="row">
    <div class="form-group col-md-2">
      <label for="input_id">Get&nbsp;details&nbsp;for&nbsp;Alarm&nbsp;ID</label>
      <input type="text" class="form-control" id="input_id" name="id"/>
    </div>
  </div>
  <button type="submit" class="btn btn-secondary">Search</button>
</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

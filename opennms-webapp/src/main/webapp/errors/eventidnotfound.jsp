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
	import="org.opennms.web.event.*, org.opennms.web.utils.ExceptionUtils, org.opennms.core.utils.WebSecurityUtils"
%>

<%
    EventIdNotFoundException einfe = ExceptionUtils.getRootCause(exception, EventIdNotFoundException.class);
%>


<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Event ID Not Found")
          .headTitle("Error")
          .breadcrumb("Error")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<h1>Event ID Not Found</h1>

<p>
  The event ID <%=WebSecurityUtils.sanitizeString(einfe.getBadID())%> is invalid. <%=WebSecurityUtils.sanitizeString(einfe.getMessage())%>
  <br/>
  You can re-enter it here or <a href="event/list.htm?acktyp=unack">browse all
  of the events</a> to find the event you are looking for.
</p>

<form method="post" action="event/detail.jsp">
  <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
  <p>
    Get&nbsp;details&nbsp;for&nbsp;Event&nbsp;ID
    <br/>
    <input type="text" name="id"/>
    <input type="submit" value="Search"/>
  </p>
</form>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

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
	import="org.opennms.netmgt.events.api.EventProxyException, org.opennms.web.utils.ExceptionUtils"
%>


<%
    EventProxyException e = ExceptionUtils.getRootCause(exception, EventProxyException.class);
%>


<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Connection Error")
          .headTitle("Error")
          .breadcrumb("Error")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<h1>Event Delivery Error</h1>

<p>
  Could not send an event to the OpenNMS event daemon due to this
  error: <%= org.opennms.web.api.Util.htmlify(e.getMessage()) %>
</p>

<p>
  Is the OpenNMS daemon running?
</p>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

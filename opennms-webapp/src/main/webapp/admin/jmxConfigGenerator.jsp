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
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("JMX Configuration Generator")
          .breadcrumb("Admin", "admin/index.jsp")
          .breadcrumb("JMX Configuration Generator")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<iframe id="vaadin-content" name="jmx-config-ui" src="admin/jmx-config-tool" frameborder="0" class="vaadin-fullscreen"></iframe>
<jsp:include page="/includes/bootstrap-footer.jsp" flush="true"/>

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
<%
    String title = org.opennms.web.api.Util.getParameter(request, "title", "Undefined");
    // Accept only the list types that the onms-status-list directive knows.
    String type = org.opennms.web.api.Util.getParameter(request, "type");
    if (type == null || !java.util.Arrays.asList("applications", "business-services", "nodes").contains(type)) {
        type = "unknown";
    }
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitleText(title)
          .breadcrumbText(title)
          .ngApp("onms.default.apps")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<jsp:include page="/assets/load-assets.jsp" flush="false">
  <jsp:param name="asset" value="angular-status" />
  <jsp:param name="asset-type" value="js" />
</jsp:include>

<onms-status-list type="<%= type %>"></onms-status-list>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

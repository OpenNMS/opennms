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
	import="java.util.*"
%>
<%@ page import="org.opennms.core.utils.WebSecurityUtils" %>

<%
    String uri = request.getRequestURI();
    String qs = request.getQueryString();
    String success = request.getParameter("success");
    String systemId = request.getParameter("nmsSystemId");
    String displayName = request.getParameter("nmsDisplayName");
    String accessToken = request.getParameter("accessToken");
    String refreshToken = request.getParameter("refreshToken");

    List<String[]> displayItems = new ArrayList<String[]>();

    displayItems.add(new String[] { "Request URI:", uri });
    displayItems.add(new String[] { "Success:", success });
    displayItems.add(new String[] { "System ID:", systemId });
    displayItems.add(new String[] { "Display Name:", displayName });
    displayItems.add(new String[] { "Access Token:", accessToken });
    displayItems.add(new String[] { "Refresh Token:", refreshToken });
    displayItems.add(new String[] { "Query String:", qs });
%>

<%@ page import="org.opennms.web.utils.Bootstrap" %>
<% Bootstrap.with(pageContext)
          .headTitle("Zenith Connect Success")
          .build(request);
%>
<jsp:directive.include file="/includes/bootstrap.jsp" />

<div class="row">
  <div class="col-lg-6 col-md-8">

    <div class="card">
      <div class="card-header">
        <span>Zenith Connect Registration</span>
      </div>
      <div class="card-body">
        <div class="container">
          <%
            for (String[] displayItem : displayItems) {
          %>
                <div class="row">
                  <div class="col-lg-6">
                    <%=WebSecurityUtils.sanitizeString(displayItem[0])%>
                  </div>
                  <div class="col-lg-6">
                    <%=WebSecurityUtils.sanitizeString(displayItem[1])%>
                  </div>
                </div>
          <%
            }
          %>

        </div><!-- container -->

      </div> <!-- card-body -->
    </div> <!-- panel -->
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

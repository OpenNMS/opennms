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
        import="org.opennms.web.api.Util"
        %>

<%
    final String baseHref = Util.calculateUrlBase(request);
%>


<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8"/>
    <meta http-equiv="Content-Style-Type" content="text/css"/>
    <meta http-equiv="Content-Script-Type" content="text/javascript"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>

    <c:if test="${param.nobase != 'true' }">
        <base href="<%= baseHref %>" />
    </c:if>

    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="manifest" />
      <jsp:param name="asset-type" value="js" />
    </jsp:include>

    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="bootstrap" />
      <jsp:param name="asset-media" value="screen" />
      <jsp:param name="asset-type" value="css" />
    </jsp:include>
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="opennms-theme" />
      <jsp:param name="asset-media" value="screen" />
      <jsp:param name="asset-type" value="css" />
    </jsp:include>
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="font-awesome" />
      <jsp:param name="asset-type" value="css" />
    </jsp:include>

    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="vendor" />
    </jsp:include>
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="global" />
    </jsp:include>

    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="jquery-js" />
    </jsp:include>
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="bootstrap" />
      <jsp:param name="asset-type" value="js" />
    </jsp:include>
    <jsp:include page="/assets/load-assets.jsp" flush="false">
      <jsp:param name="asset" value="opennms-theme" />
      <jsp:param name="asset-media" value="screen" />
      <jsp:param name="asset-type" value="js" />
    </jsp:include>

    <title>RTC Console</title>
</head>
<body>
    <jsp:include page="/includes/categories-box.jsp" flush="false"/>
</body>
</html>

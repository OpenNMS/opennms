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
        import="org.opennms.netmgt.config.trend.TrendDefinition"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
    int columns = 2;

    if (request.getParameter("columns") != null) {
        columns = Integer.parseInt(request.getParameter("columns"));
    }

    int colClass = 12 / columns;
%>

<div class="card">
    <div class="card-header">
        <span>Trend</span>
    </div>
    <div class="alert-box card-body">
        <div class="row">
            <c:forEach var="trendDefinition" items="${trendDefinitions}">
                <div class="col-sm-<%= colClass %>">
                    <jsp:include page="/trend/trend.htm" flush="false">
                        <jsp:param name="name" value="${trendDefinition.name}"/>
                    </jsp:include>
                </div>
            </c:forEach>
        </div>
    </div>
</div>



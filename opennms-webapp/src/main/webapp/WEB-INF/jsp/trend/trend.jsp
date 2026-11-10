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
<%@ page language="java" contentType="text/html" session="true" import="org.opennms.netmgt.config.trend.TrendAttribute"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="opennms-trendline" />
</jsp:include>

<div class="alert bg-light" role="alert">
    <table width="100%" border="0" cellpadding="0" cellspacing="0">
        <tr>
            <td width="1%">
                <h1 style="margin:0;" class="mr-2"><span class="fas ${trendDefinition.icon}" aria-hidden="true"></span></h1>
            </td>
            <td style="white-space: nowrap;">
                <h4 style="margin:0;">${trendDefinition.title}</h4><h6 style="margin:0;">${trendDefinition.subtitle}</h6>
            </td>
            <td width="50%" align="right">
                <jsp:text><![CDATA[<span "]]></jsp:text>
                class="sparkline-${trendDefinition.name}"
                <c:forEach var="trendAttribute" items="${trendDefinition.trendAttributes}">
                    <c:if test="${fn:startsWith(trendAttribute.key,'spark')}">
                        ${trendAttribute.key}="${trendAttribute.value}"
                    </c:if>
                </c:forEach>
                >
                ${trendValuesString}
                <jsp:text><![CDATA[</span>]]></jsp:text>
            </td>
        </tr>
    </table>

    <hr style="margin-top:5px;margin-bottom:5px;"/>

    <c:choose>
        <c:when test="${trendDefinition.descriptionLink!=''}">
            <a href="${trendDefinition.descriptionLink}">${trendDefinition.description}</a>
        </c:when>
        <c:otherwise>
            ${trendDefinition.description}
        </c:otherwise>
    </c:choose>
</div>

<script type="text/javascript">
    $('.sparkline-${trendDefinition.name}').sparkline('html', { enableTagOptions: true });
</script>

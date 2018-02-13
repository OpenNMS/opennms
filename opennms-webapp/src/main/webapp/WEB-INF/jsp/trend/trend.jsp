<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
--%>

<%@ page language="java" contentType="text/html" session="true" import="org.opennms.netmgt.config.trend.TrendAttribute"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<jsp:include page="/assets/load-assets.jsp" flush="false">
    <jsp:param name="asset" value="opennms-trendline" />
</jsp:include>

<div class="alert alert-trend" role="alert">
    <table width="100%" border="0" cellpadding="0" cellspacing="0">
        <tr>
            <td width="1%">
                <h1 style="margin:0;"><span class="glyphicon ${trendDefinition.icon}" aria-hidden="true"></span></h1>
            </td>
            <td style="white-space: nowrap;">
                <h3 style="margin:0;">${trendDefinition.title}</h3><h4 style="margin:0;">${trendDefinition.subtitle}</h4>
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

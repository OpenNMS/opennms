<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Resolved Outages" />
	<jsp:param name="headTitle" value="Outages" />
	<jsp:param name="breadcrumb" value="All resolved outages by OutageId" />
</jsp:include>



<html>
<head>
<title>All Outages</title>


</head>
<body>

<link rel="stylesheet" type="text/css"
	href="<c:url value="/css/styles.css"/>">
<link rel="stylesheet" type="text/css"
	href="<c:url value="/css/extremecomponents.css"/>">

<center><ec:table items="tabledata" var="tabledata"
	action="${pageContext.request.contextPath}/displayResolvedOutages.htm?${pageContext.request.queryString}"
	filterable="false"
	imagePath="${pageContext.request.contextPath}/images/table/compact/*.gif"
	title="Resolved Outages" showExports="false" retrieveRowsCallback="limit"
	filterRowsCallback="limit" sortRowsCallback="limit" rowsDisplayed="25"
	tableId="tabledata" autoIncludeParameters="false"
	view="org.opennms.web.svclayer.etable.FixedRowCompact">

	<ec:exportPdf fileName="CurrentOutages.pdf" tooltip="Export PDF"
		headerColor="black" headerBackgroundColor="#b6c2da"
		headerTitle="Current Outages" />
	<ec:exportXls fileName="output.xls" tooltip="Export Excel" />
	<ec:row highlightRow="false">
		<ec:column property="outageid" alias="ID" >
			<a href="outage/detail.htm?id=${tabledata.outageid}">${tabledata.outageid}</a>
		</ec:column>
		<ec:column property="nodeid" alias="Node">

			<a href="element/node.jsp?node=${tabledata.nodeid}">${tabledata.node}</a>
			&nbsp;
			
			<c:choose>
				<c:when test='${suffix == null}'>
					<a
						href="${pageContext.request.contextPath}/displayResolvedOutages.htm?nodeid=${tabledata.nodeid}">[+]</a>
					<a
						href="${pageContext.request.contextPath}/displayResolvedOutages.htm?not_nodeid=${tabledata.nodeid}">[-]</a>
				</c:when>

				<c:otherwise>
					<a
						href="${pageContext.request.contextPath}/displayResolvedOutages.htm?${suffix}&nodeid=${tabledata.nodeid}">[+]</a>
					<a
						href="${pageContext.request.contextPath}/displayResolvedOutages.htm?${suffix}&not_nodeid=${tabledata.nodeid}">[-]</a>
				</c:otherwise>
			</c:choose>

		</ec:column>
		<ec:column property="ipaddr" alias="Interface">
			<c:url var="interfaceLink" value="element/interface.jsp">
				<c:param name="node" value="${tabledata.nodeid}"/>
				<c:param name="intf" value="${tabledata.ipaddr}"/>
			</c:url>
			<a href="<c:out value="${interfaceLink}"/>">${tabledata.ipaddr}</a>
			
			&nbsp;
			
			<c:url var="withIpaddrLink" value="${pageContext.request.contextPath}/displayResolvedOutages.htm">
				<c:param name="ipaddr" value="${tabledata.ipaddr}"/>
			</c:url>
			<c:url var="withoutIpaddrLink" value="${pageContext.request.contextPath}/displayResolvedOutages.htm">
				<c:param name="not_ipaddr" value="${tabledata.ipaddr}"/>
			</c:url>
			<c:choose>
				<c:when test='${suffix == null}'>
					<a href="<c:out value="${withIpaddrLink}"/>">[+]</a>
					<a href="<c:out value="${withoutIpaddrLink}"/>">[-]</a>
				</c:when>
				<c:otherwise>
					<a href="<c:out value="${withIpaddrLink + '&' + suffix}"/>">[+]</a>
					<a href="<c:out value="${withoutIpaddrLink + '&' + suffix}"/>">[-]</a>
				</c:otherwise>
			</c:choose>
		</ec:column>
		<ec:column property="serviceid" alias="Service">
			<a
				href="element/service.jsp?node=${tabledata.nodeid}&intf=${tabledata.ipaddr }&service=${tabledata.serviceid }">${tabledata.service}</a>
				&nbsp;

			<c:choose>
				<c:when test='${suffix == null}'>
					<a
						href="${pageContext.request.contextPath}/displayResolvedOutages.htm?serviceid=${tabledata.serviceid}">[+]</a>
					<a
						href="${pageContext.request.contextPath}/displayResolvedOutages.htm?not_serviceid=${tabledata.serviceid}">[-]</a>
				</c:when>
				<c:otherwise>
					<a
						href="${pageContext.request.contextPath}/displayResolvedOutages.htm?${suffix}&serviceid=${tabledata.serviceid}">[+]</a>
					<a
						href="${pageContext.request.contextPath}/displayResolvedOutages.htm?${suffix}&not_serviceid=${tabledata.serviceid}">[-]</a>
				</c:otherwise>
			</c:choose>
		</ec:column>

		<ec:column property="iflostservice" alias="Down">
			
			${tabledata.iflostservice}
			&nbsp;
			
			<c:choose>
				<c:when test='${suffix == null}'>
					<a
						href="${pageContext.request.contextPath}/displayResolvedOutages.htm?bigger_iflostservice=${tabledata.iflostservicelong}">&lt;</a>
					<a
						href="${pageContext.request.contextPath}/displayResolvedOutages.htm?smaller_iflostservice=${tabledata.iflostservicelong}">&gt;</a>
				</c:when>
				<c:otherwise>
					<a
						href="${pageContext.request.contextPath}/displayResolvedOutages.htm?${suffix}&bigger_iflostservice=${tabledata.iflostservicelong}">&lt;</a>
					<a
						href="${pageContext.request.contextPath}/displayResolvedOutages.htm?${suffix}&smaller_iflostservice=${tabledata.iflostservicelong}">&gt;</a>
				</c:otherwise>
			</c:choose>


		</ec:column>


		<ec:column property="ifregainedservice" format="MM-dd-yyyy hh:mm:ss"
			parse="yyyy-MM-dd" alias="Regained"
			interceptor="org.opennms.web.svclayer.outage.RedCell">

				${tabledata.ifregainedservice}
				&nbsp;
				
			<c:if test='${tabledata.ifregainedservice ne null}'>

				<c:choose>
					<c:when test='${suffix == null}'>
						<a
							href="${pageContext.request.contextPath}/displayResolvedOutages.htm?bigger_ifregainedservice=${tabledata.ifregainedservicelong}">&lt;</a>
						<a
							href="${pageContext.request.contextPath}/displayResolvedOutages.htm?smaller_ifregainedservice=${tabledata.ifregainedservicelong}">&gt;</a>
					</c:when>
					<c:otherwise>
						<a
							href="${pageContext.request.contextPath}/displayResolvedOutages.htm?${suffix}&bigger_ifregainedservice=${tabledata.ifregainedservicelong}">&lt;</a>
						<a
							href="${pageContext.request.contextPath}/displayResolvedOutages.htm?${suffix}&smaller_ifregainedservice=${tabledata.ifregainedservicelong}">&gt;</a>
					</c:otherwise>
				</c:choose>

			</c:if>

		</ec:column>

	</ec:row>
</ec:table></center>

<jsp:include page="/includes/footer.jsp" flush="false" />
</body>
</html>


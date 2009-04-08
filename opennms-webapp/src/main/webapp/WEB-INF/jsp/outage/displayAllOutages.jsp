<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="ALl Outages" />
	<jsp:param name="headTitle" value="Outages" />
	<jsp:param name="breadcrumb" value="All outages by OutageId" />
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
	action="${pageContext.request.contextPath}/displayAllOutages.htm?${pageContext.request.queryString}"
	filterable="false"
	imagePath="${pageContext.request.contextPath}/images/table/compact/*.gif"
	title="All Outages" showExports="false" retrieveRowsCallback="limit"
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
						href="${pageContext.request.contextPath}/displayAllOutages.htm?nodeid=${tabledata.nodeid}">[+]</a>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?not_nodeid=${tabledata.nodeid}">[-]</a>
				</c:when>

				<c:otherwise>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?${suffix}&nodeid=${tabledata.nodeid}">[+]</a>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?${suffix}&not_nodeid=${tabledata.nodeid}">[-]</a>
				</c:otherwise>
			</c:choose>

		</ec:column>
		<ec:column property="ipaddr" alias="Interface">
			<a
				href="element/interface.jsp?node=${tabledata.nodeid}&intf=${tabledata.ipaddr}">${tabledata.ipaddr}</a>
				&nbsp;
				
			<c:choose>
				<c:when test='${suffix == null}'>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?ipaddr=${tabledata.ipaddr}">[+]</a>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?not_ipaddr=${tabledata.ipaddr}">[-]</a>
				</c:when>
				<c:otherwise>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?${suffix}&ipaddr=${tabledata.ipaddr}">[+]</a>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?${suffix}&not_ipaddr=${tabledata.ipaddr}">[-]</a>
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
						href="${pageContext.request.contextPath}/displayAllOutages.htm?serviceid=${tabledata.serviceid}">[+]</a>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?not_serviceid=${tabledata.serviceid}">[-]</a>
				</c:when>
				<c:otherwise>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?${suffix}&serviceid=${tabledata.serviceid}">[+]</a>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?${suffix}&not_serviceid=${tabledata.serviceid}">[-]</a>
				</c:otherwise>
			</c:choose>
		</ec:column>

		<ec:column property="iflostservice" alias="Down">
			
			${tabledata.iflostservice}
			&nbsp;
			
			<c:choose>
				<c:when test='${suffix == null}'>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?bigger_iflostservice=${tabledata.iflostservicelong}">&lt;</a>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?smaller_iflostservice=${tabledata.iflostservicelong}">&gt;</a>
				</c:when>
				<c:otherwise>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?${suffix}&bigger_iflostservice=${tabledata.iflostservicelong}">&lt;</a>
					<a
						href="${pageContext.request.contextPath}/displayAllOutages.htm?${suffix}&smaller_iflostservice=${tabledata.iflostservicelong}">&gt;</a>
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
							href="${pageContext.request.contextPath}/displayAllOutages.htm?bigger_ifregainedservice=${tabledata.ifregainedservicelong}">&lt;</a>
						<a
							href="${pageContext.request.contextPath}/displayAllOutages.htm?smaller_ifregainedservice=${tabledata.ifregainedservicelong}">&gt;</a>
					</c:when>
					<c:otherwise>
						<a
							href="${pageContext.request.contextPath}/displayAllOutages.htm?${suffix}&bigger_ifregainedservice=${tabledata.ifregainedservicelong}">&lt;</a>
						<a
							href="${pageContext.request.contextPath}/displayAllOutages.htm?${suffix}&smaller_ifregainedservice=${tabledata.ifregainedservicelong}">&gt;</a>
					</c:otherwise>
				</c:choose>

			</c:if>

		</ec:column>

	</ec:row>
</ec:table></center>

<jsp:include page="/includes/footer.jsp" flush="false" />
</body>
</html>


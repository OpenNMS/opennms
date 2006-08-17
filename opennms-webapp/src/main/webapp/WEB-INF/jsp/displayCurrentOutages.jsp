<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="/tld/extremecomponents" prefix="ec"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Current Outages" />
	<jsp:param name="headTitle" value="Outages" />
	<jsp:param name="breadcrumb" value="Current Outages By Node" />
</jsp:include>


<html>
<head>
<title>Current Outages</title>

</head>
<body>

<link rel="stylesheet" type="text/css"
	href="<c:url value="/css/styles.css"/>">
<link rel="stylesheet" type="text/css"
	href="<c:url value="/css/extremecomponents.css"/>">

<p>Now : <c:out value="${now}" /></p>
<p>Start : <c:out value="${begin}" /></p>
<p>End : <c:out value="${end}" /></p>
<p>Rows : <c:out value="${rows}" /></p>
<p>Page : <c:out value="${page}" /></p>
<p>Page : <c:out value="${context}" /></p>
<p>The whole shit <c:out value="${mymodel}" /></p>

<center><ec:table items="tabledata" var="tabledata"
	action="${pageContext.request.contextPath}/displayCurrentOutages.htm"
	filterable="false" showExports="true"
	imagePath="${pageContext.request.contextPath}/images/table/compact/*.gif"
	title="Current Outages"
	retrieveRowsCallback="limit" 
	filterRowsCallback="limit"
	sortRowsCallback="limit" 
	rowsDisplayed="75" 
	tableId="tabledata"
	
	>
	
	<ec:exportPdf fileName="CurrentOutages.pdf" tooltip="Export PDF"
		headerColor="black" headerBackgroundColor="#b6c2da"
		headerTitle="Current Outages" />
	<ec:exportXls fileName="output.xls" tooltip="Export Excel" />
	<ec:row highlightRow="false">
		<ec:column property="id">
			<a href="outage/detail.jsp?id=${tabledata.id}">${tabledata.id}</a>
		</ec:column>
		<ec:column property="node">
			<a href="element/node.jsp?node=${tabledata.nodeid}">${tabledata.node}</a>
			&nbsp;
			<a
				href="${pageContext.request.contextPath}/displayCurrentOutages.htm?ec_f_node=${tabledata.node}&ec_f_a=fa">[+]</a>
			<a
				href="${pageContext.request.contextPath}/displayCurrentOutages.htm?ec_f_node=">[-]</a>


		</ec:column>
		<ec:column property="interface">
			<a
				href="element/interface.jsp?node=${tabledata.nodeid}&intf=${tabledata.interface}">${tabledata.interface}</a>
				&nbsp;
			<a
				href="${pageContext.request.contextPath}/displayCurrentOutages.htm?ec_f_interface=${tabledata.interface}&ec_f_a=fa">[+]</a>
			<a
				href="${pageContext.request.contextPath}/displayCurrentOutages.htm?ec_f_interface=">[-]</a>
		</ec:column>
		<ec:column property="service">
			<a
				href="element/service.jsp?node=${tabledata.nodeid}&intf=${tabledata.interface }&service=${tabledata.serviceid }">${tabledata.service}</a>
				&nbsp;
			<a
				href="${pageContext.request.contextPath}/displayCurrentOutages.htm?ec_f_service=${tabledata.service}&ec_f_a=fa">[+]</a>
			<a
				href="${pageContext.request.contextPath}/displayCurrentOutages.htm?ec_f_node=">[-]</a>
		</ec:column>
		<ec:column property="down" cell="date" format="MM-dd-yyyy hh:mm:ss"
			parse="yyyy-MM-dd" />

		<ec:column property="up" cell="date" format="MM-dd-yyyy hh:mm:ss"
			parse="yyyy-MM-dd"
			interceptor="org.opennms.web.svclayer.outage.RedCell" />

	</ec:row>
</ec:table></center>

<jsp:include page="/includes/footer.jsp" flush="false" />
</body>
</html>


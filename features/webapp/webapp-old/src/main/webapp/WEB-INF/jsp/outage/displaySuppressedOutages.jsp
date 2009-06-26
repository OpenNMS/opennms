<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec"%>



<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Suppressed Outages" />
	<jsp:param name="headTitle" value="Outages" />
	<jsp:param name="breadcrumb"
		value="<a href='outage/index.jsp'>Outages</a>" />
	<jsp:param name="breadcrumb" value="Suppressed By Node" />
</jsp:include>

<script type="text/javascript"
	src="<c:url value="/js/extremecomponents.js"/>"></script>


<link rel="stylesheet" type="text/css"
	href="<c:url value="/css/styles.css"/>">
<link rel="stylesheet" type="text/css"
	href="<c:url value="/css/extremecomponents.css"/>">
<center>
<form id="outageForm"
	action="<c:url value="displaySuppressedOutages.htm"/>" method="post">

<ec:table items="tabledata" var="tabledata"
	action="${pageContext.request.contextPath}/displaySuppressedOutages.htm"
	filterable="false"
	imagePath="${pageContext.request.contextPath}/images/table/compact/*.gif"
	title="Suppressed Outages" 
	retrieveRowsCallback="limit"
	filterRowsCallback="limit" 
	sortRowsCallback="limit" 
	rowsDisplayed="25"
	tableId="tabledata"
	form="outageForm"
	view="org.opennms.web.svclayer.etable.FixedRowCompact"
	showExports="false" showStatusBar="true" 
	autoIncludeParameters="false"
	showPagination="true">
	
	<ec:row highlightRow="false">
	

		<ec:column property="nodeid" alias="Node"
			cell="org.opennms.web.svclayer.outage.GroupCell">
			<a href="element/node.jsp?node=${tabledata.nodeid}">${tabledata.node}</a>
		</ec:column>

		<ec:column property="ipaddr" alias="Interface">
			<a
				href="element/interface.jsp?node=${tabledata.nodeid}&intf=${tabledata.ipaddr}">${tabledata.ipaddr}</a>
		</ec:column>

		<ec:column property="serviceid" alias="Service">
			<a
				href="element/service.jsp?node=${tabledata.nodeid}&intf=${tabledata.ipaddr}&service=${tabledata.serviceid }">${tabledata.service}
			</a>
		</ec:column>

		<ec:column property="suppresstime" alias="Suppressed" cell="date"
			format="MM-dd-yyyy hh:mm:ss" parse="yyyy-MM-dd" />
		
		<ec:column property="suppressedby" alias="User"  />

		<ec:column property="iflostservice" alias="Down" cell="date"
			format="MM-dd-yyyy hh:mm:ss" parse="yyyy-MM-dd" />
		
		<ec:column property="outageid" alias="ID">
			<a href="outage/detail.htm?id=${tabledata.outageid}">${tabledata.outageid}</a>
		</ec:column>

		
		

		<ec:column alias="checkbox" title=" " width="5px" filterable="false"
			sortable="false"
			cell="org.opennms.web.svclayer.outage.SuppressOutageCheckBox" />

		<ec:column sortable="false" alias="droplist" title=" " width="5px"
			filterable="false">
			<select name="suppresstime_${tabledata.outageid}"
				id="suppresstime_${tabledata.outageid}">
				<option value="-2" SELECTED>Activate</option>
				<option value="0"></option>
				<option value="15">15m</option>
				<option value="30">30m</option>
				<option value="30">60m</option>
				<option value="180">3h</option>
				<option value="480">8h</option>
				<option value="720">12h</option>
				<option value="1440">1d</option>
				<option value="4320">3d</option>
				<option value="10080">1w</option>
				<option value="-1">Forever</option>
			</select>
		</ec:column>

	</ec:row>

</ec:table>


<p><input type="submit" name="sel" class="button"
	value="Suppress Outage"
	onclick="setFormAction('outageForm','displaySuppressedOutages.htm', 'post');
               document.outageForm.submit();" />
</p>

<script type="text/javascript">
                        function setOutageState(chkbx) {
                        		
                                //make sure that always know the state of the checkbox
                                if (chkbx.checked) {
                                		
                                        eval('document.forms.outageForm.chkbx_' + chkbx.name).value='SELECTED';
                                } else {
                                        eval('document.forms.outageForm.chkbx_' + chkbx.name).value='UNSELECTED';
                                }
                        }
     </script></form>

</center>


<jsp:include page="/includes/footer.jsp" flush="false" />


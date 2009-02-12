<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib tagdir="/WEB-INF/tags/tree" prefix="tree" %>
<%@ taglib tagdir="/WEB-INF/tags/springx" prefix="springx" %>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Provisioning Groups" /> 
	<jsp:param name="headTitle" value="Provisioning Groups" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/provisiond.jsp'>Provisioning Groups</a>" />
	<jsp:param name="link" value="<link rel='stylesheet' type='text/css' href='extJS/resources/css/ext-all.css'></link>" />
	<jsp:param name="link" value="<link rel='stylesheet' type='text/css' href='extJS/resources/css/opennmsGridTheme.css'></link>" />
	<jsp:param name="script" value="<script type='text/javascript' src='extJS/adapter/ext/ext-base.js'></script>" />
	<jsp:param name="script" value="<script type='text/javascript' src='extJS/ext-all-debug.js'></script>" />
	<jsp:param name="script" value="<script type='text/javascript' src='js/XmlTreeLoader.js'></script>" />
	<jsp:param name="script" value="<script type='text/javascript' src='js/provisiondForeignSource.js'></script>" />
</jsp:include>

<h3>Provisioning Groups</h3>

<div id="pendingForeignSources"></div>

<div id="pendingTree"></div>

<jsp:include page="/includes/footer.jsp" flush="false"/>

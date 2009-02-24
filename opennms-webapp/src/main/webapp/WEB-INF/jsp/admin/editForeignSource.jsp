<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib tagdir="/WEB-INF/tags/tree" prefix="tree" %>
<%@ taglib tagdir="/WEB-INF/tags/springx" prefix="springx" %>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Provisioning Groups" /> 
	<jsp:param name="headTitle" value="Provisioning Groups" />
	<jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb" value="<a href='admin/provisioningGroups.htm'>Provisioning Groups</a>" />
	<jsp:param name="breadcrumb" value="Edit Foreign Source" />
</jsp:include>

<h3>Foreign Source: ${foreignSourceEditForm.foreignSourceName}</h3>

<tree:form commandName="foreignSourceEditForm"> 

	<input type="hidden" id="foreignSourceName" name="foreignSourceName" value="${foreignSourceEditForm.foreignSourceName}"/> 
	<tree:actionButton label="Done" action="done" />

	<br />
	<br />
	
	<tree:field label="Scan Interval" property="scanInterval" />

	<h4>
		Detectors <tree:actionButton label="Add Detector" action="addDetector" />
	</h4>

	<tree:tree root="${foreignSourceEditForm.formData}" childProperty="detectors" var="detector" varStatus="detectorIter">
		<tree:nodeForm>
			<tree:field label="name" property="name" />
			<tree:select label="class" property="pluginClass" items="${detectorTypes}" />
		</tree:nodeForm>

	</tree:tree>
	
	<h4>
		Policies <tree:actionButton label="Add Policy" action="addPolicy" />
	</h4>

	<tree:tree root="${foreignSourceEditForm.formData}" childProperty="policies" var="policy" varStatus="policyIter">
		<tree:nodeForm>
			<tree:field label="name" property="name" />
			<tree:select label="class" property="pluginClass" items="${policyTypes}" />
		</tree:nodeForm>
	</tree:tree>
	  
</tree:form> 
<jsp:include page="/includes/footer.jsp" flush="false"/>

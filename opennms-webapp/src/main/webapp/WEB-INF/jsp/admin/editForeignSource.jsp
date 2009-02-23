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

	<tree:field label="Scan Interval" property="scanInterval" />
    <tree:action label="[Add Detector]" action="addDetector" />
    <tree:action label="[Add Policy]" action="addPolicy" />

	<c:if test="${!empty foreignSourceEditForm.formData.detectors}">
	  <h4>Detectors</h4>
	</c:if>
	<tree:tree root="${foreignSourceEditForm.formData}" childProperty="detectors" var="detector" varStatus="detectorIter">
		<tree:nodeForm>
			<tree:field label="name" property="name" />
			<tree:field label="class" property="pluginClass" />
		</tree:nodeForm>
	</tree:tree>
	
	<c:if test="${!empty foreignSourceEditForm.formData.policies}">
	  <h4>Policies</h4>
	</c:if>
	<tree:tree root="${foreignSourceEditForm.formData}" childProperty="policies" var="policy" varStatus="policyIter">
		<tree:form>
			<tree:field label="name" property="name" />
			<tree:field label="class" property="pluginClass" />
		</tree:form>
	</tree:tree>
	  
<%-- 
 <node:actionButton label="Done" action="done" />
 <node:actionButton label="Add Node" action="addNode"/> 

 <tree:tree root="${nodeEditForm.formData}" childProperty="node" var="node" varStatus="nodeIter">
    <!-- Form for editing node fields -->
    <node:form>
      <node:field label="Node" property="nodeLabel" />
      <node:field label="ForeignId" property="foreignId" />
      <node:field label="Site" property="building" />
      <node:action label="[Add Interface]" action="addInterface" />
      <node:action label="[Add Node Category]" action="addCategory" />
      <node:action label="[Add Node Asset]" action="addAssetField" />
    </node:form> 
    
    <!--  Tree of interface under the node -->
    <tree:tree root="${node}" childProperty="interface" var="ipInterface" varStatus="ipIter">
    
      <!-- Form for editing an interface -->
      <node:form>
        <node:field label="IP Interface" property="ipAddr" />
        <node:field label="Description" property="descr" />
        
        <node:select label="Snmp Primary" property="snmpPrimary" items="${snmpPrimaryChoices}" />
        <node:action label="Add Service" action="addService" />
      </node:form>

      <!-- Tree of services under the interface -->
      <tree:tree root="${ipInterface}" childProperty="monitoredService" var="svc" varStatus="svcIter">
      
        <!--  Form for editing a service -->
        <node:form>  
            <node:select label="Service" property="serviceName" items="${services}" />
        </node:form>
      </tree:tree>

    </tree:tree>
    
    <!--  Tree of categories for a node -->
    <tree:tree root="${node}" childProperty="category" var="category" varStatus="catIter">
    
      <!--  Form for editing a category -->
      <node:form>
        <node:select label="Node Category" property="name" items="${categories}"/>
      </node:form>
      
    </tree:tree>
    
    <!--  Tree of assets for a node -->
    <tree:tree root="${node}" childProperty="asset" var="asset" varStatus="assetIter">
    
      <!--  Form for editing a category -->
      <node:form>
      	<node:select label="asset" property="name" items="${assetFields}"/>
        <node:field label="" property="value" />
      </node:form>
      
    </tree:tree>
    
 </tree:tree>
--%>

</tree:form> 
<jsp:include page="/includes/footer.jsp" flush="false"/>

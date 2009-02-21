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
	<jsp:param name="breadcrumb" value="Edit Requisition" />
</jsp:include>

<h3>Manually Provisioned Nodes for Group: ${nodeEditForm.groupName}</h3>

 <tree:form commandName="nodeEditForm"> 

  <input type="hidden" id="groupName" name="groupName" value="${nodeEditForm.groupName}"/> 
 
 <tree:actionButton label="Done" action="done" />
 <tree:actionButton label="Add Node" action="addNode"/> 

 <tree:tree root="${nodeEditForm.formData}" childProperty="node" var="node" varStatus="nodeIter">
    <!-- Form for editing node fields -->
    <tree:nodeForm>
      <tree:field label="Node" property="nodeLabel" />
      <tree:field label="ForeignId" property="foreignId" />
      <tree:field label="Site" property="building" />
      <tree:action label="[Add Interface]" action="addInterface" />
      <tree:action label="[Add Node Category]" action="addCategory" />
      <tree:action label="[Add Node Asset]" action="addAssetField" />
    </tree:nodeForm> 
    
    <!--  Tree of interface under the node -->
    <tree:tree root="${node}" childProperty="interface" var="ipInterface" varStatus="ipIter">
    
      <!-- Form for editing an interface -->
      <tree:nodeForm>
        <tree:field label="IP Interface" property="ipAddr" />
        <tree:field label="Description" property="descr" />
        
        <tree:select label="Snmp Primary" property="snmpPrimary" items="${snmpPrimaryChoices}" />
        <tree:action label="Add Service" action="addService" />
      </tree:nodeForm>

      <!-- Tree of services under the interface -->
      <tree:tree root="${ipInterface}" childProperty="monitoredService" var="svc" varStatus="svcIter">
      
        <!--  Form for editing a service -->
        <tree:nodeForm>  
            <tree:select label="Service" property="serviceName" items="${services}" />
        </tree:nodeForm>
      </tree:tree>

    </tree:tree>
    
    <!--  Tree of categories for a node -->
    <tree:tree root="${node}" childProperty="category" var="category" varStatus="catIter">
    
      <!--  Form for editing a category -->
      <tree:nodeForm>
        <tree:select label="Node Category" property="name" items="${categories}"/>
      </tree:nodeForm>
      
    </tree:tree>
    
    <!--  Tree of assets for a node -->
    <tree:tree root="${node}" childProperty="asset" var="asset" varStatus="assetIter">
    
      <!--  Form for editing a category -->
      <tree:nodeForm>
      	<tree:select label="asset" property="name" items="${assetFields}"/>
        <tree:field label="" property="value" />
      </tree:nodeForm>
      
    </tree:tree>
    
 </tree:tree>

</tree:form> 
<jsp:include page="/includes/footer.jsp" flush="false"/>

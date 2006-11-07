<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib tagdir="/WEB-INF/tags/tree" prefix="tree" %>
<%@ taglib tagdir="/WEB-INF/tags/springx" prefix="springx" %>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Application" /> 
	<jsp:param name="headTitle" value="Application" />
	<jsp:param name="breadcrumb"
               value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb"
	           value="<a href='admin/import.htm'>Import</a>" />
	<jsp:param name="breadcrumb" value="Show" />
</jsp:include>

<h3>Manually Provisioned Nodes</h3>
  
 <tree:form commandName="nodeEditForm"> 
 
 <tree:action label="Add Node" action="addNode"/> 

 <tree:tree root="${nodeEditForm.formData}" childProperty="node" var="node" varStatus="nodeIter">
    <!-- Form for editing node fields -->
    <tree:nodeForm>
      <tree:field label="Node" property="nodeLabel" />
      <tree:field label="ForeignId" property="foreignId" />
      <!-- tree:select label="Primary Interface" property="parentNodeLabel" itemLabel="ipAddr" items="${node.interface}" / -->
      <tree:action label="Add Interface" action="addInterface" />
      <tree:action label="Add Category" action="addCategory" />
    </tree:nodeForm> 
    
    <!--  Tree of interface under the node -->
    <tree:tree root="${node}" childProperty="interface" var="ipInterface" varStatus="ipIter">
    
      <!-- Form for editing an interface -->
      <tree:nodeForm>
        <tree:field label="IP Interface" property="ipAddr" />
        <tree:field label="Description" property="descr" />
        <tree:action label="Add Service" action="addService" />
      </tree:nodeForm>

      <!-- Tree of services under the interface -->
      <tree:tree root="${ipInterface}" childProperty="monitoredService" var="svc" varStatus="svcIter">
      
        <!--  Form for editing a service -->
        <tree:nodeForm>  
            <tree:field label="Service" property="serviceName" />
        </tree:nodeForm>
      </tree:tree>

    </tree:tree>
    
    <!--  Tree of categories for a node -->
    <tree:tree root="${node}" childProperty="category" var="category" varStatus="catIter">
    
      <!--  Form for editing a category -->
      <tree:nodeForm>
        <tree:field label="Category" property="name" />
      </tree:nodeForm>
      
    </tree:tree>
 </tree:tree>

</tree:form> 
<jsp:include page="/includes/footer.jsp" flush="false"/>
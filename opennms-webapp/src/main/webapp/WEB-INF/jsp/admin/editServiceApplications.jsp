<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Category" />
	<jsp:param name="headTitle" value="Category" />
	<jsp:param name="breadcrumb"
               value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb"
	           value="<a href='admin/applications.htm'>Application</a>" />
	<jsp:param name="breadcrumb" value="Show" />
</jsp:include>

<h3>Edit applications on ${model.service.serviceName}</h3>

<p>
Service <a href="<c:url value='element/service.jsp?ifserviceid=${model.service.id}'/>">${model.service.serviceName}</a>
on interface <a href="<c:url value='element/interface.jsp?ipinterfaceid=${model.service.ipInterface.id}'/>">${model.service.ipAddress}</a>
of node <a href="<c:url value='element/node.jsp?node=${model.service.ipInterface.node.id}'/>">${model.service.ipInterface.node.label}</a>
(node ID: ${model.service.ipInterface.node.id}) has ${fn:length(model.service.applications)} applications
</p>

<form action="admin/applications.htm" method="get">
  <input type="hidden" name="ifserviceid" value="${model.service.id}"/>
  <input type="hidden" name="edit" value=""/>
  
  <table class="normal">
    <tr>
      <td class="normal" align="center">
		Available applications
      </td>
      
      <td class="normal">  
      </td>

      <td class="normal" align="center">
      	Applications on service
      </td>
    </tr>
      
    <tr>
      <td class="normal">  
    <select name="toAdd" size="20" multiple>
	  <c:forEach items="${model.applications}" var="application">
	    <option value="${application.id}">${application.name}</option>
	  </c:forEach>
    </select>
      </td>
      
      <td class="normal" style="text-align:center; vertical-align:middle;">  
        <input type="submit" name="action" value="Add &#155;&#155;"/>
        <br/>
        <br/>
        <input type="submit" name="action" value="&#139;&#139; Remove"/>
      </td>
    
      <td class="normal">  
    <select name="toDelete" size="20" multiple>
	  <c:forEach items="${model.sortedApplications}" var="application">
	    <option value="${application.id}">${application.name}</option>
	  </c:forEach>
    </select>
      </td>
    </tr>
  </table>
</form>

<jsp:include page="/includes/footer.jsp" flush="false"/>
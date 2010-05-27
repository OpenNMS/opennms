<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Application" />
	<jsp:param name="headTitle" value="Application" />
	<jsp:param name="breadcrumb"
               value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb"
	           value="<a href='admin/applications.htm'>Applications</a>" />
	<jsp:param name="breadcrumb" value="Show" />
</jsp:include>

<h3>Edit application ${model.application.name}</h3>

<p>
Application '${model.application.name}' has ${fn:length(model.sortedMemberServices)} services 
</p>

<form action="admin/applications.htm" method="get">
  <input type="hidden" name="applicationid" value="${model.application.id}"/>
  <input type="hidden" name="edit" value="edit"/>
  
  <table class="normal">
    <tr>
      <td class="normal" align="center">
		Available services
      </td>
      
      <td class="normal">  
      </td>

      <td class="normal" align="center">
      	Services on application
      </td>
    </tr>
      
    <tr>
      <td class="normal">  
    <select name="toAdd" size="20" multiple>
	  <c:forEach items="${model.monitoredServices}" var="service">
	    <option value="${service.id}">${service.ipInterface.node.label} / ${service.ipAddress} / ${service.serviceName}</option>
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
	  <c:forEach items="${model.sortedMemberServices}" var="service">
	    <option value="${service.id}">${service.ipInterface.node.label} / ${service.ipAddress} / ${service.serviceName}</option>
	  </c:forEach>
    </select>
      </td>
    </tr>
  </table>
</form>

<jsp:include page="/includes/footer.jsp" flush="false"/>
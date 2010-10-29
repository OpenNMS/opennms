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

<h3>Application: ${fn:escapeXml(model.application.name)}</h3>

<p>
Application '${fn:escapeXml(model.application.name)}' has ${fn:length(model.memberServices)} services.
</p>

<p>
<a href="admin/applications.htm?edit=edit&applicationid=${model.application.id}">Edit application</a>
</p>

<table>
  <tr>
    <th>Node</th>
    <th>Interface</th>
    <th>Service</th>
  </tr>
  <c:forEach items="${model.memberServices}" var="service">
    <tr>
    	<td><a href="element/node.jsp?node=${service.ipInterface.node.id}">${fn:escapeXml(service.ipInterface.node.label)}</a></td> 
    	<td><a href="element/interface.jsp?ipinterfaceid=${service.ipInterface.id}">${service.ipInterface.ipAddress}</a></td> 
    	<td><a href="element/service.jsp?ifserviceid=${service.id}">${fn:escapeXml(service.serviceName)}</a></td> 
    </tr>
  </c:forEach>
</table>

<jsp:include page="/includes/footer.jsp" flush="false"/>
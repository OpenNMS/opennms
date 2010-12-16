<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Category" />
	<jsp:param name="headTitle" value="Category" />
	<jsp:param name="breadcrumb"
               value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb"
	           value="<a href='admin/categories.htm'>Categories</a>" />
	<jsp:param name="breadcrumb" value="Show" />
</jsp:include>

<h3>Surveillance Category: ${fn:escapeXml(model.category.name)}</h3>

<p>
Category '${fn:escapeXml(model.category.name)}' has ${fn:length(model.memberNodes)} nodes.
</p>

<p>
<a href="admin/categories.htm?edit&categoryid=${model.category.id}">Edit category</a>
</p>

<table>
  <tr>
    <th>Node</th>
  </tr>
  <c:forEach items="${model.memberNodes}" var="node">
    <tr>
    	<td><a href="element/node.jsp?node=${node.id}">${fn:escapeXml(node.label)}</a></td> 
    </tr>
  </c:forEach>
</table>

<jsp:include page="/includes/footer.jsp" flush="false"/>

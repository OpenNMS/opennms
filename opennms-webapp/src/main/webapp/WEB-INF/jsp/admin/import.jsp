<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Application" />
	<jsp:param name="headTitle" value="Application" />
	<jsp:param name="breadcrumb"
               value="<a href='admin/index.jsp'>Admin</a>" />
	<jsp:param name="breadcrumb"
	           value="<a href='admin/import.htm'>Import</a>" />
	<jsp:param name="breadcrumb" value="Show" />
</jsp:include>

<h3>Add Nodes</h3>
 <a href="#">Add node</a>

<form method="post" action="admin/import.htm">
<table>
  <c:forEach items="${importData.node}" var="node" varStatus="nodeIter">
    <tr>
      <td colspan="3"><input type="text" name="node[${nodeIter.index}].nodeLabel"/>${node.nodeLabel}</td>
      <td><a href="#">Add Category</a></td>
      <td><a href="#">Add Interface</a></td>
    </tr>
      <c:forEach items="${node.category}" var="category" varStatus="catIter">
        <tr>
          <td width="2%">&nbsp;</td>
          <td colspan="4"><input type="text" name="node[${nodeIter.index}].category[${catIter.index}].name"/>${category.name}</td>
        </tr>
      </c:forEach>
      <c:forEach items="${node.interface}" var="ipInterface" varStatus="ifIter">
        <tr>
          <td width="2%">&nbsp;</td>
          <td colspan="3"><input type="text" name="node[${nodeIter.index}].interface[${ifIter.index}].ipAddr"/>${ipInterface.ipAddr}</td>
          <td><a href="#">Add Service</a></td>
        </tr>
        <c:forEach items="${ipInterface.monitoredService}" var="svc" varStatus="svcIter">
          <tr>
            <td width="2%">&nbsp;</td>
            <td width="2%">&nbsp;</td>
            <td colspan="3">
              <input type="text" name="node[${nodeIter.index}].interface[${ifIter.index}].monitoredService[${svcIter.index}].serviceName"/>
            ${svc.serviceName}
            </td> 
        </c:forEach>
      </c:forEach>
  </c:forEach>
</table>
<input type="submit"/>
</form>

<jsp:include page="/includes/footer.jsp" flush="false"/>
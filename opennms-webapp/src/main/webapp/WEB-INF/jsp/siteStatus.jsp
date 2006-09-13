<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Aggregate Status Page" />
	<jsp:param name="headTitle" value="AggregateStatus" />
	<jsp:param name="breadcrumb" value="Aggregated Status" />
</jsp:include>

<h3>Site: <c:out value="${view.columnValue}" /></h3>

  <table>
    <thead>
      <tr>
        <th>Device Type</th>
        <th>Nodes Down</th>
      </tr>
    </thead>
    <c:forEach items="${stati}" var="status">
      <tr class="CellStatus" >
        <td><c:out value="${status.label}" /></td>
        <td class="<c:out value='${status.status}'/> divider" ><c:out value="${status.downEntityCount}" /> of <c:out value="${status.totalEntityCount}" /></td>
      </tr>
    </c:forEach>
  </table>

<jsp:include page="/includes/footer.jsp" flush="false" />

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Surveillance View" />
	<jsp:param name="headTitle" value="Surveillance" />
	<jsp:param name="breadcrumb" value="Surveillance" />
</jsp:include>

<h1 align="center">Surveillance View</h1>

<div id="index-contentmiddle">

<h3> <c:out value="${webTable.title}" /> </h3>

<table>

  <tr>
  <c:forEach items="${webTable.columnHeaders}" var="headerCell">
    <th class="<c:out value='${headerCell.styleClass}'/>">
      <c:out value="${headerCell.content}"/>
    </th>
  </c:forEach>
  </tr>
  
  <c:forEach items="${webTable.rows}" var="row">
    <tr>
      <c:forEach items="${row}" var="cell">
        <td class="<c:out value='${cell.styleClass}'/>">
           <c:out value="${cell.content}"/>
        </td>
      </c:forEach>
    </tr>
  </c:forEach>
</table>

</div>

<jsp:include page="/includes/footer.jsp" flush="false" />

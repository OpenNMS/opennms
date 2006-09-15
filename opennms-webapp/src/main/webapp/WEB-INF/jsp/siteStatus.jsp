<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Site Status Page" />
	<jsp:param name="headTitle" value="SiteStatus" />
	<jsp:param name="breadcrumb" value="Site Status" />
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
        <td class="<c:out value='${status.status}'/> divider" >
          <c:choose>
            <c:when test="${! empty status.link}">
              <a href="<c:out value='${status.link}'/>"><c:out value="${status.downEntityCount}" /> of <c:out value="${status.totalEntityCount}" /></a>
            </c:when>
            <c:otherwise>
              <c:out value="${status.downEntityCount}" /> of <c:out value="${status.totalEntityCount}" />
            </c:otherwise>
          </c:choose>
        </td>
      </tr>
    </c:forEach>
  </table>

<jsp:include page="/includes/footer.jsp" flush="false" />

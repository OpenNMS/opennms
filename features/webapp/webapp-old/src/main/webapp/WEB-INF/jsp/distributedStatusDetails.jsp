<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Distributed Status Details" />
	<jsp:param name="headTitle" value="Distributed Status Details" />
	<jsp:param name="breadcrumb" value="<a href='distributedStatusSummary.htm'>Distributed Status</a>" />
	<jsp:param name="breadcrumb" value="Details" />
</jsp:include>

<c:choose>
  <c:when test="${webTable.errors.errorCount > 0}">
    <h3><spring:message code="error"/></h3>
    <div class="boxWrapper">
      <ul class="error">
        <c:forEach var="err" items="${webTable.errors.allErrors}">
          <li><spring:message code="${err.code}" arguments="${err.arguments}"/></li>
        </c:forEach>
      </ul>
    </div>
  </c:when>
  
  <c:otherwise>
    <h3>${webTable.title}</h3>
    
    <table>
    
      <tr>
        <c:forEach items="${webTable.columnHeaders}" var="headerCell">
          <th class="${headerCell.styleClass}">
            <c:choose>
              <c:when test="${! empty headerCell.link}">
                <a href="${headerCell.link}">${headerCell.content}</a>
              </c:when>
              <c:otherwise>
                ${headerCell.content}
              </c:otherwise>
            </c:choose>
          </th>
        </c:forEach>
      </tr>
      
      <c:forEach items="${webTable.rows}" var="row">
        <tr class="${row[0].styleClass}">
          <c:forEach items="${row}" var="cell">
            <td class="${cell.styleClass} divider">
              <c:choose>
                <c:when test="${! empty cell.link}">
                  <a href="${cell.link}">${cell.content}</a>
                </c:when>
                <c:otherwise>
                  ${cell.content}
                </c:otherwise>
              </c:choose>
            </td>
          </c:forEach>
        </tr>
      </c:forEach>
    </table>
  
  </c:otherwise>
</c:choose>

<jsp:include page="/includes/footer.jsp" flush="false"/>

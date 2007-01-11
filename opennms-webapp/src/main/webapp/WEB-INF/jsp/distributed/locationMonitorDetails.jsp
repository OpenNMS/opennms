<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Location Monitor Details" />
  <jsp:param name="headTitle" value="Location Monitor  Details" />
  <jsp:param name="breadcrumb" value="<a href='admin/'>Admin</a>" />
  <jsp:param name="breadcrumb" value="<a href='distributedPollerStatus.htm'>Location Monitors</a>" />
  <jsp:param name="breadcrumb" value="Details" />
</jsp:include>

<c:choose>
  <c:when test="${model.errors.errorCount > 0}">
    <h3><spring:message code="error"/></h3>
    <div class="boxWrapper">
      <ul class="error">
        <c:forEach var="err" items="${model.errors.allErrors}">
          <li><spring:message message="${err}"/></li>
        </c:forEach>
      </ul>
    </div>
  </c:when>
  
  <c:otherwise>
    <h3><spring:message message="${model.title}"/></h3>
    <table>
      <c:forEach items="${model.mainDetails}" var="detail">
        <tr>
          <th>
            <spring:message message="${detail.key}"/>
          </th>
          <td>
            <spring:message message="${detail.value}"/>
          </td>
        </tr>
      </c:forEach>
    </table>
    
    <h3><spring:message message="${model.additionalDetailsTitle}"/></h3>
    <table>
      <c:forEach items="${model.additionalDetails}" var="detail">
        <tr>
          <th>
            <spring:message message="${detail.key}"/>
          </th>
          <td>
            <spring:message message="${detail.value}"/>
          </td>
        </tr>
      </c:forEach>
    </table>
  
  </c:otherwise>
</c:choose>

<jsp:include page="/includes/footer.jsp" flush="false"/>

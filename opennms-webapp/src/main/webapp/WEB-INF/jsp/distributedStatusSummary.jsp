<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="/includes/header.jsp" flush="false">
    <jsp:param name="title" value="Distributed Status Summary" />
    <jsp:param name="headTitle" value="Summary" />
    <jsp:param name="breadcrumb" value="Distributed Status" />
</jsp:include>

<jsp:include page="/includes/key.jsp" flush="false" >

   <jsp:param name="clearedCaption" value="CLEARED" />
   <jsp:param name="normalCaption" value="NORMAL" />
   <jsp:param name="indetermCaption" value="INDETERMINATE" />
   <jsp:param name="warnCaption" value="WARNING" />
   <jsp:param name="minorCaption" value="MINOR" />
   <jsp:param name="majorCaption" value="MAJOR" />
   <jsp:param name="criticalCaption" value="CRITICAL" />

 </jsp:include>
<h3><c:out value="${webTable.title}" /></h3>

<table>

  <tr>
  <c:forEach items="${webTable.columnHeaders}" var="headerCell">
    <th class="<c:out value='${headerCell.styleClass}'/>">
      <c:choose>
        <c:when test="${! empty headerCell.link}">
          <a href="<c:out value='${headerCell.link}'/>"><c:out value="${headerCell.content}"/></a>
        </c:when>
        <c:otherwise>
          <c:out value="${headerCell.content}"/>
        </c:otherwise>
      </c:choose>
    </th>
  </c:forEach>
  </tr>
  
  <c:forEach items="${webTable.rows}" var="row">
    <tr class="CellStatus">
      <c:forEach items="${row}" var="cell">
        <td class="<c:out value='${cell.styleClass}'/> divider">
          <c:choose>
            <c:when test="${! empty cell.link}">
                <a href="<c:out value='${cell.link}'/>"><c:out value="${cell.content}"/></a>
            </c:when>
            <c:otherwise>
                 <c:out value="${cell.content}"/>
            </c:otherwise>
          </c:choose>
        </td>
      </c:forEach>
    </tr>
  </c:forEach>
</table>

<jsp:include page="/includes/footer.jsp" flush="false"/>

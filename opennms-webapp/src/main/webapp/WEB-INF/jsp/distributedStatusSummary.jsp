<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="/includes/header.jsp" flush="false">
    <jsp:param name="title" value="Distributed Status Summary" />
    <jsp:param name="headTitle" value="Summary" />
    <jsp:param name="breadcrumb" value="Distributed Status" />
</jsp:include>

<jsp:include page="/includes/key.jsp" flush="false">

   <jsp:param name="clearedCaption" value="Not applicable for this page." />

   <jsp:param name="normalCaption" value="A Green status Cell (Application Up) indicates that *all* of the Application's services 
   are available from at least 1 Started remote poller in that Location." />
   
   <jsp:param name="minorCaption" value="Not applicable for this page." />
   
   <jsp:param name="majorCaption" value="Not applicable for this page." />
   
   <jsp:param name="indetermCaption" value="A Puke colored cell (Indeterminate (no current data)) indicates that there is no
    current data which means there are no Started remote pollers.  If the percentage in this colored cell is > 0, then this means 
    there has been data reported since midnight but there is just no current data being reported." />
    
   <jsp:param name="warnCaption" value="A Yellow status cell (Application Impaired) indicates that 1 or more of the Applications 
   set of IP services are currently reported as unavailable from 1, but not all, of the remote pollers in that location." />
   
   <jsp:param name="criticalCaption" value="A Red status cell (Application Down) indicates that at least 1 of the Application's services
    are currently unavailable from from *all* Started remote pollers in that location." />

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

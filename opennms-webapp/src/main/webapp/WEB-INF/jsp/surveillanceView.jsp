<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>OpenNMS Surveillance View Page</title>
</head>
<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Surveillance View" />
	<jsp:param name="headTitle" value="Surveillance" />
	<jsp:param name="breadcrumb" value="Surveillance" />
</jsp:include>

<body>
<h1 align="center">Surveillance View</h1>

<div id="index-contentmiddle">

<h2>New Table</h2>

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

<h2>Old Table</h2>

<h3> <c:out value="${table.label}" /></h3>

  <table>
  
    <!--  column headers -->
    
    <thead>
      <tr>
        <th>Nodes Down</th>
        <c:forEach items="${table.columnHeaderList}" var="header">
          <th><c:out value="${header}" /></th>
        </c:forEach>
      </tr>
    </thead>
    
    <!-- print the rows -->
    
    <c:forEach items="${table.rowHeaderList}" var="header">
      <tr>
        <td><c:out value="${header}" /></td>
          <c:forEach items="${table.columnOrderedRowsWithHeaders}" var="statusMap">
            <td class="<c:out value="${statusMap[$header].status}" />" >
              <c:out value="${statusMap[$header].downEntityCount}" /> of <c:out value="${statusMap[$header].totalEntityCount}" /> 
            </td>
          </c:forEach>
      </tr>
    </c:forEach>
    
  </table>
</div>
</body>
<jsp:include page="/includes/footer.jsp" flush="false" />
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="/includes/header.jsp" flush="false">
    <jsp:param name="title" value="System Reports" />
    <jsp:param name="headTitle" value="System Reports" />
    <jsp:param name="breadcrumb" value="Support" />
    <jsp:param name="breadcrumb" value="System Reports" />
</jsp:include>

<script language="javascript" type="text/javascript">
<!-- Begin
function checkAll(field)
{
for (i = 0; i < field.length; i++)
	field[i].checked = true ;
}

function uncheckAll(field)
{
for (i = 0; i < field.length; i++)
	field[i].checked = false ;
}

function toggle(box, field)
{
	if (box.checked == true) {
		checkAll(field);
	} else {
		uncheckAll(field);
	}
}
//  End -->
</script>

<form name="report" action="admin/support/systemReport.htm" method="post">

<h3>Plugins &nbsp;&nbsp;&nbsp; <input type="checkbox" name="all" onclick="toggle(document.report.all, document.report.plugins)" checked /> All</h3>
<p class="normal">Choose which plugins to enable:</p>
<c:forEach items="${report.plugins}" var="plugin">
 <input type="checkbox" name="plugins" value="${plugin.name}" checked /> <c:out value="${plugin.name}" />: <c:out value="${plugin.description}" /> <br />
</c:forEach>

<h3>Report Type</h3>
<p class="normal">Choose which report to use:</p>
<p class="normal">
<select name="formatter">
<c:forEach items="${report.formatters}" var="formatter">
 <c:choose>
  <c:when test="${formatter.name == 'text'}">
   <c:set var="formatterSelected" value="selected" />
  </c:when>
  <c:otherwise>
   <c:set var="formatterSelected" value="" />
  </c:otherwise>
 </c:choose>
 <option value="<c:out value="${formatter.name}" />" <c:out value="${formatterSelected}" />><c:out value="${formatter.name}: ${formatter.description}" /></option>
</c:forEach>
</select>
Output: <input type="text" name="output" />
</p>

<input type="hidden" name="operation" value="run" />
<input type="submit" />
</form>

<jsp:include page="/includes/footer.jsp" flush="false"/>

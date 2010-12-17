<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="/includes/header.jsp" flush="false">
    <jsp:param name="title" value="System Reports" />
    <jsp:param name="headTitle" value="System Reports" />
    <jsp:param name="breadcrumb" value="Support" />
    <jsp:param name="breadcrumb" value="System Reports" />
    <jsp:param name="breadcrumb" value="${report.type}" />
</jsp:include>

<h3>Plugins</h3>
<p class="normal">Choose which plugins to enable:</p>
<c:forEach items="${report.plugins}" var="plugin">
 <c:out value="using plugin ${plugin.name}" />
</c:forEach>

<jsp:include page="/includes/footer.jsp" flush="false"/>

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<jsp:include page="/includes/header.jsp" flush="false">
	<jsp:param name="title" value="Distributed Status Summary" />
	<jsp:param name="headTitle" value="Summary" />
	<jsp:param name="breadcrumb" value="Distributed Status" />
</jsp:include>

<h3>Distributed Status Summary Error: ${error.shortDescr} </h3>

<p>
${error.longDescr}
</p>
<p>
Click <a href="admin/applications.htm">here</a> to see defined applications
</p>

<jsp:include page="/includes/footer.jsp" flush="false"/>

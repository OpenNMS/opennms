<%@ page language="java" contentType="text/html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
	<head><title>Edit Schedule</title></head>
	<body>
		<c:set value="schedule[${currentSchedIndex}]" var="schedId" />
		<form action="" method="post">
			<label for="<c:out value="${schedId}.name"/>">Name:</label>
			<input id="<c:out value="${schedId}.name"/>" type="text" readonly="true" value="<c:out value="${currentSchedule.name}"/>"/>
			<label for="<c:out value="${schedId}.type"/>">Type:</label>
			<input id="<c:out value="${schedId}.type"/>" type="text" readonly="true" value="<c:out value="${currentSchedule.type}"/>"/>
			
		</form>
	</body>
</html>

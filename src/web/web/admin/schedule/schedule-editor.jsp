<%@ page language="java" contentType="text/html"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<html>
	<head><title>Schedule Editor</title></head>
	<body>
		
		<table id="schedules">
		<c:forEach var="sched" items="${scheduleList}" varStatus="schedStatus">
			<c:set value="sched.${schedStatus.count}" var="schedId"/>
			<tr>
			  <!--  The name of the schedule -->
			  <td id="<c:out value="${schedId}.name"/>"><c:out value="${sched.name}"/></td>
			  
			  <!--  The tpe of the schedule -->
			  <td id="<c:out value="${schedId}.type"/>"><c:out value="${sched.type}"/></td>
			  
			  <!--  A cell containing a table of the times for this schedule -->
			  <td id="<c:out value="${schedId}.times"/>">
			  <table id="<c:out value="${schedId}.timesTable"/>">
			  <!--  For each time  -->
			  <c:forEach var="time" items="${sched.time}" varStatus="timeStatus">
			  <c:set value="${schedId}.time.${timeStatus.count}" var="timeId"/>
			  <tr>
			  	  <!--  Only include the 'day' column if its not a specific type -->
				  <c:if test="${sched.type ne 'specific'}">
				    <td id="<c:out value="${timeId}.day"/>"><c:out value="${time.day}"/></td>
				  </c:if>
				  <!--  the begin time  -->
				  <td id="<c:out value="${timeId}.begins"/>"><c:out value="${time.begins}"/></td>
				  <!--  the end time -->
				  <td id="<c:out value="${timeId}.ends"/>"><c:out value="${time.ends}"/></td>
			  </tr>
			  </c:forEach>
			  </table>
			  </td>
			  
			  <!--  This is the baseUrl for this schedule -->
			  <c:url value="schedule-editor" var="baseUrl">
			  	<c:param name="id" value="${schedId}"/>
			  	<c:if test="${! empty param.file}">
			  		<c:param name="file" value="${param.file}"/>
			  	</c:if>
			  </c:url>
			  <!--  This is the edit url created by appending the edit cmd to the base -->
			  <c:url value="${baseUrl}" var="editUrl">
			  	<c:param name="do" value="edit"/>
			  </c:url>
			  <!--  This is the delete url created by appending the delete cmd to the base -->
			  <c:url value="${baseUrl}" var="deleteUrl">
			  	<c:param name="do" value="delete"/>
			  </c:url>
			  
			  <!-- The edit link for initiating an edit of this schedule -->
			  <td><a id="<c:out value="${schedId}.edit"/>" href="<c:out value="${editUrl}"/>">Edit</a></td>
			  <!-- The delete link for deleting this schedule -->
			  <td><a id="<c:out value="${schedId}.delete"/>" href="<c:out value="${deleteUrl}"/>">Delete</a></td>			  
			</tr>
			
		</c:forEach>
		</table>
		
	</body>
</html>

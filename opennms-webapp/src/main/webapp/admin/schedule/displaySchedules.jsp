<%@page language="java"
	contentType="text/html"
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
	<head><title>Schedule Editor</title></head>
	<body>
	
		<!--  Table of schedules -->
		<table id="schedules" border="1">
		<c:forEach var="sched" items="${schedMgr.schedule}" varStatus="schedStatus">
			<c:set value="schedule[${schedStatus.index}]" var="schedId"/>
			<tr>
			  <!--  The name of the schedule -->
			  <td id="<c:out value="${schedId}.name"/>"><c:out value="${sched.name}"/></td>
			  
			  <!--  The type of the schedule -->
			  <td id="<c:out value="${schedId}.type"/>"><c:out value="${sched.type}"/></td>
			  
			  <!--  A cell containing a table of the times for this schedule -->
			  <td id="<c:out value="${schedId}.times"/>">
			  <table id="<c:out value="${schedId}.timesTable"/>">
			  <!--  For each time  -->
			  <c:forEach var="time" items="${sched.time}" varStatus="timeStatus">
			  <c:set value="${schedId}.time[${timeStatus.index}]" var="timeId"/>
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

			  <!-- The add time button for initiating an edit of this schedule -->
			  <td>
			    <form method="get" id="<c:out value="${schedId}.addTimeForm"/>">
			    		<input id="<c:out value="${schedId}.addTimeOp"/>" type="hidden" name="op" value="addTime"/>
			    		<input id="<c:out value="${schedId}.addTimeIndex"/>" type="hidden" name="scheduleIndex" value="<c:out value="${schedStatus.index}"/>">
			    		<input id="<c:out value="${schedId}.doAddTime"/>" type="submit" name="submit" value="Add Time"/>
			    </form>
			  </td>
			  <!-- The edit button for initiating an edit of this schedule -->
			  <td>
			    <form method="get" id="<c:out value="${schedId}.editForm"/>">
			    		<input id="<c:out value="${schedId}.editOp"/>" type="hidden" name="op" value="edit"/>
			    		<input id="<c:out value="${schedId}.editIndex"/>" type="hidden" name="scheduleIndex" value="<c:out value="${schedStatus.index}"/>">
			    		<input id="<c:out value="${schedId}.doEdit"/>" type="submit" name="submit" value="Edit"/>
			    </form>
			  </td>
			  <!-- The delete button for deleting this schedule -->
			  <td>
			    <form method="get" id="<c:out value="${schedId}.deleteForm"/>" onSubmit="return confirm('Are you sure you wish to delete this schedule?');">
			    		<input id="<c:out value="${schedId}.deleteOp"/>" type="hidden" name="op" value="delete"/>
			    		<input id="<c:out value="${schedId}.deleteIndex"/>" type="hidden" name="scheduleIndex" value="<c:out value="${schedStatus.index}"/>">
			    		<input id="<c:out value="${schedId}.doDelete"/>" type="submit" name="submit" value="Delete" />
			    </form>
			  </td>
			</tr>
			
		</c:forEach>
		</table>
		<form id="newScheduleForm" method="get">
		<table>
			<tr>
			<input id="newScheduleOp" name="op" type="hidden" value="newSchedule"/>
			<td><input id="newScheduleName" name="name" type="text" readonly="true" value="Schedule Name"/></td>
			<td>
				<SELECT id="newScheduleType" name="type" value="Monthly Schedule">
				   <OPTION value="specific">Specific Time Period</OPTION>
				   <OPTION value="monthly">Monthly Schedule</OPTION>
				   <OPTION value="weekly">Weekly Schedule</OPTION>
				</SELECT>
			</td>
			<td><input id="doNewSchedule" type="submit" name="submit" value="New Schedule"/></td>
		</table>
		</form>
		
	</body>
</html>

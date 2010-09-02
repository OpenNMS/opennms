<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
%>

<%@page import="org.opennms.util.ilr.Collector"%>
<%@page import="java.io.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>



<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Instrumentation Log Reader" />
  <jsp:param name="headTitle" value="Instrumentation Log Reader" />
  <jsp:param name="location" value="Instrumentation Log Reader" />  
  <jsp:param name="breadcrumb" value="Instrumentation Log Reader" />
</jsp:include>

<%

String opennmsHome = System.getProperty("opennms.home");

Collector c = new Collector();

String baseFileName = opennmsHome + "/logs/daemon/instrumentation.log";

for(int i = 5; i > 0; i--) {
	String fileName = baseFileName + "." + i;
	File file = new File(fileName);
	if (file.exists()) {
		c.readLogMessagesFromFile(fileName);
	}
}
File file = new File(baseFileName);



if(file.exists() && file.length() != 0) {
	c.readLogMessagesFromFile(baseFileName);
}

pageContext.setAttribute("fileLength",file.length());
pageContext.setAttribute("collector",c);
pageContext.setAttribute("OpennmsHome",opennmsHome);

%>
<c_rt:set var="nan" value="<%=java.lang.Double.NaN%>"/>

<c:choose>
	<c:when test="${fileLength == 0}">
		<script language="JavaScript">
			alert ("Instrumentation.log either does not exist or is empty. Check to see if you have it set to DEBUG in log4j.properties")
		</script>
	</c:when>
</c:choose>

<br/>
<p>
StartTime: ${collector.startTime}
</p>
<p>
EndTime: ${collector.endTime}
</p>
<p>
Duration: ${collector.formattedDuration}
</p>
<p>
Total Services ${collector.serviceCount}
</p>
<p>
Threads Used: ${collector.threadCount}
</p>

<table>
<tr>
<th>Service</th>
<th>Collections</th>
<th>Average Collection Time</th>
<th>Average Time Between Collections</th>
<th>Successful Collections</th>
<th>Success Percentage</th>
<th>Average Successful Collection Time</th>
<th>Unsuccessful Collections</th>
<th>Unsuccessful Percentage</th>
<th>Average Unsuccessful Collection Time</th>
</tr>
<c:forEach  var="svcCollector" items="${collector.serviceCollectors}">
<c:choose>
    <c:when test="${svcCollector.successPercentage > 50}" >
        <tr class="Normal">
    </c:when>
    <c:when test="${svcCollector.successPercentage == -1}" >
        <tr class="Warning">
    </c:when>
    <c:otherwise>
        <tr class="Critical">
    </c:otherwise>
</c:choose>
<td><a href="element/node.jsp?node=${svcCollector.parsedServiceID}">${svcCollector.serviceID}</a></td>
<td align="right">${svcCollector.collectionCount}</td>
<td align="right">${svcCollector.averageCollectionDuration}</td>
<td align="right">${svcCollector.averageDurationBetweenCollections}</td>
<td align="right">${svcCollector.successfulCollectionCount}</td>
<c:choose>
    <c:when test="${svcCollector.successPercentage == -1}" >
        <td align="right">No Collections</td>
    </c:when>
    <c:otherwise>
        <td align="right"><fmt:formatNumber type="number" maxFractionDigits="1" minFractionDigits="1" value="${svcCollector.successPercentage}" /></td>
    </c:otherwise>
</c:choose>
<td align="right">${svcCollector.averageSuccessfulCollectionDuration}</td>
<td align="right">${svcCollector.errorCollectionCount}</td>
<c:choose>	
	<c:when test="${svcCollector.errorPercentage == -1}">
		<td align="right">No Collections</td>
	</c:when>
	<c:otherwise>
		<td align="right"><fmt:formatNumber type="number" maxFractionDigits="1" minFractionDigits="1" value="${svcCollector.errorPercentage}" /></td>
	</c:otherwise>
</c:choose>
<td align="right">${svcCollector.averageErrorCollectionDuration}</td>
</tr>

</c:forEach>
</table>

	<hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>

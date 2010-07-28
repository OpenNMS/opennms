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
// Modifications:
//
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Feb 04: Added a check to prevent null entries in queries. Bug #536.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

<%@page import="edu.ncsu.pdgrenon.Collector"%>
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
c.readLogMessagesFromFile(opennmsHome + "/logs/daemon/instrumentation.log");

pageContext.setAttribute("collector",c);

%>

<br/>
<p>
StartTime: ${collector.startTime}
</p>
<p>
EndTime: ${collector.endTime}
</p>
<p>
Duration: ${collector.duration}
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
<th>Total Collection Time</th>
</tr>
<c:forEach  var="svcCollector" items="${collector.serviceCollectors}">
<tr>
<td>${svcCollector.serviceID}</td>
<td>${svcCollector.collectionCount}</td>
<td>${svcCollector.averageCollectionDuration}</td>
<td>${svcCollector.totalCollectionDuration}</td>
</tr>

</c:forEach>
</table>
    
	<hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>

<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	isErrorPage="true"
	import="org.opennms.web.controller.statisticsReports.*"
%>

<%
    StatisticsReportIdNotFoundException e = null;
    
    if( exception instanceof StatisticsReportIdNotFoundException ) {
        e = (StatisticsReportIdNotFoundException)exception;
    }
    else if( exception instanceof ServletException ) {
        e = (StatisticsReportIdNotFoundException)((ServletException)exception).getRootCause();
    } else {
        throw new ServletException( "This error page does not handle this exception type.", exception );
    }
%>


<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Error" />
  <jsp:param name="headTitle" value="Statistics Report Not Found" />
  <jsp:param name="headTitle" value="Error" />
  <jsp:param name="breadcrumb" value="Error" />
</jsp:include>

<h1>Statistics Report ID Not Found</h1>
    
<p>
  A statistics report could not be found for the identifier <%=e.getBadID()%>.
</p>

<p>
  You can enter a new identifier here or <a href="statisticsReports/index.htm">browse
  the list of statistics reports</a> to find the one you are looking for.
</p>

<form method="get" action="statisticsReports/report.htm">
  <p>
    Get&nbsp;Statistics&nbsp;Report&nbsp;ID:
    <br/>
    <input type="text" name="id"/>
    <input type="submit" value="Search"/>
  </p>
</form>

<jsp:include page="/includes/footer.jsp" flush="false" />

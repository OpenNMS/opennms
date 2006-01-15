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
// 2005 Oct 01: Convert to use CSS for layout. -- DJ Gregor
// 2005 Oct 01: Refactor relative date code. -- DJ Gregor
// 2003 Feb 28: Corrected day/week/month/year reports on some browsers.
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Feb 28: Added day/week/month/year reports.
// 2002 Nov 26: Fixed breadcrumbs issue.
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
//      http://www.opennms.com/
//

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
	import="org.opennms.web.*,
		org.opennms.web.performance.*,
		org.opennms.web.graph.*,
		org.opennms.core.resource.Vault,
		java.util.*,
		java.io.*,
		org.opennms.web.element.NetworkElementFactory
	"
%>

<%@ include file="/WEB-INF/jspf/graph-common.jspf"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<%!
    protected GraphModel m_model = null;
    
    public void init() throws ServletException {
        try {
            m_model = new PerformanceModel(Vault.getHomeDir());
        } catch (Throwable t) {
            throw new ServletException("Could not initialize the PerformanceModel", t);
        }

	initPeriods();
    }
%>

<%
    String[] requiredParameters = new String[] { "report", "node" };

    // required parameter reports
    String reports[] = request.getParameterValues("reports");
    if (reports == null) {
        throw new MissingParameterException("report", requiredParameters);
    }
        
    // required parameter node
    String nodeIdString = request.getParameter("node");
    if (nodeIdString == null) {
        throw new MissingParameterException("node", requiredParameters);
    }
    int nodeId = Integer.parseInt(nodeIdString);
    
    // optional parameter intf
    String intf = request.getParameter("intf");

    // see if the start and end time were explicitly set as params    
    String start = request.getParameter("start");
    String end   = request.getParameter("end");

    String relativeTime = request.getParameter("relativetime");
        
    if ((start == null || end == null) && relativeTime != null) {
	TimePeriod period = m_periods[0]; // default to the first one
	for (int i = 0; i < m_periods.length; i++) {
	    if (relativeTime.equals(m_periods[i].getId())) {
		period = m_periods[i];
		break;
	    }
	}
        Calendar cal = new GregorianCalendar();
        end = Long.toString(cal.getTime().getTime());
        cal.add(period.getOffsetField(), period.getOffsetAmount());
        start = Long.toString(cal.getTime().getTime());        
    }
    
    if (start == null || end == null) {
        String startMonth = request.getParameter("startMonth");
        String startDate  = request.getParameter("startDate");
        String startYear  = request.getParameter("startYear");
        String startHour  = request.getParameter("startHour");

        String endMonth = request.getParameter("endMonth");
        String endDate  = request.getParameter("endDate");
        String endYear  = request.getParameter("endYear");
        String endHour  = request.getParameter("endHour");

        if (startMonth == null || startDate == null || startYear == null
	    || startHour == null || endMonth == null || endDate == null
            || endYear == null   || endHour == null ) {
            throw new MissingParameterException("startMonth", new String[] {
		"startMonth", "startDate", "startYear", "startHour",
		"endMonth", "endDate", "endYear", "endHour" } );
        }

        Calendar startCal = Calendar.getInstance();
        startCal.set( Calendar.MONTH, Integer.parseInt( startMonth ));
        startCal.set( Calendar.DATE, Integer.parseInt( startDate ));
        startCal.set( Calendar.YEAR, Integer.parseInt( startYear ));
        startCal.set( Calendar.HOUR_OF_DAY, Integer.parseInt( startHour ));
        startCal.set( Calendar.MINUTE, 0 );
        startCal.set( Calendar.SECOND, 0 );
        startCal.set( Calendar.MILLISECOND, 0 );

        Calendar endCal = Calendar.getInstance();
        endCal.set( Calendar.MONTH, Integer.parseInt( endMonth ));
        endCal.set( Calendar.DATE, Integer.parseInt( endDate ));
        endCal.set( Calendar.YEAR, Integer.parseInt( endYear ));
        endCal.set( Calendar.HOUR_OF_DAY, Integer.parseInt( endHour ));
        endCal.set( Calendar.MINUTE, 0 );
        endCal.set( Calendar.SECOND, 0 );
        endCal.set( Calendar.MILLISECOND, 0 );

        start = Long.toString(startCal.getTime().getTime());
        end   = Long.toString(endCal.getTime().getTime());
    }

    // gather information for displaying around the image
    Date startDate = new Date(Long.parseLong(start));
    Date endDate   = new Date(Long.parseLong(end));

    GraphResults results = new GraphResults();
    results.setModel(m_model);
    results.setNodeId(nodeId);
    results.setIntf(intf);
    results.setReports(reports);
    results.setStart(startDate);
    results.setEnd(endDate);
    results.setRelativeTime(relativeTime);

    results.initializeGraphs();

    pageContext.setAttribute("results", results);
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Performance Results" />
  <jsp:param name="headTitle" value="Results" />
  <jsp:param name="headTitle" value="Performance" />
  <jsp:param name="headTitle" value="Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" value="<a href='performance/index.jsp'>Performance</a>" />
  <jsp:param name="breadcrumb" value="Results" />
</jsp:include>

<div id="graph-results">
  <h3>
    Node: <a href="element/node.jsp?node=<c:out value="${results.nodeId}"/>"><c:out value="${results.nodeLabel}"/></a>
    <c:if test="${!empty results.intf}">
      <br/>
      Interface: <c:out value="${results.humanReadableNameForIfLabel}"/>
    </c:if>
  </h3>

  <% printRelativeTimeForm(out, relativeTime, nodeId, intf, reports,
			   "performance/results.jsp"); %>

  <h3>Interface Performance Data</h3>
  <strong>From</strong> <c:out value="${results.start}"/> <br/>
  <strong>To</strong> <c:out value="${results.end}"/> <br/>

  <c:choose>
    <c:when test="${!empty results.graphs}">
      <c:forEach var="graph" items="${results.graphs}">
	<a href="/opennms/performance/zoom.jsp?intf=<c:out value="${graph.intf}"/>&amp;node=<c:out value="${graph.nodeId}"/>&amp;reports=<c:out value="${graph.name}"/>&amp;start=<c:out value="${graph.start.time}"/>&amp;end=<c:out value="${graph.end.time}"/>&amp;props=<c:out value="${results.nodeId}"/>/strings.properties">
	  <img src="<c:out value="${graph.graphURL}"/>&amp;props=<c:out value="${results.nodeId}"/>/strings.properties"/>
	</a>

	<br/>
      </c:forEach>
    </c:when>

    <c:otherwise>
      No SNMP performance data has been gathered at this level
    </c:otherwise>
  </c:choose>


  <% printRelativeTimeForm(out, relativeTime, nodeId, intf, reports,
			   "performance/results.jsp"); %>

  <jsp:include page="/includes/bookmark.jsp" flush="false" />
</div>

<jsp:include page="/includes/footer.jsp" flush="false" />

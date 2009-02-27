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
// 2007 Feb 19: Convert to MVC. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

<%-- 
  This page is included by other JSPs to create a box containing an
  abbreviated list of outages.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
        contentType="text/html"
        session="true"
        import="org.opennms.web.outage.*" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%!
    public static final int ROW_COUNT = 12;
    private OutageModel m_model = new OutageModel();    
%>

<%
    OutageSummary[] summaries = m_model.getCurrentOutageSummaries();
    int last = (summaries.length <= ROW_COUNT) ? summaries.length : ROW_COUNT;
    OutageSummary[] displaySummaries = new OutageSummary[last];
    System.arraycopy(summaries, 0, displaySummaries, 0, last);
    
    pageContext.setAttribute("summaries", displaySummaries);
    pageContext.setAttribute("moreCount", summaries.length - displaySummaries.length);
%>

<!-- includes/servicesdown-box.jsp -->
<c:url var="headingLink" value="outage/current.jsp"/>
<h3 class="o-box"><a href="${headingLink}">Nodes with Outages</a></h3>
<div class="boxWrapper">
  <c:choose>
    <c:when test="${empty summaries}">
      <p class="noBottomMargin">
        There are no current outages
      </p>
    </c:when>

    <c:otherwise>
      <ul class="o-box plain">
        <c:forEach var="summary" items="${summaries}">
          <c:url var="nodeLink" value="element/node.jsp">
            <c:param name="node" value="${summary.nodeId}"/>
          </c:url>
          <li><a href="${nodeLink}">${summary.nodeLabel}</a> (${summary.fuzzyTimeDown})</li>
        </c:forEach>
      </ul>
    
      <c:if test="${moreCount > 0}">
        <p class="noBottomMargin" align="right">
          <c:url var="moreLink" value="outage/current.jsp"/>
          <a href="${moreLink}">${moreCount} more...</a>
        </p>
      </c:if>
    </c:otherwise>
  </c:choose>
</div>
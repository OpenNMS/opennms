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
	import="java.util.*,
		org.opennms.web.WebSecurityUtils,
		org.opennms.web.outage.*,
		java.text.DateFormat
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%!
    public static DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
%>

<%
	Outage outage = (Outage)request.getAttribute("outage");

    if( outage == null ) {
        throw new org.opennms.web.outage.OutageIdNotFoundException( "An outage with this id was not found.", (String)request.getAttribute("id") );
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Outage Details" />
  <jsp:param name="headTitle" value="Detail" />
  <jsp:param name="headTitle" value="Outages" />
  <jsp:param name="breadcrumb" value="<a href='outage/list'>Outages</a>" />
  <jsp:param name="breadcrumb" value="Detail " />
</jsp:include>

      <!-- page title -->
      <h3>Outage: <%=outage.getId()%></h3>
          
      <table class="standardfirst">
        <tr>
          <td class="standardheader" width="10%">Node:</td>
          <td class="standard">
            <% if( outage.getNodeId() > 0 ) { %>
              <a href="element/node.jsp?node=<%=outage.getNodeId()%>"><%=outage.getNodeLabel()%></a>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          
          <td class="standardheader" width="10%">Lost&nbsp;Service&nbsp;Time:</td>
          <td class="standard"><%=DATE_FORMAT.format(outage.getLostServiceTime())%></td>
          
          <td class="standardheader" width="10%">Lost&nbsp;Service&nbsp;Event:</td>
          <td class="standard"><a href="event/detail.jsp?id=<%=outage.getLostServiceEventId()%>"><%=outage.getLostServiceEventId()%></a></td>          
          
        </tr>
        <tr>
          <td class="standardheader">Interface:</td>
          <td class="standard">
            <% if( outage.getIpAddress() != null ) { %>
              <% if( outage.getNodeId() > 0 ) { %>
                <c:url var="interfaceLink" value="element/interface.jsp">
                  <c:param name="node" value="<%=String.valueOf(outage.getNodeId())%>"/>
                  <c:param name="intf" value="<%=outage.getIpAddress()%>"/>
                </c:url>
                <a href="${interfaceLink}"><%=outage.getIpAddress()%></a>
              <% } else { %>
                <%=outage.getIpAddress()%>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          
          <td class="standardheader">Regained&nbsp;Service:</td>
          <td class="standard">
            <% Date regainTime = outage.getRegainedServiceTime(); %>
            
            <% if(regainTime != null) { %>
              <%=DATE_FORMAT.format(regainTime)%>
            <% } else { %>
              <% String label = OutageUtil.getStatusLabel(outage); %>              
              <%=(label == null) ? "&nbsp;" : label %>
            <% } %>
          </td>

          <td class="standardheader">Regained&nbsp;Service&nbsp;Event:</td>
          <td class="standard">
            <% Integer regainedEventId = outage.getRegainedServiceEventId(); %>
            <% if(regainedEventId != null) { %>
              <a href="event/detail.jsp?id=<%=regainedEventId%>">
                <%=regainedEventId%>
              </a>
            <% } else { %>
              <% String label = OutageUtil.getStatusLabel(outage); %>              
              <%=(label == null) ? "&nbsp;" : label %>
            <% } %>
          </td>          
          
        </tr>
        <tr>
          <td class="standardheader">Service:</td>
          <td class="standard">
            <% if( outage.getServiceName() != null ) { %>
              <% if( outage.getIpAddress() != null && outage.getNodeId() > 0 ) { %>
                <c:url var="serviceLink" value="element/service.jsp">
                  <c:param name="node" value="<%=String.valueOf(outage.getNodeId())%>"/>
                  <c:param name="intf" value="<%=outage.getIpAddress()%>"/>
                  <c:param name="service" value="<%=String.valueOf(outage.getServiceId())%>"/>
                </c:url>
                <a href="${serviceLink}"><c:out value="<%=outage.getServiceName()%>"/></a>              
              <% } else { %>
                <c:out value="<%=outage.getServiceName()%>"/>
              <% } %>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          <td class="standard" colspan="4">&nbsp;</td>
        </tr>
      </table>

<jsp:include page="/includes/footer.jsp" flush="false" />

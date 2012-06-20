<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
	import="java.util.*,
		org.opennms.core.utils.WebSecurityUtils,
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

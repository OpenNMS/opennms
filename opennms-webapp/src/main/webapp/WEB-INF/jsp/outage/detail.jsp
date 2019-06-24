<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
	import="java.util.*,
		org.opennms.web.outage.*,
		java.text.DateFormat,
		org.opennms.core.utils.WebSecurityUtils
	"
%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/taglib.tld" prefix="onms" %>

<%!
    public static DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
%>

<%
	Outage outage = (Outage)request.getAttribute("outage");

    if( outage == null ) {
        throw new org.opennms.web.outage.OutageIdNotFoundException( "An outage with this ID was not found.", WebSecurityUtils.sanitizeString((String)request.getAttribute("id")) );
    }
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Outage Details" />
  <jsp:param name="headTitle" value="Detail" />
  <jsp:param name="headTitle" value="Outages" />
  <jsp:param name="breadcrumb" value="<a href='outage/list'>Outages</a>" />
  <jsp:param name="breadcrumb" value='<%="Outage " + outage.getId()%>' />
</jsp:include>

<div class="card">
  <div class="card-header">
    <span>Outage <%=outage.getId()%></span>
  </div>

  <table class="table table-sm severity">
        <tr class="d-flex">
          <th class="col-2">Node</th>
          <td class="col-2">
            <% if( outage.getNodeId() > 0 ) { %>
              <a href="element/node.jsp?node=<%=outage.getNodeId()%>"><%=outage.getNodeLabel()%></a>
            <% } else {%>
              &nbsp;
            <% } %>
          </td>
          
          <th class="col-2">Lost&nbsp;Service&nbsp;Time</th>
          <td class="col-2"><onms:datetime date="<%=outage.getLostServiceTime()%>" /></td>
          
          <th class="col-2">Lost&nbsp;Service&nbsp;Event</th>
          <td class="col-2"><a href="event/detail.jsp?id=<%=outage.getLostServiceEventId()%>"><%=outage.getLostServiceEventId()%></a></td>
          
        </tr>
        <tr class="d-flex">
          <th class="col-2">Interface</th>
          <td class="col-2">
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
          
          <th class="col-2">Regained&nbsp;Service&nbsp;Time</th>
          <td class="col-2">
            <% Date regainTime = outage.getRegainedServiceTime(); %>
            
            <% if(regainTime != null) { %>
              <onms:datetime date="<%=regainTime%>" />
            <% } else { %>
              <% String label = OutageUtil.getStatusLabel(outage); %>
              <%=(label == null) ? "&nbsp;" : label %>
            <% } %>
          </td>

          <th class="col-2">Regained&nbsp;Service&nbsp;Event</th>
          <td class="col-2">
            <% Integer regainedEventId = outage.getRegainedServiceEventId(); %>
            <% if(regainedEventId != null && regainedEventId > 0) { %>
              <a href="event/detail.jsp?id=<%=regainedEventId%>">
                <%=regainedEventId%>
              </a>
            <% } else { %>
              <% String label = OutageUtil.getStatusLabel(outage); %>
              <%=(label == null) ? "&nbsp;" : label %>
            <% } %>
          </td>
        </tr>
        <tr class="d-flex">
          <th class="col-2">Service</th>
          <td class="col-2">
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
          <th class="col-2">Event Source Location</th>
          <td class="col-2">
            <% if( outage.getEventLocation() != null ) { %>
            <%=outage.getEventLocation()%>
            <% } else { %>
            &nbsp;
            <% } %>
          </td>
          <th class="col-2">Node Location</th>
          <td class="col-2">
            <% if( outage.getLocation() != null ) { %>
            <%=outage.getLocation()%>
            <% } else { %>
            &nbsp;
            <% } %>
          <td>
        </tr>
      </table>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

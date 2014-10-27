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
	import="org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.notification.*,
		org.opennms.web.element.*,
                org.opennms.web.event.*
	"
%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%!
    NotificationModel model = new NotificationModel();
%>

<%
    String noticeIdString = request.getParameter("notice");

    String eventSeverity;
    
    int noticeID = -1;
    
    try {
        noticeID = WebSecurityUtils.safeParseInt( noticeIdString );
    }
    catch( NumberFormatException e ) {
        throw new NoticeIdNotFoundException("The notice id must be an integer.",
					     noticeIdString );
    }
    
    Notification notice = this.model.getNoticeInfo(noticeID);
    
    if( notice == null ) {
        throw new NoticeIdNotFoundException("An notice with this id was not found.", String.valueOf(noticeID));
    }

    if (NoticeFactory.canDisplayEvent(notice.getEventId())) {
		Event event = EventFactory.getEvent(notice.getEventId());
		eventSeverity = event.getSeverity().getLabel();
    } else {
		eventSeverity = new String("Cleared");
    }

%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Notification Detail" />
  <jsp:param name="headTitle" value="Notification Detail" />
  <jsp:param name="breadcrumb" value="<a href='notification/index.jsp'>Notification</a>" />
  <jsp:param name="breadcrumb" value="Detail" />
</jsp:include>

<h3>Notice #<%=notice.getId()%> 
  <% if ( NoticeFactory.canDisplayEvent(notice.getEventId()) ) { %>
    from event #<a href="event/detail.jsp?id=<%=notice.getEventId()%>"><%=notice.getEventId()%></a>
  <% } %>
</h3>
      
<table>
  <tr class="<%=eventSeverity%>">
    <td width="15%">Notification Time</td>
    <td width="17%"><fmt:formatDate value="<%=notice.getTimeSent()%>" type="BOTH" /></td>
    <td width="15%">Time&nbsp;Replied</td>
    <td width="17%">
      <c:choose>
        <c:when test="<%=notice.getTimeReplied() != null%>">
          <fmt:formatDate value="<%=notice.getTimeReplied()%>" type="BOTH" />
        </c:when>
        <c:otherwise>
          &nbsp;
        </c:otherwise>
      </c:choose>
    </td>
    <td width="15%">Responder</td>
    <td width="17%"><%=notice.getResponder()!=null ? notice.getResponder() : "&nbsp;"%></td>
  </tr>
  <tr class="<%=eventSeverity%>">
    <td width="15%">Node</td>
    <td width="17%">
      <%if (NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(notice.getNodeId())!=null) { %>
        <a href="element/node.jsp?node=<%=notice.getNodeId()%>"><c:out value="<%=NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(notice.getNodeId())%>"/></a>
      <% } else { %>
        &nbsp;
      <% } %>
    </td>

    <td width="15%">Interface</td>

    <td width="17%">
      <%if (NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(notice.getNodeId())!=null && notice.getIpAddress()!=null) { %>
        <c:url var="interfaceLink" value="element/interface.jsp">
          <c:param name="node" value="<%=String.valueOf(notice.getNodeId())%>"/>
          <c:param name="intf" value="<%=notice.getIpAddress()%>"/>
        </c:url>
        <a href="${interfaceLink}"><%=notice.getIpAddress()%></a>
      <% } else if (notice.getIpAddress()!=null) { %>
        <%=notice.getIpAddress()%>
      <% } else { %>
        &nbsp;
      <% } %>
    </td>
          
    <td width="15%">Service</td>

    <td width="17%">
      <%if (NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(notice.getNodeId())!=null && notice.getIpAddress()!=null && notice.getServiceName()!=null) { %>
        <c:url var="serviceLink" value="element/service.jsp">
          <c:param name="node" value="<%=String.valueOf(notice.getNodeId())%>"/>
          <c:param name="intf" value="<%=notice.getIpAddress()%>"/>
          <c:param name="service" value="<%=String.valueOf(notice.getServiceId())%>"/>
        </c:url>
        <a href="${serviceLink}"><c:out value="<%=notice.getServiceName()%>"/></a>
      <% } else if (notice.getServiceName()!=null) { %>
        <c:out value="<%=notice.getServiceName()%>"/>
      <% } else { %>
        &nbsp;
      <% } %>
    </td>
  </tr>

  <%if (NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(notice.getNodeId())!=null) { %>
    <tr class="<%=eventSeverity%>">
      <td colspan="6">
        <c:url var="outageLink" value="outage/list.htm">
          <c:param name="filter" value="<%="node=" + notice.getNodeId()%>"/>
        </c:url>
        <a href="${outageLink}">See outages for <c:out value="<%=NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(notice.getNodeId())%>"/></a>
      </td>
    </tr>
  <% } %>
</table>
      
<% if (notice.getNumericMessage() != null || notice.getTextMessage() != null) { %>
  <table>
    <% if (notice.getNumericMessage()!=null) { %>
      <tr class="<%=eventSeverity%>">
        <td width="10%">Numeric Message</td>
      </tr>

      <tr class="<%=eventSeverity%>">
        <td><%=notice.getNumericMessage()%></td>
      </tr>
    <% } %>
          
    <% if (notice.getTextMessage() != null) { %>
      <tr class="<%=eventSeverity%>">
        <td width="10%">Text Message</td>
      </tr>

      <tr class="<%=eventSeverity%>">
        <td><pre><%=notice.getTextMessage()%></pre></td>
      </tr>
    <% } %>
  </table>
<% } %>
      
<table>
  <thead>
    <tr>
      <th>Sent To</th>
      <th>Sent At</th>
      <th>Media</th>
      <th>Contact Info</th>
    </tr>
  </thead>
  
  <%  for (NoticeSentTo sentTo : notice.getSentTo()) { %>

    <tr class="<%=eventSeverity%>">
      <td><%=sentTo.getUserId()%></td>

      <td><fmt:formatDate value="<%=sentTo.getTime()%>" type="BOTH" /></td>

      <td>
        <% if (sentTo.getMedia()!=null && !sentTo.getMedia().trim().equals("")) { %>
          <%=sentTo.getMedia()%>
        <% } else { %>
          &nbsp;
        <% } %>
      </td>

      <td>
        <% if (sentTo.getContactInfo()!=null && !sentTo.getContactInfo().trim().equals("")) { %>
          <%=sentTo.getContactInfo()%>
        <% } else { %>
          &nbsp;
        <% } %>
      </td>
    </tr>
  <% } %>
</table>

<br/>

<% if (notice.getTimeReplied()==null) { %>
  <form method="post" name="acknowledge" action="notification/acknowledge">
    <input type="hidden" name="curUser" value="<%=request.getRemoteUser()%>">
    <input type="hidden" name="notices" value="<%=notice.getId()%>"/>
    <input type="hidden" name="redirect" value="<%= request.getServletPath() + "?" + request.getQueryString()%>" />
    <input type="submit" value="Acknowledge" />
  </form>
<% } %>
      
<jsp:include page="/includes/footer.jsp" flush="false" />

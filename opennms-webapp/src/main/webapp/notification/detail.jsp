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
	import="org.opennms.core.utils.WebSecurityUtils,
		org.opennms.web.notification.*,
		org.opennms.web.element.*,
		org.opennms.web.event.*,
		org.springframework.web.context.WebApplicationContext,
		org.springframework.web.context.support.WebApplicationContextUtils
	"
%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/taglib.tld" prefix="onms" %>

<%
    WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(application);
    WebNotificationRepository repository = context.getBean(WebNotificationRepository.class);

    String noticeIdString = request.getParameter("notice");

    String eventSeverity = null;
    String eventLocation = null;
    
    int noticeID = -1;
    
    try {
        noticeID = WebSecurityUtils.safeParseInt( noticeIdString );
    }
    catch( NumberFormatException e ) {
        throw new NoticeIdNotFoundException("The notice ID must be an integer.",
					     WebSecurityUtils.sanitizeString(noticeIdString) );
    }
    
    Notification notice = repository.getNotification(noticeID);
    
    if( notice == null ) {
        throw new NoticeIdNotFoundException("A notice with this ID was not found.", String.valueOf(noticeID));
    }

    if (NoticeFactory.canDisplayEvent(notice.getEventId())) {
		Event event = EventFactory.getEvent(notice.getEventId());
		eventSeverity = event.getSeverity().getLabel();
		eventLocation = event.getLocation();
    } else {
		eventSeverity = new String("Cleared");
    }

	String nodeLabel = NetworkElementFactory.getInstance(getServletContext()).getNodeLabel(notice.getNodeId());
	String nodeLocation = NetworkElementFactory.getInstance(getServletContext()).getNodeLocation(notice.getNodeId());
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false" >
  <jsp:param name="title" value="Notice Detail" />
  <jsp:param name="headTitle" value="Notice Detail" />
  <jsp:param name="breadcrumb" value="<a href='notification/index.jsp'>Notifications</a>" />
  <jsp:param name="breadcrumb" value='<%="Notice " + notice.getId()%>' />
</jsp:include>

<div class="card">
  <div class="card-header">
<span>Notice <%=notice.getId()%>
  <% if ( NoticeFactory.canDisplayEvent(notice.getEventId()) ) { %>
    from <a href="event/detail.jsp?id=<%=notice.getEventId()%>">Event <%=notice.getEventId()%></a>
  <% } %>
</span>
  </div>

  <table class="table table-sm severity">
  <tr class="d-flex severity-<%=eventSeverity.toLowerCase()%>">
    <th class="col-md-1">Notification&nbsp;Time</th>
    <td class="col-md-2"><onms:datetime date="<%=notice.getTimeSent()%>" /></td>
    <th class="col-md-1">Time&nbsp;Replied</th>
    <td class="col-md-2">
      <c:choose>
        <c:when test="<%=notice.getTimeReplied() != null%>">
          <onms:datetime date="<%=notice.getTimeReplied()%>"/>
        </c:when>
        <c:otherwise>
          &nbsp;
        </c:otherwise>
      </c:choose>
    </td>
    <th class="col-md-1">Responder</th>
    <td class="col-md-2"><%=notice.getResponder()!=null ? notice.getResponder() : "&nbsp;"%></td>
    <th class="col-md-1">Location</th>
    <td class="col-md-2">
      <c:choose>
        <c:when test="<%= eventLocation != null %>">
          <a href="event/detail.jsp?id=<%=notice.getEventId()%>"><%= eventLocation %></a>
        </c:when>
        <c:otherwise>
          &nbsp;
        </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr class="d-flex severity-<%=eventSeverity.toLowerCase()%>">
    <th class="col-md-1">Node</th>
    <td class="col-md-2">
      <%if (nodeLabel!=null) { %>
        <c:url var="nodeLink" value="element/node.jsp">
          <c:param name="node" value="<%=String.valueOf(notice.getNodeId())%>"/>
        </c:url>
        <a href="${nodeLink}"><c:out value="<%=nodeLabel%>"/></a>
      <% } else { %>
        &nbsp;
      <% } %>
    </td>

    <th class="col-md-1">Interface</th>
    <td class="col-md-2">
      <%if (nodeLabel!=null && notice.getIpAddress()!=null) { %>
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
          
    <th class="col-md-1">Service</th>
    <td class="col-md-2">
      <%if (nodeLabel!=null && notice.getIpAddress()!=null && notice.getServiceName()!=null) { %>
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

    <th class="col-md-1">Node&nbsp;Location</th>
    <td class="col-md-2">
      <c:choose>
        <c:when test="<%= nodeLocation != null %>">
          <c:url var="nodeLocationLink" value="element/node.jsp">
            <c:param name="node" value="<%=String.valueOf(notice.getNodeId())%>"/>
          </c:url>
          <a href="${nodeLink}"><c:out value="<%=nodeLocation%>"/></a>
        </c:when>
        <c:otherwise>
          &nbsp;
        </c:otherwise>
      </c:choose>
    </td>
  </tr>

  <%if (nodeLabel!=null) { %>
    <tr class="severity-<%=eventSeverity.toLowerCase()%>">
      <td colspan="8">
        <c:url var="outageLink" value="outage/list.htm">
          <c:param name="filter" value='<%="node=" + notice.getNodeId()%>'/>
        </c:url>
        <a href="${outageLink}">Current outages for <c:out value="<%=nodeLabel%>"/></a>
      </td>
    </tr>
  <% } %>
</table>
</div>

<% if (notice.getNumericMessage() != null && !"".equals(notice.getNumericMessage().trim())) { %>
<div class="card">
  <div class="card-header">
    <span>Numeric Message</span>
  </div>
  <div class="card-body">
    <%=notice.getNumericMessage()%>
  </div>
</div>
<% } %>

<% if (notice.getTextMessage() != null && !"".equals(notice.getTextMessage().trim())) { %>
<div class="card">
  <div class="card-header">
    <span>Text Message</span>
  </div>
  <div class="card-body">
    <pre><%=notice.getTextMessage()%></pre>
  </div>
</div>
<% } %>

<div class="card">
  <div class="card-header">
    <span>Users Notified</span>
  </div>
  <table class="table table-sm severity">
    <tr>
      <th>Sent To</th>
      <th>Sent At</th>
      <th>Media</th>
      <th>Contact Info</th>
    </tr>
  
  <%  for (NoticeSentTo sentTo : notice.getSentTo()) { %>

    <tr class="severity-<%=eventSeverity.toLowerCase()%>">
      <td><%=sentTo.getUserId()%></td>

      <td>
        <c:choose>
          <c:when test="<%=sentTo.getTime() != null && sentTo.getTime().getTime() > 0%>">
            <onms:datetime date="<%=sentTo.getTime()%>" />
          </c:when>
          <c:otherwise>
            &nbsp;
          </c:otherwise>
        </c:choose>
      </td>

      <td>
        <% if (sentTo.getMedia()!=null && !"".equals(sentTo.getMedia().trim())) { %>
          <%=sentTo.getMedia()%>
        <% } else { %>
          &nbsp;
        <% } %>
      </td>

      <td>
        <% if (sentTo.getContactInfo()!=null && !"".equals(sentTo.getContactInfo().trim())) { %>
          <%=sentTo.getContactInfo()%>
        <% } else { %>
          &nbsp;
        <% } %>
      </td>
    </tr>
  <% } %>
  </table>
</div>

<% if (notice.getTimeReplied() == null) { %>
  <form class="mb-3" method="post" name="acknowledge" action="notification/acknowledge">
    <input type="hidden" name="curUser" value="<%=request.getRemoteUser()%>">
    <input type="hidden" name="notices" value="<%=notice.getId()%>"/>
    <input type="hidden" name="redirect" value="<%= request.getServletPath() + "?" + request.getQueryString()%>" />
    <input type="submit" class="btn btn-secondary" value="Acknowledge" />
  </form>
<% } %>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

<%@page import="
	org.opennms.web.navigate.*,
	org.opennms.web.api.Authentication,
	org.opennms.web.api.Util,
	org.opennms.netmgt.config.NotifdConfigFactory
" %>

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%!
	public void init() throws ServletException {
		try {
			NotifdConfigFactory.init();
		} catch (final Throwable t) {
			// notice status will be unknown if the factory can't be initialized
		}
	}
%>
<%
	String noticeStatus = "Unknown";
	try {
		noticeStatus = NotifdConfigFactory.getPrettyStatus();
	} catch (final Throwable t) {
	}
	final String baseHref = Util.calculateUrlBase( request );
	final Boolean isAdmin = request.isUserInRole(Authentication.ROLE_ADMIN);
	pageContext.setAttribute("isAdmin", isAdmin);
%>

<c:choose>
  <c:when test="${param.bootstrap == 'true'}">
    <ul class="nav navbar-nav navbar-right">
      <c:choose>
        <c:when test="${empty pageContext.request.remoteUser}">
        </c:when>
        <c:otherwise>
          <c:forEach var="entry" items="${model.entries}">
            <c:if test="${entry.value.display}">
              <c:choose>
                <c:when test="${not empty entry.key.entries}">
                  <!-- has sub-entries, draw menu drop-downs -->
                  <li class="dropdown">
                  <a href="${entry.key.url}" name="nav-${entry.key.name}-top" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">${entry.key.name} <span class="caret"></span></a>
                  <ul class="dropdown-menu" role="menu">
                    <c:forEach var="subEntry" items="${entry.key.entries}">
                      <%
                        NavBarEntry subEntry = (NavBarEntry) pageContext.getAttribute("subEntry");
                        DisplayStatus subEntryDisplayStatus = subEntry.evaluate(request);
                        pageContext.setAttribute("subEntryDisplayStatus", subEntryDisplayStatus);
                      %>
                      <c:if test="${subEntryDisplayStatus.display}">
                        <li>
                        <c:choose>
                          <c:when test="${not empty subEntry.url}">
                            <a name="nav-${entry.key.name}-${subEntry.name}" href="${subEntry.url}">${subEntry.name}</a>
                          </c:when>
                          <c:otherwise>
                            <a name="nav-${entry.key.name}-${subEntry.name}" href="#">${subEntry.name}</a>
                          </c:otherwise>
                        </c:choose>
                        </li>
                      </c:if>
                    </c:forEach>
                  </ul>
                </c:when>
                <c:otherwise>
                  <li>
                    <c:choose>
                      <c:when test="${not empty entry.key.url}">
                        <a name="nav-${entry.key.name}-top" href="${entry.key.url}">${entry.key.name}</a>
                      </c:when>
                      <c:otherwise>
                        <a name="nav-${entry.key.name}-top" href="#">${entry.key.name}</a>
                      </c:otherwise>
                    </c:choose>
                  </li>
                </c:otherwise>
              </c:choose> <!-- has/doesn't have entries -->
            </c:if> <!-- display -->
          </c:forEach>
          <li class="dropdown">
            <a name="nav-admin-top" href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">
              <c:choose>
                <c:when test="${not empty pageContext.request.remoteUser}">
                  <span class="glyphicon glyphicon-user"></span>
                  ${pageContext.request.remoteUser}
                </c:when>
                <c:otherwise>
                  &hellip;
                </c:otherwise>
              </c:choose>
              <span class="caret"></span>
            </a>
            <ul class="dropdown-menu" role="menu">
              <li><a name="nav-admin-notice-status" href="#" style="white-space: nowrap">Notices: <b id="notification<%= noticeStatus %>"><%= noticeStatus %></b></a></li>
              <c:if test="${isAdmin}">
                <li><a name="nav-admin-admin" href="<%= baseHref %>admin/index.jsp" style="white-space: nowrap">Configure OpenNMS</a></li>
                <li><a name="nav-admin-quick-add" href="<%= baseHref %>admin/node/add.htm" style="white-space: nowrap">Quick-Add Node</a></li>
              </c:if>
              <li><a name="nav-admin-support" href="<%= baseHref %>support/index.htm">Help/Support</a></li>
              <c:if test="${not empty pageContext.request.remoteUser}">
                <li><a name="nav-admin-logout" href="<%= baseHref %>j_spring_security_logout" style="white-space: nowrap">Log Out</a></li>
              </c:if>
            </ul>
          </li>
        </c:otherwise>
      </c:choose>
    </ul>
  </c:when>
  <c:otherwise>
    <div class="navbar">
      <ul>
        <c:forEach var="entry" items="${model.entries}">
          <c:if test="${entry.value.display}">
            <li>
              <c:choose>
                <c:when test="${entry.value.displayLink}">
                  <a href="${entry.key.url}">${entry.key.displayString}</a>
                </c:when>
                <c:otherwise>
                  ${entry.key.displayString}
                </c:otherwise>
              </c:choose>
            </li>
          </c:if>
        </c:forEach>
        <c:if test="${isAdmin}">
          <li><a href="<%= baseHref %>admin/index.jsp">Admin</a></li>
        </c:if>
      </ul>
    </div>
  </c:otherwise>
</c:choose>
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

<%@page import="org.opennms.web.navigate.*" %>

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:choose>
  <c:when test="${param.bootstrap == 'true'}">
    <ul class="nav navbar-nav navbar-right">
      <c:forEach var="entry" items="${model.entries}">
        <!-- entry ${entry.key.name}, hasEntries=${not empty entry.key.entries}, display=${entry.value.display} -->
        <c:if test="${entry.value.display}">
          <c:choose>
            <c:when test="${not empty entry.key.entries}">
              <!-- has sub-entries, draw menu drop-downs -->
              <li class="dropdown">
              <a href="${entry.key.url}" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">${entry.key.name} <span class="caret"></span></a>
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
                      <c:when test="true">
                        <a href="${subEntry.url}">${subEntry.name}</a>
                      </c:when>
                      <c:otherwise>
                        ${subEntry.name}
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
                  <c:when test="${entry.value.displayLink}">
                    <!-- entry.displayLink -->
                    <a href="${entry.key.url}">${entry.key.name}</a>
                  </c:when>
                  <c:otherwise>
                    <!-- entry.key.name -->
                    ${entry.key.name}
                  </c:otherwise>
                </c:choose>
              </li>
            </c:otherwise>
          </c:choose> <!-- has/doesn't have entries -->
        </c:if> <!-- display -->
      </c:forEach>
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
      </ul>
    </div>
  </c:otherwise>
</c:choose>
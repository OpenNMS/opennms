<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
    <jsp:param name="title" value="Maps" />
    <jsp:param name="breadcrumb" value="Maps" />
</jsp:include>

<div class="panel panel-default">
    <div class="panel-heading">
        <h3 class="panel-title">Maps</h3>
    </div>

    <div class="panel-body">
      <ul class="list-unstyled">
        <c:forEach var="entry" items="${entries.entries}">
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
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

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

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Distributed Status Details" />
	<jsp:param name="headTitle" value="Distributed Status Details" />
	<jsp:param name="breadcrumb" value="<a href='distributedStatusSummary.htm'>Distributed Status</a>" />
	<jsp:param name="breadcrumb" value="Details" />
</jsp:include>

<c:choose>
  <c:when test="${webTable.errors.errorCount > 0}">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title"><spring:message code="error"/></h3>
      </div>
      <div class="panel-body">
        <ul class="error">
          <c:forEach var="err" items="${webTable.errors.allErrors}">
            <li><spring:message code="${err.code}" arguments="${err.arguments}"/></li>
          </c:forEach>
        </ul>
      </div>
    </div>
  </c:when>
  
  <c:otherwise>
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">${webTable.title}</h3>
      </div>
      <table class="table table-condensed table-bordered severity">
        <tr>
          <c:forEach items="${webTable.columnHeaders}" var="headerCell">
            <th class="${headerCell.styleClass}">
              <c:choose>
                <c:when test="${! empty headerCell.link}">
                  <a href="${headerCell.link}">${headerCell.content}</a>
                </c:when>
                <c:otherwise>
                  ${headerCell.content}
                </c:otherwise>
              </c:choose>
            </th>
          </c:forEach>
        </tr>
        <c:forEach items="${webTable.rows}" var="row">
          <tr class="severity-${row[0].styleClass}">
            <c:forEach items="${row}" var="cell">
              <td class="${cell.styleClass} divider">
                <c:choose>
                  <c:when test="${! empty cell.link}">
                    <a href="${cell.link}">${cell.content}</a>
                  </c:when>
                  <c:otherwise>
                    ${cell.content}
                  </c:otherwise>
                </c:choose>
              </td>
            </c:forEach>
          </tr>
        </c:forEach>
      </table>
    </div>
  </c:otherwise>
</c:choose>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

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

<jsp:include page="/includes/bootstrap.jsp" flush="false">
  <jsp:param name="title" value="Distributed Status Summary" />
  <jsp:param name="headTitle" value="Summary" />
  <jsp:param name="breadcrumb" value="Distributed Status" />
</jsp:include>

<jsp:include page="/includes/distStatusLegend.jsp" flush="false">

  <jsp:param name="normalCaption" value="A Green status Cell (Application Up) indicates that *all* of the Application's services 
    are available from at least 1 Started remote poller in that Location." />

  <jsp:param name="indetermCaption" value="A Golden-brown colored cell (Indeterminate (no current data)) indicates that there is no
    current data which means there are no Started remote pollers.  If the percentage in this colored cell is > 0, then this means 
    there has been data reported since midnight but there is just no current data being reported." />
    
  <jsp:param name="warnCaption" value="A Yellow status cell (Application Impaired) indicates that 1 or more of the Applications 
    set of IP services are currently reported as unavailable from 1, but not all, of the remote pollers in that location." />
   
  <jsp:param name="criticalCaption" value="A Red status cell (Application Down) indicates that at least 1 of the Application's services
    are currently unavailable from from *all* Started remote pollers in that location." />

</jsp:include>

<div class="panel panel-default">
  <div class="panel-heading">
    <h3 class="panel-title"><c:out value="${webTable.title}" /></h3>
  </div>
  <table class="table table-condensed table-bordered severity">
    <tr>
      <c:forEach items="${webTable.columnHeaders}" var="headerCell">
        <th class="<c:out value='${headerCell.styleClass}'/>">
          <c:choose>
            <c:when test="${! empty headerCell.link}">
              <a href="<c:out value='${headerCell.link}'/>"><c:out value="${headerCell.content}"/></a>
            </c:when>
            <c:otherwise>
              <c:out value="${headerCell.content}"/>
            </c:otherwise>
          </c:choose>
        </th>
      </c:forEach>
    </tr>
    <c:forEach items="${webTable.rows}" var="row">
      <tr class="CellStatus">
        <c:forEach items="${row}" var="cell">
          <td class="severity-<c:out value='${cell.styleClass}'/> bright divider">
            <c:choose>
              <c:when test="${! empty cell.link}">
                <a href="<c:out value='${cell.link}'/>"><c:out value="${cell.content}"/></a>
              </c:when>
              <c:otherwise>
                <c:out value="${cell.content}"/>
              </c:otherwise>
            </c:choose>
          </td>
        </c:forEach>
      </tr>
    </c:forEach>
  </table>
</div>

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>

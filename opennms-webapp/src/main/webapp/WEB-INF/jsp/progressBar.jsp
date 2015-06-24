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
<%

/*
 * Hopefully our servlet container will not have flushed the few newlines of
 * output from above before we set the headers.
 */

long startTime = System.currentTimeMillis();
response.setHeader("Refresh", "2");
response.setHeader("Cache-Control", "no-store, private");
response.setDateHeader("Date", startTime);
response.setDateHeader("Expires", startTime);

%>
 
<jsp:include page="/includes/bootstrap.jsp" flush="false">
	<jsp:param name="title" value="Progress" />
	<jsp:param name="headTitle" value="Progress" />
</jsp:include>

<c:set var="label" value="${progress.phaseLabel}"/>
<c:set var="percentage">
	<fmt:formatNumber maxFractionDigits="0" value="${progress.phase / progress.phaseCount * 100}"/>
</c:set>

<div class="row">
  <div class="col-md-4 col-md-offset-4 text-center">
    <p style="margin-bottom: 0px; font-size: 80%;">
      ${label}...
    </p>

    <div class="center-block" style="width: 75%; height: 25px; border-width: 1px; border-style: ridge; background-color: white;">
      <div style="width: ${percentage}%; height: 24px; background-color: green;">&nbsp;</div>
    </div>

    <p>
      ${percentage}% completed
    </p>
  </div> <!-- column -->
</div> <!-- row -->

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" />

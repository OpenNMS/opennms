<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Database Reports" />
  <jsp:param name="headTitle" value="Database Reports" />
  <jsp:param name="breadcrumb" value="<a href='report/index.jsp'>Reports</a>" />
  <jsp:param name="breadcrumb" 
		value="<a href='report/database/index.htm'>Database</a>" />
  <jsp:param name="breadcrumb" value="Run"/>
</jsp:include>

<h3>Cron Expression</h3>
<div class="boxWrapper">
    <p>We use Quartz Scheduler, for information on cron syntax used in Quartz Scheduler there is a great tutorial <a href="http://www.quartz-scheduler.org/docs/tutorials/crontrigger.html" target="_blank">here</a></p>
    <form:form commandName="triggerDetails" cssClass="stdform">

    <p>
		<form:label path="cronExpression" cssClass="label">
			<c:out value="cron expression"/>
		</form:label>
		<form:input path="cronExpression"/>

    </p>
    <span class="indent">
        <input type="submit" id="proceed" name="_eventId_proceed" value="Proceed" />&#160;
        <input type="submit" name="_eventId_revise" value="Revise"/>&#160;
        <input type="submit" name="_eventId_cancel" value="Cancel"/>&#160;
    </span>
    </form:form>
</div>
  
<jsp:include page="/includes/footer.jsp" flush="false" />
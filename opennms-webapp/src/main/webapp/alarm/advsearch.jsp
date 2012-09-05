<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Advanced Alarm Search" />
  <jsp:param name="headTitle" value="Advanced Search" />
  <jsp:param name="headTitle" value="Alarms" />
  <jsp:param name="breadcrumb" value="<a href='alarm/index.jsp'>Alarms</a>" />
  <jsp:param name="breadcrumb" value="Advanced Alarm Search" />
</jsp:include>

  <div id="contentleft">
      <h3>Advanced Alarm Search</h3>

      <jsp:include page="/includes/alarm-advquerypanel.jsp" flush="false" />
  </div> <!-- id="contentleft" -->

  <div id="contentright">
      <h3>Searching Instructions</h3>

      <p>The <strong>Advanced Alarm Search</strong> page can be used to search the alarm list on
      multiple fields. Fill in values for each field that you wish to use to narrow down
      the search.</p>

      <p>To select alarms by time, first check the box for the time range
      that you wish to limit and then fill out the time in the boxes provided.</p>

      <p>If you wish to select alarms within a specific time span, check <em>both</em>
      boxes and enter the beginning and end of the range in the boxes provided.</p>
  </div> <!-- id="contentright" -->

<jsp:include page="/includes/footer.jsp" flush="false" />

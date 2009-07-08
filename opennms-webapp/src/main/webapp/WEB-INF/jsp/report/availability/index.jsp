<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


--%>

<%@page language="java"
	contentType="text/html"
	session="true"
%>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Availability Reports" />
  <jsp:param name="headTitle" value="Availability Reports" />
	<jsp:param name="breadcrumb"
		value="<a href='report/index.jsp'>Reports</a>" />
	<jsp:param name="breadcrumb" value="Availability" />
</jsp:include>

  <div class="TwoColLeft">
    <h3>Availability Reports</h3>
    <div class="boxWrapper">
      <ul class="plain">
        <li><a href="report/availability/report.htm">Run availability report</a></li>
        <li><a href="report/availability/manage.htm">Manage and view availability reports</a></li>
      </ul>
    </div>
  </div>

  <div class="TwoColRight">
    <h3>Descriptions</h3>
    <div class="boxWrapper">
      <p><b>Availability Reports</b> provide graphical or numeric
          view of your service level metrics for the current
          month-to-date, previous month, and last twelve months by categories.
      </p>
      
      <p>You may run availability reports for any of the report categories defined.
      You can have these reports mailed to you when they have been run. You can also
      chose to save these reports on the OpenNMS server so that they may be viewed later
      </p>
            
      <p>You can view saved reports as HTML, PDF or PDF with embedded SVG graphics. You
      may also delete saved reports that are no longer needed.
      </p>
    </div>
  </div>
  <hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>
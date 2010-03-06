<%--

/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Nov 12: Created jonathan@opennms.org
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
%>

<jsp:include page="/includes/header.jsp" flush="false">
  <jsp:param name="title" value="Database Reports" />
  <jsp:param name="headTitle" value="Database Reports" />
	<jsp:param name="breadcrumb"
		value="<a href='report/index.jsp'>Reports</a>" />
	<jsp:param name="breadcrumb" value="Database" />
</jsp:include>

  <div class="TwoColLeft">
    <h3>Database Reports</h3>
    <div class="boxWrapper">
      <ul class="plain">
        <li><a href="report/database/batchList.htm">Batch reports</a></li>
        <li><a href="report/database/onlineList.htm">Online reports</a></li>
        <li><a href="report/database/manage.htm">View and manage pre-run reports</a></li>
        <li><a href="report/database/manageSchedule.htm">Manage the batch report schedule</a></li>
      </ul>
    </div>
  </div>

  <div class="TwoColRight">
    <h3>Descriptions</h3>
    <div class="boxWrapper">
      <p>These reports provide graphical or numeric
          view of your service level metrics for the current
          month-to-date, previous month, and last twelve months by categories.
      </p>
      
      <p>You may run or schedule a report for any of the report categories defined.
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
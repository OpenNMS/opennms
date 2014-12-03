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

<%@page language="java"
	contentType="text/html"
	session="true"
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
  <jsp:param name="title" value="Database Reports" />
  <jsp:param name="headTitle" value="Database Reports" />
	<jsp:param name="breadcrumb"
		value="<a href='report/index.jsp'>Reports</a>" />
	<jsp:param name="breadcrumb" value="Database" />
</jsp:include>

  <div class="row">
    <div class="col-md-5">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">Database Reports</h3>
            </div>
            <div class="panel-body">
                <ul class="list-unstyled">
                    <li><a href="report/database/reportList.htm">List reports</a></li>
                    <li><a href="report/database/manage.htm">View and manage pre-run reports</a></li>
                    <li><a href="report/database/manageSchedule.htm">Manage the batch report schedule</a></li>
                </ul>
            </div>
        </div>


    </div>

  <div class="col-md-7">
      <div class="panel panel-default">
          <div class="panel-heading">
            <h3 class="panel-title">Descriptions</h3>
          </div>
          <div class="panel-body">
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

  </div>
  </div>
  <hr />
<jsp:include page="/includes/bootstrap-footer.jsp" flush="false"/>
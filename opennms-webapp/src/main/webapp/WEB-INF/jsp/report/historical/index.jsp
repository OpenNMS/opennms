<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
        <li><a href="report/historical/alarmReportList.htm">Alarm reports</a></li>
        <li><a href="report/historical/eventReportList.htm">Event reports</a></li>
      </ul>
    </div>
  </div>

  <div class="TwoColRight">
    <h3>Descriptions</h3>
    <div class="boxWrapper">
      <p>These reports provide numeric view of historic events and alarms available
	 in the server. 
      </p>
      
      <p>You can chose to save these reports on the local machine so that they may be viewed later
      </p>
            
      <p>You can download saved reports in different format such as HTML, PDF, XLS or CVS.
      </p>
    </div>
  </div>
  <hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>

<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Aug 30: Use new alarm list URLs. - dj@opennms.org
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Feb 04: Added a check to pralarm null entries in queries. Bug #536.
// 
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Alarms" />
  <jsp:param name="headTitle" value="Alarms" />
  <jsp:param name="location" value="alarm" />  
  <jsp:param name="breadcrumb" value="Alarms" />
</jsp:include>

  <div class="TwoColLeft">
      <h3>Alarm Queries</h3>
      <div class="boxWrapper">
       <%-- <jsp:include page="/includes/alarm-querypanel.jsp" flush="false" />--%>
        <form action="alarm/detail.jsp" method="GET">
          <p align="right">Alarm ID:          
            <input type="TEXT" NAME="id" />
            <input type="submit" value="Get details"/></p>                
        </form>
        <ul class="plain">
          <li><a href="alarm/list.htm" title="Summary view of all outstanding alarms">All alarms (summary)</a></li>
          <li><a href="alarm/list.htm?display=long" title="Detailed view of all outstanding alarms">All alarms (detail)</a></li>
          <li><a href="alarm/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a></li>
        </ul>  
      </div>
  </div>

  <div class="TwoColRight">
    <h3>Outstanding and acknowledged alarms</h3>
    <div class="boxWrapper">
      <p>Alarms can be <em>acknowledged</em>, or removed from the default view of all users, by
        selecting the alarms' <em>Ack</em> check box and clicking the <em>Acknowledge Selected
        Alarms</em> at the bottom of the page.  Acknowledging an alarm gives
        users the ability to take personal responsibility for addressing a network
        or systems-related issue.  Any alarm that has not been acknowledged is
        active in the default alarms view of all users' browsers and is considered outstanding.
      </p>
            
      <p>To view acknowledged alarms, go to the <em>All Alarms</em> (<em>summary</em> or <em>details</em>) list and
         click the minus sign next to the <em>alarm is outstanding</em> search constraint.
      </p>

      <p>If an alarm has been acknowledged in error, find the alarm and <em>unacknowledge</em>
         it, making it available again to all users' default alarm views.
      </p>
        
      <p>If you have a specific alarm identifier for which you want a detailed alarm
        description, type the identifier into the <em>Get details for Alarm ID</em> box and
        hit <b>[Enter]</b>.  You will then go to the appropriate details page.
      </p>
    </div>
  </div>
  <hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>

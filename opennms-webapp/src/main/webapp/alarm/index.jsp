<%--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2008 The OpenNMS Group, Inc.  All rights reserved.
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
      <p>Alarms can be <em>acknowledged</em>, or removed from the view of other users, by
        selecting the alarm in the <em>Ack</em> check box and clicking the <em>Acknowledge
        Selected Alarms</em> at the bottom of the page.  Acknowledging an alarm gives
        users the ability to take personal responsibility for addressing a network
        or systems-related issue.  Any alarm that has not been acknowledged is
        active in all users' browsers and is considered <em>outstanding</em>.
      </p>
            
      <p>If an alarm has been acknowledged in error, you can select the 
        <em>View all acknowledged alarms</em> link, find the alarm, and <em>unacknowledge</em> it,
        making it available again to all users' views.
      </p>
        
      <p>If you have a specific alarm identifier for which you want a detailed alarm
        description, type the identifier into the <em>Get details for Alarm ID</em> box and
        hit <b>[Enter]</b>.  You will then go to the appropriate details page.
      </p>
    </div>
  </div>
  <hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>

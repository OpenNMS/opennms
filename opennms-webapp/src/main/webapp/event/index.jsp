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
// 2003 Feb 07: Fixed URLEncoder issues.
// 2003 Feb 04: Added a check to prevent null entries in queries. Bug #536.
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
  <jsp:param name="title" value="Events" />
  <jsp:param name="headTitle" value="Events" />
  <jsp:param name="location" value="event" />  
  <jsp:param name="breadcrumb" value="Events" />
</jsp:include>

  <div class="TwoColLeft">
      <h3>Event Queries</h3>
      <div class="boxWrapper">
      <%--<jsp:include page="/includes/event-querypanel.jsp" flush="false" />--%>
			<form action="event/detail.jsp" method="get">
				<p align="right">Event ID:          
					<input type="text" name="id" />
					<input type="submit" value="Get details"/></p>               
			</form>
			
			<ul class="plain">
				<li><a href="event/list" title="View all outstanding events">All events</a></li>
				<li><a href="event/advsearch.jsp" title="More advanced searching and sorting options">Advanced Search</a></li>
			</ul>
		</div>
  </div>
      
  <div class="TwoColRight">
      <h3>Outstanding and acknowledged events</h3>
		<div class="boxWrapper">
      <p>Events can be <em>acknowledged</em>, or removed from the view of other users, by
        selecting the event in the <em>Ack</em> check box and clicking the <em>Acknowledge
        Selected Events</em> at the bottom of the page.  Acknowledging an event gives
        users the ability to take personal responsibility for addressing a network
        or systems-related issue.  Any event that has not been acknowledged is
        active in all users' browsers and is considered <em>outstanding</em>.
      </p>
            
      <p>If an event has been acknowledged in error, you can select the 
        <em>View all acknowledged events</em> link, find the event, and <em>unacknowledge</em> it,
        making it available again to all users' views.
      </p>
        
      <p>If you have a specific event identifier for which you want a detailed event
        description, type the identifier into the <em>Get details for Event ID</em> box and
        hit <b>[Enter]</b>.  You will then go to the appropriate details page.
      </p>
		</div>
  </div>
	<hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>

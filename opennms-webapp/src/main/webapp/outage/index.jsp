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
// 2007 Dec 08: Make the value for the location parameter for the navBar include singular
//              ("outage") to be like other pages. - dj@opennms.org
// 2003 Apr 07: Corrected small typo.
// 2003 Feb 07: Fixed URLEncoder issues.
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
//      http://www.opennms.com/
//

--%>

<%@page language="java"
	contentType="text/html"
	session="true"
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Outages" />
  <jsp:param name="headTitle" value="Outages" />
  <jsp:param name="location" value="outage" />
  <jsp:param name="breadcrumb" value="Outages" />  
</jsp:include>

  <div class="TwoColLeft">
      <h3>Outage Menu</h3>    
		<div class="boxWrapper">
        <form method="GET" action="outage/detail.jsp" >
          <p align="right">Outage ID:
				<input type="text" name="id" />
				<input type="submit" value="Get details" /></p>
        </form>
			<ul class="plain">
				<li><a href="outage/list.htm?outtype=current">Current outages</a></li>
				<li><a href="outage/list.htm">All outages</a></li>
			</ul>
      </div>
  </div>
  <div class="TwoColRight">
      <h3>Outages and Service Level Availability</h3>
		<div class="boxWrapper">
			<p>Outages are tracked by OpenNMS by polling services that have been
			discovered.  If the service does not respond to the poll, a service outage
			is created and service availability levels are impacted.  Service 
			outages create notifications.</p>
		</div>
  </div>
  <hr />                                   
<jsp:include page="/includes/footer.jsp" flush="false" />

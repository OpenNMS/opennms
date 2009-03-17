<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Mar 17: Added more URLs
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
//      http://www.opennms.com///

--%>

<%@page language="java"
	contentType="text/html"
session="true"
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Help" />
  <jsp:param name="headTitle" value="Help" />
  <jsp:param name="location" value="help" />
  <jsp:param name="breadcrumb" value="Help" />
</jsp:include>

<div class="TwoColLeft">
  <h3>Getting Help</h3>
  <div class="boxWrapper">
    <ul class="plain">
      <li><a href="http://support.opennms.com">Log In to Commercial Support</a></li>
      <li><a href="http://bugzilla.opennms.org">Report a Bug</a></li>
      <li><a href="irc://irc.freenode.net/%23opennms">Chat with Developers on IRC</a></li>
    </ul>
  </div>
  <hr />
  <h3>Documentation</h3>
  <div class="boxWrapper">
    <ul class="plain">
      <li><a href="http://faq.opennms.org">Frequently Asked Questions</a></li>
      <li><a href="http://www.opennms.org">Online Documentation</a></li>
    </ul>
  </div>
  <hr />
  <h3>Local Resources</h3>
  <div class="boxWrapper">
    <ul class="plain">
      <li><a href="help/about.jsp">About the OpenNMS Web Console</a></li>
    </ul>
  </div>
  <hr />
</div>
<div class="TwoColRight">
  <h3>Helpful Resources</h3>
  <div class="boxWrapper">
    <p>
      <em>Local Resources</em> contain help and are located within your own OpenNMS system.
      <em>Documentation</em> are external web pages (exits from your OpenNMS Web Console)
      that have information relevant to your OpenNMS system.
    </p>
    <hr />
    <p>
      Browse the <em>Frequently Asked Questions</em> to find
      answers to common questions or read up on network management the OpenNMS way in the 
      <em>Online Documentation</em>.  Check out important attributes of your OpenNMS system
      on the <em>About page</em>.
    </p>
  </div>
</div>
<hr />
<jsp:include page="/includes/footer.jsp" flush="false"/>
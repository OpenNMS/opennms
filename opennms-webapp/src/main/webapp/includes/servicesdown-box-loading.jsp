<%--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Apr 05: Create from servicesdown-box.jsp  - jeffg@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

<%-- 
  This page is included by other JSPs to create a box containing an
  asynchronously loaded abbreviated list of outages.
  
  It expects that a <base> tag has been set in the including page
  that directs all URLs to be relative to the servlet context.
--%>

<%@page language="java"
        contentType="text/html"
        session="true"
        import="org.opennms.web.outage.*" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- includes/servicesdown-box-loading.jsp -->
<script type="text/javascript">
function onOutagesIFrameLoad() {
  var outagesDivContent = document.getElementById("outagesIFrame").contentWindow.document.body.innerHTML;
  document.getElementById("outagesDiv").innerHTML = outagesDivContent;
}
</script>

<iframe id="outagesIFrame" style="display: none; visibility: hidden; height: 1px;" onload="onOutagesIFrameLoad();"></iframe>

<div id="outagesDiv">
  <c:url var="headingLink" value="outage/current.jsp"/>
  <h3><a href="${headingLink}">Nodes with Outages</a>&nbsp;<img src="images/progress.gif" width="16" height="16" alt="(Loading...)" /></h3>
  <div class="boxWrapper">
    &nbsp;
  </div>
</div>

<script type="text/javascript">
document.getElementById("outagesIFrame").src = "includes/servicesdown-box.jsp";
</script>

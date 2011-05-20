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
// 2002 Nov 26: Fixed breadcrumbs issue.
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
	import="org.opennms.web.MissingParameterException"
%>

<%
    String nodeId = request.getParameter("node");
    if (nodeId == null) {
        throw new MissingParameterException("node");
    }
    String foreignSource = request.getParameter("foreignSource");
    if (foreignSource == null) {
        throw new MissingParameterException("foreignSource");
    }
%>

<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Node Label Provisioned" />
  <jsp:param name="headTitle" value="Node Label Provisioned" />
  <jsp:param name="breadcrumb" value="<a href='admin/index.jsp'>Admin</a>" />
  <jsp:param name="breadcrumb" value="Node Label Provisioned" />
  <jsp:param name="script" value="<script type='text/javascript' src='js/provisioningGroups.js'></script>" />
</jsp:include>
<form action="admin/provisioningGroups.htm" name="takeAction" method="post">
  <input type="hidden" name="groupName" size="20"/>
  <input type="hidden" name="action" value="addGroup" />
  <input type="hidden" name="actionTarget" value="" />
</form>
<h3>Node Label Provisioned</h3>
<br/>
<p>
  This node was created as part of a requisition via the provisioning service.
  The requested change to the node's label has been made and will be reflected immediately in the requisition, but may not take effect in the database until the next time the node or requisition is rescanned.
</p>
<p>
  If you want to re-import (or synchronize) the <strong><%=foreignSource%></strong> requisition, click on the "Synchronize" button. That will redirect you to the Provisioning Groups page, after request the requisition import.
</p>
<p>
  <br/>
  <input type="button" value="Synchronize" onclick="doAction('<%=foreignSource%>', 'import')" />
  <input type="button" value="Return to Node Page" onclick="location='element/node.jsp?node=<%=nodeId%>'" />
</p>

<jsp:include page="/includes/footer.jsp" flush="false" />

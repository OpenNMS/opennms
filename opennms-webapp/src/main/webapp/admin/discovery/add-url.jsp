<%

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
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

%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.netmgt.config.discovery.*, org.opennms.web.admin.discovery.ActionDiscoveryServlet" %>
<% 
	response.setDateHeader("Expires", 0);
	response.setHeader("Pragma", "no-cache");
	if (request.getProtocol().equals("HTTP/1.1")) {
		response.setHeader("Cache-Control", "no-cache");
	}
%>

<%
HttpSession sess = request.getSession(false);
DiscoveryConfiguration currConfig  = (DiscoveryConfiguration) sess.getAttribute("discoveryConfiguration");
%>

<html>
<head>
  <title>Add Include URL | Admin | OpenNMS Web Console</title>
  <base href="<%=org.opennms.web.api.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="css/styles.css" />
</head>

<body>
<script type="text/javascript">
function addIncludeUrl() {
	if(isNaN(document.getElementById("timeout").value)){
		alert("Timeout not valid.");
		document.getElementById("timeout").focus();
		return;		
	}

	if(isNaN(document.getElementById("retries").value)){
		alert("Retries field not valid.");
		document.getElementById("retries").focus();
		return;		
	}	

	opener.document.getElementById("iuurl").value=document.getElementById("url").value;
	opener.document.getElementById("iutimeout").value=document.getElementById("timeout").value;
	opener.document.getElementById("iuretries").value=document.getElementById("retries").value;
	opener.document.getElementById("modifyDiscoveryConfig").action=opener.document.getElementById("modifyDiscoveryConfig").action+"?action=<%=ActionDiscoveryServlet.addIncludeUrlAction%>";
	opener.document.getElementById("modifyDiscoveryConfig").submit();
	window.close();
	opener.document.focus();
}

</script>


<!-- Body -->

    <h3>Add a URL containing a list of IP addresses to include</h3>
										   

<table class="standard">
 <tr>
	  <td class="standard" align="center" width="17%">URL:<input type="text" id="url" name="url" size="30"/></td>
	  <td class="standard" align="center" width="17%">Timeout (msec):<input type="text" id="timeout" name="timeout" size="4" value="<%=currConfig.getTimeout()%>"/></td>
	  <td class="standard" align="center" width="17%">Retries:<input type="text" id="retries" name="retries" size="2" value="<%=currConfig.getRetries()%>"/></td>
 </tr>
</table>

<input type="button" name="addIncludeUrl" id="addIncludeUrl" value="Add" onclick="addIncludeUrl();" />
<input type="button" name="cancel" id="cancel" value="Cancel" onclick="window.close();opener.document.focus();" />

  <hr />

</body>
</html>

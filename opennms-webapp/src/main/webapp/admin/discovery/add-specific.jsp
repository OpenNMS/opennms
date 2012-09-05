<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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
  <title>Add Specific | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.api.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="css/styles.css" />
  <script type='text/javascript' src='js/ipv6/ipv6.js'></script>
  <script type='text/javascript' src='js/ipv6/lib/jsbn.js'></script>
  <script type='text/javascript' src='js/ipv6/lib/jsbn2.js'></script>
  <script type='text/javascript' src='js/ipv6/lib/sprintf.js'></script>
</head>

<body>
<script type="text/javascript">
function addSpecific(){
	if(!isValidIPAddress(document.getElementById("ipaddress").value)){
		alert("IP address not valid.");
		document.getElementById("ipaddress").focus();
		return;
	}
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


		
	opener.document.getElementById("specificipaddress").value=document.getElementById("ipaddress").value;
	opener.document.getElementById("specifictimeout").value=document.getElementById("timeout").value;
	opener.document.getElementById("specificretries").value=document.getElementById("retries").value;
	opener.document.getElementById("modifyDiscoveryConfig").action=opener.document.getElementById("modifyDiscoveryConfig").action+"?action=<%=ActionDiscoveryServlet.addSpecificAction%>";
	opener.document.getElementById("modifyDiscoveryConfig").submit();
	window.close();
	opener.document.focus();
}


</script>


<!-- Body -->

    <h3>Add a specific IP address to discover</h3>
										   

<table class="standard">
 <tr>
	  <td class="standard" align="center" width="17%">IP Address:<input type="text" id="ipaddress" name="ipaddress" size="10"/></td>
	  <td class="standard" align="center" width="17%">Timeout (msec):<input type="text" id="timeout" name="timeout" size="4" value="<%=currConfig.getTimeout()%>"/></td>
	  <td class="standard" align="center" width="17%">Retries:<input type="text" id="retries" name="retries" size="2" value="<%=currConfig.getRetries()%>"/></td>
 </tr>
</table>

<input type="button" name="addSpecific" id="addSpecific" value="Add" onclick="addSpecific();" />
<input type="button" name="cancel" id="cancel" value="Cancel" onclick="window.close();opener.document.focus();" />

  <hr />

</body>
</html>

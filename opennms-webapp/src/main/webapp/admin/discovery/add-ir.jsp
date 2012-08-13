<%

/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

%>

<%@page language="java" contentType="text/html" session="true" import="org.opennms.netmgt.config.discovery.*, org.opennms.web.admin.discovery.ActionDiscoveryServlet,org.opennms.protocols.snmp.SnmpPeer" %>
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
  <title>Add Include Range | Admin | OpenNMS Web Console</title>
  <base href="<%=org.opennms.web.api.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="css/styles.css" />
  <script type='text/javascript' src='js/ipv6/ipv6.js'></script>
  <script type='text/javascript' src='js/ipv6/lib/jsbn.js'></script>
  <script type='text/javascript' src='js/ipv6/lib/jsbn2.js'></script>
  <script type='text/javascript' src='js/ipv6/lib/sprintf.js'></script>

</head>

<body>
<script type="text/javascript">
function v4BigInteger(ip) {
    var a = ip.split('.');
    return parseInt(a[0])*Math.pow(2,24) + parseInt(a[1])*Math.pow(2,16) + parseInt(a[2])*Math.pow(2,8) + parseInt(a[3]);
};

function checkIpRange(ip1, ip2){
    if (verifyIPv4Address(ip1) && verifyIPv4Address(ip2)) {
        var a = v4BigInteger(ip1);
        var b = v4BigInteger(ip2);
        return b >= a;
    }
    if (verifyIPv6Address(ip1) && verifyIPv6Address(ip2)) {
        var a = new v6.Address(ip1).bigInteger();
        var b = new v6.Address(ip2).bigInteger();
        return b.compareTo(a) >= 0;
    }
    return false;
}

function addIncludeRange(){
	if(!isValidIPAddress(document.getElementById("base").value)){
		alert("Network Address not valid.");
		document.getElementById("base").focus();
		return;
	}

	if(!isValidIPAddress(document.getElementById("end").value)){
		alert("End IP not valid.");
		document.getElementById("end").focus();
		return;
	}

	if(!checkIpRange(document.getElementById("base").value, document.getElementById("end").value)){
		alert("Address Range not valid.");
		document.getElementById("end").focus();
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

	
		
	opener.document.getElementById("irbase").value=document.getElementById("base").value;
	opener.document.getElementById("irend").value=document.getElementById("end").value;
	opener.document.getElementById("irtimeout").value=document.getElementById("timeout").value;
	opener.document.getElementById("irretries").value=document.getElementById("retries").value;
	opener.document.getElementById("modifyDiscoveryConfig").action=opener.document.getElementById("modifyDiscoveryConfig").action+"?action=<%=ActionDiscoveryServlet.addIncludeRangeAction%>";
	opener.document.getElementById("modifyDiscoveryConfig").submit();
	window.close();
	opener.document.focus();
	
}

</script>

<h3>Add Include Range to Discovery</h3>
<div class="boxWrapper">
		  <p>Add a range of IP addresses to include in discovery.<br/>
	      Begin and End IP addresses are required.<br/>
	      <br/>
	      You can set the number of <i>Retries</i> and <i>Timeout</i>.
	      If these parameters are not set, default values will be used.
	      </p>
</div>

<table class="standard">
 <tr>
  <td class="standard" align="center" width="30%">Begin IP Address:<input type="text" id="base" name="base" size="15" value=''/></td>
  <td class="standard" align="center" width="30%">End IP Address:<input type="text" id="end" name="end" size="15"  value=''/></td>
  <td class="standard" align="center" width="20%">Retries:<input type="text" id="retries" name="retries" value='<%=currConfig.getRetries()%>' size="2" /></td>
  <td class="standard" align="center" width="20%">Timeout (ms):<input type="text" id="timeout" name="timeout" value='<%=currConfig.getTimeout()%>' size="5" /></td>
 </tr>
</table>
	

<input type="button" name="addIncludeRange" id="addIncludeRange" value="Add" onclick="addIncludeRange();" />
<input type="button" name="cancel" id="cancel" value="Cancel" onclick="window.close();opener.document.focus();" />
<hr />

</body>

</html>

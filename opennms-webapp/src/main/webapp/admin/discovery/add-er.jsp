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
// 2003 Feb 07: Fixed URLEncoder issues.
// 2002 Nov 26: Fixed breadcrumbs issue.
// 2002 Sep 24: Added a "select" option for SNMP data and a config page.
// 2002 Sep 19: Added a "delete nodes" page to the webUI.
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

<html>

<head>
  <title>Add Exclude Range | Admin | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="css/styles.css" />

</head>

<body>
<script type="text/javascript">
function checkIpAddr(ip){
	var ipArr = ip.split(".");
	if(ipArr.length!=4)
		return false;
	if(isNaN(ipArr[0]) || ipArr[0]=="" || isNaN(ipArr[1]) || ipArr[1]=="" || isNaN(ipArr[2]) || ipArr[2]=="" || isNaN(ipArr[3]) || ipArr[3]=="" || 
		ipArr[0]<0 || ipArr[0]>255 || ipArr[1]<0 || ipArr[1]>255 || ipArr[2]<0 || ipArr[2]>255 || ipArr[3]<0 || ipArr[3]>255)
		return false;
	return true;
}

function checkIpRange(ip1, ip2){
	var ipArr1 = ip1.split(".");	
	var ipArr2 = ip2.split(".");
	for (var i = 0; i < 4; i++) {
		ipArr1[i] = parseInt(ipArr1[i]);
		ipArr2[i] = parseInt(ipArr2[i]);
	}
	if(ipArr1[0]<ipArr2[0]){
		return true;
	}else{
	     if(ipArr1[0]==ipArr2[0]){
			if(ipArr1[1]<ipArr2[1]){
				return true;
			}else{
			     if(ipArr1[1]==ipArr2[1]){
					if(ipArr1[2]<ipArr2[2]){
						return true;
					}else{
					     if(ipArr1[2]==ipArr2[2]){
							if(ipArr1[3]<ipArr2[3]){
								return true;
							}else{
							     if(ipArr1[3]==ipArr2[3]){
								return true;	
							     }else{
								return false;
							     }	

							}
					     }else{
						return false;
					     }	

					}

			     }else{
				return false;
			     }	

			}
	     
	     }else{
	     	return false;
	     }	
	     
	}
	return true;
}

function addExcludeRange(){
	if(!checkIpAddr(document.getElementById("begin").value)){
		alert("Begin Address not valid.");
		document.getElementById("begin").focus();
		return;
	}

	if(!checkIpAddr(document.getElementById("end").value)){
		alert("End Address not valid.");
		document.getElementById("end").focus();
		return;
	}
	
	if(!checkIpRange(document.getElementById("begin").value , document.getElementById("end").value) ){
		alert("Address Range not valid.");
		document.getElementById("end").focus();
		return;
	}
	
	
		
	opener.document.getElementById("erbegin").value=document.getElementById("begin").value;
	opener.document.getElementById("erend").value=document.getElementById("end").value;
	opener.document.getElementById("modifyDiscoveryConfig").action=opener.document.getElementById("modifyDiscoveryConfig").action+"?action=<%=ActionDiscoveryServlet.addExcludeRangeAction%>";
	opener.document.getElementById("modifyDiscoveryConfig").submit();
	window.close();
	opener.document.focus();
	
}

</script>

<h3>Add Range to Exclude from Discovery</h3>
<div class="boxWrapper">
		  <p>Add a range of IP addresses to exclude from discovery.<br/>
			 Insert <i>Begin</i> and <i>End</i> IP addresses and click on <i>Add</i> to confirm.
	      </p>
</div>
<table class="standard">
<tr>
 <td class="standard" align="center" width="35%">Begin IP Address:<input type="text" id="begin" name="begin" size="15" value=''/></td>
 <td class="standard" align="center" width="35%">End IP Address:<input type="text" id="end" name="end" size="15"  value=''/></td>
</tr>
</table>


<input type="submit" name="addExcludeRange" id="addExcludeRange" value="Add" onclick="addExcludeRange();" />
<input type="button" name="cancel" id="cancel" value="Cancel" onclick="window.close();opener.document.focus();" />

<hr/>

</body>

</html>

<!--

//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2007 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 2003 Networked Knowledge Systems, Inc.  All rights reserved.
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

-->
<%@ page contentType="text/html;charset=UTF-8" language="java"%>

<% 
   String breadcrumb1 = "Network Topology Maps";

   //avoid cache
   response.setHeader("Cache-Control","no-store");
   response.setHeader("Pragma","no-cache");
   response.setHeader("Expires","0"); 
%>
		<jsp:include page="/includes/header.jsp" flush="false">
		  <jsp:param name="title" value="Display Network Topology Maps" />
		  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
		</jsp:include>
<p>

<center>
<div id="reloading">
</div>
</center>	

<script language="JavaScript" type="text/javascript" src="extJS/adapter/jquery/jquery.js"></script>
<script language="JavaScript" type="text/javascript" src="extJS/adapter/jquery/jquery.history.js"></script>

<script type="text/javascript">

var svgMapWidth,svgMapHeight;

var resizing=false;

var $j = jQuery.noConflict();
var $ = {};
	
function toggle(id)
{
	el = document.getElementById(id);
	var display = el.style.display ? '' : 'none';
	el.style.display = display;
}

window.onresize = function() {
    if (resizing) return;
    resizing=true;
    resizeSVG();
}

function resize(timeout) {
	writeReload();
	setTimeout("window.location.reload();",timeout);
}

function  writeReload() {
	var o=document.getElementById('reloading');
	o.appendChild(document.createTextNode("Resizing the maps....."));
}

function setSvgWindowSize() {
	if (window.innerWidth)  {
		svgMapWidth=window.innerWidth-35;
		svgMapHeight=window.innerHeight;
	} else if (document.all) {
		svgMapWidth=document.body.clientWidth-20;
		svgMapHeight=document.body.clientHeight;
	}
	
	if (svgMapWidth < 1072)
		svgMapWidth=1072;
	if (svgMapHeight < 600)
		svgMapHeight=600;
}

function emitSVG() {
	resizing=false;
	setSvgWindowSize();
	document.writeln('<embed id="opennmsSVGMaps" src="map/Map.svg"  style="float: left" align="left"  type="image/svg+xml" width="'+svgMapWidth+'" height="'+svgMapHeight+'">');
}

emitSVG();

</script>
</p>

    <jsp:include page="/includes/footer.jsp" flush="false" >
      <jsp:param name="location" value="map" />
    </jsp:include>
    

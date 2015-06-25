<%--
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2003-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

--%>

<%@ page contentType="text/html;charset=UTF-8" language="java"%>

<% 
   String breadcrumb1 = "Network Topology Maps";

   //avoid cache
   response.setHeader("Cache-Control","no-store");
   response.setHeader("Pragma","no-cache");
   response.setHeader("Expires","0"); 
%>

<jsp:include page="/includes/bootstrap.jsp" flush="false">
    <jsp:param name="title" value="Display Network Topology Maps" />
    <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
</jsp:include>

<p>

<center>
<div id="reloading">
</div>
</center>	

<script type="text/javascript" src="js/jquery/jquery.js"></script>
<script type="text/javascript" src="js/jquery/jquery.history.js"></script>

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
    window.resizeSVG();
}

function resize(timeout) {
	writeReload("Resizing the maps.....");
	setTimeout("window.location.reload();",timeout);
}

function reloadConfig(timeout1,timeout2) {
	setTimeout("writeReload(\"Configuration Reloaded. Restarting Maps.....\");",timeout1);
	setTimeout("window.location.reload();",timeout2);
}

function  writeReload(text) {
	var o=document.getElementById('reloading');
    var ls = o.childNodes;
    while (ls.length > 0) {
      var obj = ls.item(0);
      o.removeChild(obj);
    }
	o.appendChild(document.createTextNode(text));
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

<jsp:include page="/includes/bootstrap-footer.jsp" flush="false" >
    <jsp:param name="location" value="map" />
</jsp:include>


<!--

/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2007-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


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
<script type="text/javascript">

function toggle(id)
{
	el = document.getElementById(id);
	var display = el.style.display ? '' : 'none';
	el.style.display = display;
}


function emitSVG(embedAttrs) {
    document.writeln('<embed '+embedAttrs+' width="'+parseInt(screen.availWidth*95/100)+'" height="'+parseInt(((screen.availWidth*95/100)-100)*3/4)+'">');
}
</script>

<script language="JavaScript">
emitSVG('src="map/Map.svg"  style="float: left" align="left"  type="image/svg+xml"');
</script>
</p>
    <jsp:include page="/includes/footer.jsp" flush="false" >
      <jsp:param name="location" value="map" />
    </jsp:include>
    

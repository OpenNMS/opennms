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
   String breadcrumb1 = "Node Map";

   String openlayers_location = "<script src=\"" + System.getProperty("slippy.openlayers.url") + "/lib/OpenLayers.js\" type=\"text/javascript\"></script>";

   //avoid cache
   response.setHeader("Cache-Control","no-store");
   response.setHeader("Pragma","no-cache");
   response.setHeader("Expires","0"); 

%>
		 
		 

		<jsp:include page="/includes/header.jsp" flush="false">
		  <jsp:param name="title" value="Node Map" />
		  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
   		  <jsp:param name="js_onload" value="osm_init()" />

		  <jsp:param name="script" value="<%=openlayers_location%>" />
   		  <jsp:param name="script" value='<script src="node_map/js/options.jsp" type="text/javascript"> </script>' />
   		  <jsp:param name="script" value='<script src="node_map/js/utils.js" type="text/javascript"> </script>' />
		  <jsp:param name="script" value='<script src="node_map/js/node_map_marker.js" type="text/javascript"> </script>' />		 
   		  <jsp:param name="script" value='<script src="node_map/js/main.js" type="text/javascript"> </script>' />
		</jsp:include>


    <div id="map" style="float: left; width: 520px; height: 400px; "> </div>

    <div style="float: left; padding-left: 30px;">
      <form>
        <div>
	  <div> 
	    Select Category:
	  </div>
	  <select name="menu" id="categories">
	  </select>
	</div>
      </form>
      <br/>

      <div style="overflow: auto; padding: 0px; margin: 0px; height: 300px; width: 500px; ">

	<h3 class="o-box" style=" margin: 0px;">Node List</h3>      
	<table id="link_table" class="standard o-box"></table>
      </div>

      <div id="update_time" style="height: 1.5em; margin-top: 10px;"></div>


    </div>
    <div id="location" style="clear:both; height: 1em;"></div>

    <br/>
    <div style="with: 100%; float: left">
        <pre id="debug"></pre>    
    </div>

    <jsp:include page="/includes/footer.jsp" flush="false" >
      <jsp:param name="location" value="map" />
    </jsp:include>
    

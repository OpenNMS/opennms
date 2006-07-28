<!--

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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.map.config.*, org.opennms.web.authenticate.Authentication"%>
<%
   //String type = request.getParameter("type");
   //String format = request.getParameter("format");
   String fullscreen = request.getParameter("fullscreen");
   String refresh = request.getParameter("refresh");
   String dimension = request.getParameter("dimension");
   String mapToOpen = request.getParameter("mapToOpen");
   String[] dim = dimension.split("x");
   int mapwidth=Integer.parseInt(dim[0]);
   int mapheight=Integer.parseInt(dim[1]);
   HttpSession sess = request.getSession(true);
   sess.setAttribute("refreshTime",refresh);
   if(mapToOpen!=null)
   	sess.setAttribute("mapToOpen",new Integer(mapToOpen));
   else sess.removeAttribute("mapToOpen");
%>
<html>
<head>
  <title>Map | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<script language="VBScript" src="<%=org.opennms.web.Util.calculateUrlBase( request )%>map/svgcheck.vbs"></script>
<script language='javascript' src="<%=org.opennms.web.Util.calculateUrlBase( request )%>map/svgcheck.js"></script>

<SCRIPT language='javascript'>
	checkAndGetSVGViewer();
	
//************* global variables begin **************
	var htmldocument=document;
	var mapSvgDocument;
	var menuSvgDocument;
	var mapContextMenu;
	
	var baseContext = '<%=org.opennms.web.Util.calculateUrlBase( request )%>';




	//interval for refreshing nodes attributes (availability, status...): 2 minutes is the default value
	var refreshNodesIntervalInSec=120; 
	
	// colorSemaphoreBy must be 'A' (by availability) or 'S' (by status by default)
	var colorSemaphoreBy='S';
	
// static info for status and severity
	var STATUSES_TEXT= new Array();
	var STATUSES_COLOR= new Array();
	var STATUSES_UEI= new Array();
	
	var SEVERITIES_LABEL= new Array();
	var SEVERITIES_COLOR= new Array();
	var SEVERITIES_FLASH= new Array();

	var AVAIL_COLOR = new Array();
	var AVAIL_MIN = new Array();
	var AVAIL_FLASH= new Array();
<%
	MapPropertiesFactory.init();
	MapPropertiesFactory mpf = MapPropertiesFactory.getInstance();
	java.util.Map statuses = mpf.getStatusesMap();
	java.util.Iterator it = statuses.values().iterator();
	while(it.hasNext()){
		Status status=(Status) it.next();
		%>
			STATUSES_TEXT[<%=status.getId()%>]="<%=status.getText()%>";
			STATUSES_COLOR[<%=status.getId()%>]="<%=status.getColor()%>";
			STATUSES_UEI[<%=status.getId()%>]="<%=status.getUei()%>";
		<%
	}

	java.util.Map severities = mpf.getSeveritiesMap();
	it = severities.values().iterator();
	while(it.hasNext()){
		Severity severity=(Severity) it.next();
		%>
			SEVERITIES_LABEL[<%=severity.getId()%>]="<%=severity.getLabel()%>";
			SEVERITIES_COLOR[<%=severity.getId()%>]="<%=severity.getColor()%>";
			SEVERITIES_FLASH[<%=severity.getId()%>]=<%=severity.isFlash()%>;
		<%
	}

	java.util.Map avails = mpf.getAvailabilitiesMap();
	it = avails.values().iterator();
	while(it.hasNext()){
		Avail avail=(Avail) it.next();
		%>
			AVAIL_MIN[<%=avail.getId()%>]=<%=avail.getMin()%>;
			AVAIL_COLOR[<%=avail.getId()%>]="<%=avail.getColor()%>";
			AVAIL_FLASH[<%=avail.getId()%>]=<%=avail.isFlash()%>;
		<%
	}
	
%>
	// static actions for maps
	var ADDNODES_ACTION = "AddNodes";
	var ADDRANGE_ACTION = "AddRange";
	var ADDMAPS_ACTION = "AddMaps";
	var REFRESH_ACTION = "Refresh";
	var ADDNODES_WITH_NEIG_ACTION = "AddNodesWithNeig";
	var ADDMAPS_WITH_NEIG_ACTION = "AddMapsWithNeig";
	var ADDNODES_NEIG_ACTION = "AddNodesNeig";
	var ADDMAPS_NEIG_ACTION = "AddMapsNeig";
	var DELETENODES_ACTION = "DeleteNodes";
	var DELETEMAPS_ACTION = "DeleteMaps";
	var CLEAR_ACTION = "Clear";
	
	var LOAD_NODES_INFO_ACTION = "LoadNodesInfo";

	var NEWMAP_ACTION = "NewMap";
	var OPENMAP_ACTION = "OpenMap";	
	var CLOSEMAP_ACTION = "CloseMap";

	
	
	var IMAGES_FOLDER = "images";
	var IMAGES_BACKGROUND_FOLDER = IMAGES_FOLDER+"/background/";
	var IMAGES_ELEMENTS_FOLDER = IMAGES_FOLDER+"/elements/";
	var DEFAULT_ICON = "unspecified";
	var DEFAULT_MAP_ICON = "map";
	var DEFAULT_BG_COLOR = "ffffff";
	var NODE_TYPE = "N";
	var MAP_TYPE= "M";
	var MAP_NOT_OPENED = -1;
	var NEW_MAP = -2;
		
	//global variable for map elements dimension setting on.
	var mapElemDimension;	
	
	//the map object to work on.
	var map;
	
	var deletingMapElem=false;
	var addingMapElemNeighbors=false;
	var settingMapElemIcon=false;
	var nodesLoaded = false;
	var mapsLoaded = false;
	
	//var deletingLink=false;
		
	// a currentMapId = MAP_NOT_OPENED indicates that no Maps are opened. 
	var currentMapId=MAP_NOT_OPENED;
	var currentMapBackGround="#"+DEFAULT_BG_COLOR;
	var currentMapAccess="", currentMapName="", currentMapOwner="", currentMapUserlast="", currentMapCreatetime="", currentMapLastmodtime="";
 	
 	// if true the current user is admin.
 	var isUserAdmin = <%=request.isUserInRole( Authentication.ADMIN_ROLE )%>;
 	
	// int containing the number of loading at the moment
	var loading=0;
	
	//string containing a string form of the current map saved. this is used to test if the map is changed
	var savedMapString; 
	
	//global selectionLists for NODES and MAPS
	var selectedMapElemInList=0;
	var selNodes; 

	var nodes;
	var nodeSorts; 
	var nodeSortAss;
	var mynodesResult;
	
	var selectedMapInList=0;
	var selMaps;
	var maps;
	var mapSorts;
	var mapSortAss;
	var mymapsResult;		
	var myMapApp;
	
	
	//global selectionList for MAP BACKGROUND IMAGES
	var selectedBGImageInList=0;
	var selBGImages; 
	var BGImages;
	var BGImagesSorts; 
	var BGImagesSortAss;
	var myBGImagesResult;
	
	
	//global selectionList for NODE DIMENSION
	var selectedMapElemDimInList=3;
	var selMapElemDim; 
	var MapElemDim;
	var MapElemDimSorts; 
	var MapElemDimSortAss;
	var myMapElemDimResult;
	
	//global selectionList for MAP ELEMENT' ICONS
	var selectedMEIconInList=0;
	var selMEIcons; 
	var MEIcons;
	var MEIconsSorts; 
	var MEIconsSortAss;
	var myMEIconsResult;		


// ************* global variables end **************	
			
	
</SCRIPT>


<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">
<% 
   String breadcrumb1 = "<a href='map/index.jsp'>Map</a>";
   String breadcrumb2 = "View Network Map";

%>
<% if(fullscreen.equals("n")) { %>
<jsp:include page="/includes/header.jsp" flush="false" >
  <jsp:param name="title" value="Display Map" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
</jsp:include>
<br>
<% } %>
<div  style="background-color:#c9dfc9" align="right" width="100%">

<br>
<!-- Body -->
<p id="parentParagraph">
<script language="JavaScript">
emitSVG('src="map/Map.svg"  style="float: left" align="left"  height="<%=mapheight%>" width="<%=mapwidth%>" type="image/svg+xml" pluginspage="http://download.adobe.com/pub/adobe/magic/svgviewer/win/6.x/6.0x38363/en/SVGView.exe" ');
emitSVG('src="map/Menu.svg" valign="top" height="100%" width="200" type="image/svg+xml" pluginspage="http://download.adobe.com/pub/adobe/magic/svgviewer/win/6.x/6.0x38363/en/SVGView.exe" ');
</script>

</p>
<br>
</div>
<% if(fullscreen.equals("n")) { %>
    <jsp:include page="/includes/footer.jsp" flush="false" />
<% } %>

  </body>
</html>

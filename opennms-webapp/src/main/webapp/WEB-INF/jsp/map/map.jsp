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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
  <title>Map | OpenNMS Web Console</title>
  <base HREF="<%=org.opennms.web.Util.calculateUrlBase( request )%>" />
  <link rel="stylesheet" type="text/css" href="includes/styles.css" />
</head>
<script language="VBScript" src="<%=org.opennms.web.Util.calculateUrlBase( request )%>map/svgcheck.vbs"></script>
<script language='javascript' src="<%=org.opennms.web.Util.calculateUrlBase( request )%>map/svgcheck.js"></script>

<script language="javascript" src="map/selectionBox/helper_functions.js"></script>


<SCRIPT language='javascript'>

	checkAndGetSVGViewer();
	
//************* global variables begin **************
	// this is the map.jsp document. Parent of all
	var htmldocument=document;
	
	// this is the SVG that holds the map rendering
	// child of htmldocument
	var mapSvgDocument;
	
	// this is the SVG menu
	// child of htmldocument
	var menuSvgDocument;
	
	// Global variable used to enable context menu
	var mapContextMenu;
	
	var baseContext = '<%=org.opennms.web.Util.calculateUrlBase( request )%>';

	
	//the suffix to inject into requests to server
	var suffix = location.pathname.substr(location.pathname.lastIndexOf(".")+1);

	// colorSemaphoreBy must be by status, by availability or by severity
	var colorSemaphoreBy='<c:out value="${mapsPropertiesFactory.defaultSemaphoreColorBy}"/>';
	
// static info for status, severity, availability, links and context menu

	var STATUSES_TEXT= new Array();
	var STATUSES_COLOR= new Array();
	var STATUSES_UEI= new Array();
	
	var SEVERITIES_LABEL= new Array();
	var SEVERITIES_COLOR= new Array();
	var SEVERITIES_FLASH= new Array();

	var AVAIL_COLOR = new Array();
	var AVAIL_MIN = new Array();
	var AVAIL_FLASH= new Array();
	
	var LINK_SPEED = new Array();
	var LINK_TEXT = new Array();
	var LINK_WIDTH = new Array();
	var LINK_DASHARRAY = new Array();	
	
	var LINKSTATUS_COLOR = new Array();	
	var LINKSTATUS_FLASH = new Array();	
		
	var CM_COMMANDS = new Array();
	var CM_LINKS = new Array();
	var CM_PARAMS = new Array();

	var fullscreen = <c:out value="${manager.mapStartUpConfig.fullScreen}"/>;
	var refreshNodesIntervalInSec=<c:out value="${manager.mapStartUpConfig.refreshTime}"/>; 
	var mapToOpen=<c:out value="${manager.mapStartUpConfig.mapToOpenId}"/>;

	var reloadMap = <c:out value="${mapsPropertiesFactory.reload}"/>;
	var contextMenuEnabled = <c:out value="${mapsPropertiesFactory.contextMenuEnabled}"/>;
	var doubleClickEnabled = <c:out value="${mapsPropertiesFactory.doubleClickEnabled}"/>;

	var UNKNOWN_STATUS = <c:out value="${mapsPropertiesFactory.unknownStatus.id}"/>;
	
	<c:forEach items="${mapsPropertiesFactory.contextMenu.entries}" var="cmEntry" varStatus="status">
				CM_COMMANDS[<c:out value="${status.count}"/>]='<c:out value="${cmEntry.command}"/>';
				CM_LINKS[<c:out value="${status.count}"/>]='<c:out value="${cmEntry.link}"/>';
				CM_PARAMS[<c:out value="${status.count}"/>]='<c:out value="${cmEntry.params}"/>';
    </c:forEach>

	<c:forEach items="${mapsPropertiesFactory.links}" var="link">
		LINK_SPEED[<c:out value="${link.id}"/>]='<c:out value="${link.speed}"/>';
		LINK_TEXT[<c:out value="${link.id}"/>]='<c:out value="${link.text}"/>';
		LINK_WIDTH[<c:out value="${link.id}"/>]='<c:out value="${link.width}"/>';
		LINK_DASHARRAY[<c:out value="${link.id}"/>]='<c:out value="${link.dasharray}"/>';
    </c:forEach>

	<c:forEach items="${mapsPropertiesFactory.linkStatuses}" var="linkstatus">
		LINKSTATUS_COLOR['<c:out value="${linkstatus.id}"/>']='<c:out value="${linkstatus.color}"/>';
		LINKSTATUS_FLASH['<c:out value="${linkstatus.id}"/>']=<c:out value="${linkstatus.flash}"/>;
    </c:forEach>

	<c:forEach items="${mapsPropertiesFactory.statuses}" var="status">
			STATUSES_TEXT[<c:out value="${status.id}"/>]="<c:out value="${status.text}"/>";
			STATUSES_COLOR[<c:out value="${status.id}"/>]="<c:out value="${status.color}"/>";
			STATUSES_UEI[<c:out value="${status.id}"/>]="<c:out value="${status.uei}"/>";
    </c:forEach>

	<c:forEach items="${mapsPropertiesFactory.severities}" var="severity">
			SEVERITIES_LABEL[<c:out value="${severity.id}"/>]="<c:out value="${severity.label}"/>";
			SEVERITIES_COLOR[<c:out value="${severity.id}"/>]="<c:out value="${severity.color}"/>";
			SEVERITIES_FLASH[<c:out value="${severity.id}"/>]=<c:out value="${severity.flash}"/>;
    </c:forEach>
    
    
  	<c:forEach items="${mapsPropertiesFactory.avails}" var="avail">
			AVAIL_MIN[<c:out value="${avail.id}"/>]=<c:out value="${avail.min}"/>;
			AVAIL_COLOR[<c:out value="${avail.id}"/>]="<c:out value="${avail.color}"/>";
			AVAIL_FLASH[<c:out value="${avail.id}"/>]=<c:out value="${avail.flash}"/>;
    </c:forEach>
   
	// static actions for maps
	var ADDNODES_ACTION = "<c:out value="${mapsConstants.ADDNODES_ACTION}"/>";
	var ADDRANGE_ACTION = "<c:out value="${mapsConstants.ADDRANGE_ACTION}"/>";
	var ADDMAPS_ACTION = "<c:out value="${mapsConstants.ADDMAPS_ACTION}"/>";
	var REFRESH_ACTION = "<c:out value="${mapsConstants.REFRESH_ACTION}"/>";
	var RELOAD_ACTION = "<c:out value="${mapsConstants.RELOAD_ACTION}"/>";
	var ADDNODES_WITH_NEIG_ACTION = "<c:out value="${mapsConstants.ADDNODES_WITH_NEIG_ACTION}"/>";
	var ADDMAPS_WITH_NEIG_ACTION = "<c:out value="${mapsConstants.ADDMAPS_WITH_NEIG_ACTION}"/>";
    var ADDNODES_BY_CATEGORY_ACTION = "<c:out value="${mapsConstants.ADDNODES_BY_CATEGORY_ACTION}"/>";
	var ADDNODES_BY_LABEL_ACTION = "<c:out value="${mapsConstants.ADDNODES_BY_LABEL_ACTION}"/>";
	var ADDNODES_NEIG_ACTION = "<c:out value="${mapsConstants.ADDNODES_NEIG_ACTION}"/>";
	var ADDMAPS_NEIG_ACTION = "<c:out value="${mapsConstants.ADDMAPS_NEIG_ACTION}"/>";
	var DELETENODES_ACTION = "<c:out value="${mapsConstants.DELETENODES_ACTION}"/>";
	var DELETEMAPS_ACTION = "<c:out value="${mapsConstants.DELETEMAPS_ACTION}"/>";
	var CLEAR_ACTION = "<c:out value="${mapsConstants.CLEAR_ACTION}"/>";
	var LOAD_NODES_INFO_ACTION = "<c:out value="${mapsConstants.ADDNODES_ACTION}"/>";
	var DELETEMAP_ACTION = "<c:out value="${mapsConstants.DELETEMAP_ACTION}"/>";
	var SWITCH_MODE_ACTION = "<c:out value="${mapsConstants.SWITCH_MODE_ACTION}"/>";
	var LOADMAPS_ACTION = "<c:out value="${mapsConstants.LOADMAPS_ACTION}"/>";
	var LOADNODES_ACTION = "<c:out value="${mapsConstants.LOADNODES_ACTION}"/>";
	
	var NEWMAP_ACTION = "<c:out value="${mapsConstants.NEWMAP_ACTION}"/>";
	var OPENMAP_ACTION = "<c:out value="${mapsConstants.OPENMAP_ACTION}"/>";
	var CLOSEMAP_ACTION = "<c:out value="${mapsConstants.CLOSEMAP_ACTION}"/>";

	var SAVEMAP_ACTION = "<c:out value="${mapsConstants.SAVEMAP_ACTION}"/>";
	
	
	
	var DEFAULT_ICON = "<c:out value="${mapsPropertiesFactory.defaultNodeIcon}"/>";
	var DEFAULT_MAP_ICON =  "<c:out value="${mapsPropertiesFactory.defaultMapIcon}"/>";
	var DEFAULT_BG_COLOR =  "<c:out value="${mapsPropertiesFactory.defaultBackgroundColor}"/>";
	var NODE_TYPE = "<c:out value="${mapsConstants.NODE_TYPE}"/>";
	var MAP_TYPE= "<c:out value="${mapsConstants.MAP_TYPE}"/>";
	var MAP_NOT_OPENED = "<c:out value="${mapsConstants.MAP_NOT_OPENED}"/>";
	var NEW_MAP = "<c:out value="${mapsConstants.NEW_MAP}"/>";
		
	//global variable for map elements dimension setting on.
	var mapElemDimension=<c:out value="${mapsPropertiesFactory.defaultMapElementDimension}"/>;	
	
	var IMAGES_FOLDER = "images";
	var IMAGES_BACKGROUND_FOLDER = IMAGES_FOLDER+"/background/";
	var IMAGES_ELEMENTS_FOLDER = IMAGES_FOLDER+"/elements/";
	
	//the map object to work on.
	var map;
	
	var refreshingMapElems=false;
	var deletingMapElem=false;
	var addingMapElemNeighbors=false;
	var settingMapElemIcon=false;
	
	//TODO forced appInited to true for debugging without InitApplication()
	var appInited = true;
	var nodesLoaded = false;
	var mapsLoaded = false;
	
	// a currentMapId = MAP_NOT_OPENED indicates that no Maps are opened. 
	var currentMapId=MAP_NOT_OPENED
	
	var currentMapBackGround="";
	var currentMapAccess="", currentMapName="", currentMapOwner="", currentMapUserlast="", currentMapCreatetime="", currentMapLastmodtime="";
	
	// array containing the number of links existing between 2 elements
	// the key of the array is like 'idElem1-idElem2' and the value is an integer representin the number of links between elem1 and elem2
	var linksBetweenElements = new Array();
	
	// array containing the history of the maps opened
	var mapHistory = new Array();
	var mapHistoryName = new Array();
	// the index of the current map in the history
	var mapHistoryIndex = 0;
 	
 	// if true the current user is admin.
 	var isUserAdmin = <c:out value="${manager.userAdmin}"/>;
 	
 	// if true the user can modify the maps
 	var isAdminMode = false;
 	
	// int containing the number of loading at the moment
	var loading=0;
	
	//string containing a string form of the current map saved. this is used to test if the map is changed
	var savedMapString=new String(); 
	
	//global selectionLists for NODES, MAPS and CATEGORIES
	var selectedMapElemInList=0;
	var selNodes; 

	var nodes;
	var nodeSorts; 
	var nodeSortAss;
	var mynodesResult;
	
	var selectedMapInList=0;
	var selMaps;
	
	// populate maps here
	var maps;
	var mapSorts;
	var mapSortAss;
	
	var mymapsResult;		
	var myMapApp;
	
	var selectedCategoryInList=0;
	var selCategories; 

	var categories;
	var categorySorts; 
	var categorySortAss;
	var mycategoriesResult;
	
	categories= [" "];
	categorySorts = [null];
		
  	<c:forEach items="${manager.categories}" var="category">
			categories.push(escapeSpecialChars('<c:out value="${category}"/>'));
			categorySorts.push(escapeSpecialChars('<c:out value="${category}"/>'));
    </c:forEach>	

	categorySortAss = assArrayPopulate(categories,categorySorts);
	
	//global selectionList for MAP BACKGROUND IMAGES
	var selectedBGImageInList=0;
	var selBGImages; 
	var BGImages=[""];
	var BGImagesSorts=[null]; 
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
	var MEIcons=[""];
	var MEIconsSorts=[null]; 
	var MEIconsSortAss;
	var myMEIconsResult;		
	
	<c:forEach items="${mapsPropertiesFactory.icons}" var="icon">
		MEIcons.push('<c:out value="${icon.key}"/>');
		MEIconsSorts.push(IMAGES_ELEMENTS_FOLDER+'<c:out value="${icon.value}"/>');
    </c:forEach>
    
    <c:forEach items="${mapsPropertiesFactory.backgroundImages}" var="image">
		BGImages.push('<c:out value="${image.key}"/>');
		BGImagesSorts.push(IMAGES_BACKGROUND_FOLDER+'<c:out value="${image.value}"/>');
    </c:forEach>

    MapElemDim=new Array();
    MapElemDimSorts=new Array();
    <c:forEach items="${mapsPropertiesFactory.mapElementDimensions}" var="dim">
		MapElemDim.push('<c:out value="${dim.value}"/>');
		MapElemDimSorts.push('<c:out value="${dim.key}"/>');
    </c:forEach>


    BGImagesSortAss = assArrayPopulate(BGImages,BGImagesSorts);		
    MapElemDimSortAss = assArrayPopulate(MapElemDim,MapElemDimSorts);	
    MEIconsSortAss = assArrayPopulate(MEIcons,MEIconsSorts);
	
	
// ************* global variables end **************	
			
	
</SCRIPT>


<body marginwidth="0" marginheight="0" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0">
<% 
   String breadcrumb1 = "<a href='Index.map'>Map</a>";
   String breadcrumb2 = "View Network Map";

%>
<c:choose>
	<c:when test="${!manager.mapStartUpConfig.fullScreen}">
		<jsp:include page="/includes/header.jsp" flush="false" >
		  <jsp:param name="title" value="Display Map" />
		  <jsp:param name="breadcrumb" value="<%=breadcrumb1%>" />
		  <jsp:param name="breadcrumb" value="<%=breadcrumb2%>" />
		</jsp:include>
	</c:when>
</c:choose>

<div  style="background-color:#EFEFEF" align="right" width="100%">

<br>
<!-- Body -->
<p id="parentParagraph">
<script language="JavaScript">
emitSVG('src="map/Map.svg"  style="float: left" align="left"  height="<c:out value="${manager.mapStartUpConfig.screenHeight}"/>" width="<c:out value="${manager.mapStartUpConfig.screenWidth}"/>" type="image/svg+xml" pluginspage="http://download.adobe.com/pub/adobe/magic/svgviewer/win/6.x/6.0x38363/en/SVGView.exe" ');
emitSVG('src="map/Menu.svg" valign="top" height="<c:out value="${manager.mapStartUpConfig.screenHeight}"/>" width="200" type="image/svg+xml" pluginspage="http://download.adobe.com/pub/adobe/magic/svgviewer/win/6.x/6.0x38363/en/SVGView.exe" ');
</script>

</p>
<br>
</div>
<c:choose>
	<c:when test="${!manager.mapStartUpConfig.fullScreen}">
    <jsp:include page="/includes/footer.jsp" flush="false" />
	</c:when>
	
	<c:otherwise>
		<%= "</body>" %>
		<%= "</html>" %>
    </c:otherwise>
</c:choose>

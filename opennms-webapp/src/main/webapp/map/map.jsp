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

<%@page language="java" contentType="text/html" session="true" import="org.opennms.web.map.config.*, org.opennms.web.acegisecurity.Authentication"%>
<%
   //String type = request.getParameter("type");
   //String format = request.getParameter("format");
   String fullscreen = request.getParameter("fullscreen");
   String refresh = request.getParameter("refresh");
   String dimension = request.getParameter("dimension");
   String mapsFactory = request.getParameter("mapsFactory");
   String mapToOpen = request.getParameter("mapToOpen");
   String[] dim = dimension.split("x");
   int mapwidth=Integer.parseInt(dim[0]);
   int mapheight=Integer.parseInt(dim[1]);
   HttpSession sess = request.getSession(true);
   sess.setAttribute("mapsFactoryLabel",mapsFactory);
   sess.setAttribute("refreshTime",refresh);
   if(mapToOpen==null)
   	sess.setAttribute("mapToOpen",/*new Integer(1)*/ null);
   else sess.setAttribute("mapToOpen",new Integer(Integer.parseInt(mapToOpen)));
%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.List"%>
<%@page import="org.opennms.web.map.config.ContextMenu.CMEntry"%>
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
	var htmldocument=document;
	var mapSvgDocument;
	var menuSvgDocument;
	var mapContextMenu;
	
	var baseContext = '<%=org.opennms.web.Util.calculateUrlBase( request )%>';


	var mapsFactory= '<%=mapsFactory%>';

	//interval for refreshing nodes attributes (availability, status...): 2 minutes is the default value
	var refreshNodesIntervalInSec=120; 
	
	// colorSemaphoreBy must be 'A' (by availability) or 'S' (by status by default)
	var colorSemaphoreBy='S';
	
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
<%
	MapPropertiesFactory.init();
	MapPropertiesFactory mpf = MapPropertiesFactory.getInstance();
	MapsFactory mapsFact = null;
	if(mapsFactory==null){
		mapsFact = mpf.getDefaultFactory();
	}else{
		mapsFact = mpf.getMapsFactory(mapsFactory);
		if(mapsFact==null){
			throw new ServletException("The maps factory doesn't exists.");
		}
	}
	boolean reload = false;
	boolean contextMenu = true;
	boolean doubleClick = true;
	reload = mapsFact.isReload();
	contextMenu = mapsFact.isContextMenu();
	doubleClick = mapsFact.isDoubleClick();
	if(contextMenu){
		ContextMenu contMenu =	mapsFact.getContMenu();
		if(contMenu!=null){
			List cmEntries = contMenu.getEntries();
			Iterator it = cmEntries.iterator();
			int countEntries=0;
			while(it.hasNext()){
				ContextMenu.CMEntry entry = (ContextMenu.CMEntry)it.next();
				String command = entry.command;
				String link = entry.link;
				String params = entry.params;
				%>
				CM_COMMANDS[<%=countEntries%>]='<%=command%>';
				CM_LINKS[<%=countEntries%>]='<%=link%>';
				CM_PARAMS[<%=countEntries%>]='<%=params%>';
				<%
				countEntries++;
			}
		}
	}
	
	Map links = mpf.getLinksMap();
	Iterator it = 	links.keySet().iterator();
	while(it.hasNext()){
		Integer linkid = (Integer) it.next();
		Link link = (Link) links.get(linkid);
		%>
		LINK_SPEED[<%=linkid.intValue()%>]='<%=link.getSpeed()%>';
		LINK_TEXT[<%=linkid.intValue()%>]='<%=link.getText()%>';
		LINK_WIDTH[<%=linkid.intValue()%>]='<%=link.getWidth()%>';
		LINK_DASHARRAY[<%=linkid.intValue()%>]=<%=link.getDasharray()%>;
		<%
	}
	
	Map linkstatuses = mpf.getLinkStatusesMap();
	it = 	linkstatuses.keySet().iterator();
	while(it.hasNext()){
		String linkstid = (String) it.next();
		LinkStatus linkst = (LinkStatus) linkstatuses.get(linkstid);
		%>
		LINKSTATUS_COLOR['<%=linkstid%>']='<%=linkst.getColor()%>';
		LINKSTATUS_FLASH['<%=linkstid%>']=<%=linkst.isFlash()%>;
		<%
	}	
	
	%>
		var reloadMap = <%=reload%>;
		var contextMenuEnabled = <%=contextMenu%>;
		var doubleClickEnabled = <%=doubleClick%>;				
	<%
	Status[] statuses = mpf.getOrderedStatuses();
	for(int i=0; i<statuses.length;i++){
		Status status=statuses[i];
		%>
			STATUSES_TEXT[<%=status.getId()%>]="<%=status.getText()%>";
			STATUSES_COLOR[<%=status.getId()%>]="<%=status.getColor()%>";
			STATUSES_UEI[<%=status.getId()%>]="<%=status.getUei()%>";
		<%
	}

	Severity[] severities = mpf.getOrderedSeverities();
	for(int i=0; i<severities.length;i++){
		Severity severity=severities[i];
		%>
			SEVERITIES_LABEL[<%=severity.getId()%>]="<%=severity.getLabel()%>";
			SEVERITIES_COLOR[<%=severity.getId()%>]="<%=severity.getColor()%>";
			SEVERITIES_FLASH[<%=severity.getId()%>]=<%=severity.isFlash()%>;
		<%
	}

	Avail[] avails = mpf.getOrderedAvails();
	for(int i=0; i<avails.length;i++){
		Avail avail=avails[i];
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
	var RELOAD_ACTION = "Reload";
	var ADDNODES_WITH_NEIG_ACTION = "AddNodesWithNeig";
	var ADDMAPS_WITH_NEIG_ACTION = "AddMapsWithNeig";
	var ADDNODES_NEIG_ACTION = "AddNodesNeig";
	var ADDMAPS_NEIG_ACTION = "AddMapsNeig";
	var DELETENODES_ACTION = "DeleteNodes";
	var DELETEMAPS_ACTION = "DeleteMaps";
	var CLEAR_ACTION = "Clear";
	
	var LOAD_NODES_INFO_ACTION = "LoadNodesInfo";
	
	var DELETEMAP_ACTION = "DeleteMap";

	var INIT_ACTION = "Init";
	var LOADMAPS_ACTION = "LoadMaps";
	var LOADNODES_ACTION = "LoadNodes";
	
	var NEWMAP_ACTION = "NewMap";
	var OPENMAP_ACTION = "OpenMap";	
	var CLOSEMAP_ACTION = "CloseMap";

	var SAVEMAP_ACTION = "SaveMap";
	
	var IMAGES_FOLDER = "images";
	var IMAGES_BACKGROUND_FOLDER = IMAGES_FOLDER+"/background/";
	var IMAGES_ELEMENTS_FOLDER = IMAGES_FOLDER+"/elements/";
	var DEFAULT_ICON = "<%=mpf.getDefaultNodeIcon()%>";
	var DEFAULT_MAP_ICON = "<%=mpf.getDefaultMapIcon()%>";
	var DEFAULT_BG_COLOR = "ffffff";
	var NODE_TYPE = "N";
	var MAP_TYPE= "M";
	var MAP_NOT_OPENED = -1;
	var NEW_MAP = -2;
		
	//global variable for map elements dimension setting on.
	//by default map element dimension is 20px (normal)	
	var mapElemDimension=20;	
	
	//the map object to work on.
	var map;
	
	var refreshingMapElems=false;
	var deletingMapElem=false;
	var addingMapElemNeighbors=false;
	var settingMapElemIcon=false;
	var appInited = false;
	var nodesLoaded = false;
	var mapsLoaded = false;
	
	//var deletingLink=false;
		
	// a currentMapId = MAP_NOT_OPENED indicates that no Maps are opened. 
	var currentMapId=MAP_NOT_OPENED;
	var currentMapBackGround="#"+DEFAULT_BG_COLOR;
	var currentMapAccess="", currentMapName="", currentMapOwner="", currentMapUserlast="", currentMapCreatetime="", currentMapLastmodtime="";
	
	// array containing the history of the maps opened
	var mapHistory = new Array();
	var mapHistoryName = new Array();
	// the index of the current map in the history
	var mapHistoryIndex = 0;
 	
 	// if true the current user is admin.
 	var isUserAdmin = false;
 	
	// int containing the number of loading at the moment
	var loading=0;
	
	//string containing a string form of the current map saved. this is used to test if the map is changed
	var savedMapString=new String(); 
	
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
<%
	java.util.Map icons = mpf.getIconsMap();
	it = icons.entrySet().iterator();
	while(it.hasNext()){
		java.util.Map.Entry iconEntry=(java.util.Map.Entry) it.next();
		%>
			MEIcons.push("<%=((String)iconEntry.getKey())%>");
			MEIconsSorts.push(IMAGES_ELEMENTS_FOLDER+"<%=((String)iconEntry.getValue())%>");
		<%
	}
%>	
<%
	java.util.Map bgImages = mpf.getBackgroundImagesMap();
	it = bgImages.entrySet().iterator();
	while(it.hasNext()){
		java.util.Map.Entry bgImageEntry=(java.util.Map.Entry) it.next();
		%>
			BGImages.push("<%=((String)bgImageEntry.getKey())%>");
			BGImagesSorts.push(IMAGES_BACKGROUND_FOLDER+"<%=((String)bgImageEntry.getValue())%>");
		<%
	}
%>

	MapElemDim= ["smallest", "very small", "small","normal","big", "biggest"];
	MapElemDimSorts= ["6", "10", "15", "20", "25", "30"];


	BGImagesSortAss = assArrayPopulate(BGImages,BGImagesSorts);		
	MapElemDimSortAss = assArrayPopulate(MapElemDim,MapElemDimSorts);	
	MEIconsSortAss = assArrayPopulate(MEIcons,MEIconsSorts);
	
	
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
<div  style="background-color:#EFEFEF" align="right" width="100%">

<br>
<!-- Body -->
<p id="parentParagraph">
<script language="JavaScript">
emitSVG('src="map/Map.svg"  style="float: left" align="left"  height="<%=mapheight%>" width="<%=mapwidth%>" type="image/svg+xml" pluginspage="http://download.adobe.com/pub/adobe/magic/svgviewer/win/6.x/6.0x38363/en/SVGView.exe" ');
emitSVG('src="map/Menu.svg" valign="top" height="<%=mapheight%>" width="200" type="image/svg+xml" pluginspage="http://download.adobe.com/pub/adobe/magic/svgviewer/win/6.x/6.0x38363/en/SVGView.exe" ');
</script>

</p>
<br>
</div>
<% if(fullscreen.equals("n")) { %>
    <jsp:include page="/includes/footer.jsp" flush="false" />
<% } %>

  </body>
</html>

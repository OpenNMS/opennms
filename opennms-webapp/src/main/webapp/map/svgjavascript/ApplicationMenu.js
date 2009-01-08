// Menu Svg Object Basic Functions	
	
function enableMenu(){
	var mapMenu = document.getElementById("MapMenu");
	if(mapMenu!=null)
		mapMenu.setAttributeNS(null,'display', 'inline');
	
	var nodeMenu = 	document.getElementById("Node");
	if(nodeMenu != null)
		nodeMenu.setAttributeNS(null,'display', 'inline');
		
	var viewMenu = 	document.getElementById("View")
	if(viewMenu != null)
		viewMenu.setAttributeNS(null,'display', 'inline');

	var refreshMenu = 	document.getElementById("Refresh")
	if(viewMenu != null)
		viewMenu.setAttributeNS(null,'inline', 'none');

}

function disableMenu(){
	var mapMenu = document.getElementById("MapMenu");
	if(mapMenu!=null)
		mapMenu.setAttributeNS(null,'display', 'none');
	
	
	var nodeMenu = 	document.getElementById("Node");
	if(nodeMenu != null)
		nodeMenu.setAttributeNS(null,'display', 'none');
		
	var viewMenu = 	document.getElementById("View")
	if(viewMenu != null)
		viewMenu.setAttributeNS(null,'display', 'none');

	var refreshMenu = 	document.getElementById("Refresh")
	if(viewMenu != null)
		viewMenu.setAttributeNS(null,'display', 'none');

}
	
function hideAll(evt){
	elemHide(evt, "MapChoices");
	elemHide(evt, "NodeChoices");
	elemHide(evt, "ViewChoices");
	elemHide(evt, "RefreshChoices");
}
	
function hideSubMenus(evt){
	elemHide(evt,'SetBGColorGroup');
	elemHide(evt,'ColorNodesBySeverityGroup');
	elemHide(evt,'ColorNodesByStatusGroup');
	elemHide(evt,'ColorNodesByAvailGroup');
	elemHide(evt,'SetBGImageGroup');
}
	
function removeMenuChilds(){
	var obj, ls;
	ls = menuSvgElement.childNodes;
	while (ls.length > 0) {
	  obj = ls.item(0);
	  menuSvgElement.removeChild(obj);
	}
}

function setMenuOpenFlag(evt, subMenuName){
	if(menuOpenFlag==false){
		menuOpenFlag=true;
		elemShow(evt, subMenuName);
	} else {
		menuOpenFlag=false;
		elemHide(evt, subMenuName);
	}		
}
	
function openMapMenu(evt){
	elemColor(evt,'MapMenuGroup',downColor);
	if(menuOpenFlag==true){
		hideAll(evt);
		elemShow(evt, "MapChoices");
	}
}

function openNodeMenu(evt){
	elemColor(evt,'NodeGroup',downColor);
	if(menuOpenFlag==true){
		hideAll(evt);

		elemShow(evt, "NodeChoices");
	}
}	


function openViewMenu(evt){
	elemColor(evt,'ViewGroup',downColor);
	if(menuOpenFlag==true){
		hideAll(evt);
		elemShow(evt, "ViewChoices");
	}
}

function openRefreshMenu(evt){
	elemColor(evt,'RefreshGroup',downColor);
	if(menuOpenFlag==true){
		hideAll(evt);
		elemShow(evt, "RefreshChoices");
	}
}

function instantiateMapGroupAdminMode() {
	// *** MAP MENU ***
	var action='MapMenu';
	addMenuElement(menuSvgElement, "inline", 0, 0 , 70, 20,  action, "Map",  "elemColor(evt,'"+action+"',downColor)", "elemColor(evt, '"+action+"',upColor)", "openMapMenu(evt); hideSubMenus(evt);setMenuOpenFlag(evt, 'MapChoices');" );
	
	var MapChoices = createGroup(menuSvgElement, "MapChoices","none", "elemColor(evt,'Map',downColor)","elemColor(evt,'Map',upColor)");
	
	action = "New";
	addMenuElement(MapChoices, "inline", 0, 21 , 80, 20,  action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "newMap();hideAll(evt);menuOpenFlag=false;");
	action="Open";
	addMenuElement(MapChoices, "inline", 0, 41 , 80, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addMapsList(); hideAll(evt);menuOpenFlag=false;");
	action="Close";
	addMenuElement(MapChoices, "inline", 0, 61 , 80, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "close();hideAll(evt);menuOpenFlag=false;");
	action="Rename";
	addMenuElement(MapChoices, "inline", 0, 81 , 80, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addRenameMapBox(); hideAll(evt);menuOpenFlag=false;");
	action="Delete";
	addMenuElement(MapChoices, "inline", 0, 101 , 80, 20, action, action,  "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "deleteMap();hideAll(evt);menuOpenFlag=false;clearTopInfo();clearDownInfo();");
	action="Save";
	addMenuElement(MapChoices, "inline", 0, 121 , 80, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "saveMap();hideAll(evt);menuOpenFlag=false;");
	action="Clear";
	addMenuElement(MapChoices, "inline", 0, 141 , 80, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "clearMap();hideAll(evt);menuOpenFlag=false;clearTopInfo();clearDownInfo();");
	action="RefreshMode";
	addMenuElement(MapChoices, "inline", 0, 181 , 80, 20, action, "Refresh Mode", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "switchToNormalMode();hideAll(evt);menuOpenFlag=false;");
	action="SetBG";
	addMenuElement(MapChoices, "inline", 0, 161 , 80, 20, action, "Set Background", "elemColor(evt,'"+action+"',downColor); elemShow(evt,'SetBGColorGroup'); elemShow(evt,'SetBGImageGroup')" , "elemColor(evt,'"+action+"',upColor)", "elemShow(evt,'SetBGColor'); elemShow(evt,'SetBGImage')");
	action="SetBGColor";
	addMenuElement(MapChoices, "none", 60, 161 , 80, 20, action, "Color", "elemColor(evt,'"+action+"',downColor);", "elemColor(evt,'"+action+"',upColor)", "setBackground();  hideAll(evt);menuOpenFlag=false;clearTopInfo();clearDownInfo();");
	action="SetBGImage";
	addMenuElement(MapChoices, "none", 60, 181 , 80, 20, action, "Image", "elemColor(evt,'"+action+"',downColor);", "elemColor(evt,'"+action+"',upColor)", "addBGImagesList();hideAll(evt);menuOpenFlag=false;");

}

function instantiateMapGroupNormalMode() {
	// *** MAP MENU ***
	var action='MapMenu';
	addMenuElement(menuSvgElement, "inline", 0, 0 , 70, 20,  action, "Map",  "elemColor(evt,'"+action+"',downColor)", "elemColor(evt, '"+action+"',upColor)", "openMapMenu(evt); hideSubMenus(evt);setMenuOpenFlag(evt, 'MapChoices');" );
	
	var MapChoices = createGroup(menuSvgElement, "MapChoices","none", "elemColor(evt,'Map',downColor)","elemColor(evt,'Map',upColor)");
	
	action="Open";
	addMenuElement(MapChoices, "inline", 0, 21 , 80, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addMapsList(); hideAll(evt);menuOpenFlag=false;");
	
	action="Close";
	addMenuElement(MapChoices, "inline", 0, 41 , 80, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "close();hideAll(evt);menuOpenFlag=false;");
	
	action="AdminMode";
	addMenuElement(MapChoices, "inline", 0, 61 , 80, 20, action, "Admin Mode", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "switchToAdminMode();hideAll(evt);menuOpenFlag=false;");

}

function instantiateMapGroup() {
	// *** MAP MENU ***
	var action='MapMenu';
	addMenuElement(menuSvgElement, "inline", 0, 0 , 70, 20,  action, "Map",  "elemColor(evt,'"+action+"',downColor)", "elemColor(evt, '"+action+"',upColor)", "openMapMenu(evt); hideSubMenus(evt);setMenuOpenFlag(evt, 'MapChoices');" );

	var MapChoices = createGroup(menuSvgElement, "MapChoices","none", "elemColor(evt,'Map',downColor)","elemColor(evt,'Map',upColor)");
	
	action="Open";
	addMenuElement(MapChoices, "inline", 0, 21 , 80, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addMapsList(); hideAll(evt);menuOpenFlag=false;");
	action="Close";
	addMenuElement(MapChoices, "inline", 0, 41 , 80, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "close();hideAll(evt);menuOpenFlag=false;");

}

function instantiateRefreshGroupAdminMode() {
	action='Refresh';
	addMenuElement(menuSvgElement, "inline", 141, 0 , 70, 20,  action, "Refresh",  "elemColor(evt,'"+action+"',downColor)", "elemColor(evt, '"+action+"',upColor)", "openRefreshMenu(evt); hideSubMenus(evt);setMenuOpenFlag(evt, 'RefreshChoices');" );

	var RefreshChoices = createGroup(menuSvgElement, "RefreshChoices","none", "elemColor(evt,'"+action+"',downColor)","elemColor(evt,'"+action+"',upColor)");
	action = "LoadMaps";
	addMenuElement(RefreshChoices, "inline", 141, 21 , 80, 20,  action, "Load Maps", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "LoadMaps(); hideAll(evt);menuOpenFlag=false;");
	action = "LoadNodes";
	addMenuElement(RefreshChoices, "inline", 141, 41 , 80, 20,  action, "Load Nodes", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "LoadNodes(); hideAll(evt);menuOpenFlag=false;");
}

function instantiateRefreshGroupNormalMode() {
	action='Refresh';
	addMenuElement(menuSvgElement, "inline", 141, 0 , 70, 20,  action, "Refresh",  "elemColor(evt,'"+action+"',downColor)", "elemColor(evt, '"+action+"',upColor)", "openRefreshMenu(evt); hideSubMenus(evt);setMenuOpenFlag(evt, 'RefreshChoices');" );

	var RefreshChoices = createGroup(menuSvgElement, "RefreshChoices","none", "elemColor(evt,'"+action+"',downColor)","elemColor(evt,'"+action+"',upColor)");
	action = "RefreshMap";
	addMenuElement(RefreshChoices, "inline", 141, 21 , 80, 20,  action, "Refresh Map", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "RefreshNodes(); hideAll(evt);menuOpenFlag=false;");
}

function instantiateNodeGroup() {
	// *** NODE MENU ***
	action='Node';
	addMenuElement(menuSvgElement, "inline", 71, 0 , 70, 20,  action, "Node",  "elemColor(evt,'"+action+"',downColor)", "elemColor(evt, '"+action+"',upColor)", "openNodeMenu(evt); hideSubMenus(evt);setMenuOpenFlag(evt, 'NodeChoices');" );
	
	var NodeChoices = createGroup(menuSvgElement, "NodeChoices","none", "elemColor(evt,'"+action+"',downColor)","elemColor(evt,'"+action+"',upColor)");
	action = "Add";
	addMenuElement(NodeChoices, "inline", 71, 21 , 80, 20,  action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addMapElementList() ; hideAll(evt);menuOpenFlag=false;");
	action = "AddByCategory";
	addMenuElement(NodeChoices, "inline", 71, 41 , 80, 20,  action, "Add By Category", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addCategoryList();hideAll(evt);menuOpenFlag=false;");
	action = "AddByLabel";
	addMenuElement(NodeChoices, "inline", 71, 61 , 80, 20,  action, "Add By Label", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addNodeLabelBox();hideAll(evt);menuOpenFlag=false;");
	action = "AddRange";
	addMenuElement(NodeChoices, "inline", 71, 81 , 80, 20,  action, "Add Range", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addRangeBox();hideAll(evt);menuOpenFlag=false;");
	action = "AddNeigh";
	addMenuElement(NodeChoices, "inline", 71, 101 , 80, 20,  action, "Add Neighbors", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addMapElementNeigh(); hideAll(evt);menuOpenFlag=false;");
	action = "AddNodeNeigh";
	addMenuElement(NodeChoices, "inline", 71, 121 , 80, 20,  action, "Add With Neighbors", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addMapElementNeighList();hideAll(evt);menuOpenFlag=false;");
	action = "AddMap";
	addMenuElement(NodeChoices, "inline", 71, 141 , 80, 20,  action, "Add Map As Node", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addMapAsNodeList(); hideAll(evt);menuOpenFlag=false;");
	action = "SetIcon";
	addMenuElement(NodeChoices, "inline", 71, 161 , 80, 20,  action, "Set Icon", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addIconList();hideAll(evt);menuOpenFlag=false;" );
	action = "DelNode";
	addMenuElement(NodeChoices, "inline", 71, 181 , 80, 20,  action, "Delete", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "deleteMapElementMenu();hideAll(evt);menuOpenFlag=false;"  );
}

function instantiateViewGroup() {

	// *** VIEW MENU ***
	action='View';
	addMenuElement(menuSvgElement, "inline", 71, 0 , 70, 21,  action, "View",  "elemColor(evt,'"+action+"',downColor)", "elemColor(evt, '"+action+"',upColor); hideSubMenus(evt);", "openViewMenu(evt); hideSubMenus(evt);setMenuOpenFlag(evt, 'ViewChoices');" );
	
	var ViewChoices = createGroup(menuSvgElement, "ViewChoices","none", "elemColor(evt,'"+action+"',downColor)","elemColor(evt,'"+action+"',upColor)");
	
	action="ToggleFullScreen";
	addMenuElement(ViewChoices, "inline", 71, 21 , 80, 20, action, "Toggle Screen", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);" , "elemColor(evt,'"+action+"',upColor)","top.toggle(\"footer\"); top.toggle(\"header\");hideAll(evt);menuOpenFlag=false;");
	action = "SetDimension";	
	addMenuElement(ViewChoices, "inline", 71, 41 , 80, 20,  action, "Set Dimension", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addDimensionList();hideAll(evt);menuOpenFlag=false;");
	action="ColorNodesBy";
	addMenuElement(ViewChoices, "inline", 71, 61 , 80, 20, action, "View by...", "elemColor(evt,'"+action+"',downColor); elemShow(evt,'ColorNodesBySeverityGroup'); elemShow(evt,'ColorNodesByAvailGroup'); elemShow(evt,'ColorNodesByStatusGroup')" , "elemColor(evt,'"+action+"',upColor)","");
	action="ColorNodesBySeverity";
	addMenuElement(ViewChoices, "none", 130, 61 , 80, 20, action, "Severity", "elemColor(evt,'"+action+"',downColor);", "elemColor(evt,'"+action+"',upColor)", "colorSemaphoreBy='S';refreshMapElements(); addLegend();hideAll(evt);menuOpenFlag=false;clearTopInfo();clearDownInfo();");
	action="ColorNodesByAvail";	
	addMenuElement(ViewChoices, "none", 130, 81 , 80, 20, action, "Availability", "elemColor(evt,'"+action+"',downColor);", "elemColor(evt,'"+action+"',upColor)", "colorSemaphoreBy='A';refreshMapElements(); addLegend();hideAll(evt);menuOpenFlag=false;" );
	action="ColorNodesByStatus";	
	addMenuElement(ViewChoices, "none", 130, 101 , 80, 20, action, "Status", "elemColor(evt,'"+action+"',downColor);", "elemColor(evt,'"+action+"',upColor)", "colorSemaphoreBy='T';refreshMapElements(); addLegend();hideAll(evt);menuOpenFlag=false;");
	
}

function instantiateRWAdminMenu(){
	removeMenuChilds();
	instantiateMapGroupAdminMode();
	instantiateNodeGroup();	
	instantiateRefreshGroupAdminMode();
}
 
function instantiateRWNormalMenu(){
	removeMenuChilds();
	instantiateMapGroupNormalMode();
	instantiateViewGroup();
	instantiateRefreshGroupNormalMode();
}

function instantiateROMenu(){
	removeMenuChilds();
	instantiateMapGroup();	
	instantiateViewGroup();
}

// functions called by Menu or Refresh

function addMapElementList()
{
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	clearTopInfo();
	clearDownInfo();
	resetFlags();

	selNodes = new selectionList("nodes","nodes",nodes,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,mynodesResult);
    selNodes.sortList("asc");    
	button1  = new button("button1","nodes",addMapElement,"rect","Add",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
}

function addCategoryList()
{
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	selCategories = new selectionList("categories","categories",categories,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,mycategoriesResult);
	selCategories.sortList("asc");
	button1  = new button("button1","categories",addNodesByCategory,"rect","Add",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
}

function addMapAsNodeList(){
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	selMaps = new selectionList("maps","maps",maps,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,mymapsResult);
	selMaps.sortList("asc");
	button1  = new button("button1","maps",addMapAsNode,"rect","Add",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
}

function addMapsList()
{
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	selMaps = new selectionList("maps","maps",maps,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,mymapsResult);
	selMaps.sortList("asc");
	button1  = new button("button1","maps",openMap,"rect","Open",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
}


function addMapElementNeigh()
{
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	if( map.mapElements==null || map.mapElementSize==0)
	{
		alert('Map contains no nodes');
		return;
	}	

	clearTopInfo();
	resetFlags();
	addingMapElemNeighbors=true;
	writeDownInfo("Select the element to add");
}

function addMapElementNeighList()
{
	
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	
	selNodes = new selectionList("nodes","nodes",nodes,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,mynodesResult);
	selNodes.sortList("asc");
	button1  = new button("button1","nodes",addMapElementWithNeighbors,"rect","Add",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
	
}
 
function addIconList()
{
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	if( map.mapElements==null || map.mapElementSize==0)
	{
		alert('Map contains no nodes');
		return;
	}
	clearTopInfo();
	clearDownInfo();
	resetFlags();

	selMEIcons = new selectionList("meicons","meicons",MEIcons,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,myMEIconsResult);
	selMEIcons.sortList("asc");
} 

function addBGImagesList()
{
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	clearTopInfo();
	clearDownInfo();
	resetFlags();

	selBGImages = new selectionList("bgimages","bgimages",BGImages,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,myBGImagesResult);	
	selBGImages.sortList("asc");
	button1  = new button("button1","bgimages",setBGImage,"rect","Set Icon",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
} 
 
function addDimensionList()
{

	clearTopInfo();
	clearDownInfo();
	resetFlags();

	selMapElemDim = new selectionList("mapelemdim","mapelemdim",MapElemDim,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,1,false,true,myMapElemDimResult);	
	selMapElemDim.sortList("asc");
	button1  = new button("button1","mapelemdim",setMapElemDim,"rect","Set Dimension",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
} 

function addRenameMapBox(){
	if(currentMapId!=MAP_NOT_OPENED){
		clearTopInfo();
		clearDownInfo();
		resetFlags();

		//first a few styling parameters:
		textbox1 = new textbox("textbox1","textboxwithcommand",currentMapName,textboxmaxChars,textboxx,textboxy,textboxWidth,textboxHeight,textYOffset,textStyles,boxStyles,cursorStyles,seltextBoxStyles,"[a-zA-Z0-9 ]",undefined);
		button1  = new button("button1","textboxwithcommand",renameMap,"rect","Rename",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);        
 	}else{
		alert('No maps opened');
    }
}

function addRangeBox(){
	if(currentMapId!=MAP_NOT_OPENED){
		clearTopInfo();
		clearDownInfo();
		resetFlags();

		textbox1 = new textbox("textbox1","textboxwithcommand","*.*.*.*",textboxmaxChars,textboxx,textboxy,textboxWidth,textboxHeight,textYOffset,textStyles,boxStyles,cursorStyles,seltextBoxStyles,"[^a-zA-Z ]",undefined);
		button1  = new button("button1","textboxwithcommand",addRangeOfNodes,"rect","Add Range",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);        
		writeDownInfo3("Ip range valid are:","192.168.*.*","192.168.10-20.0-255");
 	}else{
		alert('No maps opened');
    }
}

function addNodeLabelBox(){
	if(currentMapId!=MAP_NOT_OPENED){
		clearTopInfo();
		clearDownInfo();
		resetFlags();

		textbox1 = new textbox("textbox1","textboxwithcommand","",textboxmaxChars,textboxx,textboxy,textboxWidth,textboxHeight,textYOffset,textStyles,boxStyles,cursorStyles,seltextBoxStyles,"[^]",undefined);
		button1  = new button("button1","textboxwithcommand",addNodesByLabel,"rect","Add",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);        
		writeDownInfo("Label text accept wildcard");
 	}else{
		alert('No maps opened');
    }
}

function deleteMapElementMenu()
{
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}

	if( map.mapElements==null || map.mapElementSize==0)
	{
		alert('Map contains no nodes');
		return;
	}	
	clearTopInfo();
	resetFlags();
	deletingMapElem=true;

	writeDownInfo("Select the element to delete");
}

function addRefreshTimeList()
{
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	selRefreshTimeMins = new selectionList("refreshTime","refreshTime",refreshTimeMins,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,myRefreshTimeMinsResult);	
	button1  = new button("button1","refreshTime",resetRefreshTimer,"rect","Set",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);        
}

function setBackground(){
	if(currentMapId!=MAP_NOT_OPENED){
		add_pick_color("pickColor");
	}else{
		alert('No maps opened');
    }
}
// Top Info writing
function writeTopInfo(svgObject) {

	clearTopInfo();
	if (svgObject != null) {
		document.getElementById("TopInfo").appendChild(svgObject);
	}
}

//clear the TopInfo SVG Object
function clearTopInfo(){

	if(selRefreshTimeMins!=null){
		if(selRefreshTimeMins.exists==true){
			selRefreshTimeMins.removeSelectionList();
			selRefreshTimeMins=0;
		}
	}	
	
	if(selNodes!=null){
		if(selNodes.exists==true){
			selNodes.removeSelectionList();
			selectedMapElemInList=0;
		}
	}

	if(selMaps!=null){
		if(selMaps.exists==true){
			selMaps.removeSelectionList();
			selectedMapInList=0;
		}
	}
	
	if(selCategories!=null){
		if(selCategories.exists==true){
			selCategories.removeSelectionList();
			selectedCategoryInList=0;
		}
	}

	if(selBGImages!=null){
		if(selBGImages.exists==true){
			map.render();
			selBGImages.removeSelectionList();
			selectedBGImageInList=0;
		}
	}	
	
	if(selMapElemDim!=null){
		if(selMapElemDim.exists==true){
			selMapElemDim.removeSelectionList();
			selectedMapElemDimInList="15 pixel width";
		}
	}			

	if(selMEIcons!=null){
		if(selMEIcons.exists==true){
			selMEIcons.removeSelectionList();
			selectedselMEIconInList=0;
		}
	}			

	if(textbox1!= null) {
		textbox1.removeTextbox();
		textbox1 = null;
	}
	
	if(button1!= null) {
		button1.removeButton();
		button1 = null;
	}
	
	var childNode = document.getElementById("TopInfoText");
	if (childNode)
		document.getElementById("TopInfo").removeChild(childNode);

} 

// Function to manage DownInfo Box

function clearDownInfo(){
	var childNode = document.getElementById("DownInfoText");
	if (childNode)
		childNode.parentNode.removeChild(childNode);
}

function writeDownInfo(info){
	clearDownInfo();

	var text = document.createElementNS(svgNS,"text");
	text.setAttributeNS(null, "x","5");
	text.setAttributeNS(null, "y","20");
	text.setAttributeNS(null, "font-size",textFontSize);
	text.setAttributeNS(null, "id","DownInfoText");
	
	var tspanContent = document.createTextNode(info);
	text.appendChild(tspanContent);		
	
	document.getElementById("DownInfo").appendChild(text);		
}

function writeDownInfo2(info1, info2){
	clearDownInfo();

	var text = document.createElementNS(svgNS,"text");
	text.setAttributeNS(null, "x","5");
	text.setAttributeNS(null, "y","20");
	text.setAttributeNS(null, "id","DownInfoText");
	text.setAttributeNS(null, "font-size",titleFontSize);
	text.appendChild(document.createTextNode(info1));
	
	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","7");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "font-size",titleFontSize);
	var tspanContent = document.createTextNode(info2);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);		
	
	document.getElementById("DownInfo").appendChild(text);		
}

function writeDownInfo3(info1, info2, info3){
	clearDownInfo();

	var text = document.createElementNS(svgNS,"text");
	text.setAttributeNS(null, "x","5");
	text.setAttributeNS(null, "y","20");
	text.setAttributeNS(null, "id","DownInfoText");
	text.setAttributeNS(null, "font-size",titleFontSize);
	text.appendChild(document.createTextNode(info1));
	
	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","7");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "font-size",titleFontSize);
	var tspanContent = document.createTextNode(info2);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);		
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","7");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "font-size",titleFontSize);
	var tspanContent = document.createTextNode(info3);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);		

	document.getElementById("DownInfo").appendChild(text);						
}

// Manage MapInfo SVG Objects

function viewMapInfo(){
	
	var mapInfo= document.getElementById("MapInfo");
	
	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","30");
	tspan.setAttributeNS(null, "id","mapName");
	tspan.setAttributeNS(null, "font-size",textFontSize);
	
	var tspanContent = document.createTextNode("Name: "+currentMapName+" ");
	tspan.appendChild(tspanContent);
	mapInfo.appendChild(tspan);

	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","20");
	tspan.setAttributeNS(null, "id","mapOwner");
	tspan.setAttributeNS(null, "font-size",textFontSize);
	
	var tspanContent = document.createTextNode("Owner: "+currentMapOwner+" ");
	tspan.appendChild(tspanContent);
	mapInfo.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","20");
	tspan.setAttributeNS(null, "id","mapUserLast");
	tspan.setAttributeNS(null, "font-size",textFontSize);
	
	var tspanContent = document.createTextNode("User last modified: "+currentMapUserlast+" ");
	tspan.appendChild(tspanContent);
	mapInfo.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","20");
	tspan.setAttributeNS(null, "id","mapCreateTime");
	tspan.setAttributeNS(null, "font-size",textFontSize);
	
	var tspanContent = document.createTextNode("Create time: "+currentMapCreatetime+" ");
	tspan.appendChild(tspanContent);
	mapInfo.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","20");
	tspan.setAttributeNS(null, "id","mapLastModTime");
	tspan.setAttributeNS(null, "font-size",textFontSize);
	
	var tspanContent = document.createTextNode("Last modified time: "+currentMapLastmodtime+" ");
	tspan.appendChild(tspanContent);
	mapInfo.appendChild(tspan);
	
	mapInfo.setAttributeNS(null,'display', 'inline');
	if(mapHistory.length>mapHistoryIndex+1){
		var next = mapHistory[mapHistoryIndex+1];
		var nextName = mapHistoryName[mapHistoryIndex+1]; 
		//alert("nextMap id="+next+" nextName="+nextName);
		var textContent = document.createTextNode(nextName);
		var nextText = document.getElementById("nextMapName");
		if(nextText.firstChild)
			nextText.removeChild(nextText.firstChild);		
		nextText.appendChild(textContent);
		var nextAction = document.getElementById("nextAction");
		nextAction.setAttribute("onclick","openMap("+next+");");
		document.getElementById("nextGroup").setAttributeNS(null,'display', 'inline');
	}else{
	    document.getElementById("nextGroup").setAttributeNS(null,'display', 'none');
	}
	
	if(mapHistoryIndex>0){
		var prev = mapHistory[mapHistoryIndex-1];
		var prevName = mapHistoryName[mapHistoryIndex-1]; 
		var textContent = document.createTextNode(prevName);
		var prevText = document.getElementById("prevMapName");
		if(prevText.firstChild)
			prevText.removeChild(prevText.firstChild);
		prevText.appendChild(textContent);
		var prevAction = document.getElementById("prevAction");
		prevAction.setAttribute("onclick","openMap("+prev+");");		
		document.getElementById("prevGroup").setAttributeNS(null,'display', 'inline');
	}else{
	    document.getElementById("prevGroup").setAttributeNS(null,'display', 'none');
	}	
	
}

function clearMapInfo(){
	var mapInfo= document.getElementById("MapInfo");
	var mapNameNode=document.getElementById("mapName");
	if(mapNameNode!=null)
		mapNameNode.parentNode.removeChild(mapNameNode);
	var mapOwnerNode=document.getElementById("mapOwner");
	if(mapOwnerNode!=null)
		mapOwnerNode.parentNode.removeChild(mapOwnerNode);	
	var mapUserLastNode=document.getElementById("mapUserLast");
	if(mapUserLastNode!=null)
		mapUserLastNode.parentNode.removeChild(mapUserLastNode);	
	var mapCreateTimeNode=document.getElementById("mapCreateTime");
	if(mapCreateTimeNode!=null)
		mapCreateTimeNode.parentNode.removeChild(mapCreateTimeNode);		
	var mapLastModTimeNode=document.getElementById("mapLastModTime");
	if(mapLastModTimeNode!=null)
		mapLastModTimeNode.parentNode.removeChild(mapLastModTimeNode);		
}		

function hideMapInfo(){
	var mapInfoElem = document.getElementById("MapInfo");
	if(mapInfoElem!=null)
		mapInfoElem.setAttributeNS(null,'display', 'none');
}

//Assert loading..
function assertLoading(){
	var lt = document.getElementById("LoadingText");
	if(loading==0){
		lt.setAttributeNS(null,'display', 'none');
		enableMenu();
	}else{
		lt.setAttributeNS(null,'display', 'inline');
	}
}

//Assert Refreshing....

function assertRefreshing(loading){
	var lt = document.getElementById("RefreshingText");
	if(loading==0){
		lt.setAttributeNS(null,'display', 'none');
		enableMenu();
	}else{
		lt.setAttributeNS(null,'display', 'inline');
	}
}

function showHistory(){
	document.getElementById("history").setAttributeNS(null,'display', 'inline');
}

function hideHistory(){
	document.getElementById("history").setAttributeNS(null,'display', 'none');
}

// Display countDownText function
function displayCountDown(text, editTime){
	var textEl = document.getElementById("countDownText");
	
	if(textEl.firstChild){
		textEl.removeChild(textEl.firstChild);
	}

	if(editTime){
		textEl.setAttributeNS(null, "cursor","pointer");
		textEl.addEventListener("click", addRefreshTimeList, false);
	}else{
		textEl.setAttributeNS(null, "cursor","default");
		textEl.removeEventListener("click",addRefreshTimeList,false);
	}

	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "dy","12");
	tspan.setAttributeNS(null, "fill","white");
	tspan.setAttributeNS(null, "font-size",textFontSize);
	var tspanContent = document.createTextNode(text);
	tspan.appendChild(tspanContent);
	textEl.appendChild(tspan);
}

// Legend Svg Object Methods

function removeLegend() {
	var legendSVG = document.getElementById("legend");
	var obj, ls;
	ls = legendSVG.childNodes;
	while (ls.length > 0) {
	  obj = ls.item(0);
	  legendSVG.removeChild(obj);
	}	
}

function addLegend() {
	var legendSVG = document.getElementById("legend");
	
	var legendHeight=legendSVG.getAttributeNS(null,"height");
	
	var x = 1;
	var y = 15;

	var cx = 10;
	var cy = 23;
	var cr = 5;
	var dx = 0;
	var dy = 2*cr+1;
	var fontsize=dy-1;
	var ddx = 10;
	var ddy = 3;
	
	var legendSpace = legendHeight-y-2*fontsize;

	if (legendSVG.firstChild != undefined) {
		legendSVG.removeChild(legendSVG.firstChild);
	}

	var legendGroup = document.createElementNS(svgNS,"g");

	var lgtext = document.createElementNS(svgNS,"text");
	lgtext.setAttributeNS(null,"x", x);
	lgtext.setAttributeNS(null,"y",y);
	lgtext.setAttributeNS(null,"font-size",fontsize+1);
	
	var contentText = document.createTextNode("Severity View");

	if ( colorSemaphoreBy == "A") {
		contentText = document.createTextNode("Availability View");
		var countElem = 0;
		for(var index in AVAIL_MIN) {
			if(AVAIL_MIN[index]!=undefined){
				countElem++;
			}
		}
		var newcr = parseInt((legendSpace/countElem)/2); //divided by 2 because is r of a circle
		if(newcr<=cr){
			cr=newcr;
			dy = 2*cr+1;
			fontsize=dy-1;
		}

		for(var index in AVAIL_MIN) {

			var label = "";
			if (AVAIL_MIN[index] < 0) {
				label = "Unknown";
			} else {
				label =  " Better " + AVAIL_MIN[index] +"%";
			}

			var item = document.createElementNS(svgNS,"circle");
			item.setAttributeNS(null,"cx",cx);
			item.setAttributeNS(null,"cy",cy);
			item.setAttributeNS(null,"r",cr);
			item.setAttributeNS(null,"fill",AVAIL_COLOR[index]);
			item.setAttributeNS(null,"stroke","black");
			legendGroup.appendChild(item);

			var textel = document.createElementNS(svgNS,"text");
			textel.setAttributeNS(null,"id", label+"Text");
			textel.setAttributeNS(null,"font-size",fontsize);
			textel.setAttributeNS(null,"x", cx+ddx);
			textel.setAttributeNS(null,"y",cy+ddy);
			var labelText = document.createTextNode(label);
			textel.appendChild(labelText);
			legendGroup.appendChild(textel);
			cx = cx+dx;
			cy = cy+dy;
		}
	} else if (colorSemaphoreBy == "T") {
		contentText = document.createTextNode("Status View");
		var countElem = 0;
		for(var index in STATUSES_UEI) {
			if(STATUSES_UEI[index]!=undefined){
				countElem++;
			}
		}
		var newcr = parseInt((legendSpace/countElem)/2); //divided by 2 because is r of a circle
		if(newcr<=cr){
			cr=newcr;
			dy = 2*cr+1;
			fontsize=dy-1;
		}
		
		for(var index in STATUSES_UEI) {

			var item = document.createElementNS(svgNS,"circle");
			item.setAttributeNS(null,"cx",cx);
			item.setAttributeNS(null,"cy",cy);
			item.setAttributeNS(null,"r",cr);
			item.setAttributeNS(null,"fill",STATUSES_COLOR[index]);
			item.setAttributeNS(null,"stroke","black");
			legendGroup.appendChild(item);

			var textel = document.createElementNS(svgNS,"text");
			textel.setAttributeNS(null,"font-size",fontsize);
			textel.setAttributeNS(null,"x", cx+ddx);
			textel.setAttributeNS(null,"y",cy+ddy);
			var labelText = document.createTextNode(STATUSES_TEXT[index]);
			textel.appendChild(labelText);
			legendGroup.appendChild(textel);
			cx = cx+dx;
			cy = cy+dy;
		}
	} else {
	
		var countElem = 0;
		for(var index in SEVERITIES_LABEL) {
			if(SEVERITIES_LABEL[index]!=undefined){
				countElem++;
			}
		}
		var newcr = parseInt((legendSpace/countElem)/2); //divided by 2 because is r of a circle
		if(newcr<=cr){
			cr=newcr;
			dy = 2*cr+1;
			fontsize=dy-1;
		}	
		for(var index in SEVERITIES_LABEL) {

			var item = document.createElementNS(svgNS,"circle");
			item.setAttributeNS(null,"cx",cx);
			item.setAttributeNS(null,"cy",cy);
			item.setAttributeNS(null,"r",cr);
			item.setAttributeNS(null,"fill",SEVERITIES_COLOR[index]);
			item.setAttributeNS(null,"stroke","black");
			legendGroup.appendChild(item);

			var textel = document.createElementNS(svgNS,"text");
			textel.setAttributeNS(null,"font-size",fontsize);
			textel.setAttributeNS(null,"x", cx+ddx);
			textel.setAttributeNS(null,"y",cy+ddy);
			var labelText = document.createTextNode(SEVERITIES_LABEL[index]);
			textel.appendChild(labelText);
			legendGroup.appendChild(textel);
			cx = cx+dx;
			cy = cy+dy;
	    }
		
	}
	lgtext.appendChild(contentText);
	legendGroup.appendChild(lgtext);
	legendSVG.appendChild(legendGroup);
	
}

function refreshTimeMinsResult() {selectedRefreshTimeList="30 seconds"; }

refreshTimeMinsResult.prototype.getSelectionListVal = function(selBoxName,dimNr,arrayVal) {
	if(arrayVal) selectedRefreshTimeList=arrayVal;
	else selectedRefreshTimeList="30 seconds";
}

function categoriesResult() { }

categoriesResult.prototype.getSelectionListVal = function(selBoxName,nodeNr,arrayVal) {
        selectedCategoryInList=arrayVal;
}

function nodesResult() { }

nodesResult.prototype.getSelectionListVal = function(selBoxName,nodeNr,arrayVal) {
	if(nodeNr!=0){
			clearDownInfo();
	        document.getElementById("DownInfo").appendChild(nodeSortAss[arrayVal].getInfo()); 
	}
	selectedMapElemInList=arrayVal;
}

function mapsResult() { }

mapsResult.prototype.getSelectionListVal = function(selBoxName,mapNr,arrayVal) {
	if(mapNr!=0){
		clearDownInfo();
	    document.getElementById("DownInfo").appendChild(mapSortAss[arrayVal].getInfo()); 
	}
	selectedMapInList=arrayVal;
}

function BGImagesResult() { }

BGImagesResult.prototype.getSelectionListVal = function(selBoxName,mapNr,arrayVal) {
    if(mapNr!=0){
            map.tryBackgroundImage(BGImagesSortAss[arrayVal]);
    }
    selectedBGImageInList=arrayVal;
}

function MapElemDimResult() { }

MapElemDimResult.prototype.getSelectionListVal = function(selBoxName,dimNr,arrayVal) {
	selectedMapElemDimInList=arrayVal;
}

function MEIconsResult() { }

MEIconsResult.prototype.getSelectionListVal = function(selBoxName,mapNr,arrayVal) {
	
	var iconPreviewNode = document.getElementById("iconPreview");
	if (iconPreviewNode)
       iconPreviewNode.parentNode.removeChild(iconPreviewNode);

    if(mapNr!=0){
        settingMapElemIcon=true;
        selectedMEIconInList=arrayVal;

        var iconPreviewGroup = document.createElementNS(svgNS,"g");
        iconPreviewGroup.setAttributeNS(null,"id", "iconPreview");

        var iconPreviewRect = document.createElementNS(svgNS,"rect");
        iconPreviewRect.setAttributeNS(null,"x", 57);
        iconPreviewRect.setAttributeNS(null,"y", 80);
        iconPreviewRect.setAttributeNS(null,"width", 35);
        iconPreviewRect.setAttributeNS(null,"height", 35);
        iconPreviewRect.setAttributeNS(null,"fill", "white");
        iconPreviewRect.setAttributeNS(null,"stroke", "grey");
        iconPreviewRect.setAttributeNS(null,"stroke-width", 1);

        var iconPreview = document.createElementNS(svgNS,"image");
        iconPreview.setAttributeNS(null,"x", 65);
        iconPreview.setAttributeNS(null,"y", 87);
        iconPreview.setAttributeNS(null,"width", 20);
        iconPreview.setAttributeNS(null,"height", 25);
        iconPreview.setAttributeNS(xlinkNS, "xlink:href",MEIconsSortAss[arrayVal] );
        iconPreviewGroup.appendChild(iconPreviewRect);
        iconPreviewGroup.appendChild(iconPreview);
        selMEIcons.selectionBoxGroup.appendChild(iconPreviewGroup);

        writeDownInfo2("Click on an element to set","icon.");
	} else {
		settingMapElemIcon=false;
    }
}


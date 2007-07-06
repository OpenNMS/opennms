


//////////////////////////////
// Declare Global Variables //
//////////////////////////////
var toggleState = "closed"

//say if the menu is been opened or not
var menuOpenFlag=false;

//color of the component of the menu
var downColor = "blue";
var upColor =  "black";


function removeMenuChilds(){
	var menu = menuSvgDocument.getElementById("Menu");
	var obj, ls;
	ls = menu.childNodes;
	while (ls.length > 0) {
	  obj = ls.item(0);
	  menu.removeChild(obj);
	}
}
function instantiateRWAdminMenu(){
	removeMenuChilds();
	var RWAdminMenu = menuSvgDocument.getElementById("Menu");
	

	// *** MAP MENU ***
	addMenuElement(RWAdminMenu, "inline", 0, 0 , 50, 20,  "Map", "Map",  "openMapMenu(evt); hideSubMenus(evt);", "elemColor(evt, 'Map',upColor)", "setMenuOpenFlag(evt, 'Map');" );
	var MapChoices = createGroup(RWAdminMenu, "MapChoices","none", "elemColor(evt,'Map',downColor)","elemColor(evt,'Map',upColor)");
	var action = "New";
	addMenuElement(MapChoices, "inline", 0, 21 , 100, 20,  action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "newMap();hideAll(evt);menuOpenFlag=false;");
	action="Open";
	addMenuElement(MapChoices, "inline", 0, 41 , 100, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "	if(maps!=null && maps.length>0) {addMapsList();}else{LoadMaps(true,false);}  hideAll(evt);menuOpenFlag=false;");
	action="Close";
	addMenuElement(MapChoices, "inline", 0, 61 , 100, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "close();hideAll(evt);menuOpenFlag=false;");
	action="Rename";
	addMenuElement(MapChoices, "inline", 0, 81 , 100, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addRenameMapBox(); hideAll(evt);menuOpenFlag=false;");
	action="Delete";
	addMenuElement(MapChoices, "inline", 0, 101 , 100, 20, action, action,  "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "deleteMap();hideAll(evt);menuOpenFlag=false;clearTopInfo();clearDownInfo();");
	action="Save";
	addMenuElement(MapChoices, "inline", 0, 121 , 100, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "saveMap();hideAll(evt);menuOpenFlag=false;");
	action="Clear";
	addMenuElement(MapChoices, "inline", 0, 141 , 100, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "clearMap();hideAll(evt);menuOpenFlag=false;clearTopInfo();clearDownInfo();");
	action="RefreshMode";
	addMenuElement(MapChoices, "inline", 0, 181 , 100, 20, action, "Refresh Mode", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "switchToNormalMode();hideAll(evt);menuOpenFlag=false;");
	action="SetBG";
	addMenuElement(MapChoices, "inline", 0, 161 , 100, 20, action, "Set Background...", "elemColor(evt,'"+action+"',downColor); elemShow(evt,'SetBGColorGroup'); elemShow(evt,'SetBGImageGroup')" , "elemColor(evt,'"+action+"',upColor)", "elemShow(evt,'SetBGColor'); elemShow(evt,'SetBGImage')");
	action="SetBGColor";
	addMenuElement(MapChoices, "none", 96, 160 , 100, 20, action, "Color", "elemColor(evt,'"+action+"',downColor);", "elemColor(evt,'"+action+"',upColor)", "setBackground(0);  hideAll(evt);menuOpenFlag=false;clearTopInfo();clearDownInfo();");
	action="SetBGImage";
	addMenuElement(MapChoices, "none", 96, 180 , 100, 20, action, "Image", "elemColor(evt,'"+action+"',downColor);", "elemColor(evt,'"+action+"',upColor)", "addBGImagesList();hideAll(evt);menuOpenFlag=false;");

	// *** NODE MENU ***
	action='Node';
	addMenuElement(RWAdminMenu, "inline", 51, 0 , 50, 20,  action, action,  "openNodeMenu(evt); hideSubMenus(evt);", "elemColor(evt, '"+action+"',upColor)", "setMenuOpenFlag(evt, '"+action+"');" );
	var NodeChoices = createGroup(RWAdminMenu, "NodeChoices","none", "elemColor(evt,'"+action+"',downColor)","elemColor(evt,'"+action+"',upColor)");
	action = "Add";
	addMenuElement(NodeChoices, "inline", 51, 21 , 120, 20,  action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addNodeLabelFilterBox('NodeLabelFilterBox');hideAll(evt);menuOpenFlag=false;");
	action = "AddByCategory";
	addMenuElement(NodeChoices, "inline", 51, 41 , 120, 20,  action, "Add By Category", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addCategoryList();hideAll(evt);menuOpenFlag=false;");
	action = "AddByLabel";
	addMenuElement(NodeChoices, "inline", 51, 61 , 120, 20,  action, "Add By Label", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addNodeLabelBox();hideAll(evt);menuOpenFlag=false;");
	action = "AddRange";
	addMenuElement(NodeChoices, "inline", 51, 81 , 120, 20,  action, "Add Range", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addRangeBox();hideAll(evt);menuOpenFlag=false;");
	action = "AddNeigh";
	addMenuElement(NodeChoices, "inline", 51, 101 , 120, 20,  action, "Add Neighbors", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addMapElementNeigh(); hideAll(evt);menuOpenFlag=false;");
	action = "AddNodeNeigh";
	addMenuElement(NodeChoices, "inline", 51, 121 , 120, 20,  action, "Add With Neighbors", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addNodeLabelFilterBox('NodeWithNeighLabelFilterBox');hideAll(evt);menuOpenFlag=false;");
	action = "AddMap";
	addMenuElement(NodeChoices, "inline", 51, 141 , 120, 20,  action, "Add Map As Node", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "if(maps!=null && maps.length>0) {addMapAsNodeList();}else{LoadMaps(false,true);} hideAll(evt);menuOpenFlag=false;");
	action = "SetIcon";
	addMenuElement(NodeChoices, "inline", 51, 161 , 120, 20,  action, "Set Icon", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addIconList();hideAll(evt);menuOpenFlag=false;" );
	action = "DelNode";
	addMenuElement(NodeChoices, "inline", 51, 181 , 120, 20,  action, "Delete", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "deleteMapElementMenu();hideAll(evt);menuOpenFlag=false;"  );


}


function instantiateRWNormalMenu(){
	removeMenuChilds();
	var RWNormalMenu = menuSvgDocument.getElementById("Menu");
	
	// *** MAP MENU ***
	addMenuElement(RWNormalMenu, "inline", 0, 0 , 50, 20,  "Map", "Map",  "openMapMenu(evt); hideSubMenus(evt);", "elemColor(evt, 'Map',upColor)", "setMenuOpenFlag(evt, 'Map');" );
	var MapChoices = createGroup(RWNormalMenu, "MapChoices","none", "elemColor(evt,'Map',downColor)","elemColor(evt,'Map',upColor)");
	action="Open";
	addMenuElement(MapChoices, "inline", 0, 21 , 100, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "if(maps!=null && maps.length>0) {addMapsList();}else{LoadMaps(true,false);} hideAll(evt);menuOpenFlag=false;");
	action="Close";
	addMenuElement(MapChoices, "inline", 0, 41 , 100, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "close();hideAll(evt);menuOpenFlag=false;");
	action="AdminMode";
	addMenuElement(MapChoices, "inline", 0, 61 , 100, 20, action, "Admin Mode", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "switchToAdminMode();hideAll(evt);menuOpenFlag=false;");
	
	// *** VIEW MENU ***
	action='View';
	addMenuElement(RWNormalMenu, "inline", 51, 0 , 50, 20,  action, action,  "openViewMenu(evt); hideSubMenus(evt);", "elemColor(evt, '"+action+"',upColor); hideSubMenus(evt);", "setMenuOpenFlag(evt, '"+action+"');" );
	var ViewChoices = createGroup(RWNormalMenu, "ViewChoices","none", "elemColor(evt,'"+action+"',downColor)","elemColor(evt,'"+action+"',upColor)");
	action = "SetDimension";
	addMenuElement(ViewChoices, "inline", 51, 21 , 100, 20,  action, "Set Dimension", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addDimensionList();hideAll(evt);menuOpenFlag=false;");
	action="ColorNodesBy";
	addMenuElement(ViewChoices, "inline", 51, 41 , 100, 20, action, "View by...", "elemColor(evt,'"+action+"',downColor); elemShow(evt,'ColorNodesBySeverityGroup'); elemShow(evt,'ColorNodesByAvailGroup'); elemShow(evt,'ColorNodesByStatusGroup')" , "elemColor(evt,'"+action+"',upColor)");
	action="ColorNodesBySeverity";
	addMenuElement(ViewChoices, "none", 120, 41 , 95, 20, action, "Severity", "elemColor(evt,'"+action+"',downColor);", "elemColor(evt,'"+action+"',upColor)", "colorSemaphoreBy='S';refreshMapElements(); addLegend();hideAll(evt);menuOpenFlag=false;clearTopInfo();clearDownInfo();");
	action="ColorNodesByAvail";
	addMenuElement(ViewChoices, "none", 120, 61 , 95, 20, action, "Availability", "elemColor(evt,'"+action+"',downColor);", "elemColor(evt,'"+action+"',upColor)", "colorSemaphoreBy='A';refreshMapElements(); addLegend();hideAll(evt);menuOpenFlag=false;" );
	action="ColorNodesByStatus";
	addMenuElement(ViewChoices, "none", 120, 81 , 95, 20, action, "Status", "elemColor(evt,'"+action+"',downColor);", "elemColor(evt,'"+action+"',upColor)", "colorSemaphoreBy='T';refreshMapElements(); addLegend();hideAll(evt);menuOpenFlag=false;");

}



function instantiateROMenu(){
	removeMenuChilds();
	var ROMenu = menuSvgDocument.getElementById("Menu");
	
	// *** MAP MENU ***
	addMenuElement(ROMenu, "inline", 0, 0 , 50, 20,  "Map", "Map",  "openMapMenu(evt); hideSubMenus(evt);", "elemColor(evt, 'Map',upColor)", "setMenuOpenFlag(evt, 'Map');" );
	var MapChoices = createGroup(ROMenu, "MapChoices","none", "elemColor(evt,'Map',downColor)","elemColor(evt,'Map',upColor)");
	action="Open";
	addMenuElement(MapChoices, "inline", 0, 21 , 100, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "if(maps!=null && maps.length>0) {addMapsList();}else{LoadMaps(true,false);} hideAll(evt);menuOpenFlag=false;");
	action="Close";
	addMenuElement(MapChoices, "inline", 0, 41 , 100, 20, action, action, "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "close();hideAll(evt);menuOpenFlag=false;");

	
	// *** VIEW MENU ***
	action='View';
	addMenuElement(ROMenu, "inline", 51, 0 , 50, 20,  action, action,  "openViewMenu(evt); hideSubMenus(evt);", "elemColor(evt, '"+action+"',upColor); hideSubMenus(evt);", "setMenuOpenFlag(evt, '"+action+"');" );
	var ViewChoices = createGroup(ROMenu, "ViewChoices","none", "elemColor(evt,'"+action+"',downColor)","elemColor(evt,'"+action+"',upColor)");
	action = "SetDimension";
	addMenuElement(ViewChoices, "inline", 51, 21 , 100, 20,  action, "Set Dimension", "elemColor(evt,'"+action+"',downColor); hideSubMenus(evt);", "elemColor(evt,'"+action+"',upColor)", "addDimensionList();hideAll(evt);menuOpenFlag=false;");
	action="ColorNodesBy";
	addMenuElement(ViewChoices, "inline", 51, 41 , 100, 20, action, "View by...", "elemColor(evt,'"+action+"',downColor); elemShow(evt,'ColorNodesBySeverityGroup'); elemShow(evt,'ColorNodesByAvailGroup'); elemShow(evt,'ColorNodesByStatusGroup')" , "elemColor(evt,'"+action+"',upColor)");
	action="ColorNodesBySeverity";
	addMenuElement(ViewChoices, "none", 120, 41 , 95, 20, action, "Severity", "elemColor(evt,'"+action+"',downColor);", "elemColor(evt,'"+action+"',upColor)", "colorSemaphoreBy='S';refreshMapElements(); addLegend();hideAll(evt);menuOpenFlag=false;clearTopInfo();clearDownInfo();");
	action="ColorNodesByAvail";
	addMenuElement(ViewChoices, "none", 120, 61 , 95, 20, action, "Availability", "elemColor(evt,'"+action+"',downColor);", "elemColor(evt,'"+action+"',upColor)", "colorSemaphoreBy='A';refreshMapElements(); addLegend();hideAll(evt);menuOpenFlag=false;" );
	action="ColorNodesByStatus";
	addMenuElement(ViewChoices, "none", 120, 81 , 95, 20, action, "Status", "elemColor(evt,'"+action+"',downColor);", "elemColor(evt,'"+action+"',upColor)", "colorSemaphoreBy='T';refreshMapElements(); addLegend();hideAll(evt);menuOpenFlag=false;");

}






function createGroup(parentNode, id, display, onmouseoverActions, onmouseoutActions){
	var group = menuSvgDocument.createElementNS(svgNS,"g");
	group.setAttributeNS(null,"display",display);
	group.setAttributeNS(null,"id",id);	
	group.setAttributeNS(null,"onmouseover",onmouseoverActions);	
	group.setAttributeNS(null,"onmouseout",onmouseoutActions);	
	parentNode.appendChild(group);
	return group;
}


function addMenuElement(parentNode, display, x, y , width, height, action, label, onmouseoverActions, onmouseoutActions, onclickActions){
	var Menu = menuSvgDocument.createElementNS(svgNS,"g");
	Menu.setAttributeNS(null,"display",display);
	Menu.setAttributeNS(null,"id",action+"Group");
	
	var Rect = menuSvgDocument.createElementNS(svgNS,"rect");
	Rect.setAttributeNS(null,"id", action);
	Rect.setAttributeNS(null,"x", x);
	Rect.setAttributeNS(null,"y", y);
	Rect.setAttributeNS(null,"width", width);
	Rect.setAttributeNS(null,"height", height);
	Rect.setAttributeNS(null,"stroke", "blue");
	Rect.setAttributeNS(null,"fill", "black");
	Rect.setAttributeNS(null,"stroke-width", 1);
	Menu.appendChild(Rect);
	
	var Text = menuSvgDocument.createElementNS(svgNS,"text");
	Text.setAttributeNS(null,"id", action+"Text");
	Text.setAttributeNS(null,"x", x+8);
	Text.setAttributeNS(null,"y", y+13);
	Text.setAttributeNS(null,"font-size", 11);
	Text.setAttributeNS(null,"fill", "white");
	ContentText = menuSvgDocument.createTextNode(label);
	Text.appendChild(ContentText);
	Menu.appendChild(Text);
	
	var MouseRect = menuSvgDocument.createElementNS(svgNS,"rect");
	MouseRect.setAttributeNS(null,"onmouseover", onmouseoverActions);
	MouseRect.setAttributeNS(null,"onmouseout" , onmouseoutActions);
	MouseRect.setAttributeNS(null,"onclick" , onclickActions);
	MouseRect.setAttributeNS(null,"x" , x );
	MouseRect.setAttributeNS(null,"y" , y );
	MouseRect.setAttributeNS(null,"width" , width );
	MouseRect.setAttributeNS(null,"height" , height );
	MouseRect.setAttributeNS(null,"style" , "fill:green;fill-opacity:0" );
	Menu.appendChild(MouseRect);

	parentNode.appendChild(Menu);
	return Menu;
}

// change the fill color of an element
function elemColor(mouseEvent, elemName, value) {
	// check for AI converted spaces
	elemName = spaceTrans(elemName)

	// get the element we want to change
	var thisElem = menuSvgDocument.getElementById(elemName).getStyle();

	// perform the fill color change
	thisElem.setProperty('fill', value)
}


// change the stroke width of an element
function elemStrokeWidth(mouseEvent, elemName, value) {


	// check for AI converted spaces
	elemName = spaceTrans(elemName)

	// get the element we want to change
	var thisElem = menuSvgDocument.getElementById(elemName).getStyle()
	
	// perform the stroke width change
	thisElem.setProperty('stroke-width', value)
}


// change the stroke color of an element
function elemStrokeColor(mouseEvent, elemName, value) {


	// check for AI converted spaces
	elemName = spaceTrans(elemName)

	// get the element we want to change
	var thisElem = menuSvgDocument.getElementById(elemName).getStyle()
	
	// perform the stroke color change
	thisElem.setProperty('stroke', value)
}


// show an element - note: these work with the display property, not visibility
function elemShow(mouseEvent, elemName) {
	// check for AI converted spaces
	elemName = spaceTrans(elemName)

	// get the element we want to change
	var thisElem = menuSvgDocument.getElementById(elemName).getStyle();
	
	// make the element visible
	thisElem.setProperty('display', 'inline')
}


// hide an element - note: these work with the display property, not visibility
function elemHide(mouseEvent, elemName) {


	// check for AI converted spaces
	elemName = spaceTrans(elemName)
	// get the element we want to change
	var elem = menuSvgDocument.getElementById(elemName);
	if(elem!=null){
		var thisElem = elem.getStyle()

		// hide the element
		thisElem.setProperty('display', 'none')
	}
}


// toggle between visible states (hidden and visible)
function toggle(mouseEvent, elemName) {
	if (toggleState == "closed") {
		elemShow(mouseEvent, elemName)
		toggleState = "open"
	} else {
		elemHide(mouseEvent, elemName)
		toggleState = "closed"
	}
}


// translate spaces into equivalent AI exported space string
function spaceTrans(stringIn) {
	var result = ""
	for (var i = 0; i < stringIn.length; i++) {
		if (stringIn.charAt(i) == " ") {
			result += "_x0020_"
		} else {
			result += stringIn.charAt(i)
		}
	}
	return result
}




// another function will call this, to assert the end of the loading
// this method contains the operations to do when the loading is terminated
function assertLoading(){
	if(loading==0){
		var lt = menuSvgDocument.getElementById("LoadingText");
		if(lt!=null) lt.getStyle().setProperty('display', 'none');
		enableMenu();
	}else{
		var lt = menuSvgDocument.getElementById("LoadingText");
		if(lt!=null) lt.getStyle().setProperty('display', 'inline');
	}
}

// disable the menu 
function disableMenu(){
		var mapMenu = menuSvgDocument.getElementById("Map");
		if(mapMenu!=null)
			mapMenu.getStyle().setProperty('display', 'none');
		
		var mapChoicesMenu = menuSvgDocument.getElementById("MapChoices");
		if(mapChoicesMenu!=null)
			mapChoicesMenu.getStyle().setProperty('display', 'none');			
		
		var nodeMenu = 	menuSvgDocument.getElementById("Node");
		if(nodeMenu)
			nodeMenu.getStyle().setProperty('display', 'none');
			
		var nodeChoicesMenu = 	menuSvgDocument.getElementById("NodeChoices");
		if(nodeChoicesMenu)
			nodeChoicesMenu.getStyle().setProperty('display', 'none');			
			
}

// enable the menu 
function enableMenu(){
//	if(isUserAdmin==false){
//		disableMenu();
//	}else{
		var mapMenu = menuSvgDocument.getElementById("Map");
		if(mapMenu!=null)
			mapMenu.getStyle().setProperty('display', 'inline');
		
		var nodeMenu = 	menuSvgDocument.getElementById("Node");
		if(nodeMenu)
			nodeMenu.getStyle().setProperty('display', 'inline');
			
		var viewMenu = 	menuSvgDocument.getElementById("View")
		if(viewMenu)
			viewMenu.getStyle().setProperty('display', 'inline');
//	}
}


function openMapMenu(evt){
	elemColor(evt,'Map',downColor);
	if(menuOpenFlag==true){
		hideAll(evt);

		elemShow(evt, "MapChoices");
		}
	}

function openNodeMenu(evt){
	elemColor(evt,'Node',downColor);
	if(menuOpenFlag==true){
		hideAll(evt);

		elemShow(evt, "NodeChoices");
		}
}	


function openViewMenu(evt){
	elemColor(evt,'View',downColor);
	if(menuOpenFlag==true){
		hideAll(evt);
		elemShow(evt, "ViewChoices");
		}
}

function hideAll(evt){
	elemHide(evt, "MapChoices");
	elemHide(evt, "NodeChoices");
	elemHide(evt, "ViewChoices");
}


function setMenuOpenFlag(evt, subMenuName){
	if(menuOpenFlag==false){
		menuOpenFlag=true;
		if(subMenuName=="Map"){
			elemShow(evt, "MapChoices");
			}		
		if(subMenuName=="Node"){
			elemShow(evt, "NodeChoices");
			}
		if(subMenuName=="View"){
			elemShow(evt, "ViewChoices");
			}
		}		
	else 
		{
		menuOpenFlag=false;
		if(subMenuName=="Map"){
			elemHide(evt, "MapChoices");
			}		
		if(subMenuName=="Node"){
			elemHide(evt, "NodeChoices");
			}			
		if(subMenuName=="View"){
			elemHide(evt, "ViewChoices");
			}			
		}		
}

function hideSubMenus(evt){
	elemHide(evt,'SetBGColorGroup');
	elemHide(evt,'ColorNodesBySeverityGroup');
	elemHide(evt,'ColorNodesByStatusGroup');
	elemHide(evt,'ColorNodesByAvailGroup');
	elemHide(evt,'SetBGImageGroup');
}

function removeLegend() {
	var legendSVG = menuSvgDocument.getElementById("legend");
	var obj, ls;
	ls = legendSVG.childNodes;
	while (ls.length > 0) {
	  obj = ls.item(0);
	  legendSVG.removeChild(obj);
	}	
}

function addLegend() {
	var legendSVG = menuSvgDocument.getElementById("legend");
	
	var legendHeight=legendSVG.getAttribute("height");
	
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

	var legendGroup = menuSvgDocument.createElementNS(svgNS,"g");

	var lgtext = menuSvgDocument.createElementNS(svgNS,"text");
	lgtext.setAttributeNS(null,"x", x);
	lgtext.setAttributeNS(null,"y",y);
	
	var contentText = menuSvgDocument.createTextNode("Severity View");
	
	if ( colorSemaphoreBy == "A") {
		contentText = menuSvgDocument.createTextNode("Availability View");
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

			var item = menuSvgDocument.createElementNS(svgNS,"circle");
			item.setAttributeNS(null,"cx",cx);
			item.setAttributeNS(null,"cy",cy);
			item.setAttributeNS(null,"r",cr);
			item.setAttributeNS(null,"fill",AVAIL_COLOR[index]);
			item.setAttributeNS(null,"stroke","black");
			legendGroup.appendChild(item);

			var textel = menuSvgDocument.createElementNS(svgNS,"text");
			textel.setAttributeNS(null,"font-size",fontsize);
			textel.setAttributeNS(null,"x", cx+ddx);
			textel.setAttributeNS(null,"y",cy+ddy);
			var label = "";
			if (AVAIL_MIN[index] < 0) {
				label = "Unknown";
			} else {
				label =  " Better " + AVAIL_MIN[index] +"%";
			}
			var labelText = menuSvgDocument.createTextNode(label);
			textel.appendChild(labelText);
			legendGroup.appendChild(textel);
			cx = cx+dx;
			cy = cy+dy;
		}
	} else if (colorSemaphoreBy == "T") {
		contentText = menuSvgDocument.createTextNode("Status View");
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

			var item = menuSvgDocument.createElementNS(svgNS,"circle");
			item.setAttributeNS(null,"cx",cx);
			item.setAttributeNS(null,"cy",cy);
			item.setAttributeNS(null,"r",cr);
			item.setAttributeNS(null,"fill",STATUSES_COLOR[index]);
			item.setAttributeNS(null,"stroke","black");
			legendGroup.appendChild(item);

			var textel = menuSvgDocument.createElementNS(svgNS,"text");
			textel.setAttributeNS(null,"font-size",fontsize);
			textel.setAttributeNS(null,"x", cx+ddx);
			textel.setAttributeNS(null,"y",cy+ddy);
			var labelText = menuSvgDocument.createTextNode(STATUSES_TEXT[index]);
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

			var item = menuSvgDocument.createElementNS(svgNS,"circle");
			item.setAttributeNS(null,"cx",cx);
			item.setAttributeNS(null,"cy",cy);
			item.setAttributeNS(null,"r",cr);
			item.setAttributeNS(null,"fill",SEVERITIES_COLOR[index]);
			item.setAttributeNS(null,"stroke","black");
			legendGroup.appendChild(item);

			var textel = menuSvgDocument.createElementNS(svgNS,"text");
			textel.setAttributeNS(null,"font-size",10);
			textel.setAttributeNS(null,"x", cx+ddx);
			textel.setAttributeNS(null,"y",cy+ddy);
			var labelText = menuSvgDocument.createTextNode(SEVERITIES_LABEL[index]);
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

//clearInfos: clears infos (top and down). Default is true
function addMapElementList()
{
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	resetFlags();
	if(selNodes!=null){
		if(selNodes.exists==true){
			selNodes.removeSelectionList();
			selectedMapElemInList=0;
		}
	}	
	selNodes = new selectionList("nodes",nodes,150,2,23,4,0,mynodesResult);
	selNodes.sortList("asc");


	//add confirm button to the select list		
	var groupCB = menuSvgDocument.createElementNS(svgNS,"g");
	groupCB.setAttributeNS(null,"id", "confirmButton");
	var cb = menuSvgDocument.createElementNS(svgNS,"rect");
	cb.setAttributeNS(null,"x", (selNodes.width-23));
	cb.setAttributeNS(null,"y",0);
	cb.setAttributeNS(null,"width", 25);
	cb.setAttributeNS(null,"height", 17);
	cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
	var cbText = menuSvgDocument.createElementNS(svgNS,"text");
	cbText.setAttributeNS(null,"x", (selNodes.width-23));
	cbText.setAttributeNS(null,"y",12);
	cbText.setAttributeNS(null,"style","fill:white");
	var contentText = menuSvgDocument.createTextNode("Add");		
	cbText.appendChild(contentText);
	groupCB.appendChild(cb);
	groupCB.appendChild(cbText);
	var cb2 = menuSvgDocument.createElementNS(svgNS,"rect");
	cb2.setAttributeNS(null,"x", (selNodes.width-23));
	cb2.setAttributeNS(null,"y",0);
	cb2.setAttributeNS(null,"width", 25);
	cb2.setAttributeNS(null,"height", 17);
	cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0");
	groupCB.appendChild(cb2);
	groupCB.addEventListener("click", addMapElement, false);
	selNodes.selectionBoxGroup.appendChild(groupCB);	
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
	selCategories = new selectionList("categories",categories,150,2,19,4,0,mycategoriesResult);
	selCategories.sortList("asc");


	//add confirm button to the select list		
	var groupCB = menuSvgDocument.createElementNS(svgNS,"g");
	groupCB.setAttributeNS(null,"id", "confirmButton");
	var cb = menuSvgDocument.createElementNS(svgNS,"rect");
	cb.setAttributeNS(null,"x", (selCategories.width-23));
	cb.setAttributeNS(null,"y",0);
	cb.setAttributeNS(null,"width", 25);
	cb.setAttributeNS(null,"height", 17);
	cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
	var cbText = menuSvgDocument.createElementNS(svgNS,"text");
	cbText.setAttributeNS(null,"x", (selCategories.width-23));
	cbText.setAttributeNS(null,"y",12);
	cbText.setAttributeNS(null,"style","fill:white");
	var contentText = menuSvgDocument.createTextNode("Add");		
	cbText.appendChild(contentText);
	groupCB.appendChild(cb);
	groupCB.appendChild(cbText);
	var cb2 = menuSvgDocument.createElementNS(svgNS,"rect");
	cb2.setAttributeNS(null,"x", (selCategories.width-23));
	cb2.setAttributeNS(null,"y",0);
	cb2.setAttributeNS(null,"width", 25);
	cb2.setAttributeNS(null,"height", 17);
	cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0");
	groupCB.appendChild(cb2);
	groupCB.addEventListener("click", addNodesByCategory, false);
	selCategories.selectionBoxGroup.appendChild(groupCB);	
}


function addMapAsNodeList(){
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	selMaps = new selectionList("maps",maps,150,2,19,3,0,mymapsResult);
	selMaps.sortList("asc");


	//add confirm button to the select list		
	var groupCB = menuSvgDocument.createElementNS(svgNS,"g");
	groupCB.setAttributeNS(null,"id", "confirmButton");
	var cb = menuSvgDocument.createElementNS(svgNS,"rect");
	cb.setAttributeNS(null,"x", (selMaps.width-29));
	cb.setAttributeNS(null,"y",0);
	cb.setAttributeNS(null,"width", 31);
	cb.setAttributeNS(null,"height", 17);
	cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
	var cbText = menuSvgDocument.createElementNS(svgNS,"text");
	cbText.setAttributeNS(null,"x", (selMaps.width-26));
	cbText.setAttributeNS(null,"y",12);
	cbText.setAttributeNS(null,"style","fill:white");
	var contentText = menuSvgDocument.createTextNode("Add");		
	cbText.appendChild(contentText);
	groupCB.appendChild(cb);
	groupCB.appendChild(cbText);
	var cb2 = menuSvgDocument.createElementNS(svgNS,"rect");
	cb2.setAttributeNS(null,"x", (selMaps.width-29));
	cb2.setAttributeNS(null,"y",0);
	cb2.setAttributeNS(null,"width", 31);
	cb2.setAttributeNS(null,"height", 17);
	cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0");
	groupCB.appendChild(cb2);
	groupCB.addEventListener("click", addMapAsNode, false);
	selMaps.selectionBoxGroup.appendChild(groupCB);	
}

function addMapsList()
{
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	selMaps = new selectionList("maps",maps,150,2,19,3,0,mymapsResult);
	selMaps.sortList("asc");


	//add confirm button to the select list		
	var groupCB = menuSvgDocument.createElementNS(svgNS,"g");
	groupCB.setAttributeNS(null,"id", "confirmButton");
	var cb = menuSvgDocument.createElementNS(svgNS,"rect");
	cb.setAttributeNS(null,"x", (selMaps.width-29));
	cb.setAttributeNS(null,"y",0);
	cb.setAttributeNS(null,"width", 31);
	cb.setAttributeNS(null,"height", 17);
	cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
	var cbText = menuSvgDocument.createElementNS(svgNS,"text");
	cbText.setAttributeNS(null,"x", (selMaps.width-29));
	cbText.setAttributeNS(null,"y",12);
	cbText.setAttributeNS(null,"style","fill:white");
	var contentText = menuSvgDocument.createTextNode("Open");		
	cbText.appendChild(contentText);
	groupCB.appendChild(cb);
	groupCB.appendChild(cbText);
	var cb2 = menuSvgDocument.createElementNS(svgNS,"rect");
	cb2.setAttributeNS(null,"x", (selMaps.width-29));
	cb2.setAttributeNS(null,"y",0);
	cb2.setAttributeNS(null,"width", 31);
	cb2.setAttributeNS(null,"height", 17);
	cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0");
	groupCB.appendChild(cb2);
	groupCB.addEventListener("click", openMap, false);
	selMaps.selectionBoxGroup.appendChild(groupCB);	
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
	clearDownInfo();
	resetFlags();
	addingMapElemNeighbors=true;

	var childNode = menuSvgDocument.getElementById("DownInfoText");
	if (childNode)
		menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
	menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">" +
		"<tspan x=\"5\" dy=\"0\">Select the element to add</tspan>" +
		"<tspan x=\"7\" dy=\"15\">the Neighbors to.</tspan>" +
	"</text>",menuSvgDocument));		

	
}

function addMapElementNeighList()
{
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	resetFlags();
	if(selNodes!=null){
		if(selNodes.exists==true){
			selNodes.removeSelectionList();
			selectedMapElemInList=0;
		}
	}	
	// set deletingMapElem flag to false ever.
	resetFlags();
	selNodes = new selectionList("nodes",nodes,150,2,23,4,0,mynodesResult);
	selNodes.sortList("asc");



	//add confirm button to the select list		
	var groupCB = menuSvgDocument.createElementNS(svgNS,"g");
	groupCB.setAttributeNS(null,"id", "confirmButton");
	var cb = menuSvgDocument.createElementNS(svgNS,"rect");
	cb.setAttributeNS(null,"x", (selNodes.width-22));
	cb.setAttributeNS(null,"y", 0);
	cb.setAttributeNS(null,"width", 25);
	cb.setAttributeNS(null,"height", 17);
	cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
	var cbText = menuSvgDocument.createElementNS(svgNS,"text");
	cbText.setAttributeNS(null,"x", (selNodes.width-21));
	cbText.setAttributeNS(null,"y", 12);
	cbText.setAttributeNS(null,"style","fill:white");
	var contentText = menuSvgDocument.createTextNode("Add");		
	cbText.appendChild(contentText);
	groupCB.appendChild(cb);
	groupCB.appendChild(cbText);
	var cb2 = menuSvgDocument.createElementNS(svgNS,"rect");
	cb2.setAttributeNS(null,"x", (selNodes.width-22));
	cb2.setAttributeNS(null,"y", 0);
	cb2.setAttributeNS(null,"width", 25);
	cb2.setAttributeNS(null,"height", 17);
	cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0");
	groupCB.appendChild(cb2);
	groupCB.addEventListener("click", addMapElementWithNeighbors, false);
	selNodes.selectionBoxGroup.appendChild(groupCB);	
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

	selMEIcons = new selectionList("meicons",MEIcons,150,2,19,3,0,myMEIconsResult);
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

	selBGImages = new selectionList("bgimages",BGImages,150,2,19,3,0,myBGImagesResult);
	selBGImages.sortList("asc");


	//add confirm button to the select list		
	var groupCB = menuSvgDocument.createElementNS(svgNS,"g");
	groupCB.setAttributeNS(null,"id", "confirmButton");
	var cb = menuSvgDocument.createElementNS(svgNS,"rect");
	cb.setAttributeNS(null,"x", (selBGImages.width-22));
	cb.setAttributeNS(null,"y", 0);
	cb.setAttributeNS(null,"width", 25);
	cb.setAttributeNS(null,"height", 17);
	cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
	var cbText = menuSvgDocument.createElementNS(svgNS,"text");
	cbText.setAttributeNS(null,"x", (selBGImages.width-19));
	cbText.setAttributeNS(null,"y", 12);
	cbText.setAttributeNS(null,"style","fill:white");
	var contentText = menuSvgDocument.createTextNode("Set");		
	cbText.appendChild(contentText);
	groupCB.appendChild(cb);
	groupCB.appendChild(cbText);
	var cb2 = menuSvgDocument.createElementNS(svgNS,"rect");
	cb2.setAttributeNS(null,"x", (selBGImages.width-22));
	cb2.setAttributeNS(null,"y", 0);
	cb2.setAttributeNS(null,"width", 25);
	cb2.setAttributeNS(null,"height", 17);
	cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0");
	groupCB.appendChild(cb2);
	groupCB.addEventListener("click", setBGImage, false);
	selBGImages.selectionBoxGroup.appendChild(groupCB);
} 
 
function setBGImage(){
	if(selectedBGImageInList!=0){ 
	currentMapBackGround = BGImagesSortAss[selectedBGImageInList];
	map.setBackgroundImage(currentMapBackGround);
	clearTopInfo();
	clearDownInfo();
	var childNode = menuSvgDocument.getElementById("DownInfoText");
	if (childNode)
		menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
		menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Background image setting"+
		"<tspan x=\"7\" dy=\"15\">ok.</tspan>" +
	"</text>",menuSvgDocument));	
	}
	
}

function addDimensionList()
{

	clearTopInfo();
	clearDownInfo();
	resetFlags();

	selMapElemDim = new selectionList("mapelemdim",MapElemDim,150,2,19,3,3,myMapElemDimResult);
	selectedMapElemDimInList="normal";
	//add confirm button to the select list		
	var groupCB = menuSvgDocument.createElementNS(svgNS,"g");
	groupCB.setAttributeNS(null,"id", "confirmButton");
	var cb = menuSvgDocument.createElementNS(svgNS,"rect");
	cb.setAttributeNS(null,"x", (selMapElemDim.width-22));
	cb.setAttributeNS(null,"y", 0);
	cb.setAttributeNS(null,"width", 25);
	cb.setAttributeNS(null,"height", 17);
	cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
	var cbText = menuSvgDocument.createElementNS(svgNS,"text");
	cbText.setAttributeNS(null,"x", (selMapElemDim.width-19));
	cbText.setAttributeNS(null,"y", 12);
	cbText.setAttributeNS(null,"style","fill:white");
	var contentText = menuSvgDocument.createTextNode("Set");		
	cbText.appendChild(contentText);
	groupCB.appendChild(cb);
	groupCB.appendChild(cbText);
	var cb2 = menuSvgDocument.createElementNS(svgNS,"rect");
	cb2.setAttributeNS(null,"x", (selMapElemDim.width-22));
	cb2.setAttributeNS(null,"y", 0);
	cb2.setAttributeNS(null,"width", 25);
	cb2.setAttributeNS(null,"height", 17);
	cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0");
	groupCB.appendChild(cb2);
	groupCB.addEventListener("click", setMapElemDim, false);
	selMapElemDim.selectionBoxGroup.appendChild(groupCB);
} 

function addRenameMapBox(){
	if(currentMapId!=MAP_NOT_OPENED){
		clearTopInfo();
		clearDownInfo();
		//lastRenameMapNameValue=currentMapName;
		var topInfoNode = menuSvgDocument.getElementById("TopInfo");
		var renameInfoBox = menuSvgDocument.createElementNS(svgNS,"g");
		renameInfoBox.setAttributeNS(null,"id", "RenameMapBox");
		
		//first a few styling parameters:
		var textStyles = {"font-family":"Arial,Helvetica","font-size":12,"fill":"dimgray"};
		var boxStyles = {"fill":"white","stroke":"dimgray","stroke-width":1.5};
		var cursorStyles = {"stroke":"black","stroke-width":1.5};
		var selBoxStyles = {"fill":"blue","opacity":0.5};
		textbox1 = new textbox("RenameMapBox",renameInfoBox,currentMapName,32,3,20,150,22,textStyles,boxStyles,cursorStyles,selBoxStyles,"[a-zA-Z0-9 ]",undefined);
		

		
		/*var box = menuSvgDocument.createElementNS(svgNS,"rect");
		box.setAttributeNS(null,"x", 3);
		box.setAttributeNS(null,"y", 22);
		box.setAttributeNS(null,"width", 150);
		box.setAttributeNS(null,"height", 17);
		box.setAttributeNS(null,"style","fill:white;stroke:black;stroke-width:1;stroke-opacity:0.4");
		renameInfoBox.appendChild(box);
		renameInfoBox.appendChild(parseXML("<text id=\"RenameMapText\"  onkeyup=\"testMapNameLength(evt);\"  x=\"5\" y=\"35\" editable=\"true\">" +lastRenameMapNameValue+"</text>",menuSvgDocument) );		
		*/
		var groupCB = menuSvgDocument.createElementNS(svgNS,"g");
		groupCB.setAttributeNS(null,"id", "renameButton");
		var cb = menuSvgDocument.createElementNS(svgNS,"rect");
		cb.setAttributeNS(null,"x", 105);
		cb.setAttributeNS(null,"y", 4);
		cb.setAttributeNS(null,"width", 48);
		cb.setAttributeNS(null,"height", 15);
		cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
		var cbText = menuSvgDocument.createElementNS(svgNS,"text");
		cbText.setAttributeNS(null,"x", 106);
		cbText.setAttributeNS(null,"y", 16);
		cbText.setAttributeNS(null,"style","fill:white");
		var contentText = menuSvgDocument.createTextNode("Rename");		
		cbText.appendChild(contentText);
		groupCB.appendChild(cb);
		groupCB.appendChild(cbText);
		var cb2 = menuSvgDocument.createElementNS(svgNS,"rect");
		cb2.setAttributeNS(null,"x", 104);
		cb2.setAttributeNS(null,"y", 3);
		cb2.setAttributeNS(null,"width", 49);
		cb2.setAttributeNS(null,"height", 16);
		cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0;cursor:pointer");
		groupCB.appendChild(cb2);
		groupCB.addEventListener("click", renameMap, false);
		renameInfoBox.appendChild(groupCB);
		topInfoNode.appendChild(renameInfoBox);

 	}else{
		alert('No maps opened');
        }
}

function addRangeBox(){
	if(currentMapId!=MAP_NOT_OPENED){
		clearTopInfo();
		clearDownInfo();

		var topInfoNode = menuSvgDocument.getElementById("TopInfo");
		var rangeBox = menuSvgDocument.createElementNS(svgNS,"g");
		rangeBox.setAttributeNS(null,"id", "NodeRangeBox");
		
		//first a few styling parameters:
		var textStyles = {"font-family":"Arial,Helvetica","font-size":12,"fill":"dimgray"};
		var boxStyles = {"fill":"white","stroke":"dimgray","stroke-width":1.5};
		var cursorStyles = {"stroke":"black","stroke-width":1.5};
		var selBoxStyles = {"fill":"blue","opacity":0.5};
		textbox1 = new textbox("NodeRangeBox",rangeBox,"*.*.*.*",32,3,20,150,22,textStyles,boxStyles,cursorStyles,selBoxStyles,"[^a-zA-Z ]",undefined);
				

		
		/*var box = menuSvgDocument.createElementNS(svgNS,"rect");
		box.setAttributeNS(null,"x", 3);
		box.setAttributeNS(null,"y", 20);
		box.setAttributeNS(null,"width", 160);
		box.setAttributeNS(null,"height", 17);
		box.setAttributeNS(null,"style","fill:white;stroke:black;stroke-width:1;stroke-opacity:0.4");
		rangeBox.appendChild(box);
		rangeBox.appendChild(parseXML("<text id=\"RangeText\"   x=\"5\" y=\"35\" onkeyup=\"testRangeLength(evt);\"  editable=\"true\" font-stretch=\"condensed\">"+lastRangeValue+"</text>",menuSvgDocument) );		
		*/
		
		
		var groupCB = menuSvgDocument.createElementNS(svgNS,"g");
		groupCB.setAttributeNS(null,"id", "addRangeButton");
		var cb = menuSvgDocument.createElementNS(svgNS,"rect");
		cb.setAttributeNS(null,"x", 125);
		cb.setAttributeNS(null,"y", 4);
		cb.setAttributeNS(null,"width", 28);
		cb.setAttributeNS(null,"height", 15);
		cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
		var cbText = menuSvgDocument.createElementNS(svgNS,"text");
		cbText.setAttributeNS(null,"x", 128);
		cbText.setAttributeNS(null,"y", 16);
		cbText.setAttributeNS(null,"style","fill:white");
		var contentText = menuSvgDocument.createTextNode("Add");		
		cbText.appendChild(contentText);
		groupCB.appendChild(cb);
		groupCB.appendChild(cbText);
		var cb2 = menuSvgDocument.createElementNS(svgNS,"rect");
		cb2.setAttributeNS(null,"x", 124);
		cb2.setAttributeNS(null,"y", 3);
		cb2.setAttributeNS(null,"width", 29);
		cb2.setAttributeNS(null,"height", 16);
		cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0;cursor:pointer");
		groupCB.appendChild(cb2);
		groupCB.addEventListener("click", addRangeOfNodes, false);
		rangeBox.appendChild(groupCB);
		topInfoNode.appendChild(rangeBox);
		var childNode = menuSvgDocument.getElementById("DownInfoText");
		if (childNode)
			menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
		menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Ip ranges valid are:"+
			"<tspan x=\"7\" dy=\"15\">192.168.*.*</tspan>" +
			"<tspan x=\"7\" dy=\"15\">192.168.10-20.0-255</tspan>" +
		"</text>",menuSvgDocument));	
		
		

 	}else{
		alert('No maps opened');
        }
}

function addNodeLabelBox(){
	if(currentMapId!=MAP_NOT_OPENED){
		clearTopInfo();
		clearDownInfo();

		var topInfoNode = menuSvgDocument.getElementById("TopInfo");
		var labelBox = menuSvgDocument.createElementNS(svgNS,"g");
		labelBox.setAttributeNS(null,"id", "NodeLabelBox");
		
		//first a few styling parameters:
		var textStyles = {"font-family":"Arial,Helvetica","font-size":12,"fill":"dimgray"};
		var boxStyles = {"fill":"white","stroke":"dimgray","stroke-width":1.5};
		var cursorStyles = {"stroke":"black","stroke-width":1.5};
		var selBoxStyles = {"fill":"blue","opacity":0.5};
		textbox1 = new textbox("NodeLabelBox",labelBox,"",32,3,20,150,22,textStyles,boxStyles,cursorStyles,selBoxStyles,"[^ ]",undefined);

		var groupCB = menuSvgDocument.createElementNS(svgNS,"g");
		groupCB.setAttributeNS(null,"id", "addByLabelButton");
		var cb = menuSvgDocument.createElementNS(svgNS,"rect");
		cb.setAttributeNS(null,"x", 125);
		cb.setAttributeNS(null,"y", 4);
		cb.setAttributeNS(null,"width", 28);
		cb.setAttributeNS(null,"height", 15);
		cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
		var cbText = menuSvgDocument.createElementNS(svgNS,"text");
		cbText.setAttributeNS(null,"x", 128);
		cbText.setAttributeNS(null,"y", 16);
		cbText.setAttributeNS(null,"style","fill:white");
		var contentText = menuSvgDocument.createTextNode("Add");		
		cbText.appendChild(contentText);
		groupCB.appendChild(cb);
		groupCB.appendChild(cbText);
		var cb2 = menuSvgDocument.createElementNS(svgNS,"rect");
		cb2.setAttributeNS(null,"x", 124);
		cb2.setAttributeNS(null,"y", 3);
		cb2.setAttributeNS(null,"width", 29);
		cb2.setAttributeNS(null,"height", 16);
		cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0;cursor:pointer");
		groupCB.appendChild(cb2);
		groupCB.addEventListener("click", addNodesByLabel, false);
		labelBox.appendChild(groupCB);
		topInfoNode.appendChild(labelBox);
 	}else{
		alert('No maps opened');
        }
}

function addNodeLabelFilterBox(id){
	if(currentMapId!=MAP_NOT_OPENED){
		clearTopInfo();
		clearDownInfo();

		var topInfoNode = menuSvgDocument.getElementById("TopInfo");
		var labelBox = menuSvgDocument.createElementNS(svgNS,"g");
		labelBox.setAttributeNS(null,"id", id);
		
		//first a few styling parameters:
		var textStyles = {"font-family":"Arial,Helvetica","font-size":12,"fill":"dimgray"};
		var boxStyles = {"fill":"white","stroke":"dimgray","stroke-width":1.5};
		var cursorStyles = {"stroke":"black","stroke-width":1.5};
		var selBoxStyles = {"fill":"blue","opacity":0.5};
		textbox1 = new textbox(id,labelBox,"",32,2,1,100,17,textStyles,boxStyles,cursorStyles,selBoxStyles,null,LoadNodes);
		topInfoNode.appendChild(labelBox);
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
	clearDownInfo();
	resetFlags();
	deletingMapElem=true;

	var childNode = menuSvgDocument.getElementById("DownInfoText");
	if (childNode)
		menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
	menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Select the element to delete" +
	"</text>",menuSvgDocument));
}

//num is an int to determine wich element must be colored
function setBackground(num){
if(currentMapId!=MAP_NOT_OPENED){
	add_pick_color("pick_color.svg","pickColor",num);
	}else{
			alert('No maps opened');
        }
}

//clear the space reserved for some info to view at the top of the info space
function clearTopInfo(){
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
			selectedMapElemDimInList="normal";
			}
	}			

	if(selMEIcons!=null){
		if(selMEIcons.exists==true){
			selMEIcons.removeSelectionList();
			selectedselMEIconInList=0;
			}
	}			
	var childNode = menuSvgDocument.getElementById("TopInfoText");
	if (childNode)
		menuSvgDocument.getElementById("TopInfo").removeChild(childNode);

	childNode = menuSvgDocument.getElementById("RenameMapBox");
	if (childNode)
		menuSvgDocument.getElementById("TopInfo").removeChild(childNode);
	childNode = menuSvgDocument.getElementById("NodeRangeBox");
	if (childNode)
		childNode.parentNode.removeChild(childNode);
	childNode = menuSvgDocument.getElementById("NodeLabelBox");
	if (childNode)
		childNode.parentNode.removeChild(childNode);	
	childNode = menuSvgDocument.getElementById("NodeLabelFilterBox");
	if (childNode)
		childNode.parentNode.removeChild(childNode);	
		
	childNode = menuSvgDocument.getElementById("NodeWithNeighLabelFilterBox");
	if (childNode)
		childNode.parentNode.removeChild(childNode);	
		

} 

//clear the space reserved for some info to view at the down of the info space
function clearDownInfo(){
	var childNode = menuSvgDocument.getElementById("DownInfoText");
	if (childNode)
		childNode.parentNode.removeChild(childNode);
	close_pick_color(false);
}

function writeDownInfo(info){
	clearDownInfo();
	var childNode = menuSvgDocument.getElementById("DownInfoText");
	if (childNode)
		menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
		menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">"+
		info+
	"</text>",menuSvgDocument));
}


function viewMapInfo(){
	var mapInfo= menuSvgDocument.getElementById("MapInfo");
	mapInfo.appendChild(parseXML("<tspan x=\"3\" dy=\"30\" id=\"mapName\"  font-size=\"9\" >Name: "+currentMapName+" </tspan>",menuSvgDocument));
	mapInfo.appendChild(parseXML("<tspan x=\"3\" dy=\"20\" id=\"mapOwner\" font-size=\"9\">Owner:  "+currentMapOwner+"</tspan>",menuSvgDocument));
	mapInfo.appendChild(parseXML("<tspan x=\"3\" dy=\"20\" id=\"mapUserLast\" font-size=\"9\">User last modified: "+currentMapUserlast+"</tspan>",menuSvgDocument));
	mapInfo.appendChild(parseXML("<tspan x=\"3\" dy=\"20\" id=\"mapCreateTime\" font-size=\"9\">Create time: "+currentMapCreatetime+"</tspan>",menuSvgDocument));
	mapInfo.appendChild(parseXML("<tspan x=\"3\" dy=\"20\" id=\"mapLastModTime\" font-size=\"9\">Last modified time: "+currentMapLastmodtime+"</tspan>",menuSvgDocument));
	menuSvgDocument.getElementById("MapInfo").getStyle().setProperty('display', 'inline');
	/*alert("mapHistoryIndex="+mapHistoryIndex);
	for(ind in mapHistory){

		alert(ind+" "+mapHistory[ind]);
	}*/	
	if(mapHistory.length>mapHistoryIndex+1){
		var next = mapHistory[mapHistoryIndex+1];
		var nextName = mapHistoryName[mapHistoryIndex+1]; 
		//alert("nextMap id="+next+" nextName="+nextName);
		var textContent = menuSvgDocument.createTextNode(nextName);
		var nextText = menuSvgDocument.getElementById("nextMapName");
		if(nextText.getFirstChild())
			nextText.removeChild(nextText.getFirstChild());		
		nextText.appendChild(textContent);
		var nextAction = menuSvgDocument.getElementById("nextAction");
		nextAction.setAttribute("onclick","openMap("+next+");");
		menuSvgDocument.getElementById("nextGroup").getStyle().setProperty('display', 'inline');
	}else{
	        menuSvgDocument.getElementById("nextGroup").getStyle().setProperty('display', 'none');
	}
	if(mapHistoryIndex>0){
		var prev = mapHistory[mapHistoryIndex-1];
		var prevName = mapHistoryName[mapHistoryIndex-1]; 
		//alert("prevMap id="+prev+" prevName="+prevName);
		var textContent = menuSvgDocument.createTextNode(prevName);
		var prevText = menuSvgDocument.getElementById("prevMapName");
		if(prevText.getFirstChild())
			prevText.removeChild(prevText.getFirstChild());
		prevText.appendChild(textContent);
		var prevAction = menuSvgDocument.getElementById("prevAction");
		prevAction.setAttribute("onclick","openMap("+prev+");");		
		menuSvgDocument.getElementById("prevGroup").getStyle().setProperty('display', 'inline');
	}else{
	        menuSvgDocument.getElementById("prevGroup").getStyle().setProperty('display', 'none');
	}	
	
}

function clearMapInfo(){
	var mapInfo= menuSvgDocument.getElementById("MapInfo");
	var mapNameNode=menuSvgDocument.getElementById("mapName");
	if(mapNameNode!=null)
		mapNameNode.parentNode.removeChild(mapNameNode);
	var mapOwnerNode=menuSvgDocument.getElementById("mapOwner");
	if(mapOwnerNode!=null)
		mapOwnerNode.parentNode.removeChild(mapOwnerNode);	
	var mapUserLastNode=menuSvgDocument.getElementById("mapUserLast");
	if(mapUserLastNode!=null)
		mapUserLastNode.parentNode.removeChild(mapUserLastNode);	
	var mapCreateTimeNode=menuSvgDocument.getElementById("mapCreateTime");
	if(mapCreateTimeNode!=null)
		mapCreateTimeNode.parentNode.removeChild(mapCreateTimeNode);		
	var mapLastModTimeNode=menuSvgDocument.getElementById("mapLastModTime");
	if(mapLastModTimeNode!=null)
		mapLastModTimeNode.parentNode.removeChild(mapLastModTimeNode);		

}		

function hideMapInfo(){
	var mapInfoElem = menuSvgDocument.getElementById("MapInfo");
	if(mapInfoElem!=null)
		mapInfoElem.getStyle().setProperty('display', 'none');
}

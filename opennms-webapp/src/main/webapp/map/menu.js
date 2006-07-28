/* Copyright 2000 Adobe Systems. You may copy, modify, and distribute
*  this file, if you include this notice & do not charge for the distribution.
*  This file is provided "AS-IS" without warranties of any kind, including any
*  implied warranties.
*
*  Author:  Glen H. Gersten
*
*
*  This script will perform various manipulations on SVG elements including:
*  fill color changes, stroke width changes, stroke color changes,
*  showing hidden elements, and hiding visible elements.  Additionally, this script
*  has a "toggle" function to perform switches between visible and hidden states.
*
*  To use these functions, this script will need to be sourced into the base document.
*
*  To source this file into an SVG document, use syntax similar to the following:
*	<script xlink:href="events.js" language="JavaScript"></script>
*
*  To source this file into an HTML document, use syntax similar to the following:
*	<script src="events.js" language="JavaScript"></script>
*
*
*  To actually use the functions, you must do the following in the SVG:
*	1. Make sure the element being manipulated has been assigned an id.
*	2. Add event handlers such as onmouseover, onmouseout, or onclick to
*	    the element which will trigger the change.
*	3. Make sure the right arguments are supplied to the function.
*
*  An example of how to change the color of a rectangle called "box" would be:
*	onmouseover="elemColor(evt, 'box', '#336699')"
*/


//////////////////////////////
// Declare Global Variables //
//////////////////////////////
var toggleState = "closed"

//say if the menu is been opened or not
var menuOpenFlag=false;

//color of the component of the menu
var downColor = "blue";
var upColor =  "black";




////////////////////////////////
// SVG Manipulation Functions //
////////////////////////////////

// change the fill color of an element
function elemColor(mouseEvent, elemName, value) {

	// check for AI converted spaces
	elemName = spaceTrans(elemName)

	// get the element we want to change
	var thisElem = menuSvgDocument.getElementById(elemName).getStyle()

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
	var thisElem = menuSvgDocument.getElementById(elemName).getStyle()
	
	// make the element visible
	thisElem.setProperty('display', 'inline')
}


// hide an element - note: these work with the display property, not visibility
function elemHide(mouseEvent, elemName) {


	// check for AI converted spaces
	elemName = spaceTrans(elemName)
	// get the element we want to change
	var thisElem = menuSvgDocument.getElementById(elemName).getStyle()
	
	// hide the element
	thisElem.setProperty('display', 'none')
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
	menuSvgDocument.getElementById("MapMenu").getStyle().setProperty('display', 'none');
	menuSvgDocument.getElementById("NodeMenu").getStyle().setProperty('display', 'none');
	menuSvgDocument.getElementById("NodeChoices").getStyle().setProperty('display', 'none');	
	menuSvgDocument.getElementById("MapChoices").getStyle().setProperty('display', 'none');	
}

// enable the menu 
function enableMenu(){
	if(isUserAdmin=="false" || isUserAdmin==false){
		disableMenu();
	}else{
		menuSvgDocument.getElementById("MapMenu").getStyle().setProperty('display', 'inline');
		menuSvgDocument.getElementById("NodeMenu").getStyle().setProperty('display', 'inline');
		menuSvgDocument.getElementById("ViewMenu").getStyle().setProperty('display', 'inline');
	}
}


function openMapMenu(evt){
	elemColor(evt,'mapRect',downColor);
	if(menuOpenFlag==true){
		hideAll(evt);

		elemShow(evt, "MapChoices");
		}
	}

function openNodeMenu(evt){
	elemColor(evt,'nodeRect',downColor);
	if(menuOpenFlag==true){
		hideAll(evt);

		elemShow(evt, "NodeChoices");
		}
}	


function openViewMenu(evt){
	elemColor(evt,'viewRect',downColor);
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

function addLegend() {

	var x = 1;
	var y = 15;

	var cx = 10;
	var cy = 30;
	var cr = 5;
	var dx = 0;
	var dy = 20;

	var ddx = 10;
	var ddy = 3;
	
	var legendSVG = menuSvgDocument.getElementById("legend");
	
	if (legendSVG.firstChild != undefined) {
		legendSVG.removeChild(legendSVG.firstChild);
	}

	var legendGroup = menuSvgDocument.createElementNS(svgNS,"g");

	var lgtext = menuSvgDocument.createElementNS(svgNS,"text");
	lgtext.setAttributeNS(null,"x", x);
	lgtext.setAttributeNS(null,"y",y);
	
	var contentText = menuSvgDocument.createTextNode("Severity View");
	
	if ( colorSemaphoreBy == "A") {
		contentText = menuSvgDocument.createTextNode("Availability View")
		for(var index in AVAIL_MIN) {

			var item = menuSvgDocument.createElementNS(svgNS,"circle");
			item.setAttributeNS(null,"cx",cx);
			item.setAttributeNS(null,"cy",cy);
			item.setAttributeNS(null,"r",cr);
			item.setAttributeNS(null,"fill",AVAIL_COLOR[index]);
			item.setAttributeNS(null,"stroke","black");
			legendGroup.appendChild(item);

			var textel = menuSvgDocument.createElementNS(svgNS,"text");
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
		contentText = menuSvgDocument.createTextNode("Status View")
		for(var index in STATUSES_UEI) {

			var item = menuSvgDocument.createElementNS(svgNS,"circle");
			item.setAttributeNS(null,"cx",cx);
			item.setAttributeNS(null,"cy",cy);
			item.setAttributeNS(null,"r",cr);
			item.setAttributeNS(null,"fill",STATUSES_COLOR[index]);
			item.setAttributeNS(null,"stroke","black");
			legendGroup.appendChild(item);

			var textel = menuSvgDocument.createElementNS(svgNS,"text");
			textel.setAttributeNS(null,"x", cx+ddx);
			textel.setAttributeNS(null,"y",cy+ddy);
			var labelText = menuSvgDocument.createTextNode(STATUSES_TEXT[index]);
			textel.appendChild(labelText);
			legendGroup.appendChild(textel);
			cx = cx+dx;
			cy = cy+dy;
		}
	} else {
		for(var index in SEVERITIES_LABEL) {

			var item = menuSvgDocument.createElementNS(svgNS,"circle");
			item.setAttributeNS(null,"cx",cx);
			item.setAttributeNS(null,"cy",cy);
			item.setAttributeNS(null,"r",cr);
			item.setAttributeNS(null,"fill",SEVERITIES_COLOR[index]);
			item.setAttributeNS(null,"stroke","black");
			legendGroup.appendChild(item);

			var textel = menuSvgDocument.createElementNS(svgNS,"text");
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

function addMapElementList()
{
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	selNodes = new selectionList("nodes",nodes,150,2,19,4,0,mynodesResult);
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
		alert('No maps opened.');
		return;
	}
	if( map.mapElements==null || map.mapElementSize==0)
	{
		alert('Map without nodes.');
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
	clearTopInfo();
	clearDownInfo();
	// set deletingMapElem flag to false ever.
	resetFlags();
	selNodes = new selectionList("nodes",nodes,150,2,19,4,0,mynodesResult);
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
		alert('No maps opened.');
		return;
	}
	if( map.mapElements==null || map.mapElementSize==0)
	{
		alert('Map without nodes.');
		return;
	}
	clearTopInfo();
	clearDownInfo();
	resetFlags();

	selMEIcons = new selectionList("meicons",MEIcons,150,2,0,3,0,myMEIconsResult);
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
		lastRenameMapNameValue=currentMapName;
		var topInfoNode = menuSvgDocument.getElementById("TopInfo");
		var renameInfoBox = menuSvgDocument.createElementNS(svgNS,"g");
		renameInfoBox.setAttributeNS(null,"id", "RenameMapBox");
		var box = menuSvgDocument.createElementNS(svgNS,"rect");
		box.setAttributeNS(null,"x", 3);
		box.setAttributeNS(null,"y", 22);
		box.setAttributeNS(null,"width", 150);
		box.setAttributeNS(null,"height", 17);
		box.setAttributeNS(null,"style","fill:white;stroke:black;stroke-width:1;stroke-opacity:0.4");
		renameInfoBox.appendChild(box);
		renameInfoBox.appendChild(parseXML("<text id=\"RenameMapText\"  onkeyup=\"testMapNameLength(evt);\"  x=\"5\" y=\"35\" editable=\"true\">" +lastRenameMapNameValue+"</text>",menuSvgDocument) );		
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
		cb2.setAttributeNS(null,"x", 105);
		cb2.setAttributeNS(null,"y", 4);
		cb2.setAttributeNS(null,"width", 48);
		cb2.setAttributeNS(null,"height", 15);
		cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0");
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
		lastRangeValue="*.*.*.*";
		var topInfoNode = menuSvgDocument.getElementById("TopInfo");
		var rangeBox = menuSvgDocument.createElementNS(svgNS,"g");
		rangeBox.setAttributeNS(null,"id", "NodeRangeBox");
		var box = menuSvgDocument.createElementNS(svgNS,"rect");
		box.setAttributeNS(null,"x", 3);
		box.setAttributeNS(null,"y", 20);
		box.setAttributeNS(null,"width", 160);
		box.setAttributeNS(null,"height", 17);
		box.setAttributeNS(null,"style","fill:white;stroke:black;stroke-width:1;stroke-opacity:0.4");
		rangeBox.appendChild(box);
		rangeBox.appendChild(parseXML("<text id=\"RangeText\"   x=\"5\" y=\"35\" onkeyup=\"testRangeLength(evt);\"  editable=\"true\" font-stretch=\"condensed\">"+lastRangeValue+"</text>",menuSvgDocument) );		
		var groupCB = menuSvgDocument.createElementNS(svgNS,"g");
		groupCB.setAttributeNS(null,"id", "addRangeButton");
		var cb = menuSvgDocument.createElementNS(svgNS,"rect");
		cb.setAttributeNS(null,"x", 135);
		cb.setAttributeNS(null,"y", 4);
		cb.setAttributeNS(null,"width", 28);
		cb.setAttributeNS(null,"height", 15);
		cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
		var cbText = menuSvgDocument.createElementNS(svgNS,"text");
		cbText.setAttributeNS(null,"x", 138);
		cbText.setAttributeNS(null,"y", 16);
		cbText.setAttributeNS(null,"style","fill:white");
		var contentText = menuSvgDocument.createTextNode("Add");		
		cbText.appendChild(contentText);
		groupCB.appendChild(cb);
		groupCB.appendChild(cbText);
		var cb2 = menuSvgDocument.createElementNS(svgNS,"rect");
		cb2.setAttributeNS(null,"x", 135);
		cb2.setAttributeNS(null,"y", 4);
		cb2.setAttributeNS(null,"width", 28);
		cb2.setAttributeNS(null,"height", 15);
		cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0");
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

lastRangeValue="";

function testRangeLength(evt){
	try{
		var key = evt.getKeyCode();
		var rangeNode = evt.getTarget();
		var lastValue = rangeNode.firstChild.nodeValue;
		// code embedded that discards keys dangerous for application posts to servlet and keys not usefully for range definitions
		if( (rangeNode.firstChild.nodeValue.length<=32) && ( key==61 || key==8 || key==36 || key==39 || key==45 || key==127 || key==46 || (key>=48 && key<=57) )){
			lastRangeValue=rangeNode.firstChild.nodeValue;
		}else{
			rangeNode.firstChild.nodeValue=lastRangeValue;
			rangeNode.setAttribute("editable","true");
		}
	}catch(e){
	//catch some problem of text features
	//do nothing for the moment
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
		alert('Map without nodes.');
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
			selectedMapElemDimInList=3;
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
	mapInfo.appendChild(parseXML("<tspan x=\"3\" dy=\"20\" id=\"mapUserLast\" font-size=\"9\">User last modifies: "+currentMapUserlast+"</tspan>",menuSvgDocument));
	mapInfo.appendChild(parseXML("<tspan x=\"3\" dy=\"20\" id=\"mapCreateTime\" font-size=\"9\">Create time: "+currentMapCreatetime+"</tspan>",menuSvgDocument));
	mapInfo.appendChild(parseXML("<tspan x=\"3\" dy=\"20\" id=\"mapLastModTime\" font-size=\"9\">Last modified time: "+currentMapLastmodtime+"</tspan>",menuSvgDocument));
	menuSvgDocument.getElementById("MapInfo").getStyle().setProperty('display', 'inline');
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

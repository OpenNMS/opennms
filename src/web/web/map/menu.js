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
	var thisElem = svgDocument.getElementById(elemName).getStyle()

	// perform the fill color change
	thisElem.setProperty('fill', value)
}


// change the stroke width of an element
function elemStrokeWidth(mouseEvent, elemName, value) {


	// check for AI converted spaces
	elemName = spaceTrans(elemName)

	// get the element we want to change
	var thisElem = svgDocument.getElementById(elemName).getStyle()
	
	// perform the stroke width change
	thisElem.setProperty('stroke-width', value)
}


// change the stroke color of an element
function elemStrokeColor(mouseEvent, elemName, value) {


	// check for AI converted spaces
	elemName = spaceTrans(elemName)

	// get the element we want to change
	var thisElem = svgDocument.getElementById(elemName).getStyle()
	
	// perform the stroke color change
	thisElem.setProperty('stroke', value)
}


// show an element - note: these work with the display property, not visibility
function elemShow(mouseEvent, elemName) {


	// check for AI converted spaces
	elemName = spaceTrans(elemName)

	// get the element we want to change
	var thisElem = svgDocument.getElementById(elemName).getStyle()
	
	// make the element visible
	thisElem.setProperty('display', 'inline')
}


// hide an element - note: these work with the display property, not visibility
function elemHide(mouseEvent, elemName) {


	// check for AI converted spaces
	elemName = spaceTrans(elemName)
	
	// get the element we want to change
	var thisElem = svgDocument.getElementById(elemName).getStyle()
	
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
		svgDocument.getElementById("LoadingText").getStyle().setProperty('display', 'none');
		svgDocument.getElementById("MapMenu").getStyle().setProperty('display', 'inline');
		svgDocument.getElementById("NodeMenu").getStyle().setProperty('display', 'inline');
	}else{
		svgDocument.getElementById("LoadingText").getStyle().setProperty('display', 'inline');
	}
}

// disable the menu 
function disableMenu(){
	svgDocument.getElementById("MapMenu").getStyle().setProperty('display', 'none');
	svgDocument.getElementById("NodeMenu").getStyle().setProperty('display', 'none');
	svgDocument.getElementById("NodeChoices").getStyle().setProperty('display', 'none');	
	svgDocument.getElementById("MapChoices").getStyle().setProperty('display', 'none');	
}

// enable the menu 
function enableMenu(){
	svgDocument.getElementById("MapMenu").getStyle().setProperty('display', 'inline');
	svgDocument.getElementById("NodeMenu").getStyle().setProperty('display', 'inline');
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

function hideAll(evt){
	elemHide(evt, "MapChoices");
	elemHide(evt, "NodeChoices");
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
		}
}


function InitApplication(){
	loading++;
	assertLoading();
	postURL ( "InitMapsApplication", null, analizeInitResponse, "text/xml", null );	
}

function analizeInitResponse(data) {
	var str = '';
	if(data.success) {
		str = data.content;
		
		var tmpStr=str.substring(0,6);
		
		if(tmpStr=="InitOK"){
			str=str.substring(6,str.length);
		}
		else{
		     alert('Init Maps Application failed!');
		     hideAll();
		     disableMenu();
			return;
		}
		//alert(str);

		refreshNodesIntervalInSec=parseInt(str)*60;
		loading--;	
		assertLoading();
		//start the refresh nodes countdown
		startRefreshNodesTime();		
	} else {
		alert('Init Maps Application failed');
		hideAll();
	     disableMenu();
	}
}



function LoadMaps(){
	loading++;
	assertLoading();
	//alert("loadmaps");	
	postURL ( "LoadMaps", null, analizeLoadMapsResponse, "text/xml", null );
	
}

function analizeLoadMapsResponse(data) {
	var str = '';
	if(data.success) {

		maps= [" "];
		mapSorts = [null];
		str = data.content;
		
		var tmpStr=str.substring(0,6);
		
		if(tmpStr=="LoadOK"){
			str=str.substring(6,str.length);
		}
		else{
		    alert('Load Maps failed!');
	        loading--;	
			assertLoading();
			hideAll();
  	        disableMenu();			
			return;
		}
		//alert(str);

				
		var st = str.split("@*@");
		if(str.indexOf("*@*")>=0){
			for(var k=0;k<st.length;k++){
				var nodeToken = st[k];
				var nodeST = nodeToken.split("*@*");
				var name,id,owner;
 				id=nodeST[0];
				name=nodeST[1];
				owner=nodeST[2];
				var tempStr = maps.join(".");
				while(	tempStr.indexOf(name) != -1 ){
					name=name+" ";
				}
				var tmpMap = new ElemMap(id, name, owner);
				maps.push(name);
				mapSorts.push(tmpMap);
				//alert('id='+id+' name='+name+' owner='+owner);
			}
		}
		

		mapSortAss = assArrayPopulate(maps,mapSorts);	
		//alert('Loading Maps OK!');	
		loading--;	
		assertLoading();
	} else {
		alert('Loading Maps has failed');
		loading--;	
		assertLoading();
		hideAll();	
 	    disableMenu();			
	}
}

function LoadNodes(){
	loading++;
	assertLoading();
	//alert("loading nodes");
	postURL ( "LoadNodes", null, analizeLoadNodesResponse, "text/xml", null );
	
}

function analizeLoadNodesResponse(data) {
	var str = '';
	if(data.success) {
		nodes= [" "];
		nodeSorts = [null];
		str = data.content;
		var tmpStr=str.substring(0,11);

		//alert(tmpStr);

		if(tmpStr=="loadNodesOK"){
			str=str.substring(11,str.length);
		}
		else{
		     alert('Load Nodes failed!');
		     hideAll();
			 loading--;	
		     assertLoading();		
		     disableMenu();		          
			return;
		}
		//alert(str);
		//alert(str.indexOf("**"));
		
		var st = str.split("**");
		if(str.indexOf("@@")>=0){
			for(var k=0;k<st.length;k++){
				var nodeToken = st[k];
				var nodeST = nodeToken.split("@@");
				var counter=0;
				var label,id,rtc,status,icon;
				var links=new Array();
				var interfaces = new Array();
				var endOfInterfacesFound=false;
				while(counter< nodeST.length){
					var tmp = nodeST[counter];
					if(counter==0)
					{
						id=tmp;
					}
					if(counter==1)
					{
						label=tmp;
					}
				
					if(counter==2)
					{
						rtc=tmp;
					}
					if(counter==3)
					{
						status=tmp;
					}
					if(counter==4)
					{
						icon=tmp;
					}					
					if(counter>4){
						if(tmp=="^^"){
							endOfInterfacesFound=true;
							}
							else{
								if(endOfInterfacesFound)
									links.push(tmp);
								else{
									interfaces.push(tmp);
								}
							}
					}
					counter++;
				}
				var tempStr = nodes.join(".");
				while(	tempStr.indexOf(label) != -1 ){
					label=label+" ";
				}
				var tmpNode = new Node(id, label,  rtc, status,  icon, interfaces, links);
				nodes.push(label);
				nodeSorts.push(tmpNode);
				/*for(var i=0;i<interfaces.length;i++){
					alert(interfaces[i]);
				}*/
				//alert('id='+id+' label='+label+' rtc='+rtc+' status='+status+' icon='+icon+" links="+links);
			}
		}
		
		nodeSortAss = assArrayPopulate(nodes,nodeSorts);	
		loading--;
		assertLoading();
		//alert('Loading Nodes OK!');		
	} else {
		alert('Loading Nodes has failed');
		hideAll();
		loading--;	
		assertLoading();
        disableMenu();		
	}
}


function RefreshNodes(){
	disableMenu();
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	svgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'inline');
	//alert("loading nodes");
	postURL ( "LoadNodes", null, analizeRefreshNodesResponse, "text/xml", null );
	
}
var count=0;
function analizeRefreshNodesResponse(data) {
	var str = '';
	if(data.success) {
		nodes= [" "];
		nodeSorts = [null];
		str = data.content;
		var tmpStr=str.substring(0,11);

		//alert(tmpStr);

		if(tmpStr=="loadNodesOK"){
			str=str.substring(11,str.length);
		}
		else{
		     alert('Load Nodes for refreshing failed!');
		     svgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'none');
		     enableMenu();	startRefreshNodesTime();	     
		     return;
		}
		
		//alert(str.indexOf("**"));
		
		var st = str.split("**");
		if(str.indexOf("@@")>=0){
			for(var k=0;k<st.length;k++){
				var nodeToken = st[k];
				var nodeST = nodeToken.split("@@");
				var counter=0;
				var label,id,ip,rtc,status,icon;
				var links=new Array();
				var interfaces = new Array();
				var endOfInterfacesFound=false;
				while(counter< nodeST.length){
					var tmp = nodeST[counter];
					if(counter==0)
					{
						id=tmp;
					}
					if(counter==1)
					{
						label=tmp;
					}
				
					if(counter==2)
					{
						rtc=tmp;
					}
					if(counter==3)
					{
						status=tmp;
					}
					if(counter==4)
					{
						icon=tmp;
					}					
					if(counter>4){
						if(tmp=="^^"){
							endOfInterfacesFound=true;
							}
							else{
								if(endOfInterfacesFound)
									links.push(tmp);
								else{
									interfaces.push(tmp);
								}
							}
					}
					counter++;
				}
				var tempStr = nodes.join(".");
				while(	tempStr.indexOf(label) != -1 ){
					label=label+" ";
				}
				var tmpNode = new Node(id, label,  rtc, status,  icon, interfaces, links);
				nodes.push(label);
				nodeSorts.push(tmpNode);
				//alert('id='+id+' label='+label+' rtc='+rtc+' status='+status+' icon='+icon+" links="+links);
			}
		}
		
		nodeSortAss = assArrayPopulate(nodes,nodeSorts);	
		//alert('Loading Nodes OK!');		
	} else {
		alert('Loading Nodes for refreshing has failed');
		svgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'none');
		enableMenu();startRefreshNodesTime();
		return;
	}
	
	//update nodes in the current map (semaphore color, availability, status)
	refreshMapElements();
	
	
	svgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'none');
	enableMenu();
	startRefreshNodesTime();
}


function newMap(){
	if(savedMapString!=getMapString() && currentMapId!=MAP_NOT_OPENED) {
	 if(confirm('Map \''+currentMapName+'\' not saved, do you want to proceed however?')==false)
	 	return;
	}
	map.clear();
	clearMapInfo();
	clearTopInfo();
	clearDownInfo();
	currentMapId=NEW_MAP;
	currentMapName="NewMap";
	currentMapBackGround=DEFAULT_BG_COLOR;
	currentMapAccess="";
	currentMapOwner="";
	currentMapUserlast="";
	currentMapCreatetime="";
	currentMapLastmodtime="";
	viewMapInfo();
	map.render();
}

function openMap(){ 
	if(savedMapString!=getMapString() && currentMapId!=MAP_NOT_OPENED) {
	 if(confirm('Map \''+currentMapName+'\' not saved, do you want to proceed however?')==false)
	 	return;
	}
	if(mapSortAss[selectedMapInList]!=null){
		map.clear();
		hideMapInfo();
		loading++;
		assertLoading();
		disableMenu();
		//alert(mapSortAss[selectedMapInList].id);
		postURL ( "OpenMap?MapId="+mapSortAss[selectedMapInList].id, null, openDownloadedMap, "text/xml", null );
	}
	
}


function openDownloadedMap(data) {
	var str = '';
	map.clear();
	if(data.success) {
		str = data.content;
		var tmpStr=str.substring(0,9);
		if(tmpStr=="openMapOK"){
			str=str.substring(9,str.length);
		}
		else{
	        alert('Opening Map failed!');
			loading--;
			assertLoading();
			return;
		}

		var st = str.split("**");
		for(var k=0;k<st.length;k++){
			var nodeToken = st[k];
			var nodeST = nodeToken.split("@@");
			var counter=0;
			var links=new Array();
			if(k==0){
				while(counter< nodeST.length){
					var tmp = nodeST[counter];

					//read the information of the map (id, name, ecc.) 
				
					if(counter==0)
					{
						currentMapId=tmp;
					}
					if(counter==1)
					{
						if(tmp!="null")
							currentMapBackGround=tmp;
						else currentMapBackGround=DEFAULT_BG_COLOR;
					}
					if(counter==2)
						{
						if(tmp!="null")
							currentMapAccess=tmp;
						else currentMapAccess="";
						}
					if(counter==3)
						{
						if(tmp!="null")
							currentMapName=tmp;
						else currentMapName="";
						}
					if(counter==4)
						{
						if(tmp!="null")
							currentMapOwner=tmp;
						else currentMapOwner="";
						}
					if(counter==5)
						{
						if(tmp!="null")
							currentMapUserlast=tmp;
						else currentMapUserlast="";
						}
					if(counter==6)
						{
						if(tmp!="null")
							currentMapCreatetime=tmp;
						else currentMapCreatetime="";
						}
					if(counter==7)
						{
						if(tmp!="null")
							currentMapLastmodtime=tmp;
						else currentMapLastmodtime="";
						}
					//alert(counter);	
					counter++;
					}

					clearMapInfo();
					clearTopInfo();
					clearDownInfo();
					viewMapInfo();

			}else{   // read the info of the elements of the map.
				var id,x=0,y=0,icon=DEFAULT_ICON;
				
				while(counter< nodeST.length){
					var tmp = nodeST[counter];
					
					//read the information of the map (id, name, ecc.)
				
					if(counter==0) 
					{
						id=tmp;
					}
					if(counter==1)
					{
						if(tmp!="null")
							x=parseInt(tmp);
						else x=0;
					}
					if(counter==2) 
						{
						if(tmp!="null")
							y=parseInt(tmp);
						else y=0;
						}
					if(counter==3)
						{
						if(tmp!="null")
							icon=tmp;
						}
					//alert(counter);	
					counter++;
				}
				
				var elem=null;
				for(currEl in nodeSortAss){
					if(nodeSortAss[currEl]!=null){
						//alert(nodeSortAss[currEl].id);
						if(nodeSortAss[currEl].id==id){	
							elem=currEl;	
							break;
						}
					}
				}
				if(elem!=null){
					var semaphoreColor=getSemaphoreColorForNode(nodeSortAss[elem].status,nodeSortAss[elem].avail);
					
				    
					map.addElement(id, icon, nodeSortAss[elem].label, semaphoreColor,  x, y, false,nodeSortAss[elem].status,nodeSortAss[elem].avail);
					
					//map.render();
					map.addLinks(id);
				}else{
					if(id<=0){
							var mapLabel="";
							for(var c in mapSortAss){
								if(mapSortAss[c]!=null){
									if(mapSortAss[c].id==id){
										mapLabel=mapSortAss[c].name;
										break;
										}
								}
							}
							map.addElement(id, icon, mapLabel, "white",  x, y, false,"O",0.00,[]);
							
							addMapNode(id);
					}
					else {
						alert("Node with id="+id+" don't exist.");
					}
				}
			}
			
		}
		map.render();
		loading--;
		assertLoading();
		savedMapString=getMapString();
		enableMenu();
		//alert('Opening Map OK!');		
	} else {
		alert('Opening Map has failed');
		loading--;
		assertLoading();
	}
}



function addMapNode(id){ 
	if(currentMapId==MAP_NOT_OPENED) {
		alert('No Maps opened');
	 	return;
	}
	
	loading++;
	assertLoading();
	disableMenu();
	//alert(mapSortAss[selectedMapInList].id);
	postURL ( "OpenMap?MapId="+id, null, openDownloadedMapAsNode, "text/xml", null );
}


function saveMap() {
	if(currentMapId!=MAP_NOT_OPENED){
	var query="Nodes=";
	var count=0;
	clearTopInfo();
	clearDownInfo();
	var childNode = svgDocument.getElementById("DownInfoText");
	if (childNode)
		svgDocument.getElementById("DownInfo").removeChild(childNode);		
		svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Saving map '" +currentMapName+"'"+
	"</text>",svgDocument));		
		
	
	//construct the query to post to the servlet. (nodes are formatted as follows: id1,x1,y1-id2,x2,y2 ...) 
	for (elemToRender in map.nodes){
		if(count>0)
			query+="**";
		var elem = map.nodes[elemToRender];
		query+= elemToRender+"@@"+elem.x+"@@"+elem.y+"@@"+elem.icon;
		count++;
		}
	//the map is formatted as follows: id,x,y,image
	

	query+="&MapId="+currentMapId+"&MapName="+currentMapName+"&MapBackground="+currentMapBackGround;
	//alert(query);
	postURL ( "SaveMap?"+query, null, viewSaveResponse, "text/xml", null );
	disableMenu();
	}else{
		alert("No maps opened");
	}
}

function viewSaveResponse(data) {
	if(data.success) {
		
		var answerST = data.content.split("@@");
		//alert(answerST[0]);
		if(answerST[0]!="saveMapOK")
			{	
			alert('Saving Map has failed ');	
			clearDownInfo();
			enableMenu();
			return;
			}
		//alert(answerST[0]+" "+answerST[1]+" "+answerST[2]+" "+answerST[3]+" "+answerST[4]+" "+answerST[5]+" "+answerST[6]+" "+answerST[7]+" "+answerST[8]);
		
		clearMapInfo();
		currentMapId=parseInt(answerST[1]);
		currentMapBackGround=answerST[2];
		currentMapAccess=answerST[3];
		currentMapName=answerST[4];
		currentMapOwner=answerST[5];
		currentMapUserlast=answerST[6];
		currentMapCreatetime=answerST[7];
		currentMapLastmodtime=answerST[8];
		
		viewMapInfo();
		savedMapString = getMapString();

		LoadMaps();
		
	} else {
		alert('Saving Map has failed');
		clearDownInfo();
		enableMenu();
		return;
	}
	clearDownInfo();
	var childNode = svgDocument.getElementById("DownInfoText");
	if (childNode)
		svgDocument.getElementById("DownInfo").removeChild(childNode);		
		svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Map '" +currentMapName+"' saved."+
	"</text>",svgDocument));
	enableMenu();
		
}


function deleteMapElement()
{
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	if( map.nodes==null || map.nodeSize==0)
	{
		alert('Map without nodes.');
		return;
	}	
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	deletingMapElem=true;

	var childNode = svgDocument.getElementById("DownInfoText");
	if (childNode)
		svgDocument.getElementById("DownInfo").removeChild(childNode);		
	svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Select the element to delete" +
	"</text>",svgDocument));		
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
	var groupCB = document.createElementNS(svgNS,"g");
	groupCB.setAttributeNS(null,"id", "confirmButton");
	var cb = document.createElementNS(svgNS,"rect");
	cb.setAttributeNS(null,"x", (selNodes.width-23));
	cb.setAttributeNS(null,"y",0);
	cb.setAttributeNS(null,"width", 25);
	cb.setAttributeNS(null,"height", 17);
	cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
	var cbText = document.createElementNS(svgNS,"text");
	cbText.setAttributeNS(null,"x", (selNodes.width-23));
	cbText.setAttributeNS(null,"y",12);
	cbText.setAttributeNS(null,"style","fill:white");
	var contentText = document.createTextNode("Add");		
	cbText.appendChild(contentText);
	groupCB.appendChild(cb);
	groupCB.appendChild(cbText);
	var cb2 = document.createElementNS(svgNS,"rect");
	cb2.setAttributeNS(null,"x", (selNodes.width-23));
	cb2.setAttributeNS(null,"y",0);
	cb2.setAttributeNS(null,"width", 25);
	cb2.setAttributeNS(null,"height", 17);
	cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0");
	groupCB.appendChild(cb2);
	groupCB.addEventListener("click", addMapElement, false);
	selNodes.selectionBoxGroup.appendChild(groupCB);	
}


function addMapAsNode(){ 
	if(currentMapId==MAP_NOT_OPENED) {
		alert('No Maps opened');
	 	return;
	}
	
	if(mapSortAss[selectedMapInList]==null){
		return;
	}
	
	if(mapSortAss[selectedMapInList].id==currentMapId){
		var childNode = svgDocument.getElementById("DownInfoText");
		if (childNode)
			svgDocument.getElementById("DownInfo").removeChild(childNode);		
		svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Cannot add map into itself." +
		"</text>",svgDocument));			
		return;
	}
	
	for(var elem in map.nodes){
		if(map.nodes[elem].id==mapSortAss[selectedMapInList].id){
		var childNode = svgDocument.getElementById("DownInfoText");
		if (childNode)
			svgDocument.getElementById("DownInfo").removeChild(childNode);		
		svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Element already in map." +
		"</text>",svgDocument));			
			return;
		}
	}
	if(mapSortAss[selectedMapInList]!=null){
		loading++;
		assertLoading();
		disableMenu();
		//alert(mapSortAss[selectedMapInList].id);
		postURL ( "OpenMap?MapId="+mapSortAss[selectedMapInList].id, null, addDownloadedMapAsNode, "text/xml", null );
	}
	
}


function addDownloadedMapAsNode(data) {
	var str = '';
	var countNodes=0;
	if(data.success) {
		str = data.content;
		var tmpStr=str.substring(0,9);
		if(tmpStr=="openMapOK"){
			str=str.substring(9,str.length);
		}
		else{
		    alert('Loading Map failed!');
			loading--;
			assertLoading();
			enableMenu();
		}

		var st = str.split("**");
		var mapAvail=-0.0001;
		var mapStatus="U";
		var mapId,mapLabel;
		//array containing the id of all nodes of the map
		var nodes=new Array();
		
		for(var k=0;k<st.length;k++){
			var nodeToken = st[k];
			var nodeST = nodeToken.split("@@");
			var counter=0;
			
			if(k==0){
				while(counter< nodeST.length){
					var tmp = nodeST[counter];

					//read the information of the map (id, name, ecc.) 
				
					if(counter==0)
					{
						mapId=tmp;
					}
					//background don't care here
						/*
					if(counter==1)
					{if(tmp!="null")
							currentMapBackGround=tmp;
						else currentMapBackGround=DEFAULT_BG_COLOR;
					}
					*/
					//map access don't care here (for now)
						/*
					if(counter==2)
						{if(tmp!="null")
							currentMapAccess=tmp;
						else currentMapAccess="";
						
						}
					*/
					if(counter==3)
						{
						if(tmp!="null")
							mapLabel=tmp;
						else mapLabel="";
						}
					//owner don't care here
					/*
					if(counter==4)
						{
						if(tmp!="null")
							currentMapOwner=tmp;
						else currentMapOwner="";
						
						}
					*/
					/* userlastmodified, createtime and lastmod don't care here
					if(counter==5)
						{
						if(tmp!="null")
							currentMapUserlast=tmp;
						else currentMapUserlast="";
						
						}
						
					if(counter==6)
						{
						if(tmp!="null")
							currentMapCreatetime=tmp;
						else currentMapCreatetime="";
						}
					if(counter==7)
						{
						if(tmp!="null")
							currentMapLastmodtime=tmp;
						else currentMapLastmodtime="";
						}
						*/
					//alert(counter);	
					counter++;
					}


					clearTopInfo();
					clearDownInfo();


			}else{   // read the ids
				var id; 
				//,x=0,y=0,icon=DEFAULT_ICON;

				while(counter< nodeST.length){
					var tmp = nodeST[counter];
					
					//read the information of the map (id, name, ecc.)
				
					if(counter==0) 
					{
						id=tmp;
					}/*
					if(counter==1)
					{
						if(tmp!="null")
							x=parseInt(tmp);
						else x=0;
					}
					if(counter==2) 
						{
						if(tmp!="null")
							y=parseInt(tmp);
						else y=0;
						}
					if(counter==3)
						{
						if(tmp!="null")
							icon=tmp;
						}
					*/
					//alert(counter);	
					counter++;
				}
				nodes.push(id);
				//alert(id);
				var elem=null;
				for(currEl in nodeSortAss){
					if(nodeSortAss[currEl]!=null){
						//alert(nodeSortAss[currEl].id);
						if(nodeSortAss[currEl].id==id){	
							elem=nodeSortAss[currEl];	
	
							break;
						}
					}
				}
				if(elem!=null){
					//if the element is a node
					if(elem.id>0){
						if(mapAvail<0) 
							mapAvail=0;
						mapAvail+=parseFloat(elem.avail);
						countNodes++;
					}
					
					if(mapStatus=='U'){
						if(elem.status!='D')
							mapStatus=elem.status;
					}else{
						if(elem.status=="O"){
							mapStatus='O';
						}
					}
				}else{
				//	alert("Node with id="+id+" don't exist.");
				}
			}
			
		}
		
		//map's availability is the average value of availability of its nodes 
		//map's status is 'Active' if and only if there are no nodes 'Outaged'
		if(countNodes>0)
				mapAvail=(mapAvail/countNodes);
		var semaphoreColor=getSemaphoreColorForNode(mapStatus, mapAvail);
		map.addElement(mapId, 'map', mapLabel, semaphoreColor,  70, 20, true,mapStatus,mapAvail,nodes);
		map.addLinks(mapId);
		map.render();
		loading--;
		assertLoading();
		enableMenu();
		//alert('Opening Map OK!');		
	} else {
		alert('Loading Map has failed');
		loading--;
		assertLoading();
		enableMenu();
		
	}
}

function openDownloadedMapAsNode(data) {
	var str = '';
	var countNodes=0;
	if(data.success) {
		str = data.content;
		var tmpStr=str.substring(0,9);
		if(tmpStr=="openMapOK"){
			str=str.substring(9,str.length);
		}
		else{
		     alert('Loading Map failed!');
			loading--;
			assertLoading();
			enableMenu();
		}

		var st = str.split("**");
		var mapAvail=-0.001;
		var mapStatus="U";
		var mapId,mapLabel;
		//array containing the id of all nodes of the map
		var nodes=new Array();
		
		for(var k=0;k<st.length;k++){
			var nodeToken = st[k];
			var nodeST = nodeToken.split("@@");
			var counter=0;
			
			if(k==0){
				while(counter< nodeST.length){
					var tmp = nodeST[counter];

					//read the information of the map (id, name, ecc.) 
				
					if(counter==0)
					{
						mapId=tmp;
					}
					//background don't care here
						/*
					if(counter==1)
					{if(tmp!="null")
							currentMapBackGround=tmp;
						else currentMapBackGround=DEFAULT_BG_COLOR;
					}
					*/
					//map access don't care here (for now)
						/*
					if(counter==2)
						{if(tmp!="null")
							currentMapAccess=tmp;
						else currentMapAccess="";
						
						}
					*/
					if(counter==3)
						{
						if(tmp!="null")
							mapLabel=tmp;
						else mapLabel="";
						}
					//owner don't care here
					/*
					if(counter==4)
						{
						if(tmp!="null")
							currentMapOwner=tmp;
						else currentMapOwner="";
						
						}
					*/
					/* userlastmodified, createtime and lastmod don't care here
					if(counter==5)
						{
						if(tmp!="null")
							currentMapUserlast=tmp;
						else currentMapUserlast="";
						
						}
						
					if(counter==6)
						{
						if(tmp!="null")
							currentMapCreatetime=tmp;
						else currentMapCreatetime="";
						}
					if(counter==7)
						{
						if(tmp!="null")
							currentMapLastmodtime=tmp;
						else currentMapLastmodtime="";
						}
						*/
					//alert(counter);	
					counter++;
					}


					clearTopInfo();
					clearDownInfo();


			}else{   // read the ids
				var id; 
				//,x=0,y=0,icon=DEFAULT_ICON;
				var found=false;
				var tmpMElem;
				for(tmpMElem in map.nodes){
					if(map.nodes[tmpMElem].id == mapId){
						found=true;
						break;
					}
				}
				if( !found ){
					alert('Error: map element with id '+mapId+' not found.');
					return;
				}
				while(counter< nodeST.length){
					var tmp = nodeST[counter];
					
					//read the information of the map (id, name, ecc.)
				
					if(counter==0) 
					{
						id=tmp;
					}/*
					if(counter==1)
					{
						if(tmp!="null")
							x=parseInt(tmp);
						else x=0;
					}
					if(counter==2) 
						{
						if(tmp!="null")
							y=parseInt(tmp);
						else y=0;
						}
					if(counter==3)
						{
						if(tmp!="null")
							icon=tmp;
						}
					*/
					//alert(counter);	
					counter++;
				}
				map.nodes[tmpMElem].nodes.push(id);
				//alert(id);
				var elem=null;
				for(currEl in nodeSortAss){
					if(nodeSortAss[currEl]!=null){
						//alert(nodeSortAss[currEl].id);
						if(nodeSortAss[currEl].id==id){	
							elem=nodeSortAss[currEl];	
	
							break;
						}
					}
				}
				if(elem!=null){
					//if the element is a node
					if(elem.id>0){
						if(mapAvail<0)
							mapAvail=0;
						mapAvail+=parseFloat(elem.avail);
						countNodes++;
					}
					if(mapStatus=='U'){
						if(elem.status!='D')
							mapStatus=elem.status;
					}else{
						if(elem.status=="O"){
							mapStatus='O';
						}
					}
				}else{
				//	alert("Node with id="+id+" don't exist.");
				}
			}
			
		}
		// *****************ricordati di inserire una icona MAPPA

		//alert(countNodes);
		//map's availability is the average value of availability of its nodes 
		//map's status is 'Active' if and only if there are no nodes 'Outaged'
		if(countNodes>0)
				mapAvail=(mapAvail/countNodes);
		else mapAvail=100;
		var semaphoreColor=getSemaphoreColorForNode(mapStatus, mapAvail);
		map.nodes[tmpMElem].setSemaphoreColor(semaphoreColor);
		map.nodes[tmpMElem].status=mapStatus;
		map.nodes[tmpMElem].avail=mapAvail;
		map.addLinks(mapId);
		map.render();
		loading--;
		assertLoading();
		enableMenu();
		//alert('Opening Map OK!');		
	} else {
		alert('Loading Map has failed');
		hideAll();
		loading--;
		assertLoading();
		enableMenu();		
	}
}


function addMapAsNodeList(){
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	selMaps = new selectionList("maps",maps,150,2,19,3,0,mymapsResult);
	selMaps.sortList("asc");


	//add confirm button to the select list		
	var groupCB = document.createElementNS(svgNS,"g");
	groupCB.setAttributeNS(null,"id", "confirmButton");
	var cb = document.createElementNS(svgNS,"rect");
	cb.setAttributeNS(null,"x", (selMaps.width-29));
	cb.setAttributeNS(null,"y",0);
	cb.setAttributeNS(null,"width", 31);
	cb.setAttributeNS(null,"height", 17);
	cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
	var cbText = document.createElementNS(svgNS,"text");
	cbText.setAttributeNS(null,"x", (selMaps.width-26));
	cbText.setAttributeNS(null,"y",12);
	cbText.setAttributeNS(null,"style","fill:white");
	var contentText = document.createTextNode("Add");		
	cbText.appendChild(contentText);
	groupCB.appendChild(cb);
	groupCB.appendChild(cbText);
	var cb2 = document.createElementNS(svgNS,"rect");
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
	var groupCB = document.createElementNS(svgNS,"g");
	groupCB.setAttributeNS(null,"id", "confirmButton");
	var cb = document.createElementNS(svgNS,"rect");
	cb.setAttributeNS(null,"x", (selMaps.width-29));
	cb.setAttributeNS(null,"y",0);
	cb.setAttributeNS(null,"width", 31);
	cb.setAttributeNS(null,"height", 17);
	cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
	var cbText = document.createElementNS(svgNS,"text");
	cbText.setAttributeNS(null,"x", (selMaps.width-29));
	cbText.setAttributeNS(null,"y",12);
	cbText.setAttributeNS(null,"style","fill:white");
	var contentText = document.createTextNode("Open");		
	cbText.appendChild(contentText);
	groupCB.appendChild(cb);
	groupCB.appendChild(cbText);
	var cb2 = document.createElementNS(svgNS,"rect");
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
	if( map.nodes==null || map.nodeSize==0)
	{
		alert('Map without nodes.');
		return;
	}	
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	addingMapElemNeighbors=true;

	var childNode = svgDocument.getElementById("DownInfoText");
	if (childNode)
		svgDocument.getElementById("DownInfo").removeChild(childNode);		
	svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">" +
		"<tspan x=\"5\" dy=\"0\">Select the element to add</tspan>" +
		"<tspan x=\"7\" dy=\"15\">the Neighbors to.</tspan>" +
	"</text>",svgDocument));		

	
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
	var groupCB = document.createElementNS(svgNS,"g");
	groupCB.setAttributeNS(null,"id", "confirmButton");
	var cb = document.createElementNS(svgNS,"rect");
	cb.setAttributeNS(null,"x", (selNodes.width-22));
	cb.setAttributeNS(null,"y", 0);
	cb.setAttributeNS(null,"width", 25);
	cb.setAttributeNS(null,"height", 17);
	cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
	var cbText = document.createElementNS(svgNS,"text");
	cbText.setAttributeNS(null,"x", (selNodes.width-21));
	cbText.setAttributeNS(null,"y", 12);
	cbText.setAttributeNS(null,"style","fill:white");
	var contentText = document.createTextNode("Add");		
	cbText.appendChild(contentText);
	groupCB.appendChild(cb);
	groupCB.appendChild(cbText);
	var cb2 = document.createElementNS(svgNS,"rect");
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
	if( map.nodes==null || map.nodeSize==0)
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
	var groupCB = document.createElementNS(svgNS,"g");
	groupCB.setAttributeNS(null,"id", "confirmButton");
	var cb = document.createElementNS(svgNS,"rect");
	cb.setAttributeNS(null,"x", (selBGImages.width-22));
	cb.setAttributeNS(null,"y", 0);
	cb.setAttributeNS(null,"width", 25);
	cb.setAttributeNS(null,"height", 17);
	cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
	var cbText = document.createElementNS(svgNS,"text");
	cbText.setAttributeNS(null,"x", (selBGImages.width-19));
	cbText.setAttributeNS(null,"y", 12);
	cbText.setAttributeNS(null,"style","fill:white");
	var contentText = document.createTextNode("Set");		
	cbText.appendChild(contentText);
	groupCB.appendChild(cb);
	groupCB.appendChild(cbText);
	var cb2 = document.createElementNS(svgNS,"rect");
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
	var childNode = svgDocument.getElementById("DownInfoText");
	if (childNode)
		svgDocument.getElementById("DownInfo").removeChild(childNode);		
		svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Background image setting"+
		"<tspan x=\"7\" dy=\"15\">ok.</tspan>" +
	"</text>",svgDocument));	
	}
	
}
 
function addRenameMapBox(){
	if(currentMapId!=MAP_NOT_OPENED){
		clearTopInfo();
		clearDownInfo();
		lastRenameMapNameValue=currentMapName;
		var topInfoNode = svgDocument.getElementById("TopInfo");
		var renameInfoBox = document.createElementNS(svgNS,"g");
		renameInfoBox.setAttributeNS(null,"id", "RenameMapBox");
		var box = document.createElementNS(svgNS,"rect");
		box.setAttributeNS(null,"x", 3);
		box.setAttributeNS(null,"y", 22);
		box.setAttributeNS(null,"width", 150);
		box.setAttributeNS(null,"height", 17);
		box.setAttributeNS(null,"style","fill:white;stroke:black;stroke-width:1;stroke-opacity:0.4");
		renameInfoBox.appendChild(box);
		renameInfoBox.appendChild(parseXML("<text id=\"RenameMapText\"  onkeyup=\"testMapNameLength(evt);\"  x=\"5\" y=\"35\" editable=\"true\">" +lastRenameMapNameValue+"</text>",svgDocument) );		
		var groupCB = document.createElementNS(svgNS,"g");
		groupCB.setAttributeNS(null,"id", "renameButton");
		var cb = document.createElementNS(svgNS,"rect");
		cb.setAttributeNS(null,"x", 105);
		cb.setAttributeNS(null,"y", 4);
		cb.setAttributeNS(null,"width", 48);
		cb.setAttributeNS(null,"height", 15);
		cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
		var cbText = document.createElementNS(svgNS,"text");
		cbText.setAttributeNS(null,"x", 106);
		cbText.setAttributeNS(null,"y", 16);
		cbText.setAttributeNS(null,"style","fill:white");
		var contentText = document.createTextNode("Rename");		
		cbText.appendChild(contentText);
		groupCB.appendChild(cb);
		groupCB.appendChild(cbText);
		var cb2 = document.createElementNS(svgNS,"rect");
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

var lastRenameMapNameValue="";
function testMapNameLength(evt){
try{
	var keycode = evt.getKeyCode();
	//alert(keycode);
	var renMapNameNode = evt.getTarget();
	var lastValue = renMapNameNode.firstChild.nodeValue;
	
	//alert(lastValue +" "+renMapNameNode.firstChild.nodeValue.length);
	if(renMapNameNode.firstChild.nodeValue.length>16 || keycode==32 || keycode==61 || keycode==192 || keycode==13){
		renMapNameNode.firstChild.nodeValue=lastRenameMapNameValue;
		renMapNameNode.setAttribute("editable","true");
		//alert(keycode);
	}else{
		lastRenameMapNameValue=renMapNameNode.firstChild.nodeValue;
	}
}catch(e){
//catch some problem of text features
//do nothing for the moment
}
}

function trimAll(sString)
{
	while (sString.substring(0,1) == ' ')
	{
		sString = sString.substring(1, sString.length);
	}
	while (sString.substring(sString.length-1, sString.length) == ' ')
	{
		sString = sString.substring(0,sString.length-1);
	}
	return sString;
}

function addRangeBox(){
	if(currentMapId!=MAP_NOT_OPENED){
		clearTopInfo();
		clearDownInfo();
		lastRangeValue="*.*.*.*";
		var topInfoNode = svgDocument.getElementById("TopInfo");
		var rangeBox = document.createElementNS(svgNS,"g");
		rangeBox.setAttributeNS(null,"id", "NodeRangeBox");
		var box = document.createElementNS(svgNS,"rect");
		box.setAttributeNS(null,"x", 3);
		box.setAttributeNS(null,"y", 20);
		box.setAttributeNS(null,"width", 160);
		box.setAttributeNS(null,"height", 17);
		box.setAttributeNS(null,"style","fill:white;stroke:black;stroke-width:1;stroke-opacity:0.4");
		rangeBox.appendChild(box);
		rangeBox.appendChild(parseXML("<text id=\"RangeText\"   x=\"5\" y=\"35\" onkeyup=\"testRangeLength(evt);\"  editable=\"true\" font-stretch=\"condensed\">"+lastRangeValue+"</text>",svgDocument) );		
		var groupCB = document.createElementNS(svgNS,"g");
		groupCB.setAttributeNS(null,"id", "addRangeButton");
		var cb = document.createElementNS(svgNS,"rect");
		cb.setAttributeNS(null,"x", 135);
		cb.setAttributeNS(null,"y", 4);
		cb.setAttributeNS(null,"width", 28);
		cb.setAttributeNS(null,"height", 15);
		cb.setAttributeNS(null,"style","fill:blue;stroke:pink;stroke-width:1;fill-opacity:0.5;stroke-opacity:0.9");
		var cbText = document.createElementNS(svgNS,"text");
		cbText.setAttributeNS(null,"x", 138);
		cbText.setAttributeNS(null,"y", 16);
		cbText.setAttributeNS(null,"style","fill:white");
		var contentText = document.createTextNode("Add");		
		cbText.appendChild(contentText);
		groupCB.appendChild(cb);
		groupCB.appendChild(cbText);
		var cb2 = document.createElementNS(svgNS,"rect");
		cb2.setAttributeNS(null,"x", 135);
		cb2.setAttributeNS(null,"y", 4);
		cb2.setAttributeNS(null,"width", 28);
		cb2.setAttributeNS(null,"height", 15);
		cb2.setAttributeNS(null,"style","fill:blue;fill-opacity:0");
		groupCB.appendChild(cb2);
		groupCB.addEventListener("click", addRangeOfNodes, false);
		rangeBox.appendChild(groupCB);
		topInfoNode.appendChild(rangeBox);
		var childNode = svgDocument.getElementById("DownInfoText");
		if (childNode)
			svgDocument.getElementById("DownInfo").removeChild(childNode);		
		svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Ip ranges valid are:"+
			"<tspan x=\"7\" dy=\"15\">192.168.*.*</tspan>" +
			"<tspan x=\"7\" dy=\"15\">192.168.10-20.0-255</tspan>" +
		"</text>",svgDocument));	
		
		

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
	
	if( (rangeNode.firstChild.nodeValue.length<=32) && (key==61 || key==8 || key==36 || key==39 || key==45 || key==127 || key==46 || (key>=48 && key<=57) ) ){
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



function renameMap(){
	
	var renameNode =document.getElementById("RenameMapText");
	if(renameNode!=null && trimAll(lastRenameMapNameValue)!=""){
		clearMapInfo();
		currentMapName=lastRenameMapNameValue;
		viewMapInfo();
		clearTopInfo();
		clearDownInfo();
		var childNode = svgDocument.getElementById("DownInfoText");
		if (childNode)
			svgDocument.getElementById("DownInfo").removeChild(childNode);		
			svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Map renamed."+
		"</text>",svgDocument));		
	}else{
		clearDownInfo();
		var childNode = svgDocument.getElementById("DownInfoText");
		if (childNode)
			svgDocument.getElementById("DownInfo").removeChild(childNode);		
			svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Name not valid."+
		"</text>",svgDocument));
	}
	

}


function deleteMap(){
	if(currentMapId!=MAP_NOT_OPENED && currentMapId!=NEW_MAP){
	    if(confirm('Are you sure to delete the map?')==true){ 
	    	postURL ( "DeleteMap?MapId="+currentMapId, null, viewDeleteResponse, "text/xml", null );
	    	}else return;
	}else{
		alert('No maps opened or saved');
		return;
        }
       
        disableMenu();
}


function viewDeleteResponse(data) {
	if(data.success) {

		if(data.content!="deleteMapOK")
			{	
			alert('Deleting Map has failed'+data.content);	
			return;
			}
		clearMapInfo();
		clearTopInfo();
		clearDownInfo();
		close();
		LoadMaps();
		
	} else {
		alert('Deleting Map has failed');
		return;
	}
	clearDownInfo();
	var childNode = svgDocument.getElementById("DownInfoText");
	if (childNode)
		svgDocument.getElementById("DownInfo").removeChild(childNode);		
		svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Map deleted."+
	"</text>",svgDocument));
	enableMenu();
		
}


function clearMap(){
	
	if(currentMapId==MAP_NOT_OPENED)
	{
		alert('No maps opened');
		return;
	}
	
	if (map.nodes==null || map.nodeSize==0){
	  	 alert('Map doesn\'t contain nodes.');
		return;
	}
        if(confirm('Are you sure to clear the map (remove all its elements and links)?')==true) 
         	map.clear();
}

//num is an int to determine wich element must be colored
function setBackground(num){
if(currentMapId!=MAP_NOT_OPENED){
	add_pick_color("pick_color.svg","pickColor",num);
	}else{
			alert('No maps opened');
        }
}

function pickColor_use_pick_color(result,data,num_w)
{
if (result==true){
	switch(num_w)
		{case 0:
			map.setBackgroundImage('');
			map.setBackgroundColor(data);
			break;

		}
	}
}

function close(){
	if(savedMapString!=getMapString() && currentMapId!=MAP_NOT_OPENED) {
	 if(confirm('Map \''+currentMapName+'\' not saved, do you want to proceed however?')==false)
	 	return;
	}
	map.clear();
	clearMapInfo();
	clearDownInfo();
	clearTopInfo();
	hideMapInfo();
	currentMapId=MAP_NOT_OPENED;
	currentMapBackGround=DEFAULT_BG_COLOR;
	currentMapAccess="";
	currentMapName=""; 
	currentMapOwner=""; 
	currentMapUserlast="";
	currentMapCreatetime="";
	currentMapLastmodtime="";
	map.render();
}


function resetFlags(){
	 deletingMapElem=false;
	 addingMapElemNeighbors=false;
	 settingMapElemIcon=false;
}
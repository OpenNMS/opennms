/* *************** functions for the SVG management of the client application map ********************************************** */
// Here are some basic SVG function used to create application objects

//reloads the grid of nodes on the map object
function reloadGrid(){
	var gridRectWidth=parseInt(mapElemDimension*X_FACTOR);	
	var gridRectHeight=parseInt(mapElemDimension*Y_FACTOR);
	var numCols = parseInt(map.getWidth()/gridRectWidth);
	if(numCols==0) numCols=1;
	var numRows = parseInt(map.getHeight()/gridRectHeight);
	if(numRows==0) numRows=1;

	var maxNumOfElements = numCols*numRows;
	var nodeGrid=new Array(numCols);
	for(i = 0; i< numCols; i++){
		nodeGrid[i]=new Array(numRows);
	}
	//alert(nodeGrid);
	var nodes =	map.mapElements;
	for(n in nodes){
		var i = parseInt(nodes[n].x / gridRectWidth);
		//if map dimension are not in 4/3 format
		if(i>=numCols) i=numCols-1;
		var j =	parseInt(nodes[n].y / gridRectHeight);
		if(j>=numRows) j=numRows-1;
		//alert(nodes[n].x+" "+nodes[n].y+" - "+i+" "+j);
		if(	nodeGrid[i][j] == undefined)
			nodeGrid[i][j]=1;
		else nodeGrid[i][j]++;
	}
	
	//alert(nodeGrid);
	return nodeGrid;
}


//gets the first point (Point2D) free of the grid
function getFirstFreePoint(){
	var gridRectWidth=parseInt(mapElemDimension*X_FACTOR);	
	var gridRectHeight=parseInt(mapElemDimension*Y_FACTOR);
	//first, reload grid
	var nodeGrid=reloadGrid();
	//loop first on cols, after on rows
	for(j=0; j<nodeGrid[0].length; j++){
		for(i=0; i<nodeGrid.length; i++){//grid is a 'rectangle'
		  		if(	nodeGrid[i][j]==undefined   || nodeGrid[i][j] == 0){
		  			//alert("grid element "+i+" "+j);
		  			return new Point2D(i*gridRectWidth+gridRectWidth/2, j*gridRectHeight+gridRectHeight/2+(i%2*gridRectHeight/4));	
		  			}
		}
	}
	return null;
}


//gets all free points (Array of Point2D) of the grid
function getFreePoints(){
	var gridRectWidth=parseInt(mapElemDimension*X_FACTOR);	
	var gridRectHeight=parseInt(mapElemDimension*Y_FACTOR);
	//first, reload grid
	var nodeGrid=reloadGrid();
	var freePoints = new Array();
	//loop first on cols, after on rows
	for(j=0; j<nodeGrid[0].length; j++){
		for(i=0; i<nodeGrid.length; i++){//grid is a 'rectangle'
		  		if(	nodeGrid[i][j] == undefined  || nodeGrid[i][j] == 0){
		  			//alert("grid element "+i+" "+j);
		  			var np = new Point2D(i*gridRectWidth+gridRectWidth/2, j*gridRectHeight+gridRectHeight/2+(i%2*gridRectHeight/4));	
		  			freePoints.push(np);
		  			}
		}
	}
	return freePoints;
}

// returns the mouse coordinates as an SVGPoint
function getMouse(evt) {
	var position =  document.documentElement.createSVGPoint();
	position.x = evt.clientX;
	position.y = evt.clientY;
	return position;
}

// return the value of the SVG textbox1 document	
function getTextBoxValue() {
  if (textbox1 != null) return textbox1.getValue();
  return "";
}
	
//create a string representing the status of the map
// in the moment in which is invoked. This function is used to test if the 
// map is bean modified since last saving.	
function getMapString()
{
	if(isUserAdmin==false){ // if is not admin, do not generate a string for the map
			  // because, all changes can't be saved from the user (non admin)
		return "";
	}
	var query=new String("Nodes=");
	var count=0;
	
	//construct the query to post to the servlet. (nodes are formatted as follows: id1,x1,y1-id2,x2,y2 ...) 
	if(map!=undefined){
		for (elemId in map.mapElements){
			if(count>0)
				query+="-";
			var elem = map.mapElements[elemId];
			query+= elemId+","+elem.x+","+elem.y+","+elem.icon.name;
			count++;
		}
	}
	//the map is formatted as follows: id,x,y,image
	query+="&MapId="+currentMapId+"&MapName="+currentMapName+"&MapBackground="+currentMapBackGround;			
	return query;
}

function getAvailColor(avail){
	var maxmin = -2;
	var availid;
	if (avail<0) avail=-1;

	for (index in AVAIL_MIN) {
		var min = AVAIL_MIN[index];
		if (avail >= min && min > maxmin) {
			maxmin=min;
			availid = index;
		}
	}
	return AVAIL_COLOR[availid];
}

function getStatusColor(status){
	
	return STATUSES_COLOR[status];

}

function getSeverityColor(severity){
	
	return SEVERITIES_COLOR[severity];

}

function getSemaphoreColorForNode(severity, avail,status){
	if(isAdminMode){
		return getStatusColor(UNKNOWN_STATUS);
	}else{
		if(colorSemaphoreBy=="A"){
			return getAvailColor(avail);
		}else if (colorSemaphoreBy=="T") {
			return getStatusColor(status);
		} else {
			return getSeverityColor(severity);
		}
	}
}

function getSemaphoreFlash(severity, avail){
	if(!isAdminMode){
		if(colorSemaphoreBy=="A"){
			var maxmin = -1;
			var availid;
			if (avail<0) avail=-1;
		
			for (index in AVAIL_MIN) {
				var min = AVAIL_MIN[index];
				if (avail >= min && min > maxmin) {
					maxmin=min;
					availid = index;
				}
			}
			return AVAIL_FLASH[availid];
		}else if (colorSemaphoreBy=="S"){
			return SEVERITIES_FLASH[severity];
		} 
	} 
	return false;
}

//save the mapid and mapname in the map history
function saveMapInHistory(){
	var found=false;
	for(i in mapHistory){
		if(mapHistory[i]==currentMapId){
			found=true;
			mapHistoryIndex=parseInt(i);
			mapHistoryName[mapHistoryIndex]=currentMapName;
		}
	}
	if(currentMapId!=NEW_MAP && !found){
		if(mapHistory.length==0){
			mapHistory.push(currentMapId);
			mapHistoryName.push(currentMapName);
			mapHistoryIndex = 0;
		}else{
			//alert("mapHistoryIndex="+(mapHistoryIndex));
			++mapHistoryIndex;
			var firstPart = mapHistory.slice(0,mapHistoryIndex);
			var secondPart = mapHistory.slice(mapHistoryIndex);
			var center = new Array();
			center.push(currentMapId);
			firstPart=firstPart.concat(center,secondPart);
			mapHistory=firstPart;
			/*for(ind in mapHistory){
				alert(ind+" "+mapHistory[ind]);
			}*/


			firstPart = mapHistoryName.slice(0,mapHistoryIndex);
			secondPart = mapHistoryName.slice(mapHistoryIndex);
			center = new Array();
			center.push(currentMapName);
			firstPart=firstPart.concat(center,secondPart);
			mapHistoryName=firstPart;
			/*for(ind in mapHistoryName){
				alert(ind+" "+mapHistoryName[ind]);
			}*/	
		}

	}
}

function decreaseMapElemDim(){
	switch (mapElemDimension){
		case 30:{
			mapElemDimension=25;
			break;
		}
		case 25:{
			mapElemDimension=20;
			break;
		}
		case 20:{
			mapElemDimension=15;
			break;
		}
		case 15:{
			mapElemDimension=10;
			break;
		}
		case 10:{
			mapElemDimension=6;
			break;
		}
		case 6:{
			return false;
		}
	}
	return true;
}

//clear the actions started (if there are one action started)
function clearActionsStarted(){
	if(deletingMapElem==true){
		deletingMapElem=false;
	}
	if(addingMapElemNeighbors==true){
		addingMapElemNeighbors=false;
	}
	if(settingMapElemIcon == true) {
		 settingMapElemIcon=false;
	}
}

function resetFlags(){
	 deletingMapElem=false;
	 addingMapElemNeighbors=false;
	 settingMapElemIcon=false;
}

function verifyMapString() {
	if(savedMapString!=getMapString() && currentMapId!=MAP_NOT_OPENED && isAdminMode) {
	 if(confirm('Map \''+currentMapName+'\' not saved, proceed anyway?')==false)
	 	return true;
	}
}

function removeChilds(svgObject) {
	    var ls = svgObject.childNodes;
        while (ls.length > 0) {
          var obj = ls.item(0);
          svgObject.removeChild(obj);
        }
}

function assArrayPopulate(arrayKeys,arrayValues) {
	var returnArray = new Array();
	if (arrayKeys.length != arrayValues.length) {
		alert("Error: arrays do not have same length");
	}
	else {
		for (i=0;i<arrayKeys.length;i++) {
			returnArray[arrayKeys[i]] = arrayValues[i];
		}
	}
	return returnArray;
}

function windowsClean() {
	var obj, ls;
	ls = winSvgElement.childNodes;
	while (ls.length > 0) {
	  obj = ls.item(0);
	  winSvgElement.removeChild(obj);
	}		
}

function tabClean() {
	var obj, ls;
	ls = tabSvgElement.childNodes;
	while (ls.length > 0) {
	  obj = ls.item(0);
	  tabSvgElement.removeChild(obj);
	}		
}

function mapTabSetUp(mapName) {
	if ( mapTabTitles[0] == MAP_NOT_OPENED_NAME ) {
		mapTabTitles = new Array();
	}
	for ( var i in mapTabTitles) {
		if ( mapTabTitles[i]==mapName ) {
			mapTabGroup.activateTabByTitle(mapName,false);
			return;
		}
	}
	tabClean();
	mapTabTitles.push(mapName);
	mapTabGroup = 
new tabgroup("TabPanelGroup","TabPanel",0,0,mapWidth,menuHeight,menuHeight,"rect","triangle",5,0,tabStyles,tabactivetabBGColor,tabwindowStyles,tabtextStyles,mapTabTitles,0,true,activateTabMap);
	mapTabGroup.activateTabByTitle(mapName,false);
}

function mapTabClose(mapName) {
	var tabs = new Array();
	var index = 0;
	for ( var i in mapTabTitles) {
		if ( mapTabTitles[i]==mapName ) {
			index = i;	
		} else {
			tabs.push(mapTabTitles[i]);
		}
	}
	if (tabs.length == 0) {
		tabs.push(MAP_NOT_OPENED_NAME);
	}

	tabClean();
	mapTabTitles=tabs;
	mapTabGroup = 
new tabgroup("TabPanelGroup","TabPanel",0,0,mapWidth,menuHeight,menuHeight,"rect","triangle",5,0,tabStyles,tabactivetabBGColor,tabwindowStyles,tabtextStyles,mapTabTitles,0,true,activateTabMap);

	if ( mapTabTitles[0] == MAP_NOT_OPENED_NAME ) {
			mapTabGroup.activateTabByIndex(index,false);
	} else {
		mapTabGroup.activateTabByIndex(index,true);
	}
}

function mapTabRename(oldMapName,newMapName) {
	var tabs = new Array();
	for ( var i in mapTabTitles) {
		if ( mapTabTitles[i]==oldMapName ) {
			tabs.push(newMapName);
		} else {
			tabs.push(mapTabTitles[i])			
		}
	}

	tabClean();
	mapTabTitles=tabs;
	mapTabGroup = 
new tabgroup("TabPanelGroup","TabPanel",0,0,mapWidth,menuHeight,menuHeight,"rect","triangle",5,0,tabStyles,tabactivetabBGColor,tabwindowStyles,tabtextStyles,mapTabTitles,0,true,activateTabMap);
	mapTabGroup.activateTabByTitle(newMapName,false);

}
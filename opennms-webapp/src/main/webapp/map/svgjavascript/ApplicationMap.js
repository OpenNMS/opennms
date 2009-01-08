
function LoadMaps(){
	loading++;
	assertLoading();
	postMapRequest ( "LoadMaps."+suffix+"?action="+LOADMAPS_ACTION, null, handleLoadMapsResponse, "text/xml", null );
}

function handleLoadMapsResponse(data) {
	var str = '';
	var failed = true;
	if(data.success || data.status==200) {
		str = data.content;
		if(testResponse(LOADMAPS_ACTION, str)){
			str=str.substring(LOADMAPS_ACTION.length+2,str.length);
			failed = false;
		}
	}
	if (failed) {
		alert('Loading Maps failed');
		loading--;	
		assertLoading();
		hideAll();	
 	    disableMenu();			
	}
	maps = [" "];
    mapSorts = [null];
    var st = str.split("&");
	if(str.indexOf("+")>=0){
		for(var k=0;k<st.length;k++){
			var nodeToken = st[k];
			var nodeST = nodeToken.split("+");
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
		}
	}
	mapSortAss = assArrayPopulate(maps,mapSorts);	

	loading--;	
	assertLoading();
	mapsLoaded=true;
}

function LoadNodes(){
	loading++;
	assertLoading();
	postMapRequest ("LoadNodes."+suffix+"?action="+LOADNODES_ACTION , null, handleLoadNodesResponse, "text/xml", null );
}

function handleLoadNodesResponse(data) {
	var str = '';
	var failed = true;
	
	if(data.success || data.status==200) {
		str = data.content;
		if(testResponse(LOADNODES_ACTION, str)){
			str=str.substring(LOADNODES_ACTION.length+2,str.length);
			failed = false;
		}
	}
	
	if (failed) {
	     alert('Load Nodes failed');
	     hideAll();
		 loading--;	
	     assertLoading();		
	     disableMenu();		          
		return;
	}
	var st = str.split("&");
	nodes = [" "];
       nodeSorts = [null];
	if(str.indexOf("+")>=0){
		for(var k=0;k<st.length;k++){
			var nodeToken = st[k];
			var nodeST = nodeToken.split("+");
			var counter=0;
			var id =nodeST[0];
			var label = nodeST[1];
			var tmpNode = new Node(id,label);
			nodes.push(label);
			nodeSorts.push(tmpNode);
		}
	}
		
	nodeSortAss = assArrayPopulate(nodes,nodeSorts);	
	loading--;
	assertLoading();
	nodesLoaded=true;
}

function addMapElement(){
	if(selectedMapElemInList==0 ){
		return;
	}
	var point = getFirstFreePoint();
	if(point==null){
		alert("No free points in the grid; try decreasing the node size");
		return;
	}
	loading++;
	assertLoading();
	disableMenu();
	var elem = nodeSortAss[selectedMapElemInList].id;
	postMapRequest ( "AddNodes."+suffix+"?action="+ADDNODES_ACTION+"&elems="+elem, null, handleAddElementResponse, "text/xml", null );
}

function addRangeOfNodes(){
	var range = getTextBoxValue();
	if(!isValidRange(range)){
		alert('Range not valid!');
		return;
		}
	loading++;
	assertLoading();
	disableMenu();
	postMapRequest ( "AddNodes."+suffix+"?action="+ADDRANGE_ACTION+"&elems="+range, null, handleAddElementResponse, "text/xml", null );
}

function addNodesByLabel(){
	var label = getTextBoxValue();
	if(label==""){
		alert('Invalid Label (must not be blank)');
		return;
		}
	loading++;
	assertLoading();
	disableMenu();
	postMapRequest ( "AddNodes."+suffix+"?action="+ADDNODES_BY_LABEL_ACTION+"&elems="+label, null, handleAddElementResponse, "text/xml", null );
}

function addNodesByCategory(){
	if(selectedCategoryInList==0 )  {
		return;
	}
	loading++;
	assertLoading();
	disableMenu();
	var catLabel = categorySortAss[selectedCategoryInList];
	postMapRequest ( "AddNodes."+suffix+"?action="+ADDNODES_BY_CATEGORY_ACTION+"&elems="+escape(catLabel), null, handleAddElementResponse, "text/xml", null );
}

function addMapElemNeigh(id){
	loading++;
	assertLoading();
	disableMenu();
	postMapRequest ( "AddNodes."+suffix+"?action="+ADDNODES_NEIG_ACTION+"&elems="+id, null, handleAddElementResponse, "text/xml", null );
}

function addMapElementWithNeighbors()
{
	if(selectedMapElemInList==0 )  {
		return;
	}
	loading++;
	assertLoading();
	disableMenu();
	var elem = nodeSortAss[selectedMapElemInList].id;
	postMapRequest ( "AddNodes."+suffix+"?action="+ADDNODES_WITH_NEIG_ACTION+"&elems="+elem, null, handleAddElementResponse, "text/xml", null );
}

function addMapAsNode(){ 
	if(currentMapId==MAP_NOT_OPENED) {
		alert('No Maps opened');
	 	return;
	}
	if(selectedMapInList==0){
		return;
	}
	var mapId = mapSortAss[selectedMapInList].id;
	if(mapId==currentMapId){
		writeDownInfo("Cannot add map to itself");		
		return;
	}
	loading++;
	assertLoading();
	disableMenu();
	postMapRequest ( "AddMaps."+suffix+"?action="+ADDMAPS_ACTION+"&elems="+mapId, null, handleAddElementResponse, "text/xml", null );
}

function handleAddElementResponse(data) {
	var str = '';
	if(data.success || data.status==200) {
		str = data.content;
		if(testResponse(ADDNODES_ACTION, str)){
			str=str.substring(ADDNODES_ACTION.length+2,str.length);
		}else{
			if(testResponse(ADDRANGE_ACTION, str)){
				str=str.substring(ADDRANGE_ACTION.length+2,str.length);
			}else{
				if(testResponse(ADDNODES_NEIG_ACTION, str)){
					str=str.substring(ADDNODES_NEIG_ACTION.length+2,str.length);
				}else{
					if(testResponse(ADDNODES_WITH_NEIG_ACTION, str)){
						str=str.substring(ADDNODES_WITH_NEIG_ACTION.length+2,str.length);
						selectedMapElemInList=0;
					}else{	
						if(testResponse(ADDNODES_BY_CATEGORY_ACTION, str)){
							str=str.substring(ADDNODES_BY_CATEGORY_ACTION.length+2,str.length);
							selectedCategoryInList=0;
						}else{
							if(testResponse(ADDNODES_BY_LABEL_ACTION, str)){
								str=str.substring(ADDNODES_BY_LABEL_ACTION.length+2,str.length);
							}else{																
								if(testResponse(ADDMAPS_ACTION, str)){
									str=str.substring(ADDMAPS_ACTION.length+2,str.length);
								}else{
									alert('Adding Element(s) failed');
									loading--;
									assertLoading();
									return;
								}
							}								
						}
					}
				}
			}
		}
	} else {
        alert('Adding Element(s) failed');
		loading--;
		assertLoading();
		return;
	}
	var nodesAdded=false;
	var nodesToAdd = new Array();
	var linksToAdd = new Array();
	var st = str.split("&");
	for(var k=1;k<st.length;k++){
		var nodeToken = st[k];
		var nodeST = nodeToken.split("+");
		//Manage errors
		if (nodeST.length == 1) {
			var iderror = nodeST[0];
			var tmpStr=iderror.substring(0,9);
			if(tmpStr=="loopfound"){
				alert("Add Map as Node: Found Loop Adding SubMap with ID " + iderror.substring(9));
			}
		}
		//MapElement
		if (nodeST.length > 4) {

			var id,iconName=DEFAULT_ICON,labelText="",avail=100,status=0,severity=0;
			//read the information of the map (id, name, ecc.)
			
			id=nodeST[0];
			iconName=nodeST[1];
			labelText=nodeST[2];
			avail=nodeST[3];
			status=nodeST[4];
			severity=nodeST[5];
					
			var semaphoreColor=getSemaphoreColorForNode(severity,avail,status);
			var semaphoreFlash = getSemaphoreFlash(severity,avail);

			newElem= new MapElement(id,iconName, labelText, semaphoreColor, semaphoreFlash, 0, 0, mapElemDimension, status, avail,severity)
			nodesToAdd.push(newElem);

		}
		// Links
		if (nodeST.length == 4) { // when find links into server response...

			var id1,id2, typology, status;
			id1=nodeST[0];
			id2=nodeST[1];
			typology=nodeST[2];
			status=nodeST[3];
			var linkToAdd = id1+"-"+id2+"-"+typology+"+"+LINKSTATUS_COLOR[status]+"+"+ LINK_WIDTH[typology]+"+"+LINK_DASHARRAY[typology]+"+"+LINKSTATUS_FLASH[status];
			linksToAdd.push(linkToAdd);
			
		}
		
	}
	if(!nodesAdded){
		nodesAdded=true;
		var freePoints = null;
		var alerted = false;
		do{     // try to add the elements
		   freePoints = getFreePoints();

		   if(freePoints.length>=nodesToAdd.length){
			break;
		   }else{
			if(!alerted)
				alert('Not enough space for all elements; their dimensions will be reduced');
			alerted=true;
		   }
		   var decrResult = decreaseMapElemDim();

		   if(decrResult==false) // space problems
			{
			alert('No space available for elements');
			return;
			}	   
		}while(freePoints.length<nodesToAdd.length);
		
		for(el in map.mapElements){
			map.mapElements[el].setDimension(mapElemDimension);
		}
		map.render();
		
		var index = 0;
		for(nd in nodesToAdd){	
			var point = freePoints[index++];
			//alert(point.x+" "+point.y);
			var me = new MapElement(nodesToAdd[nd].id, nodesToAdd[nd].icon, nodesToAdd[nd].label.text, nodesToAdd[nd].semaphore.svgNode.getAttribute("fill"), getSemaphoreFlash(nodesToAdd[nd].severity,nodesToAdd[nd].avail), point.x, point.y, mapElemDimension, nodesToAdd[nd].status, nodesToAdd[nd].avail, nodesToAdd[nd].severity);
			map.addMapElement(me);
		}				
	}

	var msg = "Added "+nodesToAdd.length+" nodes to the map";
	if(nodesToAdd.length==0){
		msg="No nodes added to map"
	}

	var linkId;
	for(ln in linksToAdd){	
		link = linksToAdd[ln];
		var params = link.split('+');
		var elemIds = params[0].split('-');
		var id1=elemIds[0];
		var id2=elemIds[1];
		var typo=elemIds[2];
		var color = params[1];
		var width = params[2];
		var da = params[3];
		var flash = params[4];
		map.addLink(id1,id2,typo,color,width,da,flash);
	}	
	
	clearTopInfo();
	writeDownInfo(msg);

	map.render();
	reloadGrid();
	loading--;
	assertLoading();

	enableMenu();
		
}

function deleteMapElement(elemMap)
{
	loading++;
	assertLoading();
	disableMenu();
	var ACTION = "";
	var id = -1;
	if (elemMap.isMap()) {
		ACTION = DELETEMAPS_ACTION;
		id = elemMap.getMapId();
	}
	if (elemMap.isNode()) {
		ACTION = DELETENODES_ACTION;
		id = elemMap.getNodeId();
	}
	postMapRequest ( "DeleteElements."+suffix+"?action="+ACTION+"&elems="+id, null, handleDeleteNodeResponse, "text/xml", null );
}

function handleDeleteNodeResponse(data) {
	var str = '';
	if(data.success || data.status==200) {
		str = data.content;
		if(testResponse(DELETENODES_ACTION, str)){
			str=str.substring(DELETENODES_ACTION.length+2,str.length);
		}else{
			if(testResponse(DELETEMAPS_ACTION, str)){
				str=str.substring(DELETEMAPS_ACTION.length+2,str.length);
			}else{
	    	    alert('Deleting Element(s) failed');
				loading--;
				assertLoading();
				return;
			}
		}
	} else {
        alert('Deleting Element(s) failed');
		loading--;
		assertLoading();
		return;
	}
	var st = str.split("&");
	for(var k=1;k<st.length;k++){
		var nodeToken = st[k];
		var nodeST = nodeToken.split("+");
		//Manage errors
		var id = nodeST[0];
		map.deleteMapElement(id);
	}
	loading--;
	assertLoading();
	clearTopInfo();
	writeDownInfo("Deleted selected element(s).");

	enableMenu();

}

function newMap(){
	if (verifyMapString()) return;
	currentMapBackground=DEFAULT_BG_COLOR;
	map.clear();
	clearMapInfo();
	clearTopInfo();
	clearDownInfo();
	loading++;
	assertLoading();
	disableMenu();
	postMapRequest (  "NewMap."+suffix+"?action="+NEWMAP_ACTION+"&MapId="+NEW_MAP+"&MapWidth="+mapWidth+"&MapHeight="+mapHeight, null, handleLoadingMap, "text/xml", null );
}

function close(){
	if(currentMapId==MAP_NOT_OPENED){
		alert("No maps opened");
		return;
	}
	
	if (verifyMapString()) return;

	map.clear();
	clearMapInfo();
	clearTopInfo();
	clearDownInfo();
	hideMapInfo();
	loading++;
	assertLoading();
	disableMenu();
	stopCountdown=true;
	postMapRequest ( "CloseMap."+suffix+"?action="+CLOSEMAP_ACTION+"&MapId="+MAP_NOT_OPENED+"&MapWidth="+map.getWidth()+"&MapHeight="+map.getHeight(), null, handleLoadingMap, "text/xml", null );
}

function openMap(mapId){ 		
	if (verifyMapString()) return;

	var mapToOpen;
	if(mapId!=undefined && mapId > 0){
		mapToOpen = mapId;
	}else if(selectedMapInList != undefined && mapSortAss[selectedMapInList].id > 0){
		mapToOpen = mapSortAss[selectedMapInList].id;		
	}else{
		alert("No maps to open");
		return;
	}
	map.clear();
	hideMapInfo();
	loading++;
	assertLoading();
	disableMenu();
	clearTopInfo();
	postMapRequest ( "OpenMap."+suffix+"?action="+OPENMAP_ACTION+"&MapId="+mapToOpen+"&MapWidth="+mapWidth+"&MapHeight="+mapHeight+"&adminMode="+isAdminMode, null, handleLoadingMap, "text/xml", null );
}

function handleLoadingMap(data) {
	var str = '';
	var action = null;
	var failed = true;

	if(data.success || data.status==200) {
		str = data.content;
		if(testResponse(OPENMAP_ACTION, str)){
			str=str.substring(OPENMAP_ACTION.length+2,str.length);
			action = OPENMAP_ACTION;
			selectedMapInList=0;
			failed = false;
		}else{		
			if(testResponse(NEWMAP_ACTION, str)){
				str=str.substring(NEWMAP_ACTION.length+2,str.length);
				failed = false;
				action = NEWMAP_ACTION;
			}else{				
				if(testResponse(CLOSEMAP_ACTION, str)){
					str=str.substring(CLOSEMAP_ACTION.length+2,str.length);
					action = CLOSEMAP_ACTION;
					failed = false;
				}
			}
		}
	}
	if (failed) {
		    alert('Open map failed');
			loading--;
			assertLoading();
			return;
	}

	var st = str.split("&");
	for(var k=0;k<st.length;k++){
		var nodeToken = st[k];
		var nodeST = nodeToken.split("+");
		if(k==0){
			currentMapId=nodeST[0];
			if(nodeST[1] !="null")
				currentMapBackGround=nodeST[1];
			else currentMapBackGround=DEFAULT_BG_COLOR;

			if(nodeST[2] !="null")
			currentMapAccess=nodeST[2];
					else currentMapAccess="";

			if(nodeST[3] !="null")
				currentMapName=nodeST[3];
			else currentMapName="";

			if(nodeST[4] !="null")
				currentMapOwner=nodeST[4];
			else currentMapOwner="";

			if(nodeST[5] !="null")
				currentMapUserlast=nodeST[5];
			else currentMapUserlast="";

			if(nodeST[6] !="null")
				currentMapCreatetime=nodeST[6];
			else currentMapCreatetime="";

			if(nodeST[7] !="null")
				currentMapLastmodtime=nodeST[7];
			else currentMapLastmodtime="";

			clearMapInfo();
			clearTopInfo();
			clearDownInfo();
			
			if(action==CLOSEMAP_ACTION){
				hideMapInfo();
				hideHistory()
				mapHistory=new Array();
				mapHistoryName=new Array();
				mapHistoryIndex = 0;
			}else{
				//save the map in the map history
				saveMapInHistory();
				viewMapInfo();
				showHistory();				
			}
		}	
		if (k>0 && nodeST.length > 4) {
			var id,x,y,iconName,labelText,avail,status,severity;
			
			id=nodeST[0];
			x=nodeST[1];
			y=nodeST[2];
			iconName=nodeST[3];
			labelText=nodeST[4];
			avail=nodeST[5];
			status=nodeST[6];
			severity=nodeST[7];
			
			var semaphoreColor=getSemaphoreColorForNode(severity,avail,status);
			var semaphoreFlash = getSemaphoreFlash(severity,avail);
			map.addMapElement(new MapElement(id,iconName, labelText, semaphoreColor, semaphoreFlash, x, y, mapElemDimension, status, avail,severity));
		}
		if (k>0 && nodeST.length == 4) {
			var id1,id2,typology, status;
			id1=nodeST[0];
			id2=nodeST[1];
			typology=nodeST[2];
			status=nodeST[3];
			map.addLink(id1,id2,typology,LINKSTATUS_COLOR[status], LINK_WIDTH[typology], LINK_DASHARRAY[typology], LINKSTATUS_FLASH[status]);
		}
	}
	map.render();
	reloadGrid();
	loading--;
	assertLoading();
	savedMapString=getMapString();
	enableMenu();
	if (!isAdminMode && !countdownStarted) {		
		startRefreshNodesTime();		    	
	}
}

function saveMap() {
	if(currentMapId!=MAP_NOT_OPENED){
		var query="Nodes=";
		var count=0;
		clearTopInfo();
		writeDownInfo("Saving map '" +currentMapName+"'");
		
		//construct the query to post to the servlet. 
		//the map is formatted as follows: id,x,y,image,type
		var splitInPackets = false;
		var totalPackets = parseInt(map.mapElementSize/70)+1;
		//alert(totalPackets);
		if(totalPackets>1){
			splitInPackets = true;
		}
		for (elemToRender in map.mapElements){
			if(count>70){
				break;
				}
			if(count>0)
				query+="*"; //  '*' = nodes delimiter char
			var elem = map.mapElements[elemToRender];
			var type = NODE_TYPE;
			var id = "";
			if (elem.isMap()) {
				type=MAP_TYPE;
				id = elem.getMapId();
			} else {
				id = elem.getNodeId();
			}
	
			query+= id+","+parseInt(elem.x)+","+parseInt(elem.y)+","+elem.icon+","+type;
			
			count++;
			}
	
		query+="&MapId="+currentMapId+"&MapName="+currentMapName+"&MapBackground="+currentMapBackGround+"&MapWidth="+map.getWidth()+"&MapHeight="+map.getHeight();
		if(splitInPackets==true){
			query+="&packet=1&totalPackets="+totalPackets;
		}
		//alert(query);
		
		postMapRequest ( "SaveMap."+suffix+"?action="+SAVEMAP_ACTION+"&"+query, null, handleSaveResponse, "text/xml", null );
		disableMenu();
	}else{
		alert("No maps opened");
	}
}


//save map with a lot of elements. If the elements are more than 70, packets (of 70 nodes) are required.
function saveMap2(packet, totalPackets) {
	if(currentMapId!=MAP_NOT_OPENED){
	var query="Nodes=";
	var count=0;
	
	//construct the query to post to the servlet. 
	//the map is formatted as follows: id,x,y,image,type
	var base=packet*70;
	for (elemToRender in map.mapElements){
		if(count>base){
			if(count>(base+70)){
				break;				
			}
			if(count>0)
				query+="*"; //  '*' = nodes delimiter char
			var elem = map.mapElements[elemToRender];
			var type = NODE_TYPE;
			var id = "";
			if (elem.isMap()) {
				type=MAP_TYPE;
				id = elem.getMapId();
			} else {
				id = elem.getNodeId();
			}

			query+= id+","+parseInt(elem.x)+","+parseInt(elem.y)+","+elem.icon+","+type;
		}
		count++;
	}
	var packetInt = parseInt(packet) +1;
	query+="&MapId="+currentMapId+"&MapName="+currentMapName+"&MapBackground="+currentMapBackGround+"&MapWidth="+map.getWidth()+"&MapHeight="+map.getHeight();
	query+="&packet="+packetInt+"&totalPackets="+totalPackets;
		
	//alert(query);
	postMapRequest ( "SaveMap."+suffix+"?action="+SAVEMAP_ACTION+"&"+query, null, handleSaveResponse, "text/xml", null );
	disableMenu();
	}else{
		alert("No maps opened");
	}
}


function handleSaveResponse(data) {
	if(data.success || data.status==200) {
		var str=data.content;
		if(testResponse(SAVEMAP_ACTION, str)){
			str=str.substring(SAVEMAP_ACTION.length+2,str.length);
		}else{			
			alert('Failed to save map');	
			clearDownInfo();
			enableMenu();
			return;
		}		
		var answerST = str.split("+");
		//alert(answerST[0]+" "+answerST[1]+" "+answerST[2]+" "+answerST[3]+" "+answerST[4]+" "+answerST[5]+" "+answerST[6]+" "+answerST[7]+" "+answerST[8]);
		var packet = answerST[8];
		var totalPackets = answerST[9];
		//alert("handleSaveResponse: packet="+packet+" totalPackets="+totalPackets);
		if(packet==totalPackets){

			currentMapId=parseInt(answerST[0]);
			currentMapBackGround=answerST[1];
			currentMapAccess=answerST[2];
			currentMapName=answerST[3];
			currentMapOwner=answerST[4];
			currentMapUserlast=answerST[5];
			currentMapCreatetime=answerST[6];
			currentMapLastmodtime=answerST[7];		
			LoadMaps();
			clearMapInfo();
			viewMapInfo();
			savedMapString = getMapString();			
			writeDownInfo("Map '" +currentMapName+"' saved.");

			enableMenu();			
			//save the map in the map history
			saveMapInHistory();
		}else{
			saveMap2(packet, totalPackets); 
		}
		
	} else {
		alert('Failed to save map');
		clearDownInfo();
		enableMenu();
		return;
	}

		
}

function deleteMap(){
	if(currentMapId!=MAP_NOT_OPENED && currentMapId!=NEW_MAP){
	    if(confirm('Are you sure to delete the map?')==true){ 
	    	postMapRequest ( "DeleteMap."+suffix+"?action="+DELETEMAP_ACTION, null, handleDeleteResponse, "text/xml", null );
	    	}else return;
	}else{
		alert('No maps opened or saved');
		return;
        }
       
        disableMenu();
}

function handleDeleteResponse(data) {
	if(data.success || data.status==200) {
		var str=data.content;
		if(!testResponse(DELETEMAP_ACTION, str)){
			alert('Delete Map Failed');	
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
		LoadMaps();
	} else {
		alert('Failed to delete map');
		return;
	}
	writeDownInfo("Map deleted.");

	enableMenu();

	mapHistory.splice(mapHistoryIndex,1);
	mapHistoryName.splice(mapHistoryIndex,1);
		
}

function clearMap(){
	
	if(currentMapId==MAP_NOT_OPENED)
	{
		alert('No maps opened');
		return;
	}
	
	if (map.mapElements==null || map.mapElementSize==0){
	  	 alert('Map contains no nodes');
		return;
	}

    if(confirm('Are you sure to clear the map (remove all its elements and links)?')==true) {
    	loading++;
		assertLoading();
		disableMenu();
     	postMapRequest ("ClearMap."+suffix+"?action="+CLEAR_ACTION+"&elems=", null, handleClearMapResponse, "text/xml", null );
     }
}

function handleClearMapResponse(data) {
	var str = '';
	if(data.success || data.status==200) {
		str = data.content;
		var tmpStr=str.substring(0,CLEAR_ACTION.length+2);
		if(tmpStr==CLEAR_ACTION+"OK"){
			str=str.substring(CLEAR_ACTION.length+2,str.length);
		} else {
	    	    alert('Clear Map Element(s) failed');
				loading--;
				assertLoading();
				return;
		}
	} else {
        alert('Failed to clear map element(s)');
		loading--;
		assertLoading();
		return;
	}
   	map.clear();

   	reloadGrid();
	loading--;
	assertLoading();
	enableMenu();
   	
}

function switchToNormalMode(){
	isAdminMode = false;
	postMapRequest ( "SwitchRole."+suffix+"?action="+SWITCH_MODE_ACTION+"&adminMode="+false, null, handleSwitchRole, "text/xml", null );
}

function switchToAdminMode(){
	isAdminMode = true;
	postMapRequest ( "SwitchRole."+suffix+"?action="+SWITCH_MODE_ACTION+"&adminMode="+true, null, handleSwitchRole, "text/xml", null );
}

function handleSwitchRole(data) {
	
	if(data.success || data.status==200) {
		var str = data.content;
		if(testResponse(SWITCH_MODE_ACTION, str)){
			if (isAdminMode) {
				stopCountdown = true;
				instantiateRWAdminMenu();
				removeLegend();
				for (mapElemId in map.mapElements) {
					map.mapElements[mapElemId].setSemaphoreColor(getSemaphoreColorForNode(0,0,0));
					map.mapElements[mapElemId].setSemaphoreFlash(getSemaphoreFlash(0,0));
				}
			}else{
				if (currentMapId!=MAP_NOT_OPENED && currentMapId!=NEW_MAP) {
					refreshMapElements();
				}
				instantiateRWNormalMenu();
				addLegend();
			}
			map.render();
			enableMenu();
			return;
		}					
    }
    alert('Failed to switch the role');
    if (isAdminMode) {
		isAdminMode=false;
		addLegend();
	}else{
		isAdminMode=true;
		removeLegend();
	}
    return;
}

function RefreshNodes(){
	if(map!=undefined && currentMapId!=MAP_NOT_OPENED ){
		disableMenu();
		clearTopInfo();
		clearDownInfo();
		resetFlags();
	    assertRefreshing(1);
	    
		if(reloadMap){
			postMapRequest ( "RefreshMap."+suffix+"?action="+RELOAD_ACTION, null, handleRefreshNodesResponse, "text/xml", null );
			return;
		}else{
			if(map.mapElementSize>0){
				postMapRequest (  "RefreshMap."+suffix+"?action="+REFRESH_ACTION, null, handleRefreshNodesResponse, "text/xml", null );
				return;
			}
		}
	}
}


function handleRefreshNodesResponse(data) {
	var saved=true;
	assertRefreshing(0);
	
	if(savedMapString!=getMapString()){
		saved=false;
	}
	var str = '';
	var failed = true;
	if(data.success || data.status==200) {
		str = data.content;
		if(reloadMap){
			var tmpStr=str.substring(0,RELOAD_ACTION.length+2);
			if(tmpStr==RELOAD_ACTION+"OK"){
				str=str.substring(RELOAD_ACTION.length+2,str.length);
				failed=false;
			}
		}else{
			var tmpStr=str.substring(0,REFRESH_ACTION.length+2);
			if(tmpStr==REFRESH_ACTION+"OK"){
				str=str.substring(REFRESH_ACTION.length+2,str.length);
				failed=false;
			}
		}
	}
	
	if (failed) {
        alert('Refresh failed');
		enableMenu();
		startRefreshNodesTime();
		return;
	}
	var st = str.split("&");
	map.clearLinks();
	//alert("links cleared!");
	if(reloadMap)
		map.clear();

	for(var k=1;k<st.length;k++){
		var nodeToken = st[k];
		var nodeST = nodeToken.split("+");
		//Manage errors
		if (nodeST.length == 1) {
			var iderror = nodeST[0];
				// manage loop found error
				//loopfound
			var tmpStr=iderror.substring(0,9);
			if(tmpStr=="loopfound"){
				alert("Add Map as Node: Encountered Loop Adding SubMap with ID " + iderror.substring(9));
			}
		}
		//MapElement
		if (nodeST.length > 4) {

			var id,iconName,labelText,avail,status,severity,posx,posy;
			
			id=nodeST[0];
			iconName=nodeST[1];
			labelText=nodeST[2];
			avail=nodeST[3];
			status=nodeST[4];
			severity=nodeST[5];

			var semaphoreColor=getSemaphoreColorForNode(severity,avail,status);
			var semaphoreFlash = getSemaphoreFlash(severity,avail);

			if(reloadMap){
				posx=nodeST[6];
				posy=nodeST[7];
				map.addMapElement(new MapElement(id,iconName, labelText, semaphoreColor, semaphoreFlash, posx, posy, mapElemDimension, status, avail,severity));
			}else{
				var mapElem = map.mapElements[id];
				var x=mapElem.x;
				var y=mapElem.y;
				var deleted = map.deleteMapElement(id);
				if (deleted){
					map.addMapElement(new MapElement(id,iconName, labelText, semaphoreColor, semaphoreFlash, x, y, mapElemDimension, status, avail,severity));
				}
			}
			
		}
		// Links
		
		if (nodeST.length == 4) {
			var id1,id2,typology, status;
			id1=nodeST[0];
			id2=nodeST[1];
			typology=nodeST[2];
			status=nodeST[3];
			map.addLink(id1,id2,typology,LINKSTATUS_COLOR[status], LINK_WIDTH[typology], LINK_DASHARRAY[typology], LINKSTATUS_FLASH[status]);
		}
		
	}
	map.render();

	if(saved){ 
		savedMapString=getMapString();
	}
	enableMenu();
	startRefreshNodesTime();
}

function renameMap(){
	var newMapName = getTextBoxValue();
	if(newMapName != null && trimAll(newMapName)!=""){
		clearMapInfo();
		currentMapName=newMapName;
		viewMapInfo();
		clearTopInfo();
		writeDownInfo("Map renamed.");
	} else {
		writeDownInfo("Name not valid.");
	}
}

function setBGImage(){
	if(selectedBGImageInList!=0){ 

		clearTopInfo();
		currentMapBackGround = BGImagesSortAss[selectedBGImageInList];
		map.setBackgroundImage(currentMapBackGround);
		writeDownInfo("Background image set");
	}
}

function refreshMapElements()
{
	if(currentMapId!=MAP_NOT_OPENED && map.mapElements!=null){
		for (mapElemId in map.mapElements) {
			var el = map.mapElements[mapElemId];
			map.mapElements[mapElemId].setSemaphoreColor(getSemaphoreColorForNode(el.severity,el.avail,el.status));
			map.mapElements[mapElemId].setSemaphoreFlash(getSemaphoreFlash(el.severity,el.avail));
		}
		map.render();
	}
}

function resetRefreshTimer(){
   refreshNodesIntervalInSec=refreshTimeMinsSortAss[selectedRefreshTimeList];
   clearTopInfo();
}

function startRefreshNodesTime() {
        refreshingMapElems=false;
        stopCountDown=false;
        var begin=(new Date()).getTime();
        countdown(begin);
}

function countdown(begin) {
    if (stopCountdown) {
       displayCountDown("CountDown Stopped",false);
    } else {
		countdownStarted = true;
        var actual= (new Date()).getTime();
        var secondsSinceBegin=(actual-begin)/1000;
        if (refreshNodesIntervalInSec>=secondsSinceBegin) {
              var reloadseconds=Math.round(refreshNodesIntervalInSec-secondsSinceBegin);
              var text="Next Refresh: "+((reloadseconds-(reloadseconds%60))/(60))+ "'."+(reloadseconds%60)+"''";
              displayCountDown(text,true);
              var timer=setTimeout("countdown('"+begin+"')",1000);
        }else {
			refreshingMapElems=true;
			countdownStarted=false;
            RefreshNodes();
        }
    }
}

function setMapElemDim(){
	mapElemDimension=parseInt(MapElemDimSortAss[selectedMapElemDimInList]);
	
	for(el in map.mapElements){
		map.mapElements[el].setDimension(mapElemDimension);
	}

	clearTopInfo();
	map.render();	
			
	writeDownInfo("Map element resized to" + mapElemDimension +".");

	enableMenu();
}
		

function verifyMapString() {
	if(savedMapString!=getMapString() && currentMapId!=MAP_NOT_OPENED && isAdminMode) {
	 if(confirm('Map \''+currentMapName+'\' not saved, proceed anyway?')==false)
	 	return true;
	}
}

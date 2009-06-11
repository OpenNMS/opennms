function loadDefaultMap(){
	loading++;
	assertLoading();
	postMapRequest ( "LoadDefaultMap."+suffix+"?action="+LOADDEFAULTMAP_ACTION, null, handleLoadDefaultMapResponse, "text/xml", null );	
}

function handleLoadDefaultMapResponse(data) {
	var str = '';
	var failed = true;
	if(data.success || data.status==200) {
		str = data.content;
		if(testResponse(LOADDEFAULTMAP_ACTION, str)){
			str=str.substring(LOADDEFAULTMAP_ACTION.length+2,str.length);
			failed = false;
		}
	}
    var st = str.split("&");
	if(str.indexOf("+")>=0){
		for(var k=0;k<st.length;k++){
			var nodeToken = st[k];
			var nodeST = nodeToken.split("+");
			var name,id,owner;
			id=nodeST[0];
			name=nodeST[1];
			owner=nodeST[2];
			defaultMap = new ElemMap(id, name, owner);
		}
	}
	loading--;	
	assertLoading();
        // Open the default map if it is defined
        if (defaultMap.getId() > 0 ) {
                openMapSetUp(defaultMap.getId());
        }
}

function loadMaps(){
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

function loadNodes(){
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
		 loading--;	
	     assertLoading();		
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

function addMapElement(id){
	loading++;
	assertLoading();

	postMapRequest ( "AddNodes."+suffix+"?action="+ADDNODES_ACTION+"&elems="+id, null, handleAddElementResponse, "text/xml", null );
}

function addRangeOfNodes(range){
	loading++;
	assertLoading();

	postMapRequest ( "AddNodes."+suffix+"?action="+ADDRANGE_ACTION+"&elems="+range, null, handleAddElementResponse, "text/xml", null );
}

function addNodesByLabel(label){
	loading++;
	assertLoading();

	postMapRequest ( "AddNodes."+suffix+"?action="+ADDNODES_BY_LABEL_ACTION+"&elems="+label, null, handleAddElementResponse, "text/xml", null );
}

function addNodesByCategory(catLabel){
	loading++;
	assertLoading();
	postMapRequest ( "AddNodes."+suffix+"?action="+ADDNODES_BY_CATEGORY_ACTION+"&elems="+escape(catLabel), null, handleAddElementResponse, "text/xml", null );
}

function addMapElemNeigh(id){
	loading++;
	assertLoading();
	postMapRequest ( "AddNodes."+suffix+"?action="+ADDNODES_NEIG_ACTION+"&elems="+id, null, handleAddElementResponse, "text/xml", null );
}

function addMapElementWithNeighbors(elem){
	loading++;
	assertLoading();

	postMapRequest ( "AddNodes."+suffix+"?action="+ADDNODES_WITH_NEIG_ACTION+"&elems="+elem, null, handleAddElementResponse, "text/xml", null );
}

function addMapAsNode(mapId){ 
	loading++;
	assertLoading();

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
									enableManu();
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
		enableMenu();
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
		map.addLink(id1,id2,typo,color,width,da,flash,deltaLink);
	}	
	
	map.render();
	reloadGrid();
	
	loading--;
	assertLoading();
	
	showMapInfo();
	writeDownInfo(msg);
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

	map.clear();
	
	loading++;
	assertLoading();
	
	postMapRequest (  "NewMap."+suffix+"?action="+NEWMAP_ACTION+"&MapId="+NEW_MAP+"&MapWidth="+mapWidth+"&MapHeight="+mapHeight, null, handleLoadingNewMap, "text/xml", null );
}

function handleLoadingNewMap(data) {
	var str = '';
	var failed = true;

	if(data.success || data.status==200) {
		str = data.content;
		if(testResponse(NEWMAP_ACTION, str)){
			str=str.substring(NEWMAP_ACTION.length+2,str.length);
			failed = false;
		}
	}
	if (failed) {
		    alert('Loading New map failed');
			loading--;
			assertLoading();
			enableMenu();
			return;
	}

	var nodeST = str.split("+");
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

			
	//save the map in the map history
	map.setBGvalue(currentMapBackGround);
	map.render();
	reloadGrid();
	
	loading--;
	assertLoading();

	savedMapString=getMapString();
	saveMapInHistory();
	
	writeMapInfo();
	showHistory();				


	enableMenu();
}

function close(){

	map.clear();

	loading++;
	assertLoading();

	stopCountdown=true;

	postMapRequest ( "CloseMap."+suffix+"?action="+CLOSEMAP_ACTION+"&MapId="+MAP_NOT_OPENED+"&MapWidth="+map.getWidth()+"&MapHeight="+map.getHeight(), null, handleLoadingCloseMap, "text/xml", null );
}

function handleLoadingCloseMap(data) {
	var str = '';
	var failed = true;

	if(data.success || data.status==200) {
		str = data.content;
		if(testResponse(CLOSEMAP_ACTION, str)){
			str=str.substring(CLOSEMAP_ACTION.length+2,str.length);
			failed = false;
		}
	}
	if (failed) {
		    alert('Close map failed');
			loading--;
			assertLoading();
			enableMenu();
			return;
	}

	currentMapId=MAP_NOT_OPENED;
	currentMapBackGround=DEFAULT_BG_COLOR;
	map.setBGvalue(DEFAULT_BG_COLOR);
	currentMapAccess="";
	currentMapName=""; 
	currentMapOwner=""; 
	currentMapUserlast="";
	currentMapCreatetime="";
	currentMapLastmodtime="";

	savedMapString=getMapString();
	mapHistory=new Array();
	mapHistoryName=new Array();
	mapHistoryIndex = 0;

	reloadGrid();
	
	loading--;
	assertLoading();
	
	clearMapInfo();
	hideMapInfo();

	enableMenu();
}

function openMap(mapId){ 		

	map.clear();

	loading++;
	assertLoading();

	postMapRequest ( "OpenMap."+suffix+"?action="+OPENMAP_ACTION+"&MapId="+mapId+"&MapWidth="+mapWidth+"&MapHeight="+mapHeight+"&adminMode="+isAdminMode, null, handleLoadingMap, "text/xml", null );
}

function handleLoadingMap(data) {
	var str = '';
	var failed = true;

	if(data.success || data.status==200) {
		str = data.content;
		if(testResponse(OPENMAP_ACTION, str)){
			str=str.substring(OPENMAP_ACTION.length+2,str.length);
			action = OPENMAP_ACTION;
			selectedMapInList=0;
			failed = false;
		}
	}
	if (failed) {
		    alert('Open map failed');
			loading--;
			assertLoading();
			enableMenu();
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
			map.addLink(id1,id2,typology,LINKSTATUS_COLOR[status], LINK_WIDTH[typology], LINK_DASHARRAY[typology], LINKSTATUS_FLASH[status],deltaLink);
		}
	}
	
	savedMapString=getMapString();
	saveMapInHistory();
	
	reloadGrid();
	map.setBGvalue(currentMapBackGround);
	map.render();

	
	loading--;
	assertLoading();

	writeMapInfo();
	showHistory();				

	if (!countdownStarted && !isAdminMode) {	
		startRefreshNodesTime();		    	
	}
	
	enableMenu();
}

function saveMap() {
	var query="Nodes=";
	var count=0;
	
	//construct the query to post to the servlet. 
	//the map is formatted as follows: id,x,y,image,type
	var splitInPackets = false;
	var totalPackets = parseInt(map.getMapElementsSize()/70)+1;
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
		
	postMapRequest ( "SaveMap."+suffix+"?action="+SAVEMAP_ACTION+"&"+query, null, handleSaveResponse, "text/xml", null );
}


//save map with a lot of elements. If the elements are more than 70, packets (of 70 nodes) are required.
function saveMap2(packet, totalPackets) {

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
		
	postMapRequest ( "SaveMap."+suffix+"?action="+SAVEMAP_ACTION+"&"+query, null, handleSaveResponse, "text/xml", null );
}


function handleSaveResponse(data) {
	var failed = true;
	if(data.success || data.status==200) {
		var str=data.content;
		if(testResponse(SAVEMAP_ACTION, str)){
			str=str.substring(SAVEMAP_ACTION.length+2,str.length);
			failed = false;
		}
	}
	
	if (failed) {			
		alert('Failed to save map');	
		showHistory();
		clearDownInfo();
		enableMenu();
		return;
	}		
	var answerST = str.split("+");

	var packet = answerST[8];
	var totalPackets = answerST[9];

	if(packet==totalPackets){

		currentMapId=parseInt(answerST[0]);
		currentMapBackGround=answerST[1];
		currentMapAccess=answerST[2];
		currentMapName=answerST[3];
		currentMapOwner=answerST[4];
		currentMapUserlast=answerST[5];
		currentMapCreatetime=answerST[6];
		currentMapLastmodtime=answerST[7];		
	} else {
		saveMap2(packet, totalPackets);
		return
	}

	loadMaps();
	
	savedMapString = getMapString();			
	saveMapInHistory();
	
	writeMapInfo();
	showHistory();
	writeDownInfo("Map '" +currentMapName+"' saved.");

	enableMenu();			
				
}

function deleteMap(){
	postMapRequest ( "DeleteMap."+suffix+"?action="+DELETEMAP_ACTION, null, handleDeleteResponse, "text/xml", null );
}

function handleDeleteResponse(data) {
	var failed = true;
	var str = null;
	if(data.success || data.status==200) {
		str=data.content;
		if(testResponse(DELETEMAP_ACTION, str)){
			failed = false;
		}
	}
	
	if (failed) {
		alert('Failed to delete map');
		clearDownInfo();
		enableMenu();
		return;
	}
	map.clear();
	
	currentMapId=MAP_NOT_OPENED;
	currentMapBackGround=DEFAULT_BG_COLOR;
	currentMapAccess="";
	currentMapName=""; 
	currentMapOwner=""; 
	currentMapUserlast="";
	currentMapCreatetime="";
	currentMapLastmodtime="";

	map.render();

	loadMaps();

	mapHistory.splice(mapHistoryIndex,1);
	mapHistoryName.splice(mapHistoryIndex,1);

	writeDownInfo("Map deleted.");
	clearMapInfo();
	hideMapInfo();
	showHistory();
	
	enableMenu();
		
}

function clearMap(){
	
	if (map.getMapElementsSize()==0){
	  	 alert('Map contains no nodes');
		return;
	}

   	loading++;
	assertLoading();

    postMapRequest ("ClearMap."+suffix+"?action="+CLEAR_ACTION+"&elems=", null, handleClearMapResponse, "text/xml", null );

}

function handleClearMapResponse(data) {
	var str = '';
	var failed = true;
	
	if(data.success || data.status==200) {
		str = data.content;
		if(testResponse(CLEAR_ACTION,str)) {
			failed = false;
		} 
	}
	if (failed) {
        alert('Failed to clear map element(s)');
		loading--;
		assertLoading();
		clearDownInfo();
		enableMenu();
		return;
	}

   	map.clear();

   	reloadGrid();

	loading--;
	assertLoading();

	writeDownInfo("Map Cleared");
	
	enableMenu();
   	
}

function switchRole(){
	postMapRequest ( "SwitchRole."+suffix+"?action="+SWITCH_MODE_ACTION+"&adminMode="+isAdminMode, null, handleSwitchRole, "text/xml", null );
}


function handleSwitchRole(data) {
	var failed = true;
	if(data.success || data.status==200) {
		var str = data.content;
		if(testResponse(SWITCH_MODE_ACTION, str)){
			failed = false;
		}
	}

	if (failed) {
	    alert('Failed to switch the role');
	    return;
	}

	isAdminMode = !isAdminMode;
	map.render();
	enableMenu();	
	
	if (isAdminMode) {
		showMapInfo();
		showHistory();
		removeLegend();
		for (mapElemId in map.mapElements) {
			map.mapElements[mapElemId].setSemaphoreColor(getSemaphoreColorForNode(0,0,0));
			map.mapElements[mapElemId].setSemaphoreFlash(getSemaphoreFlash(0,0));
		}
	}else{
		addLegend();
		if (currentMapId!=MAP_NOT_OPENED && currentMapId!=NEW_MAP) {
			refreshNodesSetUp();
		}
	}
	
}

function refreshNodes(){
	if(map!=undefined && currentMapId!=MAP_NOT_OPENED ){
	    assertRefreshing(1);
	    
		if(reloadMap){
			postMapRequest ( "RefreshMap."+suffix+"?action="+RELOAD_ACTION,  null, handleRefreshNodesResponse, "text/xml", null );
		}else{
			postMapRequest ( "RefreshMap."+suffix+"?action="+REFRESH_ACTION, null, handleRefreshNodesResponse, "text/xml", null );
		}
	}
}


function handleRefreshNodesResponse(data) {
	assertRefreshing(0);
	
	if (!countdownStarted)
    	startRefreshNodesTime();
	
	enableMenu();
	
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
			map.addLink(id1,id2,typology,LINKSTATUS_COLOR[status], LINK_WIDTH[typology], LINK_DASHARRAY[typology], LINKSTATUS_FLASH[status],deltaLink);
		}
		
	}

	map.render();

}

function setMapElemDim(){

	for(el in map.mapElements){
		map.mapElements[el].setDimension(mapElemDimension);
	}

	map.render();	
			
	writeDownInfo("Map element resized to" + mapElemDimension +".");

	enableMenu();
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

function startRefreshNodesTime() {
        refreshingMapElems=false;
		stopCountdown = false;	
	    var begin=(new Date()).getTime();
        countdown(begin);
}

function countdown(begin) {
    if (isAdminMode || stopCountdown) {
       displayCountDown("CountDown Stopped",false);
	   countdownStarted=false;
    } else {
		countdownStarted = true;
        var actual= (new Date()).getTime();
        var secondsSinceBegin=(actual-begin)/1000;
        if (refreshNodesIntervalInSec>=secondsSinceBegin) {
              var reloadseconds=Math.round(refreshNodesIntervalInSec-secondsSinceBegin);
              var text="Next Refresh: "+((reloadseconds-(reloadseconds%60))/(60))+ "'."+(reloadseconds%60)+"''";
              displayCountDown(text,true);
              var timer=setTimeout("countdown('"+begin+"')",1000);
        } else {
			refreshingMapElems=true;
			countdownStarted=false;
            refreshNodesSetUp();
        }
    }
}

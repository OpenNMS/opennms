function testOKResponse(action, response){
		var tmpStr=response.substring(0,action.length+2);
		if(tmpStr==(action+success_string))
			return true;
		return false;
}

function reloadConfiguration(){
	loading++;
	assertLoading();
	getMapRequest ( RELOAD_CONFIG_ACTION+"."+suffix, null, handleReloadConfigResponse, "text/xml", null );	
}

function handleReloadConfigResponse(data) {
	if((data.success || data.status==200) && testOKResponse(RELOAD_CONFIG_ACTION, data.content) ) {
		top.reloadConfig(1000,2000);
	} else {
		alert('Reloading Map configuration failed');
	}
	loading--;	
	assertLoading();
    }

function loadDefaultMap(){
	loading++;
	assertLoading();
	getMapRequest ( LOADDEFAULTMAP_ACTION+"."+suffix, null, handleLoadDefaultMapResponse, "text/xml", null );	
}

function handleLoadDefaultMapResponse(data) {
	var	content = data.content;
	if((data.success || data.status==200) && 
		content.indexOf(LOADDEFAULTMAP_ACTION+failed_string) == -1 ) {
		 var defmap = eval("("+content+")");
		 defaultMap = new ElemMap(defmap.id, defmap.name, defmap.owner);
	} else {
		 alert('Load Default Map failed');
		 defaultMap = new ElemMap(NEW_MAP, 'no default map found', 'admin');
	}
	loading--;	
	assertLoading();
    
    if (defaultMap.getId() > 0 ) {
		mapTabSetUp(defaultMap.getName());
        openMapSetUp(defaultMap.getId(),false);
    } 
}

function loadMaps(){
	loading++;
	assertLoading();
	getMapRequest ( LOADMAPS_ACTION+"."+suffix, null, handleLoadMapsResponse, "text/xml", null );
}

function handleLoadMapsResponse(data) {
    var content = data.content;
	if((data.success || data.status==200) 
	&& content.indexOf(LOADMAPS_ACTION+failed_string) == -1 ) {
		var currMaps=eval("("+content+")");
		mapLabels = [" "];
		
		var labels = [" "];
	    var mapSorts = [null];
	    var mapids = [null];
		var name,id,owner;
		for(var n in currMaps){
			var mapInfo = currMaps[n];
			id=mapInfo.id;
			name=mapInfo.name;
			owner=mapInfo.owner;
			var tempStr = mapLabels.join(".");
			while(	tempStr.indexOf(name) != -1 ){
				name=name+" ";
			}
			mapids.push(id);
			mapLabels.push(name);
			labels.push(name)
			mapSorts.push(new ElemMap(id, name, owner));
		}
	
		mapids.push(SEARCH_MAP);	
		labels.push(SEARCH_MAP_NAME);
		mapSorts.push(new ElemMap(SEARCH_MAP,SEARCH_MAP_NAME,"admin"));
	
		mapSortAss = assArrayPopulate(labels,mapSorts);	
		mapidSortAss = assArrayPopulate(mapids,labels);	
		mapsLoaded=true;
		// Load also the maps label
		loadLabelMap();
	} else {
		alert('Loading Maps failed');
	}
	loading--;	
	assertLoading();
}

function loadNodes(){
	loading++;
	assertLoading();
	getMapRequest (LOADNODES_ACTION+"."+suffix , null, handleLoadNodesResponse, "text/xml", null );
}

function handleLoadNodesResponse(data) {
    var content = data.content;
	if((data.success || data.status==200) 
	&& content.indexOf(LOADNODES_ACTION+failed_string) == -1 ) {
		var currNodes=eval("("+content+")");
		nodeLabels = [" "];
	    var nodeSorts = [null];
	    var nodeids = [null];
	    var id,label,ipaddr,tmpNode;
		for(var n in currNodes){
			id =currNodes[n].id;
			label = currNodes[n].label;
			ipaddr = currNodes[n].ipaddr;
			tmpNode = new Node(id,label,ipaddr);
			nodeids.push(id);
			nodeLabels.push(label);
			nodeSorts.push(tmpNode);
		}
			
		nodeSortAss = assArrayPopulate(nodeLabels,nodeSorts);	
		nodeidSortAss = assArrayPopulate(nodeids,nodeSorts);	
		nodesLoaded=true;
	} else {
	     alert('Load Nodes failed');
	}
	loading--;
	assertLoading();
}

function loadLabelMap(){
	loading++;
	assertLoading();
	getMapRequest (LOADLABELMAP_ACTION+"."+suffix , null, handleLoadLabelMapResponse, "text/xml", null );
}

function handleLoadLabelMapResponse(data) {
	var content= data.content;
	if((data.success || data.status==200) 
	&& content.indexOf(LOADLABELMAP_ACTION+failed_string) == -1) {	    
		var labelMap=eval("("+content+")");
		if ( labelMap != null ) {
			for (var label in labelMap) {
			  var matchingMapLabels = new Array();
			  for (var i=0; i<labelMap[label].length; i++) {
				var mapid = labelMap[label][i];
			  	matchingMapLabels.push(mapidSortAss[mapid]);
			  }
              nodeLabelMap[label]=matchingMapLabels;
			}
		}
	}else{
		alert("Load Label Map failed.");
	}	
	loading--;
	assertLoading();	
}

function addMapElement(id){
	loading++;
	assertLoading();

	getMapRequest ( ADDNODES_ACTION+"."+suffix+"?action="+ADDNODES_ACTION+"&elems="+id, null, handleAddElementResponse, "text/xml", null );
}

function addRangeOfNodes(range){
	loading++;
	assertLoading();

	getMapRequest ( ADDNODES_ACTION+"."+suffix+"?action="+ADDRANGE_ACTION+"&elems="+range, null, handleAddElementResponse, "text/xml", null );
}

function addNodesByLabel(label){
	loading++;
	assertLoading();

	getMapRequest ( ADDNODES_ACTION+"."+suffix+"?action="+ADDNODES_BY_LABEL_ACTION+"&elems="+label, null, handleAddElementResponse, "text/xml", null );
}

function addNodesByCategory(catLabel){
	loading++;
	assertLoading();
	getMapRequest ( ADDNODES_ACTION+"."+suffix+"?action="+ADDNODES_BY_CATEGORY_ACTION+"&elems="+escape(catLabel), null, handleAddElementResponse, "text/xml", null );
}

function addMapElemNeigh(id){
	loading++;
	assertLoading();
	getMapRequest ( ADDNODES_ACTION+"."+suffix+"?action="+ADDNODES_NEIG_ACTION+"&elems="+id, null, handleAddElementResponse, "text/xml", null );
}

function addMapElementWithNeighbors(elem){
	loading++;
	assertLoading();

	getMapRequest ( ADDNODES_ACTION+"."+suffix+"?action="+ADDNODES_WITH_NEIG_ACTION+"&elems="+elem, null, handleAddElementResponse, "text/xml", null );
}

function addMapAsNode(mapIds){ 
	loading++;
	assertLoading();

	getMapRequest ( ADDMAPS_ACTION+"."+suffix+"?elems="+mapIds, null, handleAddElementResponse, "text/xml", null );
}

function handleAddElementResponse(data) {
	var addElem;
	var	content = data.content;

	if( (data.success || data.status==200)  
	 && content.indexOf(ADDNODES_ACTION+failed_string) == -1 
	 && content.indexOf(ADDRANGE_ACTION+failed_string) == -1 
	 && content.indexOf(ADDNODES_NEIG_ACTION+failed_string) == -1 
	 && content.indexOf(ADDNODES_WITH_NEIG_ACTION+failed_string) == -1 
	 && content.indexOf(ADDNODES_BY_CATEGORY_ACTION+failed_string) == -1 
	 && content.indexOf(ADDNODES_BY_LABEL_ACTION+failed_string) == -1 
	 && content.indexOf(ADDMAPS_ACTION+failed_string) == -1 
	 ){
	 	addElem = eval("("+content+")");
	 }else{
        alert('Adding Element(s) failed');
		loading--;
		assertLoading();
		enableMenu();
		return;
	}

	// mapsWithLoop
	for(var mapswithLoopIndex in addElem.mapsWithLoop){
		alert("Add Map as Node: Found Loop Adding SubMap with ID " + addElem.mapsWithLoop[mapswithLoopIndex]);
	}

	// test the map element spaces available
	var freePoints = getFreePoints();
	var alerted = false;
	
	while(freePoints.length < addElem.elems.length) {
		if(!alerted)
			alert('Not enough space for all elements; their dimensions will be reduced');
	    var decrResult = decreaseMapElemDim();

	    if(decrResult==false) // space problems
		{
			alert('No space available for elements');
			return;
		}
		for(el in map.mapElements){
			map.mapElements[el].setDimension(mapElemDimension);
		}
		freePoints = getFreePoints();		
	}
	
	//elems
	var icon,point,velem,id,iconName,labelText,avail,status,severity;
	var index = 0;
	for(var elemsIndex in addElem.elems) {
		point = freePoints[index++];
		velem = addElem.elems[elemsIndex];		
		
		id=velem.id+velem.type;
		
		iconName = velem.icon;
		labelText=velem.label;
					
		avail=velem.avail;
		status=velem.status;
		severity=velem.severity;

		icon = new Icon(iconName,MEIconsSortAss[iconName]);
		map.addMapElement(new MapElement(id,icon,labelText, getSemaphoreColorForNode(severity,avail,status), getSemaphoreFlash(severity,avail), point.x, point.y, mapElemDimension, status, avail,severity,useSemaphore))
	}
	var msg = "Added "+index+" nodes to the map";
	if(index==0){
		msg="No nodes added to map"
	}	
	// Links

	var vlink,id1,id2, typology, status,nodeids,numberOfLinks, statusMap;
	for(var linksIndex in addElem.links) {
		vlink = addElem.links[linksIndex];
		id1=vlink.first;
		id2=vlink.second;
		typology=vlink.linkTypeId;
		status=vlink.linkStatusString;
		nodeids=vlink.nodeids;
		numberOfLinks=vlink.numberOfLinks;
		statusMap=vlink.vlinkStatusMap;
		if ( numberOfLinks == 1 )
			map.addLink(id1,id2,typology,numberOfLinks, statusMap,status,LINKSTATUS_COLOR[status], LINK_WIDTH[typology], LINK_DASHARRAY[typology], LINKSTATUS_FLASH[status],deltaLink,nodeids);
		else
			map.addLink(id1,id2,typology,numberOfLinks, statusMap,status,LINKSTATUS_COLOR[status], MULTILINK_WIDTH[typology], MULTILINK_DASHARRAY[typology], LINKSTATUS_FLASH[status],deltaLink,nodeids);		
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
	getMapRequest ( DELETEELEMENT_ACTION+"."+suffix+"?action="+ACTION+"&elems="+id, null, handleDeleteElementResponse, "text/xml", null );
}

function handleDeleteElementResponse(data) {
	var content = data.content;
	if((data.success || data.status==200) 	 
	 && content.indexOf(DELETENODES_ACTION+failed_string) == -1 
	 && content.indexOf(DELETEMAPS_ACTION+failed_string) == -1 ) {
	 	var delElemId = eval("("+content+")");
	 	var velem;
		for(var delElemIndex in delElemId){
			map.deleteMapElement(delElemId[delElemIndex]);
		}
		clearTopInfo();
		writeDownInfo("Deleted selected element(s).");
	} else {
        alert('Deleting Element(s) failed');
	}
	loading--;
	assertLoading();
	enableMenu();

}

function close(){

	loading++;
	assertLoading();

	stopCountdown=true;

	getMapRequest ( CLOSEMAP_ACTION+"."+suffix, null, handleLoadingCloseMap, "text/xml", null );
}

function handleLoadingCloseMap(data) {

	if((data.success || data.status==200) && testOKResponse(CLOSEMAP_ACTION, data.content) ) {

		hideNodesIds = "";
		hasHideNodes = false;

		hideMapsIds = "";
		hasHideMaps = false;

		map.clear();
		mapTabClose(mapidSortAss[currentMapId]);

		currentMapId=MAP_NOT_OPENED;
		currentMapBackGround=DEFAULT_BG_COLOR;
		map.setBGvalue(DEFAULT_BG_COLOR);
		currentMapAccess="";
		currentMapName=""; 
		currentMapOwner=""; 
		currentMapUserlast="";
		currentMapCreatetime="";
		currentMapLastmodtime="";
		currentMapType="";
		savedMapString=getMapString();
	
		reloadGrid();
		clearMapInfo();
		hideMapInfo();
		setTimeout("onClosingActiveTab();",200);
	} else {
	    alert('Close map failed');
	}

	loading--;
	assertLoading();
	
	enableMenu();
}

function newMap(){

	loading++;
	assertLoading();
	
	getMapRequest (  NEWMAP_ACTION+"."+suffix+"?MapWidth="+mapWidth+"&MapHeight="+mapHeight, null, handleLoadingMap, "text/xml", null );
}

function openMap(mapId){ 		

	loading++;
	assertLoading();

	getMapRequest ( OPENMAP_ACTION+"."+suffix+"?MapId="+mapId+"&MapWidth="+mapWidth+"&MapHeight="+mapHeight+"&adminMode="+isAdminMode, null, handleLoadingMap, "text/xml", null );
}

function searchMap(mapIds){
	
	loading++;
	assertLoading();
	var requestUrl=SEARCHMAPS_ACTION+"."+suffix+"?MapWidth="+mapWidth+"&MapHeight="+mapHeight+"&MapElemDimension="+mapElemDimension+"&elems="+mapIds;
	getMapRequest ( requestUrl , null, handleLoadingMap, "text/xml", null );
}


function handleLoadingMap(data) {
	var openingMap;
    var content = data.content;
	if((data.success || data.status==200) 
	&& content.indexOf(OPENMAP_ACTION+failed_string) == -1 
	&& content.indexOf(SEARCHMAPS_ACTION+failed_string) == -1
	&& content.indexOf(NEWMAP_ACTION+failed_string) == -1
	) {
		openingMap=eval("("+content+")");
	} else {
	    alert('Open map failed');
		loading--;
		assertLoading();
		enableMenu();
		return;
	}

	hideNodesIds = "";
	hasHideNodes = false;
	hideMapsIds = "";
	hasHideMaps = false;

	map.clear();
		
	currentMapId=openingMap.id

	if(openingMap.background !="null")
		currentMapBackGround=openingMap.background;
	else currentMapBackGround=DEFAULT_BG_COLOR;

	currentMapAccess=openingMap.accessMode;

	currentMapName=openingMap.name;

	currentMapOwner=openingMap.owner;

	currentMapUserlast=openingMap.userLastModifies;

	currentMapCreatetime=openingMap.createTimeString;

	currentMapLastmodtime=openingMap.lastModifiedTimeString;

	if(openingMap.type !="null")
		currentMapType=openingMap.type;
	else currentMapType="";

	for ( var elem in openingMap.elements ) {
		var velem = openingMap.elements[elem];	
			
		if ( velem.node || velem.map ) {
			var avail=velem.avail;
			var status=velem.status;
			var severity=velem.severity;
 		    var semaphoreColor=getSemaphoreColorForNode(severity,avail,status);
		    var semaphoreFlash = getSemaphoreFlash(severity,avail);

		    var iconName = velem.icon;
    		var icon = new Icon(iconName,MEIconsSortAss[iconName]);
		    
    		labelText=velem.label;
									    
		    map.addMapElement(new MapElement(elem,icon, labelText, semaphoreColor, semaphoreFlash, velem.x, velem.y, mapElemDimension, status, avail,severity,useSemaphore));
		} else if (velem.hideNode ) {
			if (hideNodesIds == "")
				hideNodesIds=velem.id;
			else 
				hideNodesIds=hideNodesIds+','+velem.id;
			hasHideNodes = true;
		} else if ( velem.hideMap ) {
			if (hideMapsIds == "")
				hideMapsIds=velem.id;
			else 
				hideMapsIds=hideMapsIds+','+velem.id;
			hasHideMaps = true;
			
		}
	}
	for ( var link in openingMap.links ) {
		var id1, id2, typology, status,nodeids,numberOfLinks, statusMap;
		var vlink = openingMap.links[link];
		id1=vlink.first;
		id2=vlink.second;
		typology=vlink.linkTypeId;
		status=vlink.linkStatusString;
		nodeids=vlink.nodeids;
		numberOfLinks=vlink.numberOfLinks;
		statusMap=vlink.vlinkStatusMap;
		if ( numberOfLinks == 1 )
			map.addLink(id1,id2,typology,numberOfLinks, statusMap,status,LINKSTATUS_COLOR[status], LINK_WIDTH[typology], LINK_DASHARRAY[typology], LINKSTATUS_FLASH[status],deltaLink,nodeids);
		else
			map.addLink(id1,id2,typology,numberOfLinks, statusMap,status,LINKSTATUS_COLOR[status], MULTILINK_WIDTH[typology], MULTILINK_DASHARRAY[typology], LINKSTATUS_FLASH[status],deltaLink,nodeids);		
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

	if ( currentMapId == SEARCH_MAP && !isAdminMode ) {
	    refreshNodesSetUp();
    }
    
	if (!countdownStarted && !isAdminMode) {	
		startRefreshNodesTime();		    	
	}
	
	enableMenu();
}

function saveMap() {
	saving=true;
	var data="Nodes";
	var firstItem=true;
	for (elemToRender in map.mapElements){
		if ( firstItem ) {
			data+="=";
			firstItem=false;
		} else {
			data+="*";
		}
		var elem = map.mapElements[elemToRender];
		var type = NODE_TYPE;
		var id = "";
		if (elem.isMap()) {
			type=MAP_TYPE;
			id = elem.getMapId();
		} else {
			id = elem.getNodeId();
		}

		data+= id+","+parseInt(elem.x)+","+parseInt(elem.y)+","+elem.icon.name+","+type;
		
	}

	var query="MapId="+currentMapId+"&MapName="+currentMapName+"&MapBackground="+currentMapBackGround+"&MapWidth="+map.getWidth()+"&MapHeight="+map.getHeight();
	postMapRequest ( SAVEMAP_ACTION+"."+suffix+"?"+query, data, handleSaveResponse, "text/xml", null );
}

function handleSaveResponse(data) {
	var savingMap;
    var content = data.content;
	if((data.success || data.status==200) 
	&& content.indexOf(SAVEMAP_ACTION+failed_string) == -1
	) {
		savingMap=eval("("+content+")");
	} else {
		alert('Failed to save map');	
		showHistory();
		clearDownInfo();
		enableMenu();
		return;
	}

	currentMapId=savingMap.id;
	currentMapAccess=savingMap.accessMode;
	currentMapOwner=savingMap.owner;
	currentMapUserlast=savingMap.userLastModifies;
	currentMapCreatetime=savingMap.createTimeString;
	currentMapLastmodtime=savingMap.lastModifiedTimeString;

	if (currentMapType == "A")
		currentMapType="S";

	loadMaps();
	
	savedMapString = getMapString();			
	saveMapInHistory();
	top.$j.history.load(currentMapId);
	
	writeMapInfo();
	showHistory();
	writeDownInfo("Map '" +currentMapName+"' saved.");

	enableMenu();			
				
}

function deleteMap(){
	getMapRequest ( DELETEMAP_ACTION+"."+suffix, null, handleDeleteMapResponse, "text/xml", null );
}

function handleDeleteMapResponse(data) {
	if((data.success || data.status==200) && testOKResponse(DELETEMAP_ACTION, data.content) ) {
		
		mapTabClose(mapidSortAss[currentMapId]);
		map.clear();
		deleteMapInHistory();		
		currentMapId=MAP_NOT_OPENED;
		currentMapBackGround=DEFAULT_BG_COLOR;
		currentMapAccess="";
		currentMapName=""; 
		currentMapOwner=""; 
		currentMapUserlast="";
		currentMapCreatetime="";
		currentMapLastmodtime="";
	
		loadMaps();
		setTimeout("onClosingActiveTab();",200);
	
		writeDownInfo("Map deleted.");
		clearMapInfo();
		hideMapInfo();
		showHistory();

	} else {
		alert('Failed to delete map');
		clearDownInfo();
	}
	enableMenu();		
}

function clearMap(){
	
	if (map.getMapElementsSize()==0){
	  	 alert('Map contains no nodes');
		return;
	}

   	loading++;
	assertLoading();

    getMapRequest (CLEAR_ACTION+"."+suffix, null, handleClearMapResponse, "text/xml", null );

}

function handleClearMapResponse(data) {
	
	if((data.success || data.status==200) && testOKResponse(CLEAR_ACTION, data.content) ) {
	   	map.clear();
	   	reloadGrid();
		writeDownInfo("Map Cleared");
	} else {
        alert('Failed to clear map element(s)');
		clearDownInfo();
	}

	loading--;
	assertLoading();
	enableMenu();
   	
}

function switchRole(){
	getMapRequest ( SWITCH_MODE_ACTION+"."+suffix+"?adminMode="+isAdminMode, null, handleSwitchRole, "text/xml", null );
}


function handleSwitchRole(data) {
	if((data.success || data.status==200) && testOKResponse(SWITCH_MODE_ACTION, data.content) ) {
		isAdminMode = !isAdminMode;
		
		if (isAdminMode) {
			showMapInfo();
			showHistory();
			removeLegend();
			for (var mapElemId in map.mapElements) {
				map.mapElements[mapElemId].setSemaphoreColor(getSemaphoreColorForNode(0,0,0));
				map.mapElements[mapElemId].setSemaphoreFlash(getSemaphoreFlash(0,0));
			}
		}else{
			addLegend();
			if (currentMapId!=MAP_NOT_OPENED && currentMapId!=NEW_MAP) {
				refreshNodesSetUp();
			}
		}
	} else {
	    alert('Failed to switch the role');
	}
	enableMenu();		
}

function refreshNodes(){
	if(map!=undefined && currentMapId!=MAP_NOT_OPENED ){
	    assertRefreshing(1);
	    
		if(reloadMap){
			getMapRequest ( REFRESH_BASE_ACTION+"."+suffix+"?action="+RELOAD_ACTION,  null, handleRefreshNodesResponse, "text/xml", null );
		}else{
			getMapRequest ( REFRESH_BASE_ACTION+"."+suffix+"?action="+REFRESH_ACTION, null, handleRefreshNodesResponse, "text/xml", null );
		}
	}
}


function handleRefreshNodesResponse(data) {
	assertRefreshing(0);
	
	if (!countdownStarted)
    	startRefreshNodesTime();
	
	enableMenu();
	
	var content = data.content;
	var refreshResponse;
	if((data.success || data.status==200) 
	&& content.indexOf(RELOAD_ACTION+failed_string) == -1 
	&& content.indexOf(REFRESH_ACTION+failed_string) == -1 ) {
		refreshResponse=eval("("+content+")");
	} else {
        alert('Refresh failed');
		return;
	}

	map.clearLinks();

	if(reloadMap)
		map.clear();

	hideNodesIds = "";
	hasHideNodes = false;
	
	hideMapsIds = "";
	hasHideMaps = false;
	
	var icon,velem,id,iconName,labelText,avail,status,severity,posx,posy;
	for(var elemsIndex in refreshResponse.elems) {
		velem = refreshResponse.elems[elemsIndex];		
		
		id=velem.id+velem.type;
		
		iconName = velem.icon;
		labelText=velem.label;
					
		avail=velem.avail;
		status=velem.status;
		severity=velem.severity;
	
		var testHideNode = id.indexOf('H');
		var testHideMap = id.indexOf('W');
		if ( testHideNode == -1 && testHideMap == -1 ) {
    		icon = new Icon(iconName,MEIconsSortAss[iconName]);
    		var deleted = true;

			if(reloadMap){
				posx=velem.x;
				posy=velem.y;
			}else{
				var mapElem = map.mapElements[id];
				posx=mapElem.x;
				posy=mapElem.y;
				var deleted = map.deleteMapElement(id);
			}
			if (deleted){
				map.addMapElement(new MapElement(id,icon, labelText, getSemaphoreColorForNode(severity,avail,status), getSemaphoreFlash(severity,avail), posx, posy, mapElemDimension, status, avail,severity,useSemaphore));
			}
		} else if (testHideMap == -1 ){
			var nodeid = id.substring(0,testHideNode);
			if (hideNodesIds == "")
				hideNodesIds=nodeid;
			else 
				hideNodesIds=hideNodesIds+','+nodeid;
				hasHideNodes = true;
		} else {
			var mapid = id.substring(0,testHideMap);
			if (hideMapsIds == "")
				hideMapsIds=mapid;
			else 
				hideMapsIds=hideMapsIds+','+mapid;
			hasHideMaps = true;				
		}
	}
	// Links		
	var vlink,id1,id2, typology, status,nodeids,numberOfLinks, statusMap;
	for(var linksIndex in refreshResponse.links) {
		vlink = refreshResponse.links[linksIndex];
		id1=vlink.first;
		id2=vlink.second;
		typology=vlink.linkTypeId;
		status=vlink.linkStatusString;
		nodeids=vlink.nodeids;
		numberOfLinks=vlink.numberOfLinks;
		statusMap=vlink.vlinkStatusMap;
		if ( numberOfLinks == 1 )
			map.addLink(id1,id2,typology,numberOfLinks, statusMap,status,LINKSTATUS_COLOR[status], LINK_WIDTH[typology], LINK_DASHARRAY[typology], LINKSTATUS_FLASH[status],deltaLink,nodeids);
		else
			map.addLink(id1,id2,typology,numberOfLinks, statusMap,status,LINKSTATUS_COLOR[status], MULTILINK_WIDTH[typology], MULTILINK_DASHARRAY[typology], LINKSTATUS_FLASH[status],deltaLink,nodeids);		
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

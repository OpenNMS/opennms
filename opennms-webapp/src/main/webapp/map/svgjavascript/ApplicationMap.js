var suffix = "map";
var failed_string = "Failed";

function testResponse(action, response){
		var tmpStr=response.substring(0,action.length+2);
		if(tmpStr==(action+"OK"))
			return true;
		return false;
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

function loadDefaultMap(){
	loading++;
	assertLoading();
	getMapRequest ( LOADDEFAULTMAP_ACTION+"."+suffix, null, handleLoadDefaultMapResponse, "text/xml", null );	
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
	getMapRequest ( LOADMAPS_ACTION+"."+suffix, null, handleLoadMapsResponse, "text/xml", null );
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
	mapLabels = [" "];
    var mapSorts = [null];
    var mapids = [null];
    var st = str.split("&");
	if(str.indexOf("+")>=0){
		for(var k=0;k<st.length;k++){
			var nodeToken = st[k];
			var nodeST = nodeToken.split("+");
			var name,id,owner;
				id=nodeST[0];
			name=nodeST[1];
			owner=nodeST[2];
			var tempStr = mapLabels.join(".");
			while(	tempStr.indexOf(name) != -1 ){
				name=name+" ";
			}
			var tmpMap = new ElemMap(id, name, owner);
			mapids.push(id);
			mapLabels.push(name);
			mapSorts.push(tmpMap);
		}
	}
	mapSortAss = assArrayPopulate(mapLabels,mapSorts);	
	mapidSortAss = assArrayPopulate(mapids,mapLabels);	
	loading--;	
	assertLoading();
	mapsLoaded=true;
	// Load also the maps label
	loadLabelMap();
	
}

function loadNodes(){
	loading++;
	assertLoading();
	getMapRequest (LOADNODES_ACTION+"."+suffix , null, handleLoadNodesResponse, "text/xml", null );
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
	nodeLabels = [" "];
    var nodeSorts = [null];
    var nodeids = [null];
	if(str.indexOf("+")>=0){
		for(var k=0;k<st.length;k++){
			var nodeToken = st[k];
			var nodeST = nodeToken.split("+");
			var id =nodeST[0];
			var label = nodeST[1];
			// the second label should be the ip address but for the moment we are still using the FQDN
			var tmpNode = new Node(id,label,label);
			
			nodeids.push(id);
			nodeLabels.push(label);
			nodeSorts.push(tmpNode);
		}
	}
		
	nodeSortAss = assArrayPopulate(nodeLabels,nodeSorts);	
	nodeidSortAss = assArrayPopulate(nodeids,nodeSorts);	
	loading--;
	assertLoading();
	nodesLoaded=true;
}

function loadLabelMap(){
	loading++;
	assertLoading();
	getMapRequest (LOADLABELMAP_ACTION+"."+suffix , null, handleLoadLabelMapResponse, "text/xml", null );
}

function handleLoadLabelMapResponse(data) {
	var content= data.content;
	if((data.success || data.status==200) && content.indexOf(failed_string) == -1) {
	    
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
									enableMenu();
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
		if (nodeST.length > 6) {

			var id,iconName,labelText,avail,status,severity;
			//read the information of the map (id, name, ecc.)
			
			id=nodeST[0];
			iconName=nodeST[1];
			labelText=nodeST[2];
			avail=nodeST[3];
			status=nodeST[4];
			severity=nodeST[5];
					
			var semaphoreColor=getSemaphoreColorForNode(severity,avail,status);
			var semaphoreFlash = getSemaphoreFlash(severity,avail);
			//Adding only the node label without domain
			var index = labelText.indexOf('.');
			if (index > 0)
				labelText=labelText.substr(0,index);
			
			newElem= new MapElement(id,iconName, labelText, semaphoreColor, semaphoreFlash, 0, 0, mapElemDimension, status, avail,severity)
			nodesToAdd.push(newElem);

		}
		// Links
		if (nodeST.length == 6) { // when find links into server response...

			var id1,id2, typology, status,nodeid1,nodeid2;
			id1=nodeST[0];
			id2=nodeST[1];
			typology=nodeST[2];
			status=nodeST[3];
			nodeid1=nodeST[4];
			nodeid2=nodeST[5];
			var linkToAdd = id1+"-"+id2+"-"+typology+"+"+LINKSTATUS_COLOR[status]+"+"+ LINK_WIDTH[typology]+"+"+LINK_DASHARRAY[typology]+"+"+LINKSTATUS_FLASH[status]+"+"+nodeid1+"+"+nodeid2+"+"+status;
			linksToAdd.push(linkToAdd);
			
		}
		
	}

	var freePoints = getFreePoints();
	var alerted = false;
	while(freePoints.length<nodesToAdd.length) {

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
	}
	
	for(el in map.mapElements){
		map.mapElements[el].setDimension(mapElemDimension);
	}
	
	var index = 0;
	for(nd in nodesToAdd){	
		var point = freePoints[index++];

		var me = new MapElement(nodesToAdd[nd].id, nodesToAdd[nd].icon, nodesToAdd[nd].label.text, nodesToAdd[nd].semaphore.svgNode.getAttribute("fill"), getSemaphoreFlash(nodesToAdd[nd].severity,nodesToAdd[nd].avail), point.x, point.y, mapElemDimension, nodesToAdd[nd].status, nodesToAdd[nd].avail, nodesToAdd[nd].severity);
		map.addMapElement(me);
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
		var nodeid1 = params[5];
		var nodeid2 = params[6];
		var status = params[7];
		map.addLink(id1,id2,typo,status,color,width,da,flash,deltaLink,nodeid1,nodeid2);
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
	getMapRequest ( DELETEELEMENT_ACTION+"."+suffix+"?action="+ACTION+"&elems="+id, null, handleDeleteNodeResponse, "text/xml", null );
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

function close(){

	hideNodesIds = "";
	hasHideNodes = false;

	hideMapsIds = "";
	hasHideMaps = false;

	map.clear();

	loading++;
	assertLoading();

	stopCountdown=true;

	getMapRequest ( CLOSEMAP_ACTION+"."+suffix, null, handleLoadingCloseMap, "text/xml", null );
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
	currentMapType="";
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

function newMap(){

	hideNodesIds = "";
	hasHideNodes = false;

	hideMapsIds = "";
	hasHideMaps = false;

	map.clear();
	
	loading++;
	assertLoading();
	
	getMapRequest (  NEWMAP_ACTION+"."+suffix+"?MapWidth="+mapWidth+"&MapHeight="+mapHeight, null, handleLoadingMap, "text/xml", null );
}

function openMap(mapId){ 		

	map.clear();

	loading++;
	assertLoading();

	getMapRequest ( OPENMAP_ACTION+"."+suffix+"?MapId="+mapId+"&MapWidth="+mapWidth+"&MapHeight="+mapHeight+"&adminMode="+isAdminMode, null, handleLoadingMap, "text/xml", null );
}

function searchMap(mapIds){
 
 	hideNodesIds = "";
	hasHideNodes = false;

	hideMapsIds = "";
	hasHideMaps = false;

	map.clear();
	
	loading++;
	assertLoading();
	var requestUrl=SEARCHMAPS_ACTION+"."+suffix+"?MapWidth="+mapWidth+"&MapHeight="+mapHeight+"&MapElemDimension="+mapElemDimension+"&elems="+mapIds;
	getMapRequest ( requestUrl , null, handleLoadingMap, "text/xml", null );
}


function handleLoadingMap(data) {
	var openingMap;
    var failed = false;
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
		    map.addMapElement(new MapElement(elem,velem.icon, velem.label, semaphoreColor, semaphoreFlash, velem.x, velem.y, mapElemDimension, status, avail,severity));
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
		var id1, id2, typology, status,nodeid1,nodeid2;
		var vlink = openingMap.links[link];
		id1=vlink.first;
		id2=vlink.second;
		typology=vlink.linkTypeId;
		status=vlink.linkStatusString;
		nodeid1=vlink.firstNodeid;
		nodeid2=vlink.secondNodeid;

		map.addLink(id1,id2,typology,status,LINKSTATUS_COLOR[status], LINK_WIDTH[typology], LINK_DASHARRAY[typology], LINKSTATUS_FLASH[status],deltaLink,nodeid1,nodeid2);
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

		data+= id+","+parseInt(elem.x)+","+parseInt(elem.y)+","+elem.icon+","+type;
		
	}

	var query="MapId="+currentMapId+"&MapName="+currentMapName+"&MapBackground="+currentMapBackGround+"&MapWidth="+map.getWidth()+"&MapHeight="+map.getHeight();
	postMapRequest ( SAVEMAP_ACTION+"."+suffix+"?"+query, data, handleSaveResponse, "text/xml", null );
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

	currentMapId=parseInt(answerST[0]);
	currentMapBackGround=answerST[1];
	currentMapAccess=answerST[2];
	currentMapName=answerST[3];
	currentMapOwner=answerST[4];
	currentMapUserlast=answerST[5];
	currentMapCreatetime=answerST[6];
	currentMapLastmodtime=answerST[7];
	if (currentMapType == "A")
		currentMapType="S";

	loadMaps();
	
	savedMapString = getMapString();			
	saveMapInHistory();
	
	writeMapInfo();
	showHistory();
	writeDownInfo("Map '" +currentMapName+"' saved.");

	enableMenu();			
				
}

function deleteMap(){
	getMapRequest ( DELETEMAP_ACTION+"."+suffix, null, handleDeleteResponse, "text/xml", null );
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

    getMapRequest (CLEAR_ACTION+"."+suffix, null, handleClearMapResponse, "text/xml", null );

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
	getMapRequest ( SWITCH_MODE_ACTION+"."+suffix+"?adminMode="+isAdminMode, null, handleSwitchRole, "text/xml", null );
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
	
	if (isAdminMode) {
		showMapInfo();
		showHistory();
		removeLegend();
		for (mapElemId in map.mapElements) {
			map.mapElements[mapElemId].setSemaphoreColor(getSemaphoreColorForNode(0,0,0));
			map.mapElements[mapElemId].setSemaphoreFlash(getSemaphoreFlash(0,0));
		}
		map.render();
	}else{
		addLegend();
		if (currentMapId!=MAP_NOT_OPENED && currentMapId!=NEW_MAP) {
			refreshNodesSetUp();
		}
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

	hideNodesIds = "";
	hasHideNodes = false;
	
	hideMapsIds = "";
	hasHideMaps = false;
	
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
		if (nodeST.length > 6) {

			var id,iconName,labelText,avail,status,severity,posx,posy;
			
			id=nodeST[0];
			iconName=nodeST[1];
			labelText=nodeST[2];
			avail=nodeST[3];
			status=nodeST[4];
			severity=nodeST[5];

			var semaphoreColor=getSemaphoreColorForNode(severity,avail,status);
			var semaphoreFlash = getSemaphoreFlash(severity,avail);

			var testHideNode = id.indexOf('H');
			var testHideMap = id.indexOf('W');
			if ( testHideNode == -1 && testHideMap == -1 ) {

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
		
		if (nodeST.length == 6) {
			var id1,id2,typology, status,nodeid1,nodeid2;
			id1=nodeST[0];
			id2=nodeST[1];
			typology=nodeST[2];
			status=nodeST[3];
			nodeid1=nodeST[4];
			nodeid2=nodeST[5];
			
			map.addLink(id1,id2,typology,status,LINKSTATUS_COLOR[status], LINK_WIDTH[typology], LINK_DASHARRAY[typology], LINKSTATUS_FLASH[status],deltaLink,nodeid1,nodeid2);
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

// xlink namespace
function ApplicationMap()
{
	this.svgnsXLink = "http://www.w3.org/1999/xlink";
}

// returns the mouse coordinates as an SVGPoint
ApplicationMap.prototype.getMouse = function(evt) {
	var position =  mapSvgDocument.documentElement.createSVGPoint();
	position.x = evt.clientX;
	position.y = evt.clientY;
	return position;
}

var application = new ApplicationMap();

/* *************** functions for the management of the client application map ********************************************** */
		
		
//clear the actions started (if there are one action started)
function clearActionsStarted(){
	if(deletingMapElem==true){
		deletingMapElem=false;
	}
	if(addingMapElemNeighbors==true){
		addingMapElemNeighbors=false;
	}
}
		
//functions for matching ip with range in input
function ipmatch(ip, ipLike){
	var ottectsLike = ipLike.split(".");
	var ottectsIp = ip.split(".");
	return (ottectMatch(parseInt(ottectsIp[0]),ottectsLike[0]) && ottectMatch(parseInt(ottectsIp[1]), ottectsLike[1]) && ottectMatch(parseInt(ottectsIp[2]),ottectsLike[2]) && ottectMatch(parseInt(ottectsIp[3]),ottectsLike[3]) )

}

function ottectMatch(ott, ottLike){
	//alert(ott+" "+ ottLike);
	try{
	if(ottLike=="*"){
		if(ott<=255 && ott>=0)

		 	return true;

		return false;
	}
	if(ottLike.indexOf("-")>=0){
		var range = ottLike.split("-")
		var start=parseInt(range[0]);
		var end=parseInt(range[1]);
		if(start>end  || start>255 || end >255 ||start<0 || end<0)
			 return false;
		if(ott>=start && ott<=end)
			return true;
		return false;
	}
	if(ott==parseInt(ottLike))
		return true;
	return false;
	}catch(e){
		return false;
	}
}

function isValidOttect(ott){
	if(ott=="*"){
		return true;
	}
	if(ott.indexOf('-')>=0){
		var ottRange = ott.split('-');
		if(ottRange.length>2)
			return false;
		var start=parseInt(ottRange[0]);
		var end=parseInt(ottRange[1]);
		if(start<=end  && start<=255 && end <=255 && start>=0 && end>=0)
			 return true;
		
	}
	if(parseInt(ott)>=0 && parseInt(ott)<=255)
		return true;
	return false;
}

function isValidRange(range){
	var ottects = range.split(".");
	if(ottects.length!=4){
		return false;
	}
	return (isValidOttect(ottects[0]) && isValidOttect(ottects[1]) && isValidOttect(ottects[2]) && isValidOttect(ottects[3]));
}

//create a string representing the status of the map
// in the moment in wich is invoked. This function is used to test if the 
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
			query+= elemId+","+elem.x+","+elem.y+","+elem.icon;
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

/*
 param createList: if true, the handler have to create the maps list. Nothing, otherwise.
*/
var createListOfMaps=false;
var createListOfMapsToAdd = false;
function LoadMaps(createList, createListOfMaps2Add){
	if(createList!=undefined && createList==true)
		createListOfMaps=true;
		
	if(createListOfMaps2Add!=undefined && createListOfMaps2Add==true)
		createListOfMapsToAdd=true;
		
	loading++;
	assertLoading();
	postURL ( baseContext+"LoadMaps."+suffix+"?action="+LOADMAPS_ACTION, null, handleLoadMapsResponse, "text/xml", null );
}

function handleLoadMapsResponse(data) {
	
	var str = '';
	if(data.success) {
		maps= [" "];
		mapSorts = [null];
		str = data.content;
		if(testResponse(LOADMAPS_ACTION, str)){
			str=str.substring(LOADMAPS_ACTION.length+2,str.length);
		}else{
		    alert('Loading Maps failed 1');
	        	loading--;	
			assertLoading();
			hideAll();
  	        	disableMenu();			
			return;
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
		if(createListOfMaps)
			addMapsList();	
		createListOfMaps=false;	
		if(createListOfMapsToAdd)
			addMapAsNodeList();	
		createListOfMapsToAdd=false;			
	} else {
		alert('Loading Maps failed');
		loading--;	
		assertLoading();
		hideAll();	
 	    disableMenu();			
	}
}

var createAddNodeList = false;
var createAddWithNeighList = false;
function LoadNodes(id,textVal,changeType){
	//loading++;
	createAddNodeList = false;
	createAddWithNeighList = false;
	//assertLoading();
	var url = baseContext+"LoadNodes."+suffix+"?action="+LOADNODES_ACTION;
	if(changeType!=undefined ){
		if(changeType!="release" && textVal!=""){
			url+="&like="+textVal;
			if(id=="NodeLabelFilterBox")
				createAddNodeList = true;
			else if(id=="NodeWithNeighLabelFilterBox")
					createAddWithNeighList = true;
			postURL (url , null, handleLoadNodesResponse, "text/xml", null );
		}
	}else{
		postURL (url , null, handleLoadNodesResponse, "text/xml", null );
	}
}

function handleLoadNodesResponse(data) {
	var str = '';
	if(data.success) {
		nodes= [" "];
		nodeSorts = [null];
		str = data.content;
		if(testResponse(LOADNODES_ACTION, str)){
			str=str.substring(LOADNODES_ACTION.length+2,str.length);
		}else{
		     alert('Load Nodes failed');
		     hideAll();
			 //loading--;	
		     //assertLoading();		
		     disableMenu();		          
			return;
		}
		var st = str.split("&");
		if(str.indexOf("+")>=0){
			for(var k=0;k<st.length;k++){
				var nodeToken = st[k];
				var nodeST = nodeToken.split("+");
				var counter=0;
				var label,id;
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
					counter++;
				}
				var tmpNode = new Node(id, label);
				nodes.push(label);
				nodeSorts.push(tmpNode);
			}
		}
		
		nodeSortAss = assArrayPopulate(nodes,nodeSorts);	
		//loading--;
		//assertLoading();
		nodesLoaded=true;
	} else {
		alert('Loading Nodes has failed');
		hideAll();
		//loading--;	
		//assertLoading();
        disableMenu();		
	}
	if(createAddNodeList){
		addMapElementList();
	}	
	if(createAddWithNeighList){
		addMapElementNeighList();
	}
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
	postURL ( baseContext+"AddNodes."+suffix+"?action="+ADDNODES_ACTION+"&elems="+elem, null, handleAddElementResponse, "text/xml", null );
}

function addRangeOfNodes(){
	var range = menuSvgDocument.getElementById("NodeRangeBoxText").firstChild.nodeValue;
	if(!isValidRange(range)){
		alert('Range not valid!');
		return;
		}
	loading++;
	assertLoading();
	disableMenu();
	postURL ( baseContext+"AddNodes."+suffix+"?action="+ADDRANGE_ACTION+"&elems="+range, null, handleAddElementResponse, "text/xml", null );
}

function addNodesByLabel(){
	var label = menuSvgDocument.getElementById("NodeLabelBoxText").firstChild.nodeValue;
	if(label==""){
		alert('Invalid Label (must not be blank)');
		return;
		}
	loading++;
	assertLoading();
	disableMenu();
	postURL ( baseContext+"AddNodes."+suffix+"?action="+ADDNODES_BY_LABEL_ACTION+"&elems="+label, null, handleAddElementResponse, "text/xml", null );
}

function addNodesByCategory(){
	if(selectedCategoryInList==0 )  {
		return;
	}
	loading++;
	assertLoading();
	disableMenu();
	var catLabel = categorySortAss[selectedCategoryInList];
	postURL ( baseContext+"AddNodes."+suffix+"?action="+ADDNODES_BY_CATEGORY_ACTION+"&elems="+escape(catLabel), null, handleAddElementResponse, "text/xml", null );
}

function addMapElemNeigh(id){
	loading++;
	assertLoading();
	disableMenu();
	postURL ( baseContext+"AddNodes."+suffix+"?action="+ADDNODES_NEIG_ACTION+"&elems="+id, null, handleAddElementResponse, "text/xml", null );
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
	postURL ( baseContext+"AddNodes."+suffix+"?action="+ADDNODES_WITH_NEIG_ACTION+"&elems="+elem, null, handleAddElementResponse, "text/xml", null );
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
		var childNode = menuSvgDocument.getElementById("DownInfoText");
		if (childNode)
			menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
		menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Cannot add map to itself" +
		"</text>",menuSvgDocument));			
		return;
	}
	loading++;
	assertLoading();
	disableMenu();
	postURL ( baseContext+"AddMaps."+suffix+"?action="+ADDMAPS_ACTION+"&elems="+mapId, null, handleAddElementResponse, "text/xml", null );
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
	postURL ( baseContext+"DeleteElements."+suffix+"?action="+ACTION+"&elems="+id, null, handleDeleteNodeResponse, "text/xml", null );
}

function getElemInfo(elemMap)
{
	loading++;
	assertLoading();
	var ACTION = LOAD_NODES_INFO_ACTION;
	var id = -1;
	var type = "";
			
	if (elemMap.isMap()) {
		id = elemMap.getMapId();
		type=MAP_TYPE;
	}
	if (elemMap.isNode()) {
		id = elemMap.getNodeId();
		type=NODE_TYPE;
	}
	postURL ( "LoadInfos?action="+ACTION+"&elem="+id+"&type="+type, null, handleLoadInfosResponse, "text/xml", null );
}

function handleDeleteNodeResponse(data) {
	var str = '';
	if(data.success) {
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
		var counter=0;
		//Manage errors
		var id;
		while(counter< nodeST.length){
			var tmp = nodeST[counter];
			//read the mapid
			if(counter==0) 
			{
				id=tmp;
			}
			counter++;
		}
		map.deleteMapElement(id);
	}
	loading--;
	assertLoading();
	clearTopInfo();
	enableMenu();
	var childNode = menuSvgDocument.getElementById("DownInfoText");		
	if (childNode)
		menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
	menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">" +
		"<tspan x=\"5\" dy=\"0\">Deleted selected element(s).</tspan>" +
		"</text>",menuSvgDocument));	
}


function handleLoadInfosResponse(data) {
	var str = '';
	if(data.success) {
		str = data.content;
		if(testResponse(LOAD_NODES_INFO_ACTION, str)){
			str=str.substring(LOAD_NODES_INFO_ACTION.length+2,str.length);
		}else{
			//if load infos failed, do nothing 
			loading--;
			assertLoading();
			enableMenu();
			return;			
		}
	} else {
    	loading--;
		assertLoading();
		enableMenu();
		return;
	}
	var tiText = menuSvgDocument.getElementById("TopInfoText");
	if(tiText!=null){ // if TopInfoText svg node exists, continue to write element infos.
		var infos ="";	
		var st = str.split("+");
		for(var k=1;k<st.length;k++){
			var nodeToken = st[k];
			infos+="<tspan x=\"3\" dy=\"15\" font-size=\"9\">"+nodeToken+"</tspan>";		
		}
	//	var nodeToken = st[1];
	//	var labelText = menuSvgDocument.getElementById("TopInfoLabelText");
	//	labelText.firstChild.nodeValue+=" ("+nodeToken+")";
		tiText.appendChild(parseXML(infos,menuSvgDocument));
	}
	loading--;
	assertLoading();	
}

function handleAddElementResponse(data) {
	var str = '';
	if(data.success) {
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
		var counter=0;
		//Manage errors
		if (nodeST.length == 1) {
			var iderror;
			while(counter< nodeST.length){
				var tmp = nodeST[counter];
				if(counter==0) 
				{
					iderror=tmp;
				}
				counter++;
				// manage loop found error
				//loopfound
				var tmpStr=iderror.substring(0,9);
				if(tmpStr=="loopfound"){
					alert("Add Map as Node: Found Loop Adding SubMap with ID " + iderror.substring(9));
				}
			}			
		}
		//MapElement
		if (nodeST.length > 4) {

			var id,iconName=DEFAULT_ICON,labelText="",avail=100,status=0,severity=0;
			
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
						iconName=tmp;
					}
				if(counter==2)
					{
					if(tmp!="null")
						labelText=tmp;
					}

				if(counter==3)
					{
					if(tmp!="null")
						avail=tmp;
					}
					
				if(counter==4)
					{
					if(tmp!="null")
						status=tmp;
					}
				
				if(counter==5)
					{
					if(tmp!="null")
						severity=tmp;
					}
					
				//alert(counter);	
				counter++;
			}
			var semaphoreColor=getSemaphoreColorForNode(severity,avail,status);
			var semaphoreFlash = getSemaphoreFlash(severity,avail);
			//alert("add element " + id);
			newElem= new MapElement(id,iconName, labelText, semaphoreColor, semaphoreFlash, 0, 0, mapElemDimension, status, avail,severity)
			nodesToAdd.push(newElem);

			//reloadGrid();
			// reloadgrid se non esiste lo spazio allora !!!!!!! tratto
		}
		// Links
		if (nodeST.length == 4) { // when find links into server response...

			var id1,id2, typology, status, flash;
			
			while(counter< nodeST.length){
				var tmp = nodeST[counter];
				
				//read the information of the map (id, name, ecc.)
			
				if(counter==0) 
				{
					id1=tmp;
				}
				if(counter==1)
				{
					id2=tmp;
				}
				if(counter==2)
				{
					typology=tmp;
				}
				if(counter==3)
				{
					status=tmp;
				}
				counter++;
			}
		
			var linkToAdd = id1+"-"+id2+"-"+typology+"+"+LINKSTATUS_COLOR[status]+"+"+ LINK_WIDTH[typology]+"+"+LINK_DASHARRAY[typology]+"+"+LINKSTATUS_FLASH[status];
			linksToAdd.push(linkToAdd);
			
			//map.addLink(id1,id2,"green",1);
		}
		
	}
	if(!nodesAdded){
		nodesAdded=true;
		var freePoints = null;
		var alerted = false;
		do{     // try to add the elements
		   //reloadGrid();
		   freePoints = getFreePoints();
		   //alert("freePoints.length="+freePoints.length+"  nodesToAdd.length="+nodesToAdd.length);
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
	clearTopInfo();
	var msg = "Added "+nodesToAdd.length+" nodes to the map";
	if(nodesToAdd.length==0){
		msg="No nodes added to map"
	}
	var childNode = menuSvgDocument.getElementById("DownInfoText");		
	if (childNode)
		menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
	menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">" +
		"<tspan x=\"5\" dy=\"0\">"+msg+"</tspan>" +
		"</text>",menuSvgDocument));	

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
	
	map.render();
	reloadGrid();
	loading--;
	assertLoading();
//	savedMapString=getMapString();
	enableMenu();
		
}

function setMapElemDim(){
	var dim = parseInt(MapElemDimSortAss[selectedMapElemDimInList]);
	setMapElemDimension(dim);
}
	
function setMapElemDimension(dim){
	mapElemDimension=dim;
	
	for(el in map.mapElements){
		map.mapElements[el].setDimension(mapElemDimension);
	}
	map.render();	
	
	
	
	clearTopInfo();
	clearDownInfo();
}

function newMap(){
	if(savedMapString!=getMapString() && currentMapId!=MAP_NOT_OPENED && isAdminMode) {
	 if(confirm('Map \''+currentMapName+'\' not saved, proceed anyway?')==false)
	 	return;
	}
	map.clear();
	clearMapInfo();
	clearTopInfo();
	clearDownInfo();
	hideMapInfo();
	loading++;
	assertLoading();
	disableMenu();
	postURL (  baseContext+"NewMap."+suffix+"?action="+NEWMAP_ACTION+"&MapId="+NEW_MAP+"&MapWidth="+map.getWidth()+"&MapHeight="+map.getHeight(), null, handleLoadingMap, "text/xml", null );
}

function openMap(mapId){ 
	
	if(!refreshingMapElems){
			if(savedMapString!=getMapString() && currentMapId!=MAP_NOT_OPENED && isAdminMode) {
			 if(confirm('Map \''+currentMapName+'\' not saved, proceed anyway?')==false)
				return;
			}	
			map.clear();
			hideMapInfo();
			loading++;
			assertLoading();
			disableMenu();
			
			if(mapId!=undefined && (typeof mapId) != "object")
				postURL ( baseContext+"OpenMap."+suffix+"?action="+OPENMAP_ACTION+"&MapId="+mapId, null, handleLoadingMap, "text/xml", null );
			else if(selectedMapInList!=0){
				var mapIdToOpen = mapSortAss[selectedMapInList].id;
				postURL ( baseContext+"OpenMap."+suffix+"?action="+OPENMAP_ACTION+"&MapId="+mapIdToOpen, null, handleLoadingMap, "text/xml", null );
			}else{
				//open the map choosed by the user in the index page
				postURL ( baseContext+"OpenMap."+suffix+"?action="+OPENMAP_ACTION, null, handleLoadingMap, "text/xml", null );				
			}
		}

}

function handleLoadingMap(data) {
	var str = '';
	var action = null;
	//reset zoom and pan
	reset();
	if(data.success) {
		str = data.content;
		if(testResponse(OPENMAP_ACTION, str)){
			str=str.substring(OPENMAP_ACTION.length+2,str.length);
			action = OPENMAP_ACTION;
			selectedMapInList=0;
		}else{		
			if(testResponse(NEWMAP_ACTION, str)){
				str=str.substring(NEWMAP_ACTION.length+2,str.length);
				action = NEWMAP_ACTION;
			}else{				
				if(testResponse(CLOSEMAP_ACTION, str)){
					str=str.substring(CLOSEMAP_ACTION.length+2,str.length);
					action = CLOSEMAP_ACTION;
				}else{			
					alert('Failed to load map');
					loading--;
					assertLoading();
					return;
					}
			}
		}
	} else {
		        alert('Failed to reset zoom / pan controls');
			loading--;
			assertLoading();
			return;
	}
	//alert(str);
	var st = str.split("&");
	for(var k=0;k<st.length;k++){
		var nodeToken = st[k];
		var nodeST = nodeToken.split("+");
		var counter=0;
		if(k==0){
			while(counter< nodeST.length){
				var tmp = nodeST[counter];

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
			if(action==CLOSEMAP_ACTION){
				hideMapInfo();
				menuSvgDocument.getElementById("history").getStyle().setProperty('display', 'none');
				mapHistory=new Array();
				mapHistoryName=new Array();
				mapHistoryIndex = 0;
				
			}else{
				//save the map in the map history
				saveMapInHistory();
				viewMapInfo();
				menuSvgDocument.getElementById("history").getStyle().setProperty('display', 'inline');				
			}
		}	
		if (k>0 && nodeST.length > 4) {

			var id,x=0,y=0,iconName=DEFAULT_ICON,labelText="",avail=100,status=0,severity=0;
			
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
						iconName=tmp;
					}
				if(counter==4)
					{
					if(tmp!="null")
						labelText=tmp;
					}

				if(counter==5)
					{
					if(tmp!="null")
						avail=tmp;
					}
					
				if(counter==6)
					{
					if(tmp!="null")
						status=tmp;
					}
				
				if(counter==7)
					{
					if(tmp!="null")
						severity=tmp;
					}
					
				//alert(counter);	
				counter++;
			}
			var semaphoreColor=getSemaphoreColorForNode(severity,avail,status);
			var semaphoreFlash = getSemaphoreFlash(severity,avail);
			//alert("add element " + id);
			map.addMapElement(new MapElement(id,iconName, labelText, semaphoreColor, semaphoreFlash, x, y, mapElemDimension, status, avail,severity));
		}
		if (k>0 && nodeST.length == 4) {

			var id1,id2,typology, status, flash;;
			
			while(counter< nodeST.length){
				var tmp = nodeST[counter];
				
				//load the links

				if(counter==0) 
				{
					id1=tmp;
				}
				if(counter==1)
				{
					id2=tmp;
				}
				if(counter==2)
				{
					typology=tmp;
				}
				if(counter==3)
				{
					status=tmp;
				}

				counter++;
			}
			map.addLink(id1,id2,typology,LINKSTATUS_COLOR[status], LINK_WIDTH[typology], LINK_DASHARRAY[typology], LINKSTATUS_FLASH[status]);
		}
	}
	map.render();
	reloadGrid();
	loading--;
	assertLoading();
	savedMapString=getMapString();
	enableMenu();
//	alert('Opening Map OK!');		
}

function saveMap() {
	if(currentMapId!=MAP_NOT_OPENED){
		var query="Nodes=";
		var count=0;
		clearTopInfo();
		clearDownInfo();
		var childNode = menuSvgDocument.getElementById("DownInfoText");
		if (childNode)
			menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
			menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Saving map '" +currentMapName+"'"+
		"</text>",menuSvgDocument));		
			
		
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
		
		postURL ( baseContext+"SaveMap."+suffix+"?action="+SAVEMAP_ACTION+"&"+query, null, handleSaveResponse, "text/xml", null );
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
	postURL ( baseContext+"SaveMap."+suffix+"?action="+SAVEMAP_ACTION+"&"+query, null, handleSaveResponse, "text/xml", null );
	disableMenu();
	}else{
		alert("No maps opened");
	}
}


function handleSaveResponse(data) {
	if(data.success) {
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
			clearDownInfo();
			var childNode = menuSvgDocument.getElementById("DownInfoText");
			if (childNode)
				menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
				menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Map '" +currentMapName+"' saved"+
			"</text>",menuSvgDocument));
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


function renameMap(){
	var renameNode =menuSvgDocument.getElementById("RenameMapBoxText");
	var isValid=false;
	if(renameNode!=null){
		var newMapName = renameNode.firstChild.nodeValue;
		if(trimAll(newMapName)!=""){
			isValid=true;
			clearMapInfo();
			currentMapName=newMapName;
			viewMapInfo();
			clearTopInfo();
			clearDownInfo();
			var childNode = menuSvgDocument.getElementById("DownInfoText");
			if (childNode)
				menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
				menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Map renamed"+
			"</text>",menuSvgDocument));		
		}
	}
	if(!isValid){
		clearDownInfo();
		var childNode = menuSvgDocument.getElementById("DownInfoText");
		if (childNode)
			menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
			menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Name not valid"+
		"</text>",menuSvgDocument));
	}
}

/*
function testMapNameLength(evt){
	try{
		var keycode = evt.getKeyCode();
		//alert(keycode);
		var renMapNameNode = evt.getTarget();
		var lastValue = renMapNameNode.firstChild.nodeValue;
		
		//alert(lastValue +" "+renMapNameNode.firstChild.nodeValue.length);
		// embedded code discards chars used in post to the servlet (that are ? and , and + and *) 
		if(renMapNameNode.firstChild.nodeValue.length>16 || keycode==91 || keycode==61 || keycode==44){
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
*/


function deleteMap(){
	if(currentMapId!=MAP_NOT_OPENED && currentMapId!=NEW_MAP){
	    if(confirm('Are you sure to delete the map?')==true){ 
	    	postURL (baseContext+ "DeleteMap."+suffix+"?action="+DELETEMAP_ACTION, null, handleDeleteResponse, "text/xml", null );
	    	}else return;
	}else{
		alert('No maps opened or saved');
		return;
        }
       
        disableMenu();
}

function handleDeleteResponse(data) {
	if(data.success) {
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
	clearDownInfo();
	var childNode = menuSvgDocument.getElementById("DownInfoText");
	if (childNode)
		menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
		menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Map deleted"+
	"</text>",menuSvgDocument));
	enableMenu();
	mapHistory.splice(mapHistoryIndex,1);
	mapHistoryName.splice(mapHistoryIndex,1);
		
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
     	postURL (baseContext+"ClearMap."+suffix+"?action="+CLEAR_ACTION+"&elems=", null, handleClearMapResponse, "text/xml", null );
     }
}

function handleClearMapResponse(data) {
	var str = '';
	if(data.success) {
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
//	savedMapString=getMapString();
	enableMenu();
   	
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
	if(currentMapId==MAP_NOT_OPENED){
		alert("No maps opened");
		return;
	}
	
	if(savedMapString!=getMapString() && currentMapId!=MAP_NOT_OPENED && isAdminMode) {
	 if(confirm('Map \''+currentMapName+'\' not saved, proceed anyway?')==false)
	 	return;
	}
	map.clear();
	clearMapInfo();
	clearTopInfo();
	clearDownInfo();
	hideMapInfo();
	loading++;
	assertLoading();
	disableMenu();
	postURL ( baseContext+"CloseMap."+suffix+"?action="+CLOSEMAP_ACTION+"&MapId="+MAP_NOT_OPENED+"&MapWidth="+map.getWidth()+"&MapHeight="+map.getHeight(), null, handleLoadingMap, "text/xml", null );
}


function resetFlags(){
	 deletingMapElem=false;
	 addingMapElemNeighbors=false;
	 settingMapElemIcon=false;
}

function RefreshNodes(){
	disableMenu();
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	menuSvgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'inline');
	var elems="";
	if(map!=undefined){
		if(reloadMap){
			refreshingMapElems=true;
			postURL (baseContext+ "RefreshMap."+suffix+"?action="+RELOAD_ACTION+"&elems="+elems, null, handleRefreshNodesResponse, "text/xml", null );
			return;
		}else{
			if(map.mapElementSize>0){
				refreshingMapElems=true;
				postURL ( baseContext+ "RefreshMap."+suffix+"?action="+REFRESH_ACTION+"&elems="+elems, null, handleRefreshNodesResponse, "text/xml", null );
			}else{
				menuSvgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'none');
				enableMenu();
				startRefreshNodesTime();
			}
			return;
		}
	}
	menuSvgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'none');
	enableMenu();
	startRefreshNodesTime();			
}


function handleRefreshNodesResponse(data) {
	var saved=true;
	if(savedMapString!=getMapString()){
		saved=false;
	}
	var str = '';
	if(data.success) {
		str = data.content;
		if(reloadMap){
			var tmpStr=str.substring(0,RELOAD_ACTION.length+2);
			if(tmpStr==RELOAD_ACTION+"OK"){
				str=str.substring(RELOAD_ACTION.length+2,str.length);
			} else {
	    	    		alert('Refresh failed');
				menuSvgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'none');
				enableMenu();
				startRefreshNodesTime();
				return;
			}
		}else{
			var tmpStr=str.substring(0,REFRESH_ACTION.length+2);
			if(tmpStr==REFRESH_ACTION+"OK"){
				str=str.substring(REFRESH_ACTION.length+2,str.length);
			} else {
				alert('Refresh failed');
				menuSvgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'none');
				enableMenu();
				startRefreshNodesTime();
				return;
			}
		}
	} else {
        alert('Refresh failed');
		menuSvgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'none');
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
		var counter=0;
		//Manage errors
		if (nodeST.length == 1) {
			var iderror;
			while(counter< nodeST.length){
				var tmp = nodeST[counter];
				if(counter==0) 
				{
					iderror=tmp;
				}
				counter++;
				// manage loop found error
				//loopfound
				var tmpStr=iderror.substring(0,9);
				if(tmpStr=="loopfound"){
					alert("Add Map as Node: Encountered Loop Adding SubMap with ID " + iderror.substring(9));
				}
			}			
		}
		//MapElement
		if (nodeST.length > 4) {

			var id,iconName=DEFAULT_ICON,labelText="",avail=100,status=0,severity=0,posx=0,posy=0;
			
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
						iconName=tmp;
					}
				if(counter==2)
					{
					if(tmp!="null")
						labelText=tmp;
					}

				if(counter==3)
					{
					if(tmp!="null")
						avail=tmp;
					}
					
				if(counter==4)
					{
					if(tmp!="null")
						status=tmp;
					}
				
				if(counter==5)
					{
					if(tmp!="null")
						severity=tmp;
					}
				if(reloadMap){
					if(counter==6)
						{
						if(tmp!="null")
							posx=tmp;
						}
					if(counter==7)
						{
						if(tmp!="null")
							posy=tmp;
						}
				}
				//alert(counter);	
				counter++;
			}
			var semaphoreColor=getSemaphoreColorForNode(severity,avail,status);
			var semaphoreFlash = getSemaphoreFlash(severity,avail);
			//alert("add element " + id);
			if(reloadMap){
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
			var id1,id2, typology, status, flash;
			
			while(counter< nodeST.length){
				var tmp = nodeST[counter];
				
				//read the information of the map (id, name, ecc.)
			
				if(counter==0) 
				{
					id1=tmp;
				}
				if(counter==1)
				{
					id2=tmp;
				}
				if(counter==2)
				{
					typology=tmp;
				}
				if(counter==3)
				{
					status=tmp;
				}
				counter++;
			}
			map.addLink(id1,id2,typology,LINKSTATUS_COLOR[status],LINK_WIDTH[typology],LINK_DASHARRAY[typology],LINKSTATUS_FLASH[status]);
		}
		//map.render();
		
	}
	map.render();
	//reloadGrid();
	menuSvgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'none');
	if(saved){ 
		savedMapString=getMapString();
	}
	enableMenu();
	startRefreshNodesTime();
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

function testResponse(action, response){
		var tmpStr=response.substring(0,action.length+2);
		if(tmpStr==(action+"OK"))
			return true;
		return false;
}

function switchToNormalMode(){
	isAdminMode = false;
	postURL ( baseContext+"SwitchRole."+suffix+"?action="+SWITCH_MODE_ACTION+"&adminMode="+false, null, handleSwitchRole, "text/xml", null );
}

function switchToAdminMode(){
	isAdminMode = true;
	postURL ( baseContext+"SwitchRole."+suffix+"?action="+SWITCH_MODE_ACTION+"&adminMode="+true, null, handleSwitchRole, "text/xml", null );
}

		
function handleSwitchRole(data) {
	
	if(data.success) {
		var str = data.content;
		if(testResponse(SWITCH_MODE_ACTION, str)){
			if (isAdminMode) {
				instantiateRWAdminMenu();
				removeLegend();
				for (mapElemId in map.mapElements) {
					map.mapElements[mapElemId].setSemaphoreColor(getSemaphoreColorForNode(0,0,0));
					map.mapElements[mapElemId].setSemaphoreFlash(getSemaphoreFlash(0,0));
				}
			}else{
					refreshMapElements();
					instantiateRWNormalMenu();
					addLegend();
			}
			map.render();
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


		
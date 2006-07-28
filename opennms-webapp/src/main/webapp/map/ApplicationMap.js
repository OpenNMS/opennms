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
	if(isUserAdmin=="false" || isUserAdmin==false){ // if is not admin, do not generate a string for the map
			  // because, all changes can't be saved from the user (non admin)
		return "";
	}
	var query="Nodes=";
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
	if(colorSemaphoreBy=="A"){
		return getAvailColor(avail);
	}else if (colorSemaphoreBy=="T") {
		return getStatusColor(status);
	} else {
		return getSeverityColor(severity);
	}
}

function getSemaphoreFlash(severity, avail){
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
	return false;
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
		var splitStr = str.split("&");
		refreshNodesIntervalInSec=parseInt(splitStr[0])*60;
		var mapToOpen = splitStr[1];
		//alert("refreshnodesinterval= "+refreshNodesIntervalInSec+" isUserAdmin="+isUserAdmin+ " mapToOpen="+mapToOpen);

		refreshNodesIntervalInSec=parseInt(str)*60;
		if(isUserAdmin=="false"){
			
			hideAll();
     		disableMenu();
		}
		if(mapToOpen!=undefined){
     		openMap(parseInt(mapToOpen));
		}
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
		//alert(str.indexOf("&"));
		
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

function addMapElement(){
	//alert(selectedMapElemInList);
	if(selectedMapElemInList==0 ){
		return;
	}
			
	var point = getFirstFreePoint();
	if(point==null){
		alert("no free points in the grid, cambiare grandezza nodi!!");
		return;
	}
	loading++;
	assertLoading();
	disableMenu();

	var elem = nodeSortAss[selectedMapElemInList].id;
	//alert("loading nodes");
	postURL ( "LoadCurrentNodes?action="+ADDNODES_ACTION+"&elems="+elem, null, analizeAddNodeResponse, "text/xml", null );

}

function addRangeOfNodes(){
	var range = menuSvgDocument.getElementById("RangeText").firstChild.nodeValue;
	if(!isValidRange(range)){
		alert('Range not valid!');
		return;
		}
	var point = getFirstFreePoint();
	if(point==null){
		alert("no free points in the grid, cambiare grandezza nodi!!");
		return;
	}
	loading++;
	assertLoading();
	disableMenu();
	postURL ( "LoadCurrentNodes?action="+ADDRANGE_ACTION+"&elems="+range, null, analizeAddNodeResponse, "text/xml", null );

}

function addMapElemNeigh(id){
	var point = getFirstFreePoint();
	if(point==null){
		alert("no free points in the grid, cambiare grandezza nodi!!");
		return;
	}
	loading++;
	assertLoading();
	disableMenu();
	postURL ( "LoadCurrentNodes?action="+ADDNODES_NEIG_ACTION+"&elems="+id, null, analizeAddNodeResponse, "text/xml", null );

}

function addMapElementWithNeighbors()
{
	if(selectedMapElemInList!=0 )  {
		return;
	}
			
	var point = getFirstFreePoint();
	if(point==null){
		alert("no free points in the grid, cambiare grandezza nodi!!");
		return;
	}
	loading++;
	assertLoading();
	disableMenu();

	var elem = nodeSortAss[selectedMapElemInList].id;
	//alert("loading nodes");
	postURL ( "LoadCurrentNodes?action="+ADDNODES_WITH_NEIG_ACTION+"&elems="+elem, null, analizeAddNodeResponse, "text/xml", null );
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
		menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Cannot add map into itself." +
		"</text>",menuSvgDocument));			
		return;
	}
	
	loading++;
	assertLoading();
	disableMenu();
	//alert(mapSortAss[selectedMapInList].id);
	postURL ( "LoadCurrentNodes?action="+ADDMAPS_ACTION+"&elems="+mapId, null, analizeAddNodeResponse, "text/xml", null );
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

	postURL ( "LoadCurrentNodes?action="+ACTION+"&elems="+id, null, analizeDeleteNodeResponse, "text/xml", null );
}

function getElemInfo(elemMap)
{
	loading++;
	assertLoading();
	var ACTION = "";
	var id = -1;
	if (elemMap.isMap()) {
		//ACTION = LOAD_NODES_INFO_ACTION;
		//id = elemMap.getMapId();
	}
	if (elemMap.isNode()) {
		ACTION = LOAD_NODES_INFO_ACTION;
		id = elemMap.getNodeId();
	}

	postURL ( "LoadInfos?action="+ACTION+"&elem="+id, null, analizeLoadInfosResponse, "text/xml", null );
}

function analizeDeleteNodeResponse(data) {
	var str = '';
	if(data.success) {
		str = data.content;
		var tmpStr=str.substring(0,DELETENODES_ACTION.length+2);
		if(tmpStr==DELETENODES_ACTION+"OK"){
			str=str.substring(DELETENODES_ACTION.length+2,str.length);
		} else {
		tmpStr=str.substring(0,DELETEMAPS_ACTION.length+2);
		if(tmpStr==DELETEMAPS_ACTION+"OK"){
			str=str.substring(DELETEMAPS_ACTION.length+2,str.length);
			} else {
	    	    alert('Deleting Element/s failed!');
				loading--;
				assertLoading();
				return;
			}
		}
	} else {
        alert('Deleting Element/s failed!');
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
				
			//read the information of the map (id, name, ecc.)
		
			if(counter==0) 
			{
				id=tmp;
			}
			//alert(counter);	
			counter++;
		}
		map.deleteMapElement(id);
			// reloadgrid se non esiste lo spazio allora !!!!!!! tratto
	}
	reloadGrid();
	loading--;
	assertLoading();
//	savedMapString=getMapString();
	enableMenu();
}


function analizeLoadInfosResponse(data) {
	var str = '';
	if(data.success) {
		str = data.content;
		var tmpStr=str.substring(0,LOAD_NODES_INFO_ACTION.length+2);
		if(tmpStr==LOAD_NODES_INFO_ACTION+"OK"){
			str=str.substring(LOAD_NODES_INFO_ACTION.length+2,str.length);
		} else {
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
		for(var k=2;k<st.length;k++){
			var nodeToken = st[k];
			infos+="<tspan x=\"3\" dy=\"15\" font-size=\"9\">"+nodeToken+"</tspan>";		
		}
		var nodeToken = st[1];
		var labelText = menuSvgDocument.getElementById("TopInfoLabelText");
		labelText.firstChild.nodeValue+=" ("+nodeToken+")";
		tiText.appendChild(parseXML(infos,menuSvgDocument));
	}
       	loading--;
	assertLoading();	
}

function analizeAddNodeResponse(data) {
	
	var str = '';
	if(data.success) {
		str = data.content;
		var tmpStr=str.substring(0,ADDNODES_ACTION.length+2);
		if(tmpStr==ADDNODES_ACTION+"OK"){
			str=str.substring(ADDNODES_ACTION.length+2,str.length);
			selectedMapElemInList=0;
		} else {
			tmpStr=str.substring(0,ADDRANGE_ACTION.length+2)
			if(tmpStr==ADDRANGE_ACTION+"OK"){
				str=str.substring(ADDRANGE_ACTION.length+2,str.length);
			} else {
				tmpStr=str.substring(0,ADDNODES_NEIG_ACTION.length+2)
				if(tmpStr==ADDNODES_NEIG_ACTION+"OK"){
					str=str.substring(ADDNODES_NEIG_ACTION.length+2,str.length);
				} else {
					tmpStr=str.substring(0,ADDNODES_WITH_NEIG_ACTION.length+2)
					if(tmpStr==ADDNODES_WITH_NEIG_ACTION+"OK"){
						str=str.substring(ADDNODES_WITH_NEIG_ACTION.length+2,str.length);
						selectedMapElemInList=0;
					} else {
						tmpStr=str.substring(0,ADDMAPS_ACTION.length+2)
						if(tmpStr==ADDMAPS_ACTION+"OK"){
							str=str.substring(ADDMAPS_ACTION.length+2,str.length);
						} else {
				    	    alert('Adding Node/s failed!');
							loading--;
							assertLoading();
							return;
						}
					}
				}
			}
		}
	} else {
        alert('Adding Element/s failed!');
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
					alert("Add Map As Node: Found Loop Adding SubMap with ID " + iderror.substring(9));
				}
			}			
		}
		//MapElement
		if (nodeST.length > 2) {

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
			var point = getFirstFreePoint();
			newElem= new MapElement(id,iconName, labelText, semaphoreColor, semaphoreFlash, point.x, point.y, mapElemDimension, status, avail,severity)
			nodesToAdd.push(newElem);

			//reloadGrid();
			// reloadgrid se non esiste lo spazio allora !!!!!!! tratto
		}
		// Links
		if (nodeST.length == 2) { // when find links into server response...

			var id1,id2;
			
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
				counter++;
			}
			var linkId = id1+"-"+id2;
			linksToAdd.push(linkId);
			//map.addLink(id1,id2,"green",1);
		}
		
	}
	if(!nodesAdded){
		nodesAdded=true;
		var freePoints = null;
		var alerted = false;
		do{     // try to add the elements
		   reloadGrid();
		   freePoints = getFreePoints();
		   //alert("freePoints.length="+freePoints.length+"  nodesToAdd.length="+nodesToAdd.length);
		   if(freePoints.length>=nodesToAdd.length){
			break;
		   }else{
			if(!alerted)
				alert('Not enough space for all elements, theirs dimension will be decreased.');
			alerted=true;
		   }
		   var decrResult = decreaseMapElemDim();

		   if(decrResult==false) // space problems
			{
			alert('No space available for elements.');
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
	
	var linkId;
	for(ln in linksToAdd){	
		linkId = linksToAdd[ln];
		var elemIds = linkId.split('-');
		var id1=elemIds[0];
		var id2=elemIds[1];
		map.addLink(id1,id2,"green",1);
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
	if(savedMapString!=getMapString() && currentMapId!=MAP_NOT_OPENED) {
	 if(confirm('Map \''+currentMapName+'\' not saved, do you want to proceed however?')==false)
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
	postURL ( "OpenMap?action="+NEWMAP_ACTION+"&MapId="+NEW_MAP+"&MapWidth="+map.getWidth()+"&MapHeight="+map.getHeight(), null, openDownloadedMap, "text/xml", null );
}

function openMap(mapId){ 
	if(selectedMapInList!=0 || (mapId!=undefined && (typeof mapId)!="object")){
		if(savedMapString!=getMapString() && currentMapId!=MAP_NOT_OPENED) {
		 if(confirm('Map \''+currentMapName+'\' not saved, do you want to proceed however?')==false)
			return;
		}	
		map.clear();
		hideMapInfo();
		loading++;
		assertLoading();
		disableMenu();
		var mapIdToOpen = mapId;
		if(mapIdToOpen==undefined || (typeof mapIdToOpen)=="object")
			mapIdToOpen = mapSortAss[selectedMapInList].id;
		//alert("OpenMap: mapIdToOpen="+mapIdToOpen);
		postURL ( "OpenMap?action="+OPENMAP_ACTION+"&MapId="+mapIdToOpen+"&MapWidth="+map.getWidth()+"&MapHeight="+map.getHeight(), null, openDownloadedMap, "text/xml", null );
	}
	
}

function openDownloadedMap(data) {
	var str = '';
	var action = null;
	//reset zoom and pan
	reset();
	if(data.success) {
		str = data.content;
		var tmpStr=str.substring(0,OPENMAP_ACTION.length+2);
		if(tmpStr==OPENMAP_ACTION+"OK"){
			str=str.substring(OPENMAP_ACTION.length+2,str.length);
			action = OPENMAP_ACTION;
			selectedMapInList=0;
		} else {
			tmpStr=str.substring(0,NEWMAP_ACTION.length+2);
			if(tmpStr==NEWMAP_ACTION+"OK"){
				str=str.substring(NEWMAP_ACTION.length+2,str.length);
				action = NEWMAP_ACTION;
			} else {	
				tmpStr=str.substring(0,CLOSEMAP_ACTION.length+2);
				if(tmpStr==CLOSEMAP_ACTION+"OK"){
					str=str.substring(CLOSEMAP_ACTION.length+2,str.length);
					action = CLOSEMAP_ACTION;
				} else {			
					alert('Loading Map Failed!');
					loading--;
					assertLoading();
					return;
					}
			}
		}
	} else {
		        alert('Loading Map: Response Failed!');
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
			}else{
				viewMapInfo();
			}
		}	
		if (k>0 && nodeST.length > 2) {

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
		if (k>0 && nodeST.length == 2) {

			var id1,id2;
			
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
				counter++;
			}
			map.addLink(id1,id2,"green",1);
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
	
	postURL ( "SaveMap?"+query, null, viewSaveResponse, "text/xml", null );
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
	postURL ( "SaveMap?"+query, null, viewSaveResponse, "text/xml", null );
	disableMenu();
	}else{
		alert("No maps opened");
	}
}


function viewSaveResponse(data) {
	if(data.success) {
		
		var answerST = data.content.split("+");
		//alert(answerST[0]);
		if(answerST[0]!="saveMapOK")
			{	
			alert('Saving Map has failed ');	
			clearDownInfo();
			enableMenu();
			return;
			}
		//alert(answerST[0]+" "+answerST[1]+" "+answerST[2]+" "+answerST[3]+" "+answerST[4]+" "+answerST[5]+" "+answerST[6]+" "+answerST[7]+" "+answerST[8]);
		
		

		var packet = answerST[9];
		var totalPackets = answerST[10];
		//alert("viewSaveResponse: packet="+packet+" totalPackets="+totalPackets);
		if(packet==totalPackets){

			currentMapId=parseInt(answerST[1]);
			currentMapBackGround=answerST[2];
			currentMapAccess=answerST[3];
			currentMapName=answerST[4];
			currentMapOwner=answerST[5];
			currentMapUserlast=answerST[6];
			currentMapCreatetime=answerST[7];
			currentMapLastmodtime=answerST[8];		
			LoadMaps();
			clearMapInfo();
			viewMapInfo();
			savedMapString = getMapString();			
			clearDownInfo();
			var childNode = menuSvgDocument.getElementById("DownInfoText");
			if (childNode)
				menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
				menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Map '" +currentMapName+"' saved."+
			"</text>",menuSvgDocument));
			enableMenu();			
		}else{
			saveMap2(packet, totalPackets); 
		}
		
	} else {
		alert('Saving Map has failed');
		clearDownInfo();
		enableMenu();
		return;
	}

		
}

var lastRenameMapNameValue="";
function renameMap(){
	var renameNode =menuSvgDocument.getElementById("RenameMapText");
	if(renameNode!=null && trimAll(lastRenameMapNameValue)!=""){
		clearMapInfo();
		currentMapName=lastRenameMapNameValue;
		viewMapInfo();
		clearTopInfo();
		clearDownInfo();
		var childNode = menuSvgDocument.getElementById("DownInfoText");
		if (childNode)
			menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
			menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Map renamed."+
		"</text>",menuSvgDocument));		
	}else{
		clearDownInfo();
		var childNode = menuSvgDocument.getElementById("DownInfoText");
		if (childNode)
			menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
			menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Name not valid."+
		"</text>",menuSvgDocument));
	}
}

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
		alert('Deleting Map has failed');
		return;
	}
	clearDownInfo();
	var childNode = menuSvgDocument.getElementById("DownInfoText");
	if (childNode)
		menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
		menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">Map deleted."+
	"</text>",menuSvgDocument));
	enableMenu();
		
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
	  	 alert('Map doesn\'t contain nodes.');
		return;
	}

    if(confirm('Are you sure to clear the map (remove all its elements and links)?')==true) {
    	loading++;
		assertLoading();
		disableMenu();
     	postURL ( "LoadCurrentNodes?action="+CLEAR_ACTION+"&elems=", null, analizeClearMapResponse, "text/xml", null );
     }
}

function analizeClearMapResponse(data) {
	var str = '';
	if(data.success) {
		str = data.content;
		var tmpStr=str.substring(0,CLEAR_ACTION.length+2);
		if(tmpStr==CLEAR_ACTION+"OK"){
			str=str.substring(CLEAR_ACTION.length+2,str.length);
		} else {
	    	    alert('Clear Map Element/s failed!');
				loading--;
				assertLoading();
				return;
		}
	} else {
        alert('Clear Map Element/s failed!');
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
		alert("No Maps opened.");
		return;
	}
	
	if(savedMapString!=getMapString() && currentMapId!=MAP_NOT_OPENED) {
	 if(confirm('Map \''+currentMapName+'\' not saved, do you want to proceed however?')==false)
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
	postURL ( "OpenMap?action="+CLOSEMAP_ACTION+"&MapId="+MAP_NOT_OPENED+"&MapWidth="+map.getWidth()+"&MapHeight="+map.getHeight(), null, openDownloadedMap, "text/xml", null );
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
	if(map!=undefined && map.mapElementSize>0){
	//alert("loading nodes");
		postURL ( "LoadCurrentNodes?action="+REFRESH_ACTION+"&elems="+elems, null, analizeRefreshNodesResponse, "text/xml", null );
	}else{
		menuSvgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'none');
		enableMenu();
		startRefreshNodesTime();
	}
}

var count=0;

function analizeRefreshNodesResponse(data) {
	var str = '';
	if(data.success) {
		str = data.content;
		var tmpStr=str.substring(0,REFRESH_ACTION.length+2);
		if(tmpStr==REFRESH_ACTION+"OK"){
			str=str.substring(REFRESH_ACTION.length+2,str.length);
		} else {
    	    	alert('Refresh failed!');
		menuSvgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'none');
		enableMenu();
		startRefreshNodesTime();
		return;
		}
	} else {
        alert('Refresh failed!');
		menuSvgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'none');
		enableMenu();
		startRefreshNodesTime();
		return;
	}

	var st = str.split("&");
	map.clearLinks();
	//alert("links cleared!");
			

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
					alert("Add Map As Node: Found Loop Adding SubMap with ID " + iderror.substring(9));
				}
			}			
		}
		//MapElement
		if (nodeST.length > 2) {

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
			var deleted = map.deleteMapElement(id);
			var point = getFirstFreePoint();
			if (deleted){
				map.addMapElement(new MapElement(id,iconName, labelText, semaphoreColor, semaphoreFlash, point.x, point.y, mapElemDimension, status, avail,severity));
			}
			
		}
		// Links
		if (nodeST.length == 2) {
			var id1,id2;
			
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
				counter++;
			}
			//alert(id1+" " +id2);
			map.addLink(id1,id2,"green",1);
		}
		//map.render();
		
	}
	map.render();
	reloadGrid();
	menuSvgDocument.getElementById("RefreshingText").getStyle().setProperty('display', 'none');
//	savedMapString=getMapString();
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
		
		
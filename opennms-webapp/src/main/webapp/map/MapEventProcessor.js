// == pEventProcessor.js 
// xlink namespace
function MapEventProcessor()
{
	this.svgnsXLink = "http://www.w3.org/1999/xlink";
}

var mapeventprocessor = new MapEventProcessor();

//if double-click on an element (map) open the map 
function onClickMapElement(evt)
{
	

	if ((typeof map) == "object")
	{	
		map.startSelectionRectangle = null;
		map.endSelectionRectangle = null;
						
	}
	
	//select the element
	resetSelectedObjects();
	map.selectedObjects=new Array();
	map.selectedObjects.push( evt.getTarget().parentNode );
	evt.getTarget().parentNode.getStyle().setProperty("opacity", "0.5");
	
	var id = evt.getTarget().parentNode.getAttribute("id");
	var mapElement = map.mapElements[id];
		
	// view info node
	clearDownInfo();			
	clearTopInfo();
	menuSvgDocument.getElementById("TopInfo").appendChild(parseXML(mapElement.getInfo(),menuSvgDocument));

	//write element info in info panel (on the right)
	if(mapElement.isNode())
		getElemInfo(mapElement);
	
	setContextMenuForElement(evt, mapElement);

	if (evt.getDetail() == 2)
	{

		if(mapElement.isNode())
		{
			openLink('element/node.jsp?node='+mapElement.getNodeId(),'','left=0,top=0, width='+screen.width+',height='+screen.height+',toolbar=no,menubar=no,location=no,scrollbars=1,resize=1,minimize=1')
		}
	
		if(mapElement.isMap())
		{
			if(!refreshingMapElems){
				if(savedMapString!=getMapString() && currentMapId!=MAP_NOT_OPENED && isAdminMode) {
					if(confirm('Map \''+currentMapName+'\' not saved, do you want to proceed however?')==false)
				 		return;
				}
				map.clear();
				hideMapInfo();
				clearTopInfo();
				clearDownInfo();
				loading++;
				assertLoading();
				disableMenu();
				postURL (baseContext+ "OpenMap."+suffix+"?action="+OPENMAP_ACTION+"&MapId="+mapElement.getMapId()+"&MapWidth="+map.getWidth()+"&MapHeight="+map.getHeight(), null, handleLoadingMap, "text/xml", null );			
			}
		}
			
	}
}



function onMouseDownOnMapElement(evt)
{	
	
	//printDebug(mapSvgDocument.getElementById("testo3"), "on Element |" + evt.getTarget().parentNode.getAttribute("id"));
	if ((typeof map) == "object")
	{ 
  		
  		

		
		var mapElement = map.mapElements[evt.getTarget().parentNode.getAttribute("id")];		
		setContextMenuForElement(evt, mapElement);

		var matrix;
		// track the origin
		map.draggableObject = null;
		if(map.selectedObjects==null){	
			//add the element to the selection
			map.selectedObjects=new Array();
			map.selectedObjects.push( evt.getTarget().parentNode );
			evt.getTarget().parentNode.getStyle().setProperty("opacity", "0.5");
		}else{
			var found=false;
			for(selObj in  map.selectedObjects){
				if(map.selectedObjects[selObj] == evt.getTarget().parentNode){
					found=true;
					break;
				}
			}
			if(!found){
				resetSelectedObjects();
				resetDraggableObject();
				map.selectedObjects=new Array();
				map.selectedObjects.push( evt.getTarget().parentNode );					
			}
						
		}
		
		if(map.selectedObjects.length==1){
			// view info node
			clearDownInfo();			
			clearTopInfo();
			menuSvgDocument.getElementById("TopInfo").appendChild(parseXML(mapElement.getInfo(),menuSvgDocument));
		}
		map.draggableObject =  evt.getTarget().parentNode;
		// get the relative position
		var matrix = evt.getTarget().parentNode.getCTM();
  		var mouse = application.getMouse(evt);

  		map.offset.x = (matrix.e - mouse.x); //mapSvgDocument.getFirstChild().currentScale;
  		map.offset.y = (matrix.f - mouse.y); //mapSvgDocument.getFirstChild().currentScale;
		
	
		
		//delete the element if flag 'deletingMapElem' is true
		if(deletingMapElem==true){

			deleteMapElement(mapElement);

			deletingMapElem=false;
		}
		
		//add the element neighbors if flag 'addingMapElemNeighbors' is true
		if(addingMapElemNeighbors==true){
			var elemMap = mapElement;
			if(elemMap.isMap()){
				var childNode = menuSvgDocument.getElementById("DownInfoText");		
				if (childNode)
					menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
				menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">" +
					"<tspan x=\"5\" dy=\"0\">Cannot add neighbors</tspan>" +
					"<tspan x=\"7\" dy=\"10\">to a map.</tspan>" +
					"</text>",menuSvgDocument));						
			} else {
				addMapElemNeigh(elemMap.getNodeId());
			}
			addingMapElemNeighbors=false;
		}

		//set the icon selected into the relative selection list to the selected element
		if(settingMapElemIcon==true){
			mapElement.icon=selectedMEIconInList;
			mapElement.image.setAttributeNS(application.svgnsXLink, "xlink:href", MEIconsSortAss[selectedMEIconInList]);
			map.render();
			settingMapElemIcon=false;
			var childNode = menuSvgDocument.getElementById("DownInfoText");		
			if (childNode)
				menuSvgDocument.getElementById("DownInfo").removeChild(childNode);		
			menuSvgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">" +
				"<tspan x=\"5\" dy=\"0\">Icon setting ok.</tspan>" +
				"</text>",menuSvgDocument));			
		}
		
		
		
	}
}


function removeSelectionRect(){
		var selectionRectangle = mapSvgDocument.getElementById("selectionRect");
		if(selectionRectangle!=undefined)
			selectionRectangle.parentNode.removeChild(selectionRectangle);
}


function createSelectionRect(evt, x, y, width, height){
	//security check
	
	if(map.startSelectionRectangle!=null){
		var selectionRectangle = mapSvgDocument.getElementById("selectionRect");
		if(selectionRectangle!=undefined)
			selectionRectangle.parentNode.removeChild(selectionRectangle);
		selectionRectangle = mapSvgDocument.createElementNS("http://www.w3.org/2000/svg","rect");
		selectionRectangle.setAttributeNS(null, "onmouseup", "onMouseUp(evt);" );
		selectionRectangle.setAttributeNS(null, "onmousemove", "onMouseMove(evt);" );
		selectionRectangle.setAttributeNS(null,"x", x);
		selectionRectangle.setAttributeNS(null,"y", y);
		selectionRectangle.setAttributeNS(null,"width", width);
		selectionRectangle.setAttributeNS(null,"height", height);
		selectionRectangle.setAttributeNS(null,"id", "selectionRect");
		selectionRectangle.setAttributeNS(null,"style","fill:white; fill-opacity:0; stroke:black;stroke-width:1;stroke-opacity:0.5;stroke-dasharray:1");		
		map.getSvgNode().appendChild(selectionRectangle);	
	}else{
		removeSelectionRect();
	}
	
}

function onMouseDownOnMap(evt)
{

	if ((typeof map) == "object" && currentMapId != MAP_NOT_OPENED){
		map.startSelectionRectangle = application.getMouse(evt);
		
	}
	
	disableContextMenu(evt);
	
	// remove node information
	clearTopInfo();
	clearDownInfo();
	
	//close the menu
	hideAll(evt);
	menuOpenFlag=false;
	
	//clear the actions started
	clearActionsStarted();	

	resetSelectedObjects();
	resetDraggableObject();
	

}

function resetSelectedObjects(){
	
	if ((typeof map) == "object")
	{	
		if(map.selectedObjects != null){
			for(selObj in map.selectedObjects)
				map.selectedObjects[selObj].getStyle().setProperty("opacity", "1");		
		}

		map.selectedObjects = null;
	}
}


function resetDraggableObject(){
	
	if ((typeof map) == "object")
	{
		map.draggableObject = null;
	}
}		
		
function onMouseDownOnLink(evt)
{
	
	disableContextMenu(evt);
	resetSelectedObjects();
	if ((typeof map) == "object")
	{
		clearTopInfo();
		clearDownInfo();
		clearActionsStarted();
		
		var mapLink = map.mapLinks[evt.getTarget().getAttribute("id")];
		menuSvgDocument.getElementById("TopInfo").appendChild(parseXML(mapLink.getInfo(),menuSvgDocument));
		
		//delete the link if flag 'deletingLink' is true
		
		/*if(deletingLink==true){
			map.deleteLink(evt.getTarget().getAttribute("id"));
			clearDownInfo();
			deletingLink=false;
		}*/	
	}		
}

function onMouseMove(evt)
{
	var mapsvgRoot = mapSvgDocument.documentElement;
	var pan = mapsvgRoot.getCurrentTranslate();
	var zoom = mapsvgRoot.getCurrentScale();
	var mouse = application.getMouse(evt);
	if((typeof map) == "object" && map.startSelectionRectangle!=null ){
		var minX = map.startSelectionRectangle.x;
		var maxX = mouse.x;
		if(map.startSelectionRectangle.x > mouse.x){
			minX = mouse.x;
			maxX = map.startSelectionRectangle.x;
		}
		var minY = map.startSelectionRectangle.y;
		var maxY = mouse.y;
		if(map.startSelectionRectangle.y > mouse.y){
			minY = mouse.y;
			maxY = map.startSelectionRectangle.y;
		}		
		minX = (minX- pan.x)/zoom;
		maxX = (maxX- pan.x)/zoom;
		minY = (minY- pan.y)/zoom;
		maxY = (maxY- pan.y)/zoom;
		
		
		createSelectionRect(evt, minX, minY, maxX-minX, maxY-minY)
		

	}
	
	if ((typeof map) == "object" && map.selectedObjects != null && map.draggableObject!=null)
	{
	var mapElements = new Array();
	if(map.selectedObjects != null){
		for(selObj in map.selectedObjects){
			map.selectedObjects[selObj].getStyle().setProperty("opacity", "0.5");
			mapElements.push( map.mapElements[map.selectedObjects[selObj].getAttribute("id")] );
		}
	}
	var transScaledPoint = mapsvgRoot.createSVGPoint();
	//alert(evt.getTarget().parentNode+" "+evt.getTarget().parentNode.getAttribute("id"));
	

	var movingElement = map.mapElements[map.draggableObject.getAttribute("id")];
	
	var dX = mouse.x - movingElement.x*zoom;	
	var dY = mouse.y - movingElement.y*zoom;
	
/*	
	//print coordinates for debugging
	var coord = menuSvgDocument.getElementById("coordinate");
	if(!coord.getFirstChild()){
		coord.appendChild(menuSvgDocument.createTextNode("mouse:"+mouse.x+","+mouse.y+" xy:"+transScaledPoint.x+","+transScaledPoint.y+"  off:"+map.offset.x+","+map.offset.y));
	}else{
		coord.getFirstChild().nodeValue = mapSvgDocument.getFirstChild().currentScale+" m:"+mouse.x+","+mouse.y+" xy:"+transScaledPoint.x+","+transScaledPoint.y+"  off:"+map.offset.x+","+map.offset.y;
	}
*/
	
  	if(mapElements.length!=0)
  		for(drObj in mapElements){
  			
			transScaledPoint.x = parseInt(((mapElements[drObj].x*zoom+dX+map.offset.x) - pan.x) / zoom);
			transScaledPoint.y = parseInt(((mapElements[drObj].y*zoom+dY+map.offset.y) - pan.y) / zoom);
			
			if(transScaledPoint.x<=0){
				transScaledPoint.x=0;
			}
			if(transScaledPoint.x>=(map.getWidth()-mapElemDimension)){
				transScaledPoint.x=map.getWidth()-mapElemDimension;
			}			
			if(transScaledPoint.y<=0){
				transScaledPoint.y=0;
			}	
			if(transScaledPoint.y>=(map.getHeight()-mapElemDimension)){
				transScaledPoint.y=map.getHeight()-mapElemDimension;
			}			
			
			mapElements[drObj].move(transScaledPoint.x, transScaledPoint.y);
			map.redrawLinkOnElement(mapElements[drObj].id);
		}
	}

}

function onMouseUp(evt)
{
	//alert("mouse up!");
	var mapsvgRoot = mapSvgDocument.documentElement;
	var zoom = mapsvgRoot.getCurrentScale();
	var pan = mapsvgRoot.getCurrentTranslate();
	disableContextMenu(evt);
	removeSelectionRect();
	//reset the selection rectangle

	
	if ((typeof map) == "object")
	{
		//resetSelectedObjects();
		resetDraggableObject();
		
		//
		if(map.startSelectionRectangle!=null){
			map.endSelectionRectangle = application.getMouse(evt);
			
			var minX = (map.startSelectionRectangle.x < map.endSelectionRectangle.x)?map.startSelectionRectangle.x:map.endSelectionRectangle.x;
			var maxX = (map.startSelectionRectangle.x > map.endSelectionRectangle.x)?map.startSelectionRectangle.x:map.endSelectionRectangle.x;
			var minY = (map.startSelectionRectangle.y < map.endSelectionRectangle.y)?map.startSelectionRectangle.y:map.endSelectionRectangle.y;
			var maxY = (map.startSelectionRectangle.y > map.endSelectionRectangle.y)?map.startSelectionRectangle.y:map.endSelectionRectangle.y;
			
			map.selectedObjects = new Array();
			
			var count=0;
			for(mEl in map.mapElements){
				
				if(( ((map.mapElements[mEl].x+mapElemDimension/2)*zoom) +pan.x )>=minX && ( ((map.mapElements[mEl].x+mapElemDimension/2)*zoom) +pan.x)<=maxX && ( ((map.mapElements[mEl].y+mapElemDimension/2)*zoom) +pan.y)>=minY && ( ((map.mapElements[mEl].y+mapElemDimension/2)*zoom) +pan.y)<=maxY){
					map.mapElements[mEl].getSvgNode().getStyle().setProperty("opacity", "0.5");
					map.selectedObjects.push(map.mapElements[mEl].getSvgNode());
					count++;
				}
			}
			if(count>0)
				writeDownInfo(count+" element/s selected.")
			map.startSelectionRectangle=null;
			map.endSelectionRectangle=null;	
		}			
		
	}
}



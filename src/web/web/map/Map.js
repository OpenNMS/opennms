// you must be istantied a map object in onLoad event of svg node root
// Example:
// Map map = new Map("#bbbbbb", "", "Background", 600, 400, 0, 0);
Map.prototype = new SVGElement;
Map.superclass = SVGElement.prototype;


function Map(color, image, id, width, height, x, y)
{
 	if ( arguments.length == 7 )
		this.init(color, image, id, width, height, x, y);
	else
		alert("Map constructor call error");
}

Map.prototype.init = function(color, image, id, width, height, x, y)
{
	this.nodes = new Array();
	this.nodeSize = 0;
	this.links = new Array();
	this.linkSize = 0;
	this.draggableObject = null;
	this.selectedObject = null;
	this.offset = svgDocument.documentElement.createSVGPoint();
	
	this.svgNode = svgDocument.createElement("g");
	
	this.background = svgDocument.createElement("g");
	this.background.setAttribute("id", id);
	this.background.addEventListener("mousemove", this.onMouseMove, false);
	this.background.addEventListener("mouseup", this.onMouseUp, false);
	this.background.addEventListener("mousedown", this.onMouseDownOnMap, false);
	
	this.rect = svgDocument.createElement("rect"); 
	this.rect.setAttribute("width", width);
	this.rect.setAttribute("height", height);
	this.rect.setAttribute("fill", color);
	this.rect.setAttribute("stroke", "black");
	this.rect.setAttribute("stroke-width","1");
	this.image = svgDocument.createElement("image");
	this.image.setAttribute("width", width);
	this.image.setAttribute("height", height);
	this.image.setAttribute("x", x);
	this.image.setAttribute("y", y);
	
	//this.image.setAttribute("preserveAspectRatio","xMidYMid");
	this.image.setAttributeNS('http://www.w3.org/2000/xlink/namespace/', 'xlink:href', image);
	
	this.background.appendChild(this.rect);
	this.background.appendChild(this.image);
	this.svgNode.appendChild(this.background);

}

Map.prototype.setBackgroundColor = function(color){
	currentMapBackGround=color.split("#")[1];
	this.rect.setAttribute("fill", color);
}

Map.prototype.setBackgroundImage = function(image){
	currentMapBackGround=image;
	
	this.image.setAttributeNS('http://www.w3.org/2000/xlink/namespace/', 'xlink:href', image);
}

//only show the map with the background image in input
Map.prototype.tryBackgroundImage = function(image){
	this.image.setAttributeNS('http://www.w3.org/2000/xlink/namespace/', 'xlink:href', image);
}


// delete all nodes from map view
Map.prototype.clean = function()
{
		var elemToRender = null;
		if(this.links!=null && this.linkSize>0)
		for (elemToRender in this.links){
			if(elemToRender!=null){
				var elem = this.links[elemToRender].getSvgNode();
				if(elem.parentNode==this.svgNode){
					this.svgNode.removeChild(elem);
					}
				}
			}
		if(this.nodes!=null && this.nodeSize>0)
		for (elemToRender in this.nodes){
			if(elemToRender!=null){
				var elem = this.nodes[elemToRender].getSvgNode();
				if(elem.parentNode==this.svgNode){
					this.svgNode.removeChild(elem);
					}
				}
			}

}

// delete all nodes from map 
Map.prototype.clear = function()
{
		var elemToRender = null;
		if(this.links!=null && this.linkSize>0)
		for (elemToRender in this.links){
			if(elemToRender!=null){
				var elem = this.links[elemToRender].getSvgNode();
				if(elem.parentNode==this.svgNode){
					this.svgNode.removeChild(elem);
					}
				}
			}
		this.links=new Array();
		this.linkSize=0;
		if(this.nodes!=null && this.nodeSize>0)
		for (elemToRender in this.nodes){
			if(elemToRender!=null){
				var elem = this.nodes[elemToRender].getSvgNode();
				if(elem.parentNode==this.svgNode){
					this.svgNode.removeChild(elem);
					}
				}
			}
		this.nodes=new Array();
		this.nodeSize=0;
}


// delete the node (and all its links) with the id in input
Map.prototype.deleteMapElement = function(elemToDelete)
{
		//remove the links of the element
		if(this.links!=null)
			if(elemToDelete!=null){
				for (currLink in this.links){
					var mapElem1 = this.links[currLink].mapElement1.id;
					var mapElem2 = this.links[currLink].mapElement2.id;
					if(elemToDelete==mapElem1 || elemToDelete==mapElem2){
						this.deleteLink(currLink);
						}
					}
				}
		var labelText = this.nodes[elemToDelete].label.text;		
		//remove the element from the svg view	
		if(this.nodes!=null)
			if(elemToDelete!=null){
				var elem = this.nodes[elemToDelete].getSvgNode();

				if(elem.parentNode==this.svgNode){
					this.svgNode.removeChild(elem);
					}
				}

		//remove the element from the nodes array
		var tempNodes = new Array();
		for (currNode in this.nodes){
			if(elemToDelete!=currNode)
				tempNodes[currNode]=this.nodes[currNode];
		}
		this.nodes=tempNodes;
		this.nodeSize--;

		//remove the info about the element removed 
		clearTopInfo();
		//display that the element is been deleted
		clearDownInfo();
		svgDocument.getElementById("DownInfo").appendChild(parseXML(
			"<text id=\"DownInfoText\" x=\"3\" y=\"20\">'"+labelText+"'" +
			"<tspan x='0' dx='7' dy='15'>deleted.</tspan>"+
			"</text>", svgDocument));		
		map.selectedObject = null;
		
}


//delete all links of the map
Map.prototype.deleteAllLinks = function(){
		if(this.links!=null)
			for (currLink in this.links){
				var elem = this.links[currLink].getSvgNode();
				if(elem.parentNode==this.svgNode){
					this.svgNode.removeChild(elem);
				}
			}
		this.links=new Array();
		this.linkSize=0;
}

// delete the link with the id in input
Map.prototype.deleteLink = function(linkToDelete)
{
		if(this.links!=null)
			if(linkToDelete!=null){
				for (currLink in this.links){
					
					if(linkToDelete==currLink){
						var elem = this.links[currLink].getSvgNode();
						if(elem.parentNode==this.svgNode){
							this.svgNode.removeChild(elem);
							}
						}
					}
				}
		//remove the link from the links array
		var tempLinks = new Array();
		for (currLink in this.links){
			if(linkToDelete!=currLink)
				tempLinks[currLink]=this.links[currLink];
		}
		this.links=tempLinks;
		this.linkSize--;	

		//remove the info of the link removed
		clearTopInfo();
		map.selectedObject = null;
}

//add links from the node with id in input to all linked nodes in map
Map.prototype.addLinks = function(id){
   	var mapsInMap = new Array();
   	for(_node in map.nodes){
   		if(map.nodes[_node].isMap()){
   			mapsInMap.push(map.nodes[_node]);
   		}
   	} 

  //if is a node
  if(id>0){
	var elem=null;
	for(currEl in nodeSortAss){
		if(nodeSortAss[currEl]!=null){
			if(nodeSortAss[currEl].id==id){	
				elem=currEl;	
				break;
			}
		}
	}
	if(elem!=null){
		var lnks =  nodeSortAss[elem].links;
		for(l in lnks){
			//add link to node (simple)
			var linkedElem = lnks[l];
			if(linkedElem!=id)
				map.addLink(id, lnks[l], "green", 1);
			
			// add links to node-map in map
			for(mim in mapsInMap){
				var nodesOfNodeMap = mapsInMap[mim].nodes;
				for(nn in nodesOfNodeMap){
				 	if(nodesOfNodeMap[nn]==linkedElem && nodesOfNodeMap[nn]!=id)
						map.addLink(id, mapsInMap[mim].id, "green", 1);
				}
			}
		}
	}	
   }else{ //if is a map 
		var m = map.nodes[id].nodes;
		for(nid in m){
				var elem=null;
				for(currEl in nodeSortAss){
					if(nodeSortAss[currEl]!=null){
						if(nodeSortAss[currEl].id==m[nid]){	
							elem=currEl;	
							break;
						}
					}
				}
				if(elem!=null){
					var lnks =  nodeSortAss[elem].links;
					for(l in lnks){
						var linkedElem = lnks[l];
						if(linkedElem!=nodeSortAss[elem].id)
							map.addLink(id, lnks[l], "green", 1);
			
						// add links to node-map in map
						for(mim in mapsInMap){
							if(mapsInMap[mim].id!=id){
								var nodesOfNodeMap = mapsInMap[mim].nodes;
								for(nn in nodesOfNodeMap){
								 	if(nodesOfNodeMap[nn] == linkedElem && nodeSortAss[elem].id!=nodesOfNodeMap[nn])
										map.addLink(id, mapsInMap[mim].id, "green", 1);
								}
							}
						}						
					}
				}	
		}
   }
}

// render map
Map.prototype.render = function()
{
	
	this.clean();	
	//try to set the background (first the color and then the image)
	map.setBackgroundColor("#"+currentMapBackGround);
	map.setBackgroundImage(currentMapBackGround)
	
	//this.rect.setAttribute("fill", "#"+currentMapBackGround);	
	var elemToRender = null;
	for (elemToRender in this.links)
		this.svgNode.appendChild(this.links[elemToRender].getSvgNode());
	for (elemToRender in this.nodes)
		this.svgNode.appendChild(this.nodes[elemToRender].getSvgNode());
	


}

	


// add a new node element to map 
/*
id=the id of the element 
iconName=the name of the icon 
labelText= the label of the element
semaphoreColor=the color of the semaphore (rgb or 'white','yellow' ecc.)
x=x position
y=y position
displayInfo= boolean saying if display the info about the adding of the element or not
status=the element status at the moment of the adding
avail=availability of the element at the moment of the adding
nodes=array containing the nodes of the element (IF AND ONLY IF the element to add is a MAP)
*/
Map.prototype.addElement = function(id, iconName, labelText, semaphoreColor, x, y, displayInfo, status, avail, nodes)
{
	if (this.nodes[id] == undefined)
	{
		var mapElement = new MapElement(id, iconName, labelText, semaphoreColor, x, y, status, avail, nodes);
		this.nodes[id] = mapElement;
		this.nodeSize++;
		clearDownInfo();
		//clearTopInfo();
		if(displayInfo==true){		
			svgDocument.getElementById("DownInfo").appendChild(parseXML(
			"<text id=\"DownInfoText\" x=\"3\" y=\"20\">'"+labelText+"'" +
			"<tspan x='0' dx='7' dy='15'>added.</tspan>"+
			"</text>", svgDocument));
			}
		return mapElement;
	}else{ 	
		clearDownInfo();
		if(displayInfo==true){
			svgDocument.getElementById("DownInfo").appendChild(parseXML(
				"<text id=\"DownInfoText\" x=\"2\" y=\"20\">'"+labelText+"'" +
							"<tspan dx='7' dy='15'> already exists.</tspan>"+
				"</text>", svgDocument));
			}
		
	}
	return null;
}



// create linkId from connected node id
Map.prototype.getIdLink = function(id1, id2)
{
	var a = id1.toString();
	var b = id2.toString();
	var rv = a + "-" + b;
	
	if (a > b) 
		rv = b + "-" + a;
		
	return rv;
}

// add a new link to map
Map.prototype.addLink = function(id1, id2, stroke, stroke_width)
{
	// check parameter
	if (id1 == id2) {
		//alertDebug("id1 and id2 must be different");
		return null;
	}
	
	if (this.nodes[id1] == undefined) {
		//alert("Paramater id1 error: map doesn't contain mapnode with id=" + id1);
		return null;
	}
	if (this.nodes[id2] == undefined) {
		//alert("Paramater id2 error: map doesn't contain mapnode with id=" + id2);
		return null;
	}
	var id = this.getIdLink(id1, id2);
	if( (this.links[id] != undefined) ){
		//alertDebug("Map already contains the element");
			return null;
	}

	var link = new Link(id, this.nodes[id1], this.nodes[id2], stroke, stroke_width);
	this.links[id] = link;
	this.linkSize++;
	return link;
}

Map.prototype.onMouseDownOnMap = onMouseDownOnMap;
Map.prototype.onMouseMove = onMouseMove;
Map.prototype.onMouseUp = onMouseUp;


// handler utility 
function onMouseMove(evt)
{
	if ((typeof map) == "object" && map.draggableObject != null)
	{
		var mapElement = map.nodes[map.draggableObject.getAttribute("id")];
		var mouse = application.getMouse(evt);
  		var x = mouse.x + map.offset.x;
  		var y = mouse.y + map.offset.y;		
  		if(mapElement!=null && mapElement!=undefined)
			mapElement.move(x, y);
	}
}

function onMouseUp(evt)
{
	if ((typeof map) == "object" && map.draggableObject != null)
	{
		map.draggableObject.getStyle().setProperty("opacity", "1");
		map.draggableObject = null;
	}
}

function onMouseDownOnMapElement(evt)
{
	//printDebug(svgDocument.getElementById("testo3"), "on Element |" + evt.getTarget().parentNode.getAttribute("id"));
	if ((typeof map) == "object")
	{
		// view info node
		clearDownInfo();			
		clearTopInfo();
		svgDocument.getElementById("TopInfo").appendChild(map.nodes[evt.getTarget().parentNode.getAttribute("id")].getInfo());
		
		// track the origin
		map.draggableObject = evt.getTarget().parentNode;
		map.draggableObject.getStyle().setProperty("opacity", "0.5");
		var matrix = map.draggableObject.getCTM();
  		// get the relative position
  		var mouse = application.getMouse(evt);
  		map.offset.x = matrix.e - mouse.x;
  		map.offset.y = matrix.f - mouse.y;

		// deselect previous element and select new element
		// evidence selected node (apply ombra filter)
		if (map.selectedObject != null)
			map.selectedObject.setAttribute("filter", "");
		
		if (map.selectedObject != evt.getTarget().parentNode) {
			map.selectedObject = evt.getTarget().parentNode;
			//map.selectedObject.setAttribute("filter", "url(#ombra)");
		}
		
		
		
		//delete the element if flag 'deletingMapElem' is true
		if(deletingMapElem==true){
			map.deleteMapElement(evt.getTarget().parentNode.getAttribute("id"));
			deletingMapElem=false;
		}
		
		//add the element neighbors if flag 'addingMapElemNeighbors' is true
		if(addingMapElemNeighbors==true){
			if(evt.getTarget().parentNode.getAttribute("id")<=0){
				var childNode = svgDocument.getElementById("DownInfoText");		
				if (childNode)
					svgDocument.getElementById("DownInfo").removeChild(childNode);		
				svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">" +
					"<tspan x=\"5\" dy=\"0\">Cannot add neighbors</tspan>" +
					"<tspan x=\"7\" dy=\"10\">to a map.</tspan>" +
					"</text>",svgDocument));						
			}
			addMapElemNeigh(evt.getTarget().parentNode.getAttribute("id"));
			addingMapElemNeighbors=false;
		}

		//set the icon selected into the relative selection list to the selected element
		if(settingMapElemIcon==true){
			map.nodes[evt.getTarget().parentNode.getAttribute("id")].icon=selectedMEIconInList;
			map.nodes[evt.getTarget().parentNode.getAttribute("id")].image.setAttributeNS(application.svgnsXLink, "xlink:href", MEIconsSortAss[selectedMEIconInList]);
			map.render();
			settingMapElemIcon=false;
			var childNode = svgDocument.getElementById("DownInfoText");		
			if (childNode)
				svgDocument.getElementById("DownInfo").removeChild(childNode);		
			svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">" +
				"<tspan x=\"5\" dy=\"0\">Icon setting ok.</tspan>" +
				"</text>",svgDocument));			
		}
		
		
		
	}
}

function onMouseDownOnMap(evt)
{
	//printDebug(svgDocument.getElementById("testo3"), "on Map |" + evt.getTarget().parentNode.getAttribute("id"));
	
	// remove node information
	clearTopInfo();
	clearDownInfo();
	
	//close the menu
	hideAll(evt);
	menuOpenFlag=false;
	
	//clear the actions started
	clearActionsStarted();	

	if ((typeof map) == "object" && map.selectedObject != null)
	{
		//map.selectedObject.setAttribute("filter", "");		
		map.selectedObject = null;
	}

	// this istruction is superfluos but sometimes ...
	if ((typeof map) == "object" && map.draggableObject != null)
	{		
		map.draggableObject.getStyle().setProperty("opacity", "1");
		map.draggableObject = null;
	}
}

function onMouseDownOnLink(evt)
{
	if ((typeof map) == "object")
	{
		map.selectedObject = null;
		clearTopInfo();
		clearDownInfo();
		clearActionsStarted();
		svgDocument.getElementById("TopInfo").appendChild(map.links[evt.getTarget().getAttribute("id")].getInfo());		
		
		//delete the link if flag 'deletingLink' is true
		
		/*if(deletingLink==true){
			map.deleteLink(evt.getTarget().getAttribute("id"));
			clearDownInfo();
			deletingLink=false;
		}*/	
	}		
}

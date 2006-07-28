// you must create an instance of a map object in onLoad event of svg node root
// Map map = new Map("#bbbbbb", "", "Background", 600, 400, 0, 0);

Map.prototype = new SVGElement;
Map.superclass = SVGElement.prototype;

// color  == background color of the map
// image  == background image of the map
// id     == map Id
// width  == map width in pixel
// height == map height in pixel
// x      == x coord of map's "centre" 
// y      == y coord of map's "centre" 
// mapElements  == array of MapElement Objects
// mapLinks   == array of Link Object

function Map(color, image, id, width, height, x, y)
{
 	if ( arguments.length == 7 )
		this.init(color, image, id, width, height, x, y);
	else
		alert("Map constructor call error");
}

Map.prototype.init = function(color, image, id, width, height, x, y)
{
	
	//following attributes are the starting and the ending SVGPoints of the rectangle 
	//for the selection of the nodes on the map.
	this.startSelectionRectangle = null;
	this.endSelectionRectangle = null;
	
	
	this.mapElements = new Array();
	this.mapElementSize = 0;

	this.mapLinks = new Array();
	this.mapLinkSize = 0;
	
	this.draggableObject = null;
	this.selectedObjects = null;
	this.offset = mapSvgDocument.documentElement.createSVGPoint();
	this.svgNode = mapSvgDocument.createElement("g");
	
	this.rect = mapSvgDocument.createElement("rect"); 
	this.rect.setAttribute("width", width);
	this.rect.setAttribute("height", height);
	this.rect.setAttribute("fill", color);
	this.rect.setAttribute("stroke-width","1");
	
	this.image = mapSvgDocument.createElement("image");
	this.image.setAttribute("width", width);
	this.image.setAttribute("height", height);
	this.image.setAttribute("x", x);
	this.image.setAttribute("y", y);
	
	//this.image.setAttribute("preserveAspectRatio","xMidYMid");
	this.image.setAttributeNS('http://www.w3.org/2000/xlink/namespace/', 'xlink:href', image);

	this.background = mapSvgDocument.createElement("g");
	this.background.setAttribute("id", id);
	this.background.appendChild(this.rect);
	this.background.appendChild(this.image);

	this.background.addEventListener("mousemove", this.onMouseMove, false);
	this.background.addEventListener("mouseup", this.onMouseUp, false);
	this.background.addEventListener("mousedown", this.onMouseDownOnMap, false);
	
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

//get the width of the map
Map.prototype.getWidth = function(){
	return this.rect.getAttribute("width");
}

//get the height of the map
Map.prototype.getHeight= function(){
	return this.rect.getAttribute("height");
}

// clean map and set the array of map element and render
Map.prototype.setMapElements = function(nodes){

	if ( nodes == undefined ) return null;

	this.clean();

	this.mapElements = nodes;
	this.mapElementSize = nodes.length;
	var elemToRender = null;
	for (elemToRender in this.mapElements) //render mapElement
		this.svgNode.appendChild(this.mapElements[elemToRender].getSvgNode());
}

Map.prototype.setMapElement = function(mapElement) {

	var elementAdded=false;

	var labelText = mapElement.label.text;		

	if (mapElement == null || mapElement.id == undefined) return null;
	// An array of Link object that where previus linked to this new element if already exists

	var mapElementLinked = new Array();
	// if exist first save link then remove element (of course with links)
	if (this.mapElements[mapElement.id] != undefined) { 
		mapElementLinked = this.getLinksOnElement(mapElement.id);
		this.deleteMapElement(mapElement.id);
	}
	
	// now add map element
	this.mapElements[mapElement.id] = mapElement;
	this.mapElementSize++;
	this.svgNode.appendChild(this.mapElements[mapElement.id].getSvgNode());
	
	elementAdded=true;
	// add links previusly saved 
	if ( mapElementLinked != null && mapElementLinked.length != 0) {
		var elemToRender = null;
		for (elemToRender in mapElementLinked) {
			this.setLink(mapElement.id, elemToRender, mapElementLinked[elemToRender].getStroke(), mapElementLinked[elemToRender].getStrokeWidth());
		}
	}
	return elementAdded;
}


Map.prototype.addMapElement = function(mapElement) {

	var elementAdded=false;

	var labelText = mapElement.label.text;		

	if (mapElement == null || mapElement.id == undefined) return null;
	// An array of Link object that where previus linked to this new element if already exists

	var mapElementLinked = new Array();
	// if exist first save link then remove element (of course with links)
	if (this.mapElements[mapElement.id] != undefined) { 
		mapElementLinked = this.getLinksOnElement(mapElement.id);
		this.deleteMapElement(mapElement.id);
	}
	
	// now add map element
	this.mapElements[mapElement.id] = mapElement;
	this.mapElementSize++;
	
	elementAdded=true;
	// add links previusly saved 
	
	if ( mapElementLinked != null && mapElementLinked.length != 0) {
		var elemToRender = null;
		for (elemToRender in mapElementLinked) {
			alert('adding link '+mapElement.id+' '+elemToRender);
			this.addLink(mapElement.id, elemToRender, mapElementLinked[elemToRender].getStroke(), mapElementLinked[elemToRender].getStrokeWidth());
		}
	}
	return elementAdded;
}

// delete the node (and all its links) with the id in input
Map.prototype.deleteMapElement = function(elemId)
{
	var elementDeleted = false;
	if (elemId == undefined) {
		return elementDeleted;
	}

	//remove the links of the element
	this.deleteLinksOnElement(elemId);
		
	//remove the element from the svg view	

	if(this.mapElements !=null) {
		var i = 0;
		var tempNodes = new Array();
		var curnode = null;
		for (currNode in this.mapElements){
			if (currNode == elemId ) {
				var elem = this.mapElements[currNode].getSvgNode();
				if(elem.parentNode==this.svgNode){
					this.svgNode.removeChild(elem);
					elementDeleted = true;
					//alert("removed"+currNode);
				}
			} else {
				i++;
				tempNodes[currNode]=this.mapElements[currNode];
			}
		}
		this.mapElements=tempNodes;
		this.mapElementSize = i;
	}
	return elementDeleted;
}

Map.prototype.getLinkId = function(id1,id2) {

	var a = id1.toString();
	var b = id2.toString();
	var id = a + "-" + b;
	var na = a.substr(0,id1.length-2);
	var nb = b.substr(0,id2.length-2);
	if (na > nb) {
		id = b + "-" + a;
	}
	
	if (na == nb && id2.indexOf(MAP_TYPE)== -1) {
		id = b + "-" + a;
	}
	return id;
} 

Map.prototype.matchLink = function(id,idlink) {

	return ( idlink.substr(0,id.length+1)==id+"-" || idlink.substr(idlink.length-id.length-1,idlink.length)=="-"+id );
} 

Map.prototype.getLinksOnElement = function(id)
{
	var elements = new Array();
	var elemToRender = null;
	for (elemToRender in this.mapLinks) {
	if(this.matchLink(id,elemToRender))
		elements[elemToRender] = this.mapLinks[elemToRender];
	}
	return elements;
}

Map.prototype.deleteLinksOnElement = function(id)
{
	var linksDeleted = false;
	if(this.mapLinks!=null){
		var elements = new Array();
		var elemToRender = null;
		var  i = 0;
		for (elemToRender in this.mapLinks) {
			if(this.matchLink(id,elemToRender)) {
				var elem = this.mapLinks[elemToRender].getSvgNode();
				if(elem.parentNode==this.svgNode){
					this.svgNode.removeChild(elem);
					linksDeleted = true;
					//alert("removed"+elemToRender);
				}
			} else {
				i++;
				elements[elemToRender] = this.mapLinks[elemToRender];
			}
		}
		this.mapLinks = elements;
		this.mapLinkSize = i;
	}
	return linksDeleted;
}

// add a new link to map
Map.prototype.addLink = function(id1, id2, stroke, stroke_width)
{
//alert("Map::addLink("+id1+", "+id2+")");
	// check parameter
	var linkAdded = false;
	if (id1 == id2 ) {
		//alertDebug("id1 and id2 must be different");
		return linkAdded;
	}
	
	//gets elements
	var first = null;
	var second = null;

	//remove the element from the svg view	
	if(this.mapElements !=null && id1 != null && id2 != null )
	{
		first = this.mapElements[id1];
		second = this.mapElements[id2];
	} else {
		return linkAdded;
	}

	if (first == undefined || second == undefined) {
		//alert("Paramater id1 error: map doesn't contain mapnode with id=" + id1);
		return linkAdded;
	}

	var id = this.getLinkId(id1,id2);
	//alert(id);
	if( (this.mapLinks[id] != undefined) ){
		//alertDebug("Map already contains the element");
		this.deleteLink(id1,id2);
	}

	var link = new Link(id, first, second, stroke, stroke_width);
	this.mapLinks[id] = link;
	this.mapLinkSize++;
	linkAdded = true;
	//alert('link with id '+id+' added');
	return linkAdded;
}

Map.prototype.setLink = function(id1, id2, stroke, stroke_width)
{
//alert("Map::addLink("+id1+", "+id2+")");
	// check parameter
	var linkAdded = false;
	if (id1 == id2 ) {
		//alertDebug("id1 and id2 must be different");
		return linkAdded;
	}
	
	//gets elements
	var first = null;
	var second = null;

	//remove the element from the svg view	
	if(this.mapElements !=null && id1 != null && id2 != null )
	{
		first = this.mapElements[id1];
		second = this.mapElements[id2];
	} else {
		return linkAdded;
	}

	if (first == undefined || second == undefined) {
		//alert("Paramater id1 error: map doesn't contain mapnode with id=" + id1);
		return linkAdded;
	}

	var id = this.getLinkId(id1,id2);
	
	if( (this.mapLinks[id] != undefined) ){
		//alertDebug("Map already contains the element");
		this.deleteLink(id1,id2);
	}

	var link = new Link(id, first, second, stroke, stroke_width);
	this.mapLinks[id] = link;
	this.mapLinkSize++;
	this.svgNode.appendChild(link.getSvgNode());
	linkAdded = true;
	return linkAdded;
}

// delete the link with the id in input
Map.prototype.deleteLink = function(id1,id2)
{
	var linkDeleted = false;
	var linkId = this.getLinkId(id1,id2);
	if(this.mapLinks!=null && linkId !=null){
		var elem = this.mapLinks[linkId].getSvgNode();
		if(elem.parentNode==this.svgNode){
			this.svgNode.removeChild(elem);
			linkDeleted = true;
		}
	}

	//remove the link from the links array
	var tempLinks = new Array();
	for (currLink in this.mapLinks){
		if(linkId !=currLink)
			tempLinks[currLink]=this.mapLinks[currLink];
	}
	this.MapLinks=tempLinks;
	this.mapLinkSize=tempLinks.length;	
	return linkDeleted;
}

// this render graphs of nodes and links
Map.prototype.render = function()
{
	this.clean();
	//try to set the background (first the color and then the image)
	this.setBackgroundColor("#"+currentMapBackGround);
	this.setBackgroundImage(currentMapBackGround)
	
	//this.rect.setAttribute("fill", "#"+currentMapBackGround);	
	var elemToRender = null;
	for (elemToRender in this.mapLinks) //render links
		this.svgNode.appendChild(this.mapLinks[elemToRender].getSvgNode());
	for (elemToRender in this.mapElements) //render mapElement
		this.svgNode.appendChild(this.mapElements[elemToRender].getSvgNode());
}

// delete all nodes from map view
Map.prototype.clean = function()
{
	var elemToRender = null;
	//alert("clean in");
	if(this.mapLinks!=null && this.mapLinkSize>0)
	
	for (elemToRender in this.mapLinks){
		//alert(elemToRender);
		if(elemToRender!=null){
			var elem = this.mapLinks[elemToRender].getSvgNode();
			if(elem.parentNode==this.svgNode){
				this.svgNode.removeChild(elem);
			}
		}
	}
	if(this.mapElements!=null && this.mapElementSize>0)
	for (elemToRender in this.mapElements){
		//alert(elemToRender);
		if(elemToRender!=null){
			var elem = this.mapElements[elemToRender].getSvgNode();
			if(elem.parentNode==this.svgNode){
				this.svgNode.removeChild(elem);
			}
		}
	}
	//alert("clean out");
}

// delete all nodes and  from map 
Map.prototype.clear = function()
{
	this.clean();

	this.mapLinks=new Array();
	this.mapLinkSize=0;

	this.mapElements=new Array();
	this.mapElementSize=0;

}

//delete all  from map view
Map.prototype.cleanLinks = function(){
	
	if(this.mapLinks!=null && this.mapLinkSize>0)
	for (currLink in this.mapLinks){
		var elem = this.mapLinks[currLink].getSvgNode();
		if(elem.parentNode==this.svgNode){
			this.svgNode.removeChild(elem);
		}
	}
}

//delete all links from map
Map.prototype.clearLinks = function(){
	this.cleanLinks();

	this.mapLinks=new Array();
	this.mapLinkSize=0;

}

Map.prototype.redrawLink = function()
{
	for(var i in this.mapLinks)
	{
		this.mapLinks[i].update();
	}	
}

Map.prototype.redrawLinkOnElement = function(id)
{
	for (var i in this.mapLinks) {
		if(i.substr(0,id.length)==id || i.substr(i.length-id.length,i.length)==id ) 
			this.mapLinks[i].update();
	} 
}

Map.prototype.getMapElement = function(id)
{
return this.mapElements[id];

}
Map.prototype.onMouseDownOnMap = onMouseDownOnMap;
Map.prototype.onMouseMove = onMouseMove;
Map.prototype.onMouseUp = onMouseUp;


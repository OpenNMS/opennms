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
	this.offset = document.documentElement.createSVGPoint();
	this.svgNode = document.createElementNS(svgNS,"g");
	
	this.rect = document.createElementNS(svgNS,"rect"); 
	this.rect.setAttributeNS(null,"width", width);
	this.rect.setAttributeNS(null,"height", height);
	this.rect.setAttributeNS(null,"fill", color);
	this.rect.setAttributeNS(null,"stroke-width","1");
	
	this.image = document.createElementNS(svgNS,"image");
	this.image.setAttributeNS(null,"width", width);
	this.image.setAttributeNS(null,"height", height);
	this.image.setAttributeNS(null,"x", x);
	this.image.setAttributeNS(null,"y", y);
	
	//this.image.setAttribute("preserveAspectRatio","xMidYMid");
	this.image.setAttributeNS(xlinkNS, 'xlink:href', image);

	this.background = document.createElementNS(svgNS,"g");
	this.background.setAttributeNS(null,"id", id);
	this.background.appendChild(this.rect);
	this.background.appendChild(this.image);

	this.background.addEventListener("mousemove", this.onMouseMove, false);
	this.background.addEventListener("mouseup", this.onMouseUp, false);
	this.background.addEventListener("mousedown", this.onMouseDownOnMap, false);
	
	this.svgNode.appendChild(this.background);

}

Map.prototype.setBackgroundColor = function(color){
	currentMapBackGround=color.split("#")[1];
	this.rect.setAttributeNS(null,"fill", color);
}

Map.prototype.setBackgroundImage = function(image){
	currentMapBackGround=image;
	this.image.setAttributeNS(xlinkNS, 'xlink:href', image);
}

//only show the map with the background image in input
Map.prototype.tryBackgroundImage = function(image){
	this.image.setAttributeNS(xlinkNS, 'xlink:href', image);
}

//get the width of the map
Map.prototype.getWidth = function(){
	return this.rect.getAttributeNS(null,"width");
}

//get the height of the map
Map.prototype.getHeight= function(){
	return this.rect.getAttributeNS(null,"height");
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
			this.setLink(mapElement.id, elemToRender, mapElementLinked[elemToRender].getTypology(),mapElementLinked[elemToRender].getStroke(), mapElementLinked[elemToRender].getStrokeWidth());
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
			this.addLink(mapElement.id, elemToRender, mapElementLinked[elemToRender].getTypology(), mapElementLinked[elemToRender].getStroke(), mapElementLinked[elemToRender].getStrokeWidth());
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

Map.prototype.getLinkId = function(id1,id2,typology) {

	var a = id1.toString();
	var b = id2.toString();
	var id = a + "-" + b;
	var na = a.substr(0,id1.length-1);
	var nb = b.substr(0,id2.length-1);
	
	if (na > nb) {
		id = b + "-" + a;
	}
	
	if (na == nb && id2.indexOf(MAP_TYPE)== -1) {
		id = b + "-" + a;
	}
	id=id+"-"+typology;
	//alert(id);
	return id;
} 

//return the result of matching by links' ids, ignoring link type
Map.prototype.matchLink = function(id,idlink) {
	var idLinkSplitted = idlink.split("-");
	return ( idLinkSplitted[0]==id || idLinkSplitted[1]==id );
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
		var elemToRender = null;
		var  i = 0;
		for (elemToRender in this.mapLinks) {
			if(this.matchLink(id,elemToRender)) {
				var ids=elemToRender.split('-');
				this.deleteLink(ids[0],ids[1],ids[2]);
				linksDeleted = true;
			} 
		}
//		this.mapLinks = elements;
//		this.mapLinkSize = i;
	}
	return linksDeleted;
}

// add a new link to map
Map.prototype.addLink = function(id1, id2, typology, stroke, stroke_width, dash_array, flash)
{
	var id = this.getLinkId(id1,id2,typology);
	if(this.mapLinks[id]==null){
		// check parameter
		var linkAdded = false;

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

		
		//alert(id+" " +this.mapLinks[id]);
		if( (this.mapLinks[id] != undefined) ){
			//alertDebug("Map already contains the element");
			this.deleteLink(id1,id2,typology);
		}

		//calculate and mantains the number of links between the elements
		var idSplitted = id.split("-");
		var idWithoutTypology = idSplitted[0]+"-"+idSplitted[1];
		if(linksBetweenElements[idWithoutTypology]==undefined){
			linksBetweenElements[idWithoutTypology]=new Array();
			linksBetweenElements[idWithoutTypology][typology]=1;
		}else{
			linksBetweenElements[idWithoutTypology][typology]++;
		}


		var link = new Link(id, typology, first, second, stroke, stroke_width, dash_array, flash);


		this.mapLinks[id] = link;
		this.mapLinkSize++;
		linkAdded = true;
		//alert('link with id '+id+' added');
	}
	return linkAdded;
}

Map.prototype.setLink = function(id1, id2, typology, stroke, stroke_width, dash_array, flash)
{
	// check parameter
	var linkAdded = false;
	
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
		return linkAdded;
	}

	var id = this.getLinkId(id1,id2,typology);
	if( (this.mapLinks[id] != undefined) ){
		this.deleteLink(id1,id2,typology);
	}
	
	//calculate and mantains the number of links between the elements
	var idSplitted = id.split("-");
	var idWithoutTypology = idSplitted[0]+"-"+idSplitted[1];
	if(linksBetweenElements[idWithoutTypology]==undefined){
		linksBetweenElements[idWithoutTypology]=new Array();
		linksBetweenElements[idWithoutTypology][typology]=1;
	}else{
		linksBetweenElements[idWithoutTypology][typology]++;
	}
	
	var link = new Link(id, typology, first, second, stroke, stroke_width, dash_array, flash);
			
	this.mapLinks[id] = link;
	this.mapLinkSize++;
	this.svgNode.appendChild(link.getSvgNode());
	linkAdded = true;
	return linkAdded;
}

// delete the link with the id in input
Map.prototype.deleteLink = function(id1,id2,typology)
{
	var links="";
	if(this.mapLinks!=null && this.mapLinkSize>0)
	for (currLink in this.mapLinks){
		links+=","+currLink;
	}
	
	var linkDeleted = false;
	var linkId = this.getLinkId(id1,id2,typology);
	if(this.mapLinks[linkId]==undefined){
		linkId = this.getLinkId(id2,id1,typology);
		if(this.mapLinks[linkId]==undefined){
			alert("Warning: link between "+id1+" and "+id2+" not found.");
			return linkDeleted;
		}
	}
	if(this.mapLinks!=null && linkId !=null){
		var elem = this.mapLinks[linkId].getSvgNode();
		if(elem!=null){
			if(elem.parentNode==this.svgNode){
				this.svgNode.removeChild(elem);
				linkDeleted = true;
			}
		}else{
			return linkDeleted;
		}
	}

	//remove the link from the links array
	var counter=0;
	var tempLinks = new Array();
	for (currLink in this.mapLinks){
		if(linkId !=currLink){
			tempLinks[currLink]=this.mapLinks[currLink];
			counter++;
		}
			
	}
	this.mapLinks=tempLinks;
	this.mapLinkSize=counter;	
	
	
	//calculate and mantains the number of links between the elements
	var idWithoutTypology = id1+"-"+id2;
	if(linksBetweenElements[idWithoutTypology]==undefined){
		linksBetweenElements[idWithoutTypology]=new Array();
		linksBetweenElements[idWithoutTypology][typology]=0;
	}else{
		linksBetweenElements[idWithoutTypology][typology]--;
	}
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

// delete all nodes and links from map view
Map.prototype.clean = function()
{
	var elemToRender = null;
	if(this.mapLinks!=null && this.mapLinkSize>0) {
	
		for (elemToRender in this.mapLinks){
			if(this.mapLinks[elemToRender]!=undefined){
				var elem = this.mapLinks[elemToRender].getSvgNode();
				if(elem.parentNode==this.svgNode){
					this.svgNode.removeChild(elem);
				}
			}
		}
	}
	if(this.mapElements!=null && this.mapElementSize>0) {
		for (elemToRender in this.mapElements){
			if(elemToRender!=null){
				var elem = this.mapElements[elemToRender].getSvgNode();
				if(elem.parentNode==this.svgNode){
					this.svgNode.removeChild(elem);
				}
			}
		}
	}
}

// delete all nodes and  from map 
Map.prototype.clear = function()
{
	this.clean();

	this.mapLinks=new Array();
	this.mapLinkSize=0;

	this.mapElements=new Array();
	this.mapElementSize=0;
	
	//reset the array mantaining the number of links between the elements
	linksBetweenElements=new Array();
}

//delete all  from map view
Map.prototype.cleanLinks = function(){
	//alert("this.mapLinks:"+this.mapLinks+"    this.mapLinkSize:"+this.mapLinkSize);
	if(this.mapLinks!=null && this.mapLinkSize>0)
	for (currLink in this.mapLinks){
		if(this.mapLinks[currLink]!=undefined){
			var elem = this.mapLinks[currLink].getSvgNode();
			//elem.parentNode.removeChild(elem);
			//alert(currLink);
			if(elem.parentNode==this.svgNode){
				this.svgNode.removeChild(elem);
			}
		}
	}
}

//delete all links from map
Map.prototype.clearLinks = function(){
	this.cleanLinks();

	this.mapLinks=new Array();
	this.mapLinkSize=0;

		//reset the array mantaining the number of links between the elements
	linksBetweenElements=new Array();
	//alert('links cleaned');
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
		var splittedLinkId = i.split("-");
		if(splittedLinkId[0]==id || splittedLinkId[1]==id ) 
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


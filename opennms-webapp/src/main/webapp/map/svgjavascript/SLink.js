// == Slink.js -- 

SLink.prototype = new SVGElement;
SLink.superclass = SVGElement.prototype;

function SLink(id, typology, mapElement1,mapElement2, stroke, stroke_width, dash_array, totalLinks, deltaLink)
{
	if (arguments.length >= 9) {
		var idSplitted = id.split("-");
		
		if(mapElement1.id==idSplitted[1]){
			var tmp = mapElement1;
			mapElement1=mapElement2;
			mapElement2=tmp;
		}
		
		this.typology=typology;
		this.animateTag = null;
		this.id = id;
		
		this.status = 'Not Defined';
		this.mapElement1 = mapElement1;
		this.mapElement2 = mapElement2;
		
		var x1 = this.mapElement1.getX()+this.mapElement1.width/2;
		var y1 = this.mapElement1.getY()+this.mapElement1.height/2+deltaLink*totalLinks;

		var x2=this.mapElement2.getX()+this.mapElement2.width/2;
		var y2=this.mapElement2.getY()+this.mapElement2.height/2+deltaLink*totalLinks;
		
		this.deltaX1FromElem1Center=x1-this.mapElement1.getX();
		this.deltaY1FromElem1Center=y1-this.mapElement1.getY();
		this.deltaX2FromElem2Center=x2-this.mapElement2.getX();
		this.deltaY2FromElem2Center=y2-this.mapElement2.getY();

		this.init(id, x1, x2, y1, y2, stroke, stroke_width, dash_array);
	}
	else
		alert("Summary Link constructor call error");
}

// <line onclick="*" stroke="*" stroke-width="*"/>
SLink.prototype.init = function(id, x1, x2, y1, y2, stroke, stroke_width, dash_array)
{

	this.stroke =stroke;
	this.stroke_width=stroke.width;
	this.dash_array = dash_array;

	this.links = new Array();
	this.statusMap = new Array();
	this.numberOfLinks=0;
	this.numberOfMultiLinks=0;
	
	this.svgNode = document.createElementNS(svgNS,"g");
	this.svgNode.setAttributeNS(null,"id", id);	

	this.line = document.createElementNS(svgNS,"line");
	this.line.setAttributeNS(null,"id", id+"line");	
	this.line.setAttributeNS(null,"x1", x1);	
	this.line.setAttributeNS(null,"x2", x2);	
	this.line.setAttributeNS(null,"y1", y1);
	this.line.setAttributeNS(null,"y2", y2);
	this.line.setAttributeNS(null,"stroke", stroke);
	this.line.setAttributeNS(null,"stroke-width", stroke_width);
	if(dash_array!=-1 && dash_array!=0){
		this.line.setAttributeNS(null,"stroke-dasharray", dash_array);
	}
	this.line.setAttributeNS(null,"style", "z-index:0");
	this.line.setAttributeNS(null,"cursor", "pointer");
	this.line.addEventListener("click", this.onClick, false);
	this.line.addEventListener("mouseover", this.onMouseOver, false);
	this.line.addEventListener("mouseout", this.onMouseOut, false);
	this.svgNode.appendChild(this.line);
	
	this.animateTag = document.createElementNS(svgNS,"animate");
	this.animateTag.setAttributeNS(null,"attributeName", "stroke");	
	this.animateTag.setAttributeNS(null,"from", stroke);	
	this.animateTag.setAttributeNS(null,"to", "white");
	this.animateTag.setAttributeNS(null,"dur", "0ms");
	this.animateTag.setAttributeNS(null,"repeatCount", "indefinite");
	this.animateTag.addEventListener("repeat", this.onRepeat, false);
	this.line.appendChild(this.animateTag);
}

SLink.prototype.getId = function() {
	return this.id;
}

SLink.prototype.getMapElement1 = function() {
	return this.mapElement1;
}

SLink.prototype.getMapElement2 = function() {
	return this.mapElement2;
}

SLink.prototype.getTypology = function() {
	return this.typology;
}

SLink.prototype.getStatus = function() {
	return this.status;
}

SLink.prototype.getStroke = function() {
	return this.line.getAttribute("stroke");
}

SLink.prototype.getStrokeWidth = function() {
	return this.line.getAttribute("stroke-width");
}

SLink.prototype.getDashArray = function() {
	return this.line.getAttribute("dash-array");
}

SLink.prototype.getFlash = function() {
	return this.flash;
}

/*
	set flashing of link
*/
SLink.prototype.setFlash = function(bool)
{
	this.flash=bool;
	if (this.animateTag != null) 
	{
		var val;
		if (bool)
			val = "400ms";
		else
			val = "0";
		
		this.animateTag.setAttribute("dur", val);
	}
}

SLink.prototype.getFirstElementId = function()
{
     var ids = this.id.split('-');
     return ids[0];
}

SLink.prototype.getSecondElementId = function()
{
     var ids = this.id.split('-');
     return ids[1];
}

SLink.prototype.getStatusMap = function()
{
	return this.statusMap;
}

SLink.prototype.getNumberOfLinks = function()
{
	return this.numberOfLinks;
}

SLink.prototype.getNumberOfMultiLinks = function()
{
	return this.numberOfMultiLinks;
}

SLink.prototype.getLinks = function()
{
	return this.links;
}

SLink.prototype.addLink = function(link)
{
	this.links[link.id]=link;
	this.numberOfMultiLinks++;
	this.numberOfLinks=this.numberOfLinks+link.getNumberOfLinks();
	var statutesfound=0;
	for (var status in link.getStatusMap()) {
	    statutesfound++;
		if (this.statusMap[status]==undefined) {
			this.statusMap[status]=link.getStatusMap()[status];
		} else {
		  var i = this.statusMap[status]+link.getStatusMap()[status];
		  this.statusMap[status] = i;
		}
	}
	if (statutesfound == 1) {
		this.line.setAttributeNS(null,"stroke", link.stroke);
	} else {
		this.line.setAttributeNS(null,"stroke", this.stroke);
	}
}

SLink.prototype.switch = function(linkId)
{
	if (this.id == linkId) {
		this.line.setAttributeNS(null,"stroke", this.stroke);
		this.line.setAttributeNS(null,"stroke-width", this.stroke_width);
		if(this.dash_array!=-1 && this.dash_array!=0){
			this.line.setAttributeNS(null,"stroke-dasharray", this.dash_array);
		} else {
			this.line.setAttributeNS(null,"stroke-dasharray", 0);
		}
		this.setFlash(false);
	} else {
		var link = links[linkId];
		if ( link==undefined )
			return;
		this.line.setAttributeNS(null,"stroke", link.stroke);
		this.line.setAttributeNS(null,"stroke-width", link.stroke_width);
		if(link.dash_array!=-1 && link.dash_array!=0){
			this.line.setAttributeNS(null,"stroke-dasharray", link.dash_array);
		} else {
			this.line.setAttributeNS(null,"stroke-dasharray", 0);
		}
		this.setFlash(link.flash);
	}
}

// update link
SLink.prototype.update = function()
{
	var x1=this.mapElement1.getX()+this.deltaX1FromElem1Center;
	var y1=this.mapElement1.getY()+this.deltaY1FromElem1Center;	
	
	var x2=this.mapElement2.getX()+this.deltaX2FromElem2Center;	
	var y2=this.mapElement2.getY()+this.deltaY2FromElem2Center;	

	this.line.setAttributeNS(null,"x1", x1);	
	this.line.setAttributeNS(null,"x2", x2);	
	this.line.setAttributeNS(null,"y1", y1);
	this.line.setAttributeNS(null,"y2", y2);	
}

/*
	OnRepeat event handler
*/
SLink.prototype.onRepeat = function(evt)
{
	var from = evt.getTarget().getAttribute("from");
	var to = evt.getTarget().getAttribute("to");
	if (from == "white") {
		from = evt.getTarget().parentNode.getAttribute("stroke");
		to = "white";
	}
	else {
		from = "white";
		to = evt.getTarget().parentNode.getAttribute("stroke");
	}
	evt.getTarget().setAttribute("from", from);
	evt.getTarget().setAttribute("to", to);	
}

SLink.prototype.onClick = onMouseDownOnSLink;
SLink.prototype.onMouseOver = onMouseOverLink;
SLink.prototype.onMouseOut  = onMouseOutLink;
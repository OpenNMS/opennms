// == link.js -- 

Link.prototype = new SVGElement;
Link.superclass = SVGElement.prototype;

function Link(id, typology, status, numberOfLinks, statusMap, mapElement1, mapElement2, stroke, stroke_width, dash_array, flash, totalLinks, deltaLink,nodeid1,nodeid2)
{
	if (arguments.length >= 8) {
		var idSplitted = id.split("-");
		
		if(mapElement1.id==idSplitted[1]){
			var tmp = mapElement1;
			mapElement1=mapElement2;
			mapElement2=tmp;
			tmp = nodeid1;
			nodeid1 = nodeid2;
			nodeid2 = tmp;
		}
		
		this.typology=typology;
		this.status=status;
		this.animateTag = null;
		this.id = id;
		
		this.mapElement1 = mapElement1;
		this.mapElement2 = mapElement2;
		
		this.nodeid1=nodeid1;
		this.nodeid2=nodeid2;

		this.numberOfLinks = numberOfLinks;
		this.statusMap = statusMap;

		this.stroke =stroke;
		this.stroke_width=stroke.width;
		this.dash_array = dash_array;
		
		this.totalLinks = totalLinks;
		this.deltaLink = deltaLink;

		var alfa = Math.atan((this.mapElement2.getY()-this.mapElement1.getY())/(this.mapElement2.getX()-this.mapElement1.getX()));
		
		var delta1,delta2;
		
		if (totalLinks%2 == 0) {
			delta1 = -1*totalLinks*deltaLink/2/this.mapElement1.getRadius();
			delta2 = -1*totalLinks*deltaLink/2/this.mapElement2.getRadius();
		} else {
			delta1 = (totalLinks+1)*(deltaLink)/2/this.mapElement1.getRadius();
			delta2 = (totalLinks+1)*(deltaLink)/2/this.mapElement2.getRadius();
		}
		
		var x1,y1,x2,y2;
		if ( this.mapElement2.getX() > this.mapElement1.getX() ) {
			x1 = this.mapElement1.getCX()+(this.mapElement1.getRadius()*Math.cos(alfa+delta1));
			y1 = this.mapElement1.getCY()+(this.mapElement1.getRadius()*Math.sin(alfa+delta1));
	
			x2 = this.mapElement2.getCX()-(this.mapElement2.getRadius()*Math.cos(alfa-delta2));
			y2 = this.mapElement2.getCY()-(this.mapElement2.getRadius()*Math.sin(alfa-delta2));
		} else 	if ( this.mapElement2.getX() == this.mapElement1.getX() && alfa < 0) {
			x1 = this.mapElement1.getCX()+(this.mapElement1.getRadius()*Math.sin(delta1));
			y1 = this.mapElement1.getCY()-(this.mapElement1.getRadius()*Math.cos(delta1));
	
			x2 = this.mapElement2.getCX()+(this.mapElement2.getRadius()*Math.sin(delta2));
			y2 = this.mapElement2.getCY()+(this.mapElement2.getRadius()*Math.cos(delta2));
		} else 	if ( this.mapElement2.getX() == this.mapElement1.getX() && alfa > 0) {
			x1 = this.mapElement1.getCX()-(this.mapElement1.getRadius()*Math.sin(delta1));
			y1 = this.mapElement1.getCY()+(this.mapElement1.getRadius()*Math.cos(delta1));
	
			x2 = this.mapElement2.getCX()-(this.mapElement2.getRadius()*Math.sin(delta2));
			y2 = this.mapElement2.getCY()-(this.mapElement2.getRadius()*Math.cos(delta2));
		} else {
			x1 = this.mapElement1.getCX()-(this.mapElement1.getRadius()*Math.cos(alfa-delta1));
			y1 = this.mapElement1.getCY()-(this.mapElement1.getRadius()*Math.sin(alfa-delta1));
	
			x2 = this.mapElement2.getCX()+(this.mapElement2.getRadius()*Math.cos(alfa+delta2));
			y2 = this.mapElement2.getCY()+(this.mapElement2.getRadius()*Math.sin(alfa+delta2));			
		}

		this.init(id, x1, x2, y1, y2, stroke, stroke_width, dash_array, flash);
	}
	else
		alert("Link constructor call error");
}

// <line onclick="*" stroke="*" stroke-width="*"/>
Link.prototype.init = function(id, x1, x2, y1, y2, stroke, stroke_width, dash_array, flash)
{
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
	this.flash=false;
	if(flash!=undefined && flash==true)
		this.setFlash(true);
}

Link.prototype.getId = function() {
	return this.id;
}

Link.prototype.getMapElement1 = function() {
	return this.mapElement1;
}

Link.prototype.getMapElement2 = function() {
	return this.mapElement2;
}

Link.prototype.getTypology = function() {
	return this.typology;
}

Link.prototype.getStatus = function() {
	return this.status;
}

Link.prototype.getStroke = function() {
	return this.line.getAttribute("stroke");
}

Link.prototype.getStrokeWidth = function() {
	return this.line.getAttribute("stroke-width");
}

Link.prototype.getDashArray = function() {
	return this.line.getAttribute("dash-array");
}

Link.prototype.getFlash = function() {
	return this.flash;
}

/*
	set flashing of link
*/
Link.prototype.setFlash = function(bool)
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

Link.prototype.getFirstElementId = function()
{
     var ids = this.id.split('-');
     return ids[0];
}

Link.prototype.getSecondElementId = function()
{
     var ids = this.id.split('-');
     return ids[1];
}

Link.prototype.getFirstNodeId = function()
{
	return this.nodeid1;
}

Link.prototype.getSecondNodeId = function()
{
	return this.nodeid2;
}

Link.prototype.getStatusMap = function()
{
	return this.statusMap;
}

Link.prototype.getNumberOfLinks = function()
{
	return this.numberOfLinks;
}

// update link
Link.prototype.update = function()
{
	var alfa = Math.atan((this.mapElement2.getY()-this.mapElement1.getY())/(this.mapElement2.getX()-this.mapElement1.getX()));

	var delta1,delta2;
	if (this.totalLinks%2 == 0) {
		delta1 = -1*this.totalLinks*this.deltaLink/2/this.mapElement1.getRadius();
		delta2 = -1*this.totalLinks*this.deltaLink/2/this.mapElement2.getRadius();
	} else {
		delta1 = (this.totalLinks+1)*(this.deltaLink)/2/this.mapElement1.getRadius();
		delta2 = (this.totalLinks+1)*(this.deltaLink)/2/this.mapElement2.getRadius();
	}

	var x1,y1,x2,y2;
	if ( this.mapElement2.getX() > this.mapElement1.getX() ) {
		x1 = this.mapElement1.getCX()+(this.mapElement1.getRadius()*Math.cos(alfa+delta1));
		y1 = this.mapElement1.getCY()+(this.mapElement1.getRadius()*Math.sin(alfa+delta1));

		x2 = this.mapElement2.getCX()-(this.mapElement2.getRadius()*Math.cos(alfa-delta2));
		y2 = this.mapElement2.getCY()-(this.mapElement2.getRadius()*Math.sin(alfa-delta2));
	} else 	if ( this.mapElement2.getX() == this.mapElement1.getX() && alfa < 0) {
		x1 = this.mapElement1.getCX()+(this.mapElement1.getRadius()*Math.sin(delta1));
		y1 = this.mapElement1.getCY()-(this.mapElement1.getRadius()*Math.cos(delta1));

		x2 = this.mapElement2.getCX()+(this.mapElement2.getRadius()*Math.sin(delta2));
		y2 = this.mapElement2.getCY()+(this.mapElement2.getRadius()*Math.cos(delta2));
	} else 	if ( this.mapElement2.getX() == this.mapElement1.getX() && alfa > 0) {
		x1 = this.mapElement1.getCX()-(this.mapElement1.getRadius()*Math.sin(delta1));
		y1 = this.mapElement1.getCY()+(this.mapElement1.getRadius()*Math.cos(delta1));

		x2 = this.mapElement2.getCX()-(this.mapElement2.getRadius()*Math.sin(delta2));
		y2 = this.mapElement2.getCY()-(this.mapElement2.getRadius()*Math.cos(delta2));
	} else {
		x1 = this.mapElement1.getCX()-(this.mapElement1.getRadius()*Math.cos(alfa-delta1));
		y1 = this.mapElement1.getCY()-(this.mapElement1.getRadius()*Math.sin(alfa-delta1));

		x2 = this.mapElement2.getCX()+(this.mapElement2.getRadius()*Math.cos(alfa+delta2));
		y2 = this.mapElement2.getCY()+(this.mapElement2.getRadius()*Math.sin(alfa+delta2));			
	}

	this.line.setAttributeNS(null,"x1", x1);	
	this.line.setAttributeNS(null,"x2", x2);	
	this.line.setAttributeNS(null,"y1", y1);
	this.line.setAttributeNS(null,"y2", y2);	
}

/*
	OnRepeat event handler
*/
Link.prototype.onRepeat = function(evt)
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

Link.prototype.onClick = onMouseDownOnLink;
Link.prototype.onMouseOver = onMouseOverLink;
Link.prototype.onMouseOut  = onMouseOutLink;
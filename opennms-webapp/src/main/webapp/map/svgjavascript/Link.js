// == link.js -- 

Link.prototype = new SVGElement;
Link.superclass = SVGElement.prototype;

function Link(id, typology, status, mapElement1, mapElement2, stroke, stroke_width, dash_array, flash, totalLinks, deltaLink,nodeid1,nodeid2)
{
	if (arguments.length >= 8) {
		var idSplitted = id.split("-");
		
		if(mapElement1.id==idSplitted[1]){
			var tmp = mapElement1;
			mapElement1=mapElement2;
			mapElement2=tmp;
		}
		
		this.typology=typology;
		this.status=status;
		this.animateTag = null;
		this.id = id;
		
		this.mapElement1 = mapElement1;
		this.mapElement2 = mapElement2;
		
		this.nodeid1=nodeid1;
		this.nodeid2=nodeid2;

//		var heightCapacity=this.mapElement1.height/deltaLink;		
//		var widthCapacity=this.mapElement1.width/deltaLink;		

		var x1 = this.mapElement1.getX()+this.mapElement1.width/2;
		var y1 = this.mapElement1.getY()+this.mapElement1.height/2+deltaLink*totalLinks;
//		var startY2=this.mapElement2.getY()-parseInt((heightCapacity-1)/2)*deltaLink;		
		var x2=this.mapElement2.getX()+this.mapElement2.width/2;
		var y2=this.mapElement2.getY()+this.mapElement2.height/2+deltaLink*totalLinks;
		
		this.deltaX1FromElem1Center=x1-this.mapElement1.getX();
		this.deltaY1FromElem1Center=y1-this.mapElement1.getY();
		this.deltaX2FromElem2Center=x2-this.mapElement2.getX();
		this.deltaY2FromElem2Center=y2-this.mapElement2.getY();

		this.init(id, x1, x2, y1, y2, stroke, stroke_width, dash_array, flash);
	}
	else
		alert("Link constructor call error");
}

// <line onclick="*" stroke="*" stroke-width="*"/>
Link.prototype.init = function(id, x1, x2, y1, y2, stroke, stroke_width, dash_array, flash)
{
	this.svgNode = document.createElementNS(svgNS,"line");
	this.svgNode.setAttributeNS(null,"id", id);	
	this.svgNode.setAttributeNS(null,"x1", x1);	
	this.svgNode.setAttributeNS(null,"x2", x2);	
	this.svgNode.setAttributeNS(null,"y1", y1);
	this.svgNode.setAttributeNS(null,"y2", y2);
	this.svgNode.setAttributeNS(null,"stroke", stroke);
	this.svgNode.setAttributeNS(null,"stroke-width", stroke_width);
	if(dash_array!=-1 && dash_array!=0){
		this.svgNode.setAttributeNS(null,"stroke-dasharray", dash_array);
	}
	this.svgNode.setAttributeNS(null,"style", "z-index:0");
	this.svgNode.setAttributeNS(null,"cursor", "pointer");
	this.svgNode.addEventListener("click", this.onClick, false);
	this.animateTag = document.createElementNS(svgNS,"animate");
	this.animateTag.setAttributeNS(null,"attributeName", "stroke");	
	this.animateTag.setAttributeNS(null,"from", stroke);	
	this.animateTag.setAttributeNS(null,"to", "white");
	this.animateTag.setAttributeNS(null,"dur", "0ms");
	this.animateTag.setAttributeNS(null,"repeatCount", "indefinite");
	this.animateTag.addEventListener("repeat", this.onRepeat, false);
	this.svgNode.appendChild(this.animateTag);
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
	return this.svgNode.getAttribute("stroke");
}

Link.prototype.getStrokeWidth = function() {
	return this.svgNode.getAttribute("stroke-width");
}

Link.prototype.getDashArray = function() {
	return this.svgNode.getAttribute("dash-array");
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

// update link
Link.prototype.update = function()
{
	var x1=this.mapElement1.getX()+this.deltaX1FromElem1Center;
	var y1=this.mapElement1.getY()+this.deltaY1FromElem1Center;	
	
	var x2=this.mapElement2.getX()+this.deltaX2FromElem2Center;	
	var y2=this.mapElement2.getY()+this.deltaY2FromElem2Center;	

	this.svgNode.setAttributeNS(null,"x1", x1);	
	this.svgNode.setAttributeNS(null,"x2", x2);	
	this.svgNode.setAttributeNS(null,"y1", y1);
	this.svgNode.setAttributeNS(null,"y2", y2);	
}

Link.prototype.getInfo = function(evt)
{
	var text = document.createElementNS(svgNS,"text");
	text.setAttributeNS(null, "x","3");
	text.setAttributeNS(null, "dy","15");
	text.setAttributeNS(null, "id","topInfoTextTitle");
	text.setAttributeNS(null, "font-size",titleFontSize);
	
	var textLabel = document.createTextNode("Link info");
	text.appendChild(textLabel);
	
	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","20");
	var tspanContent = document.createTextNode(" From: " + nodeidSortAss[this.nodeid1].getLabel());
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspanContent = document.createTextNode("    To: " + nodeidSortAss[this.nodeid2].getLabel());
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "fill",LINKSTATUS_COLOR[this.status]);
	tspanContent = document.createTextNode(" Status: "+this.status);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspanContent = document.createTextNode(" Type: "+LINK_TEXT[this.typology]);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspanContent = document.createTextNode(" Speed: "+LINK_SPEED[this.typology]);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);	
	
	return text;
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

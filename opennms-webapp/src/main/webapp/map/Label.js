// == Label.js -- Copyright (C) Michele Masullo ========================

Label.prototype = new MoveableSVGElement;
Label.superclass = MoveableSVGElement.prototype;

function Label(text, x, y, fontsize, linkToNodePage)
{
	if (arguments.length >= 4)
	{
	   //Title.scale  = doc.getDocumentElement().getCurrentScale();     // scaling modified by zooming ..
	   //Title.offset = doc.getDocumentElement().getCurrentTranslate(); // offset modified by zooming ..
	   this.init(text, x, y, fontsize, linkToNodePage);
	   //doc.getDocumentElement().addEventListener("zoom", Title.Zoom, false);
	} 
	else
		alert("Label constructor call error");
}

Label.prototype.init = function(text, x, y, fontsize, linkToNodePage)
{
	this.text = text;
	
	Label.superclass.init.call(this, "x", "y", x, y);
	this.svgNode = mapSvgDocument.createElement("a");
	
	this.svgNode.setAttributeNS("http://www.w3.org/1999/xlink","xlink:href",linkToNodePage);
	
	
	this.textNode = mapSvgDocument.createElement("text");	
	this.textNode.setAttribute(this.attributeX, x);	
	this.textNode.setAttribute(this.attributeY, y);
	this.textNode.setAttribute("text-anchor", "middle");
	
	this.disableLink();
	
	/* the link is disabled, to open the node page, double click is implemented.
	if (linkToNodePage == undefined) {
		this.disableLink();
	} else {
		this.enableLink();
	}*/
	this.textNode.setAttribute("font-size", fontsize);
	this.textNode.appendChild(mapSvgDocument.createTextNode(this.text));

	this.svgNode.appendChild(this.textNode);
}



Label.prototype.setFontSize = function(newFontSize){
	this.textNode.setAttribute("font-size", newFontSize);
	var x = Math.round(newFontSize/3)*2;
	
	var y = newFontSize*8/3 + newFontSize*2*0.7
	this.textNode.setAttribute(this.attributeX, x);	
	this.textNode.setAttribute(this.attributeY, y);	
}

Label.prototype.enableLink = function(){
	this.textNode.setAttribute("pointer-events", "visible");	
}

Label.prototype.disableLink = function(){
	this.textNode.setAttribute("pointer-events", "none");
}

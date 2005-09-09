// == Label.js -- Copyright (C) Michele Masullo ========================

Label.prototype = new MoveableSVGElement;
Label.superclass = MoveableSVGElement.prototype;

function Label(text, x, y, linkToNodePage)
{
	if (arguments.length == 4)
	{
	   //Title.scale  = doc.getDocumentElement().getCurrentScale();     // scaling modified by zooming ..
	   //Title.offset = doc.getDocumentElement().getCurrentTranslate(); // offset modified by zooming ..
	   this.init(text, x, y,linkToNodePage);
	   //doc.getDocumentElement().addEventListener("zoom", Title.Zoom, false);
	}
	else
		alert("Label constructor call error");
}

// <text x="*" y="*" text-anchor="*">*****</text>
Label.prototype.init = function(text, x, y,linkToNodePage)
{
	this.text = text;
	
	Label.superclass.init.call(this, "x", "y", x, y);
	this.svgNode = svgDocument.createElement("a");
	
	this.svgNode.setAttributeNS("http://www.w3.org/1999/xlink","xlink:href",linkToNodePage);
	
	
	this.textNode = svgDocument.createElement("text");	
	this.textNode.setAttribute(this.attributeX, x);	
	this.textNode.setAttribute(this.attributeY, y);
	this.textNode.setAttribute("text-anchor", "middle");
	this.textNode.setAttribute("pointer-events", "none");
	this.textNode.appendChild(svgDocument.createTextNode(this.text));
	this.svgNode.appendChild(this.textNode);
}

Label.prototype.enableLink = function(){
	this.textNode.setAttribute("pointer-events", "visible");	
}

Label.prototype.disableLink = function(){
	this.textNode.setAttribute("pointer-events", "none");
}

// == Semaphore.js --

// Semaphore inherits Element
Semaphore.prototype = new MoveableSVGElement;
Semaphore.superclass = MoveableSVGElement.prototype;

// constructor
function Semaphore(r, cx, cy, fill, stroke)
{
	if (arguments.length == 5)
	{
		this.animateTag = null; 
		//Title.scale  = doc.getDocumentElement().getCurrentScale();     // scaling modified by zooming ..
		//Title.offset = doc.getDocumentElement().getCurrentTranslate(); // offset modified by zooming ..
		this.init(r, cx, cy, fill, stroke);
		//doc.getDocumentElement().addEventListener("zoom", Title.Zoom, false);
	}
	else
		alert("Semaphore constructor call error");
}

// <circle r="*" cx="*" cy="*" stroke="*" fill="*">
// 		<animate attributeName="fill" from="*" to="white" dur="400ms" repeatCount="indefinite" onrepeat="onRepeat(evt)"/>
// </circle>
Semaphore.prototype.init = function(r, cx, cy, fill, stroke)
{
	Semaphore.superclass.init.call(this, "cx", "cy", cx, cy);
	
	this.svgNode = mapSvgDocument.createElement("circle");
	this.svgNode.setAttribute("r", r);	
	this.svgNode.setAttribute(this.attributeX, cx);	
	this.svgNode.setAttribute(this.attributeY, cy);
	this.svgNode.setAttribute("fill", fill);
	this.svgNode.setAttribute("stroke", stroke);
	this.animateTag = mapSvgDocument.createElement("animate");
	this.animateTag.setAttribute("attributeName", "fill");	
	this.animateTag.setAttribute("from", fill);	
	this.animateTag.setAttribute("to", "white");
	this.animateTag.setAttribute("dur", "400ms");
	this.animateTag.setAttribute("repeatCount", "indefinite");
	this.animateTag.addEventListener("repeat", this.onRepeat, false);
	this.svgNode.appendChild(this.animateTag);
	
}

Semaphore.prototype.setDimension = function(newDimension){
	this.svgNode.setAttribute("r", newDimension);
	this.svgNode.setAttribute(this.attributeX, 5*newDimension);	
	this.svgNode.setAttribute(this.attributeY, newDimension*16/3);	
}

/*
	set flashing of semaphore
*/
Semaphore.prototype.flash = function(bool)
{
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

/*
	OnRepeat event handler
*/
Semaphore.prototype.onRepeat = function(evt)
{
	var from = evt.getTarget().getAttribute("from");
	var to = evt.getTarget().getAttribute("to");
	if (from == "white") {
		from = evt.getTarget().parentNode.getAttribute("fill");
		to = "white";
	}
	else {
		from = "white";
		to = evt.getTarget().parentNode.getAttribute("fill");
	}
	evt.getTarget().setAttribute("from", from);
	evt.getTarget().setAttribute("to", to);	
}
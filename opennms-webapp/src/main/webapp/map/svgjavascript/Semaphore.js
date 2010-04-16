// == Semaphore.js --

// Semaphore inherits Element
Semaphore.prototype = new MoveableSVGElement;
Semaphore.superclass = MoveableSVGElement.prototype;

// constructor
function Semaphore(width,height, x, y, fill, stroke)
//function Semaphore(r, cx, cy, fill, stroke){
	if (arguments.length == 6)
//	if (arguments.length == 5)
	{
		this.animateTag = null; 
		this.init(width, height, x, y, fill, stroke);
		//this.init(r, cx, cy, fill, stroke);
	}
	else
		alert("Semaphore constructor call error");
}

//Semaphore.prototype.init = function(r, cx, cy, fill, stroke)
Semaphore.prototype.init = function(width, height, x, y, fill, stroke)
{
//	Semaphore.superclass.init.call(this, "cx", "cy", cx, cy);
	
//	this.svgNode = document.createElementNS(svgNS,"circle");
//	this.svgNode.setAttribute("r", r);	
	Semaphore.superclass.init.call(this, "x", "y", x, y);
	
	this.svgNode = document.createElementNS(svgNS,"rect");
	this.svgNode.setAttributeNS(null,"width", width);	
	this.svgNode.setAttributeNS(null,"height", height);	
	this.svgNode.setAttribute(this.attributeX, x);	
	this.svgNode.setAttributeNS(null,this.attributeY, y);
	this.svgNode.setAttributeNS(null,"fill", fill);
	this.svgNode.setAttributeNS(null,"stroke", stroke);
//	this.svgNode.setAttributeNS(null,"stroke-width", 3);
	this.animateTag = document.createElementNS(svgNS,"animate");
	this.animateTag.setAttributeNS(null,"attributeName", "fill");	
	this.animateTag.setAttributeNS(null,"from", fill);	
	this.animateTag.setAttributeNS(null,"to", "white");
	this.animateTag.setAttributeNS(null,"dur", "400ms");
	this.animateTag.setAttributeNS(null,"repeatCount", "indefinite");
	this.animateTag.addEventListener("repeat", this.onRepeat, false);
	this.svgNode.appendChild(this.animateTag);
	
}

Semaphore.prototype.setDimension = function(newDimension){
	this.svgNode.setAttributeNS(null,"r", newDimension);
	this.svgNode.setAttributeNS(null,this.attributeX, 5*newDimension);	
	this.svgNode.setAttributeNS(null,this.attributeY, newDimension*16/3);	
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
		
		this.animateTag.setAttributeNS(null,"dur", val);
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
	evt.getTarget().setAttributeNS(null,"from", from);
	evt.getTarget().setAttributeNS(null,"to", to);	
}
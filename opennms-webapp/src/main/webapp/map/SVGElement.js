// == SVGElement.js -- Copyright (C) Michele Masullo ========================
// class Element, ancestor of element
function SVGElement()
{
	this.svgNode = null;
}

SVGElement.prototype.getSvgNode = function()
{
	return this.svgNode;	
}

// class MoveableSVGElement ancestor of a moveable graphical element
MoveableSVGElement.prototype = new SVGElement;
MoveableSVGElement.superclass = SVGElement.prototype;

function MoveableSVGElement(attributeX, attributeY, x, y)
{
	if (arguments.length == 4)
		this.init(attributeX, attributeY, x, y);
//	else
//		return null;
//		alert("MoveableSVGElement constructor call error");
}

MoveableSVGElement.prototype.init = function(attributeX, attributeY, x, y)
{
	this.x = x;
	this.y = y;
	this.attributeX = attributeX;
	this.attributeY = attributeY;
}

MoveableSVGElement.prototype.moveBy = function(x, y)
{
	x += this.x;
	y += this.y;
	this.move(x, y);
}

MoveableSVGElement.prototype.move = function(x, y)
{
	this.x = x;
	this.y = y;
	this.svgNode.setAttribute(this.attributeX, this.x);
	this.svgNode.setAttribute(this.attributeY, this.y);
}
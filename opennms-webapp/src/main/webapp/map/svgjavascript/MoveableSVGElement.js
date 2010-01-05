function SVGElement()
{
	this.svgNode = null;
}

SVGElement.prototype.getSvgNode = function()
{
	return this.svgNode;	
}

MoveableSVGElement.prototype = new SVGElement;
MoveableSVGElement.superclass = SVGElement.prototype;

function MoveableSVGElement(attributeX, attributeY, x, y)
{
	if (arguments.length == 4)
		this.init(attributeX, attributeY, x, y);
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
	this.svgNode.setAttributeNS(null,this.attributeX, this.x);
	this.svgNode.setAttributeNS(null,this.attributeY, this.y);
}
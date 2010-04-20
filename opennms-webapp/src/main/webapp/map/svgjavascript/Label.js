// == Label.js --

Label.prototype = new MoveableSVGElement;
Label.superclass = MoveableSVGElement.prototype;

function Label(text, x, y, fontsize, anchor)
{
	if (arguments.length == 5)
	{
	   this.init(text, x, y, fontsize, anchor);
	} 
	else
		alert("Label constructor call error");
}

Label.prototype.init = function(text, x, y, fontsize, anchor)
{
	this.text = text;
	
	Label.superclass.init.call(this, "x", "y", x, y);
	
	this.svgNode = document.createElementNS(svgNS,"text");	
	this.svgNode.setAttributeNS(null,this.attributeX, x);	
	this.svgNode.setAttributeNS(null,this.attributeY, y);
	this.svgNode.setAttributeNS(null,"text-anchor", anchor);
	this.svgNode.setAttributeNS(null,"font-size", fontsize);
	this.svgNode.appendChild(document.createTextNode(this.text));

}



Label.prototype.setFontSize = function(x,y,newFontSize){
	this.svgNode.setAttributeNS(null,"font-size", newFontSize);
	this.svgNode.setAttributeNS(null,this.attributeX, x);	
	this.svgNode.setAttributeNS(null,this.attributeY, y);	
}


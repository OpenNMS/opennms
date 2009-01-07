// == Node.js --

function Node(id, label, status, icon)
{
	   	this.init(id, label);
}

Node.prototype.init = function(id, label)
{

	this.id = id;
	this.label = label;
}

Node.prototype.getInfo = function(){
	
	var text = document.createElementNS(svgNS,"text");
	text.setAttributeNS(null, "x","3");
	text.setAttributeNS(null, "y","20");
	text.setAttributeNS(null, "id","DownInfoText");
	text.setAttributeNS(null, "font-size",titleFontSize);
	text.appendChild(document.createTextNode("Selected Node info"));
	
	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","30");
	tspan.setAttributeNS(null, "font-size",textFontSize);
	var tspanContent = document.createTextNode("Label: " + this.label);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);	
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","12");
	tspan.setAttributeNS(null, "font-size",textFontSize);
	tspanContent = document.createTextNode("Id: " + this.id);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);
	
	return text;
}




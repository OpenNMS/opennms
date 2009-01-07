// == ElemMap.js

/*
This class is designed to be used for the list of maps defined before the moment
of the load of the Maps page. 
*/


function ElemMap(id, name, owner)
{
	this.init(id, name,  owner);
}


ElemMap.prototype.init = function(id, name,  owner)
{
	this.id=id;
	this.name=name;
	this.owner=owner;
}

ElemMap.prototype.getInfo = function()
{
	// get info 	
	var text = document.createElementNS(svgNS,"text");
	text.setAttributeNS(null, "x","3");
	text.setAttributeNS(null, "y","20");
	text.setAttributeNS(null, "id","DownInfoText");
	text.setAttributeNS(null, "font-size",titleFontSize);
	text.appendChild(document.createTextNode("Selected Map info"));
	
	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","20");
	tspan.setAttributeNS(null, "font-size",textFontSize);
	var tspanContent = document.createTextNode("Name: " + this.name);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "font-size",textFontSize);
	tspanContent = document.createTextNode("Owner: " + this.owner);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);
	return text;
}

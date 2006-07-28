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
	var str = "<text id=\"DownInfoText\" x=\"3\" y=\"20\">Selected Map info" +
		"<tspan x=\"3\" dy=\"30\" font-size=\"9\">Name: " + this.name + "</tspan>" +
		"<tspan x=\"3\" dy=\"15\" font-size=\"9\">Owner: " + this.owner + "</tspan>"+
		"</text>";
	return parseXML(str, svgDocument);
}

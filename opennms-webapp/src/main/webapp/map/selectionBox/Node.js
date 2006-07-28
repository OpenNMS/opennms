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
	
	var str = "<text id=\"DownInfoText\" x=\"3\" y=\"0\">Selected Node info" +
		"<tspan x=\"3\" dy=\"30\" font-size=\"9\">Label: " + this.label + "</tspan>" +
		"<tspan x=\"3\" dy=\"15\" font-size=\"9\">Id: " + this.id + "</tspan>";		
	str+="</text>";
	return parseXML(str, svgDocument);
}




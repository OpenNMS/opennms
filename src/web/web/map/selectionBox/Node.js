// == Node.js -- Copyright (C) Maurizio Migliore ========================



// interfaces must be an array strings representing ipaddress of the interfaces of the node.
// links must be an array of ids representing the linked nodes.
function Node(id, label,  availVal, status,  icon, interfaces, links)
{
	   	this.init(id, label,  availVal, status, icon, interfaces, links);
}


Node.prototype.init = function(id, label, availVal, status, icon, interfaces, links)
{

	this.id = id;
	this.label = label;
	this.interfaces = interfaces;
	this.links = links;
	this.icon = icon;
	this.avail = availVal;
	this.status=status;
}

Node.prototype.getInfo = function(){
	
	var statusColor=getStatusColor(this.status);
	var status = "Active";
	if(this.status=="D"){
		status="Deleted";
		}
	if(this.status=="O"){
		status="Outaged";
		}
	var line1 = "";

	for(var i=0; i<this.interfaces.length;i++){
			line1+=this.interfaces[i];
			if(i!=this.interfaces.length-1){
				line1+=","
			}
		}
	
	var availColor = getAvailColor(this.avail);
	// get info 	
	var str = "<text id=\"DownInfoText\" x=\"3\" y=\"0\">Selected Node info" +
		"<tspan x=\"3\" dy=\"30\" font-size=\"9\">Label: " + this.label + "</tspan>" +
		"<tspan x=\"3\" dy=\"15\" font-size=\"9\">Id: " + this.id + "</tspan>"+		
		"<tspan x=\"3\" dy=\"15\" font-size=\"9\" fill=\""+statusColor+"\">Status: " + status + "</tspan>"+
		"<tspan x=\"3\" dy=\"15\" font-size=\"9\" fill=\""+availColor+"\">Availability: " + this.avail + "%</tspan>";
	   str+="<tspan x=\"3\" dy=\"15\" font-size=\"9\" >Interfaces: "+line1+"</tspan>";
	/*
	if(this.interfaces.length>0){
		str+="<tspan x=\"3\" dy=\"8\" font-size=\"9\" >";
		for(var i=0; i<this.interfaces.length;i++){
			     str+=this.interfaces[i];
			     if(i<length) 
			     	str+=", ";
			}
	 	str+="</tspan>";
	}*/	
		
	
	/*var count=0;
	for(elem in this.links ){
		if(count==0){
			str+="<tspan x=\"10\" dy=\"15\">Is linked to:</tspan>";
		}
		
		str+="<tspan x=\"20\" dy=\"10\">- " + this.links[elem].label + "</tspan>"
		count++;
	}
	*/	
	str+="</text>";
	return parseXML(str, svgDocument);
}




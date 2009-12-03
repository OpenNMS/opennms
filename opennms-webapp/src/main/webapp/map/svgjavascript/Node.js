// == Node.js --

function Node(id, label, ipaddress, status, icon)
{
	   	this.init(id, label, ipaddress);
}

Node.prototype.init = function(id, label,ipaddress)
{

	this.id = id;
	this.label = label;
	this.ipaddress = ipaddress;
}

Node.prototype.getLabel = function(){
	return this.label;
}

Node.prototype.getIpAddr = function(){
	return this.ipaddress;
}

Node.prototype.getId = function(){
	return this.id;
}



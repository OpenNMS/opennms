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

Node.prototype.getLabel = function(){
	return this.label;
}

Node.prototype.getId = function(){
	return this.id;
}



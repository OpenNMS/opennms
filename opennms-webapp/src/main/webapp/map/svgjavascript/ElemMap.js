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

ElemMap.prototype.getId = function() {
   return this.id;
}

ElemMap.prototype.getName = function() {
   return this.name;
}

ElemMap.prototype.getOwner = function() {
	return this.owner
}


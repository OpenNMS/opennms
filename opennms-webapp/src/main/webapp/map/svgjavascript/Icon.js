// == Icon.js

/*
This class is designed to be used for representing an Icon
in the MapElement. 
*/


function Icon(name, url)
{
	this.init(name,  url);
}


Icon.prototype.init = function(name,  url)
{
	this.name=name;
	this.url=url;
}

Icon.prototype.getName = function() {
   return this.name;
}

Icon.prototype.getUrl = function() {
	return this.url
}


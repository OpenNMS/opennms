MapElement.prototype = new MoveableSVGElement;
MapElement.prototype.constructor = MoveableSVGElement;
MapElement.superclass = MoveableSVGElement.prototype;

/*
id=the id of the element 
iconUrl=the Url of the icon 
labelText= the label of the element
semaphoreColor=the color of the semaphore (rgb or 'white','yellow' ecc.)
x=x position
y=y position
status=the element status at the moment of the creation
avail=availability of the element at the moment of the creation
*/

function MapElement(id,icon, labelText, semaphoreColor, semaphoreFlash, x, y, dimension, status, avail, severity)
{
	if ( arguments.length == 11 )
	{
	   	this.init(id,icon, labelText, semaphoreColor, semaphoreFlash, x, y, dimension, status, avail, severity);
	}
	else
		alert("MapElement constructor call error");
}

MapElement.prototype.init = function(id,icon, labelText, semaphoreColor, semaphoreFlash, x, y, dimension, status, avail, severity)
{

	MapElement.superclass.init.call(this, "x", "y", x, y);
	this.id = new String(id);
	this.width = dimension;
	this.height = dimension*4/3;
	this.icon = icon;
	this.avail = avail;
	this.status = status;
	this.severity = severity;
        this.dimension = dimension;	
	//mantains the number of links on this elements
	this.numOfLinks=0;
	// renderize element
	this.svgNode = document.createElementNS(svgNS,"g");
	this.svgNode.setAttributeNS(null,"id", this.id);

	// renderize icon
	this.image = document.createElementNS(svgNS,"image");
	this.image.setAttributeNS(null,"width", this.width);
	this.image.setAttributeNS(null,"height", this.height);
	this.image.setAttributeNS(null,this.attributeX, dimension/20);
	this.image.setAttributeNS(null,this.attributeY, 0);
	this.image.setAttributeNS(null,"preserveAspectRatio", "xMinYMin");
	this.image.setAttributeNS(null,"cursor", "pointer");
	this.image.setAttributeNS(xlinkNS, "xlink:href",this.icon.getUrl());
	this.image.addEventListener("click", this.onClick, false);
	this.image.addEventListener("mousedown", this.onMouseDown, false);
	this.image.addEventListener("mousemove", this.onMouseMove, false);
	this.image.addEventListener("mouseup", this.onMouseUp, false);
	this.image.addEventListener("mouseover", this.onMouseOver, false);
	this.image.addEventListener("mouseout", this.onMouseOut, false);
	
	var labelx = Math.round(this.width/3);
	var labely = this.height + dimension*0.7;
	var labelSize = dimension/2;
	var labelAnchor = "middle";

    this.label = new Label(labelText, labelx, labely, labelSize, labelAnchor);
	
	//renderize semaphore
	//var r=dimension/4;
    //var cx=this.width+r;
	//var cy=this.height-r;

//	this.semaphore = new Semaphore(r, cx, cy, semaphoreColor, "black");
	this.semaphore = new Semaphore(this.width+dimension/10, this.height, 0, 0, semaphoreColor, "black");
	this.semaphore.flash(semaphoreFlash);

	this.svgNode.appendChild(this.semaphore.getSvgNode());
	this.svgNode.appendChild(this.label.getSvgNode());
	this.svgNode.appendChild(this.image);
	
	this.svgNode.setAttributeNS(null,"transform", "translate(" + this.x + "," + this.y + ")");
	this.svgNode.setAttributeNS(null,"opacity", "0.9");
}


MapElement.prototype.setDimension = function(newDimension) {
	this.width = newDimension;
	this.height = newDimension*4/3;
	this.image.setAttributeNS(null,"width", this.width);
	this.image.setAttributeNS(null,"height", this.height);	
	this.label.setFontSize(newDimension/2);
	this.semaphore.setDimension(newDimension/4);
}

MapElement.prototype.getMapId = function() {
	var mapId = this.id;
	return mapId.substr(0,this.id.indexOf(MAP_TYPE));
}

MapElement.prototype.getNodeId = function() {
	var nodeId = this.id;
	return nodeId.substr(0,this.id.indexOf(NODE_TYPE));
}

MapElement.prototype.isMap = function(){
	if(this.id.indexOf(MAP_TYPE)== -1) return false;
	return true;
}

MapElement.prototype.isNode = function(){
	if(this.id.indexOf(NODE_TYPE) == -1) return false;
	return true;
}

MapElement.prototype.getType = function(){
	if(this.id.indexOf(NODE_TYPE) != -1){
		return NODE_TYPE;
	} 
	if(this.id.indexOf(MAP_TYPE) != -1){
		return MAP_TYPE;
	} 
	
}

MapElement.prototype.getX = function(){
	return parseInt(this.x,10);
}

MapElement.prototype.getY = function(){
	return parseInt(this.y,10);
}

MapElement.prototype.setSemaphoreColor = function(semaphoreColor){
	this.semaphore.svgNode.setAttributeNS(null,"fill", semaphoreColor);
}

MapElement.prototype.setSemaphoreFlash = function(flag)
{
	this.semaphore.flash(flag);
}

MapElement.prototype.getCPoint = function()
{
	return new Point2D(this.x + (this.width / 2), this.y + (this.height / 2));
}

MapElement.prototype.move = function(x, y)
{	
	this.x = x;
	this.y = y;
	this.svgNode.setAttributeNS(null,"transform", "translate(" + this.x + "," + this.y + ")");
}

MapElement.prototype.getIcon = function()
{
	return this.icon.name;
}

MapElement.prototype.setIcon = function(icon)
{
	this.icon = icon;
	this.image.setAttributeNS(xlinkNS, "xlink:href", this.icon.getUrl());
	
}
MapElement.prototype.getLabel = function()
{
	return this.label.text;
}

MapElement.prototype.getSeverity = function()
{
	return this.severity;
}

MapElement.prototype.getStatus = function()
{
	return this.status;
}

MapElement.prototype.getAvail = function()
{
	return this.avail;
}

MapElement.prototype.getNumberOfLink = function()
{
	return this.numOfLinks;
}

MapElement.prototype.onMouseDown = onMouseDownOnMapElement;
MapElement.prototype.onMouseMove = onMouseMove;
MapElement.prototype.onMouseUp   = onMouseUp;
MapElement.prototype.onClick     = onClickMapElement;
MapElement.prototype.onMouseOver = onMouseOverMapElement;
MapElement.prototype.onMouseOut  = onMouseOutMapElement;

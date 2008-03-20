// == MapElement.js
// before include this file must be include:
// 1 - Application.js
// 2 - Debug.js
// 3 - SVGElement.js
// 4 - Label.js
// 5 - Semaphore.js
// class MapElement

MapElement.prototype = new MoveableSVGElement;
MapElement.prototype.constructor = MoveableSVGElement;
MapElement.superclass = MoveableSVGElement.prototype;

/*
id=the id of the element 
iconName=the name of the icon 
labelText= the label of the element
semaphoreColor=the color of the semaphore (rgb or 'white','yellow' ecc.)
x=x position
y=y position
status=the element status at the moment of the creation
avail=availability of the element at the moment of the creation
*/

function MapElement(id,iconName, labelText, semaphoreColor, semaphoreFlash, x, y, dimension, status, avail, severity)
{
	if ( arguments.length == 11 )
	{
	   //Title.scale  = doc.getDocumentElement().getCurrentScale();     // scaling modified by zooming ..
	   //Title.offset = doc.getDocumentElement().getCurrentTranslate(); // offset modified by zooming ..
	   	this.init(id,iconName, labelText, semaphoreColor, semaphoreFlash, x, y, dimension, status, avail, severity);
	   //doc.getDocumentElement().addEventListener("zoom", Title.Zoom, false);
	}
	else
		alert("MapElement constructor call error");
}

MapElement.prototype.init = function(id,iconName, labelText, semaphoreColor, semaphoreFlash, x, y, dimension, status, avail, severity)
{
	MapElement.superclass.init.call(this, "x", "y", x, y);
	
	this.id = new String(id);
	this.width = dimension;
	this.height = dimension*4/3;
	this.icon = iconName;
	this.avail = avail;
	this.status = status;
	this.severity = severity;
	
	//mantains the number of links on this elements
	this.numOfLinks=0;

	// renderize element
	this.svgNode = mapSvgDocument.createElement("g");
	this.svgNode.setAttribute("id", this.id);
	this.svgNode.setAttribute("transform", "translate(" + this.x + "," + this.y + ")");
	
	// renderize icon
	this.image = mapSvgDocument.createElement("image");
	this.image.setAttribute("width", this.width);
	this.image.setAttribute("height", this.height);
	this.image.setAttribute(this.attributeX, 0);
	this.image.setAttribute(this.attributeY, 0);
	this.image.setAttribute("style", "cursor:pointer");
	this.image.setAttributeNS(application.svgnsXLink, "xlink:href", MEIconsSortAss[this.icon]);

	this.image.addEventListener("click", this.onClick, false);
	this.image.addEventListener("mousedown", this.onMouseDown, false);
	this.image.addEventListener("mousemove", this.onMouseMove, false);
	this.image.addEventListener("mouseup", this.onMouseUp, false);

	this.svgNode.appendChild(this.image);

	// renderize label
	if (this.isNode()) {
		this.label = new Label(labelText, Math.round(this.width/3), this.height + dimension*0.7, dimension/2, "element/node.jsp?node="+this.getNodeId());
	} else {
		this.label = new Label(labelText, Math.round(this.width/3), this.height + dimension*0.7, dimension/2);
	}
	this.svgNode.appendChild(this.label.getSvgNode());
	
	//renderize semaphore

	this.semaphore = new Semaphore(dimension/4, this.width + dimension/4, this.height, semaphoreColor, "black");
	this.semaphore.flash(semaphoreFlash);
	this.svgNode.appendChild(this.semaphore.getSvgNode());
}


MapElement.prototype.setDimension = function(newDimension){
	this.width = newDimension;
	this.height = newDimension*4/3;
	this.image.setAttribute("width", this.width);
	this.image.setAttribute("height", this.height);	
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

MapElement.prototype.setSemaphoreColor = function(semaphoreColor){
	this.semaphore.svgNode.setAttribute("fill", semaphoreColor);
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
{	//alert(mapSvgDocument.firstChild().currentScale);
	this.x = x;//mapSvgDocument.firstChild().currentScale;
	this.y = y;//mapSvgDocument.firstChild().currentScale;
	this.svgNode.setAttribute("transform", "translate(" + this.x + "," + this.y + ")");
}

MapElement.prototype.getLabel = function()
{
return this.label.text;
}
MapElement.prototype.getInfo = function()
{

    var severityColor=getSeverityColor(this.severity)
	var statusColor=getStatusColor(this.status);

    var severityLabel = SEVERITIES_LABEL[this.severity];
	var status = STATUSES_TEXT[this.status];
	
	var availColor = getAvailColor(this.avail);

	var avail;

	if(this.avail<0){
		avail="Unknown";
	}else{	
		avail = (""+this.avail).substring(0,6)+"%";
	}

	if(availColor=='white')
		availColor='black';
	if(statusColor=='white')
		statusColor='black';		

	var str= "<text id=\"TopInfoText\" x=\"3\" y=\"20\">Map Element info" +
			"<tspan x=\"3\" dy=\"20\" font-size=\"9\" id=\"TopInfoLabelText\">" + this.label.text + "</tspan>";

	if(this.isMap()){
		str+="<tspan x=\"3\" dy=\"15\" font-size=\"9\">Id: "+ this.getMapId() + "  (Map)</tspan>";		
	}

	if(this.isNode()){
		str+="<tspan x=\"3\" dy=\"15\" font-size=\"9\">Id: " + this.getNodeId() +"  (Node)</tspan>";		
	}

		
	str+="<tspan x=\"3\" dy=\"15\" font-size=\"9\" fill=\""+statusColor+"\">Status: " + status + "</tspan>"+
		"<tspan x=\"3\" dy=\"15\" font-size=\"9\" fill=\""+availColor+"\">Availability: " + avail + "</tspan>"+
		"<tspan x=\"3\" dy=\"15\" font-size=\"9\" fill=\""+severityColor+"\">Severity: " + severityLabel + "</tspan>";

	str+="</text>";
	// get info 	
	return str;
}

MapElement.prototype.getNumberOfLink = function()
{
return this.numOfLinks;
}

MapElement.prototype.onMouseDown = onMouseDownOnMapElement;
MapElement.prototype.onMouseMove = onMouseMove;
MapElement.prototype.onMouseUp   = onMouseUp;
MapElement.prototype.onClick     = onClickMapElement;

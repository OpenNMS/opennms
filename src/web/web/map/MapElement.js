// == MapElement.js -- Copyright (C) Michele Masullo ========================
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
nodes=array containing the nodes of the element (IF AND ONLY IF the element to add is a MAP)
*/
function MapElement(id, iconName, labelText, semaphoreColor, x, y, status, avail, nodes)
{
	if ( arguments.length >= 8 )
	{
	   //Title.scale  = doc.getDocumentElement().getCurrentScale();     // scaling modified by zooming ..
	   //Title.offset = doc.getDocumentElement().getCurrentTranslate(); // offset modified by zooming ..
	   	this.init(id, iconName, labelText, semaphoreColor, x, y, status, avail, nodes);
	   //doc.getDocumentElement().addEventListener("zoom", Title.Zoom, false);
	}
//	else
//		alert("MapElement constructor call error");
}

//	<g id ="*">
//		<image xlink:href="*" width="*" height="*" x="*" y="*"/>
//		<text x="*" y="*" text-anchor="*">*****</text>
// 		<circle r="*" cx="*" cy="*" stroke="*" fill="*">
// 			<animate attributeName="fill" from="black" to="white" dur="400ms" repeatCount="indefinite" onrepeat="onRepeat		(evt)"/>
//		</circle>
//	</g>
MapElement.prototype.init = function(id, iconName, labelText, semaphoreColor, x, y, status, avail, nodes)
{
	MapElement.superclass.init.call(this, "x", "y", x, y);
	
	this.id = id;
	this.width = 20;
	this.height = 25;
	this.icon = iconName;
	this.avail = avail;
	this.status = status;
	this.links = new Array();
	//alert(nodes);
	if(nodes!=null && nodes!="undefined")
	{
		this.nodes=nodes;
	}

	// renderize element
	this.svgNode = svgDocument.createElement("g");
	this.svgNode.setAttribute("id", this.id);
	this.svgNode.setAttribute("transform", "translate(" + this.x + "," + this.y + ")");
	
	// renderize icon
	this.image = svgDocument.createElement("image");
	this.image.setAttribute("width", this.width);
	this.image.setAttribute("height", this.height);
	this.image.setAttribute(this.attributeX, 0);
	this.image.setAttribute(this.attributeY, 0);
	this.image.setAttributeNS(application.svgnsXLink, "xlink:href", MEIconsSortAss[this.icon]);
	this.svgNode.appendChild(this.image);

	// renderize label
	this.label = new Label(labelText, Math.round(this.width/2), this.height + 20, "/opennms/element/node.jsp?node="+this.id);
	this.svgNode.appendChild(this.label.getSvgNode());
	
	//renderize semaphore

	this.semaphore = new Semaphore(5, this.width + 5, this.height, semaphoreColor, "black");
	this.semaphore.flash(true);
	this.svgNode.appendChild(this.semaphore.getSvgNode());
	this.image.addEventListener("click", this.onClick, false);
	this.image.addEventListener("mousedown", this.onMouseDown, false);
	this.image.addEventListener("mousemove", this.onMouseMove, false);
	this.image.addEventListener("mouseup", this.onMouseUp, false);
}

MapElement.prototype.isMap = function(){
	if(this.id<=0) return true;
	return false;
}

MapElement.prototype.isNode = function(){
	if(this.id>0) return true;
	return false;
}

MapElement.prototype.setSemaphoreColor = function(semaphoreColor){
	this.semaphore.svgNode.setAttribute("fill", semaphoreColor);
}

MapElement.prototype.onMouseDown = onMouseDownOnMapElement;
MapElement.prototype.onMouseMove = onMouseMove;
MapElement.prototype.onMouseUp = onMouseUp;

//if double-click on an element (map) open the map 
MapElement.prototype.onClick = function(evt)
{
	if (evt.getDetail() == 2)
	{var id = evt.getTarget().parentNode.getAttribute("id");
	if(id<=0){
			if(savedMapString!=getMapString() && currentMapId!=MAP_NOT_OPENED) {
			 if(confirm('Map \''+currentMapName+'\' not saved, do you want to proceed however?')==false)
			 	return;
			}
			map.clear();
			hideMapInfo();
			clearTopInfo();
			clearDownInfo();
			loading++;
			assertLoading();
			disableMenu();
			postURL ( "OpenMap?MapId="+id, null, openDownloadedMap, "text/xml", null );			
		}
	}
}

MapElement.prototype.addLink = function(link)
{
	this.links.push(link);
}

MapElement.prototype.redrawLink = function()
{
	for(var i in this.links)
	{
		this.links[i].update();
	}	
}

MapElement.prototype.move = function(x, y)
{
	this.x = x;
	this.y = y;
	this.svgNode.setAttribute("transform", "translate(" + this.x + "," + this.y + ")");
	this.redrawLink();
}

MapElement.prototype.flash = function(flag)
{
	this.semaphore.flash(flag);
}

MapElement.prototype.getCPoint = function()
{
	return new Point2D(this.x + (this.width / 2), this.y + (this.height / 2));
}

MapElement.prototype.getInfo = function()
{
	var statusColor=getStatusColor(this.status);
	var status = "Active";
	if(this.status=="D"){
		status="Deleted";
		}
	if(this.status=="O"){
		status="Outaged";
		}
	if(this.status=="U"){
		status="Unknown";
		}		
	
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
	var str= "<text id=\"TopInfoText\" x=\"3\" y=\"20\">Element info" +
		"<tspan x=\"3\" dy=\"20\" font-size=\"9\">Label: " + this.label.text + "</tspan>";
		if(this.id>0) str+="<tspan x=\"3\" dy=\"15\" font-size=\"9\">Id: " + this.id + "</tspan>" ;
		str+="<tspan x=\"3\" dy=\"15\" font-size=\"9\" fill=\""+statusColor+"\">Status: " + status + "</tspan>"+
		"<tspan x=\"3\" dy=\"15\" font-size=\"9\" fill=\""+availColor+"\">Availability: " + avail + "</tspan>";
	//if this is a map, add number of element to info
	if(this.id<=0){
		var countMaps= 0;
		for(nn in this.nodes){
			if (this.nodes[nn]<=0)
				countMaps++;
		}
		str+="<tspan x=\"3\" dy=\"15\" font-size=\"9\">Map with " + this.nodes.length + " elements ("+countMaps+" map/s).</tspan>";		
	}
	str+="</text>";
	// get info 	
	return parseXML(str	,svgDocument);
}

MapNode.prototype = new MapElement;
MapNode.prototype.constructor = MapElement;
MapNode.superclass = MapElement.prototype;

function MapNode(id, iconName, labelText, semaphoreColor, x, y)
{
	if ( arguments.length == 6 )
		this.init(id, iconName, labelText, semaphoreColor, x, y);
	else
		alert("MapElement constructor call error");
}

MapNode.prototype.init = function(id, iconName, labelText, semaphoreColor, x, y)
{
	MapNode.superclass.init.call(this, id, iconName, labelText, semaphoreColor, x, y);
}

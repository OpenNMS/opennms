//set the context menu for a map element
//the parameter elemType says if the element is a MAP or is a NODE
function ContextMenuSimulator(parentNode,id,menulabel,x,y,width,height,menuStyle,menuElementStyle,menuElementTextStyle,menuElementMouseStyle,delta) {
	if ( arguments.length == 12 )
	{
	   	this.init(parentNode,id,menulabel,x,y,width,height,menuStyle,menuElementStyle,menuElementTextStyle,menuElementMouseStyle,delta);
	}
	else
		alert("ContextMenuSimulator constructor error");
} 

ContextMenuSimulator.prototype.init = function(parentNode,id,menulabel,x,y,width,height,menuStyle,menuElementStyle,menuElementTextStyle,menuElementMouseStyle,delta) {

	this.parentNode = parentNode; //the id or node reference of the parent group where the menu can be appended
	this.id = id; // the element identifier (The nodeid)
	this.menulabel = menulabel;
	this.x =x;
	this.y=y;
	this.width = width;
	this.height = height;
	this.menuStyle = menuStyle;
	this.menuElementStyle = menuElementStyle;
	this.menuElementTextStyle = menuElementTextStyle;
	this.menuElementMouseStyle = menuElementMouseStyle;
	if (!this.menuElementTextStyle["font-size"]) {
		this.menuElementTextStyle["font-size"] = 10;
	}
	this.delta=delta;
	this.contextMenuGroup = null;
	this.contextMenuMain = null;
	this.menuItems = new Array();
	
	var result = this.testParent();
	if (result) {	
		this.createContextMenuMain();
	} else {
		alert("ContextMenuSimulator parent node is not valid");
	}
}

ContextMenuSimulator.prototype.testParent = function() {
    //test if of type object
    var nodeValid = false;
    if (typeof(this.parentNode) == "object") {
    	if (this.parentNode.nodeName == "svg" || this.parentNode.nodeName == "g" || this.parentNode.nodeName == "svg:svg" || this.parentNode.nodeName == "svg:g") {
    		nodeValid = true;
    	}
    }  else if (typeof(this.parentNode) == "string") { 
    	//first test if menu group exists
    	if (document.getElementById(this.parentNode)) {
    		this.parentNode = document.getElementById(this.parentNode);
        	nodeValid = true;
   		}
   	}
   	return nodeValid;
}

ContextMenuSimulator.prototype.getMenuItems = function() {
	return this.menuItems;
}

ContextMenuSimulator.prototype.getMenuItemsLength = function() {
	return this.menuItems.length;
}

ContextMenuSimulator.prototype.createContextMenuMain = function() {
	
	this.contextMenuGroup = document.createElementNS(svgNS,"g");
	this.contextMenuGroup.setAttributeNS(null,"display","inline");
	this.contextMenuGroup.setAttributeNS(null,"id",this.id+"CMGroup");
	
	this.contextMenuMain = document.createElementNS(svgNS,"rect");
	this.contextMenuMain.setAttributeNS(null,"id", this.id+"CMRect");
	this.contextMenuMain.setAttributeNS(null,"x", this.x);
	this.contextMenuMain.setAttributeNS(null,"y", this.y);
	this.contextMenuMain.setAttributeNS(null,"width", this.width);
	this.contextMenuMain.setAttributeNS(null,"height", this.height);
	for (var attrib in this.menuStyle) {
		this.contextMenuMain.setAttributeNS(null,attrib,this.menuStyle[attrib]);
	}		
	this.contextMenuGroup.appendChild(this.contextMenuMain);
	this.parentNode.appendChild(this.contextMenuGroup);
	
}

ContextMenuSimulator.prototype.getId = function() {
	return this.id;
}

ContextMenuSimulator.prototype.addItem = function(index,itemlabel,onClickActions) {
	if ( arguments.length == 3 ) {
		var items = this.getMenuItemsLength();
		var x = this.x;
		var y = this.y+items*this.delta;
		var width = this.width;
		var height = this.delta;
		var cmItem= new ContextMenuSimulatorItem(this.id,index,itemlabel,this.menulabel,x,y,width, height,this.menuElementStyle,this.menuElementTextStyle,this.menuElementMouseStyle,onClickActions);
		this.menuItems.push(cmItem);
		this.contextMenuGroup.appendChild(cmItem.getGroup());
	} else {
			alert("Error Adding Item in ContextMenuSimulator");
	}
}


function ContextMenuSimulatorItem(id,index,itemlabel,menulabel,x,y,width, height,menuElementStyle,menuElementTextStyle,menuElementMouseStyle,onClickActions) {
	if ( arguments.length == 12 )
	{
	   	this.init(id,index,itemlabel,menulabel,x,y,width, height,menuElementStyle,menuElementTextStyle,menuElementMouseStyle,onClickActions);
	}
	else
		alert("ContextMenuSimulatorItem constructor error");
}

ContextMenuSimulatorItem.prototype.init = function(id,index,itemlabel,menulabel,x,y,width, height,menuElementStyle,menuElementTextStyle,menuElementMouseStyle,onClickActions) {

	this.id = id; // the element identifier
	this.label = itemlabel // the label of he menu voice
	this.menulabel = menulabel;
	this.index = index;
	this.x =x;
	this.y=y;
	this.width = width;
	this.height = height;
	this.menuElementStyle = menuElementStyle;
	this.menuElementTextStyle = menuElementTextStyle;
	this.menuElementMouseStyle = menuElementMouseStyle;
	if (!this.menuElementTextStyle["font-size"]) {
		this.menuElementTextStyle["font-size"] = 10;
	}
	this.onClickActions = onClickActions;
	this.contextMenuItemGroup = null;
	this.createContextMenuItemGroup();
}


ContextMenuSimulatorItem.prototype.createContextMenuItemGroup = function() {

	this.contextMenuItemGroup = document.createElementNS(svgNS,"g");
	this.contextMenuItemGroup.setAttributeNS(null,"display","inline");
	this.contextMenuItemGroup.setAttributeNS(null,"id",this.id+this.index+"CMItemGroup");

	var menuElement = document.createElementNS(svgNS,"rect");
	menuElement.setAttributeNS(null,"id", this.id+this.index);
	menuElement.setAttributeNS(null,"x", this.x);
	menuElement.setAttributeNS(null,"y", this.y);
	menuElement.setAttributeNS(null,"width", this.width);
	menuElement.setAttributeNS(null,"height", this.height);
	for (var attrib in this.menuElementStyle) {
		menuElement.setAttributeNS(null,attrib,this.menuElementStyle[attrib]);
	}		
	this.contextMenuItemGroup.appendChild(menuElement);
	
	var Text = document.createElementNS(svgNS,"text");
	Text.setAttributeNS(null,"id", this.id+this.index+"CMItemText");
	Text.setAttributeNS(null,"x", this.x+3);
	Text.setAttributeNS(null,"y", this.y+13);
	for (var attrib in this.menuElementTextStyle) {
		Text.setAttributeNS(null,attrib,this.menuElementTextStyle[attrib]);
	}	
	var contentText = document.createTextNode(this.label);
	Text.appendChild(contentText);
	this.contextMenuItemGroup.appendChild(Text);

	var MouseRect = document.createElementNS(svgNS,"rect");
	MouseRect.setAttributeNS(null,"cursor","pointer");
    MouseRect.addEventListener("click",this,false);
    
	MouseRect.setAttributeNS(null,"x" , this.x );
	MouseRect.setAttributeNS(null,"y" , this.y );
	MouseRect.setAttributeNS(null,"width" , this.width );
	MouseRect.setAttributeNS(null,"height" , this.height );
	for (var attrib in this.menuElementMouseStyle) {
		MouseRect.setAttributeNS(null,attrib,this.menuElementMouseStyle[attrib]);
	}
	this.contextMenuItemGroup.appendChild(MouseRect);

}

ContextMenuSimulatorItem.prototype.getGroup = function() {
	return this.contextMenuItemGroup;
}

ContextMenuSimulatorItem.prototype.getIndex = function() {
	return this.index;
}

ContextMenuSimulatorItem.prototype.handleEvent = function(evt) {

	if (evt.type == "mousedown") {
		return;
	}
	if (evt.type == "mouseup") {
		return;	
	}
	
	if (evt.type == "mouseover") {
		return;
	}

	if (evt.type == "mouseout") {
		return;
	}
	
	if (evt.type == "click") {
		this.onClick(evt);
	}
}

ContextMenuSimulatorItem.prototype.onClick = function(evt) {
		this.onClickActions(this.index,this.id,this.menulabel,evt);
}

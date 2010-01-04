// This is the Menu SVG Object
// Author: antonio@opennms.it
// Date: 10 jan 2009
// Copyright: OpenNMS Group
// Licence: GPL
// you must create an instance of a menu object in onLoad event of svg node root

function Menu(parentNode,id,label,x,y,width,height,menuStyle,menuTextStyle,menuMouseStyle,mouseupColor,mousedownColor,onClickActions) {
	if ( arguments.length == 13 )
	{
	   	this.init(parentNode,id,label,x,y,width,height,menuStyle,menuTextStyle,menuMouseStyle,mouseupColor,mousedownColor,onClickActions);
	}
	else
		alert("Menu constructor error");
}

Menu.prototype.init = function(parentNode,id,label,x,y,width,height,menuStyle,menuTextStyle,menuMouseStyle,upColor,downColor,onClickActions) {

	this.parentNode = parentNode; //the id or node reference of the parent group where the menu can be appended
	this.id = id; // the element identifier
	this.x =x;
	this.y=y;
	this.width = width;
	this.height = height;
	this.label = label;
	this.menuStyle = menuStyle;
	this.menuTextStyle = menuTextStyle;
	if (!this.menuTextStyle["font-size"]) {
		this.menuTextStyle["font-size"] = 12;
	}
	this.menuMouseStyle = menuMouseStyle;
	this.upColor = upColor;
	this.downColor = downColor;
	this.onClickActions = onClickActions;
	this.enable = false;
	this.menuGroup = null;
	this.rootMenuGroup = null;
	this.rootMenu = null;
	this.openMenuFlag = false;
	
	var result = this.testParent();
	if (result) {	
		this.createMenuRoot();
		this.createMenuGroup();
	} else {
		alert("Menu parent node is not valid");
	}
}

Menu.prototype.testParent = function() {
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

Menu.prototype.createMenuRoot = function() {
	
	this.rootMenuGroup = document.createElementNS(svgNS,"g");
	this.rootMenuGroup.setAttributeNS(null,"display","none");
	this.rootMenuGroup.setAttributeNS(null,"id",this.id+"Group");
	
	this.rootMenu = document.createElementNS(svgNS,"rect");
	this.rootMenu.setAttributeNS(null,"id", this.id);
	this.rootMenu.setAttributeNS(null,"x", this.x);
	this.rootMenu.setAttributeNS(null,"y", this.y);
	this.rootMenu.setAttributeNS(null,"width", this.width);
	this.rootMenu.setAttributeNS(null,"height", this.height);
	for (var attrib in this.menuStyle) {
		this.rootMenu.setAttributeNS(null,attrib,this.menuStyle[attrib]);
	}		
	this.rootMenuGroup.appendChild(this.rootMenu);
	
	var Text = document.createElementNS(svgNS,"text");
	Text.setAttributeNS(null,"id", this.id+"Text");
	Text.setAttributeNS(null,"x", this.x+3);
	Text.setAttributeNS(null,"y", this.y+13);
	for (var attrib in this.menuTextStyle) {
		Text.setAttributeNS(null,attrib,this.menuTextStyle[attrib]);
	}	
	var contentText = document.createTextNode(this.label);
	Text.appendChild(contentText);
	this.rootMenuGroup.appendChild(Text);

	var MouseRect = document.createElementNS(svgNS,"rect");
	MouseRect.setAttributeNS(null,"cursor","pointer");
    MouseRect.addEventListener("mouseover",this,false);
    MouseRect.addEventListener("mouseout",this,false);   
    MouseRect.addEventListener("click",this,false);
    
	MouseRect.setAttributeNS(null,"x" , this.x );
	MouseRect.setAttributeNS(null,"y" , this.y );
	MouseRect.setAttributeNS(null,"width" , this.width );
	MouseRect.setAttributeNS(null,"height" , this.height );
	for (var attrib in this.menuMouseStyle) {
		MouseRect.setAttributeNS(null,attrib,this.menuMouseStyle[attrib]);
	}
	this.rootMenuGroup.appendChild(MouseRect);

	this.parentNode.appendChild(this.rootMenuGroup);

}
Menu.prototype.getId = function() {
	return this.id;
}

Menu.prototype.createMenuGroup = function() {
		this.group = document.createElementNS(svgNS,"g");
		this.group.setAttributeNS(null,"display","none");
		this.group.setAttributeNS(null,"id",this.id+"Choices");	
		this.parentNode.appendChild(this.group);			
}

Menu.prototype.handleEvent = function(evt) {

	if (evt.type == "mousedown") {
		return;
	}
	if (evt.type == "mouseup") {
		return;	
	}
	
	if (evt.type == "mouseover") {
		this.rootMenu.setAttributeNS(null,"fill",this.downColor);
	}

	if (evt.type == "mouseout") {
		this.rootMenu.setAttributeNS(null,"fill",this.upColor);
	}
	
	if (evt.type == "click") {
		this.onClick(evt);
	}
}

Menu.prototype.onClick = function(evt) {
	if (this.enable) {
		this.onClickActions(this.id,!this.menuOpenFlag,evt);
		this.toggle();
	}
}

Menu.prototype.toggle = function() {
	if (this.menuOpenFlag) {
		this.close();
	} else {
		this.open();
	}		
}

Menu.prototype.close = function() {
	this.group.setAttributeNS(null,'display', 'none');
	this.menuOpenFlag=false;
}

Menu.prototype.open = function() {
	this.group.setAttributeNS(null,'display', 'inline');
	this.menuOpenFlag=true;
}

Menu.prototype.deactivate = function() {
	this.enable = false;
	this.menuOpenFlag=false;
	this.rootMenuGroup.setAttributeNS(null,'display','none');
}

Menu.prototype.activate = function() {
	this.enable=true;
	this.menuOpenFlag=false;
	this.rootMenuGroup.setAttributeNS(null,'display','inline');
}

Menu.prototype.remove = function() {
	this.parentNode.removeChild(this.group);
	this.parentNode.removeChild(this.rootMenuGroup);
}

Menu.prototype.removeChilds = function() {
	    var ls = this.group.childNodes;
        while (ls.length > 0) {
          var obj = ls.item(0);
          this.group.removeChild(obj);
        }
}

Menu.prototype.addItem = function(id,label,tx,ty,width,height, itemgroups) {
	if ( arguments.length == 7 ) {
		var menuElement = 
new MenuElement(this.group,id,label,this.x+tx,this.y+ty,width,height,this.menuStyle,this.menuTextStyle,this.menuMouseStyle,this.upColor,this.downColor,null,'inline', null, itemgroups);
		this.group.appendChild(menuElement.getSvgGroup());
	} else {
		alert("Error addItem in Menu");
	}
}

Menu.prototype.addElement = function(id,label,tx,ty,width,height,onclickActions,subitemstohide) {
	
	if ( arguments.length == 8 ) {
		var menuElement = 
new MenuElement(this.group,id,label,this.x+tx,this.y+ty,width,height,this.menuStyle,this.menuTextStyle,this.menuMouseStyle,this.upColor,this.downColor,onclickActions,'inline', subitemstohide, null);
		this.group.appendChild(menuElement.getSvgGroup());	
	} else {
		alert("Error Element in Menu");
	}
}

Menu.prototype.addItemElement = function(id,label,tx,ty,width,height,onclickActions) {
	
	if ( arguments.length == 7 ) {
		var menuElement = 
new MenuElement(this.group,id,label,this.x+tx,this.y+ty,width,height,this.menuStyle,this.menuTextStyle,this.menuMouseStyle,this.upColor,this.downColor,onclickActions,'none', null,null);
		this.group.appendChild(menuElement.getSvgGroup());	
	} else {
		alert("Error Element in Menu");
	}

}

function MenuElement(parentNode,id,label,x,y,width,height,menuStyle,menuTextStyle,menuMouseStyle,mouseupColor,mousedownColor,onClickActions,display, subitemstohide, subitemstoshow) {
	if ( arguments.length == 16 ) {
	   	this.init(parentNode,id,label,x,y,width,height,menuStyle,menuTextStyle,menuMouseStyle,mouseupColor,mousedownColor,onClickActions,display, subitemstohide, subitemstoshow);
	} else { 
		alert("MenuElement constructor error");
	}
}

MenuElement.prototype.init = function(parentNode,id,label,x,y,width,height,menuStyle,menuTextStyle,menuMouseStyle,upColor,downColor,onClickActions,display, subitemstohide, subitemstoshow) {

	this.parentNode = parentNode; //the id or node reference of the parent group where the menu can be appended
	this.id = id; // the element identifier
	this.x =x;
	this.y=y;
	this.width = width;
	this.height = height;
	this.label = label;
	this.menuStyle = menuStyle;
	this.menuTextStyle = menuTextStyle;
	if (!this.menuTextStyle["font-size"]) {
		this.menuTextStyle["font-size"] = 12;
	}
	this.menuMouseStyle = menuMouseStyle;
	this.upColor = upColor;
	this.downColor = downColor;
	this.onClickActions = onClickActions;
	this.display = display;
	this.subitemstohide = subitemstohide;
	this.subitemstoshow = subitemstoshow;
	
	this.menuElementGroup = null;
	this.menuElement = null;
	
	var result = this.testParent();
	if (result) {	
		this.createMenuElement();
	} else {
		alert("MenuElement parent node is not valid");
	}
}

MenuElement.prototype.testParent = function() {
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

MenuElement.prototype.createMenuElement = function() {
	
	this.menuElementGroup = document.createElementNS(svgNS,"g");
	this.menuElementGroup.setAttributeNS(null,"display",this.display);
	this.menuElementGroup.setAttributeNS(null,"id",this.id+"ElementGroup");
	
	this.menuElement = document.createElementNS(svgNS,"rect");
	this.menuElement.setAttributeNS(null,"id", this.id);
	this.menuElement.setAttributeNS(null,"x", this.x);
	this.menuElement.setAttributeNS(null,"y", this.y);
	this.menuElement.setAttributeNS(null,"width", this.width);
	this.menuElement.setAttributeNS(null,"height", this.height);
	for (var attrib in this.menuStyle) {
		this.menuElement.setAttributeNS(null,attrib,this.menuStyle[attrib]);
	}		
	this.menuElementGroup.appendChild(this.menuElement);
	
	var Text = document.createElementNS(svgNS,"text");
	Text.setAttributeNS(null,"id", this.id+"Text");
	Text.setAttributeNS(null,"x", this.x+3);
	Text.setAttributeNS(null,"y", this.y+13);
	for (var attrib in this.menuTextStyle) {
		Text.setAttributeNS(null,attrib,this.menuTextStyle[attrib]);
	}	
	var contentText = document.createTextNode(this.label);
	Text.appendChild(contentText);
	this.menuElementGroup.appendChild(Text);

	var MouseRect = document.createElementNS(svgNS,"rect");
	MouseRect.setAttributeNS(null,"cursor","pointer");
    MouseRect.addEventListener("mouseover",this,false);
    MouseRect.addEventListener("mouseout",this,false);   
    MouseRect.addEventListener("click",this,false);
    
	MouseRect.setAttributeNS(null,"x" , this.x );
	MouseRect.setAttributeNS(null,"y" , this.y );
	MouseRect.setAttributeNS(null,"width" , this.width );
	MouseRect.setAttributeNS(null,"height" , this.height );
	for (var attrib in this.menuMouseStyle) {
		MouseRect.setAttributeNS(null,attrib,this.menuMouseStyle[attrib]);
	}
	this.menuElementGroup.appendChild(MouseRect);
}

MenuElement.prototype.getSvgGroup = function () {
	return this.menuElementGroup;
}

MenuElement.prototype.handleEvent = function(evt) {

	if (evt.type == "mousedown") {
		return;
	}
	if (evt.type == "mouseup") {
		return;	
	}
	
	if (evt.type == "mouseover") {
		this.menuElement.setAttributeNS(null,'fill',this.downColor);
		if (this.subitemstoshow == null) return;
		for (var a in this.subitemstoshow) {
			document.getElementById(this.subitemstoshow[a]+"ElementGroup").setAttributeNS(null,'display','inline');
		}
	}

	if (evt.type == "mouseout") {
		this.menuElement.setAttributeNS(null,'fill',this.upColor);
		if (this.subitemstohide == null) return;
		for (var a in this.subitemstohide) {
			document.getElementById(this.subitemstohide[a]+"ElementGroup").setAttributeNS(null,'display','none');
		}
	}
	
	if (evt.type == "click") {
		this.onClickActions();
	}
}

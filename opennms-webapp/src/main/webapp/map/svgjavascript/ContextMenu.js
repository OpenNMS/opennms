//set the context menu for a map element
//the parameter elemType says if the element is a MAP or is a NODE
function ContextMenuSimulator(parentNode,id,menulabel,x,y,width,submenuwidth,height,rightavail,bottomavail,menuStyle,menuElementStyle,menuElementTextStyle,menuBarStyle,menuElementMouseStyle,delta) {
	if ( arguments.length == 16 )
	{
	   	this.init(parentNode,id,menulabel,x,y,width,submenuwidth,height,rightavail,bottomavail,menuStyle,menuElementStyle,menuElementTextStyle,menuBarStyle,menuElementMouseStyle,delta);
	}
	else
		alert("ContextMenuSimulator constructor error");
} 

ContextMenuSimulator.prototype.init = function(parentNode,id,menulabel,x,y,width,submenuwidth,height,rightavail,bottomavail,menuStyle,menuElementStyle,menuElementTextStyle,menuBarStyle,menuElementMouseStyle,delta) {

	this.parentNode = parentNode; //the id or node reference of the parent group where the menu can be appended
	this.id = id; // the element identifier (The nodeid)
	this.menulabel = menulabel;
	this.width = width;
	this.submenuwidth =submenuwidth;
	this.maxy=y + bottomavail-delta;

	if (this.width > rightavail ) {
		this.x = x - this.width + rightavail;
		y=y+delta;
		bottomavail=bottomavail-delta;
	} else {
		this.x=x;
	}

	this.opensubmenuright=true;
	if (this.width+this.submenuwidth > rightavail )
		this.opensubmenuright=false;

	this.height = height;


	this.opensubmenubottom=true;
	if (this.height > bottomavail-delta ) { 
		this.y = y - this.height + bottomavail-delta;
	} else {
		this.y = y;
	}

	this.menuStyle = menuStyle;
	this.menuElementStyle = menuElementStyle;
	this.menuElementTextStyle = menuElementTextStyle;
	this.menuElementMouseStyle = menuElementMouseStyle;
	this.menuBarStyle = menuBarStyle;
	if (!this.menuElementTextStyle["font-size"]) {
		this.menuElementTextStyle["font-size"] = 10;
	}
	this.delta=delta;
	this.contextMenuGroup = null;
	this.contextMenuMain = null;
	this.contextMenuLabel = null;
	this.menuItems = new Array();
	this.menuItemsAction = new Array();
	this.subMenuY= new Array();
	this.subMenuOpenBottom= new Array();
	
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
	this.contextMenuGroup.setAttributeNS(null,"visibility","visible");
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
	//Add Menu label
	this.contextMenuLabel = document.createElementNS(svgNS,"g");
	this.contextMenuLabel.setAttributeNS(null,"display","inline");
	this.contextMenuLabel.setAttributeNS(null,"id",this.id+"CMLabelGroup");

	var menuElement = document.createElementNS(svgNS,"rect");
	menuElement.setAttributeNS(null,"id", this.id+this.index);
	menuElement.setAttributeNS(null,"x", this.x);
	menuElement.setAttributeNS(null,"y", this.y);
	menuElement.setAttributeNS(null,"width", this.width);
	menuElement.setAttributeNS(null,"height", this.delta);
	for (var attrib in this.menuElementStyle) {
		menuElement.setAttributeNS(null,attrib,this.menuElementStyle[attrib]);
	}		
	this.contextMenuLabel.appendChild(menuElement);
	
	var Text = document.createElementNS(svgNS,"text");
	Text.setAttributeNS(null,"id", this.id+"CMItemText");
	Text.setAttributeNS(null,"x", this.x+this.width * 0.1);
	Text.setAttributeNS(null,"y", this.y+this.menuElementTextStyle["font-size"]);
	for (var attrib in this.menuElementTextStyle) {
		Text.setAttributeNS(null,attrib,this.menuElementTextStyle[attrib]);
	}	
	var contentText = document.createTextNode(this.menulabel);
	Text.appendChild(contentText);
	this.contextMenuLabel.appendChild(Text);

	this.contextMenuGroup.appendChild(this.contextMenuLabel);

	var bbox=Text.getBBox();
	if (bbox.width > this.width*0.8 ) {
		var length = Math.round(this.menulabel.length*this.width*0.8/bbox.width);
		var oadip=this.menulabel.substr(0,length-2)+'...';
	    var ls = Text.childNodes;
        while (ls.length > 0) {
          var obj = ls.item(0);
          Text.removeChild(obj);
        }
		contentText = document.createTextNode(oadip);
		Text.appendChild(contentText);
	}
	this.y = this.y+this.delta;	
}

ContextMenuSimulator.prototype.getId = function() {
	return this.id;
}

ContextMenuSimulator.prototype.addSubMenuItem = function(index,subindex,itemlabel,withbarbefore) {
	if ( arguments.length != 4 ) {
		alert("Error Adding SubMenuItem in ContextMenuSimulator");
		return;
	}
	var subMenuGroup = document.getElementById(this.id+"__"+index+"__00__CMSubMenuGroup");
	if (subMenuGroup) {
		var y = this.subMenuY[index];
		var x;
		
		if (this.opensubmenuright) {
			x=this.x+this.width+1;
		} else {
			x=this.x-1-this.submenuwidth;
		}
		
		if (this.subMenuOpenBottom[index]) {
			y = y+1;		
		} else {
			y = y-1;		
		}
		
		var subMenuItemGroup = document.createElementNS(svgNS,"g");
		subMenuItemGroup.setAttributeNS(null,"id",this.id+"__"+index+"__"+subindex+"__CMItemGroup");
		subMenuItemGroup.addEventListener("mouseover",this,false);
		subMenuItemGroup.addEventListener("mouseout",this,false);
	
		var menuElement = document.createElementNS(svgNS,"rect");
		menuElement.setAttributeNS(null,"id", this.id+"__"+index+"__"+subindex+"__CMRect");
		menuElement.setAttributeNS(null,"x", x);
		menuElement.setAttributeNS(null,"y", y);
		menuElement.setAttributeNS(null,"width", this.submenuwidth);
		menuElement.setAttributeNS(null,"height", this.delta);
		for (var attrib in this.menuElementStyle) {
			menuElement.setAttributeNS(null,attrib,this.menuElementStyle[attrib]);
		}		
		subMenuItemGroup.appendChild(menuElement);
		
		var Text = document.createElementNS(svgNS,"text");
		Text.setAttributeNS(null,"id" , this.id+"__"+index+"__"+subindex );
		Text.setAttributeNS(null,"cursor","pointer");
		Text.addEventListener("click",this,false);
		Text.setAttributeNS(null,"x", x+this.submenuwidth*0.02);
		Text.setAttributeNS(null,"y", y+0.6*this.delta );
		for (var attrib in this.menuElementTextStyle) {
			Text.setAttributeNS(null,attrib,this.menuElementTextStyle[attrib]);
		}	
		var contentText = document.createTextNode(itemlabel);
		Text.appendChild(contentText);
		subMenuItemGroup.appendChild(Text);
	
		subMenuGroup.appendChild(subMenuItemGroup);

		var bbox=Text.getBBox();
		if (bbox.width > this.submenuwidth*0.95 ) {
			var length = Math.round(itemlabel.length*this.submenuwidth*0.95/bbox.width);
			var oadip=itemlabel.substr(0,length-2)+'...';
		    var ls = Text.childNodes;
	        while (ls.length > 0) {
	          var obj = ls.item(0);
	          Text.removeChild(obj);
	        }
			contentText = document.createTextNode(oadip);
			Text.appendChild(contentText);
		}

		if (withbarbefore) {
			var line = 	document.createElementNS(svgNS,"line")		
			line.setAttributeNS(null,"x1", x+this.submenuwidth*0.01);
			line.setAttributeNS(null,"y1", y);
			line.setAttributeNS(null,"x2", x+this.submenuwidth*0.99);
			line.setAttributeNS(null,"y2", y);
			for (var attrib in this.menuBarStyle) {
				line.setAttributeNS(null,attrib,this.menuBarStyle[attrib]);
			}
			subMenuGroup.appendChild(line);		
		}

		if (this.subMenuOpenBottom[index]) {
			this.subMenuY[index] = y+this.delta-1;		
		} else {
			this.subMenuY[index] = y-this.delta+1;		
		}
	} else {
		alert("Error Adding SubMenuItem in ContextMenuSimulator");
	}	
}

ContextMenuSimulator.prototype.addSubMenu = function(index,itemlabel,onClickActions,withbarbefore,nitems) {
	if ( arguments.length == 5 ) {
		this.menuItemsAction[index]=onClickActions;
		this.subMenuY[index]=this.y;
		
		if (this.y + nitems*this.delta > this.maxy ) {
			this.subMenuOpenBottom[index]=false; 
		} else {
			this.subMenuOpenBottom[index]=true; 
		}
		this.menuItems[index] = document.createElementNS(svgNS,"g");
		this.menuItems[index].setAttributeNS(null,"display","inline");
		this.menuItems[index].setAttributeNS(null,"id",this.id+"__"+index+"__00__CMSubMenuItemGroup");
		this.menuItems[index].addEventListener("mouseover",this,false);
		this.menuItems[index].addEventListener("mouseout",this,false);
	
		var menuElement = document.createElementNS(svgNS,"rect");
		menuElement.setAttributeNS(null,"id", this.id+"__"+index+"__00__CMRect");
		menuElement.setAttributeNS(null,"x", this.x);
		menuElement.setAttributeNS(null,"y", this.y);
		menuElement.setAttributeNS(null,"width", this.width);
		menuElement.setAttributeNS(null,"height", this.delta);
		for (var attrib in this.menuElementStyle) {
			menuElement.setAttributeNS(null,attrib,this.menuElementStyle[attrib]);
		}		
		this.menuItems[index].appendChild(menuElement);
		
		var Text = document.createElementNS(svgNS,"text");
		Text.setAttributeNS(null,"id" , this.id+"__"+index+"__00" );
		Text.setAttributeNS(null,"cursor","pointer");
		Text.addEventListener("click",this,false);
		Text.setAttributeNS(null,"x", this.x+this.width * 0.1);
		Text.setAttributeNS(null,"y", this.y+0.6*this.delta );
		for (var attrib in this.menuElementTextStyle) {
			Text.setAttributeNS(null,attrib,this.menuElementTextStyle[attrib]);
		}	
		var contentText = document.createTextNode(itemlabel);
		Text.appendChild(contentText);
		this.menuItems[index].appendChild(Text);

		var trianglePath = document.createElementNS(svgNS,"path");
		trianglePath.setAttributeNS(null,"id" , this.id+"__"+index+"__00__CMTriangle" );
		var triangleBaseSide=Math.round(this.delta*0.25);
		var triangleHalfBaseSide=Math.round(this.delta*0.125);
		var triangleY=Math.round(this.delta*0.5);
		var triangleP = new String();
		if (this.opensubmenuright) {
			triangleP += "M"+ (this.x+this.width-4) + " " + (this.y+triangleY);
			triangleP += "L" + (this.x+this.width-4-triangleBaseSide) + " " + (this.y+triangleY - triangleHalfBaseSide);
			triangleP += "L" + (this.x+this.width-4-triangleBaseSide) + " " + (this.y+triangleY+triangleHalfBaseSide);
			triangleP += " Z";
		} else {
			triangleP += "M"+ (this.x+4) + " " + (this.y+triangleY);
			triangleP += "L" + (this.x+4+triangleBaseSide) + " " + (this.y+triangleY - triangleHalfBaseSide);
			triangleP += "L" + (this.x+4+triangleBaseSide) + " " + (this.y+triangleY+triangleHalfBaseSide);
			triangleP += " Z";
		}
		trianglePath.setAttributeNS(null,"d", triangleP);	
		trianglePath.setAttributeNS(null,"fill", "gray");	
		this.menuItems[index].appendChild(trianglePath);

		var subMenuGroup = document.createElementNS(svgNS,"g");
		subMenuGroup.setAttributeNS(null,"visibility","hidden");
		subMenuGroup.setAttributeNS(null,"id",this.id+"__"+index+"__00__CMSubMenuGroup");
		
		var height = Math.round(this.delta*nitems);
		var x,y;
		if (this.opensubmenuright) {
			x=this.x+this.width+1;
		} else {
			x=this.x-1-this.submenuwidth;
		}

		if (this.subMenuOpenBottom[index]) {
			y=this.y+1;
		} else {
			y=this.y-1-height+this.delta;
		}

		var submenu = document.createElementNS(svgNS,"rect");
		submenu.setAttributeNS(null,"id", this.id+"__"+index+"__00__CMRect");
		submenu.setAttributeNS(null,"x", x);
		submenu.setAttributeNS(null,"y", y);
		submenu.setAttributeNS(null,"width", this.submenuwidth);
		submenu.setAttributeNS(null,"height", height);
		for (var attrib in this.menuStyle) {
			submenu.setAttributeNS(null,attrib,this.menuStyle[attrib]);
		}		
		subMenuGroup.appendChild(submenu);
		this.menuItems[index].appendChild(subMenuGroup);

		this.contextMenuGroup.appendChild(this.menuItems[index]);

		var bbox=Text.getBBox();
		if (bbox.width > this.width*0.8 ) {
			var length = Math.round(itemlabel.length*this.width*0.8/bbox.width);
			var oadip=itemlabel.substr(0,length-2)+'...';
		    var ls = Text.childNodes;
	        while (ls.length > 0) {
	          var obj = ls.item(0);
	          Text.removeChild(obj);
	        }
			contentText = document.createTextNode(oadip);
			Text.appendChild(contentText);
		}
	
		if (withbarbefore) {
			var line = 	document.createElementNS(svgNS,"line")		
			line.setAttributeNS(null,"x1", this.x+this.width*0.01);
			line.setAttributeNS(null,"y1", this.y);
			line.setAttributeNS(null,"x2", this.x+this.width*0.99);
			line.setAttributeNS(null,"y2", this.y);
			for (var attrib in this.menuBarStyle) {
				line.setAttributeNS(null,attrib,this.menuBarStyle[attrib]);
			}	
			this.contextMenuGroup.appendChild(line);		
		}
		
				
		this.y = this.y+this.delta;		
	} else {
		alert("Error Adding SubMenu in ContextMenuSimulator");
	}

}

ContextMenuSimulator.prototype.addItem = function(index,itemlabel,onClickActions,withbarbefore) {
	if ( arguments.length == 4 ) {
		
		this.menuItemsAction[index]=onClickActions;
		
		this.menuItems[index] = document.createElementNS(svgNS,"g");
		this.menuItems[index].setAttributeNS(null,"display","inline");
		this.menuItems[index].setAttributeNS(null,"id",this.id+"__"+index+"__00__CMItemGroup");
		this.menuItems[index].addEventListener("mouseover",this,false);
		this.menuItems[index].addEventListener("mouseout",this,false);
	
		var menuElement = document.createElementNS(svgNS,"rect");
		menuElement.setAttributeNS(null,"id", this.id+"__"+index+"__00__CMRect");
		menuElement.setAttributeNS(null,"x", this.x);
		menuElement.setAttributeNS(null,"y", this.y);
		menuElement.setAttributeNS(null,"width", this.width);
		menuElement.setAttributeNS(null,"height", this.delta);
		for (var attrib in this.menuElementStyle) {
			menuElement.setAttributeNS(null,attrib,this.menuElementStyle[attrib]);
		}		
		this.menuItems[index].appendChild(menuElement);
		
		var Text = document.createElementNS(svgNS,"text");
		Text.setAttributeNS(null,"id" , this.id+"__"+index+"__00" );
		Text.setAttributeNS(null,"cursor","pointer");
		Text.addEventListener("click",this,false);
		Text.setAttributeNS(null,"x", this.x+this.width * 0.1);
		Text.setAttributeNS(null,"y", this.y+0.6*this.delta );
		for (var attrib in this.menuElementTextStyle) {
			Text.setAttributeNS(null,attrib,this.menuElementTextStyle[attrib]);
		}	
		var contentText = document.createTextNode(itemlabel);
		Text.appendChild(contentText);
		this.menuItems[index].appendChild(Text);
	
		this.contextMenuGroup.appendChild(this.menuItems[index]);

		var bbox=Text.getBBox();
		if (bbox.width > this.width*0.8 ) {
			var length = Math.round(itemlabel.length*this.width*0.8/bbox.width);
			var oadip=itemlabel.substr(0,length-2)+'...';
		    var ls = Text.childNodes;
	        while (ls.length > 0) {
	          var obj = ls.item(0);
	          Text.removeChild(obj);
	        }
			contentText = document.createTextNode(oadip);
			Text.appendChild(contentText);
		}

		if (withbarbefore) {
			var line = 	document.createElementNS(svgNS,"line")		
			line.setAttributeNS(null,"x1", this.x+this.width*0.01);
			line.setAttributeNS(null,"y1", this.y);
			line.setAttributeNS(null,"x2", this.x+this.width*0.99);
			line.setAttributeNS(null,"y2", this.y);
			for (var attrib in this.menuBarStyle) {
				line.setAttributeNS(null,attrib,this.menuBarStyle[attrib]);
			}	
			this.contextMenuGroup.appendChild(line);		
		}
		

		this.y = this.y+this.delta;		
	} else {
		alert("Error Adding Item in ContextMenuSimulator");
	}

}

ContextMenuSimulator.prototype.handleEvent = function(evt) {

	var item = evt.target;
	var id = item.getAttributeNS(null,"id");
	var idArray = id.split("__");
	var index = idArray[1];
	var subindex = idArray[2];

	if (evt.type == "mousedown") {
		return;
	}
	if (evt.type == "mouseup") {
		return;	
	}
	
	if (evt.type == "mouseover") {
		var rect = document.getElementById(this.id+"__"+index+"__"+subindex+"__CMRect");
		for (var attrib in this.menuElementMouseStyle) {
			rect.setAttributeNS(null,attrib,this.menuElementMouseStyle[attrib]);
		}
		for ( var cindex in this.subMenuY) {
			var submenu = document.getElementById(this.id+"__"+cindex+"__00__CMSubMenuGroup");
			if (cindex == index)
				submenu.setAttributeNS(null,"visibility","visible");
			else
				submenu.setAttributeNS(null,"visibility","hidden");
		}	
	}

	if (evt.type == "mouseout") {
		var rect = document.getElementById(this.id+"__"+index+"__"+subindex+"__CMRect");
		for (var attrib in this.menuElementStyle) {
			rect.setAttributeNS(null,attrib,this.menuElementStyle[attrib]);
		}
	}
	
	if (evt.type == "click") {
		this.menuItemsAction[index](index,this.id,this.menulabel,subindex);
		this.contextMenuGroup.setAttributeNS(null,"display","none");
	}
}
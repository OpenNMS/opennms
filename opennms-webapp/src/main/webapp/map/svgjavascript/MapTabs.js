/*
Scripts to create interactive tabs in SVG using ECMA script

Credits:
Copyright (C) <2006>  <Andreas Neumann>
Version 1.2, 2006-04-03
neumann@karto.baug.ethz.ch
http://www.carto.net/
http://www.carto.net/neumann/

*/

function MapTabs(id,parentNode,width,tabwidth,tabheight,tabStyles,activetabBGColor,tabtextStyles,tabTitles,activeTabindex) {
	var nrArguments = 10;
	var createTabgroup = true;
	if (arguments.length == nrArguments) {
		this.id = id;
		this.parentNode = parentNode; //can be of type string (id) or node reference (svg or g node)
		this.x = 0;
		this.y = 0;
		this.width = width;
		var tabminsize=Math.round(width/tabTitles.length);
		if (tabminsize > tabwidth) {
			this.tabwidth = tabwidth;
		} else {
			this.tabwidth = tabminsize-1;
		}
		this.tabheight = tabheight;
		this.tabStyles = tabStyles;
		if (!this.tabStyles["fill"]) {
			this.tabStyles["fill"] = "lightgray";
		}
		this.activetabBGColor = activetabBGColor;
		this.tabtextStyles = tabtextStyles;
		if (!this.tabtextStyles["font-size"]) {
			this.tabtextStyles["font-size"] = 15;
		}
		this.tabTitles = tabTitles;
		if (this.tabTitles instanceof Array) {
			if (this.tabTitles.length == 0) {
				createTabgroup = false;
				alert("Error ("+this.id+"): the array 'tabTitles' has no elements!");
			}
		} else {
			createTabgroup = false;
			alert("Error ("+this.id+"): the array 'tabTitles' is not of type array!");
		}
		this.activeTabindex = activeTabindex;
		if (this.activeTabindex >= this.tabTitles.length) {
			createTabgroup = false;
			this.outOfBoundMessage(this.activeTabindex);
		}
	} else {
		createTabgroup = false;
		alert("Error ("+id+"): wrong nr of arguments! You have to pass over "+nrArguments+" parameters.");
		return;
	}
	this.parentGroup = null; 
	//set the node reference to the parent group
	var result = this.testParent();
	if (result) {
		this.tabGroup = null; //later a reference to the group within the parentGroup
		this.tabwindows = new Array();
		this.tabline = null;
		for (var i=0;i<this.tabTitles.length;i++) {
			this.tabwindows[i] = new Array();
		}
		this.createTabGroup();
	} else {
		alert("could not create or reference 'parentNode' of tabgroup with id '"+this.id+"'");
	}
}

//test if window group exists or create a new group at the end of the file
MapTabs.prototype.testParent = function() {
    //test if of type object
    var nodeValid = false;
    if (typeof(this.parentNode) == "object") {
    	if (this.parentNode.nodeName == "svg" || this.parentNode.nodeName == "g") {
    		this.parentGroup = this.parentNode;
    		nodeValid = true;
    	}
    } else if (typeof(this.parentNode) == "string") { 
    	//first test if Windows group exists
    	if (!document.getElementById(this.parentNode)) {
        	this.parentGroup = document.createElementNS(svgNS,"g");
        	this.parentGroup.setAttributeNS(null,"id",this.parentNode);
        	document.documentElement.appendChild(this.parentGroup);
        	nodeValid = true;
   		} else {
       		this.parentGroup = document.getElementById(this.parentNode);
       		nodeValid = true;
   		}
   	}
   	return nodeValid;
}

MapTabs.prototype.createTabGroup = function() {
	this.tabGroup = document.createElementNS(svgNS,"g");
	this.parentGroup.appendChild(this.tabGroup);
	//loop to create all tabs
	var currentX = this.x;
	var buttonSide = Math.round(this.tabheight/2);
	var buttonLeftMargin = Math.round(this.tabheight/4);
	var lineButtonMargin = Math.round(this.tabheight/10);
	var buttonPx = Math.round(this.tabheight/20);
	var buttonMargin = this.tabheight;

	for (var i=0;i<this.tabTitles.length;i++) {
		var currentLeft = currentX;
		//create group
		this.tabwindows[i]["group"] = document.createElementNS(svgNS,"g");
		this.tabGroup.appendChild(this.tabwindows[i]["group"]);
		
		// add close tab button
		this.tabwindows[i]["closeButton"] = document.createElementNS(svgNS,"g");
		this.tabwindows[i]["closeButton"].setAttributeNS(null,"id",this.id+"__"+i+"_closeButton");

		var rectClose=document.createElementNS(svgNS,"rect");
		rectClose.setAttributeNS(null,"id",this.id+"__"+i+"_closeButtonRect");
		rectClose.setAttributeNS(null,"x",currentLeft+buttonLeftMargin);
		rectClose.setAttributeNS(null,"y",buttonLeftMargin);
		rectClose.setAttributeNS(null,"rx",buttonPx);
		rectClose.setAttributeNS(null,"ry",buttonPx);
		rectClose.setAttributeNS(null,"width",buttonSide);
		rectClose.setAttributeNS(null,"height",buttonSide);
		rectClose.setAttributeNS(null,"fill","dimgrey");
		rectClose.setAttributeNS(null,"stroke","dimgrey");
		rectClose.setAttributeNS(null,"opacity",0.9);		
		
		var line1 = document.createElementNS(svgNS,"line");
		line1.setAttributeNS(null,"id",this.id+"__"+i+"_closeButtonLine1");
		line1.setAttributeNS(null,"x1",currentLeft+buttonLeftMargin+lineButtonMargin);
		line1.setAttributeNS(null,"y1",buttonLeftMargin+lineButtonMargin);
		line1.setAttributeNS(null,"x2",currentLeft+buttonLeftMargin+buttonSide-lineButtonMargin);
		line1.setAttributeNS(null,"y2",buttonLeftMargin+buttonSide-lineButtonMargin);
		line1.setAttributeNS(null,"stroke-width",buttonPx);
		line1.setAttributeNS(null,"stroke","black");
		line1.setAttributeNS(null,"opacity",0.5);		

		var line2 = document.createElementNS(svgNS,"line");
		line2.setAttributeNS(null,"id",this.id+"__"+i+"_closeButtonLine2");
		line2.setAttributeNS(null,"x1",currentLeft+buttonLeftMargin+buttonSide-lineButtonMargin);
		line2.setAttributeNS(null,"y1",buttonLeftMargin+lineButtonMargin);
		line2.setAttributeNS(null,"x2",currentLeft+buttonLeftMargin+lineButtonMargin);
		line2.setAttributeNS(null,"y2",buttonLeftMargin+buttonSide-lineButtonMargin);
		line2.setAttributeNS(null,"stroke-width",buttonPx);
		line2.setAttributeNS(null,"stroke","black");
		line2.setAttributeNS(null,"opacity",0.5);		
		 
		this.tabwindows[i]["closeButton"].appendChild(rectClose);
		this.tabwindows[i]["closeButton"].appendChild(line1);
		this.tabwindows[i]["closeButton"].appendChild(line2);
		this.tabwindows[i]["closeButton"].addEventListener("click",this.closeTab,false);
//		this.tabwindows[i]["closeButton"].setAttributeNS(null,"display","none");	

		currentLeft += buttonMargin;
		//create tabTitle
		this.tabwindows[i]["tabTitle"] = document.createElementNS(svgNS,"text");
		this.tabwindows[i]["tabTitle"].setAttributeNS(null,"x",currentX+this.tabwidth * 0.5);
		this.tabwindows[i]["tabTitle"].setAttributeNS(null,"y",this.y + this.tabtextStyles["font-size"]);
		var value="";
		for (var attrib in this.tabtextStyles) {
			value = this.tabtextStyles[attrib];
			if (attrib == "font-size") {
				value += "px";
			}
			this.tabwindows[i]["tabTitle"].setAttributeNS(null,attrib,value);
		}		
		//create tspans and add text contents
		var tspan = document.createElementNS(svgNS,"tspan");
		tspan.setAttributeNS(null,"dy",0);
		var textNode = document.createTextNode(this.tabTitles[i]);
		tspan.appendChild(textNode);
		this.tabwindows[i]["tabTitle"].appendChild(tspan);
		this.tabwindows[i]["tabTitle"].setAttributeNS(null,"pointer-events","none");
		this.tabwindows[i]["tabTitle"].setAttributeNS(null,"text-anchor","middle");
		//now draw tab
		//start the path for tab windows
		var d = "M";
		//left up corner of tab
		d += currentX+" "+this.y;
		//right up corner of tab
		d += "L"+(currentX+this.tabwidth-buttonSide)+" "+this.y;
		d += "L"+(currentX+this.tabwidth)+" "+(this.y+buttonSide);
		d += "L"+(currentX+this.tabwidth)+" "+(this.y+this.tabheight);
		d += "L"+currentX+" "+(this.y+this.tabheight) + "Z";
		//create tab element
		this.tabwindows[i]["tab"] = document.createElementNS(svgNS,"path");
		for (var attrib in this.tabStyles) {
			this.tabwindows[i]["tab"].setAttributeNS(null,attrib,this.tabStyles[attrib]);
		}
		this.tabwindows[i]["tab"].setAttributeNS(null,"d",d);
		this.tabwindows[i]["tab"].setAttributeNS(null,"id",this.id+"__"+i);
		this.tabwindows[i]["tab"].addEventListener("click",this.onClick,false);
//		this.tabwindows[i]["tab"].addEventListener("mouseover",this.onMouseOver,false);
//		this.tabwindows[i]["tab"].addEventListener("mouseout",this.onMouseOut,false);

		this.tabwindows[i]["group"].appendChild(this.tabwindows[i]["tab"]);
		this.tabwindows[i]["group"].appendChild(this.tabwindows[i]["closeButton"]);
		this.tabwindows[i]["group"].appendChild(this.tabwindows[i]["tabTitle"]);

		//increment currentX with the space between tabs
		currentX += this.tabwidth;
	}
	this.tabline = document.createElementNS(svgNS,"path");
	var d = "M";
	d += currentX+" "+(this.y+this.tabheight);
	d += "L"+this.width+" "+(this.y+this.tabheight) + "z";
	for (var attrib in this.tabStyles) {
		this.tabline.setAttributeNS(null,attrib,this.tabStyles[attrib]);
	}
	this.tabline.setAttributeNS(null,"d",d);
	this.tabline.setAttributeNS(null,"pointer-events","none");
	this.tabGroup.appendChild(this.tabline);
	//activate one tab
	this.tabwindows[this.activeTabindex]["tab"].setAttributeNS(null,"fill",this.activetabBGColor);
}

MapTabs.prototype.activateTabByIndex = function(tabindex) {
	if (tabindex >= this.tabTitles.length) {
		this.outOfBoundMessage(tabindex);
		tabindex = 0;
	}
	//set old active tab to inactive
	this.tabwindows[this.activeTabindex]["tab"].setAttributeNS(null,"fill",this.tabStyles["fill"]);
	//set new index
	this.activeTabindex = tabindex;
	//activate new tab
	this.tabwindows[this.activeTabindex]["tab"].setAttributeNS(null,"fill",this.activetabBGColor);
	//reorder tabs
	this.tabGroup.appendChild(this.tabwindows[this.activeTabindex]["group"]);
}

MapTabs.prototype.activateTabByTitle = function(title) {
	var tabindex = -1;
	for (var i=0;i<this.tabTitles.length;i++) {
		if (this.tabTitles[i] == title) {
			tabindex = i;
			break;
		}
	}
	if (tabindex != -1) {
		this.activateTabByIndex(tabindex);
	} else {
		alert("Error ("+this.id+"): Could not find title '"+title+"' in array tabTitles!");
	}
}

//out of bound error message
MapTabs.prototype.outOfBoundMessage = function(tabindex) {
	alert("Error ("+this.id+"): the 'tabindex' (value: "+tabindex+") is out of bounds!\nThe index nr is bigger than the number of tabs.");
}

MapTabs.prototype.getActiveTabIndex = function() {
	return this.activeTabindex;
}

MapTabs.prototype.getActiveTabTitle = function() {
		return this.tabTitles[this.activeTabindex];
}

MapTabs.prototype.getTabTitleByIndex = function(tabindex) {
		return this.tabTitles[tabindex];
}

MapTabs.prototype.onClick = onClickTab;
MapTabs.prototype.closeTab = onCloseTab;

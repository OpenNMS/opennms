/*
Scripts to create interactive selectionLists/Dropdown boxes in SVG using ECMA script
Copyright (C) <2004>  <Andreas Neumann>
Version 0.99, 2004-10-26
neumann@karto.baug.ethz.ch
http://www.carto.net/
http://www.karto.ethz.ch/neumann/

Initial code was taken from Michel Hirtzler --> pilat.free.fr (thanks!)
Thanks also to many people of svgdevelopers@yahoogroups.com

This ECMA script library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library (lesser_gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

----

original document site: http://www.carto.net/papers/svg/gui/selectionlist/
Please contact the author in case you want to use code or ideas commercially.
If you use this code, please include this copyright header, the included full
LGPL 2.1 text and read the terms provided in the LGPL 2.1 license
(http://www.gnu.org/copyleft/lesser.txt)

-------------------------------

If you wish, you can modify parameters (such as "look and feel") in the function "selectionList".
You can adapt colors, fonts, cellpaddings, etc.

-------------------------------

Please report bugs and send improvements to neumann@karto.baug.ethz.ch
If you use this control, please link to the original (http://www.carto.net/papers/svg/gui/selectionlist/)
somewhere in the source-code-comment or the "about" of your project and give credits, thanks!

*/

function selectionList(groupName,elementsArray,width,xOffset,yOffset,heightNrElements,preSelect,functionToCall) {
	//define central look and feel here (colors, fonts and stroke-width, cellHeight, etc.)
	
	this.textLook = "fill:black;font-size:11px;";
	this.selectBoxfillStroke = "stroke:black;fill:white;";
	this.scrollBarfillStroke = "stroke:black;fill:whitesmoke;";
	this.smallRectLook = "stroke:black;fill:lightGray;";
	this.highLightColor = "fill:black;stroke:none;fill-opacity:0.3;";
	this.highLightColorUnsel = "fill:white;stroke:none;";
	this.triangleLook = "stroke:none;fill:darkcyan;";
	this.cellHeight = 16; //outer Height
	this.triangleFourth = Math.round(this.cellHeight / 4);
	this.textPaddingHorizontal = 3; //this is relative to the left of the cell
	this.textPaddingVertical = 12; //this is relative to the top of the cell
	this.scrollerMinHeight = 10; //minimal height of the scroller rect

	
	//individual variables
	this.groupName = groupName;
	this.elementsArray = elementsArray;
	this.width = width;
	this.xOffset = xOffset;
	this.yOffset = yOffset;
	this.heightNrElements = heightNrElements;
	this.preSelect = preSelect;
	this.functionToCall = functionToCall;
	
	//status variables
	this.activeSelection = this.preSelect; //holds currently selected index value
	this.listOpen = false; //status folded=false, open=true - previously been sLselectionVisible
	this.curLowerIndex = this.preSelect; //this value is adapted if the user moves scrollbar
	this.scrollStep = 0; //y-value to go for one element
	this.scrollerHeight = 0; //height of dragable scroller bar
	this.scrollActive = false; //determines if scrolling per up/down button is active
	this.panY = false; //stores the y value of event
	this.scrollCumulus = 0; //if value is less then a scrollstep we need to accumulate scroll values
	this.scrollDir = ""; //later holds "up" and "down"
	this.exists = true; //true means it exists, gets value false if method "removeSelectionList" is called

	//createSelectionList
	this.selectionBoxGroup = document.getElementById(this.groupName);
	this.createSelectionList();
}

selectionList.prototype.createSelectionList = function() {
		//initial Rect, visible at the beginning
		var node = document.createElementNS(svgNS,"rect");
		node.setAttributeNS(null,"x",this.xOffset);
		node.setAttributeNS(null,"y",this.yOffset);
		node.setAttributeNS(null,"width",this.width);
		node.setAttributeNS(null,"height",this.cellHeight);
		node.setAttributeNS(null,"style",this.selectBoxfillStroke);
		node.setAttributeNS(null,"id","selRect_"+this.groupName);
		node.addEventListener("click", this, false);
		this.selectionBoxGroup.appendChild(node);
		//initial text
		this.selectedText = document.createElementNS(svgNS,"text");
		this.selectedText.setAttributeNS(null,"x",this.xOffset + this.textPaddingHorizontal);
		this.selectedText.setAttributeNS(null,"y",this.yOffset + this.textPaddingVertical);
		this.selectedText.setAttributeNS(null,"style",this.textLook);
		this.selectedText.setAttributeNS(null,"pointer-events","none");
		var selectionText = document.createTextNode(replaceSpecialChars(this.elementsArray[this.activeSelection]));
		this.selectedText.appendChild(selectionText);
		this.selectionBoxGroup.appendChild(this.selectedText);
		//small Rectangle to the right, onclick unfolds the selectionList
		var node = document.createElementNS(svgNS,"rect");
		node.setAttributeNS(null,"x",this.xOffset + this.width - this.cellHeight);
		node.setAttributeNS(null,"y",this.yOffset);
		node.setAttributeNS(null,"width",this.cellHeight);
		node.setAttributeNS(null,"height",this.cellHeight);
		node.setAttributeNS(null,"style",this.smallRectLook);
		node.setAttributeNS(null,"id","selPulldown_"+this.groupName);
		node.addEventListener("click", this, false);
		this.selectionBoxGroup.appendChild(node);
		//triangle
		var node=document.createElementNS(svgNS,"path");
		var myTrianglePath = "M"+(this.xOffset + this.width - 3 * this.triangleFourth)+" "+(this.yOffset + this.triangleFourth)+" L"+(this.xOffset + this.width - this.triangleFourth)+" "+(this.yOffset + this.triangleFourth)+" L"+(this.xOffset + this.width - 2 * this.triangleFourth)+" "+(this.yOffset + 3 * this.triangleFourth)+" Z";
		node.setAttributeNS(null,"d",myTrianglePath);
		node.setAttributeNS(null,"style",this.triangleLook);
		node.setAttributeNS(null,"pointer-events","none");
		this.selectionBoxGroup.appendChild(node);
		//rectangle below unfolded selectBox, at begin invisible
		this.rectBelowBox = document.createElementNS(svgNS,"rect");
		this.rectBelowBox.setAttributeNS(null,"x",this.xOffset);
		this.rectBelowBox.setAttributeNS(null,"y",this.yOffset + this.cellHeight);
		this.rectBelowBox.setAttributeNS(null,"width",this.width - this.cellHeight);
		this.rectBelowBox.setAttributeNS(null,"height",0);
		this.rectBelowBox.setAttributeNS(null,"style",this.selectBoxfillStroke);
		this.rectBelowBox.setAttributeNS(null,"visibility","hidden");
		this.selectionBoxGroup.appendChild(this.rectBelowBox);
		//group node to hold dynamic text elements and highLight-Rects
		this.dynamicTextGroup = document.createElementNS(svgNS,"g");
		this.selectionBoxGroup.appendChild(this.dynamicTextGroup);
}



selectionList.prototype.handleEvent = function(evt) {
	var el = evt.currentTarget;
	var callerId = el.getAttributeNS(null,"id");
	var myRegExp = new RegExp(this.groupName);
	if (evt.type == "click") {
		if (myRegExp.test(callerId)) {
			if (callerId.match(/\bselPulldown|\bselRect/)) {
				if (this.listOpen == false) {
					this.unfoldList();
					this.listOpen = true;
				}
				else {
					this.foldList();
					this.listOpen = false;
					evt.stopPropagation();
				}
			}
			if (callerId.match(/\bselHighlightSelection_/)) {
				this.selectAndClose(evt);
				evt.stopPropagation();
			}
		}
		else {/*
			//case that the event comes from the documentRoot element
			//but not from the selectionList itself
			if (!myRegExp.test(evt.target.getAttributeNS(null,"id"))) {
				this.foldList();
				this.listOpen = false;
			}
			*/
		}
	}
	if (evt.type == "mouseover") {
		if (callerId.match(/\bselHighlightSelection_/)) {
			el.setAttributeNS(null,"style",this.highLightColor);
		}
	}
	if (evt.type == "mouseout") {
		if (callerId.match(/\bselHighlightSelection_/) ){
			el.setAttributeNS(null,"style",this.highLightColorUnsel);
		}
		if (callerId.match(/\bselScrollbarRect_/)) {
		    this.scrollBarMove(evt);
		}
	}
	if (evt.type == "mousedown") {
		if (callerId.match(/\bselScrollUpperRect_|\bselScrollLowerRect/)) {
			this.scrollDir = "down";
			this.scrollActive = true;
			if (callerId.match(/UpperRect/)) {
				this.scrollDir = "up";
			}
			//setTimeout(mister(this),3000);
			//window.setTimeout(this.scrollPerButton(scrollDir),3000);
			this.scroll(this.scrollDir,1,true);
		}
		if (callerId.match(/\bselScrollbarRect_/)) {
		    this.scrollBarMove(evt);
		}
	}
	if (evt.type == "mouseup") {
		if (callerId.match(/\bselScrollUpperRect_|\bselScrollLowerRect/)) {
			this.scrollActive = false;
		}
		if (callerId.match(/\bselScrollbarRect_/)) {
		    this.scrollBarMove(evt);
		}
	}
	if (evt.type == "mousemove") {
		if (callerId.match(/\bselScrollbarRect_/)) {
		    this.scrollBarMove(evt);
		}
	}	//add keypress event
	if (evt.type == "keypress") {
		var pressedKey = String.fromCharCode(evt.charCode).toLowerCase();
		this.scrollToKey(pressedKey);	
	}
}

//this function is called when selectionList is unfolded
selectionList.prototype.unfoldList = function() {
	if (!this.dynamicTextGroup) {
		this.dynamicTextGroup = document.createElementNS(svgNS,"g");
		this.selectionBoxGroup.appendChild(this.dynamicTextGroup);	
	}
	var nrSelectionsOrig = this.elementsArray.length;
	if (this.heightNrElements < nrSelectionsOrig) {
		nrSelections = this.heightNrElements;
	}
	else {
		nrSelections = nrSelectionsOrig;
	}
	var selectionHeight = this.cellHeight * nrSelections;
	this.rectBelowBox.setAttributeNS(null,"height",selectionHeight);
	this.rectBelowBox.setAttributeNS(null,"visibility","visible");
	//build textElements from array
	//hold currentSelection Index to unroll list at new offset
	if ((nrSelectionsOrig - this.activeSelection) >= nrSelections) { 
		this.curLowerIndex = this.activeSelection;
	}
	else {
		this.curLowerIndex = nrSelectionsOrig - nrSelections;
	}
	for (var i=0;i<nrSelections;i++) {
		//add rectangles to capture events
		var node = document.createElementNS(svgNS,"rect");
		node.setAttributeNS(null,"x",this.xOffset + this.textPaddingHorizontal / 2);
		node.setAttributeNS(null,"y",this.yOffset + this.cellHeight * (i+1));
		node.setAttributeNS(null,"width",this.width - this.cellHeight - this.textPaddingHorizontal);
		node.setAttributeNS(null,"height",this.cellHeight);
		node.setAttributeNS(null,"style",this.highLightColorUnsel);
		node.setAttributeNS(null,"id","selHighlightSelection_" + this.groupName + "_" + (i + this.curLowerIndex));
		//add event-handler
		node.addEventListener("mouseover", this, false);
		node.addEventListener("mouseout", this, false);
		node.addEventListener("click", this, false);
		node.addEventListener("keypress", this, false);
		this.dynamicTextGroup.appendChild(node);
		//add text-elements
		var node = document.createElementNS(svgNS,"text");
		node.setAttributeNS(null,"id","selTexts_" + this.groupName + "_" + (i + this.curLowerIndex));
		node.setAttributeNS(null,"x",this.xOffset + this.textPaddingHorizontal);
		node.setAttributeNS(null,"y",this.yOffset + this.textPaddingVertical + this.cellHeight * (i + 1));
		node.setAttributeNS(null,"pointer-events","none");
		node.setAttributeNS(null,"style",this.textLook);
		var selectionText = document.createTextNode(replaceSpecialChars(this.elementsArray[this.curLowerIndex + i]));
		node.appendChild(selectionText);
		this.dynamicTextGroup.appendChild(node);
	}
	
	//create Scrollbar
	if (this.heightNrElements < nrSelectionsOrig) {
		//calculate scrollstep
		this.scrollerHeight = (this.heightNrElements / nrSelectionsOrig) * (selectionHeight - 2 * this.cellHeight);
		if (this.scrollerHeight < this.scrollerMinHeight) {
			this.scrollerHeight = this.scrollerMinHeight;
		}
		this.scrollStep = (selectionHeight - 2 * this.cellHeight - this.scrollerHeight) / (nrSelectionsOrig - this.heightNrElements);
		//scrollbar
		var node = document.createElementNS(svgNS,"rect");
		node.setAttributeNS(null,"x",this.xOffset + this.width - this.cellHeight);
		node.setAttributeNS(null,"y",this.yOffset + this.cellHeight * 2);
		node.setAttributeNS(null,"width",this.cellHeight);
		node.setAttributeNS(null,"height",selectionHeight - this.cellHeight * 2);
		node.setAttributeNS(null,"style",this.scrollBarfillStroke);
		node.setAttributeNS(null,"id","selScrollbarRect_"+this.groupName);
		node.addEventListener("mousedown", this, false);
		node.addEventListener("mouseup", this, false);
		node.addEventListener("mousemove", this, false);
		node.addEventListener("mouseout", this, false);
		this.dynamicTextGroup.appendChild(node);
		//upper rectangle		
		var node = document.createElementNS(svgNS,"rect");
		node.setAttributeNS(null,"x",this.xOffset + this.width - this.cellHeight);
		node.setAttributeNS(null,"y",this.yOffset + this.cellHeight);
		node.setAttributeNS(null,"width",this.cellHeight);
		node.setAttributeNS(null,"height",this.cellHeight);
		node.setAttributeNS(null,"style",this.smallRectLook);
		node.setAttributeNS(null,"id","selScrollUpperRect_"+this.groupName);
		node.addEventListener("mousedown", this, false);
		node.addEventListener("mouseup", this, false);
		this.dynamicTextGroup.appendChild(node);
		//upper triangle
		var node = document.createElementNS(svgNS,"path");
		var myPath = "M"+(this.xOffset + this.width - 3 * this.triangleFourth)+" "+(this.yOffset + this.cellHeight + 3 * this.triangleFourth)+" L"+(this.xOffset + this.width - this.triangleFourth)+" "+(this.yOffset + this.cellHeight + 3 * this.triangleFourth)+" L"+(this.xOffset + this.width - 2 * this.triangleFourth)+" "+(this.yOffset + this.cellHeight + this.triangleFourth)+" Z";
		node.setAttributeNS(null,"d",myPath);
		node.setAttributeNS(null,"style",this.triangleLook);
		node.setAttributeNS(null,"pointer-events","none");
		this.dynamicTextGroup.appendChild(node);
		//lower rectangle		
		var node = document.createElementNS(svgNS,"rect");
		node.setAttributeNS(null,"x",this.xOffset + this.width - this.cellHeight);
		node.setAttributeNS(null,"y",this.yOffset + selectionHeight);
		node.setAttributeNS(null,"width",this.cellHeight);
		node.setAttributeNS(null,"height",this.cellHeight);
		node.setAttributeNS(null,"style",this.smallRectLook);
		node.setAttributeNS(null,"id","selScrollLowerRect_" + this.groupName);
		node.addEventListener("mousedown", this, false);
		node.addEventListener("mouseup", this, false);
		this.dynamicTextGroup.appendChild(node);
		//lower triangle
		var node = document.createElementNS(svgNS,"path");
		var myPath = "M"+(this.xOffset + this.width - 3 * this.triangleFourth)+" "+(this.yOffset + selectionHeight + this.triangleFourth)+" L"+(this.xOffset + this.width - this.triangleFourth)+" "+(this.yOffset + selectionHeight + this.triangleFourth)+" L"+(this.xOffset + this.width - 2 * this.triangleFourth)+" "+(this.yOffset + selectionHeight + 3 * this.triangleFourth)+" Z";
		node.setAttributeNS(null,"d",myPath);
		node.setAttributeNS(null,"style",this.triangleLook);
		node.setAttributeNS(null,"pointer-events","none");
		this.dynamicTextGroup.appendChild(node);
		//scrollerRect
		var node = document.createElementNS(svgNS,"rect");
		node.setAttributeNS(null,"x",this.xOffset + this.width - this.cellHeight);
		node.setAttributeNS(null,"y",this.yOffset + this.cellHeight * 2 + this.scrollStep * this.curLowerIndex);
		node.setAttributeNS(null,"width",this.cellHeight);
		node.setAttributeNS(null,"height",this.scrollerHeight);
		node.setAttributeNS(null,"style",this.smallRectLook);
		node.setAttributeNS(null,"pointer-events","none");
		node.setAttributeNS(null,"id","selScroller_"+this.groupName);
		this.dynamicTextGroup.appendChild(node);
	}
	//add event handler to root element to close selectionList if one clicks outside
	document.documentElement.addEventListener("click",this,false);
	document.documentElement.addEventListener("keypress",this,false);
}

//this function folds/hides the selectionList again
selectionList.prototype.foldList = function() {
	this.selectionBoxGroup.removeChild(this.dynamicTextGroup);
	this.dynamicTextGroup = null;
	this.rectBelowBox.setAttributeNS(null,"visibility","hidden");
	document.documentElement.removeEventListener("click",this,false);
	document.documentElement.removeEventListener("keypress",this,false);
}

selectionList.prototype.selectAndClose = function(evt) {
	var mySelEl = evt.target;
	var result = mySelEl.getAttributeNS(null,"id").split("_");
	this.activeSelection = parseInt(result[2]);
	this.curLowerIndex = this.activeSelection;
	this.foldList();
	this.listOpen = false;
	this.selectedText.firstChild.nodeValue = replaceSpecialChars(this.elementsArray[this.activeSelection]);
	this.fireFunction();
}

selectionList.prototype.fireFunction = function() {
	if (typeof(this.functionToCall) == "string") {
		var functionString = this.functionToCall+"('"+this.groupName+"',"+this.activeSelection+",'"+this.elementsArray[this.activeSelection]+"')";
		window.setTimeout("eval("+functionString+")",300);
	}
	if (typeof(this.functionToCall) == "function") {
	alert("function");
		window.setTimeout(this.functionToCall(this.groupName,this.activeSelection,this.elementsArray[this.activeSelection]),300);
	}
	if (typeof(this.functionToCall) == "object") {
	//alert("obj");
  	  try{
		window.setTimeout(this.functionToCall.getSelectionListVal(this.groupName,this.activeSelection,this.elementsArray[this.activeSelection]),300);	
	  }catch(e){
	   	
	   }
	}	
}

selectionList.prototype.scrollPerButton = function() {
	//this.scroll(this.scrollDir,1,true); //1 is one element to scroll, true says that we should move scrollbar
	alert(this.scrollDir);
	if (this.scrollActive == true) {
		//setTimeout("this.scrollPerButton("+scrollDir+")",1000);
	}
}

selectionList.prototype.scroll = function(scrollDir,scrollNr,scrollBool) {
	var nrSelections = this.elementsArray.length;
	var scroller = document.getElementById("selScroller_"+this.groupName);
	if (scrollNr < this.heightNrElements) {
		if ((this.curLowerIndex > 0) && (scrollDir == "up")) {
			if (scrollNr > this.curLowerIndex) {
				scrollNr = this.curLowerIndex;
			}
			//decrement current index
			this.curLowerIndex = this.curLowerIndex - scrollNr;
			//move scroller
			if (scrollBool == true) {
				scroller.setAttributeNS(null,"y",parseFloat(scroller.getAttributeNS(null,"y"))+ this.scrollStep * -1);
			}
			//add upper rect elements
			for (var i=0;i<scrollNr;i++) {
				var node = document.createElementNS(svgNS,"rect");
				node.setAttributeNS(null,"x",this.xOffset + this.textPaddingHorizontal / 2);
				node.setAttributeNS(null,"y",this.yOffset + this.cellHeight + this.cellHeight * i);
				node.setAttributeNS(null,"width",this.width - this.cellHeight - this.textPaddingHorizontal);
				node.setAttributeNS(null,"height",this.cellHeight);
				node.setAttributeNS(null,"style",this.highLightColorUnsel);
				node.setAttributeNS(null,"id","selHighlightSelection_" + this.groupName + "_" + (i + this.curLowerIndex));
				//add event-handler
				node.addEventListener("mouseover", this, false);
				node.addEventListener("mouseout", this, false);
				node.addEventListener("click", this, false);
				node.addEventListener("keypress", this, false);
				this.dynamicTextGroup.appendChild(node);
				//add text-nodes
				var node = document.createElementNS(svgNS,"text");
				node.setAttributeNS(null,"id","selTexts_" + this.groupName + "_" + (i + this.curLowerIndex));
				node.setAttributeNS(null,"x",this.xOffset + this.textPaddingHorizontal);
				node.setAttributeNS(null,"y",this.yOffset + this.textPaddingVertical + this.cellHeight * (i + 1));
				node.setAttributeNS(null,"pointer-events","none");
				node.setAttributeNS(null,"style",this.textLook);
				var selectionText = document.createTextNode(replaceSpecialChars(this.elementsArray[this.curLowerIndex + i]));
				node.appendChild(selectionText);
				this.dynamicTextGroup.appendChild(node);
			}
			//move middle elements
			for (var j=i;j<this.heightNrElements;j++) {
				var node = document.getElementById("selTexts_" + this.groupName + "_" + (j + this.curLowerIndex));
				node.setAttributeNS(null,"y",parseFloat(node.getAttributeNS(null,"y")) + (this.cellHeight * scrollNr));
				var node = document.getElementById("selHighlightSelection_" + this.groupName + "_" + (j + this.curLowerIndex));
				node.setAttributeNS(null,"y",parseFloat(node.getAttributeNS(null,"y")) + (this.cellHeight * scrollNr));
			}
			//remove lower elements
			for (var k=j;k<(j+scrollNr);k++) {
				var node = document.getElementById("selTexts_" + this.groupName + "_" + (k + this.curLowerIndex));
				this.dynamicTextGroup.removeChild(node);
				var node = document.getElementById("selHighlightSelection_" + this.groupName +"_" + (k + this.curLowerIndex));
				this.dynamicTextGroup.removeChild(node);
			}
		}
		else if ((this.curLowerIndex < nrSelections - this.heightNrElements) && (scrollDir == "down")) {
			//move Scroller
			if (scrollBool == true) {
				scroller.setAttributeNS(null,"y",parseFloat(scroller.getAttributeNS(null,"y")) + this.scrollStep);
			}
			//remove most upper element 
			for (var i=0;i<scrollNr;i++) {
				var node = document.getElementById("selTexts_" + this.groupName + "_" + (this.curLowerIndex + i));
				this.dynamicTextGroup.removeChild(node);
				var node = document.getElementById("selHighlightSelection_" + this.groupName + "_" + (this.curLowerIndex + i));
				this.dynamicTextGroup.removeChild(node);
			}
			//move middle elements
			for (var j=i;j<this.heightNrElements;j++) {
				var node = document.getElementById("selTexts_" + this.groupName + "_" + (j + this.curLowerIndex));
				node.setAttributeNS(null,"y",parseFloat(node.getAttributeNS(null,"y")) - (this.cellHeight * scrollNr));
				var node = document.getElementById("selHighlightSelection_" + this.groupName + "_" + (j + this.curLowerIndex));
				node.setAttributeNS(null,"y",parseFloat(node.getAttributeNS(null,"y")) - (this.cellHeight * scrollNr));
			}
			//add most lower element
			for (var k=j;k<(j+scrollNr);k++) {
				var node = document.createElementNS(svgNS,"rect");
				node.setAttributeNS(null,"x",this.xOffset + this.textPaddingHorizontal / 2);
				node.setAttributeNS(null,"y",this.yOffset + this.cellHeight * (k - (scrollNr - 1)));
				node.setAttributeNS(null,"width",this.width - this.cellHeight - this.textPaddingHorizontal);
				node.setAttributeNS(null,"height",this.cellHeight);
				node.setAttributeNS(null,"style",this.highLightColorUnsel);
				node.setAttribute("id","selHighlightSelection_" + this.groupName + "_" + (k + this.curLowerIndex));
				//add event-handler
				node.addEventListener("mouseover", this, false);
				node.addEventListener("mouseout", this, false);
				node.addEventListener("click", this, false);
				node.addEventListener("keypress", this, false);
				this.dynamicTextGroup.appendChild(node);
				//add text-nodes
				var node = document.createElementNS(svgNS,"text");
				node.setAttributeNS(null,"id","selTexts_" + this.groupName + "_" + (k + this.curLowerIndex));
				node.setAttributeNS(null,"x",this.xOffset + this.textPaddingHorizontal);
				node.setAttributeNS(null,"y",this.yOffset + this.textPaddingVertical + this.cellHeight * (k - (scrollNr - 1)));
				node.setAttributeNS(null,"pointer-events","none");
				node.setAttributeNS(null,"style",this.textLook);
				var selectionText = document.createTextNode(replaceSpecialChars(this.elementsArray[this.curLowerIndex + k]));
				node.appendChild(selectionText);
				this.dynamicTextGroup.appendChild(node);
			}
			//increment current index
			this.curLowerIndex = this.curLowerIndex + scrollNr;
		}
	}
	else {
			//remove lower elements
			for (var i=0;i<this.heightNrElements;i++) {
				var node = document.getElementById("selTexts_" + this.groupName + "_" + (i + this.curLowerIndex));
				this.dynamicTextGroup.removeChild(node);
				var node = document.getElementById("selHighlightSelection_" + this.groupName +"_" + (i + this.curLowerIndex));
				this.dynamicTextGroup.removeChild(node);
			}
			if (scrollDir == "down") {
				this.curLowerIndex = this.curLowerIndex + scrollNr;
			}
			else {
				this.curLowerIndex = this.curLowerIndex - scrollNr;			
			}
			for (var i=0;i<this.heightNrElements;i++) {
				var node = document.createElementNS(svgNS,"rect");
				node.setAttributeNS(null,"x",this.xOffset + this.textPaddingHorizontal / 2);
				node.setAttributeNS(null,"y",this.yOffset + this.cellHeight + this.cellHeight * i);
				node.setAttributeNS(null,"width",this.width - this.cellHeight - this.textPaddingHorizontal);
				node.setAttributeNS(null,"height",this.cellHeight);
				node.setAttributeNS(null,"style",this.highLightColorUnsel);
				node.setAttributeNS(null,"id","selHighlightSelection_" + this.groupName + "_" + (i + this.curLowerIndex));
				//add event-handler
				node.addEventListener("mouseover", this, false);
				node.addEventListener("mouseout", this, false);
				node.addEventListener("click", this, false);
				node.addEventListener("keypress", this, false);
				this.dynamicTextGroup.appendChild(node);
				//add text-nodes
				var node = document.createElementNS(svgNS,"text");
				node.setAttributeNS(null,"id","selTexts_" + this.groupName + "_" + (i + this.curLowerIndex));
				node.setAttributeNS(null,"x",this.xOffset + this.textPaddingHorizontal);
				node.setAttributeNS(null,"y",this.yOffset + this.textPaddingVertical + this.cellHeight * (i + 1));
				node.setAttributeNS(null,"pointer-events","none");
				node.setAttributeNS(null,"style",this.textLook);
				var selectionText = document.createTextNode(replaceSpecialChars(this.elementsArray[this.curLowerIndex + i]));
				node.appendChild(selectionText);
				this.dynamicTextGroup.appendChild(node);
			}
	}
}

//event listener for Scrollbar element
selectionList.prototype.scrollBarMove = function(evt) {
	var scrollBar = evt.target;
	var scroller = document.getElementById("selScroller_" + this.groupName);
	var scrollerMinY = parseFloat(scrollBar.getAttributeNS(null,"y"));
	var scrollerMaxY = parseFloat(scrollBar.getAttributeNS(null,"y")) + parseFloat(scrollBar.getAttributeNS(null,"height")) - parseFloat(scroller.getAttributeNS(null,"height"));
	if (evt.type == "mousedown") {
		this.panStatus = true;
		scroller.setAttributeNS(null,"style",this.triangleLook);
		var coords = myMapApp.calcCoord(evt.clientX,evt.clientY);
		this.panY = coords["y"];
		var oldY = parseFloat(scroller.getAttributeNS(null,"y"));
		var newY = this.panY - parseFloat(scroller.getAttributeNS(null,"height")) / 2;
		if (newY < scrollerMinY) {
			newY = scrollerMinY;
			//maybe recalculate this.panY ??
		}
		if (newY > scrollerMaxY) {
			newY = scrollerMaxY;
		}
		var panDiffY = newY - oldY;
		var scrollDir = "down";
		this.scrollCumulus = 0;
		if(Math.abs(panDiffY) > this.scrollStep) {
			var scrollNr = Math.abs(Math.round(panDiffY / this.scrollStep));
			if (panDiffY > 0) {
				this.scrollCumulus = panDiffY - this.scrollStep * scrollNr;
			}
			else {
				this.scrollCumulus = panDiffY + this.scrollStep * scrollNr;
				scrollDir = "up";			
			}
			newY = oldY + panDiffY;
			scroller.setAttributeNS(null,"y",newY);
			this.scroll(scrollDir,scrollNr,false);	
		}
	}
	if (evt.type == "mouseup" || evt.type == "mouseout") {
		if (this.panStatus == true) {
			var newY = parseFloat(scroller.getAttributeNS(null,"y"));
			scroller.setAttributeNS(null,"style",this.smallRectLook);
			scroller.setAttributeNS(null,"y",this.yOffset + this.cellHeight * 2 + this.scrollStep * this.curLowerIndex);
		}
		this.panStatus = false;
	}
	if (evt.type == "mousemove") {
		if (this.panStatus == true) {
			var coords = myMapApp.calcCoord(evt.clientX,evt.clientY);
			var panNewEvtY = coords["y"];
			var panDiffY = panNewEvtY - this.panY;
			var oldY = parseFloat(scroller.getAttributeNS(null,"y"));
			var newY = oldY + panDiffY;
			if (newY < scrollerMinY) {
				newY = scrollerMinY;
			}
			if (newY > scrollerMaxY) {
				newY = scrollerMaxY;
			}
			var panDiffY = newY - oldY;
			this.scrollCumulus += panDiffY;
			var scrollDir = "down";
			var scrollNr = 0;
			if(Math.abs(this.scrollCumulus) >= this.scrollStep) {
				scrollNr = Math.abs(Math.round(this.scrollCumulus / this.scrollStep));
				if (this.scrollCumulus > 0) {
					this.scrollCumulus = this.scrollCumulus - this.scrollStep * scrollNr;
				}
				else {
					this.scrollCumulus = this.scrollCumulus + this.scrollStep * scrollNr;
					scrollDir = "up";
				}
				this.scroll(scrollDir,scrollNr,false);
			}
			else {
				if (Math.abs(this.scrollCumulus) > this.scrollStep) {
					scrollNr = 1;
					if (panDiffY < 0) {
						scrollDir = "up";
						this.scrollCumulus = this.scrollCumulus + this.scrollStep;
					}
					else {
						this.scrollCumulus = this.scrollCumulus - this.scrollStep;
					}
					panDiffY = this.scrollCumulus;
					this.scroll(scrollDir,scrollNr,false);
				}
				else {
					if (newY == scrollerMinY && this.curLowerIndex != 0) {
						this.scroll("up",1,false);	
					}
					else if (newY == scrollerMaxY && this.curLowerIndex != (this.elementsArray.length - 	this.heightNrElements)) {
						this.scroll("down",1,false);			
					}
				}
			}
			newY = oldY + panDiffY;	
			scroller.setAttributeNS(null,"y",newY);
			this.panY = panNewEvtY;
		}
	}
}

selectionList.prototype.scrollToKey = function(pressedKey) {
	var oldActiveSelection = this.activeSelection;
	for (var i=0;i<this.elementsArray.length;i++) {
		if (this.elementsArray[i].toLowerCase().charAt(0) == pressedKey) {
			if (this.listOpen == true) {
				this.foldList();	
			}
			this.activeSelection = i;
			this.unfoldList();
			this.listOpen = true;
			this.activeSelection = oldActiveSelection;
			break;
		}	
	}	
}

selectionList.prototype.elementExists = function(elementName) {
	var result = -1;
	for (i=0;i<this.elementsArray.length;i++) {
		if (this.elementsArray[i] == elementName) {
			result = i;
		}	
	}
	return result;
}

selectionList.prototype.selectElementByName = function(elementName,fireFunction) {
	//fireFunction: (true|false); determines whether to execute selectFunction or not
	existsPosition = this.elementExists(elementName);
	if (existsPosition != -1) {
		if (this.listOpen == true) {
			this.foldList();	
		}
		this.activeSelection = existsPosition;
		this.selectedText.firstChild.nodeValue = this.elementsArray[this.activeSelection];
		if (this.listOpen == true) {
			this.unfoldList();	
		}
		if (fireFunction == true) {
			this.fireFunction();
		}
	}	
	return existsPosition;
}

selectionList.prototype.selectElementByPosition = function(position,fireFunction) {
	//fireFunction: (true|false); determines whether to execute selectFunction or not
	if (position < this.elementsArray.length) {
		if (this.listOpen == true) {
			this.foldList();	
		}
		this.activeSelection = position;
		this.selectedText.firstChild.nodeValue = this.elementsArray[this.activeSelection];
		if (this.listOpen == true) {
			this.unfoldList();	
		}
		if (fireFunction == true) {
			this.fireFunction();
		}
	}
	else {
		position = -1;	
	}
	return position;
}

selectionList.prototype.sortList = function(direction) {
	//direction: asc|desc, for ascending or descending
	var mySelElementString = this.elementsArray[this.activeSelection];
	if (this.listOpen == true) {
		this.foldList();
	}	
	this.elementsArray.sort();
	if (direction == "desc") {
		this.elementsArray.reverse();		
	}
	this.activeSelection = this.elementExists(mySelElementString);
	if (this.listOpen == true) {
		this.unfoldList();
	}	
	
	return direction;	
}

selectionList.prototype.deleteElement = function(elementName) {
	existsPosition = this.elementExists(elementName);
	if (existsPosition != -1) {
		if (this.listOpen == true) {
			this.foldList();	
		}
		var tempArray = new Array;
		tempArray = tempArray.concat(this.elementsArray.slice(0,existsPosition),this.elementsArray.slice(existsPosition + 1,this.elementsArray.length));
		this.elementsArray = tempArray;
		if (this.activeSelection == existsPosition) {
			this.selectedText.firstChild.nodeValue = this.elementsArray[this.activeSelection];
		}
		if (this.activeSelection > existsPosition) {
			this.activeSelection -= 1;	
		}
		if (this.listOpen == true) {
			this.unfoldList();	
		}
	}
	return existsPosition;
}

selectionList.prototype.addElementAtPosition = function(elementName,position) {
	if (position > this.elementsArray.length) {
		this.elementsArray.push(elementName);
		position = this.elementsArray.length - 1;
	}
	else {
		var tempArray = new Array;
		tempArray = tempArray.concat(this.elementsArray.slice(0,position),elementName,this.elementsArray.slice(position,this.elementsArray.length));
		this.elementsArray = tempArray;
	}
	if (this.listOpen == true) {
		this.foldList();	
	}
	if (position <= this.activeSelection) {
		this.activeSelection += 1;	
	}
	if (this.listOpen == true) {
		this.unfoldList();	
	}
	return position;
}

selectionList.prototype.getCurrentSelectionElement = function() {
	return this.elementsArray[this.activeSelection];
}

selectionList.prototype.getCurrentSelectionIndex = function() {
	return this.activeSelection;
}

selectionList.prototype.removeSelectionList = function() {
	//remove all Elements of selectionList
	this.exists = false;
	while (this.selectionBoxGroup.hasChildNodes()) {
		this.selectionBoxGroup.removeChild(this.selectionBoxGroup.firstChild);
	}
}

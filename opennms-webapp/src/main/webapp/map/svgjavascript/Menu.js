// Function used by Menu

function elemStrokeWidth(mouseEvent, elemName, value) {
	elemName = spaceTrans(elemName);
	var thisElem = document.getElementById(elemName);
	if(thisElem){
		thisElem.setAttributeNS(null,'stroke-width', value);
	}
}

function elemStrokeColor(mouseEvent, elemName, value) {
	elemName = spaceTrans(elemName);
	var thisElem = document.getElementById(elemName);
	if(thisElem){
		thisElem.setAttributeNS(null,'stroke', value)
	}
}

function elemShow(mouseEvent, elemName) {
	elemName = spaceTrans(elemName);
	var thisElem = document.getElementById(elemName);
	if(thisElem){
		thisElem.setAttributeNS(null,'display', 'inline');
	}
}

function elemHide(mouseEvent, elemName) {
	elemName = spaceTrans(elemName);
	var thisElem = document.getElementById(elemName);
	if(thisElem){
		thisElem.setAttributeNS(null,'display', 'none');
	}
}

function elemColor(mouseEvent, elemName, value) {
	elemName = spaceTrans(elemName);
	var thisElem = document.getElementById(elemName);
	if(thisElem){
		thisElem.setAttributeNS(null,'fill', value);
	}
}



function createGroup(parentNode, id, display, onmouseoverActions, onmouseoutActions){
	
	var group = document.createElementNS(svgNS,"g");
	group.setAttributeNS(null,"display",display);
	group.setAttributeNS(null,"id",id);	
	group.setAttributeNS(null,"onmouseover",onmouseoverActions);	
	group.setAttributeNS(null,"onmouseout",onmouseoutActions);	
	parentNode.appendChild(group);
	return group;
}

function addMenuElement(parentNode, display, x, y , width, height, action, label, onmouseoverActions, onmouseoutActions, onclickActions){
	var Menu = document.createElementNS(svgNS,"g");
	Menu.setAttributeNS(null,"display",display);
	Menu.setAttributeNS(null,"id",action+"Group");
	
	var Rect = document.createElementNS(svgNS,"rect");
	Rect.setAttributeNS(null,"id", action);
	Rect.setAttributeNS(null,"x", x);
	Rect.setAttributeNS(null,"y", y);
	Rect.setAttributeNS(null,"width", width);
	Rect.setAttributeNS(null,"height", height);
	Rect.setAttributeNS(null,"stroke", "blue");
	Rect.setAttributeNS(null,"fill", "black");
	Rect.setAttributeNS(null,"stroke-width", 1);
	
	Menu.appendChild(Rect);
	
	var Text = document.createElementNS(svgNS,"text");
	Text.setAttributeNS(null,"id", action+"Text");
	Text.setAttributeNS(null,"x", x+8);
	Text.setAttributeNS(null,"y", y+13);
	Text.setAttributeNS(null,"font-size", 11);
	Text.setAttributeNS(null,"fill", "white");
	
	var contentText = document.createTextNode(label);
	Text.appendChild(contentText);
	
	Menu.appendChild(Text);

	var MouseRect = document.createElementNS(svgNS,"rect");
	MouseRect.setAttributeNS(null,"onmouseover", onmouseoverActions);
	MouseRect.setAttributeNS(null,"onmouseout" , onmouseoutActions);
	MouseRect.setAttributeNS(null,"onclick" , onclickActions);
	MouseRect.setAttributeNS(null,"x" , x );
	MouseRect.setAttributeNS(null,"y" , y );
	MouseRect.setAttributeNS(null,"width" , width );
	MouseRect.setAttributeNS(null,"height" , height );
	MouseRect.setAttributeNS(null,"style" , "fill:green;fill-opacity:0" );
	Menu.appendChild(MouseRect);
	
	parentNode.appendChild(Menu);
}

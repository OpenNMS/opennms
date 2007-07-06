//set the context menu for a map element
//the parameter elemType says if the element is a MAP or is a NODE
function setContextMenuForElement(evt, elem){
	var elemType = elem.getType();
	if(elemType==NODE_TYPE){
		var id =elem.getNodeId(); 
		var label = elem.getLabel();
		var str = "<menu id='ContextMenu' xmlns='http://foo' >"+
				" <header>OpenNMS IA Context Menu</header> ";
		if(contextMenuEnabled){
			for(index in CM_COMMANDS){
				if(CM_COMMANDS[index]=="-"){
					str+="<separator />";
				}else{
					var link = CM_LINKS[index];
					var params = CM_PARAMS[index];				
					link = link.replace("ELEMENT_ID",""+id);
					link = link.replace("ELEMENT_LABEL",label);
					str+=" <item onactivate='openLink(\""+escape(link)+"\",\""+params+"\");' action='"+CM_COMMANDS[index]+"'>"+unescape(CM_COMMANDS[index])+"</item>";
				}
			}
		}
		str+="</menu>";
		var newMenuRoot = parseXML(str, mapContextMenu).firstChild;
		mapContextMenu.replaceChild( newMenuRoot, mapContextMenu.firstChild );
	}
	if(elemType==MAP_TYPE){ //if is a map, remove the menu, for the moment.
		disableContextMenu(evt);
	}
}


function openLink( link, params){
	open(baseContext+unescape(link), '', params);	
}

//funtion for disabling context menu.
function disableContextMenu(evt){
	evt.preventDefault();
}

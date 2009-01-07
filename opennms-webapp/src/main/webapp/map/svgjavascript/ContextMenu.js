//set the context menu for a map element
//the parameter elemType says if the element is a MAP or is a NODE
function setContextMenuForElement(evt, elem){
	var elemType = elem.getType();
	if(elemType==NODE_TYPE){
		var id =elem.getNodeId(); 
		var label = elem.getLabel();		
		var str = "<menu id='ContextMenu' xmlns='http://mapsContextMenu.openNMS.org' >"+
				" <header>OpenNMS IA Context Menu</header> ";
		if(contextMenuEnabled){
			for(index in CM_COMMANDS){
				if(CM_COMMANDS[index]=="-"){
					var item = document.createElementNS(svgNS,"separator");
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
		if((typeof contextMenu)!="undefined"){
			var newMenuRoot = parseXML(str, contextMenu).firstChild;
			contextMenu.replaceChild( newMenuRoot, contextMenu.firstChild );			
		}

	}
	if(elemType==MAP_TYPE){ //if is a map, remove the menu, for the moment.
		disableContextMenu(evt);
	}
}

//funtion for disabling context menu.
function disableContextMenu(evt){
	var str="<menu>";
        str+="<separator />";
	str+="<menu><header>Zoom</header>";
        str+="<item action='ZoomIn'>Zoom In</item>";
        str+="<item action='ZoomOut'>Zoom Out</item>";
        str+="<item action='OriginalView'>Original View</item>";
        str+="</menu>";
	str+="</menu>";

	if((typeof contextMenu)!="undefined"){
                        var newMenuRoot = parseXML(str, contextMenu).firstChild;
                        contextMenu.replaceChild( newMenuRoot, contextMenu.firstChild );
                }
	//evt.preventDefault();
}

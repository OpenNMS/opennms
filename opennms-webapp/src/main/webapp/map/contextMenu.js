//set the context menu for a map element
//the parameter elemType says if the element is a MAP or is a NODE
function setContextMenuForElement(evt, elem){
	var elemType = elem.getType();
	if(elemType==NODE_TYPE){
		var pingLink = "response/ping.jsp?node="+elem.getNodeId();
		var id =elem.getNodeId(); 
		var tracerouteLink = "response/traceroute.jsp?node=";
		var str = "<menu id='ContextMenu' xmlns='http://foo' >"+
				" <header>OpenNMS IA Context Menu</header> "+
				" <item onactivate='openPing("+id+");' action='Ping'>Ping</item>"+
				" <item onactivate='openTraceroute("+id+");' action='Traceroute'>Traceroute</item>"+
			     "</menu>";
			   
		var newMenuRoot = parseXML(str, mapContextMenu).firstChild;
		mapContextMenu.replaceChild( newMenuRoot, mapContextMenu.firstChild );
	}
	if(elemType==MAP_TYPE){ //if is a map, remove the menu, for the moment.
		disableContextMenu(evt);
	}
}

function openPing(id){
	open(baseContext+'response/ping.jsp?node='+id, '', 'toolbar,width=300,height=300, left=0, top=0, scrollbars=1, resizable=1');	
}

function openTraceroute(id){
	open(baseContext+'response/traceroute.jsp?node='+id, '', 'toolbar,width=300,height=300, left=0, top=0, scrollbars=1, resizable=1');	
}
//funtion for disabling context menu.
function disableContextMenu(evt){
	evt.preventDefault();
}
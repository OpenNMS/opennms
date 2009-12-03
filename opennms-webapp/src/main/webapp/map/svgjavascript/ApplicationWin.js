function windowsClean() {
	var obj, ls;
	ls = winSvgElement.childNodes;
	while (ls.length > 0) {
	  obj = ls.item(0);
	  winSvgElement.removeChild(obj);
	}		
}

function ciao() {
	return;
}

function execSelectedCMAction(index,nodeid,nodelabel,evt) {
	if(CM_COMMANDS[index]){
		var link = CM_LINKS[index];
		var params = CM_PARAMS[index];				
		link = link.replace("ELEMENT_ID",""+nodeid);
		link = link.replace("ELEMENT_LABEL",nodelabel);
		link = link.replace("ELEMENT_HOSTNAME",nodeidSortAss[nodeid].getLabel());
		link = link.replace("ELEMENT_IP",nodeidSortAss[nodeid].getIpAddr());
		openLink(escape(link),params);
	} else {
		alert("Windows Menu Command Error");
	}
}


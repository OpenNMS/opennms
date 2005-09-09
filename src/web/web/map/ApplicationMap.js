// == ApplicationMap.js -- Copyright (C) Michele Masullo ========================
// xlink namespace
function ApplicationMap()
{
	this.svgnsXLink = "http://www.w3.org/1999/xlink";
}

// returns the mouse coordinates as an SVGPoint
ApplicationMap.prototype.getMouse = function(evt) {
	var position = svgRoot.createSVGPoint();
	position.x = evt.clientX;
	position.y = evt.clientY;
	return position;
}


// create reference to svgDocument and to outermost svg element
if ( window.svgDocument == null )
	svgDocument = evt.target.ownerDocument;
svgRoot = svgDocument.documentElement;
var application = new ApplicationMap();





/* *************** functions for the management of the client application map ********************************************** */
		
		//clear the space reserved for some info to view at the top of the info space
		function clearTopInfo(){
			if(selNodes!=null){
				if(selNodes.exists==true){
					selNodes.removeSelectionList();
					selectedMapElemInList=0;
					}
			}
			if(selMaps!=null){
				if(selMaps.exists==true){
					selMaps.removeSelectionList();
					selectedMapInList=0;
					}
			}
			
			if(selBGImages!=null){
				if(selBGImages.exists==true){
					map.render();
					selBGImages.removeSelectionList();
					selectedBGImageInList=0;
					}
			}			
			
			if(selMEIcons!=null){
				if(selMEIcons.exists==true){
					selMEIcons.removeSelectionList();
					selectedselMEIconInList=0;
					}
			}			
			
			var childNode = svgDocument.getElementById("TopInfoText");
			if (childNode)
				svgDocument.getElementById("TopInfo").removeChild(childNode);
				
			childNode = svgDocument.getElementById("RenameMapBox");
			if (childNode)
				svgDocument.getElementById("TopInfo").removeChild(childNode);
			childNode = svgDocument.getElementById("NodeRangeBox");
			if (childNode)
				childNode.parentNode.removeChild(childNode);
			
		} 
		
		//clear the space reserved for some info to view at the down of the info space
		function clearDownInfo(){
			var childNode = svgDocument.getElementById("DownInfoText");
			if (childNode)
				childNode.parentNode.removeChild(childNode);
			close_pick_color(false);
		}

		
		function viewMapInfo(){
			var mapInfo= svgDocument.getElementById("MapInfo");
			mapInfo.appendChild(parseXML("<tspan x=\"3\" dy=\"30\" id=\"mapName\"  font-size=\"9\" >Name: "+currentMapName+" </tspan>",svgDocument));
			mapInfo.appendChild(parseXML("<tspan x=\"3\" dy=\"20\" id=\"mapOwner\" font-size=\"9\">Owner:  "+currentMapOwner+"</tspan>",svgDocument));
			mapInfo.appendChild(parseXML("<tspan x=\"3\" dy=\"20\" id=\"mapUserLast\" font-size=\"9\">User last modifies: "+currentMapUserlast+"</tspan>",svgDocument));
			mapInfo.appendChild(parseXML("<tspan x=\"3\" dy=\"20\" id=\"mapCreateTime\" font-size=\"9\">Create time: "+currentMapCreatetime+"</tspan>",svgDocument));
			mapInfo.appendChild(parseXML("<tspan x=\"3\" dy=\"20\" id=\"mapLastModTime\" font-size=\"9\">Last modified time: "+currentMapLastmodtime+"</tspan>",svgDocument));
			svgDocument.getElementById("MapInfo").getStyle().setProperty('display', 'inline');
		}

		function clearMapInfo(){
			var mapInfo= svgDocument.getElementById("MapInfo");
			var mapNameNode=svgDocument.getElementById("mapName");
			if(mapNameNode!=null)
				mapNameNode.parentNode.removeChild(mapNameNode);
			var mapOwnerNode=svgDocument.getElementById("mapOwner");
			if(mapOwnerNode!=null)
				mapOwnerNode.parentNode.removeChild(mapOwnerNode);	
			var mapUserLastNode=svgDocument.getElementById("mapUserLast");
			if(mapUserLastNode!=null)
				mapUserLastNode.parentNode.removeChild(mapUserLastNode);	
			var mapCreateTimeNode=svgDocument.getElementById("mapCreateTime");
			if(mapCreateTimeNode!=null)
				mapCreateTimeNode.parentNode.removeChild(mapCreateTimeNode);		
			var mapLastModTimeNode=svgDocument.getElementById("mapLastModTime");
			if(mapLastModTimeNode!=null)
				mapLastModTimeNode.parentNode.removeChild(mapLastModTimeNode);		

		}		
		
		function hideMapInfo(){
			var mapInfoElem = svgDocument.getElementById("MapInfo");
			if(mapInfoElem!=null)
				mapInfoElem.getStyle().setProperty('display', 'none');
		}
		
		//clear the actions started (if there are one action started)
		function clearActionsStarted(){
			if(deletingMapElem==true){
				deletingMapElem=false;
			}
			if(addingMapElemNeighbors==true){
				addingMapElemNeighbors=false;
			}
		}
		
		//functions for matching ip with range in input
		function ipmatch(ip, ipLike){
			var ottectsLike = ipLike.split(".");
			var ottectsIp = ip.split(".");
			return (ottectMatch(parseInt(ottectsIp[0]),ottectsLike[0]) && ottectMatch(parseInt(ottectsIp[1]), ottectsLike[1]) && ottectMatch(parseInt(ottectsIp[2]),ottectsLike[2]) && ottectMatch(parseInt(ottectsIp[3]),ottectsLike[3]) )

		}

		function ottectMatch(ott, ottLike){
			//alert(ott+" "+ ottLike);
			try{
			if(ottLike=="*"){
				if(ott<=255 && ott>=0)

				 	return true;

				return false;
			}
			if(ottLike.indexOf("-")>=0){
				var range = ottLike.split("-")
				var start=parseInt(range[0]);
				var end=parseInt(range[1]);
				if(start>end  || start>255 || end >255 ||start<0 || end<0)
					 return false;
				if(ott>=start && ott<=end)
					return true;
				return false;
			}
			if(ott==parseInt(ottLike))
				return true;
			return false;
			}catch(e){
				return false;
			}
		}
		
		function isValidOttect(ott){
			if(ott=="*"){
				return true;
			}
			if(ott.indexOf('-')>=0){
				var ottRange = ott.split('-');
				if(ottRange.length>2)
					return false;
				var start=parseInt(ottRange[0]);
				var end=parseInt(ottRange[1]);
				if(start<=end  && start<=255 && end <=255 && start>=0 && end>=0)
					 return true;
				
			}
			if(parseInt(ott)>=0 && parseInt(ott)<=255)
				return true;
			return false;
		}

		function isValidRange(range){
			var ottects = range.split(".");
			if(ottects.length!=4){
				return false;
			}
			return (isValidOttect(ottects[0]) && isValidOttect(ottects[1]) && isValidOttect(ottects[2]) && isValidOttect(ottects[3]));
		}

		function addRangeOfNodes(){
			var range = svgDocument.getElementById("RangeText").firstChild.nodeValue;
			if(!isValidRange(range)){
				alert('Range not valid!');
				return;
				}
			var mapWidth = svgDocument.getElementById("ApplicationSvg").getAttribute("width");
			var xPosition = 0;
			var yPosition = 10;
			var count = 0;
			var countAlreadyInMap=0;
			for(nodeTmp in nodeSortAss){
				if(nodeSortAss[nodeTmp]!=null){
					var found=false;
					for(iface in nodeSortAss[nodeTmp].interfaces){
						var result = ipmatch(nodeSortAss[nodeTmp].interfaces[iface], range);
						if(result){
							found=true;
						}
					}
					if(found){
						count++;
						if((count%2)>0)
							yPosition+=15;
						else yPosition-=15;
						if(xPosition>=(mapWidth-100)){
							xPosition=0;
							yPosition+=70;	
						}
						xPosition+=70;					
						if(map.addElement(nodeSortAss[nodeTmp].id, nodeSortAss[nodeTmp].icon, nodeSortAss[nodeTmp].label, getSemaphoreColorForNode(nodeSortAss[nodeTmp].status, nodeSortAss[nodeTmp].avail),  xPosition, yPosition, false,nodeSortAss[nodeTmp].status,nodeSortAss[nodeTmp].avail)==null)
						{
							xPosition-=70;
							count--;
							countAlreadyInMap++;
						}else{
							var lnks = nodeSortAss[nodeTmp].links;
							for(l in lnks){
								var linkedElem = lnks[l];
								// add links to nodes map
								for(var n in map.nodes){
									if(map.nodes[n].isMap()){
										var nodeMap = map.nodes[n];
										for(var nn in nodeMap.nodes){
											if(nodeMap.nodes[nn]==linkedElem){
												map.addLink(nodeMap.id, nodeSortAss[nodeTmp].id, "green", 1);
												break;
											}
										}
									}
								}								
								map.addLink(nodeSortAss[nodeTmp].id, linkedElem, "green", 1);

							}							
						}
					}
				}
			}	
			map.render();	
			clearTopInfo();
			clearDownInfo();
			var childNode = svgDocument.getElementById("DownInfoText");
			if (childNode)
				svgDocument.getElementById("DownInfo").removeChild(childNode);		
			var str = "<text id=\"DownInfoText\" x=\"5\" y=\"20\">"+count+" Nodes added.";
			if(countAlreadyInMap>0){
				str+="<tspan x=\"5\" dy=\"15\">"+countAlreadyInMap+" already present on map.</tspan>";
			}
			str+="</text>";
			svgDocument.getElementById("DownInfo").appendChild(parseXML(str,svgDocument));	
		}
		
		
		function addMapElemNeigh(id){
			var mapWidth = svgDocument.getElementById("ApplicationSvg").getAttribute("width");
			
			//gets the node from the nodeSortAss
			var node=null;
			for(nodeTmp in nodeSortAss){
				if(nodeSortAss[nodeTmp]!=null){
					if(nodeSortAss[nodeTmp].id == id){
						node = nodeSortAss[nodeTmp];
					}
				}
			}	
			
			if(node==null){
				return;
			}
			var lnks = node.links;
			var xPosition = 0;
			var yPosition = 10;
			var count = 0;
			// find all linked elements
			for(l in lnks){
				var elemLinkedId = lnks[l];
				if(id!=elemLinkedId){
					var currNode=null;
					for(nodeTmp in nodeSortAss){
						if(nodeSortAss[nodeTmp]!=null){
							if(nodeSortAss[nodeTmp].id == elemLinkedId){
								currNode = nodeSortAss[nodeTmp];
								break;
							}
						}
					}
					if(currNode!=null){
						count++;
						if((count%2)>0)
							yPosition+=15;
						else yPosition-=15;
						if(xPosition>=(mapWidth-100)){
							xPosition=0;
							yPosition+=70;	
						}
						xPosition+=70;
						//alert(node.id);
						if(map.addElement(currNode.id, currNode.icon, currNode.label, getSemaphoreColorForNode(currNode.status, currNode.avail),  xPosition, yPosition, false,currNode.status,currNode.avail)==null)
							xPosition-=70;
						map.addLinks(currNode.id);
					}
				}
			}
			map.render();
		}
		
		
		function addMapElement(){
			if(selectedMapElemInList!=0){
					
					map.addElement(nodeSortAss[selectedMapElemInList].id, nodeSortAss[selectedMapElemInList].icon, nodeSortAss[selectedMapElemInList].label, getSemaphoreColorForNode(nodeSortAss[selectedMapElemInList].status,nodeSortAss[selectedMapElemInList].avail),  70, 10, true, nodeSortAss[selectedMapElemInList].status,nodeSortAss[selectedMapElemInList].avail);
					//map.render();
					var lnks = nodeSortAss[selectedMapElemInList].links;
					for(l in lnks){
						var linkedElem = lnks[l];
						// add links to nodes map
						for(var n in map.nodes){
							if(map.nodes[n].isMap()){
								var nodeMap = map.nodes[n];
								for(var nn in nodeMap.nodes){
									if(nodeMap.nodes[nn]==linkedElem){
										map.addLink(nodeMap.id, nodeSortAss[selectedMapElemInList].id, "green", 1);
										break;
									}
								}
							}
						}
						map.addLink(nodeSortAss[selectedMapElemInList].id, linkedElem, "green", 1);
						
					}
					map.render();
				}
			}
			
		
			
		function addMapElementWithNeighbors(){
		
			var mapWidth = svgDocument.getElementById("ApplicationSvg").getAttribute("width");
			if(selectedMapElemInList!=0){
			  		//add the selected element
					if(map.addElement(nodeSortAss[selectedMapElemInList].id, nodeSortAss[selectedMapElemInList].icon, nodeSortAss[selectedMapElemInList].label, getSemaphoreColorForNode(nodeSortAss[selectedMapElemInList].status,nodeSortAss[selectedMapElemInList].avail),  70, 10, true,nodeSortAss[selectedMapElemInList].status,nodeSortAss[selectedMapElemInList].avail)==null) {
						var childNode = svgDocument.getElementById("DownInfoText");		
						if (childNode)
							svgDocument.getElementById("DownInfo").removeChild(childNode);		
						svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">" +
							"<tspan x=\"5\" dy=\"0\">Element already present</tspan>" +
							"<tspan x=\"7\" dy=\"15\">in map.</tspan>" +
						"</text>",svgDocument));
						
						return;
					}
					var lnks = nodeSortAss[selectedMapElemInList].links;
					//add the links from the node added to the nodes in the map

				
					var xPosition = 70;
					var yPosition = 10;
					var count=0;
					// find all linked elements
					for(l in lnks){
						var elemLinkedId = lnks[l];
						
						if(nodeSortAss[selectedMapElemInList].id!=elemLinkedId){
							map.addLinks(nodeSortAss[selectedMapElemInList].id);							
							var node=null;
							for(nodeTmp in nodeSortAss){
								if(nodeSortAss[nodeTmp]!=null){
									if(nodeSortAss[nodeTmp].id == elemLinkedId){
										node = nodeSortAss[nodeTmp];
									}
								}
							}
							if(node!=null){
								count++;
								if((count%2)>0)
									yPosition+=15;
								else yPosition-=15;
								if(xPosition>=(mapWidth-100)){
									xPosition=0;
									yPosition+=70;	
								}
								xPosition+=70;
								
								if(map.addElement(node.id, node.icon, node.label, getSemaphoreColorForNode(node.status,node.avail),  xPosition, yPosition, false, node.status,node.avail)==null)
									xPosition-=70;							
								map.addLinks(node.id);
							}
						}
					}
					map.render();
				}
			}
			
		//create a string representing the status of the map
		// in the moment in wich is invoked. This function is used to test if the 
		// map is bean modified since last saving.	
		function getMapString()
		{
		var query="Nodes=";
		var count=0;

		//construct the query to post to the servlet. (nodes are formatted as follows: id1,x1,y1-id2,x2,y2 ...) 
		for (elem in map.nodes){
			if(count>0)
				query+="-";
			var elem = map.nodes[elem];
			query+= elem+","+elem.x+","+elem.y+","+elem.icon;
			count++;
			}
		//the map is formatted as follows: id,x,y,image
		query+="&MapId="+currentMapId+"&MapName="+currentMapName+"&MapBackground="+currentMapBackGround;			
		return query;
		}
		
		function getAvailColor(avail){
			if(avail<0)
				return A_UNKNOWN_COLOR;
			if(avail>=95){
			if(avail<99)
				return A_GOOD_COLOR;
			else
				return A_BEST_COLOR;
			}
			else
				return A_BAD_COLOR;
		}
		
		function getStatusColor(status){
				if(status=='A'){
					return S_ACTIVE_COLOR;
				}			
				if(status=='D'){
					return S_DELETED_COLOR;
				}			
				if(status=='O'){
					return S_OUTAGED_COLOR;
				}
				if(status=='U'){
					return S_UNKNOWN_COLOR;
				}				
		}
		
		function getSemaphoreColorForNode(status, avail){
			if(colorSemaphoreBy=="A"){
				return getAvailColor(avail);
			}else{
			        return getStatusColor(status);
			}
		}
		
		// refresh nodes' attributes:  status, availability, links to other nodes.
		function refreshMapElements(){
			if(currentMapId!=MAP_NOT_OPENED && map.nodes!=null){
					map.deleteAllLinks();
					
					for(var mapNode in map.nodes){
						map.addLinks(map.nodes[mapNode].id);
						for(var nodeInList in nodeSortAss){
							if(nodeSortAss[nodeInList]!=null){
								if(map.nodes[mapNode].id == nodeSortAss[nodeInList].id){
									//alert(map.nodes[mapNode].id);
									map.nodes[mapNode].setSemaphoreColor(getSemaphoreColorForNode(nodeSortAss[nodeInList].status, nodeSortAss[nodeInList].avail));
									map.nodes[mapNode].status=nodeSortAss[nodeInList].status;
									map.nodes[mapNode].avail=nodeSortAss[nodeInList].avail;
								}
							}
						}
						if(map.nodes[mapNode].isMap()){
							var status = "U";
							var avail = -0.001;
							var countNodes=0;
							for(var n in map.nodes[mapNode].nodes){
								var nd = map.nodes[mapNode].nodes[n];
								for(var tmpNode in nodeSortAss){
									if(nodeSortAss[tmpNode]!=null){								
										if(nodeSortAss[tmpNode].id==nd){
											countNodes++;
											if(avail<0)
												avail=0;
											avail+=parseFloat(nodeSortAss[tmpNode].avail);
											
											if(status=='U'){
												if(nodeSortAss[tmpNode].status!='D')
													status=nodeSortAss[tmpNode].status;
												}else{
													if(nodeSortAss[tmpNode].status=="O"){
														status='O';
													}
												}													
											}
										}
									}
								}
							
							if(countNodes>0){
								avail=avail/countNodes;
							}
							map.nodes[mapNode].setSemaphoreColor(getSemaphoreColorForNode(status, avail));
							map.nodes[mapNode].status=status;
							map.nodes[mapNode].avail=avail;		
						}
					}
			map.render();
			}
		}		
		
		// *************** classes for the definition of the "getSelectionListVal" function ******
		// invoked when an element of the list is selected
		
		var selectedMapElemInList=0;
		
		function nodesResult() { }
		
		nodesResult.prototype.getSelectionListVal = function(selBoxName,nodeNr,arrayVal) {
			clearDownInfo();
			svgDocument.getElementById("DownInfo").appendChild(nodeSortAss[arrayVal].getInfo());		
			selectedMapElemInList=arrayVal;
		
		}
		
		var selectedMapInList=0;
		
		function mapsResult() { }
		
		mapsResult.prototype.getSelectionListVal = function(selBoxName,mapNr,arrayVal) {
			clearDownInfo();
			svgDocument.getElementById("DownInfo").appendChild(mapSortAss[arrayVal].getInfo());		
			selectedMapInList=arrayVal;
		
		}
		
		
		var selectedBGImageInList=0;
		
		function BGImagesResult() { }
		
		BGImagesResult.prototype.getSelectionListVal = function(selBoxName,mapNr,arrayVal) {
			clearDownInfo();
			map.tryBackgroundImage(BGImagesSortAss[arrayVal]);
			selectedBGImageInList=arrayVal;
		
		}	
		
		
		var selectedMEIconInList=0;
		
		function MEIconsResult() { }
		
		MEIconsResult.prototype.getSelectionListVal = function(selBoxName,mapNr,arrayVal) {
			var iconPreviewNode = svgDocument.getElementById("iconPreview");		
			if (iconPreviewNode)
				iconPreviewNode.parentNode.removeChild(iconPreviewNode);
				
			if(arrayVal!=""){
			settingMapElemIcon=true;
			selectedMEIconInList=arrayVal;
			

			var iconPreviewGroup = document.createElementNS(svgNS,"g");
			iconPreviewGroup.setAttributeNS(null,"id", "iconPreview");
			
			var iconPreviewRect = document.createElementNS(svgNS,"rect");
			iconPreviewRect.setAttributeNS(null,"x", 57);
			iconPreviewRect.setAttributeNS(null,"y", 65);
			iconPreviewRect.setAttributeNS(null,"width", 35);
			iconPreviewRect.setAttributeNS(null,"height", 35);
			iconPreviewRect.setAttributeNS(null,"fill", "white");
			iconPreviewRect.setAttributeNS(null,"stroke", "grey");
			iconPreviewRect.setAttributeNS(null,"stroke-width", 1);
			
			
			var iconPreview = document.createElementNS(svgNS,"image");
			iconPreview.setAttributeNS(null,"x", 65);
			iconPreview.setAttributeNS(null,"y", 70);
			iconPreview.setAttributeNS(null,"width", 20);
			iconPreview.setAttributeNS(null,"height", 25);
			iconPreview.setAttributeNS(application.svgnsXLink, "xlink:href",MEIconsSortAss[arrayVal] );
			iconPreviewGroup.appendChild(iconPreviewRect);
			iconPreviewGroup.appendChild(iconPreview);
			selMEIcons.selectionBoxGroup.appendChild(iconPreviewGroup);
			
			var childNode = svgDocument.getElementById("DownInfoText");		
			if (childNode)
				svgDocument.getElementById("DownInfo").removeChild(childNode);		
			svgDocument.getElementById("DownInfo").appendChild(parseXML("<text id=\"DownInfoText\" x=\"5\" y=\"20\">" +
				"<tspan x=\"5\" dy=\"0\">Click on the element to set</tspan>" +
				"<tspan x=\"7\" dy=\"15\">the icon to.</tspan>" +
			"</text>",svgDocument));
			}
			else{
				clearDownInfo();
			}
			
		
		}		
		

		// ************************************************************************
		
		
		
// variable used only locally to this set of functions
var selectedMapInList=0;
var selMaps;

// Menu Svg Object Basic Functions	
function instantiateRWAdminMenu(){
	mapMenu.removeChilds();
	instantiateMapGroupAdminMode();
	nodeMenu.removeChilds();
	instantiateNodeGroup();	
	refreshMenu.removeChilds();
	instantiateRefreshGroupAdminMode();
	mapMenu.activate();
	nodeMenu.activate();
	refreshMenu.activate();
}
 
function instantiateRWNormalMenu(){
	mapMenu.removeChilds();
	instantiateMapGroupNormalMode();

	viewMenu.removeChilds();
	instantiateViewGroup();

	refreshMenu.removeChilds();
	instantiateRefreshGroupNormalMode();

	mapMenu.activate();
	viewMenu.activate();
	refreshMenu.activate();
}

function instantiateROMenu(){
	mapMenu.removeChilds();
	instantiateMapGroup();	

	viewMenu.removeChilds();
	instantiateViewGroup();

	refreshMenu.removeChilds();
	instantiateRefreshGroupNormalMode();

	mapMenu.activate();
	viewMenu.activate();
	refreshMenu.activate();
}
	
function enableMenu(){
 	if(isUserAdmin){
 		if (isAdminMode) {
 			instantiateRWAdminMenu();
 		} else {
			instantiateRWNormalMenu();
		}
	}else{
		instantiateROMenu();	
	}
}

function disableMenu(){
	mapMenu.deactivate();
	nodeMenu.deactivate();
	viewMenu.deactivate();
	refreshMenu.deactivate();
}

function resetWorkPanel(menuName, menuOpening){
	windowsClean();

	if (menuOpening) {
		hideMapInfo();
		hideHistory();
	} else {
		showMapInfo();
		showHistory();
	}
	if(mapMenuId != menuName)
		mapMenu.close();
	
	if(nodeMenuId != menuName)
		nodeMenu.close();
		
	if(viewMenuId != menuName)
		viewMenu.close();

	if(refreshMenuId != menuName)
		refreshMenu.close();
}

function closeAllMenu(){
	mapMenu.close();
	nodeMenu.close();
	viewMenu.close();
	refreshMenu.close();
}

function instantiateMapGroupAdminMode() {
	// ***ADMIN MODE MAP MENU ***
	var itemId="SetBG";
	var itemEl1id = "SetBGColor";
	var itemEl2id = "SetBGImage";
	var subitems = new Array(itemEl1id,itemEl2id);
	var id = "New";
	mapMenu.addElement(id, id, menuDeltaX,menuDeltaY,menuWidth,menuHeight, newMapSetUp, subitems);
	id="Open";
	mapMenu.addElement(id, id, menuDeltaX,2*menuDeltaY,menuWidth,menuHeight, addMapsList, subitems);
	id="Close";
	mapMenu.addElement(id, id, menuDeltaX,3*menuDeltaY,menuWidth,menuHeight, closeSetUp, subitems);
	id="Rename";
	mapMenu.addElement(id, id, menuDeltaX,4*menuDeltaY,menuWidth,menuHeight, addRenameMapBox, subitems);
	id="Delete";
	mapMenu.addElement(id, id, menuDeltaX,5*menuDeltaY,menuWidth,menuHeight, deleteMapSetUp, subitems);
	id="Save";
	mapMenu.addElement(id, id, menuDeltaX,6*menuDeltaY,menuWidth,menuHeight, saveMapSetUp, subitems);
	id="Clear";
	mapMenu.addElement(id, id, menuDeltaX,7*menuDeltaY,menuWidth,menuHeight, clearMapSetUp, subitems);

	mapMenu.addItem(itemId, "Background",menuDeltaX,8*menuDeltaY,menuWidth,menuHeight, subitems);	
	id="RefreshMode";
	mapMenu.addElement(id, "Refresh Mode", menuDeltaX,9*menuDeltaY,menuWidth,menuHeight, switchRoleSetUp, subitems);

	mapMenu.addItemElement(itemEl1id, "Color", menuWidth+menuDeltaX,8*menuDeltaY,menuWidth,menuHeight, addBgColorBox);
	mapMenu.addItemElement(itemEl2id, "Image", menuWidth+menuDeltaX,9*menuDeltaY,menuWidth,menuHeight, addBGImagesList);
}

function instantiateMapGroupNormalMode() {
	// *** MAP MENU FOR ADMINISTRATOR***
	instantiateMapGroup();
	id="AdminMode";
	mapMenu.addElement(id, "Admin Mode", menuDeltaX,4*menuDeltaY,menuWidth,menuHeight, switchRoleSetUp,null);
}

function instantiateMapGroup() {
	// ***READ ONLY MAP MENU ***
	var id="Open";
	mapMenu.addElement(id, id, menuDeltaX,menuDeltaY,menuWidth,menuHeight, addMapsList,null);
	id="Close";
	mapMenu.addElement(id, id, menuDeltaX,2*menuDeltaY,menuWidth,menuHeight, closeSetUp,null);
	id="Search";
	mapMenu.addElement(id, id, menuDeltaX,3*menuDeltaY,menuWidth,menuHeight, addSearchMapList,null);	
}

function instantiateRefreshGroupAdminMode() {
	// *** REFRESH MENU ***
	var id = "LoadMaps";
	refreshMenu.addElement(id, "Maps", menuDeltaX,menuDeltaY,menuWidth,menuHeight, loadMapsSetUp,null);
	id = "LoadNodes";
	refreshMenu.addElement(id, "Nodes", menuDeltaX,2*menuDeltaY,menuWidth,menuHeight, loadNodesSetUp,null);
}

function instantiateRefreshGroupNormalMode() {
	// *** REFRESH MENU ***
	var id = "RefreshMap";
	refreshMenu.addElement(id, "Refresh", menuDeltaX,menuDeltaY,menuWidth,menuHeight, refreshNodesSetUp,null);
}

function instantiateNodeGroup() {
	// *** NODE MENU ***
	var id = "Add";
	nodeMenu.addElement(id, id, menuDeltaX,menuDeltaY,menuWidth,menuHeight, addMapElementList,null);
	id = "AddByCategory";
	nodeMenu.addElement(id, "Add Category", menuDeltaX,2*menuDeltaY,menuWidth,menuHeight, addCategoryList,null);
	id = "AddByLabel";
	nodeMenu.addElement(id, "Add Label", menuDeltaX,3*menuDeltaY,menuWidth,menuHeight, addNodeLabelBox,null);
	id = "AddRange";
	nodeMenu.addElement(id, "Add Range", menuDeltaX,4*menuDeltaY,menuWidth,menuHeight, addRangeBox,null);
	id = "AddNeigh";
	nodeMenu.addElement(id, "Add Neighs", menuDeltaX,5*menuDeltaY,menuWidth,menuHeight, addMapElementNeigh,null);
	id = "AddNodeNeigh";
	nodeMenu.addElement(id, "Add With Neighs", menuDeltaX,6*menuDeltaY,menuWidth,menuHeight, addMapElementNeighList,null);
	id = "AddMap";
	nodeMenu.addElement(id, "Add Map", menuDeltaX,7*menuDeltaY,menuWidth,menuHeight, addMapAsNodeList,null);
	id = "SetIcon";
	nodeMenu.addElement(id, "Set Icon", menuDeltaX,8*menuDeltaY,menuWidth,menuHeight, addIconList,null);
	id = "DelNode";
	nodeMenu.addElement(id, "Delete", menuDeltaX,9*menuDeltaY,menuWidth,menuHeight, deleteMapElementList,null);
	if (hasHideNodes) {
		id = "AddHidesNodes";
		nodeMenu.addElement(id, "Add Hidden Nodes", menuDeltaX,10*menuDeltaY,menuWidth,menuHeight, addHideNodes,null);
	}
	if (hasHideMaps) {
		id = "AddHidesMaps";
		nodeMenu.addElement(id, "Add Hidden Maps", menuDeltaX,10*menuDeltaY,menuWidth,menuHeight, addHideMaps,null);
	}
}

function instantiateViewGroup() {

	// *** VIEW MENU ***		
	var itemId="ColorNodesBy";
	var itemEl1id="ColorNodesBySeverity";
	var itemEl2id="ColorNodesByAvail";	
	var itemEl3id="ColorNodesByStatus";
	var subitems= new Array(itemEl1id,itemEl2id,itemEl3id);
	var id="ToggleFullScreen";
	viewMenu.addElement(id, "Toggle Screen", menuDeltaX,menuDeltaY,menuWidth,menuHeight,toggleScreenSetUp, subitems);
	id = "SetDimension";	
	viewMenu.addElement(id, "Set Dimension", menuDeltaX,2*menuDeltaY,menuWidth,menuHeight,addDimensionList, subitems);
	viewMenu.addItem(itemId, "View by...", menuDeltaX,3*menuDeltaY,menuWidth,menuHeight, subitems);
	viewMenu.addItemElement(itemEl1id, "Severity", menuDeltaX+menuWidth,3*menuDeltaY,menuWidth,menuHeight, viewBySeveritySetUp);
	viewMenu.addItemElement(itemEl2id,    "Avail", menuDeltaX+menuWidth,4*menuDeltaY,menuWidth,menuHeight, viewByAvailSetUp);
	viewMenu.addItemElement(itemEl3id,   "Status", menuDeltaX+menuWidth,5*menuDeltaY,menuWidth,menuHeight,viewByStatusSetUp);
	
}

// ***********functions called by Map Menu ************************
// New Map
function newMapSetUp() {	
	closeAllMenu();
	if (verifyMapString()) return;
	hideMapInfo();
	hideHistory();
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();
	disableMenu();
	newMap();
}

// Search Map
function addSearchMapList()
{
	closeAllMenu();
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();
	textbox1 = new textbox("textbox1","textboxwithcommand","",textboxmaxChars,textboxx,textboxy,textboxWidth,textboxHeight,textYOffset,textStyles,boxStyles,cursorStyles,seltextBoxStyles,"",filterSearchMapSelectionList);
	selMaps = new selectionList("maps","maps",mapLabels,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,mymapsResult);
	selMaps.sortList("asc");
	selMaps.selectElementByPosition(0, false);
	button1  = new button("button1","maps",searchMapSetUp,"rect","Open",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
}

function filterSearchMapSelectionList(textboxId,value,changeType) {
		
		if (changeType == "change") {
			var matchingMaps = new Array(); 
			for(var i=0;i<nodeLabels.length;i++) {
			    var nodeLabel = nodeLabels[i];
				if (nodeLabel.indexOf(value) >= 0) {
				    var mapLbl = nodeLabelMap[nodeLabel];
				    if (mapLbl!=undefined) {
						for (var j=0; j<mapLbl.length;j++ ){
					    	matchingMaps.push(mapLbl[j]);
						}
					}
				}  
			}
			var elementInList = mapLabels.length;
			for(var k=1;k<mapLabels.length;k++) {
				var mapLabel = mapLabels[k];
				var match = true;
				for(var l=0;l<matchingMaps.length;l++) {
					if (matchingMaps[l] == mapLabel ) {
					   match = false;
					   break;
					}
				}
				if  ( match ) {
					elementInList--;
					if (selMaps.elementExists(mapLabel) >= 0) 
						selMaps.deleteElement(mapLabel);
				} else if (selMaps.elementExists(mapLabel) == -1 && match >= 0) {
					selMaps.addElementAtPosition(mapLabel,k);
				}
			}
		}
}

function searchMapSetUp(mapId)
{
	if (verifyMapString()) return;

	if (selectedMapInList == 0) {
		var elems = new String();
		var first = true;
		for ( var i = 1; i< selMaps.elementsArray.length; i++ ) {
			if (first) {
				elems = mapSortAss[selMaps.elementsArray[i]].id;
				first = false;
			} else {
				elems = elems + "," + mapSortAss[selMaps.elementsArray[i]].id;
			}
		}
		windowsClean();
		clearTopInfo();
		clearDownInfo();
		searchMap(elems);
		
	} else {
		openMapSetUp(mapId);
	}
}

// Open Map
function addMapsList()
{
	closeAllMenu();
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();
	textbox1 = new textbox("textbox1","textboxwithcommand","",textboxmaxChars,textboxx,textboxy,textboxWidth,textboxHeight,textYOffset,textStyles,boxStyles,cursorStyles,seltextBoxStyles,"",filterMapSelectionList);
	selMaps = new selectionList("maps","maps",mapLabels,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,mymapsResult);
	selMaps.sortList("asc");
	selMaps.selectElementByPosition(1, true);
	button1  = new button("button1","maps",openMapSetUp,"rect","Open",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
}

function filterMapSelectionList(textboxId,value,changeType) {
		if (changeType == "change") {
			var elementInList = mapLabels.length;
			for(var k=1;k<mapLabels.length;k++) {
				var mapLabel = mapLabels[k];
				var match = mapLabel.indexOf(value);
				if  ( match == -1 ) {
					elementInList--;
					if (selMaps.elementExists(mapLabel) >= 0) 
						selMaps.deleteElement(mapLabel);
				} else if (selMaps.elementExists(mapLabel) == -1 && match >= 0) {
					selMaps.addElementAtPosition(mapLabel,k);
				}
			}
			if (elementInList > 1 )
				selMaps.selectElementByPosition(1,true);
			else 
				selMaps.selectElementByPosition(0,false);
		}
}
// This function is called by open map and add map as node!
function mapsResult() { }

mapsResult.prototype.getSelectionListVal = function(selBoxName,mapNr,arrayVal) {
	if(mapNr!=0){
		writeDownInfo3("Selected Map Info", "Name: " +mapSortAss[arrayVal].getName(), "Owner: " + mapSortAss[arrayVal].getOwner() ); 
	}
	selectedMapInList=arrayVal;
}

function openMapSetUp(mapId) {
	if (verifyMapString()) return;

	var mapToOpen;
	if(mapId != undefined && mapId > 0){
		mapToOpen = mapId;
	}else if(selectedMapInList != undefined && mapSortAss[selectedMapInList].id > 0){
		mapToOpen = mapSortAss[selectedMapInList].id;		
	}else{
		alert("No maps to open");
		return;
	}
	windowsClean();
	clearTopInfo();
	clearDownInfo();
	openMap(mapToOpen);	
}

// Close Map
function closeSetUp() {	
	closeAllMenu();
	if(currentMapId==MAP_NOT_OPENED){
		alert("No maps opened");
		return;
	}
	
	if (verifyMapString()) return;
	
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();
	disableMenu();
	close();
}

// Rename Map
function addRenameMapBox(){
	closeAllMenu();
	if(currentMapType == "U" && currentMapId!=MAP_NOT_OPENED){
		clearTopInfo();
		clearDownInfo();
		hidePickColor();
		resetFlags();

		//first a few styling parameters:
		textbox1 = new textbox("textbox1","textboxwithcommand",currentMapName,textboxmaxChars,textboxx,textboxy,textboxWidth,textboxHeight,textYOffset,textStyles,boxStyles,cursorStyles,seltextBoxStyles,"",undefined);
		button1  = new button("button1","textboxwithcommand",renameMap,"rect","Rename",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);        
 	} else if (currentMapType == "A") {
		alert('Cannot rename automatic map');
 	} else if (currentMapType == "S" && currentMapId!=MAP_NOT_OPENED) {
		clearTopInfo();
		clearDownInfo();
		hidePickColor();
		resetFlags();

		//first a few styling parameters:
		textbox1 = new textbox("textbox1","textboxwithcommand",currentMapName,textboxmaxChars,textboxx,textboxy,textboxWidth,textboxHeight,textYOffset,textStyles,boxStyles,cursorStyles,seltextBoxStyles,"",undefined);
		button1  = new button("button1","textboxwithcommand",renameMap,"rect","Rename",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);        
	}else{
		alert('No maps opened');
    }
}

function renameMap(){
	var newMapName = getTextBoxValue();
	clearTopInfo();
	if(newMapName != null && trimAll(newMapName)!=""){
		currentMapName=newMapName;
		writeMapInfo();
		writeDownInfo("Map renamed.");
	} else {
		writeDownInfo("Name not valid.");
	}
}

// Delete Map
function deleteMapSetUp() {	
	closeAllMenu();
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();
	if(currentMapType == "U" && currentMapId!=MAP_NOT_OPENED && currentMapId!=NEW_MAP){
	    if(confirm('Are you sure to delete the map?')==true){ 
	 		disableMenu();
			deleteMap();
    	}
 	} else if (currentMapType == "A") {
		alert('Cannot delete automatic map');
 	} else if (currentMapType == "S" && currentMapId!=MAP_NOT_OPENED && currentMapId!=NEW_MAP ) {
	    if(confirm('Are you sure to delete static the map?')==true){ 
	 		disableMenu();
			deleteMap();
    	}
 	} else if (currentMapType == "A") {
		alert('Cannot delete automatic map');
 	} else if (currentMapType == "S") {
		alert('Cannot delete static map');
	}else{
		alert('No maps to delete found');
    }	
}

// Save Map
function saveMapSetUp() {	
	closeAllMenu();
	if(currentMapId!=MAP_NOT_OPENED){
		clearTopInfo();
		disableMenu();
		writeDownInfo("Saving map '" +currentMapName+"'");
		resetFlags();
		saveMap();
	}else{
		alert("No maps opened");
	}
	
}

// Clear Map
function clearMapSetUp() {	
	closeAllMenu();
	hidePickColor();
	resetFlags();
	if(currentMapId!=MAP_NOT_OPENED) {
	    if(confirm('Are you sure to clear the map (remove all its elements and links)?')==true) {
			clearTopInfo();
			disableMenu();
			writeDownInfo("Clearing map '" +currentMapName+"'");
			clearMap();
     	}
	}else{
		alert("No maps opened");
	}	
}

// Map Switch role
function switchRoleSetUp(){
	closeAllMenu();
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();
	disableMenu();
	switchRole();
}

// Set map background Color
function addBgColorBox(){
	closeAllMenu();
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	if(currentMapId!=MAP_NOT_OPENED){
		showPickColor();
	}else{
		alert('No maps opened');
    }
}


function close_pick_color(setColor)
{
	if(setColor==true){
		currentMapBackGround=pick_color.split("#")[1];
		map.setBackgroundColor(pick_color);
		map.setBGvalue(pick_color);
	}
	if(pick_prefixe!=null){
		remove_pick_color(pick_prefixe);
	}
}

// Map Background Image List
function addBGImagesList()
{
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	closeAllMenu();
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();

	selBGImages = new selectionList("bgimages","bgimages",BGImages,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,myBGImagesResult);	
	selBGImages.sortList("asc");
	button1  = new button("button1","bgimages",setBGImageSetUp,"rect","Set BG",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
} 

// Function used by previuos function
function BGImagesResult() { }

BGImagesResult.prototype.getSelectionListVal = function(selBoxName,mapNr,arrayVal) {
    if(mapNr!=0){
            map.setBackgroundImage(BGImagesSortAss[arrayVal]);
    }
    selectedBGImageInList=arrayVal;
}

function setBGImageSetUp() {
	if(selectedBGImageInList!=0){ 
		setBGImage();
	}
}

function setBGImage(){
	if(selectedBGImageInList!=0){ 
		currentMapBackGround = BGImagesSortAss[selectedBGImageInList];
		map.setBGvalue(currentMapBackGround);
		clearTopInfo();
		writeDownInfo("Background image set");
	}
}

// *****************Functions Called by  Refresh Menu **********************

// Reload Maps List
function loadMapsSetUp() {	
	closeAllMenu();
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();
	loadMaps();
	showMapInfo();
	showHistory();
}

// Reload Nodes List
function loadNodesSetUp() {	
	closeAllMenu();
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();
	loadNodes();
	showMapInfo();
	showHistory();
}

// Refresh Map
function refreshNodesSetUp() {	
	closeAllMenu();
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}		
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	disableMenu();
	refreshNodes();
	showMapInfo();
	showHistory();
}

// *****************Functions Called by  Node Menu **********************
// Add Hide Nodes function
function addHideNodes()
{
	closeAllMenu();
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();
	addMapElement(hideNodesIds);
	hideNodesIds = "";
	hasHideNodes = false;

}

//Add Hide Nodes function
function addHideMaps()
{
	closeAllMenu();
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();
	addMapAsNode(hideMapsIds);
	hideMapsIds = "";
	hasHideMaps = false;

}
// Add Node function
function addMapElementList()
{
	closeAllMenu();
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();

	textbox1 = new textbox("textbox1","textboxwithcommand","",textboxmaxChars,textboxx,textboxy,textboxWidth,textboxHeight,textYOffset,textStyles,boxStyles,cursorStyles,seltextBoxStyles,"",filterNodeSelectionList);
	selNodes = new selectionList("nodes","nodes",nodeLabels,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,mynodesResult);
    selNodes.sortList("asc");    
	selNodes.selectElementByPosition(1, true);
	button1  = new button("button1","nodes",addMapElementSetUp,"rect","Add",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
}

function filterNodeSelectionList(textboxId,value,changeType) {
	if (changeType == "change") {
		var elementInList = nodeLabels.length;
		for(var k=1;k<nodeLabels.length;k++) {
			var nodeLabel = nodeLabels[k];
			var match = nodeLabel.indexOf(value);
			if  ( match == -1 ) {
				elementInList--;
				if (selNodes.elementExists(nodeLabel) >= 0) 
					selNodes.deleteElement(nodeLabel);
			} else if (selNodes.elementExists(nodeLabel) == -1 && match >= 0) {
				selNodes.addElementAtPosition(nodeLabel,k);
			}
		}
		if (elementInList > 1 )
			selNodes.selectElementByPosition(1,true);
		else 
			selNodes.selectElementByPosition(0,false);
	}
}

function nodesResult() { }

nodesResult.prototype.getSelectionListVal = function(selBoxName,nodeNr,arrayVal) {
	if(nodeNr!=0){
			writeDownInfo3("Selected Node Info", "Label: " +nodeSortAss[arrayVal].getLabel(), "Id: "+nodeSortAss[arrayVal].getId()); 
	}
	selectedMapElemInList=arrayVal;
}

function addMapElementSetUp() {
	if(selectedMapElemInList==0 ){
		return;
	}
	
	clearDownInfo();
	disableMenu();
	addMapElement(nodeSortAss[selectedMapElemInList].getId());	
}

// Add Node by Category
function addCategoryList()
{
	closeAllMenu();
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();
	textbox1 = new textbox("textbox1","textboxwithcommand","",textboxmaxChars,textboxx,textboxy,textboxWidth,textboxHeight,textYOffset,textStyles,boxStyles,cursorStyles,seltextBoxStyles,"",filterCatSelectionList);
	selCategories = new selectionList("categories","categories",categories,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,mycategoriesResult);
	selCategories.sortList("asc");
	selCategories.selectElementByPosition(1, true);
	button1  = new button("button1","categories",addNodesByCategorySetUp,"rect","Add",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
}

function filterCatSelectionList(textboxId,value,changeType) {
	if (changeType == "change") {
		var elementInList = categories.length;
		for(var k=1;k<categories.length;k++) {
			var category = categories[k];
			var match = category.indexOf(value);
			if  ( match == -1 ) {
				elementInList--;
				if (selCategories.elementExists(category) >= 0) 
					selCategories.deleteElement(category);
			} else if (selCategories.elementExists(category) == -1 && match >= 0) {
				selCategories.addElementAtPosition(category,k);
			}
		}
		if (elementInList > 1 )
			selCategories.selectElementByPosition(1,true);
		else 
			selCategories.selectElementByPosition(0,false);
	}
}

function categoriesResult() { }

categoriesResult.prototype.getSelectionListVal = function(selBoxName,nodeNr,arrayVal) {
        selectedCategoryInList=arrayVal;
}

function addNodesByCategorySetUp() {
	if(selectedCategoryInList==0 )  {
		return;
	}
	clearTopInfo();
	clearDownInfo();
	disableMenu();
	addNodesByCategory(categorySortAss[selectedCategoryInList]);	
}

// Add node using nodelabel
function addNodeLabelBox(){
	closeAllMenu();
	if(currentMapId!=MAP_NOT_OPENED){
		clearTopInfo();
		clearDownInfo();
		hidePickColor();
		resetFlags();

		textbox1 = new textbox("textbox1","textboxwithcommand","",textboxmaxChars,textboxx,textboxy,textboxWidth,textboxHeight,textYOffset,textStyles,boxStyles,cursorStyles,seltextBoxStyles,"[^]",undefined);
		button1  = new button("button1","textboxwithcommand",addNodesByLabelSetUp,"rect","Add",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);        
		writeDownInfo("Label text accept wildcard");
 	}else{
		alert('No maps opened');
    }
}

function addNodesByLabelSetUp() {
	var label = getTextBoxValue();
	if(label==""){
		alert('Invalid Label (must not be blank)');
		return;
	}
	clearTopInfo();
	clearDownInfo();
	disableMenu();
	addNodesByLabel(label);
}


// Add node using ip range functions 
function addRangeBox(){
	closeAllMenu();
	if(currentMapId!=MAP_NOT_OPENED){
		clearTopInfo();
		clearDownInfo();
		hidePickColor();
		resetFlags();

		textbox1 = new textbox("textbox1","textboxwithcommand","*.*.*.*",textboxmaxChars,textboxx,textboxy,textboxWidth,textboxHeight,textYOffset,textStyles,boxStyles,cursorStyles,seltextBoxStyles,"[^a-zA-Z ]",undefined);
		button1  = new button("button1","textboxwithcommand",addRangeOfNodesSetUp,"rect","Add",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);        
		writeDownInfo3("Ip range valid are:","192.168.*.*","192.168.10-20.0-255");
 	}else{
		alert('No maps opened');
    }
}

function addRangeOfNodesSetUp() {
	var range = getTextBoxValue();
	if(!isValidRange(range)){
		alert('Range not valid!');
		return;
	}
	clearTopInfo();
	clearDownInfo();
	disableMenu();
	addRangeOfNodes(range);
}

// Add Map using neighbor
function addMapElementNeigh()
{
	closeAllMenu();
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	if( map.getMapElementsSize()==0)
	{
		alert('Map contains no nodes');
		return;
	}	

	clearTopInfo();
	hidePickColor();
	resetFlags();
	addingMapElemNeighbors=true;
	writeDownInfo("click element to add neighb");
}

function addMapElemNeighSetUp(id) {
	clearTopInfo();
	clearDownInfo();
	disableMenu();
		
	addMapElemNeigh(id);
}

// Add a node with all his neighb function
function addMapElementNeighList()
{
	closeAllMenu();	
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();
	
	textbox1 = new textbox("textbox1","textboxwithcommand","",textboxmaxChars,textboxx,textboxy,textboxWidth,textboxHeight,textYOffset,textStyles,boxStyles,cursorStyles,seltextBoxStyles,"",filterNodeSelectionList);
	selNodes = new selectionList("nodes","nodes",nodeLabels,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,mynodesResult);
	selNodes.sortList("asc");
	selNodes.selectElementByPosition(1, true);
	button1  = new button("button1","nodes",addMapElementWithNeighborsSetUp,"rect","Add",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);	
}

function addMapElementWithNeighborsSetUp() {
	if(selectedMapElemInList==0 )  {
		return;
	}
	clearDownInfo();
	disableMenu();		
	addMapElementWithNeighbors(nodeSortAss[selectedMapElemInList].id);
}

// Add Map as a Node Functions
function addMapAsNodeList(){
	closeAllMenu();	
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}

	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();

	textbox1 = new textbox("textbox1","textboxwithcommand","",textboxmaxChars,textboxx,textboxy,textboxWidth,textboxHeight,textYOffset,textStyles,boxStyles,cursorStyles,seltextBoxStyles,"",filterMapSelectionList);
	selMaps = new selectionList("maps","maps",mapLabels,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,mymapsResult);
	selMaps.sortList("asc");
	selMaps.selectElementByPosition(1, true);

	
	button1  = new button("button1","maps",addMapAsNodeSetUp,"rect","Add",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
}
 
function addMapAsNodeSetUp() {
	if(selectedMapInList==0){
		return;
	}
	
	var mapId = mapSortAss[selectedMapInList].id;
	if(mapId==currentMapId){
		writeDownInfo("Cannot add map to itself");		
		return;
	}
	clearTopInfo();
	clearDownInfo();
	disableMenu();
	addMapAsNode(mapId);
}

// Add icons List function
function addIconList()
{
	closeAllMenu();	
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}
	if( map.getMapElementsSize()==0)
	{
		alert('Map contains no nodes');
		return;
	}
	clearTopInfo();
	clearDownInfo();
	hidePickColor();
	resetFlags();
    settingMapElemIcon=true;

	selMEIcons = new selectionList("meicons","meicons",MEIcons,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,myMEIconsResult);
	selMEIcons.sortList("asc");
	selMEIcons.selectElementByPosition(1,true)
} 

function setIconSetUp() {
	writeDownInfo3("Icon setting ok.","Click on an element to set","another icon.");			
} 

function MEIconsResult() { }

MEIconsResult.prototype.getSelectionListVal = function(selBoxName,mapNr,arrayVal) {
	
	var iconPreviewNode = document.getElementById("iconPreview");
	if (iconPreviewNode)
       iconPreviewNode.parentNode.removeChild(iconPreviewNode);

    if(mapNr!=0){
        selectedMEIconInList=arrayVal;

        var iconPreviewGroup = document.createElementNS(svgNS,"g");
        iconPreviewGroup.setAttributeNS(null,"id", "iconPreview");

        var iconPreviewRect = document.createElementNS(svgNS,"rect");
        iconPreviewRect.setAttributeNS(null,"x", 57);
        iconPreviewRect.setAttributeNS(null,"y", 80);
        iconPreviewRect.setAttributeNS(null,"width", 35);
        iconPreviewRect.setAttributeNS(null,"height", 35);
        iconPreviewRect.setAttributeNS(null,"fill", "white");
        iconPreviewRect.setAttributeNS(null,"stroke", "grey");
        iconPreviewRect.setAttributeNS(null,"stroke-width", 1);

        var iconPreview = document.createElementNS(svgNS,"image");
        iconPreview.setAttributeNS(null,"x", 65);
        iconPreview.setAttributeNS(null,"y", 87);
        iconPreview.setAttributeNS(null,"width", 20);
        iconPreview.setAttributeNS(null,"height", 25);
        iconPreview.setAttributeNS(xlinkNS, "xlink:href",IMAGES_ELEMENTS_FOLDER+MEIconsSortAss[arrayVal].fileName );
        iconPreviewGroup.appendChild(iconPreviewRect);
        iconPreviewGroup.appendChild(iconPreview);
        selMEIcons.parentGroup.appendChild(iconPreviewGroup);

        writeDownInfo2("Click on an element to set","icon.");
	} else {
		settingMapElemIcon=false;
    }
}

// Delete Map Element List
function deleteMapElementList()
{
	closeAllMenu()
	if(currentMapId==MAP_NOT_OPENED){
		alert('No maps opened');
		return;
	}

	if( map.getMapElementsSize()==0)
	{
		alert('Map contains no nodes');
		return;
	}	


	clearTopInfo();
	hidePickColor();
	resetFlags();
	deletingMapElem=true;
	writeDownInfo("Select the element to delete");
}

function deleteMapElementSetUp() {
	writeDownInfo3("Element Deleted.","Click on an Other Element","to delete.");			

}

// ***************** function called by View Menu **********
// Set element dimension 

function addDimensionList()
{

	closeAllMenu();
	clearTopInfo();
	clearDownInfo();
	resetFlags();

	selMapElemDim = new selectionList("mapelemdim","mapelemdim",MapElemDim,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,1,false,true,myMapElemDimResult);	
	selMapElemDim.sortList("asc");
	button1  = new button("button1","mapelemdim",setMapElemDimSetUp,"rect","Set",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);
} 

function MapElemDimResult() { }

MapElemDimResult.prototype.getSelectionListVal = function(selBoxName,dimNr,arrayVal) {
	selectedMapElemDimInList=arrayVal;
}

function setMapElemDimSetUp(){
	
	mapElemDimension=parseInt(MapElemDimSortAss[selectedMapElemDimInList]);
	
	clearTopInfo();
	map.render();	
			
	disableMenu();
	setMapElemDim()	
}

function viewBySeveritySetUp() {
	closeAllMenu();
	colorSemaphoreBy='S';
	refreshNodes();
	clearTopInfo();
	addLegend();
	showMapInfo();
	showHistory();
}

function viewByAvailSetUp() {
	closeAllMenu();
	colorSemaphoreBy='A';
	refreshNodes();
	clearTopInfo();
	addLegend();
	showMapInfo();
	showHistory();
}

function viewByStatusSetUp() {
	closeAllMenu();
	colorSemaphoreBy='T';
	refreshNodes();
	clearTopInfo();
	addLegend();
	showMapInfo();
	showHistory();
}

function toggleScreenSetUp() {
	closeAllMenu();
	top.toggle('footer');
	top.toggle('header');
	showMapInfo();
	showHistory();
}
// ***************function called by clicking on count down ******************
function addRefreshTimeList()
{	
	windowsClean();
	closeAllMenu();
	clearTopInfo();
	clearDownInfo();
	resetFlags();
	selRefreshTimeMins = new selectionList("refreshTime","refreshTime",refreshTimeMins,selBoxwidth,selBoxxOffset,selBoxyOffset,selBoxCellHeight,selBoxTextpadding,selBoxheightNrElements,selBoxtextStyles,selBoxStyles,selBoxScrollbarStyles,selBoxSmallrectStyles,selBoxHighlightStyles,selBoxTriangleStyles,selBoxpreSelect,false,true,myRefreshTimeMinsResult);	
	button1  = new button("button1","refreshTime",resetRefreshTimer,"rect","Set",undefined,buttonx,buttony,buttonwidth,buttonheight,buttonTextStyles,buttonStyles,shadeLightStyles,shadeDarkStyles,shadowOffset);        
}

function refreshTimeMinsResult() {selectedRefreshTimeList="30 seconds"; }

refreshTimeMinsResult.prototype.getSelectionListVal = function(selBoxName,dimNr,arrayVal) {
	if(arrayVal) selectedRefreshTimeList=arrayVal;
	else selectedRefreshTimeList="30 seconds";
}

function resetRefreshTimer(){
   refreshNodesIntervalInSec=refreshTimeMinsSortAss[selectedRefreshTimeList];
   clearTopInfo();
}

// Top Info writing
function writeTopInfoText(svgObject) {
	if (svgObject != null) {
		var topInfoText = document.getElementById("TopInfoText");
		if (topInfoText != null) {
			clearTopInfo();
			topInfoText.appendChild(svgObject);
			topInfoText.setAttributeNS(null,"display","inline");	
		}
	}
}

//clear the TopInfo SVG Object
function clearTopInfo(){

	if(selRefreshTimeMins!=null){
		if(selRefreshTimeMins.exists==true){
			selRefreshTimeMins.removeSelectionList();
			selRefreshTimeMins=0;
		}
	}	
	
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
	
	if(selCategories!=null){
		if(selCategories.exists==true){
			selCategories.removeSelectionList();
			selectedCategoryInList=0;
		}
	}

	if(selBGImages!=null){
		if(selBGImages.exists==true){
			map.render();
			selBGImages.removeSelectionList();
			selectedBGImageInList=0;
		}
	}	
	
	if(selMapElemDim!=null){
		if(selMapElemDim.exists==true){
			selMapElemDim.removeSelectionList();
			selectedMapElemDimInList="15 pixel width";
		}
	}			

	if(selMEIcons!=null){
		if(selMEIcons.exists==true){
			selMEIcons.removeSelectionList();
			selectedselMEIconInList=0;
		}
	}			

	if(textbox1!= null) {
		textbox1.removeTextbox();
		textbox1 = null;
	}
	
	if(button1!= null) {
		button1.removeButton();
		button1 = null;
	}
	
	var topInfoText = document.getElementById("TopInfoText");
	if (topInfoText ) {
         removeChilds(topInfoText);
    }
		

} 

// Function to manage DownInfo Box

function clearDownInfo(){
	var childNode = document.getElementById("DownInfoText");
	if (childNode)
		childNode.parentNode.removeChild(childNode);
}

function writeDownInfo(info){
	clearDownInfo();

	var text = document.createElementNS(svgNS,"text");
	text.setAttributeNS(null, "x","5");
	text.setAttributeNS(null, "y","20");
	text.setAttributeNS(null, "font-size",titleFontSize);
	text.setAttributeNS(null,"font-family",textFamily);
	text.setAttributeNS(null, "id","DownInfoText");
	
	var tspanContent = document.createTextNode(info);
	text.appendChild(tspanContent);		
	
	document.getElementById("DownInfo").appendChild(text);		
}

function writeDownInfo2(info1, info2){
	clearDownInfo();

	var text = document.createElementNS(svgNS,"text");
	text.setAttributeNS(null, "x","5");
	text.setAttributeNS(null, "y","20");
	text.setAttributeNS(null, "id","DownInfoText");
	text.setAttributeNS(null, "font-size",titleFontSize);
	text.setAttributeNS(null,"font-family",textFamily);
	text.appendChild(document.createTextNode(info1));
	
	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","7");
	tspan.setAttributeNS(null, "dy","25");
	tspan.setAttributeNS(null, "font-size",titleFontSize);
	tspan.setAttributeNS(null,"font-family",textFamily);
	var tspanContent = document.createTextNode(info2);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);		
	
	document.getElementById("DownInfo").appendChild(text);		
}

function writeDownInfo3(info1, info2, info3){
	clearDownInfo();

	var text = document.createElementNS(svgNS,"text");
	text.setAttributeNS(null, "x","5");
	text.setAttributeNS(null, "y","20");
	text.setAttributeNS(null, "id","DownInfoText");
	text.setAttributeNS(null, "font-size",titleFontSize);
	text.setAttributeNS(null,"font-family",textFamily);
	text.appendChild(document.createTextNode(info1));
	
	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","7");
	tspan.setAttributeNS(null, "dy","25");
	tspan.setAttributeNS(null, "font-size",titleFontSize);
	tspan.setAttributeNS(null,"font-family",textFamily);
	var tspanContent = document.createTextNode(info2);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);		
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","7");
	tspan.setAttributeNS(null, "dy","25");
	tspan.setAttributeNS(null, "font-size",titleFontSize);
	tspan.setAttributeNS(null,"font-family",textFamily);
	var tspanContent = document.createTextNode(info3);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);		

	document.getElementById("DownInfo").appendChild(text);						
}

// Manage MapInfo SVG Objects

function hideMapInfo(){
	var mapInfo = document.getElementById("MapInfo");
	if(mapInfo!=null)
		mapInfo.setAttributeNS(null,'display', 'none');
}

function showMapInfo(){
	var mapInfo = document.getElementById("MapInfo");
	if(mapInfo!=null)
		mapInfo.setAttributeNS(null,'display', 'inline');
}

function writeMapInfo(){
	clearMapInfo();
	var hasHideText = '';
	if (hasHideNodes && hasHideMaps) {
		hasHideText = ' : map has hidden Nodes and Maps';
	} else if (hasHideNodes) {
		hasHideText = ' : map has hidden Nodes';
	} else if (hasHideMaps) {
		hasHideText = ' : map has hidden Maps';		
	}
	var mapType="Unknown";
	
	if (currentMapType == "U") {
		mapType="User";
	} else if (currentMapType == "A") {
		mapType="Automatic"
	}  else if (currentMapType == "S") {
		mapType="Static"
	}
	var mapInfo= document.getElementById("MapInfo");
	
	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "id","MapInfoTitle");
	tspan.setAttributeNS(null, "font-size",titleFontSize);
	
	var tspanContent = document.createTextNode("Map Info"+hasHideText);
	tspan.appendChild(tspanContent);
	mapInfo.appendChild(tspan);

	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","20");
	tspan.setAttributeNS(null, "id","mapName");
	
	var tspanContent = document.createTextNode("Name: "+currentMapName+" ");
	tspan.appendChild(tspanContent);
	mapInfo.appendChild(tspan);

	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "id","mapOwner");
	
	var tspanContent = document.createTextNode("Owner: "+currentMapOwner+" ");
	tspan.appendChild(tspanContent);
	mapInfo.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "id","mapUserLast");
	
	var tspanContent = document.createTextNode("User last modified: "+currentMapUserlast+" ");
	tspan.appendChild(tspanContent);
	mapInfo.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "id","mapCreateTime");
	
	var tspanContent = document.createTextNode("Create time: "+currentMapCreatetime+" ");
	tspan.appendChild(tspanContent);
	mapInfo.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "id","mapLastModTime");
	
	var tspanContent = document.createTextNode("Last modified time: "+currentMapLastmodtime+" ");
	tspan.appendChild(tspanContent);
	mapInfo.appendChild(tspan);

	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "id","mapType");
	
	var tspanContent = document.createTextNode("Map Type: "+mapType+" ");
	tspan.appendChild(tspanContent);
	mapInfo.appendChild(tspan);

	mapInfo.setAttributeNS(null,'display', 'inline');
}

function clearMapInfo(){
   
   var mapInfoTitle=document.getElementById("MapInfoTitle");
   if(mapInfoTitle!=null)
           mapInfoTitle.parentNode.removeChild(mapInfoTitle);
   var mapNameNode=document.getElementById("mapName");
   if(mapNameNode!=null)
           mapNameNode.parentNode.removeChild(mapNameNode);
   var mapOwnerNode=document.getElementById("mapOwner");
   if(mapOwnerNode!=null)
           mapOwnerNode.parentNode.removeChild(mapOwnerNode);
   var mapUserLastNode=document.getElementById("mapUserLast");
   if(mapUserLastNode!=null)
           mapUserLastNode.parentNode.removeChild(mapUserLastNode);
   var mapCreateTimeNode=document.getElementById("mapCreateTime");
   if(mapCreateTimeNode!=null)
           mapCreateTimeNode.parentNode.removeChild(mapCreateTimeNode);            
   var mapLastModTimeNode=document.getElementById("mapLastModTime");
   if(mapLastModTimeNode!=null)
           mapLastModTimeNode.parentNode.removeChild(mapLastModTimeNode);          
   var mapType=document.getElementById("mapType");
   if(mapType!=null)
       mapType.parentNode.removeChild(mapType);          
}

//Assert loading..
function assertLoading(){
	var lt = document.getElementById("LoadingText");
	if(loading==0){
		lt.setAttributeNS(null,'display', 'none');
	}else{
		lt.setAttributeNS(null,'display', 'inline');
	}
}

//Assert Refreshing....

function assertRefreshing(loading){
	var lt = document.getElementById("RefreshingText");
	if(loading==0){
		lt.setAttributeNS(null,'display', 'none');
	}else{
		lt.setAttributeNS(null,'display', 'inline');
	}
}

function showHistory(){
	if(mapHistory.length>mapHistoryIndex+1){
		var next = mapHistory[mapHistoryIndex+1];
		var nextName = mapHistoryName[mapHistoryIndex+1]; 
		//alert("nextMap id="+next+" nextName="+nextName);
		var textContent = document.createTextNode(nextName);
		var nextText = document.getElementById("nextMapName");
		if(nextText.firstChild)
			nextText.removeChild(nextText.firstChild);		
		nextText.appendChild(textContent);
		var nextAction = document.getElementById("nextAction");
		nextAction.setAttribute("onclick","openMapSetUp("+next+");");
		document.getElementById("nextGroup").setAttributeNS(null,'display', 'inline');
	}else{
	    document.getElementById("nextGroup").setAttributeNS(null,'display', 'none');
	}
	
	if(mapHistoryIndex>0){
		var prev = mapHistory[mapHistoryIndex-1];
		var prevName = mapHistoryName[mapHistoryIndex-1]; 
		var textContent = document.createTextNode(prevName);
		var prevText = document.getElementById("prevMapName");
		if(prevText.firstChild)
			prevText.removeChild(prevText.firstChild);
		prevText.appendChild(textContent);
		var prevAction = document.getElementById("prevAction");
		prevAction.setAttribute("onclick","openMapSetUp("+prev+");");		
		document.getElementById("prevGroup").setAttributeNS(null,'display', 'inline');
	}else{
	    document.getElementById("prevGroup").setAttributeNS(null,'display', 'none');
	}		
	document.getElementById("history").setAttributeNS(null,'display', 'inline');
}

function hideHistory(){
	document.getElementById("history").setAttributeNS(null,'display', 'none');
}

// Display countDownText function
function displayCountDown(text, editTime){
	var textEl = document.getElementById("countDownText");
	
	if(textEl.firstChild){
		textEl.removeChild(textEl.firstChild);
	}

	if(editTime){
		textEl.setAttributeNS(null, "cursor","pointer");
		textEl.addEventListener("click", addRefreshTimeList, false);
	}else{
		textEl.setAttributeNS(null, "cursor","default");
		textEl.removeEventListener("click",addRefreshTimeList,false);
	}

	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "dy","12");
	var tspanContent = document.createTextNode(text);
	tspan.appendChild(tspanContent);
	textEl.appendChild(tspan);
}

// Legend Svg Object Methods

function removeLegend() {
	var legendSVG = document.getElementById("legend");
	var obj, ls;
	ls = legendSVG.childNodes;
	while (ls.length > 0) {
	  obj = ls.item(0);
	  legendSVG.removeChild(obj);
	}	
}

function addLegend() {
	var legendSVG = document.getElementById("legend");
	
	var legendHeight=legendSVG.getAttributeNS(null,"height");
	
	var x = 1;
	var y = 15;

	var cx = 10;
	var cy = 23;
	var cr = 5;
	var dx = 0;
	var dy = 2*cr+1;
	var fontsize=dy-1;
	var ddx = 10;
	var ddy = 3;
	
	var legendSpace = legendHeight-y-2*fontsize;

	if (legendSVG.firstChild != undefined) {
		legendSVG.removeChild(legendSVG.firstChild);
	}

	var legendGroup = document.createElementNS(svgNS,"g");

	var lgtext = document.createElementNS(svgNS,"text");
	lgtext.setAttributeNS(null,"x", x);
	lgtext.setAttributeNS(null,"y",y);
	lgtext.setAttributeNS(null,"font-size",fontsize+1);
	lgtext.setAttributeNS(null,"font-family",textFamily);
	
	var contentText = document.createTextNode("Severity View");

	if ( colorSemaphoreBy == "A") {
		contentText = document.createTextNode("Availability View");
		var countElem = 0;
		for(var index in AVAIL_MIN) {
			if(AVAIL_MIN[index]!=undefined){
				countElem++;
			}
		}
		var newcr = parseInt((legendSpace/countElem)/2); //divided by 2 because is r of a circle
		if(newcr<=cr){
			cr=newcr;
			dy = 2*cr+1;
			fontsize=dy-1;
		}

		for(var index in AVAIL_MIN) {

			var label = "";
			if (AVAIL_MIN[index] < 0) {
				label = "Unknown";
			} else {
				label =  " Better " + AVAIL_MIN[index] +"%";
			}

			var item = document.createElementNS(svgNS,"circle");
			item.setAttributeNS(null,"cx",cx);
			item.setAttributeNS(null,"cy",cy);
			item.setAttributeNS(null,"r",cr);
			item.setAttributeNS(null,"fill",AVAIL_COLOR[index]);
			item.setAttributeNS(null,"stroke","black");
			legendGroup.appendChild(item);

			var textel = document.createElementNS(svgNS,"text");
			textel.setAttributeNS(null,"id", label+"Text");
			textel.setAttributeNS(null,"font-size",fontsize);
			textel.setAttributeNS(null,"font-family",textFamily);
			
			textel.setAttributeNS(null,"x", cx+ddx);
			textel.setAttributeNS(null,"y",cy+ddy);
			var labelText = document.createTextNode(label);
			textel.appendChild(labelText);
			legendGroup.appendChild(textel);
			cx = cx+dx;
			cy = cy+dy;
		}
	} else if (colorSemaphoreBy == "T") {
		contentText = document.createTextNode("Status View");
		var countElem = 0;
		for(var index in STATUSES_UEI) {
			if(STATUSES_UEI[index]!=undefined){
				countElem++;
			}
		}
		var newcr = parseInt((legendSpace/countElem)/2); //divided by 2 because is r of a circle
		if(newcr<=cr){
			cr=newcr;
			dy = 2*cr+1;
			fontsize=dy-1;
		}
		
		for(var index in STATUSES_UEI) {

			var item = document.createElementNS(svgNS,"circle");
			item.setAttributeNS(null,"cx",cx);
			item.setAttributeNS(null,"cy",cy);
			item.setAttributeNS(null,"r",cr);
			item.setAttributeNS(null,"fill",STATUSES_COLOR[index]);
			item.setAttributeNS(null,"stroke","black");
			legendGroup.appendChild(item);

			var textel = document.createElementNS(svgNS,"text");
			textel.setAttributeNS(null,"font-size",fontsize);
			textel.setAttributeNS(null,"font-family",textFamily);

			textel.setAttributeNS(null,"x", cx+ddx);
			textel.setAttributeNS(null,"y",cy+ddy);
			var labelText = document.createTextNode(STATUSES_TEXT[index]);
			textel.appendChild(labelText);
			legendGroup.appendChild(textel);
			cx = cx+dx;
			cy = cy+dy;
		}
	} else {
	
		var countElem = 0;
		for(var index in SEVERITIES_LABEL) {
			if(SEVERITIES_LABEL[index]!=undefined){
				countElem++;
			}
		}
		var newcr = parseInt((legendSpace/countElem)/2); //divided by 2 because is r of a circle
		if(newcr<=cr){
			cr=newcr;
			dy = 2*cr+1;
			fontsize=dy-1;
		}	
		for(var index in SEVERITIES_LABEL) {

			var item = document.createElementNS(svgNS,"circle");
			item.setAttributeNS(null,"cx",cx);
			item.setAttributeNS(null,"cy",cy);
			item.setAttributeNS(null,"r",cr);
			item.setAttributeNS(null,"fill",SEVERITIES_COLOR[index]);
			item.setAttributeNS(null,"stroke","black");
			legendGroup.appendChild(item);

			var textel = document.createElementNS(svgNS,"text");
			textel.setAttributeNS(null,"font-size",fontsize);
			textel.setAttributeNS(null,"font-family",textFamily);

			textel.setAttributeNS(null,"x", cx+ddx);
			textel.setAttributeNS(null,"y",cy+ddy);
			var labelText = document.createTextNode(SEVERITIES_LABEL[index]);
			textel.appendChild(labelText);
			legendGroup.appendChild(textel);
			cx = cx+dx;
			cy = cy+dy;
	    }
		
	}
	lgtext.appendChild(contentText);
	legendGroup.appendChild(lgtext);
	legendSVG.appendChild(legendGroup);
	
}

function hidePickColor() {
	document.getElementById("pickColor").setAttributeNS(null,'display', 'none');
}

function showPickColor() {
	add_pick_color("pickColor");
	document.getElementById("pickColor").setAttributeNS(null,'display', 'inline');
}

function getInfoOnMapElement(mapElement)
{

    //var severityColor=getSeverityColor(mapElement.getSeverity())
	var severityColor='black';
    var severityLabel = SEVERITIES_LABEL[mapElement.getSeverity()];

    //var statusColor=getStatusColor(mapElement.getStatus());
	var statusColor='black'
	var status = STATUSES_TEXT[mapElement.getStatus()];
	
	var avail=mapElement.getAvail();
	var availColor='black';
	//var availColor = getAvailColor(avail);



	if(avail<0){
		avail="Unknown";
	}else{	
		avail = (""+avail).substring(0,6)+"%";
	}

	var text = document.createElementNS(svgNS,"text");
	text.setAttributeNS(null, "x","3");
	text.setAttributeNS(null, "dy","15");
	text.setAttributeNS(null, "id","topInfoTextTitle");
	
	var textLabel = document.createTextNode("Map Element info");
	text.appendChild(textLabel);
	
	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","20");
	
	var tspanContent = document.createTextNode(mapElement.getLabel());
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);

	if(mapElement.isMap()){
		tspan = document.createElementNS(svgNS,"tspan");
		tspan.setAttributeNS(null, "x","3");
		tspan.setAttributeNS(null, "dy","15");
		tspanContent = document.createTextNode("Id: "+ mapElement.getMapId() + "  (Map)");
		tspan.appendChild(tspanContent);
		text.appendChild(tspan);
	}

	if(mapElement.isNode()){
		tspan = document.createElementNS(svgNS,"tspan");
		tspan.setAttributeNS(null, "x","3");
		tspan.setAttributeNS(null, "dy","15");
		tspan.setAttributeNS(null, "id","TopInfoLabelText");
		tspanContent = document.createTextNode("Id: "+ mapElement.getNodeId() + "  (Node)");
		tspan.appendChild(tspanContent);
		text.appendChild(tspan);	
	}

	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "fill",statusColor);
	tspanContent = document.createTextNode("Status: " + status);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);	
		
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "fill",availColor);
	tspanContent = document.createTextNode("Availability: " + avail );
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "fill",severityColor);
	tspanContent = document.createTextNode("Severity: " + severityLabel );
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);		

	// get info 	
	return text;
}

function getInfoOnLink(link) 
{
	//var statusColor = LINKSTATUS_COLOR[link.getStatus()];
	var statusColor = 'black';
	var text = document.createElementNS(svgNS,"text");
	text.setAttributeNS(null, "x","3");
	text.setAttributeNS(null, "dy","15");
	text.setAttributeNS(null, "id","topInfoTextTitle");
	text.setAttributeNS(null, "font-size",titleFontSize);
	
	var textLabel = document.createTextNode("Link info");
	text.appendChild(textLabel);
	
	var tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","20");
	var tspanContent = document.createTextNode(" From: " + nodeidSortAss[link.getFirstNodeId()].getLabel());
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspanContent = document.createTextNode("    To: " + nodeidSortAss[link.getSecondNodeId()].getLabel());
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspan.setAttributeNS(null, "fill",statusColor);
	tspanContent = document.createTextNode(" Status: "+link.getStatus());
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspanContent = document.createTextNode(" Type: "+LINK_TEXT[link.getTypology()]);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);
	
	tspan = document.createElementNS(svgNS,"tspan");
	tspan.setAttributeNS(null, "x","3");
	tspan.setAttributeNS(null, "dy","15");
	tspanContent = document.createTextNode(" Speed: "+LINK_SPEED[link.getTypology()]);
	tspan.appendChild(tspanContent);
	text.appendChild(tspan);	
	
	return text;
}


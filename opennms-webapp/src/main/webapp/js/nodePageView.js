
function initPageView(elementId, nodeId){
	var interfacesPanel;
	var ipInterfaceGrid;
	var physicalInterfaceGrid;
	var searchIpGrid;
	
	ipInterfaceGrid = new OpenNMS.ux.IPInterfaceGrid({
		id:'nodeInterfaceGrid',
		title:'IP Interfaces',
		nodeId: nodeId,
		height:495
	});

	
	physicalInterfaceGrid = new OpenNMS.ux.SNMPInterfaceGrid({
		id:'nodePhysicalInterfaceGrid',
		title:'Physical Interfaces',
		nodeId: nodeId,
		height:495
	});
	
	
	searchIpGrid = new OpenNMS.ux.SearchFilterGrid({
		id:'ipSearchGrid',
		title:'IP Interfaces',
		grid:ipInterfaceGrid
		
	});
	
	searchPhysicalIntergaceGrid = new OpenNMS.ux.SearchFilterGrid({
		id:'physicalSearchGrid',
		title:'Physical Interfaces',
		grid:physicalInterfaceGrid
	});
	
	interfacesPanel = new Ext.TabPanel({
		renderTo:elementId,
		activeTab:0,
		width:'auto',
		autoHeight:true,
		minHeight:400,
		bodyBorder:false,
		deferredRender: !Ext.isIE,
		border:false,
		items:[
			searchIpGrid,
			searchPhysicalIntergaceGrid
		]
	});

};
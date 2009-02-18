var interfacesPanel;
var ipInterfaceGrid;
var physicalInterfaceGrid;

function initPageView(elementId, nodeId){

	
	ipInterfaceGrid = new OpenNMS.ux.IPInterfaceGrid({
		id:'nodeInterfaceGrid',
		title:'IP Interfaces',
		nodeId: nodeId,
	});

	
	physicalInterfaceGrid = new OpenNMS.ux.SNMPInterfaceGrid({
		id:'nodePhysicalInterfaceGrid',
		title:'Physical Interfaces',
		nodeId: nodeId,
	});
	
//	filterableGrid = new OpenNMS.ux.SearchFilterGrid({
//		id:'searchFilterPhysGrid',
//		grid:physicalInterfaceGrid,
//	});
	
	interfacesPanel = new Ext.TabPanel({
		renderTo:elementId,
		activeTab:0,
		width:'auto',
		autoHeight:true,
		bodyBorder:false,
		//deferredRender: false,
		border:false,
		items:[
			ipInterfaceGrid,
			physicalInterfaceGrid,
		],

	});

};
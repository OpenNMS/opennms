var interfacesPanel;
var ipInterfaceGrid;
var physicalInterfaceGrid;

function initPageView(elementId, nodeId){

	
	ipInterfaceGrid = new OpenNMS.ux.IPInterfaceGrid({
		id:'nodeInterfaceGrid',
		title:'IP Interfaces',
	});

	
	physicalInterfaceGrid = new OpenNMS.ux.IPInterfaceGrid({
		id:'nodePhysicalInterfaceGrid',
		title:'Physical Interfaces',
	});
	
	filterableGrid = new OpenNMS.ux.SearchFilterGrid({
		id:'searchFilterPhysGrid',
		grid:physicalInterfaceGrid,
	});
	
	interfacesPanel = new Ext.TabPanel({
		renderTo:elementId,
		activeTab:0,
		width:'auto',
		autoHeight:true,
		bodyBorder:false,
		border:false,
		items:[
			ipInterfaceGrid,
			filterableGrid
		],

	});
	
	ipInterfaceGrid.load("rest/nodes/"+nodeId+"/ipinterfaces", {offset:0, limit:20});
};
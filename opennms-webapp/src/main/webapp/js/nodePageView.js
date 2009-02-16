var interfacesPanel;
var ipInterfaceGrid;
var physicalInterfaceGrid;

function initPageView(elementId){
	
	ipInterfaceGrid = new OpenNMS.ux.PageableGrid({
		id:'nodeInterfaceGrid',
		title:'IP Interfaces',
	});

	
	physicalInterfaceGrid = new OpenNMS.ux.PageableGrid({
		id:'nodePhysicalInterfaceGrid',
		title:'Physical Interfaces',
		pagingBarButtons: [{id:'searchButton', text:'Search'}],
	});
	
	filterableGrid = new OpenNMS.ux.SearchFilterGrid({
		id:'searchFilterPhysGrid',
		grid:physicalInterfaceGrid,
		searchButton:'searchButton'
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

	})
};
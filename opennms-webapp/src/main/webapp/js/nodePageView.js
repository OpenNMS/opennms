
function initPageView(elementId, nodeId){
	var interfacesPanel;
	var ipInterfaceGrid;
	var physicalInterfaceGrid;
	var searchIpGrid;

	ipInterfaceGrid = new OpenNMS.ux.IPInterfaceGrid({
		id:'nodeInterfaceGrid',
		title:'IP Interfaces',
		viewConfig:{
			autoFill: true,
			forceFit: true,
			scrollOffset: 5,
			getRowClass : function(record, rowIndex, p, store) {
				return getIpStatusColor(record.data.isDown, record.data.isManaged);
			}
		},
		nodeId: nodeId,
		height:495
	});

	
	physicalInterfaceGrid = new OpenNMS.ux.SNMPInterfaceGrid({
		id:'nodePhysicalInterfaceGrid',
		title:'Physical Interfaces',
		viewConfig:{
			autoFill: true,
	  		forceFit: true,
	  		scrollOffset:5,
  		 	getRowClass : function(record, rowIndex, p, store){
	            return getSnmpStatusColor(record.data.ifAdminStatus, record.data.ifOperStatus);
            }
		},
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
	
	function getIpStatusColor(isDown, isManaged) {
		var bgStyle = 'grid-status-blue';
		if (isManaged) {
			bgStyle = 'grid-status-green';
			if (isDown == 'true') {
				bgStyle = 'grid-status-red';
			}
		}

		return String.format('x-grid3-row {0}', bgStyle);
	}

	function getSnmpStatusColor(ifAdminStatus, ifOperStatus){
		var bgStyle;
		if(ifAdminStatus != 1){
			bgStyle = 'grid-status-blue';
		}else if(ifAdminStatus == 1 && ifOperStatus == 1){
			bgStyle = 'grid-status-green';
		}else if(ifAdminStatus == 1 && ifOperStatus != 1){
			bgStyle = 'grid-status-red';
		}
		
		return String.format('x-grid3-row {0}', bgStyle);
	};

};
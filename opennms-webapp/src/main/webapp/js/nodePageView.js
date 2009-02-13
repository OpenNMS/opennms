var physicalAddrColModel = new Ext.grid.ColumnModel([
	{
		header:"ID",
		name:"theId",
		width:50,
		sortable:false,
		align:"left"
	},{
		header :'ifAdminStatus',
		name :'ifAdminStatus',
		width :100,
		sortable :true,
		align :'left'
	},{
		header:"ifDescr",
		name:"ifDescr",
		width:100,
		sortable:false,
		align:"left"
	},{
		header:"ifIndex",
		name:"ifIndex",
		hidden:true,
		width:100,
		align:"left"
	},{
		header:"ifName",
		name:"ifName",
		hidden:true,
		width:100,
		align:"left"	
	},{
		header:"ifOperStatus",
		name:"ifOperStatus",
		hidden:true,
		width:100,
		align:"left"
	},{
		header:"ifSpeed",
		name:"ifSpeed",
		hidden:true,
		width:100,
		align:"left"
	},{
		header:"ifType",
		name:"ifType",
		hidden:true,
		width:100,
		align:"left"
	},{
		header:"ipAddress",
		name:"ipAddress",
		hidden:true,
		width:100,
		align:"left"
	},{
		header:"physAddr",
		name:"physAddr",
		hidden:true,
		width:100,
		align:"left"
	}
]);

var physicalAddrStore = new Ext.data.Store({
	baseParams:{limit:20},
	proxy:new Ext.data.HttpProxy({
		method:"GET",
		extraParams:{
			limit:20
		}
	}),
	reader:new Ext.data.XmlReader({
		record:"snmpInterface",
		totalRecords:"@totalCount"
	}, [
		{name:"theId", mapping:"id"},
		{name:"ifAdminStatus", mapping:"ifAdminStatus"},
		{name:"ifDescr", mapping:"ifDescr"},
		{name:"ifIndex", mapping:"ifIndex"},
		{name:"ifName", mapping:"ifName"},
		{name:"ifOperStatus", mapping:"ifOperStatus"},
		{name:"ifSpeed", mapping:"ifSpeed"},
		{name:"ifType", mapping:"ifType"},
		{name:"ipAddress", mapping:"ipAddress"},
		{name:"physAddr", mapping:"physAddr"}])
});

var interfacesPanel;
var ipInterfaceGrid;
var physicalInterfaceGrid;

function initPageView(elementId){
	
	ipInterfaceGrid = new OpenNMS.ux.SearchFilterGrid({
		title:'IP Interfaces',
		id:'nodeInterfaceGrid',
		renderTo:'interfaces-panel',
	});
	
	physicalInterfaceGrid = new OpenNMS.ux.SearchFilterGrid({
		title:'Physical Interfaces',
		id:'nodePhysicalInterfaceGrid',
        store:physicalAddrStore,
        colModel:physicalAddrColModel,
        renderTo:'interfaces-panel',
	});
	
	interfacesPanel = new Ext.TabPanel({
		applyTo:elementId,
		activeTab:0,
		width:'auto',
		autoHeight:true,
		bodyBorder:false,
		border:false,
		items:[
			ipInterfaceGrid,
			physicalInterfaceGrid
		],

	})
};
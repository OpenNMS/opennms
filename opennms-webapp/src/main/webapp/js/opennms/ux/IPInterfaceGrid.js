Ext.namespace("OpenNMS.ux")
OpenNMS.ux.IPInterfaceGrid = Ext.extend(OpenNMS.ux.PageableGrid, {
	title:'IP Interfaces',
	urlTemplate:"rest/nodes/{nodeId}/ipinterfaces",
	xmlNodeToRecord:'ipInterface',
	columns:[
		{
			header :'Interface Id',
			name :'interfaceId',
			width:100,
			sortable :true,
			align :'left'
		},{
			header :'ip Address',
			name :'ipAddress',
			width :100,
			sortable :true,
			align :'left'
		},{
			header:'ip Host Name',
			name:'hostName',
			width:200,
			align:'left',
		},{
			header:'ifIndex',
			name:'ifIndex',
			width:75,
			align:'left',
			hidden:true
		},{
			header :'isManaged',
			name :'isManaged',
			width :75,
			sortable :true,
			align :'left',
			hidden:true
		},{
			header:'Last Capsd Poll',
			name:'capsdPoll',
			width:150,
			hidden:true,
			align:'left'
		},{
			header:'Node',
			name:'node',
			width:20,
			hidden:true,
			align:'left'
		}
	],
	recordTag:'ipInterface',
	recordMap:[
			    {name:"interfaceId", mapping:"interfaceId"},
			    {name:"ipAddress", mapping:"ipAddress"},
			    {name:'hostName', mapping:'ipHostName'},
			    {name:'ifIndex', mapping:'ifIndex'},
			    {name:"isManaged", mapping:"isManaged"},
			    {name:'capsdPoll', mapping:'ipLastCapsdPoll'},
			    {name:"snmpInterface", mapping:"snmpInterface"}
	],
	

	initComponent:function(){
	
		if (!this.nodeId) {
			throw "nodeId must be set in the config for IPInterfaceGrid";
		}

		OpenNMS.ux.IPInterfaceGrid.superclass.initComponent.apply(this, arguments);
		
	},
	


});

Ext.reg('o-ipgrid', OpenNMS.ux.IPInterfaceGrid);
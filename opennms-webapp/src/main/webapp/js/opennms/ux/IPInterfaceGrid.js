Ext.namespace("OpenNMS.ux");
OpenNMS.ux.IPInterfaceGrid = Ext.extend(OpenNMS.ux.PageableGrid, {
	title:'IP Interfaces',
	urlTemplate:"rest/nodes/{nodeId}/ipinterfaces",
	xmlNodeToRecord:'ipInterface',
	columns:[
		{
			id: 'interfaceId',
			header :'ID',
			dataIndex :'interfaceId',
			width:100,
			sortable :true,
			hidden:true,
			align :'left'
		},{
			id: 'ipAddress',
			header :'IP Address',
			dataIndex :'ipAddress',
			width :100,
			sortable :true,
			searchable: true,
			defaultSearch: true,
			align :'left'
		},{
			id: 'ipHostName',
			header:'IP Host Name',
			dataIndex:'ipHostName',
			sortable :true,
			searchable: true,
			width:200,
			align:'left'
		},{
			id: 'ifIndex',
			header:'IfIndex',
			dataIndex:'ifIndex',
			width:75,
			sortable: true,
			searchable: true,
			align:'left',
			hidden:true
		},{
			id: 'isManaged',
			header :'Managed',
			dataIndex :'isManaged',
			width :75,
			sortable :true,
			align :'left'
		},{
			id: 'isDown',
			header :'Down',
			dataIndex :'isDown',
			width :30,
			sortable :true,
			align :'left',
			hidden :true
		},{
			id: 'ipLastCapsdPoll',
			header:'Last Node Scan',
			dataIndex:'ipLastCapsdPoll',
			width:150,
			hidden:true,
			align:'left'
		},{
			id:'node',
			header:'Node',
			dataIndex:'node',
			width:20,
			hidden:true,
			searchable: false,
			align:'left'
		}
	],
	recordTag:'ipInterface',
	recordMap:[
			    {name:'interfaceId', mapping:'interfaceId'},
			    {name:'ipAddress', mapping:'ipAddress'},
			    {name:'ipHostName', mapping:'ipHostName'},
			    {name:'ifIndex', mapping:'ifIndex'},
			    {name:'isManaged', mapping:'isManaged'},
			    {name:'isDown', mapping:'isDown'},
			    {name:'ipLastCapsdPoll', mapping:'ipLastCapsdPoll'},
			    {name:'snmpInterface', mapping:'snmpInterface'}
	],
	

	initComponent:function(){
	
		if (!this.nodeId) {
			throw "nodeId must be set in the config for IPInterfaceGrid";
		}

		OpenNMS.ux.IPInterfaceGrid.superclass.initComponent.apply(this, arguments);
		
	},
	
	onDoubleClick:function(event){
		window.location = "element/interface.jsp?ipinterfaceid=" + this.getSelectionModel().getSelected().data.interfaceId;
	}


});

Ext.reg('o-ipgrid', OpenNMS.ux.IPInterfaceGrid);
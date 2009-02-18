Ext.namespace("OpenNMS.ux")
OpenNMS.ux.SNMPInterfaceGrid = Ext.extend(OpenNMS.ux.PageableGrid, {

	title:'Physical Interfaces',
	urlTemplate:"rest/nodes/{nodeId}/snmpinterfaces",
	xmlNodeToRecord:'snmpInterface',
	columns:[
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
	    ],
	    recordTag:'snmpInterface',
	    recordMap:[
					{name:"theId", mapping:"id"},
					{name:"ifAdminStatus", mapping:"ifAdminStatus"},
					{name:"ifDescr", mapping:"ifDescr"},
					{name:"ifIndex", mapping:"ifIndex"},
					{name:"ifName", mapping:"ifName"},
					{name:"ifOperStatus", mapping:"ifOperStatus"},
					{name:"ifSpeed", mapping:"ifSpeed"},
					{name:"ifType", mapping:"ifType"},
					{name:"ipAddress", mapping:"ipAddress"},
					{name:"physAddr", mapping:"physAddr"}
		],
	

	initComponent:function(){
	
		if (!this.nodeId) {
			throw "nodeId must be set in the config for SNMPInterfaceGrid";
		}
		
		OpenNMS.ux.SNMPInterfaceGrid.superclass.initComponent.apply(this, arguments);
		
	},
	
	

});

Ext.reg('o-snmpgrid', OpenNMS.ux.SNMPInterfaceGrid);
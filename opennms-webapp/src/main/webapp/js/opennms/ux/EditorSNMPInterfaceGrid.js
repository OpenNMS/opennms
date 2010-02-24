Ext.namespace("OpenNMS.ux")
OpenNMS.ux.EditorSNMPInterfaceGrid = Ext.extend(OpenNMS.ux.EditorPageableGrid, {

	title:'Physical Interfaces',
	clicksToEdit:1,
	urlTemplate:"rest/nodes/{nodeId}/snmpinterfaces",
	xmlNodeToRecord:'snmpInterface',
	columns:[
	     	{
	    		header:"ID",
	    		dataIndex:"theId",
	    		width:50,
	    		sortable:false,
	    		align:"left",
	    		hidden:true
	    	},{
	    		header:"Index",
	    		dataIndex:"ifIndex",
	    		sortable: true,
	    		searchable: true,
	    		width:30,
	    		align:"center"
	    	},{
	    		header:"IP Address",
	    		dataIndex:"ipAddress",
	    		sortable: true,
	    		searchable: true,
	    		hidden:false,
	    		width:100,
	    		align:"left"
	    	},{
	    		header:"SNMP ifType",
	    		dataIndex:"ifType",
	    		sortable: true,
	    		hidden:false,
	    		width:50,
	    		align:"center"
	    	},{
	    		header:"SNMP ifDescr",
	    		dataIndex:"ifDescr",
	    		sortable:true,
	    		hidden:false,
	    		width:50,
	    		align:"center",
	    		searchable:true,
	    		defaultSearch: true
	    		
	    	},{
	    		header:"SNMP ifName",
	    		dataIndex:"ifName",
	    		sortable: true,
	    		searchable: true,
	    		width:90,
	    		align:"center"	
	    	},{
	    		header:"SNMP ifAlias",
	    		dataIndex:"ifAlias",
	    		sortable: true,
	    		searchable: true,
	    		hidden:false,
	    		width:90,
	    		align:"left"	
	    	},{
	    		header:"Collect",
	    		dataIndex:"collect",
	    		sortable: true,
	    		searchable: true,
	    		hidden:false,
	    		width:100,
	    		align:"center",
	    		renderer:function(value, p, record){
	    			var retVal;
					
					if(value == "C" || value == "UC"){
						retVal = "Collect";
					}else if(value == "N" || value == "UN"){
						retVal = "Don't Collect";
					}else if(value == "Default"){
						retVal = "Default";
					}
					
					return retVal;
				},
	    		editor: new Ext.form.ComboBox({
		               typeAhead: true,
		               triggerAction: 'all',
		               store:[["UC","Collect"], ["UN","Don't Collect"], ["Default", "Default"]],
		               lazyRender:true,
		               listClass: 'x-combo-list-small'
		            })
	    		
	    	},{
	    		header:"SNMP ifSpeed",
	    		dataIndex:"ifSpeed",
	    		sortable: true,
	    		searchable: true,
	    		hidden:true,
	    		width:100,
	    		align:"right"
	    	},{
	    		header :'SNMP ifAdminStatus',
	    		dataIndex :'ifAdminStatus',
	    		width :100,
	    		sortable :true,
	    		align :'left',
	    		hidden:true
	    	},{
	    		header:"SNMP ifOperStatus",
	    		dataIndex:"ifOperStatus",
	    		sortable: true,
	    		hidden:true,
	    		width:100,
	    		align:"left"
	    	},{
	    		header:"SNMP ifPhysAddr",
	    		dataIndex:"physAddr",
	    		sortable: true,
	    		searchable: true,
	    		hidden:true,
	    		width:100,
	    		align:"left"
	    	}
	    	
	    	
	    ],
	    recordTag:'snmpInterface',
	    recordMap:[
					{name:"theId", mapping:"@id"},
					{name:"ifAdminStatus", mapping:"ifAdminStatus"},
					{name:"ifDescr", mapping:"ifDescr"},
					{name:"ifIndex", mapping:"@ifIndex"},
					{name:"ifName", mapping:"ifName"},
					{name:"ifAlias", mapping:"ifAlias"},
					{name:"ifOperStatus", mapping:"ifOperStatus"},
					{name:"ifSpeed", mapping:"ifSpeed"},
					{name:"ifType", mapping:"ifType"},
					{name:"ipAddress", mapping:"ipAddress"},
					{name:"physAddr", mapping:"physAddr"},
					{name:"collect", mapping:"@collectFlag"},
					{name:'collectionEnabled', mapping:'@collect', type:'bool'}
		],
		
	initComponent:function(){
	
		if (!this.nodeId) {
			throw "nodeId must be set in the config for SNMPInterfaceGrid";
		}
		
		OpenNMS.ux.EditorSNMPInterfaceGrid.superclass.initComponent.apply(this, arguments);
		
	}

});

Ext.reg('o-editsnmpgrid', OpenNMS.ux.EditableSNMPInterfaceGrid);
var pagesize = 10;

function domainGridInitView(elementId, data, url){
	
}

function nodeSNMPReportsInitView(elementId, snmpNodeData, urlTemplate){
	
	var snmpFields = [
				  {name:'value', mapping:'value'},
	      		  {name:'id', mapping:'id'},
	      		  {name:'type', mapping:'type'}
	      		 ]
	 
	var snmpStore = new Ext.data.Store({
		autoLoad:true,
		proxy: new OpenNMS.ux.LocalPageableProxy(snmpNodeData, pagesize),
		reader:new Ext.data.JsonReader({id:"resources", root:"records", totalProperty:"total"}, snmpFields)
	});

	
	
	var snmpNodeGrid = new OpenNMS.ux.PageableGrid({
		id:"resourceGrid",
		columns:[{
		 			id: 'resource',
		 			header :'Nodes',
		 			align :'left'
		 		}],
		selectionModel:new Ext.grid.RowSelectionModel({
			singleSelect:true
		}),
		pageSize:pagesize,
		store:snmpStore,
		width:"100%",
		height:250,
		region:'west',
		enableDragDrop: true,
		stripeRows: true,
		onDoubleClick:submitSnmpNodeChoice
		
	});
	
	
	var snmpNodePanel = new Ext.Panel({
		autoWidth:true,
		border:false,
		height:300,
		items:[
				snmpNodeGrid,
				{
					xtype:"panel",
					border:false,
					height:10
				},{
					xtype:"button",
					text:"Submit",
					style:'padding-top:10px;',
					handler:submitSnmpNodeChoice
				}
				
		],
		
		renderTo:elementId
	});
	
	function submitSnmpNodeChoice(){
		if(snmpNodeGrid.getSelectionModel().getSelected() != undefined){
			var urlTpl = new Ext.XTemplate(urlTemplate);
			var url = urlTpl.apply(snmpNodeGrid.getSelectionModel().getSelected().data);
			window.location = url;
		}else{
			alert('Please Select a Node to Continue');
		}
	}
}

function customizedReportsInitView(elementId, data, url){
	
}
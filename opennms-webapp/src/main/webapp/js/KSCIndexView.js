var pagesize = 10;

function domainGridInitView(elementId, domainData, urlTemplate){
	
	var domainGrid = new OpenNMS.ux.ResourcesPageableGrid({
		id:"domainResourceGrid",
		pageSize:pagesize,
		columns:[{
		 			id: 'resource',
		 			header :'Reports',
		 			align :'left'
		 		}],
		width:"100%",
		height:250,
		data:domainData,
		onDoubleClick:submitSnmpNodeChoice
		
	});
	
	
	var domainPanel = new Ext.Panel({
		autoWidth:true,
		border:false,
		height:300,
		items:[
				domainGrid,
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
	
	function submitDomainChoice(){
		if(domainGrid.getSelectionModel().getSelected() != undefined){
			var urlTpl = new Ext.XTemplate(urlTemplate);
			var url = urlTpl.apply(domainGrid.getSelectionModel().getSelected().data);
			window.location = url;
		}else{
			alert('Please select a node to continue.');
		}
	}
}

function nodeSNMPReportsInitView(elementId, snmpNodeData, urlTemplate){

	var snmpNodeGrid = new OpenNMS.ux.ResourcesPageableGrid({
		id:"resourceGrid",
		pageSize:pagesize,
		columns:[{
		 			id: 'resource',
		 			header :'Reports',
		 			align :'left'
		 		}],
		width:"100%",
		height:250,
		data:snmpNodeData,
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
			alert('Please select the node that you would like to report on.');
		}
	}
}

function customizedReportsInitView(elementId, customData, urlTemplate){
	
	var customResourceGrid = new OpenNMS.ux.ResourcesPageableGrid({
		id:"customResourceGrid",
		pageSize:pagesize,
		columns:[{
		 			id: 'resource',
		 			header :'Reports',
		 			align :'left'
		 		}],
		width:"100%",
		height:250,
		data:customData, 
		onDoubleClick:submitCustomChoice
	});
	
	
	var customResourcePanel = new Ext.Panel({
		autoWidth:true,
		border:false,
		height:415,
		items:[
				customResourceGrid,
				{
					xtype:"panel",
					border:false,
					height:10
				},{
					xtype:"radiogroup",
					fieldLabel: 'radio',
					id:"radioGroup",
					columns:1,
					items:[
						{boxLabel: 'View', id:'viewRB', name: 'rb-auto', inputValue: 1, checked: true},
	                    {boxLabel: 'Customize', id:'customizeRB', name: 'rb-auto', inputValue: 2},
	                    {boxLabel: 'Create New', id:'createNewRB', name: 'rb-auto', inputValue: 3},
	                    {boxLabel: 'Create New from Existing', id:'createNewfromExistingRB', name: 'rb-auto', inputValue: 4},
	                    {boxLabel: 'Delete', id:'deleteRB', name: 'rb-auto', inputValue: 5}
						
					]
				},{
					xtype:"button",
					text:"Submit",
					handler:submitCustomChoice	
				}
				
		],
		
		renderTo:elementId
	});
	
	function submitCustomChoice(){
		var reportAction = {};
		if(Ext.getCmp('viewRB').checked){
			reportAction.action = "View";
		}else if(Ext.getCmp('customizeRB').checked){
			reportAction.action = "Customize";
		}else if(Ext.getCmp('createNewRB').checked){
			reportAction.action = "Create";
		}else if(Ext.getCmp('createNewfromExistingRB').checked){
			reportAction.action = "CreateFrom";
		}else if(Ext.getCmp('deleteRB').checked){
			reportAction.action = "Delete";
		}
		
		var urlTpl = new Ext.XTemplate(urlTemplate);
		var url = urlTpl.apply(reportAction);
		if(reportAction.reportId){
			url += "&report=" + reportAction.reportId;
		}
		
		if(customResourceGrid.getSelectionModel().getSelected() != undefined){
			url += "&report=" + customResourceGrid.getSelectionModel().getSelected().data.id;
			window.location = url;
		}else{
			if(reportAction.action == "Create"){
				window.location = url;
			}else{
				alert('No reports are selected.  Please click on a report title to make a report selection.');	
			}
		}
	};
	
	function navigateToURL(url){
		window.location = url;
	};
}

Ext.BLANK_IMAGE_URL = "extJS/resources/images/default/s.gif";

function standardResourceViewInit(elementId, dataArray, urlTemplate){
	alert("urlTemplate: " + urlTemplate);
	var pagesize = 10;
	var fields = [
				  {name:'value', mapping:'value'},
	      		  {name:'id', mapping:'id'},
	      		  {name:'type', mapping:'type'}
	      		 ]
	 
	var simpleStore = new Ext.data.Store({
		autoLoad:true,
		proxy: new OpenNMS.ux.LocalPageableProxy(dataArray, pagesize),
		reader:new Ext.data.JsonReader({id:"resources", root:"records", totalProperty:"total"}, fields)
	});

	
	
	var resourcesGrid = new OpenNMS.ux.PageableGrid({
		id:"resourceGrid",
		columns:[{
		 			id: 'resource',
		 			header :'Resources',
		 			align :'left'
		 		}],
		selectionModel:new Ext.grid.RowSelectionModel({
			singleSelect:true
		}),
		pageSize:pagesize,
		store:simpleStore,
		width:"100%",
		height:250,
		region:'west',
		enableDragDrop: true,
		stripeRows: true,
		onDoubleClick:startAction
		
	});
	
	
	var gridPanel = new Ext.Panel({
		autoWidth:true,
		border:false,
		height:300,
		items:[
				resourcesGrid,
				{
					xtype:"panel",
					border:false,
					height:10
				},{
					xtype:"button",
					text:"Start",
					style:'padding-top:10px;',
					handler:startAction
				}
				
		],
		
		renderTo:elementId
	});
	
	function startAction(){
		if(resourcesGrid.getSelectionModel().getSelected() != undefined){
			var urlTpl = new Ext.XTemplate(urlTemplate);
			var url = urlTpl.apply(resourcesGrid.getSelectionModel().getSelected().data);
			window.location = url;
		}else{
			alert("Please Select a Resource");
		}
	};
	
};

function customResourceViewInit(elementId, dataArray, urlTemplate){
	var pagesize = 10;
	var fields = [
				  {name:'value', mapping:'value'},
	      		  {name:'id', mapping:'id'},
	      		  {name:'type', mapping:'type'}
	      		 ]
	 
	var simpleStore = new Ext.data.Store({
		autoLoad:true,
		proxy: new OpenNMS.ux.LocalPageableProxy(dataArray, pagesize),
		reader:new Ext.data.JsonReader({id:"resources", root:"records", totalProperty:"total"}, fields)
	});

	
	
	var resourcesGrid = new OpenNMS.ux.PageableGrid({
		id:"resourceGrid",
		columns:[{
		 			id: 'resource',
		 			header :'Resources',
		 			align :'left'
		 		}],
		selectionModel:new Ext.grid.RowSelectionModel({
			singleSelect:true
		}),
		pageSize:pagesize,
		store:simpleStore,
		width:"100%",
		height:250,
		region:'west',
		enableDragDrop: true,
		stripeRows: true,
		onDoubleClick:startAction
		
	});
	
	
	var gridPanel = new Ext.Panel({
		autoWidth:true,
		border:false,
		height:300,
		items:[
				resourcesGrid,
				{
					xtype:"panel",
					border:false,
					height:10
				},{
					xtype:"button",
					text:"Start",
					style:'padding-top:10px;',
					handler:startAction
				}
				
		],
		
		renderTo:elementId
	});
	
	function startAction(){
		if(resourcesGrid.getSelectionModel().getSelected() != undefined){
			var urlTpl = new Ext.XTemplate(urlTemplate);
			var url = urlTpl.apply(resourcesGrid.getSelectionModel().getSelected().data);
			window.location = url;
		}else{
			alert("Please Select a Resource");
		}
	};
	
};
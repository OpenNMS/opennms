Ext.BLANK_IMAGE_URL = "extJS/resources/images/default/s.gif";

function chooseResourceViewInit(elementId, dataArray, destURL){
	var pagesize = 20;
	var fields = [
				  {name:'value', mapping:'value'},
	      		  {name:'id', mapping:'id'},
	      		  {name:'type', mapping:'type'}
	      		 ]
	 
	var simpleStore = new Ext.data.Store({
		autoLoad:true,
		proxy: new OpenNMS.ux.LocalPageableProxy(dataArray, pagesize, fields),
		reader:new Ext.data.JsonReader({id:"resources", root:"records", totalProperty:"total"}, fields)
	});

	var graphStore = new Ext.data.Store({
		//fields: fields
		proxy: new OpenNMS.ux.LocalPageableProxy({}, pagesize),
		reader:new Ext.data.JsonReader({id:"resources", root:"records", totalProperty:"total"}, fields)
	});
	
	var resourcesGrid = new OpenNMS.ux.PageableGrid({
		id:"resourceGrid",
		columns:[{
		 			id: 'resource',
		 			header :'Resources',
		 			align :'left'
		 		}],
		pageSize:pagesize,
		store:simpleStore,
		width:"40%",
		minWidth:250,
		height:500,
		region:'west',
		enableDragDrop: true,
		stripeRows: true,
		ddGroup: 'resourcesGridDDGroup',
		listeners:{
			'dblclick':{
				scope:this,
				fn:function(event){
					addRecordsToGraphGrid(resourcesGrid.getSelectionModel().getSelections());
				}
			}
		}
	});
	var button = new Ext.Button({
		text:"test"
	})
	resourcesGrid.addPagingBarButtons([{
										xtype:"button",
									 	text:"Add All",
									 	scope:this,
									 	handler:addAllRecords
									}]);

	var selectControls = new Ext.Panel({
		region:"center",
		layout:"absolute",
		bodyStyle:"background:transparent",
		items:[
			{
				xtype:'label',
				text:'Drag Resources on the left to the bin on the right to graph',
				style: 'font-family:"Lucida Grande",Verdana,sans-serif; font-size:70%; text-align:center;',
				width:"80%",
				x:"10%",
				y:"40%"
			}
		]
	})
	
	var graphGrid = new Ext.grid.GridPanel({
		id:"graphGrid",
		columns:[
		 		{
		 			id: 'id',
		 			header :'Selected Resources to Graph',
		 			align :'left'
		 		}],
		pageSize:pagesize,
		store: graphStore,
		width:"40%",
		minWidth:250,
		height:500,
		region:'east',
		viewConfig : {
		  autoFill: true,
		  forceFit: true
		},
		
		bbar:new Ext.Toolbar({
			 height:25,
			 items:[
			 	{
			 		xtype:'button',
			 		text:'Remove',
			 		cls:"x-btn-text-icon",
			 		icon:'images/trash.gif',
			 		scope:this,
			 		handler:removeSelectedRecord
			 	},"-",{
			 		xtype:'button',
			 		text:'Remove All',
			 		scope:this,
			 		handler:removeAllFromGraphGrid
			 	},"-",{
					xtype:"button",
					text:"Graph Selection",
					scope:this,
				 	handler:graphSelectedResources
				},"-",{
			 		xtype:'label',
			 		id:'graphResourceLabel',
			 		text:"Total 0"
			 	}
			 ]
		}),
		listeners:{
			'dblclick':{
				scope:this,
				fn:removeSelectedRecord
			}
		}
	});

	var gridPanel = new Ext.Panel({
		title:"Node Resources",
		autoWidth:true,
		border:false,
		height:500,
		layout:"border",
		items:[
				resourcesGrid,
				selectControls,
				graphGrid
		],
		
		renderTo:elementId
	});

	
	var resourcesGridDropTargetEl =  graphGrid.getView().el.dom.childNodes[0].childNodes[1];
	var resourcesGridDropTarget = new Ext.dd.DropTarget(resourcesGridDropTargetEl, {
		ddGroup    : 'resourcesGridDDGroup',
		copy       : true,
		notifyDrop : function(ddSource, e, data){
			
			// Generic function to add records.
			function addRow(record, index, allItems) {
				
				// Search for duplicates
				var foundItem = graphStore.find('value', record.data.value);
				
				// if not found
				if (foundItem  == -1) {
					addRecordsToGraphGrid(record);
					
					//Remove Record from the source
					//ddSource.grid.store.remove(record);
				}
			}

			// Loop through the selections
			Ext.each(ddSource.dragData.selections ,addRow);
			return(true);
		}
	});
	
	function addRecordsToGraphGrid(records){
		graphStore.add(records);
		updateTotalDisplayLabel();
	}
	
	function removeRecordsFromGraphGrid(records){
		graphStore.remove(records);
		updateTotalDisplayLabel();
	}
	
	function removeAllFromGraphGrid(){
		graphStore.removeAll();
		updateTotalDisplayLabel();
	}
	
	function updateTotalDisplayLabel(){
		var lbl = Ext.getCmp("graphResourceLabel");

		lbl.setText("Total " + graphStore.getCount());
	}
	
	function addSelectedRecord(){
		var selection = resourcesGrid.getSelectionModel().getSelections();
		addRecordsToGraphGrid(selection);
	}
	
	function removeSelectedRecord(){
		var selection = graphGrid.getSelectionModel().getSelected();
		if(selection != undefined){
			removeRecordsFromGraphGrid(selection);
		}else{
			alert("please select a resource to remove");
		}
	}
	
	function removeAllRecords(){
		removeAllFromGraphGrid();
	}
	
	function addAllRecords(){
		//simpleStore.proxy.getAllData()
		//alert(simpleStore.proxy.getAllData().toSource());
		removeAllFromGraphGrid();
		addRecordsToGraphGrid(simpleStore.proxy.getAllData());//simpleStore.getRange(0, simpleStore.getCount() - 1 ));
	}
	
	function graphSelectedResources(){
		if(getResourceIds() != "fail"){
			window.location = destURL + "?reports=all&" + getResourceIds();
		}else{
			alert("You need to select a resource to graph");
		}
	}
	
	function getResourceIds(){
		if(graphStore.getCount() > 0){
			var query = new Array();
			var records = graphStore.getRange(0, graphStore.getCount() - 1 );
			
			for(var i = 0; i < records.length; i++){
				query.push("resourceId=" + records[i].data.id);
			}
			
			var params = query.join("&");
			return params;
		}else{
			return "fail";
		}	
	}
	
};
Ext.namespace("OpenNMS.ux");
OpenNMS.ux.ResourcesPageableGrid = Ext.extend(OpenNMS.ux.PageableGrid, {
	
	fields:[
				  {name:'value', mapping:'value'},
	      		  {name:'id', mapping:'id'},
	      		  {name:'type', mapping:'type'}
	      	],
	      	
	columns:[{
		 			id: 'resource',
		 			header :'Resources',
		 			align :'left'
		 		}],
	
	data:{total:"0", records:[]},
	
	selectionModel:new Ext.grid.RowSelectionModel({
		singleSelect:true
	}),
	
	initComponent:function(){
		stripeRows = true;
		
		if(this.store == undefined){
			this.store = new Ext.data.Store({
				autoLoad:true,
				proxy: new OpenNMS.ux.LocalPageableProxy(this.data, this.pageSize),
				reader:new Ext.data.JsonReader({id:"resources", root:"records", totalProperty:"total"}, this.fields)
			});
		}
		
		OpenNMS.ux.ResourcesPageableGrid.superclass.initComponent.apply(this, arguments);
	}
	
});
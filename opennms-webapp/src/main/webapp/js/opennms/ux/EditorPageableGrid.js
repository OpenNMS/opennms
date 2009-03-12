Ext.namespace("OpenNMS.ux");
OpenNMS.ux.EditorPageableGrid = Ext.extend(Ext.grid.EditorGridPanel, {
	header:false,
	pageSize:20,
	width:'100%',
    displayInfo: true,
    border:true,
    displayMsg: 'Displaying topics {0} - {1} of {2}',
    emptyMsg: "No topics to display",
    viewConfig : {
	  autoFill: true,
	  forceFit: true,
	  scrollOffset:2
	},
	
	initComponent:function(){
		this.on('rowdblclick', this.onDoubleClick, this);
		var tpl = new Ext.XTemplate(this.urlTemplate);
		
		this.url = tpl.apply(this);
		
		if(this.store == undefined){
			this.store = new Ext.data.Store({
				proxy:new Ext.data.HttpProxy({
					method:"GET",
					url: this.url
				}),
				paramNames:{
				"start" : "offset",
				"limit" : "limit",
				"sort" : "orderby",
				"dir" : "dir"
				},
				autoLoad:true,
				reader:new Ext.data.XmlReader({ record:this.recordTag, totalRecords:"@totalCount" }, this.recordMap)
			});
		}
		
		this.store.on("update", this.updateRecord, this);
	

		Ext.apply(this,{
			bbar: new Ext.PagingToolbar({
			        pageSize: this.pageSize,
			        store: this.store,
			        displayInfo: this.displayInfo,
			        displayMag: this.displayMsg,
			        emptyMsg: this.emptyMsg,
			        items:[ '-' ]
			}),
			
			loadMask:true,
			stripeRows:true,
			viewConfig:this.viewConfig
			
		});
		
		OpenNMS.ux.EditorPageableGrid.superclass.initComponent.apply(this, arguments);
		
	},
	
	loadSearch:function(searchCriteria){
		this.store.baseParams = searchCriteria;
		this.store.reload();
	},
	
	addPagingBarButtons:function(items) {
		this.pagingBarButtons = items;
	},
	
	onRender:function(){
		OpenNMS.ux.EditorPageableGrid.superclass.onRender.apply(this, arguments);
		
		if(this.pagingBarButtons && this.pagingBarButtons.length > 0){
			this.getBottomToolbar().addButton(this.pagingBarButtons);
		}
	},
	
	onDoubleClick:function(event){
		
	},
	
	updateRecord:function(store, record, operation){
		
	}
});

Ext.reg('editpageablegrid', OpenNMS.ux.EditorPageableGrid);

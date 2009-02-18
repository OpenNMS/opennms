Ext.namespace("OpenNMS.ux")
OpenNMS.ux.PageableGrid = Ext.extend(Ext.grid.GridPanel, {
	header:false,
	pageSize:20,
    height:474,
	width:'100%',
    displayInfo: true,
    border:true,
    displayMsg: 'Displaying topics {0} - {1} of {2}',
    emptyMsg: "No topics to display",
    viewConfig : {
	  autoFill: true,
	},
	pagingBarButtons:[],
	
	initComponent:function(){
	
		var tpl = new Ext.XTemplate(this.urlTemplate);
		
		this.url = tpl.apply(this);
	
		this.store = new Ext.data.Store({
			proxy:new Ext.data.HttpProxy({
				method:"GET",
				url: this.url,
			}),
			paramNames:{
			"start" : "offset",
			"limit" : "limit",
			"sort" : "orderby",
			"dir" : "dir"
		},
		reader:new Ext.data.XmlReader({
			record:this.recordTag,
			totalRecords:"@totalCount"
		}, this.recordMap)
		});
	

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
			viewConfig:{
				autoFill:true
			}
			
		});
		
		OpenNMS.ux.PageableGrid.superclass.initComponent.apply(this, arguments);
		
	},
	
	initialLoad:function() {
		this.store.load({params:{start:0, limit:this.pageSize}});
	},
	
	addPagingBarButtons:function(items) {
		this.pagingBarButtons = items;
	},
	
	onRender:function(){
		OpenNMS.ux.PageableGrid.superclass.onRender.apply(this, arguments);
		
		if(this.pagingBarButtons.length > 0){
			this.getBottomToolbar().addButton(this.pagingBarButtons);
		}
		
		this.initialLoad();
	}
})

Ext.reg('opennmspageablegrid', OpenNMS.ux.PageableGrid);
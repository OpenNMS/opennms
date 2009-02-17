Ext.namespace("OpenNMS.ux")
OpenNMS.ux.PageableGrid = Ext.extend(Ext.grid.GridPanel, {
	header:false,
	offSet:0,
	limit:20,
    height:474,
	width:'100%',
    displayInfo: true,
    displayMsg: 'Displaying topics {0} - {1} of {2}',
    emptyMsg: "No topics to display",
	pagingBarButtons:[],
	
	initComponent:function(){
		alert('this.store = '+this.store);
		Ext.apply(this,{
			bbar: new Ext.PagingToolbar({
			        pageSize: this.limit,
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
	
	addPagingBarButtons:function(items) {
		this.pagingBarButtons = items;
	},
	
	load:function(url, params){
		this.store.proxy.conn.url = url;
		
		if(!params){
			this.store.load({params:{start:0, limit:this.limit}});
		}else{
			this.store.load({params:params});
		}
		
	},
	
	onRender:function(){
		OpenNMS.ux.PageableGrid.superclass.onRender.apply(this, arguments);
		
		if(this.pagingBarButtons.length > 0){
			this.getBottomToolbar().addButton(this.pagingBarButtons);
		}
	}
})

Ext.reg('opennmspageablegrid', OpenNMS.ux.PageableGrid);
Ext.namespace("OpenNMS.ux")
OpenNMS.ux.PageableGrid = Ext.extend(Ext.grid.GridPanel, {
	header:false,
	title:'IP Interfaces',
	baseUrl:"rest/nodes/",
	offSet:0,
	limit:20,
	xmlNodeToRecord:'ipInterface',
	record:[
				{name:"interfaceId", mapping:"interfaceId"},
				{name:"ipAddress", mapping:"ipAddress"},
				{name:'hostName', mapping:'ipHostName'},
				{name:'ifIndex', mapping:'ifIndex'},
				{name:"isManaged", mapping:"isManaged"},
				{name:'capsdPoll', mapping:'ipLastCapsdPoll'},
				{name:"snmpInterface", mapping:"snmpInterface"}
			],
	columns:[
		{
			header :'Interface Id',
			name :'interfaceId',
			width:100,
			sortable :true,
			align :'left'
		},{
			header :'ip Address',
			name :'ipAddress',
			width :100,
			sortable :true,
			align :'left'
		},{
			header:'ip Host Name',
			name:'hostName',
			width:200,
			align:'left',
		},{
			header:'ifIndex',
			name:'ifIndex',
			width:75,
			align:'left',
			hidden:true
		},{
			header :'isManaged',
			name :'isManaged',
			width :75,
			sortable :true,
			align :'left',
			hidden:true
		},{
			header:'Last Capsd Poll',
			name:'capsdPoll',
			width:150,
			hidden:true,
			align:'left'
		},{
			header:'Node',
			name:'node',
			width:20,
			hidden:true,
			align:'left'
		}
	],
	
	pagingBarButtons:[],
	
	
	initComponent:function(){
		
		var dataStore = new Ext.data.Store({
				baseParams:{limit:this.limit},
				proxy:new Ext.data.HttpProxy({
					method:"GET",
					extraParams:{
						limit:this.limit
					}
				}),
				reader:new Ext.data.XmlReader({
					record:this.xmlNodeToRecord,
					totalRecords:"@totalCount"
				}, this.record)
			});
			
		var iconPath = "extJS/resources/images/onmsTheme/magnifier.png";
		
		Ext.apply(this,{
			store:dataStore,
			
			columns:this.columns,
			
			width:'100%',
	        header:this.header,
	        height:474,
			bbar: new Ext.PagingToolbar({
			        pageSize: this.limit,
			        store: dataStore,
			        displayInfo: true,
			        displayMsg: 'Displaying topics {0} - {1} of {2}',
			        emptyMsg: "No topics to display",
			        
			        items:[
			            '-', this.pagingBarButtons]
			
			}),
			
			loadMask:true,
			stripeRows:true,
			
			viewConfig:{
				autoFill:true
			}
			
		});
		
		OpenNMS.ux.PageableGrid.superclass.initComponent.apply(this, arguments);
		
	},
	
	addPagingBarButton:function(item) {
		this.getBottomToolbar().addButton(item);
	},
	
	load:function(url, params){
		this.store.proxy.conn.url = url;
		
		if(!params){
			this.store.load({params:{start:0, limit:this.limit}});
		}else{
			params.start = 0;
			params.limit = this.limit;
			this.store.load({params:params});
		}
		
	},
	
	loadDataFromXml:function(url){
		this.load(url);
	},
	
	onRender:function(){
		
		OpenNMS.ux.PageableGrid.superclass.onRender.apply(this, arguments);
	}
})

Ext.reg('opennmspageablegrid', OpenNMS.ux.PageableGrid);
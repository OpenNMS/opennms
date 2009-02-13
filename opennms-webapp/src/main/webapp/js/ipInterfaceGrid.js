IpInterfaceGrid = Ext.extend(Ext.grid.GridPanel, {
	renderTo:'grid-panel',
	
	initComponent:function(){
		
		Ext.apply(this,{
			store:new Ext.data.Store({
				baseParams:{limit:20},
				proxy:new Ext.data.HttpProxy({
					method:"GET",
					extraParams:{
						limit:20
					}
				}),
				reader:new Ext.data.XmlReader({
					record:"ipInterface",
					totalRecords:"@totalCount"
				}, [
						{name:"interfaceId", mapping:"interfaceId"},
						{name:"ipAddress", mapping:"ipAddress"},
						{name:'hostName', mapping:'ipHostName'},
						{name:'ifIndex', mapping:'ifIndex'},
						{name:"isManaged", mapping:"isManaged"},
						{name:'capsdPoll', mapping:'ipLastCapsdPoll'},
						{name:"snmpInterface", mapping:"snmpInterface"}
					])
			}),
			
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
			
			width:'auto',
	        header:false,
	        height:474,
			bbar: new Ext.PagingToolbar({
			        pageSize: 20,
			        store: dataStore,
			        displayInfo: true,
			        displayMsg: 'Displaying topics {0} - {1} of {2}',
			        emptyMsg: "No topics to display",
			        
			        items:[
			            '-', new Ext.Button({
			            	handler:function(){
			            		gridWindow.add(new Ext.form.FormPanel({
			            			title:"search Criteria",
			            		}));
			            	},
			            	text:"  ",
			            	icon:iconPath,
			            })]
			
			}),
			
			loadMask:true,
			stripeRows:true,
			
			viewConfig:{
				autoFill:true
			}
			
		});
		
		IpInterfaceGrid.superclass.initComponent.apply(this, arguments);
		
	},
	
	onRender:function(){
		
		IpInterfaceGrid.superclass.onRender.apply(this, arguments);
	}
})

Ext.reg('ipinterfacegrid', IpInterfaceGrid);
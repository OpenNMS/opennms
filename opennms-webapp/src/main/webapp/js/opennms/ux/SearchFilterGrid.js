/**
 * @author thedesloge
 */
Ext.namespace("OpenNMS.ux");
OpenNMS.ux.SearchFilterGrid = Ext.extend(Ext.Container, {
	layout: 'absolute',
	
	autoEl: {
        cls: 'x-filter-condition'
    },
	
	contentEl:'searchCriteria',
	
	searchFormItems:[
		{
			xtype:'textfield',
            fieldLabel: 'Token Field',
            name: 'Bogus',
            allowBlank:false
        }],
	
	//Public properties associated with Pageable Panel
	gridTitle:'IP Interfaces',
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
	
	initComponent:function(){
		
		Ext.apply(this,{
			items: [
				{
					xtype:'opennmspageablegrid',
					id:'pagingGrid',
					title:this.gridTitle,
					xmlNodeToRecord:this.xmlNodeToRecord,
					record:this.record,
					columns:this.columns,
					paginBarButtons:[
						new Ext.Button({
							id:'searchCritBtn',
							text:'Add Search Criteria',
							listeners:{
								'click':{
									fn:this.searchCriteriaClickHandler,
									scope:this,
								}
							}
						})
					]
				},{
					xtype:'form',
					id:'searchCrit',
					labelWidth:100,
					frame:true,
					title:'Simple Form',
					defaults:{width:230},
					//defaultType:'textfield',
					listeners:{
						'render':{
							fn:this.searchFilterRenderHandler,
							scope:this,
						}
					},
					//hidden:true,
					width:'100%',
					items:this.searchFormItems,
					buttons: [{
					            text: 'Search',
								listeners:{
									'click':{
										fn:this.searchBtnClickHandler,
										scope:this
									}
								}
					        },{
					            text: 'Close',
								listeners:{
									'click':{
										fn:this.closeBtnClickHandler,
										scope:this
									}
								}
					        }]
				}
			],

			
		})
		
		OpenNMS.ux.SearchFilterGrid.superclass.initComponent.apply(this, arguments);
	},
	
	onRender:function(){
		OpenNMS.ux.SearchFilterGrid.superclass.onRender.apply(this, arguments);
		
	},
	
	afterRender:function(){
		OpenNMS.ux.SearchFilterGrid.superclass.afterRender.apply(this, arguments);
		
	},
	
	searchCriteriaClickHandler:function(event){
		this.showSearchCrit();
	},
	
	searchFilterRenderHandler:function(event){
		var searchForm = Ext.get('searchCrit');
		searchForm.setVisible(false);
	},
	
	searchBtnClickHandler:function(event,target){
		var searchForm = Ext.getCmp('searchCrit');//document.getElementById(this.contentEl);
		var basicForm = searchForm.getForm();
		var params = basicForm.getValues();
		alert(params.toSource());
		var pagingGrid = Ext.getCmp('pagingGrid');
		pagingGrid.load("xml/node-147-ipinterfaces.xml", params);
		
	},
	
	closeBtnClickHandler:function(event, target){
		this.hideSearchCrit();
	},
	
	hideSearchCrit:function(){
		Ext.get('searchCrit').hide(true);
	},
	
	showSearchCrit:function(){
		var pagingGrid = Ext.get('pagingGrid');
		var searchForm = Ext.get('searchCrit');
		var formHeight = searchForm.getSize().height;
		searchForm.setY(pagingGrid.getSize().height - formHeight - 25)
		searchForm.show(true);
	},
	
	sayHello:function(event, target){
		alert('saying hello');
	}
	
})

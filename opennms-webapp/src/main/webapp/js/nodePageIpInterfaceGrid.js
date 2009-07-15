var grid;
var physicalInterfaceGrid;
var iconPath = "extJS/resources/images/onmsTheme/magnifier.png";
var popupWindow;

Ext.BLANK_IMAGE_URL = 'extJS/resources/images/default/s.gif';

Ext.onReady(function(){
	nodePageGridInit();
	initEventView();
})

function nodePageGridInit(){
	
	grid = new Ext.grid.GridPanel({
		title:'IP Interfaces',
		id:'nodeInterfaceGrid',
        store: dataStore,
        colModel:colModel,
        width:'auto',
        header:false,
        height:474,
        renderTo:'interfaces-panel',
		bbar: ipInterfacePagingBar,
		loadMask:true,
		stripeRows:true,
		
		viewConfig:{
			autoFill:true
		}
	})
	
	grid.on("rowdblclick", function(grid) {
		var selId = grid.getSelectionModel().getSelected().data.interfaceId;
		window.location = 'element/interface.jsp?ipinterfaceid=' + selId;   
    });
    
    physicalInterfaceGrid = new Ext.grid.GridPanel({
    	title:'Physical Interfaces',
		id:'nodePhysicalInterfaceGrid',
        store: physicalAddrStore,
        colModel:physicalAddrColModel,
        width:'auto',
        header:false,
        height:474,
		loadMask:true,
		stripeRows:true,
		bbar: new Ext.PagingToolbar({
        	pageSize: 20,
        	store: physicalAddrStore,
        	displayInfo: true,
        	displayMsg: 'Displaying topics {0} - {1} of {2}',
        	emptyMsg: "No topics to display",
        
	        items:[
	            '-', new Ext.Button({
	            	handler:function(){
	            		if(!popupWindow){
	            			popupWindow = new Ext.Window({
	            				applyTo     : Ext.getBody(),
                				layout      : 'fit',
				                width       : 500,
				                height      : 300,
				                closeAction :'hide',
				                plain       : true,
				                items:[
				                	new Ext.Button({
				                		text:"a button"
				                	})
				                ]
	            				
	            			})
	            		}
	            		popup.show(grid); 
	            	},
	            	text:"  ",
	            	icon:iconPath,
	            })]

		}),
		
		viewConfig:{
			autoFill:true
		}
    });
    
    physicalInterfaceGrid.on("rowdblclick", function(grid) {
		var selId = grid.getSelectionModel().getSelected().data.interfaceId;
		alert("row id: " + selId);
		//window.location = 'element/interface.jsp?ipinterfaceid=' + selId;   
    });
	
	var panel = new Ext.TabPanel({
		applyTo:'interfaces-panel',
		activeTab:0,
		width:'auto',
		autoHeight:true,
		bodyBorder:false,
		border:false,
		items:[
			grid,
			physicalInterfaceGrid
			
		],
		/*tbar: [
            new Ext.FormPanel({
				labelWidth: 108, // label settings here cascade unless overridden
		        frame:true,
		        bodyStyle:'padding:5px 5px 0',
				title:"<p style='color:#000000;'>SEARCH CRITERIA</p>",
				cls:'x-panel-custom-color',
				collapsible:true,
				collapsed:true,
				header:true,
				width:550,
				autoHeight:true,

				items: [{
		            layout:'column',
		            items:[{
		                columnWidth:.5,
		                layout: 'form',
		                items: [{
		                    xtype:'textfield',
		                    fieldLabel: 'TCP/IP Address Like',
		                    name: 'ipLikeCriteria',
		                    id:'ipLike',
							emptyText:'*.*.*.*',
		                    anchor:'95%'
		                }, {
		                    xtype:'textfield',
		                    fieldLabel: 'Name Containing',
		                    name: 'nameContainingCriteria',
		                    id:'nameContaining',
		                    anchor:'95%'
		                }]
		            },{
		                columnWidth:.5,
		                layout: 'form',
		                items: [{
		                    xtype:'textfield',
		                    fieldLabel: 'Mac Address Like',
		                    name: 'macLike',
		                    id:'macLike',
		                    anchor:'95%'
		                },{
		                    xtype:'combo',
               				fieldLabel: 'Providing Service',
							store:["one", "two", "three"],
               				name: 'providedService',
               				id:'providedService',
							anchor: '95%'
		                }]
		            }]
		        }],
				
				buttons: [{
		            text: 'Reset',
					handler:interfaceGridResetButtonHandler,
		        },{
		            text: 'Search',
					handler:interfaceSearchButtonHandler,
		        }]


			})
        ],*/

	})
	
	panel.on("tabChange", function(tabPanel, panel){
    	//ipInterfacePagingBar.show();
    	
    });
	
	physicalAddrStore.proxy.on("loadexception", function(object, options, response, e){
		alert("error: " + e);
		//alert("there was a load exception in the physicalAddressStore");
	})
	
	var getParams = document.URL.split("?");
	if(getParams.length > 1){
		urlParams = Ext.urlDecode(getParams[1]);
		loadNodeInterfaces(urlParams.node);
	}else{
		loadNodeInterfaces(-1);
	}
};

var record = new Ext.data.Record.create([
	{name:"interfaceId", mapping:"@id"},
	{name:"ipAddress", mapping:"ipAddress"},
	{name:'ipHostName', mapping:'hostName'},
	{name:'ifIndex', mapping:'@ifIndex'},
	{name:"isManaged", mapping:"@isManaged"},
	{name:'capsdPoll', mapping:'lastCapsdPoll'},
	{name:"snmpInterface", mapping:"snmpInterface"}
]);
	
var dataStore = new Ext.data.Store({
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
	}, record)
})

var colModel = new Ext.grid.ColumnModel([
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
		name:'ipHostName',
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
]);

var physicalAddrStore = new Ext.data.Store({
	baseParams:{limit:20},
	proxy:new Ext.data.HttpProxy({
		method:"GET",
		extraParams:{
			limit:20
		}
	}),
	reader:new Ext.data.XmlReader({
		record:"snmpInterface",
		totalRecords:"@totalCount"
	}, [
		{name:"theId", mapping:"id"},
		{name:"ifAdminStatus", mapping:"ifAdminStatus"},
		{name:"ifDescr", mapping:"ifDescr"},
		{name:"ifIndex", mapping:"ifIndex"},
		{name:"ifName", mapping:"ifName"},
		{name:"ifOperStatus", mapping:"ifOperStatus"},
		{name:"ifSpeed", mapping:"ifSpeed"},
		{name:"ifType", mapping:"ifType"},
		{name:"ipAddress", mapping:"ipAddress"},
		{name:"physAddr", mapping:"physAddr"}])
});

var physicalAddrColModel = new Ext.grid.ColumnModel([
	{
		header:"ID",
		name:"theId",
		width:50,
		sortable:false,
		align:"left"
	},{
		header :'ifAdminStatus',
		name :'ifAdminStatus',
		width :100,
		sortable :true,
		align :'left'
	},{
		header:"ifDescr",
		name:"ifDescr",
		width:100,
		sortable:false,
		align:"left"
	},{
		header:"ifIndex",
		name:"ifIndex",
		hidden:true,
		width:100,
		align:"left"
	},{
		header:"ifName",
		name:"ifName",
		hidden:true,
		width:100,
		align:"left"	
	},{
		header:"ifOperStatus",
		name:"ifOperStatus",
		hidden:true,
		width:100,
		align:"left"
	},{
		header:"ifSpeed",
		name:"ifSpeed",
		hidden:true,
		width:100,
		align:"left"
	},{
		header:"ifType",
		name:"ifType",
		hidden:true,
		width:100,
		align:"left"
	},{
		header:"ipAddress",
		name:"ipAddress",
		hidden:true,
		width:100,
		align:"left"
	},{
		header:"physAddr",
		name:"physAddr",
		hidden:true,
		width:100,
		align:"left"
	}
]);



var ipInterfacePagingBar = new Ext.PagingToolbar({
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

});

var filterMenuItems = [
    new Ext.menu.CheckItem({ 
        text: 'Ip Address', 
        checked: true, 
        group: 'filter',
        id: 'ipAddress',
        checkHandler: onFilterItemCheck 
    }),
    new Ext.menu.CheckItem({ 
        text: 'Interface Id', 
        checked: false, 
        group: 'filter',
        id: 'interfaceId',
        checkHandler: onFilterItemCheck    
    }),
    new Ext.menu.CheckItem({ 
        text: 'Is Managed', 
        checked: false, 
        group: 'filter',
        id: 'isManaged',
        checkHandler: onFilterItemCheck    
    })
];
var filterMenu = new Ext.menu.Menu({
    id: 'filterMenu',
    items: filterMenuItems
});

function loadNodeInterfaces(nodeId){
	if(nodeId != -1){
		var baseUrl = "rest/nodes/" + nodeId;
		dataStore.proxy.conn.url = baseUrl + "/ipinterfaces"; //"xml/node-147-ipinterfaces.xml"; 
		dataStore.load({params:{start:0, limit:20}});
	
		physicalAddrStore.proxy.conn.url = baseUrl + "/snmpinterfaces";
		physicalAddrStore.load({params:{start:0, limit:20}});
	}else{
		alert('not yet implemented on getting a node from one particular something');
	}
};

function updateFilter(field, newValue, oldValue){
	var filterCol = filterMenuItems.filter(function(element, index, array) {
	        return element.checked;
	    })[0].id;
	dataStore.filter(filterCol, newValue.value, true, false);
	var total = dataStore.getTotalCount();
	var count = dataStore.getCount();
	Ext.get('recordslabel').update('displaying ' + count + " of " + total);
}

//Event Handlers
function interfaceGridResetButtonHandler(){
	Ext.getCmp('ipLike').update("");
	Ext.getCmp('nameContaining').update("");
	Ext.getCmp('macLike').update("");
}

function interfaceSearchButtonHandler(){
	var searchParams = new Object();
	searchParams.start = 0;
	if(Ext.getCmp('ipLike').getValue() != ""){
		searchParams.ipAddress = Ext.getCmp('ipLike').getValue();
	}		
	searchParams.comparator='ge';				
	dataStore.load({params:searchParams});
};

function onFilterItemCheck(item, checked){
	if(checked) {
        Ext.get('filterlabel').update('['+item.text+']');    
    }
}
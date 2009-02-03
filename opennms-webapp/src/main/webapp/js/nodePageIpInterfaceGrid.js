var grid;

Ext.onReady(function(){
	nodePageGridInit();
	initEventView();
})

function nodePageGridInit(){
	
	var getParams = document.URL.split("?");
	if(getParams.length > 1){
		urlParams = Ext.urlDecode(getParams[1]);
	}
	
	grid = new Ext.grid.GridPanel({
        store: dataStore,
        colModel:colModel,
        width:'auto',
        height:300,
        renderTo:'grid-panel',
		tbar:tb,
		bbar: ipInterfacePagingBar,
		loadMask:true
	})
	
	grid.on("rowdblclick", function(grid) {
		//eventGrid
		var selId = grid.getSelectionModel().getSelected().data.interfaceId;
		window.location = 'element/interface.jsp?ipinterfaceid=' + selId;   
    });	
	
	tb.add({
	    text: 'filter by ',
	    tooltip: 'set column for search.',
	    icon: 'find.png',
	    cls: 'x-btn-text-icon btn-search-icon',
	    menu: filterMenu
	});
	
	var filterlabel = tb.addDom({
	    tag: 'div',
	    id: 'filterlabel',
        style:'color:#66a6f9;padding:0 4px;width:60px;text-align:center;'
    });
	
	tb.addSeparator();
	
	var filterTextF = tb.addField(new Ext.form.TextField({
		id:"filterTF",
		name:"search feild",
		emptyText:"search...",
	}));
	
	Ext.get('filterTF').on('change', updateFilter, this);
	
	tb.addSeparator();
	
	tb.addButton(new Ext.Button({
		text:"Search Now"
	}))
	
	tb.addSeparator();
	
	tb.addDom({
		tag: 'div',
       id: 'recordslabel',
       style: 'float:right;'
	})
	
	var panel = new Ext.Panel({
		applyTo:'grid-panel',
		width:554,
		title:'INTERFACES',
		autoHeight:true,
		bodyBorder:false,
		border:false,
		items:[
			grid
		],
		tbar: [
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
					handler:function(){
						Ext.get('ipLike').text = "";
						Ext.get('nameContaining').text = '';
						Ext.get('macLike').text = '';
					}
		        },{
		            text: 'Search',
					handler:function(){
						var searchParams = new Object();
						searchParams.label='mrmakay';
						
						
						dataStore.load({params:searchParams});
					}
		        }]


			})
        ],

	})
	
	loadNodeInterfaces(urlParams.node);

};

var record = new Ext.data.Record.create([
	{name:"", mapping:""},
	{name:"interfaceId", mapping:"interfaceId"},
	{name:"ipAddress", mapping:"ipAddress"},
	{name:'hostName', mapping:'ipHostName'},
	{name:'ifIndex', mapping:'ifIndex'},
	{name:"isManaged", mapping:"isManaged"},
	{name:'capsdPoll', mapping:'ipLastCapsdPoll'},
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
	new Ext.grid.RowNumberer({
		width:30
	}),
	{
		header :'Interface Id',
		name :'interfaceId',
		width:100,
		sortable :true,
		align :'center'
	},{
		header :'ip Address',
		name :'ipAddress',
		width :100,
		sortable :true,
		align :'center'
	},{
		header:'ip Host Name',
		name:'hostName',
		width:200,
		align:'center',
	},{
		header:'ifIndex',
		name:'ifIndex',
		width:75,
		align:'center',
		hidden:true
	},{
		header :'isManaged',
		name :'isManaged',
		width :75,
		sortable :true,
		align :'center',
		hidden:true
	},{
		header:'Last Capsd Poll',
		name:'capsdPoll',
		width:150,
		hidden:true,
		align:'center'
	},{
		header:'Node',
		name:'node',
		width:20,
		hidden:true,
		align:'center'
	}
]);

var ipInterfacePagingBar = new Ext.PagingToolbar({
        pageSize: 20,
        store: dataStore,
        displayInfo: true,
        displayMsg: 'Displaying topics {0} - {1} of {2}',
        emptyMsg: "No topics to display",
        
        items:[
            '-', { }]

});

var tb = new Ext.Toolbar({});

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

function onFilterItemCheck(item, checked){
	if(checked) {
        Ext.get('filterlabel').update('['+item.text+']');    
    }
}

function loadNodeInterfaces(nodeId){
	dataStore.proxy.conn.url ="rest/nodes/" + nodeId + "/ipinterfaces"; //"xml/node-147-ipinterfaces.xml"; 
	dataStore.load({params:{start:0, limit:20}});
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
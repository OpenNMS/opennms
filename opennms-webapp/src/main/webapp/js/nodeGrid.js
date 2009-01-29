var grid;
var urlParams;
var panel;
Ext.onReady(function(){
	
	var getParams = document.URL.split("?");
	if(getParams.length > 1){
		urlParams = Ext.urlDecode(getParams[1]);
	}
	
    // create the grid
    grid = new Ext.grid.GridPanel({
    	title:'Node List',
        store: dataStore,
        colModel:colModel,
        width:'auto',
        height:600,
        loadMask:new Ext.LoadMask(Ext.getBody(), {msg:'Please wait...'}),
        bbar: pagingBar,
        deferRowRender:false,
        renderTo:'node-grid'
    });
    
    grid.getSelectionModel().on('rowselect', function(sm, rowIdx, r){
    	alert(r.data.nodeId + ": node id and nodeLabel: " + r.data.nodeLabel);
    	if(r.data.nodeId > 0){
    		window.location = "element/node.jsp?node=" + r.data.nodeId;	
    	};
    })
	
    /*var filterMenuItems = [
	    new Ext.menu.CheckItem({ 
            text: 'Company', 
            checked: true, 
            group: 'filter',
            id: 'company',
            //checkHandler: onFilterItemCheck 
        }),
	    new Ext.menu.CheckItem({ 
            text: 'Price', 
            checked: false, 
            group: 'filter',
            id: 'price',
            //checkHandler: onFilterItemCheck    
        }),
        new Ext.menu.CheckItem({ 
            text: 'Change', 
            checked: false, 
            group: 'filter',
            id: 'change',
            //checkHandler: onFilterItemCheck    
        }),
        new Ext.menu.CheckItem({ 
            text: '% Change', 
            checked: false, 
            group: 'filter',
            id: 'pctchange',
            //checkHandler: onFilterItemCheck    
        })
    ];
    var filterMenu = new Ext.menu.Menu({
	    id: 'filterMenu',
	    items: filterMenuItems
    });*/
    
	dataStore.on("load", checkFilters, this);
	dataStore.load();
})

var record = new Ext.data.Record.create([
	{name:"nodeLabel", mapping:"label"},
	{name:"nodeId", mapping:"nodeId"},
	{name:"type", mapping:"type"},
	{name:"label_Source", mapping:"labelSource"},
	{name:"created", mapping:"createTime"},
	{name:"last-CapsdPoll", mapping:"lastCapsdPoll"},
	{name:"blank", mapping:"assetRecord.node"}
]);
	
var dataStore = new Ext.data.Store({
	baseParams:{limit:20},
	proxy:new Ext.data.HttpProxy({
		url:"rest/nodes",//'xml/node-147.xml',//"rest/nodes",
		method:"GET",
		extraParams:{
			limit:20
		}
	}),
	reader:new Ext.data.XmlReader({
		record:"node"
	}, record)
})

var pagingBar = new Ext.PagingToolbar({
        pageSize: 20,
        store: dataStore,
        displayInfo: true,
        displayMsg: 'Displaying topics {0} - {1} of {2}',
        emptyMsg: "No topics to display",
        
        items:[
            '-', {
            pressed: true,
            enableToggle:true,
            text: 'Show Preview',
            cls: 'x-btn-text-icon details',
            toggleHandler: function(btn, pressed){
                var view = grid.getView();
                view.showPreview = pressed;
                view.refresh();
            }
        }]

});

var colModel = new Ext.grid.ColumnModel([{
		header :'Node Label',
		name :'nodeLabel',
		width:150,
		sortable :true,
		align :'center'
	},{
		header :'Node ID',
		name :'nodeId',
		width :50,
		sortable :true,
		align :'center'
	},{
		header :'Type',
		name :'type',
		width :50,
		sortable :true,
		align :'center'
	},{
		header :'Label Source',
		name :'label_Source',
		width :100,
		sortable :true,
		align :'center'
	},{
		header :'Created',
		name :'created',
		width :200,
		sortable :true,
		align :'center'
	},{
		header :'Last Capsd Poll',
		name :'last-CapsdPoll',
		width :200,
		sortable :true,
		align :'center'
	}
]) 

function checkFilters(){
	if(urlParams != undefined && urlParams.nodename != undefined){
		dataStore.filter("nodeLabel", urlParams.nodename, true, false);
	}
	
};


function loadData(conn, response, options){
	Ext.Ajax.un('requestComplete', this.loadData, this);
	dataStore.loadData(response.responseXML);
}

function checkAdded(store, records, index){

	if(records[index].data.nodeId > 0){
		store.remove(records[index]);
	
	}
}

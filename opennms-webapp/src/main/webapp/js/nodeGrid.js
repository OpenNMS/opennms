var grid;
var urlParams;
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
        
        bbar: pagingBar,
        
        renderTo:'node-grid'
    });
    
    grid.getSelectionModel().on('rowselect', function(sm, rowIdx, r){
    	if(r.data.nodeId > 0){
    		window.location = "element/node.jsp?node=" + r.data.nodeId;	
    	};
    })
    
	dataStore.on("load", checkFilters, this);
    dataStore.load();
    
})

var record = new Ext.data.Record.create([
	{name:"nodeLabel", mapping:"label"},
	{name:"nodeId", mapping:"nodeId"},
	{name:"labelSource", mapping:"labelSource"},
	{name:"created", mapping:"createTime"},
	{name:"lastCapsdPoll", mapping:"lastCapsdPoll"},
	{name:"type", mapping:"type"}
]);
	
var dataStore = new Ext.data.Store({
	totalCount:10,
	url:"rest/nodes",
	params:{},
	reader:new Ext.data.XmlReader({
		record:"node"
	}, record)
})

var pagingBar = new Ext.PagingToolbar({
        pageSize: 100,
        store: dataStore,
        displayInfo: true,
        emptyMsg: "No topics to display"
});

var colModel = new Ext.grid.ColumnModel([{
		header :'Node Label',
		name :'nodeLabel',
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
		width :75,
		sortable :true,
		align :'center'
	},{
		header :'Label Source',
		name :'labelSource',
		width :100,
		sortable :true,
		align :'center'
	},{
		header :'Created',
		name :'created',
		width :100,
		sortable :true,
		align :'center'
	},{
		header :'Last Capsd Poll',
		name :'lastCapsdPoll',
		width :100,
		sortable :true,
		align :'center'
	}
]) 

function checkFilters(){
	if(urlParams.nodename != undefined){
		dataStore.filter("nodeLabel", urlParams.nodename, true, false);
	}
	
};

function getNodes(){
	var xmlHttp;
	xmlHttp = new XMLHttpRequest();
	xmlHttp.onreadystatechange=function(){
		if(xmlHttp.readyState == 4){
			alert("test: " + xmlHttp.responseText);
		}
	};
	xmlHttp.open("GET", "rest/nodes", true);
	xmlHttp.send(null);
};
var grid;
Ext.onReady(function(){
	
	grid = new Ext.grid.GridPanel({
		title:'Node List',
        //store: dataStore,
        colModel:colModel,
        width:400,
        height:600,
        
        renderTo:'grid-panel'
	})

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
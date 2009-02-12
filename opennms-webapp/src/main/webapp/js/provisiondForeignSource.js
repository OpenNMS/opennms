var pendingForeignSourceGrid;
var pendingForeignSourcePanel;
var pendingForeignSourceRecord;
var pendingForeignSourceStore;
var fsColumnModel;

Ext.onReady(function(){
	initForeignSourceView();
})

function initForeignSourceView(){
	
	pendingForeignSourceGrid = new Ext.grid.GridPanel({
		store:pendingForeignSourceStore,
        width:'auto',
        autoHeight: true,
		renderTo:'pendingForeignSources',
		colModel:fsColumnModel
	});

	/*
	pendingForeignSourceGrid.on("rowdblclick", function(grid) {
		var selId = grid.getSelectionModel().getSelected().data.name;
	});
	*/

	pendingForeignSourcePanel = new Ext.Panel({
		applyTo:'pendingForeignSources',
		width:'auto',
		title:'Pending Foreign Sources',
		autoHeight:true,
		bodyBorder:false,
		border:false,
		items:[
		    pendingForeignSourceGrid
		]
	});

	pendingForeignSourceStore.load();
}

pendingForeignSourceRecord = new Ext.data.Record.create([
    {name:"name",mapping:'@name'},
    {name:"scanInterval",mapping:'scan-interval'}
]);

pendingForeignSourceStore = new Ext.data.Store({
	proxy:new Ext.data.HttpProxy({
		url:'rest/foreignSources/pending',
		method:"GET",
	}),
	reader:new Ext.data.XmlReader({
		record:"foreign-source",
		totalRecords:"@count"
	}, pendingForeignSourceRecord)
})

fsColumnModel = new Ext.grid.ColumnModel([
    {
    	id: 'nameCol',
    	header: 'Foreign Source',
    	name: 'name',
    	sortable: true,
    	align: 'left'
    },
    {
    	header: 'Scan Interval',
    	name: 'scanInterval',
    	align: 'left'
    }
])


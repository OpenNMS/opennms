var pendingForeignSourceGrid;
var pendingForeignSourcePanel;
var pendingForeignSourceRecord;
var pendingForeignSourceStore;
var fsColumnModel;

Ext.data.StrippingXmlReader = function(meta, recordType){
    meta = meta || {};
    Ext.data.StrippingXmlReader.superclass.constructor.call(this, meta, recordType||meta.fields);
};
Ext.extend(Ext.data.StrippingXmlReader, Ext.data.DataReader, {
});

Ext.onReady(function(){
	Ext.Ajax.defaultHeaders = {'Accept': 'application/json'};
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
    {name:"name",mapping:'foreign-source'.'@name'},
    {name:"scanInterval",mapping:'foreign-source'.'scan-interval'}
]);

pendingForeignSourceStore = new Ext.data.Store({
	proxy:new Ext.data.HttpProxy({
		url:'rest/foreignSources/pending',
		method:"GET",
	}),
	/*
	reader:new Ext.data.XmlReader({
		record:"foreign-source",
		totalRecords:"@count"
	}, pendingForeignSourceRecord)
	*/
	reader:new Ext.data.JsonReader({
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


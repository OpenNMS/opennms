var eventPanel;
var eventGrid;
var eventStore;
var eventRecord;
var eventColumnModel;
var eventSelectionModel;

function initEventView(){
	
	eventGrid = new Ext.grid.GridPanel({
		store:eventStore,
        width:'auto',
        height:474,
        renderTo:'event-view',
		colModel:eventColumnModel,
		loadMask:true,
		bbar:eventPagingBar,
		
		// customize view config
        viewConfig: {
        	autoFill:true,
            forceFit:true,
            enableRowBody:true,
            showPreview:true,
            getRowClass : function(record, rowIndex, p, store){
                if(this.showPreview){
                    p.body = '<p><br/><b>Event Description:</b> '+record.data.eventDescr+'</br></p>';
                    return getSeverityStyle(record.data.severityLevel);
                }
                return 'x-grid3-row-collapsed';
            }
        },

	});
	
	eventGrid.on("rowdblclick", function(grid) {
		//eventGrid
		var selId = grid.getSelectionModel().getSelected().data.eventId;
		window.location = 'event/detail.jsp?id=' + selId;   
    });
	
	
	eventPanel = new Ext.Panel({
		applyTo:'event-view',
		width:'auto',
		title:'RECENT NODE EVENTS',
		autoHeight:true,
		bodyBorder:false,
		border:false,
		items:[
			eventGrid
		]
	});
	
	
	if(urlParams.node){
		loadNodeEvents(urlParams.node);
	}else{
		loadNodeEvents(-1);
	}
}

eventRecord = new Ext.data.Record.create([
	{name:"eventId", mapping:"id"},
	{name:'severityLevel', mapping:'eventSeverity'},
	{name:"eventTime", mapping:"eventTime"},
	{name:"eventDescr", mapping:"eventDescr"},
	{name:"createTime", mapping:"eventCreateTime"},
	{name:"eventUei", mapping:"eventUei"},
])

eventStore = new Ext.data.Store({
	baseParams:{limit:20},
	proxy:new Ext.data.HttpProxy({
		url:'rest/events',
		method:"GET",
		extraParams:{
			limit:20
		}
	}),
	reader:new Ext.data.XmlReader({
		record:"event",
		totalRecords:"@totalCount"
	}, eventRecord)
})

eventColumnModel = new Ext.grid.ColumnModel([
	{
		id:'eventIDCol',
		header: "Event ID",
		name:'eventId',
		sortable: true,
		align:'left',
		renderer:renderTopic
	},{
		header:"Severity",
		name:'severityLevel',
		renderer:renderEventSeverity,
		align:'left'
	},{
		header: "Event Time",
		name:'eventTime',
		width: 150,
		sortable: true,
		renderer:eventTime,
		align:'left'
	}
])

var eventPagingBar = new Ext.PagingToolbar({
	pageSize: 20,
    store: eventStore,
    displayInfo: true,
    displayMsg: 'Displaying topics {0} - {1} of {2}',
    emptyMsg: "No topics to display",
    items:[
            '-', { }]
})

// pluggable renders
function renderTopic(value, p, record){
    return String.format('<b><a href="http://localhost:8080/opennms-webapp/event/detail.jsp?id={0}" target="_self">{0}</a></b>',value);
}

function renderEventSeverity(value, p, record){
	var severityLevel;
	
	if(value == 0){
		severityLevel = 'CLEARED';
	}else if(value == 1){
		severityLevel = 'NORMAL';
	}else if(value == 2){
		severityLevel = 'INDITERMINATE';
	}else if(value == 3){
		severityLevel = 'WARNING';
	}else if(value == 4){
		severityLevel = 'MINOR';
	}else if(value == 5){
		severityLevel = 'MAJOR';
	}else if(value == 6){
		severityLevel = 'CRITICAL';
	}
	
	return String.format('<p><b>{0}</b></p>', severityLevel);
}

function eventTime(value, p, r){
	var date = onmsEventDateFormatter(value);
    return String.format('{0}', date);
}

function onmsEventDateFormatter(value){
	var dateArray = value.split("T");
	var date = dateArray[0].split("-");
	var time = dateArray[1].split('-')[0];
	return String.format("{0}/{1}/{2} {3}",date[1], date[2], date[0], time);
}

function getSeverityStyle(severityLevel){
	var bgStyle;
	if(severityLevel == 0){
		bgStyle = 'event-severity-cleared';
	}else if(severityLevel == 1){
		bgStyle = 'event-severity-normal';
	}else if(severityLevel == 2){
		bgStyle = 'event-severity-indeterminate';
	}else if(severityLevel == 3){
		bgStyle = 'event-severity-warning';
	}else if(severityLevel == 4){
		bgStyle = 'event-severity-minor';
	}else if(severityLevel == 5){
		bgStyle = 'event-severity-major';
	}else if(severityLevel == 6){
		bgStyle = 'event-severity-critical';
	} 
	return String.format('x-grid3-row-expanded event-severity-border {0}', bgStyle);
}

function acknowledgeEvent(button, eventObj){
	var totalSelected =  eventGrid.getSelectionModel().getCount();
	var selected;
	
	selected = eventGrid.getSelectionModel().getSelections();
	
	if(selected.length < 1){
		alert('Please check the events that you would like to acknowledge.');
		return;
	}

	var selectedIds = "";
	for (var i=0; i<selected.length; i++) {
		selectedIds += selected[i].data.eventId + ",";
	};
	
	alert('selectioned events: ' + selectedIds);
}

function loadNodeEvents(nodeId){
	if(nodeId != -1){
		eventStore.load({params:{'node.id':nodeId, start:0}});	
	}else{
		alert("event tried to load -1");
	}
	
}


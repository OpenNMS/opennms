var eventPanel;
var eventGrid;
var eventStore;
var eventRecord;
var eventColumnModel;
var eventSelectionModel = new Ext.grid.CheckboxSelectionModel();

function initEventView(){
	
	eventGrid = new Ext.grid.GridPanel({
		store:eventStore,
        width:'auto',
        height:300,
        renderTo:'event-view',
		colModel:eventColumnModel,
		sm: eventSelectionModel,
		loadMask:true,
		bbar:eventPagingBar,
		
		// customize view config
        viewConfig: {
            forceFit:true,
            enableRowBody:true,
            showPreview:true,
            getRowClass : function(record, rowIndex, p, store){
                if(this.showPreview){
                    p.body = '<p><br/><b>Event Description:</b> '+record.data.eventDescr+'</p>';
                    return getSeverityStyle(record.data.severityLevel);
                }
                return 'x-grid3-row-collapsed';
            }
        },

	});
	
	eventGrid.on("rowdblclick", function(grid) {
		alert("Yo you clicked an event grid");
		/*var view=new Ext.View(Ext.get('squote'),tpl,{
               store:eventStore 
        });
        var sel = grid.getSelectionModel().getSelected();
        var selIndex = ds.indexOf(sel);
        var seldata=sel.data;
        	
        seldata.change=change(seldata.change);
        seldata.pctChange=pctChange(seldata.pctChange);
        seldata.lastChange=Ext.util.Format.date(seldata.lastChange,'m/d/Y');
        tpl.overwrite(view.getEl(), seldata);
            
        dialog.show(grid.getView().getRow(selIndex));*/  
    });
	
	
	eventPanel = new Ext.Panel({
		applyTo:'event-view',
		width:'auto',
		title:'EVENTS',
		autoHeight:true,
		bodyBorder:false,
		border:false,
		items:[
			eventGrid
		]
	});
	
	
	loadNodeEvents(urlParams.node);
}

eventRecord = new Ext.data.Record.create([
	{name:'empty', mapping:''},
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
		record:"onmsEvent",
		totalRecords:"@totalCount"
	}, eventRecord)
})

eventColumnModel = new Ext.grid.ColumnModel([
	eventSelectionModel,
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
		align:'center'
	},{
		header: "Event Time",
		name:'eventTime',
		width: 150,
		sortable: true,
		renderer:eventTime,
		align:'center'
	}
])

var eventPagingBar = new Ext.PagingToolbar({
	pageSize: 20,
    store: eventStore,
    displayInfo: true,
    displayMsg: 'Displaying topics {0} - {1} of {2}',
    emptyMsg: "No topics to display",
})

// pluggable renders
function renderTopic(value, p, record){
    return String.format('<b><a href="http://localhost:8080/opennms-webapp/event/detail.jsp?id={0}" target="_blank">{0}</a></b>',value);
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

function resetEventGridSelection(button, eventObj){
	eventSelectionModel.clearSelections();	
}

function loadNodeEvents(nodeId){
	eventStore.load({params:{'node.id':nodeId}});
}


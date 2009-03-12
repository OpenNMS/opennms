function snmpCollectionViewInit(elementId, nodeId){
	var urlTemplate = "rest/nodes/{nodeId}/snmpinterfaces"
	var physicalInterfaceGrid = new OpenNMS.ux.EditorSNMPInterfaceGrid({
		id:'nodePhysicalInterfaceGrid',
		title:'Physical Interfaces',
		viewConfig:{
			autoFill: true,
	  		forceFit: true,
	  		scrollOffset:5,
  		 	getRowClass : function(record, rowIndex, p, store){
	            return getStatusColor(record.data.ifAdminStatus, record.data.ifOperStatus);
            }
		},
		nodeId: nodeId,
		height:495,
		updateRecord:updateSNMPInterface
	});
	
	var snmpPanel = new Ext.Panel({
		width:"100%",
		renderTo:elementId,
		items:[
		       	physicalInterfaceGrid
		       ]
	});
	
	function getStatusColor(ifAdminStatus, ifOperStatus){
		var bgStyle;
		if(ifAdminStatus != 1){
			bgStyle = 'grid-status-blue';
		}else if(ifAdminStatus == 1 && ifOperStatus == 1){
			bgStyle = 'grid-status-green';
		}else if(ifAdminStatus == 1 && ifOperStatus != 1){
			bgStyle = 'grid-status-red';
		}
		
		return String.format('x-grid3-row {0}', bgStyle);
	};
	
	function updateSNMPInterface(store, record, operation){
		var tpl = new Ext.XTemplate(urlTemplate);
		
		var coll = record.data.collect == "Default" ? "N" : record.data.collect;
		var requestObj = {};
		requestObj.url = tpl.apply(this) + "/" + record.data.ifIndex;
		requestObj.method = "PUT";
		requestObj.params = {collect:coll};
		sendRequest(requestObj);
	};
	
	function sendRequest(requestObj){
		Ext.Ajax.request({
			header:{
				'Content-Type':"application/x-www-form-urlencoded"
			},
			url:requestObj.url,
			params:requestObj.params,
			method:"PUT",
 			failure: function(){alert('Error: PUT to Rest service failed please try again.')}
		});
	}
	
}
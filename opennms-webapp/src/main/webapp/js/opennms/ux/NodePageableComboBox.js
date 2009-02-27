Ext.namespace("OpenNMS.ux");
OpenNMS.ux.NodePageableComboBox=Ext.extend(OpenNMS.ux.ComboFilterBox,{
	
	url:"rest/nodes",
	recordMap:[
		{name:"name", mapping:"label"},
		{name:"id", mapping:"nodeId"}
	],
	width:"100%",
	hideTrigger:false,
	queryParam:"label",
	minHeight:300,
	emptyText:"-- Choose A Node --"
	
});

Ext.reg('o-nodepageablecombo', OpenNMS.ux.NodePageableComboBox);
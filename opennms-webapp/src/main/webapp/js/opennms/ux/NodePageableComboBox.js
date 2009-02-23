Ext.namespace("OpenNMS.ux");
OpenNMS.ux.NodePageabelComboBox=Ext.extend(OpenNMS.ux.ComboBoxFilter,{
	
	initComponent:function(){
		OpenNMS.ux.NodePageableComboBox.superclass.initComponent.apply(this, arguments);
	}
	
});

Ext.reg('o-nodepageablecombo', OpenNMS.ux.NodePageableComoBox);
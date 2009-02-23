Ext.namespace("OpenNMS.ux");
OpenNMS.ux.ComboFilterBox = Ext.extend(Ext.form.ComboBox,{
	recordTag:"node",
	recordMap:[
			    {name:"interfaceId", mapping:"interfaceId"},
			    {name:"ipAddress", mapping:"ipAddress"},
			    {name:'hostName', mapping:'ipHostName'},
			    {name:'ifIndex', mapping:'ifIndex'},
			    {name:"isManaged", mapping:"isManaged"},
			    {name:'capsdPoll', mapping:'ipLastCapsdPoll'},
			    {name:"snmpInterface", mapping:"snmpInterface"}
	],
	url:'rest/nodes',
    displayField:'title',
    typeAhead: false,
    loadingText: 'Searching...',
    width: 570,
    pageSize:10,
    hideTrigger:true,
    itemSelector: 'div.search-item',
    onSelect: function(record){ // override default onSelect to do redirect
       alert('you clicked an item');
    },
   
    resultTpl:new Ext.XTemplate(
        '<tpl for="."><div class="search-item">',
            '<h3><span>{lastPost:date("M j, Y")}<br />by {author}</span>{title}</h3>',
            '{excerpt}',
        '</div></tpl>'
    ),
	
	initComponent:function(){
		var ds = new Ext.data.Store({
	        proxy: new Ext.data.HttpProxy({
	            url: this.url
	        }),
	        reader: new Ext.data.XMLReader({ record: this.recordTag, totalProperty: '@totalCount' }, this.recordMap)
	    });
		
		Ext.apply(this, {
			store: ds,
			tpl: this.resultTpl	
		})
	    

		OpenNMS.ux.ComboFilterBox.superclass.initComponent.apply(this, apply);
	}
	
});

Ext.reg('o-filtercombobox', OpenNMS.ux.ComboFilterBox);
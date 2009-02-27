Ext.namespace("OpenNMS.ux");
OpenNMS.ux.ComboFilterBox = Ext.extend(Ext.form.ComboBox,{
	recordTag:"node",
	recordMap:[
			    {name:"name", mapping:"label"},
	],
	url:'rest/nodes',
    displayField:'title',
    typeAhead: false,
    loadingText: 'Searching...',
    width: "100%",
    pageSize:10,
    hideTrigger:false,
    itemSelector: 'div.search-item',
   
    resultTpl:new Ext.XTemplate(
        '<tpl for="."><div class="search-item">',
            '{name}',
        '</div></tpl>'
    ),
	
	initComponent:function(){
		var ds = new Ext.data.Store({
	        proxy: new Ext.data.HttpProxy({
	            url: this.url,
	            method:'GET'
	        }),
	        baseParams:{
	        	comparator:"contains"
	        },
	        reader: new Ext.data.XmlReader({ record:this.recordTag, totalRecords:"@totalCount" }, this.recordMap)
	    });
		
		Ext.apply(this, {
			store: ds,
			tpl: this.resultTpl	
		})
	    

		OpenNMS.ux.ComboFilterBox.superclass.initComponent.apply(this, arguments);
	}
	
});

Ext.reg('o-filtercombobox', OpenNMS.ux.ComboFilterBox);
Ext.BLANK_IMAGE_URL = "extJS/resources/images/default/s.gif";
Ext.namespace("OpenNMS.ux");
OpenNMS.ux.SearchFilterGrid = Ext.extend(Ext.Container, {
	
	autoEl: 'div',
	deferredRender: false,
	
	initComponent:function(){
	
		this.setLayout(new OpenNMS.ux.SearchFilterLayout({
			deferredRender: this.deferredRender
		}));
		
		this.cancelId = newGuid();
		this.searchBtnId = newGuid();
		
		var searchButton = new Ext.Button({
			text:'Search',
			id:this.searchBtnId,
			cls:'x-btn-text-icon',
			iconCls:'search-criteria-icon',
			scope: this,
			handler: this.showSearchPanel,
		});
		
		if (this.grid !== undefined && this.grid.title !== undefined) {
			this.title = this.grid.title;
			this.grid.addPagingBarButtons([searchButton]);
			
		}else{
			this.grid = new OpenNMS.ux.PageableGrid({
				pagingBarButtons:[searchButton]
			})
		}
		
		var comboData = [];
		
		var cols = this.grid.columns;
		var defaultSearch;
		var firstColumn;
		
		for(i = 0; i < cols.length; i++) {
			var col = cols[i];
			if (col.searchable) {
				if (col.defaultSearch) {
					initialValue = col.dataIndex;
				}
				if (!firstColumn) {
					firstColumn = col.dataIndex;
				}
				
				if(col.type){
					comboData.push([ col.dataIndex, col.header ]);
				}else{
					comboData.push([ col.dataIndex, col.header ]);
				}
				
			}
		}
		
		var intialValue = defaultSearch ? defaultSearch : firstColumn;
		
		var comboBox = new Ext.form.ComboBox({
			fieldLabel:'Search Column',
	   		store: comboData,
	   		editable:false,
	   		selectOnFocus:true,
	   		allowBlank: false,
	   		mode: 'local',
	   		triggerAction:'all',
	   		value: initialValue,
	   		width:'100%'
		});
		
		var searchTextField = new Ext.form.TextField({
			fieldLabel:'Search Text',
			enableKeyEvents:true, 
			listeners:{
				'keydown':{
					scope:this,
					fn:function(tf, event){
						if(event.keyCode == 13){
							this.search();
						}
					}
				}
			}
		});
		
		Ext.apply(this, {
	    	activeItem: 0,
	    	searchColumn: comboBox,
	    	searchText: searchTextField,
	    	items: [
	    	   this.grid,
	    	   {    
	    		    xtype:'form',
	    		   	cls: 'o-panel',
	    		   	items: [
	    		   	        comboBox,
	    		   	        searchTextField
	    		   	 ],
	    		   	
	    		   	buttons:[
	    		   		{
	    		   	    	id:this.cancelId, //'cancelBtn',
				   	    	text:'Cancel',
				   	    	scope: this,
				   	    	handler:this.cancel
	    	   			},{
	    		   			text:'Search',
	    		   			scope: this,
	    		   			handler: this.search
	    		   		}
	    		   	]    		   	
	    		}
	  	      ]
	  	    });

		OpenNMS.ux.SearchFilterGrid.superclass.initComponent.apply(this, arguments);
   },
   
   showSearchPanel:function(event){
   		this.getLayout().setActiveItem(1);
   		
   		if(this.searchText.getValue() != ""){
   			//this.cancelBtn.setText('Reset');
   			Ext.getCmp(this.cancelId).setText('Reset');
   		}else{
   			//this.cancelBtn.setText('Cancel');
   			Ext.getCmp(this.cancelId).setText('Cancel');
   		}
   },
   
   cancel:function(event){
   	   this.searchText.setValue("");
	   this.getLayout().setActiveItem(0);
	   Ext.getCmp(this.searchBtnId).setIconClass('search-criteria-icon');
	   this.grid.loadSearch({});
   },
   
   search:function(event){
	   var dataIndex = this.searchColumn.getValue();
	   var searchVal = this.searchText.getValue();
	   var type = this.searchColumn.getRawValue();
	   var searchParams = {};
	   searchParams[dataIndex] = searchVal;
	   
	   if(dataIndex != "ifIndex"){
		   searchParams.comparator = "contains";   
	   }
	   
	   if(searchVal != ""){
	   		Ext.getCmp(this.searchBtnId).setIconClass('search-criteria-star');
	   }else{
	   		Ext.getCmp(this.searchBtnId).setIconClass('search-criteria-icon');
	   }
	   
	   this.getLayout().setActiveItem(0);
	   this.grid.loadSearch(searchParams); 
	   
   }
	
});

function newGuid() {
    var g = "";
    for(var i = 0; i < 16; i++)
    g += Math.floor(Math.random() * 0xF).toString(0xF) + (i == 8 || i == 12 || i == 16 || i == 20 ? "-" : "")
    return g;
}

Ext.reg('o-searchfiltergrid', OpenNMS.ux.SearchFilterGrid);
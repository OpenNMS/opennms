Ext.BLANK_IMAGE_URL = "extJS/resources/images/default/s.gif";
Ext.namespace("OpenNMS.ux");
OpenNMS.ux.SearchFilterGrid = Ext.extend(Ext.Container, {
	
	autoEl: 'div',
	deferredRender: false,
	
	initComponent:function(){
	
		this.setLayout(new OpenNMS.ux.SearchFilterLayout({
			deferredRender: this.deferredRender
		}));
	
		var searchButton = new Ext.Button({
			text:'Search',
			id:'searchBtn',
			cls:'x-btn-text-icon',
			iconCls:'search-criteria-icon',
			scope: this,
			handler: this.showSearchPanel,
		})
		
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
				comboData.push([ col.dataIndex, col.header ]);
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
	    		   	    	id:'cancelBtn',
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
   			Ext.getCmp('cancelBtn').setText('Reset');
   		}else{
   			Ext.getCmp('cancelBtn').setText('Cancel');
   		}
   },
   
   cancel:function(event){
   	   this.searchText.setValue("");
	   this.getLayout().setActiveItem(0);
	   Ext.getCmp('searchBtn').setIconClass('search-criteria-icon');
	   this.grid.loadSearch({});
   },
   
   search:function(event){
	   var dataIndex = this.searchColumn.getValue();
	   var searchVal = this.searchText.getValue();
	   var searchParams = {};
	   searchParams[dataIndex] = searchVal;
	   searchParams.comparator = "contains";
	   
	   if(searchVal != ""){
	   		Ext.getCmp('searchBtn').setIconClass('search-criteria-star');
	   }else{
	   		Ext.getCmp('searchBtn').setIconClass('search-criteria-icon');
	   }
	   
	   this.getLayout().setActiveItem(0);
	   this.grid.loadSearch(searchParams); 
	   
   }
	
});

Ext.reg('o-searchfiltergrid', OpenNMS.ux.SearchFilterGrid);
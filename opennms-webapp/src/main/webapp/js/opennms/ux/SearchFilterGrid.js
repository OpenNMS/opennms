/**
 * @author thedesloge
 */
Ext.namespace("OpenNMS.ux");
OpenNMS.ux.SearchFilterGrid = Ext.extend(Ext.Container, {
	
	autoEl: 'div',
	deferredRender: false,
	
	height: 300,
	width: 400,
	
	initComponent:function(){
	
		this.setLayout(new OpenNMS.ux.SearchFilterLayout({
			deferredRender: this.deferredRender
		}));
	
		var searchButton = new Ext.Button({
			text:'search',
			listeners:{
				'click':{
					fn:this.searchCritBtnClickHandler,
					scope:this,
				}
			}
		})
		
		if (this.grid !== undefined && this.grid.title !== undefined) {
			this.title = this.grid.title;
			this.grid.addPagingBarButtons([searchButton]);
		}else{
			this.grid = new OpenNMS.ux.PageableGrid({
				pagingBarButtons:[searchButton]
			})
		}

	
		var button1 = new Ext.Button({
			region: 'center',
			text: 'button1',
			listeners:{
				click:{
			    	scope: this,
			    	fn:function() {
		  	   			this.getLayout().setActiveItem(0);
	                },
    		   },
		    },
		});
		
		var button2 = new Ext.Button({
			region: 'south',
			text: 'button2',
			listeners:{
				click:{
					scope: this,
					fn:function()  {
						this.getLayout().setActiveItem(1);
	          		},
    			}
			}
		});
		

	    Ext.apply(this, {
	    	activeItem: 0,
	    	items: [
	    	   this.grid,
	    	   new Ext.Panel({ cls: 'o-panel', layout: 'absolute', items: [ button1 ]}),
	  	      ]
	  	      });

	    
	
		OpenNMS.ux.SearchFilterGrid.superclass.initComponent.apply(this, arguments);
	
   },
   
   searchCritBtnClickHandler:function(event){
   		this.getLayout().setActiveItem(1);
   }
	
})

Ext.reg('o-searchfiltergrid', OpenNMS.ux.SearchFilterGrid);
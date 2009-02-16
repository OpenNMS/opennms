/**
 * @author thedesloge
 */
Ext.namespace("OpenNMS.ux");
OpenNMS.ux.SearchFilterGrid = Ext.extend(Ext.Container, {
	
	layout: 'searchfilter',
	autoEl: 'div',
	
	height: 300,
	width: 400,
	
	initComponent:function(){
	
		if (this.grid !== undefined && this.grid.title !== undefined) {
			this.title = this.grid.title;
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
	
})

Ext.reg('o-searchfiltergrid', OpenNMS.ux.SearchFilterGrid);
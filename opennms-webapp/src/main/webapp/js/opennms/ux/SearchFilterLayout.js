Ext.namespace("OpenNMS.ux");
OpenNMS.ux.SearchFilterLayout = Ext.extend(Ext.layout.FitLayout, {
    /* private */
    renderHidden : true,

    /**
     * Sets the active (visible) item in the layout.
     * @param {String/Number} item The string component id or numeric index of the item to activate
     */
    setActiveItem : function(item){
        item = this.container.getComponent(item);
        if(this.activeItem != item){
            if(this.activeItem){
                this.activeItem.hide();
            }
            this.activeItem = item;
            item.show();
            this.layout();
            item.getEl().fadeIn();
        }
    }

});
Ext.Container.LAYOUTS['searchfilter'] = OpenNMS.ux.SearchFilterLayout;
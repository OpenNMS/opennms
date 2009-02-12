var pendingForeignSourceGrid;
var pendingForeignSourcePanel;
var pendingForeignSourceRecord;
var pendingForeignSourceStore;
var fsColumnModel;

Ext.onReady(function(){
	// initForeignSourceView();
	initTree();
})

Ext.app.ForeignSourceLoader = Ext.extend(Ext.ux.XmlTreeLoader, {
    processAttributes : function(attr){
		attr.loaded = true;
        if (attr.tagName == 'foreign-source'){
        	attr.text = attr.name;
        }
        else if (attr.tagName == 'scan-interval') {
        	attr.text = 'scan-interval';
        }
        else if (attr.tagName == 'detectors') {
        	attr.text = 'Detectors';
        }
        else if (attr.tagName == 'detector') {
            attr.text = attr.name + ' (' + attr['class'] + ')';
        }
        else if (attr.tagName == 'policies') {
        	attr.text = 'Policies';
        }
        else if (attr.tagName == 'policy') {
            attr.text = attr.name + ' (' + attr['class'] + ')';
        }
        else if (attr.tagName == 'parameter') {
        	attr.text = attr.key + '=' + attr.value;
        }
    }
});
function initTree() {
	new Ext.Panel({
        title: 'Pending Foreign Sources',
	    renderTo: 'pendingTree',
        layout: 'border',
	    width: 500,
        height: 500,
        items: [{
            xtype: 'treepanel',
            id: 'tree-panel',
            region: 'center',
            margins: '2 2 0 2',
            autoScroll: true,
	        rootVisible: false,
	        root: new Ext.tree.AsyncTreeNode(),
            
            // Our custom TreeLoader:
	        loader: new Ext.app.ForeignSourceLoader({
	            dataUrl:'rest/foreignSources/pending',
	            requestMethod:"GET"
	        }),
        }]
    });

}


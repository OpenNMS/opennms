Ext.namespace("OpenNMS.ux");
OpenNMS.ux.LocalPageableProxy = function(data, pagesize, recordMap){
	OpenNMS.ux.LocalPageableProxy.superclass.constructor.call(this);
    this.data = data;
    this.pagesize = pagesize;
    this.recordMap = recordMap;
};

Ext.extend(OpenNMS.ux.LocalPageableProxy, Ext.data.DataProxy, {
    
    
    
    load : function(params, reader, callback, scope, arg){
    	params = params || {};
    	
    	if(params.start == undefined){
    		params.start = 0;
    	}
    	
    	var offset = this.pagesize + params.start;
    	if(offset >= this.data.records.length){
    		offset = this.data.records.length - 1;
    	}
    	
    	var returnData = {total:this.data.total, records:this.data.records.slice( params.start, this.pagesize + params.start)};
    	
        var result;
        try {
            result = reader.readRecords(returnData);
        }catch(e){
            this.fireEvent("loadexception", this, arg, null, e);
            callback.call(scope, null, arg, false);
            return;
        }
        callback.call(scope, result, arg, true);
    },
    
    // private
    update : function(params, records){
        
    },
    
    copyArray : function(iterable){
		  if (iterable.item){
			var array = [];
			for (var i = 0, l = iterable.length; i < l; i++) array[i] = iterable[i];
			return array;
		}
		return Array.prototype.slice.call(iterable);

		
    },
    
    getAllData:function(){
    	var DataRecord = Ext.data.Record.create(this.recordMap);
    	
    	var records = new Array();

    	for each(dataObj in this.data.records){
    		
    		if(!(dataObj instanceof Function)){
    			records.push(new DataRecord(dataObj));
    		}
    	}
    	return records;
    }
    
});
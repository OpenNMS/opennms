function MetricMapping(metric) {
    this.metric = metric;
    this.mappings = new Array();
    this.consolidationFunctions = new Array();

    this.consolidationFunctions["LAST"] = function(target, value, timestamp) {
        if (typeof(data[target])== 'undefined')
            data[target] = new Array();
        data[target].push( {v:value, t:timestamp} );
    }

    this.consolidationFunctions["MAX"] = function(target, value, timestamp) {
        if (typeof(data[target])== 'undefined') {
            data[target] = new Array();
            data[target].push( {v:value, t:timestamp} );
        } else {
            var oldMax = data[target][data[target].length-1].v;
            data[target].push( {v:Math.max(oldMax, value), t:timestamp} );
        }
    }

    this.consolidationFunctions["MIN"] = function(target, value, timestamp) {
        if (typeof(data[target])== 'undefined') {
            data[target] = new Array();
            data[target].push( {v:value, t:timestamp} );
        } else {
            var oldMin = data[target][data[target].length-1].v;
            data[target].push( {v:Math.min(oldMin, value), t:timestamp} );
        }
    }

    this.consolidationFunctions["AVERAGE"] = function(target, value, timestamp) {
        if (typeof(data[target])== 'undefined') {
            data[target] = new Array();
            data[target].push( {v:value, t:timestamp} );
        } else {
            var oldAverage = data[target][data[target].length-1].v * data[target].length;
            data[target].push( {v:(oldAverage+value)/(data[target].length+1), t:timestamp} );
        }
    }

    this.addTarget = function(consolidationFunction, vname) {
        if (typeof(this.mappings[consolidationFunction])== 'undefined')
            this.mappings[consolidationFunction] = new Array();
        this.mappings[consolidationFunction].push(vname);
    }

    this.computeData = function(value, timestamp) {
        for(var consolidationFunction in this.mappings) {
            for(var target in this.mappings[consolidationFunction]) {
                this.consolidationFunctions[consolidationFunction](this.mappings[consolidationFunction][target], value, timestamp);
            }
        }
    }
}

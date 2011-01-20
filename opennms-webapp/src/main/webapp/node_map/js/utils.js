var onms;
if (!onms) onms = {};
if (!onms.vs) onms.vs = {};
if (!onms.vs.utils) onms.vs.utils = {};

if (!onms.vs.utils.dumpProps) {
    onms.vs.utils.dumpProps = function(obj) {
	function dumpProps2(obj, level) {
	    // Go through all the properties of the passed-in object
	    
	    if (obj) {
		for (var i in obj) { 
		    var msg =  level + i + ":\t" + obj[i];
		    print(msg);
		    
		    if (obj[i] && typeof obj[i] == "object") {
			print(level + "\t[ Decending to " + obj[i] + "... ]");
			dumpProps2(obj[i], level + "\t");
		    }
		}
	    }
	}



	print("<br/><br/> ---- Dumping props for " + obj + " ----");
	dumpProps2(obj, "");
	print(" ---- Done ----<br/><br/>");
    };

    onms.vs.utils.projection_lonlat = function(map, node) {
	var location    = new OpenLayers.LonLat(node.longitude, node.latitude);
	var latlon_proj = new OpenLayers.Projection("EPSG:4326");
	return location.transform(latlon_proj, map.getProjectionObject());	
    };

    onms.vs.utils.projection_lonlat_from_4326 = function(map, node) {
	var location     = new OpenLayers.LonLat(node.longitude, node.latitude);
	var latlon_proj  = new OpenLayers.Projection("EPSG:900913");
	var latlon_proj2 = new OpenLayers.Projection("EPSG:4326");
	return location.transform(latlon_proj, latlon_proj2);	
    };

}

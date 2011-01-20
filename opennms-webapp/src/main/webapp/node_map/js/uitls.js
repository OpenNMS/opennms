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



	print("\n\n ---- Dumping props for " + obj + " ----");
	dumpProps2(obj, "");
	print(" ---- Done ----\n\n");
    };
}

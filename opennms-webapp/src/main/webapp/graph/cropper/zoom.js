/* adapted from Tobi Oetiker's SmokePing zoom */

function urlObj(url) {
   var urlBaseAndParameters;

   urlBaseAndParameters = url.split("?"); 
   this.urlBase = urlBaseAndParameters[0];
   this.urlParameters = new Array();

   var parameters = urlBaseAndParameters[1].split(/[;&]/);
   for (i = 0; i < parameters.length; i++) {
   	  var entry = parameters[i].split("=");
   	  this.urlParameters[i] = { "key": entry[0], "value": entry[1] };
   }
   
   this.getUrlBase = urlObjGetUrlBase;
   this.getUrlParameters = urlObjGetUrlParameters;
}

function urlObjGetUrlBase() {
   return this.urlBase;
}

function urlObjGetUrlParameters() {
	return this.urlParameters;
}

var doDebug = true;

function log(message) {
	if (doDebug) {
		if(window.console) {
			window.console.log(message);
		} 
		else {
			// alert(message);
		}
	}
}
	
// example with minimum dimensions
var myCropper;

var StartEpoch = 0;
var EndEpoch = 0;

var JRobinLeft = 74;
var JRobinRight = 15;

var RRDLeft  = 74;
var RRDRight = 15;

function changeRRDImage(coords,dimensions) {

    var SelectLeft = Math.min(coords.x1,coords.x2);
    var SelectRight = Math.max(coords.x1,coords.x2);

    if (SelectLeft == SelectRight)
         return; // abort if nothing is selected.

    var left  = RRDLeft;        // difference between left border of RRD image and content
    var right = RRDRight;        // difference between right border of RRD image and content
    var RRDImgWidth  = $('zoom').getDimensions().width;       // Width of the Smokeping RRD Graphik
    var RRDImgUsable = RRDImgWidth - left - right;  
    var form = $('range_form');

    var myURLObj = new urlObj(document.URL); 
    var myURL = myURLObj.getUrlBase(); 
	var parameters = myURLObj.getUrlParameters();

	if (SelectLeft < left) {
		SelectLeft = left;
	}
	if (SelectRight > (RRDImgWidth - right)) {
		SelectRight = (RRDImgWidth - right);
	}
    
	StartEpoch = graphStart;
	EndEpoch = graphEnd;

    var DivEpoch = EndEpoch - StartEpoch; 

    var NewStartEpoch = Math.floor(StartEpoch + ((SelectLeft  - left) * DivEpoch / RRDImgUsable) );
    EndEpoch  =  Math.ceil(StartEpoch + (SelectRight - left) * DivEpoch / RRDImgUsable );

    StartEpoch = NewStartEpoch;

	var newUrl = myURL + "?";

	for (i = 0; i < parameters.length; i++) {
		entry = parameters[i];
		if (entry.key == "start") {
			entry.value = StartEpoch;
		} else if (entry.key == "end") {
			entry.value = EndEpoch;
		}
		newUrl = newUrl + entry.key + "=" + entry.value + "&";
	}

    myCropper.setParams();
	window.location.href = newUrl;

};

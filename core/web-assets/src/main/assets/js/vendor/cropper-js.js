require('script-loader!lib/3rdparty/prototype');
require('script-loader!lib/3rdparty/cropper/lib/scriptaculous');
require('script-loader!lib/3rdparty/cropper/cropper.uncompressed');

/* adapted from Tobi Oetiker's SmokePing zoom */

/* eslint-disable no-invalid-this */
function urlObj(url) {
    var urlBaseAndParameters;

    urlBaseAndParameters = url.split('?');
    this.urlBase = urlBaseAndParameters[0];
    this.urlParameters = new Array();

    var parameters = urlBaseAndParameters[1].split(/[;&]/);
    for (i = 0; i < parameters.length; i++) {
        var entry = parameters[i].split('=');
        this.urlParameters[i] = { 'key': entry[0], 'value': entry[1] };
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
        } else {
        	// alert(message);
        }
    }
}

// example with minimum dimensions
var myCropper;

function changeRRDImage(coords,dimensions) {

    var SelectLeft = Math.min(coords.x1,coords.x2);
    var SelectRight = Math.max(coords.x1,coords.x2);

    if (SelectLeft === SelectRight) {
		return; // abort if nothing is selected.
    }

    var left  = zoomGraphLeftOffset;
    var right = zoomGraphRightOffset; // the right offset is relative to the right-side
    var RRDImgWidth  = $('zoomImage').getDimensions().width;
    var RRDImgUsable = RRDImgWidth - left + right;
    var form = $('range_form');

    var myURLObj = new urlObj(document.URL);
    var myURL = myURLObj.getUrlBase();
    var parameters = myURLObj.getUrlParameters();

    if (SelectLeft < left) {
        SelectLeft = left;
    }
    if (SelectRight > (RRDImgWidth + right)) {
        SelectRight = (RRDImgWidth + right);
    }

    StartEpoch = zoomGraphStart;
    EndEpoch = zoomGraphEnd;

    var DivEpoch = EndEpoch - StartEpoch;

    var NewStartEpoch = Math.floor(StartEpoch + ((SelectLeft  - left) * DivEpoch / RRDImgUsable) );
    EndEpoch  =  Math.ceil(StartEpoch + (SelectRight - left) * DivEpoch / RRDImgUsable );

    StartEpoch = NewStartEpoch;

    var newUrl = myURL + '?';

    for (i = 0; i < parameters.length; i++) {
        entry = parameters[i];
        if (entry.key === 'start') {
            entry.value = StartEpoch;
        } else if (entry.key === 'end') {
            entry.value = EndEpoch;
        }
        newUrl = newUrl + entry.key + '=' + entry.value + '&';
    }

    myCropper.setParams();
    window.location.href = newUrl;
}

console.log('init: cropper-js'); // eslint-disable-line no-console

window.changeRRDImage = changeRRDImage;
module.exports = changeRRDImage;
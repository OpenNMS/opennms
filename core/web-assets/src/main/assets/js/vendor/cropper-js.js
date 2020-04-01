const load = require('./vendor-loader');

module.exports = load('cropper', () => {
  require('script-loader!lib/3rdparty/prototype');
  require('script-loader!lib/3rdparty/cropper/lib/scriptaculous');
  require('script-loader!lib/3rdparty/cropper/cropper.uncompressed');
  
  /* adapted from Tobi Oetiker's SmokePing zoom */
  
  /* eslint-disable no-invalid-this */
  function urlObj(url) {
    let urlBaseAndParameters;
  
    urlBaseAndParameters = url.split('?');
    this.urlBase = urlBaseAndParameters[0];
    this.urlParameters = new Array();
  
    const parameters = urlBaseAndParameters[1].split(/[;&]/);
    for (let i = 0; i < parameters.length; i++) {
      const entry = parameters[i].split('=');
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
  
  let doDebug = true;
  
  function log(message) {
    if (doDebug) {
      if(window.console) {
        window.console.debug(message);
      } else {
        // alert(message);
      }
    }
  }
  
  // example with minimum dimensions
  let myCropper;
  
  function changeRRDImage(coords,dimensions) {
  
    let SelectLeft = Math.min(coords.x1,coords.x2);
    let SelectRight = Math.max(coords.x1,coords.x2);
  
    if (SelectLeft === SelectRight) {
      return; // abort if nothing is selected.
    }
  
    const left  = zoomGraphLeftOffset;
    const right = zoomGraphRightOffset; // the right offset is relative to the right-side
    const RRDImgWidth  = $('zoomImage').getDimensions().width;
    const RRDImgUsable = RRDImgWidth - left + right;
    // const form = $('range_form');
  
    const myURLObj = new urlObj(document.URL);
    const myURL = myURLObj.getUrlBase();
    const parameters = myURLObj.getUrlParameters();
  
    if (SelectLeft < left) {
      SelectLeft = left;
    }
    if (SelectRight > (RRDImgWidth + right)) {
      SelectRight = (RRDImgWidth + right);
    }
  
    let StartEpoch = zoomGraphStart;
    let EndEpoch = zoomGraphEnd;
  
    const DivEpoch = EndEpoch - StartEpoch;
  
    const NewStartEpoch = Math.floor(StartEpoch + ((SelectLeft  - left) * DivEpoch / RRDImgUsable) );
    EndEpoch  =  Math.ceil(StartEpoch + (SelectRight - left) * DivEpoch / RRDImgUsable );
  
    StartEpoch = NewStartEpoch;
  
    let newUrl = myURL + '?';
  
    for (let i = 0; i < parameters.length; i++) {
      const entry = parameters[i];
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
  
  window['changeRRDImage'] = changeRRDImage;
  return changeRRDImage;
});

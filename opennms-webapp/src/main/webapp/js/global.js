function getBaseHref() {
	return document.getElementsByTagName('base')[0].href;
}

function setLocation(url) {
	window.location.href = getBaseHref() + url;
}

function toggle(booleanValue, elementName) {
    var checkboxes = document.getElementsByName(elementName);
    for (var index in checkboxes) {
        checkboxes[index].checked = booleanValue;
    }
}

if (typeof requirejs === "function") {
  requirejs.config({
    baseUrl: 'lib',
    paths: {
      c3: '../webjars/c3/0.4.11/c3.js',
      d3: '../webjars/d3/3.5.17/d3.min',
      backshift: '../js/backshift.onms.min',
      geomap: '../geomap/js/geomap',
      holder: '../js/holder.min',
      jquery: '../webjars/jquery/2.2.4/dist/jquery',
      'jquery-ui' : '../webjars/jquery-ui/1.12.0/jquery-ui',
      'jquery-ui-treemap' : '../js/jquery.ui.treemap',
      'jquery-sparkline' : '../webjars/jquery-sparkline/2.1.3/jquery.sparkline.min',
      'leaflet' : 'leaflet/dist/leaflet-src',
      'markercluster' : 'leaflet.markercluster/dist/leaflet.markercluster-src'
    },
      shim : {
        'jquery-ui' : { deps: ['jquery'] },
        'jquery-ui-treemap' : { deps: ['jquery-ui'] },
        'jquery-sparkline' : { deps: ['jquery'] },
        'leaflet': { exports: 'L' },
        'markercluster': { deps: ['leaflet'] }
      }
  });
}

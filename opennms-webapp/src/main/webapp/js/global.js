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
      c3: 'c3/c3',
      d3: 'd3/d3.min',
      backshift: '../js/backshift.onms.min',
      geomap: '../geomap/js/geomap',
      holder: '../js/holder.min',
      jquery: 'jquery/dist/jquery',
      'jquery-ui' : 'jquery-ui/jquery-ui',
      'jquery-ui-treemap' : 'jquery-ui-treemap/js/jquery.ui.treemap',
      'jquery-sparkline' : 'jquery-sparkline/dist/jquery.sparkline',
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

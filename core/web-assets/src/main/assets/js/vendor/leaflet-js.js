const L = require('leaflet/dist/leaflet-src');
require('leaflet.markercluster/dist/leaflet.markercluster-src');
require('../../static/legacy/openlayers-2.10/OpenLayers');

//require('leaflet.scss');

console.log('init: leaflet-js'); // eslint-disable-line no-console

module.exports = window['L'] = L;
const load = require('./vendor-loader');

module.exports = load('leaflet', () => {
  const L = require('leaflet/dist/leaflet-src');
  require('leaflet.markercluster/dist/leaflet.markercluster-src');
  require('../../static/legacy/openlayers-2.10/OpenLayers');
  
  //require('leaflet.scss');

  window['L'] = L;
  return L;
});

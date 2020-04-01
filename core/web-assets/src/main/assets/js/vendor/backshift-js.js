const load = require('./vendor-loader');

module.exports = load('backshift', () => {
  require('vendor/d3-js');
  require('vendor/flot-js');
  
  const Backshift = require('backshift/dist/backshift.onms');
  window['Backshift'] = Backshift;
  return Backshift;
});

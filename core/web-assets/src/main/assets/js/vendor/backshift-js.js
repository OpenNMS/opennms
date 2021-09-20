require('vendor/d3-js');
require('vendor/flot-js');

const Backshift = require('backshift/dist/backshift.onms');

console.log('init: backshift-js'); // eslint-disable-line no-console

module.exports = window['Backshift'] = Backshift;
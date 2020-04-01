const load = require('./vendor-loader');

module.exports = load('jquery', () => {
  const jQuery = require('jquery');
  window['jQuery'] = window['$'] = jQuery;
  return jQuery;
});

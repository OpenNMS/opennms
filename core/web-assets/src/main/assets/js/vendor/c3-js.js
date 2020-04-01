const load = require('./vendor-loader');

module.exports = load('c3', () => {
  require('vendor/d3-js');

  const c3 = require('c3/c3');
  window['c3'] = c3;
  return c3;
});

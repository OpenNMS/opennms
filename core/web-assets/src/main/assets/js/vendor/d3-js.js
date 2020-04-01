const load = require('./vendor-loader');

module.exports = load('d3', () => {
  const d3 = require('d3/d3');
  window['d3'] = d3;
  return d3;
});

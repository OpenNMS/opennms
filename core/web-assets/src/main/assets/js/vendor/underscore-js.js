const load = require('./vendor-loader');

module.exports = load('underscore', () => {
  const _ = require('underscore');
  window['_'] = _;
  return _;
});

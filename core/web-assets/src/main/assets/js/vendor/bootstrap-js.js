const load = require('./vendor-loader');

module.exports = load('bootstrap', () => {
  const jQuery = require('vendor/jquery-js');
  require('bootstrap/dist/js/bootstrap');
  return jQuery;
});

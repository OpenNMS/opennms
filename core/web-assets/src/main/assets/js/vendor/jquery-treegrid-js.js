const load = require('./vendor-loader');

module.exports = load('jquery-treegrid', () => {
  const jQuery = require('vendor/jquery-js');

  require('jquery-treegrid/js/jquery.treegrid');
  require('jquery-treegrid/css/jquery.treegrid.css');
  
  return jQuery;
});

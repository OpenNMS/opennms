const load = require('./vendor-loader');

module.exports = load('bootbox', () => {
  require('./jquery-ui-js');
  const bootbox = require('bootbox');
  
  window['bootbox'] = bootbox;
  return bootbox;
});

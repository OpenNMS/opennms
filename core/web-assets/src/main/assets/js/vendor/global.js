const load = require('./vendor-loader');

module.exports = load('global', () => {
  require('vendor/jquery-ui-js');
  require('vendor/bootstrap-js');
  
  const Util = require('lib/util');
  
  window['getBaseHref'] = Util.getBaseHref;
  window['setLocation'] = Util.setLocation;
  window['toggle'] = Util.toggle;

  return Util;
});

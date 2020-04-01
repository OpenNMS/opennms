const load = require('./vendor-loader');

module.exports = load('extremecomponents', () => {
  const ex = require('../lib/3rdparty/extremecomponents/extremecomponents.js');

  window['getParameterMap'] = ex.getParameterMap;
  window['setFormAction'] = ex.setFormAction;

  return ex;
});

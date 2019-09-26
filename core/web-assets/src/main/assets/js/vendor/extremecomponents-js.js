const ex = require('../lib/3rdparty/extremecomponents/extremecomponents.js');

window.getParameterMap = ex.getParameterMap;
window.setFormAction = ex.setFormAction;

console.log('init: extremecomponents-js'); // eslint-disable-line no-console

module.exports = ex;
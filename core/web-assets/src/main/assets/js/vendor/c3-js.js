if (window['c3']) {
  console.debug('init: c3-js already loaded'); // eslint-disable-line no-console
} else {
  console.info('init: c3-js'); // eslint-disable-line no-console
  require('vendor/d3-js');
  window['c3'] = require('c3');
}

module.exports = window['c3'];
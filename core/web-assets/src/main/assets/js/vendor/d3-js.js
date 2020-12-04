if (window['d3']) {
  console.debug('init: d3-js already loaded'); // eslint-disable-line no-console
} else {
  console.info('init: d3-js'); // eslint-disable-line no-console
  window['d3'] = require('d3');
}

module.exports = window['d3'];
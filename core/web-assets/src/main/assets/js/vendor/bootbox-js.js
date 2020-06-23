require('./jquery-ui-js');
const bootbox = require('bootbox');

console.log('init: bootbox-js'); // eslint-disable-line no-console

module.exports = window['bootbox'] = bootbox;
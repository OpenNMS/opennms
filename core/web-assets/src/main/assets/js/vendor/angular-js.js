/* Angular Core */
const angular = require('angular');
require('angular-animate');
require('angular-cookies');
require('angular-route');
require('angular-resource');
require('angular-sanitize');

/* 3rd-Party Modules */
require('angular-growl-v2');
require('angular-loading-bar');

require('angular-growl-v2/build/angular-growl.css');
require('angular-loading-bar/build/loading-bar.css');

/* Bootstrap UI */
require('vendor/bootstrap-js');
require('angular-bootstrap-checkbox');
require('ui-bootstrap4'); // angular-ui-boostrap for bootstrap 4

console.log('init: angular-js'); // eslint-disable-line no-console

module.exports = window['angular'] = angular;
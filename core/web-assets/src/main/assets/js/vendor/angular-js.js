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
require('./bootstrap-js');
require('angular-bootstrap-checkbox');
require('angular-ui-bootstrap');

console.log('init: angular-js'); // eslint-disable-line no-console

module.exports = window['angular'] = angular;
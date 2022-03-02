const angular = require('vendor/angular-js');
const search = require('apps/search');
const centralSearch = require('apps/onms-central-search');

const MODULE_NAME = 'onms.default.apps';

angular.module(MODULE_NAME, [ 'onms.central.search', 'onms-search' ]);

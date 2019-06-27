const angular = require('vendor/angular-js');
const spotlight = require('apps/onms-spotlight');
const search = require('apps/search');

const MODULE_NAME = 'onms.default.apps';

angular.module(MODULE_NAME, [ 'onms.spotlight', 'onms-search' ]);

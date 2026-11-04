/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
// This module carries global AngularJS setup (loading bar, ngResource, ui.router,
// onms.http, angular-bootstrap-toggle and hashPrefix('')) formerly provided by the
// removed onms-central-search module. Many apps depend on onms.default.apps for it,
// so do not remove it as unused.
const angular = require('vendor/angular-js');
require('../../lib/onms-http');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle');
require('angular-bootstrap-toggle/dist/angular-bootstrap-toggle.css');
require('angular-ui-router');
const search = require('apps/search');

const MODULE_NAME = 'onms.default.apps';

angular.module(MODULE_NAME, [
    'angular-loading-bar',
    'ngResource',
    'ui.router',
    'onms.http',
    'onms-search',
])
    .config(['$locationProvider', function($locationProvider) {
        $locationProvider.hashPrefix('');
    }]);

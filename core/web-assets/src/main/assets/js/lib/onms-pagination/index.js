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
const angular = require('vendor/angular-js');
const paginationTemplate = require('./pagination-toolbar.html');

const MODULE_NAME = 'onms.pagination';

angular.module(MODULE_NAME, [ 'ui.bootstrap' ])
    .directive('pagination', function() {
        return {
            restrict: 'E',
            scope: {
                model: '=model',
                position: '@position',
                onChangeCallback: '=onChange'
            },
            link: function(scope, element, attrs) {
                if (scope.model === undefined) { throw new Error('No model defined.'); }
                if (scope.model.page === undefined) { throw new Error('No attribute model.page defined'); }
                if (scope.model.totalItems === undefined) { throw new Error('No attribute model.totalItems defined'); }
                if (scope.model.limit === undefined) { throw new Error('No attribute model.limit defined'); }

                let currentPage = scope.model.page;
                scope.onChange = function() {
                    if (currentPage !== scope.model.page) {
                        currentPage = scope.model.page;
                        if (scope.onChangeCallback) {
                            scope.onChangeCallback();
                        }
                    }
                };
            },
            transclude: true,
            templateUrl: paginationTemplate
        }
    });

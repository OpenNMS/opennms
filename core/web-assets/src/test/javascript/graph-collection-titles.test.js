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

'use strict';

// Collection titles are user input and the server does not restrict them, so the two
// AngularJS places that show them as HTML must neutralize any markup in them.

const fs = require('fs');
const path = require('path');
const angular = require('angular-js');
require('angular-mocks');
require('search');
require('graph-results');

const TITLE = 'Core <img src="x" onerror="window.pwned = true">';

describe('onms-search graph collection typeahead', function() {
  let scope, compile, httpBackend;

  beforeEach(angular.mock.module('onms-search'));

  beforeEach(angular.mock.inject(function($rootScope, $compile, $httpBackend, $controller) {
    scope = $rootScope.$new();
    compile = $compile;
    httpBackend = $httpBackend;
    $controller('GraphCollectionSearchCtrl', { '$scope': scope });
  }));

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  test('sanitizes a title before showing it as a match', function() {
    // the directive's templateUrl is mocked away under jest, so compile the template itself
    const template = fs.readFileSync(path.join(__dirname, '../../main/assets/js/apps/search/template.graph-collections.html'), 'utf8');
    const element = compile(template)(scope);
    document.body.appendChild(element[0]);
    scope.$digest();

    httpBackend.expect('GET', 'api/v2/graph-collections').respond([{ id: 1, title: TITLE, graphs: [] }]);
    const input = element.find('input');
    input.val('Core');
    input.triggerHandler('input');
    httpBackend.flush();
    scope.$digest();

    const match = element[0].querySelector('.uib-typeahead-match');
    expect(match).not.toBeNull();
    expect(match.textContent).toContain('Core');
    expect(match.querySelector('[onerror]')).toBeNull();
    expect(window.pwned).toBeUndefined();
    element.remove();
  });
});

describe('onms-graph-results growl messages', function() {
  const growl = { success: jest.fn(), warning: jest.fn(), error: jest.fn() };
  let scope, httpBackend;

  beforeEach(angular.mock.module('onms-graph-results', function($provide) {
    $provide.value('growl', growl);
  }));

  beforeEach(angular.mock.inject(function($rootScope, $httpBackend, $controller) {
    scope = $rootScope.$new();
    httpBackend = $httpBackend;
    $controller('AddToGraphCollectionCtrl', { '$scope': scope });
    growl.success.mockClear();
    growl.warning.mockClear();
    growl.error.mockClear();
  }));

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  test('escape the names they quote', function() {
    httpBackend.expect('POST', 'api/v2/graph-collections/3/graphs').respond(200, {});
    scope.addGraph({ id: 3, title: TITLE, graphs: [] }, 'node[1].nodeSnmp[]', 'rtr <b>1</b>', 'mib2.bits', 'Bits <i>in</i>', '1_day');
    httpBackend.flush();
    expect(growl.success).toHaveBeenCalledWith('The graph "Bits &lt;i&gt;in&lt;/i&gt;" has been added to "Core &lt;img src=&quot;x&quot; onerror=&quot;window.pwned = true&quot;&gt;"');
  });

  test('escape the names in the already-added warning', function() {
    const collection = { id: 3, title: 'Core <b>x</b>', graphs: [{ resourceId: 'node[1].nodeSnmp[]', graphtype: 'mib2.bits' }] };
    scope.addGraph(collection, 'node[1].nodeSnmp[]', 'rtr <b>1</b>', 'mib2.bits', 'Bits <i>in</i>', '1_day');
    expect(growl.warning).toHaveBeenCalledWith('The graph "Bits &lt;i&gt;in&lt;/i&gt;" for resource "rtr &lt;b&gt;1&lt;/b&gt;" has been already added to "Core &lt;b&gt;x&lt;/b&gt;"');
  });

  test('escape the server\'s refusal, which may quote the title', function() {
    httpBackend.expect('POST', 'api/v2/graph-collections/3/graphs').respond(400, 'Graph 1: the title must be <plain>.');
    scope.addGraph({ id: 3, title: 'Core', graphs: [] }, 'node[1].nodeSnmp[]', 'rtr', 'mib2.bits', 'Bits', '1_day');
    httpBackend.flush();
    expect(growl.error).toHaveBeenCalledWith('Graph 1: the title must be &lt;plain&gt;.');
  });
});

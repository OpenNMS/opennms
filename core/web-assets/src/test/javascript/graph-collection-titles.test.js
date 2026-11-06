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

describe('onms-graph-results add dialog', function() {
  let scope, httpBackend;

  beforeEach(angular.mock.module('onms-graph-results'));

  beforeEach(angular.mock.inject(function($rootScope, $httpBackend, $controller) {
    scope = $rootScope.$new();
    httpBackend = $httpBackend;
    httpBackend.expect('GET', 'api/v2/graph-collections').respond([
      { id: 3, title: 'Core', graphs: [{ resourceId: 'node[1].nodeSnmp[]', graphtype: 'mib2.bits', timespan: '1_day' }] },
      { id: 4, title: 'Edge', graphs: [] }
    ]);
    httpBackend.expect('GET', 'api/v2/graph-collections/timespans').respond([{ id: '1_day', label: '1 day' }]);
    $controller('AddToGraphCollectionModalInstanceCtrl', {
      '$scope': scope,
      '$uibModalInstance': { close: jest.fn(), dismiss: jest.fn() },
      'resourceId': 'node[1].nodeSnmp[]',
      'resourceLabel': 'rtr',
      'graphName': 'mib2.bits',
      'graphTitle': 'Bits'
    });
    httpBackend.flush();
  }));

  afterEach(function() {
    httpBackend.verifyNoOutstandingExpectation();
    httpBackend.verifyNoOutstandingRequest();
  });

  test('points out a collection that already holds the graph, without refusing it', function() {
    expect(scope.alreadyIn()).toBe(false);
    scope.collection = scope.collections[0];
    expect(scope.alreadyIn()).toBe(true);
    scope.collection = scope.collections[1];
    expect(scope.alreadyIn()).toBe(false);
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

  test('add a graph already in the collection a second time, as the Vue dialog does', function() {
    const collection = { id: 3, title: 'Core', graphs: [{ resourceId: 'node[1].nodeSnmp[]', graphtype: 'mib2.bits', timespan: '1_day' }] };
    httpBackend.expect('POST', 'api/v2/graph-collections/3/graphs', function(body) {
      return JSON.parse(body).timespan === '7_day';
    }).respond(200, {});
    scope.addGraph(collection, 'node[1].nodeSnmp[]', 'rtr', 'mib2.bits', 'Bits', '7_day');
    httpBackend.flush();
    expect(growl.warning).not.toHaveBeenCalled();
    expect(growl.success).toHaveBeenCalledWith('The graph "Bits" has been added to "Core"');
  });

  test('escape the server\'s refusal, which may quote the title', function() {
    httpBackend.expect('POST', 'api/v2/graph-collections/3/graphs').respond(400, 'Graph 1: the title must be <plain>.');
    scope.addGraph({ id: 3, title: 'Core', graphs: [] }, 'node[1].nodeSnmp[]', 'rtr', 'mib2.bits', 'Bits', '1_day');
    httpBackend.flush();
    expect(growl.error).toHaveBeenCalledWith('Graph 1: the title must be &lt;plain&gt;.');
  });
});

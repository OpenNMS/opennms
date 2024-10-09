///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { describe, expect, test } from 'vitest'
import { categories, monitoringLocations } from './utils'
import { useNodeQuery } from '@/components/Nodes/hooks/useNodeQuery'
import { MatchType, NodeQueryFilter, SetOperator } from '@/types'

const {
  buildNodeQueryFilterFromQueryString,
  buildUpdatedNodeStructureQueryParameters,
  getDefaultNodeQueryFilter,
  queryStringHasTrackedValues
} = useNodeQuery()

describe('Nodes useNodeQuery test', () => {
  describe('buildNodeQueryFilterFromQueryString', () => {
    test('empty query', () => {
      const queryObject = {}
      const filter: NodeQueryFilter = buildNodeQueryFilterFromQueryString(queryObject, categories, monitoringLocations)
      
      const expected = getDefaultNodeQueryFilter()
      expect(filter).toEqual(expected)
    })

    test('nodename -> searchTerm', () => {
      const queryObject = { nodename: 'Node1' }
      const filter = buildNodeQueryFilterFromQueryString(queryObject, categories, monitoringLocations)

      const expected = getDefaultNodeQueryFilter()
      expected.searchTerm = 'Node1'
      expect(filter).toEqual(expected)
    })

    test.each([
      [ 'by id, union', { categories: '1,2' }, SetOperator.Union, [categories[0], categories[1]]],
      [ 'by id, intersection', { categories: '1;2' }, SetOperator.Intersection, [categories[0], categories[1]]],
      [ 'by names, union', { categories: 'Routers,Switches' }, SetOperator.Union, [categories[0], categories[1]]],
      [ 'by names, intersection', { categories: 'Routers;Switches' }, SetOperator.Intersection, [categories[0], categories[1]]],
      [ 'by names, including invalid name', { categories: 'Routers,Whatever,Switches,Development' }, SetOperator.Union, [categories[0], categories[1], categories[5]]],
      [ 'by names, no valid names', { categories: 'Whatever,Something' }, SetOperator.Union, []]
    ]) (
      'buildNodeQueryFilterFromQueryString: categories %s',
      (title, queryObject, expectedCategoryMode, expectedCategories) => {
        const filter = buildNodeQueryFilterFromQueryString(queryObject, categories, monitoringLocations)

        const expected = getDefaultNodeQueryFilter()
        expected.categoryMode = expectedCategoryMode
        expected.selectedCategories = expectedCategories
        expect(filter).toEqual(expected)
      }
    )

    test.each([
      [ 'Default', { monitoringLocation: 'Default' }, SetOperator.Union, [monitoringLocations[0]]],
      [ 'Loc0', { monitoringLocation: 'Loc0' }, SetOperator.Union, [monitoringLocations[1]]],
      [ 'not found', { monitoringLocation: 'Somewhere' }, SetOperator.Union, []]
    ]) (
      'buildNodeQueryFilterFromQueryString: monitoring locations, %s',
      (title, queryObject, expectedCategoryMode, expectedLocations) => {
        const filter = buildNodeQueryFilterFromQueryString(queryObject, categories, monitoringLocations)

        const expected = getDefaultNodeQueryFilter()
        expected.categoryMode = expectedCategoryMode
        expected.selectedMonitoringLocations = expectedLocations
        expect(filter).toEqual(expected)
      }
    )

    test.each([
      [ 'true', { flows: 'true' }, SetOperator.Union, ['Ingress', 'Egress']],
      [ 'ingress', { flows: 'ingress' }, SetOperator.Union, ['Ingress']],
      [ 'egress', { flows: 'egress' }, SetOperator.Union, ['Egress']],
      [ 'false', { flows: 'false' }, SetOperator.Union, []],
      [ 'invalid', { flows: 'whatever' }, SetOperator.Union, []],
      [ 'empty', { flows: '' }, SetOperator.Union, []]
    ]) (
      'buildNodeQueryFilterFromQueryString: flows, %s',
      (title, queryObject, expectedCategoryMode, expectedFlows) => {
        const filter = buildNodeQueryFilterFromQueryString(queryObject, categories, monitoringLocations)

        const expected = getDefaultNodeQueryFilter()
        expected.categoryMode = expectedCategoryMode
        expected.selectedFlows = expectedFlows
        expect(filter).toEqual(expected)
      }
    )

    test.each([
      [ 'iplike, empty', { iplike: '' }, ''],
      [ 'iplike, valid IPv4', { iplike: '192.168.1.1' }, '192.168.1.1'],
      [ 'iplike, valid IPv6', { iplike: 'FE80:0000:0000:0000:0202:B3FF:FE1E:8329' }, 'FE80:0000:0000:0000:0202:B3FF:FE1E:8329'],
      [ 'iplike, invalid IP', { iplike: '192.168.' }, ''],
      [ 'ipAddress, valid IPv4', { ipAddress: '192.168.1.1' }, '192.168.1.1'],
      [ 'ipAddress, valid IPv6', { ipAddress: 'FE80:0000:0000:0000:0202:B3FF:FE1E:8329' }, 'FE80:0000:0000:0000:0202:B3FF:FE1E:8329'],
      [ 'ipAddress, invalid IP', { ipAddress: '192.168.' }, '']
    ]) (
      'buildNodeQueryFilterFromQueryString: ipLike, %s',
      (title, queryObject, expectedIp) => {
        const filter = buildNodeQueryFilterFromQueryString(queryObject, categories, monitoringLocations)

        const expected = getDefaultNodeQueryFilter()
        expected.extendedSearch.ipAddress = expectedIp
        expect(filter).toEqual(expected)
      }
    )

    test.each([
      [
        'all properties, contains',
        {
          snmpifalias: 'Alias',
          snmpifdescription: 'Description',
          snmpifindex: 14,
          snmpifname: 'Name',
          snmpiftype: 'Type',
          snmpMatchType: 'contains'
        },
        {
          snmpIfAlias: 'Alias',
          snmpIfDescription: 'Description',
          snmpIfIndex: 14,
          snmpIfName: 'Name',
          snmpIfType: 'Type',
          snmpMatchType: MatchType.Contains
        }
      ],
      [
        'most properties, equals',
        {
          snmpifalias: 'Alias',
          snmpifdescription: 'Description',
          snmpifindex: 14,
          snmpifname: 'Name',
          snmpiftype: 'Type',
          snmpMatchType: ''
        },
        {
          snmpIfAlias: 'Alias',
          snmpIfDescription: 'Description',
          snmpIfIndex: 14,
          snmpIfName: 'Name',
          snmpIfType: 'Type',
          snmpMatchType: MatchType.Equals
        }
      ]
    ]) (
      'buildNodeQueryFilterFromQueryString: snmpParams, %s',
      (title, queryObject, expectedSnmp) => {
        const filter = buildNodeQueryFilterFromQueryString(queryObject, categories, monitoringLocations)

        const expected = getDefaultNodeQueryFilter()
        expected.extendedSearch.snmpParams = expectedSnmp
        expect(filter).toEqual(expected)
      }
    )

    test.each([
      [
        'all properties',
        {
          sysContact: 'Contact',
          sysDescription: 'Description',
          sysLocation: 'Location',
          sysName: 'Name',
          sysObjectId: '.1.3.6.1'
        },
        {
          sysContact: 'Contact',
          sysDescription: 'Description',
          sysLocation: 'Location',
          sysName: 'Name',
          sysObjectId: '.1.3.6.1'
        }
      ],
      [
        'some properties',
        {
          sysContact1: 'Contact',
          sysDescription: 'Description',
          sysLocation3: 'Location',
          sysName: 'Name',
          sysObjectId: '.1.3.6.1',
          somethingElse: 'Something'
        },
        {
          sysContact: '',
          sysDescription: 'Description',
          sysLocation: '',
          sysName: 'Name',
          sysObjectId: '.1.3.6.1'
        }
      ]
    ]) (
      'buildNodeQueryFilterFromQueryString: sysParams, %s',
      (title, queryObject, expectedSys) => {
        const filter = buildNodeQueryFilterFromQueryString(queryObject, categories, monitoringLocations)

        const expected = getDefaultNodeQueryFilter()
        expected.extendedSearch.sysParams = expectedSys
        expect(filter).toEqual(expected)
      }
    )

    test.each([
      [
        'foreignSource only',
        {
          foreignSource: 'FS'
        },
        {
          foreignSource: 'FS',
          foreignId: '',
          foreignSourceId: ''
        }
      ],
      [
        'foreignId only',
        {
          foreignId: 'ID'
        },
        {
          foreignSource: '',
          foreignId: 'ID',
          foreignSourceId: ''
        }
      ],
      [
        'fsfid only',
        {
          fsfid: 'FS:FID'
        },
        {
          foreignSource: '',
          foreignId: '',
          foreignSourceId: 'FS:FID'
        }
      ],
      [
        'foreignSourceId only',
        {
          foreignSourceId: 'FS:FID'
        },
        {
          foreignSource: '',
          foreignId: '',
          foreignSourceId: 'FS:FID'
        }
      ],
      [
        'all',
        {
          foreignSource: 'FS',
          foreignId: 'ID',
          foreignSourceId: 'FS:FID'
        },
        {
          foreignSource: 'FS',
          foreignId: 'ID',
          foreignSourceId: 'FS:FID'
        }
      ]
    ]) (
      'buildNodeQueryFilterFromQueryString: foreignSource, %s',
      (title, queryObject, expectedParams) => {
        const filter = buildNodeQueryFilterFromQueryString(queryObject, categories, monitoringLocations)

        const expected = getDefaultNodeQueryFilter()
        expected.extendedSearch.foreignSourceParams = expectedParams
        expect(filter).toEqual(expected)
      }
    )
  })

  describe('test buildUpdatedNodeStructureQueryParameters', () => {
    test.each([
      [
        'empty, _s is removed',
        {
          limit: 10,
          offset: 20,
          _s: ''
        },
        {
          ...getDefaultNodeQueryFilter()
        },
        {
          limit: 10,
          offset: 20
        }
      ],
      [
        'search term, overrides _s with correct node.label FIQL query',
        {
          limit: 10,
          offset: 20,
          _s: 'whatever'
        },
        {
          ...getDefaultNodeQueryFilter(),
          searchTerm: 'Node1'
        },
        {
          limit: 10,
          offset: 20,
          _s: 'node.label==*Node1*'
        }
      ]
    ]) (
      'buildUpdatedNodeStructureQueryParameters: %s',
      (title, queryParams, filter, expectedParams) => {
        const updatedParams = buildUpdatedNodeStructureQueryParameters(queryParams, filter)
        expect(updatedParams).toEqual(expectedParams)
      }
    )
  })
 
  describe('queryStringHasTrackedValues', () => {
    const trackedValues = [
      'categories',
      'flows',
      'ipAddress',
      'iplike',
      'listInterfaces',
      'monitoredService',
      'monitoringLocation',
      'nodeLabel',
      'nodename',
      'snmpifalias',
      'snmpifdescription',
      'snmpifindex',
      'snmpifname',
      'snmpMatchType',
      'snmpphysaddr',
      'foreignSource',
      'foreignId',
      'fsfid',
      'sysContact',
      'sysDescription',
      'sysLocation',
      'sysName',
      'sysObjectId'
    ]

    test.each([
      [ 'empty', {}, false ],
      [ 'untracked values', { something: 'abc' }, false ],
      [ 'empty flows', { flows: '' }, false ],
      [ 'flows false', { flows: 'false' }, true ],
      [ 'flows false, untracked value', { flows: 'false', something: 'whatever' }, true ]
    ]) (
      'queryStringHasTrackedValues: %s',
      (title, queryObject, expected) => {
        expect(queryStringHasTrackedValues(queryObject)).toBe(expected)
      }
    )

    test('queryStringHasTrackedValues: check all tracked values', () => {
      for (const t of trackedValues) {
        const queryObject = {};
        (queryObject as any)[t] = 'something'

        expect(queryStringHasTrackedValues(queryObject)).toBe(true)
      }
    })
  })
})

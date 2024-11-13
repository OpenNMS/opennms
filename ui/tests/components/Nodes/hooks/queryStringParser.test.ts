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
import {
  parseCategories,
  parseFlows,
  parseForeignSource,
  parseIplike,
  parseMonitoringLocation,
  parseNodeLabel,
  parseSnmpParams,
  parseSysParams
} from '@/components/Nodes/hooks/queryStringParser'
import { categories, monitoringLocations } from './utils'
import { MatchType, SetOperator } from '@/types'

describe('Nodes queryStringParser test', () => {
  describe('queryStringParser, parseNodeLabel', () => {
    test.each([
      ['empty', {}, ''],
      ['nodename', { nodename: 'NodeName'}, 'NodeName'],
      ['nodename takes priority over nodeLabel', { nodename: 'NodeName', nodeLabel: 'NodeLabel'}, 'NodeName'],
      ['nodeLabel with nodename being invalid', { nodeName: 'NodeName', nodeLabel: 'NodeLabel'}, 'NodeLabel'],
      ['nodeLabel with empty nodename', { nodename: '', nodeLabel: 'NodeLabel'}, 'NodeLabel'],
      ['nodeLabel only', { nodeLabel: 'NodeLabel'}, 'NodeLabel'],
      ['invalid nodeLabel', { nodelabel: 'NodeLabel'}, ''],
      ['nodename with another property', { nodename: 'NodeName', a: 'Whatever' }, 'NodeName']
    ]) (
      'parseNodeLabel: %s',
      (title, queryObject, expected) => {
        const result = parseNodeLabel(queryObject)
        expect(result).toEqual(expected)
      }
    )
  })

  describe('queryStringParser, parseCategories', () => {
    test.each([
      ['empty', {}, SetOperator.Union, []],
      ['empty categories', { categories: '' }, SetOperator.Union, []],
      ['one category by id, union', { categories: '1' }, SetOperator.Union, [categories[0]]],
      ['three categories by id, union', { categories: '1,2,4' }, SetOperator.Union, [categories[0], categories[1], categories[3]]],
      ['three categories by name, union', { categories: 'Routers,Switches,Production' }, SetOperator.Union, [categories[0], categories[1], categories[3]]],
      ['three categories by name, intersection', { categories: 'Routers;Switches;Production' }, SetOperator.Intersection, [categories[0], categories[1], categories[3]]],
      ['three categories by name and id, union', { categories: 'Routers,2,5' }, SetOperator.Union, [categories[0], categories[1], categories[4]]],
      ['three categories by name and id, one invalid, union', { categories: 'Routers,2,Whatever' }, SetOperator.Union, [categories[0], categories[1]]],
      ['three categories by name and id, one invalid, intersection', { categories: 'Routers;2;Whatever' }, SetOperator.Intersection, [categories[0], categories[1]]]
    ]) (
      'parseCategories: %s',
      (title, queryObject, expectedCategoryMode, expectedCategories) => {
        const result = parseCategories(queryObject, categories)
        expect(result).toEqual({ categoryMode: expectedCategoryMode, selectedCategories: expectedCategories })
      }
    )
  })

  describe('queryStringParser, parseMonitoringLocation', () => {
    test.each([
      ['empty', {}, null],
      ['empty location', { monitoringLocation: '' }, null],
      ['default location', { monitoringLocation: 'Default' }, monitoringLocations[0]],
      ['other location', { monitoringLocation: 'Loc0' }, monitoringLocations[1]],
      ['invalid location', { monitoringLocation: 'Something' }, null]
    ]) (
      'parseMonitoringLocation: %s',
      (title, queryObject, expected) => {
        const result = parseMonitoringLocation(queryObject, monitoringLocations)
        expect(result).toEqual(expected)
      }
    )
  })

  describe('queryStringParser, parseFlows', () => {
    test.each([
      ['empty', {}, []],
      ['true', { flows: 'true' }, ['Ingress', 'Egress']],
      ['ingress', { flows: 'ingress' }, ['Ingress']],
      ['egress', { flows: 'egress' }, ['Egress']],
      ['false', { flows: 'false' }, []]
    ]) (
      'parseFlows: %s',
      (title, queryObject, expected) => {
        const result = parseFlows(queryObject)
        expect(result).toEqual(expected)
      }
    )
  })

  describe('queryStringParser, parseIpLike', () => {
    test.each([
      ['valid ipAddress IPv4 1', { ipAddress: '0.0.0.0'}, '0.0.0.0'],
      ['valid ipAddress IPv4 2', { ipAddress: '192.168.0.1'}, '192.168.0.1'],
      ['valid ipAddress IPv6', { ipAddress: 'FE80:0000:0000:0000:0202:B3FF:FE1E:8329'}, 'FE80:0000:0000:0000:0202:B3FF:FE1E:8329'],

      ['valid iplike IPv4 1', { iplike: '0.0.0.0'}, '0.0.0.0'],
      ['valid iplike IPv4 2', { iplike: '192.168.0.1'}, '192.168.0.1'],
      ['valid iplike IPv6', { iplike: 'FE80:0000:0000:0000:0202:B3FF:FE1E:8329'}, 'FE80:0000:0000:0000:0202:B3FF:FE1E:8329'],

      ['empty ipAddress', { ipAddress: ''}, null],
      ['invalid ipAddress', { ipAddress: 'abc'}, null],
      ['invalid ipAddress localhost', { ipAddress: 'localhost'}, null],
      ['invalid partial ipAddress', { ipAddress: '192.168.'}, null],
      ['invalid ipAddress', { ipAddress: 'A.B.C.D'}, null],

      ['empty iplike', { iplike: ''}, null],
      ['invalid iplike', { iplike: 'abc'}, null],
      ['invalid iplike localhost', { iplike: 'localhost'}, null],
      ['invalid partial iplike', { iplike: '192.168.'}, null],
      ['invalid iplike', { iplike: 'A.B.C.D'}, null]
    ]) (
      'parseIpLike: %s',
      (title, queryObject, expected) => {
        const result = parseIplike(queryObject)
        expect(result).toEqual(expected)
      }
    )
  })

  describe('queryStringParser, parseForeignSource', () => {
    test.each([
      ['empty', {}, null],
      ['FS only', { foreignSource: 'FS' }, { foreignSource: 'FS', foreignId: '', foreignSourceId: '' }],
      ['FID only', { foreignId: 'FID' }, { foreignSource: '', foreignId: 'FID', foreignSourceId: '' }],
      ['FS:FID only', { foreignSourceId: 'FS:FID' }, { foreignSource: '', foreignId: '', foreignSourceId: 'FS:FID' }],
      ['FS:FID only, fsfid', { fsfid: 'FS:FID' }, { foreignSource: '', foreignId: '', foreignSourceId: 'FS:FID' }],
      ['foreignSource, fsfid', { foreignSource: 'FS', fsfid: 'FS:FID' }, { foreignSource: 'FS', foreignId: '', foreignSourceId: 'FS:FID' }]
    ]) (
      'parseForeignSource: %s',
      (title, queryObject, expected) => {
        const result = parseForeignSource(queryObject)
        expect(result).toEqual(expected)
      }
    )
  })

  describe('queryStringParser, parseSnmpParams', () => {
    test.each([
      ['empty', {}, null],
      ['snmpifalias only', { snmpifalias: 'IfAlias' }, { snmpIfAlias: 'IfAlias' }],
      [
        'several properties',
        { snmpifalias: 'IfAlias', snmpifdescription: 'If Description', snmpifindex: '3', snmpifname: 'Snmp Name' },
        {
          snmpIfAlias: 'IfAlias',
          snmpIfDescription: 'If Description',
          snmpIfIndex: '3',
          snmpIfName: 'Snmp Name',
          snmpIfType: '',
          snmpMatchType: MatchType.Equals
        }
      ],
      [
        'several properties, contains',
        { snmpifalias: 'IfAlias', snmpifdescription: 'If Description', snmpifindex: '3', snmpifname: 'Snmp Name', snmpMatchType: 'contains' },
        {
          snmpIfAlias: 'IfAlias',
          snmpIfDescription: 'If Description',
          snmpIfIndex: '3',
          snmpIfName: 'Snmp Name',
          snmpIfType: '',
          snmpMatchType: MatchType.Contains
        }
      ]
    ]) (
      'parseSnmpParams: %s',
      (title, queryObject, expected) => {
        const result = parseSnmpParams(queryObject)

        const fullExpected = {
          snmpIfAlias: '',
          snmpIfDescription: '',
          snmpIfIndex: '',
          snmpIfName: '',
          snmpIfType: '',
          snmpMatchType: MatchType.Equals,
          ...expected
        }

        if (expected === null) {
          expect(result).toBeNull()
        } else {
          expect(result).toEqual(fullExpected)
        }
      }
    )
  })

  describe('queryStringParser, parseSysParams', () => {
    test.each([
      ['empty', {}, null],
      ['sysContact only', { sysContact: 'A Contact' }, { sysContact: 'A Contact' }],
      [
        'several properties',
        { sysContact: 'Contact', sysDescription: 'Sys Description', sysLocation: 'Location', sysName: 'Sys Name', sysObjectId: '.1.3.6.1' },
        { sysContact: 'Contact', sysDescription: 'Sys Description', sysLocation: 'Location', sysName: 'Sys Name', sysObjectId: '.1.3.6.1' }
      ]
    ]) (
      'parseSysParams: %s',
      (title, queryObject, expected) => {
        const result = parseSysParams(queryObject)

        const fullExpected = {
          sysContact: '',
          sysDescription: '',
          sysLocation: '',
          sysName: '',
          sysObjectId: '',
          ...expected
        }

        if (expected === null) {
          expect(result).toBeNull()
        } else {
          expect(result).toEqual(fullExpected)
        }
      }
    )
  })
})

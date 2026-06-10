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
  parseMaclike,
  parseMib2Params,
  parseMonitoredServices,
  parseMonitoringLocation,
  parseNodeLabel,
  parseSnmpParams,
  parseSnmpParmParams,
  parseSysParams
} from '@/components/Nodes/hooks/queryStringParser'
import { categories, monitoringLocations, serviceTypes } from './utils'
import { MatchType, SetOperator } from '@/types'
import { DEFAULT_MONITORING_LOCATION } from '@/lib/constants'

describe('Nodes queryStringParser test', () => {
  describe('queryStringParser, parseNodeLabel', () => {
    test.each([
      ['empty', {}, ''],
      ['nodename', { nodename: 'NodeName' }, 'NodeName'],
      ['nodename takes priority over nodeLabel', { nodename: 'NodeName', nodeLabel: 'NodeLabel' }, 'NodeName'],
      ['nodeLabel with nodename being invalid', { nodeName: 'NodeName', nodeLabel: 'NodeLabel' }, 'NodeLabel'],
      ['nodeLabel with empty nodename', { nodename: '', nodeLabel: 'NodeLabel' }, 'NodeLabel'],
      ['nodeLabel only', { nodeLabel: 'NodeLabel' }, 'NodeLabel'],
      ['invalid nodeLabel', { nodelabel: 'NodeLabel' }, ''],
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
        expect(result).toEqual({ categoryMode: expectedCategoryMode, selectedCategories: expectedCategories, selectedCategories2: [] })
      }
    )
  })

  describe('queryStringParser, parseMonitoringLocation', () => {
    test.each([
      ['empty', {}, null],
      ['empty location', { monitoringLocation: '' }, null],
      ['default location', { monitoringLocation: DEFAULT_MONITORING_LOCATION }, monitoringLocations[0]],
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
      ['false', { flows: 'false' }, ['No Flows']]
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
      ['valid ipAddress IPv4 1', { ipAddress: '0.0.0.0' }, '0.0.0.0'],
      ['valid ipAddress IPv4 2', { ipAddress: '192.168.0.1' }, '192.168.0.1'],
      ['valid ipAddress IPv6', { ipAddress: 'FE80:0000:0000:0000:0202:B3FF:FE1E:8329' }, 'FE80:0000:0000:0000:0202:B3FF:FE1E:8329'],

      ['valid iplike IPv4 1', { iplike: '0.0.0.0' }, '0.0.0.0'],
      ['valid iplike IPv4 2', { iplike: '192.168.0.1' }, '192.168.0.1'],
      ['valid iplike IPv6', { iplike: 'FE80:0000:0000:0000:0202:B3FF:FE1E:8329' }, 'FE80:0000:0000:0000:0202:B3FF:FE1E:8329'],

      ['empty ipAddress', { ipAddress: '' }, null],
      ['invalid ipAddress', { ipAddress: 'abc' }, null],
      ['invalid ipAddress localhost', { ipAddress: 'localhost' }, null],
      ['invalid partial ipAddress', { ipAddress: '192.168.' }, null],
      ['invalid ipAddress', { ipAddress: 'A.B.C.D' }, null],

      ['empty iplike', { iplike: '' }, null],
      ['invalid iplike', { iplike: 'abc' }, null],
      ['invalid iplike localhost', { iplike: 'localhost' }, null],
      ['invalid partial iplike', { iplike: '192.168.' }, null],
      ['invalid iplike', { iplike: 'A.B.C.D' }, null]
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

  describe('parseMib2Params', () => {
    test.each([
      ['sysDescription contains', { mib2Parm: 'sysDescription', mib2ParmValue: 'Linux', mib2ParmMatchType: 'contains' },
        { sysContact: '', sysDescription: 'Linux', sysLocation: '', sysName: '', sysObjectId: '', sysMatchType: MatchType.Contains }],
      ['sysContact equals', { mib2Parm: 'sysContact', mib2ParmValue: 'admin', mib2ParmMatchType: 'equals' },
        { sysContact: 'admin', sysDescription: '', sysLocation: '', sysName: '', sysObjectId: '', sysMatchType: MatchType.Equals }],
      ['sysName, match type defaults to Contains when omitted', { mib2Parm: 'sysName', mib2ParmValue: 'router' },
        { sysContact: '', sysDescription: '', sysLocation: '', sysName: 'router', sysObjectId: '', sysMatchType: MatchType.Contains }],
      ['sysLocation', { mib2Parm: 'sysLocation', mib2ParmValue: 'datacenter', mib2ParmMatchType: 'contains' },
        { sysContact: '', sysDescription: '', sysLocation: 'datacenter', sysName: '', sysObjectId: '', sysMatchType: MatchType.Contains }],
      ['sysObjectId', { mib2Parm: 'sysObjectId', mib2ParmValue: '.1.3.6.1', mib2ParmMatchType: 'equals' },
        { sysContact: '', sysDescription: '', sysLocation: '', sysName: '', sysObjectId: '.1.3.6.1', sysMatchType: MatchType.Equals }]
    ]) ('parseMib2Params: %s', (title, queryObject, expected) => {
      expect(parseMib2Params(queryObject)).toEqual(expected)
    })

    test.each([
      ['empty parm', { mib2Parm: '', mib2ParmValue: 'Linux' }],
      ['empty value', { mib2Parm: 'sysDescription', mib2ParmValue: '' }],
      ['invalid parm name', { mib2Parm: 'badField', mib2ParmValue: 'Linux' }],
      ['both empty', {}]
    ]) ('parseMib2Params: returns null for invalid input: %s', (title, queryObject) => {
      expect(parseMib2Params(queryObject)).toBeNull()
    })
  })

  describe('parseSnmpParmParams', () => {
    test.each([
      ['ifAlias equals', { snmpParm: 'ifAlias', snmpParmValue: 'Uplink', snmpParmMatchType: 'equals' },
        { snmpIfAlias: 'Uplink', snmpIfDescription: '', snmpIfIndex: '', snmpIfName: '', snmpIfType: '', snmpMatchType: MatchType.Equals }],
      ['ifName contains', { snmpParm: 'ifName', snmpParmValue: 'eth', snmpParmMatchType: 'contains' },
        { snmpIfAlias: '', snmpIfDescription: '', snmpIfIndex: '', snmpIfName: 'eth', snmpIfType: '', snmpMatchType: MatchType.Contains }],
      ['ifDescr, match type defaults to Equals when omitted', { snmpParm: 'ifDescr', snmpParmValue: 'GigabitEthernet' },
        { snmpIfAlias: '', snmpIfDescription: 'GigabitEthernet', snmpIfIndex: '', snmpIfName: '', snmpIfType: '', snmpMatchType: MatchType.Equals }]
    ]) ('parseSnmpParmParams: %s', (title, queryObject, expected) => {
      expect(parseSnmpParmParams(queryObject)).toEqual(expected)
    })

    test.each([
      ['empty parm', { snmpParm: '', snmpParmValue: 'value' }],
      ['empty value', { snmpParm: 'ifAlias', snmpParmValue: '' }],
      ['unsupported parm (ifIndex)', { snmpParm: 'ifIndex', snmpParmValue: '1' }],
      ['unknown parm', { snmpParm: 'badField', snmpParmValue: 'value' }],
      ['both empty', {}]
    ]) ('parseSnmpParmParams: returns null for invalid input: %s', (title, queryObject) => {
      expect(parseSnmpParmParams(queryObject)).toBeNull()
    })
  })

  describe('parseCategories: category1/category2 aliases', () => {
    test('category1 alone maps as union in group 1', () => {
      const result = parseCategories({ category1: 'Routers' }, categories)
      expect(result.categoryMode).toBe(SetOperator.Union)
      expect(result.selectedCategories).toEqual([categories[0]])
      expect(result.selectedCategories2).toEqual([])
    })

    test('category2 alone maps as union in group 1 (backwards compat)', () => {
      const result = parseCategories({ category2: 'Switches' }, categories)
      expect(result.categoryMode).toBe(SetOperator.Union)
      expect(result.selectedCategories).toEqual([categories[1]])
      expect(result.selectedCategories2).toEqual([])
    })

    test('category1 and category2 populate separate groups', () => {
      const result = parseCategories({ category1: 'Routers', category2: 'Switches' }, categories)
      expect(result.categoryMode).toBe(SetOperator.Union)
      expect(result.selectedCategories).toEqual([categories[0]])
      expect(result.selectedCategories2).toEqual([categories[1]])
    })

    test('category1 array and category2 array populate two groups with union within each', () => {
      const result = parseCategories(
        { category1: ['Routers', 'Switches'], category2: ['Production', 'Servers'] },
        categories
      )
      expect(result.selectedCategories).toEqual([categories[0], categories[1]])
      expect(result.selectedCategories2).toEqual([categories[3], categories[2]])
    })

    test('category1 array alone (no category2) puts all in group 1', () => {
      const result = parseCategories({ category1: ['Routers', 'Switches'] }, categories)
      expect(result.selectedCategories).toEqual([categories[0], categories[1]])
      expect(result.selectedCategories2).toEqual([])
    })

    test('categories param takes precedence over category1/category2', () => {
      const result = parseCategories({ categories: 'Servers', category1: 'Routers', category2: 'Switches' }, categories)
      expect(result.selectedCategories).toEqual([categories[2]])
      expect(result.selectedCategories2).toEqual([])
    })

    test('unknown category1 value returns empty', () => {
      const result = parseCategories({ category1: 'NonExistent' }, categories)
      expect(result.selectedCategories).toEqual([])
      expect(result.selectedCategories2).toEqual([])
    })
  })

  describe('parseForeignSource: lowercase alias', () => {
    test('foreignsource (lowercase) maps to foreignSource', () => {
      const result = parseForeignSource({ foreignsource: 'MyFS' })
      expect(result).toEqual({ foreignSource: 'MyFS', foreignId: '', foreignSourceId: '' })
    })

    test('foreignSource (camelCase) still works', () => {
      const result = parseForeignSource({ foreignSource: 'MyFS' })
      expect(result).toEqual({ foreignSource: 'MyFS', foreignId: '', foreignSourceId: '' })
    })

    test('foreignSource takes precedence over foreignsource', () => {
      const result = parseForeignSource({ foreignSource: 'CamelFS', foreignsource: 'LowerFS' })
      expect(result).toEqual({ foreignSource: 'CamelFS', foreignId: '', foreignSourceId: '' })
    })
  })

  describe('parseMaclike', () => {
    test.each([
      ['empty', {}, null],
      ['maclike with colons', { maclike: 'AA:BB:CC:DD:EE:FF' }, 'aabbccddeeff'],
      ['maclike with dashes', { maclike: 'AA-BB-CC-DD-EE-FF' }, 'aabbccddeeff'],
      ['maclike no separators', { maclike: 'AABBCCDDEEFF' }, 'aabbccddeeff'],
      ['maclike already lowercase', { maclike: 'aabbccddeeff' }, 'aabbccddeeff'],
      ['snmpphysaddr', { snmpphysaddr: 'AA:BB:CC:DD:EE:FF' }, 'aabbccddeeff'],
      ['maclike takes precedence over snmpphysaddr', { maclike: '112233445566', snmpphysaddr: 'aabbccddeeff' }, '112233445566'],
      ['partial MAC still stripped', { maclike: 'AA:BB:CC' }, 'aabbcc'],
      ['empty maclike', { maclike: '' }, null]
    ]) ('parseMaclike: %s', (title, queryObject, expected) => {
      expect(parseMaclike(queryObject)).toEqual(expected)
    })
  })

  describe('parseMonitoredServices', () => {
    test('returns [] when no service param present', () => {
      expect(parseMonitoredServices({}, serviceTypes)).toEqual([])
    })

    test('resolves monitoredService by exact name', () => {
      expect(parseMonitoredServices({ monitoredService: 'HTTPS' }, serviceTypes)).toEqual(['HTTPS'])
    })

    test('resolves monitoredService case-insensitively', () => {
      expect(parseMonitoredServices({ monitoredService: 'https' }, serviceTypes)).toEqual(['HTTPS'])
    })

    test('resolves service param by exact name', () => {
      expect(parseMonitoredServices({ service: 'HTTPS' }, serviceTypes)).toEqual(['HTTPS'])
    })

    test('resolves service param by numeric ID', () => {
      expect(parseMonitoredServices({ service: '8' }, serviceTypes)).toEqual(['HTTPS'])
    })

    test('returns [] for unknown name', () => {
      expect(parseMonitoredServices({ monitoredService: 'UNKNOWN' }, serviceTypes)).toEqual([])
    })

    test('returns [] for unknown numeric ID', () => {
      expect(parseMonitoredServices({ service: '999' }, serviceTypes)).toEqual([])
    })

    test('monitoredService takes precedence over service when both present', () => {
      expect(parseMonitoredServices({ monitoredService: 'HTTP', service: 'ICMP' }, serviceTypes)).toEqual(['HTTP'])
    })
  })
})

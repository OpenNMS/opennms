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
import { rraToString, rraFromString } from '@/lib/timeSeriesHelpers'
import { ConsolidationFunctionType, RRA } from '@/types/timeSeries'

describe('timeSeriesHelpers', () => {
  describe('rraToString', () => {
    test('should convert RRA object to string with AVERAGE consolidation function', () => {
      const rra: RRA = {
        cf: ConsolidationFunctionType.AVERAGE,
        xff: 0.5,
        steps: 1,
        rows: 2880
      }
      expect(rraToString(rra)).toBe('RRA:AVERAGE:0.5:1:2880')
    })

    test('should convert RRA object to string with MIN consolidation function', () => {
      const rra: RRA = {
        cf: ConsolidationFunctionType.MIN,
        xff: 0.25,
        steps: 12,
        rows: 1440
      }
      expect(rraToString(rra)).toBe('RRA:MIN:0.25:12:1440')
    })

    test('should convert RRA object to string with MAX consolidation function', () => {
      const rra: RRA = {
        cf: ConsolidationFunctionType.MAX,
        xff: 0.75,
        steps: 60,
        rows: 720
      }
      expect(rraToString(rra)).toBe('RRA:MAX:0.75:60:720')
    })

    test('should convert RRA object to string with LAST consolidation function', () => {
      const rra: RRA = {
        cf: ConsolidationFunctionType.LAST,
        xff: 1,
        steps: 288,
        rows: 365
      }
      expect(rraToString(rra)).toBe('RRA:LAST:1:288:365')
    })

    test('should handle xff value of 0', () => {
      const rra: RRA = {
        cf: ConsolidationFunctionType.AVERAGE,
        xff: 0,
        steps: 1,
        rows: 100
      }
      expect(rraToString(rra)).toBe('RRA:AVERAGE:0:1:100')
    })

    test('should handle decimal xff values', () => {
      const rra: RRA = {
        cf: ConsolidationFunctionType.AVERAGE,
        xff: 0.123456,
        steps: 5,
        rows: 500
      }
      expect(rraToString(rra)).toBe('RRA:AVERAGE:0.123456:5:500')
    })
  })

  describe('rraFromString', () => {
    test('should parse valid RRA string with AVERAGE consolidation function', () => {
      const rraStr = 'RRA:AVERAGE:0.5:1:2880'
      const result = rraFromString(rraStr)
      
      expect(result.cf).toBe('AVERAGE')
      expect(result.xff).toBe(0.5)
      expect(result.steps).toBe(1)
      expect(result.rows).toBe(2880)
    })

    test('should parse valid RRA string with MIN consolidation function', () => {
      const rraStr = 'RRA:MIN:0.25:12:1440'
      const result = rraFromString(rraStr)
      
      expect(result.cf).toBe('MIN')
      expect(result.xff).toBe(0.25)
      expect(result.steps).toBe(12)
      expect(result.rows).toBe(1440)
    })

    test('should parse valid RRA string with MAX consolidation function', () => {
      const rraStr = 'RRA:MAX:0.75:60:720'
      const result = rraFromString(rraStr)
      
      expect(result.cf).toBe('MAX')
      expect(result.xff).toBe(0.75)
      expect(result.steps).toBe(60)
      expect(result.rows).toBe(720)
    })

    test('should parse valid RRA string with LAST consolidation function', () => {
      const rraStr = 'RRA:LAST:1:288:365'
      const result = rraFromString(rraStr)
      
      expect(result.cf).toBe('LAST')
      expect(result.xff).toBe(1)
      expect(result.steps).toBe(288)
      expect(result.rows).toBe(365)
    })

    test('should parse RRA string with xff value of 0', () => {
      const rraStr = 'RRA:AVERAGE:0:1:100'
      const result = rraFromString(rraStr)
      
      expect(result.xff).toBe(0)
    })

    test('should parse RRA string with decimal xff values', () => {
      const rraStr = 'RRA:AVERAGE:0.123456:5:500'
      const result = rraFromString(rraStr)
      
      expect(result.xff).toBeCloseTo(0.123456)
    })

    test('should throw error for string not starting with RRA', () => {
      const rraStr = 'INVALID:AVERAGE:0.5:1:2880'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: INVALID:AVERAGE:0.5:1:2880')
    })

    test('should throw error for string with too few parts', () => {
      const rraStr = 'RRA:AVERAGE:0.5:1'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: RRA:AVERAGE:0.5:1')
    })

    test('should throw error for string with too many parts', () => {
      const rraStr = 'RRA:AVERAGE:0.5:1:2880:extra'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: RRA:AVERAGE:0.5:1:2880:extra')
    })

    test('should throw error for invalid xff (non-numeric)', () => {
      const rraStr = 'RRA:AVERAGE:invalid:1:2880'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: RRA:AVERAGE:invalid:1:2880')
    })

    test('should throw error for invalid steps (non-numeric)', () => {
      const rraStr = 'RRA:AVERAGE:0.5:invalid:2880'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: RRA:AVERAGE:0.5:invalid:2880')
    })

    test('should throw error for invalid rows (non-numeric)', () => {
      const rraStr = 'RRA:AVERAGE:0.5:1:invalid'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: RRA:AVERAGE:0.5:1:invalid')
    })

    test('should throw error for xff less than 0', () => {
      const rraStr = 'RRA:AVERAGE:-0.1:1:2880'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: RRA:AVERAGE:-0.1:1:2880')
    })

    test('should throw error for xff greater than 1', () => {
      const rraStr = 'RRA:AVERAGE:1.5:1:2880'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: RRA:AVERAGE:1.5:1:2880')
    })

    test('should throw error for steps less than or equal to 0', () => {
      const rraStr = 'RRA:AVERAGE:0.5:0:2880'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: RRA:AVERAGE:0.5:0:2880')
    })

    test('should throw error for negative steps', () => {
      const rraStr = 'RRA:AVERAGE:0.5:-1:2880'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: RRA:AVERAGE:0.5:-1:2880')
    })

    test('should throw error for rows less than or equal to 0', () => {
      const rraStr = 'RRA:AVERAGE:0.5:1:0'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: RRA:AVERAGE:0.5:1:0')
    })

    test('should throw error for negative rows', () => {
      const rraStr = 'RRA:AVERAGE:0.5:1:-100'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: RRA:AVERAGE:0.5:1:-100')
    })

    test('should throw error for empty string', () => {
      const rraStr = ''
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: ')
    })

    test('should throw error for Infinity values', () => {
      const rraStr = 'RRA:AVERAGE:Infinity:1:2880'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: RRA:AVERAGE:Infinity:1:2880')
    })

    test('should throw error for NaN values', () => {
      const rraStr = 'RRA:AVERAGE:NaN:1:2880'
      
      expect(() => rraFromString(rraStr)).toThrow('Invalid RRA string: RRA:AVERAGE:NaN:1:2880')
    })
  })

  describe('round-trip conversion', () => {
    test('should maintain data integrity through conversion cycle with AVERAGE', () => {
      const original: RRA = {
        cf: ConsolidationFunctionType.AVERAGE,
        xff: 0.5,
        steps: 1,
        rows: 2880
      }
      
      const str = rraToString(original)
      const parsed = rraFromString(str)
      
      expect(parsed.cf).toBe(original.cf)
      expect(parsed.xff).toBe(original.xff)
      expect(parsed.steps).toBe(original.steps)
      expect(parsed.rows).toBe(original.rows)
    })

    test('should maintain data integrity through conversion cycle with MIN', () => {
      const original: RRA = {
        cf: ConsolidationFunctionType.MIN,
        xff: 0.25,
        steps: 12,
        rows: 1440
      }
      
      const str = rraToString(original)
      const parsed = rraFromString(str)
      
      expect(parsed.cf).toBe(original.cf)
      expect(parsed.xff).toBe(original.xff)
      expect(parsed.steps).toBe(original.steps)
      expect(parsed.rows).toBe(original.rows)
    })

    test('should maintain data integrity through conversion cycle with decimal xff', () => {
      const original: RRA = {
        cf: ConsolidationFunctionType.AVERAGE,
        xff: 0.333,
        steps: 5,
        rows: 500
      }
      
      const str = rraToString(original)
      const parsed = rraFromString(str)
      
      expect(parsed.cf).toBe(original.cf)
      expect(parsed.xff).toBeCloseTo(original.xff)
      expect(parsed.steps).toBe(original.steps)
      expect(parsed.rows).toBe(original.rows)
    })

    test('should maintain data integrity through multiple conversion cycles', () => {
      const original: RRA = {
        cf: ConsolidationFunctionType.MAX,
        xff: 0.75,
        steps: 60,
        rows: 720
      }
      
      const str1 = rraToString(original)
      const parsed1 = rraFromString(str1)
      const str2 = rraToString(parsed1)
      const parsed2 = rraFromString(str2)
      
      expect(parsed2.cf).toBe(original.cf)
      expect(parsed2.xff).toBe(original.xff)
      expect(parsed2.steps).toBe(original.steps)
      expect(parsed2.rows).toBe(original.rows)
      expect(str1).toBe(str2)
    })
  })
})

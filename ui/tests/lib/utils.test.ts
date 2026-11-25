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
import { reactive } from 'vue'
import {
  cloneState,
  htmlToText,
  ellipsify,
  hasNonEmptyProperty,
  isConvertibleToInteger,
  isNumber,
  isString,
  plural,
  shallowEqual
} from '@/lib/utils'

describe('lib/utils test', () => {
  test('test isNumber', async () => {
    const numberValues = [0, 1, 999, -1, -999, NaN]

    const nonNumberValues = [null, undefined, '', '0', '1', '999', '-1', '-999', 'abc', [0], { a: 1 }]

    for (const value of numberValues) {
      expect(isNumber(value)).toBe(true)
    }

    for (const value of nonNumberValues) {
      expect(isNumber(value)).toBe(false)
    }
  })

  test('test isConvertibleToInteger', async () => {
    const integerValues = [0, 1, 999, -1, -999, '0', '1', '999', '-1', '-999', [0], [1]]

    const nonIntegerValues = [null, undefined, '', 'abc', [0, 1], { a: 1 }, NaN, 1.1, -9.9]

    for (const value of integerValues) {
      expect(isConvertibleToInteger(value)).toBe(true)
    }

    for (const value of nonIntegerValues) {
      expect(isConvertibleToInteger(value), `value failed: ${value}`).toBe(false)
    }
  })

  test('test isString', async () => {
    const stringValues = ['', 'a', 'abc', 'DEFghi', 'MNO123!!!', new String(), new String('hello')]

    const nonStringValues = [null, 1, -1, 999, [0, 1], { a: 1 }, NaN, 1.1, -9.9]

    for (const value of stringValues) {
      expect(isString(value)).toBe(true)
    }

    for (const value of nonStringValues) {
      expect(isString(value)).toBe(false)
    }
  })

  test('test ellipsify', async () => {
    expect(ellipsify('', 1)).toBe('')
    expect(ellipsify('', 4)).toBe('')
    expect(ellipsify('abc', 4)).toBe('abc')
    expect(ellipsify('abcdef', 4)).toBe(`abc${'\u2026'}`)
    expect(ellipsify('Lorem ipsum dolor sit amet', 20)).toBe(`Lorem ipsum dolor s${'\u2026'}`)
  })

  test('test hasNonEmptyProperty', async () => {
    const emptyProperties = [null, undefined, {}, { a: 1 }, { a: [1, 2, 3] }]

    const nonEmptyProperties = [{ a: 'a' }, { a: 1, b: 'two' }]

    for (const value of emptyProperties) {
      expect(hasNonEmptyProperty(value), `value: ${value}`).toBe(false)
    }

    for (const value of nonEmptyProperties) {
      expect(hasNonEmptyProperty(value)).toBe(true)
    }
  })
})

describe('htmlToText', () => {
  test('drops the markup and collapses whitespace', () => {
    expect(htmlToText('<p>Interface <b>eth0</b>\n  down</p>')).toBe('Interface eth0 down')
  })

  test('decodes entities', () => {
    expect(htmlToText('a &amp; b &lt;c&gt;')).toBe('a & b <c>')
  })

  test('answers empty for nothing', () => {
    expect(htmlToText(undefined)).toBe('')
    expect(htmlToText('')).toBe('')
  })
})

describe('plural', () => {
  test.each([
    [0, 'service', undefined, '0 services'],
    [1, 'service', undefined, '1 service'],
    [2, 'unacknowledged alarm', undefined, '2 unacknowledged alarms'],
    [3, 'child', 'children', '3 children']
  ])('%i %s', (count, singular, pluralForm, expected) => {
    expect(plural(count, singular, pluralForm)).toBe(expected)
  })
})

describe('shallowEqual', () => {
  test('equal when the same keys hold equal primitives', () => {
    expect(shallowEqual({ a: 1, b: 'x', c: true, d: undefined }, { d: undefined, c: true, b: 'x', a: 1 })).toBe(true)
    expect(shallowEqual({ a: NaN }, { a: NaN })).toBe(true)
    expect(shallowEqual({}, {})).toBe(true)
  })

  test('not equal when a value differs', () => {
    expect(shallowEqual({ a: 1 }, { a: 2 })).toBe(false)
    expect(shallowEqual({ a: 1 }, { a: '1' })).toBe(false)
  })

  test('not equal when the keys differ, even when the extra key is undefined', () => {
    expect(shallowEqual({ a: 1 }, { a: 1, b: 2 })).toBe(false)
    expect(shallowEqual({ a: 1, b: undefined }, { a: 1 })).toBe(false)
    expect(shallowEqual({ a: undefined }, { b: undefined })).toBe(false)
  })

  test('compares nested objects by reference', () => {
    const nested = { x: 1 }

    expect(shallowEqual({ a: nested }, { a: nested })).toBe(true)
    expect(shallowEqual({ a: { x: 1 }}, { a: { x: 1 }})).toBe(false)
  })
})

describe('cloneState', () => {
  test('deep-copies plain data', () => {
    const date = new Date(0)
    const original = { name: 'pkg', services: [{ name: 'ICMP', params: [{ key: 'a', value: 1 }] }], when: date }
    const copy = cloneState(original)

    expect(copy).toEqual(original)
    expect(copy).not.toBe(original)
    expect(copy.services[0]).not.toBe(original.services[0])
    expect(copy.services[0].params[0]).not.toBe(original.services[0].params[0])
    expect(copy.when).not.toBe(date)
    expect(copy.when.getTime()).toBe(0)
  })

  test('copies reactive state, including proxies held inside plain arrays', () => {
    const state = reactive({ services: [{ name: 'ICMP' }, { name: 'HTTP' }] })
    // an array spread from a reactive array holds reactive proxies, and Vue stores it raw
    state.services = [...state.services, { name: 'SSH' }]

    expect(() => structuredClone(state)).toThrow()

    const copy = cloneState(state)

    expect(copy).toEqual({ services: [{ name: 'ICMP' }, { name: 'HTTP' }, { name: 'SSH' }] })
    copy.services[0].name = 'changed'
    expect(state.services[0].name).toBe('ICMP')
  })
})

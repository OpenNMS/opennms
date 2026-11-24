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
import { encodeFiqlPattern, encodeFiqlValue } from '@/lib/fiql'

describe('fiql', () => {
  describe('encodeFiqlValue', () => {
    test.each([
      ['comma', 'a,b', 'a%252Cb'],
      ['semicolon', 'a;b', 'a%253Bb'],
      ['parentheses', '(a)', '%2528a%2529'],
      ['URL characters', 'a&b#c+d e', 'a%2526b%2523c%252Bd%2520e'],
      ['percent', '50%', '50%2525'],
      ['characters encodeURIComponent leaves alone', '!\'*', '%2521%2527%252A'],
      ['plain text', 'node-1.example', 'node-1.example']
    ])('double-encodes %s', (_title, value, expected) => {
      expect(encodeFiqlValue(value)).toBe(expected)
    })

    test('decodes back to the original value after two decodes', () => {
      const value = 'ABC Systems., Inc. (R&D; #1+2*)'
      expect(decodeURIComponent(decodeURIComponent(encodeFiqlValue(value)))).toBe(value)
    })
  })

  describe('encodeFiqlPattern', () => {
    test('keeps * raw and double-encodes the segments between stars', () => {
      expect(encodeFiqlPattern('*a,b*c;d*')).toBe('*a%252Cb*c%253Bd*')
    })

    test('keeps leading, trailing and consecutive stars', () => {
      expect(encodeFiqlPattern('**a**')).toBe('**a**')
    })

    test('a value without stars is the same as encodeFiqlValue', () => {
      expect(encodeFiqlPattern('a,b (c)')).toBe(encodeFiqlValue('a,b (c)'))
    })

    test('an empty value stays empty', () => {
      expect(encodeFiqlPattern('')).toBe('')
    })
  })
})

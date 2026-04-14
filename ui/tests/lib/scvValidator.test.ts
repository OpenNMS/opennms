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

import { describe, it, expect } from 'vitest'
import { validateScvPattern } from '@/lib/scvValidator'

describe('validateScvPattern', () => {
  describe('valid expressions', () => {
    it('should accept a simple alphabetic key', () => {
      expect(validateScvPattern('${scv:key}')).toBe(true)
    })

    it('should accept a single letter key', () => {
      expect(validateScvPattern('${scv:a}')).toBe(true)
    })

    it('should accept a single digit key', () => {
      expect(validateScvPattern('${scv:1}')).toBe(true)
    })

    it('should accept a key starting with a digit', () => {
      expect(validateScvPattern('${scv:1key}')).toBe(true)
    })

    it('should accept a key with a dash in the middle', () => {
      expect(validateScvPattern('${scv:my-key}')).toBe(true)
    })

    it('should accept a key with multiple dashes', () => {
      expect(validateScvPattern('${scv:my-long-key-name}')).toBe(true)
    })

    it('should accept a key with digits and dashes', () => {
      expect(validateScvPattern('${scv:a1-b2}')).toBe(true)
    })

    it('should accept a fully numeric key', () => {
      expect(validateScvPattern('${scv:123}')).toBe(true)
    })

    it('should accept a mixed alphanumeric key', () => {
      expect(validateScvPattern('${scv:key123}')).toBe(true)
    })

    it('should accept two subkeys separated by a colon', () => {
      expect(validateScvPattern('${scv:key1:key2}')).toBe(true)
    })

    it('should accept two subkeys where both start with digits', () => {
      expect(validateScvPattern('${scv:1:2}')).toBe(true)
    })

    it('should accept two subkeys with dashes in each', () => {
      expect(validateScvPattern('${scv:my-key:sub-key}')).toBe(true)
    })

    it('should accept two subkeys with mixed alphanumeric keys', () => {
      expect(validateScvPattern('${scv:group1:item2}')).toBe(true)
    })

    it('should accept a single underscore key', () => {
      expect(validateScvPattern('${scv:_}')).toBe(true)
    })

    it('should accept a key starting with an underscore', () => {
      expect(validateScvPattern('${scv:_key}')).toBe(true)
    })

    it('should accept a key ending with an underscore', () => {
      expect(validateScvPattern('${scv:key_}')).toBe(true)
    })

    it('should accept a key with underscores and dashes', () => {
      expect(validateScvPattern('${scv:my_key-name}')).toBe(true)
    })

    it('should accept two subkeys with underscores', () => {
      expect(validateScvPattern('${scv:_alias_:_key_}')).toBe(true)
    })

    it('should accept a key with a dot in the middle', () => {
      expect(validateScvPattern('${scv:my.key}')).toBe(true)
    })

    it('should accept a key with dots and dashes', () => {
      expect(validateScvPattern('${scv:my.key-name}')).toBe(true)
    })

    it('should accept two subkeys each containing dots', () => {
      expect(validateScvPattern('${scv:group.name:item.key}')).toBe(true)
    })

    it('should accept a key with a default value', () => {
      expect(validateScvPattern('${scv:key|my-default}')).toBe(true)
    })

    it('should accept two subkeys with a default value', () => {
      expect(validateScvPattern('${scv:key:subkey|my-default}')).toBe(true)
    })

    it('should accept a default value with spaces', () => {
      expect(validateScvPattern('${scv:key|default value}')).toBe(true)
    })

    it('should accept a default value with special characters', () => {
      expect(validateScvPattern('${scv:key|p@$$w0rd!}')).toBe(true)
    })

    it('should accept a default value that is a single character', () => {
      expect(validateScvPattern('${scv:key|x}')).toBe(true)
    })
  })

  describe('invalid expressions', () => {
    it('should reject an empty key', () => {
      expect(validateScvPattern('${scv:}')).toBe(false)
    })

    it('should reject a key starting with a dash', () => {
      expect(validateScvPattern('${scv:-key}')).toBe(false)
    })

    it('should reject a key ending with a dash', () => {
      expect(validateScvPattern('${scv:key-}')).toBe(false)
    })

    it('should reject a key that is only a dash', () => {
      expect(validateScvPattern('${scv:-}')).toBe(false)
    })

    it('should reject a string missing the ${scv: prefix', () => {
      expect(validateScvPattern('key')).toBe(false)
    })

    it('should reject a string missing the closing brace', () => {
      expect(validateScvPattern('${scv:key')).toBe(false)
    })

    it('should reject an empty string', () => {
      expect(validateScvPattern('')).toBe(false)
    })

    it('should reject a key with a space', () => {
      expect(validateScvPattern('${scv:my key}')).toBe(false)
    })

    it('should reject a key with special characters', () => {
      expect(validateScvPattern('${scv:my@key}')).toBe(false)
    })

    it('should reject more than two subkeys', () => {
      expect(validateScvPattern('${scv:a:b:c}')).toBe(false)
    })

    it('should reject a trailing colon with no second subkey', () => {
      expect(validateScvPattern('${scv:key:}')).toBe(false)
    })

    it('should reject a second subkey starting with a dash', () => {
      expect(validateScvPattern('${scv:key:-sub}')).toBe(false)
    })

    it('should reject a second subkey ending with a dash', () => {
      expect(validateScvPattern('${scv:key:sub-}')).toBe(false)
    })

    it('should reject any extra curly braces', () => {
      expect(validateScvPattern('${scv:key:sub}}')).toBe(false)
    })

    it('should reject a pipe with no default value', () => {
      expect(validateScvPattern('${scv:key|}')).toBe(false)
    })

    it('should reject a default value containing a closing brace', () => {
      expect(validateScvPattern('${scv:key|val}extra}')).toBe(false)
    })

    it('should reject a default value containing a pipe character', () => {
      expect(validateScvPattern('${scv:key|val|extra}')).toBe(false)
    })
  })
})

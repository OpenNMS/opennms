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

import {
  highestSeverity,
  isProblemSeverity,
  PROBLEM_SEVERITIES,
  severityColorVar,
  severityId,
  severityKey,
  severityName,
  severityRank,
  severityTag,
  severityTintClass
} from '@/lib/severity'
import { describe, expect, it } from 'vitest'

describe('severity', () => {
  describe('severityKey', () => {
    it.each([
      ['CRITICAL', 'critical'],
      ['major', 'major'],
      ['Minor', 'minor'],
      ['WARNING', 'warning'],
      ['NORMAL', 'normal'],
      ['Indeterminate', 'indeterminate']
    ])('gives %s the look %s, in any case', (severity, key) => {
      expect(severityKey(severity)).toBe(key)
    })

    it.each(['CLEARED', 'BOGUS', '', null, undefined])('gives %s no look of its own', (severity) => {
      expect(severityKey(severity)).toBe('none')
    })
  })

  describe('severityName', () => {
    it.each([
      ['CRITICAL', 'Critical'],
      ['major', 'Major'],
      ['INDETERMINATE', 'Indeterminate'],
      ['Cleared', 'Cleared']
    ])('names %s %s', (severity, name) => {
      expect(severityName(severity)).toBe(name)
    })

    it.each(['BOGUS', '', null, undefined])('names %s Unknown', (severity) => {
      expect(severityName(severity)).toBe('Unknown')
    })
  })

  describe('severityId', () => {
    it.each([
      ['INDETERMINATE', 1],
      ['CLEARED', 2],
      ['NORMAL', 3],
      ['WARNING', 4],
      ['minor', 5],
      ['Major', 6],
      ['CRITICAL', 7]
    ])('gives %s the id %i', (severity, id) => {
      expect(severityId(severity)).toBe(id)
    })

    it.each(['BOGUS', '', null, undefined])('gives %s no id', (severity) => {
      expect(severityId(severity)).toBeUndefined()
    })
  })

  describe('severityRank', () => {
    it.each([
      ['indeterminate', 1],
      ['CLEARED', 2],
      ['Critical', 7],
      [null, 0],
      [undefined, 0],
      ['FOO', 0]
    ])('ranks %s as %i', (severity, rank) => {
      expect(severityRank(severity)).toBe(rank)
    })
  })

  describe('highestSeverity', () => {
    it('picks the highest, upper-case', () => {
      expect(highestSeverity(['normal', 'MAJOR', 'warning'])).toBe('MAJOR')
      expect(highestSeverity(['INDETERMINATE', 'CLEARED'])).toBe('CLEARED')
      expect(highestSeverity(['INDETERMINATE'])).toBe('INDETERMINATE')
      expect(highestSeverity(new Set(['minor', 'critical']))).toBe('CRITICAL')
    })

    it('is undefined when nothing is recognized', () => {
      expect(highestSeverity([])).toBeUndefined()
      expect(highestSeverity([null, 'FOO'])).toBeUndefined()
    })
  })

  describe('problem severities', () => {
    it('lists WARNING up to CRITICAL', () => {
      expect(PROBLEM_SEVERITIES).toEqual(['WARNING', 'MINOR', 'MAJOR', 'CRITICAL'])
    })

    it.each([['warning', true], ['NORMAL', false], ['INDETERMINATE', false], [undefined, false]])('%s -> %s', (severity, problem) => {
      expect(isProblemSeverity(severity)).toBe(problem)
    })
  })

  describe('severityTag', () => {
    it.each([
      ['CRITICAL', 'danger'],
      ['Major', 'danger'],
      ['minor', 'warn'],
      ['WARNING', 'warn'],
      ['NORMAL', 'success'],
      ['CLEARED', 'success'],
      ['INDETERMINATE', 'secondary'],
      ['FOO', 'secondary'],
      [undefined, 'secondary']
    ])('%s -> %s', (severity, tag) => {
      expect(severityTag(severity)).toBe(tag)
    })
  })

  describe('severityColorVar', () => {
    it('names the CSS custom property', () => {
      expect(severityColorVar('Critical')).toBe('--onms-critical')
      expect(severityColorVar('normal')).toBe('--onms-normal')
      expect(severityColorVar('INDETERMINATE')).toBe('--onms-indeterminate')
      expect(severityColorVar('FOO')).toBe('--onms-cleared')
      expect(severityColorVar(null)).toBe('--onms-cleared')
    })
  })

  describe('severityTintClass', () => {
    it('names the tint class', () => {
      expect(severityTintClass('MAJOR')).toBe('severity-tint--major')
      expect(severityTintClass('FOO')).toBeUndefined()
    })
  })
})

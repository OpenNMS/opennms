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

import { describe, expect, it } from 'vitest'
import {
  cardProblems,
  monthProblem,
  formatSpecific,
  newCard,
  parseSpecific,
  toCard,
  toSchedule
} from '@/lib/onCallSchedule'
import { OnCallSchedule } from '@/types/onCallRoleAdmin'

const en = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
const de = ['Jan.', 'Feb.', 'März', 'Apr.', 'Mai', 'Juni', 'Juli', 'Aug.', 'Sept.', 'Okt.', 'Nov.', 'Dez.']

const daily = (begins: string, ends: string): OnCallSchedule =>
  ({ user: 'admin', type: 'daily', times: [{ id: '1', day: null, begins, ends }] })

describe('round trip', () => {
  const stored: OnCallSchedule[] = [
    { user: 'a', type: 'specific', times: [{ id: '1', day: null, begins: '14-Mar-2026 18:00:00', ends: '15-Mar-2026 06:30:00' }] },
    daily('08:00:00', '17:00:00'),
    { user: 'a', type: 'weekly', times: [{ id: '2', day: 'monday', begins: '09:00:00', ends: '12:00:00' }] },
    { user: 'a', type: 'monthly', times: [{ id: '3', day: '15', begins: '00:00:00', ends: '23:59:59' }] }
  ]

  it.each(stored.map(s => [s.type, s] as const))('keeps a stored %s schedule byte-identical', (_t, s) => {
    const card = toCard(s, en)
    expect(card.legacy).toBe(false)
    expect(card.dirty).toBe(false)
    expect(card.addedThisSession).toBe(false)
    expect(toSchedule(card, en)).toEqual(s)
  })

  it('reserializes an untouched time of a dirty card unchanged', () => {
    const s = daily('08:00:00', '17:00:00')
    const card = toCard(s, en)
    card.dirty = true
    expect(toSchedule(card, en)).toEqual(s)
  })

  it('formats an edited time and preserves its id', () => {
    const s: OnCallSchedule = {
      user: 'a', type: 'weekly',
      times: [
        { id: '1', day: 'monday', begins: '08:00:00', ends: '12:00:00' },
        { id: '2', day: 'tuesday', begins: '08:00:00', ends: '12:00:00' }
      ]
    }
    const card = toCard(s, en)
    card.times[1].end = '13:15'
    card.dirty = true
    expect(toSchedule(card, en).times).toEqual([
      s.times[0],
      { id: '2', day: 'tuesday', begins: '08:00:00', ends: '13:15:00' }
    ])
  })

  it('drops the day when a weekly card becomes daily', () => {
    const card = toCard({ user: 'a', type: 'weekly', times: [{ id: '2', day: 'monday', begins: '09:00:00', ends: '12:00:00' }] }, en)
    card.type = 'daily'
    card.dirty = true
    expect(toSchedule(card, en).times).toEqual([{ id: '2', day: null, begins: '09:00:00', ends: '12:00:00' }])
  })

  it('serializes end of day as 23:59:59', () => {
    const card = toCard(daily('08:00:00', '17:00:00'), en)
    card.times[0].endOfDay = true
    card.dirty = true
    expect(toSchedule(card, en).times[0].ends).toBe('23:59:59')
  })
})

describe('seconds handling', () => {
  it('drops :00 seconds', () => {
    const card = toCard(daily('08:00:00', '17:00:00'), en)
    expect(card.times[0]).toMatchObject({ begin: '08:00', end: '17:00', endOfDay: false })
  })

  it('maps 23:59:59 to end of day', () => {
    const card = toCard(daily('00:00:00', '23:59:59'), en)
    expect(card.legacy).toBe(false)
    expect(card.times[0]).toMatchObject({ begin: '00:00', endOfDay: true })
  })

  it('maps a specific end of 23:59:59 to end of day', () => {
    const s: OnCallSchedule = { user: 'a', type: 'specific', times: [{ begins: '14-Mar-2026 08:00:00', ends: '14-Mar-2026 23:59:59' }] }
    const card = toCard(s, en)
    expect(card.legacy).toBe(false)
    expect(card.times[0]).toMatchObject({ begin: '2026-03-14T08:00', end: '2026-03-14T23:59', endOfDay: true })
    card.dirty = true
    card.times[0].begin = '2026-03-14T09:00'
    expect(toSchedule(card, en).times[0]).toMatchObject({ begins: '14-Mar-2026 09:00:00', ends: '14-Mar-2026 23:59:59' })
  })

  it('treats non-zero begin seconds as legacy', () => {
    expect(toCard(daily('08:00:30', '17:00:00'), en).legacy).toBe(true)
  })

  it('treats non-zero end seconds other than 23:59:59 as legacy', () => {
    expect(toCard(daily('08:00:00', '17:00:30'), en).legacy).toBe(true)
    expect(toCard(daily('08:00:00', '17:59:59'), en).legacy).toBe(true)
  })
})

describe('legacy detection', () => {
  const legacy = (s: OnCallSchedule) => toCard(s, en).legacy

  it('flags an unknown type', () => {
    expect(legacy({ user: 'a', type: 'yearly', times: [] })).toBe(true)
  })

  it('flags times that do not fit their type', () => {
    expect(legacy({ user: 'a', type: 'daily', times: [{ day: 'monday', begins: '08:00:00', ends: '17:00:00' }] })).toBe(true)
    expect(legacy({ user: 'a', type: 'daily', times: [{ begins: '08:00', ends: '17:00' }] })).toBe(true)
    expect(legacy({ user: 'a', type: 'specific', times: [{ begins: '08:00:00', ends: '17:00:00' }] })).toBe(true)
    expect(legacy({ user: 'a', type: 'specific', times: [{ day: 'monday', begins: '14-Mar-2026 08:00:00', ends: '14-Mar-2026 09:00:00' }] })).toBe(true)
    expect(legacy({ user: 'a', type: 'weekly', times: [{ begins: '08:00:00', ends: '17:00:00' }] })).toBe(true)
    expect(legacy({ user: 'a', type: 'weekly', times: [{ day: 'Monday', begins: '08:00:00', ends: '17:00:00' }] })).toBe(true)
    expect(legacy({ user: 'a', type: 'monthly', times: [{ day: '32', begins: '08:00:00', ends: '17:00:00' }] })).toBe(true)
    expect(legacy({ user: 'a', type: 'monthly', times: [{ day: '0', begins: '08:00:00', ends: '17:00:00' }] })).toBe(true)
    expect(legacy({ user: 'a', type: 'monthly', times: [{ day: 'x', begins: '08:00:00', ends: '17:00:00' }] })).toBe(true)
  })

  it('flags begins at or after ends', () => {
    expect(legacy(daily('17:00:00', '08:00:00'))).toBe(true)
    expect(legacy(daily('08:00:00', '08:00:00'))).toBe(true)
    expect(legacy({ user: 'a', type: 'specific', times: [{ begins: '15-Mar-2026 08:00:00', ends: '14-Mar-2026 09:00:00' }] })).toBe(true)
  })

  it('flags an unknown month name', () => {
    expect(legacy({ user: 'a', type: 'specific', times: [{ begins: '14-Xyz-2026 08:00:00', ends: '14-Mar-2026 09:00:00' }] })).toBe(true)
  })

  it('returns the stored schedule untouched for a legacy card', () => {
    const s = daily('08:00:30', '17:00:00')
    const card = toCard(s, en)
    card.dirty = true
    expect(toSchedule(card, en)).toEqual(s)
    expect(cardProblems(card, en)).toEqual([])
  })
})

describe('specific dates', () => {
  it('formats with the server locale months', () => {
    expect(formatSpecific('2026-03-14T18:00', de)).toBe('14-März-2026 18:00:00')
    expect(formatSpecific('2026-03-04T18:00:05', en)).toBe('04-Mar-2026 18:00:05')
  })

  it('parses them back', () => {
    expect(parseSpecific('14-März-2026 18:00:00', de)).toBe('2026-03-14T18:00:00')
    expect(parseSpecific('14-mar-2026 18:00:00', en)).toBe('2026-03-14T18:00:00')
  })

  it('refuses to format an unparseable value', () => {
    expect(() => formatSpecific('08:00', en)).toThrow()
    expect(() => formatSpecific('2026-13-01T08:00', en)).toThrow()
  })

  it('rejects malformed or impossible values', () => {
    expect(parseSpecific('14-Mar-2026 18:00', en)).toBeNull()
    expect(parseSpecific('31-Feb-2026 18:00:00', en)).toBeNull()
    expect(parseSpecific('14-Mar-2026 25:00:00', en)).toBeNull()
    expect(parseSpecific('nonsense', en)).toBeNull()
  })

  it('round trips a German stored schedule', () => {
    const s: OnCallSchedule = { user: 'a', type: 'specific', times: [{ begins: '14-März-2026 18:00:00', ends: '15-März-2026 06:00:00' }] }
    const card = toCard(s, de)
    expect(card.legacy).toBe(false)
    expect(card.times[0]).toMatchObject({ begin: '2026-03-14T18:00', end: '2026-03-15T06:00' })
    card.dirty = true
    card.times[0].end = '2026-03-15T07:00'
    expect(toSchedule(card, de).times[0].ends).toBe('15-März-2026 07:00:00')
  })
})

describe('specific months notifd cannot read', () => {
  const specific = (begin: string, end: string, original: OnCallSchedule | null = null) => {
    const c = original ? toCard(original, de) : newCard('admin', 'specific')
    c.times = original ? c.times : [{ id: null, day: null, begin, end, endOfDay: false, original: null }]
    return c
  }
  const MARCH = 'This server\'s locale writes March as \'März\', which notifd cannot read; run OpenNMS with an English locale to use one-time entries in this month.'

  it('flags a month whose abbreviation is not 3 characters', () => {
    expect(cardProblems(specific('2026-03-14T10:00', '2026-03-14T11:00'), de)).toEqual([MARCH])
    expect(cardProblems(specific('2026-02-14T10:00', '2026-02-14T11:00'), de)).toEqual([
      'This server\'s locale writes February as \'Feb.\', which notifd cannot read; run OpenNMS with an English locale to use one-time entries in this month.'
    ])
  })

  it('flags a range that ends in such a month', () => {
    expect(cardProblems(specific('2026-02-28T10:00', '2026-03-01T11:00'), ['Jan', 'Feb', 'März', ...en.slice(3)])).toEqual([MARCH])
  })

  it('accepts a locale whose months are all 3 letters', () => {
    expect(cardProblems(specific('2026-03-14T10:00', '2026-03-14T11:00'), en)).toEqual([])
  })

  it('does not flag a stored time that was not edited', () => {
    const s: OnCallSchedule = { user: 'a', type: 'specific', times: [{ begins: '14-März-2026 18:00:00', ends: '15-März-2026 06:00:00' }] }
    const card = toCard(s, de)
    expect(cardProblems(card, de)).toEqual([])
    card.times[0].end = '2026-03-15T07:00'
    expect(cardProblems(card, de)).toEqual([MARCH])
  })

  it('only applies to specific schedules', () => {
    const c = newCard('admin', 'daily')
    c.times.push({ id: null, day: null, begin: '08:00', end: '17:00', endOfDay: false, original: null })
    expect(cardProblems(c, de)).toEqual([])
  })

  it('reports the same problem for a single date', () => {
    expect(monthProblem(['2026-03-14T10:00'], de)).toBe(MARCH)
    expect(monthProblem(['2026-03-14', '2026-03-15T00:00'], en)).toBeNull()
  })
})

describe('newCard', () => {
  it('starts dirty with no times', () => {
    const card = newCard('admin', 'weekly')
    expect(card).toMatchObject({ user: 'admin', type: 'weekly', times: [], legacy: false, original: null, dirty: true, addedThisSession: false })
    expect(newCard('admin', 'weekly').key).not.toBe(card.key)
  })
})

describe('cardProblems', () => {
  const valid = () => {
    const c = newCard('admin', 'daily')
    c.times.push({ id: null, day: null, begin: '08:00', end: '17:00', endOfDay: false, original: null })
    return c
  }

  it('accepts a valid card', () => {
    expect(cardProblems(valid(), en)).toEqual([])
  })

  it('requires a user', () => {
    const c = valid()
    c.user = ''
    expect(cardProblems(c, en)).toEqual(['Choose a user.'])
  })

  it('requires a time', () => {
    expect(cardProblems(newCard('admin', 'daily'), en)).toEqual(['Add at least one time range.'])
  })

  it('hints at two ranges for a shift past midnight', () => {
    const c = valid()
    c.times[0].begin = '22:00'
    c.times[0].end = '06:00'
    const problems = cardProblems(c, en)
    expect(problems).toHaveLength(1)
    expect(problems[0]).toContain('For a shift past midnight, add two time ranges')
    expect(problems[0]).toContain('22:00 to End of day, and 00:00 to 06:00')
    expect(problems[0]).not.toContain('23:59')
  })

  it('accepts end of day regardless of the end input', () => {
    const c = valid()
    c.times[0].begin = '22:00'
    c.times[0].end = '06:00'
    c.times[0].endOfDay = true
    expect(cardProblems(c, en)).toEqual([])
  })

  it('reports begin at or after end for a specific time without the hint', () => {
    const c = newCard('admin', 'specific')
    c.times.push({ id: null, day: null, begin: '2026-03-14T10:00', end: '2026-03-14T09:00', endOfDay: false, original: null })
    const problems = cardProblems(c, en)
    expect(problems).toHaveLength(1)
    expect(problems[0]).not.toContain('past midnight')
  })

  it('requires a weekday for weekly', () => {
    const c = valid()
    c.type = 'weekly'
    expect(cardProblems(c, en)).toEqual(['Choose a weekday for each time range.'])
    c.times[0].day = 'monday'
    expect(cardProblems(c, en)).toEqual([])
  })

  it('rejects monthly day 32', () => {
    const c = valid()
    c.type = 'monthly'
    c.times[0].day = '32'
    expect(cardProblems(c, en)).toEqual(['The day of the month must be between 1 and 31.'])
    c.times[0].day = '31'
    expect(cardProblems(c, en)).toEqual([])
  })

  it('requires begin and end values', () => {
    const c = valid()
    c.times[0].begin = ''
    expect(cardProblems(c, en)).toEqual(['Enter a begin and an end for each time range.'])
  })

  it('requires an end date for a specific time even with end of day', () => {
    const c = newCard('admin', 'specific')
    c.times.push({ id: null, day: null, begin: '2026-03-14T10:00', end: '', endOfDay: true, original: null })
    expect(cardProblems(c, en)).toEqual(['Enter a begin and an end for each time range.'])
  })

  it('rejects values in the wrong format for the type', () => {
    const c = newCard('admin', 'specific')
    c.times.push({ id: null, day: null, begin: '08:00', end: '17:00', endOfDay: false, original: null })
    expect(cardProblems(c, en)).toEqual(['Enter a begin and an end for each time range.'])
    const d = valid()
    d.times[0].begin = '2026-03-14T10:00'
    expect(cardProblems(d, en)).toEqual(['Enter a begin and an end for each time range.'])
  })
})

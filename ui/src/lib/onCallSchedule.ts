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

// Converts stored on-call schedules (server strings) to editable cards and back. Anything the
// user did not edit is returned byte-identical: the server only grandfathers a stored schedule
// that comes back unchanged.

import { OnCallSchedule, OnCallScheduleType, OnCallTime } from '@/types/onCallRoleAdmin'

export const WEEKDAYS = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'] as const
export const END_OF_DAY = '23:59:59'

const TYPES: string[] = ['specific', 'daily', 'weekly', 'monthly']
const CLOCK = /^([01]\d|2[0-3]):([0-5]\d):([0-5]\d)$/
const SPECIFIC = /^(\d{2})-(.+?)-(\d{4}) (\d{2}):(\d{2}):(\d{2})$/
const ISO_LOCAL = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?$/
// the second half of a shift past midnight falls on the next day, which a weekly or monthly range must name
const OVERNIGHT_HINTS: Record<string, string> = {
  daily: 'For a shift past midnight, add two time ranges (for example 22:00 to End of day, and 00:00 to 06:00).',
  weekly: 'For a shift past midnight, add two time ranges, the second on the next weekday (for example Monday 22:00 to End of day, and Tuesday 00:00 to 06:00).',
  monthly: 'For a shift past midnight, add two time ranges, the second on the next day of the month (for example day 5 22:00 to End of day, and day 6 00:00 to 06:00).'
}

export interface TimeRange {
  id: string | null
  day: string | null // weekday or '1'..'31'
  begin: string // 'HH:mm', or 'yyyy-MM-ddTHH:mm' for specific
  end: string
  endOfDay: boolean
  original: OnCallTime | null
}

export interface ScheduleCard {
  key: string
  user: string
  type: OnCallScheduleType
  times: TimeRange[]
  legacy: boolean
  original: OnCallSchedule | null
  dirty: boolean
  addedThisSession: boolean
}

let keyCounter = 0
const nextKey = () => `schedule-${++keyCounter}`

const isType = (t: string): t is OnCallScheduleType => TYPES.includes(t)
const pad = (n: number) => String(n).padStart(2, '0')

export function formatSpecific(isoLocal: string, shortMonths: string[]): string {
  const m = ISO_LOCAL.exec(isoLocal)
  const month = m ? shortMonths[Number(m[2]) - 1] : undefined
  if (!m || month === undefined) {
    throw new Error(`Cannot format '${isoLocal}' as a specific date`)
  }
  return `${m[3]}-${month}-${m[1]} ${m[4]}:${m[5]}:${m[6] ?? '00'}`
}

export function parseSpecific(s: string, shortMonths: string[]): string | null {
  const m = SPECIFIC.exec(s)
  if (!m) {
    return null
  }
  const monthIndex = shortMonths.findIndex(name => name.toLowerCase() === m[2].toLowerCase())
  if (monthIndex < 0) {
    return null
  }
  const [year, day, h, min, sec] = [Number(m[3]), Number(m[1]), Number(m[4]), Number(m[5]), Number(m[6])]
  const date = new Date(Date.UTC(year, monthIndex, day))
  if (date.getUTCMonth() !== monthIndex || date.getUTCDate() !== day || h > 23 || min > 59 || sec > 59) {
    return null
  }
  return `${m[3]}-${pad(monthIndex + 1)}-${m[1]}T${m[4]}:${m[5]}:${m[6]}`
}

// The editable form of one stored time, or null when it can't be represented without loss.
function toRange(t: OnCallTime, type: OnCallScheduleType, shortMonths: string[]): TimeRange | null {
  const hasDay = t.day !== null && t.day !== undefined
  let begin: string
  let end: string
  let endOfDay = false

  if (type === 'specific') {
    const b = parseSpecific(t.begins, shortMonths)
    const e = parseSpecific(t.ends, shortMonths)
    if (hasDay || !b || !e || b.slice(17) !== '00' || b >= e) {
      return null
    }
    if (e.slice(11) === END_OF_DAY) {
      endOfDay = true
    } else if (e.slice(17) !== '00') {
      return null
    }
    begin = b.slice(0, 16)
    end = endOfDay ? `${e.slice(0, 10)}T23:59` : e.slice(0, 16)
  } else {
    if (!dayFits(type, t.day)) {
      return null
    }
    const b = CLOCK.exec(t.begins)
    const e = CLOCK.exec(t.ends)
    if (!b || !e || b[3] !== '00' || t.begins >= t.ends) {
      return null
    }
    if (t.ends === END_OF_DAY) {
      endOfDay = true
    } else if (e[3] !== '00') {
      return null
    }
    begin = t.begins.slice(0, 5)
    end = t.ends.slice(0, 5)
  }
  return { id: t.id ?? null, day: hasDay ? t.day as string : null, begin, end, endOfDay, original: t }
}

function isMonthDay(day: string | null | undefined): boolean {
  return day !== null && day !== undefined && /^[0-9]{1,2}$/.test(day) && Number(day) >= 1 && Number(day) <= 31
}

// Whether a time's day suits its schedule type: a weekday for weekly, a day of the month for monthly, none otherwise.
function dayFits(type: string, day: string | null | undefined): boolean {
  if (type === 'weekly') {
    return WEEKDAYS.includes(day as typeof WEEKDAYS[number])
  }
  if (type === 'monthly') {
    return isMonthDay(day)
  }
  return day === null || day === undefined
}

// Whether a time is still exactly as stored: re-derives the card entry from the stored strings and compares it with
// the current one, so an edit that was undone counts as untouched too.
function sameAsOriginal(r: TimeRange, type: OnCallScheduleType, shortMonths: string[]): boolean {
  if (!r.original) {
    return false
  }
  const fresh = toRange(r.original, type, shortMonths)
  return !!fresh && fresh.begin === r.begin && fresh.end === r.end && fresh.endOfDay === r.endOfDay && fresh.day === r.day
}

export function toCard(s: OnCallSchedule, shortMonths: string[]): ScheduleCard {
  const base = { key: nextKey(), user: s.user, original: s, dirty: false, addedThisSession: false }
  if (!isType(s.type)) {
    return {
      ...base,
      type: s.type as OnCallScheduleType,
      times: s.times.map(t => rawRange(t)),
      legacy: true
    }
  }
  const ranges = s.times.map(t => toRange(t, s.type as OnCallScheduleType, shortMonths))
  if (ranges.some(r => r === null)) {
    return { ...base, type: s.type, times: s.times.map(t => rawRange(t)), legacy: true }
  }
  return { ...base, type: s.type, times: ranges as TimeRange[], legacy: false }
}

// A time shown as stored, for cards that can't be edited.
function rawRange(t: OnCallTime): TimeRange {
  return { id: t.id ?? null, day: t.day ?? null, begin: t.begins, end: t.ends, endOfDay: false, original: t }
}

export function newCard(user: string, type: OnCallScheduleType): ScheduleCard {
  return { key: nextKey(), user, type, times: [], legacy: false, original: null, dirty: true, addedThisSession: false }
}

export function toSchedule(c: ScheduleCard, shortMonths: string[]): OnCallSchedule {
  if (c.original && (!c.dirty || c.legacy)) {
    return c.original
  }
  const times = c.times.map((r): OnCallTime => {
    if (r.original && sameAsOriginal(r, c.type, shortMonths)) {
      return r.original
    }
    const hasDay = c.type === 'weekly' || c.type === 'monthly'
    let begins: string
    let ends: string
    if (c.type === 'specific') {
      begins = formatSpecific(r.begin, shortMonths)
      ends = formatSpecific(r.endOfDay ? `${r.end.slice(0, 10)}T${END_OF_DAY}` : r.end, shortMonths)
    } else {
      begins = `${r.begin}:00`
      ends = r.endOfDay ? END_OF_DAY : `${r.end}:00`
    }
    return { id: r.id, day: hasDay ? r.day : null, begins, ends }
  })
  return { user: c.user, type: c.type, times }
}

// an editor value that is a real clock time ('HH:mm') or a real day and time ('yyyy-MM-ddTHH:mm'), not just the shape
const isClock = (v: string): boolean => {
  const m = /^(\d{2}):(\d{2})$/.exec(v)
  return !!m && Number(m[1]) <= 23 && Number(m[2]) <= 59
}

const isDayTime = (v: string): boolean => {
  const m = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}:\d{2})$/.exec(v)
  if (!m || !isClock(m[4])) {
    return false
  }
  const date = new Date(Date.UTC(Number(m[1]), Number(m[2]) - 1, Number(m[3])))
  return date.getUTCMonth() === Number(m[2]) - 1 && date.getUTCDate() === Number(m[3])
}

export function cardProblems(c: ScheduleCard): string[] {
  if (c.legacy) {
    return []
  }
  const problems: string[] = []
  if (!c.user.trim()) {
    problems.push('Choose a user.')
  }
  if (c.times.length === 0) {
    problems.push('Add at least one time range.')
  }
  const seen = new Set<string>()
  const add = (p: string) => {
    if (!seen.has(p)) {
      seen.add(p)
      problems.push(p)
    }
  }
  for (const r of c.times) {
    if (c.type === 'weekly' && !dayFits(c.type, r.day)) {
      add('Choose a weekday for each time range.')
    }
    if (c.type === 'monthly' && !dayFits(c.type, r.day)) {
      add('The day of the month must be between 1 and 31.')
    }
    const valid = c.type === 'specific' ? isDayTime : isClock
    const endOk = valid(r.end) || (r.endOfDay && c.type !== 'specific')
    if (!valid(r.begin) || !endOk) {
      add('Enter a begin and an end for each time range.')
      continue
    }
    const effectiveEnd = r.endOfDay
      ? (c.type === 'specific' ? `${r.end.slice(0, 10)}T${END_OF_DAY}` : END_OF_DAY)
      : `${r.end}:00`
    if (`${r.begin}:00` >= effectiveEnd) {
      add(c.type === 'specific'
        ? 'Each time range must begin before it ends.'
        : `Each time range must begin before it ends. ${OVERNIGHT_HINTS[c.type]}`)
    }
  }
  return problems
}

import { differenceInCalendarDays, differenceInCalendarMonths, sub } from 'date-fns'
import { describe, expect, it } from 'vitest'

import {
  DEFAULT_RANGE,
  relativeRangeOf,
  resolveRelativeRange,
  TIME_RANGE_OPTIONS
} from '@/components/Common/utils/timeRangeOptions'

// A date well away from a DST boundary or month end, so the arithmetic below is
// unambiguous.
const NOW = new Date('2026-08-10T12:00:00Z')

const hoursBack = (duration: Parameters<typeof sub>[1]) =>
  (NOW.getTime() - sub(NOW, duration).getTime()) / 3_600_000

const optionFor = (label: string) => {
  const option = TIME_RANGE_OPTIONS.find(entry => entry.label === label)
  expect(option, `no option labeled ${label}`).toBeDefined()
  return option!
}

/**
 * The regression these guard: date-fns sums duration fields before applying them
 * (`minutes + hours * 60`, `days + weeks * 7`, `months + years * 12`). A string
 * first operand turns that `+` into concatenation, so `{ minutes: '60' }` went
 * back 600 minutes and `{ days: '7' }` went back 70 days. Seven of the twelve
 * ranges were wrong; only the hours-only ones survived, because multiplication
 * coerces where addition does not.
 */
describe('TIME_RANGE_OPTIONS', () => {
  it('uses numeric duration values throughout', () => {
    for (const option of TIME_RANGE_OPTIONS) {
      for (const [field, value] of Object.entries(option.time)) {
        expect(typeof value, `${option.label}.${field} must be a number`).toBe('number')
      }
    }
  })

  it.each([
    ['Last hour', 1],
    ['Last 2 hours', 2],
    ['Last 4 hours', 4],
    ['Last 8 hours', 8],
    ['Last 12 hours', 12],
    ['Last day', 24],
    ['Last two days', 48]
  ])('%s goes back exactly %i hours', (label, expected) => {
    expect(hoursBack(optionFor(label).time)).toBe(expected)
  })

  it('Last week goes back exactly 7 days', () => {
    expect(differenceInCalendarDays(NOW, sub(NOW, optionFor('Last week').time))).toBe(7)
  })

  it.each([
    ['Last month', 1],
    ['Last three months', 3],
    ['Last six months', 6]
  ])('%s goes back exactly %i months', (label, expected) => {
    expect(differenceInCalendarMonths(NOW, sub(NOW, optionFor(label).time))).toBe(expected)
  })

  it('Last year goes back exactly 12 months', () => {
    expect(differenceInCalendarMonths(NOW, sub(NOW, optionFor('Last year').time))).toBe(12)
  })

  it('is ordered shortest range first, with no duplicate labels', () => {
    const spans = TIME_RANGE_OPTIONS.map(option => hoursBack(option.time))
    const sorted = [...spans].sort((a, b) => a - b)

    expect(spans).toEqual(sorted)
    expect(new Set(TIME_RANGE_OPTIONS.map(option => option.label)).size).toBe(TIME_RANGE_OPTIONS.length)
  })

  // The emitted `format` is Object.keys(time)[0], which drives the x-axis label
  // granularity, so every option needs exactly one field.
  it('carries exactly one duration field per option', () => {
    for (const option of TIME_RANGE_OPTIONS) {
      expect(Object.keys(option.time), option.label).toHaveLength(1)
    }
  })
})

describe('relativeRangeOf', () => {
  it('reduces every preset to a single unit and amount', () => {
    for (const option of TIME_RANGE_OPTIONS) {
      const range = relativeRangeOf(option)
      expect(range, option.label).not.toBeNull()
      expect(range!.amount).toBeGreaterThan(0)
    }
  })

  it('reads the unit and amount the label promises', () => {
    const byLabel = (label: string) => relativeRangeOf(TIME_RANGE_OPTIONS.find(o => o.label === label)!)

    expect(byLabel('Last hour')).toEqual({ unit: 'hours', amount: 1 })
    expect(byLabel('Last two days')).toEqual({ unit: 'hours', amount: 48 })
    expect(byLabel('Last week')).toEqual({ unit: 'days', amount: 7 })
    expect(byLabel('Last six months')).toEqual({ unit: 'months', amount: 6 })
  })

  it('refuses a duration that is not one clean unit', () => {
    expect(relativeRangeOf({ label: 'mixed', time: { hours: 1, minutes: 30 }})).toBeNull()
    expect(relativeRangeOf({ label: 'empty', time: {}})).toBeNull()
  })
})

describe('resolveRelativeRange', () => {
  it('anchors the window to the clock it is given', () => {
    const resolved = resolveRelativeRange({ unit: 'hours', amount: 2 }, NOW)

    expect(resolved.endTime).toBe(Math.floor(NOW.getTime() / 1000))
    expect(Number(resolved.endTime) - Number(resolved.startTime)).toBe(7200)
  })

  // The point of the whole exercise: the same range resolved later must move.
  it('slides forward when resolved against a later clock', () => {
    const range = { unit: 'hours', amount: 24 } as const
    const earlier = resolveRelativeRange(range, NOW)
    const later = resolveRelativeRange(range, new Date(NOW.getTime() + 3_600_000))

    expect(Number(later.endTime) - Number(earlier.endTime)).toBe(3600)
    expect(Number(later.startTime) - Number(earlier.startTime)).toBe(3600)
    // Same width, different position.
    expect(Number(later.endTime) - Number(later.startTime))
      .toBe(Number(earlier.endTime) - Number(earlier.startTime))
  })

  it('carries the range along so it can be resolved again', () => {
    expect(resolveRelativeRange(DEFAULT_RANGE, NOW).range).toEqual(DEFAULT_RANGE)
  })

  it('uses the unit as the axis-label granularity', () => {
    expect(resolveRelativeRange({ unit: 'days', amount: 7 }, NOW).format).toBe('days')
  })
})

import { differenceInCalendarDays, differenceInCalendarMonths, sub } from 'date-fns'
import { describe, expect, it } from 'vitest'

import { HOUR_OPTIONS, TIME_RANGE_OPTIONS } from '@/components/Resources/utils/timeRangeOptions'

// A date well away from a DST boundary or month end, so the arithmetic below is
// unambiguous.
const NOW = new Date('2026-08-10T12:00:00Z')

const hoursBack = (duration: Parameters<typeof sub>[1]) =>
  (NOW.getTime() - sub(NOW, duration).getTime()) / 3_600_000

const optionFor = (label: string) => {
  const option = TIME_RANGE_OPTIONS.find(entry => entry.label === label)
  expect(option, `no option labelled ${label}`).toBeDefined()
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

describe('HOUR_OPTIONS', () => {
  it('covers all 24 hours of the day in order', () => {
    expect(HOUR_OPTIONS).toHaveLength(24)
    expect(HOUR_OPTIONS.map(option => option.time.hours)).toEqual(
      Array.from({ length: 24 }, (_unused, hour) => hour)
    )
  })

  it('labels midnight and noon as 12 AM and 12 PM', () => {
    expect(HOUR_OPTIONS[0].label).toBe('12 AM')
    expect(HOUR_OPTIONS[12].label).toBe('12 PM')
  })

  it('uses numeric hours, so add() cannot concatenate them', () => {
    for (const option of HOUR_OPTIONS) {
      expect(typeof option.time.hours, option.label).toBe('number')
    }
  })
})

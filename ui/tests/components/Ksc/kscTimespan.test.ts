import { describe, it, expect } from 'vitest'
import { add, getUnixTime, startOfDay, startOfWeek, sub } from 'date-fns'
import { kscTimespanToStartEndTime, KSC_TIMESPAN_OPTIONS } from '@/components/Ksc/utils/kscTimespan'

// Wednesday, 15 July 2026, 13:30 local time — a fixed reference so every
// calendar-relative case is deterministic.
const NOW = new Date(2026, 6, 15, 13, 30, 0)

describe('kscTimespanToStartEndTime', () => {
  it('exposes all 25 legacy timespan options in order', () => {
    expect(KSC_TIMESPAN_OPTIONS).toHaveLength(25)
    expect(KSC_TIMESPAN_OPTIONS[0]).toBe('1_hour')
    expect(KSC_TIMESPAN_OPTIONS[KSC_TIMESPAN_OPTIONS.length - 1]).toBe('Last Year')
  })

  it('maps relative spans to now minus the offset', () => {
    const r = kscTimespanToStartEndTime('7_day', NOW)
    expect(r.endTime).toBe(getUnixTime(NOW))
    expect(r.startTime).toBe(getUnixTime(sub(NOW, { days: 7 })))
    // 7 days is between 48h and 60d -> day-granularity labels
    expect(r.format).toBe('days')
  })

  it('uses minute labels for a one-hour span and hour labels for a day', () => {
    expect(kscTimespanToStartEndTime('1_hour', NOW).format).toBe('minutes')
    expect(kscTimespanToStartEndTime('Today', NOW).format).toBe('hours')
  })

  it('Today spans midnight to next midnight', () => {
    const r = kscTimespanToStartEndTime('Today', NOW)
    expect(r.startTime).toBe(getUnixTime(startOfDay(NOW)))
    expect(r.endTime).toBe(getUnixTime(add(startOfDay(NOW), { days: 1 })))
  })

  it('Yesterday spans the previous calendar day', () => {
    const r = kscTimespanToStartEndTime('Yesterday', NOW)
    expect(r.startTime).toBe(getUnixTime(sub(startOfDay(NOW), { days: 1 })))
    expect(r.endTime).toBe(getUnixTime(startOfDay(NOW)))
  })

  it('This Month runs from the 1st to the 1st of next month', () => {
    const r = kscTimespanToStartEndTime('This Month', NOW)
    expect(r.startTime).toBe(getUnixTime(new Date(2026, 6, 1)))
    expect(r.endTime).toBe(getUnixTime(new Date(2026, 7, 1)))
  })

  it('Last Month runs from the 1st of last month to the 1st of this month', () => {
    const r = kscTimespanToStartEndTime('Last Month', NOW)
    expect(r.startTime).toBe(getUnixTime(new Date(2026, 5, 1)))
    expect(r.endTime).toBe(getUnixTime(new Date(2026, 6, 1)))
  })

  it('This Quarter (Q3 for July) runs Jul 1 to Oct 1', () => {
    const r = kscTimespanToStartEndTime('This Quarter', NOW)
    expect(r.startTime).toBe(getUnixTime(new Date(2026, 6, 1)))
    expect(r.endTime).toBe(getUnixTime(new Date(2026, 9, 1)))
  })

  it('Last Quarter (from Q3) runs Apr 1 to Jul 1', () => {
    const r = kscTimespanToStartEndTime('Last Quarter', NOW)
    expect(r.startTime).toBe(getUnixTime(new Date(2026, 3, 1)))
    expect(r.endTime).toBe(getUnixTime(new Date(2026, 6, 1)))
  })

  it('This Year runs Jan 1 to Jan 1 of next year', () => {
    const r = kscTimespanToStartEndTime('This Year', NOW)
    expect(r.startTime).toBe(getUnixTime(new Date(2026, 0, 1)))
    expect(r.endTime).toBe(getUnixTime(new Date(2027, 0, 1)))
  })

  it('This Week starts on the locale first day of week', () => {
    const r = kscTimespanToStartEndTime('This Week', NOW)
    expect(r.startTime).toBe(getUnixTime(startOfWeek(startOfDay(NOW))))
  })

  it('falls back to the last day for an unknown timespan', () => {
    const r = kscTimespanToStartEndTime('nonsense', NOW)
    expect(r.endTime).toBe(getUnixTime(NOW))
    expect(r.startTime).toBe(getUnixTime(sub(NOW, { days: 1 })))
  })
})

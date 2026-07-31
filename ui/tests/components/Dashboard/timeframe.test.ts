import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { timeframeRange } from '@/services/topnService'
import { timeframeOptions, refreshOptions } from '@/components/Dashboard/timeframe'
import { TimeframePreset } from '@/types/dashboard'

const HOUR = 3_600_000
const DAY = 24 * HOUR

describe('timeframeRange', () => {
  beforeEach(() => {
    // a fixed noon so start-of-day math is unambiguous
    vi.useFakeTimers()
    vi.setSystemTime(new Date(2026, 6, 15, 12, 0, 0))
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  const at = (preset: TimeframePreset, from: string | null = null, to: string | null = null) =>
    timeframeRange({ preset, from, to })

  it('last 24 hours ends now and spans one day', () => {
    const { start, end } = at(TimeframePreset.Last24h)
    expect(end).toBe(Date.now())
    expect(end - start).toBe(DAY)
  })

  it('today starts at midnight', () => {
    const { start, end } = at(TimeframePreset.Today)
    expect(new Date(start).getHours()).toBe(0)
    expect(end).toBe(Date.now())
    expect(end - start).toBe(12 * HOUR)
  })

  it('yesterday is the full previous day', () => {
    const { start, end } = at(TimeframePreset.Yesterday)
    expect(end - start).toBe(DAY)
    expect(new Date(start).getDate()).toBe(14)
    expect(new Date(end).getHours()).toBe(0)
  })

  it('last week is the seven days before the last seven', () => {
    const { start, end } = at(TimeframePreset.LastWeek)
    expect(end).toBe(Date.now() - 7 * DAY)
    expect(end - start).toBe(7 * DAY)
  })

  it('custom uses the given range and falls back when absent', () => {
    const from = new Date(2026, 0, 1).toISOString()
    const to = new Date(2026, 0, 2).toISOString()
    expect(at(TimeframePreset.Custom, from, to)).toEqual({ start: Date.parse(from), end: Date.parse(to) })

    const fallback = at(TimeframePreset.Custom)
    expect(fallback.end).toBe(Date.now())
    expect(fallback.end - fallback.start).toBe(DAY)
  })

  it('an unknown preset behaves like last 24 hours', () => {
    const { start, end } = timeframeRange({ preset: 'bogus' as TimeframePreset, from: null, to: null })
    expect(end - start).toBe(DAY)
  })
})

describe('timeframe and refresh option lists', () => {
  it('cover every preset exactly once', () => {
    const values = timeframeOptions.map((o) => o.value)
    expect(new Set(values).size).toBe(values.length)
    for (const preset of Object.values(TimeframePreset)) {
      expect(values).toContain(preset)
    }
  })

  it('refresh options start with off and ascend', () => {
    expect(refreshOptions[0].value).toBe(0)
    const rest = refreshOptions.slice(1).map((o) => o.value)
    expect([...rest].sort((a, b) => a - b)).toEqual(rest)
  })
})

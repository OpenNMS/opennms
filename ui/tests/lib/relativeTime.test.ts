import { describe, expect, it } from 'vitest'
import { ageSeverity, formatAbsolute, isOlderThan, relativeTimeSince, toMillis } from '@/lib/relativeTime'

const NOW = Date.UTC(2026, 8, 24, 12, 0, 0)
const ago = (ms: number) => NOW - ms
const SEC = 1000
const MIN = 60 * SEC
const HOUR = 60 * MIN
const DAY = 24 * HOUR

describe('relativeTime', () => {
  describe('relativeTimeSince', () => {
    it('renders seconds, minutes, hours and days', () => {
      expect(relativeTimeSince(ago(11 * SEC), NOW)).toBe('11 s ago')
      expect(relativeTimeSince(ago(59 * SEC), NOW)).toBe('59 s ago')
      expect(relativeTimeSince(ago(60 * SEC), NOW)).toBe('1 min ago')
      expect(relativeTimeSince(ago(18 * MIN), NOW)).toBe('18 min ago')
      expect(relativeTimeSince(ago(3 * HOUR + 20 * MIN), NOW)).toBe('3 h ago')
      expect(relativeTimeSince(ago(DAY), NOW)).toBe('1 day ago')
      expect(relativeTimeSince(ago(12 * DAY), NOW)).toBe('12 days ago')
    })

    it('accepts epoch millis, ISO strings and Dates', () => {
      expect(relativeTimeSince(new Date(ago(5 * SEC)).toISOString(), NOW)).toBe('5 s ago')
      expect(relativeTimeSince(new Date(ago(5 * SEC)), NOW)).toBe('5 s ago')
    })

    it('clamps a future timestamp (clock skew) to 0 s', () => {
      expect(relativeTimeSince(NOW + 5 * SEC, NOW)).toBe('0 s ago')
    })

    it('is null for missing or unparseable input', () => {
      expect(relativeTimeSince(null, NOW)).toBeNull()
      expect(relativeTimeSince(undefined, NOW)).toBeNull()
      expect(relativeTimeSince('', NOW)).toBeNull()
      expect(relativeTimeSince('not a date', NOW)).toBeNull()
    })
  })

  describe('ageSeverity', () => {
    it('is success within 5 min, warn within 2 h, danger beyond', () => {
      expect(ageSeverity(ago(4 * MIN), NOW)).toBe('success')
      expect(ageSeverity(ago(5 * MIN), NOW)).toBe('warn')
      expect(ageSeverity(ago(90 * MIN), NOW)).toBe('warn')
      expect(ageSeverity(ago(2 * HOUR), NOW)).toBe('danger')
      expect(ageSeverity(ago(3 * HOUR), NOW)).toBe('danger')
    })

    it('is danger when the timestamp is unknown', () => {
      expect(ageSeverity(null, NOW)).toBe('danger')
      expect(ageSeverity('bogus', NOW)).toBe('danger')
    })
  })

  describe('isOlderThan', () => {
    it('compares against the given age and treats unknown as old', () => {
      expect(isOlderThan(ago(DAY + SEC), DAY, NOW)).toBe(true)
      expect(isOlderThan(ago(DAY - SEC), DAY, NOW)).toBe(false)
      expect(isOlderThan(null, DAY, NOW)).toBe(true)
    })
  })

  describe('toMillis / formatAbsolute', () => {
    it('parses instants and renders a dash for the unknown', () => {
      expect(toMillis(NOW)).toBe(NOW)
      expect(toMillis(new Date(NOW).toISOString())).toBe(NOW)
      expect(toMillis('x')).toBeNull()
      expect(formatAbsolute(null)).toBe('-')
      expect(formatAbsolute(NOW)).toBe(new Date(NOW).toLocaleString())
    })
  })
})

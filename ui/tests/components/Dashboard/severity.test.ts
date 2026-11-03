import { describe, expect, it } from 'vitest'
import { ALARM_CHART_SEVERITIES, SEVERITIES, maxSeverity, severityColor, severityLabel, severityMeta, severityTint } from '@/components/Dashboard/severity'

describe('severity helpers', () => {
  it('keeps the list highest-severity-first with descending weights', () => {
    const weights = SEVERITIES.map(s => s.weight)
    expect([...weights].sort((a, b) => b - a)).toEqual(weights)
    expect(SEVERITIES[0].key).toBe('CRITICAL')
  })

  it('meta lookup is case-insensitive and falls back for unknowns', () => {
    expect(severityMeta('major').key).toBe('MAJOR')
    expect(severityMeta('WARNING').label).toBe('Warning')
    const unknown = severityMeta('WEIRD')
    expect(unknown.weight).toBe(0)
    expect(unknown.key).toBe('WEIRD')
    expect(severityColor('WEIRD')).toBe('#999999')
  })

  it('maxSeverity picks the heaviest of the given keys', () => {
    expect(maxSeverity(['NORMAL', 'MAJOR', 'WARNING'])).toBe('MAJOR')
    expect(maxSeverity([])).toBeDefined()
  })

  it('tint derives from the color and chart severities are all registered', () => {
    expect(severityTint('CRITICAL')).toBe(`${severityColor('CRITICAL')}33`)
    for (const key of ALARM_CHART_SEVERITIES) {
      expect(severityMeta(key).weight, key).toBeGreaterThan(0)
    }
    expect(severityLabel('MINOR')).toBe('Minor')
  })
})

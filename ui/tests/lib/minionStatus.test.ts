import { describe, expect, it } from 'vitest'
import { minionState, minionStateSeverity } from '@/lib/minionStatus'

describe('minionStatus', () => {
  it('recognises up and down case-insensitively and treats everything else as unknown', () => {
    expect(minionState('up')).toBe('up')
    expect(minionState('DOWN')).toBe('down')
    expect(minionState('unknown')).toBe('unknown')
    expect(minionState('degraded')).toBe('unknown')
    expect(minionState('')).toBe('unknown')
    expect(minionState(null)).toBe('unknown')
    expect(minionState(undefined)).toBe('unknown')
  })

  it('maps the state onto the tag severities', () => {
    expect(minionStateSeverity('Up')).toBe('success')
    expect(minionStateSeverity('down')).toBe('danger')
    expect(minionStateSeverity('unknown')).toBe('warn')
    expect(minionStateSeverity('weird')).toBe('warn')
    expect(minionStateSeverity(null)).toBe('warn')
  })
})

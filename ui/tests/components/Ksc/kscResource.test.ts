import { describe, it, expect } from 'vitest'
import { decodeResourceId } from '@/components/Ksc/utils/kscResource'

describe('decodeResourceId', () => {
  it('returns an empty string for null/undefined/empty', () => {
    expect(decodeResourceId(undefined)).toBe('')
    expect(decodeResourceId(null)).toBe('')
    expect(decodeResourceId('')).toBe('')
  })

  it('leaves an unencoded resource id untouched', () => {
    const id = 'node[1].interfaceSnmp[en0]'
    expect(decodeResourceId(id)).toBe(id)
  })

  it('decodes a single-encoded resource id', () => {
    expect(decodeResourceId('node%5B1%5D.interfaceSnmp%5Ben0%5D')).toBe('node[1].interfaceSnmp[en0]')
  })

  it('decodes a double-encoded resource id (NMS-10309)', () => {
    expect(decodeResourceId('node%255B1%255D')).toBe('node[1]')
  })

  it('falls back to the raw value on a malformed sequence rather than throwing', () => {
    // %C3%28 is an invalid UTF-8 sequence; decodeURIComponent throws.
    expect(decodeResourceId('%C3%28')).toBe('%C3%28')
    expect(decodeResourceId('50%')).toBe('50%')
  })
})

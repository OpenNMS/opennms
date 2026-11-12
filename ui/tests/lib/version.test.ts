import { describe, expect, it } from 'vitest'
import { normalizeVersion, sameVersion, versionSortKey } from '@/lib/version'

describe('version', () => {
  it('normalizes a Minion VersionBean string and a /rest/info version to the same triple', () => {
    expect(normalizeVersion('v37.0.0-SNAPSHOT')).toBe('37.0.0')
    expect(normalizeVersion('37.0.0')).toBe('37.0.0')
    expect(normalizeVersion('37.0.0-SNAPSHOT')).toBe('37.0.0')
    expect(normalizeVersion(' v36.0.2 ')).toBe('36.0.2')
  })

  it('is null without a full major.minor.patch triple or for non-strings', () => {
    expect(normalizeVersion('1.0')).toBeNull()
    expect(normalizeVersion('37.0.0.1')).toBeNull()
    expect(normalizeVersion('')).toBeNull()
    expect(normalizeVersion(null)).toBeNull()
    expect(normalizeVersion(undefined)).toBeNull()
    expect(normalizeVersion(37)).toBeNull()
  })

  it('compares triples, ignoring the leading v and a SNAPSHOT qualifier', () => {
    expect(sameVersion('v37.0.0-SNAPSHOT', '37.0.0')).toBe(true)
    expect(sameVersion('v36.0.2', '37.0.0')).toBe(false)
    expect(sameVersion('1.0', '37.0.0')).toBe(false)
    expect(sameVersion('37.0.0', null)).toBe(false)
  })

  it('sorts on the bare triple, so a leading v no longer pushes a version past every unprefixed one', () => {
    const collate = new Intl.Collator(undefined, { numeric: true }).compare
    const sorted = ['36.0.10', 'v37.0.0-SNAPSHOT', '36.0.9', 'v35.0.2']
      .sort((a, b) => collate(versionSortKey(a), versionSortKey(b)))
    expect(sorted).toEqual(['v35.0.2', '36.0.9', '36.0.10', 'v37.0.0-SNAPSHOT'])
    expect(versionSortKey('1.0')).toBe('1.0')
    expect(versionSortKey(null)).toBe('')
  })
})

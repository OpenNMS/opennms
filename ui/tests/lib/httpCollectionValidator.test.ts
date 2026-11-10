import { describe, expect, test } from 'vitest'
import {
  groupCount,
  hasErrors,
  sameCollectionName,
  validateCollection,
  validateSettings,
  validateUri
} from '@/lib/httpCollectionValidator'
import { getDefaultCollection, getDefaultUri } from '@/stores/httpCollectionStore'
import type { HttpCollection, HttpUri } from '@/types/httpCollectionConfig'

const collection = (overrides: Partial<HttpCollection> = {}): HttpCollection => ({
  ...getDefaultCollection(),
  name: 'doc-count',
  ...overrides
})

const uri = (overrides: Partial<HttpUri> = {}): HttpUri => {
  const base = getDefaultUri()
  base.name = 'status'
  base.url.path = '/status'
  base.url.matches = 'Count: ([0-9]+)'
  base.attributes = [{ alias: 'count', matchGroup: 1, type: 'gauge' }]
  return { ...base, ...overrides }
}

describe('httpCollectionValidator', () => {
  test('accepts the defaults once named', () => {
    expect(hasErrors(validateCollection(collection()))).toBe(false)
    expect(hasErrors(validateUri(uri()))).toBe(false)
  })

  test('requires the rrd repository setting', () => {
    expect(validateSettings({ rrdRepository: ' ' }).rrdRepository).toBeTruthy()
    expect(hasErrors(validateSettings({ rrdRepository: '/rrd' }))).toBe(false)
  })

  test('refuses a name another collection has, ignoring case', () => {
    expect(validateCollection(collection({ name: 'DOC-COUNT' }), ['doc-count']).name).toBe('A collection with this name already exists.')
    expect(sameCollectionName(' Doc ', 'doc')).toBe(true)
  })

  test('checks the rrd', () => {
    expect(validateCollection(collection({ rrd: { step: 0, rras: ['RRA:AVERAGE:0.5:1:10'] }})).step).toBeTruthy()
    expect(validateCollection(collection({ rrd: { step: 300, rras: [] }})).rras).toBe('At least one RRA is required.')
    expect(validateCollection(collection({ rrd: { step: 300, rras: ['RRA:SUM:0.5:1:10'] }})).rras).toContain('RRA:SUM')
  })

  test('checks the url', () => {
    const invalid = uri()
    invalid.url.path = ''
    invalid.url.port = 70000
    invalid.url.responseRange = '399-100'
    invalid.url.headers = [{ name: 'Accept', value: '' }]
    invalid.url.parameters = [{ key: '', value: 'x' }]

    const errors = validateUri(invalid)
    expect(errors.path).toBeTruthy()
    expect(errors.port).toBeTruthy()
    expect(errors.responseRange).toBeTruthy()
    expect(errors.headers).toBeTruthy()
    expect(errors.parameters).toBeTruthy()
  })

  test('refuses a uri name used twice in the collection', () => {
    expect(validateUri(uri(), ['status']).name).toBe('Another URI of this collection has this name.')
  })

  test('checks the attributes against the groups of the expression', () => {
    const tooFar = uri({ attributes: [{ alias: 'count', matchGroup: 2, type: 'gauge' }] })
    expect(validateUri(tooFar).attributes).toContain('does not exist; the expression has 1 group')

    const tooLong = uri({ attributes: [{ alias: 'aVeryLongAliasNameOver19', matchGroup: 1, type: 'gauge' }] })
    expect(validateUri(tooLong).attributes).toContain('longer than 19')

    const badType = uri({ attributes: [{ alias: 'count', matchGroup: 1, type: 'timeticks' }] })
    expect(validateUri(badType).attributes).toContain('gauge, counter or string')

    const aliasType = uri({ attributes: [{ alias: 'count', matchGroup: 1, type: 'counter32' }] })
    expect(validateUri(aliasType).attributes).toBeUndefined()

    const twice = uri({ attributes: [{ alias: 'count', matchGroup: 1, type: 'gauge' }, { alias: 'count', matchGroup: 1, type: 'gauge' }] })
    expect(validateUri(twice).attributes).toContain('used more than once')
  })

  test('counts groups, leaving what JavaScript cannot judge to the server', () => {
    expect(groupCount({ path: '/', matches: 'a(b)(c)' })).toBe(2)
    expect(groupCount({ path: '/', matches: null })).toBe(1)
    expect(groupCount({ path: '/', matches: '(a)', literal: true })).toBe(0)
    expect(groupCount({ path: '/', matches: '(a) # comment', comments: true })).toBeNull()
    // Possessive quantifiers are Java-only.
    expect(groupCount({ path: '/', matches: '(a++)' })).toBeNull()
  })
})

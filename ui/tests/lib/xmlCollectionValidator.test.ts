import { describe, expect, test } from 'vitest'
import {
  hasErrors,
  validateCollectionBasics,
  validateGroup,
  validateGroupFileId,
  validateObject,
  validateRra,
  validateSource
} from '@/lib/xmlCollectionValidator'
import { getDefaultGroup, getDefaultObject, getDefaultRequest, getDefaultSource } from '@/stores/xmlCollectionConfigStore'
import type { XmlGroup, XmlSource } from '@/types/xmlCollectionConfig'

const group = (overrides: Partial<XmlGroup> = {}): XmlGroup => ({
  ...getDefaultGroup(),
  name: 'g',
  objects: [{ ...getDefaultObject(), name: 'value', xpath: 'value' }],
  ...overrides
})

const source = (overrides: Partial<XmlSource> = {}): XmlSource => ({
  ...getDefaultSource(),
  url: 'http://{ipaddr}/status',
  groups: [group()],
  ...overrides
})

describe('xmlCollectionValidator', () => {
  test('accepts the RRAs the server accepts', () => {
    for (const rra of ['RRA:AVERAGE:0.5:1:2016', 'RRA:MAX:0:288:366', 'RRA:LAST:1:1:1', 'RRA:MIN:.5:12:1488']) {
      expect(validateRra(rra), rra).toBeUndefined()
    }
    for (const rra of ['RRA:SUM:0.5:1:2016', 'RRA:AVERAGE:1.5:1:2016', 'RRA:AVERAGE:0.5:0:2016', '']) {
      expect(validateRra(rra), rra).toBeDefined()
    }
  })

  test('checks the collection basics', () => {
    expect(hasErrors(validateCollectionBasics('c', { step: 300, rras: ['RRA:AVERAGE:0.5:1:2016'] }, []))).toBe(false)

    const errors = validateCollectionBasics('taken', { step: 0, rras: [] }, ['taken'])
    expect(errors.name).toContain('already exists')
    expect(errors.step).toBeDefined()
    expect(errors.rras).toBe('At least one RRA is required.')

    expect(validateCollectionBasics('c', { step: null, rras: ['bad'] }, []).rras).toContain('\'bad\' is invalid')
  })

  test('accepts a source with groups or imports', () => {
    expect(hasErrors(validateSource(source(), [], []))).toBe(false)
    expect(hasErrors(validateSource(source({ groups: [], importGroups: ['shared'] }), [], ['shared']))).toBe(false)
  })

  test('refuses a source without groups and imports', () => {
    expect(validateSource(source({ groups: [] }), [], []).groups).toBe('Define at least one group or import a group file.')
  })

  test('refuses a duplicate URL and a missing group file', () => {
    const errors = validateSource(source({ importGroups: ['gone'] }), [' http://{ipaddr}/status '], ['shared'])
    expect(errors.url).toContain('already uses this URL')
    expect(errors.importGroups).toBe('Group file \'gone\' does not exist.')
  })

  test('checks the request', () => {
    const request = { ...getDefaultRequest(), method: 'GE T', content: '{}', contentType: '', headers: [{ key: '', value: 'x' }] }
    const errors = validateSource(source({ request }), [], [])
    expect(errors.method).toBeDefined()
    expect(errors.contentType).toBe('The request content needs a content type.')
    expect(errors.headers).toBe('Every request header needs a name.')
  })

  test('checks groups', () => {
    expect(hasErrors(validateGroup(group()))).toBe(false)
    expect(validateGroup(group(), ['g']).name).toContain('already exists')
    expect(validateGroup(group({ resourceXpath: ' ' })).resourceXpath).toBeDefined()
    expect(validateGroup(group({ objects: [] })).objects).toBe('At least one object is required.')

    const twice = group({ objects: [group().objects[0], group().objects[0]] })
    expect(validateGroup(twice).objects).toBe('Object 1: \'value\' is defined more than once.')
  })

  test('checks objects and their mappings', () => {
    expect(validateObject({ name: 'o', type: 'COUNTER32', xpath: 'x', mappings: [] }).type).toBeUndefined()
    expect(validateObject({ name: 'o', type: 'float', xpath: 'x', mappings: [] }).type).toContain('invalid')
    expect(validateObject({ name: 'o', type: 'gauge', xpath: 'x', mappings: [{ from: null, to: '1' }, { from: '', to: '2' }] }).mappings)
      .toBe('Only one value mapping may leave "from" empty.')
    expect(validateObject({ name: 'o', type: 'gauge', xpath: 'x', mappings: [{ from: 'a', to: ' ' }] }).mappings)
      .toBe('Every value mapping needs a value to map to.')
  })

  test('checks group file ids', () => {
    expect(validateGroupFileId('elasticsearch-cluster-stats', [])).toBeUndefined()
    expect(validateGroupFileId('../x', [])).toBeDefined()
    expect(validateGroupFileId('taken', ['taken'])).toContain('already exists')
  })
})

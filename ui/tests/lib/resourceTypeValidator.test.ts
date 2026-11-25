import { describe, expect, test } from 'vitest'
import { hasErrors, overrideWarning, validateConfigId, validateResourceType } from '@/lib/resourceTypeValidator'
import { getDefaultResourceType } from '@/stores/resourceTypesConfigStore'
import type { ResourceTypeDefinition, ResourceTypesMetadata } from '@/types/resourceTypesConfig'

const type = (overrides: Partial<ResourceTypeDefinition> = {}): ResourceTypeDefinition => ({
  ...getDefaultResourceType(),
  name: 'pgTableSpace',
  label: 'PostgreSQL Tablespace',
  ...overrides
})

const metadata: ResourceTypesMetadata = {
  storageStrategies: [],
  persistenceSelectorStrategies: [],
  dataCollectionTypeNames: ['hrStorageIndex'],
  extensionTypeNames: ['fromExtension']
}

describe('resourceTypeValidator', () => {
  describe('validateConfigId', () => {
    test.each(['postgresql-resource', 'wsman.dell_idrac', '1abc'])('accepts %s', (id) => {
      expect(hasErrors(validateConfigId(id, []))).toBe(false)
    })

    test.each(['', ' ', '-leading', 'with blank', '../etc', 'a/b'])('rejects \'%s\'', (id) => {
      expect(validateConfigId(id, []).id).toBeTruthy()
    })

    test('rejects a taken id', () => {
      expect(validateConfigId('jmx-resource', ['jmx-resource']).id).toContain('already exists')
    })
  })

  describe('validateResourceType', () => {
    test('accepts the default strategies', () => {
      expect(hasErrors(validateResourceType(type(), []))).toBe(false)
    })

    test('requires name and label', () => {
      const errors = validateResourceType(type({ name: ' ', label: '' }), [])

      expect(errors.name).toBe('Name is required.')
      expect(errors.label).toBe('Label is required.')
    })

    test('rejects names that are no valid directory names', () => {
      expect(validateResourceType(type({ name: 'a/b' }), []).name).toContain('must not contain')
      expect(validateResourceType(type({ name: 'a b' }), []).name).toContain('must not contain')
    })

    test('rejects a name taken elsewhere', () => {
      expect(validateResourceType(type(), ['pgTableSpace']).name).toContain('already exists')
    })

    test('requires valid strategy classes', () => {
      const errors = validateResourceType(type({
        persistenceSelectorStrategy: { className: '', parameters: [] },
        storageStrategy: { className: 'not a class', parameters: [] }
      }), [])

      expect(errors.persistenceSelectorStrategy).toBe('The class is required.')
      expect(errors.storageStrategy).toBe('Not a valid Java class name.')
    })

    test('requires parameter keys and values but allows repeated keys', () => {
      const repeated = type({
        storageStrategy: {
          className: 'org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy',
          parameters: [{ key: 'replace', value: 's/a/b/' }, { key: 'replace', value: 's/c/d/' }]
        }
      })
      expect(hasErrors(validateResourceType(repeated, []))).toBe(false)

      const noValue = type({ storageStrategy: { className: 'a.B', parameters: [{ key: 'k', value: '' }] }})
      expect(validateResourceType(noValue, []).storageParameters).toBe('Parameter \'k\' needs a value.')

      const noKey = type({ persistenceSelectorStrategy: { className: 'a.B', parameters: [{ key: '', value: 'v' }] }})
      expect(validateResourceType(noKey, []).persistenceSelectorParameters).toBe('Every parameter needs a key.')
    })
  })

  describe('overrideWarning', () => {
    test('says which definition wins', () => {
      expect(overrideWarning('fromExtension', metadata)).toContain('extension\'s definition is used')
      expect(overrideWarning('hrStorageIndex', metadata)).toContain('replaces it')
      expect(overrideWarning('other', metadata)).toBeUndefined()
      expect(overrideWarning('hrStorageIndex', null)).toBeUndefined()
    })
  })
})

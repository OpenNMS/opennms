import { describe, expect, test } from 'vitest'
import { configEditPath, configPath, idFromParam, indexFromParam, typeCreatePath, typeEditPath } from '@/lib/resourceTypeRoutes'

describe('resourceTypeRoutes', () => {
  test('builds the paths with the id encoded once', () => {
    expect(configPath('my types')).toBe('/resource-types/config/my%20types')
    expect(configEditPath('jmx-resource')).toBe('/resource-types/config/jmx-resource/edit')
    expect(typeCreatePath('jmx-resource')).toBe('/resource-types/config/jmx-resource/create-type')
    expect(typeEditPath('jmx-resource', 2)).toBe('/resource-types/config/jmx-resource/type/2')
  })

  test('reads params without decoding them again', () => {
    expect(idFromParam('a%20b')).toBe('a%20b')
    expect(idFromParam(undefined)).toBe('')
    expect(indexFromParam(undefined)).toBe(-1)
    expect(indexFromParam('3')).toBe(3)
  })
})

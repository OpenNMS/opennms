import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, test, vi } from 'vitest'
import { getDefaultResourceType, useResourceTypesConfigStore } from '@/stores/resourceTypesConfigStore'
import API from '@/services'
import type { ResourceTypeDefinition, ResourceTypesConfig } from '@/types/resourceTypesConfig'
import { RELOADED_AFTER_PRECONDITION_FAILED } from '@/types/validation'

vi.mock('@/services', () => ({
  default: {
    getResourceTypesConfigs: vi.fn(),
    getResourceTypesConfig: vi.fn(),
    getResourceTypesMetadata: vi.fn(),
    createResourceTypesConfig: vi.fn(),
    updateResourceTypesConfig: vi.fn(),
    deleteResourceTypesConfig: vi.fn(),
    uploadResourceTypesConfig: vi.fn()
  }
}))

const ok = <T = undefined>(payload?: T) => ({ success: true, message: '', payload })
const fail = (message: string, status?: number) => ({ success: false, message, status })

const type = (name: string): ResourceTypeDefinition => ({ ...getDefaultResourceType(), name, label: name })

const config = (overrides: Partial<ResourceTypesConfig> = {}): ResourceTypesConfig => ({
  id: 'jmx-resource',
  resourceTypes: [type('kafkaLag'), type('jvm')],
  version: 'v1',
  ...overrides
})

describe('resourceTypesConfigStore', () => {
  let store: ReturnType<typeof useResourceTypesConfigStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    store = useResourceTypesConfigStore()
    vi.mocked(API.getResourceTypesConfigs).mockResolvedValue(ok([
      { id: 'jmx-resource', resourceTypeNames: ['kafkaLag', 'jvm'] },
      { id: 'postgresql-resource', resourceTypeNames: ['pgTableSpace'] }
    ]))
    vi.mocked(API.getResourceTypesConfig).mockResolvedValue(ok(config({ version: 'v2' })))
  })

  test('knows the names of the other configurations', async () => {
    await store.fetchConfigs()

    expect(store.configIds).toEqual(['jmx-resource', 'postgresql-resource'])
    expect(store.typeNamesOutside('jmx-resource')).toEqual(['pgTableSpace'])
  })

  test('adds a type on a copy and writes the whole configuration', async () => {
    store.currentConfig = config()
    vi.mocked(API.updateResourceTypesConfig).mockResolvedValue(ok())

    const result = await store.saveResourceType(null, type('new'))

    expect(result.success).toBe(true)
    const written = vi.mocked(API.updateResourceTypesConfig).mock.calls[0]
    expect(written[0]).toBe('jmx-resource')
    expect(written[1].resourceTypes.map(t => t.name)).toEqual(['kafkaLag', 'jvm', 'new'])
    expect(written[1].version).toBe('v1')
    // reloaded for the new entity tag
    expect(store.currentConfig?.version).toBe('v2')
  })

  test('replaces and deletes a type by index', async () => {
    store.currentConfig = config()
    vi.mocked(API.updateResourceTypesConfig).mockResolvedValue(ok())

    await store.saveResourceType(1, type('jvm2'))
    expect(vi.mocked(API.updateResourceTypesConfig).mock.calls[0][1].resourceTypes.map(t => t.name)).toEqual(['kafkaLag', 'jvm2'])

    store.currentConfig = config()
    await store.deleteResourceType(0)
    expect(vi.mocked(API.updateResourceTypesConfig).mock.calls[1][1].resourceTypes.map(t => t.name)).toEqual(['jvm'])
  })

  test('a failed save leaves the loaded configuration alone', async () => {
    store.currentConfig = config()
    vi.mocked(API.updateResourceTypesConfig).mockResolvedValue(fail('boom', 400))

    const result = await store.saveResourceType(null, type('new'))

    expect(result.success).toBe(false)
    expect(store.currentConfig.resourceTypes).toHaveLength(2)
  })

  test('reloads after a 412', async () => {
    store.currentConfig = config()
    vi.mocked(API.updateResourceTypesConfig).mockResolvedValue(fail('changed', 412))

    const result = await store.deleteResourceType(0)

    expect(result.message).toBe(`changed ${RELOADED_AFTER_PRECONDITION_FAILED}`)
    expect(store.currentConfig?.version).toBe('v2')
  })

  test('renames by writing the configuration under its new id', async () => {
    store.currentConfig = config()
    vi.mocked(API.updateResourceTypesConfig).mockResolvedValue(ok())

    await store.renameConfig('jmx-resource', 'kafka-resource')

    expect(API.updateResourceTypesConfig).toHaveBeenCalledWith('jmx-resource', expect.objectContaining({ id: 'kafka-resource', version: 'v1' }))
    expect(API.getResourceTypesConfig).toHaveBeenCalledWith('kafka-resource')
  })

  test('hands out a filled-in clone of a type', () => {
    store.currentConfig = config({
      resourceTypes: [{ name: 'bare', label: 'Bare' } as unknown as ResourceTypeDefinition]
    })

    const copy = store.resourceTypeAt(0)

    expect(copy?.resourceLabel).toBe('')
    expect(copy?.storageStrategy.parameters).toEqual([])
    expect(store.resourceTypeAt(1)).toBeNull()
  })

  test('reports a failed refresh after a successful write', async () => {
    vi.mocked(API.createResourceTypesConfig).mockResolvedValue(ok())
    vi.mocked(API.getResourceTypesConfigs).mockResolvedValue(fail('list down'))

    const result = await store.createConfig({ id: 'new', resourceTypes: [] })

    expect(result.success).toBe(true)
    expect(result.errors).toEqual(['list down'])
  })

  test('forgets a deleted configuration', async () => {
    store.currentConfig = config()
    vi.mocked(API.deleteResourceTypesConfig).mockResolvedValue(ok())

    await store.deleteConfig('jmx-resource', 'v1')

    expect(API.deleteResourceTypesConfig).toHaveBeenCalledWith('jmx-resource', 'v1')
    expect(store.currentConfig).toBeNull()
  })
})

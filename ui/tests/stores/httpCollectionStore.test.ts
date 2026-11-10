import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, test, vi } from 'vitest'
import { getDefaultUri, useHttpCollectionStore } from '@/stores/httpCollectionStore'
import API from '@/services'
import type { HttpCollection } from '@/types/httpCollectionConfig'

vi.mock('@/services', () => ({
  default: {
    getHttpCollections: vi.fn(),
    getHttpCollection: vi.fn(),
    getHttpCollectionSettings: vi.fn(),
    getHttpCollectionMetadata: vi.fn(),
    updateHttpCollectionSettings: vi.fn(),
    createHttpCollection: vi.fn(),
    updateHttpCollection: vi.fn(),
    deleteHttpCollection: vi.fn()
  }
}))

const ok = (payload?: unknown) => ({ success: true, message: '', payload })
const fail = (message: string, status?: number) => ({ success: false, message, status })

const collection = (): HttpCollection => {
  const first = getDefaultUri()
  first.name = 'first'
  return { name: 'doc-count', rrd: { step: 300, rras: ['RRA:AVERAGE:0.5:1:10'] }, uris: [first], version: 'v1' }
}

describe('httpCollectionStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.mocked(API.getHttpCollections).mockResolvedValue(ok([{ name: 'doc-count' }]) as never)
    vi.mocked(API.getHttpCollection).mockResolvedValue(ok(collection()) as never)
  })

  test('loads the collections and one collection', async () => {
    const store = useHttpCollectionStore()
    await store.fetchCollections()
    await store.fetchCollection('doc-count')

    expect(store.collectionNames).toEqual(['doc-count'])
    expect(store.currentUris.map(uri => uri.name)).toEqual(['first'])
  })

  test('forgets the collection when it cannot be loaded', async () => {
    const store = useHttpCollectionStore()
    store.currentCollection = collection()
    vi.mocked(API.getHttpCollection).mockResolvedValue(fail('gone', 404) as never)

    const result = await store.fetchCollection('doc-count')

    expect(result.success).toBe(false)
    expect(store.currentCollection).toBeNull()
  })

  test('adds a uri on a copy and saves the whole collection with its version', async () => {
    const store = useHttpCollectionStore()
    await store.fetchCollection('doc-count')
    vi.mocked(API.updateHttpCollection).mockResolvedValue(ok())

    const added = getDefaultUri()
    added.name = 'second'
    const result = await store.saveUri(null, added)

    expect(result.success).toBe(true)
    const [name, saved] = vi.mocked(API.updateHttpCollection).mock.calls[0]
    expect(name).toBe('doc-count')
    expect(saved.uris.map(uri => uri.name)).toEqual(['first', 'second'])
    expect(saved.version).toBe('v1')
  })

  test('leaves the loaded collection alone when a save fails', async () => {
    const store = useHttpCollectionStore()
    await store.fetchCollection('doc-count')
    vi.mocked(API.updateHttpCollection).mockResolvedValue(fail('invalid', 400))

    const result = await store.deleteUri(0)

    expect(result.success).toBe(false)
    expect(result.message).toBe('invalid')
    expect(store.currentUris).toHaveLength(1)
  })

  test('reloads the stored collection after a conflict', async () => {
    const store = useHttpCollectionStore()
    await store.fetchCollection('doc-count')
    vi.mocked(API.updateHttpCollection).mockResolvedValue(fail('changed', 412))

    const result = await store.deleteUri(0)

    expect(result.message).toContain('The current version has been loaded')
    expect(API.getHttpCollection).toHaveBeenCalledTimes(2)
  })

  test('hands out copies of uris so cancel discards', async () => {
    const store = useHttpCollectionStore()
    await store.fetchCollection('doc-count')

    const copy = store.uriAt(0)
    copy!.name = 'changed'

    expect(store.currentUris[0].name).toBe('first')
    expect(store.uriAt(5)).toBeNull()
    expect(store.uriAt(null)?.url.method).toBe('GET')
  })

  test('refetches the list after a delete', async () => {
    const store = useHttpCollectionStore()
    await store.fetchCollection('doc-count')
    vi.mocked(API.deleteHttpCollection).mockResolvedValue(ok())

    await store.deleteCollection('doc-count', 'v1')

    expect(API.deleteHttpCollection).toHaveBeenCalledWith('doc-count', 'v1')
    expect(store.currentCollection).toBeNull()
    expect(API.getHttpCollections).toHaveBeenCalled()
  })
})

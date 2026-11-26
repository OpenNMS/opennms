import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, test, vi } from 'vitest'
import { getDefaultCollection, getDefaultSource, useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import API from '@/services'
import type { XmlCollection } from '@/types/xmlCollectionConfig'

vi.mock('@/services', () => ({
  default: {
    getXmlCollections: vi.fn(),
    getXmlCollection: vi.fn(),
    updateXmlCollection: vi.fn(),
    deleteXmlCollection: vi.fn(),
    getXmlGroupFiles: vi.fn(),
    getXmlGroupFile: vi.fn(),
    updateXmlGroupFile: vi.fn(),
    getXmlCollectionMetadata: vi.fn(),
    getXmlCollectionSettings: vi.fn(),
    updateXmlCollectionSettings: vi.fn()
  }
}))

const ok = <T>(payload?: T) => ({ success: true, message: '', payload })
const fail = (message: string, status?: number) => ({ success: false, message, status })

const collection = (): XmlCollection => ({
  ...getDefaultCollection(),
  name: 'c',
  sources: [{ ...getDefaultSource(), url: 'http://a' }, { ...getDefaultSource(), url: 'http://b' }],
  version: 'v1'
})

describe('xmlCollectionConfigStore', () => {
  let store: ReturnType<typeof useXmlCollectionConfigStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    store = useXmlCollectionConfigStore()
    vi.mocked(API.getXmlCollections).mockResolvedValue(ok([]))
    vi.mocked(API.getXmlGroupFiles).mockResolvedValue(ok([]))
    vi.mocked(API.getXmlCollection).mockResolvedValue(ok(collection()))
  })

  test('saves a changed source on a copy of the collection', async () => {
    store.currentCollection = collection()
    vi.mocked(API.updateXmlCollection).mockResolvedValue(ok())

    const result = await store.saveSource(1, { ...getDefaultSource(), url: 'http://changed' })

    expect(result.success).toBe(true)
    const [name, sent] = vi.mocked(API.updateXmlCollection).mock.calls[0]
    expect(name).toBe('c')
    expect(sent.sources.map(source => source.url)).toEqual(['http://a', 'http://changed'])
    expect(sent.version).toBe('v1')
    // Reloaded for the new version.
    expect(API.getXmlCollection).toHaveBeenCalledWith('c')
  })

  test('appends a new source and removes one', async () => {
    store.currentCollection = collection()
    vi.mocked(API.updateXmlCollection).mockResolvedValue(fail('invalid'))

    await store.saveSource(null, { ...getDefaultSource(), url: 'http://new' })
    expect(vi.mocked(API.updateXmlCollection).mock.calls[0][1].sources).toHaveLength(3)

    await store.deleteSource(0)
    expect(vi.mocked(API.updateXmlCollection).mock.calls[1][1].sources.map(source => source.url)).toEqual(['http://b'])

    // A failed save leaves the loaded collection alone.
    expect(store.currentCollection?.sources).toHaveLength(2)
  })

  test('refuses a source index the collection does not have', async () => {
    store.currentCollection = collection()

    expect((await store.saveSource(5, getDefaultSource())).success).toBe(false)
    expect((await store.deleteSource(-1)).success).toBe(false)
    expect(API.updateXmlCollection).not.toHaveBeenCalled()
  })

  test('reloads the collection after a 412', async () => {
    store.currentCollection = collection()
    vi.mocked(API.updateXmlCollection).mockResolvedValue(fail('changed since it was read.', 412))

    const result = await store.saveSource(0, getDefaultSource())

    expect(result.message).toBe('changed since it was read. The current version has been loaded.')
    expect(API.getXmlCollection).toHaveBeenCalledWith('c')
  })

  test('hands out copies of sources', () => {
    store.currentCollection = collection()

    const source = store.sourceAt(0)
    source!.url = 'changed'

    expect(store.currentCollection.sources[0].url).toBe('http://a')
    expect(store.sourceAt(9)).toBeNull()
    expect(store.sourceAt(null)?.url).toBe('')
  })

  test('reloads a group file after a 412', async () => {
    vi.mocked(API.updateXmlGroupFile).mockResolvedValue(fail('stale', 412))
    vi.mocked(API.getXmlGroupFile).mockResolvedValue(ok({ id: 'f', groups: [], version: 'v2' }))

    const result = await store.updateGroupFile({ id: 'f', groups: [], version: 'v1' })

    expect(result.success).toBe(false)
    expect(store.currentGroupFile?.version).toBe('v2')
  })

  test('loads settings and metadata', async () => {
    vi.mocked(API.getXmlCollectionSettings).mockResolvedValue(ok({ rrdRepository: '/rrd' }))
    vi.mocked(API.getXmlCollectionMetadata).mockResolvedValue(ok({ attributeTypes: ['gauge'], resourceTypes: [], groupFiles: ['f'] }))

    const result = await store.fetchAll()

    expect(result.success).toBe(true)
    expect(store.rrdRepository).toBe('/rrd')
    expect(store.metadata.groupFiles).toEqual(['f'])
  })
})

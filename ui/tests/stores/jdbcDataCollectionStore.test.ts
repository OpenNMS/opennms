import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, test, vi } from 'vitest'
import { getDefaultCollection, getDefaultQuery, useJdbcDataCollectionStore } from '@/stores/jdbcDataCollectionStore'
import API from '@/services'
import type { JdbcCollection, JdbcSource } from '@/types/jdbcDataCollection'

vi.mock('@/services', () => ({
  default: {
    getJdbcSources: vi.fn(),
    getJdbcSource: vi.fn(),
    getJdbcDataCollectionMetadata: vi.fn(),
    createJdbcSource: vi.fn(),
    updateJdbcSource: vi.fn(),
    deleteJdbcSource: vi.fn(),
    createJdbcCollection: vi.fn(),
    updateJdbcCollection: vi.fn(),
    deleteJdbcCollection: vi.fn(),
    uploadJdbcSource: vi.fn()
  }
}))

const ok = (payload?: unknown) => ({ success: true, message: '', payload })
const fail = (message: string, status?: number) => ({ success: false, message, status })

const collection = (name: string, queryNames: string[] = []): JdbcCollection => ({
  ...getDefaultCollection(),
  name,
  queries: queryNames.map(queryName => ({ ...getDefaultQuery(), name: queryName })),
  usedBy: ['pkg/svc'],
  shadowedBy: null
})

const source = (overrides: Partial<JdbcSource> = {}): JdbcSource => ({
  id: 'mysql',
  rrdRepository: '/rrd',
  collections: [collection('MySQL', ['q1', 'q2'])],
  version: 'v1',
  ...overrides
})

describe('jdbcDataCollectionStore', () => {
  let store: ReturnType<typeof useJdbcDataCollectionStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    store = useJdbcDataCollectionStore()
    vi.mocked(API.getJdbcSource).mockResolvedValue(ok(source({ version: 'v2' })) as never)
    vi.mocked(API.getJdbcSources).mockResolvedValue(ok([
      { id: 'default', collectionCount: 1, collectionNames: ['default'] },
      { id: 'mysql', collectionCount: 1, collectionNames: ['MySQL'] }
    ]) as never)
  })

  test('loads the sources and a source', async () => {
    await store.fetchSources()
    await store.fetchSource('mysql')

    expect(store.sourceIds).toEqual(['default', 'mysql'])
    expect(store.currentSource?.version).toBe('v2')
  })

  test('clears the current source when it cannot be loaded', async () => {
    store.currentSource = source()
    vi.mocked(API.getJdbcSource).mockResolvedValue(fail('gone', 404) as never)

    const result = await store.fetchSource('mysql')

    expect(result.success).toBe(false)
    expect(store.currentSource).toBeNull()
  })

  test('collection names in use exclude only the one being edited', async () => {
    await store.fetchSources()

    expect(store.takenCollectionNames()).toEqual(['default', 'MySQL'])
    expect(store.takenCollectionNames('mysql', 'MySQL')).toEqual(['default'])
    expect(store.takenCollectionNames('default', 'MySQL')).toEqual(['default', 'MySQL'])
  })

  test('adds a query by writing the whole collection without the read-only fields', async () => {
    store.currentSource = source()
    vi.mocked(API.updateJdbcCollection).mockResolvedValue(ok() as never)

    const result = await store.saveQuery('MySQL', -1, { ...getDefaultQuery(), name: 'q3' })

    expect(result.success).toBe(true)
    const [id, name, written, version] = vi.mocked(API.updateJdbcCollection).mock.calls[0]
    expect([id, name, version]).toEqual(['mysql', 'MySQL', 'v1'])
    expect(written.queries.map(query => query.name)).toEqual(['q1', 'q2', 'q3'])
    expect(written).not.toHaveProperty('usedBy')
    expect(written).not.toHaveProperty('shadowedBy')
    expect(store.currentSource?.version).toBe('v2')
  })

  test('replaces and deletes a query by position', async () => {
    store.currentSource = source()
    vi.mocked(API.updateJdbcCollection).mockResolvedValue(ok() as never)

    await store.saveQuery('MySQL', 1, { ...getDefaultQuery(), name: 'changed' })
    store.currentSource = source()
    await store.deleteQuery('MySQL', 0)

    const calls = vi.mocked(API.updateJdbcCollection).mock.calls
    expect(calls[0][2].queries.map(query => query.name)).toEqual(['q1', 'changed'])
    expect(calls[1][2].queries.map(query => query.name)).toEqual(['q2'])
  })

  test('does not touch the loaded source before the write succeeded', async () => {
    store.currentSource = source()
    vi.mocked(API.updateJdbcCollection).mockResolvedValue(fail('invalid', 400) as never)

    const result = await store.saveQuery('MySQL', -1, { ...getDefaultQuery(), name: 'q3' })

    expect(result.message).toBe('invalid')
    expect(store.currentSource?.collections[0].queries).toHaveLength(2)
    expect(API.getJdbcSource).not.toHaveBeenCalled()
  })

  test('reloads the source after a conflict and says so', async () => {
    store.currentSource = source()
    vi.mocked(API.deleteJdbcCollection).mockResolvedValue(fail('changed since it was read.', 412) as never)

    const result = await store.deleteCollection('MySQL')

    expect(API.getJdbcSource).toHaveBeenCalledWith('mysql')
    expect(result.message).toContain('The current version has been loaded')
    expect(store.currentSource?.version).toBe('v2')
  })

  test('refuses query changes without a loaded collection', async () => {
    expect((await store.saveQuery('MySQL', -1, getDefaultQuery())).success).toBe(false)
    store.currentSource = source()
    expect((await store.deleteQuery('MySQL', 5)).success).toBe(false)
    expect(API.updateJdbcCollection).not.toHaveBeenCalled()
  })

  test('hands out copies, so a cancelled edit leaves the source alone', () => {
    store.currentSource = source()

    const copy = store.collectionAt('MySQL')
    copy?.queries.pop()
    const query = store.queryAt('MySQL', 0)
    query?.columns.push({ name: 'x', type: 'gauge', alias: 'x' })

    expect(store.currentSource.collections[0].queries).toHaveLength(2)
    expect(store.currentSource.collections[0].queries[0].columns).toHaveLength(1)
    expect(store.queryAt('MySQL', 9)).toBeNull()
    expect(store.queryAt('MySQL', -1)?.name).toBe('')
    expect(store.collectionAt('nope')).toBeNull()
  })

  test('creates a source without collections and refreshes the list', async () => {
    vi.mocked(API.createJdbcSource).mockResolvedValue(ok() as never)

    await store.createSource({ id: ' custom ', rrdRepository: '/rrd', collections: [collection('x')] })

    expect(API.createJdbcSource).toHaveBeenCalledWith({ id: 'custom', rrdRepository: '/rrd', collections: [] })
    expect(API.getJdbcSources).toHaveBeenCalled()
  })

  test('reports a failed refresh after a successful write in errors', async () => {
    store.currentSource = source()
    vi.mocked(API.updateJdbcSource).mockResolvedValue(ok() as never)
    vi.mocked(API.getJdbcSources).mockResolvedValue(fail('list failed') as never)

    const result = await store.updateRepository('/var/rrd')

    expect(API.updateJdbcSource).toHaveBeenCalledWith(expect.objectContaining({ id: 'mysql', rrdRepository: '/var/rrd', version: 'v1' }))
    expect(result.success).toBe(true)
    expect(result.errors).toEqual(['list failed'])
  })

  test('forgets a deleted source', async () => {
    store.currentSource = source()
    vi.mocked(API.deleteJdbcSource).mockResolvedValue(ok() as never)

    await store.deleteSource('mysql', 'v1')

    expect(store.currentSource).toBeNull()
    expect(API.deleteJdbcSource).toHaveBeenCalledWith('mysql', 'v1')
  })
})

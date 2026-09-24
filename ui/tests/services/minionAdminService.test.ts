import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { AxiosError, AxiosHeaders } from 'axios'
import { NODE_LOOKUP_CHUNK, deleteMinion, getMinionNodeIds, isFiqlSafeId, listMinions, updateMinion } from '@/services/minionAdminService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({ v2: { get: vi.fn(), put: vi.fn(), delete: vi.fn() }}))

const http = (status: number) => {
  const e = new AxiosError('x')
  e.response = { status, data: '', statusText: '', headers: {}, config: { headers: new AxiosHeaders() }}
  return e
}

const minion = (id: string, location = 'Default') => ({ id, label: id, location, type: 'Minion', status: 'up', version: '1', properties: {}}) as any

describe('minionAdminService', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  it('listMinions fetches a bounded page (not limit=0), reports the total, and maps 204 to empty', async () => {
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: { minion: [{ id: 'm1' }], totalCount: 7 }} as any)
    expect(await listMinions()).toEqual({ minions: [{ id: 'm1' }], totalCount: 7 })
    expect(vi.mocked(v2.get).mock.calls[0][0]).not.toContain('limit=0')

    vi.mocked(v2.get).mockResolvedValueOnce({ status: 204 } as any)
    expect(await listMinions()).toEqual({ minions: [], totalCount: 0 })
  })

  it('listMinions returns null on failure', async () => {
    vi.mocked(v2.get).mockRejectedValue(http(500))
    expect(await listMinions()).toBeNull()
  })

  it('getMinionNodeIds ORs foreignId== per minion and maps node ids by id+location', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { node: [
      { id: '100', foreignId: 'm1', location: 'Default' },
      { id: '101', foreignId: 'm2', location: 'RemoteA' }
    ] }} as any)
    const map = await getMinionNodeIds([minion('m1'), minion('m2', 'RemoteA')])
    const url = vi.mocked(v2.get).mock.calls[0][0] as string
    expect(decodeURIComponent(url)).toContain('(foreignId==m1,foreignId==m2)')
    expect(map).toEqual({ 'm1\u0000Default': 100, 'm2\u0000RemoteA': 101 })
  })

  it('getMinionNodeIds leaves out ids that would alter the FIQL query', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 204 } as any)
    await getMinionNodeIds([minion('ok-1'), minion('a,b'), minion('x;y'), minion('p(q)'), minion('has space'), minion('a=b')])
    expect(v2.get).toHaveBeenCalledTimes(1)
    expect(decodeURIComponent(vi.mocked(v2.get).mock.calls[0][0] as string)).toContain('(foreignId==ok-1)')
    expect(isFiqlSafeId('6b1f0d9a-2c4e-4f7b-9a1d-0e5c8b7a6f21')).toBe(true)
    expect(isFiqlSafeId('a,b')).toBe(false)
  })

  it('getMinionNodeIds chunks the lookup so the URL stays short, and keeps the chunks that succeed', async () => {
    const many = Array.from({ length: NODE_LOOKUP_CHUNK * 2 + 1 }, (_, i) => minion(`m${i}`))
    vi.mocked(v2.get).mockImplementation(async (url: string) => {
      const ids = [...decodeURIComponent(url).matchAll(/foreignId==([^,)]+)/g)].map(m => m[1])
      if (ids.includes('m0')) {
        throw http(500)
      }
      return { status: 200, data: { node: ids.map((id, i) => ({ id: String(1000 + i), foreignId: id, location: 'Default' })) }} as any
    })
    const map = await getMinionNodeIds(many)
    expect(v2.get).toHaveBeenCalledTimes(3)
    for (const call of vi.mocked(v2.get).mock.calls) {
      expect((call[0] as string).length).toBeLessThan(4000)
    }
    expect(map['m0\u0000Default']).toBeUndefined()
    expect(map[`m${NODE_LOOKUP_CHUNK}\u0000Default`]).toBe(1000)
    expect(map[`m${NODE_LOOKUP_CHUNK * 2}\u0000Default`]).toBe(1000)
  })

  it('getMinionNodeIds is best-effort — no minions or a failure yields an empty map', async () => {
    expect(await getMinionNodeIds([])).toEqual({})
    vi.mocked(v2.get).mockRejectedValue(http(500))
    expect(await getMinionNodeIds([minion('m1')])).toEqual({})
  })

  it('updateMinion reads the current row and changes only label/location/properties', async () => {
    // fresh server row has a NEWER status than any client snapshot
    vi.mocked(v2.get).mockResolvedValue({ data: { id: 'm1', label: 'old', location: 'Default', type: 'Minion', status: 'DOWN', version: '2.0', date: 999, properties: {}}})
    vi.mocked(v2.put).mockResolvedValue({})

    const result = await updateMinion({ id: 'm1', label: 'new label', location: 'RemoteA', properties: { k: 'v' }})
    expect(result.success).toBe(true)

    const [, body] = vi.mocked(v2.put).mock.calls[0]
    expect(body).toMatchObject({
      id: 'm1', label: 'new label', location: 'RemoteA', properties: { k: 'v' },
      status: 'DOWN', version: '2.0', date: 999 // server-maintained fields from the FRESH read, not clobbered
    })
  })

  it('updateMinion returns a failure result with the server detail when it is a short plain message', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: { id: 'm1' }})
    const err = http(400)
    err.response!.data = 'Location does not exist'
    vi.mocked(v2.put).mockRejectedValue(err)
    expect(await updateMinion({ id: 'm1', label: null, location: 'Nope', properties: {}})).toEqual({ success: false, message: 'Location does not exist' })
  })

  it('updateMinion falls back to a generic message for an HTML error page', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: { id: 'm1' }})
    const err = http(500)
    err.response!.data = '<html><body>Server Error</body></html>'
    vi.mocked(v2.put).mockRejectedValue(err)
    const result = await updateMinion({ id: 'm1', label: 'One', location: 'Default', properties: {}})
    expect(result.success).toBe(false)
    expect(result.message).toBe('Failed to update minion \'One\'.')
  })

  it('deleteMinion treats a 404 (already deleted) as success', async () => {
    vi.mocked(v2.delete).mockRejectedValue(http(404))
    expect(await deleteMinion('gone').then(r => r.success)).toBe(true)
  })

  it('deleteMinion returns a failure result on a real failure', async () => {
    vi.mocked(v2.delete).mockRejectedValue(http(500))
    const result = await deleteMinion('m1')
    expect(result.success).toBe(false)
    expect(result.message).toBe('Failed to delete minion \'m1\'.')
  })
})

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { AxiosError, AxiosHeaders } from 'axios'
import { deleteMinion, getMinionNodeIds, listMinions, updateMinion } from '@/services/minionAdminService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({ v2: { get: vi.fn(), put: vi.fn(), delete: vi.fn() }}))
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar: vi.fn() }) }))
vi.mock('@/composables/useSpinner', () => ({ default: () => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() }) }))

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

  it('getMinionNodeIds is best-effort — no minions or a failure yields an empty map', async () => {
    expect(await getMinionNodeIds([])).toEqual({})
    vi.mocked(v2.get).mockRejectedValue(http(500))
    expect(await getMinionNodeIds([minion('m1')])).toEqual({})
  })

  it('updateMinion reads the current row and changes only label/location/properties', async () => {
    // fresh server row has a NEWER status than any client snapshot
    vi.mocked(v2.get).mockResolvedValue({ data: { id: 'm1', label: 'old', location: 'Default', type: 'Minion', status: 'DOWN', version: '2.0', date: 999, properties: {}}})
    vi.mocked(v2.put).mockResolvedValue({})

    await updateMinion({ id: 'm1', label: 'new label', location: 'RemoteA', properties: { k: 'v' }})

    const [, body] = vi.mocked(v2.put).mock.calls[0]
    expect(body).toMatchObject({
      id: 'm1', label: 'new label', location: 'RemoteA', properties: { k: 'v' },
      status: 'DOWN', version: '2.0', date: 999 // server-maintained fields from the FRESH read, not clobbered
    })
  })

  it('deleteMinion treats a 404 (already deleted) as success', async () => {
    vi.mocked(v2.delete).mockRejectedValue(http(404))
    expect(await deleteMinion('gone')).toBeNull()
  })

  it('deleteMinion returns the error message on a real failure', async () => {
    vi.mocked(v2.delete).mockRejectedValue(http(500))
    expect(await deleteMinion('m1')).toBeTruthy()
  })
})

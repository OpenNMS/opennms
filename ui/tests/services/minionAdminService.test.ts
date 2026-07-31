import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { AxiosError, AxiosHeaders } from 'axios'
import { deleteMinion, updateMinion } from '@/services/minionAdminService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({ v2: { get: vi.fn(), put: vi.fn(), delete: vi.fn() } }))

const http = (status: number) => {
  const e = new AxiosError('x')
  e.response = { status, data: '', statusText: '', headers: {}, config: { headers: new AxiosHeaders() } }
  return e
}

describe('minionAdminService', () => {
  beforeEach(() => vi.clearAllMocks())
  afterEach(() => vi.restoreAllMocks())

  it('updateMinion reads the current row and changes only label/location/properties', async () => {
    // fresh server row has a NEWER status than any client snapshot
    vi.mocked(v2.get).mockResolvedValue({ data: { id: 'm1', label: 'old', location: 'Default', type: 'Minion', status: 'DOWN', version: '2.0', date: 999, properties: {} } })
    vi.mocked(v2.put).mockResolvedValue({})

    await updateMinion({ id: 'm1', label: 'new label', location: 'RemoteA', properties: { k: 'v' } })

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

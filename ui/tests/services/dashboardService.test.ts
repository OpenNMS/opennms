import { beforeEach, describe, expect, it, vi } from 'vitest'
import { AxiosError, AxiosHeaders } from 'axios'
import { getSystemDashboard, saveSystemDashboard, getServiceTypes } from '@/services/dashboardService'
import { v2 } from '@/services/axiosInstances'
import { createDefaultLayout } from '@/components/Dashboard/defaultLayout'

vi.mock('@/services/axiosInstances', () => ({
  v2: { get: vi.fn(), put: vi.fn() }
}))

const axios404 = () => {
  const err = new AxiosError('not found')
  err.response = { status: 404, data: null, statusText: '', headers: {}, config: { headers: new AxiosHeaders() }}
  return err
}

describe('dashboardService', () => {
  beforeEach(() => vi.clearAllMocks())

  it('returns the stored layout when it looks valid', async () => {
    const layout = createDefaultLayout()
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: layout })
    expect(await getSystemDashboard()).toEqual(layout)
  })

  it('normalizes a partial document by filling missing blocks from defaults', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { panels: [] }})
    const result = await getSystemDashboard()
    // missing refresh/globalTimeframe/globalFilter must be present so store
    // getters never dereference undefined
    expect(result.refresh).toBeDefined()
    expect(typeof result.refresh.seconds).toBe('number')
    expect(result.globalTimeframe.preset).toBeTruthy()
    expect(result.globalFilter.surveillanceCategories).toEqual([])
    expect(Array.isArray(result.panels)).toBe(true)
    // a doc predating the squeeze option defaults to packed (true)
    expect(result.autoCompact).toBe(true)
  })

  it('preserves a stored autoCompact=false (free-form) layout', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { panels: [], autoCompact: false }})
    const result = await getSystemDashboard()
    expect(result.autoCompact).toBe(false)
  })

  it('drops malformed panels but keeps unknown types (rendered as missing, not lost on save)', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { panels: [
      { id: 'ok', type: 'notes', x: 0, y: 0, w: 3, h: 100 },
      { id: 'bad-type', type: 'no-such-panel', x: 0, y: 0, w: 3, h: 100 },
      { id: 'no-geometry', type: 'notes' },
      'garbage'
    ] }})
    const result = await getSystemDashboard()
    // unknown type is preserved so it isn't silently pruned and lost on the next save;
    // the structurally-malformed ones (missing geometry / non-object) are still dropped
    expect(result.panels.map(p => p.id)).toEqual(['ok', 'bad-type'])
  })

  it('falls back to the (empty) default on 404 (nothing saved yet)', async () => {
    vi.mocked(v2.get).mockRejectedValue(axios404())
    const result = await getSystemDashboard()
    expect(Array.isArray(result.panels)).toBe(true)
    expect(result.refresh).toBeDefined()
  })

  it('propagates non-404 failures instead of fabricating the default', async () => {
    vi.mocked(v2.get).mockRejectedValue(new Error('offline'))
    await expect(getSystemDashboard()).rejects.toThrow()
  })

  it('save propagates failures to the caller', async () => {
    vi.mocked(v2.put).mockRejectedValue(new Error('500'))
    await expect(saveSystemDashboard(createDefaultLayout())).rejects.toThrow()
  })

  it('service types returns empty on failure and passes data through', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: [{ id: 1, name: 'ICMP' }] })
    expect(await getServiceTypes()).toEqual([{ id: 1, name: 'ICMP' }])
    vi.mocked(v2.get).mockRejectedValue(new Error('boom'))
    expect(await getServiceTypes()).toEqual([])
  })
})

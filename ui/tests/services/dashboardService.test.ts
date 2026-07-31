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
  err.response = { status: 404, data: null, statusText: '', headers: {}, config: { headers: new AxiosHeaders() } }
  return err
}

describe('dashboardService', () => {
  beforeEach(() => vi.clearAllMocks())

  it('returns the stored layout when it looks valid', async () => {
    const layout = createDefaultLayout()
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: layout })
    expect(await getSystemDashboard()).toEqual(layout)
  })

  it('falls back to the default when the shape is wrong', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { panels: 'nope' } })
    const result = await getSystemDashboard()
    expect(Array.isArray(result.panels)).toBe(true)
    expect(result.panels.length).toBeGreaterThan(0)
  })

  it('falls back to the default on 404 (nothing saved yet)', async () => {
    vi.mocked(v2.get).mockRejectedValue(axios404())
    const result = await getSystemDashboard()
    expect(result.panels.length).toBeGreaterThan(0)
  })

  it('falls back to the default on network errors', async () => {
    vi.mocked(v2.get).mockRejectedValue(new Error('offline'))
    const result = await getSystemDashboard()
    expect(result.panels.length).toBeGreaterThan(0)
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

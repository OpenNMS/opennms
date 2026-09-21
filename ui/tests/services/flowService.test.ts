import { describe, it, expect, vi, beforeEach } from 'vitest'
import { rest } from '@/services/axiosInstances'
import { getFlowGraphUrl } from '@/services/flowService'

vi.mock('@/services/axiosInstances', () => ({ rest: { get: vi.fn() }}))

describe('getFlowGraphUrl', () => {
  beforeEach(() => vi.clearAllMocks())

  // The endpoint takes one interface at a time -- there is no bulk form.
  it('asks for one interface of one node', async () => {
    vi.mocked(rest.get).mockResolvedValue({ status: 200, data: { flowGraphUrl: 'http://grafana:3000/d/flows' }} as never)

    await getFlowGraphUrl('42', 14)

    expect(rest.get).toHaveBeenCalledWith('/flows/flowGraphUrl', {
      params: { exporterNode: '42', ifIndex: 14 }
    })
  })

  // The URL points at a separate tool, so it comes back absolute and must not be prefixed with
  // the OpenNMS baseHref.
  it('returns the resolved URL untouched', async () => {
    const flowGraphUrl = 'http://grafana:3000/dashboard/flows?node=42&interface=14'
    vi.mocked(rest.get).mockResolvedValue({ status: 200, data: { flowGraphUrl }} as never)

    expect(await getFlowGraphUrl('42', 14)).toBe(flowGraphUrl)
  })

  // flowGraphUrl is unconfigured by default: getFlowGraphUrlInfo returns null, which reaches the
  // client as 204 with no body.
  it('returns an empty string when the server has no flowGraphUrl configured', async () => {
    vi.mocked(rest.get).mockResolvedValue({ status: 204, data: '' } as never)

    expect(await getFlowGraphUrl('42', 14)).toBe('')
  })

  it('returns an empty string for a 200 carrying no URL', async () => {
    vi.mocked(rest.get).mockResolvedValue({ status: 200, data: {}} as never)

    expect(await getFlowGraphUrl('42', 14)).toBe('')
  })

  // "Not configured" and "the lookup failed" are different things to tell the user, so a failure
  // is not flattened into the empty-URL case here.
  it('propagates a request failure', async () => {
    vi.mocked(rest.get).mockRejectedValue(new Error('404'))

    await expect(getFlowGraphUrl('42', 14)).rejects.toThrow('404')
  })
})

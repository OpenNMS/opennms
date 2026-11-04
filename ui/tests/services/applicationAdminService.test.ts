///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { AxiosError, AxiosHeaders } from 'axios'
import {
  createApplication, deleteApplication, getApplicationMembers, listApplicationSummaries, searchServiceCandidates, updateApplicationMembers
} from '@/services/applicationAdminService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({ v2: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }}))

const http = (status: number, data: any = '') => {
  const e = new AxiosError('x')
  e.response = { status, data, statusText: '', headers: {}, config: { headers: new AxiosHeaders() }}
  return e
}

const service = { id: 7, nodeId: 1, nodeLabel: 'web-01', ipInterfaceId: 3, ipAddress: '10.0.0.5', serviceName: 'HTTP' }

describe('applicationAdminService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(console, 'error').mockImplementation(() => {})
  })
  afterEach(() => vi.restoreAllMocks())

  it('lists the summaries, and reports a failed load apart from an empty one', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: [{ id: 1, name: 'Web', serviceCount: 2, perspectiveLocations: [] }] })
    expect(await listApplicationSummaries()).toEqual({
      success: true, message: '', payload: [{ id: 1, name: 'Web', serviceCount: 2, perspectiveLocations: [] }]
    })
    expect(vi.mocked(v2.get).mock.calls[0][0]).toBe('/applications/summaries')

    vi.mocked(v2.get).mockRejectedValue(http(500))
    expect(await listApplicationSummaries()).toEqual({ success: false, message: 'Failed to load applications.', payload: undefined })
  })

  it('creates by name and surfaces a short 4xx message', async () => {
    vi.mocked(v2.post).mockResolvedValue({ headers: { location: 'http://host/opennms/api/v2/applications/42' }})
    expect(await createApplication('Web')).toEqual({ success: true, message: '', payload: 42 })
    expect(v2.post).toHaveBeenCalledWith('/applications', { name: 'Web' })
    vi.mocked(v2.post).mockResolvedValue({})
    expect(await createApplication('Web')).toEqual({ success: true, message: '', payload: undefined })

    vi.mocked(v2.post).mockRejectedValue(http(400, 'An application named Web already exists.'))
    expect(await createApplication('Web')).toEqual({ success: false, message: 'An application named Web already exists.' })
  })

  it('hides a 500 body or markup behind the fallback message', async () => {
    vi.mocked(v2.post).mockRejectedValue(http(500, 'could not execute statement; SQL [n/a]'))
    expect((await createApplication('Web')).message).toBe('Failed to create application \'Web\'.')
    vi.mocked(v2.delete).mockRejectedValue(http(404, '<html>nope</html>'))
    expect((await deleteApplication(3, 'Web')).message).toBe('Failed to delete application \'Web\'.')
  })

  it('deletes by id', async () => {
    vi.mocked(v2.delete).mockResolvedValue({})
    expect((await deleteApplication(3, 'Web')).success).toBe(true)
    expect(v2.delete).toHaveBeenCalledWith('/applications/3')
  })

  it('reads the members with their revision, defaulting missing lists, and reports a failure', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: { id: 3, name: 'Web', services: [service], revision: 'r1' }})
    expect((await getApplicationMembers(3)).payload).toEqual({ id: 3, name: 'Web', services: [service], perspectiveLocations: [], revision: 'r1' })
    expect(vi.mocked(v2.get).mock.calls[0][0]).toBe('/applications/3/members')

    vi.mocked(v2.get).mockRejectedValue(http(404))
    expect((await getApplicationMembers(3)).success).toBe(false)
  })

  it('puts the members and surfaces a rejection', async () => {
    vi.mocked(v2.put).mockResolvedValue({})
    const update = { serviceIds: [7], perspectiveLocations: ['RDU'] }
    expect((await updateApplicationMembers(3, 'Web', update)).success).toBe(true)
    expect(v2.put).toHaveBeenCalledWith('/applications/3/members', update)

    vi.mocked(v2.put).mockRejectedValue(http(409, 'Application Web was changed by someone else after you opened it. Reopen it to see the current members.'))
    expect((await updateApplicationMembers(3, 'Web', update)).message).toBe('Application Web was changed by someone else after you opened it. Reopen it to see the current members.')
  })

  it('returns the services the server reports as gone, with its message', async () => {
    const message = 'A selected service no longer exists. Remove it and save again.'
    vi.mocked(v2.put).mockRejectedValue(http(400, { message, missingServiceIds: [7] }))
    expect(await updateApplicationMembers(3, 'Web', { serviceIds: [7] })).toEqual({ success: false, message, payload: [7] })
  })

  it('searches candidates with the text and limit as query parameters', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: { totalCount: 120, services: [service] }})
    expect((await searchServiceCandidates('web', 50)).payload).toEqual({ totalCount: 120, services: [service] })
    expect(v2.get).toHaveBeenCalledWith('/applications/service-candidates', { params: { search: 'web', limit: 50 }})

    vi.mocked(v2.get).mockRejectedValue(http(500))
    expect((await searchServiceCandidates('web', 50)).success).toBe(false)
  })
})

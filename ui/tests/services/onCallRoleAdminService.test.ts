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
import axios from 'axios'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import * as svc from '@/services/onCallRoleAdminService'
import { v2 } from '@/services/axiosInstances'
import { adminWriteSuccess } from '@/types/adminWrite'
import { ManagedOnCallRole, ManagedOnCallRoleCreate } from '@/types/onCallRoleAdmin'

// feedback belongs to the components: the service must never show any
const showSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))
const startSpinner = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSpinner', () => ({ default: () => ({ startSpinner, stopSpinner: vi.fn() }) }))

vi.mock('@/services/axiosInstances', async () => {
  const actual = await vi.importActual<any>('@/services/axiosInstances')
  return {
    ifMatch: actual.ifMatch,
    rest: { get: vi.fn() },
    v2: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
  }
})

const IF_MATCH_7 = { headers: { 'If-Match': '"7"' }}
const UNAVAILABLE = 'User management through this API needs org.opennms.usermgmt.store=db.'

const requestedUri = (call: any[], configIndex: number) => axios.getUri({ url: call[0], params: call[configIndex]?.params })

const draft: ManagedOnCallRoleCreate = { name: 'Ops Rota', membershipGroup: 'NOC', supervisor: 'admin', schedules: [] }

describe('onCallRoleAdminService reads', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('returns the roles as the payload', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: [{ name: 'Ops Rota', version: 1 }] })

    expect(await svc.getManagedOnCallRoles()).toEqual({ success: true, message: '', payload: [{ name: 'Ops Rota', version: 1 }] })
    expect(v2.get).toHaveBeenCalledWith('/oncall-roles')
  })

  it('fails a role load with the server text, else a default', async () => {
    vi.mocked(v2.get).mockRejectedValue({ response: { status: 503, data: UNAVAILABLE }})
    expect(await svc.getManagedOnCallRoles()).toEqual(expect.objectContaining({ success: false, message: UNAVAILABLE }))

    vi.mocked(v2.get).mockRejectedValue(new Error('Network Error'))
    expect(await svc.getManagedOnCallRoles()).toEqual(expect.objectContaining({ success: false, message: 'Failed to load on-call roles.' }))
  })

  it('reads the schedule settings', async () => {
    const settings = { timeZone: 'America/New_York', shortMonths: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'] }
    vi.mocked(v2.get).mockResolvedValue({ data: settings })

    expect(await svc.getOnCallScheduleSettings()).toEqual({ success: true, message: '', payload: settings })
    expect(v2.get).toHaveBeenCalledWith('/oncall-roles/schedule-settings')

    vi.mocked(v2.get).mockRejectedValue(new Error('Network Error'))
    expect(await svc.getOnCallScheduleSettings()).toEqual(expect.objectContaining({ success: false, message: 'Failed to load the schedule settings.' }))
  })
})

describe('onCallRoleAdminService calendar preview', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('posts the draft to the calendar endpoint with the month as a param', async () => {
    const calendar = { timeZone: 'UTC', month: '2026-11', entries: [] }
    vi.mocked(v2.post).mockResolvedValue({ data: calendar })

    expect(await svc.previewOnCallCalendar(draft, '2026-11')).toEqual({ success: true, message: '', payload: calendar })
    expect(v2.post).toHaveBeenCalledWith('/oncall-roles/calendar', draft, { params: { month: '2026-11' }})
  })

  it('fails with the server text, else a default', async () => {
    vi.mocked(v2.post).mockRejectedValue({ response: { status: 400, data: 'Bad schedule.' }})
    expect(await svc.previewOnCallCalendar(draft, '2026-11')).toEqual(expect.objectContaining({ success: false, message: 'Bad schedule.' }))

    vi.mocked(v2.post).mockRejectedValue(new Error('Network Error'))
    expect(await svc.previewOnCallCalendar(draft, '2026-11')).toEqual(expect.objectContaining({ success: false, message: 'Failed to preview the calendar.' }))
  })
})

describe('onCallRoleAdminService writes', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('creates by posting the role to the collection', async () => {
    vi.mocked(v2.post).mockResolvedValue({ data: {}})

    expect(await svc.createManagedOnCallRole(draft)).toEqual(adminWriteSuccess())
    expect(v2.post).toHaveBeenCalledWith('/oncall-roles', draft)
  })

  it('sends If-Match with the version on update/rename/delete', async () => {
    vi.mocked(v2.put).mockResolvedValue({ data: {}})
    vi.mocked(v2.post).mockResolvedValue({ data: {}})
    vi.mocked(v2.delete).mockResolvedValue({ data: {}})
    const role = { ...draft, version: 7 } as ManagedOnCallRole

    expect(await svc.updateManagedOnCallRole(role)).toEqual(adminWriteSuccess())
    expect(v2.put).toHaveBeenCalledWith('/oncall-roles/Ops%20Rota', role, IF_MATCH_7)

    await svc.renameManagedOnCallRole('Ops Rota', 'New', 7)
    expect(v2.post).toHaveBeenCalledWith('/oncall-roles/Ops%20Rota/rename', { newName: 'New' }, IF_MATCH_7)

    expect(await svc.deleteManagedOnCallRole('Ops Rota', 7)).toEqual(adminWriteSuccess())
    expect(v2.delete).toHaveBeenCalledWith('/oncall-roles/Ops%20Rota', IF_MATCH_7)
  })

  it('on 412 returns the stale flag, whatever the body says', async () => {
    vi.mocked(v2.delete).mockRejectedValue({ response: { status: 412, data: 'Precondition failed.' }})
    expect((await svc.deleteManagedOnCallRole('x', 3))?.stale).toBe(true)
  })

  it('on 409 returns the server message', async () => {
    vi.mocked(v2.post).mockRejectedValue({ response: { status: 409, data: 'Role \'y\' already exists.' }})
    expect(await svc.renameManagedOnCallRole('x', 'y', 3)).toEqual({ success: false, message: 'Role \'y\' already exists.', stale: false })
  })

  it('falls back to a default message', async () => {
    vi.mocked(v2.post).mockRejectedValue(new Error('Network Error'))
    expect(await svc.createManagedOnCallRole(draft)).toEqual(expect.objectContaining({ success: false, message: 'Failed to create on-call role \'Ops Rota\'.' }))
  })
})

describe('onCallRoleAdminService item URLs', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(v2.put).mockResolvedValue({ data: {}})
    vi.mocked(v2.post).mockResolvedValue({ data: {}})
    vi.mocked(v2.delete).mockResolvedValue({ data: {}})
  })

  it('reaches a name the path cannot carry through the query form on every per-role call', async () => {
    await svc.updateManagedOnCallRole({ ...draft, name: 'a/b', version: 7 } as ManagedOnCallRole)
    expect(requestedUri(vi.mocked(v2.put).mock.lastCall!, 2)).toBe('/oncall-roles/item?name=a%2Fb')

    await svc.renameManagedOnCallRole('a/b', 'ab', 7)
    expect(v2.post).toHaveBeenLastCalledWith('/oncall-roles/item/rename', { newName: 'ab' }, { ...IF_MATCH_7, params: { name: 'a/b' }})
    expect(requestedUri(vi.mocked(v2.post).mock.lastCall!, 2)).toBe('/oncall-roles/item/rename?name=a%2Fb')

    await svc.deleteManagedOnCallRole('a/b', 7)
    expect(v2.delete).toHaveBeenLastCalledWith('/oncall-roles/item', { ...IF_MATCH_7, params: { name: 'a/b' }})
  })

  // the path form would hit the endpoint of the same name
  it.each(['item', 'calendar', 'schedule-settings'])('reaches a role named %s through the query form', async (name) => {
    await svc.updateManagedOnCallRole({ ...draft, name, version: 7 } as ManagedOnCallRole)
    expect(v2.put).toHaveBeenLastCalledWith('/oncall-roles/item', { ...draft, name, version: 7 }, { ...IF_MATCH_7, params: { name }})

    await svc.deleteManagedOnCallRole(name, 7)
    expect(v2.delete).toHaveBeenLastCalledWith('/oncall-roles/item', { ...IF_MATCH_7, params: { name }})
  })
})

describe('onCallRoleAdminService feedback', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows no snackbar or spinner on any path', async () => {
    const failures = [{ response: { status: 412, data: '' }}, { response: { status: 409, data: 'Conflict' }}, new Error('Network Error')]
    const calls = [
      () => svc.getManagedOnCallRoles(),
      () => svc.getOnCallScheduleSettings(),
      () => svc.previewOnCallCalendar(draft, '2026-11'),
      () => svc.createManagedOnCallRole(draft),
      () => svc.updateManagedOnCallRole({ ...draft, version: 1 }),
      () => svc.renameManagedOnCallRole('x', 'y', 1),
      () => svc.deleteManagedOnCallRole('x', 1)
    ]
    for (const mock of [v2.get, v2.post, v2.put, v2.delete]) {
      vi.mocked(mock).mockResolvedValue({ data: [] })
    }
    for (const call of calls) {
      await call()
    }
    for (const failure of failures) {
      for (const mock of [v2.get, v2.post, v2.put, v2.delete]) {
        vi.mocked(mock).mockRejectedValue(failure)
      }
      for (const call of calls) {
        await call()
      }
    }

    expect(showSnackBar).not.toHaveBeenCalled()
    expect(startSpinner).not.toHaveBeenCalled()
  })
})

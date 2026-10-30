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
import * as svc from '@/services/userAdminService'
import { v2 } from '@/services/axiosInstances'
import { adminWriteSuccess } from '@/types/adminWrite'

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

// the URL a mocked axios call would have requested: its path plus the query built from its params
const requestedUri = (call: any[], configIndex: number) => axios.getUri({ url: call[0], params: call[configIndex]?.params })

describe('userAdminService versions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('sends If-Match with the version on update/rename/delete/password', async () => {
    vi.mocked(v2.put).mockResolvedValue({ data: {}})
    vi.mocked(v2.post).mockResolvedValue({ data: {}})
    vi.mocked(v2.delete).mockResolvedValue({ data: {}})

    await svc.updateManagedUser({ userId: 'My User', version: 7 } as any)
    expect(v2.put).toHaveBeenCalledWith('/users/My%20User', { userId: 'My User', version: 7 }, IF_MATCH_7)

    await svc.renameManagedUser('My User', 'New', 7)
    expect(v2.post).toHaveBeenCalledWith('/users/My%20User/rename', { newUserId: 'New' }, IF_MATCH_7)

    expect(await svc.deleteManagedUser('My User', 7)).toEqual(adminWriteSuccess())
    expect(v2.delete).toHaveBeenCalledWith('/users/My%20User', IF_MATCH_7)

    await svc.setManagedUserPassword('My User', 'pw', 7)
    expect(v2.put).toHaveBeenLastCalledWith('/users/My%20User/password', { password: 'pw' }, IF_MATCH_7)
  })

  it('on 412 returns the stale flag, whatever the body says', async () => {
    vi.mocked(v2.delete).mockRejectedValue({ response: { status: 412, data: 'Precondition failed.' }})
    const error = await svc.deleteManagedUser('x', 3)
    expect(error?.stale).toBe(true)
  })

  it('on 409 returns the server message', async () => {
    vi.mocked(v2.post).mockRejectedValue({ response: { status: 409, data: 'User \'y\' already exists.' }})
    expect(await svc.renameManagedUser('x', 'y', 3)).toEqual({ success: false, message: 'User \'y\' already exists.', stale: false })
  })
})

describe('userAdminService item URLs', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(v2.put).mockResolvedValue({ data: {}})
    vi.mocked(v2.post).mockResolvedValue({ data: {}})
    vi.mocked(v2.delete).mockResolvedValue({ data: {}})
  })

  // a migrated user-id the path form cannot carry
  it('reaches DOMAIN\\jdoe through the query form on every per-user call', async () => {
    await svc.updateManagedUser({ userId: 'DOMAIN\\jdoe', version: 7 } as any)
    expect(v2.put).toHaveBeenLastCalledWith('/users/item', { userId: 'DOMAIN\\jdoe', version: 7 }, { ...IF_MATCH_7, params: { userId: 'DOMAIN\\jdoe' }})
    expect(requestedUri(vi.mocked(v2.put).mock.lastCall!, 2)).toBe('/users/item?userId=DOMAIN%5Cjdoe')

    await svc.setManagedUserPassword('DOMAIN\\jdoe', 'pw', 7)
    expect(v2.put).toHaveBeenLastCalledWith('/users/item/password', { password: 'pw' }, { ...IF_MATCH_7, params: { userId: 'DOMAIN\\jdoe' }})
    expect(requestedUri(vi.mocked(v2.put).mock.lastCall!, 2)).toBe('/users/item/password?userId=DOMAIN%5Cjdoe')

    await svc.renameManagedUser('DOMAIN\\jdoe', 'jdoe', 7)
    expect(v2.post).toHaveBeenLastCalledWith('/users/item/rename', { newUserId: 'jdoe' }, { ...IF_MATCH_7, params: { userId: 'DOMAIN\\jdoe' }})
    expect(requestedUri(vi.mocked(v2.post).mock.lastCall!, 2)).toBe('/users/item/rename?userId=DOMAIN%5Cjdoe')

    await svc.deleteManagedUser('DOMAIN\\jdoe', 7)
    expect(v2.delete).toHaveBeenLastCalledWith('/users/item', { ...IF_MATCH_7, params: { userId: 'DOMAIN\\jdoe' }})
    expect(requestedUri(vi.mocked(v2.delete).mock.lastCall!, 1)).toBe('/users/item?userId=DOMAIN%5Cjdoe')
  })

  // the path form would hit the endpoint of the same name
  it.each(['item', 'available-roles'])('reaches a user named %s through the query form', async (userId) => {
    await svc.updateManagedUser({ userId, version: 7 } as any)
    expect(v2.put).toHaveBeenLastCalledWith('/users/item', { userId, version: 7 }, { ...IF_MATCH_7, params: { userId }})

    await svc.deleteManagedUser(userId, 7)
    expect(v2.delete).toHaveBeenLastCalledWith('/users/item', { ...IF_MATCH_7, params: { userId }})
  })
})

describe('userAdminService reads', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('returns the users as the payload', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: [{ userId: 'admin', version: 1 }] })

    expect(await svc.getManagedUsers()).toEqual({ success: true, message: '', payload: [{ userId: 'admin', version: 1 }] })
    expect(v2.get).toHaveBeenCalledWith('/users')
  })

  it('fails a user load with the server text', async () => {
    vi.mocked(v2.get).mockRejectedValue({ response: { status: 503, data: UNAVAILABLE }})

    expect(await svc.getManagedUsers()).toEqual(expect.objectContaining({ success: false, message: UNAVAILABLE }))
  })

  it('fails a user load that got an HTML error page with the default, not the page', async () => {
    vi.mocked(v2.get).mockRejectedValue({ response: { status: 502, data: '<html><body><h2>HTTP ERROR 502</h2></body></html>', headers: { 'content-type': 'text/html' }}})

    expect(await svc.getManagedUsers()).toEqual(expect.objectContaining({ success: false, message: 'Failed to load users.' }))
  })

  it('fails a user load without a server text with a default', async () => {
    vi.mocked(v2.get).mockRejectedValue(new Error('Network Error'))

    expect(await svc.getManagedUsers()).toEqual(expect.objectContaining({ success: false, message: 'Failed to load users.' }))
  })

  it('returns the available roles, and fails their load with the server text', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: ['ROLE_ADMIN'] })
    expect(await svc.getAvailableUserRoles()).toEqual({ success: true, message: '', payload: ['ROLE_ADMIN'] })
    expect(v2.get).toHaveBeenCalledWith('/users/available-roles')

    vi.mocked(v2.get).mockRejectedValue({ response: { status: 503, data: UNAVAILABLE }})
    expect(await svc.getAvailableUserRoles()).toEqual(expect.objectContaining({ success: false, message: UNAVAILABLE }))
  })
})

describe('userAdminService feedback', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows no snackbar or spinner on any path', async () => {
    const ok = { data: [] }
    const failures = [{ response: { status: 412, data: '' }}, { response: { status: 409, data: 'Conflict' }}, { response: { status: 503, data: UNAVAILABLE }}, new Error('Network Error')]
    const calls = [
      () => svc.getManagedUsers(),
      () => svc.getAvailableUserRoles(),
      () => svc.createManagedUser({ userId: 'x', password: 'pw' }),
      () => svc.updateManagedUser({ userId: 'x', version: 1 }),
      () => svc.setManagedUserPassword('x', 'pw', 1),
      () => svc.renameManagedUser('x', 'y', 1),
      () => svc.deleteManagedUser('x', 1)
    ]
    for (const mock of [v2.get, v2.post, v2.put, v2.delete]) {
      vi.mocked(mock).mockResolvedValue(ok)
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

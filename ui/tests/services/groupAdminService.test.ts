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
import * as svc from '@/services/groupAdminService'
import { rest, v2 } from '@/services/axiosInstances'

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

describe('groupAdminService member candidates', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('reads the user ids from the v2 users list', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: [{ userId: 'admin', version: 1 }, { userId: 'DOMAIN\\jdoe', version: 2 }] })

    expect(await svc.getGroupMemberCandidates()).toEqual({ success: true, message: '', payload: ['admin', 'DOMAIN\\jdoe'] })
    expect(v2.get).toHaveBeenCalledWith('/users')
    expect(rest.get).not.toHaveBeenCalled()
  })

  it('fails with the server text', async () => {
    vi.mocked(v2.get).mockRejectedValue({ response: { status: 503, data: UNAVAILABLE }})

    expect(await svc.getGroupMemberCandidates()).toEqual(expect.objectContaining({ success: false, message: UNAVAILABLE }))
  })

  it('fails without a server text with a default', async () => {
    vi.mocked(v2.get).mockRejectedValue(new Error('Network Error'))

    expect(await svc.getGroupMemberCandidates()).toEqual(expect.objectContaining({ success: false, message: 'Failed to load users.' }))
  })
})

describe('groupAdminService versions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('sends If-Match with the version on update/rename/delete', async () => {
    vi.mocked(v2.put).mockResolvedValue({ data: {}})
    vi.mocked(v2.post).mockResolvedValue({ data: {}})
    vi.mocked(v2.delete).mockResolvedValue({ data: {}})

    await svc.updateManagedGroup({ name: 'My Group', version: 7 } as any)
    expect(v2.put).toHaveBeenCalledWith('/groups/My%20Group', { name: 'My Group', version: 7 }, IF_MATCH_7)

    await svc.renameManagedGroup('My Group', 'New', 7)
    expect(v2.post).toHaveBeenCalledWith('/groups/My%20Group/rename', { newName: 'New' }, IF_MATCH_7)

    await svc.deleteManagedGroup('My Group', 7)
    expect(v2.delete).toHaveBeenCalledWith('/groups/My%20Group', IF_MATCH_7)
  })

  it('on 412 returns the stale flag, whatever the body says', async () => {
    vi.mocked(v2.delete).mockRejectedValue({ response: { status: 412, data: 'Precondition failed.' }})
    const error = await svc.deleteManagedGroup('x', 3)
    expect(error?.stale).toBe(true)
  })

  it('on 409 returns the server message', async () => {
    vi.mocked(v2.post).mockRejectedValue({ response: { status: 409, data: 'Group \'y\' already exists.' }})
    expect(await svc.renameManagedGroup('x', 'y', 3)).toEqual({ message: 'Group \'y\' already exists.', stale: false })
  })
})

describe('groupAdminService item URLs', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(v2.put).mockResolvedValue({ data: {}})
    vi.mocked(v2.post).mockResolvedValue({ data: {}})
    vi.mocked(v2.delete).mockResolvedValue({ data: {}})
  })

  it('reaches a name the path cannot carry through the query form on every per-group call', async () => {
    await svc.updateManagedGroup({ name: 'a/b', version: 7 } as any)
    expect(v2.put).toHaveBeenLastCalledWith('/groups/item', { name: 'a/b', version: 7 }, { ...IF_MATCH_7, params: { name: 'a/b' }})
    expect(requestedUri(vi.mocked(v2.put).mock.lastCall!, 2)).toBe('/groups/item?name=a%2Fb')

    await svc.renameManagedGroup('a/b', 'ab', 7)
    expect(v2.post).toHaveBeenLastCalledWith('/groups/item/rename', { newName: 'ab' }, { ...IF_MATCH_7, params: { name: 'a/b' }})
    expect(requestedUri(vi.mocked(v2.post).mock.lastCall!, 2)).toBe('/groups/item/rename?name=a%2Fb')

    await svc.deleteManagedGroup('a/b', 7)
    expect(v2.delete).toHaveBeenLastCalledWith('/groups/item', { ...IF_MATCH_7, params: { name: 'a/b' }})
    expect(requestedUri(vi.mocked(v2.delete).mock.lastCall!, 1)).toBe('/groups/item?name=a%2Fb')
  })

  // the path form would hit the endpoint of the same name
  it.each(['item', 'available-categories'])('reaches a group named %s through the query form', async (name) => {
    await svc.updateManagedGroup({ name, version: 7 } as any)
    expect(v2.put).toHaveBeenLastCalledWith('/groups/item', { name, version: 7 }, { ...IF_MATCH_7, params: { name }})

    await svc.renameManagedGroup(name, 'other', 7)
    expect(v2.post).toHaveBeenLastCalledWith('/groups/item/rename', { newName: 'other' }, { ...IF_MATCH_7, params: { name }})

    await svc.deleteManagedGroup(name, 7)
    expect(v2.delete).toHaveBeenLastCalledWith('/groups/item', { ...IF_MATCH_7, params: { name }})
  })
})

describe('groupAdminService reads', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('returns the groups as the payload', async () => {
    vi.mocked(v2.get).mockResolvedValue({ data: [{ name: 'Admin', version: 1 }] })

    expect(await svc.getManagedGroups()).toEqual({ success: true, message: '', payload: [{ name: 'Admin', version: 1 }] })
    expect(v2.get).toHaveBeenCalledWith('/groups')
  })

  it('fails a group load with the server text, else a default', async () => {
    vi.mocked(v2.get).mockRejectedValue({ response: { status: 503, data: UNAVAILABLE }})
    expect(await svc.getManagedGroups()).toEqual(expect.objectContaining({ success: false, message: UNAVAILABLE }))

    vi.mocked(v2.get).mockRejectedValue(new Error('Network Error'))
    expect(await svc.getManagedGroups()).toEqual(expect.objectContaining({ success: false, message: 'Failed to load groups.' }))
  })
})

describe('groupAdminService feedback', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('shows no snackbar or spinner on any path', async () => {
    const failures = [{ response: { status: 412, data: '' }}, { response: { status: 409, data: 'Conflict' }}, { response: { status: 503, data: UNAVAILABLE }}, new Error('Network Error')]
    const calls = [
      () => svc.getManagedGroups(),
      () => svc.getGroupMemberCandidates(),
      () => svc.createManagedGroup({ name: 'x' }),
      () => svc.updateManagedGroup({ name: 'x', version: 1 }),
      () => svc.renameManagedGroup('x', 'y', 1),
      () => svc.deleteManagedGroup('x', 1)
    ]
    for (const mock of [v2.get, v2.post, v2.put, v2.delete, rest.get]) {
      vi.mocked(mock).mockResolvedValue({ data: [] })
    }
    for (const call of calls) {
      await call()
    }
    for (const failure of failures) {
      for (const mock of [v2.get, v2.post, v2.put, v2.delete, rest.get]) {
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

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
import { describe, it, expect, beforeEach, vi } from 'vitest'
import * as svc from '@/services/userAdminService'
import { v2 } from '@/services/axiosInstances'

const showSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))

vi.mock('@/services/axiosInstances', async () => {
  const actual = await vi.importActual<any>('@/services/axiosInstances')
  return {
    ifMatch: actual.ifMatch,
    rest: { get: vi.fn() },
    v2: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
  }
})

const IF_MATCH_7 = { headers: { 'If-Match': '"7"' }}

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

    await svc.deleteManagedUser('My User', 7)
    expect(v2.delete).toHaveBeenCalledWith('/users/My%20User', IF_MATCH_7)

    await svc.setManagedUserPassword('My User', 'pw', 7)
    expect(v2.put).toHaveBeenLastCalledWith('/users/My%20User/password', { password: 'pw' }, IF_MATCH_7)
  })

  it('on 412 returns the stale flag, whatever the body says, and shows no snackbar', async () => {
    vi.mocked(v2.delete).mockRejectedValue({ response: { status: 412, data: 'Precondition failed.' }})
    const error = await svc.deleteManagedUser('x', 3)
    expect(error?.stale).toBe(true)
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('on 409 returns the server message', async () => {
    vi.mocked(v2.post).mockRejectedValue({ response: { status: 409, data: 'User \'y\' already exists.' }})
    expect(await svc.renameManagedUser('x', 'y', 3)).toEqual({ message: 'User \'y\' already exists.', stale: false })
  })
})

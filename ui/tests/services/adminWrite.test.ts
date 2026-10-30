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

import { describe, expect, it, vi } from 'vitest'

import { adminWriteError, reloadAfterWrite } from '@/services/adminWrite'
import { adminWriteSuccess } from '@/types/adminWrite'

const STALE = { reloaded: 'Changed by someone else; reloaded.', reloadFailed: 'Changed by someone else; reload failed.' }
const httpError = (status: number, data: unknown = '') => ({ response: { status, data, headers: { 'content-type': 'text/plain' }}})

describe('adminWriteError', () => {
  it('flags a 412 as stale, with no message', () => {
    expect(adminWriteError(httpError(412, 'Precondition failed'), 'Failed.')).toEqual({ success: false, message: '', stale: true })
  })

  it('carries the server text of any other failure', () => {
    expect(adminWriteError(httpError(400, 'Unknown category: Gone'), 'Failed.'))
      .toEqual({ success: false, message: 'Unknown category: Gone', stale: false })
  })

  it('falls back when the server gave no text', () => {
    expect(adminWriteError(new Error('Network Error'), 'Failed to save.')).toEqual({ success: false, message: 'Failed to save.', stale: false })
  })
})

describe('reloadAfterWrite', () => {
  it('reloads after a success, and keeps the result when the reload works', async () => {
    const reload = vi.fn().mockResolvedValue({ success: true, message: '' })
    expect(await reloadAfterWrite(adminWriteSuccess(), reload, STALE)).toEqual(adminWriteSuccess())
    expect(reload).toHaveBeenCalledTimes(1)
  })

  it('puts a failed reload after a success in the errors', async () => {
    const reload = vi.fn().mockResolvedValue({ success: false, message: 'Failed to load.' })
    expect(await reloadAfterWrite(adminWriteSuccess(), reload, STALE)).toEqual({ ...adminWriteSuccess(), errors: ['Failed to load.'] })
  })

  it('reloads after a stale write and words the message by the reload', async () => {
    const stale = { success: false, message: '', stale: true }
    expect(await reloadAfterWrite(stale, vi.fn().mockResolvedValue({ success: true, message: '' }), STALE))
      .toEqual({ ...stale, message: STALE.reloaded })
    expect(await reloadAfterWrite(stale, vi.fn().mockResolvedValue({ success: false, message: 'x' }), STALE))
      .toEqual({ ...stale, message: STALE.reloadFailed })
  })

  it('leaves any other failure alone, without a reload', async () => {
    const failed = { success: false, message: 'Unknown user: x', stale: false }
    const reload = vi.fn()
    expect(await reloadAfterWrite(failed, reload, STALE)).toBe(failed)
    expect(reload).not.toHaveBeenCalled()
  })
})

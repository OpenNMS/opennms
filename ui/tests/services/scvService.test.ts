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

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { rest } from '@/services/axiosInstances'
import { addCredentials, getAliases, getAllCredentials, getCredentialsByAlias, updateCredentials } from '@/services/scvService'
import { SCV_GET_ALL_ALIAS } from '@/lib/constants'
import { SCVCredentials } from '@/types/scv'

vi.mock('@/services/axiosInstances', () => ({
  rest: { get: vi.fn(), post: vi.fn(), put: vi.fn() }
}))

const creds: SCVCredentials = { alias: 'a1', username: 'u', password: 'p', attributes: {}}

describe('scvService', () => {
  beforeEach(() => {
    vi.resetAllMocks()
  })

  it('getAliases returns the aliases as the payload', async () => {
    vi.mocked(rest.get).mockResolvedValue({ data: ['a', 'b'] })
    const result = await getAliases()
    expect(rest.get).toHaveBeenCalledWith('/scv')
    expect(result).toEqual({ success: true, message: '', payload: ['a', 'b'] })
  })

  it('getAliases failure carries the message', async () => {
    vi.mocked(rest.get).mockRejectedValue(new Error('boom'))
    const result = await getAliases()
    expect(result.success).toBe(false)
    expect(result.message).toBe('Failed to return aliases.')
    expect(result.payload).toBeUndefined()
  })

  it('getCredentialsByAlias returns the credentials as the payload', async () => {
    vi.mocked(rest.get).mockResolvedValue({ data: creds })
    const result = await getCredentialsByAlias('a1')
    expect(rest.get).toHaveBeenCalledWith('/scv/a1')
    expect(result).toEqual({ success: true, message: '', payload: creds })
  })

  it('getCredentialsByAlias failure carries the message', async () => {
    vi.mocked(rest.get).mockRejectedValue(new Error('boom'))
    const result = await getCredentialsByAlias('a1')
    expect(result.success).toBe(false)
    expect(result.message).toBe('Failed to retrieve credentials.')
  })

  it('getAllCredentials returns the list as the payload', async () => {
    vi.mocked(rest.get).mockResolvedValue({ data: [creds] })
    const result = await getAllCredentials()
    expect(rest.get).toHaveBeenCalledWith(`/scv/${SCV_GET_ALL_ALIAS}`)
    expect(result).toEqual({ success: true, message: '', payload: [creds] })
  })

  it('getAllCredentials failure carries the message', async () => {
    vi.mocked(rest.get).mockRejectedValue(new Error('boom'))
    const result = await getAllCredentials()
    expect(result.success).toBe(false)
    expect(result.message).toBe('Failed to retrieve credentials.')
  })

  it('addCredentials succeeds without a message', async () => {
    vi.mocked(rest.post).mockResolvedValue({ status: 200 })
    const result = await addCredentials(creds)
    expect(rest.post).toHaveBeenCalledWith('/scv', creds)
    expect(result.success).toBe(true)
  })

  it('addCredentials failure carries the message', async () => {
    vi.mocked(rest.post).mockRejectedValue(new Error('boom'))
    expect(await addCredentials(creds)).toEqual({ success: false, message: 'Failed to add credentials.' })
  })

  it('updateCredentials succeeds without a message', async () => {
    vi.mocked(rest.put).mockResolvedValue({ status: 200 })
    const result = await updateCredentials(creds)
    expect(rest.put).toHaveBeenCalledWith('/scv/a1', creds)
    expect(result.success).toBe(true)
  })

  it('updateCredentials failure carries the message', async () => {
    vi.mocked(rest.put).mockRejectedValue(new Error('boom'))
    expect(await updateCredentials(creds)).toEqual({ success: false, message: 'Failed to update credentials.' })
  })
})

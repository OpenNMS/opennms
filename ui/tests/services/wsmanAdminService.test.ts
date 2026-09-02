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
import { getWsmanConfig, updateWsmanConfig } from '@/services/wsmanAdminService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: { get: vi.fn(), put: vi.fn() }
}))
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: vi.fn() })
}))
vi.mock('@/composables/useSpinner', () => ({
  default: () => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() })
}))

describe('wsmanAdminService', () => {
  beforeEach(() => vi.clearAllMocks())

  it('reads /wsman-config and normalizes a missing definitions list', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { defaults: { username: 'root', hasPassword: true }}})
    const result = await getWsmanConfig()
    expect(vi.mocked(v2.get).mock.calls[0][0]).toBe('/wsman-config')
    expect(result).toEqual({ defaults: { username: 'root', hasPassword: true }, definitions: [] })
  })

  it('PUTs the document and returns null on success or the server reason on failure', async () => {
    const input = { defaults: { username: 'x', password: null, clearPassword: false }, definitions: [] } as any
    vi.mocked(v2.put).mockResolvedValueOnce({ status: 200, data: {}})
    expect(await updateWsmanConfig(input)).toBeNull()
    expect(vi.mocked(v2.put).mock.calls[0][0]).toBe('/wsman-config')
    expect(vi.mocked(v2.put).mock.calls[0][1]).toBe(input)

    vi.mocked(v2.put).mockRejectedValueOnce({ response: { status: 400, data: 'Definition 1 has a range whose end address is before its begin address.' }})
    expect(await updateWsmanConfig(input)).toContain('before its begin')
    // an HTML error page is never shown verbatim
    vi.mocked(v2.put).mockRejectedValueOnce({ response: { status: 500, data: '<html>boom</html>' }})
    expect(await updateWsmanConfig(input)).toBe('Failed to save the WS-Man configuration.')
  })

  it('returns null on failure or an unexpected body', async () => {
    vi.mocked(v2.get).mockRejectedValueOnce(new Error('403'))
    expect(await getWsmanConfig()).toBeNull()
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: '<html>' })
    expect(await getWsmanConfig()).toBeNull()
  })
})

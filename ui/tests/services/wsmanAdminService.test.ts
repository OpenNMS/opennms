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
import { getRequisitionNames, getWsmanConfig, getWsmanDataCollection, getWsmanReadiness, getWsmanStatus, resetWsmanDataCollection, runWsmanReadinessAction, syncWsmanDefinition, updateWsmanConfig, updateWsmanDataCollectionFile } from '@/services/wsmanAdminService'
import { rest, v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: { get: vi.fn(), put: vi.fn(), post: vi.fn() },
  rest: { get: vi.fn() }
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
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: { version: 'abc', defaults: { username: 'root', hasPassword: true }}})
    const result = await getWsmanConfig()
    expect(vi.mocked(v2.get).mock.calls[0][0]).toBe('/wsman-config')
    expect(result).toEqual({ version: 'abc', defaults: { username: 'root', hasPassword: true }, definitions: [] })
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

  it('reads the data collection sub-resource and normalizes missing lists', async () => {
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: { sources: ['wsman-datacollection-config.xml'], collections: [{ name: 'default' }] }})
    const result = await getWsmanDataCollection()
    expect(vi.mocked(v2.get).mock.calls[0][0]).toBe('/wsman-config/data-collection')
    expect(result).toEqual({ rrdRepository: null, sources: ['wsman-datacollection-config.xml'], versions: {}, collections: [{ name: 'default' }], groups: [], systemDefinitions: [] })
    vi.mocked(v2.get).mockRejectedValueOnce(new Error('500'))
    expect(await getWsmanDataCollection()).toBeNull()
  })

  it('PUTs a data collection file by name as a query parameter', async () => {
    const input = { version: 'v', rrdRepository: null, collections: [], groups: [], systemDefinitions: [] }
    vi.mocked(v2.put).mockResolvedValueOnce({ status: 200, data: {}})
    expect(await updateWsmanDataCollectionFile('custom.xml', input)).toBeNull()
    expect(vi.mocked(v2.put).mock.calls[0][0]).toBe('/wsman-config/data-collection?file=custom.xml')
    vi.mocked(v2.put).mockRejectedValueOnce({ response: { status: 409, data: 'custom.xml changed since it was loaded; reload the page and apply the change again.' }})
    expect(await updateWsmanDataCollectionFile('custom.xml', input)).toContain('changed since')
  })

  it('reads the status and returns null rather than zeros on failure', async () => {
    const status = { serviceName: 'WS-Man', servers: 1, definitions: [{ index: 0, servers: 1, responding: 1, down: 0, unpolled: 0, lastResponse: null }], defaults: { servers: 0, responding: 0, down: 0, unpolled: 0, lastResponse: null }}
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: status })
    expect(await getWsmanStatus()).toEqual(status)
    expect(vi.mocked(v2.get).mock.calls[0][0]).toBe('/wsman-config/status')
    vi.mocked(v2.get).mockRejectedValueOnce(new Error('500'))
    expect(await getWsmanStatus()).toBeNull()
  })

  it('posts a sync and returns the result or the server reason', async () => {
    const result = { requisition: 'windows', addedNodes: ['10.0.0.1'], existingNodes: 0, addedRanges: [], existingRanges: 0, skippedPatterns: [], importRequested: true, discoveryReloadRequested: false }
    vi.mocked(v2.post).mockResolvedValueOnce({ status: 200, data: result })
    expect(await syncWsmanDefinition(2)).toEqual(result)
    expect(vi.mocked(v2.post).mock.calls[0][0]).toBe('/wsman-config/definitions/2/sync')
    vi.mocked(v2.post).mockRejectedValueOnce({ response: { status: 400, data: 'Server definition 3 is not linked to a requisition.' }})
    expect(await syncWsmanDefinition(2)).toContain('not linked')
  })

  it('reads the requisition names from the v1 endpoint and tolerates failure', async () => {
    vi.mocked(rest.get).mockResolvedValueOnce({ status: 200, data: { count: 2, 'foreign-source': ['a', 'b'] }})
    expect(await getRequisitionNames()).toEqual(['a', 'b'])
    expect(vi.mocked(rest.get).mock.calls[0][0]).toBe('/requisitionNames')
    vi.mocked(rest.get).mockRejectedValueOnce(new Error('500'))
    expect(await getRequisitionNames()).toEqual([])
  })

  it('reads readiness, runs its actions, and resets the data collection', async () => {
    const readiness = { ready: false, pollerService: false, pollerMonitor: false, pollerPackage: null, collectdService: true, collectdCollector: true, servers: 0, polledServers: 0, unpolledServers: 0, requisitionsWithUnpolled: [] }
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: readiness })
    expect(await getWsmanReadiness()).toEqual(readiness)
    expect(vi.mocked(v2.get).mock.calls[0][0]).toBe('/wsman-config/readiness')
    vi.mocked(v2.post).mockResolvedValueOnce({ status: 200, data: { ...readiness, ready: true, pollerService: true, pollerMonitor: true }})
    expect(await runWsmanReadinessAction('enable-polling')).toMatchObject({ ready: true })
    expect(vi.mocked(v2.post).mock.calls[0][0]).toBe('/wsman-config/readiness/enable-polling')
    vi.mocked(v2.post).mockRejectedValueOnce({ response: { status: 500, data: '<html>' }})
    expect(await runWsmanReadinessAction('rescan')).toBe('Failed to rescan the requisitions.')
    vi.mocked(v2.post).mockResolvedValueOnce({ status: 200, data: { sources: ['wsman-datacollection-config.xml'], versions: {}}})
    expect(await resetWsmanDataCollection()).toMatchObject({ sources: ['wsman-datacollection-config.xml'], groups: [] })
    expect(vi.mocked(v2.post).mock.calls[2][0]).toBe('/wsman-config/data-collection/reset')
  })

  it('returns null on failure or an unexpected body', async () => {
    vi.mocked(v2.get).mockRejectedValueOnce(new Error('403'))
    expect(await getWsmanConfig()).toBeNull()
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: '<html>' })
    expect(await getWsmanConfig()).toBeNull()
    // a payload without the version token cannot be saved back safely
    vi.mocked(v2.get).mockResolvedValueOnce({ status: 200, data: { defaults: {}}})
    expect(await getWsmanConfig()).toBeNull()
  })
})

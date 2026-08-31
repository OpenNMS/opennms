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
import { RELOADABLE_DAEMONS, reloadDaemon } from '@/services/daemonService'
import { rest } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  rest: { post: vi.fn() }
}))

describe('daemonService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('publishes the reloadDaemonConfig event with the daemonName parm as XML', async () => {
    vi.mocked(rest.post).mockResolvedValue({ status: 202 })

    await reloadDaemon('Pollerd')

    const [url, body, config] = vi.mocked(rest.post).mock.calls[0]
    expect(url).toBe('/events')
    expect(config).toEqual({ headers: { 'Content-Type': 'application/xml' }})
    expect(body).toContain('<uei>uei.opennms.org/internal/reloadDaemonConfig</uei>')
    expect(body).toContain('<parmName>daemonName</parmName>')
    expect(body).toContain('<value type="string" encoding="text">Pollerd</value>')
    expect(body).toContain('xmlns="http://xmlns.opennms.org/xsd/event"')
  })

  it('propagates a publish failure to the caller', async () => {
    vi.mocked(rest.post).mockRejectedValue(new Error('403'))
    await expect(reloadDaemon('trapd')).rejects.toThrow('403')
  })

  it('keeps the exact daemonName casing the daemons match on', () => {
    // reload matching is case-sensitive per daemon (DaemonReloadEnum wire values)
    expect(RELOADABLE_DAEMONS.map(d => d.name)).toEqual(
      ['alarmd', 'Collectd', 'Eventd', 'Notifd', 'Pollerd', 'syslogd', 'Telemetryd', 'trapd'])
  })
})

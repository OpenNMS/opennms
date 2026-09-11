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
import { getChart, getCharts } from '@/services/chartService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: { get: vi.fn() }
}))

describe('chartService', () => {
  beforeEach(() => vi.clearAllMocks())

  it('lists the configured charts and treats no content as an empty list', async () => {
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: [{ name: 'sample-bar-chart', title: 'Alarms' }] })
    expect(await getCharts()).toEqual([{ name: 'sample-bar-chart', title: 'Alarms' }])
    expect(v2.get).toHaveBeenCalledWith('/charts')

    vi.mocked(v2.get).mockResolvedValue({ status: 204, data: '' })
    expect(await getCharts()).toEqual([])
  })

  it('fetches one chart by name, encoded, and returns null when it is missing', async () => {
    const chart = { name: 'a b', title: 'A', categories: [], series: [] }
    vi.mocked(v2.get).mockResolvedValue({ status: 200, data: chart })
    expect(await getChart('a b')).toEqual(chart)
    expect(v2.get).toHaveBeenCalledWith('/charts/a%20b')

    vi.mocked(v2.get).mockRejectedValue(new Error('404'))
    expect(await getChart('nope')).toBeNull()
    vi.mocked(v2.get).mockRejectedValue(new Error('500'))
    expect(await getCharts()).toBeNull()
  })
})

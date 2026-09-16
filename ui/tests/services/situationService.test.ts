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
import { getPendingSituations, situationAlarmCount } from '@/services/situationService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: { get: vi.fn() }
}))

describe('situationService', () => {
  beforeEach(() => vi.clearAllMocks())

  it('derives the alarm count from relatedAlarms — AlarmDTO has no situationAlarmCount', () => {
    expect(situationAlarmCount({ id: 1, severity: 'MAJOR', relatedAlarms: [{}, {}, {}] })).toBe(3)
    expect(situationAlarmCount({ id: 2, severity: 'MAJOR' })).toBe(0)
  })

  it('returns null on failure so the panel can show an error, not all-clear', async () => {
    vi.mocked(v2.get).mockRejectedValue(new Error('500'))
    expect(await getPendingSituations()).toBeNull()
  })
})

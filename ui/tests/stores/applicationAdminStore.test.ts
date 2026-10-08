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
import { createPinia, setActivePinia } from 'pinia'
import { useApplicationAdminStore } from '@/stores/applicationAdminStore'
import API from '@/services'

vi.mock('@/services', () => ({
  default: {
    listApplicationSummaries: vi.fn(),
    createApplication: vi.fn(),
    deleteApplication: vi.fn(),
    getApplicationMembers: vi.fn(),
    updateApplicationMembers: vi.fn(),
    searchServiceCandidates: vi.fn()
  }
}))

const app = (id: number, name: string) => ({ id, name, serviceCount: 0, perspectiveLocations: [] })
const ok = { success: true, message: '' }
const failed = (message: string) => ({ success: false, message })

describe('useApplicationAdminStore', () => {
  let store: ReturnType<typeof useApplicationAdminStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useApplicationAdminStore()
    vi.clearAllMocks()
  })

  it('loads the applications and keeps the previous list when a reload fails', async () => {
    vi.mocked(API.listApplicationSummaries).mockResolvedValueOnce([app(1, 'Web')])
    expect(await store.getApplications()).toBe(true)
    expect(store.applications).toEqual([app(1, 'Web')])
    expect(store.loadError).toBe(false)

    vi.mocked(API.listApplicationSummaries).mockResolvedValueOnce(null)
    expect(await store.getApplications()).toBe(false)
    expect(store.applications).toEqual([app(1, 'Web')])
    expect(store.loadError).toBe(true)
    expect(store.loading).toBe(false)
  })

  it('keeps the newest list when an older load answers last', async () => {
    let answerOld: (value: any) => void = () => {}
    vi.mocked(API.listApplicationSummaries)
      .mockImplementationOnce(() => new Promise((resolve) => {
        answerOld = resolve
      }))
      .mockResolvedValueOnce([app(1, 'Web'), app(2, 'Billing')])
    const older = store.getApplications()
    await store.getApplications()
    answerOld([app(1, 'Web')])
    await older
    expect(store.applications).toEqual([app(1, 'Web'), app(2, 'Billing')])
    expect(store.loading).toBe(false)
  })

  it('reloads after a successful create, delete or member update, and not after a failure', async () => {
    vi.mocked(API.listApplicationSummaries).mockResolvedValue([])
    vi.mocked(API.createApplication).mockResolvedValueOnce(ok).mockResolvedValueOnce(failed('taken'))
    vi.mocked(API.deleteApplication).mockResolvedValueOnce(ok)
    vi.mocked(API.updateApplicationMembers).mockResolvedValueOnce(ok)

    expect(await store.createApplication('Web')).toEqual(ok)
    expect(await store.createApplication('Web')).toEqual(failed('taken'))
    expect(await store.deleteApplication(app(1, 'Web'))).toEqual(ok)
    expect(API.deleteApplication).toHaveBeenCalledWith(1, 'Web')
    expect(await store.updateMembers({ id: 1, name: 'Web' }, { serviceIds: [] })).toEqual(ok)
    expect(API.updateApplicationMembers).toHaveBeenCalledWith(1, 'Web', { serviceIds: [] })
    expect(API.listApplicationSummaries).toHaveBeenCalledTimes(3)
  })
})

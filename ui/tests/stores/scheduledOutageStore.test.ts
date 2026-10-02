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
import { useScheduledOutageStore } from '@/stores/scheduledOutageStore'
import { getActiveOutagesForNode } from '@/services/scheduledOutagesService'

vi.mock('@/services/scheduledOutagesService', () => ({
  getActiveOutagesForNode: vi.fn()
}))

const maint = { name: 'maint', node: [{ id: 42 }] }

describe('scheduledOutageStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('publishes the node\'s active outages, stamped with the node id', async () => {
    vi.mocked(getActiveOutagesForNode).mockResolvedValue([maint])
    const store = useScheduledOutageStore()

    const result = await store.getNodeActiveOutages('42')

    expect(getActiveOutagesForNode).toHaveBeenCalledWith('42')
    expect(result).toEqual(expect.objectContaining({ success: true, payload: [maint] }))
    expect(store.nodeActiveOutages).toEqual([maint])
    expect(store.nodeActiveOutagesNodeId).toBe('42')
  })

  // A window closing is an empty answer, which must replace the outages that were in effect.
  it('replaces the slice with an empty answer', async () => {
    vi.mocked(getActiveOutagesForNode).mockResolvedValueOnce([maint]).mockResolvedValueOnce([])
    const store = useScheduledOutageStore()

    await store.getNodeActiveOutages('42')
    await store.getNodeActiveOutages('42')

    expect(store.nodeActiveOutages).toEqual([])
  })

  it('keeps the previous slice and stamp when a fetch fails', async () => {
    vi.mocked(getActiveOutagesForNode).mockResolvedValueOnce([maint]).mockResolvedValueOnce(null)
    const store = useScheduledOutageStore()

    await store.getNodeActiveOutages('42')
    const result = await store.getNodeActiveOutages('99')

    expect(result.success).toBe(false)
    expect(store.nodeActiveOutagesNodeId).toBe('42')
    expect(store.nodeActiveOutages).toEqual([maint])
  })

  it('discards a superseded fetch: node 42 answering late does not replace node 99', async () => {
    let resolve42: (value: unknown) => void = () => undefined
    vi.mocked(getActiveOutagesForNode)
      .mockImplementationOnce(() => new Promise((r) => {
        resolve42 = r
      }) as never)
      .mockResolvedValueOnce([])
    const store = useScheduledOutageStore()

    const call42 = store.getNodeActiveOutages('42')
    await store.getNodeActiveOutages('99')
    resolve42([maint])

    expect((await call42).success).toBe(false)
    expect(store.nodeActiveOutagesNodeId).toBe('99')
    expect(store.nodeActiveOutages).toEqual([])
  })
})

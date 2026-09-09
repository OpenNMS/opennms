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
import { setActivePinia, createPinia } from 'pinia'
import { useEventStore } from '@/stores/eventStore'
import API from '@/services'
import { Event } from '@/types'

vi.mock('@/services', () => ({
  default: {
    getEvents: vi.fn()
  }
}))

const event = { id: 101, severity: 'Major', logMessage: 'msg' } as unknown as Event

describe('eventStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('getEvents', () => {
    it('publishes the fetched page into events/totalCount', async () => {
      vi.mocked(API.getEvents).mockResolvedValue({ event: [event], totalCount: 1, count: 1, offset: 0 })
      const store = useEventStore()

      await store.getEvents({ limit: 5, offset: 0 })

      expect(store.events).toEqual([event])
      expect(store.totalCount).toBe(1)
    })
  })

  describe('getEventsForExport', () => {
    it('returns the fetched events', async () => {
      vi.mocked(API.getEvents).mockResolvedValue({ event: [event], totalCount: 1, count: 1, offset: 0 })
      const store = useEventStore()

      await expect(store.getEventsForExport({ limit: 0, offset: 0 })).resolves.toEqual([event])
    })

    // A download runs its own query; publishing the result into the store would replace the
    // page the events table is showing.
    it('leaves the currently displayed page untouched', async () => {
      const displayed = { id: 7 } as unknown as Event
      vi.mocked(API.getEvents).mockResolvedValue({ event: [event], totalCount: 500, count: 500, offset: 0 })
      const store = useEventStore()
      store.events = [displayed]
      store.totalCount = 1

      await store.getEventsForExport({ limit: 0, offset: 0 })

      expect(store.events).toEqual([displayed])
      expect(store.totalCount).toBe(1)
    })

    it('returns an empty list when the request fails', async () => {
      vi.mocked(API.getEvents).mockResolvedValue(false)
      const store = useEventStore()

      await expect(store.getEventsForExport({ limit: 0, offset: 0 })).resolves.toEqual([])
    })
  })
})

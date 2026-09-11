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

  describe('clearEvents', () => {
    // The Node Details page clears these when its node fails to load: the events table fetches
    // by node id on its own and only replaces its rows on a successful response.
    it('empties the events and the count', () => {
      const store = useEventStore()
      store.events = [event]
      store.totalCount = 1

      store.clearEvents()

      expect(store.events).toEqual([])
      expect(store.totalCount).toBe(0)
    })
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

  // Fired per node id as the user moves between nodes, and per page as the paginator moves:
  // a page-2-then-page-3 click can land page 2's rows under page 3's paginator state.
  describe('stale responses', () => {
    it('discards a response a newer request has superseded', async () => {
      const deferredPage = <T>() => {
        let resolve: (value: T) => void = () => undefined
        const promise = new Promise<T>((r) => {
          resolve = r
        })

        return { promise, resolve }
      }

      const page2 = deferredPage<unknown>()
      const page3 = deferredPage<unknown>()
      vi.mocked(API.getEvents)
        .mockImplementationOnce(() => page2.promise as never)
        .mockImplementationOnce(() => page3.promise as never)
      const store = useEventStore()

      const page2Call = store.getEvents({ limit: 5, offset: 5 })
      const page3Call = store.getEvents({ limit: 5, offset: 10 })

      page3.resolve({ event: [{ id: 3 }], totalCount: 1, count: 1, offset: 10 })
      await page3Call
      page2.resolve({ event: [{ id: 2 }], totalCount: 9, count: 9, offset: 5 })
      await page2Call

      expect(store.events).toEqual([{ id: 3 }])
      expect(store.totalCount).toBe(1)
    })
  })
})

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

    it('reports a failed fetch and keeps what it had', async () => {
      vi.mocked(API.getEvents).mockResolvedValue(false)
      const store = useEventStore()
      store.events = [event]

      const result = await store.getEvents()

      expect(result.success).toBe(false)
      expect(store.events).toEqual([event])
    })

    it('leaves the node slice alone', async () => {
      vi.mocked(API.getEvents).mockResolvedValue({ event: [event], totalCount: 1, count: 1, offset: 0 })
      const store = useEventStore()

      await store.getEvents()

      expect(store.nodeEvents).toEqual([])
      expect(store.nodeEventsNodeId).toBeUndefined()
    })
  })

  describe('getNodeEvents', () => {
    it('scopes the query to the node and keeps the caller\'s paging', async () => {
      vi.mocked(API.getEvents).mockResolvedValue({ event: [event], totalCount: 1, count: 1, offset: 0 })
      const store = useEventStore()

      await store.getNodeEvents('42', { limit: 5, offset: 10 })

      expect(API.getEvents).toHaveBeenCalledWith({ limit: 5, offset: 10, _s: 'node.id==42' })
    })

    it('publishes into the node slice, stamped with the node id', async () => {
      vi.mocked(API.getEvents).mockResolvedValue({ event: [event], totalCount: 7, count: 1, offset: 0 })
      const store = useEventStore()

      const result = await store.getNodeEvents('42')

      expect(result).toEqual(expect.objectContaining({ success: true, payload: [event] }))
      expect(store.nodeEvents).toEqual([event])
      expect(store.nodeEventsTotalCount).toBe(7)
      expect(store.nodeEventsNodeId).toBe('42')
      expect(store.events).toEqual([])
    })

    // The stamp is what lets a panel tell a failed fetch for node 99 from node 99's events.
    it('keeps the previous node\'s slice and stamp when a fetch fails', async () => {
      vi.mocked(API.getEvents)
        .mockResolvedValueOnce({ event: [event], totalCount: 1, count: 1, offset: 0 })
        .mockResolvedValueOnce(false)
      const store = useEventStore()

      await store.getNodeEvents('42')
      const result = await store.getNodeEvents('99')

      expect(result.success).toBe(false)
      expect(store.nodeEventsNodeId).toBe('42')
    })
  })

  // What the Node Details tables need to tell a page that failed from a page that loaded.
  describe('getNodeEvents: page and failure state', () => {
    it('records the page it holds on success', async () => {
      vi.mocked(API.getEvents).mockResolvedValue({ event: [], totalCount: 0, count: 0, offset: 0 } as never)
      const store = useEventStore()

      await store.getNodeEvents('42', { offset: 10, limit: 5 })

      expect(store.nodeEventsPage).toEqual({ offset: 10, limit: 5 })
      expect(store.nodeEventsFailedNodeId).toBeUndefined()
    })

    it('keeps the page and marks the node failed when a fetch fails', async () => {
      vi.mocked(API.getEvents).mockResolvedValueOnce({ event: [], totalCount: 0, count: 0, offset: 0 } as never).mockResolvedValueOnce(false)
      const store = useEventStore()

      await store.getNodeEvents('42', { offset: 0, limit: 5 })
      await store.getNodeEvents('42', { offset: 5, limit: 5 })

      expect(store.nodeEventsPage).toEqual({ offset: 0, limit: 5 })
      expect(store.nodeEventsFailedNodeId).toBe('42')
    })

    it('clears another node\'s failure as soon as a fetch for a new node starts', async () => {
      vi.mocked(API.getEvents).mockResolvedValueOnce(false).mockImplementationOnce(() => new Promise(() => undefined) as never)
      const store = useEventStore()

      await store.getNodeEvents('42')
      void store.getNodeEvents('99')

      expect(store.nodeEventsFailedNodeId).toBeUndefined()
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

    it('discards a superseded node fetch: node 42 answering late does not replace node 99', async () => {
      let resolve42: (value: unknown) => void = () => undefined
      vi.mocked(API.getEvents)
        .mockImplementationOnce(() => new Promise((r) => {
          resolve42 = r
        }) as never)
        .mockResolvedValueOnce({ event: [{ id: 99 }], totalCount: 1, count: 1, offset: 0 } as never)
      const store = useEventStore()

      const call42 = store.getNodeEvents('42')
      await store.getNodeEvents('99')
      resolve42({ event: [{ id: 42 }], totalCount: 1, count: 1, offset: 0 })
      const result42 = await call42

      expect(result42.success).toBe(false)
      expect(store.nodeEventsNodeId).toBe('99')
      expect(store.nodeEvents).toEqual([{ id: 99 }])
    })

    // One counter per slice: an unscoped query must not void an in-flight node fetch.
    it('does not let a request for one slice discard the other\'s response', async () => {
      let resolveNode: (value: unknown) => void = () => undefined
      vi.mocked(API.getEvents)
        .mockImplementationOnce(() => new Promise((r) => {
          resolveNode = r
        }) as never)
        .mockResolvedValueOnce({ event: [], totalCount: 0, count: 0, offset: 0 } as never)
      const store = useEventStore()

      const nodeCall = store.getNodeEvents('42')
      await store.getEvents()
      resolveNode({ event: [event], totalCount: 1, count: 1, offset: 0 })

      expect((await nodeCall).success).toBe(true)
      expect(store.nodeEvents).toEqual([event])
    })
  })
})

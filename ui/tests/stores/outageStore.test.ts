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
import { useOutageStore } from '@/stores/outageStore'
import API from '@/services'

vi.mock('@/services', () => ({
  default: {
    getOutages: vi.fn(),
    getNodeOutages: vi.fn()
  }
}))

const response = (outages: unknown[], totalCount = outages.length) => ({ outage: outages, totalCount, count: outages.length, offset: 0 }) as never

describe('outageStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('getOutages', () => {
    it('publishes the result into outages/totalCount, leaving the node slice alone', async () => {
      vi.mocked(API.getOutages).mockResolvedValue(response([{ id: 1 }], 9))
      const store = useOutageStore()

      const result = await store.getOutages({ limit: 1 })

      expect(API.getOutages).toHaveBeenCalledWith({ limit: 1 })
      expect(result).toEqual(expect.objectContaining({ success: true, payload: [{ id: 1 }] }))
      expect(store.outages).toEqual([{ id: 1 }])
      expect(store.totalCount).toBe(9)
      expect(store.nodeOutages).toEqual([])
    })

    it('reports a failed fetch and keeps what it had', async () => {
      vi.mocked(API.getOutages).mockResolvedValue(false)
      const store = useOutageStore()
      store.outages = [{ id: 1 }] as never

      expect((await store.getOutages()).success).toBe(false)
      expect(store.outages).toEqual([{ id: 1 }])
    })
  })

  describe('getNodeOutages', () => {
    it('passes the node id and the caller\'s parameters through', async () => {
      vi.mocked(API.getNodeOutages).mockResolvedValue(response([]))

      await useOutageStore().getNodeOutages('42', { limit: 5, offset: 0 })

      expect(API.getNodeOutages).toHaveBeenCalledWith('42', { limit: 5, offset: 0 })
    })

    it('publishes into the node slice, stamped with the node id', async () => {
      vi.mocked(API.getNodeOutages).mockResolvedValue(response([{ id: 1 }]))
      const store = useOutageStore()

      const result = await store.getNodeOutages('42')

      expect(result).toEqual(expect.objectContaining({ success: true, payload: [{ id: 1 }] }))
      expect(store.nodeOutages).toEqual([{ id: 1 }])
      expect(store.nodeOutagesTotalCount).toBe(1)
      expect(store.nodeOutagesNodeId).toBe('42')
      expect(store.outages).toEqual([])
    })

    it('keeps the previous node\'s slice and stamp when a fetch fails', async () => {
      vi.mocked(API.getNodeOutages).mockResolvedValueOnce(response([{ id: 1 }])).mockResolvedValueOnce(false)
      const store = useOutageStore()

      await store.getNodeOutages('42')
      const result = await store.getNodeOutages('99')

      expect(result.success).toBe(false)
      expect(store.nodeOutagesNodeId).toBe('42')
    })

    it('discards a superseded fetch: node 42 answering late does not replace node 99', async () => {
      let resolve42: (value: unknown) => void = () => undefined
      vi.mocked(API.getNodeOutages)
        .mockImplementationOnce(() => new Promise((r) => {
          resolve42 = r
        }) as never)
        .mockResolvedValueOnce(response([{ id: 99 }]))
      const store = useOutageStore()

      const call42 = store.getNodeOutages('42')
      await store.getNodeOutages('99')
      resolve42(response([{ id: 42 }]))

      expect((await call42).success).toBe(false)
      expect(store.nodeOutagesNodeId).toBe('99')
      expect(store.nodeOutages).toEqual([{ id: 99 }])
    })
  })

  // What the Node Details tables need to tell a page that failed from a page that loaded.
  describe('getNodeOutages: page and failure state', () => {
    it('records the page it holds on success', async () => {
      vi.mocked(API.getNodeOutages).mockResolvedValue(response([]))
      const store = useOutageStore()

      await store.getNodeOutages('42', { offset: 10, limit: 5 })

      expect(store.nodeOutagesPage).toEqual({ offset: 10, limit: 5 })
      expect(store.nodeOutagesFailedNodeId).toBeUndefined()
    })

    it('keeps the page and marks the node failed when a fetch fails', async () => {
      vi.mocked(API.getNodeOutages).mockResolvedValueOnce(response([])).mockResolvedValueOnce(false)
      const store = useOutageStore()

      await store.getNodeOutages('42', { offset: 0, limit: 5 })
      await store.getNodeOutages('42', { offset: 5, limit: 5 })

      expect(store.nodeOutagesPage).toEqual({ offset: 0, limit: 5 })
      expect(store.nodeOutagesFailedNodeId).toBe('42')
    })

    it('clears another node\'s failure as soon as a fetch for a new node starts', async () => {
      vi.mocked(API.getNodeOutages).mockResolvedValueOnce(false).mockImplementationOnce(() => new Promise(() => undefined) as never)
      const store = useOutageStore()

      await store.getNodeOutages('42')
      void store.getNodeOutages('99')

      expect(store.nodeOutagesFailedNodeId).toBeUndefined()
    })
  })
})

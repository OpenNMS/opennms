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
import { useAlarmStore } from '@/stores/alarmStore'
import API from '@/services'
import { Alarm } from '@/types'

vi.mock('@/services', () => ({
  default: {
    getAlarms: vi.fn(),
    getNodeAlarmStatus: vi.fn()
  }
}))

const alarm = { id: '1', severity: 'MAJOR', uei: 'uei.opennms.org/nodes/nodeDown', nodeId: 42 } as Alarm
const response = (alarms: unknown[], totalCount = alarms.length) => ({ alarm: alarms, totalCount, count: alarms.length, offset: 0 }) as never

describe('alarmStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('getAlarms', () => {
    it('publishes the result into alarms/totalCount, leaving the node slice alone', async () => {
      vi.mocked(API.getAlarms).mockResolvedValue(response([alarm], 3))
      const store = useAlarmStore()

      const result = await store.getAlarms({ limit: 1 })

      expect(API.getAlarms).toHaveBeenCalledWith({ limit: 1 })
      expect(result).toEqual(expect.objectContaining({ success: true, payload: [alarm] }))
      expect(store.alarms).toEqual([alarm])
      expect(store.totalCount).toBe(3)
      expect(store.nodeAlarms).toEqual([])
    })

    it('reports a failed fetch and keeps what it had', async () => {
      vi.mocked(API.getAlarms).mockResolvedValue(false)
      const store = useAlarmStore()
      store.alarms = [alarm]

      expect((await store.getAlarms()).success).toBe(false)
      expect(store.alarms).toEqual([alarm])
    })
  })

  describe('getNodeAlarms', () => {
    // The status no longer needs every alarm, so the caller says how many it wants.
    it('asks only for what the caller asks, within the node', async () => {
      vi.mocked(API.getAlarms).mockResolvedValue(response([]))

      await useAlarmStore().getNodeAlarms('42', { limit: 5, offset: 10 })

      expect(API.getAlarms).toHaveBeenCalledWith({ limit: 5, offset: 10, _s: 'node.id==42' })
    })

    it('applies the caller\'s criteria within the node', async () => {
      vi.mocked(API.getAlarms).mockResolvedValue(response([]))

      await useAlarmStore().getNodeAlarms('42', { limit: 10, _s: 'severity=gt=NORMAL' })

      expect(API.getAlarms).toHaveBeenCalledWith({ limit: 10, _s: 'node.id==42;(severity=gt=NORMAL)' })
    })

    it('publishes into the node slice, stamped with the node id', async () => {
      vi.mocked(API.getAlarms).mockResolvedValue(response([alarm]))
      const store = useAlarmStore()

      const result = await store.getNodeAlarms('42')

      expect(result).toEqual(expect.objectContaining({ success: true, payload: [alarm] }))
      expect(store.nodeAlarms).toEqual([alarm])
      expect(store.nodeAlarmsTotalCount).toBe(1)
      expect(store.nodeAlarmsNodeId).toBe('42')
      expect(store.alarms).toEqual([])
    })

    it('keeps the previous node\'s slice and stamp when a fetch fails', async () => {
      vi.mocked(API.getAlarms).mockResolvedValueOnce(response([alarm])).mockResolvedValueOnce(false)
      const store = useAlarmStore()

      await store.getNodeAlarms('42')
      const result = await store.getNodeAlarms('99')

      expect(result.success).toBe(false)
      expect(store.nodeAlarmsNodeId).toBe('42')
      expect(store.nodeAlarms).toEqual([alarm])
    })

    it('discards a superseded fetch: node 42 answering late does not replace node 99', async () => {
      let resolve42: (value: unknown) => void = () => undefined
      vi.mocked(API.getAlarms)
        .mockImplementationOnce(() => new Promise((r) => {
          resolve42 = r
        }) as never)
        .mockResolvedValueOnce(response([{ id: '99' }]))
      const store = useAlarmStore()

      const call42 = store.getNodeAlarms('42')
      await store.getNodeAlarms('99')
      resolve42(response([{ id: '42' }]))

      expect((await call42).success).toBe(false)
      expect(store.nodeAlarmsNodeId).toBe('99')
      expect(store.nodeAlarms).toEqual([{ id: '99' }])
    })

    describe('failure state', () => {
      const pending = () => new Promise(() => undefined) as never

      it('marks the node failed when its fetch fails, and clears it when a fetch succeeds', async () => {
        vi.mocked(API.getAlarms).mockResolvedValueOnce(false).mockResolvedValueOnce(response([alarm]))
        const store = useAlarmStore()

        await store.getNodeAlarms('42')
        expect(store.nodeAlarmsFailedNodeId).toBe('42')

        await store.getNodeAlarms('42')
        expect(store.nodeAlarmsFailedNodeId).toBeUndefined()
      })

      // Node 99 is loading, not failed: node 42's failure must not show against it.
      it('clears another node\'s failure as soon as a fetch for a new node starts', async () => {
        vi.mocked(API.getAlarms).mockResolvedValueOnce(false).mockImplementationOnce(pending)
        const store = useAlarmStore()

        await store.getNodeAlarms('42')
        void store.getNodeAlarms('99')

        expect(store.nodeAlarmsFailedNodeId).toBeUndefined()
      })

      it('keeps the failure while the same node is refreshed', async () => {
        vi.mocked(API.getAlarms).mockResolvedValueOnce(false).mockImplementationOnce(pending)
        const store = useAlarmStore()

        await store.getNodeAlarms('42')
        void store.getNodeAlarms('42')

        expect(store.nodeAlarmsFailedNodeId).toBe('42')
      })

      it('does not mark a node failed for a superseded fetch', async () => {
        let resolve42: (value: unknown) => void = () => undefined
        vi.mocked(API.getAlarms)
          .mockImplementationOnce(() => new Promise((r) => {
            resolve42 = r
          }) as never)
          .mockImplementationOnce(pending)
        const store = useAlarmStore()

        const call42 = store.getNodeAlarms('42')
        void store.getNodeAlarms('99')
        resolve42(false)
        await call42

        expect(store.nodeAlarmsFailedNodeId).toBeUndefined()
      })
    })
  })

  describe('getNodeAlarmStatus', () => {
    const status = { severity: 'MAJOR', nodeDown: false, interfacesDown: 0, servicesDown: 1, acknowledgedCount: 0, unacknowledgedCount: 1 }
    const ok = (payload = status) => ({ success: true, message: '', payload }) as never
    const failed = { success: false, message: 'nope' } as never
    const pending = () => new Promise(() => undefined) as never

    it('publishes the summary, stamped with the node id', async () => {
      vi.mocked(API.getNodeAlarmStatus).mockResolvedValue(ok())
      const store = useAlarmStore()

      const result = await store.getNodeAlarmStatus('42')

      expect(API.getNodeAlarmStatus).toHaveBeenCalledWith('42')
      expect(result.success).toBe(true)
      expect(store.nodeAlarmStatus).toEqual(status)
      expect(store.nodeAlarmStatusNodeId).toBe('42')
    })

    it('keeps the previous summary and marks the node failed when a fetch fails', async () => {
      vi.mocked(API.getNodeAlarmStatus).mockResolvedValueOnce(ok()).mockResolvedValueOnce(failed)
      const store = useAlarmStore()

      await store.getNodeAlarmStatus('42')
      const result = await store.getNodeAlarmStatus('99')

      expect(result.success).toBe(false)
      expect(store.nodeAlarmStatusNodeId).toBe('42')
      expect(store.nodeAlarmStatus).toEqual(status)
      expect(store.nodeAlarmStatusFailedNodeId).toBe('99')
    })

    it('clears another node\'s failure as soon as a fetch for a new node starts', async () => {
      vi.mocked(API.getNodeAlarmStatus).mockResolvedValueOnce(failed).mockImplementationOnce(pending)
      const store = useAlarmStore()

      await store.getNodeAlarmStatus('42')
      void store.getNodeAlarmStatus('99')

      expect(store.nodeAlarmStatusFailedNodeId).toBeUndefined()
    })

    it('discards a superseded fetch', async () => {
      let resolve42: (value: unknown) => void = () => undefined
      vi.mocked(API.getNodeAlarmStatus)
        .mockImplementationOnce(() => new Promise((r) => {
          resolve42 = r
        }) as never)
        .mockResolvedValueOnce(ok({ ...status, severity: 'MINOR' }))
      const store = useAlarmStore()

      const call42 = store.getNodeAlarmStatus('42')
      await store.getNodeAlarmStatus('99')
      resolve42(ok())

      expect((await call42).success).toBe(false)
      expect(store.nodeAlarmStatusNodeId).toBe('99')
      expect(store.nodeAlarmStatus?.severity).toBe('MINOR')
    })
  })
})

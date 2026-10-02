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
    getAlarms: vi.fn()
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
    // The node's status is worked out from every alarm it has, as the JSP's NO_LIMIT did.
    it('asks for all of the node\'s alarms by default', async () => {
      vi.mocked(API.getAlarms).mockResolvedValue(response([]))

      await useAlarmStore().getNodeAlarms('42')

      expect(API.getAlarms).toHaveBeenCalledWith({ limit: 0, _s: 'node.id==42' })
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
  })
})

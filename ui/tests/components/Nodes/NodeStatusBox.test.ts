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

import NodeStatusBox from '@/components/Nodes/NodeStatusBox.vue'
import { useAlarmStore } from '@/stores/alarmStore'
import { useMenuStore } from '@/stores/menuStore'
import { createTestingPinia } from '@pinia/testing'
import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h, KeepAlive, nextTick, ref } from 'vue'
import { useRoute } from 'vue-router'

vi.mock('vue-router', async () => {
  const { reactive } = await import('vue')
  const route = reactive({ params: { id: '42' }})

  return { useRoute: () => route }
})

const summary = (overrides: Record<string, unknown> = {}) => ({
  severity: 'NORMAL', nodeDown: false, interfacesDown: 0, servicesDown: 0, acknowledgedCount: 0, unacknowledgedCount: 0, ...overrides
})
// One service down and unacknowledged, plus one minor problem someone has acknowledged.
const major = summary({ severity: 'MAJOR', servicesDown: 1, unacknowledgedCount: 1, acknowledgedCount: 1 })
const twoUnacked = summary({ severity: 'MAJOR', servicesDown: 2, unacknowledgedCount: 2 })

// Every test moves the one shared route; a box left mounted from an earlier test would follow it.
enableAutoUnmount(afterEach)

describe('NodeStatusBox.vue', () => {
  let alarmStore: ReturnType<typeof useAlarmStore>

  // Resolves as a successful fetch would: the slice replaced and stamped with the node id.
  const answerWith = (status: ReturnType<typeof summary>) => {
    const store = alarmStore
    store.getNodeAlarmStatus = vi.fn(async (nodeId: string) => {
      store.nodeAlarmStatus = status
      store.nodeAlarmStatusNodeId = nodeId
      store.nodeAlarmStatusFailedNodeId = undefined

      return { success: true, message: '', payload: status }
    })
  }

  // Resolves as a failed fetch would: the slice left alone, the node marked failed.
  const failFetch = () => {
    const store = alarmStore
    store.getNodeAlarmStatus = vi.fn(async (nodeId: string) => {
      store.nodeAlarmStatusFailedNodeId = nodeId

      return { success: false, message: 'nope' }
    })
  }

  const createPinia = (setup: () => void) => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    alarmStore = useAlarmStore(pinia)
    useMenuStore(pinia).mainMenu = { baseHref: '/opennms/' } as any
    setup()

    return pinia
  }

  const mountBox = (setup: () => void = () => answerWith(summary())) =>
    mount(NodeStatusBox, { global: { plugins: [createPinia(setup)] }})

  beforeEach(() => {
    vi.useFakeTimers()
    ;(useRoute() as any).params.id = '42'
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('fetches the node\'s alarm status on mount', async () => {
    mountBox()
    await flushPromises()

    expect(alarmStore.getNodeAlarmStatus).toHaveBeenCalledWith('42')
  })

  it('says it is checking until the first answer arrives, in a neutral banner', () => {
    const wrapper = mountBox(() => {
      alarmStore.getNodeAlarmStatus = vi.fn(() => new Promise(() => undefined)) as any
    })

    expect(wrapper.text()).toContain('Checking node status')
    expect(wrapper.find('[data-test="node-status"]').classes()).toContain('node-details-banner--none')
  })

  it('shows a node with no problems as normal, without counts', async () => {
    const wrapper = mountBox()
    await flushPromises()

    expect(wrapper.find('[data-test="node-status-message"]').text()).toBe('Node has no problems.')
    expect(wrapper.find('[data-test="node-status"]').classes()).toContain('node-details-banner--normal')
    expect(wrapper.find('[data-test="node-status-unack-link"]').exists()).toBe(false)
  })

  it('shows the headline, severity and linked counts for a node with problems', async () => {
    const wrapper = mountBox(() => answerWith(major))
    await flushPromises()

    expect(wrapper.find('[data-test="node-status"]').classes()).toContain('node-details-banner--major')
    expect(wrapper.find('[data-test="node-status-message"]').text()).toBe('Node has 1 service down.')
    expect(wrapper.text().replace(/\s+/g, ' ')).toContain('There is 1 unacknowledged problem, and 1 acknowledged problem.')
    expect(wrapper.find('[data-test="node-status-unack-link"]').attributes('href'))
      .toBe('/opennms/alarm/list.htm?filter=node%3d42&acktype=unack')
    expect(wrapper.find('[data-test="node-status-ack-link"]').attributes('href'))
      .toBe('/opennms/alarm/list.htm?filter=node%3d42&acktype=ack')
  })

  it('pluralises the counts', async () => {
    const wrapper = mountBox(() => answerWith(twoUnacked))
    await flushPromises()

    expect(wrapper.text().replace(/\s+/g, ' ')).toContain('There are 2 unacknowledged problems, and 0 acknowledged problems.')
  })

  it('says the status is unavailable when the first fetch fails', async () => {
    const wrapper = mountBox(failFetch)
    await flushPromises()

    expect(wrapper.find('[data-test="node-status-unavailable"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="node-status"]').classes()).toContain('node-details-banner--none')
  })

  // A minute-old status says more than none.
  it('keeps the last status when a later refresh fails', async () => {
    const wrapper = mountBox(() => answerWith(major))
    await flushPromises()

    failFetch()
    await vi.advanceTimersByTimeAsync(60_000)

    expect(alarmStore.getNodeAlarmStatus).toHaveBeenCalled()
    expect(wrapper.find('[data-test="node-status-message"]').text()).toBe('Node has 1 service down.')
  })

  describe('node changes', () => {
    it('refetches for the new node', async () => {
      mountBox()
      await flushPromises()

      ;(useRoute() as any).params.id = '99'
      await flushPromises()

      expect(alarmStore.getNodeAlarmStatus).toHaveBeenLastCalledWith('99')
    })

    // The store's slice is replaced only on success, so it may still be node 42's.
    it('does not show the previous node\'s status while the new one is loading', async () => {
      const wrapper = mountBox(() => answerWith(major))
      await flushPromises()

      alarmStore.getNodeAlarmStatus = vi.fn(() => new Promise(() => undefined)) as any
      ;(useRoute() as any).params.id = '99'
      await flushPromises()

      expect(wrapper.find('[data-test="node-status-message"]').exists()).toBe(false)
      expect(wrapper.text()).toContain('Checking node status')
    })

    // Node 42's failure says nothing about node 99, which is still loading.
    it('does not carry the previous node\'s failure over to the new one', async () => {
      const wrapper = mountBox(failFetch)
      await flushPromises()
      expect(wrapper.find('[data-test="node-status-unavailable"]').exists()).toBe(true)

      alarmStore.getNodeAlarmStatus = vi.fn(() => new Promise(() => undefined)) as any
      ;(useRoute() as any).params.id = '99'
      await flushPromises()

      expect(wrapper.find('[data-test="node-status-unavailable"]').exists()).toBe(false)
      expect(wrapper.text()).toContain('Checking node status')
    })

    it('says unavailable, not the previous node\'s status, when the new node\'s fetch fails', async () => {
      const wrapper = mountBox(() => answerWith(major))
      await flushPromises()

      failFetch()
      ;(useRoute() as any).params.id = '99'
      await flushPromises()

      expect(wrapper.find('[data-test="node-status-unavailable"]').exists()).toBe(true)
    })
  })

  describe('polling', () => {
    it('refreshes every minute while visible', async () => {
      mountBox()
      await flushPromises()
      vi.mocked(alarmStore.getNodeAlarmStatus).mockClear()

      await vi.advanceTimersByTimeAsync(60_000)
      expect(alarmStore.getNodeAlarmStatus).toHaveBeenCalledTimes(1)

      await vi.advanceTimersByTimeAsync(60_000)
      expect(alarmStore.getNodeAlarmStatus).toHaveBeenCalledTimes(2)
    })

    it('stops when unmounted', async () => {
      const wrapper = mountBox()
      await flushPromises()
      wrapper.unmount()
      vi.mocked(alarmStore.getNodeAlarmStatus).mockClear()

      await vi.advanceTimersByTimeAsync(180_000)

      expect(alarmStore.getNodeAlarmStatus).not.toHaveBeenCalled()
    })

    // The Main tab sits in a KeepAlive: switched away, it is alive but not on screen.
    it('pauses while its tab is switched away, and refreshes at once on return', async () => {
      const showBox = ref(true)
      const Host = defineComponent({
        setup: () => () => h(KeepAlive, null, [showBox.value ? h(NodeStatusBox) : h('div')])
      })
      mount(Host, { global: { plugins: [createPinia(() => answerWith(summary()))] }})
      await flushPromises()

      showBox.value = false
      await nextTick()
      vi.mocked(alarmStore.getNodeAlarmStatus).mockClear()
      await vi.advanceTimersByTimeAsync(180_000)
      expect(alarmStore.getNodeAlarmStatus).not.toHaveBeenCalled()

      showBox.value = true
      await flushPromises()
      expect(alarmStore.getNodeAlarmStatus).toHaveBeenCalledTimes(1)

      await vi.advanceTimersByTimeAsync(60_000)
      expect(alarmStore.getNodeAlarmStatus).toHaveBeenCalledTimes(2)
    })
  })
})

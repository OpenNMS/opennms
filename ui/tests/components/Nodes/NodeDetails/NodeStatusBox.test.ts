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

import NodeStatusBox from '@/components/Nodes/NodeDetails/NodeStatusBox.vue'
import { useAlarmStore } from '@/stores/alarmStore'
import { useMenuStore } from '@/stores/menuStore'
import { OnmsTag, OnmsTooltip } from '@opennms/onms-ui'
import { createTestingPinia } from '@pinia/testing'
import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h, KeepAlive, nextTick, ref } from 'vue'
import { useRoute } from 'vue-router'

vi.mock('vue-router', async () => {
  const { reactive } = await import('vue')
  const route = reactive({ params: { id: '42' }})

  return { useRoute: () => route }
})

const bySeverity = (counts: Record<string, number> = {}) => ({ CRITICAL: 0, MAJOR: 0, MINOR: 0, WARNING: 0, ...counts })
const summary = (overrides: Record<string, unknown> = {}) => ({
  severity: 'NORMAL', unacknowledgedCount: 0, unacknowledgedBySeverity: bySeverity(), ...overrides
})
// One major alarm unacknowledged.
const major = summary({ severity: 'MAJOR', unacknowledgedCount: 1, unacknowledgedBySeverity: bySeverity({ MAJOR: 1 }) })
// Unacknowledged alarms at three severities.
const mixed = summary({ severity: 'CRITICAL', unacknowledgedCount: 6, unacknowledgedBySeverity: bySeverity({ CRITICAL: 1, MAJOR: 2, WARNING: 3 }) })

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
    mount(NodeStatusBox, {
      global: {
        plugins: [PrimeVue, createPinia(setup)],
        directives: { 'onms-tooltip': OnmsTooltip }
      }
    })

  // Where the tooltip directive parks its resolved text; see OnmsTooltip.test.ts.
  const tooltipOf = (wrapper: ReturnType<typeof mountBox>, selector: string) =>
    (wrapper.find(selector).element as never as Record<string, unknown>).$_ptooltipValue

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
    expect(wrapper.find('[data-test="node-status-icon"]').exists()).toBe(false)
  })

  // Acknowledged alarms are not in the summary, so a node whose alarms are all acknowledged looks the same.
  it('shows a node with no unacknowledged alarms as normal, without tags', async () => {
    const wrapper = mountBox()
    await flushPromises()

    expect(wrapper.find('[data-test="node-status-message"]').text()).toBe('Node has no unacknowledged alarms.')
    expect(wrapper.find('[data-test="node-status"]').classes()).toContain('node-details-banner--normal')
    expect(wrapper.find('[data-test="node-status-icon"]').classes()).toContain('severity-icon--normal')
    expect(wrapper.findAllComponents(OnmsTag)).toHaveLength(0)
  })

  it('leads with the icon for the worst unacknowledged alarm\'s severity', async () => {
    const wrapper = mountBox(() => answerWith(major))
    await flushPromises()

    const icon = wrapper.find('[data-test="node-status-icon"]')
    expect(icon.classes()).toContain('severity-icon--major')
    expect(icon.attributes('aria-label')).toBe('Major severity')
    expect(icon.element.nextElementSibling?.getAttribute('data-test')).toBe('node-status-message')
  })

  describe('count tags', () => {
    const tag = (severity: string) => `[data-test="node-status-unack-${severity}-link"]`

    it('says the node has unacknowledged alarms, then a linked tag for each severity with any', async () => {
      const wrapper = mountBox(() => answerWith(mixed))
      await flushPromises()

      expect(wrapper.find('[data-test="node-status"]').classes()).toContain('node-details-banner--critical')
      expect(wrapper.find('[data-test="node-status-message"]').text()).toBe('Node has unacknowledged alarms.')
      expect(wrapper.find(tag('critical')).text()).toBe('1')
      expect(wrapper.find(tag('major')).text()).toBe('2')
      expect(wrapper.find(tag('warning')).text()).toBe('3')
    })

    it('puts the tags highest severity first, and leaves out a severity with none', async () => {
      const wrapper = mountBox(() => answerWith(mixed))
      await flushPromises()

      expect(wrapper.findAll('.node-status-tags a').map(link => link.attributes('data-test'))).toEqual([
        'node-status-unack-critical-link', 'node-status-unack-major-link', 'node-status-unack-warning-link'
      ])
      expect(wrapper.find(tag('minor')).exists()).toBe(false)
    })

    it('makes each tag neutral, with the severity\'s icon for its color', async () => {
      const wrapper = mountBox(() => answerWith(mixed))
      await flushPromises()

      expect(wrapper.findAllComponents(OnmsTag).map(t => t.props('severity'))).toEqual(['secondary', 'secondary', 'secondary'])
      expect(wrapper.find(tag('major')).find('.severity-icon').classes()).toContain('severity-icon--major')
    })

    it('links each tag to the node\'s unacknowledged alarms at that severity', async () => {
      const wrapper = mountBox(() => answerWith(mixed))
      await flushPromises()

      expect(wrapper.find(tag('critical')).attributes('href'))
        .toBe('/opennms/alarm/list.htm?filter=node%3d42&filter=severity%3d7&acktype=unack')
      expect(wrapper.find(tag('warning')).attributes('href'))
        .toBe('/opennms/alarm/list.htm?filter=node%3d42&filter=severity%3d4&acktype=unack')
    })

    // The tag itself is a number and an icon; the words are in its name and its tooltip.
    it('names each tag and gives it a tooltip, in the singular or plural', async () => {
      const wrapper = mountBox(() => answerWith(mixed))
      await flushPromises()

      expect(wrapper.find(tag('critical')).attributes('aria-label')).toBe('1 unacknowledged critical alarm')
      expect(tooltipOf(wrapper, tag('critical'))).toBe('1 unacknowledged critical alarm')
      expect(wrapper.find(tag('major')).attributes('aria-label')).toBe('2 unacknowledged major alarms')
      expect(tooltipOf(wrapper, tag('major'))).toBe('2 unacknowledged major alarms')
    })
  })

  it('says the status is unavailable when the first fetch fails', async () => {
    const wrapper = mountBox(failFetch)
    await flushPromises()

    expect(wrapper.find('[data-test="node-status-unavailable"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="node-status"]').classes()).toContain('node-details-banner--none')
    expect(wrapper.find('[data-test="node-status-icon"]').classes()).toContain('severity-icon--none')
    expect(wrapper.find('[data-test="node-status-icon"]').attributes('aria-label')).toBe('Unknown severity')
  })

  // A minute-old status says more than none.
  it('keeps the last status when a later refresh fails', async () => {
    const wrapper = mountBox(() => answerWith(major))
    await flushPromises()

    failFetch()
    await vi.advanceTimersByTimeAsync(60_000)

    expect(alarmStore.getNodeAlarmStatus).toHaveBeenCalled()
    expect(wrapper.find('[data-test="node-status-message"]').text()).toBe('Node has unacknowledged alarms.')
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

    // The Monitoring tab sits in a KeepAlive: switched away, it is alive but not on screen.
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

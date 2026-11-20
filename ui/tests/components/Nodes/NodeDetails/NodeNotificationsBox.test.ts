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

import NodeNotificationsBox from '@/components/Nodes/NodeDetails/NodeNotificationsBox.vue'
import { useNotificationsStore } from '@/stores/notificationsStore'
import { NodeNotificationStatus } from '@/types'
import { OnmsIcon, OnmsTag } from '@opennms/onms-ui'
import Group from '@opennms/onms-ui/icons/action/Group.vue'
import Person from '@opennms/onms-ui/icons/action/Person.vue'
import ViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import { createTestingPinia } from '@pinia/testing'
import { enableAutoUnmount, flushPromises, mount, RouterLinkStub } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { markRaw, nextTick } from 'vue'
import { useRoute } from 'vue-router'

vi.mock('vue-router', async (importOriginal) => {
  const { reactive } = await import('vue')
  const route = reactive({ params: { id: '42' }})

  return { ...(await importOriginal<typeof import('vue-router')>()), useRoute: () => route }
})

const counts = (overrides: Partial<NodeNotificationStatus> = {}): NodeNotificationStatus =>
  ({ user: 'admin', yourOutstanding: 2, allOutstanding: 5, total: 40, ...overrides })

// Every test moves the one shared route; a box left mounted from an earlier test would follow it.
enableAutoUnmount(afterEach)

const links = [
  ['your-outstanding-notifications-link', 'yourOutstanding', 'View your outstanding notifications for this node', Person, '2'],
  ['all-outstanding-notifications-link', 'allOutstanding', 'View all outstanding notifications for this node', Group, '5'],
  ['view-all-notifications-link', 'all', 'View all notifications for this node', ViewDetails, '40']
] as const

describe('NodeNotificationsBox.vue', () => {
  let store: ReturnType<typeof useNotificationsStore>

  // Resolves as a successful fetch would: the slice replaced and stamped with the node id.
  const answerWith = (status: NodeNotificationStatus) => {
    const s = store
    s.getNodeNotificationStatus = vi.fn(async (nodeId: string) => {
      s.nodeNotificationStatus = status
      s.nodeNotificationStatusNodeId = nodeId
      s.nodeNotificationStatusFailedNodeId = undefined

      return { success: true, message: '', payload: status }
    })
  }

  // Resolves as a failed fetch would: the slice left alone, the node marked failed.
  const failFetch = () => {
    const s = store
    s.getNodeNotificationStatus = vi.fn(async (nodeId: string) => {
      s.nodeNotificationStatusFailedNodeId = nodeId

      return { success: false, message: 'nope' }
    })
  }

  const mountBox = (setup: () => void = () => answerWith(counts())) => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    store = useNotificationsStore(pinia)
    setup()

    return mount(NodeNotificationsBox, {
      global: {
        plugins: [PrimeVue, pinia],
        stubs: { RouterLink: RouterLinkStub }
      }
    })
  }

  const linkOf = (wrapper: ReturnType<typeof mountBox>, dataTest: string) =>
    wrapper.findAllComponents(RouterLinkStub).find(l => l.attributes('data-test') === dataTest)!

  const countsShown = (wrapper: ReturnType<typeof mountBox>) =>
    links.map(([dataTest]) => wrapper.find(`[data-test="${dataTest}-count"]`))

  // Where the tooltip directive parks its resolved text; see OnmsTooltip.test.ts.
  const tooltipOf = (el: Element) => (el as never as Record<string, unknown>).$_ptooltipValue

  beforeEach(() => {
    vi.useFakeTimers()
    ;(useRoute() as any).params.id = '42'
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('has the panels\' untinted background, without the severity strip', () => {
    const box = mountBox().find('[data-test="node-notifications-box"]')

    expect(box.classes()).toContain('node-details-banner--none')
    expect(box.classes()).toContain('node-details-banner--no-strip')
  })

  it('fetches the node\'s notification counts on mount', async () => {
    mountBox()
    await flushPromises()

    expect(store.getNodeNotificationStatus).toHaveBeenCalledWith('42')
  })

  it('has a neutral tag for yours, everyone\'s outstanding, then all of the node\'s notifications', async () => {
    const wrapper = mountBox()
    await flushPromises()

    expect(wrapper.findAllComponents(RouterLinkStub).map(link => link.attributes('data-test'))).toEqual(links.map(([dataTest]) => dataTest))
    expect(wrapper.findAllComponents(OnmsTag).map(tag => tag.props('severity'))).toEqual(['secondary', 'secondary', 'secondary'])
  })

  describe.each(links)('%s', (dataTest, preset, label, icon, count) => {
    it('links to the Notifications page for this node', () => {
      expect(linkOf(mountBox(), dataTest).props('to')).toEqual({ name: 'Notifications', query: { preset, node: '42' }})
    })

    it('says what it is in its name and tooltip', () => {
      const link = linkOf(mountBox(), dataTest)

      expect(link.attributes('aria-label')).toBe(label)
      expect(tooltipOf(link.element)).toBe(label)
    })

    it('shows the count, then the icon', async () => {
      const wrapper = mountBox()
      await flushPromises()
      const link = linkOf(wrapper, dataTest)

      expect(link.find(`[data-test="${dataTest}-count"]`).text()).toBe(count)
      expect(link.findAllComponents(OnmsIcon).map(i => i.props('icon'))).toEqual([markRaw(icon)])
      expect(link.find('.node-notifications-tag-count + .node-notifications-icon').exists()).toBe(true)
    })
  })

  it('shows a count of 0, as the tag is the way to the page too', async () => {
    const wrapper = mountBox(() => answerWith(counts({ yourOutstanding: 0, allOutstanding: 0, total: 0 })))
    await flushPromises()

    expect(countsShown(wrapper).map(c => c.text())).toEqual(['0', '0', '0'])
  })

  it('shows the tags without counts until the counts arrive', () => {
    const wrapper = mountBox(() => {
      store.getNodeNotificationStatus = vi.fn(() => new Promise(() => undefined)) as any
    })

    expect(wrapper.findAllComponents(RouterLinkStub)).toHaveLength(3)
    expect(countsShown(wrapper).some(c => c.exists())).toBe(false)
  })

  it('shows the tags without counts when the counts fail to load', async () => {
    const wrapper = mountBox(failFetch)
    await flushPromises()

    expect(wrapper.findAllComponents(RouterLinkStub)).toHaveLength(3)
    expect(countsShown(wrapper).some(c => c.exists())).toBe(false)
  })

  it('keeps the last counts when a later refresh fails', async () => {
    const wrapper = mountBox()
    await flushPromises()
    failFetch()
    await vi.advanceTimersByTimeAsync(60_000)

    expect(store.getNodeNotificationStatus).toHaveBeenCalled()
    expect(countsShown(wrapper).map(c => c.text())).toEqual(['2', '5', '40'])
  })

  it('refreshes every minute', async () => {
    mountBox()
    await flushPromises()
    expect(store.getNodeNotificationStatus).toHaveBeenCalledTimes(1)

    await vi.advanceTimersByTimeAsync(60_000)
    expect(store.getNodeNotificationStatus).toHaveBeenCalledTimes(2)
  })

  describe('when the node changes', () => {
    it('refetches, and links to the new node', async () => {
      const wrapper = mountBox()
      await flushPromises()
      ;(useRoute() as any).params.id = '43'
      await flushPromises()

      expect(store.getNodeNotificationStatus).toHaveBeenLastCalledWith('43')
      expect(linkOf(wrapper, 'view-all-notifications-link').props('to')).toEqual({ name: 'Notifications', query: { preset: 'all', node: '43' }})
    })

    it('does not show the previous node\'s counts while the new ones load', async () => {
      const wrapper = mountBox()
      await flushPromises()
      store.getNodeNotificationStatus = vi.fn(() => new Promise(() => undefined)) as any
      ;(useRoute() as any).params.id = '43'
      await nextTick()

      expect(countsShown(wrapper).some(c => c.exists())).toBe(false)
    })
  })
})

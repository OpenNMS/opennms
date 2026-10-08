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

import NodeScheduledOutagesBox from '@/components/Nodes/NodeScheduledOutagesBox.vue'
import { useScheduledOutageStore } from '@/stores/scheduledOutageStore'
import { createTestingPinia } from '@pinia/testing'
import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'

vi.mock('vue-router', async () => {
  const { reactive } = await import('vue')
  const route = reactive({ params: { id: '42' }})

  return { useRoute: () => route }
})

const isAdmin = ref(true)
vi.mock('@/composables/useRole', () => ({
  default: () => ({ adminRole: computed(() => isAdmin.value) })
}))

enableAutoUnmount(afterEach)

const RouterLink = {
  name: 'RouterLink',
  props: ['to'],
  template: '<a :data-to="JSON.stringify(to)"><slot /></a>'
}

describe('NodeScheduledOutagesBox.vue', () => {
  let store: ReturnType<typeof useScheduledOutageStore>

  // Resolves as a successful fetch would: the slice replaced and stamped with the node id.
  const answerWith = (names: string[]) => {
    const s = store
    s.getNodeActiveOutages = vi.fn(async (nodeId: string) => {
      s.nodeActiveOutageNames = names
      s.nodeActiveOutagesNodeId = nodeId

      return { success: true, message: '' }
    })
  }

  const mountBox = (setup: () => void) => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    store = useScheduledOutageStore(pinia)
    setup()

    return mount(NodeScheduledOutagesBox, { global: { plugins: [pinia], stubs: { RouterLink }}})
  }

  beforeEach(() => {
    vi.useFakeTimers()
    isAdmin.value = true
    ;(useRoute() as any).params.id = '42'
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('checks the node on mount', async () => {
    mountBox(() => answerWith([]))
    await flushPromises()

    expect(store.getNodeActiveOutages).toHaveBeenCalledWith('42')
  })

  // As the legacy page: nothing at all unless an outage applies.
  it('renders nothing when no scheduled outage is in effect', async () => {
    const wrapper = mountBox(() => answerWith([]))
    await flushPromises()

    expect(wrapper.find('[data-test="node-scheduled-outages"]').exists()).toBe(false)
  })

  it('renders nothing when the check fails', async () => {
    const wrapper = mountBox(() => {
      store.getNodeActiveOutages = vi.fn().mockResolvedValue({ success: false, message: 'nope' })
    })
    await flushPromises()

    expect(wrapper.find('[data-test="node-scheduled-outages"]').exists()).toBe(false)
  })

  it('lists the outages in effect in a critical banner', async () => {
    const wrapper = mountBox(() => answerWith(['Weekend maintenance', 'Patch window']))
    await flushPromises()

    const banner = wrapper.find('[data-test="node-scheduled-outages"]')
    expect(banner.classes()).toContain('node-details-banner--critical')
    expect(banner.text().replace(/\s+/g, ' '))
      .toBe('This node is currently affected by the following scheduled outages: Weekend maintenance, Patch window')
  })

  it('links each outage to its editor for an admin', async () => {
    const wrapper = mountBox(() => answerWith(['Weekend maintenance']))
    await flushPromises()

    const link = wrapper.find('[data-test="node-scheduled-outage-link"]')
    expect(JSON.parse(link.attributes('data-to')!))
      .toEqual({ path: '/scheduled-outages/edit', query: { name: 'Weekend maintenance' }})
  })

  // The editor route is admin-only, so anyone else just sees the name.
  it('shows plain names to a non-admin', async () => {
    isAdmin.value = false
    const wrapper = mountBox(() => answerWith(['Weekend maintenance']))
    await flushPromises()

    expect(wrapper.find('[data-test="node-scheduled-outage-link"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="node-scheduled-outage-name"]').text()).toBe('Weekend maintenance')
  })

  it('does not show the previous node\'s outages after moving to another node', async () => {
    const wrapper = mountBox(() => answerWith(['Weekend maintenance']))
    await flushPromises()

    store.getNodeActiveOutages = vi.fn(() => new Promise(() => undefined)) as any
    ;(useRoute() as any).params.id = '99'
    await flushPromises()

    expect(store.getNodeActiveOutages).toHaveBeenCalledWith('99')
    expect(wrapper.find('[data-test="node-scheduled-outages"]').exists()).toBe(false)
  })

  it('rechecks every minute, so a window opening or closing shows up', async () => {
    mountBox(() => answerWith([]))
    await flushPromises()
    vi.mocked(store.getNodeActiveOutages).mockClear()

    await vi.advanceTimersByTimeAsync(60_000)

    expect(store.getNodeActiveOutages).toHaveBeenCalledTimes(1)
  })
})

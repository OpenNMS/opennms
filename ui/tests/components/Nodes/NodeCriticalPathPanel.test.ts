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

import NodeCriticalPathPanel from '@/components/Nodes/NodeCriticalPathPanel.vue'
import { useNodeStore } from '@/stores/nodeStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { computed, ref } from 'vue'

const push = vi.fn()
vi.mock('vue-router', () => ({ useRouter: () => ({ push }) }))

const isAdmin = ref(true)
vi.mock('@/composables/useRole', () => ({
  default: () => ({ adminRole: computed(() => isAdmin.value) })
}))

const mountPanel = (criticalPath?: object) => {
  const pinia = createTestingPinia({ createSpy: vi.fn })
  useNodeStore(pinia).criticalPath = criticalPath as never

  return mount(NodeCriticalPathPanel, { global: { plugins: [pinia, PrimeVue] }})
}

describe('NodeCriticalPathPanel.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    isAdmin.value = true
  })

  it('shows the node\'s critical path address and service', () => {
    const wrapper = mountPanel({ criticalPathIp: '10.0.0.3', criticalPathServiceName: 'ICMP' })

    expect(wrapper.text()).toContain('Path Outage - Critical Path')
    expect(wrapper.find('[data-test="critical-path"]').text()).toBe('10.0.0.3 (ICMP)')
  })

  it('shows just the address when no service is recorded', () => {
    const wrapper = mountPanel({ criticalPathIp: '10.0.0.3', criticalPathServiceName: null })

    expect(wrapper.find('[data-test="critical-path"]').text()).toBe('10.0.0.3')
  })

  // As the legacy page: no panel at all for a node without a critical path of its own.
  it('renders nothing for a node with no critical path', () => {
    expect(mountPanel(undefined).find('[data-test="critical-path-panel"]').exists()).toBe(false)
  })

  it('gives an admin a button to the Path Outages configuration tab', async () => {
    const wrapper = mountPanel({ criticalPathIp: '10.0.0.3', criticalPathServiceName: 'ICMP' })

    await wrapper.find('[data-test="configure-path-outages-button"]').trigger('click')
    await flushPromises()

    expect(push).toHaveBeenCalledWith({ path: '/notifications-config', query: { tab: 'path-outages' }})
  })

  // The configuration page is admin-only.
  it('does not offer the configuration button to a non-admin', () => {
    isAdmin.value = false
    const wrapper = mountPanel({ criticalPathIp: '10.0.0.3', criticalPathServiceName: 'ICMP' })

    expect(wrapper.find('[data-test="configure-path-outages-button"]').exists()).toBe(false)
  })
})

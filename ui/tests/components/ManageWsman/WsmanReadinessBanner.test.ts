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

import WsmanReadinessBanner from '@/components/ManageWsman/WsmanReadinessBanner.vue'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it, vi } from 'vitest'

const BASE = { ready: true, pollerService: true, pollerMonitor: true, pollerPackage: 'example1', collectdService: true, collectdCollector: true, servers: 3, polledServers: 3, unpolledServers: 0, requisitionsWithUnpolled: [] }

const mountBanner = (readiness: any, action = vi.fn().mockResolvedValue(null)) => {
  const wrapper = mount(WsmanReadinessBanner, {
    props: { readiness, requisitionsUrl: '/opennms/admin/ng-requisitions/index.jsp', runAction: action },
    global: { plugins: [PrimeVue] }
  })
  return { wrapper, action }
}

describe('WsmanReadinessBanner.vue', () => {
  it('renders nothing when everything is ready and servers are polled', () => {
    expect(mountBanner(BASE).wrapper.html()).toBe('<!--v-if-->')
  })

  it('explains a missing poller service and offers to enable polling', async () => {
    const { wrapper, action } = mountBanner({ ...BASE, ready: false, pollerService: false, pollerMonitor: false, pollerPackage: null, servers: 0, polledServers: 0 })
    expect(wrapper.find('[data-test="readiness-not-ready"]').text()).toContain('poller-configuration.xml')
    await wrapper.find('[data-test="enable-polling"]').trigger('click')
    await flushPromises()
    expect(action).toHaveBeenCalledWith('enable-polling')
  })

  it('offers a rescan when servers were provisioned before polling was enabled, and shows the reason on failure', async () => {
    const action = vi.fn().mockResolvedValue('Failed to rescan the requisitions.')
    const { wrapper } = mountBanner({ ...BASE, servers: 2, polledServers: 0, unpolledServers: 2, requisitionsWithUnpolled: ['wsman-lab'] }, action)
    expect(wrapper.find('[data-test="readiness-unpolled"]').text()).toContain('wsman-lab')
    await wrapper.find('[data-test="rescan"]').trigger('click')
    await flushPromises()
    expect(action).toHaveBeenCalledWith('rescan')
    expect(wrapper.find('[data-test="readiness-message"]').text()).toContain('Failed to rescan')
  })

  it('points at provisioning when there are no servers at all', () => {
    const { wrapper } = mountBanner({ ...BASE, servers: 0, polledServers: 0 })
    expect(wrapper.find('[data-test="readiness-no-servers"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="requisitions-link"]').attributes('href')).toBe('/opennms/admin/ng-requisitions/index.jsp')
  })
})

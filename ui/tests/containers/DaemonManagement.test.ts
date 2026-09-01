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

import DaemonManagement from '@/containers/DaemonManagement.vue'
import DaemonManagementTable from '@/components/DaemonManagement/DaemonManagementTable.vue'
import { useMenuStore } from '@/stores/menuStore'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const mountContainer = () => mount(DaemonManagement, {
  global: {
    plugins: [PrimeVue],
    stubs: { DaemonManagementTable: true, BreadCrumbs: true }
  }
})

describe('DaemonManagement.vue (container)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ createSpy: vi.fn, stubActions: false }))
    useMenuStore().mainMenu = { homeUrl: '/opennms/index.jsp' } as any
  })

  it('renders the page title and the daemons table', () => {
    const wrapper = mountContainer()
    expect(wrapper.find('h1.page-title').text()).toBe('Daemon Management')
    expect(wrapper.findComponent(DaemonManagementTable).exists()).toBe(true)
  })

  it('points the Home crumb at the OpenNMS home page, not the SPA root', () => {
    const wrapper = mountContainer()
    const crumbs = (wrapper.vm as any).breadcrumbs
    expect(crumbs[0]).toEqual({ label: 'Home', to: '/opennms/index.jsp', isAbsoluteLink: true })
    expect(crumbs[1]).toEqual({ label: 'Daemon Management', to: '#', position: 'last' })
  })
})

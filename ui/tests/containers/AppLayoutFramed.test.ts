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

import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { createRouter, createWebHashHistory } from 'vue-router'
import { defineComponent, h } from 'vue'
import App from '@/main/App.vue'

// While the SPA is inside an iframe it is always chromeless. A separate file from
// AppLayoutClasses because vi.mock of the framing helper is hoisted for the whole file.
vi.mock('@/lib/framing', () => ({ isFramed: () => true, hidesMenuInFrame: () => true }))
vi.mock('@/components/Menu/Menubar.vue', () => ({
  default: { name: 'Menubar', render: () => h('div', { class: 'stub-menubar' }) }
}))
vi.mock('@/components/Menu/SideMenu.vue', () => ({
  default: { name: 'SideMenu', render: () => h('div', { class: 'stub-sidemenu' }) }
}))
vi.mock('@/components/Layout/Footer.vue', () => ({
  default: { name: 'Footer', render: () => h('div', { class: 'stub-footer' }) }
}))
vi.mock('@/components/Common/Spinner.vue', () => ({
  default: { name: 'Spinner', render: () => h('div') }
}))
vi.mock('@opennms/onms-ui', async importOriginal => ({
  ...(await importOriginal<typeof import('@opennms/onms-ui')>()),
  OnmsToastHost: { name: 'OnmsToastHost', render: () => h('div') }
}))

const router = createRouter({
  history: createWebHashHistory(),
  routes: [{ path: '/nodes', name: 'Nodes', component: defineComponent({ name: 'NodesPage', render: () => h('div') }) }]
})

const mounted: { unmount: () => void }[] = []

afterEach(() => {
  while (mounted.length) {
    mounted.pop()!.unmount()
  }
  document.body.innerHTML = ''
})

describe('App layout when framed', () => {
  it('renders chromeless on a plain route and keeps it on Escape', async () => {
    await router.push('/nodes')
    await router.isReady()
    const wrapper = mount(App, {
      global: {
        plugins: [router, createTestingPinia()],
        stubs: { OnmsAppLayout: { template: '<div :class="$attrs.class"><slot name="header" /><slot /><slot name="footer" /></div>' }}
      }
    })
    mounted.push(wrapper)

    expect(wrapper.html()).toContain('app-layout-chromeless')
    expect(wrapper.find('.stub-menubar').exists()).toBe(false)
    expect(wrapper.find('.stub-sidemenu').exists()).toBe(false)
    expect(wrapper.find('.stub-footer').exists()).toBe(false)

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
    await wrapper.vm.$nextTick()
    expect(wrapper.html()).toContain('app-layout-chromeless')
    expect(wrapper.find('.stub-menubar').exists()).toBe(false)
  })
})

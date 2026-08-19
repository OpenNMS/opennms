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
import { describe, expect, it, vi } from 'vitest'
import { createRouter, createWebHashHistory } from 'vue-router'
import { defineComponent, h } from 'vue'
import App from '@/main/App.vue'

// The topology page has to sit exactly inside the layout's main grid row, which
// two route-scoped classes arrange: one bounds the layout to the viewport, the
// other passes that height down. Without a test, renaming the route or adding a
// new one silently unbounds the layout and puts the footer below the fold again,
// which is the bug those classes were added to fix.
// vi.mock is hoisted, so each factory builds its own stub rather than closing
// over a shared helper.
const blank = (name: string) => defineComponent({ name, render: () => h('div') })

vi.mock('@/components/Menu/Menubar.vue', () => ({
  default: { name: 'Menubar', render: () => h('div') }
}))
vi.mock('@/components/Menu/SideMenu.vue', () => ({
  default: { name: 'SideMenu', render: () => h('div') }
}))
vi.mock('@/components/Layout/Footer.vue', () => ({
  default: { name: 'Footer', render: () => h('div') }
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
  routes: [
    { path: '/topology/:source?', name: 'Topology', component: blank('TopologyPage') },
    { path: '/nodes', name: 'Nodes', component: blank('NodesPage') }
  ]
})

const mountAt = async (path: string) => {
  await router.push(path)
  await router.isReady()
  const wrapper = mount(App, {
    global: {
      // stubActions: App.vue's onMounted kicks off several store fetches, and this
      // test is about two CSS classes.
      plugins: [router, createTestingPinia()],
      stubs: { OnmsAppLayout: { template: '<div :class="$attrs.class"><slot /></div>' }}
    }
  })
  return wrapper
}

describe('App layout classes', () => {
  it('bounds the layout and fills the cell on the topology route', async () => {
    const wrapper = await mountAt('/topology/custom')
    expect(wrapper.find('.main-content').classes()).toContain('main-content-fill')
    expect(wrapper.html()).toContain('app-layout-bounded')
  })

  it('leaves every other route alone', async () => {
    const wrapper = await mountAt('/nodes')
    expect(wrapper.find('.main-content').classes()).not.toContain('main-content-fill')
    expect(wrapper.html()).not.toContain('app-layout-bounded')
  })
})

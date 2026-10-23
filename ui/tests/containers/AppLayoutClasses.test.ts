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
import { useOnmsChromeless } from '@opennms/onms-ui'

// The topology page has to sit exactly inside the layout's main grid row, which
// two route-scoped classes arrange: one bounds the layout to the viewport, the
// other passes that height down. Without a test, renaming the route or adding a
// new one silently unbounds the layout and puts the footer below the fold again,
// which is the bug those classes were added to fix. Routes with meta.chromeless (wall displays,
// plugin child routes) drop the Menubar, side rail and footer.
// vi.mock is hoisted, so each factory builds its own stub rather than closing
// over a shared helper.
const blank = (name: string) => defineComponent({ name, render: () => h('div') })

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
  routes: [
    { path: '/topology/:source?', name: 'Topology', component: blank('TopologyPage') },
    { path: '/nodes', name: 'Nodes', component: blank('NodesPage') },
    {
      path: '/plugins/exampleUiExtension/:resourceRootPath/:moduleFileName',
      name: 'Plugin-exampleUiExtension',
      component: blank('PluginHost'),
      children: [
        { path: 'board', name: 'PluginBoard', component: blank('PluginBoard'), meta: { chromeless: true }},
        { path: 'editor', name: 'PluginEditor', component: blank('PluginEditor') }
      ]
    }
  ]
})

// Mounted wrappers are tracked and torn down: a live App keeps its router
// watchers, and a later test's navigation would drive a stale instance.
const mounted: { unmount: () => void }[] = []

afterEach(() => {
  while (mounted.length) {
    mounted.pop()!.unmount()
  }
  document.body.innerHTML = ''
  // Chromeless state is module-level and outlives the mounted App.
  useOnmsChromeless().exitChromeless()
  vi.clearAllMocks()
})

const mountAt = async (path: string) => {
  await router.push(path)
  await router.isReady()
  const wrapper = mount(App, {
    global: {
      // stubActions: App.vue's onMounted kicks off several store fetches, and this
      // test is about the layout's classes and chrome.
      plugins: [router, createTestingPinia()],
      stubs: { OnmsAppLayout: { template: '<div :class="$attrs.class"><slot name="header" /><slot /><slot name="footer" /></div>' }}
    }
  })
  mounted.push(wrapper)
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

  it('drops the chrome on a plugin child route with meta.chromeless, not on its sibling', async () => {
    const wrapper = await mountAt('/plugins/exampleUiExtension/example-ui/example.es.js/board')
    expect(wrapper.html()).toContain('app-layout-chromeless')
    expect(wrapper.find('.stub-menubar').exists()).toBe(false)
    expect(wrapper.find('.stub-sidemenu').exists()).toBe(false)
    expect(wrapper.find('.stub-footer').exists()).toBe(false)

    await router.push('/plugins/exampleUiExtension/example-ui/example.es.js/editor')
    await wrapper.vm.$nextTick()
    expect(wrapper.html()).not.toContain('app-layout-chromeless')
    expect(wrapper.find('.stub-menubar').exists()).toBe(true)
    expect(wrapper.find('.stub-footer').exists()).toBe(true)
  })

  it('brings the chrome back on Escape until the next navigation', async () => {
    const board = '/plugins/exampleUiExtension/example-ui/example.es.js/board'
    const wrapper = await mountAt(board)
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.stub-menubar').exists()).toBe(true)
    expect(wrapper.html()).not.toContain('app-layout-chromeless')

    await router.push('/nodes')
    await router.push(board)
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.stub-menubar').exists()).toBe(false)
  })

  it('leaves the chrome hidden when Escape is closing a dialog', async () => {
    const wrapper = await mountAt('/plugins/exampleUiExtension/example-ui/example.es.js/board')
    const mask = document.createElement('div')
    mask.className = 'p-dialog-mask p-overlay-mask'
    document.body.appendChild(mask)
    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.stub-menubar').exists()).toBe(false)
  })

  it('drops the chrome on enterChromeless() and restores it on Escape', async () => {
    const wrapper = await mountAt('/nodes')
    useOnmsChromeless().enterChromeless()
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.stub-menubar').exists()).toBe(false)
    expect(wrapper.html()).toContain('app-layout-chromeless')

    document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.stub-menubar').exists()).toBe(true)
  })
})

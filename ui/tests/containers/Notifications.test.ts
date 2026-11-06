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

import Notifications from '@/containers/Notifications.vue'
import { SAME_PAGE_NAVIGATION_EVENT } from '@/lib/navigation'
import { useAuthStore } from '@/stores/authStore'
import { useNotificationsStore } from '@/stores/notificationsStore'
import { createSuccessResponse } from '@/types/validation'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { reactive } from 'vue'

const mounted: { unmount: () => void }[] = []

const route = reactive({ name: 'Notifications', query: {} as Record<string, string> })
const replace = vi.fn((to: { query: Record<string, string> }) => {
  route.query = to.query
})
vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: vi.fn(), replace })
}))

const report = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useActionFeedback', () => ({
  default: () => ({ withSpinner: (fn: () => unknown) => fn(), report })
}))

const mountPage = (query: Record<string, string>, initial: Partial<ReturnType<typeof useNotificationsStore>> = {}) => {
  route.query = query
  const pinia = createTestingPinia({ stubActions: true })
  const auth = useAuthStore(pinia)
  auth.loaded = true
  auth.whoAmI = { id: 'admin', fullName: 'Admin', internal: true, roles: [] }
  const store = useNotificationsStore(pinia)
  Object.assign(store, initial)
  vi.mocked(store.applyQuery).mockImplementation(async (preset, nodeId) => {
    store.preset = preset
    store.nodeFilter = nodeId ? { id: nodeId, label: null } : null
    return createSuccessResponse()
  })
  vi.mocked(store.setNodeFilter).mockImplementation(async (nodeId) => {
    store.nodeFilter = nodeId ? { id: nodeId, label: null } : null
    return createSuccessResponse()
  })
  const wrapper = mount(Notifications, {
    global: {
      plugins: [PrimeVue, pinia],
      stubs: { BreadCrumbs: true, NotificationQueriesCard: true, NotificationsTable: true, NotificationsHelpDialog: true }
    }
  })
  mounted.push(wrapper)
  return { wrapper, store }
}

describe('Notifications page URL', () => {
  beforeEach(() => {
    replace.mockClear()
    report.mockClear()
    route.name = 'Notifications'
  })

  // the mocked route is shared, so earlier pages must stop watching it
  afterEach(() => {
    mounted.splice(0).forEach(w => w.unmount())
  })

  it('applies the preset and node from the URL', async () => {
    const { store } = mountPage({ preset: 'yourAcknowledged', node: '16' })
    await flushPromises()

    expect(store.applyQuery).toHaveBeenCalledTimes(1)
    expect(store.applyQuery).toHaveBeenCalledWith('yourAcknowledged', '16')
    expect(replace).not.toHaveBeenCalled()
  })

  it('ignores an unknown preset and a non-numeric node', async () => {
    const { store } = mountPage({ preset: 'bogus', node: 'abc' })
    await flushPromises()

    expect(store.load).toHaveBeenCalledTimes(1)
    expect(store.applyQuery).not.toHaveBeenCalled()
    expect(store.setNodeFilter).not.toHaveBeenCalled()
  })

  it('ignores a node id too large to be exact', async () => {
    const { store } = mountPage({ node: '99999999999999999999' })
    await flushPromises()

    expect(store.setNodeFilter).not.toHaveBeenCalled()
    expect(store.applyQuery).not.toHaveBeenCalled()
    expect(store.nodeFilter).toBeNull()
  })

  it('reloads the current view on a URL without a preset, keeping a user search', async () => {
    const { store } = mountPage({})
    await flushPromises()

    expect(store.load).toHaveBeenCalledTimes(1)
    expect(store.applyQuery).not.toHaveBeenCalled()

    store.preset = 'userSearch'
    store.userFilter = 'bob'
    await flushPromises() // the page drops the default preset from the URL
    vi.mocked(store.load).mockClear()
    window.dispatchEvent(new CustomEvent(SAME_PAGE_NAVIGATION_EVENT))
    await flushPromises()

    expect(store.load).toHaveBeenCalledTimes(1)
    expect(store.applyQuery).not.toHaveBeenCalled()
    expect(store.preset).toBe('userSearch')
    expect(store.userFilter).toBe('bob')
  })

  // A load that finishes after the user left must not write this page's query onto the new page's
  // URL, or show its message there.
  it.each([
    ['a bare URL', {}],
    ['a node only', { node: '16' }],
    ['a preset', { preset: 'allOutstanding' }]
  ])('leaves the URL and the message alone when the page closes during a load from %s', async (_name, query) => {
    let finish: () => void = () => undefined
    const pending = new Promise<void>((resolve) => {
      finish = resolve
    })
    const { store } = mountPage(query)
    await flushPromises() // the first load, which resolves at once
    for (const action of [store.load, store.setNodeFilter, store.applyQuery]) {
      vi.mocked(action).mockImplementation(async () => {
        await pending
        return createSuccessResponse()
      })
    }
    // the page mounted before the slow mocks above, so run its load again through the menu event
    replace.mockClear()
    report.mockClear()
    window.dispatchEvent(new CustomEvent(SAME_PAGE_NAVIGATION_EVENT))
    await flushPromises()

    route.name = 'Home'
    finish()
    await flushPromises()

    expect(replace).not.toHaveBeenCalled()
    expect(report).not.toHaveBeenCalled()
  })

  it('applies only the node when the URL has a node but no preset', async () => {
    const { store } = mountPage({ node: '16' })
    await flushPromises()

    expect(store.setNodeFilter).toHaveBeenCalledWith('16')
    expect(store.applyQuery).not.toHaveBeenCalled()
    expect(store.load).not.toHaveBeenCalled()
  })

  it('does not rewrite the URL when a bare-URL reload runs during a user search', async () => {
    const { store } = mountPage({})
    await flushPromises()
    store.preset = 'userSearch'
    store.userFilter = 'bob'
    await flushPromises()
    replace.mockClear()
    vi.mocked(store.load).mockClear()

    window.dispatchEvent(new CustomEvent(SAME_PAGE_NAVIGATION_EVENT))
    await flushPromises()

    expect(replace).not.toHaveBeenCalled()
    expect(store.load).toHaveBeenCalledTimes(1)
  })

  it('writes the view on screen to a bare URL, loading once', async () => {
    const { store } = mountPage({}, { preset: 'allAcknowledged', nodeFilter: { id: '16', label: null }})
    await flushPromises()

    expect(store.load).toHaveBeenCalledTimes(1)
    expect(replace).toHaveBeenCalledTimes(1)
    expect(replace).toHaveBeenCalledWith({ query: { preset: 'allAcknowledged', node: '16' }})
  })

  it('leaves a bare URL alone during a user search without a node', async () => {
    const { store } = mountPage({}, { preset: 'userSearch' })
    await flushPromises()

    expect(store.load).toHaveBeenCalledTimes(1)
    expect(replace).not.toHaveBeenCalled()
  })

  it('writes the default preset to a bare URL for a fresh store, loading once', async () => {
    const { store } = mountPage({})
    await flushPromises()

    expect(store.load).toHaveBeenCalledTimes(1)
    expect(replace).toHaveBeenCalledTimes(1)
    expect(replace).toHaveBeenCalledWith({ query: { preset: 'yourOutstanding' }})
  })

  it('writes a preset chosen on the page to the URL, without loading twice', async () => {
    const { store } = mountPage({ node: '16' })
    await flushPromises()
    vi.mocked(store.applyQuery).mockClear()

    store.preset = 'allAcknowledged'
    await flushPromises()

    expect(replace).toHaveBeenCalledWith({ query: { preset: 'allAcknowledged', node: '16' }})
    expect(store.applyQuery).not.toHaveBeenCalled()
  })

  it('drops node from the URL when the node filter is cleared', async () => {
    const { store } = mountPage({ preset: 'allOutstanding', node: '16' })
    await flushPromises()

    store.nodeFilter = null
    await flushPromises()

    expect(replace).toHaveBeenLastCalledWith({ query: { preset: 'allOutstanding' }})
  })

  it('keeps userSearch out of the URL', async () => {
    const { store } = mountPage({ preset: 'allOutstanding' })
    await flushPromises()

    store.preset = 'userSearch'
    await flushPromises()

    expect(replace).toHaveBeenLastCalledWith({ query: {}})
  })
})

import Notifications from '@/containers/Notifications.vue'
import { useAuthStore } from '@/stores/authStore'
import { useNotificationsStore } from '@/stores/notificationsStore'
import { createSuccessResponse } from '@/types/validation'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { reactive } from 'vue'

const mounted: { unmount: () => void }[] = []

const route = reactive({ query: {} as Record<string, string> })
const replace = vi.fn((to: { query: Record<string, string> }) => {
  route.query = to.query
})
vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: vi.fn(), replace })
}))

vi.mock('@/composables/useActionFeedback', () => ({
  default: () => ({ withSpinner: (fn: () => unknown) => fn(), report: vi.fn() })
}))

const mountPage = (query: Record<string, string>) => {
  route.query = query
  const pinia = createTestingPinia({ stubActions: true })
  const auth = useAuthStore(pinia)
  auth.loaded = true
  auth.whoAmI = { id: 'admin', fullName: 'Admin', internal: true, roles: [] }
  const store = useNotificationsStore(pinia)
  vi.mocked(store.applyQuery).mockImplementation(async (preset, nodeId) => {
    store.preset = preset
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

    expect(store.applyQuery).toHaveBeenCalledWith('yourOutstanding', null)
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

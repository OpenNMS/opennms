import NotificationQueriesCard from '@/components/Notifications/NotificationQueriesCard.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useNotificationsStore } from '@/stores/notificationsStore'
import { createSuccessResponse } from '@/types/validation'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBar } = vi.hoisted(() => ({ showSnackBar: vi.fn() }))
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar })
}))

// The three select buttons share one selection; this reads which option (if
// any) each group shows as pressed.
const pressed = (wrapper: VueWrapper, group: string) =>
  wrapper.find(`[data-test="${group}"]`).findAll('button').filter(b => b.attributes('aria-pressed') === 'true').map(b => b.text())

const clickOption = async (wrapper: VueWrapper, group: string, label: string) => {
  const button = wrapper.find(`[data-test="${group}"]`).findAll('button').find(b => b.text() === label)
  expect(button, `option "${label}" in ${group}`).toBeDefined()
  await button!.trigger('click')
  await flushPromises()
}

describe('NotificationQueriesCard', () => {
  let wrapper: VueWrapper
  let store: ReturnType<typeof useNotificationsStore>

  const mountCard = () => {
    wrapper = mount(NotificationQueriesCard, {
      attachTo: document.body,
      global: {
        plugins: [PrimeVue, createTestingPinia({ stubActions: true })]
      }
    })
    store = useNotificationsStore()
    vi.mocked(store.applyPreset).mockResolvedValue(createSuccessResponse())
    vi.mocked(store.load).mockResolvedValue(createSuccessResponse())
    useMenuStore().mainMenu = { baseHref: 'http://localhost:8980/opennms/' } as any
    return wrapper
  }

  beforeEach(() => {
    showSnackBar.mockClear()
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('starts with the store preset selected and nothing else', () => {
    mountCard()

    expect(pressed(wrapper, 'query-presets')).toEqual(['Your outstanding notifications'])
    expect(pressed(wrapper, 'query-user-search')).toEqual([])
    expect(pressed(wrapper, 'query-notification-id')).toEqual([])
  })

  it('applies a preset when one is chosen', async () => {
    mountCard()

    await clickOption(wrapper, 'query-presets', 'All acknowledged notifications')

    expect(store.applyPreset).toHaveBeenCalledWith('allAcknowledged', undefined)
    expect(pressed(wrapper, 'query-presets')).toEqual(['All acknowledged notifications'])
  })

  it('choosing user search with nothing typed enters the awaiting-user state, like Clear', async () => {
    mountCard()

    await clickOption(wrapper, 'query-user-search', 'Notifications for user:')

    expect(pressed(wrapper, 'query-user-search')).toEqual(['Notifications for user:'])
    expect(pressed(wrapper, 'query-presets')).toEqual([])
    // the store (and so the table, title and Refresh) now matches the highlight
    // instead of still showing the previous preset
    expect(store.applyPreset).toHaveBeenCalledWith('userSearch', undefined)
  })

  it('choosing user search re-runs a user id that is already typed', async () => {
    mountCard()
    await wrapper.find('#notification-user-search').setValue('  operator  ')

    await clickOption(wrapper, 'query-user-search', 'Notifications for user:')

    expect(store.applyPreset).toHaveBeenCalledWith('userSearch', 'operator')
  })

  it('clicking user search after focus already highlighted it still runs it (no stale preset under the highlight)', async () => {
    mountCard()
    await wrapper.find('#notification-user-search').trigger('focusin')
    expect(pressed(wrapper, 'query-user-search')).toEqual(['Notifications for user:'])
    expect(store.applyPreset).not.toHaveBeenCalled()

    await clickOption(wrapper, 'query-user-search', 'Notifications for user:')

    expect(store.applyPreset).toHaveBeenCalledTimes(1)
    expect(store.applyPreset).toHaveBeenCalledWith('userSearch', undefined)
  })

  it('refresh runs user search when that is highlighted, not the previous preset', async () => {
    mountCard()
    await wrapper.find('#notification-user-search').setValue('operator')
    await wrapper.find('#notification-user-search').trigger('focusin')

    await wrapper.find('[data-test="query-refresh-button"]').trigger('click')
    await flushPromises()

    expect(store.applyPreset).toHaveBeenCalledWith('userSearch', 'operator')
    expect(store.load).not.toHaveBeenCalled()
  })

  it('choosing a not-yet-highlighted user search runs it once', async () => {
    mountCard()

    await clickOption(wrapper, 'query-user-search', 'Notifications for user:')

    expect(store.applyPreset).toHaveBeenCalledTimes(1)
  })

  it('focusing a search field selects its button without searching', async () => {
    mountCard()
    await wrapper.find('#notification-user-search').setValue('operator')

    await wrapper.find('#notification-user-search').trigger('focusin')

    expect(pressed(wrapper, 'query-user-search')).toEqual(['Notifications for user:'])
    expect(pressed(wrapper, 'query-presets')).toEqual([])
    expect(store.applyPreset).not.toHaveBeenCalled()

    await wrapper.find('#notification-id-search').trigger('focusin')

    expect(pressed(wrapper, 'query-notification-id')).toEqual(['View details for ID:'])
    expect(pressed(wrapper, 'query-user-search')).toEqual([])
  })

  it('searches for the trimmed user id on Enter', async () => {
    mountCard()
    const input = wrapper.find('#notification-user-search')
    await input.setValue(' operator ')

    await input.trigger('keyup', { key: 'Enter' })
    await flushPromises()

    expect(store.applyPreset).toHaveBeenCalledWith('userSearch', 'operator')
  })

  it('clearing the user search stays in user-search mode and waits for a new id', async () => {
    mountCard()
    const input = wrapper.find('#notification-user-search')
    await input.setValue('operator')
    await input.trigger('focusin')

    await wrapper.find('[data-test="user-search-input-clear"]').trigger('click')
    await flushPromises()

    // applyPreset('userSearch') with no user: the store's "awaiting a user" state
    expect(store.applyPreset).toHaveBeenCalledWith('userSearch', undefined)
    expect(pressed(wrapper, 'query-user-search')).toEqual(['Notifications for user:'])
  })

  it('opens the notification detail page on Enter in the id field', async () => {
    const assigned: string[] = []
    const original = window.location
    Object.defineProperty(window, 'location', {
      configurable: true,
      value: {
        ...original,
        set href(url: string) {
          assigned.push(url)
        }
      }
    })
    try {
      mountCard()
      const input = wrapper.find('#notification-id-search')
      await input.setValue(' 42 ')

      await input.trigger('keyup', { key: 'Enter' })

      expect(assigned).toEqual(['http://localhost:8980/opennms/notification/detail.jsp?notice=42'])
    } finally {
      Object.defineProperty(window, 'location', { configurable: true, value: original })
    }
  })

  it('refresh re-runs the current query', async () => {
    mountCard()

    await wrapper.find('[data-test="query-refresh-button"]').trigger('click')
    await flushPromises()

    expect(store.load).toHaveBeenCalledTimes(1)
  })

  it('shows a failed query as an error snackbar', async () => {
    mountCard()
    vi.mocked(store.applyPreset).mockResolvedValue({ success: false, message: 'Failed to load notifications.' })

    await clickOption(wrapper, 'query-presets', 'All outstanding notifications')

    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Failed to load notifications.', error: true })
  })

  it('follows a preset change made elsewhere (e.g. the bell deep link)', async () => {
    mountCard()
    await wrapper.find('#notification-user-search').trigger('focusin')

    store.preset = 'teamOutstanding'
    await flushPromises()

    expect(pressed(wrapper, 'query-presets')).toEqual(['Outstanding for anyone but you'])
    expect(pressed(wrapper, 'query-user-search')).toEqual([])
  })
})

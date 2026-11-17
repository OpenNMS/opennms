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
  })

  it('offers your acknowledged notifications as a preset', () => {
    mountCard()
    expect(wrapper.find('[data-test="query-presets"]').text()).toContain('Your acknowledged notifications')
  })

  it('shows the node filter as a removable chip, and removing it clears the filter', async () => {
    mountCard()
    store.nodeFilter = { id: '16', label: 'Core-Router-01' }
    vi.mocked(store.clearNodeFilter).mockResolvedValue(createSuccessResponse())
    await flushPromises()

    const chip = wrapper.find('[data-test="node-filter-chip"]')
    expect(chip.text()).toContain('Node: Core-Router-01')

    await wrapper.findComponent({ name: 'OnmsChip' }).vm.$emit('remove', new Event('click'))
    await flushPromises()

    expect(store.clearNodeFilter).toHaveBeenCalled()
  })

  it('shows no chip without a node filter', () => {
    mountCard()
    expect(wrapper.find('[data-test="node-filter-chip"]').exists()).toBe(false)
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

  it('focusing the user field selects its button without searching', async () => {
    mountCard()
    await wrapper.find('#notification-user-search').setValue('operator')

    await wrapper.find('#notification-user-search').trigger('focusin')

    expect(pressed(wrapper, 'query-user-search')).toEqual(['Notifications for user:'])
    expect(pressed(wrapper, 'query-presets')).toEqual([])
    expect(store.applyPreset).not.toHaveBeenCalled()
  })

  it('"View details for ID:" is a plain label, not part of the selection', async () => {
    mountCard()
    const label = wrapper.find('[data-test="notification-id-label"]')

    expect(label.element.tagName).toBe('LABEL')
    expect(label.attributes('for')).toBe('notification-id-search')
    expect(label.find('button').exists()).toBe(false)

    // focusing the id field changes neither the highlight nor the query
    await wrapper.find('#notification-id-search').trigger('focusin')
    expect(pressed(wrapper, 'query-presets')).toEqual(['Your outstanding notifications'])
    expect(store.applyPreset).not.toHaveBeenCalled()
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

  describe('View details for ID', () => {
    const original = window.location
    let assigned: string[]

    beforeEach(() => {
      assigned = []
      Object.defineProperty(window, 'location', {
        configurable: true,
        value: {
          ...original,
          set href(url: string) {
            assigned.push(url)
          }
        }
      })
    })

    afterEach(() => {
      Object.defineProperty(window, 'location', { configurable: true, value: original })
    })

    const enterId = async (value: string) => {
      const input = wrapper.find('#notification-id-search')
      await input.setValue(value)
      await input.trigger('keyup', { key: 'Enter' })
    }

    it('opens the detail page for a positive integer id (trimmed)', async () => {
      mountCard()

      await enterId(' 42 ')

      expect(assigned).toEqual(['http://localhost:8980/opennms/notification/detail.jsp?notice=42'])
      expect(showSnackBar).not.toHaveBeenCalled()
    })

    it.each(['0', '00', '-1', '1.5', 'abc', '12a', '1e3', '+5'])('rejects "%s" with an error and does not navigate', async (value) => {
      mountCard()

      await enterId(value)

      expect(assigned).toEqual([])
      expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Enter a notification ID as a positive whole number, e.g. 42.', error: true })
    })

    it('accepts leading zeros as the same id', async () => {
      mountCard()

      await enterId('007')

      expect(assigned).toEqual(['http://localhost:8980/opennms/notification/detail.jsp?notice=7'])
    })

    it('ignores Enter on an empty field', async () => {
      mountCard()

      await enterId('   ')

      expect(assigned).toEqual([])
      expect(showSnackBar).not.toHaveBeenCalled()
    })
  })

  describe('Filter by node', () => {
    const enterNode = async (value: string) => {
      const input = wrapper.find('#notification-node-search')
      await input.setValue(value)
      await input.trigger('keyup', { key: 'Enter' })
      await flushPromises()
    }

    const mountWithNodeActions = () => {
      mountCard()
      vi.mocked(store.setNodeFilter).mockResolvedValue(createSuccessResponse())
      vi.mocked(store.clearNodeFilter).mockResolvedValue(createSuccessResponse())
    }

    it('is a plain label, not part of the selection', async () => {
      mountCard()
      const label = wrapper.find('[data-test="node-search-label"]')

      expect(label.element.tagName).toBe('LABEL')
      expect(label.attributes('for')).toBe('notification-node-search')
      expect(label.text()).toBe('Filter by node:')

      await wrapper.find('#notification-node-search').trigger('focusin')
      expect(pressed(wrapper, 'query-presets')).toEqual(['Your outstanding notifications'])
      expect(store.applyPreset).not.toHaveBeenCalled()
    })

    it('sits between the user search and the notification id field', () => {
      mountCard()
      const fields = wrapper.findAll('.query-searches .query-search').map(field =>
        field.find('[data-test="query-user-search"], [data-test="node-search-label"], [data-test="notification-id-label"]').attributes('data-test'))

      expect(fields).toEqual(['query-user-search', 'node-search-label', 'notification-id-label'])
    })

    it('filters the current view by the trimmed node id on Enter', async () => {
      mountWithNodeActions()
      store.preset = 'allAcknowledged'

      await enterNode(' 153 ')

      expect(store.setNodeFilter).toHaveBeenCalledWith('153')
      expect(store.applyPreset).not.toHaveBeenCalled()
      expect(showSnackBar).not.toHaveBeenCalled()
    })

    it('accepts leading zeros as the same id', async () => {
      mountWithNodeActions()

      await enterNode('007')

      expect(store.setNodeFilter).toHaveBeenCalledWith('7')
    })

    it.each(['abc', '0', '00', '-1', '1.5'])('rejects "%s" with an error and applies nothing', async (value) => {
      mountWithNodeActions()

      await enterNode(value)

      expect(store.setNodeFilter).not.toHaveBeenCalled()
      expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Enter a node ID as a positive whole number, e.g. 42.', error: true })
    })

    it('rejects a node id too large to be exact, applying nothing', async () => {
      mountWithNodeActions()

      await enterNode('99999999999999999999')

      expect(store.setNodeFilter).not.toHaveBeenCalled()
      expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Enter a node ID as a positive whole number, e.g. 42.', error: true })
    })

    it('ignores Enter on an empty field', async () => {
      mountWithNodeActions()

      await enterNode('   ')

      expect(store.setNodeFilter).not.toHaveBeenCalled()
      expect(store.clearNodeFilter).not.toHaveBeenCalled()
      expect(showSnackBar).not.toHaveBeenCalled()
    })

    it('clears the node filter from the input\'s clear button', async () => {
      mountWithNodeActions()
      await wrapper.find('#notification-node-search').setValue('153')

      await wrapper.find('[data-test="node-search-input-clear"]').trigger('click')
      await flushPromises()

      expect(store.clearNodeFilter).toHaveBeenCalled()
    })

    it('follows the store filter: a deep link fills the input, removing it empties it', async () => {
      mountCard()
      const input = wrapper.find('#notification-node-search').element as HTMLInputElement

      store.nodeFilter = { id: '16', label: 'x' }
      await flushPromises()
      expect(input.value).toBe('16')

      store.nodeFilter = null
      await flushPromises()
      expect(input.value).toBe('')
    })

    it('keeps the user search when a node is applied in user-search mode', async () => {
      mountWithNodeActions()
      store.preset = 'userSearch'
      store.userFilter = 'operator'

      await enterNode('153')

      expect(store.setNodeFilter).toHaveBeenCalledWith('153')
      // the user search is neither re-run nor cleared by the component
      expect(store.applyPreset).not.toHaveBeenCalled()
      expect(store.userFilter).toBe('operator')
    })

    it('shows a failed node filter as an error snackbar', async () => {
      mountWithNodeActions()
      vi.mocked(store.setNodeFilter).mockResolvedValue({ success: false, message: 'Failed to load notifications.' })

      await enterNode('153')

      expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Failed to load notifications.', error: true })
    })
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

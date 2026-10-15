import ThresholdGroupEdit from '@/containers/ThresholdGroupEdit.vue'
import { useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { name: 'Threshold Group Create', params: {} as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock })
}))

describe('ThresholdGroupEdit.vue', () => {
  let store: ReturnType<typeof useThresholdGroupStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const mountComponent = async () => {
    wrapper = mount(ThresholdGroupEdit, { global: { plugins: [PrimeVue] }})
    await flushPromises()
    return wrapper
  }

  const type = async (dataTest: string, value: string) => {
    await wrapper.find(`[data-test="${dataTest}"]`).setValue(value)
  }

  beforeEach(() => {
    vi.clearAllMocks()
    route.name = 'Threshold Group Create'
    route.params = {}
    setActivePinia(createTestingPinia({ stubActions: true }))
    store = useThresholdGroupStore()
    store.groups = [{ name: 'mib2', rrdRepository: '/rrd', thresholdCount: 0, expressionCount: 0 }]
    store.fetchGroups = vi.fn().mockResolvedValue(ok)
    store.fetchGroup = vi.fn().mockImplementation(async () => {
      store.currentGroup = { name: 'mib2', rrdRepository: '/rrd', thresholds: [], expressions: [], version: 'v1' }
      return ok
    })
    store.createGroup = vi.fn().mockResolvedValue(ok)
    store.renameGroup = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  describe('create', () => {
    it('reads as a create page', async () => {
      await mountComponent()

      expect(wrapper.find('[data-test="threshold-edit-title"]').text()).toBe('Create New Threshold Group')
      expect(wrapper.find('[data-test="threshold-edit-save"]').text()).toBe('Create Threshold Group')
    })

    it('keeps save disabled while the form is invalid', async () => {
      await mountComponent()
      await type('threshold-group-edit-name', 'mib2')
      await type('threshold-group-edit-repository', '/rrd')

      expect(wrapper.find('[data-test="threshold-edit-save"]').attributes('disabled')).toBeDefined()
    })

    it('creates the group and opens it', async () => {
      await mountComponent()
      await type('threshold-group-edit-name', 'new group')
      await type('threshold-group-edit-repository', '/rrd')
      await wrapper.find('[data-test="threshold-edit-save"]').trigger('click')
      await flushPromises()

      expect(store.createGroup).toHaveBeenCalledWith(expect.objectContaining({ name: 'new group', rrdRepository: '/rrd' }))
      expect(pushMock).toHaveBeenCalledWith('/threshold-config/group/new%20group')
    })

    it('stays on the page with the input when the save fails', async () => {
      store.createGroup = vi.fn().mockResolvedValue({ success: false, message: 'boom' })

      await mountComponent()
      await type('threshold-group-edit-name', 'other')
      await type('threshold-group-edit-repository', '/rrd')
      await wrapper.find('[data-test="threshold-edit-save"]').trigger('click')
      await flushPromises()

      expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'boom', error: true })
      expect(pushMock).not.toHaveBeenCalled()
      expect((wrapper.find('[data-test="threshold-group-edit-name"]').element as HTMLInputElement).value).toBe('other')
    })

    it('goes back to the group list on cancel', async () => {
      await mountComponent()
      await wrapper.find('[data-test="threshold-edit-cancel"]').trigger('click')

      expect(pushMock).toHaveBeenCalledWith('/threshold-config?tab=groups')
    })
  })

  describe('edit', () => {
    beforeEach(() => {
      route.name = 'Threshold Group Edit'
      route.params = { name: 'mib2' }
    })

    it('loads the group into the form', async () => {
      await mountComponent()

      expect(store.fetchGroup).toHaveBeenCalledWith('mib2')
      expect(wrapper.find('[data-test="threshold-edit-title"]').text()).toBe('Edit Threshold Group Details')
      expect((wrapper.find('[data-test="threshold-group-edit-name"]').element as HTMLInputElement).value).toBe('mib2')
    })

    it('renames under the old name, keeping the version, and follows the new name', async () => {
      await mountComponent()
      await type('threshold-group-edit-name', 'renamed')
      await wrapper.find('[data-test="threshold-edit-save"]').trigger('click')
      await flushPromises()

      expect(store.renameGroup).toHaveBeenCalledWith('mib2', expect.objectContaining({ name: 'renamed', version: 'v1' }))
      expect(pushMock).toHaveBeenCalledWith('/threshold-config/group/renamed')
    })

    it('goes back to the group on cancel', async () => {
      await mountComponent()
      await wrapper.find('[data-test="threshold-edit-cancel"]').trigger('click')

      expect(pushMock).toHaveBeenCalledWith('/threshold-config/group/mib2')
    })

    it('shows a not-found state for an unknown group', async () => {
      store.fetchGroup = vi.fn().mockImplementation(async () => {
        store.currentGroup = null
        return { success: false, message: 'gone' }
      })

      await mountComponent()

      expect(wrapper.find('[data-test="threshold-edit-not-found"]').exists()).toBe(true)
    })

    it('refuses to edit a read-only group', async () => {
      store.fetchGroup = vi.fn().mockImplementation(async () => {
        store.currentGroup = { name: 'mib2', rrdRepository: '/rrd', thresholds: [], expressions: [], readOnly: true }
        return ok
      })

      await mountComponent()

      expect(wrapper.find('[data-test="threshold-edit-not-found"]').text()).toContain('read only')
    })
  })
})

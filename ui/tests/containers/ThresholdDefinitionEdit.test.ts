import ThresholdDefinitionEdit from '@/containers/ThresholdDefinitionEdit.vue'
import { ThresholdDefinitionKind } from '@/lib/thresholdValidator'
import { getDefaultExpression, getDefaultThreshold, useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import type { ThresholdGroup } from '@/types/thresholdConfig'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock, route } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  pushMock: vi.fn(),
  route: { params: {} as Record<string, string> }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRoute: () => route,
  useRouter: () => ({ push: pushMock })
}))

describe('ThresholdDefinitionEdit.vue', () => {
  let store: ReturnType<typeof useThresholdGroupStore>
  let wrapper: VueWrapper<any>

  const ok = { success: true, message: '' }

  const group = (overrides: Partial<ThresholdGroup> = {}): ThresholdGroup => ({
    name: 'mib2',
    rrdRepository: '/rrd',
    thresholds: [{ ...getDefaultThreshold(), dsName: 'cpu', value: '90', rearm: '80', trigger: '3' }],
    expressions: [{ ...getDefaultExpression(), expression: 'a + b' }],
    ...overrides
  })

  const mountComponent = async () => {
    wrapper = mount(ThresholdDefinitionEdit, {
      global: {
        plugins: [PrimeVue],
        stubs: { ThresholdDefinitionForm: true, ResourceFilterEditor: true, ThresholdHelpDialog: true }
      }
    })
    await flushPromises()
    return wrapper
  }

  beforeEach(() => {
    vi.clearAllMocks()
    route.params = { name: 'mib2', kind: 'threshold', index: '0' }
    setActivePinia(createTestingPinia({ stubActions: true }))
    store = useThresholdGroupStore()
    store.fetchGroup = vi.fn().mockImplementation(async () => {
      store.currentGroup = group()
      return ok
    })
    store.fetchMetadata = vi.fn().mockResolvedValue(ok)
    // The real lookup: the page must edit a copy of what the store holds.
    store.definitionAt = vi.fn().mockImplementation((kind: ThresholdDefinitionKind, index: number | null) => {
      if (index === null) {
        return kind === ThresholdDefinitionKind.Threshold ? getDefaultThreshold() : getDefaultExpression()
      }
      const definitions = kind === ThresholdDefinitionKind.Threshold ? store.currentGroup?.thresholds : store.currentGroup?.expressions
      return definitions?.[index] ? { ...definitions[index] } : null
    })
    store.saveDefinition = vi.fn().mockResolvedValue(ok)
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('edits the definition the route points at', async () => {
    await mountComponent()

    expect(store.fetchGroup).toHaveBeenCalledWith('mib2')
    expect(wrapper.find('[data-test="threshold-edit-title"]').text()).toBe('Edit Threshold Details')
    expect(wrapper.findComponent({ name: 'ThresholdDefinitionForm' }).props('modelValue')).toMatchObject({ dsName: 'cpu' })
  })

  it('names expression thresholds in create mode', async () => {
    route.params = { name: 'mib2', kind: 'expression' }

    await mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-title"]').text()).toBe('Create New Expression Threshold')
    expect(wrapper.find('[data-test="threshold-edit-save"]').text()).toBe('Create Expression Threshold')
  })

  it('saves at the index and returns to the group', async () => {
    await mountComponent()
    await wrapper.find('[data-test="threshold-edit-save"]').trigger('click')
    await flushPromises()

    expect(store.saveDefinition).toHaveBeenCalledWith(ThresholdDefinitionKind.Threshold, 0, expect.objectContaining({ dsName: 'cpu' }))
    expect(pushMock).toHaveBeenCalledWith('/threshold-config/group/mib2')
  })

  it('stays on the page when the save fails', async () => {
    store.saveDefinition = vi.fn().mockResolvedValue({ success: false, message: 'conflict' })

    await mountComponent()
    await wrapper.find('[data-test="threshold-edit-save"]').trigger('click')
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'conflict', error: true })
    expect(pushMock).not.toHaveBeenCalled()
  })

  it('shows a not-found state for an index the group does not have', async () => {
    route.params = { name: 'mib2', kind: 'threshold', index: '7' }

    await mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-not-found"]').exists()).toBe(true)
  })

  it('refuses to edit a read-only group', async () => {
    store.fetchGroup = vi.fn().mockImplementation(async () => {
      store.currentGroup = group({ readOnly: true })
      return ok
    })

    await mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-not-found"]').text()).toContain('read only')
  })
})

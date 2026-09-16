import ThresholdDefinitionTable from '@/components/ThresholdConfiguration/Group/ThresholdDefinitionTable.vue'
import ThresholdUeiCell from '@/components/ThresholdConfiguration/Group/ThresholdUeiCell.vue'
import { ThresholdDefinitionKind } from '@/lib/thresholdValidator'
import { getDefaultExpression, getDefaultThreshold, useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import { CreateEditMode } from '@/types'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock } = vi.hoisted(() => ({ showSnackBarMock: vi.fn() }))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

describe('ThresholdDefinitionTable.vue', () => {
  let store: ReturnType<typeof useThresholdGroupStore>
  let wrapper: VueWrapper<any>

  const mountComponent = (kind: ThresholdDefinitionKind, readOnly = false) => {
    wrapper = mount(ThresholdDefinitionTable, {
      props: { kind, readOnly },
      global: { plugins: [PrimeVue], stubs: { 'router-link': true }}
    })
    return wrapper
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ stubActions: false }))
    store = useThresholdGroupStore()
    store.currentGroup = {
      name: 'mib2',
      rrdRepository: '/rrd',
      thresholds: [{ ...getDefaultThreshold(), dsName: 'cpuUtilization', triggeredUEI: 'uei.opennms.org/example/x' }],
      expressions: [{ ...getDefaultExpression(), expression: 'a + b', exprLabel: 'sum' }]
    }
    store.saveCurrentGroup = vi.fn().mockResolvedValue({ success: true, message: '' })
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('shows a datasource column for thresholds', () => {
    const wrapper = mountComponent(ThresholdDefinitionKind.Threshold)

    expect(wrapper.text()).toContain('Basic thresholds')
    expect(wrapper.text()).toContain('Datasource')
    expect(wrapper.text()).toContain('cpuUtilization')
    expect(wrapper.text()).not.toContain('Expression label')
  })

  it('shows expression columns for expressions', () => {
    const wrapper = mountComponent(ThresholdDefinitionKind.Expression)

    expect(wrapper.text()).toContain('Expression-based thresholds')
    expect(wrapper.text()).toContain('Expression label')
    expect(wrapper.text()).toContain('a + b')
    expect(wrapper.text()).toContain('sum')
  })

  it('labels the create button for the kind it shows', () => {
    expect(mountComponent(ThresholdDefinitionKind.Threshold).text()).toContain('Create new threshold')
    wrapper.unmount()
    expect(mountComponent(ThresholdDefinitionKind.Expression).text()).toContain('Create new expression-based threshold')
  })

  it('renders UEI cells through the notification-link component', () => {
    const wrapper = mountComponent(ThresholdDefinitionKind.Threshold)

    expect(wrapper.findAllComponents(ThresholdUeiCell).length).toBeGreaterThan(0)
  })

  it('opens the drawer in create mode', async () => {
    const wrapper = mountComponent(ThresholdDefinitionKind.Threshold)

    await wrapper.find('[data-test="threshold-create-threshold"]').trigger('click')

    expect(store.definitionDrawer).toMatchObject({
      visible: true,
      mode: CreateEditMode.Create,
      kind: ThresholdDefinitionKind.Threshold
    })
  })

  it('opens the drawer in edit mode on the right row', async () => {
    const wrapper = mountComponent(ThresholdDefinitionKind.Threshold)

    await wrapper.find('[data-test="threshold-edit-threshold"]').trigger('click')

    expect(store.definitionDrawer).toMatchObject({ visible: true, mode: CreateEditMode.Edit, index: 0 })
  })

  it('asks before deleting and only then saves', async () => {
    const wrapper = mountComponent(ThresholdDefinitionKind.Threshold)

    await wrapper.find('[data-test="threshold-delete-threshold"]').trigger('click')
    expect(store.saveCurrentGroup).not.toHaveBeenCalled()

    await wrapper.findComponent({ name: 'OnmsConfirmationDialog' }).vm.$emit('ok')
    await new Promise(resolve => setTimeout(resolve))

    expect(store.currentGroup?.thresholds).toHaveLength(0)
    expect(store.saveCurrentGroup).toHaveBeenCalledTimes(1)
  })

  it('keeps the definition when the delete is cancelled', async () => {
    const wrapper = mountComponent(ThresholdDefinitionKind.Threshold)

    await wrapper.find('[data-test="threshold-delete-threshold"]').trigger('click')
    await wrapper.findComponent({ name: 'OnmsConfirmationDialog' }).vm.$emit('cancel')

    expect(store.currentGroup?.thresholds).toHaveLength(1)
    expect(store.saveCurrentGroup).not.toHaveBeenCalled()
  })

  it('disables every mutating control for an extension-provided group', () => {
    const wrapper = mountComponent(ThresholdDefinitionKind.Threshold, true)

    expect(wrapper.find('[data-test="threshold-create-threshold"]').attributes('disabled')).toBeDefined()
    expect(wrapper.find('[data-test="threshold-edit-threshold"]').attributes('disabled')).toBeDefined()
    expect(wrapper.find('[data-test="threshold-delete-threshold"]').attributes('disabled')).toBeDefined()
  })

  it('explains an empty table instead of showing nothing', () => {
    store.currentGroup!.thresholds = []
    const wrapper = mountComponent(ThresholdDefinitionKind.Threshold)

    expect(wrapper.find('[data-test="threshold-empty-threshold"]').text())
      .toBe('This group has no basic thresholds.')
  })
})

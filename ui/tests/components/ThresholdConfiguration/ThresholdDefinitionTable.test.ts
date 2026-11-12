import ThresholdDefinitionTable from '@/components/ThresholdConfiguration/Group/ThresholdDefinitionTable.vue'
import ThresholdUeiCell from '@/components/ThresholdConfiguration/Group/ThresholdUeiCell.vue'
import { ThresholdDefinitionKind } from '@/lib/thresholdValidator'
import { getDefaultExpression, getDefaultThreshold, useThresholdGroupStore } from '@/stores/thresholdGroupStore'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, pushMock } = vi.hoisted(() => ({ showSnackBarMock: vi.fn(), pushMock: vi.fn() }))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock })
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
    store.deleteDefinition = vi.fn().mockResolvedValue({ success: true, message: '' })
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it.each([ThresholdDefinitionKind.Threshold, ThresholdDefinitionKind.Expression])(
    'leaves the datasource columns out of the %s table',
    (kind) => {
      const wrapper = mountComponent(kind)

      expect(wrapper.text()).not.toContain('Datasource')
      expect(wrapper.text()).not.toContain('cpuUtilization')
    }
  )

  it('shows no expression columns for thresholds', () => {
    const wrapper = mountComponent(ThresholdDefinitionKind.Threshold)

    expect(wrapper.text()).toContain('Basic thresholds')
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

  it('opens the create page for the kind it shows', async () => {
    const wrapper = mountComponent(ThresholdDefinitionKind.Expression)

    await wrapper.find('[data-test="threshold-create-expression"]').trigger('click')

    expect(pushMock).toHaveBeenCalledWith('/threshold-config/group/mib2/expression/create')
  })

  it('opens the edit page on the right row', async () => {
    const wrapper = mountComponent(ThresholdDefinitionKind.Threshold)

    await wrapper.find('[data-test="threshold-edit-threshold"]').trigger('click')

    expect(pushMock).toHaveBeenCalledWith('/threshold-config/group/mib2/threshold/0/edit')
  })

  it('asks before deleting and only then saves', async () => {
    const wrapper = mountComponent(ThresholdDefinitionKind.Threshold)

    await wrapper.find('[data-test="threshold-delete-threshold"]').trigger('click')
    expect(store.deleteDefinition).not.toHaveBeenCalled()

    await wrapper.findComponent({ name: 'OnmsConfirmationDialog' }).vm.$emit('ok')
    await new Promise(resolve => setTimeout(resolve))

    expect(store.deleteDefinition).toHaveBeenCalledWith(ThresholdDefinitionKind.Threshold, 0)
  })

  it('keeps the definition when the delete is cancelled', async () => {
    const wrapper = mountComponent(ThresholdDefinitionKind.Threshold)

    await wrapper.find('[data-test="threshold-delete-threshold"]').trigger('click')
    await wrapper.findComponent({ name: 'OnmsConfirmationDialog' }).vm.$emit('cancel')

    expect(store.currentGroup?.thresholds).toHaveLength(1)
    expect(store.deleteDefinition).not.toHaveBeenCalled()
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

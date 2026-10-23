import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

describe('ThresholdEditPage.vue', () => {
  const mountComponent = (props: Record<string, unknown> = {}) =>
    mount(ThresholdEditPage, {
      props: { title: 'Create New Threshold Group', sectionTitle: 'Basic Information', saveLabel: 'Create Threshold Group', ...props },
      slots: { default: '<div data-test="slot-content">fields</div>' },
      global: { plugins: [PrimeVue] }
    })

  it('shows the title, the section and the fields', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-title"]').text()).toBe('Create New Threshold Group')
    expect(wrapper.text()).toContain('Basic Information')
    expect(wrapper.find('[data-test="slot-content"]').exists()).toBe(true)
  })

  it('labels the buttons like the other edit pages', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('[data-test="threshold-edit-cancel"]').text()).toBe('Cancel')
    expect(wrapper.find('[data-test="threshold-edit-save"]').text()).toBe('Create Threshold Group')
    expect(wrapper.find('[data-test="threshold-edit-back"]').text()).toContain('Go Back')
  })

  it('emits back, cancel and save', async () => {
    const wrapper = mountComponent()

    await wrapper.find('[data-test="threshold-edit-back"]').trigger('click')
    await wrapper.find('[data-test="threshold-edit-cancel"]').trigger('click')
    await wrapper.find('[data-test="threshold-edit-save"]').trigger('click')

    expect(wrapper.emitted('back')).toHaveLength(1)
    expect(wrapper.emitted('cancel')).toHaveLength(1)
    expect(wrapper.emitted('save')).toHaveLength(1)
  })

  it('disables save on request', () => {
    const wrapper = mountComponent({ saveDisabled: true })

    expect(wrapper.find('[data-test="threshold-edit-save"]').attributes('disabled')).toBeDefined()
  })

  it('shows only the not-found state with a way back', async () => {
    const wrapper = mountComponent({ notFoundMessage: 'Nothing here.' })

    expect(wrapper.find('[data-test="threshold-edit-not-found"]').text()).toContain('Nothing here.')
    expect(wrapper.find('[data-test="slot-content"]').exists()).toBe(false)

    await wrapper.find('[data-test="threshold-edit-not-found-back"]').trigger('click')
    expect(wrapper.emitted('back')).toHaveLength(1)
  })
})

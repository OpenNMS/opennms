import ResourceFilterEditor from '@/components/ThresholdConfiguration/Group/ResourceFilterEditor.vue'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { ResourceFilter } from '@/types/thresholdConfig'

describe('ResourceFilterEditor.vue', () => {
  let wrapper: VueWrapper<any>

  const filters = (): ResourceFilter[] => [
    { field: 'first', content: 'a.*' },
    { field: 'second', content: 'b.*' },
    { field: 'third', content: 'c.*' }
  ]

  const mountComponent = (modelValue: ResourceFilter[] = filters()) => {
    wrapper = mount(ResourceFilterEditor, {
      props: { modelValue, filterOperator: 'or' },
      global: { plugins: [PrimeVue] }
    })
    return wrapper
  }

  const emittedFilters = (index = 0) =>
    (wrapper.emitted('update:modelValue')?.[index]?.[0] as ResourceFilter[]).map(filter => filter.field)

  beforeEach(() => {
    vi.clearAllMocks()
    // stubActions false so moveResourceFilter really runs; the reorder logic is the point of this component.
    setActivePinia(createTestingPinia({ stubActions: false }))
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('renders one row per filter, numbered from one', () => {
    const wrapper = mountComponent()

    expect(wrapper.findAll('[data-test="resource-filter-field"]').map(node => node.text()))
      .toEqual(['first', 'second', 'third'])
  })

  it('shows a placeholder when a filter has no pattern', () => {
    const wrapper = mountComponent([{ field: 'only', content: '' }])

    expect(wrapper.find('[data-test="resource-filter-content"]').text()).toBe('--')
  })

  it('moves a filter up and emits the whole reordered list', async () => {
    const wrapper = mountComponent()

    await wrapper.findAll('[data-test="resource-filter-move-up"]')[1].trigger('click')

    expect(emittedFilters()).toEqual(['second', 'first', 'third'])
  })

  it('moves a filter down', async () => {
    const wrapper = mountComponent()

    await wrapper.findAll('[data-test="resource-filter-move-down"]')[0].trigger('click')

    expect(emittedFilters()).toEqual(['second', 'first', 'third'])
  })

  it('disables the move buttons at the bounds so order cannot be lost off the ends', () => {
    const wrapper = mountComponent()

    const upButtons = wrapper.findAll('[data-test="resource-filter-move-up"]')
    const downButtons = wrapper.findAll('[data-test="resource-filter-move-down"]')

    expect(upButtons[0].attributes('disabled')).toBeDefined()
    expect(upButtons[2].attributes('disabled')).toBeUndefined()
    expect(downButtons[2].attributes('disabled')).toBeDefined()
    expect(downButtons[0].attributes('disabled')).toBeUndefined()
  })

  it('deletes a filter', async () => {
    const wrapper = mountComponent()

    await wrapper.findAll('[data-test="resource-filter-delete"]')[1].trigger('click')

    expect(emittedFilters()).toEqual(['first', 'third'])
  })

  it('swaps a row to inputs on edit and writes the change back on save', async () => {
    const wrapper = mountComponent()

    await wrapper.findAll('[data-test="resource-filter-edit"]')[0].trigger('click')
    expect(wrapper.find('[data-test="resource-filter-edit-field"]').exists()).toBe(true)

    await wrapper.find('[data-test="resource-filter-edit-field"]').setValue('renamed')
    await wrapper.find('[data-test="resource-filter-save"]').trigger('click')

    expect(emittedFilters()).toEqual(['renamed', 'second', 'third'])
  })

  it('discards an edit on cancel', async () => {
    const wrapper = mountComponent()

    await wrapper.findAll('[data-test="resource-filter-edit"]')[0].trigger('click')
    await wrapper.find('[data-test="resource-filter-edit-field"]').setValue('renamed')
    await wrapper.find('[data-test="resource-filter-cancel"]').trigger('click')

    expect(wrapper.emitted('update:modelValue')).toBeUndefined()
    expect(wrapper.find('[data-test="resource-filter-edit-field"]').exists()).toBe(false)
  })

  it('appends a new filter and clears the add row', async () => {
    const wrapper = mountComponent()

    await wrapper.find('[data-test="resource-filter-new-field"]').setValue('fourth')
    await wrapper.find('[data-test="resource-filter-new-content"]').setValue('d.*')
    await wrapper.find('[data-test="resource-filter-add"]').trigger('click')

    expect(emittedFilters()).toEqual(['first', 'second', 'third', 'fourth'])
    expect((wrapper.find('[data-test="resource-filter-new-field"]').element as HTMLInputElement).value).toBe('')
  })

  it('will not add a filter without a field name', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('[data-test="resource-filter-add"]').attributes('disabled')).toBeDefined()
  })

  it('emits the operator change rather than mutating the prop', async () => {
    const wrapper = mountComponent()

    await wrapper.findComponent({ name: 'OnmsSelect' }).vm.$emit('update:modelValue', 'and')

    expect(wrapper.emitted('update:filterOperator')?.[0]).toEqual(['and'])
  })

  it('tells the user that order matters', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Filters are applied in the order listed.')
  })
})

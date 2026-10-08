import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it } from 'vitest'

import SelectionColumn from '@/components/AdhocGraphs/SelectionColumn.vue'

interface Option {
  id: string
  label: string
}

const OPTIONS: Option[] = [
  { id: 'node[1].interfaceSnmp[eth0]', label: 'eth0' },
  { id: 'node[1].interfaceSnmp[eth1]', label: 'eth1' },
  { id: 'node[1].responseTime[127.0.0.1]', label: 'Response Time for 127.0.0.1' }
]

const mounted: ReturnType<typeof mount>[] = []

const mountColumn = (props: Record<string, unknown> = {}, slots: Record<string, string> = {}) => {
  const wrapper = mount(SelectionColumn, {
    props: {
      title: 'Resources',
      dataTest: 'resources',
      options: OPTIONS,
      modelValue: [],
      dataKey: 'id',
      optionLabel: 'label',
      filterTerm: '',
      keyOf: (option: unknown) => (option as Option).id,
      labelOf: (option: unknown) => (option as Option).label,
      ...props
    },
    slots,
    global: { plugins: [PrimeVue] }
  })

  mounted.push(wrapper)
  return wrapper
}

const listedIds = (wrapper: ReturnType<typeof mount>) =>
  ((wrapper.findComponent('[data-test="resources-list"]') as VueWrapper<any>).props('options') as Option[]).map(option => option.id)

const filterInput = (wrapper: ReturnType<typeof mount>) => wrapper.find('input[data-test="resources-filter"]')

const count = (wrapper: ReturnType<typeof mount>) => wrapper.find('[data-test="resources-count"]').text()

describe('SelectionColumn', () => {
  afterEach(() => {
    while (mounted.length) {
      mounted.pop()?.unmount()
    }
  })

  it('lists exactly what it is given; matching is the parent\'s job', () => {
    const wrapper = mountColumn()

    expect(listedIds(wrapper)).toEqual(OPTIONS.map(option => option.id))
  })

  it('reports every keystroke upward', async () => {
    const wrapper = mountColumn()

    await filterInput(wrapper).setValue('interfaceSnmp[eth*]')

    expect(wrapper.emitted('filter')).toEqual([['interfaceSnmp[eth*]']])
    expect(listedIds(wrapper)).toHaveLength(3)
  })

  it('follows a filter term the parent owns, so a restored link shows in the box', async () => {
    const wrapper = mountColumn({ filterTerm: 'catincRouters' })

    expect((filterInput(wrapper).element as HTMLInputElement).value).toBe('catincRouters')

    await wrapper.setProps({ filterTerm: 'catincSwitches' })
    await flushPromises()

    expect((filterInput(wrapper).element as HTMLInputElement).value).toBe('catincSwitches')
  })

  describe('the count label', () => {
    it('says what is available when nothing is filtered or picked', () => {
      expect(count(mountColumn())).toBe('3 available')
    })

    it('says everything matching is in when there is a filter and no picks', () => {
      expect(count(mountColumn({ filterActive: true }))).toBe('all 3 matching')
    })

    it('counts the selection against the matches, pinned selections included once', () => {
      // The parent pins picks on top of the list, so a pick the filter no longer
      // matches is still among the options.
      expect(count(mountColumn({ filterActive: true, modelValue: [OPTIONS[0]] }))).toBe('1 of 3 selected')
    })
  })

  describe('picking', () => {
    it('selects everything listed on Select all, so items can then be deselected', async () => {
      const wrapper = mountColumn()

      await wrapper.find('[data-test="resources-select-all"]').trigger('click')

      expect(wrapper.emitted('update:modelValue')?.[0][0]).toEqual(OPTIONS)
    })

    it('drops every selection on Clear', async () => {
      const wrapper = mountColumn({ modelValue: [OPTIONS[1]] })

      await wrapper.find('[data-test="resources-clear"]').trigger('click')

      expect(wrapper.emitted('update:modelValue')?.[0][0]).toEqual([])
    })

    it('disables Clear when nothing is selected', () => {
      const wrapper = mountColumn()

      expect(wrapper.find('[data-test="resources-clear"]').attributes('disabled')).toBeDefined()
    })
  })

  describe('status', () => {
    it('shows the empty message when there is nothing to list', () => {
      const wrapper = mountColumn({ options: [], emptyMessage: 'Type a node filter first.' })

      expect(wrapper.find('[data-test="resources-empty"]').text()).toBe('Type a node filter first.')
      expect(wrapper.find('[data-test="resources-list"]').exists()).toBe(false)
    })

    it('shows an error from the parent in place of the list', () => {
      const wrapper = mountColumn({ options: [], errorMessage: 'That filter rule could not be parsed.' })

      expect(wrapper.find('[data-test="resources-error"]').text()).toBe('That filter rule could not be parsed.')
      expect(wrapper.find('[data-test="resources-empty"]').exists()).toBe(false)
    })

    it('keeps the list, with the error above it, when there are still items to act on', () => {
      const wrapper = mountColumn({ modelValue: [OPTIONS[0]], errorMessage: 'That filter rule could not be parsed.' })

      expect(wrapper.find('[data-test="resources-error"]').text()).toBe('That filter rule could not be parsed.')
      expect(wrapper.find('[data-test="resources-list"]').exists()).toBe(true)
    })

    it('shows the spinner, and nothing else, while loading', () => {
      const wrapper = mountColumn({ loading: true, errorMessage: 'stale' })

      expect(wrapper.find('[data-test="resources-loading"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="resources-error"]').exists()).toBe(false)
      expect(wrapper.find('[data-test="resources-list"]').exists()).toBe(false)
    })

    it('shows a note under the box when given one', () => {
      const wrapper = mountColumn({ note: 'Showing the first 100 of 2,310 nodes.' })

      expect(wrapper.find('[data-test="resources-note"]').text()).toBe('Showing the first 100 of 2,310 nodes.')
      expect(mountColumn().find('[data-test="resources-note"]').exists()).toBe(false)
    })
  })
})

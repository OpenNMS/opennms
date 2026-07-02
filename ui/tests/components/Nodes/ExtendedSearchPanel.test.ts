// ui/tests/components/Nodes/ExtendedSearchPanel.test.ts
import ExtendedSearchPanel from '@/components/Nodes/ExtendedSearchPanel.vue'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Select from 'primevue/select'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import { setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

describe('ExtendedSearchPanel.vue', () => {
  let store: ReturnType<typeof useNodeStructureStore>

  const mountPanel = () =>
    mount(ExtendedSearchPanel, {
      global: {
        plugins: [PrimeVue],
        stubs: {
          FeatherIcon: { name: 'FeatherIcon', template: '<span />', props: ['icon'] }
        }
      }
    })

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ createSpy: vi.fn, stubActions: false }))
    store = useNodeStructureStore()
    store.setExtendedSearchParams = vi.fn()
    store.queryFilter = {
      ...store.queryFilter,
      extendedSearch: {}
    } as any
  })

  // ── Add-row structure ──────────────────────────────────────────────────────

  it('renders a PrimeVue Select for search type', () => {
    const wrapper = mountPanel()
    expect(wrapper.findComponent(Select).exists()).toBe(true)
  })

  it('renders a PrimeVue InputText for search term', () => {
    const wrapper = mountPanel()
    // The add-row InputText (grid hidden when no rows)
    expect(wrapper.findComponent(InputText).exists()).toBe(true)
  })

  it('renders a PrimeVue Button for Add', () => {
    const wrapper = mountPanel()
    const btn = wrapper.findAllComponents(Button).find(b => b.attributes('data-test') === 'add-search-term-button')
    expect(btn).toBeDefined()
  })

  it('wraps the Select in a FormField with label "Search Type"', () => {
    const wrapper = mountPanel()
    const labels = wrapper.findAll('.form-field__label').map(el => el.text())
    expect(labels).toContain('Search Type')
  })

  it('wraps the InputText in a FormField with label "Search Term"', () => {
    const wrapper = mountPanel()
    const labels = wrapper.findAll('.form-field__label').map(el => el.text())
    expect(labels).toContain('Search Term')
  })

  // ── Add row behaviour ──────────────────────────────────────────────────────

  it('adds an extended-search row to the grid when Add is clicked with valid data', async () => {
    const wrapper = mountPanel()

    // Set refs directly via the component instance
    wrapper.vm.currentSelection = { title: 'Foreign Source', value: 'foreignSource' }
    wrapper.vm.searchTerm = 'foo'
    await nextTick()

    const addBtn = wrapper.findAllComponents(Button).find(b => b.attributes('data-test') === 'add-search-term-button')
    expect(addBtn).toBeDefined()
    await addBtn!.trigger('click')
    await nextTick()

    expect(wrapper.vm.gridItems.length).toBe(1)
    expect(wrapper.vm.gridItems[0]).toMatchObject({ key: 'foreignSource', label: 'Foreign Source', value: 'foo' })
  })

  it('does not add a row when selection or value is missing', async () => {
    const wrapper = mountPanel()
    const addBtn = wrapper.findAllComponents(Button).find(b => b.attributes('data-test') === 'add-search-term-button')
    await addBtn!.trigger('click')
    await nextTick()
    expect(wrapper.vm.gridItems.length).toBe(0)
  })

  // ── applyToStore ───────────────────────────────────────────────────────────

  it('adds an extended-search row and applies grouped params to the store', async () => {
    const wrapper = mountPanel()

    // Add a foreignSource row
    wrapper.vm.currentSelection = { title: 'Foreign Source', value: 'foreignSource' }
    wrapper.vm.searchTerm = 'foo'
    await nextTick()

    const addBtn = wrapper.findAllComponents(Button).find(b => b.attributes('data-test') === 'add-search-term-button')
    await addBtn!.trigger('click')
    await nextTick()

    wrapper.vm.applyToStore()

    expect(store.setExtendedSearchParams).toHaveBeenCalledOnce()
    const arg = (store.setExtendedSearchParams as ReturnType<typeof vi.fn>).mock.calls[0][0]
    expect(arg.foreignSourceParams).toBeDefined()
    expect(arg.foreignSourceParams.foreignSource).toBe('foo')
    expect(arg.snmpParams).toBeUndefined()
    expect(arg.sysParams).toBeUndefined()
  })

  it('applyToStore() groups snmp keys into snmpParams', async () => {
    const wrapper = mountPanel()

    wrapper.vm.currentSelection = { title: 'SNMP Alias', value: 'snmpIfAlias' }
    wrapper.vm.searchTerm = 'eth0'
    await nextTick()

    const addBtn = wrapper.findAllComponents(Button).find(b => b.attributes('data-test') === 'add-search-term-button')
    await addBtn!.trigger('click')
    await nextTick()

    wrapper.vm.applyToStore()

    const arg = (store.setExtendedSearchParams as ReturnType<typeof vi.fn>).mock.calls[0][0]
    expect(arg.snmpParams).toBeDefined()
    expect(arg.snmpParams.snmpIfAlias).toBe('eth0')
    expect(arg.foreignSourceParams).toBeUndefined()
    expect(arg.sysParams).toBeUndefined()
  })

  it('applyToStore() groups sys keys into sysParams', async () => {
    const wrapper = mountPanel()

    wrapper.vm.currentSelection = { title: 'Sys Contact', value: 'sysContact' }
    wrapper.vm.searchTerm = 'admin'
    await nextTick()

    const addBtn = wrapper.findAllComponents(Button).find(b => b.attributes('data-test') === 'add-search-term-button')
    await addBtn!.trigger('click')
    await nextTick()

    wrapper.vm.applyToStore()

    const arg = (store.setExtendedSearchParams as ReturnType<typeof vi.fn>).mock.calls[0][0]
    expect(arg.sysParams).toBeDefined()
    expect(arg.sysParams.sysContact).toBe('admin')
    expect(arg.foreignSourceParams).toBeUndefined()
    expect(arg.snmpParams).toBeUndefined()
  })

  // ── resetFromStore ─────────────────────────────────────────────────────────

  it('resetFromStore() seeds grid rows from nodeStructureStore.queryFilter.extendedSearch', async () => {
    const wrapper = mountPanel()

    // Simulate a store with a pre-existing foreignSource param
    store.queryFilter = {
      ...store.queryFilter,
      extendedSearch: {
        foreignSourceParams: {
          foreignSource: 'bar',
          foreignId: '',
          foreignSourceId: ''
        }
      }
    } as any

    wrapper.vm.resetFromStore()
    await nextTick()

    // Should have exactly 1 row for foreignSource=bar (empty values are filtered out by getExtendedSearchValues)
    expect(wrapper.vm.gridItems.length).toBeGreaterThan(0)
    const fsRow = wrapper.vm.gridItems.find(i => i.key === 'foreignSource')
    expect(fsRow).toBeDefined()
    expect(fsRow?.value).toBe('bar')
  })
})

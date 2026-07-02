// ui/tests/components/Nodes/AssetFilterPanel.test.ts
import AssetFilterPanel from '@/components/Nodes/AssetFilterPanel.vue'
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

describe('AssetFilterPanel.vue', () => {
  let store: ReturnType<typeof useNodeStructureStore>

  const mountPanel = () =>
    mount(AssetFilterPanel, {
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
    store.setFilterWithAssetFilters = vi.fn()
    store.queryFilter = {
      ...store.queryFilter,
      assetFilters: []
    } as any
  })

  // ── Add-row structure ──────────────────────────────────────────────────────

  it('renders a PrimeVue Select for asset field', () => {
    const wrapper = mountPanel()
    expect(wrapper.findComponent(Select).exists()).toBe(true)
  })

  it('renders a PrimeVue InputText for value', () => {
    const wrapper = mountPanel()
    // The add-row InputText (not the grid inline editors — grid is hidden when no rows)
    expect(wrapper.findComponent(InputText).exists()).toBe(true)
  })

  it('renders a PrimeVue Button for Add', () => {
    const wrapper = mountPanel()
    const btn = wrapper.findAllComponents(Button).find(b => b.attributes('data-test') === 'asset-add-button')
    expect(btn).toBeDefined()
  })

  it('wraps the Select in a FormField with label "Asset Field"', () => {
    const wrapper = mountPanel()
    const labels = wrapper.findAll('.form-field__label').map(el => el.text())
    expect(labels).toContain('Asset Field')
  })

  it('wraps the InputText in a FormField with label "Value"', () => {
    const wrapper = mountPanel()
    const labels = wrapper.findAll('.form-field__label').map(el => el.text())
    expect(labels).toContain('Value')
  })

  // ── Add row behaviour ──────────────────────────────────────────────────────

  it('adds an asset filter row to the grid when Add is clicked with valid data', async () => {
    const wrapper = mountPanel()

    // Set refs directly via the component instance
    wrapper.vm.currentSelection = { title: 'Building', value: 'building' }
    wrapper.vm.assetValue = 'ServerRoom'
    await nextTick()

    const addBtn = wrapper.findAllComponents(Button).find(b => b.attributes('data-test') === 'asset-add-button')
    expect(addBtn).toBeDefined()
    await addBtn!.trigger('click')
    await nextTick()

    // DataTable becomes visible once there is at least one row; check its :value
    expect(wrapper.vm.gridItems.length).toBe(1)
    expect(wrapper.vm.gridItems[0]).toMatchObject({ column: 'building', label: 'Building', value: 'ServerRoom' })
  })

  it('does not add a row when selection or value is missing', async () => {
    const wrapper = mountPanel()
    // No selection, no value
    const addBtn = wrapper.findAllComponents(Button).find(b => b.attributes('data-test') === 'asset-add-button')
    await addBtn!.trigger('click')
    await nextTick()
    expect(wrapper.vm.gridItems.length).toBe(0)
  })

  // ── applyToStore ───────────────────────────────────────────────────────────

  it('applyToStore() calls setFilterWithAssetFilters with current grid rows', async () => {
    const wrapper = mountPanel()
    wrapper.vm.currentSelection = { title: 'Floor', value: 'floor' }
    wrapper.vm.assetValue = '3'
    await nextTick()

    const addBtn = wrapper.findAllComponents(Button).find(b => b.attributes('data-test') === 'asset-add-button')
    await addBtn!.trigger('click')
    await nextTick()

    wrapper.vm.applyToStore()
    expect(store.setFilterWithAssetFilters).toHaveBeenCalledWith([{ column: 'floor', value: '3' }])
  })

  // ── resetFromStore ─────────────────────────────────────────────────────────

  it('resetFromStore() seeds grid rows from nodeStructureStore.queryFilter.assetFilters', async () => {
    const wrapper = mountPanel()

    store.queryFilter = {
      ...store.queryFilter,
      assetFilters: [{ column: 'room', value: 'B204' }]
    } as any

    wrapper.vm.resetFromStore()
    await nextTick()

    expect(wrapper.vm.gridItems.length).toBe(1)
    expect(wrapper.vm.gridItems[0]).toMatchObject({ column: 'room', value: 'B204' })
  })
})

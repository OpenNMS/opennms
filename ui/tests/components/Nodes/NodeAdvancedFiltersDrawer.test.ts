///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements. See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version. You may not use this file except in
/// compliance with the License. You may obtain a copy of the
/// License at:
///
/// https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied. See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import NodeAdvancedFiltersDrawer from '@/components/Nodes/NodeAdvancedFiltersDrawer.vue'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

// ── Module mocks ───────────────────────────────────────────────────────────────

vi.mock('@/components/Nodes/hooks/useNodeQuery', () => {
  const makeDefaultFilter = () => ({
    searchTerm: '',
    categoryMode: 'Union',
    selectedCategories: [],
    selectedCategories2: [],
    selectedServices: [],
    selectedFlows: [],
    selectedMonitoringLocations: [],
    ipAddress: '',
    macAddress: '',
    topology: '',
    nodesWithDownAggregateStatus: false,
    nodesWithAssets: false,
    assetFilters: [],
    extendedSearch: {
      foreignSourceParams: { foreignId: '', foreignSource: '', foreignSourceId: '' },
      snmpParams: { snmpIfAlias: '', snmpIfDescription: '', snmpIfIndex: '', snmpIfName: '', snmpIfType: '' },
      sysParams: { sysContact: '', sysDescription: '', sysLocation: '', sysName: '', sysObjectId: '' }
    }
  })
  return {
    useNodeQuery: () => ({
      buildUpdatedNodeStructureQueryParameters: vi.fn().mockImplementation(params => params),
      getExtendedSearchValues: vi.fn().mockReturnValue([]),
      getDefaultNodeQueryFilter: makeDefaultFilter,
      getDefaultNodeQueryForeignSourceParams: () => ({ foreignId: '', foreignSource: '', foreignSourceId: '' }),
      getDefaultNodeQuerySnmpParams: () => ({ snmpIfAlias: '', snmpIfDescription: '', snmpIfIndex: '', snmpIfName: '', snmpIfType: '' }),
      getDefaultNodeQuerySysParams: () => ({ sysContact: '', sysDescription: '', sysLocation: '', sysName: '', sysObjectId: '' }),
      buildNodeQueryFilterFromQueryString: vi.fn().mockReturnValue(makeDefaultFilter()),
      queryStringHasTrackedValues: vi.fn().mockReturnValue(false)
    })
  }
})

// ── Stubs ──────────────────────────────────────────────────────────────────────

// Stub PrimeVue Drawer: renders its default slot + footer slot when visible.
const DrawerStub = {
  name: 'Drawer',
  props: ['visible', 'position', 'header', 'style'],
  emits: ['update:visible'],
  template: '<div class="drawer-stub" v-if="visible"><slot /><slot name="footer" /></div>'
}

// Stub MultiSelect: exposes options, modelValue and re-emits update:modelValue so we
// can drive selection without PrimeVue overlay/teleport flakiness.
const MultiSelectStub = {
  name: 'MultiSelect',
  props: ['modelValue', 'options', 'optionLabel', 'dataKey', 'filter', 'display', 'placeholder'],
  emits: ['update:modelValue'],
  template: '<div class="multiselect-stub"></div>'
}

// Stub the embedded panels so we can assert their applyToStore/resetFromStore are
// called. The spies are module-level so we can reference them after mount, and are
// exposed via defineExpose-equivalent so they are reachable on the ref.
const assetApplyToStore = vi.fn()
const assetResetFromStore = vi.fn()
const extApplyToStore = vi.fn()
const extResetFromStore = vi.fn()

const AssetFilterPanelStub = {
  name: 'AssetFilterPanel',
  template: '<div class="asset-filter-panel-stub"></div>',
  setup(_: unknown, { expose }: any) {
    expose({ applyToStore: assetApplyToStore, resetFromStore: assetResetFromStore })
  }
}
const ExtendedSearchPanelStub = {
  name: 'ExtendedSearchPanel',
  template: '<div class="extended-search-panel-stub"></div>',
  setup(_: unknown, { expose }: any) {
    expose({ applyToStore: extApplyToStore, resetFromStore: extResetFromStore })
  }
}

// ── Mount helper ───────────────────────────────────────────────────────────────

const seedStore = (store: ReturnType<typeof useNodeStructureStore>) => {
  store.categories = [
    { id: 1, name: 'Routers', authorizedGroups: [] },
    { id: 2, name: 'Switches', authorizedGroups: [] }
  ] as any
  store.monitoringLocations = [
    { name: 'Default' },
    { name: 'RDU' }
  ] as any
  store.allServiceTypes = [
    { id: 10, name: 'ICMP' },
    { id: 11, name: 'SNMP' }
  ] as any
}

const mountDrawer = () =>
  mount(NodeAdvancedFiltersDrawer, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs: {
        Drawer: DrawerStub,
        MultiSelect: MultiSelectStub,
        AssetFilterPanel: AssetFilterPanelStub,
        ExtendedSearchPanel: ExtendedSearchPanelStub,
        MessageDialog: true
      }
    }
  })

// ── Tests ──────────────────────────────────────────────────────────────────────

describe('NodeAdvancedFiltersDrawer.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useNodeStructureStore>

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountDrawer()
    store = useNodeStructureStore()
    seedStore(store)
    store.drawerState = { visible: true } as any
    await nextTick()
    await nextTick()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Drawer visibility', () => {
    it('shows content when drawerState.visible is true', () => {
      expect(wrapper.find('.drawer-stub').exists()).toBe(true)
    })

    it('hides content when drawerState.visible is false', async () => {
      store.drawerState = { visible: false } as any
      await nextTick()
      expect(wrapper.find('.drawer-stub').exists()).toBe(false)
    })

    it('calls the close action when drawerVisible is set false', () => {
      wrapper.vm.drawerVisible = false
      expect(store.closeInstancesDrawerModal).toHaveBeenCalled()
    })
  })

  describe('MultiSelects', () => {
    it('renders at least 4 MultiSelects bound to store option lists', () => {
      const multiselects = wrapper.findAllComponents({ name: 'MultiSelect' })
      expect(multiselects.length).toBeGreaterThanOrEqual(4)
    })

    it('passes the store category list as options to the categories MultiSelect', () => {
      const ms = wrapper.find('[data-test="categories-multiselect"]').findComponent({ name: 'MultiSelect' })
      expect(ms.exists()).toBe(true)
      const options = ms.props('options') as any[]
      expect(options.map(o => o._text)).toEqual(['Routers', 'Switches'])
    })

    it('routes a categories selection through updateFilter into selectedFilters', async () => {
      const ms = wrapper.find('[data-test="categories-multiselect"]').findComponent({ name: 'MultiSelect' })
      const selection = [{ _text: 'Routers', _value: 1 }]
      ms.vm.$emit('update:modelValue', selection)
      await nextTick()
      expect(wrapper.vm.selectedFilters.categories).toEqual(selection)
    })
  })

  describe('IP validation', () => {
    it('disables Apply and sets the FormField error when the IP is invalid', async () => {
      wrapper.vm.selectedFilters.ipAddress = 'not-an-ip!!'
      await nextTick()
      expect(wrapper.vm.isApplyDisabled).toBe(true)
      const ipField = wrapper.find('[data-test="ip-field"]')
      expect(ipField.find('.field-error').exists()).toBe(true)
    })

    it('keeps Apply enabled for a valid IP', async () => {
      wrapper.vm.selectedFilters.ipAddress = '192.168.1.1'
      await nextTick()
      expect(wrapper.vm.isApplyDisabled).toBe(false)
    })
  })

  describe('Toggles', () => {
    it('toggles the down-only flag via the ToggleSwitch', async () => {
      const toggle = wrapper.find('[data-test="down-only"]').findComponent({ name: 'ToggleSwitch' })
      expect(toggle.exists()).toBe(true)
      toggle.vm.$emit('update:modelValue', true)
      await nextTick()
      expect(wrapper.vm.selectedFilters.nodesWithDownAggregateStatus).toBe(true)
    })

    it('toggles the with-assets flag via the ToggleSwitch', async () => {
      const toggle = wrapper.find('[data-test="with-assets"]').findComponent({ name: 'ToggleSwitch' })
      expect(toggle.exists()).toBe(true)
      toggle.vm.$emit('update:modelValue', true)
      await nextTick()
      expect(wrapper.vm.selectedFilters.nodesWithAssets).toBe(true)
    })
  })

  describe('Apply', () => {
    it('calls both panels applyToStore and the store apply actions', async () => {
      const applyBtn = wrapper.findAll('button').find(b => b.text().includes('Apply Filters'))
      expect(applyBtn).toBeDefined()
      await applyBtn!.trigger('click')
      await flushPromises()

      expect(assetApplyToStore).toHaveBeenCalled()
      expect(extApplyToStore).toHaveBeenCalled()
      expect(store.updateSelectedCategories).toHaveBeenCalled()
      expect(store.closeInstancesDrawerModal).toHaveBeenCalled()
    })
  })
})

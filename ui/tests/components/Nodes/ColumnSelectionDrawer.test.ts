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

import ColumnSelectionDrawer from '@/components/Nodes/ColumnSelectionDrawer.vue'
import { useNodeStructureStore } from '@/stores/nodeStructureStore'
import { defaultColumns } from '@/components/Nodes/utils'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

// ── Module mocks ───────────────────────────────────────────────────────────────

vi.mock('@/services/localStorageService', () => ({
  saveNodePreferences: vi.fn()
}))

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

// Stub Draggable so it renders its #item slot for each element of modelValue.
const DraggableStub = {
  name: 'Draggable',
  props: ['modelValue', 'itemKey', 'handle'],
  template: '<div class="draggable-stub"><template v-for="(element, index) in modelValue" :key="index"><slot name="item" :element="element" :index="index" /></template></div>'
}

// Stub PrimeVue Drawer: renders its default slot directly so we can test the body.
const DrawerStub = {
  name: 'Drawer',
  props: ['visible', 'position', 'header', 'style'],
  emits: ['update:visible'],
  template: '<div class="drawer-stub" v-if="visible"><slot /></div>'
}

// ── Mount helper ───────────────────────────────────────────────────────────────

const mountDrawer = () =>
  mount(ColumnSelectionDrawer, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs: {
        Draggable: DraggableStub,
        Drawer: DrawerStub
      }
    }
  })

// ── Tests ──────────────────────────────────────────────────────────────────────

describe('ColumnSelectionDrawer.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useNodeStructureStore>

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountDrawer()
    store = useNodeStructureStore()

    // Seed the store with two selected columns so the drawer has rows to display.
    store.columns = defaultColumns.map(c => ({ ...c }))
    store.columnsDrawerState = { visible: true, isAdvanceFilterModal: false }

    await nextTick()
    await nextTick()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('rendering', () => {
    it('renders a Select per selected column', async () => {
      const selectedCount = defaultColumns.filter(c => c.selected).length
      const selects = wrapper.findAllComponents({ name: 'Select' })
      expect(selects.length).toBe(selectedCount)
    })

    it('renders the Draggable with drag-handle class on each row', () => {
      expect(wrapper.findComponent({ name: 'Draggable' }).exists()).toBe(true)
      const handles = wrapper.findAll('.drag-handle')
      const selectedCount = defaultColumns.filter(c => c.selected).length
      expect(handles.length).toBe(selectedCount)
    })
  })

  describe('Add Column', () => {
    it('adds a new row when Add Column is clicked', async () => {
      const selectedCount = defaultColumns.filter(c => c.selected).length
      const addBtn = wrapper.findAll('button').find(b => b.text().includes('Add Column'))
      expect(addBtn).toBeDefined()

      await addBtn!.trigger('click')
      await nextTick()

      const selects = wrapper.findAllComponents({ name: 'Select' })
      expect(selects.length).toBe(selectedCount + 1)
    })
  })

  describe('Remove Column', () => {
    it('removes a row when the remove button for that row is clicked', async () => {
      const selectedCount = defaultColumns.filter(c => c.selected).length
      // Each row has a remove button with data-test="remove-column-{index}"
      const removeBtn = wrapper.find('[data-test="remove-column-0"]')
      expect(removeBtn.exists()).toBe(true)

      await removeBtn.trigger('click')
      await nextTick()

      const selects = wrapper.findAllComponents({ name: 'Select' })
      expect(selects.length).toBe(selectedCount - 1)
    })
  })

  describe('Save', () => {
    it('updates store.columns and closes the drawer when Save is clicked', async () => {
      const saveBtn = wrapper.findAll('button').find(b => b.text().includes('Save'))
      expect(saveBtn).toBeDefined()

      store.getNodePreferences = vi.fn().mockResolvedValue({})

      await saveBtn!.trigger('click')
      await flushPromises()

      // columnsDrawerState should be set to false
      expect(store.columnsDrawerState.visible).toBe(false)
    })

    it('persists the canonical column label (by id) when a re-added column is selected', async () => {
      store.getNodePreferences = vi.fn().mockResolvedValue({})

      // "Add Column" appends a blank row { name: '', value: '' }.
      const addBtn = wrapper.findAll('button').find(b => b.text().includes('Add Column'))
      await addBtn!.trigger('click')
      await nextTick()

      // Select a not-currently-selected column on the new (last) row. The real
      // PrimeVue Select (optionValue="value") emits only the id, leaving the
      // row's `name` empty — this is the re-add scenario from the bug report.
      const reAdded = defaultColumns.find(c => !c.selected)! // { id: 'id', label: 'ID' }
      const selects = wrapper.findAllComponents({ name: 'Select' })
      selects[selects.length - 1].vm.$emit('update:modelValue', reAdded.id)
      await nextTick()

      const saveBtn = wrapper.findAll('button').find(b => b.text().includes('Save'))
      await saveBtn!.trigger('click')
      await flushPromises()

      const persisted = store.columns.find(c => c.id === reAdded.id)
      expect(persisted).toBeDefined()
      // Before the fix this was '' (label came from the unsynced row.name),
      // so the DataTable header rendered empty.
      expect(persisted!.label).toBe(reAdded.label)
    })

    it('does not persist a phantom column when a blank row is saved without a selection', async () => {
      store.getNodePreferences = vi.fn().mockResolvedValue({})
      const selectedCount = defaultColumns.filter(c => c.selected).length

      // "Add Column" appends a blank row, then Save without picking anything.
      const addBtn = wrapper.findAll('button').find(b => b.text().includes('Add Column'))
      await addBtn!.trigger('click')
      await nextTick()

      const saveBtn = wrapper.findAll('button').find(b => b.text().includes('Save'))
      await saveBtn!.trigger('click')
      await flushPromises()

      // The blank row (empty id) must not be persisted as a column.
      expect(store.columns.some(c => !c.id)).toBe(false)
      expect(store.columns.length).toBe(selectedCount)
    })
  })

  describe('Drawer visibility', () => {
    it('hides content when columnsDrawerState.visible is false', async () => {
      store.columnsDrawerState = { visible: false, isAdvanceFilterModal: false }
      await nextTick()

      // DrawerStub only renders content when visible is true
      expect(wrapper.find('.drawer-stub').exists()).toBe(false)
    })

    it('shows content when columnsDrawerState.visible is true', async () => {
      store.columnsDrawerState = { visible: true, isAdvanceFilterModal: false }
      await nextTick()

      expect(wrapper.find('.drawer-stub').exists()).toBe(true)
    })
  })
})

import MibGroupsTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/MibGroupsTable.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionMibGroup } from '@/types/snmpDataCollection'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

// Lightweight stubs for the child dialogs/drawer so these tests focus on the
// table's own behaviour and the props/events it exchanges with them.
const stubs = {
  DeleteConfirmationDialog: {
    name: 'DeleteConfirmationDialog',
    template: '<div class="delete-dialog-stub"></div>',
    props: ['visible', 'selected', 'type'],
    emits: ['close', 'confirm']
  },
  SnmpDataCollectionChangeStatusDialog: {
    name: 'SnmpDataCollectionChangeStatusDialog',
    template: '<div class="change-status-dialog-stub"></div>',
    props: ['visible', 'selected', 'type', 'status'],
    emits: ['close', 'confirm']
  },
  MibGroupCreationDrawer: {
    name: 'MibGroupCreationDrawer',
    template: '<div class="mib-group-creation-drawer-stub"></div>'
  }
}

describe('MibGroupsTable.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>
  let mockMibGroup: SnmpCollectionMibGroup
  let mockMibGroup2: SnmpCollectionMibGroup
  let disabledMibGroup: SnmpCollectionMibGroup

  const mountTable = () => mount(MibGroupsTable, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs
    }
  })

  beforeEach(async () => {
    vi.clearAllMocks()
    vi.useFakeTimers()

    mockMibGroup = {
      id: 1,
      name: 'mib2-interfaces',
      ifType: 'all',
      mibGroupNames: ['ifTable', 'ifXTable'],
      mibObjects: '[{"alias":"ifIndex","oid":"1.3.6.1.2.1.2.2.1.1","instance":"ifIndex","type":"gauge"}]',
      mibObjProperties: '[]',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }
    mockMibGroup2 = {
      id: 2,
      name: 'mib2-host-resources',
      ifType: 'ignore',
      mibGroupNames: ['hrStorageTable'],
      mibObjects: '[{"alias":"hrStorageIndex","oid":"1.3.6.1.2.1.25.2.3.1.1","instance":"hrStorageIndex","type":"gauge"}]',
      mibObjProperties: '[]',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }
    disabledMibGroup = {
      id: 3,
      name: 'disabled-mib-group',
      ifType: 'all',
      mibGroupNames: ['disabledTable'],
      mibObjects: '[]',
      mibObjProperties: '[]',
      enabled: false,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }

    wrapper = mountTable()
    store = useSnmpDataCollectionDetailStore()
    store.mibGroups = []
    store.mibGroupsSearchTerm = ''
    store.mibGroupsPagination = { page: 1, pageSize: 10, total: 0 }
    store.mibGroupsSorting = { sortKey: 'createdTime', sortOrder: 'desc' }
    store.fetchMibGroups = vi.fn().mockResolvedValue(undefined)
    store.resetMibGroupsFilters = vi.fn().mockResolvedValue(undefined)
    store.onChangeMibGroupsSearchTerm = vi.fn().mockResolvedValue(undefined)
    store.onMibGroupsPageChange = vi.fn().mockResolvedValue(undefined)
    store.onMibGroupsPageSizeChange = vi.fn().mockResolvedValue(undefined)
    store.onMibGroupsSortChange = vi.fn().mockResolvedValue(undefined)
    store.openMibGroupCreationDrawer = vi.fn()
    store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any

    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.useRealTimers()
  })

  describe('Initial Rendering', () => {
    it('renders the container and header controls', () => {
      expect(wrapper.find('.mib-groups-table-container').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="add-mib-group-button"]').exists()).toBe(true)
    })

    it('renders the child dialogs and creation drawer', () => {
      expect(wrapper.findComponent({ name: 'DeleteConfirmationDialog' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'MibGroupCreationDrawer' }).exists()).toBe(true)
    })
  })

  describe('Empty State', () => {
    it('shows EmptyList and no table when there are no mib groups', async () => {
      store.mibGroups = []
      await nextTick()
      expect(wrapper.find('[data-test="mib-groups-table"]').exists()).toBe(false)
      const emptyList = wrapper.findComponent({ name: 'EmptyList' })
      expect(emptyList.exists()).toBe(true)
      expect(wrapper.text()).toContain('No MIB Groups found.')
    })
  })

  describe('Table Rendering with Data', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup, mockMibGroup2]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 2 }
      await nextTick()
    })

    it('renders the DataTable with a row per mib group', () => {
      expect(wrapper.find('[data-test="mib-groups-table"]').exists()).toBe(true)
      const bodyRows = wrapper.findAll('tbody tr')
      expect(bodyRows.length).toBe(2)
    })

    it('renders name and ifType values', () => {
      expect(wrapper.text()).toContain('mib2-interfaces')
      expect(wrapper.text()).toContain('all')
      expect(wrapper.text()).toContain('mib2-host-resources')
      expect(wrapper.text()).toContain('ignore')
    })

    it.each([
      { enabled: true, expectedText: 'Enabled', expectedClass: 'enabled-tag' },
      { enabled: false, expectedText: 'Disabled', expectedClass: 'disabled-tag' }
    ])('renders a $expectedText status tag', async ({ enabled, expectedText, expectedClass }) => {
      store.mibGroups = [{ ...mockMibGroup, enabled }]
      await nextTick()
      const tag = wrapper.find('[data-test="status-tag"]')
      expect(tag.text()).toBe(expectedText)
      expect(tag.classes()).toContain(expectedClass)
    })
  })

  describe('Search', () => {
    it('updates the store term immediately and debounces the fetch', () => {
      wrapper.vm.onChangeSearchTerm('  interfaces  ')
      expect(store.mibGroupsSearchTerm).toBe('  interfaces  ')
      expect(store.onChangeMibGroupsSearchTerm).not.toHaveBeenCalled()

      vi.advanceTimersByTime(500)
      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledWith('interfaces')
    })

    it('debounces rapid changes to a single trailing call', () => {
      wrapper.vm.onChangeSearchTerm('a')
      wrapper.vm.onChangeSearchTerm('ab')
      wrapper.vm.onChangeSearchTerm('abc')
      vi.advanceTimersByTime(500)
      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeMibGroupsSearchTerm).toHaveBeenCalledWith('abc')
    })
  })

  describe('Refresh / Add / Edit', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await nextTick()
    })

    it('calls resetMibGroupsFilters on refresh', async () => {
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetMibGroupsFilters).toHaveBeenCalledTimes(1)
    })

    it('opens the creation drawer in Create mode from Add', async () => {
      await wrapper.get('[data-test="add-mib-group-button"]').trigger('click')
      expect(store.openMibGroupCreationDrawer).toHaveBeenCalledWith(null, CreateEditMode.Create)
    })

    it('opens the creation drawer in Edit mode from the row edit button', async () => {
      await wrapper.get('[data-test="edit-button"]').trigger('click')
      expect(store.openMibGroupCreationDrawer).toHaveBeenCalledWith(mockMibGroup, CreateEditMode.Edit)
    })
  })

  describe('Lazy sort and pagination', () => {
    it('maps a sort event to onMibGroupsSortChange', () => {
      wrapper.vm.onSort({ sortField: 'name', sortOrder: 1 })
      expect(store.onMibGroupsSortChange).toHaveBeenCalledWith('name', 'asc')

      wrapper.vm.onSort({ sortField: 'ifType', sortOrder: -1 })
      expect(store.onMibGroupsSortChange).toHaveBeenCalledWith('ifType', 'desc')
    })

    it('falls back to default sort when no field is present', () => {
      wrapper.vm.onSort({ sortField: null, sortOrder: 1 })
      expect(store.onMibGroupsSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it('maps a page event to onMibGroupsPageChange (1-based)', () => {
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 50 }
      wrapper.vm.onPage({ page: 2, rows: 10, first: 20, pageCount: 5 })
      expect(store.onMibGroupsPageChange).toHaveBeenCalledWith(3)
    })

    it('maps a page-size change to onMibGroupsPageSizeChange', () => {
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 50 }
      wrapper.vm.onPage({ page: 0, rows: 20, first: 0, pageCount: 3 })
      expect(store.onMibGroupsPageSizeChange).toHaveBeenCalledWith(20)
    })
  })

  describe('Row expansion content', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      store.mibGroupsPagination = { page: 1, pageSize: 10, total: 1 }
      await nextTick()
    })

    it('renders the expansion content for an expanded row', async () => {
      wrapper.vm.expandedRows = { [mockMibGroup.id]: true }
      await nextTick()

      const expanded = wrapper.find('.expanded-content')
      expect(expanded.exists()).toBe(true)
      expect(expanded.text()).toContain('ifTable, ifXTable')
      expect(expanded.text()).toContain('ifIndex')
      expect(expanded.text()).toContain('1.3.6.1.2.1.2.2.1.1')
      expect(expanded.text()).toContain('gauge')
    })

    it('omits the MIB Objects section when there are none', async () => {
      store.mibGroups = [disabledMibGroup]
      await nextTick()
      wrapper.vm.expandedRows = { [disabledMibGroup.id]: true }
      await nextTick()

      const expanded = wrapper.find('.expanded-content')
      expect(expanded.exists()).toBe(true)
      expect(expanded.text()).not.toContain('Object 1')
      expect(expanded.text()).not.toContain('MIB Objects:')
    })
  })

  describe('Row action menu', () => {
    it('builds enable/disable + delete menu items for the targeted row', () => {
      wrapper.vm.rowMenuTarget = { ...mockMibGroup, enabled: true }
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).toContain('Disable MIB Group')
      expect(labels).toContain('Delete MIB Group')
    })

    it('shows Enable label when the row is disabled', () => {
      wrapper.vm.rowMenuTarget = { ...mockMibGroup, enabled: false }
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).toContain('Enable MIB Group')
    })

    it('omits Delete when the source is plugin-sourced', () => {
      store.selectedCollectionSource = { id: 1, name: 'Plugin', uploadedBy: 'opennms-plugins' } as any
      wrapper.vm.rowMenuTarget = mockMibGroup
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).not.toContain('Delete MIB Group')
    })

    it('change-status menu command opens the change-status dialog', () => {
      wrapper.vm.rowMenuTarget = mockMibGroup
      wrapper.vm.rowMenuItems
        .find((i: any) => i.label.includes('MIB Group') && !i.label.includes('Delete'))
        .command()
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedMibGroup?.id).toBe(mockMibGroup.id)
    })

    it('delete menu command opens the delete dialog', () => {
      wrapper.vm.rowMenuTarget = mockMibGroup
      wrapper.vm.rowMenuItems.find((i: any) => i.label === 'Delete MIB Group').command()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedMibGroup?.id).toBe(mockMibGroup.id)
    })
  })

  describe('Delete dialog wiring', () => {
    beforeEach(async () => {
      store.mibGroups = [mockMibGroup]
      await nextTick()
    })

    it('passes selection through to DeleteConfirmationDialog', async () => {
      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await nextTick()
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('selected')?.id).toBe(mockMibGroup.id)
      expect(dialog.props('type')).toBe('mib-group')
    })

    it('clears state on close', async () => {
      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await nextTick()
      wrapper.findComponent({ name: 'DeleteConfirmationDialog' }).vm.$emit('close')
      await nextTick()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedMibGroup).toBeNull()
    })
  })

  describe('Delete action', () => {
    let deleteMibGroupsSpy: any

    beforeEach(async () => {
      const service = await import('@/services/snmpDataCollectionService')
      deleteMibGroupsSpy = vi.spyOn(service, 'deleteMibGroups')
      store.mibGroups = [mockMibGroup]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await nextTick()
    })

    it('calls deleteMibGroups and refetches on success', async () => {
      deleteMibGroupsSpy.mockResolvedValue(true)
      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await nextTick()

      await wrapper.findComponent({ name: 'DeleteConfirmationDialog' }).vm.$emit('confirm', { id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      expect(deleteMibGroupsSpy).toHaveBeenCalledWith(1, [mockMibGroup.id])
      expect(store.fetchMibGroups).toHaveBeenCalled()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
    })

    it.each([
      { desc: 'type mismatch', selected: { id: 1, name: 'mib2-interfaces' }, type: 'wrong' },
      { desc: 'id mismatch', selected: { id: 999, name: 'mib2-interfaces' }, type: 'mib-group' },
      { desc: 'null selection', selected: null, type: 'mib-group' }
    ])('does not call deleteMibGroups on $desc', async ({ selected, type }) => {
      deleteMibGroupsSpy.mockResolvedValue(true)
      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await nextTick()
      await wrapper.vm.deleteMibGroup(selected, type)
      await flushPromises()
      expect(deleteMibGroupsSpy).not.toHaveBeenCalled()
    })

    it('keeps the dialog open on failure', async () => {
      deleteMibGroupsSpy.mockResolvedValue(false)
      wrapper.vm.openDeleteMibGroupDialog(mockMibGroup)
      await nextTick()
      await wrapper.vm.deleteMibGroup({ id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedMibGroup).not.toBeNull()
    })
  })

  describe('Change status action', () => {
    let enableDisableSpy: any

    beforeEach(async () => {
      const service = await import('@/services/snmpDataCollectionService')
      enableDisableSpy = vi.spyOn(service, 'enableDisableSnmpMibGroups')
      store.mibGroups = [mockMibGroup]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await nextTick()
    })

    it('toggles status via enableDisableSnmpMibGroups and refetches on success', async () => {
      enableDisableSpy.mockResolvedValue(true)
      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      await nextTick()

      await wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' }).vm.$emit('confirm', { id: mockMibGroup.id, name: mockMibGroup.name }, 'mib-group')
      await flushPromises()

      // mockMibGroup.enabled is true, so the new status should be disabled (false)
      expect(enableDisableSpy).toHaveBeenCalledWith(1, false, [mockMibGroup.id])
      expect(store.fetchMibGroups).toHaveBeenCalled()
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
    })

    it('passes the inverse status to the change-status dialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockMibGroup)
      await nextTick()
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Disable')
    })
  })
})

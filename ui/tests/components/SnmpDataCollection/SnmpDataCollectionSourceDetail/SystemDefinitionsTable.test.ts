import SystemDefinitionsTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/SystemDefinitionsTable.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionSystemDef } from '@/types/snmpDataCollection'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

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
  SystemDefinitionCreationDrawer: {
    name: 'SystemDefinitionCreationDrawer',
    template: '<div class="system-definition-creation-drawer-stub"></div>'
  }
}

describe('SystemDefinitionsTable.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>
  let mockSystemDef: SnmpCollectionSystemDef
  let disabledSystemDef: SnmpCollectionSystemDef

  const mountTable = () => mount(SystemDefinitionsTable, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs
    }
  })

  beforeEach(async () => {
    vi.clearAllMocks()
    vi.useFakeTimers()

    mockSystemDef = {
      id: 1,
      name: 'Cisco Routers',
      sysoid: '.1.3.6.1.4.1.9',
      sysoidMask: '.1.3.6.1.4.1.9.*',
      ipAddresses: [],
      ipAddressMasks: [],
      mibGroupNames: ['mib2-interfaces', 'cisco-cpu'],
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }
    disabledSystemDef = {
      ...mockSystemDef,
      id: 2,
      name: 'disabled-def',
      enabled: false
    }

    wrapper = mountTable()
    store = useSnmpDataCollectionDetailStore()
    store.systemDefinitions = []
    store.systemDefsSearchTerm = ''
    store.systemDefsPagination = { page: 1, pageSize: 10, total: 0 }
    store.systemDefsSorting = { sortKey: 'createdTime', sortOrder: 'desc' }
    store.fetchSystemDefinitions = vi.fn().mockResolvedValue(undefined)
    store.resetSystemDefinitionsFilters = vi.fn().mockResolvedValue(undefined)
    store.onChangeSystemDefsSearchTerm = vi.fn().mockResolvedValue(undefined)
    store.onSystemDefsPageChange = vi.fn().mockResolvedValue(undefined)
    store.onSystemDefsPageSizeChange = vi.fn().mockResolvedValue(undefined)
    store.onSystemDefsSortChange = vi.fn().mockResolvedValue(undefined)
    store.openSystemDefCreationDrawer = vi.fn()
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
      expect(wrapper.find('.system-definitions-table-container').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="add-system-definition-button"]').exists()).toBe(true)
    })

    it('renders the child dialogs and creation drawer', () => {
      expect(wrapper.findComponent({ name: 'DeleteConfirmationDialog' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'SystemDefinitionCreationDrawer' }).exists()).toBe(true)
    })
  })

  describe('Empty State', () => {
    it('shows EmptyList and no table when there are no system definitions', async () => {
      store.systemDefinitions = []
      await nextTick()
      expect(wrapper.find('[data-test="system-definitions-table"]').exists()).toBe(false)
      expect(wrapper.findComponent({ name: 'EmptyList' }).exists()).toBe(true)
      expect(wrapper.text()).toContain('No System Definitions found.')
    })
  })

  describe('Table Rendering with Data', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef, disabledSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 2 }
      await nextTick()
    })

    it('renders a row per system definition', () => {
      expect(wrapper.find('[data-test="system-definitions-table"]').exists()).toBe(true)
      expect(wrapper.findAll('tbody tr').length).toBe(2)
    })

    it('renders name, sysoid and mask values', () => {
      expect(wrapper.text()).toContain('Cisco Routers')
      expect(wrapper.text()).toContain('.1.3.6.1.4.1.9')
    })

    it.each([
      { enabled: true, expectedText: 'Enabled', expectedClass: 'enabled-tag' },
      { enabled: false, expectedText: 'Disabled', expectedClass: 'disabled-tag' }
    ])('renders a $expectedText status tag', async ({ enabled, expectedText, expectedClass }) => {
      store.systemDefinitions = [{ ...mockSystemDef, enabled }]
      await nextTick()
      const tag = wrapper.find('[data-test="status-tag"]')
      expect(tag.text()).toBe(expectedText)
      expect(tag.classes()).toContain(expectedClass)
    })
  })

  describe('Search', () => {
    it('updates the store term immediately and debounces the fetch', () => {
      wrapper.vm.onChangeSearchTerm('  cisco  ')
      expect(store.systemDefsSearchTerm).toBe('  cisco  ')
      expect(store.onChangeSystemDefsSearchTerm).not.toHaveBeenCalled()
      vi.advanceTimersByTime(500)
      expect(store.onChangeSystemDefsSearchTerm).toHaveBeenCalledWith('cisco')
    })
  })

  describe('Refresh / Add / Edit', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef]
      await nextTick()
    })

    it('calls resetSystemDefinitionsFilters on refresh', async () => {
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetSystemDefinitionsFilters).toHaveBeenCalledTimes(1)
    })

    it('opens the creation drawer in Create mode from Add', async () => {
      await wrapper.get('[data-test="add-system-definition-button"]').trigger('click')
      expect(store.openSystemDefCreationDrawer).toHaveBeenCalledWith(null, CreateEditMode.Create)
    })

    it('opens the creation drawer in Edit mode from the row edit button', async () => {
      await wrapper.get('[data-test="edit-button"]').trigger('click')
      expect(store.openSystemDefCreationDrawer).toHaveBeenCalledWith(mockSystemDef, CreateEditMode.Edit)
    })
  })

  describe('Lazy sort and pagination', () => {
    it('maps a sort event to onSystemDefsSortChange', () => {
      wrapper.vm.onSort({ sortField: 'name', sortOrder: 1 })
      expect(store.onSystemDefsSortChange).toHaveBeenCalledWith('name', 'asc')
      wrapper.vm.onSort({ sortField: 'sysoid', sortOrder: -1 })
      expect(store.onSystemDefsSortChange).toHaveBeenCalledWith('sysoid', 'desc')
    })

    it('falls back to default sort when no field is present', () => {
      wrapper.vm.onSort({ sortField: null, sortOrder: 1 })
      expect(store.onSystemDefsSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it('maps page and page-size events to the store', () => {
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 50 }
      wrapper.vm.onPage({ page: 2, rows: 10, first: 20, pageCount: 5 })
      expect(store.onSystemDefsPageChange).toHaveBeenCalledWith(3)
      wrapper.vm.onPage({ page: 0, rows: 20, first: 0, pageCount: 3 })
      expect(store.onSystemDefsPageSizeChange).toHaveBeenCalledWith(20)
    })
  })

  describe('Row expansion content', () => {
    beforeEach(async () => {
      store.systemDefinitions = [mockSystemDef]
      store.systemDefsPagination = { page: 1, pageSize: 10, total: 1 }
      await nextTick()
    })

    it('renders the MIB group names when expanded', async () => {
      wrapper.vm.expandedRows = { [mockSystemDef.id]: true }
      await nextTick()
      const expanded = wrapper.find('.expanded-content')
      expect(expanded.exists()).toBe(true)
      expect(expanded.text()).toContain('mib2-interfaces, cisco-cpu')
    })
  })

  describe('Row action menu', () => {
    it('builds enable/disable + delete menu items for the targeted row', () => {
      wrapper.vm.rowMenuTarget = { ...mockSystemDef, enabled: true }
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).toContain('Disable Definition')
      expect(labels).toContain('Delete Definition')
    })

    it('omits Delete when the source is plugin-sourced', () => {
      store.selectedCollectionSource = { id: 1, name: 'Plugin', uploadedBy: 'opennms-plugins' } as any
      wrapper.vm.rowMenuTarget = mockSystemDef
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).not.toContain('Delete Definition')
    })

    it('delete menu command opens the delete dialog', () => {
      wrapper.vm.rowMenuTarget = mockSystemDef
      wrapper.vm.rowMenuItems.find((i: any) => i.label === 'Delete Definition').command()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSystemDef?.id).toBe(mockSystemDef.id)
    })
  })

  describe('Delete action', () => {
    let deleteSpy: any

    beforeEach(async () => {
      const service = await import('@/services/snmpDataCollectionService')
      deleteSpy = vi.spyOn(service, 'deleteSystemDefinitions')
      store.systemDefinitions = [mockSystemDef]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await nextTick()
    })

    it('calls deleteSystemDefinitions and refetches on success', async () => {
      deleteSpy.mockResolvedValue(true)
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await nextTick()
      await wrapper.findComponent({ name: 'DeleteConfirmationDialog' }).vm.$emit('confirm', { id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()
      expect(deleteSpy).toHaveBeenCalledWith(1, [mockSystemDef.id])
      expect(store.fetchSystemDefinitions).toHaveBeenCalled()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
    })

    it('keeps the dialog open on failure', async () => {
      deleteSpy.mockResolvedValue(false)
      wrapper.vm.openDeleteSystemDefDialog(mockSystemDef)
      await nextTick()
      await wrapper.vm.deleteSystemDef({ id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
    })
  })

  describe('Change status action', () => {
    let enableDisableSpy: any

    beforeEach(async () => {
      const service = await import('@/services/snmpDataCollectionService')
      enableDisableSpy = vi.spyOn(service, 'enableDisableSnmpSystemDefs')
      store.systemDefinitions = [mockSystemDef]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await nextTick()
    })

    it('toggles status and refetches on success', async () => {
      enableDisableSpy.mockResolvedValue(true)
      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await nextTick()
      await wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' }).vm.$emit('confirm', { id: mockSystemDef.id, name: mockSystemDef.name }, 'system-def')
      await flushPromises()
      expect(enableDisableSpy).toHaveBeenCalledWith(1, false, [mockSystemDef.id])
      expect(store.fetchSystemDefinitions).toHaveBeenCalled()
    })

    it('passes the inverse status to the change-status dialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockSystemDef)
      await nextTick()
      expect(wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' }).props('status')).toBe('Disable')
    })
  })
})

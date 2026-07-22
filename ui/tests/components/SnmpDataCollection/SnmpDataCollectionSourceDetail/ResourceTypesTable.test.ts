import ResourceTypesTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/ResourceTypesTable.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionResourceType } from '@/types/snmpDataCollection'
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
  ResourceTypeCreationDrawer: {
    name: 'ResourceTypeCreationDrawer',
    template: '<div class="resource-type-creation-drawer-stub"></div>'
  }
}

describe('ResourceTypesTable.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>
  let mockResourceType: SnmpCollectionResourceType
  let disabledResourceType: SnmpCollectionResourceType

  const mountTable = () => mount(ResourceTypesTable, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs
    }
  })

  beforeEach(async () => {
    vi.clearAllMocks()
    vi.useFakeTimers()

    mockResourceType = {
      id: 1,
      name: 'hrStorageIndex',
      label: 'Storage Index',
      resourceLabel: '${hrStorageDescr}',
      persistenceSelectorStrategy: 'org.opennms.PersistAll',
      persistenceSelectorParams: '[]',
      storageStrategy: 'org.opennms.IndexStorage',
      storageStrategyParams: '[]',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }
    disabledResourceType = {
      ...mockResourceType,
      id: 2,
      name: 'disabled-rt',
      label: 'Disabled RT',
      enabled: false
    }

    wrapper = mountTable()
    store = useSnmpDataCollectionDetailStore()
    store.resourceTypes = []
    store.resourceTypesSearchTerm = ''
    store.resourceTypesPagination = { page: 1, pageSize: 10, total: 0 }
    store.resourceTypesSorting = { sortKey: 'createdTime', sortOrder: 'desc' }
    store.fetchResourceTypes = vi.fn().mockResolvedValue(undefined)
    store.resetResourceTypesFilters = vi.fn().mockResolvedValue(undefined)
    store.onChangeResourceTypesSearchTerm = vi.fn().mockResolvedValue(undefined)
    store.onResourceTypesPageChange = vi.fn().mockResolvedValue(undefined)
    store.onResourceTypesPageSizeChange = vi.fn().mockResolvedValue(undefined)
    store.onResourceTypesSortChange = vi.fn().mockResolvedValue(undefined)
    store.openResourceTypeCreationDrawer = vi.fn()
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
      expect(wrapper.find('.resource-types-table-container').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="refresh-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="add-resource-type-button"]').exists()).toBe(true)
    })

    it('renders the child dialogs and creation drawer', () => {
      expect(wrapper.findComponent({ name: 'DeleteConfirmationDialog' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'ResourceTypeCreationDrawer' }).exists()).toBe(true)
    })
  })

  describe('Empty State', () => {
    it('shows EmptyList and no table when there are no resource types', async () => {
      store.resourceTypes = []
      await nextTick()
      expect(wrapper.find('[data-test="resource-types-table"]').exists()).toBe(false)
      expect(wrapper.findComponent({ name: 'EmptyList' }).exists()).toBe(true)
      expect(wrapper.text()).toContain('No Resource Types found.')
    })
  })

  describe('Table Rendering with Data', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType, disabledResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 2 }
      await nextTick()
    })

    it('renders a row per resource type', () => {
      expect(wrapper.find('[data-test="resource-types-table"]').exists()).toBe(true)
      expect(wrapper.findAll('tbody tr').length).toBe(2)
    })

    it('renders name, label and resourceLabel values', () => {
      expect(wrapper.text()).toContain('hrStorageIndex')
      expect(wrapper.text()).toContain('Storage Index')
    })

    it.each([
      { enabled: true, expectedText: 'Enabled', expectedClass: 'enabled-tag' },
      { enabled: false, expectedText: 'Disabled', expectedClass: 'disabled-tag' }
    ])('renders a $expectedText status tag', async ({ enabled, expectedText, expectedClass }) => {
      store.resourceTypes = [{ ...mockResourceType, enabled }]
      await nextTick()
      const tag = wrapper.find('[data-test="status-tag"]')
      expect(tag.text()).toBe(expectedText)
      expect(tag.classes()).toContain(expectedClass)
    })
  })

  describe('Search', () => {
    it('updates the store term immediately and debounces the fetch', () => {
      wrapper.vm.onChangeSearchTerm('  storage  ')
      expect(store.resourceTypesSearchTerm).toBe('  storage  ')
      expect(store.onChangeResourceTypesSearchTerm).not.toHaveBeenCalled()
      vi.advanceTimersByTime(500)
      expect(store.onChangeResourceTypesSearchTerm).toHaveBeenCalledWith('storage')
    })
  })

  describe('Refresh / Add / Edit', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      await nextTick()
    })

    it('calls resetResourceTypesFilters on refresh', async () => {
      await wrapper.get('[data-test="refresh-button"]').trigger('click')
      expect(store.resetResourceTypesFilters).toHaveBeenCalledTimes(1)
    })

    it('opens the creation drawer in Create mode from Add', async () => {
      await wrapper.get('[data-test="add-resource-type-button"]').trigger('click')
      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledWith(null, CreateEditMode.Create)
    })

    it('opens the creation drawer in Edit mode from the row edit button', async () => {
      await wrapper.get('[data-test="edit-button"]').trigger('click')
      expect(store.openResourceTypeCreationDrawer).toHaveBeenCalledWith(mockResourceType, CreateEditMode.Edit)
    })
  })

  describe('Lazy sort and pagination', () => {
    it('maps a sort event to onResourceTypesSortChange', () => {
      wrapper.vm.onSort({ sortField: 'name', sortOrder: 1 })
      expect(store.onResourceTypesSortChange).toHaveBeenCalledWith('name', 'asc')
      wrapper.vm.onSort({ sortField: 'label', sortOrder: -1 })
      expect(store.onResourceTypesSortChange).toHaveBeenCalledWith('label', 'desc')
    })

    it('falls back to default sort when no field is present', () => {
      wrapper.vm.onSort({ sortField: null, sortOrder: 1 })
      expect(store.onResourceTypesSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it('maps page and page-size events to the store', () => {
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 50 }
      wrapper.vm.onPage({ page: 2, rows: 10, first: 20, pageCount: 5 })
      expect(store.onResourceTypesPageChange).toHaveBeenCalledWith(3)
      wrapper.vm.onPage({ page: 0, rows: 20, first: 0, pageCount: 3 })
      expect(store.onResourceTypesPageSizeChange).toHaveBeenCalledWith(20)
    })
  })

  describe('Row expansion content', () => {
    beforeEach(async () => {
      store.resourceTypes = [mockResourceType]
      store.resourceTypesPagination = { page: 1, pageSize: 10, total: 1 }
      await nextTick()
    })

    it('renders storage and persistence strategy when expanded', async () => {
      wrapper.vm.expandedRows = { [mockResourceType.id]: true }
      await nextTick()
      const expanded = wrapper.find('.expanded-content')
      expect(expanded.exists()).toBe(true)
      expect(expanded.text()).toContain('org.opennms.IndexStorage')
      expect(expanded.text()).toContain('org.opennms.PersistAll')
    })
  })

  describe('Row action menu', () => {
    it('builds enable/disable + delete menu items for the targeted row', () => {
      wrapper.vm.rowMenuTarget = { ...mockResourceType, enabled: true }
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).toContain('Disable Resource Type')
      expect(labels).toContain('Delete Resource Type')
    })

    it('omits Delete when the source is plugin-sourced', () => {
      store.selectedCollectionSource = { id: 1, name: 'Plugin', uploadedBy: 'opennms-plugins' } as any
      wrapper.vm.rowMenuTarget = mockResourceType
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).not.toContain('Delete Resource Type')
    })

    it('delete menu command opens the delete dialog', () => {
      wrapper.vm.rowMenuTarget = mockResourceType
      wrapper.vm.rowMenuItems.find((i: any) => i.label === 'Delete Resource Type').command()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedResourceType?.id).toBe(mockResourceType.id)
    })
  })

  describe('Delete action', () => {
    let deleteSpy: any

    beforeEach(async () => {
      const service = await import('@/services/snmpDataCollectionService')
      deleteSpy = vi.spyOn(service, 'deleteResourceTypes')
      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await nextTick()
    })

    it('calls deleteResourceTypes and refetches on success', async () => {
      deleteSpy.mockResolvedValue(true)
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await nextTick()
      await wrapper.findComponent({ name: 'DeleteConfirmationDialog' }).vm.$emit('confirm', { id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()
      expect(deleteSpy).toHaveBeenCalledWith(1, [mockResourceType.id])
      expect(store.fetchResourceTypes).toHaveBeenCalled()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
    })

    it('keeps the dialog open on failure', async () => {
      deleteSpy.mockResolvedValue(false)
      wrapper.vm.openResourceTypeDeleteDialog(mockResourceType)
      await nextTick()
      await wrapper.vm.deleteResourceType({ id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
    })
  })

  describe('Change status action', () => {
    let enableDisableSpy: any

    beforeEach(async () => {
      const service = await import('@/services/snmpDataCollectionService')
      enableDisableSpy = vi.spyOn(service, 'enableDisableSnmpResourceTypes')
      store.resourceTypes = [mockResourceType]
      store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
      await nextTick()
    })

    it('toggles status and refetches on success', async () => {
      enableDisableSpy.mockResolvedValue(true)
      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await nextTick()
      await wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' }).vm.$emit('confirm', { id: mockResourceType.id, name: mockResourceType.name }, 'resource-type')
      await flushPromises()
      expect(enableDisableSpy).toHaveBeenCalledWith(1, false, [mockResourceType.id])
      expect(store.fetchResourceTypes).toHaveBeenCalled()
    })

    it('passes the inverse status to the change-status dialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockResourceType)
      await nextTick()
      expect(wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' }).props('status')).toBe('Disable')
    })
  })
})

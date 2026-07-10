import SnmpDataCollectionSourcesTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourcesTable.vue'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpCollectionSource } from '@/types/snmpDataCollection'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Tooltip from 'primevue/tooltip'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

const mockDownloadSnmpDataCollectionById = vi.fn()
const mockDeleteSnmpCollectionSources = vi.fn()
const mockEnableDisableSnmpDataCollectionSources = vi.fn()
const mockGetAllSnmpCollectionProfiles = vi.fn().mockResolvedValue([])
vi.mock('@/services/snmpDataCollectionService', () => ({
  downloadSnmpDataCollectionById: (...args: any[]) => mockDownloadSnmpDataCollectionById(...args),
  deleteSnmpCollectionSources: (...args: any[]) => mockDeleteSnmpCollectionSources(...args),
  enableDisableSnmpDataCollectionSources: (...args: any[]) => mockEnableDisableSnmpDataCollectionSources(...args),
  getAllSnmpCollectionProfiles: (...args: any[]) => mockGetAllSnmpCollectionProfiles(...args),
  // The store's fetchSnmpCollectionSources action runs on mount (before the test
  // overrides it), so this service function must resolve cleanly.
  filterSnmpCollectionSources: vi.fn().mockResolvedValue({ sources: [], totalRecords: 0 })
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: mockShowSnackBar
  })
}))

// Lightweight stubs for the child dialogs so these tests focus on the table's
// own behaviour and the props/events it exchanges with them.
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
  }
}

describe('SnmpDataCollectionSourcesTable.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionStore>
  let mockSource: SnmpCollectionSource
  let disabledMockSource: SnmpCollectionSource
  let pluginSource: SnmpCollectionSource

  const mountTable = () => mount(SnmpDataCollectionSourcesTable, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      directives: { tooltip: Tooltip },
      stubs
    }
  })

  beforeEach(async () => {
    vi.clearAllMocks()
    vi.useFakeTimers()
    mockGetAllSnmpCollectionProfiles.mockResolvedValue([])

    mockSource = {
      id: 1,
      name: 'Test Source',
      vendor: 'Cisco',
      description: 'Test description',
      enabled: true,
      uploadedBy: 'TestUser',
      createdTime: new Date('2024-01-01'),
      lastModified: new Date('2024-01-02')
    } as any
    disabledMockSource = { ...mockSource, id: 2, name: 'Disabled Source', enabled: false } as any
    pluginSource = { ...mockSource, id: 3, name: 'Plugin Source', uploadedBy: 'opennms-plugins' } as any

    wrapper = mountTable()
    store = useSnmpDataCollectionStore()
    store.sources = []
    store.sourcesSearchTerm = ''
    store.sourcesPagination = { page: 1, pageSize: 10, total: 0 }
    store.sourcesSorting = { sortKey: 'createdTime', sortOrder: 'desc' }
    store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
    store.fetchAllSourcesNames = vi.fn().mockResolvedValue(undefined)
    store.onChangeSourcesSearchTerm = vi.fn().mockResolvedValue(undefined)
    store.onSourcePageChange = vi.fn().mockResolvedValue(undefined)
    store.onSourcePageSizeChange = vi.fn().mockResolvedValue(undefined)
    store.onSourcesSortChange = vi.fn().mockResolvedValue(undefined)

    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.useRealTimers()
  })

  describe('Initial Rendering', () => {
    it('renders the container and header controls', () => {
      expect(wrapper.find('.snmp-data-collection-source-table').exists()).toBe(true)
      expect(wrapper.find('[data-test="search-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="create-source-button"]').exists()).toBe(true)
    })

    it('renders the child dialogs', () => {
      expect(wrapper.findComponent({ name: 'DeleteConfirmationDialog' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' }).exists()).toBe(true)
    })
  })

  describe('Empty State', () => {
    it('shows EmptyList and no table when there are no sources', async () => {
      store.sources = []
      await nextTick()
      expect(wrapper.find('[data-test="sources-table"]').exists()).toBe(false)
      expect(wrapper.findComponent({ name: 'EmptyList' }).exists()).toBe(true)
      expect(wrapper.text()).toContain('No results found.')
    })
  })

  describe('Table Rendering with Data', () => {
    beforeEach(async () => {
      store.sources = [mockSource, disabledMockSource]
      store.sourcesPagination = { page: 1, pageSize: 10, total: 2 }
      await nextTick()
    })

    it('renders the DataTable with a row per source', () => {
      expect(wrapper.find('[data-test="sources-table"]').exists()).toBe(true)
      expect(wrapper.findAll('tbody tr').length).toBe(2)
    })

    it('renders source names', () => {
      expect(wrapper.text()).toContain('Test Source')
      expect(wrapper.text()).toContain('Disabled Source')
    })

    it.each([
      { enabled: true, expectedText: 'Enabled', expectedClass: 'enabled-tag' },
      { enabled: false, expectedText: 'Disabled', expectedClass: 'disabled-tag' }
    ])('renders a $expectedText status tag', async ({ enabled, expectedText, expectedClass }) => {
      store.sources = [{ ...mockSource, enabled }]
      await nextTick()
      const tag = wrapper.find('[data-test="status-tag"]')
      expect(tag.text()).toBe(expectedText)
      expect(tag.classes()).toContain(expectedClass)
    })

    it('shows an em dash when a source has no attached profiles', () => {
      expect(wrapper.find('.empty-profiles').exists()).toBe(true)
    })
  })

  describe('Profiles column', () => {
    it('renders a chip per attached profile (case-insensitive match)', async () => {
      wrapper.vm.availableProfiles = [
        { id: 10, name: 'Profile A', sourceNames: ['test source'] },
        { id: 11, name: 'Profile B', sourceNames: ['other'] }
      ]
      store.sources = [mockSource]
      await nextTick()
      const matched = wrapper.vm.profilesForSource('Test Source')
      expect(matched).toEqual([{ id: 10, name: 'Profile A' }])
    })
  })

  describe('Search', () => {
    it('updates the store term immediately and debounces the fetch', () => {
      wrapper.vm.onChangeSearchTerm('  cisco  ')
      expect(store.sourcesSearchTerm).toBe('  cisco  ')
      expect(store.onChangeSourcesSearchTerm).not.toHaveBeenCalled()

      vi.advanceTimersByTime(500)
      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith('cisco')
    })

    it('debounces rapid changes to a single trailing call', () => {
      wrapper.vm.onChangeSearchTerm('a')
      wrapper.vm.onChangeSearchTerm('ab')
      wrapper.vm.onChangeSearchTerm('abc')
      vi.advanceTimersByTime(500)
      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledTimes(1)
      expect(store.onChangeSourcesSearchTerm).toHaveBeenCalledWith('abc')
    })
  })

  describe('Navigation', () => {
    it('navigates to create on the create button', async () => {
      await wrapper.get('[data-test="create-source-button"]').trigger('click')
      expect(mockPush).toHaveBeenCalledWith({
        name: 'SNMP Data Collection Source Detail',
        params: { id: 'create' }
      })
    })

    it('navigates to the source detail on view click', () => {
      wrapper.vm.onSourceClick(mockSource)
      expect(mockPush).toHaveBeenCalledWith({
        name: 'SNMP Data Collection Source Detail',
        params: { id: mockSource.id }
      })
    })
  })

  describe('Lazy sort and pagination', () => {
    it('maps a sort event to onSourcesSortChange', () => {
      wrapper.vm.onSort({ sortField: 'name', sortOrder: 1 })
      expect(store.onSourcesSortChange).toHaveBeenCalledWith('name', 'asc')

      wrapper.vm.onSort({ sortField: 'enabled', sortOrder: -1 })
      expect(store.onSourcesSortChange).toHaveBeenCalledWith('enabled', 'desc')
    })

    it('falls back to default sort when no field is present', () => {
      wrapper.vm.onSort({ sortField: null, sortOrder: 1 })
      expect(store.onSourcesSortChange).toHaveBeenCalledWith('createdTime', 'desc')
    })

    it('maps a page event to onSourcePageChange (1-based)', () => {
      store.sourcesPagination = { page: 1, pageSize: 10, total: 50 }
      wrapper.vm.onPage({ page: 2, rows: 10, first: 20, pageCount: 5 })
      expect(store.onSourcePageChange).toHaveBeenCalledWith(3)
    })

    it('maps a page-size change to onSourcePageSizeChange', () => {
      store.sourcesPagination = { page: 1, pageSize: 10, total: 50 }
      wrapper.vm.onPage({ page: 0, rows: 20, first: 0, pageCount: 3 })
      expect(store.onSourcePageSizeChange).toHaveBeenCalledWith(20)
    })
  })

  describe('Row action menu', () => {
    it('builds download + status + delete items for a non-plugin source', () => {
      wrapper.vm.rowMenuTarget = mockSource
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).toEqual(['Download XML', 'Download JSON', 'Disable Source', 'Delete Source'])
    })

    it('shows Enable label when the source is disabled', () => {
      wrapper.vm.rowMenuTarget = disabledMockSource
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).toContain('Enable Source')
    })

    it('omits Delete for a plugin-sourced source', () => {
      wrapper.vm.rowMenuTarget = pluginSource
      const labels = wrapper.vm.rowMenuItems.map((i: any) => i.label)
      expect(labels).not.toContain('Delete Source')
    })

    it('change-status command opens the change-status dialog', () => {
      wrapper.vm.rowMenuTarget = mockSource
      wrapper.vm.rowMenuItems.find((i: any) => i.label === 'Disable Source').command()
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(true)
      expect(wrapper.vm.selectedCollectionSource?.id).toBe(mockSource.id)
    })

    it('delete command opens the delete dialog', () => {
      wrapper.vm.rowMenuTarget = mockSource
      wrapper.vm.rowMenuItems.find((i: any) => i.label === 'Delete Source').command()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedCollectionSource?.id).toBe(mockSource.id)
    })
  })

  describe('Download', () => {
    it('downloads via the inline xml command', async () => {
      mockDownloadSnmpDataCollectionById.mockResolvedValue(new Blob(['<xml/>']))
      const createSpy = vi.spyOn(window.URL, 'createObjectURL').mockReturnValue('blob:url')
      vi.spyOn(window.URL, 'revokeObjectURL').mockImplementation(() => {})

      await wrapper.vm.downloadCollectionSource(mockSource, 'xml')
      await flushPromises()

      expect(mockDownloadSnmpDataCollectionById).toHaveBeenCalledWith(mockSource.id, 'xml')
      expect(createSpy).toHaveBeenCalled()
    })

    it('shows an error snackbar when download fails', async () => {
      mockDownloadSnmpDataCollectionById.mockResolvedValue(null)
      await wrapper.vm.downloadCollectionSource(mockSource, 'json')
      await flushPromises()
      expect(mockShowSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
    })
  })

  describe('Delete action', () => {
    it('deletes and refetches on a matching selection', async () => {
      mockDeleteSnmpCollectionSources.mockResolvedValue(true)
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
      await nextTick()

      await wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
        .vm.$emit('confirm', { id: mockSource.id, name: mockSource.name }, 'source')
      await flushPromises()

      expect(mockDeleteSnmpCollectionSources).toHaveBeenCalledWith([mockSource.id])
      expect(store.fetchSnmpCollectionSources).toHaveBeenCalled()
      expect(wrapper.vm.isDeleteDialogVisible).toBe(false)
    })

    it.each([
      { desc: 'type mismatch', selected: { id: 1, name: 'Test Source' }, type: 'wrong' },
      { desc: 'id mismatch', selected: { id: 999, name: 'Test Source' }, type: 'source' },
      { desc: 'null selection', selected: null, type: 'source' }
    ])('does not delete on $desc', async ({ selected, type }) => {
      mockDeleteSnmpCollectionSources.mockResolvedValue(true)
      wrapper.vm.openDeleteCollectionSourceDialog(mockSource)
      await nextTick()
      await wrapper.vm.deleteCollectionSource(selected, type)
      await flushPromises()
      expect(mockDeleteSnmpCollectionSources).not.toHaveBeenCalled()
    })
  })

  describe('Change status action', () => {
    it('toggles status and refetches on a matching selection', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      wrapper.vm.openChangeStatusDialog(mockSource)
      await nextTick()

      await wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
        .vm.$emit('confirm', { id: mockSource.id, name: mockSource.name }, 'source')
      await flushPromises()

      // mockSource.enabled is true, so the new status should be disabled (false)
      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(false, [mockSource.id])
      expect(store.fetchSnmpCollectionSources).toHaveBeenCalled()
      expect(wrapper.vm.isChangeStatusDialogVisible).toBe(false)
    })

    it('passes the inverse status to the change-status dialog', async () => {
      wrapper.vm.openChangeStatusDialog(mockSource)
      await nextTick()
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Disable')
    })
  })
})

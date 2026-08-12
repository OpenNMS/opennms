import SnmpDataCollectionSourceDetail from '@/containers/SnmpDataCollectionSourceDetail.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpCollectionSource } from '@/types/snmpDataCollection'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

// Hoisted so the service mock below can resolve the on-mount fetch to the same
// source the tests assert against.
const { mockCollectionSource } = vi.hoisted(() => ({
  mockCollectionSource: {
    id: 1,
    name: 'Test Collection',
    vendor: 'Test Vendor',
    description: 'Test Description',
    enabled: true,
    uploadedBy: 'test-user',
    createdTime: new Date('2024-01-15'),
    lastModified: new Date('2024-02-20')
  } as SnmpCollectionSource
}))

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => ({ params: { id: '1' }})),
  useRouter: vi.fn(() => ({ push: mockPush }))
}))

const mockDeleteSnmpCollectionSources = vi.fn()
const mockEnableDisableSnmpDataCollectionSources = vi.fn()
vi.mock('@/services/snmpDataCollectionService', () => ({
  deleteSnmpCollectionSources: (...args: any[]) => mockDeleteSnmpCollectionSources(...args),
  enableDisableSnmpDataCollectionSources: (...args: any[]) => mockEnableDisableSnmpDataCollectionSources(...args),
  updateDataCollectionProfile: vi.fn().mockResolvedValue(true),
  // The detail store's fetchCollectionSourceById action runs on mount (before the
  // test overrides it), so the service functions it chains through must resolve cleanly.
  getSnmpDataCollectionSourceById: vi.fn().mockResolvedValue(mockCollectionSource),
  getSnmpDataCollectionResourceTypes: vi.fn().mockResolvedValue({ resourceTypes: [], totalRecords: 0 }),
  getSnmpDataCollectionMibGroups: vi.fn().mockResolvedValue({ mibGroups: [], totalRecords: 0 }),
  getSnmpDataCollectionSystemDefinitions: vi.fn().mockResolvedValue({ systemDefinitions: [], totalRecords: 0 }),
  getAllResourceTypeNames: vi.fn().mockResolvedValue([]),
  getAllMibGroupNames: vi.fn().mockResolvedValue([])
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: mockShowSnackBar, hideSnackbar: vi.fn() })
}))

const stubs = {
  TableCard: { name: 'TableCard', template: '<div class="table-card-stub"><slot></slot></div>' },
  MibGroupsTable: { name: 'MibGroupsTable', template: '<div class="mib-groups-table-stub"></div>' },
  ResourceTypesTable: { name: 'ResourceTypesTable', template: '<div class="resource-types-table-stub"></div>' },
  SystemDefinitionsTable: { name: 'SystemDefinitionsTable', template: '<div class="system-definitions-table-stub"></div>' },
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
  SnmpDataCollectionSourceProfilesDrawer: {
    name: 'SnmpDataCollectionSourceProfilesDrawer',
    template: '<div class="profiles-drawer-stub"></div>',
    props: ['visible', 'sourceName', 'profiles'],
    emits: ['close', 'saved']
  }
}

describe('SnmpDataCollectionSourceDetail.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>
  let sourcesStore: ReturnType<typeof useSnmpDataCollectionStore>

  const mountContainer = () => mount(SnmpDataCollectionSourceDetail, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs
    }
  })

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountContainer()
    store = useSnmpDataCollectionDetailStore()
    sourcesStore = useSnmpDataCollectionStore()
    store.fetchCollectionSourceById = vi.fn().mockResolvedValue(undefined)
    store.selectedCollectionSource = { ...mockCollectionSource }
    store.activeTab = 0
    sourcesStore.fetchSnmpCollectionProfiles = vi.fn().mockResolvedValue(undefined)
    sourcesStore.fetchAllSourcesNames = vi.fn().mockResolvedValue(undefined)
    sourcesStore.profilesForSource = vi.fn().mockReturnValue([])
    sourcesStore.sources = []
    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Rendering (edit mode)', () => {
    it('renders the title and a status tag', () => {
      expect(wrapper.text()).toContain('Source Details for Test Collection')
      const tag = wrapper.find('[data-test="status-tag"]')
      expect(tag.exists()).toBe(true)
      expect(tag.text()).toBe('Enabled')
    })

    it('renders the back button and the three source-detail tabs', () => {
      expect(wrapper.find('[data-test="back-button"]').exists()).toBe(true)
      expect(wrapper.text()).toContain('System Definitions')
      expect(wrapper.text()).toContain('MIB Groups')
      expect(wrapper.text()).toContain('Resource Types')
    })

    it('shows the disable + delete actions for an enabled source', () => {
      expect(wrapper.find('[data-test="disable-source"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="delete-source"]').exists()).toBe(true)
    })

    it('navigates back when the back button is clicked', async () => {
      await wrapper.get('[data-test="back-button"]').trigger('click')
      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection' })
    })
  })

  describe('Delete source', () => {
    it('passes selection to the delete dialog and deletes on confirm', async () => {
      wrapper.vm.openDeleteCollectionSourceDialog(store.selectedCollectionSource)
      await nextTick()
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.props('visible')).toBe(true)
      expect(dialog.props('type')).toBe('source')

      mockDeleteSnmpCollectionSources.mockResolvedValue(true)
      await dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()
      expect(mockDeleteSnmpCollectionSources).toHaveBeenCalledWith([1])
      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection' })
    })
  })

  describe('Change status', () => {
    it('passes the inverse status and toggles on confirm', async () => {
      wrapper.vm.openChangeStatusDialog(store.selectedCollectionSource)
      await nextTick()
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.props('status')).toBe('Disable')

      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      await dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()
      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(false, [1])
      expect(store.fetchCollectionSourceById).toHaveBeenCalled()
    })
  })

  describe('Create mode', () => {
    beforeEach(async () => {
      // switch to create mode
      wrapper.vm.mode = 1 // CreateEditMode.Create
      store.selectedCollectionSource = { ...mockCollectionSource, id: 0, name: '' }
      await nextTick()
    })

    it('shows the create button and source-name input', () => {
      expect(wrapper.find('[data-test="create-source-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="source-name-input"]').exists()).toBe(true)
    })

    it('flags a missing name and missing profiles on save', async () => {
      wrapper.vm.localSourceName = ''
      await wrapper.vm.onSaveSource()
      await flushPromises()
      expect(wrapper.vm.sourceNameError).toBeTruthy()
      expect(wrapper.vm.profilesError).toBeTruthy()
    })

    it('creates the source and navigates on success', async () => {
      sourcesStore.createSnmpDataCollectionSource = vi.fn().mockResolvedValue(42)
      wrapper.vm.localSourceName = 'New Source'
      wrapper.vm.createModeProfiles = [{ id: 1, name: 'p1', enabled: true, sourceNames: [], rrdStep: 300, rrdRras: [], storageFlag: 'select' }] as any
      await nextTick()
      await wrapper.vm.onSaveSource()
      await flushPromises()
      expect(sourcesStore.createSnmpDataCollectionSource).toHaveBeenCalledWith('New Source', ['p1'])
      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection Source Detail', params: { id: 42 }})
    })
  })
})

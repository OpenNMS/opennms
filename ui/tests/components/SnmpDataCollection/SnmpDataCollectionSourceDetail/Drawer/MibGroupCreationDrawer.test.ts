import MibGroupCreationDrawer from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/Drawer/MibGroupCreationDrawer.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const mockCreateMibGroup = vi.fn()
const mockUpdateMibGroup = vi.fn()
vi.mock('@/services/snmpDataCollectionService', () => ({
  createMibGroup: (...args: any[]) => mockCreateMibGroup(...args),
  updateMibGroup: (...args: any[]) => mockUpdateMibGroup(...args)
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: mockShowSnackBar, hideSnackbar: vi.fn() })
}))

// PrimeVue Drawer teleports to body; stub it so its slot renders inline.
const DrawerStub = {
  name: 'Drawer',
  template: '<div class="drawer-stub" v-if="visible"><slot></slot></div>',
  props: ['visible', 'header', 'position'],
  emits: ['update:visible', 'hide']
}

describe('MibGroupCreationDrawer.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>

  const mountDrawer = () => mount(MibGroupCreationDrawer, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs: { Drawer: DrawerStub }
    }
  })

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountDrawer()
    store = useSnmpDataCollectionDetailStore()
    store.resourceTypeNames = ['indexResource']
    store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
    store.selectedMibGroup = null
    store.fetchMibGroups = vi.fn().mockResolvedValue(undefined)
    store.closeMibGroupDrawer = vi.fn().mockResolvedValue(undefined)
    store.mibGroupDrawerState = { visible: true, isEditMode: CreateEditMode.Create }
    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Rendering', () => {
    it('renders the drawer content with Create title', () => {
      expect(wrapper.find('.drawer-stub').exists()).toBe(true)
      expect(wrapper.vm.drawerTitle).toBe('Create MIB Group')
      expect(wrapper.find('[data-test="save-mib-group"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="cancel-mib-group"]').exists()).toBe(true)
    })

    it('shows the Edit title in edit mode', async () => {
      store.mibGroupDrawerState = { visible: true, isEditMode: CreateEditMode.Edit }
      store.selectedMibGroup = {
        id: 5, name: 'edit-me', ifType: 'all', enabled: true,
        mibObjects: '[]', mibGroupNames: [], mibObjProperties: '[]',
        collectionSourceId: 1, collectionSourceName: 'Test Source'
      }
      await nextTick()
      expect(wrapper.vm.drawerTitle).toBe('Edit MIB Group')
    })
  })

  describe('Validation', () => {
    it('disables save until name is provided', async () => {
      expect(wrapper.vm.isSaveDisabled).toBe(true)
      wrapper.vm.name = 'mib2-interfaces'
      await nextTick()
      expect(wrapper.vm.isSaveDisabled).toBe(false)
    })
  })

  describe('MIB Object sub-form', () => {
    it('adds a valid MIB object to the list', async () => {
      wrapper.vm.openMibObjectDrawer(-1, null, CreateEditMode.Create)
      await nextTick()
      wrapper.vm.oid = '1.3.6.1.2.1'
      wrapper.vm.alias = 'ifIndex'
      await nextTick()
      wrapper.vm.saveMibObject()
      expect(wrapper.vm.mibObjects).toHaveLength(1)
      expect(wrapper.vm.mibObjects[0].alias).toBe('ifIndex')
      // sub-form closes after save
      expect(wrapper.vm.mibObjectDrawerState.visible).toBe(false)
    })

    it('does not add an invalid MIB object (bad OID)', async () => {
      wrapper.vm.openMibObjectDrawer(-1, null, CreateEditMode.Create)
      await nextTick()
      wrapper.vm.oid = 'not-an-oid'
      wrapper.vm.alias = 'x'
      wrapper.vm.saveMibObject()
      expect(wrapper.vm.mibObjects).toHaveLength(0)
    })

    it('edits an existing MIB object in place', async () => {
      wrapper.vm.mibObjects = [{ oid: '1.1', alias: 'a', instance: '0', type: 'gauge', maxval: null, minval: null }]
      await nextTick()
      wrapper.vm.openMibObjectDrawer(0, wrapper.vm.mibObjects[0], CreateEditMode.Edit)
      await nextTick()
      wrapper.vm.alias = 'updated-alias'
      wrapper.vm.saveMibObject()
      expect(wrapper.vm.mibObjects[0].alias).toBe('updated-alias')
    })

    it('deletes a MIB object by index', async () => {
      wrapper.vm.mibObjects = [
        { oid: '1.1', alias: 'a', instance: '0', type: 'gauge', maxval: null, minval: null },
        { oid: '1.2', alias: 'b', instance: '0', type: 'gauge', maxval: null, minval: null }
      ]
      await nextTick()
      wrapper.vm.deleteMibObject(0)
      expect(wrapper.vm.mibObjects).toHaveLength(1)
      expect(wrapper.vm.mibObjects[0].alias).toBe('b')
    })
  })

  describe('Save MIB Group', () => {
    it('creates the group and refetches on success', async () => {
      mockCreateMibGroup.mockResolvedValue(true)
      wrapper.vm.name = 'mib2-interfaces'
      await nextTick()
      await wrapper.vm.saveMibGroup()
      await flushPromises()
      expect(mockCreateMibGroup).toHaveBeenCalled()
      expect(store.fetchMibGroups).toHaveBeenCalled()
      expect(store.closeMibGroupDrawer).toHaveBeenCalled()
    })

    it('shows an error and does not refetch on failure', async () => {
      mockCreateMibGroup.mockResolvedValue(false)
      wrapper.vm.name = 'mib2-interfaces'
      await nextTick()
      await wrapper.vm.saveMibGroup()
      await flushPromises()
      expect(mockShowSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
      expect(store.fetchMibGroups).not.toHaveBeenCalled()
    })

    it('does not save when validation fails', async () => {
      wrapper.vm.name = ''
      await nextTick()
      await wrapper.vm.saveMibGroup()
      expect(mockCreateMibGroup).not.toHaveBeenCalled()
    })
  })

  describe('Cancel', () => {
    it('resets and closes via the store', async () => {
      await wrapper.get('[data-test="cancel-mib-group"]').trigger('click')
      await flushPromises()
      expect(store.closeMibGroupDrawer).toHaveBeenCalled()
    })
  })
})

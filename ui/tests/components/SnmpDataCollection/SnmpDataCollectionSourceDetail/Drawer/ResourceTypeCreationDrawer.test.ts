import ResourceTypeCreationDrawer from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/Drawer/ResourceTypeCreationDrawer.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const mockCreateResourceType = vi.fn()
const mockUpdateResourceType = vi.fn()
vi.mock('@/services/snmpDataCollectionService', () => ({
  createResourceType: (...args: any[]) => mockCreateResourceType(...args),
  updateResourceType: (...args: any[]) => mockUpdateResourceType(...args)
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: mockShowSnackBar, hideSnackbar: vi.fn() })
}))

const DrawerStub = {
  name: 'Drawer',
  template: '<div class="drawer-stub" v-if="visible"><slot></slot></div>',
  props: ['visible', 'header', 'position'],
  emits: ['update:visible', 'hide']
}

describe('ResourceTypeCreationDrawer.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>

  const mountDrawer = () => mount(ResourceTypeCreationDrawer, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs: { Drawer: DrawerStub }
    }
  })

  const fillValidResourceType = () => {
    wrapper.vm.name = 'cpu'
    wrapper.vm.label = 'CPU'
    wrapper.vm.storageStrategy = { _text: 'IndexStorage', _value: 'org.opennms.IndexStorage' }
    wrapper.vm.persistenceSelectorStrategy = { _text: 'PersistAll', _value: 'org.opennms.PersistAll' }
  }

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountDrawer()
    store = useSnmpDataCollectionDetailStore()
    store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
    store.selectedResourceType = null
    store.fetchResourceTypes = vi.fn().mockResolvedValue(undefined)
    store.closeResourceTypeDrawer = vi.fn().mockResolvedValue(undefined)
    store.resourceTypeDrawerState = { visible: true, isEditMode: CreateEditMode.Create }
    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Rendering', () => {
    it('renders the drawer content with Create title and buttons', () => {
      expect(wrapper.find('.drawer-stub').exists()).toBe(true)
      expect(wrapper.vm.drawerTitle).toBe('Create Resource Type')
      expect(wrapper.find('[data-test="save-resource-type"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="cancel-resource-type"]').exists()).toBe(true)
    })
  })

  describe('Validation', () => {
    it('disables save until required fields are set', async () => {
      expect(wrapper.vm.isSaveDisabled).toBe(true)
      fillValidResourceType()
      await nextTick()
      expect(wrapper.vm.isSaveDisabled).toBe(false)
    })
  })

  describe('Strategy autocomplete search', () => {
    it('filters storage strategy options and offers a custom value', () => {
      wrapper.vm.onSearchStorageStrategy({ query: 'zzz-custom' })
      expect(wrapper.vm.storageStrategyResults).toEqual([{ _text: 'zzz-custom', _value: 'zzz-custom' }])
    })
  })

  describe('Parameter sub-form', () => {
    it('adds a storage strategy parameter', async () => {
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Create)
      await nextTick()
      wrapper.vm.key = 'sysName'
      wrapper.vm.value = 'value1'
      wrapper.vm.saveResourceTypeParameter()
      expect(wrapper.vm.storageStrategyParams).toEqual([{ key: 'sysName', value: 'value1' }])
    })

    it('adds a persistence selector strategy parameter', async () => {
      wrapper.vm.openPersistenceSelectorStrategyDrawer(CreateEditMode.Create)
      await nextTick()
      wrapper.vm.key = 'match'
      wrapper.vm.value = 'any'
      wrapper.vm.saveResourceTypeParameter()
      expect(wrapper.vm.persistenceSelectorStrategyParams).toEqual([{ key: 'match', value: 'any' }])
    })

    it('edits a storage strategy parameter in place', async () => {
      wrapper.vm.storageStrategyParams = [{ key: 'k', value: 'v' }]
      await nextTick()
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Edit, 0, { key: 'k', value: 'v' })
      await nextTick()
      wrapper.vm.value = 'updated'
      wrapper.vm.saveResourceTypeParameter()
      expect(wrapper.vm.storageStrategyParams[0]).toEqual({ key: 'k', value: 'updated' })
    })

    it('deletes a storage strategy parameter by index', async () => {
      wrapper.vm.storageStrategyParams = [{ key: 'a', value: '1' }, { key: 'b', value: '2' }]
      await nextTick()
      wrapper.vm.deleteStorageStrategy(0)
      expect(wrapper.vm.storageStrategyParams).toEqual([{ key: 'b', value: '2' }])
    })
  })

  describe('Save', () => {
    it.each([
      { mode: CreateEditMode.Create, msg: 'created' },
      { mode: CreateEditMode.Edit, msg: 'updated' }
    ])('saves in $msg mode and refetches on success', async ({ mode, msg }) => {
      mockCreateResourceType.mockResolvedValue(true)
      mockUpdateResourceType.mockResolvedValue(true)
      store.resourceTypeDrawerState = { visible: true, isEditMode: mode }
      await nextTick()
      fillValidResourceType()
      await nextTick()
      await wrapper.vm.saveResourceType()
      await flushPromises()
      if (mode === CreateEditMode.Create) {
        expect(mockCreateResourceType).toHaveBeenCalled()
      } else {
        expect(mockUpdateResourceType).toHaveBeenCalled()
      }
      expect(store.fetchResourceTypes).toHaveBeenCalled()
      expect(store.closeResourceTypeDrawer).toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith(expect.objectContaining({ msg: expect.stringContaining(msg) }))
    })

    it('does not save when validation fails', async () => {
      await wrapper.vm.saveResourceType()
      expect(mockCreateResourceType).not.toHaveBeenCalled()
    })
  })

  describe('Cancel', () => {
    it('closes via the store', async () => {
      await wrapper.get('[data-test="cancel-resource-type"]').trigger('click')
      await flushPromises()
      expect(store.closeResourceTypeDrawer).toHaveBeenCalled()
    })
  })
})

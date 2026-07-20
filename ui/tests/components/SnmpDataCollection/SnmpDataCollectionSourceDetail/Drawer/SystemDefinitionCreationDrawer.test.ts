import SystemDefinitionCreationDrawer from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/Drawer/SystemDefinitionCreationDrawer.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const mockCreateSystemDefinition = vi.fn()
const mockUpdateSystemDefinition = vi.fn()
vi.mock('@/services/snmpDataCollectionService', () => ({
  createSystemDefinition: (...args: any[]) => mockCreateSystemDefinition(...args),
  updateSystemDefinition: (...args: any[]) => mockUpdateSystemDefinition(...args)
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

describe('SystemDefinitionCreationDrawer.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>

  const mountDrawer = () => mount(SystemDefinitionCreationDrawer, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs: { Drawer: DrawerStub }
    }
  })

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountDrawer()
    store = useSnmpDataCollectionDetailStore()
    store.selectedCollectionSource = { id: 1, name: 'Test Source' } as any
    store.selectedSystemDef = null
    store.mibGroupNames = ['mib2-interfaces', 'cisco-cpu']
    store.fetchSystemDefinitions = vi.fn().mockResolvedValue(undefined)
    store.closeSystemDefDrawer = vi.fn().mockResolvedValue(undefined)
    store.systemDefDrawerState = { visible: true, isEditMode: CreateEditMode.Create }
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
      expect(wrapper.vm.drawerTitle).toBe('Create System Definition')
      expect(wrapper.find('[data-test="save-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="cancel-button"]').exists()).toBe(true)
    })

    it('renders an OID Type radio per option', () => {
      const radios = wrapper.findAll('input[type="radio"]')
      expect(radios.length).toBeGreaterThanOrEqual(2)
    })
  })

  describe('Validation', () => {
    it('disables save until required fields are valid', async () => {
      expect(wrapper.vm.isSaveDisabled).toBe(true)
      wrapper.vm.name = 'Cisco Routers'
      wrapper.vm.oidValue = '.1.3.6.1.4.1.9'
      wrapper.vm.mibGroupNames = [{ _text: 'mib2-interfaces', _value: 'mib2-interfaces' }]
      await nextTick()
      expect(wrapper.vm.isSaveDisabled).toBe(false)
    })

    it('flags an invalid OID value', async () => {
      wrapper.vm.name = 'x'
      wrapper.vm.oidValue = 'not-an-oid'
      wrapper.vm.mibGroupNames = [{ _text: 'm', _value: 'm' }]
      await nextTick()
      expect(wrapper.vm.errors.oidValue).toBeTruthy()
    })
  })

  describe('MIB groups search', () => {
    it('filters the available MIB group names', () => {
      wrapper.vm.search({ query: 'cisco' })
      expect(wrapper.vm.results).toEqual([{ _text: 'cisco-cpu', _value: 'cisco-cpu' }])
    })
  })

  describe('Save', () => {
    it('creates the definition and refetches on success', async () => {
      mockCreateSystemDefinition.mockResolvedValue(true)
      wrapper.vm.name = 'Cisco Routers'
      wrapper.vm.oidValue = '.1.3.6.1.4.1.9'
      wrapper.vm.mibGroupNames = [{ _text: 'mib2-interfaces', _value: 'mib2-interfaces' }]
      await nextTick()
      await wrapper.vm.saveSystemDef()
      await flushPromises()
      expect(mockCreateSystemDefinition).toHaveBeenCalled()
      expect(store.fetchSystemDefinitions).toHaveBeenCalled()
      expect(store.closeSystemDefDrawer).toHaveBeenCalled()
    })

    it('does not save when validation fails', async () => {
      await wrapper.vm.saveSystemDef()
      expect(mockCreateSystemDefinition).not.toHaveBeenCalled()
    })
  })

  describe('Cancel', () => {
    it('closes via the store', async () => {
      await wrapper.get('[data-test="cancel-button"]').trigger('click')
      await flushPromises()
      expect(store.closeSystemDefDrawer).toHaveBeenCalled()
    })
  })
})

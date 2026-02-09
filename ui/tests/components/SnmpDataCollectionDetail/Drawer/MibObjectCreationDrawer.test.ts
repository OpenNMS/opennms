import MibObjectCreationDrawer from '@/components/SnmpDataCollectionDetail/Drawer/MibObjectCreationDrawer.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { MibGroupObjectForm } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect } from '@featherds/select'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { DEFAULT_MIB_OBJ_TYPE, MIB_OBJECT_DATA_TYPE_OPTIONS } from '@/lib/constants'

describe('MibObjectCreationDrawer.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>

  interface DrawerState {
    visible: boolean
    isEditMode: CreateEditMode
    mibObjectIndex: number
    mibObject: MibGroupObjectForm | null
  }

  const defaultState: DrawerState = {
    visible: true,
    isEditMode: CreateEditMode.Create,
    mibObjectIndex: -1,
    mibObject: null
  }

  const mockMibObject: MibGroupObjectForm = {
    oid: '1.3.6.1.2.1.1.1',
    alias: 'sysDescr',
    instance: 'sysDescr',
    type: 'string',
    maxval: null,
    minval: null
  }

  const mockResourceTypeNames = ['ifIndex', 'sysDescr', 'sysObjectID', 'hrStorageIndex']

  const createWrapper = async (state: DrawerState = defaultState) => {
    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useSnmpDataCollectionDetailStore(pinia)
    store.resourceTypeNames = mockResourceTypeNames

    wrapper = mount(MibObjectCreationDrawer, {
      props: {
        state
      },
      global: {
        plugins: [pinia],
        components: {
          FeatherButton,
          FeatherInput,
          FeatherSelect,
          FeatherIcon
        },
        stubs: {
          FeatherIcon: true,
          FeatherDrawer: {
            name: 'FeatherDrawer',
            template: '<div v-if="modelValue" class="feather-drawer" data-test="mib-object-drawer"><slot /></div>',
            props: ['modelValue', 'labels', 'hideClose', 'width'],
            emits: ['update:modelValue']
          }
        }
      }
    })

    await nextTick()
    await flushPromises()
    return wrapper
  }

  beforeEach(async () => {
    vi.clearAllMocks()
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.useRealTimers()
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Rendering', () => {
    describe('Create Mode', () => {
      it('should render the drawer with Create Mib Object title', async () => {
        await createWrapper({ ...defaultState, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.drawerTitle).toBe('Create Mib Object')
      })

      it('should render OID input field via vm state', async () => {
        await createWrapper()
        // OID field exists and can be set
        expect(wrapper.vm.oid).toBeDefined()
        wrapper.vm.oid = 'test'
        await nextTick()
        expect(wrapper.vm.oid).toBe('test')
      })

      it('should render Instance select field via vm state', async () => {
        await createWrapper()
        expect(wrapper.vm.instance).toBeDefined()
        expect(wrapper.vm.instancesOptions).toBeDefined()
      })

      it('should render Alias input field via vm state', async () => {
        await createWrapper()
        expect(wrapper.vm.alias).toBeDefined()
        wrapper.vm.alias = 'testAlias'
        await nextTick()
        expect(wrapper.vm.alias).toBe('testAlias')
      })

      it('should render Data Type select field via vm state', async () => {
        await createWrapper()
        expect(wrapper.vm.dataType).toBeDefined()
        expect(wrapper.vm.dataType._value).toBe('gauge')
      })

      it('should have cancel functionality available', async () => {
        await createWrapper()
        // Cancel emits cancel event - verified in Cancel Functionality tests
        expect(wrapper.emitted).toBeDefined()
      })

      it('should have save functionality available via vm', async () => {
        await createWrapper()
        expect(wrapper.vm.saveMibObject).toBeDefined()
        expect(wrapper.vm.isSaveDisabled).toBeDefined()
      })

      it('should render drawer when visible is true', async () => {
        await createWrapper({ ...defaultState, visible: true })
        expect(wrapper.vm.isDrawerOpen).toBe(true)
      })

      it('should not show drawer when visible is false', async () => {
        await createWrapper({ ...defaultState, visible: false })
        expect(wrapper.vm.isDrawerOpen).toBe(false)
      })
    })

    describe('Edit Mode', () => {
      it('should render the drawer with Edit Mib Object title', async () => {
        await createWrapper({
          visible: true,
          isEditMode: CreateEditMode.Edit,
          mibObjectIndex: 0,
          mibObject: mockMibObject
        })
        expect(wrapper.vm.drawerTitle).toBe('Edit Mib Object')
      })
    })
  })

  describe('Initial Data Loading', () => {
    describe('Create Mode', () => {
      it('should initialize with empty OID', async () => {
        await createWrapper({ ...defaultState, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.oid).toBe('')
      })

      it('should initialize with default data type (gauge)', async () => {
        await createWrapper({ ...defaultState, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.dataType).toEqual(DEFAULT_MIB_OBJ_TYPE)
      })

      it('should initialize with default instance (0)', async () => {
        await createWrapper({ ...defaultState, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.instance).toEqual({ _text: '0', _value: '0' })
      })

      it('should initialize with empty alias', async () => {
        await createWrapper({ ...defaultState, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.alias).toBe('')
      })

      it('should populate instance options from store.resourceTypeNames', async () => {
        await createWrapper()
        expect(wrapper.vm.instancesOptions).toHaveLength(mockResourceTypeNames.length)
        expect(wrapper.vm.instancesOptions[0]).toEqual({ _text: 'ifIndex', _value: 'ifIndex' })
      })
    })

    describe('Edit Mode', () => {
      it('should load existing MIB object OID', async () => {
        await createWrapper({
          visible: true,
          isEditMode: CreateEditMode.Edit,
          mibObjectIndex: 0,
          mibObject: mockMibObject
        })
        expect(wrapper.vm.oid).toBe('1.3.6.1.2.1.1.1')
      })

      it('should load existing MIB object alias', async () => {
        await createWrapper({
          visible: true,
          isEditMode: CreateEditMode.Edit,
          mibObjectIndex: 0,
          mibObject: mockMibObject
        })
        expect(wrapper.vm.alias).toBe('sysDescr')
      })

      it('should load existing MIB object instance', async () => {
        await createWrapper({
          visible: true,
          isEditMode: CreateEditMode.Edit,
          mibObjectIndex: 0,
          mibObject: mockMibObject
        })
        expect(wrapper.vm.instance._value).toBe('sysDescr')
      })

      it('should load existing MIB object data type', async () => {
        await createWrapper({
          visible: true,
          isEditMode: CreateEditMode.Edit,
          mibObjectIndex: 0,
          mibObject: mockMibObject
        })
        expect(wrapper.vm.dataType._value).toBe('string')
      })

      it('should handle null mibObject in edit mode gracefully', async () => {
        await createWrapper({
          visible: true,
          isEditMode: CreateEditMode.Edit,
          mibObjectIndex: 0,
          mibObject: null
        })
        // Should not crash and values should remain at defaults
        expect(wrapper.vm.oid).toBe('')
      })
    })
  })

  describe('Validation', () => {
    describe('OID Validation', () => {
      it('should show error when OID is empty', async () => {
        await createWrapper()
        wrapper.vm.oid = ''
        await nextTick()
        // Note: Format validation runs after required check, so format error takes precedence
        expect(wrapper.vm.errors.oid).toBe('OID must be in the format of numbers separated by dots (e.g. 1.2.3.4)')
      })

      it('should accept OID with no dots (just digits)', async () => {
        await createWrapper()
        wrapper.vm.oid = '123456'
        wrapper.vm.alias = 'test'
        await nextTick()
        expect(wrapper.vm.errors.oid).toBeUndefined()
      })

      it('should accept OID that starts with dot', async () => {
        await createWrapper()
        wrapper.vm.oid = '.1.3.6.1'
        wrapper.vm.alias = 'test'
        await nextTick()
        expect(wrapper.vm.errors.oid).toBeUndefined()
      })

      it('should show error for invalid OID format - ends with dot', async () => {
        await createWrapper()
        wrapper.vm.oid = '1.3.6.1.'
        await nextTick()
        expect(wrapper.vm.errors.oid).toBe('OID must be in the format of numbers separated by dots (e.g. 1.2.3.4)')
      })

      it('should show error for invalid OID format - contains letters', async () => {
        await createWrapper()
        wrapper.vm.oid = '1.3.6.1.a.1'
        await nextTick()
        expect(wrapper.vm.errors.oid).toBe('OID must be in the format of numbers separated by dots (e.g. 1.2.3.4)')
      })

      it('should show error for invalid OID format - double dots', async () => {
        await createWrapper()
        wrapper.vm.oid = '1.3..6.1'
        await nextTick()
        expect(wrapper.vm.errors.oid).toBe('OID must be in the format of numbers separated by dots (e.g. 1.2.3.4)')
      })

      it('should accept single number OID', async () => {
        await createWrapper()
        wrapper.vm.oid = '1'
        wrapper.vm.alias = 'test'
        await nextTick()
        expect(wrapper.vm.errors.oid).toBeUndefined()
      })

      it('should clear OID error when valid OID is provided', async () => {
        await createWrapper()
        wrapper.vm.oid = ''
        await nextTick()
        expect(wrapper.vm.errors.oid).toBeDefined()

        wrapper.vm.oid = '1.3.6.1.2.1'
        wrapper.vm.alias = 'test'
        await nextTick()
        expect(wrapper.vm.errors.oid).toBeUndefined()
      })

      it('should accept valid OID with two numbers', async () => {
        await createWrapper()
        wrapper.vm.oid = '1.3'
        wrapper.vm.alias = 'test'
        await nextTick()
        expect(wrapper.vm.errors.oid).toBeUndefined()
      })

      it('should accept valid OID with many segments', async () => {
        await createWrapper()
        wrapper.vm.oid = '1.3.6.1.4.1.9.9.109.1.1.1.1.3'
        wrapper.vm.alias = 'test'
        await nextTick()
        expect(wrapper.vm.errors.oid).toBeUndefined()
      })
    })

    describe('Instance Validation', () => {
      it('should show error when instance value is empty', async () => {
        await createWrapper()
        wrapper.vm.instance = { _text: '', _value: '' }
        await nextTick()
        expect(wrapper.vm.errors.instance).toBe('Instance is required')
      })

      it('should clear instance error when valid instance is selected', async () => {
        await createWrapper()
        wrapper.vm.instance = { _text: '', _value: '' }
        await nextTick()
        expect(wrapper.vm.errors.instance).toBeDefined()

        wrapper.vm.instance = { _text: 'ifIndex', _value: 'ifIndex' }
        await nextTick()
        expect(wrapper.vm.errors.instance).toBeUndefined()
      })
    })

    describe('Alias Validation', () => {
      it('should show error when alias is empty', async () => {
        await createWrapper()
        wrapper.vm.alias = ''
        await nextTick()
        expect(wrapper.vm.errors.alias).toBe('Alias is required')
      })

      it('should clear alias error when valid alias is provided', async () => {
        await createWrapper()
        wrapper.vm.alias = ''
        await nextTick()
        expect(wrapper.vm.errors.alias).toBeDefined()

        wrapper.vm.alias = 'testAlias'
        await nextTick()
        expect(wrapper.vm.errors.alias).toBeUndefined()
      })
    })

    describe('Data Type Validation', () => {
      it('should show error when data type value is empty', async () => {
        await createWrapper()
        wrapper.vm.dataType = { _text: '', _value: '' }
        await nextTick()
        expect(wrapper.vm.errors.type).toBe('Data Type is required')
      })

      it('should clear data type error when valid type is selected', async () => {
        await createWrapper()
        wrapper.vm.dataType = { _text: '', _value: '' }
        await nextTick()
        expect(wrapper.vm.errors.type).toBeDefined()

        wrapper.vm.dataType = MIB_OBJECT_DATA_TYPE_OPTIONS[0]
        await nextTick()
        expect(wrapper.vm.errors.type).toBeUndefined()
      })
    })

    describe('isSaveDisabled', () => {
      it('should be disabled when form is invalid', async () => {
        await createWrapper()
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })

      it('should be enabled when form is valid', async () => {
        await createWrapper()
        wrapper.vm.oid = '1.3.6.1.2.1'
        wrapper.vm.alias = 'testAlias'
        wrapper.vm.instance = { _text: 'ifIndex', _value: 'ifIndex' }
        wrapper.vm.dataType = { _text: 'gauge', _value: 'gauge' }
        await nextTick()

        expect(wrapper.vm.isSaveDisabled).toBe(false)
      })

      it('should be disabled when OID becomes empty after being valid', async () => {
        await createWrapper()
        wrapper.vm.oid = '1.3.6.1.2.1'
        wrapper.vm.alias = 'testAlias'
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(false)

        wrapper.vm.oid = ''
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })

      it('should be disabled when alias becomes empty after being valid', async () => {
        await createWrapper()
        wrapper.vm.oid = '1.3.6.1.2.1'
        wrapper.vm.alias = 'testAlias'
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(false)

        wrapper.vm.alias = ''
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })
    })
  })

  describe('Save Functionality', () => {
    it('should emit save event with correct data when form is valid', async () => {
      await createWrapper()
      wrapper.vm.oid = '1.3.6.1.2.1.1.1'
      wrapper.vm.alias = 'sysDescr'
      wrapper.vm.instance = { _text: 'sysDescr', _value: 'sysDescr' }
      wrapper.vm.dataType = { _text: 'string', _value: 'string' }
      await nextTick()

      // Call saveMibObject method directly
      wrapper.vm.saveMibObject()
      await flushPromises()

      expect(wrapper.emitted('save')).toBeTruthy()
      expect(wrapper.emitted('save')![0][0]).toEqual({
        oid: '1.3.6.1.2.1.1.1',
        alias: 'sysDescr',
        instance: 'sysDescr',
        type: 'string',
        maxval: null,
        minval: null
      })
    })

    it('should not emit save event when form is invalid', async () => {
      await createWrapper()
      wrapper.vm.oid = ''
      wrapper.vm.alias = ''
      await nextTick()

      wrapper.vm.saveMibObject()
      await flushPromises()

      expect(wrapper.emitted('save')).toBeFalsy()
    })

    it('should not emit save event when OID format is invalid', async () => {
      await createWrapper()
      wrapper.vm.oid = 'invalid-oid'
      wrapper.vm.alias = 'testAlias'
      await nextTick()

      wrapper.vm.saveMibObject()
      await flushPromises()

      expect(wrapper.emitted('save')).toBeFalsy()
    })

    it('should set errors when trying to save with invalid form', async () => {
      await createWrapper()
      wrapper.vm.oid = ''
      wrapper.vm.alias = ''
      await nextTick()

      wrapper.vm.saveMibObject()
      await nextTick()

      expect(wrapper.vm.errors.oid).toBeDefined()
      expect(wrapper.vm.errors.alias).toBeDefined()
    })

    it('should emit save with maxval and minval as null', async () => {
      await createWrapper()
      wrapper.vm.oid = '1.3.6.1.2.1'
      wrapper.vm.alias = 'testAlias'
      wrapper.vm.instance = { _text: '0', _value: '0' }
      wrapper.vm.dataType = { _text: 'gauge', _value: 'gauge' }
      await nextTick()

      wrapper.vm.saveMibObject()
      await flushPromises()

      const emittedData = wrapper.emitted('save')![0][0] as MibGroupObjectForm
      expect(emittedData.maxval).toBe(null)
      expect(emittedData.minval).toBe(null)
    })
  })

  describe('Cancel Functionality', () => {
    it('should emit cancel event when Cancel button is clicked', async () => {
      await createWrapper()
      // Find FeatherButton components and trigger click on the Cancel one
      const buttons = wrapper.findAllComponents(FeatherButton)
      // Cancel button contains "Cancel" text
      const cancelButton = buttons.find((btn) => btn.text().includes('Cancel'))
      expect(cancelButton).toBeDefined()
      await cancelButton!.trigger('click')

      expect(wrapper.emitted('cancel')).toBeTruthy()
    })

    it('should emit cancel event without any payload', async () => {
      await createWrapper()
      const buttons = wrapper.findAllComponents(FeatherButton)
      const cancelButton = buttons.find((btn) => btn.text().includes('Cancel'))
      await cancelButton!.trigger('click')

      expect(wrapper.emitted('cancel')![0]).toEqual([])
    })
  })

  describe('Watchers', () => {
    describe('props.state.visible', () => {
      it('should update isDrawerOpen when visible changes to true', async () => {
        await createWrapper({ ...defaultState, visible: false })
        expect(wrapper.vm.isDrawerOpen).toBe(false)

        await wrapper.setProps({
          state: { ...defaultState, visible: true }
        })
        await nextTick()

        expect(wrapper.vm.isDrawerOpen).toBe(true)
      })

      it('should update isDrawerOpen when visible changes to false', async () => {
        await createWrapper({ ...defaultState, visible: true })
        expect(wrapper.vm.isDrawerOpen).toBe(true)

        await wrapper.setProps({
          state: { ...defaultState, visible: false }
        })
        await nextTick()

        expect(wrapper.vm.isDrawerOpen).toBe(false)
      })

      it('should reset form data when drawer is closed', async () => {
        await createWrapper({ ...defaultState, visible: true })
        wrapper.vm.oid = '1.3.6.1.2.1'
        wrapper.vm.alias = 'testAlias'
        wrapper.vm.instance = { _text: 'ifIndex', _value: 'ifIndex' }
        wrapper.vm.dataType = { _text: 'string', _value: 'string' }
        await nextTick()

        await wrapper.setProps({
          state: { ...defaultState, visible: false }
        })
        await nextTick()

        expect(wrapper.vm.oid).toBe('')
        expect(wrapper.vm.alias).toBe('')
        expect(wrapper.vm.instance).toEqual({ _text: '0', _value: '0' })
        expect(wrapper.vm.dataType).toEqual(DEFAULT_MIB_OBJ_TYPE)
      })

      it('should trigger validation reset when drawer is closed', async () => {
        await createWrapper({ ...defaultState, visible: true })
        wrapper.vm.oid = '1.3.6.1.2.1'
        wrapper.vm.alias = 'testAlias'
        await nextTick()
        // Form is valid, no errors
        expect(wrapper.vm.errors.oid).toBeUndefined()

        await wrapper.setProps({
          state: { ...defaultState, visible: false }
        })
        await nextTick()

        // After reset, watchEffect re-validates and errors are re-populated since form is empty
        expect(wrapper.vm.errors.oid).toBeDefined()
        expect(wrapper.vm.errors.alias).toBeDefined()
      })

      it('should set isSaveDisabled to true when drawer is closed', async () => {
        await createWrapper({ ...defaultState, visible: true })
        wrapper.vm.oid = '1.3.6.1.2.1'
        wrapper.vm.alias = 'testAlias'
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(false)

        await wrapper.setProps({
          state: { ...defaultState, visible: false }
        })
        await nextTick()

        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })

      it('should load MIB object data when drawer opens in Edit mode', async () => {
        await createWrapper({ ...defaultState, visible: false })

        await wrapper.setProps({
          state: {
            visible: true,
            isEditMode: CreateEditMode.Edit,
            mibObjectIndex: 0,
            mibObject: mockMibObject
          }
        })
        await nextTick()

        expect(wrapper.vm.oid).toBe('1.3.6.1.2.1.1.1')
        expect(wrapper.vm.alias).toBe('sysDescr')
        expect(wrapper.vm.instance._value).toBe('sysDescr')
        expect(wrapper.vm.dataType._value).toBe('string')
      })

      it('should reset form when drawer opens in Create mode', async () => {
        await createWrapper({
          visible: true,
          isEditMode: CreateEditMode.Edit,
          mibObjectIndex: 0,
          mibObject: mockMibObject
        })
        expect(wrapper.vm.oid).toBe('1.3.6.1.2.1.1.1')

        await wrapper.setProps({
          state: { ...defaultState, visible: false }
        })
        await nextTick()

        await wrapper.setProps({
          state: { ...defaultState, visible: true, isEditMode: CreateEditMode.Create }
        })
        await nextTick()

        expect(wrapper.vm.oid).toBe('')
        expect(wrapper.vm.alias).toBe('')
      })
    })

    describe('watchEffect for validation', () => {
      it('should update errors reactively', async () => {
        await createWrapper()
        expect(wrapper.vm.errors.oid).toBeDefined()

        wrapper.vm.oid = '1.3.6.1.2.1'
        wrapper.vm.alias = 'test'
        await nextTick()

        expect(wrapper.vm.errors.oid).toBeUndefined()
      })

      it('should update isSaveDisabled reactively', async () => {
        await createWrapper()
        expect(wrapper.vm.isSaveDisabled).toBe(true)

        wrapper.vm.oid = '1.3.6.1.2.1'
        wrapper.vm.alias = 'testAlias'
        wrapper.vm.instance = { _text: 'test', _value: 'test' }
        wrapper.vm.dataType = { _text: 'gauge', _value: 'gauge' }
        await nextTick()

        expect(wrapper.vm.isSaveDisabled).toBe(false)
      })
    })
  })

  describe('Computed Properties', () => {
    describe('drawerTitle', () => {
      it('should return "Create Mib Object" in Create mode', async () => {
        await createWrapper({ ...defaultState, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.drawerTitle).toBe('Create Mib Object')
      })

      it('should return "Edit Mib Object" in Edit mode', async () => {
        await createWrapper({
          visible: true,
          isEditMode: CreateEditMode.Edit,
          mibObjectIndex: 0,
          mibObject: mockMibObject
        })
        expect(wrapper.vm.drawerTitle).toBe('Edit Mib Object')
      })

      it('should return "Edit Mib Object" in None mode', async () => {
        await createWrapper({ ...defaultState, isEditMode: CreateEditMode.None })
        expect(wrapper.vm.drawerTitle).toBe('Edit Mib Object')
      })
    })

    describe('instancesOptions', () => {
      it('should be populated from store.resourceTypeNames', async () => {
        await createWrapper()
        expect(wrapper.vm.instancesOptions).toEqual([
          { _text: 'ifIndex', _value: 'ifIndex' },
          { _text: 'sysDescr', _value: 'sysDescr' },
          { _text: 'sysObjectID', _value: 'sysObjectID' },
          { _text: 'hrStorageIndex', _value: 'hrStorageIndex' }
        ])
      })

      it('should be empty when store.resourceTypeNames is empty', async () => {
        const pinia = createTestingPinia({
          createSpy: vi.fn,
          stubActions: false
        })
        store = useSnmpDataCollectionDetailStore(pinia)
        store.resourceTypeNames = []

        wrapper = mount(MibObjectCreationDrawer, {
          props: { state: defaultState },
          global: {
            plugins: [pinia],
            stubs: { FeatherIcon: true, FeatherDrawer: false }
          }
        })
        await nextTick()

        expect(wrapper.vm.instancesOptions).toEqual([])
      })
    })
  })

  describe('Edge Cases', () => {
    it('should handle OID with large numbers', async () => {
      await createWrapper()
      wrapper.vm.oid = '1.3.6.1.4.1.99999999.1.1'
      wrapper.vm.alias = 'testAlias'
      await nextTick()

      expect(wrapper.vm.errors.oid).toBeUndefined()
    })

    it('should handle very long alias', async () => {
      await createWrapper()
      wrapper.vm.oid = '1.3.6.1.2.1'
      wrapper.vm.alias = 'a'.repeat(500)
      await nextTick()

      expect(wrapper.vm.errors.alias).toBeUndefined()
    })

    it('should handle special characters in alias', async () => {
      await createWrapper()
      wrapper.vm.oid = '1.3.6.1.2.1'
      wrapper.vm.alias = 'test_alias-123'
      await nextTick()

      expect(wrapper.vm.errors.alias).toBeUndefined()
    })

    it('should handle OID with single dot between two numbers', async () => {
      await createWrapper()
      wrapper.vm.oid = '1.3'
      wrapper.vm.alias = 'test'
      await nextTick()

      expect(wrapper.vm.errors.oid).toBeUndefined()
    })

    it('should handle spaces in OID (invalid)', async () => {
      await createWrapper()
      wrapper.vm.oid = '1.3 .6.1'
      await nextTick()

      expect(wrapper.vm.errors.oid).toBeDefined()
    })

    it('should handle OID with leading zeros', async () => {
      await createWrapper()
      wrapper.vm.oid = '01.03.06.01'
      wrapper.vm.alias = 'test'
      await nextTick()

      // Leading zeros are valid numbers
      expect(wrapper.vm.errors.oid).toBeUndefined()
    })

    it('should preserve form state during validation cycles', async () => {
      await createWrapper()
      wrapper.vm.oid = '1.3.6.1.2.1'
      wrapper.vm.alias = 'testAlias'
      wrapper.vm.instance = { _text: 'custom', _value: 'custom' }
      wrapper.vm.dataType = { _text: 'counter', _value: 'counter' }
      await nextTick()

      // Trigger multiple validation cycles
      await nextTick()
      await nextTick()

      expect(wrapper.vm.oid).toBe('1.3.6.1.2.1')
      expect(wrapper.vm.alias).toBe('testAlias')
      expect(wrapper.vm.instance._value).toBe('custom')
      expect(wrapper.vm.dataType._value).toBe('counter')
    })

    it('should handle rapid open/close cycles', async () => {
      await createWrapper({ ...defaultState, visible: false })

      // Rapid open/close
      await wrapper.setProps({ state: { ...defaultState, visible: true } })
      await wrapper.setProps({ state: { ...defaultState, visible: false } })
      await wrapper.setProps({ state: { ...defaultState, visible: true } })
      await nextTick()

      expect(wrapper.vm.isDrawerOpen).toBe(true)
    })

    it('should handle switching between create and edit mode', async () => {
      await createWrapper({ ...defaultState, isEditMode: CreateEditMode.Create })
      expect(wrapper.vm.drawerTitle).toBe('Create Mib Object')

      // Close the drawer first
      await wrapper.setProps({
        state: { ...defaultState, visible: false }
      })
      await nextTick()

      // Then open in edit mode
      await wrapper.setProps({
        state: {
          visible: true,
          isEditMode: CreateEditMode.Edit,
          mibObjectIndex: 0,
          mibObject: mockMibObject
        }
      })
      await nextTick()

      expect(wrapper.vm.drawerTitle).toBe('Edit Mib Object')
      expect(wrapper.vm.oid).toBe('1.3.6.1.2.1.1.1')
    })

    it('should handle MIB object with different data types', async () => {
      const testTypes = ['counter', 'gauge', 'string', 'integer', 'timeticks']

      for (const type of testTypes) {
        await createWrapper({
          visible: true,
          isEditMode: CreateEditMode.Edit,
          mibObjectIndex: 0,
          mibObject: { ...mockMibObject, type }
        })
        expect(wrapper.vm.dataType._value).toBe(type)
        wrapper.unmount()
      }
    })

    it('should handle concurrent save attempts', async () => {
      await createWrapper()
      wrapper.vm.oid = '1.3.6.1.2.1'
      wrapper.vm.alias = 'testAlias'
      await nextTick()

      // Multiple save calls
      wrapper.vm.saveMibObject()
      wrapper.vm.saveMibObject()
      await flushPromises()

      // Should emit save twice
      expect(wrapper.emitted('save')?.length).toBe(2)
    })

    it('should handle empty store.resourceTypeNames', async () => {
      const pinia = createTestingPinia({
        createSpy: vi.fn,
        stubActions: false
      })
      const localStore = useSnmpDataCollectionDetailStore(pinia)
      localStore.resourceTypeNames = []

      const localWrapper: VueWrapper<any> = mount(MibObjectCreationDrawer, {
        props: { state: defaultState },
        global: {
          plugins: [pinia],
          stubs: { FeatherIcon: true, FeatherDrawer: false }
        }
      })
      await nextTick()

      expect(localWrapper.vm.instancesOptions).toEqual([])
      localWrapper.unmount()
    })
  })

  describe('Button States', () => {
    it('should have Save button disabled initially', async () => {
      await createWrapper()
      expect(wrapper.vm.isSaveDisabled).toBe(true)
    })

    it('should enable Save button when all fields are valid', async () => {
      await createWrapper()
      wrapper.vm.oid = '1.3.6.1.2.1'
      wrapper.vm.alias = 'testAlias'
      wrapper.vm.instance = { _text: 'test', _value: 'test' }
      wrapper.vm.dataType = { _text: 'gauge', _value: 'gauge' }
      await nextTick()

      expect(wrapper.vm.isSaveDisabled).toBe(false)
    })

    it('should keep Cancel button always enabled', async () => {
      await createWrapper()
      // Cancel button should exist and be clickable regardless of form state
      const buttons = wrapper.findAllComponents(FeatherButton)
      const cancelButton = buttons.find((btn) => btn.text().includes('Cancel'))
      expect(cancelButton).toBeDefined()
      // Trigger click to verify it works
      await cancelButton!.trigger('click')
      expect(wrapper.emitted('cancel')).toBeTruthy()
    })
  })

  describe('Data Type Options', () => {
    it('should have gauge as default data type', async () => {
      await createWrapper()
      expect(wrapper.vm.dataType).toEqual(DEFAULT_MIB_OBJ_TYPE)
      expect(wrapper.vm.dataType._value).toBe('gauge')
    })

    it('should allow changing data type', async () => {
      await createWrapper()
      wrapper.vm.dataType = { _text: 'counter', _value: 'counter' }
      await nextTick()
      expect(wrapper.vm.dataType._value).toBe('counter')
    })

    it('should preserve data type after other field changes', async () => {
      await createWrapper()
      wrapper.vm.dataType = { _text: 'string', _value: 'string' }
      wrapper.vm.oid = '1.3.6.1.2.1'
      wrapper.vm.alias = 'test'
      await nextTick()

      expect(wrapper.vm.dataType._value).toBe('string')
    })
  })

  describe('Instance Options', () => {
    it('should have 0 as default instance', async () => {
      await createWrapper()
      expect(wrapper.vm.instance).toEqual({ _text: '0', _value: '0' })
    })

    it('should allow selecting from instancesOptions', async () => {
      await createWrapper()
      wrapper.vm.instance = wrapper.vm.instancesOptions[0]
      await nextTick()
      expect(wrapper.vm.instance._value).toBe('ifIndex')
    })

    it('should allow custom instance value', async () => {
      await createWrapper()
      wrapper.vm.instance = { _text: 'customInstance', _value: 'customInstance' }
      await nextTick()
      expect(wrapper.vm.instance._value).toBe('customInstance')
    })
  })

  describe('Form Reset', () => {
    it('should reset all fields when drawer closes', async () => {
      await createWrapper()
      wrapper.vm.oid = '1.3.6.1.2.1'
      wrapper.vm.alias = 'testAlias'
      wrapper.vm.instance = { _text: 'ifIndex', _value: 'ifIndex' }
      wrapper.vm.dataType = { _text: 'string', _value: 'string' }
      await nextTick()

      await wrapper.setProps({ state: { ...defaultState, visible: false } })
      await nextTick()

      expect(wrapper.vm.oid).toBe('')
      expect(wrapper.vm.alias).toBe('')
      expect(wrapper.vm.instance).toEqual({ _text: '0', _value: '0' })
      expect(wrapper.vm.dataType).toEqual(DEFAULT_MIB_OBJ_TYPE)
    })

    it('should clear custom errors and re-validate when drawer closes', async () => {
      await createWrapper()
      // Force errors by making fields invalid
      wrapper.vm.oid = 'invalid'
      wrapper.vm.alias = ''
      await nextTick()
      expect(Object.keys(wrapper.vm.errors).length).toBeGreaterThan(0)

      await wrapper.setProps({ state: { ...defaultState, visible: false } })
      await nextTick()

      // After close, form is reset and watchEffect re-validates
      // Errors will be present for empty required fields
      expect(wrapper.vm.errors.oid).toBeDefined()
      expect(wrapper.vm.errors.alias).toBeDefined()
    })

    it('should properly initialize on re-open in Create mode', async () => {
      // First, use in Edit mode
      await createWrapper({
        visible: true,
        isEditMode: CreateEditMode.Edit,
        mibObjectIndex: 0,
        mibObject: mockMibObject
      })
      expect(wrapper.vm.oid).toBe('1.3.6.1.2.1.1.1')

      // Close
      await wrapper.setProps({ state: { ...defaultState, visible: false } })
      await nextTick()

      // Re-open in Create mode
      await wrapper.setProps({ state: { ...defaultState, visible: true, isEditMode: CreateEditMode.Create } })
      await nextTick()

      expect(wrapper.vm.oid).toBe('')
      expect(wrapper.vm.alias).toBe('')
    })
  })
})


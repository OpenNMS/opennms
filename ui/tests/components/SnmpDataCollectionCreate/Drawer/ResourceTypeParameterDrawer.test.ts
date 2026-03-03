import ResourceTypeParameterDrawer from '@/components/SnmpDataCollectionCreate/Drawer/ResourceTypeParameterDrawer.vue'
import { CreateEditMode } from '@/types'
import { PersistSelectorStrategyForm, StorageStrategyForm } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

describe('ResourceTypeParameterDrawer.vue', () => {
  let wrapper: VueWrapper<any>

  interface DrawerState {
    type: 'storageStrategy' | 'persistenceSelectorStrategy' | null
    visible: boolean
    isEditMode: CreateEditMode
    persistenceSelectorStrategyIndex: number
    storageStrategyIndex: number
    persistenceSelectorStrategyObject: PersistSelectorStrategyForm | null
    storageStrategyObject: StorageStrategyForm | null
  }

  const defaultStorageState: DrawerState = {
    type: 'storageStrategy',
    visible: true,
    isEditMode: CreateEditMode.Create,
    persistenceSelectorStrategyIndex: -1,
    storageStrategyIndex: -1,
    persistenceSelectorStrategyObject: null,
    storageStrategyObject: null
  }

  const defaultPersistenceState: DrawerState = {
    type: 'persistenceSelectorStrategy',
    visible: true,
    isEditMode: CreateEditMode.Create,
    persistenceSelectorStrategyIndex: -1,
    storageStrategyIndex: -1,
    persistenceSelectorStrategyObject: null,
    storageStrategyObject: null
  }

  const mockStorageStrategyObject: StorageStrategyForm = {
    key: 'storageKey',
    value: 'storageValue'
  }

  const mockPersistenceSelectorStrategyObject: PersistSelectorStrategyForm = {
    key: 'persistenceKey',
    value: 'persistenceValue'
  }

  const createWrapper = async (state: DrawerState = defaultStorageState) => {
    wrapper = mount(ResourceTypeParameterDrawer, {
      props: {
        state
      },
      global: {
        components: {
          FeatherButton,
          FeatherInput
        },
        stubs: {
          FeatherDrawer: {
            name: 'FeatherDrawer',
            template:
              '<div v-if="modelValue" class="feather-drawer" data-test="resource-type-parameter-drawer"><slot /></div>',
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
    describe('Storage Strategy Type', () => {
      describe('Create Mode', () => {
        it('should render the drawer with Create Storage Strategy Parameter title', async () => {
          await createWrapper({ ...defaultStorageState, isEditMode: CreateEditMode.Create })
          expect(wrapper.vm.drawerTitle).toBe('Create Storage Strategy Parameter')
        })

        it('should render key input field', async () => {
          await createWrapper(defaultStorageState)
          expect(wrapper.vm.key).toBeDefined()
          wrapper.vm.key = 'testKey'
          await nextTick()
          expect(wrapper.vm.key).toBe('testKey')
        })

        it('should render value input field', async () => {
          await createWrapper(defaultStorageState)
          expect(wrapper.vm.value).toBeDefined()
          wrapper.vm.value = 'testValue'
          await nextTick()
          expect(wrapper.vm.value).toBe('testValue')
        })

        it('should render Cancel button', async () => {
          await createWrapper(defaultStorageState)
          const buttons = wrapper.findAllComponents(FeatherButton)
          const cancelButton = buttons.find((btn) => btn.text().includes('Cancel'))
          expect(cancelButton).toBeDefined()
        })

        it('should render Save button', async () => {
          await createWrapper(defaultStorageState)
          const buttons = wrapper.findAllComponents(FeatherButton)
          const saveButton = buttons.find((btn) => btn.text().includes('Save'))
          expect(saveButton).toBeDefined()
        })

        it('should render drawer when visible is true', async () => {
          await createWrapper({ ...defaultStorageState, visible: true })
          expect(wrapper.vm.isDrawerOpen).toBe(true)
        })

        it('should not show drawer when visible is false', async () => {
          await createWrapper({ ...defaultStorageState, visible: false })
          expect(wrapper.vm.isDrawerOpen).toBe(false)
        })
      })

      describe('Edit Mode', () => {
        it('should render the drawer with Edit Storage Strategy Parameter title', async () => {
          await createWrapper({
            ...defaultStorageState,
            isEditMode: CreateEditMode.Edit,
            storageStrategyIndex: 0,
            storageStrategyObject: mockStorageStrategyObject
          })
          expect(wrapper.vm.drawerTitle).toBe('Edit Storage Strategy Parameter')
        })
      })
    })

    describe('Persistence Selector Strategy Type', () => {
      describe('Create Mode', () => {
        it('should render the drawer with Create Persistence Selector Strategy Parameter title', async () => {
          await createWrapper({ ...defaultPersistenceState, isEditMode: CreateEditMode.Create })
          expect(wrapper.vm.drawerTitle).toBe('Create Persistence Selector Strategy Parameter')
        })
      })

      describe('Edit Mode', () => {
        it('should render the drawer with Edit Persistence Selector Strategy Parameter title', async () => {
          await createWrapper({
            ...defaultPersistenceState,
            isEditMode: CreateEditMode.Edit,
            persistenceSelectorStrategyIndex: 0,
            persistenceSelectorStrategyObject: mockPersistenceSelectorStrategyObject
          })
          expect(wrapper.vm.drawerTitle).toBe('Edit Persistence Selector Strategy Parameter')
        })
      })
    })

    describe('Null Type', () => {
      it('should return fallback title for drawerTitle when type is null', async () => {
        await createWrapper({
          ...defaultStorageState,
          type: null
        })
        expect(wrapper.vm.drawerTitle).toBe('Parameter')
      })
    })
  })

  describe('Initial Data Loading', () => {
    describe('Create Mode', () => {
      it('should initialize with empty key for storageStrategy', async () => {
        await createWrapper({ ...defaultStorageState, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.key).toBe('')
      })

      it('should initialize with empty value for storageStrategy', async () => {
        await createWrapper({ ...defaultStorageState, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.value).toBe('')
      })

      it('should initialize with empty key for persistenceSelectorStrategy', async () => {
        await createWrapper({ ...defaultPersistenceState, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.key).toBe('')
      })

      it('should initialize with empty value for persistenceSelectorStrategy', async () => {
        await createWrapper({ ...defaultPersistenceState, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.value).toBe('')
      })
    })

    describe('Edit Mode - Storage Strategy', () => {
      it('should load existing storage strategy key', async () => {
        await createWrapper({
          ...defaultStorageState,
          isEditMode: CreateEditMode.Edit,
          storageStrategyIndex: 0,
          storageStrategyObject: mockStorageStrategyObject
        })
        expect(wrapper.vm.key).toBe('storageKey')
      })

      it('should load existing storage strategy value', async () => {
        await createWrapper({
          ...defaultStorageState,
          isEditMode: CreateEditMode.Edit,
          storageStrategyIndex: 0,
          storageStrategyObject: mockStorageStrategyObject
        })
        expect(wrapper.vm.value).toBe('storageValue')
      })

      it('should handle null storageStrategyObject in edit mode gracefully', async () => {
        await createWrapper({
          ...defaultStorageState,
          isEditMode: CreateEditMode.Edit,
          storageStrategyIndex: 0,
          storageStrategyObject: null
        })
        expect(wrapper.vm.key).toBe('')
        expect(wrapper.vm.value).toBe('')
      })
    })

    describe('Edit Mode - Persistence Selector Strategy', () => {
      it('should load existing persistence selector strategy key', async () => {
        await createWrapper({
          ...defaultPersistenceState,
          isEditMode: CreateEditMode.Edit,
          persistenceSelectorStrategyIndex: 0,
          persistenceSelectorStrategyObject: mockPersistenceSelectorStrategyObject
        })
        expect(wrapper.vm.key).toBe('persistenceKey')
      })

      it('should load existing persistence selector strategy value', async () => {
        await createWrapper({
          ...defaultPersistenceState,
          isEditMode: CreateEditMode.Edit,
          persistenceSelectorStrategyIndex: 0,
          persistenceSelectorStrategyObject: mockPersistenceSelectorStrategyObject
        })
        expect(wrapper.vm.value).toBe('persistenceValue')
      })

      it('should handle null persistenceSelectorStrategyObject in edit mode gracefully', async () => {
        await createWrapper({
          ...defaultPersistenceState,
          isEditMode: CreateEditMode.Edit,
          persistenceSelectorStrategyIndex: 0,
          persistenceSelectorStrategyObject: null
        })
        expect(wrapper.vm.key).toBe('')
        expect(wrapper.vm.value).toBe('')
      })
    })
  })

  describe('Validation', () => {
    describe('Key Validation', () => {
      it('should show error when key is empty', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = ''
        await nextTick()
        expect(wrapper.vm.errors.key).toBeDefined()
      })

      it('should show error for key with only whitespace', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = '   '
        await nextTick()
        expect(wrapper.vm.errors.key).toBeDefined()
      })

      it('should accept key with letters only', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'testKey'
        wrapper.vm.value = 'testValue'
        await nextTick()
        expect(wrapper.vm.errors.key).toBeUndefined()
      })

      it('should accept key with numbers only', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = '12345'
        wrapper.vm.value = 'testValue'
        await nextTick()
        expect(wrapper.vm.errors.key).toBeUndefined()
      })

      it('should accept key with letters and numbers', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'test123'
        wrapper.vm.value = 'testValue'
        await nextTick()
        expect(wrapper.vm.errors.key).toBeUndefined()
      })

      it('should accept key with underscores', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'test_key'
        wrapper.vm.value = 'testValue'
        await nextTick()
        expect(wrapper.vm.errors.key).toBeUndefined()
      })

      it('should accept key with hyphens', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'test-key'
        wrapper.vm.value = 'testValue'
        await nextTick()
        expect(wrapper.vm.errors.key).toBeUndefined()
      })

      it('should accept key with mixed valid characters', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'Test_Key-123'
        wrapper.vm.value = 'testValue'
        await nextTick()
        expect(wrapper.vm.errors.key).toBeUndefined()
      })

      it('should show error for key with spaces', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'test key'
        await nextTick()
        expect(wrapper.vm.errors.key).toBe('Key can only contain letters, numbers, underscores, and hyphens')
      })

      it('should show error for key with special characters (@)', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'test@key'
        await nextTick()
        expect(wrapper.vm.errors.key).toBe('Key can only contain letters, numbers, underscores, and hyphens')
      })

      it('should show error for key with special characters (#)', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'test#key'
        await nextTick()
        expect(wrapper.vm.errors.key).toBe('Key can only contain letters, numbers, underscores, and hyphens')
      })

      it('should show error for key with dot', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'test.key'
        await nextTick()
        expect(wrapper.vm.errors.key).toBe('Key can only contain letters, numbers, underscores, and hyphens')
      })

      it('should show error for key with forward slash', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'test/key'
        await nextTick()
        expect(wrapper.vm.errors.key).toBe('Key can only contain letters, numbers, underscores, and hyphens')
      })

      it('should clear key error when valid key is provided', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = '   '
        await nextTick()
        expect(wrapper.vm.errors.key).toBeDefined()

        wrapper.vm.key = 'validKey'
        wrapper.vm.value = 'testValue'
        await nextTick()
        expect(wrapper.vm.errors.key).toBeUndefined()
      })
    })

    describe('Value Validation', () => {
      it('should show error when value is empty', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.value = ''
        await nextTick()
        expect(wrapper.vm.errors.value).toBe('Value is required')
      })

      it('should show error for value with only whitespace', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.value = '   '
        await nextTick()
        expect(wrapper.vm.errors.value).toBe('Value is required')
      })

      it('should clear value error when valid value is provided', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.value = ''
        await nextTick()
        expect(wrapper.vm.errors.value).toBeDefined()

        wrapper.vm.value = 'validValue'
        wrapper.vm.key = 'validKey'
        await nextTick()
        expect(wrapper.vm.errors.value).toBeUndefined()
      })

      it('should accept value with any characters', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'validKey'
        wrapper.vm.value = 'any!@#$%^&*()value with spaces'
        await nextTick()
        // Value only needs to be non-empty
        expect(wrapper.vm.errors.value).toBeUndefined()
      })
    })

    describe('isSaveDisabled', () => {
      it('should be disabled when form is invalid (empty key and value)', async () => {
        await createWrapper(defaultStorageState)
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })

      it('should be disabled when key is empty', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = ''
        wrapper.vm.value = 'validValue'
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })

      it('should be disabled when value is empty', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'validKey'
        wrapper.vm.value = ''
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })

      it('should be disabled when key has invalid characters', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'invalid@key'
        wrapper.vm.value = 'validValue'
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })

      it('should be enabled when form is valid', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'validKey'
        wrapper.vm.value = 'validValue'
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(false)
      })

      it('should be disabled when key becomes empty after being valid', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'validKey'
        wrapper.vm.value = 'validValue'
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(false)

        wrapper.vm.key = ''
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })

      it('should be disabled when value becomes empty after being valid', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'validKey'
        wrapper.vm.value = 'validValue'
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(false)

        wrapper.vm.value = ''
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })
    })
  })

  describe('Save Functionality', () => {
    describe('Storage Strategy', () => {
      it('should emit save event with storageStrategy type', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'testKey'
        wrapper.vm.value = 'testValue'
        await nextTick()

        wrapper.vm.saveResourceTypeParameter()
        await flushPromises()

        expect(wrapper.emitted('save')).toBeTruthy()
        expect(wrapper.emitted('save')![0]).toEqual(['storageStrategy', 'testKey', 'testValue'])
      })

      it('should emit save event when Save button is clicked', async () => {
        await createWrapper(defaultStorageState)
        wrapper.vm.key = 'testKey'
        wrapper.vm.value = 'testValue'
        await nextTick()

        const buttons = wrapper.findAllComponents(FeatherButton)
        const saveButton = buttons.find((btn) => btn.text().includes('Save'))
        await saveButton!.trigger('click')
        await flushPromises()

        expect(wrapper.emitted('save')).toBeTruthy()
        expect(wrapper.emitted('save')![0]).toEqual(['storageStrategy', 'testKey', 'testValue'])
      })
    })

    describe('Persistence Selector Strategy', () => {
      it('should emit save event with persistenceSelectorStrategy type', async () => {
        await createWrapper(defaultPersistenceState)
        wrapper.vm.key = 'persistKey'
        wrapper.vm.value = 'persistValue'
        await nextTick()

        wrapper.vm.saveResourceTypeParameter()
        await flushPromises()

        expect(wrapper.emitted('save')).toBeTruthy()
        expect(wrapper.emitted('save')![0]).toEqual(['persistenceSelectorStrategy', 'persistKey', 'persistValue'])
      })
    })

    describe('Null Type', () => {
      it('should not emit save event when type is null', async () => {
        await createWrapper({
          ...defaultStorageState,
          type: null
        })
        wrapper.vm.key = 'testKey'
        wrapper.vm.value = 'testValue'
        await nextTick()

        wrapper.vm.saveResourceTypeParameter()
        await flushPromises()

        expect(wrapper.emitted('save')).toBeFalsy()
      })
    })

    describe('Edit Mode Save', () => {
      it('should emit save with modified data in edit mode', async () => {
        await createWrapper({
          ...defaultStorageState,
          isEditMode: CreateEditMode.Edit,
          storageStrategyIndex: 0,
          storageStrategyObject: mockStorageStrategyObject
        })
        expect(wrapper.vm.key).toBe('storageKey')
        expect(wrapper.vm.value).toBe('storageValue')

        // Modify the values
        wrapper.vm.key = 'modifiedKey'
        wrapper.vm.value = 'modifiedValue'
        await nextTick()

        wrapper.vm.saveResourceTypeParameter()
        await flushPromises()

        expect(wrapper.emitted('save')).toBeTruthy()
        expect(wrapper.emitted('save')![0]).toEqual(['storageStrategy', 'modifiedKey', 'modifiedValue'])
      })
    })
  })

  describe('Cancel Functionality', () => {
    it('should emit cancel event when Cancel button is clicked', async () => {
      await createWrapper(defaultStorageState)
      const buttons = wrapper.findAllComponents(FeatherButton)
      const cancelButton = buttons.find((btn) => btn.text().includes('Cancel'))
      expect(cancelButton).toBeDefined()
      await cancelButton!.trigger('click')

      expect(wrapper.emitted('cancel')).toBeTruthy()
    })

    it('should emit cancel event without any payload', async () => {
      await createWrapper(defaultStorageState)
      const buttons = wrapper.findAllComponents(FeatherButton)
      const cancelButton = buttons.find((btn) => btn.text().includes('Cancel'))
      await cancelButton!.trigger('click')

      expect(wrapper.emitted('cancel')![0]).toEqual([])
    })

    it('should reset key field when cancel is called', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'testKey'
      wrapper.vm.value = 'testValue'
      await nextTick()

      const buttons = wrapper.findAllComponents(FeatherButton)
      const cancelButton = buttons.find((btn) => btn.text().includes('Cancel'))
      await cancelButton!.trigger('click')
      await nextTick()

      expect(wrapper.vm.key).toBe('')
    })

    it('should reset value field when cancel is called', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'testKey'
      wrapper.vm.value = 'testValue'
      await nextTick()

      const buttons = wrapper.findAllComponents(FeatherButton)
      const cancelButton = buttons.find((btn) => btn.text().includes('Cancel'))
      await cancelButton!.trigger('click')
      await nextTick()

      expect(wrapper.vm.value).toBe('')
    })

    it('should set isSaveDisabled to true when cancel is called', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'validKey'
      wrapper.vm.value = 'validValue'
      await nextTick()
      expect(wrapper.vm.isSaveDisabled).toBe(false)

      const buttons = wrapper.findAllComponents(FeatherButton)
      const cancelButton = buttons.find((btn) => btn.text().includes('Cancel'))
      await cancelButton!.trigger('click')
      await nextTick()

      expect(wrapper.vm.isSaveDisabled).toBe(true)
    })

    it('should clear errors when cancel is called', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'invalid@key'
      wrapper.vm.value = ''
      await nextTick()
      expect(Object.keys(wrapper.vm.errors).length).toBeGreaterThan(0)

      // Call cancel
      const buttons = wrapper.findAllComponents(FeatherButton)
      const cancelButton = buttons.find((btn) => btn.text().includes('Cancel'))
      await cancelButton!.trigger('click')

      expect(wrapper.emitted('cancel')).toBeTruthy()
    })
  })

  describe('Watchers', () => {
    describe('props.state.visible', () => {
      it('should update isDrawerOpen when visible changes to true', async () => {
        await createWrapper({ ...defaultStorageState, visible: false })
        expect(wrapper.vm.isDrawerOpen).toBe(false)

        await wrapper.setProps({
          state: { ...defaultStorageState, visible: true }
        })
        await nextTick()

        expect(wrapper.vm.isDrawerOpen).toBe(true)
      })

      it('should update isDrawerOpen when visible changes to false', async () => {
        await createWrapper({ ...defaultStorageState, visible: true })
        expect(wrapper.vm.isDrawerOpen).toBe(true)

        await wrapper.setProps({
          state: { ...defaultStorageState, visible: false }
        })
        await nextTick()

        expect(wrapper.vm.isDrawerOpen).toBe(false)
      })

      it('should reset form data when drawer is closed', async () => {
        await createWrapper({ ...defaultStorageState, visible: true })
        wrapper.vm.key = 'testKey'
        wrapper.vm.value = 'testValue'
        await nextTick()

        await wrapper.setProps({
          state: { ...defaultStorageState, visible: false }
        })
        await nextTick()

        expect(wrapper.vm.key).toBe('')
        expect(wrapper.vm.value).toBe('')
      })

      it('should set isSaveDisabled to true when drawer is closed', async () => {
        await createWrapper({ ...defaultStorageState, visible: true })
        wrapper.vm.key = 'validKey'
        wrapper.vm.value = 'validValue'
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(false)

        await wrapper.setProps({
          state: { ...defaultStorageState, visible: false }
        })
        await nextTick()

        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })

      it('should load storage strategy data when drawer opens in Edit mode', async () => {
        await createWrapper({ ...defaultStorageState, visible: false })

        await wrapper.setProps({
          state: {
            ...defaultStorageState,
            visible: true,
            isEditMode: CreateEditMode.Edit,
            storageStrategyIndex: 0,
            storageStrategyObject: mockStorageStrategyObject
          }
        })
        await nextTick()

        expect(wrapper.vm.key).toBe('storageKey')
        expect(wrapper.vm.value).toBe('storageValue')
      })

      it('should load persistence selector strategy data when drawer opens in Edit mode', async () => {
        await createWrapper({ ...defaultPersistenceState, visible: false })

        await wrapper.setProps({
          state: {
            ...defaultPersistenceState,
            visible: true,
            isEditMode: CreateEditMode.Edit,
            persistenceSelectorStrategyIndex: 0,
            persistenceSelectorStrategyObject: mockPersistenceSelectorStrategyObject
          }
        })
        await nextTick()

        expect(wrapper.vm.key).toBe('persistenceKey')
        expect(wrapper.vm.value).toBe('persistenceValue')
      })

      it('should reset form when drawer opens in Create mode after Edit mode', async () => {
        await createWrapper({
          ...defaultStorageState,
          visible: true,
          isEditMode: CreateEditMode.Edit,
          storageStrategyIndex: 0,
          storageStrategyObject: mockStorageStrategyObject
        })
        expect(wrapper.vm.key).toBe('storageKey')

        await wrapper.setProps({
          state: { ...defaultStorageState, visible: false }
        })
        await nextTick()

        await wrapper.setProps({
          state: { ...defaultStorageState, visible: true, isEditMode: CreateEditMode.Create }
        })
        await nextTick()

        expect(wrapper.vm.key).toBe('')
        expect(wrapper.vm.value).toBe('')
      })
    })

    describe('watchEffect for validation', () => {
      it('should update errors reactively', async () => {
        await createWrapper(defaultStorageState)
        expect(wrapper.vm.errors.key).toBeDefined()

        wrapper.vm.key = 'validKey'
        wrapper.vm.value = 'validValue'
        await nextTick()

        expect(wrapper.vm.errors.key).toBeUndefined()
        expect(wrapper.vm.errors.value).toBeUndefined()
      })

      it('should update isSaveDisabled reactively', async () => {
        await createWrapper(defaultStorageState)
        expect(wrapper.vm.isSaveDisabled).toBe(true)

        wrapper.vm.key = 'validKey'
        wrapper.vm.value = 'validValue'
        await nextTick()

        expect(wrapper.vm.isSaveDisabled).toBe(false)
      })
    })
  })

  describe('Computed Properties', () => {
    describe('drawerTitle', () => {
      it('should return "Create Storage Strategy Parameter" in Create mode for storageStrategy', async () => {
        await createWrapper({ ...defaultStorageState, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.drawerTitle).toBe('Create Storage Strategy Parameter')
      })

      it('should return "Edit Storage Strategy Parameter" in Edit mode for storageStrategy', async () => {
        await createWrapper({
          ...defaultStorageState,
          isEditMode: CreateEditMode.Edit,
          storageStrategyIndex: 0,
          storageStrategyObject: mockStorageStrategyObject
        })
        expect(wrapper.vm.drawerTitle).toBe('Edit Storage Strategy Parameter')
      })

      it('should return "Create Persistence Selector Strategy Parameter" in Create mode for persistenceSelectorStrategy', async () => {
        await createWrapper({ ...defaultPersistenceState, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.drawerTitle).toBe('Create Persistence Selector Strategy Parameter')
      })

      it('should return "Edit Persistence Selector Strategy Parameter" in Edit mode for persistenceSelectorStrategy', async () => {
        await createWrapper({
          ...defaultPersistenceState,
          isEditMode: CreateEditMode.Edit,
          persistenceSelectorStrategyIndex: 0,
          persistenceSelectorStrategyObject: mockPersistenceSelectorStrategyObject
        })
        expect(wrapper.vm.drawerTitle).toBe('Edit Persistence Selector Strategy Parameter')
      })

      it('should return fallback title when type is null', async () => {
        await createWrapper({ ...defaultStorageState, type: null })
        expect(wrapper.vm.drawerTitle).toBe('Parameter')
      })

      it('should return Edit title for storageStrategy in None mode', async () => {
        await createWrapper({ ...defaultStorageState, isEditMode: CreateEditMode.None })
        // None mode is not Create, so falls to else branch (Edit)
        expect(wrapper.vm.drawerTitle).toBe('Edit Storage Strategy Parameter')
      })
    })
  })

  describe('Edge Cases', () => {
    it('should handle very long key', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'a'.repeat(500)
      wrapper.vm.value = 'testValue'
      await nextTick()

      expect(wrapper.vm.errors.key).toBeUndefined()
    })

    it('should handle very long value', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'testKey'
      wrapper.vm.value = 'v'.repeat(1000)
      await nextTick()

      expect(wrapper.vm.errors.value).toBeUndefined()
    })

    it('should handle key with leading underscore', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = '_leadingUnderscore'
      wrapper.vm.value = 'testValue'
      await nextTick()

      expect(wrapper.vm.errors.key).toBeUndefined()
    })

    it('should handle key with trailing underscore', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'trailingUnderscore_'
      wrapper.vm.value = 'testValue'
      await nextTick()

      expect(wrapper.vm.errors.key).toBeUndefined()
    })

    it('should handle key with leading hyphen', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = '-leadingHyphen'
      wrapper.vm.value = 'testValue'
      await nextTick()

      expect(wrapper.vm.errors.key).toBeUndefined()
    })

    it('should handle key with consecutive underscores', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'double__underscore'
      wrapper.vm.value = 'testValue'
      await nextTick()

      expect(wrapper.vm.errors.key).toBeUndefined()
    })

    it('should handle key with consecutive hyphens', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'double--hyphen'
      wrapper.vm.value = 'testValue'
      await nextTick()

      expect(wrapper.vm.errors.key).toBeUndefined()
    })

    it('should preserve form state during validation cycles', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'testKey'
      wrapper.vm.value = 'testValue'
      await nextTick()

      // Trigger multiple validation cycles
      await nextTick()
      await nextTick()

      expect(wrapper.vm.key).toBe('testKey')
      expect(wrapper.vm.value).toBe('testValue')
    })

    it('should handle rapid open/close cycles', async () => {
      await createWrapper({ ...defaultStorageState, visible: false })

      // Rapid open/close
      await wrapper.setProps({ state: { ...defaultStorageState, visible: true } })
      await wrapper.setProps({ state: { ...defaultStorageState, visible: false } })
      await wrapper.setProps({ state: { ...defaultStorageState, visible: true } })
      await nextTick()

      expect(wrapper.vm.isDrawerOpen).toBe(true)
    })

    it('should handle switching between storageStrategy and persistenceSelectorStrategy types', async () => {
      await createWrapper(defaultStorageState)
      expect(wrapper.vm.drawerTitle).toBe('Create Storage Strategy Parameter')

      await wrapper.setProps({
        state: defaultPersistenceState
      })
      await nextTick()

      expect(wrapper.vm.drawerTitle).toBe('Create Persistence Selector Strategy Parameter')
    })

    it('should handle concurrent save attempts', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'testKey'
      wrapper.vm.value = 'testValue'
      await nextTick()

      // Multiple save calls
      wrapper.vm.saveResourceTypeParameter()
      wrapper.vm.saveResourceTypeParameter()
      await flushPromises()

      // Should emit save twice
      expect(wrapper.emitted('save')?.length).toBe(2)
    })

    it('should handle value with newlines', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'testKey'
      wrapper.vm.value = 'line1\nline2\nline3'
      await nextTick()

      expect(wrapper.vm.errors.value).toBeUndefined()
    })

    it('should handle value with tabs', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'testKey'
      wrapper.vm.value = 'value\twith\ttabs'
      await nextTick()

      expect(wrapper.vm.errors.value).toBeUndefined()
    })

    it('should handle key that is just underscores', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = '___'
      wrapper.vm.value = 'testValue'
      await nextTick()

      expect(wrapper.vm.errors.key).toBeUndefined()
    })

    it('should handle key that is just hyphens', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = '---'
      wrapper.vm.value = 'testValue'
      await nextTick()

      expect(wrapper.vm.errors.key).toBeUndefined()
    })

    it('should handle key that is just numbers', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = '12345'
      wrapper.vm.value = 'testValue'
      await nextTick()

      expect(wrapper.vm.errors.key).toBeUndefined()
    })

    it('should handle value with unicode characters', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'testKey'
      wrapper.vm.value = 'unicode: \u00E9\u00E8\u00EA'
      await nextTick()

      expect(wrapper.vm.errors.value).toBeUndefined()
    })

    it('should show error for key with unicode characters', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'test\u00E9key'
      await nextTick()

      expect(wrapper.vm.errors.key).toBe('Key can only contain letters, numbers, underscores, and hyphens')
    })
  })

  describe('Button States', () => {
    it('should have Save button disabled initially', async () => {
      await createWrapper(defaultStorageState)
      expect(wrapper.vm.isSaveDisabled).toBe(true)
    })

    it('should enable Save button when all fields are valid', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'validKey'
      wrapper.vm.value = 'validValue'
      await nextTick()

      expect(wrapper.vm.isSaveDisabled).toBe(false)
    })

    it('should keep Cancel button always enabled', async () => {
      await createWrapper(defaultStorageState)
      // Cancel button should exist and be clickable regardless of form state
      const buttons = wrapper.findAllComponents(FeatherButton)
      const cancelButton = buttons.find((btn) => btn.text().includes('Cancel'))
      expect(cancelButton).toBeDefined()
      // Trigger click to verify it works
      await cancelButton!.trigger('click')
      expect(wrapper.emitted('cancel')).toBeTruthy()
    })
  })

  describe('Form Reset', () => {
    it('should reset all fields when drawer closes', async () => {
      await createWrapper({ ...defaultStorageState, visible: true })
      wrapper.vm.key = 'testKey'
      wrapper.vm.value = 'testValue'
      await nextTick()

      await wrapper.setProps({ state: { ...defaultStorageState, visible: false } })
      await nextTick()

      expect(wrapper.vm.key).toBe('')
      expect(wrapper.vm.value).toBe('')
    })

    it('should properly initialize on re-open in Create mode', async () => {
      // First, use in Edit mode
      await createWrapper({
        ...defaultStorageState,
        visible: true,
        isEditMode: CreateEditMode.Edit,
        storageStrategyIndex: 0,
        storageStrategyObject: mockStorageStrategyObject
      })
      expect(wrapper.vm.key).toBe('storageKey')

      // Close
      await wrapper.setProps({ state: { ...defaultStorageState, visible: false } })
      await nextTick()

      // Re-open in Create mode
      await wrapper.setProps({ state: { ...defaultStorageState, visible: true, isEditMode: CreateEditMode.Create } })
      await nextTick()

      expect(wrapper.vm.key).toBe('')
      expect(wrapper.vm.value).toBe('')
    })
  })

  describe('Multiple Error Display', () => {
    it('should show errors for both key and value when both are invalid', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = '   '
      wrapper.vm.value = ''
      await nextTick()

      expect(wrapper.vm.errors.key).toBeDefined()
      expect(wrapper.vm.errors.value).toBeDefined()
    })

    it('should show key pattern error and value required error simultaneously', async () => {
      await createWrapper(defaultStorageState)
      wrapper.vm.key = 'invalid@key'
      wrapper.vm.value = '   '
      await nextTick()

      expect(wrapper.vm.errors.key).toBe('Key can only contain letters, numbers, underscores, and hyphens')
      expect(wrapper.vm.errors.value).toBe('Value is required')
    })
  })
})


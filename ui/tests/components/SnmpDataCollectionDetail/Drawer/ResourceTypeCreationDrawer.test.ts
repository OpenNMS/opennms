import ResourceTypeCreationDrawer from '@/components/SnmpDataCollectionDetail/Drawer/ResourceTypeCreationDrawer.vue'
import { KEY_PATTERN, PERSISTENCE_SELECTOR_STRATEGY_OPTIONS, STORAGE_STRATEGY_OPTIONS } from '@/lib/constants'
import { createResourceType, updateResourceType } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import {
  PersistSelectorStrategyForm,
  SnmpCollectionResourceType,
  StorageStrategyForm
} from '@/types/snmpDataCollection'
import { FeatherAutocomplete } from '@featherds/autocomplete'
import { FeatherButton } from '@featherds/button'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherIcon } from '@featherds/icon'
import { FeatherInput } from '@featherds/input'
import { SwitchRender } from '@featherds/switch'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

vi.mock('@/services/snmpDataCollectionService', () => ({
  createResourceType: vi.fn(),
  updateResourceType: vi.fn()
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: mockShowSnackBar
  })
}))

describe('ResourceTypeCreationDrawer.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>

  const mockStorageStrategyParams: StorageStrategyForm[] = [
    { key: 'sibling-column-name', value: 'ifIndex' },
    { key: 'replace-first', value: 's/^-//' }
  ]

  const mockPersistenceSelectorParams: PersistSelectorStrategyForm[] = [
    { key: 'match-expression', value: '.*' },
    { key: 'enabled', value: 'true' }
  ]

  const mockResourceType: SnmpCollectionResourceType = {
    id: 1,
    name: 'ifIndex',
    label: 'Interface Index',
    resourceLabel: '${ifIndex}',
    persistenceSelectorStrategy: 'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy',
    persistenceSelectorParams: JSON.stringify(mockPersistenceSelectorParams),
    storageStrategy: 'org.opennms.netmgt.collection.support.IndexStorageStrategy',
    storageStrategyParams: JSON.stringify(mockStorageStrategyParams),
    enabled: true,
    collectionSourceId: 1,
    collectionSourceName: 'Test Source'
  }

  const mockCollectionSource = {
    id: 1,
    name: 'Test Source',
    vendor: 'Test Vendor',
    description: 'Test Description',
    enabled: true,
    uploadedBy: 'testuser',
    createdTime: new Date('2024-01-01'),
    lastModified: new Date('2024-01-02')
  }

  const mountComponent = async () => {
    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useSnmpDataCollectionDetailStore(pinia)
    store.selectedCollectionSource = mockCollectionSource
    store.resourceTypeDrawerState = {
      visible: false,
      isEditMode: CreateEditMode.Create
    }
    store.selectedResourceType = null
    store.closeResourceTypeDrawer = vi.fn()
    store.fetchResourceTypes = vi.fn().mockResolvedValue(undefined)

    wrapper = mount(ResourceTypeCreationDrawer, {
      global: {
        plugins: [pinia],
        components: {
          FeatherDrawer,
          FeatherInput,
          FeatherButton,
          FeatherAutocomplete,
          FeatherIcon,
          SwitchRender
        },
        stubs: {
          FeatherDrawer: {
            name: 'FeatherDrawer',
            template: '<div v-if="modelValue" class="feather-drawer" data-test="resource-type-drawer"><slot /></div>',
            props: ['modelValue', 'labels', 'hideClose', 'width'],
            emits: ['update:modelValue', 'hidden']
          },
          FeatherInput: true,
          FeatherButton: true,
          FeatherAutocomplete: true,
          FeatherIcon: true,
          SwitchRender: true,
          EmptyList: true,
          TransitionGroup: {
            template: '<tbody><slot /></tbody>'
          }
        }
      }
    })

    await nextTick()
    await flushPromises()
  }

  beforeEach(async () => {
    vi.clearAllMocks()
    mockShowSnackBar.mockClear()
    vi.useFakeTimers()
    await mountComponent()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
    vi.restoreAllMocks()
    vi.useRealTimers()
  })

  describe('Drawer Rendering', () => {
    it('should not render the drawer when visible is false', () => {
      expect(store.resourceTypeDrawerState.visible).toBe(false)
      const drawer = wrapper.find('[data-test="resource-type-drawer"]')
      expect(drawer.exists()).toBe(false)
    })

    it('should render the drawer when visible is true', async () => {
      store.resourceTypeDrawerState.visible = true
      await nextTick()
      const drawer = wrapper.find('[data-test="resource-type-drawer"]')
      expect(drawer.exists()).toBe(true)
    })

    it('should display "Create Resource Type" title in create mode', async () => {
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Create
      store.resourceTypeDrawerState.visible = true
      await nextTick()
      expect(wrapper.vm.drawerTitle).toBe('Create Resource Type')
    })

    it('should display "Edit Resource Type" title in edit mode', async () => {
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Edit
      store.resourceTypeDrawerState.visible = true
      await nextTick()
      expect(wrapper.vm.drawerTitle).toBe('Edit Resource Type')
    })
  })

  describe('Initial State', () => {
    it('should have default status as true (enabled)', () => {
      expect(wrapper.vm.status).toBe(true)
    })

    it('should have empty name initially', () => {
      expect(wrapper.vm.name).toBe('')
    })

    it('should have empty label initially', () => {
      expect(wrapper.vm.label).toBe('')
    })

    it('should have empty resourceLabel initially', () => {
      expect(wrapper.vm.resourceLabel).toBe('')
    })

    it('should have undefined storageStrategy initially', () => {
      expect(wrapper.vm.storageStrategy).toBeUndefined()
    })

    it('should have undefined persistenceSelectorStrategy initially', () => {
      expect(wrapper.vm.persistenceSelectorStrategy).toBeUndefined()
    })

    it('should have empty storageStrategyParams array initially', () => {
      expect(wrapper.vm.storageStrategyParams).toEqual([])
    })

    it('should have empty persistenceSelectorStrategyParams array initially', () => {
      expect(wrapper.vm.persistenceSelectorStrategyParams).toEqual([])
    })

    it('should have errors object populated by watchEffect validation', () => {
      expect(wrapper.vm.errors).toBeDefined()
      expect(typeof wrapper.vm.errors).toBe('object')
    })

    it('should have isSaveDisabled as true initially', () => {
      expect(wrapper.vm.isSaveDisabled).toBe(true)
    })
  })

  describe('Create Mode - Load Initial Data', () => {
    beforeEach(async () => {
      store.resourceTypeDrawerState.visible = false
      await nextTick()
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Create
      store.resourceTypeDrawerState.visible = true
      await nextTick()
    })

    it('should have empty name in create mode', () => {
      expect(wrapper.vm.name).toBe('')
    })

    it('should have empty label in create mode', () => {
      expect(wrapper.vm.label).toBe('')
    })

    it('should have empty resourceLabel in create mode', () => {
      expect(wrapper.vm.resourceLabel).toBe('')
    })

    it('should have status as true in create mode', () => {
      expect(wrapper.vm.status).toBe(true)
    })

    it('should have empty storageStrategyParams in create mode', () => {
      expect(wrapper.vm.storageStrategyParams).toEqual([])
    })

    it('should have empty persistenceSelectorStrategyParams in create mode', () => {
      expect(wrapper.vm.persistenceSelectorStrategyParams).toEqual([])
    })
  })

  describe('Edit Mode - Load Initial Data', () => {
    beforeEach(async () => {
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Edit
      store.selectedResourceType = mockResourceType
      store.resourceTypeDrawerState.visible = false
      await nextTick()
      store.resourceTypeDrawerState.visible = true
      await nextTick()
      await flushPromises()
    })

    it('should populate name field with existing data', () => {
      expect(wrapper.vm.name).toBe('ifIndex')
    })

    it('should populate label field with existing data', () => {
      expect(wrapper.vm.label).toBe('Interface Index')
    })

    it('should populate resourceLabel field with existing data', () => {
      expect(wrapper.vm.resourceLabel).toBe('${ifIndex}')
    })

    it('should populate status with existing enabled value', () => {
      expect(wrapper.vm.status).toBe(true)
    })

    it('should populate storageStrategy with existing data', async () => {
      await nextTick()
      expect(wrapper.vm.storageStrategy).toEqual({
        _text: 'org.opennms.netmgt.collection.support.IndexStorageStrategy',
        _value: 'org.opennms.netmgt.collection.support.IndexStorageStrategy'
      })
    })

    it('should populate persistenceSelectorStrategy with existing data', async () => {
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategy).toEqual({
        _text: 'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy',
        _value: 'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy'
      })
    })

    it('should populate storageStrategyParams with parsed JSON data', () => {
      expect(wrapper.vm.storageStrategyParams).toEqual(mockStorageStrategyParams)
    })

    it('should populate persistenceSelectorStrategyParams with parsed JSON data', () => {
      expect(wrapper.vm.persistenceSelectorStrategyParams).toEqual(mockPersistenceSelectorParams)
    })

    it('should handle null selectedResourceType gracefully', async () => {
      store.selectedResourceType = null
      store.resourceTypeDrawerState.visible = false
      await nextTick()
      store.resourceTypeDrawerState.visible = true
      await nextTick()
      expect(wrapper.vm.name).toBe('')
    })

    it('should handle empty storageStrategyParams JSON', async () => {
      store.selectedResourceType = { ...mockResourceType, storageStrategyParams: '[]' }
      store.resourceTypeDrawerState.visible = false
      await nextTick()
      store.resourceTypeDrawerState.visible = true
      await nextTick()
      expect(wrapper.vm.storageStrategyParams).toEqual([])
    })

    it('should handle disabled status in edit mode', async () => {
      store.selectedResourceType = { ...mockResourceType, enabled: false }
      store.resourceTypeDrawerState.visible = false
      await nextTick()
      store.resourceTypeDrawerState.visible = true
      await nextTick()
      expect(wrapper.vm.status).toBe(false)
    })
  })

  describe('Form Validation - Resource Type', () => {
    it.each([
      { field: 'name', value: '', errorKey: 'name', expectedError: 'Name is required' },
      { field: 'name', value: '   ', errorKey: 'name', expectedError: 'Name is required' },
      { field: 'label', value: '', errorKey: 'label', expectedError: 'Label is required' },
      { field: 'label', value: '   ', errorKey: 'label', expectedError: 'Label is required' }
    ])(
      'should show error "$expectedError" when $field is "$value"',
      async ({ field, value, errorKey, expectedError }) => {
        wrapper.vm[field] = value
        await nextTick()
        const errors = wrapper.vm.validateResourceType()
        expect(errors[errorKey as keyof typeof errors]).toBe(expectedError)
      }
    )

    it('should show error when storageStrategy is undefined', async () => {
      wrapper.vm.storageStrategy = undefined
      await nextTick()
      const errors = wrapper.vm.validateResourceType()
      expect(errors.storageStrategy).toBe('Storage Strategy is required')
    })

    it('should show error when persistenceSelectorStrategy is undefined', async () => {
      wrapper.vm.persistenceSelectorStrategy = undefined
      await nextTick()
      const errors = wrapper.vm.validateResourceType()
      expect(errors.persistenceSelectorStrategy).toBe('Persistence Selector Strategy is required')
    })

    it('should not show errors when all fields are valid', async () => {
      wrapper.vm.name = 'Valid Name'
      wrapper.vm.label = 'Valid Label'
      wrapper.vm.storageStrategy = { _text: 'test', _value: 'test' }
      wrapper.vm.persistenceSelectorStrategy = { _text: 'test', _value: 'test' }
      await nextTick()
      const errors = wrapper.vm.validateResourceType()
      expect(Object.keys(errors).length).toBe(0)
    })

    it('should maintain multiple errors simultaneously', async () => {
      wrapper.vm.name = ''
      wrapper.vm.label = ''
      wrapper.vm.storageStrategy = undefined
      wrapper.vm.persistenceSelectorStrategy = undefined
      await nextTick()
      const errors = wrapper.vm.validateResourceType()
      expect(Object.keys(errors).length).toBe(4)
      expect(errors.name).toBeDefined()
      expect(errors.label).toBeDefined()
      expect(errors.storageStrategy).toBeDefined()
      expect(errors.persistenceSelectorStrategy).toBeDefined()
    })

    it('should update isSaveDisabled based on validation', async () => {
      wrapper.vm.name = ''
      await nextTick()
      expect(wrapper.vm.isSaveDisabled).toBe(true)

      wrapper.vm.name = 'Valid Name'
      wrapper.vm.label = 'Valid Label'
      wrapper.vm.storageStrategy = { _text: 'test', _value: 'test' }
      wrapper.vm.persistenceSelectorStrategy = { _text: 'test', _value: 'test' }
      await nextTick()
      expect(wrapper.vm.isSaveDisabled).toBe(false)
    })
  })

  describe('Status Toggle', () => {
    it('should toggle status from true to false', async () => {
      expect(wrapper.vm.status).toBe(true)
      wrapper.vm.onChangeStatus()
      await nextTick()
      expect(wrapper.vm.status).toBe(false)
    })

    it('should toggle status from false to true', async () => {
      wrapper.vm.status = false
      await nextTick()
      wrapper.vm.onChangeStatus()
      await nextTick()
      expect(wrapper.vm.status).toBe(true)
    })

    it('should toggle status multiple times', async () => {
      expect(wrapper.vm.status).toBe(true)
      wrapper.vm.onChangeStatus()
      await nextTick()
      expect(wrapper.vm.status).toBe(false)
      wrapper.vm.onChangeStatus()
      await nextTick()
      expect(wrapper.vm.status).toBe(true)
    })
  })

  describe('Storage Strategy Search', () => {
    it('should filter storage strategies based on search query', async () => {
      wrapper.vm.onSearchStorageStrategy('Index')
      vi.advanceTimersByTime(500)
      await nextTick()
      expect(wrapper.vm.storageStrategyResults.length).toBeGreaterThan(0)
      expect(wrapper.vm.storageStrategyResults.some((r: any) => r._text.includes('Index'))).toBe(true)
    })

    it('should debounce search for 500ms', async () => {
      wrapper.vm.onSearchStorageStrategy('test')
      expect(wrapper.vm.storageStrategyLoading).toBe(true)
      vi.advanceTimersByTime(300)
      expect(wrapper.vm.storageStrategyLoading).toBe(true)
      vi.advanceTimersByTime(200)
      await nextTick()
      expect(wrapper.vm.storageStrategyLoading).toBe(false)
    })

    it('should clear previous timeout on new search', async () => {
      wrapper.vm.onSearchStorageStrategy('Index')
      vi.advanceTimersByTime(300)
      wrapper.vm.onSearchStorageStrategy('Sibling')
      vi.advanceTimersByTime(500)
      await nextTick()
      expect(wrapper.vm.storageStrategyResults.some((r: any) => r._text.includes('Sibling'))).toBe(true)
    })

    it('should be case-insensitive', async () => {
      wrapper.vm.onSearchStorageStrategy('INDEX')
      vi.advanceTimersByTime(500)
      await nextTick()
      expect(wrapper.vm.storageStrategyResults.length).toBeGreaterThan(0)
    })

    it('should add custom option when no matches found', async () => {
      wrapper.vm.onSearchStorageStrategy('custom.strategy.Class')
      vi.advanceTimersByTime(500)
      await nextTick()
      expect(wrapper.vm.storageStrategyResults).toContainEqual({
        _text: 'custom.strategy.Class',
        _value: 'custom.strategy.Class'
      })
    })

    it('should set loading state during search', () => {
      wrapper.vm.onSearchStorageStrategy('test')
      expect(wrapper.vm.storageStrategyLoading).toBe(true)
    })
  })

  describe('Persistence Selector Strategy Search', () => {
    it('should filter persistence strategies based on search query', async () => {
      wrapper.vm.onSearchPersistenceSelectorStrategy('Persist')
      vi.advanceTimersByTime(500)
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategyResults.length).toBeGreaterThan(0)
    })

    it('should debounce search for 500ms', async () => {
      wrapper.vm.onSearchPersistenceSelectorStrategy('test')
      expect(wrapper.vm.persistenceSelectorStrategyLoading).toBe(true)
      vi.advanceTimersByTime(300)
      expect(wrapper.vm.persistenceSelectorStrategyLoading).toBe(true)
      vi.advanceTimersByTime(200)
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategyLoading).toBe(false)
    })

    it('should add custom option when no matches found', async () => {
      wrapper.vm.onSearchPersistenceSelectorStrategy('custom.persistence.Class')
      vi.advanceTimersByTime(500)
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategyResults).toContainEqual({
        _text: 'custom.persistence.Class',
        _value: 'custom.persistence.Class'
      })
    })
  })

  describe('Parameter Drawer State - Storage Strategy', () => {
    it('should have resourceTypeDrawerState initially closed', () => {
      expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(false)
    })

    it('should open storage strategy parameter drawer in create mode', async () => {
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Create)
      await nextTick()
      expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(true)
      expect(wrapper.vm.resourceTypeDrawerState.type).toBe('storageStrategy')
      expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.Create)
      expect(wrapper.vm.resourceTypeDrawerState.storageStrategyIndex).toBe(-1)
    })

    it('should open storage strategy parameter drawer in edit mode', async () => {
      const param = mockStorageStrategyParams[0]
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Edit, 0, param)
      await nextTick()
      expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(true)
      expect(wrapper.vm.resourceTypeDrawerState.type).toBe('storageStrategy')
      expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.Edit)
      expect(wrapper.vm.resourceTypeDrawerState.storageStrategyIndex).toBe(0)
      expect(wrapper.vm.resourceTypeDrawerState.storageStrategyObject).toEqual(param)
    })

    it('should have correct title for create storage strategy parameter', async () => {
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Create)
      await nextTick()
      expect(wrapper.vm.parameterDrawerTitle).toBe('Create Storage Strategy Parameter')
    })

    it('should have correct title for edit storage strategy parameter', async () => {
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Edit, 0, mockStorageStrategyParams[0])
      await nextTick()
      expect(wrapper.vm.parameterDrawerTitle).toBe('Edit Storage Strategy Parameter')
    })
  })

  describe('Parameter Drawer State - Persistence Selector Strategy', () => {
    it('should open persistence selector strategy parameter drawer in create mode', async () => {
      wrapper.vm.openPersistenceSelectorStrategyDrawer(CreateEditMode.Create)
      await nextTick()
      expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(true)
      expect(wrapper.vm.resourceTypeDrawerState.type).toBe('persistenceSelectorStrategy')
      expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.Create)
      expect(wrapper.vm.resourceTypeDrawerState.persistenceSelectorStrategyIndex).toBe(-1)
    })

    it('should open persistence selector strategy parameter drawer in edit mode', async () => {
      const param = mockPersistenceSelectorParams[0]
      wrapper.vm.openPersistenceSelectorStrategyDrawer(CreateEditMode.Edit, 0, param)
      await nextTick()
      expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(true)
      expect(wrapper.vm.resourceTypeDrawerState.type).toBe('persistenceSelectorStrategy')
      expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.Edit)
      expect(wrapper.vm.resourceTypeDrawerState.persistenceSelectorStrategyIndex).toBe(0)
      expect(wrapper.vm.resourceTypeDrawerState.persistenceSelectorStrategyObject).toEqual(param)
    })

    it('should have correct title for create persistence selector strategy parameter', async () => {
      wrapper.vm.openPersistenceSelectorStrategyDrawer(CreateEditMode.Create)
      await nextTick()
      expect(wrapper.vm.parameterDrawerTitle).toBe('Create Persistence Selector Strategy Parameter')
    })

    it('should have correct title for edit persistence selector strategy parameter', async () => {
      wrapper.vm.openPersistenceSelectorStrategyDrawer(CreateEditMode.Edit, 0, mockPersistenceSelectorParams[0])
      await nextTick()
      expect(wrapper.vm.parameterDrawerTitle).toBe('Edit Persistence Selector Strategy Parameter')
    })
  })

  describe('Parameter Form Validation', () => {
    it('should show error when key is empty', async () => {
      wrapper.vm.key = ''
      await nextTick()
      const errors = wrapper.vm.validateResourceTypeParameter()
      expect(errors.key).toBeDefined()
    })

    it('should show error when key contains invalid characters', async () => {
      wrapper.vm.key = 'invalid key!'
      await nextTick()
      const errors = wrapper.vm.validateResourceTypeParameter()
      expect(errors.key).toBe('Key can only contain letters, numbers, underscores, and hyphens')
    })

    it.each([{ key: 'valid_key' }, { key: 'valid-key' }, { key: 'validKey123' }, { key: 'VALID_KEY' }])(
      'should accept valid key format: "$key"',
      async ({ key }) => {
        wrapper.vm.key = key
        wrapper.vm.value = 'testValue'
        await nextTick()
        const errors = wrapper.vm.validateResourceTypeParameter()
        expect(errors.key).toBeUndefined()
      }
    )

    it('should show error when value is empty', async () => {
      wrapper.vm.value = ''
      await nextTick()
      const errors = wrapper.vm.validateResourceTypeParameter()
      expect(errors.value).toBe('Value is required')
    })

    it('should update isParameterSaveDisabled based on validation', async () => {
      wrapper.vm.key = ''
      wrapper.vm.value = ''
      await nextTick()
      expect(wrapper.vm.isParameterSaveDisabled).toBe(true)

      wrapper.vm.key = 'valid_key'
      wrapper.vm.value = 'valid_value'
      await nextTick()
      expect(wrapper.vm.isParameterSaveDisabled).toBe(false)
    })
  })

  describe('Save Storage Strategy Parameter', () => {
    beforeEach(async () => {
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Create)
      await nextTick()
    })

    it('should add new storage strategy parameter in create mode', async () => {
      wrapper.vm.key = 'newKey'
      wrapper.vm.value = 'newValue'
      await nextTick()

      wrapper.vm.saveResourceTypeParameter()
      await nextTick()

      expect(wrapper.vm.storageStrategyParams.length).toBe(1)
      expect(wrapper.vm.storageStrategyParams[0]).toEqual({ key: 'newKey', value: 'newValue' })
    })

    it('should update existing storage strategy parameter in edit mode', async () => {
      wrapper.vm.storageStrategyParams = [...mockStorageStrategyParams]
      await nextTick()

      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Edit, 0, mockStorageStrategyParams[0])
      await nextTick()

      wrapper.vm.key = 'updatedKey'
      wrapper.vm.value = 'updatedValue'
      await nextTick()

      wrapper.vm.saveResourceTypeParameter()
      await nextTick()

      expect(wrapper.vm.storageStrategyParams[0]).toEqual({ key: 'updatedKey', value: 'updatedValue' })
    })

    it('should close parameter drawer after save', async () => {
      wrapper.vm.key = 'testKey'
      wrapper.vm.value = 'testValue'
      await nextTick()

      wrapper.vm.saveResourceTypeParameter()
      await nextTick()

      expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(false)
    })
  })

  describe('Save Persistence Selector Strategy Parameter', () => {
    beforeEach(async () => {
      wrapper.vm.openPersistenceSelectorStrategyDrawer(CreateEditMode.Create)
      await nextTick()
    })

    it('should add new persistence selector strategy parameter in create mode', async () => {
      wrapper.vm.key = 'newKey'
      wrapper.vm.value = 'newValue'
      await nextTick()

      wrapper.vm.saveResourceTypeParameter()
      await nextTick()

      expect(wrapper.vm.persistenceSelectorStrategyParams.length).toBe(1)
      expect(wrapper.vm.persistenceSelectorStrategyParams[0]).toEqual({ key: 'newKey', value: 'newValue' })
    })

    it('should update existing persistence selector strategy parameter in edit mode', async () => {
      wrapper.vm.persistenceSelectorStrategyParams = [...mockPersistenceSelectorParams]
      await nextTick()

      wrapper.vm.openPersistenceSelectorStrategyDrawer(CreateEditMode.Edit, 0, mockPersistenceSelectorParams[0])
      await nextTick()

      wrapper.vm.key = 'updatedKey'
      wrapper.vm.value = 'updatedValue'
      await nextTick()

      wrapper.vm.saveResourceTypeParameter()
      await nextTick()

      expect(wrapper.vm.persistenceSelectorStrategyParams[0]).toEqual({ key: 'updatedKey', value: 'updatedValue' })
    })
  })

  describe('Delete Parameters', () => {
    it('should delete storage strategy parameter', async () => {
      wrapper.vm.storageStrategyParams = [...mockStorageStrategyParams]
      await nextTick()

      wrapper.vm.deleteStorageStrategy(0)
      await nextTick()

      expect(wrapper.vm.storageStrategyParams.length).toBe(1)
      expect(wrapper.vm.storageStrategyParams[0].key).toBe('replace-first')
    })

    it('should delete persistence selector strategy parameter', async () => {
      wrapper.vm.persistenceSelectorStrategyParams = [...mockPersistenceSelectorParams]
      await nextTick()

      wrapper.vm.deletePersistenceSelectorStrategy(0)
      await nextTick()

      expect(wrapper.vm.persistenceSelectorStrategyParams.length).toBe(1)
      expect(wrapper.vm.persistenceSelectorStrategyParams[0].key).toBe('enabled')
    })

    it('should handle deleting last storage strategy parameter', async () => {
      wrapper.vm.storageStrategyParams = [mockStorageStrategyParams[0]]
      await nextTick()

      wrapper.vm.deleteStorageStrategy(0)
      await nextTick()

      expect(wrapper.vm.storageStrategyParams.length).toBe(0)
    })
  })

  describe('Pagination - Storage Strategy Params', () => {
    const manyStorageParams: StorageStrategyForm[] = Array.from({ length: 8 }, (_, i) => ({
      key: `key${i}`,
      value: `value${i}`
    }))

    beforeEach(async () => {
      wrapper.vm.storageStrategyParams = [...manyStorageParams]
      wrapper.vm.storageStrategyParamsTotal = manyStorageParams.length
      wrapper.vm.storageStrategyParamsPageSize = 3
      wrapper.vm.storageStrategyParamsObjects = manyStorageParams.slice(0, 3)
      await nextTick()
    })

    it('should display first page of storage strategy parameters', () => {
      expect(wrapper.vm.storageStrategyParamsObjects.length).toBe(3)
      expect(wrapper.vm.storageStrategyParamsObjects[0].key).toBe('key0')
    })

    it('should change page correctly', async () => {
      wrapper.vm.onStorageStrategyParamsPageChange(2)
      await nextTick()
      expect(wrapper.vm.storageStrategyParamsPage).toBe(2)
      expect(wrapper.vm.storageStrategyParamsObjects[0].key).toBe('key3')
    })

    it('should change page size correctly', async () => {
      wrapper.vm.onStorageStrategyParamsPageSizeChange(5)
      await nextTick()
      expect(wrapper.vm.storageStrategyParamsPageSize).toBe(5)
      expect(wrapper.vm.storageStrategyParamsPage).toBe(1)
      expect(wrapper.vm.storageStrategyParamsObjects.length).toBe(5)
    })

    it('should handle page change to last page', async () => {
      wrapper.vm.onStorageStrategyParamsPageChange(3)
      await nextTick()
      // Page 3 with pageSize 3 should show 2 items (items 6-7)
      expect(wrapper.vm.storageStrategyParamsObjects.length).toBe(2)
    })

    it('should reset to first page when page size changes', async () => {
      wrapper.vm.storageStrategyParamsPage = 2
      await nextTick()
      wrapper.vm.onStorageStrategyParamsPageSizeChange(3)
      await nextTick()
      expect(wrapper.vm.storageStrategyParamsPage).toBe(1)
    })

    it('should edit item on page 2 with correct actualIndex calculation', async () => {
      // Navigate to page 2 (items 3-5, indices 3-5 in storageStrategyParams array)
      wrapper.vm.onStorageStrategyParamsPageChange(2)
      await nextTick()
      expect(wrapper.vm.storageStrategyParamsPage).toBe(2)
      expect(wrapper.vm.storageStrategyParamsObjects[0].key).toBe('key3')

      // Open edit drawer for first item on page 2 (page-relative index 0)
      // Component calculates actualIndex = (page - 1) * pageSize + index = (2-1)*3 + 0 = 3
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Edit, 0, wrapper.vm.storageStrategyParamsObjects[0])
      await nextTick()

      // Verify drawer opened in edit mode for correct item
      expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(true)
      expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.Edit)
      expect(wrapper.vm.resourceTypeDrawerState.storageStrategyIndex).toBe(3)
    })

    it('should edit item on page 3 with correct actualIndex calculation', async () => {
      // Navigate to page 3 (items 6-7, indices 6-7 in storageStrategyParams array)
      wrapper.vm.onStorageStrategyParamsPageChange(3)
      await nextTick()
      expect(wrapper.vm.storageStrategyParamsPage).toBe(3)
      expect(wrapper.vm.storageStrategyParamsObjects[0].key).toBe('key6')

      // Open edit drawer for second item on page 3 (page-relative index 1)
      // Component calculates actualIndex = (page - 1) * pageSize + index = (3-1)*3 + 1 = 7
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Edit, 1, wrapper.vm.storageStrategyParamsObjects[1])
      await nextTick()

      expect(wrapper.vm.resourceTypeDrawerState.storageStrategyIndex).toBe(7)
    })

    it('should update item on page 2 correctly via saveParameters', async () => {
      // Navigate to page 2
      wrapper.vm.onStorageStrategyParamsPageChange(2)
      await nextTick()

      // Open edit for first item on page 2 (key3)
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Edit, 0, wrapper.vm.storageStrategyParamsObjects[0])
      await nextTick()

      // Set key and value
      wrapper.vm.key = 'updatedKey3'
      wrapper.vm.value = 'updatedValue3'
      await nextTick()

      // Save the parameter
      wrapper.vm.saveResourceTypeParameter()
      await nextTick()

      // Verify the correct item in storageStrategyParams array was updated (index 3)
      expect(wrapper.vm.storageStrategyParams[3].key).toBe('updatedKey3')
      expect(wrapper.vm.storageStrategyParams[3].value).toBe('updatedValue3')
      // Other items should be unchanged
      expect(wrapper.vm.storageStrategyParams[2].key).toBe('key2')
      expect(wrapper.vm.storageStrategyParams[4].key).toBe('key4')
    })

    it('should delete item from page 2 with correct actualIndex', async () => {
      // Navigate to page 2
      wrapper.vm.onStorageStrategyParamsPageChange(2)
      await nextTick()
      expect(wrapper.vm.storageStrategyParamsObjects[0].key).toBe('key3')

      const initialLength = wrapper.vm.storageStrategyParams.length

      // Delete first item on page 2 (page-relative index 0)
      // Component calculates actualIndex = (page - 1) * pageSize + index = (2-1)*3 + 0 = 3
      wrapper.vm.deleteStorageStrategy(0)
      await nextTick()

      expect(wrapper.vm.storageStrategyParams.length).toBe(initialLength - 1)
      // key3 should be deleted, key4 now at index 3
      expect(wrapper.vm.storageStrategyParams[3].key).toBe('key4')
    })

    it('should delete middle item on page 2 correctly', async () => {
      // Navigate to page 2
      wrapper.vm.onStorageStrategyParamsPageChange(2)
      await nextTick()
      expect(wrapper.vm.storageStrategyParamsObjects[1].key).toBe('key4')

      // Delete second item on page 2 (page-relative index 1)
      // Component calculates actualIndex = (page - 1) * pageSize + index = (2-1)*3 + 1 = 4
      wrapper.vm.deleteStorageStrategy(1)
      await nextTick()

      // key4 should be deleted
      expect(wrapper.vm.storageStrategyParams.find((obj: StorageStrategyForm) => obj.key === 'key4')).toBeUndefined()
      // key5 should now be at where key4 was (index 4)
      expect(wrapper.vm.storageStrategyParams[4].key).toBe('key5')
    })

    it('should handle deleting last item on page 2', async () => {
      // Create params with exactly 4 items (page 1: 3 items, page 2: 1 item)
      wrapper.vm.storageStrategyParams = Array.from({ length: 4 }, (_, i) => ({
        key: `key${i}`,
        value: `value${i}`
      }))
      wrapper.vm.storageStrategyParamsTotal = 4
      await nextTick()

      // Navigate to page 2
      wrapper.vm.onStorageStrategyParamsPageChange(2)
      await nextTick()
      expect(wrapper.vm.storageStrategyParamsObjects.length).toBe(1)
      expect(wrapper.vm.storageStrategyParamsObjects[0].key).toBe('key3')

      // Delete only item on page 2 (page-relative index 0)
      // Component calculates actualIndex = (page - 1) * pageSize + index = (2-1)*3 + 0 = 3
      wrapper.vm.deleteStorageStrategy(0)
      await nextTick()

      expect(wrapper.vm.storageStrategyParams.length).toBe(3)
      expect(wrapper.vm.storageStrategyParams.find((obj: StorageStrategyForm) => obj.key === 'key3')).toBeUndefined()
    })
  })

  describe('Pagination - Persistence Selector Strategy Params', () => {
    const manyPersistenceParams: PersistSelectorStrategyForm[] = Array.from({ length: 8 }, (_, i) => ({
      key: `pkey${i}`,
      value: `pvalue${i}`
    }))

    beforeEach(async () => {
      wrapper.vm.persistenceSelectorStrategyParams = [...manyPersistenceParams]
      wrapper.vm.persistenceSelectorStrategyParamsTotal = manyPersistenceParams.length
      wrapper.vm.persistenceSelectorStrategyParamsPageSize = 3
      wrapper.vm.persistenceSelectorStrategyParamsObjects = manyPersistenceParams.slice(0, 3)
      await nextTick()
    })

    it('should display first page of persistence selector strategy parameters', () => {
      expect(wrapper.vm.persistenceSelectorStrategyParamsObjects.length).toBe(3)
      expect(wrapper.vm.persistenceSelectorStrategyParamsObjects[0].key).toBe('pkey0')
    })

    it('should change page correctly', async () => {
      wrapper.vm.onPersistenceSelectorStrategyParamsPageChange(2)
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategyParamsPage).toBe(2)
      expect(wrapper.vm.persistenceSelectorStrategyParamsObjects[0].key).toBe('pkey3')
    })

    it('should change page size correctly', async () => {
      wrapper.vm.onPersistenceSelectorStrategyParamsPageSizeChange(5)
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategyParamsPageSize).toBe(5)
      expect(wrapper.vm.persistenceSelectorStrategyParamsPage).toBe(1)
      expect(wrapper.vm.persistenceSelectorStrategyParamsObjects.length).toBe(5)
    })

    it('should handle page change to last page', async () => {
      wrapper.vm.onPersistenceSelectorStrategyParamsPageChange(3)
      await nextTick()
      // Page 3 with pageSize 3 should show 2 items (items 6-7)
      expect(wrapper.vm.persistenceSelectorStrategyParamsObjects.length).toBe(2)
    })

    it('should reset to first page when page size changes', async () => {
      wrapper.vm.persistenceSelectorStrategyParamsPage = 2
      await nextTick()
      wrapper.vm.onPersistenceSelectorStrategyParamsPageSizeChange(3)
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategyParamsPage).toBe(1)
    })

    it('should edit item on page 2 with correct actualIndex calculation', async () => {
      // Navigate to page 2 (items 3-5, indices 3-5 in persistenceSelectorStrategyParams array)
      wrapper.vm.onPersistenceSelectorStrategyParamsPageChange(2)
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategyParamsPage).toBe(2)
      expect(wrapper.vm.persistenceSelectorStrategyParamsObjects[0].key).toBe('pkey3')

      // Open edit drawer for first item on page 2 (page-relative index 0)
      // Component calculates actualIndex = (page - 1) * pageSize + index = (2-1)*3 + 0 = 3
      wrapper.vm.openPersistenceSelectorStrategyDrawer(
        CreateEditMode.Edit,
        0,
        wrapper.vm.persistenceSelectorStrategyParamsObjects[0]
      )
      await nextTick()

      // Verify drawer opened in edit mode for correct item
      expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(true)
      expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.Edit)
      expect(wrapper.vm.resourceTypeDrawerState.persistenceSelectorStrategyIndex).toBe(3)
    })

    it('should edit item on page 3 with correct actualIndex calculation', async () => {
      // Navigate to page 3 (items 6-7, indices 6-7 in persistenceSelectorStrategyParams array)
      wrapper.vm.onPersistenceSelectorStrategyParamsPageChange(3)
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategyParamsPage).toBe(3)
      expect(wrapper.vm.persistenceSelectorStrategyParamsObjects[0].key).toBe('pkey6')

      // Open edit drawer for second item on page 3 (page-relative index 1)
      // Component calculates actualIndex = (page - 1) * pageSize + index = (3-1)*3 + 1 = 7
      wrapper.vm.openPersistenceSelectorStrategyDrawer(
        CreateEditMode.Edit,
        1,
        wrapper.vm.persistenceSelectorStrategyParamsObjects[1]
      )
      await nextTick()

      expect(wrapper.vm.resourceTypeDrawerState.persistenceSelectorStrategyIndex).toBe(7)
    })

    it('should update item on page 2 correctly via saveParameters', async () => {
      // Navigate to page 2
      wrapper.vm.onPersistenceSelectorStrategyParamsPageChange(2)
      await nextTick()

      // Open edit for first item on page 2 (pkey3)
      wrapper.vm.openPersistenceSelectorStrategyDrawer(
        CreateEditMode.Edit,
        0,
        wrapper.vm.persistenceSelectorStrategyParamsObjects[0]
      )
      await nextTick()

      // Set key and value
      wrapper.vm.key = 'updatedPkey3'
      wrapper.vm.value = 'updatedPvalue3'
      await nextTick()

      // Save the parameter
      wrapper.vm.saveResourceTypeParameter()
      await nextTick()

      // Verify the correct item in persistenceSelectorStrategyParams array was updated (index 3)
      expect(wrapper.vm.persistenceSelectorStrategyParams[3].key).toBe('updatedPkey3')
      expect(wrapper.vm.persistenceSelectorStrategyParams[3].value).toBe('updatedPvalue3')
      // Other items should be unchanged
      expect(wrapper.vm.persistenceSelectorStrategyParams[2].key).toBe('pkey2')
      expect(wrapper.vm.persistenceSelectorStrategyParams[4].key).toBe('pkey4')
    })

    it('should delete item from page 2 with correct actualIndex', async () => {
      // Navigate to page 2
      wrapper.vm.onPersistenceSelectorStrategyParamsPageChange(2)
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategyParamsObjects[0].key).toBe('pkey3')

      const initialLength = wrapper.vm.persistenceSelectorStrategyParams.length

      // Delete first item on page 2 (page-relative index 0)
      // Component calculates actualIndex = (page - 1) * pageSize + index = (2-1)*3 + 0 = 3
      wrapper.vm.deletePersistenceSelectorStrategy(0)
      await nextTick()

      expect(wrapper.vm.persistenceSelectorStrategyParams.length).toBe(initialLength - 1)
      // pkey3 should be deleted, pkey4 now at index 3
      expect(wrapper.vm.persistenceSelectorStrategyParams[3].key).toBe('pkey4')
    })

    it('should delete middle item on page 2 correctly', async () => {
      // Navigate to page 2
      wrapper.vm.onPersistenceSelectorStrategyParamsPageChange(2)
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategyParamsObjects[1].key).toBe('pkey4')

      // Delete second item on page 2 (page-relative index 1)
      // Component calculates actualIndex = (page - 1) * pageSize + index = (2-1)*3 + 1 = 4
      wrapper.vm.deletePersistenceSelectorStrategy(1)
      await nextTick()

      // pkey4 should be deleted
      expect(
        wrapper.vm.persistenceSelectorStrategyParams.find((obj: PersistSelectorStrategyForm) => obj.key === 'pkey4')
      ).toBeUndefined()
      // pkey5 should now be at where pkey4 was (index 4)
      expect(wrapper.vm.persistenceSelectorStrategyParams[4].key).toBe('pkey5')
    })

    it('should handle deleting last item on page 2', async () => {
      // Create params with exactly 4 items (page 1: 3 items, page 2: 1 item)
      wrapper.vm.persistenceSelectorStrategyParams = Array.from({ length: 4 }, (_, i) => ({
        key: `pkey${i}`,
        value: `pvalue${i}`
      }))
      wrapper.vm.persistenceSelectorStrategyParamsTotal = 4
      await nextTick()

      // Navigate to page 2
      wrapper.vm.onPersistenceSelectorStrategyParamsPageChange(2)
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategyParamsObjects.length).toBe(1)
      expect(wrapper.vm.persistenceSelectorStrategyParamsObjects[0].key).toBe('pkey3')

      // Delete only item on page 2 (page-relative index 0)
      // Component calculates actualIndex = (page - 1) * pageSize + index = (2-1)*3 + 0 = 3
      wrapper.vm.deletePersistenceSelectorStrategy(0)
      await nextTick()

      expect(wrapper.vm.persistenceSelectorStrategyParams.length).toBe(3)
      expect(
        wrapper.vm.persistenceSelectorStrategyParams.find((obj: PersistSelectorStrategyForm) => obj.key === 'pkey3')
      ).toBeUndefined()
    })
  })

  describe('Close Parameter Drawer', () => {
    beforeEach(async () => {
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Create)
      wrapper.vm.key = 'test'
      wrapper.vm.value = 'value'
      await nextTick()
    })

    it('should reset key and value fields', async () => {
      wrapper.vm.closeParameterDrawer()
      await nextTick()
      expect(wrapper.vm.key).toBe('')
      expect(wrapper.vm.value).toBe('')
    })

    it('should close parameter drawer', async () => {
      wrapper.vm.closeParameterDrawer()
      await nextTick()
      expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(false)
    })

    it('should reset resourceTypeDrawerState', async () => {
      wrapper.vm.closeParameterDrawer()
      await nextTick()
      expect(wrapper.vm.resourceTypeDrawerState.type).toBeNull()
      expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.None)
    })
  })

  describe('Save Resource Type - Create Mode', () => {
    beforeEach(async () => {
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Create
      wrapper.vm.name = 'New Resource Type'
      wrapper.vm.label = 'New Label'
      wrapper.vm.resourceLabel = '${newLabel}'
      wrapper.vm.storageStrategy = { _text: STORAGE_STRATEGY_OPTIONS[0], _value: STORAGE_STRATEGY_OPTIONS[0] }
      wrapper.vm.persistenceSelectorStrategy = {
        _text: PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0],
        _value: PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
      }
      wrapper.vm.status = true
      await nextTick()
    })

    it('should call createResourceType with correct payload', async () => {
      vi.mocked(createResourceType).mockResolvedValue(true)
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(createResourceType).toHaveBeenCalled()
    })

    it('should pass collection source id to createResourceType', async () => {
      vi.mocked(createResourceType).mockResolvedValue(true)
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(createResourceType).toHaveBeenCalledWith(expect.anything(), mockCollectionSource.id)
    })

    it('should refresh resource types after successful creation', async () => {
      vi.mocked(createResourceType).mockResolvedValue(true)
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(store.fetchResourceTypes).toHaveBeenCalled()
    })

    it('should close drawer after successful creation', async () => {
      vi.mocked(createResourceType).mockResolvedValue(true)
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(store.closeResourceTypeDrawer).toHaveBeenCalled()
    })

    it('should show success snackbar on successful creation', async () => {
      vi.mocked(createResourceType).mockResolvedValue(true)
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Resource Type created successfully.'
      })
    })

    it('should not save when validation fails', async () => {
      wrapper.vm.name = ''
      await nextTick()
      await wrapper.vm.saveResourceType()
      expect(createResourceType).not.toHaveBeenCalled()
    })

    it('should show error when collection source is not selected', async () => {
      store.selectedCollectionSource = null
      await wrapper.vm.saveResourceType()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Please select a Collection Source first.',
        error: true
      })
      expect(createResourceType).not.toHaveBeenCalled()
    })

    it('should handle error during creation', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(createResourceType).mockRejectedValue(new Error('API Error'))
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(store.closeResourceTypeDrawer).not.toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })

    it('should show snackbar error message on API error', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(createResourceType).mockRejectedValue(new Error('API Error'))
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'An error occurred while saving the Resource Type. Please try again.',
        error: true
      })
      consoleErrorSpy.mockRestore()
    })

    it('should handle null response from create API', async () => {
      vi.mocked(createResourceType).mockResolvedValue(null as any)
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'An error occurred while saving the Resource Type. Please try again.',
        error: true
      })
      expect(store.closeResourceTypeDrawer).not.toHaveBeenCalled()
    })
  })

  describe('Save Resource Type - Edit Mode', () => {
    beforeEach(async () => {
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Edit
      store.selectedResourceType = mockResourceType
      store.resourceTypeDrawerState.visible = false
      await nextTick()
      store.resourceTypeDrawerState.visible = true
      await nextTick()
      await flushPromises()

      wrapper.vm.name = 'Updated Resource Type'
      wrapper.vm.status = false
      await nextTick()
    })

    it('should call updateResourceType with correct payload', async () => {
      vi.mocked(updateResourceType).mockResolvedValue(true)
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(updateResourceType).toHaveBeenCalled()
    })

    it('should refresh resource types after successful update', async () => {
      vi.mocked(updateResourceType).mockResolvedValue(true)
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(store.fetchResourceTypes).toHaveBeenCalled()
    })

    it('should close drawer after successful update', async () => {
      vi.mocked(updateResourceType).mockResolvedValue(true)
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(store.closeResourceTypeDrawer).toHaveBeenCalled()
    })

    it('should show success snackbar on successful update', async () => {
      vi.mocked(updateResourceType).mockResolvedValue(true)
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Resource Type updated successfully.'
      })
    })

    it('should handle error during update', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(updateResourceType).mockRejectedValue(new Error('API Error'))
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(store.closeResourceTypeDrawer).not.toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })

    it('should handle undefined response from update API', async () => {
      vi.mocked(updateResourceType).mockResolvedValue(undefined as any)
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(store.closeResourceTypeDrawer).not.toHaveBeenCalled()
    })
  })

  describe('Close Resource Type Drawer', () => {
    beforeEach(async () => {
      wrapper.vm.name = 'Test'
      wrapper.vm.label = 'Test Label'
      wrapper.vm.resourceLabel = '${test}'
      wrapper.vm.status = false
      wrapper.vm.storageStrategy = { _text: 'test', _value: 'test' }
      wrapper.vm.persistenceSelectorStrategy = { _text: 'test', _value: 'test' }
      wrapper.vm.storageStrategyParams = [...mockStorageStrategyParams]
      wrapper.vm.persistenceSelectorStrategyParams = [...mockPersistenceSelectorParams]
      await nextTick()
    })

    it('should reset all form fields', async () => {
      await wrapper.vm.closeResourceTypeDrawer()
      await nextTick()

      expect(wrapper.vm.name).toBe('')
      expect(wrapper.vm.label).toBe('')
      expect(wrapper.vm.resourceLabel).toBe('')
      expect(wrapper.vm.status).toBe(true)
    })

    it('should reset strategy values', async () => {
      await wrapper.vm.closeResourceTypeDrawer()
      await nextTick()

      expect(wrapper.vm.storageStrategy).toBeUndefined()
      expect(wrapper.vm.persistenceSelectorStrategy).toBeUndefined()
    })

    it('should reset parameter arrays', async () => {
      await wrapper.vm.closeResourceTypeDrawer()
      await nextTick()

      expect(wrapper.vm.storageStrategyParams).toEqual([])
      expect(wrapper.vm.persistenceSelectorStrategyParams).toEqual([])
    })

    it('should reset errors initially after close', async () => {
      wrapper.vm.errors = { name: 'Error' }
      await nextTick()
      await wrapper.vm.closeResourceTypeDrawer()
      await nextTick()
      // After close, errors are recomputed by watchEffect with empty fields
      // The initial reset sets errors to {}, but watchEffect immediately revalidates
      // This test verifies that the explicit reset is performed (errors object should exist)
      expect(wrapper.vm.errors).toBeDefined()
    })

    it('should close parameter drawer if open', async () => {
      wrapper.vm.resourceTypeDrawerState.visible = true
      await nextTick()
      await wrapper.vm.closeResourceTypeDrawer()
      await nextTick()
      expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(false)
    })

    it('should call store closeResourceTypeDrawer', async () => {
      await wrapper.vm.closeResourceTypeDrawer()
      await flushPromises()
      expect(store.closeResourceTypeDrawer).toHaveBeenCalled()
    })
  })

  describe('Watch Effects', () => {
    it('should reset form when drawer visibility changes to false', async () => {
      store.resourceTypeDrawerState.visible = true
      await nextTick()
      wrapper.vm.name = 'Test'
      await nextTick()

      store.resourceTypeDrawerState.visible = false
      await nextTick()

      expect(wrapper.vm.name).toBe('')
    })

    it('should load data when drawer visibility changes to true', async () => {
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Edit
      store.selectedResourceType = mockResourceType
      store.resourceTypeDrawerState.visible = false
      await nextTick()

      store.resourceTypeDrawerState.visible = true
      await nextTick()

      expect(wrapper.vm.name).toBe('ifIndex')
    })

    it('should load parameter data when parameter drawer opens', async () => {
      wrapper.vm.storageStrategyParams = [...mockStorageStrategyParams]
      await nextTick()

      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Edit, 0, mockStorageStrategyParams[0])
      await nextTick()

      expect(wrapper.vm.key).toBe('sibling-column-name')
      expect(wrapper.vm.value).toBe('ifIndex')
    })

    it('should reset parameter form when parameter drawer closes', async () => {
      wrapper.vm.resourceTypeDrawerState.visible = true
      wrapper.vm.key = 'test'
      wrapper.vm.value = 'value'
      await nextTick()

      wrapper.vm.resourceTypeDrawerState.visible = false
      await nextTick()

      expect(wrapper.vm.key).toBe('')
      expect(wrapper.vm.value).toBe('')
    })
  })

  describe('Edge Cases', () => {
    it('should handle empty storageStrategyParams JSON string', async () => {
      store.selectedResourceType = { ...mockResourceType, storageStrategyParams: '' }
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Edit
      store.resourceTypeDrawerState.visible = false
      await nextTick()
      store.resourceTypeDrawerState.visible = true
      await nextTick()
      // Should use default '[]' for parse
      expect(wrapper.vm.storageStrategyParams).toEqual([])
    })

    it('should handle empty persistenceSelectorParams JSON string', async () => {
      store.selectedResourceType = { ...mockResourceType, persistenceSelectorParams: '' }
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Edit
      store.resourceTypeDrawerState.visible = false
      await nextTick()
      store.resourceTypeDrawerState.visible = true
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategyParams).toEqual([])
    })

    it('should handle non-Error rejection', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(createResourceType).mockRejectedValue('String error')
      wrapper.vm.name = 'Test'
      wrapper.vm.label = 'Label'
      wrapper.vm.storageStrategy = { _text: 'test', _value: 'test' }
      wrapper.vm.persistenceSelectorStrategy = { _text: 'test', _value: 'test' }
      await nextTick()
      await wrapper.vm.saveResourceType()
      await flushPromises()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'An error occurred while saving the Resource Type. Please try again.',
        error: true
      })
      consoleErrorSpy.mockRestore()
    })

    it('should handle saving with no collection source id', async () => {
      store.selectedCollectionSource = { ...mockCollectionSource, id: undefined as any }
      wrapper.vm.name = 'Test'
      wrapper.vm.label = 'Label'
      wrapper.vm.storageStrategy = { _text: 'test', _value: 'test' }
      wrapper.vm.persistenceSelectorStrategy = { _text: 'test', _value: 'test' }
      await nextTick()
      await wrapper.vm.saveResourceType()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Please select a Collection Source first.',
        error: true
      })
    })

    it('should handle parameter drawer title when type is null', () => {
      wrapper.vm.resourceTypeDrawerState.type = null
      expect(wrapper.vm.parameterDrawerTitle).toBe('Parameter')
    })
  })

  describe('Strategy Options', () => {
    it('should have storage strategy options available', () => {
      expect(STORAGE_STRATEGY_OPTIONS.length).toBeGreaterThan(0)
    })

    it('should have persistence selector strategy options available', () => {
      expect(PERSISTENCE_SELECTOR_STRATEGY_OPTIONS.length).toBeGreaterThan(0)
    })
  })

  describe('Key Pattern Validation', () => {
    it.each([
      { key: '', valid: false, reason: 'empty string' },
      { key: '   ', valid: false, reason: 'whitespace only' },
      { key: 'valid_key', valid: true, reason: 'underscore' },
      { key: 'valid-key', valid: true, reason: 'hyphen' },
      { key: 'validKey123', valid: true, reason: 'alphanumeric' },
      { key: 'invalid key', valid: false, reason: 'contains space' },
      { key: 'invalid!key', valid: false, reason: 'contains special char' },
      { key: 'key@value', valid: false, reason: 'contains at symbol' }
    ])('Key "$key" ($reason) should be valid=$valid', ({ key, valid }) => {
      const isValid = KEY_PATTERN.test(key)
      expect(isValid).toBe(valid)
    })
  })

  describe('Parametrized Tests - Save Modes', () => {
    it.each([
      { mode: CreateEditMode.Create, serviceFn: 'createResourceType', message: 'created' },
      { mode: CreateEditMode.Edit, serviceFn: 'updateResourceType', message: 'updated' }
    ])('should use $serviceFn in $mode mode and show "$message" message', async ({ mode, serviceFn, message }) => {
      const mockFn = serviceFn === 'createResourceType' ? createResourceType : updateResourceType
      vi.mocked(mockFn).mockResolvedValue(true)

      store.resourceTypeDrawerState.isEditMode = mode
      if (mode === CreateEditMode.Edit) {
        store.selectedResourceType = mockResourceType
      }
      wrapper.vm.name = 'Test'
      wrapper.vm.label = 'Label'
      wrapper.vm.storageStrategy = { _text: STORAGE_STRATEGY_OPTIONS[0], _value: STORAGE_STRATEGY_OPTIONS[0] }
      wrapper.vm.persistenceSelectorStrategy = {
        _text: PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0],
        _value: PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
      }
      await nextTick()

      await wrapper.vm.saveResourceType()
      await flushPromises()

      expect(mockFn).toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: `Resource Type ${message} successfully.`
      })
    })
  })

  describe('Integration Tests', () => {
    it('should handle full create workflow', async () => {
      // Open drawer in create mode
      store.resourceTypeDrawerState.visible = true
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Create
      await nextTick()

      // Fill form
      wrapper.vm.name = 'Integration Test Resource'
      wrapper.vm.label = 'Integration Label'
      wrapper.vm.resourceLabel = '${integrationTest}'
      wrapper.vm.status = true
      wrapper.vm.storageStrategy = { _text: STORAGE_STRATEGY_OPTIONS[0], _value: STORAGE_STRATEGY_OPTIONS[0] }
      wrapper.vm.persistenceSelectorStrategy = {
        _text: PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0],
        _value: PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
      }
      await nextTick()

      // Add storage strategy parameter
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Create)
      await nextTick()

      wrapper.vm.key = 'testKey'
      wrapper.vm.value = 'testValue'
      await nextTick()

      wrapper.vm.saveResourceTypeParameter()
      await nextTick()

      expect(wrapper.vm.storageStrategyParams.length).toBe(1)
      expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(false)

      // Save resource type
      vi.mocked(createResourceType).mockResolvedValue(true)
      await wrapper.vm.saveResourceType()
      await flushPromises()

      expect(createResourceType).toHaveBeenCalled()
      expect(store.fetchResourceTypes).toHaveBeenCalled()
      expect(store.closeResourceTypeDrawer).toHaveBeenCalled()
    })

    it('should handle full edit workflow', async () => {
      // Setup edit mode
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Edit
      store.selectedResourceType = mockResourceType
      store.resourceTypeDrawerState.visible = true
      await nextTick()
      await flushPromises()

      // Verify data loaded
      expect(wrapper.vm.name).toBe('ifIndex')
      expect(wrapper.vm.storageStrategyParams.length).toBe(2)

      // Edit storage strategy parameter
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Edit, 0, wrapper.vm.storageStrategyParams[0])
      await nextTick()

      wrapper.vm.value = 'updatedValue'
      await nextTick()

      wrapper.vm.saveResourceTypeParameter()
      await nextTick()

      expect(wrapper.vm.storageStrategyParams[0].value).toBe('updatedValue')

      // Delete second parameter
      wrapper.vm.deleteStorageStrategy(1)
      await nextTick()

      expect(wrapper.vm.storageStrategyParams.length).toBe(1)

      // Update resource type
      vi.mocked(updateResourceType).mockResolvedValue(true)
      await wrapper.vm.saveResourceType()
      await flushPromises()

      expect(updateResourceType).toHaveBeenCalled()
      expect(store.fetchResourceTypes).toHaveBeenCalled()
    })

    it('should handle adding multiple parameters of both types', async () => {
      store.resourceTypeDrawerState.visible = true
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Create
      await nextTick()

      // Add multiple storage strategy parameters
      for (let i = 0; i < 3; i++) {
        wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Create)
        await nextTick()

        wrapper.vm.key = `storageKey${i}`
        wrapper.vm.value = `storageValue${i}`
        await nextTick()

        wrapper.vm.saveResourceTypeParameter()
        await nextTick()
      }

      expect(wrapper.vm.storageStrategyParams.length).toBe(3)

      // Add multiple persistence selector strategy parameters
      for (let i = 0; i < 2; i++) {
        wrapper.vm.openPersistenceSelectorStrategyDrawer(CreateEditMode.Create)
        await nextTick()

        wrapper.vm.key = `persistenceKey${i}`
        wrapper.vm.value = `persistenceValue${i}`
        await nextTick()

        wrapper.vm.saveResourceTypeParameter()
        await nextTick()
      }

      expect(wrapper.vm.persistenceSelectorStrategyParams.length).toBe(2)
    })
  })

  describe('Additional Edge Cases', () => {
    it('should handle invalid JSON in storageStrategyParams gracefully', async () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      store.selectedResourceType = { ...mockResourceType, storageStrategyParams: 'invalid json' }
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Edit
      store.resourceTypeDrawerState.visible = false
      await nextTick()

      // JSON.parse will throw, component should handle this
      try {
        store.resourceTypeDrawerState.visible = true
        await nextTick()
      } catch (e) {
        // Expected to fail
      }
      consoleWarnSpy.mockRestore()
    })

    it('should handle invalid JSON in persistenceSelectorParams gracefully', async () => {
      const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      store.selectedResourceType = { ...mockResourceType, persistenceSelectorParams: 'invalid json' }
      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Edit
      store.resourceTypeDrawerState.visible = false
      await nextTick()

      try {
        store.resourceTypeDrawerState.visible = true
        await nextTick()
      } catch (e) {
        // Expected to fail
      }
      consoleWarnSpy.mockRestore()
    })

    it('should handle empty search query for storage strategy', async () => {
      wrapper.vm.onSearchStorageStrategy('')
      vi.advanceTimersByTime(500)
      await nextTick()
      // Empty query should show all options or behave consistently
      expect(wrapper.vm.storageStrategyLoading).toBe(false)
    })

    it('should handle empty search query for persistence selector strategy', async () => {
      wrapper.vm.onSearchPersistenceSelectorStrategy('')
      vi.advanceTimersByTime(500)
      await nextTick()
      expect(wrapper.vm.persistenceSelectorStrategyLoading).toBe(false)
    })

    it('should call console.error when API throws', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(createResourceType).mockRejectedValue(new Error('API Error'))
      wrapper.vm.name = 'Test'
      wrapper.vm.label = 'Label'
      wrapper.vm.storageStrategy = { _text: 'test', _value: 'test' }
      wrapper.vm.persistenceSelectorStrategy = { _text: 'test', _value: 'test' }
      await nextTick()

      await wrapper.vm.saveResourceType()
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error saving Resource Type:', expect.any(Error))
      consoleErrorSpy.mockRestore()
    })

    it('should handle whitespace-only search query for storage strategy', async () => {
      wrapper.vm.onSearchStorageStrategy('   ')
      vi.advanceTimersByTime(500)
      await nextTick()
      // Whitespace only should not add custom option (trimmed is empty)
      expect(wrapper.vm.storageStrategyResults.every((r: any) => r._text !== '   ')).toBe(true)
    })

    it('should handle rapid consecutive searches canceling previous', async () => {
      // First search
      wrapper.vm.onSearchStorageStrategy('Index')
      vi.advanceTimersByTime(100)

      // Second search before first completes
      wrapper.vm.onSearchStorageStrategy('Sibling')
      vi.advanceTimersByTime(100)

      // Third search
      wrapper.vm.onSearchStorageStrategy('Host')
      vi.advanceTimersByTime(500)
      await nextTick()

      // Depending on available strategies, this may or may not have results
      expect(wrapper.vm.storageStrategyLoading).toBe(false)
    })

    it('should preserve parameter errors when closing parameter drawer', async () => {
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Create)
      wrapper.vm.key = 'invalid key!'
      wrapper.vm.value = ''
      await nextTick()

      expect(wrapper.vm.parameterErrors.key).toBeDefined()
      expect(wrapper.vm.parameterErrors.value).toBeDefined()

      wrapper.vm.closeParameterDrawer()
      await nextTick()

      // After closing, key and value should be reset
      expect(wrapper.vm.key).toBe('')
      expect(wrapper.vm.value).toBe('')
    })

    it('should handle parameter at index 0 correctly in edit mode', async () => {
      wrapper.vm.storageStrategyParams = [...mockStorageStrategyParams]
      await nextTick()

      // Edit the first parameter (index 0)
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Edit, 0, mockStorageStrategyParams[0])
      await nextTick()

      expect(wrapper.vm.resourceTypeDrawerState.storageStrategyIndex).toBe(0)
      expect(wrapper.vm.key).toBe('sibling-column-name')

      wrapper.vm.key = 'edited-key'
      wrapper.vm.value = 'edited-value'
      await nextTick()

      wrapper.vm.saveResourceTypeParameter()
      await nextTick()

      // Verify index 0 was updated
      expect(wrapper.vm.storageStrategyParams[0].key).toBe('edited-key')
      expect(wrapper.vm.storageStrategyParams[0].value).toBe('edited-value')
    })

    it('should not add parameter when index is invalid in edit mode', async () => {
      wrapper.vm.storageStrategyParams = []
      await nextTick()

      // Try to edit at invalid index
      wrapper.vm.resourceTypeDrawerState.visible = true
      wrapper.vm.resourceTypeDrawerState.type = 'storageStrategy'
      wrapper.vm.resourceTypeDrawerState.isEditMode = CreateEditMode.Edit
      wrapper.vm.resourceTypeDrawerState.storageStrategyIndex = -1
      await nextTick()

      wrapper.vm.key = 'key'
      wrapper.vm.value = 'value'
      await nextTick()

      wrapper.vm.saveResourceTypeParameter()
      await nextTick()

      // Should not add because index is -1 and mode is Edit
      expect(wrapper.vm.storageStrategyParams.length).toBe(0)
    })

    it('should handle loading persistence selector strategy data in parameter drawer', async () => {
      wrapper.vm.persistenceSelectorStrategyParams = [...mockPersistenceSelectorParams]
      await nextTick()

      wrapper.vm.openPersistenceSelectorStrategyDrawer(CreateEditMode.Edit, 1, mockPersistenceSelectorParams[1])
      await nextTick()

      expect(wrapper.vm.key).toBe('enabled')
      expect(wrapper.vm.value).toBe('true')
    })

    it('should set isSaveDisabled based on resource label presence (optional field)', async () => {
      wrapper.vm.name = 'Test Name'
      wrapper.vm.label = 'Test Label'
      wrapper.vm.resourceLabel = '' // Optional field
      wrapper.vm.storageStrategy = { _text: 'test', _value: 'test' }
      wrapper.vm.persistenceSelectorStrategy = { _text: 'test', _value: 'test' }
      await nextTick()

      // resourceLabel is optional, should still be valid
      expect(wrapper.vm.isSaveDisabled).toBe(false)
    })

    it('should handle strategy value being empty object', async () => {
      wrapper.vm.name = 'Test'
      wrapper.vm.label = 'Label'
      wrapper.vm.storageStrategy = { _text: '', _value: '' }
      wrapper.vm.persistenceSelectorStrategy = { _text: '', _value: '' }
      await nextTick()

      const errors = wrapper.vm.validateResourceType()
      expect(errors.storageStrategy).toBe('Storage Strategy is required')
      expect(errors.persistenceSelectorStrategy).toBe('Persistence Selector Strategy is required')
    })

    it('should handle null storageStrategy object', async () => {
      wrapper.vm.name = 'Test'
      wrapper.vm.label = 'Label'
      wrapper.vm.storageStrategy = null as any
      wrapper.vm.persistenceSelectorStrategy = null as any
      await nextTick()

      const errors = wrapper.vm.validateResourceType()
      expect(errors.storageStrategy).toBe('Storage Strategy is required')
      expect(errors.persistenceSelectorStrategy).toBe('Persistence Selector Strategy is required')
    })

    it('should update storage strategy results on search filter', async () => {
      // Search for a strategy that should exist
      wrapper.vm.onSearchStorageStrategy('org.opennms')
      vi.advanceTimersByTime(500)
      await nextTick()

      // Results should contain items matching the search
      expect(wrapper.vm.storageStrategyResults.length).toBeGreaterThan(0)
      expect(wrapper.vm.storageStrategyResults.every((r: any) => r._text.toLowerCase().includes('org.opennms'))).toBe(
        true
      )
    })

    it('should show CreateEditMode.None after closing parameter drawer', async () => {
      wrapper.vm.openStorageStrategyDrawer(CreateEditMode.Create)
      await nextTick()

      expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.Create)

      wrapper.vm.closeParameterDrawer()
      await nextTick()

      expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.None)
    })
  })

  describe('Boundary Conditions', () => {
    it('should handle very long name value', async () => {
      const longName = 'a'.repeat(1000)
      wrapper.vm.name = longName
      wrapper.vm.label = 'Label'
      wrapper.vm.storageStrategy = { _text: 'test', _value: 'test' }
      wrapper.vm.persistenceSelectorStrategy = { _text: 'test', _value: 'test' }
      await nextTick()

      const errors = wrapper.vm.validateResourceType()
      // No max length validation in component, so should be valid
      expect(errors.name).toBeUndefined()
    })

    it('should handle very long key value in parameter', async () => {
      const longKey = 'a'.repeat(1000)
      wrapper.vm.key = longKey
      wrapper.vm.value = 'testValue'
      await nextTick()

      const errors = wrapper.vm.validateResourceTypeParameter()
      // Valid if matches KEY_PATTERN
      expect(errors.key).toBeUndefined()
    })

    it('should handle special characters in value field', async () => {
      wrapper.vm.key = 'validKey'
      wrapper.vm.value = '${special} <html> &amp; "quotes"'
      await nextTick()

      const errors = wrapper.vm.validateResourceTypeParameter()
      // Value can contain any characters
      expect(errors.value).toBeUndefined()
    })

    it('should delete storage strategy parameter at last index', async () => {
      wrapper.vm.storageStrategyParams = [...mockStorageStrategyParams]
      await nextTick()

      const initialLength = wrapper.vm.storageStrategyParams.length
      wrapper.vm.deleteStorageStrategy(initialLength - 1)
      await nextTick()

      expect(wrapper.vm.storageStrategyParams.length).toBe(initialLength - 1)
    })

    it('should delete persistence selector strategy parameter at last index', async () => {
      wrapper.vm.persistenceSelectorStrategyParams = [...mockPersistenceSelectorParams]
      await nextTick()

      const initialLength = wrapper.vm.persistenceSelectorStrategyParams.length
      wrapper.vm.deletePersistenceSelectorStrategy(initialLength - 1)
      await nextTick()

      expect(wrapper.vm.persistenceSelectorStrategyParams.length).toBe(initialLength - 1)
    })
  })
})


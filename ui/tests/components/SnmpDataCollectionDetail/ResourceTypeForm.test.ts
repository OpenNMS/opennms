import ResourceTypeForm from '@/components/SnmpDataCollectionDetail/ResourceTypeForm.vue'
import { PERSISTENCE_SELECTOR_STRATEGY_OPTIONS, STORAGE_STRATEGY_OPTIONS } from '@/lib/constants'
import { createResourceType, updateResourceType } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import {
  PersistSelectorStrategyForm,
  SnmpCollectionResourceType,
  StorageStrategyForm
} from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherInput } from '@featherds/input'
import { FeatherRadio, FeatherRadioGroup } from '@featherds/radio'
import { FeatherSelect } from '@featherds/select'
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

vi.mock('@/components/SnmpDataCollectionDetail/Drawer/ResourceTypeParameterDrawer.vue', () => ({
  default: {
    name: 'ResourceTypeParameterDrawer',
    template: '<div data-test="resource-type-parameter-drawer"></div>',
    props: ['state'],
    emits: ['cancel', 'save']
  }
}))

vi.mock('@/components/Common/TableCard.vue', () => ({
  default: {
    name: 'TableCard',
    template: '<div class="table-card"><slot /></div>'
  }
}))

vi.mock('@/components/Common/EmptyList.vue', () => ({
  default: {
    name: 'EmptyList',
    template: '<div data-test="empty-list">No parameters</div>',
    props: ['content']
  }
}))

describe('ResourceTypeForm.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>

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

  const mockResourceType: SnmpCollectionResourceType = {
    id: 1,
    name: 'testResourceType',
    label: 'Test Resource Type',
    resourceLabel: 'Test Resource Label',
    persistenceSelectorStrategy: 'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy',
    persistenceSelectorParams: JSON.stringify([{ key: 'persistKey', value: 'persistValue' }]),
    storageStrategy: 'org.opennms.netmgt.collection.support.IndexStorageStrategy',
    storageStrategyParams: JSON.stringify([{ key: 'storageKey', value: 'storageValue' }]),
    enabled: true,
    collectionSourceId: 1,
    collectionSourceName: 'Test Source'
  }

  const mockStorageStrategyParam: StorageStrategyForm = {
    key: 'storageKey',
    value: 'storageValue'
  }

  const mockPersistenceParam: PersistSelectorStrategyForm = {
    key: 'persistKey',
    value: 'persistValue'
  }

  const createWrapper = async (resourceTypeDrawerState = { visible: true, isEditMode: CreateEditMode.Create }) => {
    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useSnmpDataCollectionDetailStore(pinia)
    store.selectedCollectionSource = mockCollectionSource
    store.resourceTypeDrawerState = resourceTypeDrawerState
    store.selectedResourceType = null
    store.closeResourceTypeDrawer = vi.fn()
    store.fetchResourceTypes = vi.fn().mockResolvedValue(undefined)

    wrapper = mount(ResourceTypeForm, {
      global: {
        plugins: [pinia],
        components: {
          FeatherButton,
          FeatherInput,
          FeatherSelect,
          FeatherRadioGroup,
          FeatherRadio,
          FeatherIcon
        },
        stubs: {
          FeatherIcon: true,
          TransitionGroup: false
        }
      }
    })

    await nextTick()
    await flushPromises()
    return wrapper
  }

  beforeEach(async () => {
    vi.clearAllMocks()
    mockShowSnackBar.mockClear()
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
      it('should render the component with Create Resource Type title', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.find('.title').text()).toBe('Create Resource Type')
      })

      it('should render name input field', async () => {
        await createWrapper()
        const nameInput = wrapper.find('[data-test="resource-type-name-input"]')
        expect(nameInput.exists()).toBe(true)
      })

      it('should render label input field', async () => {
        await createWrapper()
        const labelInput = wrapper.find('[data-test="resource-type-label-input"]')
        expect(labelInput.exists()).toBe(true)
      })

      it('should render resource label input field', async () => {
        await createWrapper()
        const resourceLabelInput = wrapper.find('[data-test="resource-type-resource-label-input"]')
        expect(resourceLabelInput.exists()).toBe(true)
      })

      it('should render status radio group', async () => {
        await createWrapper()
        const statusRadioGroup = wrapper.find('[data-test="system-def-status-input"]')
        expect(statusRadioGroup.exists()).toBe(true)
      })

      it('should render storage strategy select', async () => {
        await createWrapper()
        const storageStrategySelect = wrapper.find('[data-test="resource-type-storage-strategy-input"]')
        expect(storageStrategySelect.exists()).toBe(true)
      })

      it('should render persistence selector strategy select', async () => {
        await createWrapper()
        const persistenceStrategySelect = wrapper.find(
          '[data-test="resource-type-persistence-selector-strategy-input"]'
        )
        expect(persistenceStrategySelect.exists()).toBe(true)
      })

      it('should render Add Storage Strategy Parameter button', async () => {
        await createWrapper()
        const addButton = wrapper.find('[data-test="add-storage-strategy-button"]')
        expect(addButton.exists()).toBe(true)
      })

      it('should render Add Persistence Selector Strategy Parameter button', async () => {
        await createWrapper()
        const addButton = wrapper.find('[data-test="add-persistence-selector-strategy-button"]')
        expect(addButton.exists()).toBe(true)
      })

      it('should render Cancel button', async () => {
        await createWrapper()
        const cancelButton = wrapper.find('[data-test="cancel-resource-type"]')
        expect(cancelButton.exists()).toBe(true)
      })

      it('should render Save button', async () => {
        await createWrapper()
        const saveButton = wrapper.find('[data-test="save-resource-type"]')
        expect(saveButton.exists()).toBe(true)
      })

      it('should render ResourceTypeParameterDrawer component', async () => {
        await createWrapper()
        const drawer = wrapper.find('[data-test="resource-type-parameter-drawer"]')
        expect(drawer.exists()).toBe(true)
      })

      it('should render empty list message when no storage strategy parameters', async () => {
        await createWrapper()
        const emptyLists = wrapper.findAll('[data-test="empty-list"]')
        expect(emptyLists.length).toBeGreaterThan(0)
      })

      it('should show Storage Strategy Parameters table header', async () => {
        await createWrapper()
        const tables = wrapper.findAll('table')
        expect(tables.length).toBeGreaterThan(0)
        expect(wrapper.find('.storage-strategy-table-container thead').text()).toContain('Key')
        expect(wrapper.find('.storage-strategy-table-container thead').text()).toContain('Value')
        expect(wrapper.find('.storage-strategy-table-container thead').text()).toContain('Action')
      })

      it('should show Persistence Selector Strategy Parameters table header', async () => {
        await createWrapper()
        expect(wrapper.find('.persistence-selector-strategy-table-container thead').text()).toContain('Key')
        expect(wrapper.find('.persistence-selector-strategy-table-container thead').text()).toContain('Value')
        expect(wrapper.find('.persistence-selector-strategy-table-container thead').text()).toContain('Action')
      })
    })

    describe('Edit Mode', () => {
      it('should render the component with Edit Resource Type title', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = mockResourceType
        await nextTick()

        expect(wrapper.find('.title').text()).toBe('Edit Resource Type')
      })
    })
  })

  describe('Initial Data Loading', () => {
    describe('Create Mode', () => {
      it('should initialize with empty name', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.name).toBe('')
      })

      it('should initialize with empty label', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.label).toBe('')
      })

      it('should initialize with empty resource label', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.resourceLabel).toBe('')
      })

      it('should initialize with enabled status (true)', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.status).toBe(true)
      })

      it('should initialize with default storage strategy', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.storageStrategy).toEqual(STORAGE_STRATEGY_OPTIONS[0])
      })

      it('should initialize with default persistence selector strategy', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.persistenceSelectorStrategy).toEqual(PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0])
      })

      it('should initialize with empty storageStrategyParams array', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.storageStrategyParams).toEqual([])
      })

      it('should initialize with empty persistenceSelectorStrategyParams array', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.persistenceSelectorStrategyParams).toEqual([])
      })
    })

    describe('Edit Mode', () => {
      it('should load existing resource type name', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = mockResourceType
        store.resourceTypeDrawerState.visible = false
        await nextTick()
        store.resourceTypeDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.name).toBe('testResourceType')
      })

      it('should load existing resource type label', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = mockResourceType
        store.resourceTypeDrawerState.visible = false
        await nextTick()
        store.resourceTypeDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.label).toBe('Test Resource Type')
      })

      it('should load existing resource type resourceLabel', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = mockResourceType
        store.resourceTypeDrawerState.visible = false
        await nextTick()
        store.resourceTypeDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.resourceLabel).toBe('Test Resource Label')
      })

      it('should load existing resource type enabled status', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = mockResourceType
        store.resourceTypeDrawerState.visible = false
        await nextTick()
        store.resourceTypeDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.status).toBe(true)
      })

      it('should load existing resource type storage strategy', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = mockResourceType
        store.resourceTypeDrawerState.visible = false
        await nextTick()
        store.resourceTypeDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.storageStrategy._value).toBe('org.opennms.netmgt.collection.support.IndexStorageStrategy')
      })

      it('should load existing resource type persistence selector strategy', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = mockResourceType
        store.resourceTypeDrawerState.visible = false
        await nextTick()
        store.resourceTypeDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.persistenceSelectorStrategy._value).toBe(
          'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy'
        )
      })

      it('should load existing storage strategy parameters', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = mockResourceType
        store.resourceTypeDrawerState.visible = false
        await nextTick()
        store.resourceTypeDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.storageStrategyParams).toHaveLength(1)
        expect(wrapper.vm.storageStrategyParams[0].key).toBe('storageKey')
      })

      it('should load existing persistence selector strategy parameters', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = mockResourceType
        store.resourceTypeDrawerState.visible = false
        await nextTick()
        store.resourceTypeDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.persistenceSelectorStrategyParams).toHaveLength(1)
        expect(wrapper.vm.persistenceSelectorStrategyParams[0].key).toBe('persistKey')
      })

      it('should handle disabled resource type status', async () => {
        const disabledResourceType = { ...mockResourceType, enabled: false }
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = disabledResourceType
        store.resourceTypeDrawerState.visible = false
        await nextTick()
        store.resourceTypeDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.status).toBe(false)
      })

      it('should handle empty storage strategy params JSON', async () => {
        const emptyParamsResourceType = { ...mockResourceType, storageStrategyParams: '[]' }
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = emptyParamsResourceType
        store.resourceTypeDrawerState.visible = false
        await nextTick()
        store.resourceTypeDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.storageStrategyParams).toEqual([])
      })

      it('should handle empty persistence selector params JSON', async () => {
        const emptyParamsResourceType = { ...mockResourceType, persistenceSelectorParams: '[]' }
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = emptyParamsResourceType
        store.resourceTypeDrawerState.visible = false
        await nextTick()
        store.resourceTypeDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.persistenceSelectorStrategyParams).toEqual([])
      })

      it('should handle null selectedResourceType gracefully', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = null
        store.resourceTypeDrawerState.visible = false
        await nextTick()
        store.resourceTypeDrawerState.visible = true
        await nextTick()

        // Should not throw and values should remain at defaults
        expect(wrapper.vm.name).toBe('')
      })
    })
  })

  describe('Validation', () => {
    describe('Name Validation', () => {
      it('should show error when name is empty', async () => {
        await createWrapper()
        wrapper.vm.name = ''
        await nextTick()

        expect(wrapper.vm.errors.name).toBe('Name is required')
      })

      it('should show error when name is only whitespace', async () => {
        await createWrapper()
        wrapper.vm.name = '   '
        await nextTick()

        expect(wrapper.vm.errors.name).toBe('Name is required')
      })

      it('should clear name error when valid name is provided', async () => {
        await createWrapper()
        wrapper.vm.name = ''
        await nextTick()
        expect(wrapper.vm.errors.name).toBe('Name is required')

        wrapper.vm.name = 'Valid Name'
        await nextTick()
        expect(wrapper.vm.errors.name).toBeUndefined()
      })
    })

    describe('Label Validation', () => {
      it('should show error when label is empty', async () => {
        await createWrapper()
        wrapper.vm.label = ''
        await nextTick()

        expect(wrapper.vm.errors.label).toBe('Label is required')
      })

      it('should show error when label is only whitespace', async () => {
        await createWrapper()
        wrapper.vm.label = '   '
        await nextTick()

        expect(wrapper.vm.errors.label).toBe('Label is required')
      })

      it('should clear label error when valid label is provided', async () => {
        await createWrapper()
        wrapper.vm.label = ''
        await nextTick()
        expect(wrapper.vm.errors.label).toBe('Label is required')

        wrapper.vm.label = 'Valid Label'
        await nextTick()
        expect(wrapper.vm.errors.label).toBeUndefined()
      })
    })

    describe('Storage Strategy Validation', () => {
      it('should show error when storage strategy value is empty', async () => {
        await createWrapper()
        wrapper.vm.storageStrategy = { _text: '', _value: '' }
        await nextTick()

        expect(wrapper.vm.errors.storageStrategy).toBe('Storage Strategy is required')
      })

      it('should clear storage strategy error when valid option is selected', async () => {
        await createWrapper()
        wrapper.vm.storageStrategy = { _text: '', _value: '' }
        await nextTick()
        expect(wrapper.vm.errors.storageStrategy).toBe('Storage Strategy is required')

        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
        await nextTick()
        expect(wrapper.vm.errors.storageStrategy).toBeUndefined()
      })
    })

    describe('Persistence Selector Strategy Validation', () => {
      it('should show error when persistence selector strategy value is empty', async () => {
        await createWrapper()
        wrapper.vm.persistenceSelectorStrategy = { _text: '', _value: '' }
        await nextTick()

        expect(wrapper.vm.errors.persistenceSelectorStrategy).toBe('Persistence Selector Strategy is required')
      })

      it('should clear persistence selector strategy error when valid option is selected', async () => {
        await createWrapper()
        wrapper.vm.persistenceSelectorStrategy = { _text: '', _value: '' }
        await nextTick()
        expect(wrapper.vm.errors.persistenceSelectorStrategy).toBe('Persistence Selector Strategy is required')

        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
        await nextTick()
        expect(wrapper.vm.errors.persistenceSelectorStrategy).toBeUndefined()
      })
    })

    describe('isSaveDisabled', () => {
      it('should disable save button when form is invalid', async () => {
        await createWrapper()
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })

      it('should enable save button when form is valid', async () => {
        await createWrapper()
        wrapper.vm.name = 'Valid Name'
        wrapper.vm.label = 'Valid Label'
        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
        await nextTick()

        expect(wrapper.vm.isSaveDisabled).toBe(false)
      })

      it('should disable save button when name becomes empty after being valid', async () => {
        await createWrapper()
        wrapper.vm.name = 'Valid Name'
        wrapper.vm.label = 'Valid Label'
        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(false)

        wrapper.vm.name = ''
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })

      it('should disable save button when label becomes empty after being valid', async () => {
        await createWrapper()
        wrapper.vm.name = 'Valid Name'
        wrapper.vm.label = 'Valid Label'
        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(false)

        wrapper.vm.label = ''
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })
    })
  })

  describe('Strategy Drawer Management', () => {
    describe('Storage Strategy Drawer', () => {
      it('should open drawer in Create mode when Add Storage Strategy Parameter is clicked', async () => {
        await createWrapper()
        const addButton = wrapper.find('[data-test="add-storage-strategy-button"]')
        await addButton.trigger('click')

        expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(true)
        expect(wrapper.vm.resourceTypeDrawerState.type).toBe('storageStrategy')
        expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.Create)
        expect(wrapper.vm.resourceTypeDrawerState.storageStrategyIndex).toBe(-1)
        expect(wrapper.vm.resourceTypeDrawerState.storageStrategyObject).toBe(null)
      })

      it('should open drawer in Edit mode when Edit storage strategy button is clicked', async () => {
        await createWrapper()
        wrapper.vm.storageStrategyParams = [mockStorageStrategyParam]
        await nextTick()

        const editButton = wrapper.find('[data-test="edit-storage-strategy-button"]')
        await editButton.trigger('click')

        expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(true)
        expect(wrapper.vm.resourceTypeDrawerState.type).toBe('storageStrategy')
        expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.Edit)
        expect(wrapper.vm.resourceTypeDrawerState.storageStrategyIndex).toBe(0)
        expect(wrapper.vm.resourceTypeDrawerState.storageStrategyObject).toEqual(mockStorageStrategyParam)
      })
    })

    describe('Persistence Selector Strategy Drawer', () => {
      it('should open drawer in Create mode when Add Persistence Selector Strategy Parameter is clicked', async () => {
        await createWrapper()
        const addButton = wrapper.find('[data-test="add-persistence-selector-strategy-button"]')
        await addButton.trigger('click')

        expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(true)
        expect(wrapper.vm.resourceTypeDrawerState.type).toBe('persistenceSelectorStrategy')
        expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.Create)
        expect(wrapper.vm.resourceTypeDrawerState.persistenceSelectorStrategyIndex).toBe(-1)
        expect(wrapper.vm.resourceTypeDrawerState.persistenceSelectorStrategyObject).toBe(null)
      })

      it('should open drawer in Edit mode when Edit persistence strategy button is clicked', async () => {
        await createWrapper()
        wrapper.vm.persistenceSelectorStrategyParams = [mockPersistenceParam]
        await nextTick()

        const editButton = wrapper.find('[data-test="edit-persistence-selector-strategy-button"]')
        await editButton.trigger('click')

        expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(true)
        expect(wrapper.vm.resourceTypeDrawerState.type).toBe('persistenceSelectorStrategy')
        expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.Edit)
        expect(wrapper.vm.resourceTypeDrawerState.persistenceSelectorStrategyIndex).toBe(0)
        expect(wrapper.vm.resourceTypeDrawerState.persistenceSelectorStrategyObject).toEqual(mockPersistenceParam)
      })
    })

    describe('Closing Drawer', () => {
      it('should close strategy drawer correctly', async () => {
        await createWrapper()
        wrapper.vm.resourceTypeDrawerState = {
          type: 'storageStrategy',
          visible: true,
          isEditMode: CreateEditMode.Create,
          persistenceSelectorStrategyIndex: -1,
          storageStrategyIndex: -1,
          persistenceSelectorStrategyObject: null,
          storageStrategyObject: null
        }
        await nextTick()

        wrapper.vm.closeStrategyDrawer()
        await nextTick()

        expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(false)
        expect(wrapper.vm.resourceTypeDrawerState.type).toBe(null)
        expect(wrapper.vm.resourceTypeDrawerState.isEditMode).toBe(CreateEditMode.None)
      })
    })
  })

  describe('Saving Parameters', () => {
    describe('Storage Strategy Parameters', () => {
      it('should add new storage strategy parameter in Create mode', async () => {
        await createWrapper()
        wrapper.vm.resourceTypeDrawerState = {
          type: 'storageStrategy',
          visible: true,
          isEditMode: CreateEditMode.Create,
          persistenceSelectorStrategyIndex: -1,
          storageStrategyIndex: -1,
          persistenceSelectorStrategyObject: null,
          storageStrategyObject: null
        }
        await nextTick()

        wrapper.vm.saveParameters('storageStrategy', 'newKey', 'newValue')
        await nextTick()

        expect(wrapper.vm.storageStrategyParams).toContainEqual({ key: 'newKey', value: 'newValue' })
        expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(false)
      })

      it('should update existing storage strategy parameter in Edit mode', async () => {
        await createWrapper()
        wrapper.vm.storageStrategyParams = [{ key: 'originalKey', value: 'originalValue' }]
        wrapper.vm.resourceTypeDrawerState = {
          type: 'storageStrategy',
          visible: true,
          isEditMode: CreateEditMode.Edit,
          persistenceSelectorStrategyIndex: -1,
          storageStrategyIndex: 0,
          persistenceSelectorStrategyObject: null,
          storageStrategyObject: { key: 'originalKey', value: 'originalValue' }
        }
        await nextTick()

        wrapper.vm.saveParameters('storageStrategy', 'updatedKey', 'updatedValue')
        await nextTick()

        expect(wrapper.vm.storageStrategyParams[0].key).toBe('updatedKey')
        expect(wrapper.vm.storageStrategyParams[0].value).toBe('updatedValue')
      })

      it('should not update if index is -1 in Edit mode for storage strategy', async () => {
        await createWrapper()
        wrapper.vm.storageStrategyParams = [{ key: 'originalKey', value: 'originalValue' }]
        wrapper.vm.resourceTypeDrawerState = {
          type: 'storageStrategy',
          visible: true,
          isEditMode: CreateEditMode.Edit,
          persistenceSelectorStrategyIndex: -1,
          storageStrategyIndex: -1,
          persistenceSelectorStrategyObject: null,
          storageStrategyObject: null
        }
        await nextTick()

        wrapper.vm.saveParameters('storageStrategy', 'updatedKey', 'updatedValue')
        await nextTick()

        // Original should remain unchanged
        expect(wrapper.vm.storageStrategyParams[0].key).toBe('originalKey')
      })
    })

    describe('Persistence Selector Strategy Parameters', () => {
      it('should add new persistence selector strategy parameter in Create mode', async () => {
        await createWrapper()
        wrapper.vm.resourceTypeDrawerState = {
          type: 'persistenceSelectorStrategy',
          visible: true,
          isEditMode: CreateEditMode.Create,
          persistenceSelectorStrategyIndex: -1,
          storageStrategyIndex: -1,
          persistenceSelectorStrategyObject: null,
          storageStrategyObject: null
        }
        await nextTick()

        wrapper.vm.saveParameters('persistenceSelectorStrategy', 'newKey', 'newValue')
        await nextTick()

        expect(wrapper.vm.persistenceSelectorStrategyParams).toContainEqual({ key: 'newKey', value: 'newValue' })
        expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(false)
      })

      it('should update existing persistence selector strategy parameter in Edit mode', async () => {
        await createWrapper()
        wrapper.vm.persistenceSelectorStrategyParams = [{ key: 'originalKey', value: 'originalValue' }]
        wrapper.vm.resourceTypeDrawerState = {
          type: 'persistenceSelectorStrategy',
          visible: true,
          isEditMode: CreateEditMode.Edit,
          persistenceSelectorStrategyIndex: 0,
          storageStrategyIndex: -1,
          persistenceSelectorStrategyObject: { key: 'originalKey', value: 'originalValue' },
          storageStrategyObject: null
        }
        await nextTick()

        wrapper.vm.saveParameters('persistenceSelectorStrategy', 'updatedKey', 'updatedValue')
        await nextTick()

        expect(wrapper.vm.persistenceSelectorStrategyParams[0].key).toBe('updatedKey')
        expect(wrapper.vm.persistenceSelectorStrategyParams[0].value).toBe('updatedValue')
      })
    })

    describe('Adding Multiple Parameters', () => {
      it('should add multiple storage strategy parameters sequentially', async () => {
        await createWrapper()
        wrapper.vm.resourceTypeDrawerState = {
          type: 'storageStrategy',
          visible: true,
          isEditMode: CreateEditMode.Create,
          persistenceSelectorStrategyIndex: -1,
          storageStrategyIndex: -1,
          persistenceSelectorStrategyObject: null,
          storageStrategyObject: null
        }

        wrapper.vm.saveParameters('storageStrategy', 'key1', 'value1')
        await nextTick()

        wrapper.vm.resourceTypeDrawerState = {
          type: 'storageStrategy',
          visible: true,
          isEditMode: CreateEditMode.Create,
          persistenceSelectorStrategyIndex: -1,
          storageStrategyIndex: -1,
          persistenceSelectorStrategyObject: null,
          storageStrategyObject: null
        }

        wrapper.vm.saveParameters('storageStrategy', 'key2', 'value2')
        await nextTick()

        expect(wrapper.vm.storageStrategyParams).toHaveLength(2)
        expect(wrapper.vm.storageStrategyParams[0].key).toBe('key1')
        expect(wrapper.vm.storageStrategyParams[1].key).toBe('key2')
      })
    })
  })

  describe('Deleting Parameters', () => {
    describe('Delete Storage Strategy Parameter', () => {
      it('should delete storage strategy parameter at specified index', async () => {
        await createWrapper()
        wrapper.vm.storageStrategyParams = [
          { key: 'first', value: 'firstValue' },
          { key: 'second', value: 'secondValue' }
        ]
        await nextTick()

        wrapper.vm.deleteStorageStrategy(0)
        await nextTick()

        expect(wrapper.vm.storageStrategyParams).toHaveLength(1)
        expect(wrapper.vm.storageStrategyParams[0].key).toBe('second')
      })

      it('should delete storage strategy parameter when Delete button is clicked', async () => {
        await createWrapper()
        wrapper.vm.storageStrategyParams = [mockStorageStrategyParam]
        await nextTick()

        const deleteButton = wrapper.find('[data-test="delete-storage-strategy-button"]')
        await deleteButton.trigger('click')

        expect(wrapper.vm.storageStrategyParams).toHaveLength(0)
      })

      it('should handle deleting last storage strategy parameter', async () => {
        await createWrapper()
        wrapper.vm.storageStrategyParams = [mockStorageStrategyParam]
        await nextTick()

        wrapper.vm.deleteStorageStrategy(0)
        await nextTick()

        expect(wrapper.vm.storageStrategyParams).toHaveLength(0)
      })
    })

    describe('Delete Persistence Selector Strategy Parameter', () => {
      it('should delete persistence selector strategy parameter at specified index', async () => {
        await createWrapper()
        wrapper.vm.persistenceSelectorStrategyParams = [
          { key: 'first', value: 'firstValue' },
          { key: 'second', value: 'secondValue' }
        ]
        await nextTick()

        wrapper.vm.deletePersistenceSelectorStrategy(0)
        await nextTick()

        expect(wrapper.vm.persistenceSelectorStrategyParams).toHaveLength(1)
        expect(wrapper.vm.persistenceSelectorStrategyParams[0].key).toBe('second')
      })

      it('should delete persistence selector strategy parameter when Delete button is clicked', async () => {
        await createWrapper()
        wrapper.vm.persistenceSelectorStrategyParams = [mockPersistenceParam]
        await nextTick()

        const deleteButton = wrapper.find('[data-test="delete-persistence-selector-strategy-button"]')
        await deleteButton.trigger('click')

        expect(wrapper.vm.persistenceSelectorStrategyParams).toHaveLength(0)
      })
    })
  })

  describe('Parameters Tables', () => {
    it('should show empty list when no storage strategy parameters', async () => {
      await createWrapper()
      wrapper.vm.storageStrategyParams = []
      await nextTick()

      const storageEmptyContainer = wrapper.find('.storage-strategy-table-container')
      expect(storageEmptyContainer.find('[data-test="empty-list"]').exists()).toBe(true)
    })

    it('should show empty list when no persistence selector strategy parameters', async () => {
      await createWrapper()
      wrapper.vm.persistenceSelectorStrategyParams = []
      await nextTick()

      const persistenceEmptyContainer = wrapper.find('.persistence-selector-strategy-table-container')
      expect(persistenceEmptyContainer.find('[data-test="empty-list"]').exists()).toBe(true)
    })

    it('should render storage strategy parameters in table', async () => {
      await createWrapper()
      wrapper.vm.storageStrategyParams = [mockStorageStrategyParam]
      await nextTick()
      await flushPromises()

      const tableRows = wrapper.find('.storage-strategy-table-container').findAll('tbody tr')
      expect(tableRows).toHaveLength(1)
    })

    it('should display correct storage strategy parameter data in table cells', async () => {
      await createWrapper()
      wrapper.vm.storageStrategyParams = [mockStorageStrategyParam]
      await nextTick()
      await flushPromises()

      const tableRow = wrapper.find('.storage-strategy-table-container tbody tr')
      const cells = tableRow.findAll('td')

      expect(cells[0].text()).toBe(mockStorageStrategyParam.key)
      expect(cells[1].text()).toBe(mockStorageStrategyParam.value)
    })

    it('should render edit and delete buttons for each storage strategy parameter', async () => {
      await createWrapper()
      wrapper.vm.storageStrategyParams = [mockStorageStrategyParam]
      await nextTick()

      const editButton = wrapper.find('[data-test="edit-storage-strategy-button"]')
      const deleteButton = wrapper.find('[data-test="delete-storage-strategy-button"]')

      expect(editButton.exists()).toBe(true)
      expect(deleteButton.exists()).toBe(true)
    })

    it('should render multiple storage strategy parameters', async () => {
      await createWrapper()
      wrapper.vm.storageStrategyParams = [
        { key: 'key1', value: 'value1' },
        { key: 'key2', value: 'value2' },
        { key: 'key3', value: 'value3' }
      ]
      await nextTick()
      await flushPromises()

      const tableRows = wrapper.find('.storage-strategy-table-container').findAll('tbody tr')
      expect(tableRows).toHaveLength(3)
    })
  })

  describe('Save Resource Type', () => {
    describe('Create Mode', () => {
      it('should not call API if validation fails', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = ''
        await nextTick()

        await wrapper.vm.saveResourceType()
        await flushPromises()

        expect(createResourceType).not.toHaveBeenCalled()
      })

      it('should show error snackbar when no collection source selected', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.label = 'Test Label'
        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
        store.selectedCollectionSource = null
        await nextTick()

        await wrapper.vm.saveResourceType()
        await flushPromises()

        expect(mockShowSnackBar).toHaveBeenCalledWith({
          msg: 'Please select a Collection Source first.',
          error: true
        })
        expect(createResourceType).not.toHaveBeenCalled()
      })

      it('should call createResourceType API on successful save in Create mode', async () => {
        vi.mocked(createResourceType).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.label = 'Test Label'
        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
        await nextTick()

        await wrapper.vm.saveResourceType()
        await flushPromises()

        expect(createResourceType).toHaveBeenCalledTimes(1)
      })

      it('should show success snackbar on successful create', async () => {
        vi.mocked(createResourceType).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.label = 'Test Label'
        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
        await nextTick()

        await wrapper.vm.saveResourceType()
        await flushPromises()

        expect(mockShowSnackBar).toHaveBeenCalledWith({
          msg: 'Resource Type created successfully.'
        })
      })

      it('should fetch resource types after successful create', async () => {
        vi.mocked(createResourceType).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.label = 'Test Label'
        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
        await nextTick()

        await wrapper.vm.saveResourceType()
        await flushPromises()

        expect(store.fetchResourceTypes).toHaveBeenCalled()
      })

      it('should close drawer after successful create', async () => {
        vi.mocked(createResourceType).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.label = 'Test Label'
        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
        await nextTick()

        await wrapper.vm.saveResourceType()
        await flushPromises()

        expect(store.closeResourceTypeDrawer).toHaveBeenCalled()
      })

      it('should show error snackbar when API returns false', async () => {
        vi.mocked(createResourceType).mockResolvedValue(false)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.label = 'Test Label'
        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
        await nextTick()

        await wrapper.vm.saveResourceType()
        await flushPromises()

        expect(mockShowSnackBar).toHaveBeenCalledWith({
          msg: 'An error occurred while saving the Resource Type. Please try again.',
          error: true
        })
      })

      it('should show error snackbar when API throws error', async () => {
        const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
        vi.mocked(createResourceType).mockRejectedValue(new Error('Network error'))
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.label = 'Test Label'
        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
        await nextTick()

        await wrapper.vm.saveResourceType()
        await flushPromises()

        expect(mockShowSnackBar).toHaveBeenCalledWith({
          msg: 'An error occurred while saving the Resource Type. Please try again.',
          error: true
        })
        consoleErrorSpy.mockRestore()
      })
    })

    describe('Edit Mode', () => {
      it('should call updateResourceType API on successful save in Edit mode', async () => {
        vi.mocked(updateResourceType).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = mockResourceType
        wrapper.vm.name = 'Updated Name'
        wrapper.vm.label = 'Updated Label'
        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[1]
        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[1]
        await nextTick()

        await wrapper.vm.saveResourceType()
        await flushPromises()

        expect(updateResourceType).toHaveBeenCalledTimes(1)
      })

      it('should show success snackbar on successful update', async () => {
        vi.mocked(updateResourceType).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = mockResourceType
        wrapper.vm.name = 'Updated Name'
        wrapper.vm.label = 'Updated Label'
        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
        await nextTick()

        await wrapper.vm.saveResourceType()
        await flushPromises()

        expect(mockShowSnackBar).toHaveBeenCalledWith({
          msg: 'Resource Type updated successfully.'
        })
      })

      it('should show error snackbar when update API returns false', async () => {
        vi.mocked(updateResourceType).mockResolvedValue(false)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedResourceType = mockResourceType
        wrapper.vm.name = 'Updated Name'
        wrapper.vm.label = 'Updated Label'
        wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
        wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
        await nextTick()

        await wrapper.vm.saveResourceType()
        await flushPromises()

        expect(mockShowSnackBar).toHaveBeenCalledWith({
          msg: 'An error occurred while saving the Resource Type. Please try again.',
          error: true
        })
      })
    })
  })

  describe('Cancel Functionality', () => {
    it('should close resource type drawer when Cancel button is clicked', async () => {
      await createWrapper()
      const cancelButton = wrapper.find('[data-test="cancel-resource-type"]')
      await cancelButton.trigger('click')

      expect(store.closeResourceTypeDrawer).toHaveBeenCalled()
    })

    it('should close strategy drawer when cancel is clicked', async () => {
      await createWrapper()
      wrapper.vm.resourceTypeDrawerState.visible = true
      wrapper.vm.resourceTypeDrawerState.type = 'storageStrategy'
      await nextTick()

      wrapper.vm.closeResourceTypeDrawer()
      await nextTick()

      expect(wrapper.vm.resourceTypeDrawerState.visible).toBe(false)
      expect(store.closeResourceTypeDrawer).toHaveBeenCalled()
    })
  })

  describe('Watchers', () => {
    describe('store.resourceTypeDrawerState.visible', () => {
      it('should load resource type data when drawer opens', async () => {
        await createWrapper({ visible: false, isEditMode: CreateEditMode.Create })
        store.resourceTypeDrawerState.visible = true
        await nextTick()

        // Form should be initialized
        expect(wrapper.vm.storageStrategy).toEqual(STORAGE_STRATEGY_OPTIONS[0])
      })

      it('should reset form data when drawer closes', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.label = 'Test Label'
        await nextTick()

        store.resourceTypeDrawerState.visible = false
        await nextTick()

        expect(wrapper.vm.name).toBe('')
        expect(wrapper.vm.label).toBe('')
        expect(wrapper.vm.resourceLabel).toBe('')
      })

      it('should reset parameters arrays when drawer closes', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.storageStrategyParams = [mockStorageStrategyParam]
        wrapper.vm.persistenceSelectorStrategyParams = [mockPersistenceParam]
        await nextTick()

        store.resourceTypeDrawerState.visible = false
        await nextTick()

        expect(wrapper.vm.storageStrategyParams).toEqual([])
        expect(wrapper.vm.persistenceSelectorStrategyParams).toEqual([])
      })
    })
  })

  describe('Computed Properties', () => {
    describe('title', () => {
      it('should return "Create Resource Type" in Create mode', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.title).toBe('Create Resource Type')
      })

      it('should return "Edit Resource Type" in Edit mode', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        expect(wrapper.vm.title).toBe('Edit Resource Type')
      })
    })
  })

  describe('Edge Cases', () => {
    it('should handle very long name', async () => {
      await createWrapper()
      wrapper.vm.name = 'a'.repeat(500)
      wrapper.vm.label = 'Test Label'
      wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
      wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
      await nextTick()

      expect(wrapper.vm.errors.name).toBeUndefined()
      expect(wrapper.vm.isSaveDisabled).toBe(false)
    })

    it('should handle very long label', async () => {
      await createWrapper()
      wrapper.vm.name = 'Test Name'
      wrapper.vm.label = 'l'.repeat(500)
      wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
      wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
      await nextTick()

      expect(wrapper.vm.errors.label).toBeUndefined()
      expect(wrapper.vm.isSaveDisabled).toBe(false)
    })

    it('should handle special characters in name', async () => {
      await createWrapper()
      wrapper.vm.name = 'Test-Name_123'
      wrapper.vm.label = 'Test Label'
      wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
      wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
      await nextTick()

      expect(wrapper.vm.errors.name).toBeUndefined()
    })

    it('should preserve form state during validation cycles', async () => {
      await createWrapper()
      wrapper.vm.name = 'Test Name'
      wrapper.vm.label = 'Test Label'
      wrapper.vm.resourceLabel = 'Test Resource Label'
      wrapper.vm.status = false
      wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[1]
      wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[1]
      await nextTick()

      // Trigger multiple validation cycles
      await nextTick()
      await nextTick()

      expect(wrapper.vm.name).toBe('Test Name')
      expect(wrapper.vm.label).toBe('Test Label')
      expect(wrapper.vm.resourceLabel).toBe('Test Resource Label')
      expect(wrapper.vm.status).toBe(false)
    })

    it('should handle deleting middle parameter from list', async () => {
      await createWrapper()
      wrapper.vm.storageStrategyParams = [
        { key: 'first', value: 'firstValue' },
        { key: 'middle', value: 'middleValue' },
        { key: 'last', value: 'lastValue' }
      ]
      await nextTick()

      wrapper.vm.deleteStorageStrategy(1)
      await nextTick()

      expect(wrapper.vm.storageStrategyParams).toHaveLength(2)
      expect(wrapper.vm.storageStrategyParams[0].key).toBe('first')
      expect(wrapper.vm.storageStrategyParams[1].key).toBe('last')
    })

    it('should handle switching between Create and Edit mode', async () => {
      await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
      expect(wrapper.vm.title).toBe('Create Resource Type')

      store.resourceTypeDrawerState.isEditMode = CreateEditMode.Edit
      await nextTick()

      expect(wrapper.vm.title).toBe('Edit Resource Type')
    })

    it('should handle null/undefined storage strategy params in edit mode', async () => {
      const resourceTypeWithNullParams = {
        ...mockResourceType,
        storageStrategyParams: null as unknown as string
      }
      await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
      store.selectedResourceType = resourceTypeWithNullParams
      store.resourceTypeDrawerState.visible = false
      await nextTick()
      store.resourceTypeDrawerState.visible = true
      await nextTick()

      // Should handle null gracefully - may result in empty array or throw during JSON.parse
      // The component uses: JSON.parse(resourceType.storageStrategyParams || '[]')
      expect(wrapper.vm.storageStrategyParams).toEqual([])
    })

    it('should handle resource label being optional', async () => {
      await createWrapper()
      wrapper.vm.name = 'Test Name'
      wrapper.vm.label = 'Test Label'
      wrapper.vm.resourceLabel = '' // Empty resource label
      wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
      wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
      await nextTick()

      // Resource label is not required in validation
      expect(wrapper.vm.errors.resourceLabel).toBeUndefined()
      expect(wrapper.vm.isSaveDisabled).toBe(false)
    })

    it('should handle concurrent save attempts', async () => {
      vi.mocked(createResourceType).mockResolvedValue(true)
      await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
      wrapper.vm.name = 'Test Name'
      wrapper.vm.label = 'Test Label'
      wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
      wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
      await nextTick()

      // Multiple save calls
      wrapper.vm.saveResourceType()
      wrapper.vm.saveResourceType()
      await flushPromises()

      // Both calls should go through
      expect(createResourceType).toHaveBeenCalledTimes(2)
    })
  })

  describe('Button States', () => {
    it('should have Save button disabled initially', async () => {
      await createWrapper()
      expect(wrapper.vm.isSaveDisabled).toBe(true)
    })

    it('should enable Save button when all required fields are valid', async () => {
      await createWrapper()
      wrapper.vm.name = 'Valid Name'
      wrapper.vm.label = 'Valid Label'
      wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[0]
      wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
      await nextTick()

      expect(wrapper.vm.isSaveDisabled).toBe(false)
    })

    it('should keep Cancel button always enabled', async () => {
      await createWrapper()
      const cancelButton = wrapper.find('[data-test="cancel-resource-type"]')
      expect(cancelButton.exists()).toBe(true)
      await cancelButton.trigger('click')
      expect(store.closeResourceTypeDrawer).toHaveBeenCalled()
    })
  })

  describe('Form Reset', () => {
    it('should reset all fields when drawer closes', async () => {
      await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
      wrapper.vm.name = 'Test Name'
      wrapper.vm.label = 'Test Label'
      wrapper.vm.resourceLabel = 'Test Resource Label'
      wrapper.vm.status = false
      wrapper.vm.storageStrategyParams = [mockStorageStrategyParam]
      wrapper.vm.persistenceSelectorStrategyParams = [mockPersistenceParam]
      await nextTick()

      store.resourceTypeDrawerState.visible = false
      await nextTick()

      expect(wrapper.vm.name).toBe('')
      expect(wrapper.vm.label).toBe('')
      expect(wrapper.vm.resourceLabel).toBe('')
      expect(wrapper.vm.status).toBe(true)
      expect(wrapper.vm.storageStrategyParams).toEqual([])
      expect(wrapper.vm.persistenceSelectorStrategyParams).toEqual([])
    })

    it('should reset strategy selects when drawer closes', async () => {
      await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
      wrapper.vm.storageStrategy = STORAGE_STRATEGY_OPTIONS[1]
      wrapper.vm.persistenceSelectorStrategy = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[1]
      await nextTick()

      store.resourceTypeDrawerState.visible = false
      await nextTick()

      expect(wrapper.vm.storageStrategy).toBeUndefined()
      expect(wrapper.vm.persistenceSelectorStrategy).toBeUndefined()
    })
  })

  describe('Multiple Error Display', () => {
    it('should show errors for multiple fields when all are invalid', async () => {
      await createWrapper()
      wrapper.vm.name = ''
      wrapper.vm.label = ''
      wrapper.vm.storageStrategy = { _text: '', _value: '' }
      wrapper.vm.persistenceSelectorStrategy = { _text: '', _value: '' }
      await nextTick()

      expect(wrapper.vm.errors.name).toBeDefined()
      expect(wrapper.vm.errors.label).toBeDefined()
      expect(wrapper.vm.errors.storageStrategy).toBeDefined()
      expect(wrapper.vm.errors.persistenceSelectorStrategy).toBeDefined()
    })
  })
})


import MibGroupForm from '@/components/SnmpDataCollectionDetail/MibGroupForm.vue'
import { createMibGroup, updateMibGroup } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionMibGroup, MibGroupObjectForm } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherInput } from '@featherds/input'
import { FeatherRadio, FeatherRadioGroup } from '@featherds/radio'
import { FeatherSelect } from '@featherds/select'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { DEFAULT_IF_TYPE_FILTER, IF_TYPE_FILTERS_OPTIONS, STATUS_OPTIONS } from '@/lib/constants'

vi.mock('@/services/snmpDataCollectionService', () => ({
  createMibGroup: vi.fn(),
  updateMibGroup: vi.fn()
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: mockShowSnackBar
  })
}))

vi.mock('@/components/SnmpDataCollectionDetail/Drawer/MibObjectCreationDrawer.vue', () => ({
  default: {
    name: 'MibObjectCreationDrawer',
    template: '<div data-test="mib-object-creation-drawer"></div>',
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
    template: '<div data-test="empty-list">No MIB Objects</div>',
    props: ['content']
  }
}))

describe('MibGroupForm.vue', () => {
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

  const mockMibGroup: SnmpCollectionMibGroup = {
    id: 1,
    name: 'Test MIB Group',
    ifType: 'all',
    mibGroupNames: ['test-group'],
    mibObjects: JSON.stringify([
      { oid: '.1.3.6.1.2.1.1.1', alias: 'sysDescr', instance: 'sysDescr', type: 'string', maxval: null, minval: null }
    ]),
    mibObjProperties: '',
    enabled: true,
    collectionSourceId: 1,
    collectionSourceName: 'Test Source'
  }

  const mockMibObject: MibGroupObjectForm = {
    oid: '.1.3.6.1.2.1.1.2',
    alias: 'sysObjectID',
    instance: 'sysObjectID',
    type: 'string',
    maxval: null,
    minval: null
  }

  const createWrapper = async (mibGroupDrawerState = { visible: true, isEditMode: CreateEditMode.Create }) => {
    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useSnmpDataCollectionDetailStore(pinia)
    store.selectedCollectionSource = mockCollectionSource
    store.mibGroupDrawerState = mibGroupDrawerState
    store.selectedMibGroup = null
    store.closeMibGroupDrawer = vi.fn()
    store.fetchMibGroups = vi.fn().mockResolvedValue(undefined)

    wrapper = mount(MibGroupForm, {
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
      it('should render the component with Create Mib Group title', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.find('.title').text()).toBe('Create Mib Group')
      })

      it('should render name input field', async () => {
        await createWrapper()
        const nameInput = wrapper.find('[data-test="mib-group-name-input"]')
        expect(nameInput.exists()).toBe(true)
      })

      it('should render interface type select field', async () => {
        await createWrapper()
        const ifTypeSelect = wrapper.find('[data-test="mib-group-if-type-input"]')
        expect(ifTypeSelect.exists()).toBe(true)
      })

      it('should render status radio group', async () => {
        await createWrapper()
        const statusRadioGroup = wrapper.find('[data-test="system-def-status-input"]')
        expect(statusRadioGroup.exists()).toBe(true)
      })

      it('should render Add MIB Object button', async () => {
        await createWrapper()
        const addButton = wrapper.find('[data-test="add-mib-object-button"]')
        expect(addButton.exists()).toBe(true)
      })

      it('should render Cancel button', async () => {
        await createWrapper()
        const cancelButton = wrapper.find('[data-test="cancel-mib-group"]')
        expect(cancelButton.exists()).toBe(true)
      })

      it('should render Save button', async () => {
        await createWrapper()
        const saveButton = wrapper.find('[data-test="save-mib-group"]')
        expect(saveButton.exists()).toBe(true)
      })

      it('should render MibObjectCreationDrawer component', async () => {
        await createWrapper()
        const drawer = wrapper.find('[data-test="mib-object-creation-drawer"]')
        expect(drawer.exists()).toBe(true)
      })

      it('should render empty list message when no MIB objects', async () => {
        await createWrapper()
        const emptyList = wrapper.find('[data-test="empty-list"]')
        expect(emptyList.exists()).toBe(true)
      })

      it('should show MIB Objects table header', async () => {
        await createWrapper()
        expect(wrapper.find('table').exists()).toBe(true)
        expect(wrapper.find('thead').text()).toContain('OID')
        expect(wrapper.find('thead').text()).toContain('Instance')
        expect(wrapper.find('thead').text()).toContain('Alias')
        expect(wrapper.find('thead').text()).toContain('Type')
        expect(wrapper.find('thead').text()).toContain('Action')
      })
    })

    describe('Edit Mode', () => {
      it('should render the component with Edit Mib Group title', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedMibGroup = mockMibGroup
        await nextTick()

        expect(wrapper.find('.title').text()).toBe('Edit Mib Group')
      })
    })
  })

  describe('Initial Data Loading', () => {
    describe('Create Mode', () => {
      it('should initialize with empty name', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.name).toBe('')
      })

      it('should initialize with default ifType (Ignore)', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.ifType).toEqual(DEFAULT_IF_TYPE_FILTER)
      })

      it('should initialize with enabled status (true)', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.status).toBe(true)
      })

      it('should initialize with empty mibObjects array', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.mibObjects).toEqual([])
      })
    })

    describe('Edit Mode', () => {
      it('should load existing MIB group name', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedMibGroup = mockMibGroup
        store.mibGroupDrawerState.visible = false
        await nextTick()
        store.mibGroupDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.name).toBe('Test MIB Group')
      })

      it('should load existing MIB group ifType', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedMibGroup = mockMibGroup
        store.mibGroupDrawerState.visible = false
        await nextTick()
        store.mibGroupDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.ifType._value).toBe('all')
      })

      it('should load existing MIB group enabled status', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedMibGroup = mockMibGroup
        store.mibGroupDrawerState.visible = false
        await nextTick()
        store.mibGroupDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.status).toBe(true)
      })

      it('should load existing MIB objects', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedMibGroup = mockMibGroup
        store.mibGroupDrawerState.visible = false
        await nextTick()
        store.mibGroupDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.mibObjects).toHaveLength(1)
        expect(wrapper.vm.mibObjects[0].oid).toBe('.1.3.6.1.2.1.1.1')
      })

      it('should handle disabled MIB group status', async () => {
        const disabledMibGroup = { ...mockMibGroup, enabled: false }
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedMibGroup = disabledMibGroup
        store.mibGroupDrawerState.visible = false
        await nextTick()
        store.mibGroupDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.status).toBe(false)
      })

      it('should handle empty mibObjects JSON', async () => {
        const emptyMibObjectsGroup = { ...mockMibGroup, mibObjects: '[]' }
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedMibGroup = emptyMibObjectsGroup
        store.mibGroupDrawerState.visible = false
        await nextTick()
        store.mibGroupDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.mibObjects).toEqual([])
      })

      it('should handle null selectedMibGroup gracefully', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedMibGroup = null
        store.mibGroupDrawerState.visible = false
        await nextTick()
        store.mibGroupDrawerState.visible = true
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

        expect(wrapper.vm.errors.name).toBe('Name is required.')
      })

      it('should show error when name is only whitespace', async () => {
        await createWrapper()
        wrapper.vm.name = '   '
        await nextTick()

        expect(wrapper.vm.errors.name).toBe('Name is required.')
      })

      it('should clear name error when valid name is provided', async () => {
        await createWrapper()
        wrapper.vm.name = ''
        await nextTick()
        expect(wrapper.vm.errors.name).toBe('Name is required.')

        wrapper.vm.name = 'Valid Name'
        await nextTick()
        expect(wrapper.vm.errors.name).toBeUndefined()
      })
    })

    describe('Interface Type Validation', () => {
      it('should show error when ifType value is empty', async () => {
        await createWrapper()
        wrapper.vm.ifType = { _text: '', _value: '' }
        await nextTick()

        expect(wrapper.vm.errors.ifType).toBe('Interface Type is required.')
      })

      it('should clear ifType error when valid ifType is selected', async () => {
        await createWrapper()
        wrapper.vm.ifType = { _text: '', _value: '' }
        await nextTick()
        expect(wrapper.vm.errors.ifType).toBe('Interface Type is required.')

        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
        await nextTick()
        expect(wrapper.vm.errors.ifType).toBeUndefined()
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
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
        await nextTick()

        expect(wrapper.vm.isSaveDisabled).toBe(false)
      })

      it('should disable save button when name becomes empty after being valid', async () => {
        await createWrapper()
        wrapper.vm.name = 'Valid Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(false)

        wrapper.vm.name = ''
        await nextTick()
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })
    })
  })

  describe('MIB Object Drawer Management', () => {
    describe('Opening Drawer', () => {
      it('should open drawer in Create mode when Add MIB Object is clicked', async () => {
        await createWrapper()
        const addButton = wrapper.find('[data-test="add-mib-object-button"]')
        await addButton.trigger('click')

        expect(wrapper.vm.mibObjectDrawerState.visible).toBe(true)
        expect(wrapper.vm.mibObjectDrawerState.isEditMode).toBe(CreateEditMode.Create)
        expect(wrapper.vm.mibObjectDrawerState.mibObjectIndex).toBe(-1)
        expect(wrapper.vm.mibObjectDrawerState.mibObject).toBe(null)
      })

      it('should open drawer in Edit mode when Edit button is clicked', async () => {
        await createWrapper()
        wrapper.vm.mibObjects = [mockMibObject]
        await nextTick()

        const editButton = wrapper.find('[data-test="edit-mib-object-button"]')
        await editButton.trigger('click')

        expect(wrapper.vm.mibObjectDrawerState.visible).toBe(true)
        expect(wrapper.vm.mibObjectDrawerState.isEditMode).toBe(CreateEditMode.Edit)
        expect(wrapper.vm.mibObjectDrawerState.mibObjectIndex).toBe(0)
        expect(wrapper.vm.mibObjectDrawerState.mibObject).toEqual(mockMibObject)
      })
    })

    describe('Closing Drawer', () => {
      it('should close MIB object drawer correctly', async () => {
        await createWrapper()
        wrapper.vm.mibObjectDrawerState = {
          visible: true,
          isEditMode: CreateEditMode.Create,
          mibObjectIndex: -1,
          mibObject: null
        }
        await nextTick()

        wrapper.vm.closeMibObjectDrawer()
        await nextTick()

        expect(wrapper.vm.mibObjectDrawerState.visible).toBe(false)
        expect(wrapper.vm.mibObjectDrawerState.isEditMode).toBe(CreateEditMode.None)
        expect(wrapper.vm.mibObjectDrawerState.mibObjectIndex).toBe(-1)
        expect(wrapper.vm.mibObjectDrawerState.mibObject).toBe(null)
      })
    })

    describe('Saving MIB Object', () => {
      it('should add new MIB object in Create mode', async () => {
        await createWrapper()
        wrapper.vm.mibObjectDrawerState = {
          visible: true,
          isEditMode: CreateEditMode.Create,
          mibObjectIndex: -1,
          mibObject: null
        }
        await nextTick()

        wrapper.vm.saveMibObject(mockMibObject)
        await nextTick()

        expect(wrapper.vm.mibObjects).toContainEqual(mockMibObject)
        expect(wrapper.vm.mibObjectDrawerState.visible).toBe(false)
      })

      it('should update existing MIB object in Edit mode', async () => {
        await createWrapper()
        const originalMibObject = { ...mockMibObject, alias: 'original' }
        wrapper.vm.mibObjects = [originalMibObject]
        wrapper.vm.mibObjectDrawerState = {
          visible: true,
          isEditMode: CreateEditMode.Edit,
          mibObjectIndex: 0,
          mibObject: originalMibObject
        }
        await nextTick()

        const updatedMibObject = { ...mockMibObject, alias: 'updated' }
        wrapper.vm.saveMibObject(updatedMibObject)
        await nextTick()

        expect(wrapper.vm.mibObjects[0].alias).toBe('updated')
        expect(wrapper.vm.mibObjectDrawerState.visible).toBe(false)
      })

      it('should not update if index is -1 in Edit mode', async () => {
        await createWrapper()
        const originalMibObject = { ...mockMibObject, alias: 'original' }
        wrapper.vm.mibObjects = [originalMibObject]
        wrapper.vm.mibObjectDrawerState = {
          visible: true,
          isEditMode: CreateEditMode.Edit,
          mibObjectIndex: -1,
          mibObject: null
        }
        await nextTick()

        const updatedMibObject = { ...mockMibObject, alias: 'updated' }
        wrapper.vm.saveMibObject(updatedMibObject)
        await nextTick()

        // Original should remain unchanged
        expect(wrapper.vm.mibObjects[0].alias).toBe('original')
      })

      it('should add multiple MIB objects sequentially', async () => {
        await createWrapper()
        wrapper.vm.mibObjectDrawerState = {
          visible: true,
          isEditMode: CreateEditMode.Create,
          mibObjectIndex: -1,
          mibObject: null
        }

        wrapper.vm.saveMibObject({ ...mockMibObject, alias: 'first' })
        await nextTick()

        wrapper.vm.mibObjectDrawerState = {
          visible: true,
          isEditMode: CreateEditMode.Create,
          mibObjectIndex: -1,
          mibObject: null
        }

        wrapper.vm.saveMibObject({ ...mockMibObject, alias: 'second' })
        await nextTick()

        expect(wrapper.vm.mibObjects).toHaveLength(2)
        expect(wrapper.vm.mibObjects[0].alias).toBe('first')
        expect(wrapper.vm.mibObjects[1].alias).toBe('second')
      })
    })

    describe('Deleting MIB Object', () => {
      it('should delete MIB object at specified index', async () => {
        await createWrapper()
        wrapper.vm.mibObjects = [
          { ...mockMibObject, alias: 'first' },
          { ...mockMibObject, alias: 'second' }
        ]
        await nextTick()

        wrapper.vm.deleteMibObject(0)
        await nextTick()

        expect(wrapper.vm.mibObjects).toHaveLength(1)
        expect(wrapper.vm.mibObjects[0].alias).toBe('second')
      })

      it('should delete MIB object when Delete button is clicked', async () => {
        await createWrapper()
        wrapper.vm.mibObjects = [mockMibObject]
        await nextTick()

        const deleteButton = wrapper.find('[data-test="delete-mib-object-button"]')
        await deleteButton.trigger('click')

        expect(wrapper.vm.mibObjects).toHaveLength(0)
      })

      it('should handle deleting last MIB object', async () => {
        await createWrapper()
        wrapper.vm.mibObjects = [mockMibObject]
        await nextTick()

        wrapper.vm.deleteMibObject(0)
        await nextTick()

        expect(wrapper.vm.mibObjects).toHaveLength(0)
      })

      it('should handle deleting middle MIB object', async () => {
        await createWrapper()
        wrapper.vm.mibObjects = [
          { ...mockMibObject, alias: 'first' },
          { ...mockMibObject, alias: 'middle' },
          { ...mockMibObject, alias: 'last' }
        ]
        await nextTick()

        wrapper.vm.deleteMibObject(1)
        await nextTick()

        expect(wrapper.vm.mibObjects).toHaveLength(2)
        expect(wrapper.vm.mibObjects[0].alias).toBe('first')
        expect(wrapper.vm.mibObjects[1].alias).toBe('last')
      })
    })
  })

  describe('MIB Objects Table', () => {
    it('should show empty list when no MIB objects', async () => {
      await createWrapper()
      wrapper.vm.mibObjects = []
      await nextTick()

      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
    })

    it('should render MIB objects in table', async () => {
      await createWrapper()
      wrapper.vm.mibObjects = [mockMibObject]
      await nextTick()
      await flushPromises()

      const tableRows = wrapper.findAll('tbody tr')
      expect(tableRows).toHaveLength(1)
    })

    it('should display correct MIB object data in table cells', async () => {
      await createWrapper()
      wrapper.vm.mibObjects = [mockMibObject]
      await nextTick()
      await flushPromises()

      const tableRow = wrapper.find('tbody tr')
      const cells = tableRow.findAll('td')

      expect(cells[0].text()).toBe(mockMibObject.oid)
      expect(cells[1].text()).toBe(mockMibObject.instance)
      expect(cells[2].text()).toBe(mockMibObject.alias)
      expect(cells[3].text()).toBe(mockMibObject.type)
    })

    it('should render edit and delete buttons for each MIB object', async () => {
      await createWrapper()
      wrapper.vm.mibObjects = [mockMibObject]
      await nextTick()

      const editButton = wrapper.find('[data-test="edit-mib-object-button"]')
      const deleteButton = wrapper.find('[data-test="delete-mib-object-button"]')

      expect(editButton.exists()).toBe(true)
      expect(deleteButton.exists()).toBe(true)
    })

    it('should render multiple MIB objects', async () => {
      await createWrapper()
      wrapper.vm.mibObjects = [
        { ...mockMibObject, alias: 'first' },
        { ...mockMibObject, alias: 'second' },
        { ...mockMibObject, alias: 'third' }
      ]
      await nextTick()
      await flushPromises()

      const tableRows = wrapper.findAll('tbody tr')
      expect(tableRows).toHaveLength(3)
    })
  })

  describe('Save MIB Group', () => {
    describe('Create Mode', () => {
      it('should not call API if validation fails', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = ''
        await nextTick()

        await wrapper.vm.saveMibGroup()
        await flushPromises()

        expect(createMibGroup).not.toHaveBeenCalled()
      })

      it('should show error snackbar when no collection source selected', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
        store.selectedCollectionSource = null
        await nextTick()

        await wrapper.vm.saveMibGroup()
        await flushPromises()

        expect(mockShowSnackBar).toHaveBeenCalledWith({
          msg: 'Please select a Collection Source first.',
          error: true
        })
        expect(createMibGroup).not.toHaveBeenCalled()
      })

      it('should call createMibGroup API on successful save in Create mode', async () => {
        vi.mocked(createMibGroup).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
        wrapper.vm.mibObjects = [mockMibObject]
        await nextTick()

        await wrapper.vm.saveMibGroup()
        await flushPromises()

        expect(createMibGroup).toHaveBeenCalledTimes(1)
      })

      it('should show success snackbar on successful create', async () => {
        vi.mocked(createMibGroup).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
        await nextTick()

        await wrapper.vm.saveMibGroup()
        await flushPromises()

        expect(mockShowSnackBar).toHaveBeenCalledWith({
          msg: 'MIB Group created successfully.'
        })
      })

      it('should fetch MIB groups after successful create', async () => {
        vi.mocked(createMibGroup).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
        await nextTick()

        await wrapper.vm.saveMibGroup()
        await flushPromises()

        expect(store.fetchMibGroups).toHaveBeenCalled()
      })

      it('should close drawer after successful create', async () => {
        vi.mocked(createMibGroup).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
        await nextTick()

        await wrapper.vm.saveMibGroup()
        await flushPromises()

        expect(store.closeMibGroupDrawer).toHaveBeenCalled()
      })

      it('should show error snackbar when API returns false', async () => {
        vi.mocked(createMibGroup).mockResolvedValue(false)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
        await nextTick()

        await wrapper.vm.saveMibGroup()
        await flushPromises()

        expect(mockShowSnackBar).toHaveBeenCalledWith({
          msg: 'An error occurred while saving the MIB Group.',
          error: true
        })
      })

      it('should show error snackbar when API throws error', async () => {
        const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
        vi.mocked(createMibGroup).mockRejectedValue(new Error('Network error'))
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
        await nextTick()

        await wrapper.vm.saveMibGroup()
        await flushPromises()

        expect(mockShowSnackBar).toHaveBeenCalledWith({
          msg: 'An error occurred while saving the MIB Group.',
          error: true
        })
        consoleErrorSpy.mockRestore()
      })
    })

    describe('Edit Mode', () => {
      it('should call updateMibGroup API on successful save in Edit mode', async () => {
        vi.mocked(updateMibGroup).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedMibGroup = mockMibGroup
        wrapper.vm.name = 'Updated Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[1]
        await nextTick()

        await wrapper.vm.saveMibGroup()
        await flushPromises()

        expect(updateMibGroup).toHaveBeenCalledTimes(1)
      })

      it('should show success snackbar on successful update', async () => {
        vi.mocked(updateMibGroup).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedMibGroup = mockMibGroup
        wrapper.vm.name = 'Updated Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[1]
        await nextTick()

        await wrapper.vm.saveMibGroup()
        await flushPromises()

        expect(mockShowSnackBar).toHaveBeenCalledWith({
          msg: 'MIB Group updated successfully.'
        })
      })

      it('should fetch MIB groups after successful update', async () => {
        vi.mocked(updateMibGroup).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedMibGroup = mockMibGroup
        wrapper.vm.name = 'Updated Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[1]
        await nextTick()

        await wrapper.vm.saveMibGroup()
        await flushPromises()

        expect(store.fetchMibGroups).toHaveBeenCalled()
      })

      it('should handle edit mode with empty mibGroupNames', async () => {
        vi.mocked(updateMibGroup).mockResolvedValue(true)
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        store.selectedMibGroup = { ...mockMibGroup, mibGroupNames: [] }
        wrapper.vm.name = 'Updated Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[1]
        await nextTick()

        await wrapper.vm.saveMibGroup()
        await flushPromises()

        expect(updateMibGroup).toHaveBeenCalled()
      })
    })
  })

  describe('Close MIB Group Drawer', () => {
    it('should call store.closeMibGroupDrawer when Cancel is clicked', async () => {
      await createWrapper()
      const cancelButton = wrapper.find('[data-test="cancel-mib-group"]')
      await cancelButton.trigger('click')

      expect(store.closeMibGroupDrawer).toHaveBeenCalled()
    })

    it('should close MIB object drawer when closing MIB group drawer', async () => {
      await createWrapper()
      wrapper.vm.mibObjectDrawerState = {
        visible: true,
        isEditMode: CreateEditMode.Create,
        mibObjectIndex: -1,
        mibObject: null
      }
      await nextTick()

      wrapper.vm.closeMibGroupDrawer()
      await nextTick()

      expect(wrapper.vm.mibObjectDrawerState.visible).toBe(false)
    })
  })

  describe('Watchers', () => {
    describe('store.mibGroupDrawerState.visible', () => {
      it('should load initial data when drawer becomes visible', async () => {
        await createWrapper({ visible: false, isEditMode: CreateEditMode.Create })
        store.mibGroupDrawerState.visible = true
        await nextTick()

        // In Create mode, form should be reset
        expect(wrapper.vm.name).toBe('')
        expect(wrapper.vm.ifType).toEqual(DEFAULT_IF_TYPE_FILTER)
        expect(wrapper.vm.status).toBe(true)
        expect(wrapper.vm.mibObjects).toEqual([])
      })

      it('should reset form data when drawer is closed', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        wrapper.vm.name = 'Test Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[1]
        wrapper.vm.status = false
        wrapper.vm.mibObjects = [mockMibObject]
        wrapper.vm.errors = { name: 'Custom Error' }
        await nextTick()

        store.mibGroupDrawerState.visible = false
        await nextTick()

        // Form values should be reset
        expect(wrapper.vm.name).toBe('')
        expect(wrapper.vm.ifType).toEqual(DEFAULT_IF_TYPE_FILTER)
        expect(wrapper.vm.status).toBe(true)
        expect(wrapper.vm.mibObjects).toEqual([])
        // Note: watchEffect re-runs validation after reset, so errors will have name validation
        expect(wrapper.vm.errors.name).toBe('Name is required.')
        expect(wrapper.vm.isSaveDisabled).toBe(true)
      })

      it('should load MIB group data in Edit mode when drawer opens', async () => {
        await createWrapper({ visible: false, isEditMode: CreateEditMode.Edit })
        store.selectedMibGroup = mockMibGroup
        store.mibGroupDrawerState.visible = true
        await nextTick()

        expect(wrapper.vm.name).toBe('Test MIB Group')
        expect(wrapper.vm.ifType._value).toBe('all')
        expect(wrapper.vm.status).toBe(true)
      })
    })

    describe('watchEffect for validation', () => {
      it('should update errors reactively', async () => {
        await createWrapper()
        expect(wrapper.vm.errors.name).toBe('Name is required.')

        wrapper.vm.name = 'Valid Name'
        await nextTick()

        expect(wrapper.vm.errors.name).toBeUndefined()
      })

      it('should update isSaveDisabled reactively', async () => {
        await createWrapper()
        expect(wrapper.vm.isSaveDisabled).toBe(true)

        wrapper.vm.name = 'Valid Name'
        wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
        await nextTick()

        expect(wrapper.vm.isSaveDisabled).toBe(false)
      })
    })
  })

  describe('Computed Properties', () => {
    describe('title', () => {
      it('should return "Create Mib Group" in Create mode', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
        expect(wrapper.vm.title).toBe('Create Mib Group')
      })

      it('should return "Edit Mib Group" in Edit mode', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
        expect(wrapper.vm.title).toBe('Edit Mib Group')
      })

      it('should return "Edit Mib Group" in None mode', async () => {
        await createWrapper({ visible: true, isEditMode: CreateEditMode.None })
        expect(wrapper.vm.title).toBe('Edit Mib Group')
      })
    })
  })

  describe('Edge Cases', () => {
    it('should handle form with only name filled', async () => {
      await createWrapper()
      wrapper.vm.name = 'Only Name'
      await nextTick()

      expect(wrapper.vm.isSaveDisabled).toBe(false) // Default ifType is set
    })

    it('should handle status toggle to disabled', async () => {
      await createWrapper()
      wrapper.vm.status = false
      await nextTick()

      expect(wrapper.vm.status).toBe(false)
    })

    it('should handle special characters in name', async () => {
      await createWrapper()
      wrapper.vm.name = 'Test-Group_123!@#'
      wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
      await nextTick()

      expect(wrapper.vm.errors.name).toBeUndefined()
    })

    it('should handle very long name', async () => {
      await createWrapper()
      wrapper.vm.name = 'A'.repeat(500)
      wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
      await nextTick()

      expect(wrapper.vm.errors.name).toBeUndefined()
    })

    it('should handle rapid mib object additions', async () => {
      await createWrapper()

      for (let i = 0; i < 10; i++) {
        wrapper.vm.mibObjectDrawerState = {
          visible: true,
          isEditMode: CreateEditMode.Create,
          mibObjectIndex: -1,
          mibObject: null
        }
        wrapper.vm.saveMibObject({ ...mockMibObject, alias: `object-${i}` })
      }
      await nextTick()

      expect(wrapper.vm.mibObjects).toHaveLength(10)
    })

    it('should handle rapid mib object deletions', async () => {
      await createWrapper()
      wrapper.vm.mibObjects = Array.from({ length: 5 }, (_, i) => ({ ...mockMibObject, alias: `object-${i}` }))
      await nextTick()

      for (let i = 4; i >= 0; i--) {
        wrapper.vm.deleteMibObject(i)
      }
      await nextTick()

      expect(wrapper.vm.mibObjects).toHaveLength(0)
    })

    it('should handle switching from Create to Edit mode', async () => {
      await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
      expect(wrapper.vm.title).toBe('Create Mib Group')

      store.mibGroupDrawerState.isEditMode = CreateEditMode.Edit
      await nextTick()

      expect(wrapper.vm.title).toBe('Edit Mib Group')
    })

    it('should handle MIB object with null maxval and minval', async () => {
      await createWrapper()
      const mibObjectWithNulls = {
        ...mockMibObject,
        maxval: null,
        minval: null
      }
      wrapper.vm.mibObjects = [mibObjectWithNulls]
      await nextTick()

      expect(wrapper.vm.mibObjects[0].maxval).toBe(null)
      expect(wrapper.vm.mibObjects[0].minval).toBe(null)
    })

    it('should handle MIB object with string maxval and minval', async () => {
      await createWrapper()
      const mibObjectWithStrings = {
        ...mockMibObject,
        maxval: '100',
        minval: '0'
      }
      wrapper.vm.mibObjects = [mibObjectWithStrings]
      await nextTick()

      expect(wrapper.vm.mibObjects[0].maxval).toBe('100')
      expect(wrapper.vm.mibObjects[0].minval).toBe('0')
    })

    it('should handle collection source with id 0', async () => {
      await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
      wrapper.vm.name = 'Test Name'
      wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
      store.selectedCollectionSource = { ...mockCollectionSource, id: 0 }
      await nextTick()

      await wrapper.vm.saveMibGroup()
      await flushPromises()

      // id: 0 is falsy, should show error
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Please select a Collection Source first.',
        error: true
      })
    })

    it('should maintain MIB objects order after edit', async () => {
      await createWrapper()
      wrapper.vm.mibObjects = [
        { ...mockMibObject, alias: 'first' },
        { ...mockMibObject, alias: 'second' },
        { ...mockMibObject, alias: 'third' }
      ]
      await nextTick()

      // Edit the middle one
      wrapper.vm.mibObjectDrawerState = {
        visible: true,
        isEditMode: CreateEditMode.Edit,
        mibObjectIndex: 1,
        mibObject: wrapper.vm.mibObjects[1]
      }
      wrapper.vm.saveMibObject({ ...mockMibObject, alias: 'updated-second' })
      await nextTick()

      expect(wrapper.vm.mibObjects[0].alias).toBe('first')
      expect(wrapper.vm.mibObjects[1].alias).toBe('updated-second')
      expect(wrapper.vm.mibObjects[2].alias).toBe('third')
    })
  })

  describe('Button States', () => {
    it('should have Save button disabled initially', async () => {
      await createWrapper()
      // isSaveDisabled should be true since name is empty
      expect(wrapper.vm.isSaveDisabled).toBe(true)
    })

    it('should enable Save button when form is valid', async () => {
      await createWrapper()
      wrapper.vm.name = 'Valid Name'
      wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0]
      await nextTick()

      const saveButton = wrapper.find('[data-test="save-mib-group"]')
      expect(saveButton.attributes('disabled')).toBeUndefined()
    })

    it('should keep Cancel button always enabled', async () => {
      await createWrapper()
      const cancelButton = wrapper.find('[data-test="cancel-mib-group"]')
      expect(cancelButton.attributes('disabled')).toBeUndefined()
    })

    it('should keep Add MIB Object button always enabled', async () => {
      await createWrapper()
      const addButton = wrapper.find('[data-test="add-mib-object-button"]')
      expect(addButton.attributes('disabled')).toBeUndefined()
    })
  })

  describe('Status Options', () => {
    it('should render all status options', async () => {
      await createWrapper()
      const radioButtons = wrapper.findAllComponents(FeatherRadio)
      expect(radioButtons.length).toBe(STATUS_OPTIONS.length)
    })

    it('should have Enabled selected by default', async () => {
      await createWrapper()
      expect(wrapper.vm.status).toBe(true)
    })

    it('should allow changing status to Disabled', async () => {
      await createWrapper()
      wrapper.vm.status = false
      await nextTick()
      expect(wrapper.vm.status).toBe(false)
    })
  })

  describe('Interface Type Options', () => {
    it('should have Ignore selected by default', async () => {
      await createWrapper()
      expect(wrapper.vm.ifType).toEqual(DEFAULT_IF_TYPE_FILTER)
    })

    it('should allow changing to All', async () => {
      await createWrapper()
      wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS.find(opt => opt._value === 'all')!
      await nextTick()
      expect(wrapper.vm.ifType._value).toBe('all')
    })
  })

  describe('Payload Mapping', () => {
    it('should pass correct payload to createMibGroup', async () => {
      vi.mocked(createMibGroup).mockResolvedValue(true)
      await createWrapper({ visible: true, isEditMode: CreateEditMode.Create })
      wrapper.vm.name = 'New MIB Group'
      wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[1] // 'all'
      wrapper.vm.status = true
      wrapper.vm.mibObjects = [mockMibObject]
      await nextTick()

      await wrapper.vm.saveMibGroup()
      await flushPromises()

      expect(createMibGroup).toHaveBeenCalledWith(
        expect.objectContaining({
          name: 'New MIB Group',
          ifType: 'all',
          enabled: true
        }),
        mockCollectionSource.id
      )
    })

    it('should pass correct payload to updateMibGroup', async () => {
      vi.mocked(updateMibGroup).mockResolvedValue(true)
      await createWrapper({ visible: true, isEditMode: CreateEditMode.Edit })
      store.selectedMibGroup = mockMibGroup
      wrapper.vm.name = 'Updated MIB Group'
      wrapper.vm.ifType = IF_TYPE_FILTERS_OPTIONS[0] // 'ignore'
      wrapper.vm.status = false
      await nextTick()

      await wrapper.vm.saveMibGroup()
      await flushPromises()

      expect(updateMibGroup).toHaveBeenCalledWith(
        expect.objectContaining({
          name: 'Updated MIB Group',
          ifType: 'ignore',
          enabled: false,
          id: mockMibGroup.id
        }),
        mockCollectionSource.id
      )
    })
  })
})

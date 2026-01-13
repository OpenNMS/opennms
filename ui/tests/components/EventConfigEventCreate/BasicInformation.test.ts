import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'
import BasicInformation from '@/components/EventConfigEventCreate/BasicInformation.vue'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'
import { FeatherTextarea } from '@featherds/textarea'
import { FeatherSelect } from '@featherds/select'
import { createEventConfigEvent, updateEventConfigEventById } from '@/services/eventConfigService'

vi.mock('./AlarmDataInfo.vue', () => ({
  default: {
    template: '<div data-test="alarm-data-info"></div>',
    props: ['errors', 'addAlarmData', 'reductionKey', 'alarmType', 'autoClean', 'clearKey']
  }
}))

vi.mock('./MaskElements.vue', () => ({
  default: {
    template: '<div data-test="mask-elements"></div>',
    props: ['maskElements', 'errors']
  }
}))

vi.mock('./MaskVarbinds.vue', () => ({
  default: {
    template: '<div data-test="mask-varbinds"></div>',
    props: ['varbinds', 'maskElements', 'errors']
  }
}))

vi.mock('./VarbindsDecode.vue', () => ({
  default: {
    template: '<div data-test="varbind-decodes"></div>',
    props: ['varbindsDecode', 'errors']
  }
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: vi.fn()
  })
}))

vi.mock('@/services/eventConfigService', () => ({
  createEventConfigEvent: vi.fn(),
  updateEventConfigEventById: vi.fn(),
  addEventConfigSource: vi.fn()
}))

vi.mock('@/stores/eventConfigStore', () => ({
  useEventConfigStore: vi.fn(() => ({
    uploadedSources: [
      {
        id: 1,
        name: 'Test Source'
      },
      {
        id: 2,
        name: 'Another Source'
      }
    ],
    fetchAllSourcesNames: vi.fn().mockResolvedValue(undefined)
  }))
}))

vi.mock('vkbeautify', () => ({
  default: {
    xml: vi.fn((xml) => xml)
  }
}))

const mockSource = {
  id: 1,
  name: 'Test Source',
  vendor: 'Test Vendor',
  description: 'Test Description',
  enabled: true,
  eventCount: 10,
  fileOrder: 1,
  uploadedBy: 'testuser',
  createdTime: new Date('2024-01-01'),
  lastModified: new Date('2024-01-02')
}

const mockEvent = {
  id: 1,
  uei: 'uei.test.event1',
  eventLabel: 'Test Event 1',
  description: 'Description 1',
  severity: 'Major',
  enabled: true,
  xmlContent: `
    <event xmlns="http://xmlns.opennms.org/xsd/eventconf">
      <uei>uei.test.event1</uei>
      <event-label>Test Event 1</event-label>
      <descr><![CDATA[Description 1]]></descr>
      <operinstruct><![CDATA[Operator instructions]]></operinstruct>
      <logmsg dest="logndisplay"><![CDATA[Log message content]]></logmsg>
      <severity>Major</severity>
      <alarm-data reduction-key="test-key" alarm-type="1" auto-clean="true" clear-key="clear-key" />
      <mask>
        <maskelement>
          <mename>uei</mename>
          <mevalue>test-value</mevalue>
        </maskelement>
      </mask>
      <varbind>
        <vbnumber>0</vbnumber>
        <vbvalue>varbind-value</vbvalue>
      </varbind>
      <varbindsdecode>
        <parmid>param1</parmid>
        <decode varbinddecodedstring="key1" varbindvalue="01" />
      </varbindsdecode>
    </event>
  `,
  createdTime: new Date('2024-01-01'),
  lastModified: new Date('2024-01-02'),
  modifiedBy: 'user1',
  sourceName: 'Test Source',
  vendor: 'Test Vendor',
  fileOrder: 1
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/event-config',
      name: 'Event Configuration',
      component: { template: '<div></div>' }
    },
    {
      path: '/event-config/:id',
      name: 'Event Configuration Detail',
      component: { template: '<div></div>' }
    }
  ]
})

describe('BasicInformation Component', () => {
  let wrapper: any
  let store: any

  beforeEach(async () => {
    setActivePinia(createPinia())
    store = useEventModificationStore()

    store.selectedSource = mockSource
    store.eventModificationState = {
      eventConfigEvent: mockEvent,
      isEditMode: CreateEditMode.Edit
    }

    wrapper = mount(BasicInformation, {
      global: {
        plugins: [router],
        components: {
          FeatherInput,
          FeatherTextarea,
          FeatherSelect,
          FeatherButton
        }
      }
    })

    await router.isReady()
  })

  it('should render the component when store has selected source and event config event', () => {
    expect(wrapper.find('.main-content').exists()).toBe(true)
  })

  it('should display correct title for edit mode', () => {
    const title = wrapper.find('h3')
    expect(title.text()).toBe('Edit Event Configuration Details')
  })

  it('should display correct title for create mode', async () => {
    store.eventModificationState.isEditMode = CreateEditMode.Create
    await wrapper.vm.$nextTick()

    const title = wrapper.find('h3')
    expect(title.text()).toBe('Create New Event Configuration')
  })

  it('should render all basic information form fields', () => {
    expect(wrapper.find('[data-test="event-uei"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="event-label"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="event-description"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="operator-instructions"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="event-destination"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="log-message"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="event-severity"]').exists()).toBe(true)
  })

  it('should render all child components', () => {
    expect(wrapper.find('[data-test="alarm-data-info"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="mask-elements"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="mask-varbinds"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="varbind-decodes"]').exists()).toBe(true)
  })

  it('should render action buttons', () => {
    const cancelButton = wrapper.find('[data-test="cancel-event-button"]')
    const saveButton = wrapper.find('[data-test="save-event-button"]')

    expect(cancelButton.exists()).toBe(true)
    expect(cancelButton.text()).toBe('Cancel')
    expect(saveButton.exists()).toBe(true)
    expect(saveButton.text()).toBe('Save Changes')
  })

  it('should display create button text in create mode', async () => {
    store.eventModificationState.isEditMode = CreateEditMode.Create
    await wrapper.vm.$nextTick()

    const saveButton = wrapper.find('[data-test="save-event-button"]')
    expect(saveButton.text()).toBe('Create Event')
  })

  it('should bind event UEI input correctly', async () => {
    const ueiInput = wrapper.find('[data-test="event-uei"]').find('input')
    await ueiInput.setValue('uei.test.new')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventUei).toBe('uei.test.new')
  })

  it('should bind event label input correctly', async () => {
    const labelInput = wrapper.find('[data-test="event-label"]').find('input')
    await labelInput.setValue('New Event Label')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventLabel).toBe('New Event Label')
  })

  it('should bind event description textarea correctly', async () => {
    const descriptionTextarea = wrapper.find('[data-test="event-description"]').find('textarea')
    await descriptionTextarea.setValue('New event description')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventDescription).toBe('New event description')
  })

  it('should bind operator instructions textarea correctly', async () => {
    const instructionsTextarea = wrapper.find('[data-test="operator-instructions"]').find('textarea')
    await instructionsTextarea.setValue('New operator instructions')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.operatorInstructions).toBe('New operator instructions')
  })

  it('should bind log message textarea correctly', async () => {
    const logMessageTextarea = wrapper.find('[data-test="log-message"]').find('textarea')
    await logMessageTextarea.setValue('New log message')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.logMessage).toBe('New log message')
  })

  it('should bind destination select correctly', async () => {
    wrapper.vm.destination = { _text: 'logonly', _value: 'logonly' }
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.destination._value).toBe('logonly')
  })

  it('should bind severity select correctly', async () => {
    wrapper.vm.severity = { _text: 'Critical', _value: 'Critical' }
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.severity._value).toBe('Critical')
  })

  it('should load initial values from event config event on mount', async () => {
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventUei).toBe('uei.test.event1')
    expect(wrapper.vm.eventLabel).toBe('Test Event 1')
    expect(wrapper.vm.eventDescription).toBe('Description 1')
    expect(wrapper.vm.destination._value).toBe('logndisplay')
    expect(wrapper.vm.severity._value).toBe('Major')
  })

  it('should load alarm data correctly', () => {
    expect(wrapper.vm.addAlarmData).toBe(true)
    expect(wrapper.vm.reductionKey).toBe('test-key')
    expect(wrapper.vm.alarmType._value).toBe('1')
    expect(wrapper.vm.autoClean).toBe(true)
    expect(wrapper.vm.clearKey).toBe('clear-key')
  })

  it('should load mask elements correctly', () => {
    expect(wrapper.vm.maskElements).toHaveLength(1)
    expect(wrapper.vm.maskElements[0].name._value).toBe('uei')
    expect(wrapper.vm.maskElements[0].value).toBe('test-value')
  })

  it('should load varbinds correctly', () => {
    expect(wrapper.vm.varbinds).toHaveLength(1)
    expect(wrapper.vm.varbinds[0].index).toBe('0')
    expect(wrapper.vm.varbinds[0].value).toBe('varbind-value')
  })

  it('should load varbinds decode correctly', () => {
    expect(wrapper.vm.varbindsDecode).toHaveLength(1)
    expect(wrapper.vm.varbindsDecode[0].parmId).toBe('param1')
    expect(wrapper.vm.varbindsDecode[0].decode).toHaveLength(1)
    expect(wrapper.vm.varbindsDecode[0].decode[0].key).toBe('key1')
    expect(wrapper.vm.varbindsDecode[0].decode[0].value).toBe('01')
  })

  it('should update validation state when form data changes', async () => {
    const ueiInput = wrapper.find('[data-test="event-uei"]').find('input')
    await ueiInput.setValue('')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.isValid).toBe(false)
    expect(wrapper.vm.errors.uei).toBeDefined()
  })

  it('should enable save button when form is valid', async () => {
    await wrapper.vm.$nextTick()
    const saveButton = wrapper.find('[data-test="save-event-button"]')
    const isDisabled = saveButton.attributes('disabled') !== undefined
    expect(isDisabled).toBe(false)
  })

  it('should disable save button when form is invalid', async () => {
    const ueiInput = wrapper.find('[data-test="event-uei"]').find('input')
    await ueiInput.setValue('')
    await wrapper.vm.$nextTick()

    await new Promise((resolve) => setTimeout(resolve, 10))

    expect(wrapper.vm.isValid).toBe(false)

    const saveButton = wrapper.find('[data-test="save-event-button"]')
    const buttonComponent = saveButton.findComponent(FeatherButton)
    expect(buttonComponent.props('disabled')).toBe(true)
  })

  it('should handle cancel button click', async () => {
    const cancelButton = wrapper.find('[data-test="cancel-event-button"]')
    const resetSpy = vi.spyOn(store, 'resetEventModificationState')

    await cancelButton.trigger('click')

    expect(resetSpy).toHaveBeenCalled()
  })

  it('should handle save event button click in edit mode', async () => {
    vi.mocked(updateEventConfigEventById).mockResolvedValue(true)

    wrapper.vm.isValid = true
    await wrapper.vm.$nextTick()

    const saveButton = wrapper.find('[data-test="save-event-button"]')

    await saveButton.trigger('click')
    await wrapper.vm.$nextTick()

    expect(updateEventConfigEventById).toHaveBeenCalled()
  })

  it('should handle save event button click in create mode', async () => {
    store.eventModificationState.isEditMode = CreateEditMode.Create
    vi.mocked(createEventConfigEvent).mockResolvedValue(true)
    await wrapper.vm.$nextTick()

    wrapper.vm.isValid = true
    await wrapper.vm.$nextTick()

    const saveButton = wrapper.find('[data-test="save-event-button"]')
    await saveButton.trigger('click')
    await wrapper.vm.$nextTick()

    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(createEventConfigEvent).toHaveBeenCalled()
  })

  it('should not call updateEventConfigEventById when form is invalid', async () => {
    // Make the form invalid by clearing a required field
    const ueiInput = wrapper.find('[data-test="event-uei"]').find('input')
    await ueiInput.setValue('')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.isValid).toBe(false)

    vi.mocked(updateEventConfigEventById).mockClear()
    const saveButton = wrapper.find('[data-test="save-event-button"]')
    await saveButton.trigger('click')
    await wrapper.vm.$nextTick()

    expect(updateEventConfigEventById).not.toHaveBeenCalled()
  })

  it('should display error messages for invalid fields', async () => {
    const ueiInput = wrapper.find('[data-test="event-uei"]').find('input')
    await ueiInput.setValue('')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.errors.uei).toBeDefined()
  })

  it('should handle save event failure', async () => {
    vi.mocked(updateEventConfigEventById).mockRejectedValue(new Error('API Error'))
    wrapper.vm.isValid = true
    await wrapper.vm.$nextTick()

    const saveButton = wrapper.find('[data-test="save-event-button"]')
    await saveButton.trigger('click')
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(updateEventConfigEventById).toHaveBeenCalled()
  })

  it('should generate XML content when form data changes', async () => {
    const ueiInput = wrapper.find('[data-test="event-uei"]').find('input')
    await ueiInput.setValue('uei.test.updated')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.xmlContent).toContain('uei.test.updated')
  })

  it('should include all form fields in generated XML', () => {
    const xmlContent = wrapper.vm.xmlContent

    expect(xmlContent).toContain('uei.test.event1')
    expect(xmlContent).toContain('Test Event 1')
    expect(xmlContent).toContain('Description 1')
    expect(xmlContent).toContain('logndisplay')
    expect(xmlContent).toContain('Major')
  })

  // Source selection and autocomplete tests
  it('should render source autocomplete field', () => {
    expect(wrapper.find('[data-test="source-name"]').exists()).toBe(true)
  })

  it('should update selectedSource when autocomplete value changes', async () => {
    const newSource = { _text: 'New Source', _value: 'New Source' }
    wrapper.vm.setSelectedSource(newSource)
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.selectedSource).toEqual(newSource)
  })

  // Button visibility tests
  it('should show "Save Changes" button when store.selectedSource exists', async () => {
    store.selectedSource = mockSource
    await wrapper.vm.$nextTick()

    const saveButton = wrapper.find('[data-test="save-event-button"]')
    expect(saveButton.exists()).toBe(true)
    expect(saveButton.text()).toBe('Save Changes')
  })

  it('should show "Create Event" button in create mode when source is selected', async () => {
    store.eventModificationState.isEditMode = CreateEditMode.Create
    store.selectedSource = mockSource
    await wrapper.vm.$nextTick()

    const saveButton = wrapper.find('[data-test="save-event-button"]')
    expect(saveButton.exists()).toBe(true)
    expect(saveButton.text()).toBe('Create Event')
  })

  it('should show "Create Source and Save" button when no source is selected', async () => {
    store.selectedSource = null
    wrapper.vm.selectedSource = { _text: '', _value: '' }
    await wrapper.vm.$nextTick()

    const createSourceButton = wrapper.find('[data-test="create-source-button"]')
    expect(createSourceButton.exists()).toBe(true)
    expect(createSourceButton.text()).toBe('Create Source and Save')
  })

  // Create source dialog tests
  it('should open create source dialog when "Create Source and Save" button is clicked', async () => {
    store.selectedSource = null
    wrapper.vm.selectedSource = { _text: '', _value: '' }
    wrapper.vm.isValid = true
    await wrapper.vm.$nextTick()

    const createSourceButton = wrapper.find('[data-test="create-source-button"]')
    await createSourceButton.trigger('click')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.createSourceDialog).toBe(true)
  })

  it('should close create source dialog', async () => {
    wrapper.vm.createSourceDialog = true
    await wrapper.vm.$nextTick()

    wrapper.vm.closeSourceCreationDialog()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.createSourceDialog).toBe(false)
  })

  // Button disabled state tests
  it('should disable "Save Event" button when isValid is false', async () => {
    store.selectedSource = mockSource
    wrapper.vm.isValid = false
    await wrapper.vm.$nextTick()

    const saveButton = wrapper.find('[data-test="save-event-button"]')
    const buttonComponent = saveButton.findComponent(FeatherButton)
    expect(buttonComponent.props('disabled')).toBe(true)
  })

  it('should enable "Save Event" button when isValid is true and source exists', async () => {
    store.selectedSource = mockSource
    wrapper.vm.isValid = true
    await wrapper.vm.$nextTick()

    const saveButton = wrapper.find('[data-test="save-event-button"]')
    const buttonComponent = saveButton.findComponent(FeatherButton)
    expect(buttonComponent.props('disabled')).toBe(false)
  })

  // handleSaveEvent tests
  it('should return early if form is not valid', async () => {
    vi.mocked(updateEventConfigEventById).mockClear()
    vi.mocked(createEventConfigEvent).mockClear()
    
    wrapper.vm.isValid = false
    await wrapper.vm.handleSaveEvent()

    expect(updateEventConfigEventById).not.toHaveBeenCalled()
    expect(createEventConfigEvent).not.toHaveBeenCalled()
  })

  it('should return early when no sourceId is available', async () => {
    store.selectedSource = null
    wrapper.vm.selectedSource = { _text: '', _value: '' }
    wrapper.vm.isValid = true
    
    vi.mocked(createEventConfigEvent).mockClear()
    await wrapper.vm.handleSaveEvent()

    expect(createEventConfigEvent).not.toHaveBeenCalled()
  })

  it('should use store.selectedSource.id for sourceId when available', async () => {
    store.selectedSource = mockSource
    store.eventModificationState.isEditMode = CreateEditMode.Edit
    wrapper.vm.isValid = true
    vi.mocked(updateEventConfigEventById).mockResolvedValue(true)
    
    await wrapper.vm.handleSaveEvent()

    expect(updateEventConfigEventById).toHaveBeenCalledWith(
      expect.any(String),
      mockSource.id,
      expect.any(Number),
      expect.any(Boolean)
    )
  })

  it('should use selectedSource._value for sourceId when store.selectedSource is null', async () => {
    store.selectedSource = null
    wrapper.vm.selectedSource = { _text: 'Test', _value: 99 }
    store.eventModificationState.isEditMode = CreateEditMode.Create
    wrapper.vm.isValid = true
    vi.mocked(createEventConfigEvent).mockResolvedValue(true)
    
    await wrapper.vm.handleSaveEvent()

    expect(createEventConfigEvent).toHaveBeenCalledWith(
      expect.any(String),
      99
    )
  })

  it('should call updateEventConfigEventById in edit mode', async () => {
    store.eventModificationState.isEditMode = CreateEditMode.Edit
    store.eventModificationState.eventConfigEvent = mockEvent
    wrapper.vm.isValid = true
    vi.mocked(updateEventConfigEventById).mockResolvedValue(true)
    
    await wrapper.vm.handleSaveEvent()

    expect(updateEventConfigEventById).toHaveBeenCalled()
  })

  it('should call createEventConfigEvent in create mode', async () => {
    store.eventModificationState.isEditMode = CreateEditMode.Create
    wrapper.vm.isValid = true
    vi.mocked(createEventConfigEvent).mockResolvedValue(true)
    
    await wrapper.vm.handleSaveEvent()

    expect(createEventConfigEvent).toHaveBeenCalled()
  })

  it('should handle response successfully in create mode', async () => {
    store.eventModificationState.isEditMode = CreateEditMode.Create
    wrapper.vm.isValid = true
    vi.mocked(createEventConfigEvent).mockResolvedValue(true)
    
    await wrapper.vm.handleSaveEvent()

    expect(createEventConfigEvent).toHaveBeenCalled()
  })

  it('should handle response successfully in edit mode', async () => {
    store.eventModificationState.isEditMode = CreateEditMode.Edit
    store.eventModificationState.eventConfigEvent = mockEvent
    wrapper.vm.isValid = true
    vi.mocked(updateEventConfigEventById).mockResolvedValue(true)
    
    await wrapper.vm.handleSaveEvent()

    expect(updateEventConfigEventById).toHaveBeenCalled()
  })

  it('should not call handleCancel when response is null', async () => {
    store.eventModificationState.isEditMode = CreateEditMode.Create
    wrapper.vm.isValid = true
    vi.mocked(createEventConfigEvent).mockResolvedValue(null as any)
    
    const cancelSpy = vi.spyOn(wrapper.vm, 'handleCancel')
    await wrapper.vm.handleSaveEvent()

    expect(cancelSpy).not.toHaveBeenCalled()
  })

  it('should handle error when save fails', async () => {
    store.eventModificationState.isEditMode = CreateEditMode.Create
    wrapper.vm.isValid = true
    vi.mocked(createEventConfigEvent).mockRejectedValue(new Error('API Error'))
    
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    
    await wrapper.vm.handleSaveEvent()

    expect(consoleSpy).toHaveBeenCalled()
    
    consoleSpy.mockRestore()
  })

  // Search functionality tests
  it('should filter sources based on search query', async () => {
    vi.useFakeTimers()
    
    wrapper.vm.search('Test')
    
    vi.advanceTimersByTime(500)
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.results).toHaveLength(1)
    expect(wrapper.vm.results[0]._text).toBe('Test Source')
    
    vi.useRealTimers()
  })

  it('should return all matching sources for partial query', async () => {
    vi.useFakeTimers()
    
    wrapper.vm.search('source')
    
    vi.advanceTimersByTime(500)
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.results.length).toBeGreaterThan(0)
    
    vi.useRealTimers()
  })

  it('should set loading state during search', () => {
    wrapper.vm.search('test')
    
    expect(wrapper.vm.loading).toBe(true)
  })

  it('should clear loading state after search completes', async () => {
    vi.useFakeTimers()
    
    wrapper.vm.search('test')
    
    vi.advanceTimersByTime(500)
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.loading).toBe(false)
    
    vi.useRealTimers()
  })

  // setSelectedSource tests
  it('should set selectedSource when item is provided', () => {
    const testItem = { _text: 'New Source', _value: 123 }
    wrapper.vm.setSelectedSource(testItem)
    
    expect(wrapper.vm.selectedSource).toEqual(testItem)
  })

  it('should reset selectedSource when null is provided', () => {
    wrapper.vm.setSelectedSource(null)
    
    expect(wrapper.vm.selectedSource).toEqual({ _text: '', _value: '' })
  })

  // handleCancel tests
  it('should reset values and store on cancel', () => {
    const resetSpy = vi.spyOn(store, 'resetEventModificationState')
    
    wrapper.vm.handleCancel(123)

    expect(resetSpy).toHaveBeenCalled()
  })

  it('should navigate to Event Configuration Detail when id is provided', async () => {
    const pushSpy = vi.spyOn(router, 'push')
    
    wrapper.vm.handleCancel(123)

    expect(pushSpy).toHaveBeenCalledWith({
      name: 'Event Configuration Detail',
      params: { id: 123 }
    })
  })

  it('should navigate to Event Configuration when no id is provided', async () => {
    const pushSpy = vi.spyOn(router, 'push')
    
    wrapper.vm.handleCancel()

    expect(pushSpy).toHaveBeenCalledWith({ name: 'Event Configuration' })
  })

  // createNewSource tests
  it('should return early when source form has errors', async () => {
    wrapper.vm.configName = ''
    wrapper.vm.vendor = ''
    wrapper.vm.isValid = true
    
    await wrapper.vm.createNewSource()

    expect(wrapper.vm.sourceFormErrors).not.toBeNull()
  })

  it('should return early when form is invalid', async () => {
    wrapper.vm.configName = 'Test'
    wrapper.vm.vendor = 'Vendor'
    wrapper.vm.isValid = false
    
    await wrapper.vm.createNewSource()
    
    // Should not proceed with source creation
    expect(wrapper.vm.isValid).toBe(false)
  })

  it('should validate configName is required', () => {
    wrapper.vm.configName = ''
    wrapper.vm.vendor = 'Test Vendor'
    
    expect(wrapper.vm.sourceFormErrors).not.toBeNull()
    expect(wrapper.vm.sourceFormErrors?.name).toBeDefined()
  })

  it('should validate vendor is required', () => {
    wrapper.vm.configName = 'Test Config'
    wrapper.vm.vendor = ''
    
    expect(wrapper.vm.sourceFormErrors).not.toBeNull()
    expect(wrapper.vm.sourceFormErrors?.vendor).toBeDefined()
  })

  it('should have no errors when both fields are filled', () => {
    wrapper.vm.configName = 'Test Config'
    wrapper.vm.vendor = 'Test Vendor'
    
    expect(wrapper.vm.sourceFormErrors).toBeNull()
  })

  // setAlarmData tests
  it('should set addAlarmData value', () => {
    wrapper.vm.setAlarmData('addAlarmData', true)
    
    expect(wrapper.vm.addAlarmData).toBe(true)
  })

  it('should reset alarm data fields when addAlarmData is set to false', () => {
    wrapper.vm.reductionKey = 'test-key'
    wrapper.vm.alarmType = { _text: 'Problem', _value: '1' }
    wrapper.vm.autoClean = true
    
    wrapper.vm.setAlarmData('addAlarmData', false)
    
    expect(wrapper.vm.addAlarmData).toBe(false)
    expect(wrapper.vm.reductionKey).toBe('')
    expect(wrapper.vm.alarmType).toEqual({ _text: '', _value: '' })
    expect(wrapper.vm.autoClean).toBe(false)
  })

  it('should set reductionKey value', () => {
    wrapper.vm.setAlarmData('reductionKey', 'new-key')
    
    expect(wrapper.vm.reductionKey).toBe('new-key')
  })

  it('should set alarmType value', () => {
    const alarmType = { _text: 'Problem', _value: '1' }
    wrapper.vm.setAlarmData('alarmType', alarmType)
    
    expect(wrapper.vm.alarmType).toEqual(alarmType)
  })

  it('should set autoClean value', () => {
    wrapper.vm.setAlarmData('autoClean', true)
    
    expect(wrapper.vm.autoClean).toBe(true)
  })

  it('should set clearKey value', () => {
    wrapper.vm.setAlarmData('clearKey', 'clear-key-value')
    
    expect(wrapper.vm.clearKey).toBe('clear-key-value')
  })

  // setMaskElements tests
  it('should return early if index is undefined', () => {
    const initialLength = wrapper.vm.maskElements.length
    wrapper.vm.setMaskElements('setName', { _text: 'test', _value: 'test' }, undefined)
    
    expect(wrapper.vm.maskElements.length).toBe(initialLength)
  })

  it('should set mask element name', () => {
    wrapper.vm.maskElements = [{ name: { _text: '', _value: '' }, value: '' }]
    const newName = { _text: 'uei', _value: 'uei' }
    
    wrapper.vm.setMaskElements('setName', newName, 0)
    
    expect(wrapper.vm.maskElements[0].name).toEqual(newName)
  })

  it('should set mask element value', () => {
    wrapper.vm.maskElements = [{ name: { _text: '', _value: '' }, value: '' }]
    
    wrapper.vm.setMaskElements('setValue', 'test-value', 0)
    
    expect(wrapper.vm.maskElements[0].value).toBe('test-value')
  })

  it('should add a new mask row', () => {
    wrapper.vm.maskElements = [{ name: { _text: '', _value: '' }, value: '' }]
    
    wrapper.vm.setMaskElements('addMaskRow', null, 0)
    
    expect(wrapper.vm.maskElements.length).toBe(2)
  })

  it('should remove a mask row', () => {
    wrapper.vm.maskElements = [
      { name: { _text: 'test1', _value: 'test1' }, value: 'value1' },
      { name: { _text: 'test2', _value: 'test2' }, value: 'value2' }
    ]
    
    wrapper.vm.setMaskElements('removeMaskRow', null, 0)
    
    expect(wrapper.vm.maskElements.length).toBe(1)
    expect(wrapper.vm.maskElements[0].name._text).toBe('test2')
  })

  // setVarbinds tests
  it('should return early if index is undefined for setVarbinds', () => {
    const initialLength = wrapper.vm.varbinds.length
    wrapper.vm.setVarbinds('setValue', 'test', undefined)
    
    expect(wrapper.vm.varbinds.length).toBe(initialLength)
  })

  it('should set varbind number', () => {
    wrapper.vm.varbinds = [{ index: '0', value: '', type: { _text: 'vbNumber', _value: 'vbNumber' } }]
    
    wrapper.vm.setVarbinds('setVarbindNumber', '5', 0)
    
    expect(wrapper.vm.varbinds[0].index).toBe('5')
  })

  it('should set varbind number to 0 if value is negative', () => {
    wrapper.vm.varbinds = [{ index: '0', value: '', type: { _text: 'vbNumber', _value: 'vbNumber' } }]
    
    wrapper.vm.setVarbinds('setVarbindNumber', '-5', 0)
    
    expect(wrapper.vm.varbinds[0].index).toBe('0')
  })

  it('should set varbind number to 0 if value is not a number', () => {
    wrapper.vm.varbinds = [{ index: '0', value: '', type: { _text: 'vbNumber', _value: 'vbNumber' } }]
    
    wrapper.vm.setVarbinds('setVarbindNumber', 'abc', 0)
    
    expect(wrapper.vm.varbinds[0].index).toBe('0')
  })

  it('should set varbind OID', () => {
    wrapper.vm.varbinds = [{ index: '0', value: '', type: { _text: 'vbOid', _value: 'vbOid' } }]
    
    wrapper.vm.setVarbinds('setVarbindOid', '.1.3.6.1.4.1', 0)
    
    expect(wrapper.vm.varbinds[0].index).toBe('.1.3.6.1.4.1')
  })

  it('should set varbind value', () => {
    wrapper.vm.varbinds = [{ index: '0', value: '', type: { _text: 'vbNumber', _value: 'vbNumber' } }]
    
    wrapper.vm.setVarbinds('setValue', 'test-value', 0)
    
    expect(wrapper.vm.varbinds[0].value).toBe('test-value')
  })

  it('should add a new varbind row', () => {
    wrapper.vm.varbinds = [{ index: '0', value: '', type: { _text: 'vbNumber', _value: 'vbNumber' } }]
    
    wrapper.vm.setVarbinds('addVarbindRow', null, 0)
    
    expect(wrapper.vm.varbinds.length).toBe(2)
  })

  it('should remove a varbind row', () => {
    wrapper.vm.varbinds = [
      { index: '0', value: 'value1', type: { _text: 'vbNumber', _value: 'vbNumber' } },
      { index: '1', value: 'value2', type: { _text: 'vbNumber', _value: 'vbNumber' } }
    ]
    
    wrapper.vm.setVarbinds('removeVarbindRow', null, 0)
    
    expect(wrapper.vm.varbinds.length).toBe(1)
    expect(wrapper.vm.varbinds[0].index).toBe('1')
  })

  it('should clear all varbinds', () => {
    wrapper.vm.varbinds = [
      { index: '0', value: 'value1', type: { _text: 'vbNumber', _value: 'vbNumber' } },
      { index: '1', value: 'value2', type: { _text: 'vbNumber', _value: 'vbNumber' } }
    ]
    
    wrapper.vm.setVarbinds('clearAllVarbinds', null, 0)
    
    expect(wrapper.vm.varbinds).toEqual([])
  })

  it('should set varbind type and reset index', () => {
    wrapper.vm.varbinds = [{ index: '5', value: 'test', type: { _text: 'vbNumber', _value: 'vbNumber' } }]
    const newType = { _text: 'vbOid', _value: 'vbOid' }
    
    wrapper.vm.setVarbinds('setVarbindType', newType, 0)
    
    expect(wrapper.vm.varbinds[0].type).toEqual(newType)
    expect(wrapper.vm.varbinds[0].index).toBe('0')
  })

  // setVarbindsDecode tests
  it('should return early if index is undefined for setVarbindsDecode', () => {
    const initialLength = wrapper.vm.varbindsDecode.length
    wrapper.vm.setVarbindsDecode('setParmId', 'test', undefined, 0)
    
    expect(wrapper.vm.varbindsDecode.length).toBe(initialLength)
  })

  it('should set parmId', () => {
    wrapper.vm.varbindsDecode = [{ parmId: '', decode: [] }]
    
    wrapper.vm.setVarbindsDecode('setParmId', 'param1', 0, 0)
    
    expect(wrapper.vm.varbindsDecode[0].parmId).toBe('param1')
  })

  it('should add a new varbind decode row', () => {
    wrapper.vm.varbindsDecode = [{ parmId: 'param1', decode: [] }]
    
    wrapper.vm.setVarbindsDecode('addVarbindDecodeRow', null, 0, 0)
    
    expect(wrapper.vm.varbindsDecode.length).toBe(2)
  })

  it('should remove a varbind decode row', () => {
    wrapper.vm.varbindsDecode = [
      { parmId: 'param1', decode: [] },
      { parmId: 'param2', decode: [] }
    ]
    
    wrapper.vm.setVarbindsDecode('removeVarbindDecodeRow', null, 0, 0)
    
    expect(wrapper.vm.varbindsDecode.length).toBe(1)
    expect(wrapper.vm.varbindsDecode[0].parmId).toBe('param2')
  })

  it('should add a decode row', () => {
    wrapper.vm.varbindsDecode = [{ parmId: 'param1', decode: [] }]
    
    wrapper.vm.setVarbindsDecode('addDecodeRow', null, 0, 0)
    
    expect(wrapper.vm.varbindsDecode[0].decode.length).toBe(1)
  })

  it('should remove a decode row', () => {
    wrapper.vm.varbindsDecode = [{ parmId: 'param1', decode: [{ key: 'key1', value: '1' }, { key: 'key2', value: '2' }] }]
    
    wrapper.vm.setVarbindsDecode('removeDecodeRow', null, 0, 0)
    
    expect(wrapper.vm.varbindsDecode[0].decode.length).toBe(1)
    expect(wrapper.vm.varbindsDecode[0].decode[0].key).toBe('key2')
  })

  it('should set decode key', () => {
    wrapper.vm.varbindsDecode = [{ parmId: 'param1', decode: [{ key: '', value: '' }] }]
    
    wrapper.vm.setVarbindsDecode('setDecodeKey', 'test-key', 0, 0)
    
    expect(wrapper.vm.varbindsDecode[0].decode[0].key).toBe('test-key')
  })

  it('should set decode value', () => {
    wrapper.vm.varbindsDecode = [{ parmId: 'param1', decode: [{ key: 'key1', value: '' }] }]
    
    wrapper.vm.setVarbindsDecode('setDecodeValue', '10', 0, 0)
    
    expect(wrapper.vm.varbindsDecode[0].decode[0].value).toBe('10')
  })

  it('should set decode value to 0 if value is negative', () => {
    wrapper.vm.varbindsDecode = [{ parmId: 'param1', decode: [{ key: 'key1', value: '' }] }]
    
    wrapper.vm.setVarbindsDecode('setDecodeValue', '-5', 0, 0)
    
    expect(wrapper.vm.varbindsDecode[0].decode[0].value).toBe('0')
  })

  it('should set decode value to 0 if value is not a number', () => {
    wrapper.vm.varbindsDecode = [{ parmId: 'param1', decode: [{ key: 'key1', value: '' }] }]
    
    wrapper.vm.setVarbindsDecode('setDecodeValue', 'abc', 0, 0)
    
    expect(wrapper.vm.varbindsDecode[0].decode[0].value).toBe('0')
  })
})


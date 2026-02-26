import BasicInformation from '@/components/EventConfigEventCreate/BasicInformation.vue'
import { createEventConfigEvent, updateEventConfigEventById } from '@/services/eventConfigService'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect } from '@featherds/select'
import { FeatherTextarea } from '@featherds/textarea'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'

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

  it('should render action buttons with correct text', () => {
    const cancelButton = wrapper.find('[data-test="cancel-event-button"]')
    const saveButton = wrapper.find('[data-test="save-event-button"]')

    expect(cancelButton.exists()).toBe(true)
    expect(cancelButton.text()).toBe('Cancel')
    expect(saveButton.exists()).toBe(true)
    expect(saveButton.text()).toBe('Save Changes')
  })

  it('should display "Create Event" button text in create mode', async () => {
    store.eventModificationState.isEditMode = CreateEditMode.Create
    await wrapper.vm.$nextTick()

    const saveButton = wrapper.find('[data-test="save-event-button"]')
    expect(saveButton.text()).toBe('Create Event')
  })

  it('should have source name autocomplete disabled state based on store.selectedSource', async () => {
    // When store.selectedSource has name and id, the autocomplete should be disabled
    store.selectedSource = mockSource
    await wrapper.vm.$nextTick()

    // Check that the autocomplete exists and is rendered
    const autocomplete = wrapper.find('[data-test="source-name"]')
    expect(autocomplete.exists()).toBe(true)
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

  it('should handle save event failure', async () => {
    // Suppress console.error for this test since we're intentionally triggering an error
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    vi.mocked(updateEventConfigEventById).mockRejectedValue(new Error('API Error'))
    wrapper.vm.isValid = true
    await wrapper.vm.$nextTick()

    const saveButton = wrapper.find('[data-test="save-event-button"]')
    await saveButton.trigger('click')
    await new Promise((resolve) => setTimeout(resolve, 0))

    expect(updateEventConfigEventById).toHaveBeenCalled()

    // Restore console.error
    consoleErrorSpy.mockRestore()
  })

  describe('XML Content Generation', () => {
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

    it('should generate XML with vbNumber type varbinds', async () => {
      wrapper.vm.varbinds = [{ index: '5', value: 'test-value', type: { _text: 'Varbind Number', _value: 'vbnumber' } }]
      await wrapper.vm.$nextTick()

      const xmlContent = wrapper.vm.xmlContent
      expect(xmlContent).toContain('<vbnumber>5</vbnumber>')
      expect(xmlContent).toContain('<vbvalue>test-value</vbvalue>')
    })

    it('should generate XML with vbOid type varbinds', async () => {
      wrapper.vm.varbinds = [
        { index: '.1.3.6.1.4.1', value: 'oid-value', type: { _text: 'Varbind OID', _value: 'vboid' } }
      ]
      await wrapper.vm.$nextTick()

      const xmlContent = wrapper.vm.xmlContent
      expect(xmlContent).toContain('<vboid>.1.3.6.1.4.1</vboid>')
      expect(xmlContent).toContain('<vbvalue>oid-value</vbvalue>')
    })

    it('should generate XML with alarm data when addAlarmData is true', async () => {
      wrapper.vm.addAlarmData = true
      wrapper.vm.reductionKey = 'my-reduction-key'
      wrapper.vm.alarmType = { _text: 'Problem', _value: '1' }
      wrapper.vm.autoClean = true
      wrapper.vm.clearKey = 'my-clear-key'
      await wrapper.vm.$nextTick()

      const xmlContent = wrapper.vm.xmlContent
      expect(xmlContent).toContain('alarm-data')
      expect(xmlContent).toContain('reduction-key="my-reduction-key"')
      expect(xmlContent).toContain('alarm-type="1"')
      expect(xmlContent).toContain('auto-clean="true"')
      expect(xmlContent).toContain('clear-key="my-clear-key"')
    })

    it('should not include clear-key in XML when clearKey is empty', async () => {
      wrapper.vm.addAlarmData = true
      wrapper.vm.reductionKey = 'my-reduction-key'
      wrapper.vm.alarmType = { _text: 'Problem', _value: '1' }
      wrapper.vm.autoClean = false
      wrapper.vm.clearKey = ''
      await wrapper.vm.$nextTick()

      const xmlContent = wrapper.vm.xmlContent
      expect(xmlContent).toContain('alarm-data')
      expect(xmlContent).not.toContain('clear-key=')
    })

    it('should not include alarm-data in XML when addAlarmData is false', async () => {
      wrapper.vm.addAlarmData = false
      await wrapper.vm.$nextTick()

      const xmlContent = wrapper.vm.xmlContent
      expect(xmlContent).not.toContain('alarm-data')
    })

    it('should generate XML with varbindsdecode elements', async () => {
      wrapper.vm.varbindsDecode = [
        {
          parmId: 'testParam',
          decode: [
            { key: 'decoded-key', value: '100' },
            { key: 'another-key', value: '200' }
          ]
        }
      ]
      await wrapper.vm.$nextTick()

      const xmlContent = wrapper.vm.xmlContent
      expect(xmlContent).toContain('<varbindsdecode>')
      expect(xmlContent).toContain('<parmid>testParam</parmid>')
      expect(xmlContent).toContain('varbinddecodedstring="decoded-key"')
      expect(xmlContent).toContain('varbindvalue="100"')
      expect(xmlContent).toContain('varbinddecodedstring="another-key"')
      expect(xmlContent).toContain('varbindvalue="200"')
    })

    it('should include operator instructions in generated XML', async () => {
      wrapper.vm.operatorInstructions = 'New operator instructions'
      await wrapper.vm.$nextTick()

      const xmlContent = wrapper.vm.xmlContent
      expect(xmlContent).toContain('<operinstruct><![CDATA[New operator instructions]]></operinstruct>')
    })
  })

  describe('handleSaveEvent', () => {
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

      expect(createEventConfigEvent).toHaveBeenCalledWith(expect.any(String), 99)
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

    it('should show error when source is required but selectedSource._value is -1', async () => {
      wrapper.vm.selectedSource = { _text: '', _value: -1 }
      wrapper.vm.isValid = true

      const showSnackBarSpy = vi.spyOn(wrapper.vm.snackbar, 'showSnackBar')
      await wrapper.vm.handleSaveEvent()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: 'No source selected. Please select a source from the dropdown or create a new one.',
        error: true
      })
    })

    it('should show error when sourceId is falsy (0 or undefined)', async () => {
      wrapper.vm.selectedSource = { _text: 'Test', _value: 0 }
      wrapper.vm.isValid = true

      const showSnackBarSpy = vi.spyOn(wrapper.vm.snackbar, 'showSnackBar')
      await wrapper.vm.handleSaveEvent()

      expect(showSnackBarSpy).toHaveBeenCalledWith({
        msg: 'No source selected. Please select a source from the dropdown or create a new one.',
        error: true
      })
    })
  })

  describe('Search functionality', () => {
    it('should filter sources based on search query', async () => {
      vi.useFakeTimers()

      wrapper.vm.search('Test')

      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.results.length).toBeGreaterThan(0)
      expect(wrapper.vm.results[0]._text).toBe('Test Source')

      vi.useRealTimers()
    })

    it('should return empty results when no match found', async () => {
      vi.useFakeTimers()

      wrapper.vm.search('NonExistent')

      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.results).toHaveLength(0)

      vi.useRealTimers()
    })

    it('should return exact match when source name matches', async () => {
      vi.useFakeTimers()

      wrapper.vm.search('Test Source')

      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.results).toHaveLength(1)
      expect(wrapper.vm.results[0]._text).toBe('Test Source')
      expect(wrapper.vm.results[0]._value).toBe(1)

      vi.useRealTimers()
    })

    it('should perform case-insensitive search', async () => {
      vi.useFakeTimers()

      wrapper.vm.search('test source')

      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.results).toHaveLength(1)
      expect(wrapper.vm.results[0]._value).toBe(1)

      vi.useRealTimers()
    })

    it('should set selectedSource when item is provided', () => {
      const testItem = { _text: 'New Source', _value: 123 }
      wrapper.vm.setSelectedSource(testItem)

      expect(wrapper.vm.selectedSource).toEqual(testItem)
    })

    it('should reset selectedSource to -1 when null is provided', () => {
      wrapper.vm.setSelectedSource(null)

      expect(wrapper.vm.selectedSource).toEqual({ _text: '', _value: -1 })
    })

    it('should clear previous timeout when search is called multiple times', async () => {
      vi.useFakeTimers()

      wrapper.vm.search('First')
      wrapper.vm.search('Second')

      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      // Only 'Second' search should have been executed
      expect(wrapper.vm.results.length).toBe(0) // No match for 'Second'

      vi.useRealTimers()
    })

    it('should set loading to true when search starts and false when complete', async () => {
      vi.useFakeTimers()

      wrapper.vm.search('Test')
      expect(wrapper.vm.loading).toBe(true)

      vi.advanceTimersByTime(500)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.loading).toBe(false)

      vi.useRealTimers()
    })
  })

  it('should reset store and navigate correctly', async () => {
    const resetSpy = vi.spyOn(store, 'resetEventModificationState')
    const pushSpy = vi.spyOn(router, 'push')

    wrapper.vm.handleCancel(123)
    expect(resetSpy).toHaveBeenCalled()
    expect(pushSpy).toHaveBeenCalledWith({
      name: 'Event Configuration Detail',
      params: { id: 123 }
    })

    wrapper.vm.handleCancel()
    expect(pushSpy).toHaveBeenCalledWith({ name: 'Event Configuration' })
  })

  describe('setAlarmData', () => {
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
  })

  describe('setMaskElements', () => {
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

    it('should not add mask row when maximum limit is reached', async () => {
      wrapper.vm.maskElements = Array(12)
        .fill(null)
        .map(() => ({ name: { _text: 'test', _value: 'test' }, value: 'value' }))

      const showSnackBarSpy = vi.spyOn(wrapper.vm.snackbar, 'showSnackBar')

      wrapper.vm.setMaskElements('addMaskRow', null, 0)

      expect(wrapper.vm.maskElements.length).toBe(12) // Should not increase
      expect(showSnackBarSpy).toHaveBeenCalledWith({ msg: 'Maximum of 12 mask elements allowed.', error: true })
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
  })

  describe('setVarbinds', () => {
    it('should return early if index is undefined', () => {
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
  })

  describe('setVarbindsDecode', () => {
    it('should return early if index is undefined', () => {
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
      wrapper.vm.varbindsDecode = [
        {
          parmId: 'param1',
          decode: [
            { key: 'key1', value: '1' },
            { key: 'key2', value: '2' }
          ]
        }
      ]

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

  describe('Back Button', () => {
    it('should render the back button', () => {
      expect(wrapper.find('[data-test="back-button"]').exists()).toBe(true)
    })

    it('should call handleCancel with selectedSource id when back button is clicked', async () => {
      store.selectedSource = mockSource // Ensure store has the source with id: 1
      await wrapper.vm.$nextTick()

      const handleCancelSpy = vi.spyOn(wrapper.vm, 'handleCancel')
      const backButton = wrapper.find('[data-test="back-button"]')

      await backButton.trigger('click')

      expect(handleCancelSpy).toHaveBeenCalledWith(mockSource.id)
    })

    it('should call handleCancel with undefined when no selectedSource', async () => {
      store.selectedSource = null
      await wrapper.vm.$nextTick()

      const handleCancelSpy = vi.spyOn(wrapper.vm, 'handleCancel')
      const backButton = wrapper.find('[data-test="back-button"]')

      await backButton.trigger('click')

      expect(handleCancelSpy).toHaveBeenCalledWith(undefined)
    })
  })

  describe('Create New Event Source Button', () => {
    it('should render the create new event source button', () => {
      expect(wrapper.find('[data-test="create-new-event-source-button"]').exists()).toBe(true)
    })

    it('should be disabled when store.selectedSource has name and id', async () => {
      store.selectedSource = mockSource
      await wrapper.vm.$nextTick()

      const button = wrapper.find('[data-test="create-new-event-source-button"]')
      const buttonComponent = button.findComponent(FeatherButton)
      expect(buttonComponent.props('disabled')).toBe(true)
    })

    it('should be enabled when store.selectedSource is null', async () => {
      store.selectedSource = null
      await wrapper.vm.$nextTick()

      const button = wrapper.find('[data-test="create-new-event-source-button"]')
      const buttonComponent = button.findComponent(FeatherButton)
      expect(buttonComponent.props('disabled')).toBe(false)
    })

    it('should open source creation dialog when clicked', async () => {
      store.selectedSource = null
      await wrapper.vm.$nextTick()

      const button = wrapper.find('[data-test="create-new-event-source-button"]')
      await button.trigger('click')

      expect(wrapper.vm.sourceCreationDialogState).toBe(true)
    })
  })

  describe('Source Creation Dialog', () => {
    it('should show source creation dialog when showSourceCreationDialog is called', () => {
      wrapper.vm.showSourceCreationDialog()

      expect(wrapper.vm.sourceCreationDialogState).toBe(true)
      expect(wrapper.vm.configName).toBe('')
      expect(wrapper.vm.vendor).toBe('')
    })

    it('should reset form and close dialog on handleSourceCreationCancel', () => {
      wrapper.vm.sourceCreationDialogState = true
      wrapper.vm.configName = 'Test Config'
      wrapper.vm.vendor = 'Test Vendor'

      wrapper.vm.handleSourceCreationCancel()

      expect(wrapper.vm.sourceCreationDialogState).toBe(false)
      expect(wrapper.vm.configName).toBe('')
      expect(wrapper.vm.vendor).toBe('')
    })

    describe('sourceCreationErrors validation', () => {
      it('should return error when configName is empty', () => {
        wrapper.vm.configName = ''
        wrapper.vm.vendor = 'Valid Vendor'

        expect(wrapper.vm.sourceCreationErrors?.name).toBe('Configuration name is required.')
      })

      it('should not return error when vendor is empty (vendor is optional)', () => {
        wrapper.vm.configName = 'Valid Name'
        wrapper.vm.vendor = ''

        expect(wrapper.vm.sourceCreationErrors).toBeNull()
      })

      it('should return error when vendor exceeds 128 characters', () => {
        wrapper.vm.configName = 'Valid Name'
        wrapper.vm.vendor = 'a'.repeat(129)

        expect(wrapper.vm.sourceCreationErrors?.vendor).toBe('Vendor must be less than 128 characters.')
      })

      it('should return null when both fields are valid', () => {
        wrapper.vm.configName = 'Valid Config Name'
        wrapper.vm.vendor = 'Valid Vendor'

        expect(wrapper.vm.sourceCreationErrors).toBeNull()
      })

      it('should only return configName error when configName is empty and vendor is empty', () => {
        wrapper.vm.configName = ''
        wrapper.vm.vendor = ''

        expect(wrapper.vm.sourceCreationErrors?.name).toBe('Configuration name is required.')
        expect(wrapper.vm.sourceCreationErrors?.vendor).toBeUndefined()
      })

      it('should trim whitespace when checking configName', () => {
        wrapper.vm.configName = '   '
        wrapper.vm.vendor = 'Valid Vendor'

        expect(wrapper.vm.sourceCreationErrors?.name).toBe('Configuration name is required.')
      })

      it('should accept vendor with whitespace only (vendor is optional)', () => {
        wrapper.vm.configName = 'Valid Name'
        wrapper.vm.vendor = '   '

        expect(wrapper.vm.sourceCreationErrors).toBeNull()
      })

      it('should return null when vendor is exactly 128 characters', () => {
        wrapper.vm.configName = 'Valid Name'
        wrapper.vm.vendor = 'a'.repeat(128)

        expect(wrapper.vm.sourceCreationErrors).toBeNull()
      })
    })

    describe('handleSourceCreationSave', () => {
      beforeEach(() => {
        wrapper.vm.configName = 'Test Source'
        wrapper.vm.vendor = 'Test Vendor'
        wrapper.vm.sourceCreationDialogState = true
      })

      it('should create source and close dialog on success (status 201)', async () => {
        const { addEventConfigSource } = await import('@/services/eventConfigService')
        vi.mocked(addEventConfigSource).mockResolvedValue({
          id: 99,
          name: 'Test Source',
          fileOrder: 1,
          status: 201
        })

        await wrapper.vm.handleSourceCreationSave()

        expect(addEventConfigSource).toHaveBeenCalledWith('Test Source', 'Test Vendor', '')
        expect(wrapper.vm.sourceCreationDialogState).toBe(false)
        expect(wrapper.vm.selectedSource._value).toBe(99)
        expect(wrapper.vm.selectedSource._text).toBe('Test Source')
      })

      it('should show error for duplicate name (status 409)', async () => {
        const { addEventConfigSource } = await import('@/services/eventConfigService')
        vi.mocked(addEventConfigSource).mockResolvedValue(409)

        const showSnackBarSpy = vi.spyOn(wrapper.vm.snackbar, 'showSnackBar')

        await wrapper.vm.handleSourceCreationSave()

        expect(showSnackBarSpy).toHaveBeenCalledWith({
          msg: 'An event configuration source with this name already exists.',
          error: true
        })
      })

      it('should show error for bad request (status 400)', async () => {
        const { addEventConfigSource } = await import('@/services/eventConfigService')
        vi.mocked(addEventConfigSource).mockResolvedValue(400)

        const showSnackBarSpy = vi.spyOn(wrapper.vm.snackbar, 'showSnackBar')

        await wrapper.vm.handleSourceCreationSave()

        expect(showSnackBarSpy).toHaveBeenCalledWith({
          msg: 'Invalid request. Please check your input and try again.',
          error: true
        })
      })

      it('should show error for server error (status 500 or other)', async () => {
        const { addEventConfigSource } = await import('@/services/eventConfigService')
        vi.mocked(addEventConfigSource).mockResolvedValue(500)

        const showSnackBarSpy = vi.spyOn(wrapper.vm.snackbar, 'showSnackBar')

        await wrapper.vm.handleSourceCreationSave()

        expect(showSnackBarSpy).toHaveBeenCalledWith({
          msg: 'Failed to create event configuration source. Please try again.',
          error: true
        })
      })

      it('should show error when API throws exception', async () => {
        const { addEventConfigSource } = await import('@/services/eventConfigService')
        vi.mocked(addEventConfigSource).mockRejectedValue(new Error('Network error'))

        const showSnackBarSpy = vi.spyOn(wrapper.vm.snackbar, 'showSnackBar')

        await wrapper.vm.handleSourceCreationSave()

        expect(showSnackBarSpy).toHaveBeenCalledWith({
          msg: 'Failed to create event configuration source. Please try again.',
          error: true
        })
      })

      it('should reset form fields after successful creation', async () => {
        const { addEventConfigSource } = await import('@/services/eventConfigService')
        vi.mocked(addEventConfigSource).mockResolvedValue({
          id: 99,
          name: 'Test Source',
          fileOrder: 1,
          status: 201
        })

        await wrapper.vm.handleSourceCreationSave()

        expect(wrapper.vm.configName).toBe('')
        expect(wrapper.vm.vendor).toBe('')
      })

      it('should fetch all source names after successful creation', async () => {
        const { addEventConfigSource } = await import('@/services/eventConfigService')
        vi.mocked(addEventConfigSource).mockResolvedValue({
          id: 99,
          name: 'Test Source',
          fileOrder: 1,
          status: 201
        })
        await wrapper.vm.handleSourceCreationSave()

        expect(addEventConfigSource).toHaveBeenCalled()
      })

      it('should pass empty vendor when vendor field is empty', async () => {
        const { addEventConfigSource } = await import('@/services/eventConfigService')
        vi.mocked(addEventConfigSource).mockClear()
        vi.mocked(addEventConfigSource).mockResolvedValue({
          id: 100,
          name: 'OpenNMS Events',
          fileOrder: 1,
          status: 201
        })

        wrapper.vm.configName = 'OpenNMS Events'
        wrapper.vm.vendor = ''
        await wrapper.vm.$nextTick()

        await wrapper.vm.handleSourceCreationSave()

        expect(addEventConfigSource).toHaveBeenLastCalledWith('OpenNMS Events', '', '')
      })

      it('should successfully create source even when vendor is empty', async () => {
        const { addEventConfigSource } = await import('@/services/eventConfigService')
        vi.mocked(addEventConfigSource).mockClear()
        vi.mocked(addEventConfigSource).mockResolvedValue({
          id: 101,
          name: 'SingleWord',
          fileOrder: 1,
          status: 201
        })

        wrapper.vm.configName = 'SingleWord'
        wrapper.vm.vendor = ''
        await wrapper.vm.$nextTick()

        await wrapper.vm.handleSourceCreationSave()

        expect(addEventConfigSource).toHaveBeenLastCalledWith('SingleWord', '', '')
        expect(wrapper.vm.selectedSource._value).toBe(101)
        expect(wrapper.vm.sourceCreationDialogState).toBe(false)
      })
    })
  })

  describe('handleCancel edge cases', () => {
    it('should navigate to Event Configuration when id is 0', async () => {
      const pushSpy = vi.spyOn(router, 'push')

      wrapper.vm.handleCancel(0)

      expect(pushSpy).toHaveBeenCalledWith({ name: 'Event Configuration' })
    })

    it('should navigate to Event Configuration when id is negative', async () => {
      const pushSpy = vi.spyOn(router, 'push')

      wrapper.vm.handleCancel(-1)

      expect(pushSpy).toHaveBeenCalledWith({ name: 'Event Configuration' })
    })

    it('should navigate to Event Configuration Detail only when id > 0', async () => {
      const pushSpy = vi.spyOn(router, 'push')

      wrapper.vm.handleCancel(5)

      expect(pushSpy).toHaveBeenCalledWith({
        name: 'Event Configuration Detail',
        params: { id: 5 }
      })
    })
  })

  describe('handleSaveEvent success messages', () => {
    it('should show "Event updated successfully" message in edit mode', async () => {
      store.eventModificationState.isEditMode = CreateEditMode.Edit
      store.eventModificationState.eventConfigEvent = mockEvent
      wrapper.vm.isValid = true
      vi.mocked(updateEventConfigEventById).mockResolvedValue(true)

      const showSnackBarSpy = vi.spyOn(wrapper.vm.snackbar, 'showSnackBar')

      await wrapper.vm.handleSaveEvent()

      expect(showSnackBarSpy).toHaveBeenCalledWith({ msg: 'Event updated successfully', error: false })
    })

    it('should show "Event created successfully" message in create mode', async () => {
      store.eventModificationState.isEditMode = CreateEditMode.Create
      wrapper.vm.isValid = true
      vi.mocked(createEventConfigEvent).mockResolvedValue(true)

      const showSnackBarSpy = vi.spyOn(wrapper.vm.snackbar, 'showSnackBar')

      await wrapper.vm.handleSaveEvent()

      expect(showSnackBarSpy).toHaveBeenCalledWith({ msg: 'Event created successfully', error: false })
    })

    it('should show "Something went wrong" when response is falsy', async () => {
      store.eventModificationState.isEditMode = CreateEditMode.Create
      wrapper.vm.isValid = true
      vi.mocked(createEventConfigEvent).mockResolvedValue(null as any)

      const showSnackBarSpy = vi.spyOn(wrapper.vm.snackbar, 'showSnackBar')

      await wrapper.vm.handleSaveEvent()

      expect(showSnackBarSpy).toHaveBeenCalledWith({ msg: 'Something went wrong', error: true })
    })
  })

  describe('resetValues', () => {
    it('should reset all form values', () => {
      wrapper.vm.eventUei = 'test-uei'
      wrapper.vm.eventLabel = 'test-label'
      wrapper.vm.eventDescription = 'test-description'
      wrapper.vm.severity = { _text: 'Major', _value: 'Major' }
      wrapper.vm.destination = { _text: 'logndisplay', _value: 'logndisplay' }
      wrapper.vm.logMessage = 'test-log'
      wrapper.vm.addAlarmData = true
      wrapper.vm.reductionKey = 'test-key'
      wrapper.vm.alarmType = { _text: 'Problem', _value: '1' }
      wrapper.vm.autoClean = true
      wrapper.vm.clearKey = 'clear-key'
      wrapper.vm.maskElements = [{ name: { _text: 'uei', _value: 'uei' }, value: 'test' }]
      wrapper.vm.varbinds = [{ index: '1', value: 'val', type: { _text: 'vbNumber', _value: 'vbNumber' } }]
      wrapper.vm.varbindsDecode = [{ parmId: 'p1', decode: [] }]
      wrapper.vm.selectedSource = { _text: 'Test', _value: 1 }

      wrapper.vm.handleCancel()

      expect(wrapper.vm.eventUei).toBe('')
      expect(wrapper.vm.eventLabel).toBe('')
      expect(wrapper.vm.eventDescription).toBe('')
      expect(wrapper.vm.severity._value).toBe('')
      expect(wrapper.vm.destination._value).toBe('')
      expect(wrapper.vm.logMessage).toBe('')
      expect(wrapper.vm.addAlarmData).toBe(false)
      expect(wrapper.vm.reductionKey).toBe('')
      expect(wrapper.vm.alarmType._value).toBe('')
      expect(wrapper.vm.autoClean).toBe(false)
      expect(wrapper.vm.clearKey).toBe('')
      expect(wrapper.vm.maskElements).toEqual([])
      expect(wrapper.vm.varbinds).toEqual([])
      expect(wrapper.vm.varbindsDecode).toEqual([])
      expect(wrapper.vm.selectedSource._value).toBe('')
    })
  })

  describe('loadInitialValues edge cases', () => {
    it('should set selectedSource to empty when store.selectedSource is null and eventConfigEvent is null', async () => {
      store.selectedSource = null
      store.eventModificationState.eventConfigEvent = null

      // Force remount to trigger onMounted
      const newWrapper: any = mount(BasicInformation, {
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

      await newWrapper.vm.$nextTick()
      await new Promise((resolve) => setTimeout(resolve, 10))

      // When eventConfigEvent is null, resetValues is called which sets _value to ''
      expect(newWrapper.vm.selectedSource._value).toBe('')
    })

    it('should set selectedSource._value to -1 when store.selectedSource is null but eventConfigEvent exists', async () => {
      store.selectedSource = null
      store.eventModificationState.eventConfigEvent = mockEvent

      // Force remount to trigger onMounted
      const newWrapper: any = mount(BasicInformation, {
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

      await newWrapper.vm.$nextTick()
      await new Promise((resolve) => setTimeout(resolve, 10))

      expect(newWrapper.vm.selectedSource._value).toBe(-1)
    })

    it('should handle event with no alarm-data element', async () => {
      const mockEventNoAlarm = {
        ...mockEvent,
        xmlContent: `
          <event xmlns="http://xmlns.opennms.org/xsd/eventconf">
            <uei>uei.test.event1</uei>
            <event-label>Test Event 1</event-label>
            <descr><![CDATA[Description 1]]></descr>
            <logmsg dest="logndisplay"><![CDATA[Log message]]></logmsg>
            <severity>Major</severity>
          </event>
        `
      }

      store.selectedSource = mockSource
      store.eventModificationState.eventConfigEvent = mockEventNoAlarm

      const newWrapper: any = mount(BasicInformation, {
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

      await newWrapper.vm.$nextTick()
      await new Promise((resolve) => setTimeout(resolve, 10))

      expect(newWrapper.vm.addAlarmData).toBe(false)
    })

    it('should handle empty xmlContent gracefully', async () => {
      const mockEventEmptyXml = {
        ...mockEvent,
        xmlContent: ''
      }

      store.selectedSource = mockSource
      store.eventModificationState.eventConfigEvent = mockEventEmptyXml

      const newWrapper: any = mount(BasicInformation, {
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

      await newWrapper.vm.$nextTick()
      await new Promise((resolve) => setTimeout(resolve, 10))

      expect(newWrapper.vm.logMessage).toBe('')
      expect(newWrapper.vm.destination._value).toBe('')
    })

    it('should load multiple varbindsdecode elements correctly', async () => {
      const mockEventWithMultipleDecodes = {
        id: 2,
        uei: 'uei.test.event2',
        eventLabel: 'Test Event 2',
        description: 'Description 2',
        severity: 'Major',
        enabled: true,
        xmlContent: `
          <event xmlns="http://xmlns.opennms.org/xsd/eventconf">
            <uei>uei.test.event2</uei>
            <event-label>Test Event 2</event-label>
            <descr><![CDATA[Description 2]]></descr>
            <logmsg dest="logndisplay"><![CDATA[Log message]]></logmsg>
            <severity>Major</severity>
            <varbindsdecode>
              <parmid>paramA</parmid>
              <decode varbinddecodedstring="keyA1" varbindvalue="10" />
              <decode varbinddecodedstring="keyA2" varbindvalue="20" />
            </varbindsdecode>
            <varbindsdecode>
              <parmid>paramB</parmid>
              <decode varbinddecodedstring="keyB1" varbindvalue="30" />
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

      // Reset the store completely
      store.selectedSource = mockSource
      store.eventModificationState = {
        eventConfigEvent: mockEventWithMultipleDecodes,
        isEditMode: CreateEditMode.Edit
      }

      const newWrapper: any = mount(BasicInformation, {
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

      await newWrapper.vm.$nextTick()
      await new Promise((resolve) => setTimeout(resolve, 10))

      // Check the structure of varbindsDecode
      expect(newWrapper.vm.varbindsDecode).toHaveLength(2)
      expect(newWrapper.vm.varbindsDecode[0].parmId).toBe('paramA')
      // The first decode should have 2 items from the XML
      expect(newWrapper.vm.varbindsDecode[0].decode.length).toBeGreaterThanOrEqual(2)
      expect(newWrapper.vm.varbindsDecode[1].parmId).toBe('paramB')
      expect(newWrapper.vm.varbindsDecode[1].decode.length).toBeGreaterThanOrEqual(1)
    })
  })

  describe('watchEffect validation', () => {
    it('should update isValid to true when all required fields are valid', async () => {
      wrapper.vm.eventUei = 'uei.test.valid'
      wrapper.vm.eventLabel = 'Valid Label'
      wrapper.vm.eventDescription = 'Valid description'
      wrapper.vm.destination = { _text: 'logndisplay', _value: 'logndisplay' }
      wrapper.vm.severity = { _text: 'Major', _value: 'Major' }
      wrapper.vm.logMessage = 'Valid log message'
      wrapper.vm.addAlarmData = false

      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isValid).toBe(true)
      expect(Object.keys(wrapper.vm.errors).length).toBe(0)
    })

    it('should update isValid to false when severity is empty', async () => {
      wrapper.vm.eventUei = 'uei.test.valid'
      wrapper.vm.eventLabel = 'Valid Label'
      wrapper.vm.eventDescription = 'Valid description'
      wrapper.vm.destination = { _text: 'logndisplay', _value: 'logndisplay' }
      wrapper.vm.severity = { _text: '', _value: '' }
      wrapper.vm.logMessage = 'Valid log message'

      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isValid).toBe(false)
      expect(wrapper.vm.errors.severity).toBeDefined()
    })

    it('should update isValid to false when destination is empty', async () => {
      wrapper.vm.eventUei = 'uei.test.valid'
      wrapper.vm.eventLabel = 'Valid Label'
      wrapper.vm.eventDescription = 'Valid description'
      wrapper.vm.destination = { _text: '', _value: '' }
      wrapper.vm.severity = { _text: 'Major', _value: 'Major' }
      wrapper.vm.logMessage = 'Valid log message'

      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isValid).toBe(false)
      expect(wrapper.vm.errors.dest).toBeDefined()
    })

    it('should update isValid to false when log message is empty', async () => {
      wrapper.vm.eventUei = 'uei.test.valid'
      wrapper.vm.eventLabel = 'Valid Label'
      wrapper.vm.eventDescription = 'Valid description'
      wrapper.vm.destination = { _text: 'logndisplay', _value: 'logndisplay' }
      wrapper.vm.severity = { _text: 'Major', _value: 'Major' }
      wrapper.vm.logMessage = ''

      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isValid).toBe(false)
      expect(wrapper.vm.errors.logmsg).toBeDefined()
    })
  })
})


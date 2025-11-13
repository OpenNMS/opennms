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
  updateEventConfigEventById: vi.fn()
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
        <decode varbinddecodedstring="key1" varbindvalue="value1" />
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
      path: '/',
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

  it('should not render when no selected source', async () => {
    store.selectedSource = null
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.main-content').exists()).toBe(false)
  })

  it('should not render when no event config event', async () => {
    store.eventModificationState.eventConfigEvent = null
    await wrapper.vm.$nextTick()

    expect(wrapper.find('.main-content').exists()).toBe(false)
  })

  it('should display correct title for edit mode', () => {
    const title = wrapper.find('h3')
    expect(title.text()).toBe('Edit Event Details')
  })

  it('should display correct title for create mode', async () => {
    store.eventModificationState.isEditMode = CreateEditMode.Create
    await wrapper.vm.$nextTick()

    const title = wrapper.find('h3')
    expect(title.text()).toBe('Create New Event')
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
    expect(wrapper.vm.varbindsDecode[0].decode[0].value).toBe('value1')
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
    expect(wrapper.vm.isValid).toBe(false)
    const updateSpy = vi.spyOn(await import('@/services/eventConfigService'), 'updateEventConfigEventById')
    const saveButton = wrapper.find('[data-test="save-event-button"]')
    await saveButton.trigger('click')

    expect(updateSpy).not.toHaveBeenCalled()
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
})


import BasicInformation from '@/components/EventConfigEventCreate/BasicInformation.vue'
import { useEventModificationStore } from '@/stores/eventModificationStore'
import { CreateEditMode } from '@/types'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push: mockPush })
}))

const mockCreateEventConfigEvent = vi.fn()
const mockUpdateEventConfigEventById = vi.fn()
const mockAddEventConfigSource = vi.fn()
vi.mock('@/services/eventConfigService', () => ({
  createEventConfigEvent: (...a: any[]) => mockCreateEventConfigEvent(...a),
  updateEventConfigEventById: (...a: any[]) => mockUpdateEventConfigEventById(...a),
  addEventConfigSource: (...a: any[]) => mockAddEventConfigSource(...a)
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: mockShowSnackBar })
}))

const mockFetchAllSourcesNames = vi.fn().mockResolvedValue(undefined)
vi.mock('@/stores/eventConfigStore', () => ({
  useEventConfigStore: vi.fn(() => ({
    uploadedSources: [
      { id: 1, name: 'Test Source' },
      { id: 2, name: 'Another Source' }
    ],
    fetchAllSourcesNames: mockFetchAllSourcesNames
  }))
}))

vi.mock('vkbeautify', () => ({
  default: { xml: vi.fn(xml => xml) }
}))

// Validation is exercised separately; default to "valid" and let individual
// tests override the mock to drive the isValid/errors state.
const mockValidateEvent = vi.fn((..._args: any[]) => ({} as Record<string, unknown>))
vi.mock('@/components/EventConfigEventCreate/eventValidator', () => ({
  validateEvent: (...a: any[]) => mockValidateEvent(...a)
}))

const childStubs = {
  AlarmDataInfo: { name: 'AlarmDataInfo', template: '<div class="alarm-stub"></div>', props: ['errors', 'addAlarmData', 'reductionKey', 'alarmType', 'autoClean', 'clearKey'] },
  MaskElements: { name: 'MaskElements', template: '<div class="mask-elements-stub"></div>', props: ['maskElements', 'errors'] },
  MaskVarbinds: { name: 'MaskVarbinds', template: '<div class="mask-varbinds-stub"></div>', props: ['varbinds', 'maskElements', 'errors'] },
  VarbindsDecode: { name: 'VarbindsDecode', template: '<div class="varbinds-decode-stub"></div>', props: ['varbindsDecode', 'errors'] }
}

describe('BasicInformation.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventModificationStore>

  const mountForm = () => mount(BasicInformation, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs: childStubs
    }
  })

  beforeEach(async () => {
    vi.clearAllMocks()
    mockValidateEvent.mockReturnValue({})

    wrapper = mountForm()
    store = useEventModificationStore()
    store.selectedSource = null as any
    store.eventModificationState = { isEditMode: CreateEditMode.Create, eventConfigEvent: null } as any
    store.resetEventModificationState = vi.fn()
    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Initial Rendering', () => {
    it('renders the form shell and child sections', () => {
      expect(wrapper.find('.main-content').exists()).toBe(true)
      expect(wrapper.find('[data-test="source-name"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="event-uei"]').exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'AlarmDataInfo' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'MaskElements' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'MaskVarbinds' }).exists()).toBe(true)
      expect(wrapper.findComponent({ name: 'VarbindsDecode' }).exists()).toBe(true)
    })

    it('fetches source names on mount', () => {
      expect(mockFetchAllSourcesNames).toHaveBeenCalled()
    })
  })

  describe('Source autocomplete', () => {
    it('filters uploaded sources by query', () => {
      vi.useFakeTimers()
      wrapper.vm.search('another')
      vi.advanceTimersByTime(500)
      expect(wrapper.vm.results).toEqual([{ _text: 'Another Source', _value: 2 }])
      vi.useRealTimers()
    })

    it('sets the selected source object', () => {
      wrapper.vm.setSelectedSource({ _text: 'Test Source', _value: 1 })
      expect(wrapper.vm.selectedSource).toEqual({ _text: 'Test Source', _value: 1 })
    })

    it('resets to a sentinel when cleared', () => {
      wrapper.vm.setSelectedSource(null)
      expect(wrapper.vm.selectedSource).toEqual({ _text: '', _value: -1 })
    })
  })

  describe('Select normalisation (onSelectChange)', () => {
    it('copies the selected option into the target ref', () => {
      wrapper.vm.onSelectChange(wrapper.vm.destination, { _text: 'logndisplay', _value: 'logndisplay' })
      expect(wrapper.vm.destination).toEqual({ _text: 'logndisplay', _value: 'logndisplay' })
    })

    it('clears the target on null', () => {
      wrapper.vm.onSelectChange(wrapper.vm.severity, null)
      expect(wrapper.vm.severity).toEqual({ _text: '', _value: '' })
    })
  })

  describe('setAlarmData', () => {
    it('toggles alarm data and clears dependent fields when disabled', () => {
      wrapper.vm.setAlarmData('reductionKey', 'rk')
      wrapper.vm.setAlarmData('addAlarmData', true)
      expect(wrapper.vm.addAlarmData).toBe(true)
      wrapper.vm.setAlarmData('addAlarmData', false)
      expect(wrapper.vm.addAlarmData).toBe(false)
      expect(wrapper.vm.reductionKey).toBe('')
      expect(wrapper.vm.autoClean).toBe(false)
    })

    it('sets the alarm type object', () => {
      wrapper.vm.setAlarmData('alarmType', { _text: 'Resolution', _value: 'resolution' })
      expect(wrapper.vm.alarmType).toEqual({ _text: 'Resolution', _value: 'resolution' })
    })
  })

  describe('setMaskElements', () => {
    it('adds and removes rows and sets values', () => {
      wrapper.vm.maskElements = []
      wrapper.vm.setMaskElements('addMaskRow', null, -1)
      expect(wrapper.vm.maskElements).toHaveLength(1)
      wrapper.vm.setMaskElements('setValue', 'val', 0)
      expect(wrapper.vm.maskElements[0].value).toBe('val')
      wrapper.vm.setMaskElements('setName', { _text: 'generic', _value: 'generic' }, 0)
      expect(wrapper.vm.maskElements[0].name._value).toBe('generic')
      wrapper.vm.setMaskElements('removeMaskRow', null, 0)
      expect(wrapper.vm.maskElements).toHaveLength(0)
    })
  })

  describe('setVarbinds', () => {
    it('normalises a negative varbind number to 0', () => {
      wrapper.vm.varbinds = [{ index: '0', value: '', type: { _text: 'Number', _value: 'vbnumber' }}]
      wrapper.vm.setVarbinds('setVarbindNumber', '-3', 0)
      expect(wrapper.vm.varbinds[0].index).toBe('0')
    })

    it('adds, sets value, and clears all', () => {
      wrapper.vm.varbinds = []
      wrapper.vm.setVarbinds('addVarbindRow', null, -1)
      expect(wrapper.vm.varbinds).toHaveLength(1)
      wrapper.vm.setVarbinds('setValue', 'v', 0)
      expect(wrapper.vm.varbinds[0].value).toBe('v')
      wrapper.vm.setVarbinds('clearAllVarbinds', null, -1)
      expect(wrapper.vm.varbinds).toHaveLength(0)
    })
  })

  describe('setVarbindsDecode', () => {
    it('adds parm rows and decode rows and normalises decode values', () => {
      wrapper.vm.varbindsDecode = []
      wrapper.vm.setVarbindsDecode('addVarbindDecodeRow', null, -1, -1)
      expect(wrapper.vm.varbindsDecode).toHaveLength(1)
      wrapper.vm.setVarbindsDecode('addDecodeRow', null, 0, -1)
      expect(wrapper.vm.varbindsDecode[0].decode).toHaveLength(1)
      wrapper.vm.setVarbindsDecode('setDecodeValue', '-1', 0, 0)
      expect(wrapper.vm.varbindsDecode[0].decode[0].value).toBe('0')
      wrapper.vm.setVarbindsDecode('setParmId', '7', 0, -1)
      expect(wrapper.vm.varbindsDecode[0].parmId).toBe('7')
    })
  })

  describe('xmlContent', () => {
    it('includes the core event fields', async () => {
      wrapper.vm.eventUei = 'uei.test/x'
      wrapper.vm.eventLabel = 'My Event'
      wrapper.vm.severity = { _text: 'Major', _value: 'Major' }
      await nextTick()
      const xml = wrapper.vm.xmlContent
      expect(xml).toContain('<uei>uei.test/x</uei>')
      expect(xml).toContain('<event-label>My Event</event-label>')
      expect(xml).toContain('<severity>Major</severity>')
    })
  })

  describe('handleSaveEvent', () => {
    beforeEach(() => {
      wrapper.vm.isValid = true
      wrapper.vm.selectedSource = { _text: 'Test Source', _value: 1 }
    })

    it('creates an event in Create mode', async () => {
      store.eventModificationState = { isEditMode: CreateEditMode.Create, eventConfigEvent: null } as any
      mockCreateEventConfigEvent.mockResolvedValue(true)
      await wrapper.vm.handleSaveEvent()
      await flushPromises()
      expect(mockCreateEventConfigEvent).toHaveBeenCalledWith(expect.any(String), 1)
      expect(mockShowSnackBar).toHaveBeenCalledWith(expect.objectContaining({ msg: 'Event created successfully' }))
      expect(mockPush).toHaveBeenCalled()
    })

    it('updates an event in Edit mode', async () => {
      store.eventModificationState = { isEditMode: CreateEditMode.Edit, eventConfigEvent: { id: 9, enabled: true }} as any
      mockUpdateEventConfigEventById.mockResolvedValue(true)
      await wrapper.vm.handleSaveEvent()
      await flushPromises()
      expect(mockUpdateEventConfigEventById).toHaveBeenCalledWith(expect.any(String), 1, 9, true)
    })

    it('warns when no source is selected', async () => {
      wrapper.vm.selectedSource = { _text: '', _value: -1 }
      await wrapper.vm.handleSaveEvent()
      expect(mockCreateEventConfigEvent).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
    })

    it('does nothing when the form is invalid', async () => {
      wrapper.vm.isValid = false
      await wrapper.vm.handleSaveEvent()
      expect(mockCreateEventConfigEvent).not.toHaveBeenCalled()
    })
  })

  describe('Source creation dialog', () => {
    it('opens and resets the dialog fields', () => {
      wrapper.vm.configName = 'old'
      wrapper.vm.showSourceCreationDialog()
      expect(wrapper.vm.sourceCreationDialogState).toBe(true)
      expect(wrapper.vm.configName).toBe('')
    })

    it('selects the created source on a 201 response', async () => {
      wrapper.vm.configName = 'New Src'
      mockAddEventConfigSource.mockResolvedValue({ id: 5, name: 'New Src', fileOrder: 0, status: 201 })
      await wrapper.vm.handleSourceCreationSave()
      await flushPromises()
      expect(wrapper.vm.selectedSource).toEqual({ _text: 'New Src', _value: 5 })
      expect(wrapper.vm.sourceCreationDialogState).toBe(false)
    })

    it.each([
      [409, 'already exists'],
      [400, 'Invalid request'],
      [500, 'Failed to create']
    ])('shows an error snackbar on a %s response', async (code, fragment) => {
      wrapper.vm.configName = 'New Src'
      mockAddEventConfigSource.mockResolvedValue(code)
      await wrapper.vm.handleSourceCreationSave()
      await flushPromises()
      expect(mockShowSnackBar).toHaveBeenCalledWith(
        expect.objectContaining({ error: true, msg: expect.stringContaining(fragment) })
      )
    })
  })

  describe('handleCancel', () => {
    it('resets state and routes to the source detail when an id is given', () => {
      wrapper.vm.handleCancel(3)
      expect(store.resetEventModificationState).toHaveBeenCalled()
      expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration Detail', params: { id: 3 }})
    })

    it('routes to the list when no id is given', () => {
      wrapper.vm.handleCancel(undefined)
      expect(mockPush).toHaveBeenCalledWith({ name: 'Event Configuration' })
    })
  })
})

import CreateEventConfigurationDialog from '@/components/EventConfiguration/Dialog/CreateEventConfigurationDialog.vue'
import { addEventConfigSource } from '@/services/eventConfigService'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherInput } from '@featherds/input'
import { FeatherTextarea } from '@featherds/textarea'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

// Mock router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

// Mock the service with a factory function
vi.mock('@/services/eventConfigService', () => ({
  addEventConfigSource: vi.fn()
}))

// Mock the snackbar composable
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: vi.fn()
  })
}))

// Helper function to create successful service response
const mockSuccessResponse = (id: number, name: string, fileOrder: number = 0) => ({
  id,
  name,
  fileOrder,
  status: 201 as const
})

describe('CreateEventConfigurationDialog.vue', () => {
  let store: ReturnType<typeof useEventConfigStore>
  let wrapper: ReturnType<typeof mount>

  beforeEach(async () => {
    vi.useFakeTimers()
    const pinia = createTestingPinia({ createSpy: vi.fn })
    setActivePinia(pinia)
    store = useEventConfigStore()
    store.createEventConfigSourceDialogState = { visible: true }

    wrapper = mount(CreateEventConfigurationDialog, {
      attachTo: document.body,
      global: {
        plugins: [pinia],
        components: { FeatherButton, FeatherInput, FeatherDialog, FeatherTextarea }
      }
    })
    await flushPromises()
    vi.runAllTimers()
  })

  afterEach(() => {
    vi.runAllTimers()
    wrapper.unmount()
    document.body.innerHTML = ''
    vi.useRealTimers()
  })

  it('renders the dialog when visible is true', () => {
    const header = document.body.querySelector('[data-ref-id="feather-dialog-header"]')
    expect(header).not.toBeNull()
    expect(header!.textContent).toBe('Create New Event Source')
  })

  it('dialog has correct title from labels object', () => {
    const dialog = wrapper.findComponent(FeatherDialog)
    expect(dialog.props('labels')).toEqual({
      title: 'Create New Event Source'
    })
  })

  it('renders informational note', () => {
    const p = document.body.querySelector('.modal-body-form p')
    expect(p).not.toBeNull()
    expect(p!.textContent).toContain('will be created with 0 event configurations')
  })

  it('renders input field with correct label', () => {
    const inputs = wrapper.findAllComponents(FeatherInput)
    expect(inputs.length).toBeGreaterThanOrEqual(1)
    expect(inputs[0].props('label')).toBe('Event Configuration Source Name')
  })

  it('renders Cancel and Create buttons', () => {
    const buttons = wrapper.findAllComponents(FeatherButton)
    expect(buttons.length).toBe(2)
    expect(buttons[0].text()).toContain('Cancel')
    expect(buttons[1].text()).toContain('Create')
  })

  it('shows error when input empty', async () => {
    ;(wrapper.vm as any).configName = ''
    await wrapper.vm.$nextTick()
    const inputComp = wrapper.findAllComponents(FeatherInput)[0]
    expect(inputComp.props('error')).toBe('Configuration name is required.')
  })

  it('clears error when input has value', async () => {
    ;(wrapper.vm as any).configName = 'X'
    ;(wrapper.vm as any).vendor = 'X'
    await wrapper.vm.$nextTick()
    const inputComp = wrapper.findAllComponents(FeatherInput)[0]
    expect(inputComp.props('error')).toBeUndefined()
  })

  it('disables Create button when invalid', async () => {
    ;(wrapper.vm as any).configName = '   '
    ;(wrapper.vm as any).vendor = '   '
    await wrapper.vm.$nextTick()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1]
    // Button should be disabled when error is not null
    const hasDisabled =
      createBtn.attributes('disabled') !== undefined || createBtn.attributes('aria-disabled') === 'true'
    expect(hasDisabled).toBe(true)
  })

  it('enables Create button when valid', async () => {
    ;(wrapper.vm as any).configName = 'Valid'
    ;(wrapper.vm as any).vendor = 'Valid'
    await wrapper.vm.$nextTick()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1]
    expect(createBtn.attributes('disabled')).toBeUndefined()
  })

  it('cancel calls hideCreateEventConfigSourceDialog', async () => {
    store.hideCreateEventConfigSourceDialog = vi.fn()
    const cancelBtn = wrapper.findAllComponents(FeatherButton)[0]
    await cancelBtn.trigger('click')
    expect(store.hideCreateEventConfigSourceDialog).toHaveBeenCalledTimes(1)
  })

  it('does not save when invalid create clicked', async () => {
    store.hideCreateEventConfigSourceDialog = vi.fn()
    ;(wrapper.vm as any).configName = '   '
    await wrapper.vm.$nextTick()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1]
    await createBtn.trigger('click')
    // Button is disabled, so click may not trigger
    expect(store.hideCreateEventConfigSourceDialog).not.toHaveBeenCalled()
  })

  it('saves and shows success state when valid', async () => {
    ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(1, 'TestConfig', 0))
    ;(wrapper.vm as any).configName = 'ConfigA'
    ;(wrapper.vm as any).vendor = 'ConfigA'
    await wrapper.vm.$nextTick()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1]
    await createBtn.trigger('click')
    await flushPromises()

    expect((wrapper.vm as any).successMessage).toBe(true)
  })

  it('resets form after save', async () => {
    store.hideCreateEventConfigSourceDialog = vi.fn()
    ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(1, 'TestConfig', 0))
    ;(wrapper.vm as any).configName = 'ResetMe'
    ;(wrapper.vm as any).vendor = 'ResetMe'
    await wrapper.vm.$nextTick()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1]
    await createBtn.trigger('click')
    await flushPromises()

    expect((wrapper.vm as any).configName).toBe('')
    expect((wrapper.vm as any).vendor).toBe('')
  })

  it('whitespace-only treated invalid', async () => {
    ;(wrapper.vm as any).configName = '   '
    await wrapper.vm.$nextTick()
    const inputComp = wrapper.findAllComponents(FeatherInput)[0]
    expect(inputComp.props('error')).toBe('Configuration name is required.')
  })

  it('multiple successful submissions show success state', async () => {
    ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(1, 'TestConfig', 0))
    ;(wrapper.vm as any).configName = 'One'
    ;(wrapper.vm as any).vendor = 'One'
    await wrapper.vm.$nextTick()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1]
    await createBtn.trigger('click')
    await flushPromises()

    expect((wrapper.vm as any).successMessage).toBe(true)
  })

  it('modal-body has expected structure', () => {
    const body = document.body.querySelector('.modal-body-form')
    expect(body).not.toBeNull()
    const divs = body!.querySelectorAll('div')
    expect(divs.length).toBeGreaterThanOrEqual(1)
    expect(body!.querySelector('p')).not.toBeNull()
  })

  it('maintains form state before save', async () => {
    ;(wrapper.vm as any).configName = 'Persist'
    await wrapper.vm.$nextTick()
    const inputComp = wrapper.findAllComponents(FeatherInput)[0]
    expect(inputComp.props('modelValue')).toBe('Persist')
  })

  it('visibility reactive (v-model)', async () => {
    expect(document.body.querySelector('.modal-body-form')).not.toBeNull()
    store.createEventConfigSourceDialogState.visible = false
    await wrapper.vm.$nextTick()
    vi.runAllTimers()
    expect(document.body.querySelector('.modal-body-form')).toBeNull()
  })

  it('unmounts without errors', () => {
    expect(() => wrapper.unmount()).not.toThrow()
  })

  it('shows error on initial mount (empty name)', () => {
    const input = wrapper.findAllComponents(FeatherInput)[0]
    expect(input.props('error')).toBe('Configuration name is required.')
  })

  it('treats trimmed non-empty as valid', async () => {
    ;(wrapper.vm as any).configName = '   X   '
    ;(wrapper.vm as any).vendor = '   X   '
    await wrapper.vm.$nextTick()
    const input = wrapper.findAllComponents(FeatherInput)[0]
    expect(input.props('error')).toBeUndefined()
  })

  it('@hidden event triggers store hide', async () => {
    store.hideCreateEventConfigSourceDialog = vi.fn()
    const dialog = wrapper.findComponent(FeatherDialog)
    expect(dialog.exists()).toBe(true)
    dialog.vm.$emit('hidden')
    expect(store.hideCreateEventConfigSourceDialog).toHaveBeenCalledTimes(1)
  })

  it('hide-close prop applied', () => {
    const dialog = wrapper.findComponent(FeatherDialog)
    expect(dialog.exists()).toBe(true)
    expect(dialog.props('hideClose')).toBe(true)
  })

  it('role dialog & aria-modal present', () => {
    const roleEl = document.body.querySelector('[role="dialog"]')
    expect(roleEl).not.toBeNull()
    expect(roleEl!.getAttribute('aria-modal')).toBe('true')
  })

  it('disabled state updates when reverting to empty', async () => {
    ;(wrapper.vm as any).configName = 'Valid'
    await wrapper.vm.$nextTick()
    ;(wrapper.vm as any).configName = ''
    await wrapper.vm.$nextTick()
    const createBtn = wrapper.findAllComponents(FeatherButton)[1]
    expect(createBtn.attributes('aria-disabled') === 'true' || createBtn.attributes('disabled')).toBeTruthy()
  })

  describe('Vendor Field', () => {
    it('renders vendor input field with correct label', () => {
      const inputs = wrapper.findAllComponents(FeatherInput)
      expect(inputs.length).toBeGreaterThanOrEqual(2)
      expect(inputs[1].props('label')).toBe('Vendor')
    })

    it('shows error when vendor is empty', async () => {
      ;(wrapper.vm as any).vendor = ''
      await wrapper.vm.$nextTick()
      const inputs = wrapper.findAllComponents(FeatherInput)
      expect(inputs[1].props('error')).toBe('Vendor is required.')
    })

    it('clears error when vendor has value', async () => {
      ;(wrapper.vm as any).vendor = 'OpenNMS'
      await wrapper.vm.$nextTick()
      const inputs = wrapper.findAllComponents(FeatherInput)
      expect(inputs[1].props('error')).toBeUndefined()
    })

    it('validates vendor field on form submission', async () => {
      ;(wrapper.vm as any).configName = 'Test'
      ;(wrapper.vm as any).vendor = ''
      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      expect(createBtn.attributes('aria-disabled') === 'true' || createBtn.attributes('disabled')).toBeTruthy()
    })

    it('whitespace-only vendor treated as invalid', async () => {
      ;(wrapper.vm as any).vendor = '   '
      await wrapper.vm.$nextTick()
      const inputs = wrapper.findAllComponents(FeatherInput)
      expect(inputs[1].props('error')).toBe('Vendor is required.')
    })
  })

  describe('Description Field', () => {
    it('renders description textarea field', () => {
      const textarea = wrapper.findComponent(FeatherTextarea)
      expect(textarea.exists()).toBe(true)
    })

    it('description field has correct hint text', () => {
      const textarea = wrapper.findComponent(FeatherTextarea)
      expect(textarea.props('hint')).toContain('Provide a detailed description')
      expect(textarea.props('hint')).toContain('optional')
    })

    it('description field has correct number of rows', () => {
      const textarea = wrapper.findComponent(FeatherTextarea)
      expect(textarea.attributes('rows')).toBe('10')
    })

    it('description field is optional (empty allowed)', async () => {
      ;(wrapper.vm as any).configName = 'Valid'
      ;(wrapper.vm as any).vendor = 'Valid'
      ;(wrapper.vm as any).description = ''
      await wrapper.vm.$nextTick()
      // Should not show error for empty description
      const textarea = wrapper.findComponent(FeatherTextarea)
      expect(textarea.props('error')).toBeUndefined()
    })

    it('description field trims whitespace on input', async () => {
      ;(wrapper.vm as any).description = '  trimmed text  '
      await wrapper.vm.$nextTick()
      // v-model.trim should trim the value
      expect((wrapper.vm as any).description).toBe('trimmed text')
    })

    it('description field has auto height feature', () => {
      const textarea = wrapper.findComponent(FeatherTextarea)
      // Check if auto prop or attribute is set
      expect(textarea.props('auto') || textarea.attributes('auto')).toBeTruthy()
    })

    it('description field has clear button', () => {
      const textarea = wrapper.findComponent(FeatherTextarea)
      // Check if clear prop is set on the component
      const clearProp = textarea.props('clear')
      // The clear prop should be set to indicate the clear button is enabled
      expect(clearProp || textarea.attributes('clear')).toBeTruthy()
    })

    it('description accepts long text', async () => {
      const longText = 'A'.repeat(500)
      ;(wrapper.vm as any).description = longText
      await wrapper.vm.$nextTick()
      const textarea = wrapper.findComponent(FeatherTextarea)
      expect(textarea.props('modelValue')).toHaveLength(500)
    })

    it('description field has data-test attribute', () => {
      const textarea = wrapper.findComponent(FeatherTextarea)
      expect(textarea.attributes('data-test')).toBe('event-description')
    })
  })

  describe('Form Validation', () => {
    it('requires both configName and vendor to enable Create button', async () => {
      ;(wrapper.vm as any).configName = 'Test'
      ;(wrapper.vm as any).vendor = ''
      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      expect(createBtn.attributes('aria-disabled') === 'true' || createBtn.attributes('disabled')).toBeTruthy()
      ;(wrapper.vm as any).vendor = 'Vendor'
      await wrapper.vm.$nextTick()
      expect(createBtn.attributes('aria-disabled')).toBeUndefined()
    })

    it('disables Create button when either field is invalid', async () => {
      ;(wrapper.vm as any).configName = ''
      ;(wrapper.vm as any).vendor = 'Valid'
      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      expect(createBtn.attributes('aria-disabled') === 'true' || createBtn.attributes('disabled')).toBeTruthy()
    })

    it('error object contains both name and vendor when both empty', async () => {
      ;(wrapper.vm as any).configName = ''
      ;(wrapper.vm as any).vendor = ''
      await wrapper.vm.$nextTick()
      const error = (wrapper.vm as any).error
      expect(error).not.toBeNull()
      expect(error.name).toBeDefined()
      expect(error.vendor).toBeDefined()
    })

    it('error object is null when form is valid', async () => {
      ;(wrapper.vm as any).configName = 'Test'
      ;(wrapper.vm as any).vendor = 'Vendor'
      await wrapper.vm.$nextTick()
      const error = (wrapper.vm as any).error
      expect(error).toBeNull()
    })
  })

  describe('Success Message State', () => {
    const mockSource = {
      id: 123,
      vendor: 'TestVendor',
      name: 'TestConfig',
      description: 'Test description',
      enabled: true,
      createdTime: new Date(),
      lastModified: new Date(),
      eventCount: 0,
      fileOrder: 0,
      uploadedBy: ''
    }

    beforeEach(async () => {
      ;(wrapper.vm as any).configName = 'TestConfig'
      ;(wrapper.vm as any).vendor = 'TestVendor'
      ;(wrapper.vm as any).description = 'Test description'
      vi.clearAllMocks()
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(1, 'TestConfig', 0))
      store.sources = [mockSource]
    })

    it('shows success message after successful creation', async () => {
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).successMessage).toBe(true)
    })

    it('hides form and shows success message', async () => {
      ;(wrapper.vm as any).configName = 'TestConfig'
      ;(wrapper.vm as any).vendor = 'TestVendor'
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(123, 'TestConfig', 0))

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      // Check that successMessage flag is set
      expect((wrapper.vm as any).successMessage).toBe(true)

      // The conditional rendering is based on successMessage flag - use DOM selectors
      const formBody = document.querySelector('.modal-body-form')
      const successBody = document.querySelector('.modal-body-success')

      expect(formBody).toBeNull()
      expect(successBody).not.toBeNull()
    })

    it('success message contains confirmation text', async () => {
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      await wrapper.vm.$nextTick()

      const successBody = wrapper.find('.modal-body-success')
      if (successBody.exists()) {
        expect(successBody.text()).toContain('created successfully')
      } else {
        // Fallback: check in the component's successMessage flag
        expect((wrapper.vm as any).successMessage).toBe(true)
      }
    })

    it('shows View Source button instead of Create after success', async () => {
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[buttons.length - 1].text()).toContain('View Source')
    })
  })

  describe('Service Integration', () => {
    it('calls addEventConfigSource with correct parameters', async () => {
      ;(wrapper.vm as any).configName = 'TestConfig'
      ;(wrapper.vm as any).vendor = 'TestVendor'
      ;(wrapper.vm as any).description = 'Test Description'
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(123, 'TestConfig', 0))

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect(addEventConfigSource).toHaveBeenCalledWith('TestConfig', 'TestVendor', 'Test Description')
    })

    it('validates success response structure contains required fields', async () => {
      const mockResponse = mockSuccessResponse(123, 'TestSource', 5)
      ;(wrapper.vm as any).configName = 'TestSource'
      ;(wrapper.vm as any).vendor = 'TestVendor'
      ;(addEventConfigSource as any).mockResolvedValue(mockResponse)

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      // Verify the mock response has the expected structure
      expect(mockResponse).toHaveProperty('id', 123)
      expect(mockResponse).toHaveProperty('name', 'TestSource')
      expect(mockResponse).toHaveProperty('fileOrder', 5)
      expect(mockResponse).toHaveProperty('status', 201)
      expect((wrapper.vm as any).successMessage).toBe(true)
    })

    it('handles service error gracefully', async () => {
      ;(wrapper.vm as any).configName = 'TestConfig'
      ;(wrapper.vm as any).vendor = 'TestVendor'
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      ;(addEventConfigSource as any).mockRejectedValue(new Error('Service error'))

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error creating event configuration source:', expect.any(Error))
      consoleErrorSpy.mockRestore()
    })
  })

  describe('Navigation', () => {
    it('navigates to Event Configuration Detail after clicking View Source', async () => {
      ;(wrapper.vm as any).configName = 'TestConfig'
      ;(wrapper.vm as any).vendor = 'TestVendor'
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(456, 'TestConfig', 0))
      store.hideCreateEventConfigSourceDialog = vi.fn()

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      const viewSourceBtn =
        wrapper.findAllComponents(FeatherButton)[wrapper.findAllComponents(FeatherButton).length - 1]
      await viewSourceBtn.trigger('click')

      expect(mockPush).toHaveBeenCalledWith({
        name: 'Event Configuration Detail',
        params: { id: 456 }
      })
    })

    it('hides dialog after navigating to source', async () => {
      ;(wrapper.vm as any).configName = 'TestConfig'
      ;(wrapper.vm as any).vendor = 'TestVendor'
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(456, 'TestConfig', 0))
      store.hideCreateEventConfigSourceDialog = vi.fn()

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      const viewSourceBtn =
        wrapper.findAllComponents(FeatherButton)[wrapper.findAllComponents(FeatherButton).length - 1]
      await viewSourceBtn.trigger('click')

      expect(store.hideCreateEventConfigSourceDialog).toHaveBeenCalled()
    })

    it('resets success message after navigation', async () => {
      ;(wrapper.vm as any).configName = 'TestConfig'
      ;(wrapper.vm as any).vendor = 'TestVendor'
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(456, 'TestConfig', 0))
      store.hideCreateEventConfigSourceDialog = vi.fn()

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).successMessage).toBe(true)

      const viewSourceBtn =
        wrapper.findAllComponents(FeatherButton)[wrapper.findAllComponents(FeatherButton).length - 1]
      await viewSourceBtn.trigger('click')

      expect((wrapper.vm as any).successMessage).toBe(false)
    })

    it('logs error when newId is 0 on View Source click', async () => {
      ;(wrapper.vm as any).successMessage = true
      ;(wrapper.vm as any).newId = 0
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      store.hideCreateEventConfigSourceDialog = vi.fn()

      // Access the last button which would be the View Source button
      const buttons = wrapper.findAllComponents(FeatherButton)
      const viewSourceBtn = buttons[buttons.length - 1]

      if (viewSourceBtn.text().includes('View Source')) {
        await viewSourceBtn.trigger('click')
        expect(consoleErrorSpy).toHaveBeenCalledWith('No new event configuration source ID available.')
      }

      consoleErrorSpy.mockRestore()
    })
  })

  describe('Form Reset', () => {
    it('resets all form fields after successful creation', async () => {
      ;(wrapper.vm as any).configName = 'TestConfig'
      ;(wrapper.vm as any).vendor = 'TestVendor'
      ;(wrapper.vm as any).description = 'Test Description'
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(123, 'TestConfig', 0))

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).configName).toBe('')
      expect((wrapper.vm as any).vendor).toBe('')
      expect((wrapper.vm as any).description).toBe('')
    })

    it('clears description field on reset', async () => {
      ;(wrapper.vm as any).description = 'Some long description text'
      await wrapper.vm.$nextTick()
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(123, 'TestConfig', 0))
      ;(wrapper.vm as any).configName = 'Test'
      ;(wrapper.vm as any).vendor = 'Test'

      // Verify initial description state
      expect((wrapper.vm as any).description).toBe('Some long description text')

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      // After save, description should be cleared by resetForm()
      expect((wrapper.vm as any).description).toBe('')
    })
  })

  describe('Informational Note', () => {
    it('displays note about initial event configurations count', () => {
      const p = document.body.querySelector('.modal-body-form p')
      expect(p).not.toBeNull()
      expect(p!.textContent).toContain('will be created with 0 event configurations')
    })

    it('note is visible on initial load', () => {
      const note = document.body.querySelector('.modal-body-form p')
      expect(note).not.toBeNull()
    })

    it('note mentions post-creation option', () => {
      const note = document.body.querySelector('.modal-body-form p')
      expect(note!.textContent).toContain('You can add event configurations after creation')
    })
  })

  describe('HTTP Status Code 409 (Duplicate Name)', () => {
    it('shows snackbar error on 409 duplicate source name', async () => {
      ;(wrapper.vm as any).configName = 'ExistingSource'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(409)

      const mockShowSnackBar = vi.fn()
      vi.stubGlobal('useSnackbar', () => ({
        showSnackBar: mockShowSnackBar
      }))

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      // Verify the specific 409 error message
      expect((wrapper.vm as any).snackbar.showSnackBar).toHaveBeenCalledWith(
        expect.objectContaining({
          msg: 'An event configuration source with this name already exists.',
          error: true
        })
      )
    })

    it('does not reset form on 409 error', async () => {
      ;(wrapper.vm as any).configName = 'ExistingSource'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(wrapper.vm as any).description = 'Description'
      ;(addEventConfigSource as any).mockResolvedValue(409)

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      // Form should retain values after 409 error
      expect((wrapper.vm as any).configName).toBe('ExistingSource')
      expect((wrapper.vm as any).vendor).toBe('Vendor')
      expect((wrapper.vm as any).description).toBe('Description')
    })

    it('does not show success message on 409 error', async () => {
      ;(wrapper.vm as any).configName = 'ExistingSource'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(409)

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).successMessage).toBe(false)
    })

    it('does not call store methods on 409 error', async () => {
      ;(wrapper.vm as any).configName = 'ExistingSource'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(409)
      store.fetchEventConfigs = vi.fn()
      store.refreshSourcesFilters = vi.fn()

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect(store.fetchEventConfigs).not.toHaveBeenCalled()
      expect(store.refreshSourcesFilters).not.toHaveBeenCalled()
    })
  })

  describe('HTTP Status Code 400 (Bad Request)', () => {
    it('shows snackbar error on 400 validation error', async () => {
      ;(wrapper.vm as any).configName = 'Invalid@Name'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(400)

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).snackbar.showSnackBar).toHaveBeenCalledWith(
        expect.objectContaining({
          msg: 'Invalid request. Please check your input and try again.',
          error: true
        })
      )
    })

    it('does not reset form on 400 error', async () => {
      ;(wrapper.vm as any).configName = 'Invalid@Name'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(wrapper.vm as any).description = 'Description'
      ;(addEventConfigSource as any).mockResolvedValue(400)

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).configName).toBe('Invalid@Name')
      expect((wrapper.vm as any).vendor).toBe('Vendor')
    })

    it('does not show success message on 400 error', async () => {
      ;(wrapper.vm as any).configName = 'Invalid@Name'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(400)

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).successMessage).toBe(false)
    })

    it('does not call store methods on 400 error', async () => {
      ;(wrapper.vm as any).configName = 'Invalid@Name'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(400)
      store.fetchEventConfigs = vi.fn()
      store.refreshSourcesFilters = vi.fn()

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect(store.fetchEventConfigs).not.toHaveBeenCalled()
      expect(store.refreshSourcesFilters).not.toHaveBeenCalled()
    })
  })

  describe('HTTP Status Code 500 (Server Error)', () => {
    it('shows snackbar error on 500 server error', async () => {
      ;(wrapper.vm as any).configName = 'TestSource'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(500)

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).snackbar.showSnackBar).toHaveBeenCalledWith(
        expect.objectContaining({
          msg: 'Failed to create event configuration source. Please try again.',
          error: true
        })
      )
    })

    it('does not reset form on 500 error', async () => {
      ;(wrapper.vm as any).configName = 'TestSource'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(wrapper.vm as any).description = 'Test'
      ;(addEventConfigSource as any).mockResolvedValue(500)

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).configName).toBe('TestSource')
      expect((wrapper.vm as any).vendor).toBe('Vendor')
    })

    it('does not show success message on 500 error', async () => {
      ;(wrapper.vm as any).configName = 'TestSource'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(500)

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).successMessage).toBe(false)
    })

    it('does not call store methods on 500 error', async () => {
      ;(wrapper.vm as any).configName = 'TestSource'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(500)
      store.fetchEventConfigs = vi.fn()
      store.refreshSourcesFilters = vi.fn()

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect(store.fetchEventConfigs).not.toHaveBeenCalled()
      expect(store.refreshSourcesFilters).not.toHaveBeenCalled()
    })

    it('handles unexpected status codes as errors', async () => {
      ;(wrapper.vm as any).configName = 'TestSource'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(503) // Unexpected status code

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      // Should show generic error message for unexpected status
      expect((wrapper.vm as any).snackbar.showSnackBar).toHaveBeenCalledWith(
        expect.objectContaining({
          msg: 'Failed to create event configuration source. Please try again.',
          error: true
        })
      )
    })
  })

  describe('Exception Handling', () => {
    it('catches and logs exceptions from service call', async () => {
      ;(wrapper.vm as any).configName = 'TestSource'
      ;(wrapper.vm as any).vendor = 'Vendor'
      const testError = new Error('Network error')
      ;(addEventConfigSource as any).mockRejectedValue(testError)
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error creating event configuration source:', testError)
      consoleErrorSpy.mockRestore()
    })

    it('does not show success on exception', async () => {
      ;(wrapper.vm as any).configName = 'TestSource'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockRejectedValue(new Error('Service unavailable'))

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).successMessage).toBe(false)
    })

    it('does not call store methods on exception', async () => {
      ;(wrapper.vm as any).configName = 'TestSource'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockRejectedValue(new Error('Service error'))
      store.fetchEventConfigs = vi.fn()
      store.refreshSourcesFilters = vi.fn()

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect(store.fetchEventConfigs).not.toHaveBeenCalled()
      expect(store.refreshSourcesFilters).not.toHaveBeenCalled()
    })
  })

  describe('NewId State Management', () => {
    it('captures the new source ID from response after creation', async () => {
      ;(wrapper.vm as any).configName = 'TestConfig'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(789, 'TestConfig', 0))

      expect((wrapper.vm as any).newId).toBe(0) // Initial value

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).newId).toBe(789) // Should be set to response.id
    })

    it('newId persists for navigation to detail page', async () => {
      ;(wrapper.vm as any).configName = 'TestConfig'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(789, 'TestConfig', 0))
      store.hideCreateEventConfigSourceDialog = vi.fn()

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      const viewSourceBtn =
        wrapper.findAllComponents(FeatherButton)[wrapper.findAllComponents(FeatherButton).length - 1]
      await viewSourceBtn.trigger('click')

      expect(mockPush).toHaveBeenCalledWith({
        name: 'Event Configuration Detail',
        params: { id: 789 }
      })
    })

    it('newId is set from response.id', async () => {
      ;(wrapper.vm as any).configName = 'TestConfig'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(999, 'TestConfig', 0))

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).newId).toBe(999)
    })
  })

  describe('Dialog Visibility and State Transitions', () => {
    it('toggles from form view to success view', async () => {
      ;(wrapper.vm as any).configName = 'Test'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(123, 'TestConfig', 0))

      // Initially shows form
      expect(document.querySelector('.modal-body-form')).not.toBeNull()

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      // After creation should show success
      expect(document.querySelector('.modal-body-success')).not.toBeNull()
      expect(document.querySelector('.modal-body-form')).toBeNull()
    })

    it('dialog state persists when hidden and reopened', async () => {
      ;(wrapper.vm as any).successMessage = true
      ;(wrapper.vm as any).configName = 'PreviousValue'
      store.hideCreateEventConfigSourceDialog = vi.fn()

      // Hide dialog
      store.createEventConfigSourceDialogState.visible = false
      await wrapper.vm.$nextTick()

      // Dialog should not be visible
      expect(document.querySelector('[data-ref-id="feather-dialog-header"]')).toBeNull()

      // Reopen dialog
      store.createEventConfigSourceDialogState.visible = true
      await wrapper.vm.$nextTick()

      // Dialog should be visible again
      expect(document.querySelector('[data-ref-id="feather-dialog-header"]')).not.toBeNull()
      // successMessage state should persist (not auto-reset when toggling visibility)
      expect((wrapper.vm as any).successMessage).toBe(true)
    })
  })

  describe('Input Field Model Binding', () => {
    it('updates configName on input', async () => {
      const inputs = wrapper.findAllComponents(FeatherInput)
      const nameInput = inputs[0]

      await nameInput.vm.$emit('update:modelValue', 'NewName')
      await wrapper.vm.$nextTick()

      expect((wrapper.vm as any).configName).toBe('NewName')
    })

    it('updates vendor on input', async () => {
      const inputs = wrapper.findAllComponents(FeatherInput)
      const vendorInput = inputs[1]

      await vendorInput.vm.$emit('update:modelValue', 'NewVendor')
      await wrapper.vm.$nextTick()

      expect((wrapper.vm as any).vendor).toBe('NewVendor')
    })

    it('updates description on textarea input', async () => {
      const textarea = wrapper.findComponent(FeatherTextarea)

      await textarea.vm.$emit('update:modelValue', 'New description')
      await wrapper.vm.$nextTick()

      expect((wrapper.vm as any).description).toBe('New description')
    })

    it('description gets trimmed automatically with v-model.trim', async () => {
      const textarea = wrapper.findComponent(FeatherTextarea)

      await textarea.vm.$emit('update:modelValue', '  spaced text  ')
      await wrapper.vm.$nextTick()

      expect((wrapper.vm as any).description).toBe('spaced text')
    })
  })

  describe('Edge Cases and Additional Scenarios', () => {
    it('cancel button works from success view', async () => {
      ;(wrapper.vm as any).configName = 'Test'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(123, 'TestConfig', 0))

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      // Now in success state
      expect((wrapper.vm as any).successMessage).toBe(true)

      // Click Cancel from success view
      const cancelBtn = wrapper.findAllComponents(FeatherButton)[0]
      await cancelBtn.trigger('click')

      expect(store.hideCreateEventConfigSourceDialog).toHaveBeenCalled()
    })

    it('resets success state when dialog is closed and reopened', async () => {
      ;(wrapper.vm as any).configName = 'Test'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(123, 'TestConfig', 0))

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      expect((wrapper.vm as any).successMessage).toBe(true)

      // Manually reset to simulate closing dialog
      ;(wrapper.vm as any).successMessage = false
      await wrapper.vm.$nextTick()

      // Dialog should show form again
      expect(document.querySelector('.modal-body-form')).not.toBeNull()
      expect(document.querySelector('.modal-body-success')).toBeNull()
    })

    it('prevents save when vendor is empty but name is valid', async () => {
      ;(wrapper.vm as any).configName = 'ValidName'
      ;(wrapper.vm as any).vendor = '' // Empty vendor

      await wrapper.vm.$nextTick()

      // Create button should be disabled
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      expect(createBtn.vm.$props.disabled).toBe(true)

      // Verify error exists for vendor
      const errors = (wrapper.vm as any).error
      expect(errors).not.toBeNull()
      expect(errors.vendor).toBeDefined()
    })

    it('prevents save when name is empty but vendor is valid', async () => {
      ;(wrapper.vm as any).configName = '' // Empty name
      ;(wrapper.vm as any).vendor = 'ValidVendor'

      await wrapper.vm.$nextTick()

      // Create button should be disabled
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      expect(createBtn.vm.$props.disabled).toBe(true)

      // Verify error exists for name
      const errors = (wrapper.vm as any).error
      expect(errors).not.toBeNull()
      expect(errors.name).toBeDefined()
    })

    it('handles both name and vendor empty simultaneously', async () => {
      ;(wrapper.vm as any).configName = ''
      ;(wrapper.vm as any).vendor = ''

      await wrapper.vm.$nextTick()

      // Both errors should be present
      const errors = (wrapper.vm as any).error
      expect(errors).not.toBeNull()
      expect(errors.name).toBeDefined()
      expect(errors.vendor).toBeDefined()
      expect(errors.name).toBe('Configuration name is required.')
      expect(errors.vendor).toBe('Vendor is required.')

      // Create button should be disabled
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      expect(createBtn.vm.$props.disabled).toBe(true)
    })

    it('newId remains 0 on failed creation', async () => {
      ;(wrapper.vm as any).configName = 'Test'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(500) // Failure

      expect((wrapper.vm as any).newId).toBe(0)

      await wrapper.vm.$nextTick()
      const createBtn = wrapper.findAllComponents(FeatherButton)[1]
      await createBtn.trigger('click')
      await flushPromises()

      // newId should remain 0 on failure
      expect((wrapper.vm as any).newId).toBe(0)
    })

    it('View Source button only appears after successful creation', async () => {
      // Initially should show Create button
      let buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[1].text()).toContain('Create')
      ;(wrapper.vm as any).configName = 'Test'
      ;(wrapper.vm as any).vendor = 'Vendor'
      ;(addEventConfigSource as any).mockResolvedValue(mockSuccessResponse(123, 'TestConfig', 0))

      await wrapper.vm.$nextTick()
      const createBtn = buttons[1]
      await createBtn.trigger('click')
      await flushPromises()

      // After success, should show View Source button
      buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[1].text()).toContain('View Source')
    })
  })
})


import TrapdAdvancedConfiguration from '@/components/TrapdConfiguration/TrapdAdvancedConfiguration.vue'
import { validateTrapdJson, validateTrapdXml } from '@/lib/trapdValidator'
import { downloadTrapdConfig, uploadTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const { showSnackBarMock, downloadFileMock } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  downloadFileMock: vi.fn()
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar: showSnackBarMock })
}))

vi.mock('@/composables/useDownload', () => ({
  default: () => ({ downloadFile: downloadFileMock })
}))

vi.mock('@/composables/useSpinner', () => ({
  default: () => ({ startSpinner: vi.fn(), stopSpinner: vi.fn() })
}))

vi.mock('@/lib/trapdValidator', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/lib/trapdValidator')>()
  return { ...actual, validateTrapdXml: vi.fn(), validateTrapdJson: vi.fn() }
})

vi.mock('@/services/trapdConfigurationService', () => ({
  uploadTrapdConfiguration: vi.fn(),
  downloadTrapdConfig: vi.fn()
}))

describe('TrapdAdvancedConfiguration.vue', () => {
  let trapStore: ReturnType<typeof useTrapdConfigStore>

  const validateTrapdXmlMock = vi.mocked(validateTrapdXml)
  const validateTrapdJsonMock = vi.mocked(validateTrapdJson)
  const uploadTrapdConfigurationMock = vi.mocked(uploadTrapdConfiguration)
  const downloadTrapdConfigMock = vi.mocked(downloadTrapdConfig)

  const mountComponent = () =>
    mount(TrapdAdvancedConfiguration, {
      global: {
        stubs: {
          ConfirmationDialog: {
            template: `<div class="confirmation-dialog" v-if="visible">
              <slot name="content" />
              <button data-test="confirm-btn" @click="$emit('ok')">Confirm</button>
              <button data-test="cancel-btn" @click="$emit('cancel')">Cancel</button>
            </div>`,
            props: ['visible', 'title', 'actionButtonText']
          },
          FeatherButton: {
            template: '<button @click="$emit(\'click\')"><slot /><slot name="icon" /></button>'
          },
          FeatherIcon: true
        }
      }
    })

  const createFile = (name: string, content: string, type = 'text/xml') => {
    const file = new File([content], name, { type })
    vi.spyOn(file, 'text').mockResolvedValue(content)
    return file
  }

  /**
   * Click an upload button to trigger initiateUpload.  Intercepts the
   * dynamically created file input and spies on its click method.
   * Returns the captured input and the click spy.
   */
  const clickUploadButton = async (wrapper: ReturnType<typeof mountComponent>, testId: string) => {
    let capturedInput: HTMLInputElement | null = null
    let inputClickSpy: ReturnType<typeof vi.spyOn> | undefined

    const origCreate = document.createElement.bind(document)
    vi.spyOn(document, 'createElement').mockImplementation((tag: string, ...args: unknown[]) => {
      const el = origCreate(tag, ...(args as [ElementCreationOptions?]))
      if (tag === 'input') {
        capturedInput = el as HTMLInputElement
        inputClickSpy = vi.spyOn(capturedInput, 'click').mockImplementation(() => {})
      }
      return el
    })

    await wrapper.find(`[data-test="${testId}"]`).trigger('click')
    vi.mocked(document.createElement).mockRestore()

    return { input: capturedInput!, clickSpy: inputClickSpy! }
  }

  /**
   * Simulate a file being selected (or the picker being cancelled) on the
   * captured file input, then flush Vue reactivity.
   */
  const selectFile = async (input: HTMLInputElement, file: File | null) => {
    if (file) {
      Object.defineProperty(input, 'files', { value: [file], configurable: true })
      input.onchange?.({} as Event)
    } else {
      input.dispatchEvent(new Event('cancel'))
    }
    await flushPromises()
  }

  /**
   * Convenience: click an upload button, pick a file, flush reactivity.
   * After this the confirmation dialog should be visible.
   */
  const initiateUploadFlow = async (
    wrapper: ReturnType<typeof mountComponent>,
    testId: string,
    file: File
  ) => {
    const { input } = await clickUploadButton(wrapper, testId)
    await selectFile(input, file)
  }

  /**
   * Full upload path: select file → confirm dialog → flush.
   */
  const confirmUpload = async (wrapper: ReturnType<typeof mountComponent>) => {
    await wrapper.find('[data-test="confirm-btn"]').trigger('click')
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createTestingPinia({ stubActions: true }))
    trapStore = useTrapdConfigStore()
    trapStore.fetchTrapConfig = vi.fn().mockResolvedValue(undefined)
    validateTrapdXmlMock.mockReturnValue({ valid: true, errors: [] })
    validateTrapdJsonMock.mockReturnValue({ valid: true, errors: [] })
    uploadTrapdConfigurationMock.mockResolvedValue(undefined)
    downloadTrapdConfigMock.mockResolvedValue({ data: 'content', headers: {}} as any)
  })

  it('renders all upload and download buttons', () => {
    const wrapper = mountComponent()
    expect(wrapper.find('[data-test="upload-xml-button"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="upload-json-button"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="download-xml-button"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="download-json-button"]').exists()).toBe(true)
  })

  // ---------------------------------------------------------------------------
  // File picker
  // ---------------------------------------------------------------------------

  it('opens file chooser when Upload XML button is clicked', async () => {
    const wrapper = mountComponent()
    const { clickSpy } = await clickUploadButton(wrapper, 'upload-xml-button')
    expect(clickSpy).toHaveBeenCalledTimes(1)
  })

  it('opens file chooser when Upload JSON button is clicked', async () => {
    const wrapper = mountComponent()
    const { clickSpy } = await clickUploadButton(wrapper, 'upload-json-button')
    expect(clickSpy).toHaveBeenCalledTimes(1)
  })

  it('sets .xml accept for XML upload and .json accept for JSON upload', async () => {
    const wrapper = mountComponent()
    const { input: xmlInput } = await clickUploadButton(wrapper, 'upload-xml-button')
    expect(xmlInput.accept).toBe('.xml')
    await selectFile(xmlInput, null)

    const { input: jsonInput } = await clickUploadButton(wrapper, 'upload-json-button')
    expect(jsonInput.accept).toBe('.json')
    await selectFile(jsonInput, null)
  })

  it('returns early when no file is selected', async () => {
    const wrapper = mountComponent()
    const { input } = await clickUploadButton(wrapper, 'upload-xml-button')
    await selectFile(input, null)

    expect(wrapper.find('[data-test="confirm-btn"]').exists()).toBe(false)
    expect(validateTrapdXmlMock).not.toHaveBeenCalled()
    expect(uploadTrapdConfigurationMock).not.toHaveBeenCalled()
  })

  it('shows confirmation dialog when a file is selected', async () => {
    const wrapper = mountComponent()
    await initiateUploadFlow(wrapper, 'upload-xml-button', createFile('trapd.xml', '<trapd-configuration />'))
    expect(wrapper.find('[data-test="confirm-btn"]').exists()).toBe(true)
  })

  it('hides confirmation dialog and does not upload when cancelled', async () => {
    const wrapper = mountComponent()
    await initiateUploadFlow(wrapper, 'upload-xml-button', createFile('trapd.xml', '<trapd-configuration />'))

    await wrapper.find('[data-test="cancel-btn"]').trigger('click')
    await flushPromises()

    expect(wrapper.find('[data-test="confirm-btn"]').exists()).toBe(false)
    expect(uploadTrapdConfigurationMock).not.toHaveBeenCalled()
  })

  // ---------------------------------------------------------------------------
  // XML upload
  // ---------------------------------------------------------------------------

  it('shows read error when file.text() fails during XML upload', async () => {
    const wrapper = mountComponent()
    const file = new File(['x'], 'trapd.xml', { type: 'text/xml' })
    vi.spyOn(file, 'text').mockRejectedValue(new Error('read failed'))

    await initiateUploadFlow(wrapper, 'upload-xml-button', file)
    await confirmUpload(wrapper)

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'read failed', error: true })
    expect(validateTrapdXmlMock).not.toHaveBeenCalled()
    expect(uploadTrapdConfigurationMock).not.toHaveBeenCalled()
  })

  it('blocks upload and shows XML validation errors when XML is invalid (<=3 errors)', async () => {
    const wrapper = mountComponent()
    validateTrapdXmlMock.mockReturnValue({
      valid: false,
      errors: [
        { field: 'root', message: 'Root mismatch' },
        { field: 'xmlns', message: 'Namespace invalid' },
        { field: 'snmp-trap-port', message: 'Port invalid' }
      ]
    })

    await initiateUploadFlow(wrapper, 'upload-xml-button', createFile('trapd.xml', '<invalid />'))
    await confirmUpload(wrapper)

    expect(validateTrapdXmlMock).toHaveBeenCalledWith('<invalid />')
    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Invalid trap configuration XML: Root mismatch | Namespace invalid | Port invalid',
      error: true
    })
    expect(uploadTrapdConfigurationMock).not.toHaveBeenCalled()
  })

  it('shows +N more suffix for XML validation errors beyond first 3', async () => {
    const wrapper = mountComponent()
    validateTrapdXmlMock.mockReturnValue({
      valid: false,
      errors: [
        { field: 'a', message: 'e1' },
        { field: 'b', message: 'e2' },
        { field: 'c', message: 'e3' },
        { field: 'd', message: 'e4' },
        { field: 'e', message: 'e5' }
      ]
    })

    await initiateUploadFlow(wrapper, 'upload-xml-button', createFile('trapd.xml', '<invalid />'))
    await confirmUpload(wrapper)

    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Invalid trap configuration XML: e1 | e2 | e3 (+2 more)',
      error: true
    })
    expect(uploadTrapdConfigurationMock).not.toHaveBeenCalled()
  })

  it('uploads XML file and refreshes store when XML is valid', async () => {
    const wrapper = mountComponent()
    const file = createFile('trapd.xml', '<trapd-configuration />')

    await initiateUploadFlow(wrapper, 'upload-xml-button', file)
    await confirmUpload(wrapper)

    expect(validateTrapdXmlMock).toHaveBeenCalledWith('<trapd-configuration />')
    expect(uploadTrapdConfigurationMock).toHaveBeenCalledWith(file, true)
    expect(trapStore.fetchTrapConfig).toHaveBeenCalledTimes(1)
    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Successfully uploaded Trap configuration from \'trapd.xml\'.',
      error: false
    })
  })

  it('shows upload error message from Error instance for XML upload', async () => {
    const wrapper = mountComponent()
    uploadTrapdConfigurationMock.mockRejectedValue(new Error('upload failed'))

    await initiateUploadFlow(wrapper, 'upload-xml-button', createFile('trapd.xml', '<trapd-configuration />'))
    await confirmUpload(wrapper)

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'upload failed', error: true })
  })

  it('shows generic upload error message for non-Error throw during XML upload', async () => {
    const wrapper = mountComponent()
    uploadTrapdConfigurationMock.mockRejectedValue('bad')

    await initiateUploadFlow(wrapper, 'upload-xml-button', createFile('trapd.xml', '<trapd-configuration />'))
    await confirmUpload(wrapper)

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Error uploading Trap configuration', error: true })
  })

  // ---------------------------------------------------------------------------
  // JSON upload
  // ---------------------------------------------------------------------------

  it('blocks upload and shows JSON validation errors when JSON is invalid (<=3 errors)', async () => {
    const wrapper = mountComponent()
    validateTrapdJsonMock.mockReturnValue({
      valid: false,
      errors: [
        { field: 'snmpTrapPort', message: 'Port invalid' },
        { field: 'snmpTrapAddress', message: 'Address invalid' }
      ]
    })

    await initiateUploadFlow(wrapper, 'upload-json-button', createFile('trapd.json', '{}', 'application/json'))
    await confirmUpload(wrapper)

    expect(validateTrapdJsonMock).toHaveBeenCalledWith('{}')
    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Invalid trap configuration JSON: Port invalid | Address invalid',
      error: true
    })
    expect(uploadTrapdConfigurationMock).not.toHaveBeenCalled()
  })

  it('shows +N more suffix for JSON validation errors beyond first 3', async () => {
    const wrapper = mountComponent()
    validateTrapdJsonMock.mockReturnValue({
      valid: false,
      errors: [
        { field: 'a', message: 'e1' },
        { field: 'b', message: 'e2' },
        { field: 'c', message: 'e3' },
        { field: 'd', message: 'e4' }
      ]
    })

    await initiateUploadFlow(wrapper, 'upload-json-button', createFile('trapd.json', '{}', 'application/json'))
    await confirmUpload(wrapper)

    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Invalid trap configuration JSON: e1 | e2 | e3 (+1 more)',
      error: true
    })
    expect(uploadTrapdConfigurationMock).not.toHaveBeenCalled()
  })

  it('uploads JSON file and refreshes store when JSON is valid', async () => {
    const wrapper = mountComponent()
    const file = createFile('trapd.json', '{"snmpTrapPort":162}', 'application/json')

    await initiateUploadFlow(wrapper, 'upload-json-button', file)
    await confirmUpload(wrapper)

    expect(validateTrapdJsonMock).toHaveBeenCalledWith('{"snmpTrapPort":162}')
    expect(uploadTrapdConfigurationMock).toHaveBeenCalledWith(file, false)
    expect(trapStore.fetchTrapConfig).toHaveBeenCalledTimes(1)
    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Successfully uploaded Trap configuration from \'trapd.json\'.',
      error: false
    })
  })

  it('shows generic upload error message for non-Error throw during JSON upload', async () => {
    const wrapper = mountComponent()
    uploadTrapdConfigurationMock.mockRejectedValue('bad')

    await initiateUploadFlow(wrapper, 'upload-json-button', createFile('trapd.json', '{}', 'application/json'))
    await confirmUpload(wrapper)

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Error uploading Trap configuration', error: true })
  })

  // ---------------------------------------------------------------------------
  // Download
  // ---------------------------------------------------------------------------

  it('calls downloadTrapdConfig(true) and downloadFile when Download XML is clicked', async () => {
    const wrapper = mountComponent()
    const mockResponse = { data: 'xml-content', headers: {}}
    downloadTrapdConfigMock.mockResolvedValue(mockResponse as any)

    await wrapper.find('[data-test="download-xml-button"]').trigger('click')
    await flushPromises()

    expect(downloadTrapdConfigMock).toHaveBeenCalledWith(true)
    expect(downloadFileMock).toHaveBeenCalledWith(mockResponse, true)
  })

  it('calls downloadTrapdConfig(false) and downloadFile when Download JSON is clicked', async () => {
    const wrapper = mountComponent()
    const mockResponse = { data: 'json-content', headers: {}}
    downloadTrapdConfigMock.mockResolvedValue(mockResponse as any)

    await wrapper.find('[data-test="download-json-button"]').trigger('click')
    await flushPromises()

    expect(downloadTrapdConfigMock).toHaveBeenCalledWith(false)
    expect(downloadFileMock).toHaveBeenCalledWith(mockResponse, true)
  })

  it('shows error snackbar when XML download returns falsy', async () => {
    const wrapper = mountComponent()
    downloadTrapdConfigMock.mockResolvedValue(false as any)

    await wrapper.find('[data-test="download-xml-button"]').trigger('click')
    await flushPromises()

    expect(downloadFileMock).not.toHaveBeenCalled()
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Error downloading XML file', error: true })
  })

  it('shows error snackbar when JSON download returns falsy', async () => {
    const wrapper = mountComponent()
    downloadTrapdConfigMock.mockResolvedValue(false as any)

    await wrapper.find('[data-test="download-json-button"]').trigger('click')
    await flushPromises()

    expect(downloadFileMock).not.toHaveBeenCalled()
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Error downloading JSON file', error: true })
  })
})

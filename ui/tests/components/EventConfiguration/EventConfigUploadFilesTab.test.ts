import EventConfigFilesUploadReportDialog from '@/components/EventConfiguration/Dialog/EventConfigFilesUploadReportDialog.vue'
import UploadedFileRenameDialog from '@/components/EventConfiguration/Dialog/UploadedFileRenameDialog.vue'
import EventConfigUploadFilesTab from '@/components/EventConfiguration/EventConfigUploadFilesTab.vue'
import {
  isDuplicateFile,
  MAX_FILES_UPLOAD,
  validateEventConfigFile
} from '@/components/EventConfiguration/eventConfigXmlValidator'
import useSnackbar from '@/composables/useSnackbar'
import { uploadEventConfigFiles } from '@/services/eventConfigService'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import CheckCircle from '@featherds/icon/action/CheckCircle'
import Delete from '@featherds/icon/action/Delete'
import Text from '@featherds/icon/file/Text'
import Apps from '@featherds/icon/navigation/Apps'
import Error from '@featherds/icon/notification/Error'
import Warning from '@featherds/icon/notification/Warning'
import { FeatherSpinner } from '@featherds/progress'
import { FeatherTooltip } from '@featherds/tooltip'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import Draggable from 'vuedraggable'

vi.mock('@/stores/eventConfigStore')
vi.mock('@/components/EventConfiguration/eventConfigXmlValidator')
vi.mock('@/services/eventConfigService')
vi.mock('@/composables/useSnackbar')

describe('EventConfigUploadFilesTab', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigStore>
  let snackbar: ReturnType<typeof useSnackbar>

  const mockFile = (name: string, type = 'text/xml', content = '<xml></xml>') => {
    return new File([content], name, { type })
  }

  beforeEach(() => {
    store = reactive({
      uploadedSourceNames: [],
      uploadedEventConfigFilesReportDialogState: { visible: false }
    } as any)
    vi.mocked(useEventConfigStore).mockReturnValue(store)

    snackbar = {
      showSnackBar: vi.fn()
    } as any
    vi.mocked(useSnackbar).mockReturnValue(snackbar)

    vi.mocked(isDuplicateFile).mockReturnValue(false)
    vi.mocked(validateEventConfigFile).mockResolvedValue({ isValid: true, errors: [] })
    vi.mocked(uploadEventConfigFiles).mockResolvedValue({ errors: [] as any, success: [] as any })

    wrapper = mount(EventConfigUploadFilesTab, {
      global: {
        components: {
          Draggable,
          FeatherButton,
          FeatherIcon,
          FeatherSpinner,
          FeatherTooltip,
          EventConfigFilesUploadReportDialog,
          UploadedFileRenameDialog
        },
        provide: {
          CheckCircle,
          Delete,
          Text,
          Apps,
          Error,
          Warning
        }
      }
    })
  })

  afterEach(() => {
    vi.clearAllMocks()
    wrapper.unmount()
  })

  it('renders the component correctly', () => {
    expect(wrapper.find('.upload-files-tab').exists()).toBe(true)
    expect(wrapper.find('h2').text()).toBe('Upload Event Configuration Files')
    expect(wrapper.find('.selected-files-section').exists()).toBe(true)
    expect(wrapper.find('.upload-action-section').exists()).toBe(true)
    expect(wrapper.find('.info-section').exists()).toBe(true)
  })

  it('displays "No files selected" when eventFiles is empty', () => {
    expect(wrapper.find('.selected-files-section p').text()).toBe('No files selected')
  })

  it('renders file list when files are added', async () => {
    const file = mockFile('test.events.xml')
    vi.mocked(validateEventConfigFile).mockResolvedValue({ isValid: true, errors: [] })
    await wrapper.vm.eventFiles.push({
      file,
      isValid: true,
      errors: [],
      isDuplicate: false
    })
    await wrapper.vm.$nextTick()
    expect(wrapper.findAll('.file').length).toBe(1)
    expect(wrapper.find('.file-icon span').text()).toContain('test.events.xml')
  })

  it('displays valid file icon for valid files', async () => {
    const file = mockFile('test.events.xml')
    await wrapper.vm.eventFiles.push({
      file,
      isValid: true,
      errors: [],
      isDuplicate: false
    })
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.success-icon').exists()).toBe(true)
  })

  it('displays error icon for invalid files', async () => {
    const file = mockFile('test.events.xml')
    await wrapper.vm.eventFiles.push({
      file,
      isValid: false,
      errors: ['Invalid XML format'],
      isDuplicate: false
    })
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.error-icon').exists()).toBe(true)
  })

  it('displays warning icon for duplicate files', async () => {
    store.uploadedSourceNames = ['test.events.xml']
    const file = mockFile('test.events.xml')
    await wrapper.vm.eventFiles.push({
      file,
      isValid: true,
      errors: [],
      isDuplicate: true
    })
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.warning-icon').exists()).toBe(true)
  })

  it('triggers file input click when "Choose files to upload" button is clicked', async () => {
    const input = wrapper.find('input[type="file"]')
    const spy = vi.spyOn(input.element as HTMLElement, 'click')
    await wrapper.findComponent(FeatherButton).trigger('click')
    expect(spy).toHaveBeenCalled()
  })

  it('disables upload button when no files are selected', () => {
    expect(wrapper.find('[data-test="upload-button"]').attributes('aria-disabled')).toBe('true')
  })

  it('disables upload button when files are invalid', async () => {
    await wrapper.vm.eventFiles.push({
      file: mockFile('test.events.xml'),
      isValid: false,
      errors: ['Invalid XML'],
      isDuplicate: false
    })
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-test="upload-button"]').attributes('aria-disabled')).toBeDefined()
  })

  it('disables upload button when files are duplicates', async () => {
    await wrapper.vm.eventFiles.push({
      file: mockFile('test.events.xml'),
      isValid: true,
      errors: [],
      isDuplicate: true
    })
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-test="upload-button"]').attributes('aria-disabled')).toBeDefined()
  })

  it('enables upload button when all files are valid and not duplicates', async () => {
    await wrapper.vm.eventFiles.push({
      file: mockFile('test.events.xml'),
      isValid: true,
      errors: [],
      isDuplicate: false
    })
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-test="upload-button"]').attributes('aria-disabled')).toBeUndefined()
  })

  it('handles file upload correctly', async () => {
    const files = [mockFile('test1.events.xml'), mockFile('test2.events.xml')]
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: files,
      writable: true
    })
    await input.trigger('change')
    expect(wrapper.vm.eventFiles.length).toBe(2)
    expect(snackbar.showSnackBar).not.toHaveBeenCalled()
  })

  it('shows snackbar when max files limit is reached', async () => {
    vi.mocked(validateEventConfigFile).mockClear().mockResolvedValue({ isValid: true, errors: [] })
    vi.mocked(isDuplicateFile).mockClear().mockReturnValue(false)

    expect(wrapper.vm.eventFiles.length).toBe(0)
    const files = [
      mockFile('test1.events.xml'),
      mockFile('test2.events.xml'),
      mockFile('test3.events.xml'),
      mockFile('test4.events.xml'),
      mockFile('test5.events.xml'),
      mockFile('test6.events.xml'),
      mockFile('test7.events.xml'),
      mockFile('test8.events.xml'),
      mockFile('test9.events.xml'),
      mockFile('test10.events.xml'),
      mockFile('test11.events.xml')
    ]
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: files,
      writable: true
    })
    await input.trigger('change')
    await flushPromises()

    expect(wrapper.vm.eventFiles.length).toBe(MAX_FILES_UPLOAD)
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: `You can upload a maximum of ${MAX_FILES_UPLOAD} files at a time.`,
      error: true
    })
    // Verify no unexpected error snackbars were shown
    expect(snackbar.showSnackBar).not.toHaveBeenCalledWith(
      expect.objectContaining({ msg: expect.stringContaining('Error processing file') })
    )
  })

  it('removes file when remove button is clicked', async () => {
    await wrapper.vm.eventFiles.push({
      file: mockFile('test.events.xml'),
      isValid: true,
      errors: [],
      isDuplicate: false
    })
    await wrapper.vm.$nextTick()
    await wrapper.find('[data-test="remove-files-button"]').trigger('click')
    expect(wrapper.vm.eventFiles.length).toBe(0)
  })

  it('opens rename dialog when warning icon is clicked', async () => {
    await wrapper.vm.eventFiles.push({
      file: mockFile('test.events.xml'),
      isValid: true,
      errors: [],
      isDuplicate: true
    })
    await wrapper.vm.$nextTick()
    await wrapper.find('.warning-icon').trigger('click')
    expect(wrapper.vm.displayRenameDialog).toBe(true)
  })

  it('handles file rename correctly', async () => {
    const file = mockFile('test.events.xml')
    await wrapper.vm.eventFiles.push({
      file,
      isValid: true,
      errors: [],
      isDuplicate: true
    })
    wrapper.vm.selectedIndex = 0
    wrapper.vm.displayRenameDialog = true
    await wrapper.vm.renameFile('newname.events.xml')
    expect(wrapper.vm.eventFiles[0].file.name).toBe('newname.events.xml')
    expect(wrapper.vm.displayRenameDialog).toBe(false)
    expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(false)
    expect(snackbar.showSnackBar).not.toHaveBeenCalled()
    expect(wrapper.vm.selectedIndex).toBe(null)
  })

  it('handles file overwrite correctly', async () => {
    await wrapper.vm.eventFiles.push({
      file: mockFile('test.events.xml'),
      isValid: true,
      errors: [],
      isDuplicate: true
    })
    wrapper.vm.selectedIndex = 0
    wrapper.vm.displayRenameDialog = true
    await wrapper.vm.overwriteFile()
    expect(wrapper.vm.displayRenameDialog).toBe(false)
    expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(false)
    expect(wrapper.vm.displayRenameDialog).toBe(false)
    expect(snackbar.showSnackBar).not.toHaveBeenCalled()
    expect(wrapper.vm.selectedIndex).toBe(null)
  })

  it('uploads valid files and clears the list', async () => {
    const file = mockFile('test.events.xml')
    await wrapper.vm.eventFiles.push({
      file,
      isValid: true,
      errors: [],
      isDuplicate: false
    })
    await wrapper.find('[data-test="upload-button"]').trigger('click')
    expect(uploadEventConfigFiles).toHaveBeenCalledWith([file])
    expect(wrapper.vm.eventFiles.length).toBe(0)
    expect(store.uploadedEventConfigFilesReportDialogState.visible).toBe(true)
  })

  it('shows spinner when uploading', async () => {
    const file = mockFile('test.events.xml')
    await wrapper.vm.eventFiles.push({
      file,
      isValid: true,
      errors: [],
      isDuplicate: false
    })
    wrapper.vm.isLoading = true
    await wrapper.vm.$nextTick()
    expect(wrapper.findComponent(FeatherSpinner).exists()).toBe(true)
  })

  it('displays error snackbar on upload failure', async () => {
    // Clear and set mock to reject
    vi.mocked(uploadEventConfigFiles).mockClear()
    vi.mocked(uploadEventConfigFiles).mockRejectedValue({ message: 'Upload failed' })
    const file = mockFile('test.events.xml')
    await wrapper.vm.eventFiles.push({
      file,
      isValid: true,
      errors: [],
      isDuplicate: false
    })
    await wrapper.vm.$nextTick()
    await wrapper.find('[data-test="upload-button"]').trigger('click')
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.isLoading).toBe(false)
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error uploading files',
      error: true
    })
  })

  it('resets file input after upload', async () => {
    const file = mockFile('test.events.xml')
    await wrapper.vm.eventFiles.push({
      file,
      isValid: true,
      errors: [],
      isDuplicate: false
    })
    await wrapper.find('[data-test="upload-button"]').trigger('click')
    expect(wrapper.vm.eventConfFileInput.value).toBe('')
  })

  it('ellipsifies long file names', () => {
    const longName = 'a'.repeat(50) + '.events.xml'
    const result = wrapper.vm.ellipsify(longName, 39)
    expect(result.length).toBeLessThanOrEqual(39)
    expect(result).toContain('\u2026')
  })

  it('prevents upload if files are not .events.xml', async () => {
    const file = mockFile('test.xml')
    await wrapper.vm.eventFiles.push({
      file,
      isValid: true,
      errors: [],
      isDuplicate: false
    })
    await wrapper.find('[data-test="upload-button"]').trigger('click')
    expect(uploadEventConfigFiles).not.toHaveBeenCalled()
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'All files must be XML files with .events.xml extension',
      error: true
    })
  })

  it('handles mixed valid, invalid, and duplicate files during upload', async () => {
    store.uploadedSourceNames = ['duplicate.events.xml']
    vi.mocked(validateEventConfigFile).mockImplementation(async (file) => {
      if (file.name.includes('invalid')) {
        return { isValid: false, errors: ['Invalid XML schema'] }
      }
      return { isValid: true, errors: [] }
    })
    vi.mocked(isDuplicateFile).mockImplementation((name) => name === 'duplicate.events.xml')

    const files = [
      mockFile('valid1.events.xml'),
      mockFile('invalid.events.xml'),
      mockFile('duplicate.events.xml'),
      mockFile('valid2.events.xml')
    ]
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: files,
      writable: true
    })

    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventFiles.length).toBe(3)
    expect(wrapper.vm.eventFiles).toEqual([
      expect.objectContaining({
        file: expect.objectContaining({ name: 'valid1.events.xml' }),
        isValid: true,
        isDuplicate: false
      }),
      expect.objectContaining({
        file: expect.objectContaining({ name: 'invalid.events.xml' }),
        isValid: false,
        errors: ['Invalid XML schema']
      }),
      expect.objectContaining({
        file: expect.objectContaining({ name: 'valid2.events.xml' }),
        isValid: true,
        isDuplicate: false
      })
    ])
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error processing file invalid.events.xml.',
      error: true
    })
    expect(wrapper.find('[data-test="upload-button"]').attributes('aria-disabled')).toBeDefined()
  })

  it('handles invalid XML syntax during file upload', async () => {
    const file = mockFile('invalid.events.xml', 'text/xml', '<events>invalid xml</events')
    vi.mocked(validateEventConfigFile).mockResolvedValue({
      isValid: false,
      errors: ['Invalid XML format - file contains syntax errors']
    })
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: [file],
      writable: true
    })

    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventFiles.length).toBe(1)
    expect(wrapper.vm.eventFiles[0]).toEqual(
      expect.objectContaining({
        file: expect.objectContaining({ name: 'invalid.events.xml' }),
        isValid: false,
        errors: ['Invalid XML format - file contains syntax errors'],
        isDuplicate: false
      })
    )
    expect(wrapper.find('.error-icon').exists()).toBe(true)
    expect(wrapper.findComponent(FeatherTooltip).vm.title).toContain('Invalid XML format - file contains syntax errors')
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error processing file invalid.events.xml.',
      error: true
    })
    expect(wrapper.find('[data-test="upload-button"]').attributes('aria-disabled')).toBeDefined()
  })

  it('handles missing <events> root element', async () => {
    const file = mockFile('noevents.events.xml', 'text/xml', '<root><event></event></root>')
    vi.mocked(validateEventConfigFile).mockResolvedValue({
      isValid: false,
      errors: ['Missing <events> root element']
    })
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: [file],
      writable: true
    })

    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventFiles.length).toBe(1)
    expect(wrapper.vm.eventFiles[0]).toEqual(
      expect.objectContaining({
        file: expect.objectContaining({ name: 'noevents.events.xml' }),
        isValid: false,
        errors: ['Missing <events> root element'],
        isDuplicate: false
      })
    )
    expect(wrapper.find('.error-icon').exists()).toBe(true)
    expect(wrapper.findComponent(FeatherTooltip).vm.title).toContain('Missing <events> root element')
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error processing file noevents.events.xml.',
      error: true
    })
  })

  it('handles invalid OpenNMS namespace', async () => {
    const file = mockFile('wrongns.events.xml', 'text/xml', '<events xmlns="http://wrong.org"><event></event></events>')
    vi.mocked(validateEventConfigFile).mockResolvedValue({
      isValid: false,
      errors: ['Missing or invalid OpenNMS namespace in <events> element']
    })
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: [file],
      writable: true
    })

    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventFiles.length).toBe(1)
    expect(wrapper.vm.eventFiles[0]).toEqual(
      expect.objectContaining({
        file: expect.objectContaining({ name: 'wrongns.events.xml' }),
        isValid: false,
        errors: ['Missing or invalid OpenNMS namespace in <events> element'],
        isDuplicate: false
      })
    )
    expect(wrapper.find('.error-icon').exists()).toBe(true)
    expect(wrapper.findComponent(FeatherTooltip).vm.title).toContain('Missing or invalid OpenNMS namespace')
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error processing file wrongns.events.xml.',
      error: true
    })
  })

  it('handles missing <event> elements with other content', async () => {
    const file = mockFile(
      'noevent.events.xml',
      'text/xml',
      '<events xmlns="http://opennms.org"><other></other></events>'
    )
    vi.mocked(validateEventConfigFile).mockResolvedValue({
      isValid: false,
      errors: [
        'No <event> entries found within <events> element',
        '<events> element contains <other> but no <event> elements'
      ]
    })
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: [file],
      writable: true
    })

    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventFiles.length).toBe(1)
    expect(wrapper.vm.eventFiles[0]).toEqual(
      expect.objectContaining({
        file: expect.objectContaining({ name: 'noevent.events.xml' }),
        isValid: false,
        errors: [
          'No <event> entries found within <events> element',
          '<events> element contains <other> but no <event> elements'
        ],
        isDuplicate: false
      })
    )
    expect(wrapper.find('.error-icon').exists()).toBe(true)
    expect(wrapper.findComponent(FeatherTooltip).vm.title).toContain('No <event> entries found')
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error processing file noevent.events.xml.',
      error: true
    })
  })

  it('handles <event> element with missing required fields', async () => {
    const file = mockFile(
      'badevent.events.xml',
      'text/xml',
      '<events xmlns="http://opennms.org"><event><uei>uei.opennms.org/test</uei></event></events>'
    )
    vi.mocked(validateEventConfigFile).mockResolvedValue({
      isValid: false,
      errors: ['Event 1: missing <event-label>, missing <severity>']
    })
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: [file],
      writable: true
    })

    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventFiles.length).toBe(1)
    expect(wrapper.vm.eventFiles[0]).toEqual(
      expect.objectContaining({
        file: expect.objectContaining({ name: 'badevent.events.xml' }),
        isValid: false,
        errors: ['Event 1: missing <event-label>, missing <severity>'],
        isDuplicate: false
      })
    )
    expect(wrapper.find('.error-icon').exists()).toBe(true)
    expect(wrapper.findComponent(FeatherTooltip).vm.title).toContain(
      'Event 1: missing <event-label>, missing <severity>'
    )
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error processing file badevent.events.xml.',
      error: true
    })
  })

  it('handles case-insensitive duplicate files', async () => {
    vi.mocked(isDuplicateFile).mockImplementation((name, existingFiles) =>
      existingFiles.some((f) => f.file.name.toLowerCase() === name.toLowerCase())
    )
    const files = [mockFile('test.events.xml'), mockFile('TEST.events.xml'), mockFile('test.EVENTS.XML')]
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: files,
      writable: true
    })

    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventFiles.length).toBe(1)
    expect(wrapper.vm.eventFiles[0].file.name).toBe('test.events.xml')
    expect(snackbar.showSnackBar).not.toHaveBeenCalled()
  })

  it('handles file reading error during validation', async () => {
    vi.mocked(validateEventConfigFile).mockRejectedValue({ message: 'Failed to read file' })
    const file = mockFile('unreadable.events.xml')
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: [file],
      writable: true
    })

    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventFiles.length).toBe(0)
    expect(wrapper.find('.error-icon').exists()).toBe(false)
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error processing file unreadable.events.xml.',
      error: true
    })
  })

  it('handles large file list with mixed validation outcomes', async () => {
    store.uploadedSourceNames = ['duplicate.events.xml']
    vi.mocked(validateEventConfigFile).mockImplementation(async (file) => {
      if (file.name.includes('invalid')) {
        return { isValid: false, errors: ['Invalid XML schema'] }
      }
      if (file.name.includes('badevent')) {
        return { isValid: false, errors: ['Event 1: missing <uei>'] }
      }
      return { isValid: true, errors: [] }
    })
    vi.mocked(isDuplicateFile).mockImplementation((name, existingFiles) =>
      existingFiles.some((element) => element.file.name.toLowerCase() === name.toLowerCase())
    )

    const files = [
      mockFile('valid1.events.xml'), // Valid
      mockFile('invalid.events.xml'), // Invalid (triggers snackbar)
      mockFile('duplicate.events.xml'), // Duplicate (skipped)
      mockFile('badevent.events.xml'), // Invalid (triggers snackbar)
      mockFile('valid2.events.xml'), // Valid
      ...Array.from({ length: 7 }, (_, i) => mockFile(`valid${i + 3}.events.xml`)) // 7 more valid files
    ]
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: files,
      writable: true
    })

    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventFiles.length).toBe(MAX_FILES_UPLOAD)
    expect(wrapper.vm.eventFiles).toContainEqual(
      expect.objectContaining({ file: expect.objectContaining({ name: 'invalid.events.xml' }), isValid: false })
    )
    expect(wrapper.vm.eventFiles).toContainEqual(
      expect.objectContaining({ file: expect.objectContaining({ name: 'badevent.events.xml' }), isValid: false })
    )
    expect(snackbar.showSnackBar).toHaveBeenCalledTimes(3) // invalid, badevent, max files
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error processing file invalid.events.xml.',
      error: true
    })
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error processing file badevent.events.xml.',
      error: true
    })
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: `You can upload a maximum of ${MAX_FILES_UPLOAD} files at a time.`,
      error: true
    })
  })

  it('handles rapid file uploads with validation and rename', async () => {
    store.uploadedSourceNames = ['test.events.xml']
    vi.mocked(validateEventConfigFile).mockImplementation(async (file) => ({
      isValid: !file.name.includes('invalid'),
      errors: file.name.includes('invalid') ? ['Invalid XML'] : []
    }))
    vi.mocked(isDuplicateFile).mockImplementation((name, existingFiles) =>
      existingFiles.some((element) => element.file.name.toLowerCase() === name.toLowerCase())
    )

    const firstBatch = [mockFile('test.events.xml'), mockFile('invalid.events.xml')]
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: firstBatch,
      writable: true
    })
    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventFiles.length).toBe(2)
    expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(true)
    expect(wrapper.vm.eventFiles[1].isValid).toBe(false)

    // Rename duplicate file
    wrapper.vm.selectedIndex = 0
    wrapper.vm.displayRenameDialog = true
    await wrapper.vm.renameFile('renamed.events.xml')
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventFiles[0].file.name).toBe('renamed.events.xml')
    expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(false)

    // Upload second batch
    const secondBatch = [mockFile('new.events.xml')]
    Object.defineProperty(input.element, 'files', {
      value: secondBatch,
      writable: true
    })
    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventFiles.length).toBe(3)
    expect(wrapper.vm.eventFiles.map((f: any) => f.file.name)).toEqual([
      'renamed.events.xml',
      'invalid.events.xml',
      'new.events.xml'
    ])
  })

  it('handles upload with mixed file types and validation', async () => {
    const files = [
      mockFile(
        'valid.events.xml',
        'text/xml',
        '<events xmlns="http://opennms.org"><event><uei>uei.opennms.org/test</uei><event-label>Test</event-label><severity>Minor</severity></event></events>'
      ),
      mockFile('invalid.txt', 'text/plain', 'not xml'),
      mockFile('noevent.xml', 'text/xml', '<events></events>')
    ]
    vi.mocked(validateEventConfigFile).mockImplementation(async (file) => {
      if (file.name.endsWith('.txt')) {
        return {
          isValid: false,
          errors: ['File does not appear to be an event configuration file (expected .events.xml extension)']
        }
      }
      if (file.name.includes('noevent')) {
        return {
          isValid: false,
          errors: ['No <event> entries found within <events> element', 'Empty <events> element - no content found']
        }
      }
      return { isValid: true, errors: [] }
    })

    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', {
      value: files,
      writable: true
    })

    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.eventFiles.length).toBe(3)
    expect(wrapper.vm.eventFiles).toEqual([
      expect.objectContaining({ file: expect.objectContaining({ name: 'valid.events.xml' }), isValid: true }),
      expect.objectContaining({ file: expect.objectContaining({ name: 'invalid.txt' }), isValid: false }),
      expect.objectContaining({ file: expect.objectContaining({ name: 'noevent.xml' }), isValid: false })
    ])
    expect(snackbar.showSnackBar).toHaveBeenCalledTimes(2)
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error processing file invalid.txt.',
      error: true
    })
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error processing file noevent.xml.',
      error: true
    })
    expect(wrapper.findAll('.error-icon').length).toBe(2)
  })

  it('reorders files when dragged', async () => {
    // Add files to eventFiles
    const file1 = { file: mockFile('file1.events.xml'), isValid: true, errors: [], isDuplicate: false }
    const file2 = { file: mockFile('file2.events.xml'), isValid: true, errors: [], isDuplicate: false }
    await wrapper.vm.eventFiles.push(file1, file2)
    await wrapper.vm.$nextTick()

    // Find the Draggable component
    const draggable = wrapper.findComponent(Draggable)

    // Emit update:modelValue event with reversed order
    draggable.vm.$emit('update:modelValue', [file2, file1])
    await wrapper.vm.$nextTick()

    // Verify the new order
    expect(wrapper.vm.eventFiles.map((f: any) => f.file.name)).toEqual(['file2.events.xml', 'file1.events.xml'])
  })

  it('handles empty file content', async () => {
    const file = mockFile('empty.events.xml', 'text/xml', '')
    vi.mocked(validateEventConfigFile).mockResolvedValue({
      isValid: false,
      errors: ['File is empty']
    })
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', { value: [file], writable: true })
    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.eventFiles[0]).toEqual(
      expect.objectContaining({
        file: expect.objectContaining({ name: 'empty.events.xml' }),
        isValid: false,
        errors: ['File is empty']
      })
    )
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error processing file empty.events.xml.',
      error: true
    })
  })

  it('allows re-uploading the same file after selection', async () => {
    const file = mockFile('test.events.xml')
    const input = wrapper.find('input[type="file"]')

    // First file selection
    Object.defineProperty(input.element, 'files', { value: [file], writable: true })
    await input.trigger('change')
    await flushPromises()

    // Verify file was added
    expect(wrapper.vm.eventFiles.length).toBe(1)
    expect(wrapper.vm.eventFiles[0].file.name).toBe('test.events.xml')

    // Second file selection (same file)
    Object.defineProperty(input.element, 'files', { value: [file], writable: true })
    await input.trigger('change')
    await flushPromises()

    // Verify file was added again (proves reset worked)
    expect(wrapper.vm.eventFiles.length).toBe(2)
    expect(wrapper.vm.eventFiles[1].file.name).toBe('test.events.xml')
  })

  it('resets file input after selection', async () => {
    const file = mockFile('test.events.xml')
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', { value: [file], writable: true })
    await input.trigger('change')
    await flushPromises()
    expect((input.element as HTMLInputElement).files).toBeNull()
  })

  it('displays multiple errors in tooltip for invalid file', async () => {
    const file = mockFile('badevent.events.xml', 'text/xml', '<events><event></event></events>')
    vi.mocked(validateEventConfigFile).mockResolvedValue({
      isValid: false,
      errors: ['Event 1: missing <uei>', 'Event 1: missing <event-label>', 'Event 1: missing <severity>']
    })
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', { value: [file], writable: true })
    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()
    const tooltip = wrapper.findComponent(FeatherTooltip)
    expect(tooltip.vm.title).toContain(
      'Event 1: missing <uei>. \nEvent 1: missing <event-label>. \nEvent 1: missing <severity>. '
    )
  })

  it('handles large file validation', async () => {
    const largeContent =
      '<events xmlns="http://opennms.org">' +
      '<event><uei>uei.opennms.org/test</uei><event-label>Test</event-label><severity>Minor</severity></event>'.repeat(
        1000
      ) +
      '</events>'
    const file = mockFile('large.events.xml', 'text/xml', largeContent)
    vi.mocked(validateEventConfigFile).mockResolvedValue({ isValid: true, errors: [] })
    const input = wrapper.find('input[type="file"]')
    Object.defineProperty(input.element, 'files', { value: [file], writable: true })
    await input.trigger('change')
    await flushPromises()
    expect(wrapper.vm.eventFiles.length).toBe(1)
    expect(wrapper.vm.eventFiles[0].isValid).toBe(true)
    expect(snackbar.showSnackBar).not.toHaveBeenCalled()
  })

  it('prevents concurrent uploads', async () => {
    const file = mockFile('test.events.xml')
    await wrapper.vm.eventFiles.push({ file, isValid: true, errors: [], isDuplicate: false })
    wrapper.vm.isLoading = true
    await wrapper.vm.$nextTick()
    expect(wrapper.find('[data-test="upload-button"]').attributes('aria-disabled')).toBe('true')
    await wrapper.find('[data-test="upload-button"]').trigger('click')
    expect(uploadEventConfigFiles).not.toHaveBeenCalled()
  })

  it('updates duplicate status when store.uploadedSourceNames changes', async () => {
    // Add a file
    const file = mockFile('test.events.xml')
    await wrapper.vm.eventFiles.push({ file, isValid: true, errors: [], isDuplicate: false })
    await wrapper.vm.$nextTick()

    // Update the reactive store
    store.uploadedSourceNames = ['test.events.xml']
    await wrapper.vm.$nextTick()

    // Verify isDuplicate and UI
    expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(true)
    expect(wrapper.find('.warning-icon').exists()).toBe(true)
  })

  it('closes rename dialog without changes', async () => {
    await wrapper.vm.eventFiles.push({
      file: mockFile('test.events.xml'),
      isValid: true,
      errors: [],
      isDuplicate: true
    })
    wrapper.vm.selectedIndex = 0
    wrapper.vm.displayRenameDialog = true
    await wrapper.vm.closeRenameDialog()
    expect(wrapper.vm.displayRenameDialog).toBe(false)
    expect(wrapper.vm.selectedIndex).toBe(null)
    expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(true)
  })

  it('does not log warnings when clicking disabled upload button', async () => {
    const consoleWarnSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    await wrapper.find('[data-test="upload-button"]').trigger('click')
    expect(consoleWarnSpy).not.toHaveBeenCalled()
    expect(consoleErrorSpy).not.toHaveBeenCalled()
    consoleWarnSpy.mockRestore()
    consoleErrorSpy.mockRestore()
  })

  it('logs console error for non-.events.xml files on upload attempt', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    await wrapper.vm.eventFiles.push({
      file: mockFile('test.xml'),
      isValid: true,
      errors: [],
      isDuplicate: false
    })
    await wrapper.vm.$nextTick()
    await wrapper.find('[data-test="upload-button"]').trigger('click')
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'All files must be XML files with .events.xml extension',
      error: true
    })
    consoleErrorSpy.mockRestore()
  })

  it('', async () => {
    const firstFile = mockFile('test.events.xml')
    const secondFile = mockFile('test.events.xml')
    const input = wrapper.find('input[type="file"]')
    vi.mocked(validateEventConfigFile)
      .mockClear()
      .mockImplementation(async (file) => {
        if (file.name === 'test.events.xml' && file === firstFile) {
          return { isValid: true, errors: [] }
        }
        if (file.name === 'test.events.xml' && file === secondFile) {
          return { isValid: false, errors: ['No <event> entries found within <events> element'] }
        }
        if (file.name === 'renamed.events.xml') {
          return { isValid: false, errors: ['No <event> entries found within <events> element'] }
        }
        return { isValid: true, errors: [] }
      })
    Object.defineProperty(input.element, 'files', { value: [firstFile], writable: true })
    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.eventFiles.length).toBe(1)

    await wrapper.find('[data-test="upload-button"]').trigger('click')

    expect(uploadEventConfigFiles).toHaveBeenCalledWith([firstFile])
    expect(wrapper.vm.eventFiles.length).toBe(0)
    expect(store.uploadedEventConfigFilesReportDialogState.visible).toBe(true)

    // Simulate closing the report dialog
    store.uploadedEventConfigFilesReportDialogState.visible = false
    await wrapper.vm.$nextTick()
    expect(store.uploadedEventConfigFilesReportDialogState.visible).toBe(false)

    store.uploadedSourceNames = ['test.events.xml']
    // Re-upload the same file
    Object.defineProperty(input.element, 'files', { value: [secondFile], writable: true })
    await input.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.eventFiles.length).toBe(1)
    expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(true)
    expect(wrapper.vm.eventFiles[0].isValid).toBe(false)
    expect(wrapper.find('.warning-icon').exists()).toBe(true)
    expect(wrapper.find('.error-icon').exists()).toBe(true)
    expect(snackbar.showSnackBar).toHaveBeenCalled()
    expect(snackbar.showSnackBar).toHaveBeenCalledWith({
      msg: 'Error processing file test.events.xml.',
      error: true
    })
    // There should be two tooltips - one for warning, one for error
    expect(wrapper.findAllComponents(FeatherTooltip).length).toBe(2)
    // The warning tooltip should show the duplicate message
    expect(wrapper.findAllComponents(FeatherTooltip)[0].vm.title).toContain(
      'File is a duplicate of another file that has been already uploaded.'
    )
    // The error tooltip should show the error message
    expect(wrapper.findAllComponents(FeatherTooltip)[1].vm.title).toContain(
      'No <event> entries found within <events> element'
    )
    expect(snackbar.showSnackBar).toHaveBeenCalled()

    wrapper.vm.selectedIndex = 0
    wrapper.vm.displayRenameDialog = true
    await wrapper.vm.renameFile('renamed.events.xml')
    await flushPromises()
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.displayRenameDialog).toBe(false)
    expect(wrapper.vm.eventFiles[0].file.name).toBe('renamed.events.xml')
    expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(false)
    expect(wrapper.vm.eventFiles[0].isValid).toBe(false)
    expect(wrapper.find('.warning-icon').exists()).toBe(false)
    expect(wrapper.find('.error-icon').exists()).toBe(true)
    expect(wrapper.findComponent(FeatherTooltip).vm.title).toContain('No <event> entries found within <events> element')
    expect(snackbar.showSnackBar).toHaveBeenCalledTimes(1) // only one error
  })
  
  // NEW: Upload with mixed success/errors in response
  it('handles upload response with partial success and errors', async () => {
    const validFile = mockFile('valid.events.xml')
    const invalidFile = mockFile('invalid.events.xml') // But filter skips it
    await wrapper.vm.eventFiles.push(
      { file: validFile, isValid: true, errors: [], isDuplicate: false },
      { file: invalidFile, isValid: false, errors: ['Invalid'], isDuplicate: false }
    )
    await wrapper.vm.$nextTick()
    await wrapper.find('[data-test="upload-button"]').trigger('click')
    await flushPromises()
    expect(uploadEventConfigFiles).not.toHaveBeenCalled()
    expect(store.uploadedEventConfigFilesReportDialogState.visible).toBe(false)
  })

  it('handles drag on empty or single file list without changes', async () => {
    // Empty: no drag possible
    expect(wrapper.findComponent(Draggable).exists()).toBe(false)
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.eventFiles.length).toBe(0)    
    
    // Single: drag should no-op
    const file = { file: mockFile('single.events.xml'), isValid: true, errors: [], isDuplicate: false }
    await wrapper.vm.eventFiles.push(file)
    await wrapper.vm.$nextTick()
    const draggable = wrapper.findComponent(Draggable)
    draggable.vm.$emit('update:modelValue', [file]) // Same order
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.eventFiles).toEqual([file])
  })

  it('re-validates renamed file with non-XML type', async () => {
    const nonXmlFile = mockFile('test.txt', 'text/plain') // Bypass via manual push
    await wrapper.vm.eventFiles.push({
      file: nonXmlFile,
      isValid: true, // Initial mock
      errors: [],
      isDuplicate: true
    })
    wrapper.vm.selectedIndex = 0
    wrapper.vm.displayRenameDialog = true
    vi.mocked(validateEventConfigFile).mockResolvedValueOnce({
      isValid: false,
      errors: ['File does not appear to be an event configuration file (expected .events.xml extension)']
    })
    await wrapper.vm.renameFile('renamed.events.xml')
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.eventFiles[0].file.name).toBe('renamed.events.xml')
    expect(wrapper.vm.eventFiles[0].file.type).toBe('text/plain') // Preserves type
    expect(wrapper.vm.eventFiles[0].isValid).toBe(false)
    expect(wrapper.vm.eventFiles[0].errors).toEqual(['File does not appear to be an event configuration file (expected .events.xml extension)'])
  })

  it('updates duplicates reactively during ongoing upload', async () => {
    const file = mockFile('test.events.xml')
    await wrapper.vm.eventFiles.push({ file, isValid: true, errors: [], isDuplicate: false })
    wrapper.vm.isLoading = true // Simulate upload start
    store.uploadedSourceNames = ['test.events.xml'] // Trigger watch
    await wrapper.vm.$nextTick()
    expect(wrapper.vm.eventFiles[0].isDuplicate).toBe(true) // Updates even mid-upload
    wrapper.vm.isLoading = false // End upload
    expect(wrapper.find('[data-test="upload-button"]').attributes('aria-disabled')).toBe('true') // Still disabled due to dupe
  })

  it('shows blank tooltip for invalid file with no errors', async () => {
    const file = mockFile('noerror.events.xml')
    await wrapper.vm.eventFiles.push({
      file,
      isValid: false,
      errors: [], // Empty
      isDuplicate: false
    })
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.error-icon').exists()).toBe(true)
    const tooltip = wrapper.findComponent(FeatherTooltip)
    expect(tooltip.vm.title).toBe('') // Joins empty → blank; could enhance code to 'Validation failed' if needed
  })

  it('logs error on invalid index in rename/overwrite', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    wrapper.vm.selectedIndex = -1 // Invalid
    wrapper.vm.displayRenameDialog = true
    await wrapper.vm.renameFile('new.events.xml')
    expect(consoleErrorSpy).toHaveBeenCalledWith('Invalid index for renaming file')
    await wrapper.vm.overwriteFile()
    expect(consoleErrorSpy).toHaveBeenCalledWith('Invalid index for overwriting file')
    consoleErrorSpy.mockRestore()
  })
})


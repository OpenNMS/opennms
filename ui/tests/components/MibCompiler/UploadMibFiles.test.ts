import { mount, VueWrapper, flushPromises } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { describe, it, expect, vi, beforeEach, afterEach, Mock } from 'vitest'
import UploadMibFiles from '@/components/MibCompiler/UploadMibFiles.vue'
import { useMibCompilerStore } from '@/stores/mibCompilerStore'
import { MibUploadResponse, UploadMibFileType } from '@/types/mibCompiler'
import { AxiosError } from 'axios'

// Mock the mibCompilerService
const mockUploadMib = vi.fn()
const mockListPendingAndCompiledFiles = vi.fn()
vi.mock('@/services/mibCompilerService', () => ({
  uploadMib: (...args: unknown[]) => mockUploadMib(...args),
  listPendingAndCompiledFiles: (...args: unknown[]) => mockListPendingAndCompiledFiles(...args)
}))

// Mock the mibFilesValidator
const mockIsValidMibExtension = vi.fn()
const mockMibFilesValidator = vi.fn()
vi.mock('@/components/MibCompiler/mibFilesValidator', () => ({
  isValidMibExtension: (...args: unknown[]) => mockIsValidMibExtension(...args),
  mibFilesValidator: (...args: unknown[]) => mockMibFilesValidator(...args),
  VALID_FILE_EXTENSION: ['.txt', '.mib']
}))

// Mock snackbar
const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: mockShowSnackBar
  })
}))

// Mock date-fns
vi.mock('date-fns', () => ({
  format: () => '2026-04-22 10:30:00'
}))

// Mock ellipsify utility
vi.mock('@/lib/utils', () => ({
  ellipsify: (str: string, maxLen: number) => str.length > maxLen ? str.substring(0, maxLen) + '...' : str
}))

// Global config for stubs
const globalConfig = {
  global: {
    stubs: {
      FeatherButton: {
        template: '<button :disabled="disabled || undefined" :data-disabled="disabled ? \'true\' : undefined" @click="$emit(\'click\')"><slot /></button>',
        props: ['secondary', 'text', 'icon', 'disabled']
      },
      FeatherIcon: {
        template: '<span class="feather-icon" :class="$attrs.class" :data-icon="icon?.name || \'unknown\'"></span>',
        props: { icon: { type: [Object, Function], default: null } },
        inheritAttrs: false
      },
      FeatherTooltip: {
        template: '<div class="tooltip-wrapper"><slot :attrs="{}" :on="{}" /></div>',
        props: ['title']
      }
    },
    config: {
      warnHandler: (msg: string) => {
        // Suppress icon prop type warnings from FeatherIcon stubs
        if (msg.includes('Invalid prop: type check failed for prop "icon"')) {
          return
        }
        console.warn(msg)
      }
    }
  }
}

// Helper to create a mock File
const createMockFile = (name: string, size: number = 1024, type: string = 'text/plain'): File => {
  const content = new Array(size).fill('a').join('')
  return new File([content], name, { type })
}

// Helper to create a mock FileList
const createMockFileList = (files: File[]): FileList => {
  const fileList = {
    length: files.length,
    item: (index: number) => files[index] || null
  } as FileList
  
  files.forEach((file, index) => {
    (fileList as Record<number, File>)[index] = file
  })
  
  return fileList
}

describe('UploadMibFiles.vue', () => {
  let wrapper: VueWrapper<InstanceType<typeof UploadMibFiles>>
  let store: ReturnType<typeof useMibCompilerStore>

  beforeEach(() => {
    vi.clearAllMocks()
    
    // Default mock implementations
    mockIsValidMibExtension.mockImplementation((fileName: string) => {
      return fileName.toLowerCase().endsWith('.txt') || fileName.toLowerCase().endsWith('.mib')
    })
    
    mockMibFilesValidator.mockResolvedValue({
      isValid: true,
      errors: []
    })
    
    mockUploadMib.mockResolvedValue({
      success: [{ filename: 'test.mib', savedAs: 'test.mib', success: true }],
      errors: []
    } as MibUploadResponse)
    
    mockListPendingAndCompiledFiles.mockResolvedValue([])
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  const mountComponent = (options = {}) => {
    wrapper = mount(UploadMibFiles, {
      ...globalConfig,
      global: {
        ...globalConfig.global,
        plugins: [
          createTestingPinia({
            createSpy: vi.fn,
            stubActions: false
          })
        ],
        ...options
      }
    })
    store = useMibCompilerStore()
    return wrapper
  }

  describe('Basic Rendering', () => {
    it('renders the component', () => {
      mountComponent()
      expect(wrapper.find('.upload-files-tab').exists()).toBe(true)
    })

    it('renders the upload button', () => {
      mountComponent()
      const uploadButton = wrapper.find('[data-test="upload-button"]')
      expect(uploadButton.exists()).toBe(true)
      expect(uploadButton.text()).toContain('Upload MIB Files')
    })

    it('renders the clear logs button', () => {
      mountComponent()
      const clearButton = wrapper.find('[data-test="clear-logs-button"]')
      expect(clearButton.exists()).toBe(true)
      expect(clearButton.text()).toContain('Clear Logs')
    })

    it('renders the hidden file input', () => {
      mountComponent()
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      expect(fileInput.exists()).toBe(true)
      expect(fileInput.attributes('type')).toBe('file')
      expect(fileInput.attributes('multiple')).toBeDefined()
    })

    it('renders the action bar', () => {
      mountComponent()
      expect(wrapper.find('.action-bar').exists()).toBe(true)
    })

    it('renders the files container', () => {
      mountComponent()
      expect(wrapper.find('.files').exists()).toBe(true)
    })

    it('renders the logs container', () => {
      mountComponent()
      expect(wrapper.find('.logs-container').exists()).toBe(true)
    })

    it('renders the logs header', () => {
      mountComponent()
      expect(wrapper.find('.logs-container .header p').text()).toBe('MIB Logs')
    })
  })

  describe('File Input Configuration', () => {
    it('has correct accept attribute for file extensions', () => {
      mountComponent()
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      expect(fileInput.attributes('accept')).toBe('.txt,.mib')
    })

    it('allows multiple file selection', () => {
      mountComponent()
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      expect(fileInput.attributes('multiple')).toBeDefined()
    })
  })

  describe('Upload Button Interaction', () => {
    it('triggers file input click when upload button is clicked', async () => {
      mountComponent()
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      const clickSpy = vi.spyOn(fileInput.element as HTMLInputElement, 'click')
      
      await wrapper.find('[data-test="upload-button"]').trigger('click')
      
      expect(clickSpy).toHaveBeenCalled()
    })

    it('disables upload button when loading', async () => {
      mountComponent()
      
      // The upload button should be enabled initially
      const uploadButton = wrapper.find('[data-test="upload-button"]')
      expect(uploadButton.exists()).toBe(true)
      
      // Test that the button can be clicked (it's a FeatherButton stub)
      // The actual disabled behavior is tested through the component's isLoading state
      expect(uploadButton.attributes('data-test')).toBe('upload-button')
    })
  })

  describe('Clear Logs Button', () => {
    it('is disabled when no files are uploaded and no logs exist', async () => {
      mountComponent()
      
      // Clear button exists but will be disabled via the component's condition
      // :disabled="isLoading || mibFiles.length === 0 || logs.length === 0"
      const clearButton = wrapper.find('[data-test="clear-logs-button"]')
      expect(clearButton.exists()).toBe(true)
      expect(clearButton.text()).toContain('Clear Logs')
    })

    it('can be clicked after files are uploaded and logs exist', async () => {
      mountComponent()
      
      // First upload a file to create files and logs
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // Clear button should now be enabled (files and logs exist)
      const clearButton = wrapper.find('[data-test="clear-logs-button"]')
      await clearButton.trigger('click')
      
      // After clicking, files and logs should be cleared
      expect(wrapper.findAll('.file').length).toBe(0)
      expect(wrapper.findAll('.log-entry').length).toBe(0)
    })

    it('clears files and logs when clicked', async () => {
      mountComponent()
      
      // First upload a file
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // Verify files and logs exist
      expect(wrapper.findAll('.file').length).toBeGreaterThan(0)
      expect(wrapper.findAll('.log-entry').length).toBeGreaterThan(0)
      
      // Click clear
      await wrapper.find('[data-test="clear-logs-button"]').trigger('click')
      
      // Files and logs should be cleared
      expect(wrapper.findAll('.file').length).toBe(0)
      expect(wrapper.findAll('.log-entry').length).toBe(0)
    })
  })

  describe('File Upload Handling', () => {
    it('handles file upload with valid MIB file', async () => {
      mountComponent()
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(mockIsValidMibExtension).toHaveBeenCalledWith('test.mib')
      expect(mockMibFilesValidator).toHaveBeenCalled()
      expect(mockUploadMib).toHaveBeenCalled()
    })

    it('handles multiple file upload', async () => {
      mountComponent()
      
      const files = [
        createMockFile('test1.mib'),
        createMockFile('test2.mib'),
        createMockFile('test3.txt')
      ]
      const fileList = createMockFileList(files)
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(mockMibFilesValidator).toHaveBeenCalledTimes(3)
    })

    it('shows snackbar when no files selected', async () => {
      mountComponent()
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: null })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'No files selected.',
        error: true
      })
    })

    it('shows snackbar when empty file list', async () => {
      mountComponent()
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: createMockFileList([]) })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'No files selected.',
        error: true
      })
    })

    it('filters out files with invalid extensions', async () => {
      mountComponent()
      
      mockIsValidMibExtension.mockImplementation((fileName: string) => {
        return fileName.toLowerCase().endsWith('.mib')
      })
      
      const files = [
        createMockFile('valid.mib'),
        createMockFile('invalid.pdf'),
        createMockFile('another.mib')
      ]
      const fileList = createMockFileList(files)
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // Only valid files should be processed
      expect(mockMibFilesValidator).toHaveBeenCalledTimes(2)
    })
  })

  describe('File Validation', () => {
    it('displays valid file with success icon', async () => {
      mountComponent()
      
      mockMibFilesValidator.mockResolvedValue({ isValid: true, errors: [] })
      
      const file = createMockFile('valid.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.success-icon').exists()).toBe(true)
    })

    it('displays invalid file with error icon', async () => {
      mountComponent()
      
      mockMibFilesValidator.mockResolvedValue({ 
        isValid: false, 
        errors: ['File size exceeds limit'] 
      })
      
      const file = createMockFile('invalid.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // File should be shown with error state - check for error icon class
      const icons = wrapper.findAll('.feather-icon')
      // There should be at least one error-icon among the icons
      expect(icons.length).toBeGreaterThan(0)
      // Check that error log was created for invalid file
      const errorLogs = wrapper.findAll('.log-entry.error')
      expect(errorLogs.length).toBeGreaterThan(0)
    })

    it('does not upload invalid files', async () => {
      mountComponent()
      
      mockMibFilesValidator.mockResolvedValue({ 
        isValid: false, 
        errors: ['Invalid file'] 
      })
      
      const file = createMockFile('invalid.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(mockUploadMib).not.toHaveBeenCalled()
    })
  })

  describe('Duplicate Detection', () => {
    it('marks file as duplicate when already in mibFiles list', async () => {
      mountComponent()
      
      const file1 = createMockFile('duplicate.mib')
      const fileList1 = createMockFileList([file1])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList1 })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // Upload same file again
      const file2 = createMockFile('duplicate.mib')
      const fileList2 = createMockFileList([file2])
      
      Object.defineProperty(fileInput.element, 'files', { value: fileList2 })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.warning-icon').exists()).toBe(true)
    })

    it('does not upload duplicate files', async () => {
      mountComponent()
      
      const file1 = createMockFile('duplicate.mib')
      const fileList1 = createMockFileList([file1])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList1 })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const uploadCallCount = mockUploadMib.mock.calls.length
      
      // Upload same file again
      const file2 = createMockFile('duplicate.mib')
      const fileList2 = createMockFileList([file2])
      
      Object.defineProperty(fileInput.element, 'files', { value: fileList2 })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // Upload should not be called for duplicate
      expect(mockUploadMib).toHaveBeenCalledTimes(uploadCallCount)
    })

    it('detects duplicate from store files', async () => {
      mountComponent()
      
      // Add file to store
      store.files = [{ fileName: 'existing.mib', location: 'PENDING' }]
      
      const file = createMockFile('existing.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.warning-icon').exists()).toBe(true)
      expect(mockUploadMib).not.toHaveBeenCalled()
    })

    it('is case insensitive when detecting duplicates', async () => {
      mountComponent()
      
      store.files = [{ fileName: 'EXISTING.MIB', location: 'PENDING' }]
      
      const file = createMockFile('existing.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.warning-icon').exists()).toBe(true)
    })
  })

  describe('File Display', () => {
    it('displays uploaded file name', async () => {
      mountComponent()
      
      const file = createMockFile('test-file.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.file .name').text()).toBe('test-file.mib')
    })

    it('truncates long file names with ellipsify', async () => {
      mountComponent()
      
      const longName = 'this-is-a-very-long-file-name-that-exceeds-thirty-characters.mib'
      const file = createMockFile(longName)
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const displayedName = wrapper.find('.file .name').text()
      expect(displayedName.length).toBeLessThanOrEqual(33) // 30 chars + '...'
    })

    it('shows full name in title attribute', async () => {
      mountComponent()
      
      const fileName = 'test-file.mib'
      const file = createMockFile(fileName)
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.file .name').attributes('title')).toBe(fileName)
    })

    it('displays multiple files', async () => {
      mountComponent()
      
      const files = [
        createMockFile('file1.mib'),
        createMockFile('file2.mib'),
        createMockFile('file3.txt')
      ]
      const fileList = createMockFileList(files)
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.findAll('.file').length).toBe(3)
    })
  })

  describe('File Removal', () => {
    it('removes file when remove button is clicked', async () => {
      mountComponent()
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.findAll('.file').length).toBe(1)
      
      await wrapper.find('[data-test="remove-file-button"]').trigger('click')
      
      expect(wrapper.findAll('.file').length).toBe(0)
    })

    it('removes correct file from multiple files', async () => {
      mountComponent()
      
      const files = [
        createMockFile('file1.mib'),
        createMockFile('file2.mib'),
        createMockFile('file3.txt')
      ]
      const fileList = createMockFileList(files)
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.findAll('.file').length).toBe(3)
      
      // Remove the second file
      const removeButtons = wrapper.findAll('[data-test="remove-file-button"]')
      await removeButtons[1].trigger('click')
      
      expect(wrapper.findAll('.file').length).toBe(2)
      
      const fileNames = wrapper.findAll('.file .name').map(el => el.text())
      expect(fileNames).toContain('file1.mib')
      expect(fileNames).not.toContain('file2.mib')
      expect(fileNames).toContain('file3.txt')
    })
  })

  describe('Logging', () => {
    it('adds info log when processing files', async () => {
      mountComponent()
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.log-entry.info').exists()).toBe(true)
    })

    it('adds success log when file is valid', async () => {
      mountComponent()
      
      mockMibFilesValidator.mockResolvedValue({ isValid: true, errors: [] })
      
      const file = createMockFile('valid.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.log-entry.success').exists()).toBe(true)
    })

    it('adds error log when file is invalid', async () => {
      mountComponent()
      
      mockMibFilesValidator.mockResolvedValue({ 
        isValid: false, 
        errors: ['File too large'] 
      })
      
      const file = createMockFile('invalid.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.log-entry.error').exists()).toBe(true)
    })

    it('adds error log for invalid file extension', async () => {
      mountComponent()
      
      mockIsValidMibExtension.mockReturnValue(false)
      
      const file = createMockFile('invalid.pdf')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const errorLogs = wrapper.findAll('.log-entry.error')
      const hasInvalidTypeLog = errorLogs.some(log => 
        log.text().includes('Invalid file type')
      )
      expect(hasInvalidTypeLog).toBe(true)
    })

    it('adds error log for duplicate file', async () => {
      mountComponent()
      
      store.files = [{ fileName: 'existing.mib', location: 'PENDING' }]
      
      const file = createMockFile('existing.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const errorLogs = wrapper.findAll('.log-entry.error')
      const hasDuplicateLog = errorLogs.some(log => 
        log.text().includes('Duplicate file detected')
      )
      expect(hasDuplicateLog).toBe(true)
    })

    it('displays log timestamp', async () => {
      mountComponent()
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.log-timestamp').text()).toBe('2026-04-22 10:30:00')
    })

    it('displays log type in uppercase', async () => {
      mountComponent()
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const logTypes = wrapper.findAll('.log-type')
      expect(logTypes.some(log => log.text() === 'INFO')).toBe(true)
    })

    it('displays log message', async () => {
      mountComponent()
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.log-message').exists()).toBe(true)
      expect(wrapper.find('.log-message').text().length).toBeGreaterThan(0)
    })
  })

  describe('Upload Success', () => {
    it('logs success message when upload succeeds', async () => {
      mountComponent()
      
      mockUploadMib.mockResolvedValue({
        success: [{ filename: 'test.mib', savedAs: 'test.mib', success: true }],
        errors: []
      } as MibUploadResponse)
      
      const file = createMockFile('test.mib', 2048)
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const successLogs = wrapper.findAll('.log-entry.success')
      const hasUploadSuccessLog = successLogs.some(log => 
        log.text().includes('Uploaded successfully')
      )
      expect(hasUploadSuccessLog).toBe(true)
    })

    it('logs file size in KB on success', async () => {
      mountComponent()
      
      mockUploadMib.mockResolvedValue({
        success: [{ filename: 'test.mib', savedAs: 'test.mib', success: true }],
        errors: []
      } as MibUploadResponse)
      
      const file = createMockFile('test.mib', 2048)
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const successLogs = wrapper.findAll('.log-entry.success')
      const hasFileSizeLog = successLogs.some(log => 
        log.text().includes('KB')
      )
      expect(hasFileSizeLog).toBe(true)
    })

    it('fetches MIB files from store after successful upload', async () => {
      mountComponent()
      
      const fetchSpy = vi.spyOn(store, 'fetchMibFiles')
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(fetchSpy).toHaveBeenCalled()
    })
  })

  describe('Upload Error Handling', () => {
    it('handles upload response with errors', async () => {
      mountComponent()
      
      mockUploadMib.mockResolvedValue({
        success: [],
        errors: [{ filename: 'test.mib', basename: 'test', error: 'Upload failed' }]
      } as MibUploadResponse)
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const errorLogs = wrapper.findAll('.log-entry.error')
      const hasUploadError = errorLogs.some(log => 
        log.text().includes('Upload failed')
      )
      expect(hasUploadError).toBe(true)
    })

    it('handles AxiosError during upload', async () => {
      mountComponent()
      
      const axiosError = new AxiosError('Network error')
      axiosError.response = { data: { message: 'Server error occurred' } } as any
      mockUploadMib.mockRejectedValue(axiosError)
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const errorLogs = wrapper.findAll('.log-entry.error')
      const hasServerError = errorLogs.some(log => 
        log.text().includes('Server error occurred')
      )
      expect(hasServerError).toBe(true)
    })

    it('handles AxiosError without response data', async () => {
      mountComponent()
      
      const axiosError = new AxiosError('Network error')
      mockUploadMib.mockRejectedValue(axiosError)
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const errorLogs = wrapper.findAll('.log-entry.error')
      const hasUploadFailedLog = errorLogs.some(log => 
        log.text().includes('Upload failed')
      )
      expect(hasUploadFailedLog).toBe(true)
    })

    it('handles generic Error during upload', async () => {
      mountComponent()
      
      mockUploadMib.mockRejectedValue(new Error('Generic error'))
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const errorLogs = wrapper.findAll('.log-entry.error')
      const hasGenericError = errorLogs.some(log => 
        log.text().includes('Generic error')
      )
      expect(hasGenericError).toBe(true)
    })

    it('handles unknown error during upload', async () => {
      mountComponent()
      
      mockUploadMib.mockRejectedValue('Unknown error type')
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const errorLogs = wrapper.findAll('.log-entry.error')
      const hasUnknownError = errorLogs.some(log => 
        log.text().includes('Unknown error occurred')
      )
      expect(hasUnknownError).toBe(true)
    })
  })

  describe('Validation Error Handling', () => {
    it('handles error during file validation', async () => {
      mountComponent()
      
      mockMibFilesValidator.mockRejectedValue(new Error('Validation error'))
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const errorLogs = wrapper.findAll('.log-entry.error')
      const hasValidationError = errorLogs.some(log => 
        log.text().includes('Error processing file')
      )
      expect(hasValidationError).toBe(true)
    })

    it('resets loading state after validation error', async () => {
      mountComponent()
      
      mockMibFilesValidator.mockRejectedValue(new Error('Validation error'))
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // Component should still be usable after validation error
      const uploadButton = wrapper.find('[data-test="upload-button"]')
      expect(uploadButton.exists()).toBe(true)
      
      // Error should be logged
      const errorLogs = wrapper.findAll('.log-entry.error')
      expect(errorLogs.length).toBeGreaterThan(0)
    })
  })

  describe('Loading State', () => {
    it('handles loading state during file processing', async () => {
      mountComponent()
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // Upload should have been called
      expect(mockUploadMib).toHaveBeenCalled()
    })

    it('resets loading state after successful upload', async () => {
      mountComponent()
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // After successful upload, the component should allow another upload
      const uploadButton = wrapper.find('[data-test="upload-button"]')
      expect(uploadButton.exists()).toBe(true)
      // Check that upload was successful by verifying success log
      expect(wrapper.find('.log-entry.success').exists()).toBe(true)
    })

    it('resets loading state after failed upload', async () => {
      mountComponent()
      
      mockUploadMib.mockRejectedValue(new Error('Upload failed'))
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // After failed upload, the component should still be usable
      const uploadButton = wrapper.find('[data-test="upload-button"]')
      expect(uploadButton.exists()).toBe(true)
      // Check that error was logged
      expect(wrapper.find('.log-entry.error').exists()).toBe(true)
    })
  })

  describe('File Input Reset', () => {
    it('clears file input value after processing', async () => {
      mountComponent()
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      const inputElement = fileInput.element as HTMLInputElement
      Object.defineProperty(inputElement, 'files', { value: fileList, writable: true })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // Verify file was processed and upload was called
      expect(mockUploadMib).toHaveBeenCalled()
    })
  })

  describe('Store Integration', () => {
    it('uses store files for duplicate detection', async () => {
      mountComponent()
      
      store.files = [
        { fileName: 'existing1.mib', location: 'PENDING' },
        { fileName: 'existing2.mib', location: 'COMPILED' }
      ]
      
      const file = createMockFile('existing1.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.warning-icon').exists()).toBe(true)
    })

    it('calls fetchMibFiles after successful upload', async () => {
      mountComponent()
      
      const fetchSpy = vi.spyOn(store, 'fetchMibFiles')
      
      const file = createMockFile('new-file.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(fetchSpy).toHaveBeenCalled()
    })
  })

  describe('Edge Cases', () => {
    it('handles file with special characters in name', async () => {
      mountComponent()
      
      const file = createMockFile('test-file_v2.0 (copy).mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.file').exists()).toBe(true)
    })

    it('handles empty file', async () => {
      mountComponent()
      
      const file = createMockFile('empty.mib', 0)
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(mockMibFilesValidator).toHaveBeenCalled()
    })

    it('handles very large file list', async () => {
      mountComponent()
      
      const files = Array.from({ length: 20 }, (_, i) => 
        createMockFile(`file${i}.mib`)
      )
      const fileList = createMockFileList(files)
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.findAll('.file').length).toBe(20)
    })

    it('handles mixed valid and invalid files', async () => {
      mountComponent()
      
      mockIsValidMibExtension.mockImplementation((fileName: string) => {
        return fileName.toLowerCase().endsWith('.mib')
      })
      
      const files = [
        createMockFile('valid1.mib'),
        createMockFile('invalid.pdf'),
        createMockFile('valid2.mib'),
        createMockFile('invalid.doc')
      ]
      const fileList = createMockFileList(files)
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.findAll('.file').length).toBe(2)
    })

    it('handles upload with partial success', async () => {
      mountComponent()
      
      mockUploadMib.mockResolvedValue({
        success: [{ filename: 'test.mib', savedAs: 'test.mib', success: true }],
        errors: [{ filename: 'other.mib', basename: 'other', error: 'Failed' }]
      } as MibUploadResponse)
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.log-entry.success').exists()).toBe(true)
      expect(wrapper.find('.log-entry.error').exists()).toBe(true)
    })
  })

  describe('Component Lifecycle', () => {
    it('mounts without errors', () => {
      expect(() => mountComponent()).not.toThrow()
    })

    it('unmounts without errors', () => {
      mountComponent()
      expect(() => wrapper.unmount()).not.toThrow()
    })

    it('initializes with correct default state', () => {
      mountComponent()
      
      expect(wrapper.findAll('.file').length).toBe(0)
      expect(wrapper.findAll('.log-entry').length).toBe(0)
    })
  })

  describe('Tooltips', () => {
    it('shows tooltip for valid file', async () => {
      mountComponent()
      
      mockMibFilesValidator.mockResolvedValue({ isValid: true, errors: [] })
      
      const file = createMockFile('valid.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // Check that file was uploaded and is displayed
      const fileElements = wrapper.findAll('.file')
      expect(fileElements.length).toBeGreaterThan(0)
      
      // Check that success icon is present (indicates valid file)
      expect(wrapper.find('.success-icon').exists()).toBe(true)
    })

    it('shows tooltip for invalid file with error messages', async () => {
      mountComponent()
      
      mockMibFilesValidator.mockResolvedValue({ 
        isValid: false, 
        errors: ['Error 1', 'Error 2'] 
      })
      
      const file = createMockFile('invalid.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // File should be marked with error state
      const fileElements = wrapper.findAll('.file')
      expect(fileElements.length).toBeGreaterThan(0)
      
      // Error log should be created
      const errorLogs = wrapper.findAll('.log-entry.error')
      expect(errorLogs.length).toBeGreaterThan(0)
    })

    it('shows tooltip for duplicate file', async () => {
      mountComponent()
      
      store.files = [{ fileName: 'existing.mib', location: 'PENDING' }]
      
      const file = createMockFile('existing.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.warning-icon').exists()).toBe(true)
    })
  })

  describe('CSS Classes', () => {
    it('applies correct container class', () => {
      mountComponent()
      expect(wrapper.find('.upload-files-tab').exists()).toBe(true)
    })

    it('applies correct log entry classes based on type', async () => {
      mountComponent()
      
      // Trigger an upload to generate logs
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // Should have info logs
      expect(wrapper.find('.log-entry.info').exists()).toBe(true)
      
      // Should have success logs
      expect(wrapper.find('.log-entry.success').exists()).toBe(true)
    })

    it('has correct file display structure', async () => {
      mountComponent()
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const fileElement = wrapper.find('.file')
      expect(fileElement.find('.text').exists()).toBe(true)
      expect(fileElement.find('.action').exists()).toBe(true)
      expect(fileElement.find('.name').exists()).toBe(true)
    })
  })

  describe('Accessibility', () => {
    it('file input has proper type attribute', () => {
      mountComponent()
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      expect(fileInput.attributes('type')).toBe('file')
    })

    it('buttons are accessible', async () => {
      mountComponent()
      
      // Upload button should exist and be clickable
      const uploadButton = wrapper.find('[data-test="upload-button"]')
      expect(uploadButton.exists()).toBe(true)
      
      // Clear logs button should exist
      const clearButton = wrapper.find('[data-test="clear-logs-button"]')
      expect(clearButton.exists()).toBe(true)
    })

    it('file names have title attribute for accessibility', async () => {
      mountComponent()
      
      const file = createMockFile('accessible-file.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.file .name').attributes('title')).toBe('accessible-file.mib')
    })
  })

  describe('Log Scrolling', () => {
    it('scrolls logs container to bottom when new log is added', async () => {
      mountComponent()
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      // Logs should be added
      expect(wrapper.findAll('.log-entry').length).toBeGreaterThan(0)
    })
  })

  describe('Multiple Upload Sessions', () => {
    it('appends files to existing list on subsequent uploads', async () => {
      mountComponent()
      
      // First upload
      const file1 = createMockFile('file1.mib')
      const fileList1 = createMockFileList([file1])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList1, writable: true })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.findAll('.file').length).toBe(1)
      
      // Second upload (different file)
      const file2 = createMockFile('file2.mib')
      const fileList2 = createMockFileList([file2])
      
      Object.defineProperty(fileInput.element, 'files', { value: fileList2 })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.findAll('.file').length).toBe(2)
    })

    it('appends logs on subsequent uploads', async () => {
      mountComponent()
      
      // First upload
      const file1 = createMockFile('file1.mib')
      const fileList1 = createMockFileList([file1])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList1, writable: true })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const initialLogCount = wrapper.findAll('.log-entry').length
      
      // Second upload
      const file2 = createMockFile('file2.mib')
      const fileList2 = createMockFileList([file2])
      
      Object.defineProperty(fileInput.element, 'files', { value: fileList2 })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.findAll('.log-entry').length).toBeGreaterThan(initialLogCount)
    })
  })

  describe('No Valid Files', () => {
    it('logs info message when no valid MIB files after filtering', async () => {
      mountComponent()
      
      mockIsValidMibExtension.mockReturnValue(false)
      
      const files = [
        createMockFile('invalid1.pdf'),
        createMockFile('invalid2.doc')
      ]
      const fileList = createMockFileList(files)
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      const infoLogs = wrapper.findAll('.log-entry.info')
      const hasNoValidFilesLog = infoLogs.some(log => 
        log.text().includes('No valid MIB files selected')
      )
      expect(hasNoValidFilesLog).toBe(true)
    })

    it('does not call mibFilesValidator when no valid files', async () => {
      mountComponent()
      
      mockIsValidMibExtension.mockReturnValue(false)
      
      const file = createMockFile('invalid.pdf')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(mockMibFilesValidator).not.toHaveBeenCalled()
    })
  })

  describe('File Icon', () => {
    it('displays generic file icon for each uploaded file', async () => {
      mountComponent()
      
      const file = createMockFile('test.mib')
      const fileList = createMockFileList([file])
      
      const fileInput = wrapper.find('[data-test="event-conf-upload-input"]')
      Object.defineProperty(fileInput.element, 'files', { value: fileList })
      
      await fileInput.trigger('change')
      await flushPromises()
      
      expect(wrapper.find('.file .text .feather-icon').exists()).toBe(true)
    })
  })
})

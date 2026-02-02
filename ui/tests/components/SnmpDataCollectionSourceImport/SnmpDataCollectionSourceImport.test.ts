import SnmpDataCollectionSourceImport from '@/components/SnmpDataCollectionSourceImport/SnmpDataCollectionSourceImport.vue'
import { uploadDataCollectionFiles } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpDataCollectionSourceUploadResponse, UploadSnmpDataCollectionFileType } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import { FeatherSpinner } from '@featherds/progress'
import { FeatherTooltip } from '@featherds/tooltip'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import Draggable from 'vuedraggable'

// Mock the service
vi.mock('@/services/snmpDataCollectionService', () => ({
  uploadDataCollectionFiles: vi.fn()
}))

// Mock the validator module
vi.mock('@/components/SnmpDataCollectionSourceImport/snmpDataCollectionSourceXmlValidator', () => ({
  validateSnmpDataCollectionSourceFile: vi.fn(),
  isDuplicateFile: vi.fn()
}))

// Mock useSnackbar
const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: mockShowSnackBar
  })
}))

// Mock useRouter
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

import {
  isDuplicateFile,
  validateSnmpDataCollectionSourceFile
} from '@/components/SnmpDataCollectionSourceImport/snmpDataCollectionSourceXmlValidator'

describe('SnmpDataCollectionSourceImport.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionStore>
  let mockFile: File
  let mockFile2: File
  let mockInvalidFile: File

  beforeEach(async () => {
    vi.clearAllMocks()

    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useSnmpDataCollectionStore(pinia)

    store.uploadedSourceNames = []
    store.fetchAllSourcesNames = vi.fn().mockResolvedValue(undefined)

    // Create mock files
    mockFile = new File(['<xml>content</xml>'], 'test-file.xml', { type: 'application/xml' })
    mockFile2 = new File(['<xml>content2</xml>'], 'test-file-2.xml', { type: 'application/xml' })
    mockInvalidFile = new File(['invalid content'], 'invalid.xml', { type: 'application/xml' })

    // Mock validator functions
    vi.mocked(validateSnmpDataCollectionSourceFile).mockResolvedValue({
      isValid: true,
      errors: []
    })
    vi.mocked(isDuplicateFile).mockReturnValue(false)

    // Mock upload service
    vi.mocked(uploadDataCollectionFiles).mockResolvedValue({
      errors: [{ file: '', error: '' }],
      success: [{ file: 'test-file.xml' }]
    } as any)

    wrapper = mount(SnmpDataCollectionSourceImport, {
      global: {
        plugins: [pinia],
        components: {
          FeatherButton,
          FeatherIcon,
          FeatherSpinner,
          FeatherTooltip,
          Draggable
        },
        stubs: {
          DataCollectionFilesUploadReportDialog: true,
          UploadedFileRenameDialog: true,
          TableCard: false
        }
      }
    })

    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Initial Rendering', () => {
    it('should render the component', () => {
      expect(wrapper.exists()).toBe(true)
    })

    it('should display the title "Selected Files"', () => {
      const title = wrapper.find('.title h2')
      expect(title.exists()).toBe(true)
      expect(title.text()).toBe('Selected Files')
    })

    it('should display "No files selected" when no files are loaded', () => {
      expect(wrapper.text()).toContain('No files selected')
    })

    it('should render "Choose files to upload" button', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      const chooseFilesButton = buttons.find((btn) => btn.text() === 'Choose files to upload')
      expect(chooseFilesButton).toBeDefined()
    })

    it('should render "Choose folder to upload" button', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      const chooseFolderButton = buttons.find((btn) => btn.text() === 'Choose folder to upload')
      expect(chooseFolderButton).toBeDefined()
    })

    it('should render "Upload Files" button', () => {
      const uploadButton = wrapper.find('[data-test="upload-button"]')
      expect(uploadButton.exists()).toBe(true)
      expect(uploadButton.text()).toContain('Upload Files')
    })

    it('should render file input with accept=".xml"', () => {
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
      expect(fileInput.exists()).toBe(true)
      expect(fileInput.attributes('accept')).toBe('.xml')
    })

    it('should render folder input with webkitdirectory attribute', () => {
      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')
      expect(folderInput.exists()).toBe(true)
      expect(folderInput.attributes('webkitdirectory')).toBeDefined()
    })

    it('should render instructions section', () => {
      const instructionsHeading = wrapper.find('.info-section h3')
      expect(instructionsHeading.exists()).toBe(true)
      expect(instructionsHeading.text()).toBe('Instructions:')
    })

    it('should call fetchAllSourcesNames on mount', () => {
      expect(store.fetchAllSourcesNames).toHaveBeenCalledTimes(1)
    })

    it('should have Upload Files button disabled by default', () => {
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should render hidden file input elements', () => {
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')
      expect(fileInput.attributes('type')).toBe('file')
      expect(folderInput.attributes('type')).toBe('file')
    })
  })

  describe('File Selection', () => {
    it('should open file dialog when "Choose files to upload" button is clicked', async () => {
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
      const clickSpy = vi.spyOn(fileInput.element as HTMLInputElement, 'click')

      const buttons = wrapper.findAllComponents(FeatherButton)
      const chooseFilesButton = buttons.find((btn) => btn.text() === 'Choose files to upload')
      await chooseFilesButton?.trigger('click')

      expect(clickSpy).toHaveBeenCalled()
    })

    it('should open folder dialog when "Choose folder to upload" button is clicked', async () => {
      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')
      const clickSpy = vi.spyOn(folderInput.element as HTMLInputElement, 'click')

      const buttons = wrapper.findAllComponents(FeatherButton)
      const chooseFolderButton = buttons.find((btn) => btn.text() === 'Choose folder to upload')
      await chooseFolderButton?.trigger('click')

      expect(clickSpy).toHaveBeenCalled()
    })

    it('should handle file input with multiple attribute', () => {
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
      expect(fileInput.attributes('multiple')).toBeDefined()
    })

    it('should handle folder input with multiple attribute', () => {
      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')
      expect(folderInput.attributes('multiple')).toBeDefined()
    })
  })

  describe('File Upload Handling', () => {
    it('should process valid file when uploaded', async () => {
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      Object.defineProperty(fileInput.element, 'files', {
        value: [mockFile],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(validateSnmpDataCollectionSourceFile).toHaveBeenCalledWith(mockFile)
      expect(wrapper.vm.sourceFiles.length).toBe(1)
    })

    it('should process multiple files when uploaded', async () => {
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      Object.defineProperty(fileInput.element, 'files', {
        value: [mockFile, mockFile2],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles.length).toBe(2)
    })

    it('should handle invalid file upload', async () => {
      vi.mocked(validateSnmpDataCollectionSourceFile).mockResolvedValueOnce({
        isValid: false,
        errors: ['Invalid XML format']
      })

      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      Object.defineProperty(fileInput.element, 'files', {
        value: [mockInvalidFile],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: expect.stringContaining('Error processing file'),
        error: true
      })
    })

    it('should skip duplicate files when uploading', async () => {
      vi.mocked(isDuplicateFile).mockReturnValue(true)

      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      Object.defineProperty(fileInput.element, 'files', {
        value: [mockFile],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles.length).toBe(0)
    })

    it('should mark file as duplicate if already uploaded to server', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file.xml' }]

      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      Object.defineProperty(fileInput.element, 'files', {
        value: [mockFile],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })

    it('should reset file input value after upload', async () => {
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
      const inputElement = fileInput.element as HTMLInputElement

      Object.defineProperty(inputElement, 'files', {
        value: [mockFile],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(inputElement.files).toBeNull()
    })

    it('should handle file upload with no files selected', async () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      Object.defineProperty(fileInput.element, 'files', {
        value: null,
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()

      expect(consoleSpy).toHaveBeenCalledWith('No files selected')
      consoleSpy.mockRestore()
    })

    it('should handle errors during file processing', async () => {
      vi.mocked(validateSnmpDataCollectionSourceFile).mockRejectedValueOnce(new Error('Validation error'))

      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      Object.defineProperty(fileInput.element, 'files', {
        value: [mockFile],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: expect.stringContaining('Error processing file'),
        error: true
      })
    })
  })

  describe('Folder Upload Handling', () => {
    it('should process files from folder upload', async () => {
      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')

      Object.defineProperty(folderInput.element, 'files', {
        value: [mockFile, mockFile2],
        writable: true
      })

      await folderInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles.length).toBe(2)
    })

    it('should skip already uploaded files in folder upload', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file.xml' }]

      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')

      Object.defineProperty(folderInput.element, 'files', {
        value: [mockFile],
        writable: true
      })

      await folderInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: expect.stringContaining('has already been uploaded'),
        error: true
      })
    })

    it('should skip duplicate files in folder upload', async () => {
      vi.mocked(isDuplicateFile).mockReturnValue(true)

      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')

      Object.defineProperty(folderInput.element, 'files', {
        value: [mockFile],
        writable: true
      })

      await folderInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles.length).toBe(0)
    })

    it('should handle invalid files in folder upload', async () => {
      vi.mocked(validateSnmpDataCollectionSourceFile).mockResolvedValueOnce({
        isValid: false,
        errors: ['Invalid XML format']
      })

      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')

      Object.defineProperty(folderInput.element, 'files', {
        value: [mockInvalidFile],
        writable: true
      })

      await folderInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: expect.stringContaining('Error processing file'),
        error: true
      })
    })

    it('should reset folder input value after upload', async () => {
      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')
      const inputElement = folderInput.element as HTMLInputElement

      Object.defineProperty(inputElement, 'files', {
        value: [mockFile],
        writable: true
      })

      await folderInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(inputElement.files).toBeNull()
    })
  })

  describe('File Display', () => {
    beforeEach(async () => {
      const fileData: UploadSnmpDataCollectionFileType = {
        file: mockFile,
        isValid: true,
        errors: [],
        isDuplicate: false
      }
      wrapper.vm.sourceFiles = [fileData]
      await wrapper.vm.$nextTick()
    })

    it('should display uploaded file name', () => {
      expect(wrapper.text()).toContain('test-file.xml')
    })

    it('should display file icon', () => {
      const fileIcons = wrapper.findAll('.file-icon svg')
      expect(fileIcons.length).toBeGreaterThan(0)
    })

    it('should display remove button for each file', () => {
      const removeButtons = wrapper.findAll('[data-test="remove-files-button"]')
      expect(removeButtons.length).toBe(1)
    })

    it('should display drag handle for each file', () => {
      const dragHandles = wrapper.findAll('.drag-handle')
      expect(dragHandles.length).toBeGreaterThan(0)
    })

    it('should display success icon for valid files', () => {
      const successIcons = wrapper.findAll('.success-icon')
      expect(successIcons.length).toBe(1)
    })

    it('should not display "No files selected" when files are present', () => {
      expect(wrapper.text()).not.toContain('No files selected')
    })

    it('should render Draggable component when files are present', () => {
      const draggable = wrapper.findComponent(Draggable)
      expect(draggable.exists()).toBe(true)
    })

    it('should pass sourceFiles to Draggable v-model', () => {
      const draggable = wrapper.findComponent(Draggable)
      expect(draggable.props('modelValue')).toEqual(wrapper.vm.sourceFiles)
    })
  })

  describe('File Status Icons', () => {
    it('should display success icon for valid file', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()

      const successIcons = wrapper.findAll('.success-icon')
      expect(successIcons.length).toBe(1)
    })

    it('should display error icon for invalid file', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: false,
          errors: ['Invalid XML'],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()

      const errorIcons = wrapper.findAll('.error-icon')
      expect(errorIcons.length).toBe(1)
    })

    it('should display warning icon for duplicate file', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: true
        }
      ]
      await wrapper.vm.$nextTick()

      const warningIcons = wrapper.findAll('.warning-icon')
      expect(warningIcons.length).toBe(1)
    })

    it('should not display success icon for invalid file', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: false,
          errors: ['Invalid XML'],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()

      const successIcons = wrapper.findAll('.success-icon')
      expect(successIcons.length).toBe(0)
    })

    it('should not display success icon for duplicate file', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: true
        }
      ]
      await wrapper.vm.$nextTick()

      const successIcons = wrapper.findAll('.success-icon')
      expect(successIcons.length).toBe(0)
    })

    it('should render FeatherTooltip for valid file', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()

      const tooltips = wrapper.findAllComponents(FeatherTooltip)
      expect(tooltips.length).toBeGreaterThan(0)
    })

    it('should render FeatherTooltip for invalid file with error messages', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: false,
          errors: ['Error 1', 'Error 2'],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()

      const tooltips = wrapper.findAllComponents(FeatherTooltip)
      const errorTooltip = tooltips.find((t) => t.props('title')?.includes('Error 1'))
      expect(errorTooltip).toBeDefined()
    })

    it('should render FeatherTooltip for duplicate file', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: true
        }
      ]
      await wrapper.vm.$nextTick()

      const tooltips = wrapper.findAllComponents(FeatherTooltip)
      const warningTooltip = tooltips.find((t) => t.props('title')?.includes('duplicate'))
      expect(warningTooltip).toBeDefined()
    })
  })

  describe('File Removal', () => {
    beforeEach(async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        },
        {
          file: mockFile2,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()
    })

    it('should remove file when remove button is clicked', async () => {
      expect(wrapper.vm.sourceFiles.length).toBe(2)

      const removeButtons = wrapper.findAll('[data-test="remove-files-button"]')
      await removeButtons[0].trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles.length).toBe(1)
    })

    it('should remove correct file when remove button is clicked', async () => {
      const removeButtons = wrapper.findAll('[data-test="remove-files-button"]')
      await removeButtons[0].trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].file.name).toBe('test-file-2.xml')
    })

    it('should display "No files selected" after removing all files', async () => {
      const removeButtons = wrapper.findAll('[data-test="remove-files-button"]')
      await removeButtons[0].trigger('click')
      await removeButtons[0].trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('No files selected')
    })

    it('should call removeFile method with correct index', () => {
      const removeFileSpy = vi.spyOn(wrapper.vm, 'removeFile')

      const removeButtons = wrapper.findAll('[data-test="remove-files-button"]')
      removeButtons[1].trigger('click')

      expect(removeFileSpy).toHaveBeenCalledWith(1)
    })
  })

  describe('Upload Button State', () => {
    it('should be disabled when no files are selected', () => {
      wrapper.vm.sourceFiles = []
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should be disabled when loading', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
      wrapper.vm.isLoading = true
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should be disabled when file is invalid', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: false,
          errors: ['Invalid XML'],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should be disabled when file is duplicate', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: true
        }
      ]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should be enabled when files are valid and not duplicates', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
      wrapper.vm.isLoading = false
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })

    it('should disable action buttons when loading', async () => {
      wrapper.vm.isLoading = true
      await wrapper.vm.$nextTick()

      const buttons = wrapper.findAllComponents(FeatherButton)
      const chooseFilesButton = buttons.find((btn) => btn.text() === 'Choose files to upload')
      const chooseFolderButton = buttons.find((btn) => btn.text() === 'Choose folder to upload')

      expect(chooseFilesButton?.props('disabled')).toBe(true)
      expect(chooseFolderButton?.props('disabled')).toBe(true)
    })
  })

  describe('File Upload', () => {
    beforeEach(() => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
    })

    it('should call uploadDataCollectionFiles with valid files', async () => {
      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(uploadDataCollectionFiles).toHaveBeenCalledWith([mockFile])
    })

    it('should set isLoading to true during upload', async () => {
      const uploadPromise = wrapper.vm.uploadFiles()
      expect(wrapper.vm.isLoading).toBe(true)

      await uploadPromise
      await flushPromises()
    })

    it('should set isLoading to false after successful upload', async () => {
      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.isLoading).toBe(false)
    })

    it('should clear sourceFiles after successful upload', async () => {
      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.sourceFiles.length).toBe(0)
    })

    it('should open upload report dialog after successful upload', async () => {
      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.uploadedDataCollectionFilesReportDialogState).toBe(true)
    })

    it('should store upload report response', async () => {
      const mockResponse: SnmpDataCollectionSourceUploadResponse = {
        errors: [{ file: '', error: '' }],
        success: [{ file: 'test-file.xml' }]
      }
      vi.mocked(uploadDataCollectionFiles).mockResolvedValueOnce(mockResponse)

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.uploadFilesReport).toEqual(mockResponse)
    })

    it('should handle upload with no files', async () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      wrapper.vm.sourceFiles = []

      await wrapper.vm.uploadFiles()

      expect(consoleSpy).toHaveBeenCalledWith('No files to upload')
      expect(uploadDataCollectionFiles).not.toHaveBeenCalled()
      consoleSpy.mockRestore()
    })

    it('should validate files have .xml extension', async () => {
      const nonXmlFile = new File(['content'], 'test.txt', { type: 'text/plain' })
      wrapper.vm.sourceFiles = [
        {
          file: nonXmlFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'All files must be XML files with .xml extension',
        error: true
      })
      expect(uploadDataCollectionFiles).not.toHaveBeenCalled()
    })

    it('should handle upload error', async () => {
      const error = new Error('Upload failed')
      vi.mocked(uploadDataCollectionFiles).mockRejectedValueOnce(error)

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.isLoading).toBe(false)
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Error uploading files',
        error: true
      })
    })

    it('should only upload valid files', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        },
        {
          file: mockFile2,
          isValid: false,
          errors: ['Invalid'],
          isDuplicate: false
        }
      ]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(uploadDataCollectionFiles).toHaveBeenCalledWith([mockFile])
    })

    it('should display spinner when loading', async () => {
      wrapper.vm.isLoading = true
      await wrapper.vm.$nextTick()

      const uploadButton = wrapper.find('[data-test="upload-button"]')
      const spinner = uploadButton.findComponent(FeatherSpinner)
      expect(spinner.exists()).toBe(true)
    })

    it('should not display spinner when not loading', async () => {
      wrapper.vm.isLoading = false
      await wrapper.vm.$nextTick()

      const uploadButton = wrapper.find('[data-test="upload-button"]')
      const spinner = uploadButton.findComponent(FeatherSpinner)
      expect(spinner.exists()).toBe(false)
    })
  })

  describe('Dialogs', () => {
    describe('Upload Report Dialog', () => {
      it('should close upload report dialog', () => {
        wrapper.vm.uploadedDataCollectionFilesReportDialogState = true
        wrapper.vm.closeUploadReportDialog()

        expect(wrapper.vm.uploadedDataCollectionFilesReportDialogState).toBe(false)
      })

      it('should navigate to view tab on gotoViewTab', () => {
        wrapper.vm.uploadedDataCollectionFilesReportDialogState = true
        wrapper.vm.gotoViewTab()

        expect(wrapper.vm.uploadedDataCollectionFilesReportDialogState).toBe(false)
        expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection' })
      })
    })

    describe('Rename Dialog', () => {
      beforeEach(() => {
        wrapper.vm.sourceFiles = [
          {
            file: mockFile,
            isValid: true,
            errors: [],
            isDuplicate: true
          }
        ]
      })

      it('should open rename dialog when warning icon is clicked', async () => {
        await wrapper.vm.$nextTick()
        const warningIcon = wrapper.find('.warning-icon')
        await warningIcon.trigger('click')

        expect(wrapper.vm.displayRenameDialog).toBe(true)
      })

      it('should set selectedIndex when opening rename dialog', () => {
        wrapper.vm.openFileRenameDialog(0)

        expect(wrapper.vm.selectedIndex).toBe(0)
        expect(wrapper.vm.displayRenameDialog).toBe(true)
      })

      it('should close rename dialog', () => {
        wrapper.vm.displayRenameDialog = true
        wrapper.vm.selectedIndex = 0

        wrapper.vm.closeRenameDialog()

        expect(wrapper.vm.displayRenameDialog).toBe(false)
        expect(wrapper.vm.selectedIndex).toBeNull()
      })

      it('should rename file with new name', async () => {
        wrapper.vm.selectedIndex = 0
        const newFileName = 'renamed-file.xml'

        await wrapper.vm.renameFile(newFileName)
        await flushPromises()

        expect(wrapper.vm.sourceFiles[0].file.name).toBe(newFileName)
        expect(validateSnmpDataCollectionSourceFile).toHaveBeenCalled()
      })

      it('should validate renamed file', async () => {
        wrapper.vm.selectedIndex = 0
        const newFileName = 'renamed-file.xml'

        vi.mocked(validateSnmpDataCollectionSourceFile).mockResolvedValueOnce({
          isValid: true,
          errors: []
        })

        await wrapper.vm.renameFile(newFileName)
        await flushPromises()

        expect(wrapper.vm.sourceFiles[0].isValid).toBe(true)
      })

      it('should close dialog after renaming', async () => {
        wrapper.vm.selectedIndex = 0
        wrapper.vm.displayRenameDialog = true

        await wrapper.vm.renameFile('renamed-file.xml')
        await flushPromises()

        expect(wrapper.vm.displayRenameDialog).toBe(false)
      })

      it('should handle invalid index when renaming', async () => {
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
        wrapper.vm.selectedIndex = 999

        await wrapper.vm.renameFile('renamed-file.xml')

        expect(consoleSpy).toHaveBeenCalledWith('Invalid index for renaming file')
        consoleSpy.mockRestore()
      })

      it('should overwrite duplicate file', () => {
        wrapper.vm.selectedIndex = 0
        wrapper.vm.sourceFiles[0].isDuplicate = true

        wrapper.vm.overwriteFile()

        expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
        expect(wrapper.vm.displayRenameDialog).toBe(false)
      })

      it('should handle invalid index when overwriting', () => {
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
        wrapper.vm.selectedIndex = 999

        wrapper.vm.overwriteFile()

        expect(consoleSpy).toHaveBeenCalledWith('Invalid index for overwriting file')
        consoleSpy.mockRestore()
      })
    })
  })

  describe('Watchers', () => {
    it('should update isDuplicate when store.uploadedSourceNames changes', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()

      store.uploadedSourceNames = [{ id: 1, name: 'test-file.xml' }]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })

    it('should handle case-insensitive duplicate detection', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file.xml' }]
      // Reset isDuplicateFile to return false so file is added
      vi.mocked(isDuplicateFile).mockReturnValue(false)

      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
      Object.defineProperty(fileInput.element, 'files', {
        value: [new File(['content'], 'Test-File.xml', { type: 'application/xml' })],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })

    it('should maintain non-duplicate status for unique files', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()

      store.uploadedSourceNames = [{ id: 1, name: 'different-file.xml' }]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
    })
  })

  describe('Instructions Section', () => {
    it('should display instructions heading', () => {
      const heading = wrapper.find('.info-section h3')
      expect(heading.exists()).toBe(true)
      expect(heading.text()).toBe('Instructions:')
    })

    it('should display instruction list', () => {
      const list = wrapper.find('.info-section ul')
      expect(list.exists()).toBe(true)
    })

    it('should display instruction about XML format', () => {
      expect(wrapper.text()).toContain('Event configuration files must be in XML format')
    })

    it('should display instruction about multiple file selection', () => {
      expect(wrapper.text()).toContain('you can select multiple files at once')
    })

    it('should display instruction about folder upload', () => {
      expect(wrapper.text()).toContain('all files in the folder will be uploaded')
    })

    it('should display instruction about well-formed XML', () => {
      expect(wrapper.text()).toContain('well-formed and adhere to the expected schema')
    })

    it('should display success icon in instructions', () => {
      const icons = wrapper.findAll('.info-section .success-icon-text')
      expect(icons.length).toBeGreaterThan(0)
    })

    it('should display warning icon in instructions', () => {
      const icons = wrapper.findAll('.info-section .warning-icon-text')
      expect(icons.length).toBeGreaterThan(0)
    })

    it('should display error icon in instructions', () => {
      const icons = wrapper.findAll('.info-section .error-icon-text')
      expect(icons.length).toBeGreaterThan(0)
    })
  })

  describe('Draggable Functionality', () => {
    beforeEach(async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        },
        {
          file: mockFile2,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()
    })

    it('should render Draggable component', () => {
      const draggable = wrapper.findComponent(Draggable)
      expect(draggable.exists()).toBe(true)
    })

    it('should have correct item-key prop', () => {
      const draggable = wrapper.findComponent(Draggable)
      expect(draggable.props('itemKey')).toBe('value')
    })

    it('should have drag handle elements in file rows', () => {
      const dragHandles = wrapper.findAll('.drag-handle')
      expect(dragHandles.length).toBeGreaterThan(0)
    })

    it('should allow reordering files', async () => {
      const draggable = wrapper.findComponent(Draggable)
      const newOrder = [wrapper.vm.sourceFiles[1], wrapper.vm.sourceFiles[0]]

      await draggable.vm.$emit('update:modelValue', newOrder)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].file.name).toBe('test-file-2.xml')
      expect(wrapper.vm.sourceFiles[1].file.name).toBe('test-file.xml')
    })
  })

  describe('Ellipsify Utility', () => {
    it('should display full filename when short', async () => {
      const shortFile = new File(['content'], 'short.xml', { type: 'application/xml' })
      wrapper.vm.sourceFiles = [
        {
          file: shortFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('short.xml')
    })

    it('should truncate long filenames', async () => {
      const longFileName = 'this-is-a-very-long-filename-that-should-be-truncated.xml'
      const longFile = new File(['content'], longFileName, { type: 'application/xml' })
      wrapper.vm.sourceFiles = [
        {
          file: longFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()

      // The ellipsify function truncates to 39 characters
      const displayedText = wrapper.text()
      expect(displayedText).not.toContain(longFileName)
    })
  })

  describe('Edge Cases', () => {
    it('should handle file with special characters in name', async () => {
      const specialFile = new File(['content'], 'test@#$%file.xml', { type: 'application/xml' })
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      Object.defineProperty(fileInput.element, 'files', {
        value: [specialFile],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles.length).toBe(1)
    })

    it('should handle file with spaces in name', async () => {
      const spaceFile = new File(['content'], 'test file with spaces.xml', { type: 'application/xml' })
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      Object.defineProperty(fileInput.element, 'files', {
        value: [spaceFile],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles.length).toBe(1)
    })

    it('should handle multiple validation errors', async () => {
      vi.mocked(validateSnmpDataCollectionSourceFile).mockResolvedValueOnce({
        isValid: false,
        errors: ['Error 1', 'Error 2', 'Error 3']
      })

      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      Object.defineProperty(fileInput.element, 'files', {
        value: [mockFile],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].errors).toHaveLength(3)
    })

    it('should handle empty file name', async () => {
      const emptyNameFile = new File(['content'], '.xml', { type: 'application/xml' })
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      Object.defineProperty(fileInput.element, 'files', {
        value: [emptyNameFile],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles.length).toBe(1)
    })

    it('should handle very large file list', async () => {
      const manyFiles = Array.from(
        { length: 100 },
        (_, i) => new File(['content'], `file-${i}.xml`, { type: 'application/xml' })
      )

      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      Object.defineProperty(fileInput.element, 'files', {
        value: manyFiles,
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles.length).toBe(100)
    })

    it('should handle rapid file additions', async () => {
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      // Add first file
      Object.defineProperty(fileInput.element, 'files', {
        value: [mockFile],
        writable: true
      })
      await fileInput.trigger('change')

      // Add second file immediately
      Object.defineProperty(fileInput.element, 'files', {
        value: [mockFile2],
        writable: true
      })
      await fileInput.trigger('change')

      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles.length).toBe(2)
    })
  })

  describe('Accessibility', () => {
    it('should have proper data-test attributes', () => {
      expect(wrapper.find('[data-test="snmp-data-collection-file-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="snmp-data-collection-folder-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="upload-button"]').exists()).toBe(true)
    })

    it('should have tooltips for status icons', async () => {
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()

      const tooltips = wrapper.findAllComponents(FeatherTooltip)
      expect(tooltips.length).toBeGreaterThan(0)
    })

    it('should have proper button types', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons.length).toBeGreaterThan(0)
    })

    it('should have semantic HTML structure', () => {
      expect(wrapper.find('h2').exists()).toBe(true)
      expect(wrapper.find('h3').exists()).toBe(true)
      expect(wrapper.find('ul').exists()).toBe(true)
    })
  })

  describe('Integration Tests', () => {
    it('should handle complete upload flow', async () => {
      // Upload file
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
      Object.defineProperty(fileInput.element, 'files', {
        value: [mockFile],
        writable: true
      })
      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      // Verify file is added
      expect(wrapper.vm.sourceFiles.length).toBe(1)

      // Upload files
      await wrapper.vm.uploadFiles()
      await flushPromises()

      // Verify upload was called
      expect(uploadDataCollectionFiles).toHaveBeenCalled()

      // Verify files cleared
      expect(wrapper.vm.sourceFiles.length).toBe(0)

      // Verify dialog opened
      expect(wrapper.vm.uploadedDataCollectionFilesReportDialogState).toBe(true)
    })

    it('should handle file removal and re-upload flow', async () => {
      // Upload files
      wrapper.vm.sourceFiles = [
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        },
        {
          file: mockFile2,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ]
      await wrapper.vm.$nextTick()

      // Remove one file
      const removeButtons = wrapper.findAll('[data-test="remove-files-button"]')
      await removeButtons[0].trigger('click')
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles.length).toBe(1)

      // Upload remaining file
      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(uploadDataCollectionFiles).toHaveBeenCalledWith([mockFile2])
    })

    it('should handle duplicate detection and renaming flow', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file.xml' }]

      // Upload file
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
      Object.defineProperty(fileInput.element, 'files', {
        value: [mockFile],
        writable: true
      })
      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      // Verify duplicate detected
      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)

      // Open rename dialog
      wrapper.vm.openFileRenameDialog(0)
      expect(wrapper.vm.displayRenameDialog).toBe(true)

      // Rename file
      await wrapper.vm.renameFile('renamed-file.xml')
      await flushPromises()

      expect(wrapper.vm.displayRenameDialog).toBe(false)
    })
  })

  describe('Parametrized Tests', () => {
    describe('File Status Combinations', () => {
      const fileStatuses = [
        { isValid: true, isDuplicate: false, expectedIcon: 'success' },
        { isValid: false, isDuplicate: false, expectedIcon: 'error' },
        { isValid: true, isDuplicate: true, expectedIcon: 'warning' },
        { isValid: false, isDuplicate: true, expectedIcon: 'error' }
      ]

      it.each(fileStatuses)(
        'should display $expectedIcon icon when isValid=$isValid and isDuplicate=$isDuplicate',
        async ({ isValid, isDuplicate, expectedIcon }) => {
          wrapper.vm.sourceFiles = [
            {
              file: mockFile,
              isValid,
              errors: isValid ? [] : ['Error'],
              isDuplicate
            }
          ]
          await wrapper.vm.$nextTick()

          const iconClass = `.${expectedIcon}-icon`
          const icons = wrapper.findAll(iconClass)
          expect(icons.length).toBeGreaterThan(0)
        }
      )
    })

    describe('Multiple File Upload Scenarios', () => {
      const fileScenarios = [
        { count: 1, description: 'single file' },
        { count: 3, description: 'multiple files' },
        { count: 10, description: 'many files' }
      ]

      it.each(fileScenarios)('should handle upload of $description ($count files)', async ({ count }) => {
        const files = Array.from(
          { length: count },
          (_, i) => new File(['content'], `file-${i}.xml`, { type: 'application/xml' })
        )

        const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
        Object.defineProperty(fileInput.element, 'files', {
          value: files,
          writable: true
        })

        await fileInput.trigger('change')
        await flushPromises()
        await wrapper.vm.$nextTick()

        expect(wrapper.vm.sourceFiles.length).toBe(count)
      })
    })

    describe('File Extension Validation', () => {
      const fileExtensions = [
        { name: 'test.xml', shouldPass: true },
        { name: 'test.txt', shouldPass: false },
        { name: 'test.json', shouldPass: false },
        { name: 'test', shouldPass: false }
      ]

      it.each(fileExtensions)(
        'should validate file "$name" (shouldPass: $shouldPass)',
        async ({ name, shouldPass }) => {
          const file = new File(['content'], name, { type: 'application/xml' })
          wrapper.vm.sourceFiles = [
            {
              file,
              isValid: true,
              errors: [],
              isDuplicate: false
            }
          ]

          await wrapper.vm.uploadFiles()
          await flushPromises()

          if (shouldPass) {
            expect(uploadDataCollectionFiles).toHaveBeenCalled()
          } else {
            expect(mockShowSnackBar).toHaveBeenCalled()
          }
        }
      )
    })
  })
})


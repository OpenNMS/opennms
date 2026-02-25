import SnmpDataCollectionSourceImport from '@/components/SnmpDataCollection/SnmpDataCollectionSourceImport.vue'
import { uploadDataCollectionFiles } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpDataCollectionSourceUploadResponse, UploadSnmpDataCollectionFileType } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherChip } from '@featherds/chips'
import { FeatherIcon } from '@featherds/icon'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSpinner } from '@featherds/progress'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

// Mock the service
vi.mock('@/services/snmpDataCollectionService', () => ({
  uploadDataCollectionFiles: vi.fn()
}))

// Mock the validator module
vi.mock('@/components/SnmpDataCollection/snmpDataCollectionSourceXmlValidator', () => ({
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
} from '@/components/SnmpDataCollection/snmpDataCollectionSourceXmlValidator'

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
          FeatherChip,
          FeatherPagination
        },
        stubs: {
          DataCollectionFilesUploadReportDialog: true,
          UploadedFileRenameDialog: true,
          TableCard: false,
          EmptyList: false,
          FeatherPagination: true
        }
      }
    })

    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // ─── Helper: simulate file input change ──────────────────────────────
  const triggerFileInput = async (files: File[]) => {
    const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
    Object.defineProperty(fileInput.element, 'files', {
      value: files,
      writable: true
    })
    await fileInput.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()
  }

  const triggerFolderInput = async (files: File[]) => {
    const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')
    Object.defineProperty(folderInput.element, 'files', {
      value: files,
      writable: true
    })
    await folderInput.trigger('change')
    await flushPromises()
    await wrapper.vm.$nextTick()
  }

  const setSourceFiles = async (files: UploadSnmpDataCollectionFileType[]) => {
    wrapper.vm.sourceFiles = files
    await wrapper.vm.$nextTick()
  }

  // ─── Initial Rendering ───────────────────────────────────────────────
  describe('Initial Rendering', () => {
    it('should render the component', () => {
      expect(wrapper.exists()).toBe(true)
    })

    it('should display the title "Import Data Collection Source"', () => {
      const title = wrapper.find('.title h3')
      expect(title.exists()).toBe(true)
      expect(title.text()).toBe('Import Data Collection Source')
    })

    it('should display subtitle description text', () => {
      const sub = wrapper.find('.sub p')
      expect(sub.exists()).toBe(true)
      expect(sub.text()).toContain('Upload files in the XML format')
    })

    it('should display empty list message when no files are loaded', () => {
      const emptyList = wrapper.find('[data-test="empty-list"]')
      expect(emptyList.exists()).toBe(true)
      expect(wrapper.text()).toContain('No files selected for upload')
    })

    it('should render "Choose files to upload" button', () => {
      const btn = wrapper.find('[data-test="choose-file-button"]')
      expect(btn.exists()).toBe(true)
      expect(btn.text()).toContain('Choose files to upload')
    })

    it('should render "Choose folder to upload" button', () => {
      const btn = wrapper.find('[data-test="choose-folder-button"]')
      expect(btn.exists()).toBe(true)
      expect(btn.text()).toContain('Choose folder to upload')
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

    it('should render folder input with directory attribute', () => {
      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')
      expect(folderInput.attributes('directory')).toBeDefined()
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

    it('should render the data table with correct headers', () => {
      const table = wrapper.find('table.data-table')
      expect(table.exists()).toBe(true)
      const ths = table.findAll('th')
      expect(ths).toHaveLength(2)
      expect(ths[0].text()).toBe('Source')
      expect(ths[1].text()).toBe('Action')
    })

    it('should have aria-label on the table', () => {
      const table = wrapper.find('table.data-table')
      expect(table.attributes('aria-label')).toBe('Events Table')
    })

    it('should not render table body when no files', () => {
      const tbody = wrapper.find('tbody')
      expect(tbody.exists()).toBe(false)
    })

    it('should not render pagination when no files', () => {
      const pagination = wrapper.find('[data-test="FeatherPagination"]')
      expect(pagination.exists()).toBe(false)
    })
  })

  // ─── File Selection ──────────────────────────────────────────────────
  describe('File Selection', () => {
    it('should open file dialog when "Choose files to upload" button is clicked', async () => {
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
      const clickSpy = vi.spyOn(fileInput.element as HTMLInputElement, 'click')

      const chooseBtn = wrapper.find('[data-test="choose-file-button"]')
      await chooseBtn.trigger('click')

      expect(clickSpy).toHaveBeenCalled()
    })

    it('should open folder dialog when "Choose folder to upload" button is clicked', async () => {
      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')
      const clickSpy = vi.spyOn(folderInput.element as HTMLInputElement, 'click')

      const chooseBtn = wrapper.find('[data-test="choose-folder-button"]')
      await chooseBtn.trigger('click')

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

  // ─── File Upload Handling ────────────────────────────────────────────
  describe('File Upload Handling', () => {
    it('should process valid file when uploaded', async () => {
      await triggerFileInput([mockFile])

      expect(validateSnmpDataCollectionSourceFile).toHaveBeenCalledWith(mockFile)
      expect(wrapper.vm.sourceFiles.length).toBe(1)
    })

    it('should process multiple files when uploaded', async () => {
      await triggerFileInput([mockFile, mockFile2])

      expect(wrapper.vm.sourceFiles.length).toBe(2)
    })

    it('should handle invalid file upload', async () => {
      vi.mocked(validateSnmpDataCollectionSourceFile).mockResolvedValueOnce({
        isValid: false,
        errors: ['Invalid XML format']
      })

      await triggerFileInput([mockInvalidFile])

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: expect.stringContaining('Error processing file'),
        error: true
      })
    })

    it('should add invalid file to sourceFiles with errors', async () => {
      vi.mocked(validateSnmpDataCollectionSourceFile).mockResolvedValueOnce({
        isValid: false,
        errors: ['Invalid XML format']
      })

      await triggerFileInput([mockInvalidFile])

      expect(wrapper.vm.sourceFiles.length).toBe(1)
      expect(wrapper.vm.sourceFiles[0].isValid).toBe(false)
      expect(wrapper.vm.sourceFiles[0].errors).toEqual(['Invalid XML format'])
    })

    it('should skip duplicate files when uploading', async () => {
      vi.mocked(isDuplicateFile).mockReturnValue(true)

      await triggerFileInput([mockFile])

      expect(wrapper.vm.sourceFiles.length).toBe(0)
    })

    it('should mark file as duplicate if already uploaded to server', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file.xml' }]

      await triggerFileInput([mockFile])

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })

    it('should mark file as duplicate case-insensitively', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'TEST-FILE.xml' }]

      await triggerFileInput([mockFile])

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

    it('should handle file upload with empty file list (length 0)', async () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')

      Object.defineProperty(fileInput.element, 'files', {
        value: [],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()

      // Empty FileList has length 0, so falls into else branch
      expect(consoleSpy).toHaveBeenCalledWith('No files selected')
      consoleSpy.mockRestore()
    })

    it('should handle errors during file processing', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(validateSnmpDataCollectionSourceFile).mockRejectedValueOnce(new Error('Validation error'))

      await triggerFileInput([mockFile])

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: expect.stringContaining('Error processing file'),
        error: true
      })
      consoleErrorSpy.mockRestore()
    })

    it('should log error to console when file processing throws', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(validateSnmpDataCollectionSourceFile).mockRejectedValueOnce(new Error('Validation error'))

      await triggerFileInput([mockFile])

      expect(consoleSpy).toHaveBeenCalledWith(
        expect.stringContaining('Error processing file test-file.xml'),
        expect.any(Error)
      )
      consoleSpy.mockRestore()
    })

    it('should set isValid and errors correctly for valid file', async () => {
      await triggerFileInput([mockFile])

      expect(wrapper.vm.sourceFiles[0].isValid).toBe(true)
      expect(wrapper.vm.sourceFiles[0].errors).toEqual([])
    })

    it('should not mark non-duplicate file as duplicate', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'other-file.xml' }]

      await triggerFileInput([mockFile])

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
    })

    it('should process mix of valid and skipped files', async () => {
      vi.mocked(isDuplicateFile).mockReturnValueOnce(false).mockReturnValueOnce(true) // second file is duplicate in local list

      await triggerFileInput([mockFile, mockFile2])

      expect(wrapper.vm.sourceFiles.length).toBe(1)
      expect(wrapper.vm.sourceFiles[0].file.name).toBe('test-file.xml')
    })
  })

  // ─── Folder Upload Handling ──────────────────────────────────────────
  describe('Folder Upload Handling', () => {
    it('should process files from folder upload', async () => {
      await triggerFolderInput([mockFile, mockFile2])

      expect(wrapper.vm.sourceFiles.length).toBe(2)
    })

    it('should skip already uploaded files in folder upload', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file.xml' }]

      await triggerFolderInput([mockFile])

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: expect.stringContaining('has already been uploaded'),
        error: true
      })
      expect(wrapper.vm.sourceFiles.length).toBe(0)
    })

    it('should skip already uploaded files case-insensitively in folder upload', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'TEST-FILE.xml' }]

      await triggerFolderInput([mockFile])

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: expect.stringContaining('has already been uploaded. Skipping'),
        error: true
      })
      expect(wrapper.vm.sourceFiles.length).toBe(0)
    })

    it('should skip duplicate files in folder upload', async () => {
      vi.mocked(isDuplicateFile).mockReturnValue(true)

      await triggerFolderInput([mockFile])

      expect(wrapper.vm.sourceFiles.length).toBe(0)
    })

    it('should handle invalid files in folder upload', async () => {
      vi.mocked(validateSnmpDataCollectionSourceFile).mockResolvedValueOnce({
        isValid: false,
        errors: ['Invalid XML format']
      })

      await triggerFolderInput([mockInvalidFile])

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: expect.stringContaining('Error processing file'),
        error: true
      })
    })

    it('should handle errors during folder file processing', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(validateSnmpDataCollectionSourceFile).mockRejectedValueOnce(new Error('Validation error'))

      await triggerFolderInput([mockFile])

      expect(consoleSpy).toHaveBeenCalledWith(
        expect.stringContaining('Error processing file test-file.xml'),
        expect.any(Error)
      )
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: expect.stringContaining('Error processing file'),
        error: true
      })
      consoleSpy.mockRestore()
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

    it('should still add non-uploaded files when mix is present', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file.xml' }]

      vi.mocked(isDuplicateFile).mockReturnValue(false)

      await triggerFolderInput([mockFile, mockFile2])

      // test-file.xml is skipped because already uploaded; test-file-2.xml is added
      expect(wrapper.vm.sourceFiles.length).toBe(1)
      expect(wrapper.vm.sourceFiles[0].file.name).toBe('test-file-2.xml')
    })

    it('should not do anything when folder input has no files', async () => {
      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')

      Object.defineProperty(folderInput.element, 'files', {
        value: null,
        writable: true
      })

      await folderInput.trigger('change')
      await flushPromises()

      expect(wrapper.vm.sourceFiles.length).toBe(0)
    })
  })

  // ─── File Display ────────────────────────────────────────────────────
  describe('File Display', () => {
    beforeEach(async () => {
      await setSourceFiles([
        {
          file: mockFile,
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ])
    })

    it('should display uploaded file name', () => {
      expect(wrapper.text()).toContain('test-file.xml')
    })

    it('should display file icon in file row', () => {
      const fileDiv = wrapper.find('.file')
      expect(fileDiv.exists()).toBe(true)
      const icons = fileDiv.findAllComponents(FeatherIcon)
      expect(icons.length).toBeGreaterThan(0)
    })

    it('should display remove button for each file', () => {
      const removeButtons = wrapper.findAll('[data-test="remove-files-button"]')
      expect(removeButtons.length).toBe(1)
    })

    it('should display success icon for valid non-duplicate files', () => {
      const successIcons = wrapper.findAll('.success-icon')
      expect(successIcons.length).toBe(1)
    })

    it('should not display empty list when files are present', () => {
      const emptyList = wrapper.find('[data-test="empty-list"]')
      expect(emptyList.exists()).toBe(false)
    })

    it('should render table rows for each file', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: false }
      ])

      const rows = wrapper.findAll('tr')
      // header row + 2 data rows
      expect(rows.length).toBeGreaterThanOrEqual(3)
    })

    it('should render pagination when files are present', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      const pagination = wrapper.find('[data-test="FeatherPagination"]')
      expect(pagination.exists()).toBe(true)
    })
  })

  // ─── File Status Icons & Chips ───────────────────────────────────────
  describe('File Status Icons & Chips', () => {
    it('should display success icon for valid non-duplicate file', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      const successIcons = wrapper.findAll('.success-icon')
      expect(successIcons.length).toBe(1)
    })

    it('should display error icon for invalid file', async () => {
      await setSourceFiles([{ file: mockFile, isValid: false, errors: ['Invalid XML'], isDuplicate: false }])

      const errorIcons = wrapper.findAll('.error-icon')
      expect(errorIcons.length).toBe(1)
    })

    it('should display warning icon for duplicate file', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])

      const warningIcons = wrapper.findAll('.warning-icon')
      expect(warningIcons.length).toBe(1)
    })

    it('should not display success icon for invalid file', async () => {
      await setSourceFiles([{ file: mockFile, isValid: false, errors: ['Invalid XML'], isDuplicate: false }])

      const successIcons = wrapper.findAll('.success-icon')
      expect(successIcons.length).toBe(0)
    })

    it('should not display success icon for duplicate file', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])

      const successIcons = wrapper.findAll('.success-icon')
      expect(successIcons.length).toBe(0)
    })

    it('should display error chip with error messages for invalid file', async () => {
      await setSourceFiles([{ file: mockFile, isValid: false, errors: ['Error 1', 'Error 2'], isDuplicate: false }])

      const errorChip = wrapper.find('.error-chip')
      expect(errorChip.exists()).toBe(true)
      expect(errorChip.text()).toBe('Error 1. Error 2')
    })

    it('should display warning chip for duplicate file', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])

      const warningChip = wrapper.find('.warning-chip')
      expect(warningChip.exists()).toBe(true)
      expect(warningChip.text()).toContain('File with the same name already exists')
    })

    it('should not display error chip for valid file', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      const errorChip = wrapper.find('.error-chip')
      expect(errorChip.exists()).toBe(false)
    })

    it('should not display warning chip for non-duplicate file', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      const warningChip = wrapper.find('.warning-chip')
      expect(warningChip.exists()).toBe(false)
    })

    it('should display both error icon and error chip for invalid file', async () => {
      await setSourceFiles([{ file: mockFile, isValid: false, errors: ['XML error'], isDuplicate: false }])

      expect(wrapper.find('.error-icon').exists()).toBe(true)
      expect(wrapper.find('.error-chip').exists()).toBe(true)
    })

    it('should display both warning icon and warning chip for duplicate file', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])

      expect(wrapper.find('.warning-icon').exists()).toBe(true)
      expect(wrapper.find('.warning-chip').exists()).toBe(true)
    })

    it('should display error chip with joined error messages separated by period+space', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: false, errors: ['Err A', 'Err B', 'Err C'], isDuplicate: false }
      ])

      const errorChip = wrapper.find('.error-chip')
      expect(errorChip.text()).toBe('Err A. Err B. Err C')
    })

    it('should show correct icons for multiple files with mixed statuses', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: false, errors: ['Error'], isDuplicate: false },
        { file: new File(['x'], 'dup.xml'), isValid: true, errors: [], isDuplicate: true }
      ])

      expect(wrapper.findAll('.success-icon').length).toBe(1)
      expect(wrapper.findAll('.error-icon').length).toBe(1)
      expect(wrapper.findAll('.warning-icon').length).toBe(1)
    })

    it('should display both error icon and warning icon when file is invalid and duplicate', async () => {
      await setSourceFiles([{ file: mockFile, isValid: false, errors: ['Invalid'], isDuplicate: true }])

      expect(wrapper.find('.error-icon').exists()).toBe(true)
      expect(wrapper.find('.warning-icon').exists()).toBe(true)
      expect(wrapper.find('.success-icon').exists()).toBe(false)
    })
  })

  // ─── File Removal ────────────────────────────────────────────────────
  describe('File Removal', () => {
    beforeEach(async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: false }
      ])
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

    it('should show empty list after removing all files', async () => {
      wrapper.vm.sourceFiles = []
      await wrapper.vm.$nextTick()

      const emptyList = wrapper.find('[data-test="empty-list"]')
      expect(emptyList.exists()).toBe(true)
    })

    it('should call removeFile method with correct index', async () => {
      wrapper.vm.removeFile(1)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles.length).toBe(1)
      expect(wrapper.vm.sourceFiles[0].file.name).toBe('test-file.xml')
    })

    it('should remove the only file and show empty state', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      wrapper.vm.removeFile(0)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles.length).toBe(0)
      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
    })
  })

  // ─── Upload Button State ─────────────────────────────────────────────
  describe('Upload Button State', () => {
    it('should be disabled when no files are selected', () => {
      wrapper.vm.sourceFiles = []
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should be disabled when loading', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])
      wrapper.vm.isLoading = true
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should be disabled when file is invalid', async () => {
      await setSourceFiles([{ file: mockFile, isValid: false, errors: ['Invalid XML'], isDuplicate: false }])

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should be disabled when file is duplicate', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should be disabled when one valid and one invalid file', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: false, errors: ['Error'], isDuplicate: false }
      ])

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should be disabled when one valid and one duplicate file', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: true }
      ])

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should be enabled when files are valid and not duplicates', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])
      wrapper.vm.isLoading = false
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })

    it('should be enabled when multiple valid non-duplicate files', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: false }
      ])

      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })

    it('should disable action buttons when loading', async () => {
      wrapper.vm.isLoading = true
      await wrapper.vm.$nextTick()

      const buttons = wrapper.findAllComponents(FeatherButton)
      const chooseFilesButton = buttons.find((btn) => btn.text().includes('Choose files to upload'))
      const chooseFolderButton = buttons.find((btn) => btn.text().includes('Choose folder to upload'))

      expect(chooseFilesButton?.props('disabled')).toBe(true)
      expect(chooseFolderButton?.props('disabled')).toBe(true)
    })
  })

  // ─── File Upload ─────────────────────────────────────────────────────
  describe('File Upload', () => {
    beforeEach(() => {
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]
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
      wrapper.vm.sourceFiles = [{ file: nonXmlFile, isValid: true, errors: [], isDuplicate: false }]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'All files must be XML files with .xml extension',
        error: true
      })
      expect(uploadDataCollectionFiles).not.toHaveBeenCalled()
    })

    it('should handle upload error', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error('Upload failed')
      vi.mocked(uploadDataCollectionFiles).mockRejectedValueOnce(error)

      await wrapper.vm.uploadFiles()
      await flushPromises()
      consoleErrorSpy.mockRestore()

      expect(wrapper.vm.isLoading).toBe(false)
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Error uploading files',
        error: true
      })
    })

    it('should log error to console on upload failure', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error('Upload failed')
      vi.mocked(uploadDataCollectionFiles).mockRejectedValueOnce(error)

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(consoleSpy).toHaveBeenCalledWith(error)
      consoleSpy.mockRestore()
    })

    it('should only upload valid files (filter out invalid)', async () => {
      wrapper.vm.sourceFiles = [
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: false, errors: ['Invalid'], isDuplicate: false }
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

    it('should display "Upload Files" text when not loading', async () => {
      wrapper.vm.isLoading = false
      await wrapper.vm.$nextTick()

      const uploadButton = wrapper.find('[data-test="upload-button"]')
      expect(uploadButton.text()).toContain('Upload Files')
    })

    it('should reject upload when mix of xml and non-xml files', async () => {
      wrapper.vm.sourceFiles = [
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: new File(['x'], 'bad.txt'), isValid: true, errors: [], isDuplicate: false }
      ]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'All files must be XML files with .xml extension',
        error: true
      })
    })
  })

  // ─── Dialogs ─────────────────────────────────────────────────────────
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

      it('should pass report prop to DataCollectionFilesUploadReportDialog', async () => {
        const report = { errors: [], success: [{ file: 'a.xml' }] }
        wrapper.vm.uploadFilesReport = report
        await wrapper.vm.$nextTick()

        const dialog = wrapper.findComponent({ name: 'DataCollectionFilesUploadReportDialog' })
        if (dialog.exists()) {
          expect(dialog.props('report')).toEqual(report)
        }
      })
    })

    describe('Rename Dialog', () => {
      beforeEach(() => {
        wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: true }]
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

      it('should close rename dialog and reset selectedIndex', () => {
        wrapper.vm.displayRenameDialog = true
        wrapper.vm.selectedIndex = 0

        wrapper.vm.closeRenameDialog()

        expect(wrapper.vm.displayRenameDialog).toBe(false)
        expect(wrapper.vm.selectedIndex).toBeNull()
      })

      it('should set store.activeTab to 0 on closeRenameDialog', () => {
        store.activeTab = 1
        wrapper.vm.closeRenameDialog()

        expect(store.activeTab).toBe(0)
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

      it('should handle invalid index when renaming (null)', async () => {
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
        wrapper.vm.selectedIndex = null

        await wrapper.vm.renameFile('renamed-file.xml')

        expect(consoleSpy).toHaveBeenCalledWith('Invalid index for renaming file')
        consoleSpy.mockRestore()
      })

      it('should handle invalid index when renaming (out of range)', async () => {
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
        wrapper.vm.selectedIndex = 999

        await wrapper.vm.renameFile('renamed-file.xml')

        expect(consoleSpy).toHaveBeenCalledWith('Invalid index for renaming file')
        consoleSpy.mockRestore()
      })

      it('should handle negative index when renaming', async () => {
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
        wrapper.vm.selectedIndex = -1

        await wrapper.vm.renameFile('renamed-file.xml')

        expect(consoleSpy).toHaveBeenCalledWith('Invalid index for renaming file')
        consoleSpy.mockRestore()
      })

      it('should check duplicate status after rename', async () => {
        store.uploadedSourceNames = [{ id: 1, name: 'existing.xml' }]
        wrapper.vm.selectedIndex = 0

        await wrapper.vm.renameFile('existing.xml')
        await flushPromises()

        expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
      })

      it('should overwrite duplicate file', () => {
        wrapper.vm.selectedIndex = 0
        wrapper.vm.sourceFiles[0].isDuplicate = true

        wrapper.vm.overwriteFile()

        expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
        expect(wrapper.vm.displayRenameDialog).toBe(false)
      })

      it('should handle invalid index when overwriting (out of range)', () => {
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
        wrapper.vm.selectedIndex = 999

        wrapper.vm.overwriteFile()

        expect(consoleSpy).toHaveBeenCalledWith('Invalid index for overwriting file')
        consoleSpy.mockRestore()
      })

      it('should handle null index when overwriting', () => {
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
        wrapper.vm.selectedIndex = null

        wrapper.vm.overwriteFile()

        expect(consoleSpy).toHaveBeenCalledWith('Invalid index for overwriting file')
        consoleSpy.mockRestore()
      })

      it('should handle negative index when overwriting', () => {
        const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
        wrapper.vm.selectedIndex = -1

        wrapper.vm.overwriteFile()

        expect(consoleSpy).toHaveBeenCalledWith('Invalid index for overwriting file')
        consoleSpy.mockRestore()
      })
    })
  })

  // ─── Watchers ────────────────────────────────────────────────────────
  describe('Watchers', () => {
    it('should update isDuplicate when store.uploadedSourceNames changes', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      store.uploadedSourceNames = [{ id: 1, name: 'test-file.xml' }]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })

    it('should handle case-insensitive duplicate detection via watcher', async () => {
      await setSourceFiles([
        {
          file: new File(['content'], 'Test-File.xml', { type: 'application/xml' }),
          isValid: true,
          errors: [],
          isDuplicate: false
        }
      ])

      store.uploadedSourceNames = [{ id: 1, name: 'test-file.xml' }]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })

    it('should maintain non-duplicate status for unique files', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      store.uploadedSourceNames = [{ id: 1, name: 'different-file.xml' }]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
    })

    it('should update multiple files when uploadedSourceNames changes', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: false }
      ])

      store.uploadedSourceNames = [{ id: 1, name: 'test-file.xml' }]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
      expect(wrapper.vm.sourceFiles[1].isDuplicate).toBe(false)
    })

    it('should clear duplicate flags when uploadedSourceNames is emptied', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file.xml' }]
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])

      store.uploadedSourceNames = []
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
    })
  })

  // ─── Instructions Section ────────────────────────────────────────────
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

    it('should have at least 7 instruction items', () => {
      const items = wrapper.findAll('.info-section li')
      expect(items.length).toBeGreaterThanOrEqual(7)
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

  // ─── Ellipsify Utility ───────────────────────────────────────────────
  describe('Ellipsify Utility', () => {
    it('should display full filename when short', async () => {
      const shortFile = new File(['content'], 'short.xml', { type: 'application/xml' })
      await setSourceFiles([{ file: shortFile, isValid: true, errors: [], isDuplicate: false }])

      expect(wrapper.text()).toContain('short.xml')
    })

    it('should truncate long filenames', async () => {
      const longFileName = 'this-is-a-very-long-filename-that-should-be-truncated.xml'
      const longFile = new File(['content'], longFileName, { type: 'application/xml' })
      await setSourceFiles([{ file: longFile, isValid: true, errors: [], isDuplicate: false }])

      // The ellipsify function truncates to 39 characters
      const displayedText = wrapper.text()
      expect(displayedText).not.toContain(longFileName)
    })
  })

  // ─── Edge Cases ──────────────────────────────────────────────────────
  describe('Edge Cases', () => {
    it('should handle file with special characters in name', async () => {
      const specialFile = new File(['content'], 'test@#$%file.xml', { type: 'application/xml' })

      await triggerFileInput([specialFile])

      expect(wrapper.vm.sourceFiles.length).toBe(1)
    })

    it('should handle file with spaces in name', async () => {
      const spaceFile = new File(['content'], 'test file with spaces.xml', { type: 'application/xml' })

      await triggerFileInput([spaceFile])

      expect(wrapper.vm.sourceFiles.length).toBe(1)
    })

    it('should handle multiple validation errors', async () => {
      vi.mocked(validateSnmpDataCollectionSourceFile).mockResolvedValueOnce({
        isValid: false,
        errors: ['Error 1', 'Error 2', 'Error 3']
      })

      await triggerFileInput([mockFile])

      expect(wrapper.vm.sourceFiles[0].errors).toHaveLength(3)
    })

    it('should handle empty file name (.xml only)', async () => {
      const emptyNameFile = new File(['content'], '.xml', { type: 'application/xml' })

      await triggerFileInput([emptyNameFile])

      expect(wrapper.vm.sourceFiles.length).toBe(1)
    })

    it('should handle very large file list', async () => {
      const manyFiles = Array.from(
        { length: 100 },
        (_, i) => new File(['content'], `file-${i}.xml`, { type: 'application/xml' })
      )

      await triggerFileInput(manyFiles)

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

    it('should handle file with unicode characters in name', async () => {
      const unicodeFile = new File(['content'], 'файл-данные.xml', { type: 'application/xml' })

      await triggerFileInput([unicodeFile])

      expect(wrapper.vm.sourceFiles.length).toBe(1)
    })

    it('should handle file with very long name', async () => {
      const longName = 'a'.repeat(200) + '.xml'
      const longNameFile = new File(['content'], longName, { type: 'application/xml' })

      await triggerFileInput([longNameFile])

      expect(wrapper.vm.sourceFiles.length).toBe(1)
    })

    it('should preserve file type when adding to sourceFiles', async () => {
      await triggerFileInput([mockFile])

      expect(wrapper.vm.sourceFiles[0].file).toBeInstanceOf(File)
      expect(wrapper.vm.sourceFiles[0].file.type).toBe('application/xml')
    })
  })

  // ─── Accessibility ───────────────────────────────────────────────────
  describe('Accessibility', () => {
    it('should have proper data-test attributes', () => {
      expect(wrapper.find('[data-test="snmp-data-collection-file-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="snmp-data-collection-folder-input"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="upload-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="choose-file-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="choose-folder-button"]').exists()).toBe(true)
    })

    it('should have proper button types', () => {
      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons.length).toBeGreaterThan(0)
    })

    it('should have semantic HTML structure', () => {
      expect(wrapper.find('h3').exists()).toBe(true)
      expect(wrapper.find('table').exists()).toBe(true)
      expect(wrapper.find('thead').exists()).toBe(true)
      expect(wrapper.find('ul').exists()).toBe(true)
    })

    it('should have table with aria-label', () => {
      const table = wrapper.find('table.data-table')
      expect(table.attributes('aria-label')).toBeDefined()
    })

    it('should have remove buttons for each file row', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: false }
      ])

      const removeButtons = wrapper.findAll('[data-test="remove-files-button"]')
      expect(removeButtons.length).toBe(2)
    })
  })

  // ─── Integration Tests ───────────────────────────────────────────────
  describe('Integration Tests', () => {
    it('should handle complete upload flow', async () => {
      // Upload file
      await triggerFileInput([mockFile])

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
      // Set up files
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: false }
      ])

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
      await triggerFileInput([mockFile])

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

    it('should handle duplicate detection and overwrite flow', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file.xml' }]

      // Upload file
      await triggerFileInput([mockFile])

      // Verify duplicate detected
      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)

      // Overwrite
      wrapper.vm.selectedIndex = 0
      wrapper.vm.overwriteFile()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })

    it('should handle upload error and allow retry', async () => {
      // Set up files explicitly for this test
      wrapper.vm.sourceFiles = [
        { file: mockFile, isValid: true, errors: [], isDuplicate: false }
      ]

      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(uploadDataCollectionFiles).mockRejectedValueOnce(new Error('Network error'))

      await wrapper.vm.uploadFiles()
      await flushPromises()
      consoleErrorSpy.mockRestore()

      // Files should still be present after error
      expect(wrapper.vm.sourceFiles.length).toBe(1)
      expect(wrapper.vm.isLoading).toBe(false)

      // Reset mock and retry
      vi.mocked(uploadDataCollectionFiles).mockResolvedValueOnce({
        errors: [],
        success: [{ file: 'test-file.xml' }]
      } as any)

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.sourceFiles.length).toBe(0)
      expect(wrapper.vm.uploadedDataCollectionFilesReportDialogState).toBe(true)
    })

    it('should handle close report dialog and navigate to view', () => {
      wrapper.vm.uploadedDataCollectionFilesReportDialogState = true
      wrapper.vm.gotoViewTab()

      expect(wrapper.vm.uploadedDataCollectionFilesReportDialogState).toBe(false)
      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection' })
    })
  })

  // ─── Parametrized Tests ──────────────────────────────────────────────
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
          await setSourceFiles([
            {
              file: mockFile,
              isValid,
              errors: isValid ? [] : ['Error'],
              isDuplicate
            }
          ])

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

        await triggerFileInput(files)

        expect(wrapper.vm.sourceFiles.length).toBe(count)
      })
    })

    describe('File Extension Validation', () => {
      const fileExtensions = [
        { name: 'test.xml', shouldPass: true },
        { name: 'test.txt', shouldPass: false },
        { name: 'test.json', shouldPass: false },
        { name: 'test', shouldPass: false },
        { name: 'test.XML', shouldPass: false },
        { name: 'test.xml.bak', shouldPass: false }
      ]

      it.each(fileExtensions)(
        'should validate file "$name" (shouldPass: $shouldPass)',
        async ({ name, shouldPass }) => {
          const file = new File(['content'], name, { type: 'application/xml' })
          wrapper.vm.sourceFiles = [{ file, isValid: true, errors: [], isDuplicate: false }]

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

    describe('shouldUploadDisabled computed property', () => {
      const disabledCombinations = [
        { files: 0, isLoading: false, allValid: true, anyDuplicate: false, expected: true, desc: 'no files' },
        { files: 1, isLoading: true, allValid: true, anyDuplicate: false, expected: true, desc: 'loading' },
        { files: 1, isLoading: false, allValid: false, anyDuplicate: false, expected: true, desc: 'invalid file' },
        { files: 1, isLoading: false, allValid: true, anyDuplicate: true, expected: true, desc: 'duplicate file' },
        { files: 1, isLoading: false, allValid: true, anyDuplicate: false, expected: false, desc: 'all conditions met' }
      ]

      it.each(disabledCombinations)(
        'should return $expected when $desc',
        async ({ files, isLoading, allValid, anyDuplicate, expected }) => {
          if (files > 0) {
            wrapper.vm.sourceFiles = [
              { file: mockFile, isValid: allValid, errors: allValid ? [] : ['err'], isDuplicate: anyDuplicate }
            ]
          } else {
            wrapper.vm.sourceFiles = []
          }
          wrapper.vm.isLoading = isLoading
          await wrapper.vm.$nextTick()

          expect(wrapper.vm.shouldUploadDisabled).toBe(expected)
        }
      )
    })
  })
})

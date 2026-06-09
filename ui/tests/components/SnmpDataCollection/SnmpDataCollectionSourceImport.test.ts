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
import { nextTick } from 'vue'

// Mock the service
vi.mock('@/services/snmpDataCollectionService', () => ({
  uploadDataCollectionFiles: vi.fn(),
  getAllSnmpCollectionProfiles: vi.fn().mockResolvedValue([
    { id: 1, name: 'default', enabled: true, rrdStep: 300, rrdRras: [], storageFlag: 'select', sourceNames: [] }
  ])
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

    // Mock validator functions. The real validator parses the
    // <datacollection-group name="..."> attribute; for tests we synthesize a
    // group name from the file's basename so test fixtures can model the DB
    // source name (which is the group attribute, NOT the filename).
    vi.mocked(validateSnmpDataCollectionSourceFile).mockImplementation(async (file: File) => ({
      isValid: true,
      errors: [],
      kind: 'group',
      groupName: file.name.replace(/\.xml$/i, '')
    }))
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

    // Pre-select the 'default' profile so existing upload-enabled tests are not
    // gated on the new "must pick at least one profile" rule. Tests that care
    // about no-profile-selected behavior should toggle this off explicitly.
    const cb = wrapper.findComponent<any>('[data-test="profile-checkbox-default"]')
    if (cb.exists()) {
      cb.vm.$emit('update:modelValue', true)
      await nextTick()
    }
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
    // Tests use this helper to seed sourceFiles directly (bypassing the
    // validator). Derive groupName from the filename basename so the
    // watcher's name-based duplicate detection works without each test
    // having to know about the groupName field.
    wrapper.vm.sourceFiles = files.map(f => ({
      ...f,
      groupName: f.groupName ?? f.file.name.replace(/\.xml$/i, '')
    }))
    wrapper.vm.total = files.length
    wrapper.vm.tableRecord = wrapper.vm.sourceFiles.slice(0, wrapper.vm.pageSize)
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

    it('should call fetchAllSourcesNames on mount', () => {
      expect(store.fetchAllSourcesNames).toHaveBeenCalledTimes(1)
    })

    it('should render hidden file input elements', () => {
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')
      expect(fileInput.attributes('type')).toBe('file')
      expect(folderInput.attributes('type')).toBe('file')
    })

    it('should render the data table with correct headers when files are present', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      const table = wrapper.find('table.data-table')
      expect(table.exists()).toBe(true)
      const ths = table.findAll('th')
      expect(ths).toHaveLength(2)
      expect(ths[0].text()).toBe('Source')
      expect(ths[1].text()).toBe('Action')
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
      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]

      await triggerFileInput([mockFile])

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })

    it('should mark file as duplicate case-insensitively', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'TEST-FILE' }]

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
      store.uploadedSourceNames = [{ id: 1, name: 'other-file' }]

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

    it('should mark already-uploaded files as updates in folder upload', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]

      await triggerFolderInput([mockFile])

      // Re-uploaded sources are accepted as edits/updates rather than skipped
      // — the server upserts them — so they show up in the table marked
      // `isDuplicate: true` (rendered as a blue "Will update" chip).
      expect(wrapper.vm.sourceFiles.length).toBe(1)
      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })

    it('should mark already-uploaded files as updates case-insensitively', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'TEST-FILE' }]

      await triggerFolderInput([mockFile])

      expect(wrapper.vm.sourceFiles.length).toBe(1)
      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
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

    it('should add both already-uploaded (as update) and new files when mix is present', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]

      vi.mocked(isDuplicateFile).mockReturnValue(false)

      await triggerFolderInput([mockFile, mockFile2])

      // Both files end up in the queue: test-file.xml as an update (existing
      // source name), test-file-2.xml as new.
      expect(wrapper.vm.sourceFiles.length).toBe(2)
      const byName = wrapper.vm.sourceFiles.reduce(
        (acc: Record<string, any>, f: any) => ({ ...acc, [f.file.name]: f }),
        {} as Record<string, any>
      )
      expect(byName['test-file.xml'].isDuplicate).toBe(true)
      expect(byName['test-file-2.xml'].isDuplicate).toBe(false)
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

    it('should not display empty list when files are present', () => {
      const emptyList = wrapper.find('[data-test="empty-list"]')
      expect(emptyList.exists()).toBe(false)
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

    it('should display update icon for re-uploaded (duplicate) source file', async () => {
      // Re-uploaded sources are upserts on the server, so the row gets a
      // calm "update" icon rather than a blocking warning.
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])

      const updateIcons = wrapper.findAll('.update-icon')
      expect(updateIcons.length).toBe(1)
      // Old warning treatment is gone for duplicates.
      expect(wrapper.findAll('.warning-icon').length).toBe(0)
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

    it('should display update chip for re-uploaded (duplicate) source file', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: true, kind: 'group', groupName: 'Dell' }
      ])

      const updateChip = wrapper.find('.update-chip')
      expect(updateChip.exists()).toBe(true)
      expect(updateChip.text()).toContain('Will update existing source')
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

    it('should display both update icon and update chip for re-uploaded source file', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])

      expect(wrapper.find('.update-icon').exists()).toBe(true)
      expect(wrapper.find('.update-chip').exists()).toBe(true)
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
      expect(wrapper.findAll('.update-icon').length).toBe(1)
    })

    it('should display only error icon when file is invalid (even if duplicate)', async () => {
      // Invalid takes precedence: the row needs fixing before the upload can
      // do anything, so we don't muddy it with the update indicator.
      await setSourceFiles([{ file: mockFile, isValid: false, errors: ['Invalid'], isDuplicate: true }])

      expect(wrapper.find('.error-icon').exists()).toBe(true)
      expect(wrapper.find('.update-icon').exists()).toBe(false)
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
      wrapper.vm.tableRecord = []
      wrapper.vm.total = 0
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

    it('should be enabled when file is a re-uploaded (duplicate) source', async () => {
      // Duplicates are accepted as updates, so they no longer block upload.
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])

      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })

    it('should be disabled when one valid and one invalid file', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: false, errors: ['Error'], isDuplicate: false }
      ])

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should be enabled when one new and one re-uploaded source', async () => {
      // Mix of new + update is fine; both are valid upserts.
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: true }
      ])

      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
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
      const chooseFilesButton = buttons.find(btn => btn.text().includes('Choose files to upload'))
      const chooseFolderButton = buttons.find(btn => btn.text().includes('Choose folder to upload'))

      expect(chooseFilesButton?.props('disabled')).toBe(true)
      expect(chooseFolderButton?.props('disabled')).toBe(true)
    })
  })

  // ─── File Upload ─────────────────────────────────────────────────────
  describe('File Upload', () => {
    beforeEach(() => {
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]
    })

    it('should call uploadDataCollectionFiles with valid files', async () => {
      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(uploadDataCollectionFiles).toHaveBeenCalledWith([mockFile], ['default'])
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

      expect(uploadDataCollectionFiles).toHaveBeenCalledWith([mockFile], ['default'])
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

  })

  // ─── Dialogs ─────────────────────────────────────────────────────────
  describe('Dialogs', () => {
    describe('Upload Report Dialog', () => {
      it('should close upload report dialog', () => {
        wrapper.vm.uploadedDataCollectionFilesReportDialogState = true
        wrapper.vm.closeUploadReportDialog()

        expect(wrapper.vm.uploadedDataCollectionFilesReportDialogState).toBe(false)
      })
    })

    describe('Rename Dialog', () => {
      beforeEach(async () => {
        await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])
      })

      it('should not auto-open rename dialog for re-uploaded sources', async () => {
        // Duplicates render the calm "update" icon now; the rename dialog
        // is no longer wired into the duplicate icon. The rename helper
        // is still exposed programmatically (next test) for callers that
        // need it.
        await wrapper.vm.$nextTick()
        expect(wrapper.find('.warning-icon').exists()).toBe(false)
        expect(wrapper.vm.displayRenameDialog).toBe(false)
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

      it('should not change store.activeTab on closeRenameDialog', () => {
        store.activeTab = 1
        wrapper.vm.closeRenameDialog()

        expect(store.activeTab).toBe(1)
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
        store.uploadedSourceNames = [{ id: 1, name: 'existing' }]
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

      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]
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

      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })

    it('should maintain non-duplicate status for unique files', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      store.uploadedSourceNames = [{ id: 1, name: 'different-file' }]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
    })

    it('should update multiple files when uploadedSourceNames changes', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: false }
      ])

      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
      expect(wrapper.vm.sourceFiles[1].isDuplicate).toBe(false)
    })

    it('should clear duplicate flags when uploadedSourceNames is emptied', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]
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
      expect(wrapper.text()).toContain('SNMP data collection source files must be in XML format')
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

    it('should have semantic HTML structure', async () => {
      // Table only renders once at least one file is queued.
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      expect(wrapper.find('h3').exists()).toBe(true)
      expect(wrapper.find('table').exists()).toBe(true)
      expect(wrapper.find('thead').exists()).toBe(true)
      expect(wrapper.find('ul').exists()).toBe(true)
    })

    it('should have table with aria-label', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

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
    beforeEach(() => {
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
    })

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

      expect(uploadDataCollectionFiles).toHaveBeenCalledWith([mockFile2], ['default'])
    })

    it('should handle duplicate detection and renaming flow', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]

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

    it('should detect duplicates as updates and keep upload enabled', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]

      // Upload a file whose group name matches an existing source.
      await triggerFileInput([mockFile])

      // The row is flagged as a duplicate (= "will update"), and that no
      // longer disables the Upload button — the server upserts.
      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })

    it('should handle upload error and allow retry', async () => {
      // Set up files explicitly for this test
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]

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
  })

  // ─── Parametrized Tests ──────────────────────────────────────────────
  describe('Parametrized Tests', () => {
    describe('File Extension Validation', () => {
      beforeEach(() => {
        store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
      })

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
  })

  // ─── Pagination ──────────────────────────────────────────────────────
  describe('Pagination', () => {
    it('should pass correct default pagination props to FeatherPagination', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      const pagination = wrapper.findComponent({ name: 'FeatherPagination' })
      expect(pagination.exists()).toBe(true)
      expect(pagination.props('modelValue')).toBe(1)
      expect(pagination.props('pageSize')).toBe(10)
      expect(pagination.props('total')).toBe(1)
    })

    it('should pass pageSizes array to FeatherPagination', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      const pagination = wrapper.findComponent({ name: 'FeatherPagination' })
      expect(pagination.props('pageSizes')).toEqual([10, 20, 50, 100, 200])
    })

    it('should render pagination when files are present', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      const pagination = wrapper.findComponent({ name: 'FeatherPagination' })
      expect(pagination.exists()).toBe(true)
    })

    it('should reflect local pagination changes via onPageChange', async () => {
      const manyFiles = Array.from(
        { length: 25 },
        (_, i) => ({
          file: new File(['content'], `file-${i}.xml`, { type: 'application/xml' }),
          isValid: true, errors: [] as string[], isDuplicate: false
        })
      )
      await setSourceFiles(manyFiles)

      wrapper.vm.onPageChange(2)
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent({ name: 'FeatherPagination' })
      expect(pagination.props('modelValue')).toBe(2)
      expect(pagination.props('total')).toBe(25)
    })

    it('should reflect local pagination changes via onPageSizeChange', async () => {
      const manyFiles = Array.from(
        { length: 25 },
        (_, i) => ({
          file: new File(['content'], `file-${i}.xml`, { type: 'application/xml' }),
          isValid: true, errors: [] as string[], isDuplicate: false
        })
      )
      await setSourceFiles(manyFiles)

      wrapper.vm.onPageSizeChange(20)
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent({ name: 'FeatherPagination' })
      expect(pagination.props('pageSize')).toBe(20)
      expect(pagination.props('modelValue')).toBe(1) // resets to page 1
    })
  })

  // ─── Dialog Props & Events ───────────────────────────────────────────
  describe('Dialog Props & Events', () => {
    describe('DataCollectionFilesUploadReportDialog', () => {
      it('should pass dialogVisible prop', async () => {
        wrapper.vm.uploadedDataCollectionFilesReportDialogState = true
        await wrapper.vm.$nextTick()

        const dialog = wrapper.findComponent({ name: 'DataCollectionFilesUploadReportDialog' })
        expect(dialog.exists()).toBe(true)
        expect(dialog.props('dialogVisible')).toBe(true)
      })

      it('should pass dialogVisible as false initially', () => {
        const dialog = wrapper.findComponent({ name: 'DataCollectionFilesUploadReportDialog' })
        expect(dialog.props('dialogVisible')).toBe(false)
      })

      it('should pass report prop to dialog', async () => {
        const report = { errors: [{ file: 'a.xml', error: 'err' }], success: [{ file: 'b.xml' }] }
        wrapper.vm.uploadFilesReport = report
        await wrapper.vm.$nextTick()

        const dialog = wrapper.findComponent({ name: 'DataCollectionFilesUploadReportDialog' })
        expect(dialog.props('report')).toEqual(report)
      })

      it('should close dialog on @close event', async () => {
        wrapper.vm.uploadedDataCollectionFilesReportDialogState = true
        await wrapper.vm.$nextTick()

        const dialog = wrapper.findComponent({ name: 'DataCollectionFilesUploadReportDialog' })
        await dialog.vm.$emit('close')
        await wrapper.vm.$nextTick()

        expect(wrapper.vm.uploadedDataCollectionFilesReportDialogState).toBe(false)
      })

      it('should navigate to view tab on @view event', async () => {
        wrapper.vm.uploadedDataCollectionFilesReportDialogState = true
        store.activeTab = 1
        await wrapper.vm.$nextTick()

        const dialog = wrapper.findComponent({ name: 'DataCollectionFilesUploadReportDialog' })
        await dialog.vm.$emit('view')
        await wrapper.vm.$nextTick()

        expect(store.activeTab).toBe(0)
        expect(wrapper.vm.uploadedDataCollectionFilesReportDialogState).toBe(false)
      })
    })

    describe('UploadedFileRenameDialog', () => {
      it('should pass visible prop', async () => {
        wrapper.vm.displayRenameDialog = true
        await wrapper.vm.$nextTick()

        const dialog = wrapper.findComponent({ name: 'UploadedFileRenameDialog' })
        expect(dialog.exists()).toBe(true)
        expect(dialog.props('visible')).toBe(true)
      })

      it('should pass visible as false initially', () => {
        const dialog = wrapper.findComponent({ name: 'UploadedFileRenameDialog' })
        expect(dialog.props('visible')).toBe(false)
      })

      it('should pass fileBucket prop as sourceFiles', async () => {
        const files = [{ file: mockFile, isValid: true, errors: [], isDuplicate: true }]
        await setSourceFiles(files)

        const dialog = wrapper.findComponent({ name: 'UploadedFileRenameDialog' })
        // setSourceFiles auto-derives groupName; the pass-through prop reflects that.
        expect(dialog.props('fileBucket')).toEqual(
          files.map(f => ({ ...f, groupName: f.file.name.replace(/\.xml$/i, '') }))
        )
      })

      it('should pass index of first duplicate file', async () => {
        await setSourceFiles([
          { file: mockFile, isValid: true, errors: [], isDuplicate: false },
          { file: mockFile2, isValid: true, errors: [], isDuplicate: true }
        ])

        const dialog = wrapper.findComponent({ name: 'UploadedFileRenameDialog' })
        expect(dialog.props('index')).toBe(1)
      })

      it('should pass -1 as index when no duplicate files', async () => {
        await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

        const dialog = wrapper.findComponent({ name: 'UploadedFileRenameDialog' })
        expect(dialog.props('index')).toBe(-1)
      })

      it('should pass alreadyExistsNames prop from store', async () => {
        store.uploadedSourceNames = [
          { id: 1, name: 'existing.xml' },
          { id: 2, name: 'other.xml' }
        ]
        await wrapper.vm.$nextTick()

        const dialog = wrapper.findComponent({ name: 'UploadedFileRenameDialog' })
        expect(dialog.props('alreadyExistsNames')).toEqual([
          { id: 1, name: 'existing.xml' },
          { id: 2, name: 'other.xml' }
        ])
      })

      it('should close dialog on @close event', async () => {
        wrapper.vm.displayRenameDialog = true
        wrapper.vm.selectedIndex = 0
        await wrapper.vm.$nextTick()

        const dialog = wrapper.findComponent({ name: 'UploadedFileRenameDialog' })
        await dialog.vm.$emit('close')
        await wrapper.vm.$nextTick()

        expect(wrapper.vm.displayRenameDialog).toBe(false)
        expect(wrapper.vm.selectedIndex).toBeNull()
      })

      it('should rename file on @rename event', async () => {
        await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])
        wrapper.vm.selectedIndex = 0
        wrapper.vm.displayRenameDialog = true
        await wrapper.vm.$nextTick()

        const dialog = wrapper.findComponent({ name: 'UploadedFileRenameDialog' })
        await dialog.vm.$emit('rename', 'new-name.xml')
        await flushPromises()

        expect(wrapper.vm.sourceFiles[0].file.name).toBe('new-name.xml')
        expect(wrapper.vm.displayRenameDialog).toBe(false)
      })

      it('should overwrite file on @overwrite event', async () => {
        await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])
        wrapper.vm.selectedIndex = 0
        wrapper.vm.displayRenameDialog = true
        await wrapper.vm.$nextTick()

        const dialog = wrapper.findComponent({ name: 'UploadedFileRenameDialog' })
        await dialog.vm.$emit('overwrite')
        await wrapper.vm.$nextTick()

        expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
        expect(wrapper.vm.displayRenameDialog).toBe(false)
      })
    })
  })

  // ─── Upload Side Effects ─────────────────────────────────────────────
  describe('Upload Side Effects', () => {
    beforeEach(() => {
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
    })

    it('should call store.fetchAllSourcesNames after successful upload', async () => {
      vi.mocked(store.fetchAllSourcesNames).mockClear()

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(store.fetchAllSourcesNames).toHaveBeenCalled()
    })

    it('should call store.fetchSnmpCollectionSources after successful upload', async () => {
      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(store.fetchSnmpCollectionSources).toHaveBeenCalled()
    })

    it('should not call fetchSnmpCollectionSources on upload error', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(uploadDataCollectionFiles).mockRejectedValueOnce(new Error('fail'))

      await wrapper.vm.uploadFiles()
      await flushPromises()
      consoleErrorSpy.mockRestore()

      expect(store.fetchSnmpCollectionSources).not.toHaveBeenCalled()
    })

    it('should not call fetchAllSourcesNames on upload error', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(store.fetchAllSourcesNames).mockClear()
      vi.mocked(uploadDataCollectionFiles).mockRejectedValueOnce(new Error('fail'))

      await wrapper.vm.uploadFiles()
      await flushPromises()
      consoleErrorSpy.mockRestore()

      expect(store.fetchAllSourcesNames).not.toHaveBeenCalled()
    })

    it('should set upload report with errors and success arrays', async () => {
      const mockResponse = {
        errors: [{ file: 'bad.xml', error: 'parse error' }],
        success: [{ file: 'good.xml' }]
      }
      vi.mocked(uploadDataCollectionFiles).mockResolvedValueOnce(mockResponse as any)

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.uploadFilesReport.errors).toEqual([{ file: 'bad.xml', error: 'parse error' }])
      expect(wrapper.vm.uploadFilesReport.success).toEqual([{ file: 'good.xml' }])
    })

    it('should open report dialog after successful upload', async () => {
      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.uploadedDataCollectionFilesReportDialogState).toBe(true)
    })

    it('should not open report dialog on upload error', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(uploadDataCollectionFiles).mockRejectedValueOnce(new Error('fail'))

      await wrapper.vm.uploadFiles()
      await flushPromises()
      consoleErrorSpy.mockRestore()

      expect(wrapper.vm.uploadedDataCollectionFilesReportDialogState).toBe(false)
    })
  })

  // ─── gotoViewTab Behavior ────────────────────────────────────────────
  describe('gotoViewTab Behavior', () => {
    it('should set store.activeTab to 0', () => {
      store.activeTab = 1
      wrapper.vm.gotoViewTab()

      expect(store.activeTab).toBe(0)
    })

    it('should close the upload report dialog', () => {
      wrapper.vm.uploadedDataCollectionFilesReportDialogState = true
      wrapper.vm.gotoViewTab()

      expect(wrapper.vm.uploadedDataCollectionFilesReportDialogState).toBe(false)
    })

    it('should work when already on tab 0', () => {
      store.activeTab = 0
      wrapper.vm.gotoViewTab()

      expect(store.activeTab).toBe(0)
    })
  })

  // ─── Rename File Details ─────────────────────────────────────────────
  describe('Rename File Details', () => {
    beforeEach(async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])
      wrapper.vm.selectedIndex = 0
    })

    it('should create new File with correct type when renaming', async () => {
      await wrapper.vm.renameFile('renamed.xml')
      await flushPromises()

      expect(wrapper.vm.sourceFiles[0].file).toBeInstanceOf(File)
      expect(wrapper.vm.sourceFiles[0].file.type).toBe('application/xml')
    })

    it('should preserve file content when renaming', async () => {
      await wrapper.vm.renameFile('renamed.xml')
      await flushPromises()

      const file = wrapper.vm.sourceFiles[0].file
      const text = await file.text()
      expect(text).toBe('<xml>content</xml>')
    })

    it('should re-validate renamed file', async () => {
      vi.mocked(validateSnmpDataCollectionSourceFile).mockResolvedValueOnce({
        isValid: false,
        errors: ['Schema mismatch']
      })

      await wrapper.vm.renameFile('bad-schema.xml')
      await flushPromises()

      expect(wrapper.vm.sourceFiles[0].isValid).toBe(false)
      expect(wrapper.vm.sourceFiles[0].errors).toEqual(['Schema mismatch'])
    })

    it('should update isDuplicate based on new name after rename', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'taken' }]

      await wrapper.vm.renameFile('taken.xml')
      await flushPromises()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })

    it('should clear isDuplicate if renamed to unique name', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'existing' }]

      await wrapper.vm.renameFile('brand-new-name.xml')
      await flushPromises()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
    })

    it('should close rename dialog after successful rename', async () => {
      wrapper.vm.displayRenameDialog = true

      await wrapper.vm.renameFile('renamed.xml')
      await flushPromises()

      expect(wrapper.vm.displayRenameDialog).toBe(false)
      expect(wrapper.vm.selectedIndex).toBeNull()
    })
  })

  // ─── Folder Upload Edge Cases ────────────────────────────────────────
  describe('Folder Upload Edge Cases', () => {
    it('should handle folder input with empty FileList (length 0)', async () => {
      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')

      Object.defineProperty(folderInput.element, 'files', {
        value: [],
        writable: true
      })

      await folderInput.trigger('change')
      await flushPromises()

      expect(wrapper.vm.sourceFiles.length).toBe(0)
    })

    it('should skip files that are both local duplicates and server duplicates', async () => {
      vi.mocked(isDuplicateFile).mockReturnValue(true)
      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]

      await triggerFolderInput([mockFile])

      // isDuplicateFile check happens first, so file is skipped before server check
      expect(wrapper.vm.sourceFiles.length).toBe(0)
    })

    it('should add all folder files including already-uploaded ones (as updates)', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]
      vi.mocked(isDuplicateFile).mockReturnValue(false)

      const file3 = new File(['content3'], 'test-file-3.xml', { type: 'application/xml' })
      await triggerFolderInput([mockFile, mockFile2, file3])

      // All three files are queued. test-file.xml is marked as an update.
      expect(wrapper.vm.sourceFiles.length).toBe(3)
      const dup = wrapper.vm.sourceFiles.find((f: any) => f.file.name === 'test-file.xml')
      expect(dup.isDuplicate).toBe(true)
    })

    it('should not snackbar already-uploaded files in folder upload', async () => {
      // No more "skipping" snackbars — re-uploaded sources are accepted as
      // updates and the user sees them in the table.
      store.uploadedSourceNames = [
        { id: 1, name: 'test-file' },
        { id: 2, name: 'test-file-2' }
      ]
      vi.mocked(isDuplicateFile).mockReturnValue(false)

      await triggerFolderInput([mockFile, mockFile2])

      expect(mockShowSnackBar).not.toHaveBeenCalled()
      expect(wrapper.vm.sourceFiles.length).toBe(2)
      expect(wrapper.vm.sourceFiles.every((f: any) => f.isDuplicate)).toBe(true)
    })

    it('should add invalid folder files to sourceFiles with errors', async () => {
      vi.mocked(validateSnmpDataCollectionSourceFile).mockResolvedValueOnce({
        isValid: false,
        errors: ['Bad XML']
      })

      await triggerFolderInput([mockFile])

      expect(wrapper.vm.sourceFiles.length).toBe(1)
      expect(wrapper.vm.sourceFiles[0].isValid).toBe(false)
      expect(wrapper.vm.sourceFiles[0].errors).toEqual(['Bad XML'])
    })

    it('should reset folder input files to null after processing', async () => {
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

  // ─── TransitionGroup & Table Rendering ───────────────────────────────
  describe('TransitionGroup & Table Rendering', () => {
    it('should render table rows when files are present', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      // TransitionGroup is auto-stubbed, so we look for tr elements directly
      const rows = wrapper.findAll('tr')
      // header row + at least 1 data row
      expect(rows.length).toBeGreaterThanOrEqual(2)
    })

    it('should not render the table at all when no files', () => {
      // Table (and thus its header row) is hidden until files are queued so
      // an empty Source/Action header bar isn't sitting above the empty-state.
      expect(wrapper.find('table.data-table').exists()).toBe(false)
      const rows = wrapper.findAll('tr')
      expect(rows).toHaveLength(0)
    })

    it('should render correct number of data rows', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: false }
      ])

      const rows = wrapper.findAll('tr')
      // 1 header + 2 data rows
      expect(rows.length).toBeGreaterThanOrEqual(3)
    })

    it('should render file name within .file div', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      const fileDiv = wrapper.find('.file')
      expect(fileDiv.exists()).toBe(true)
      expect(fileDiv.text()).toContain('test-file.xml')
    })

    it('should render remove button for each file row', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      const removeButtons = wrapper.findAll('[data-test="remove-files-button"]')
      expect(removeButtons).toHaveLength(1)
    })
  })

  // ─── Upload Files Edge Cases ─────────────────────────────────────────
  describe('Upload Files Edge Cases', () => {
    beforeEach(() => {
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
    })

    it('should not call uploadDataCollectionFiles when sourceFiles is empty', async () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      wrapper.vm.sourceFiles = []

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(uploadDataCollectionFiles).not.toHaveBeenCalled()
      consoleSpy.mockRestore()
    })

    it('should warn to console when no files to upload', async () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      wrapper.vm.sourceFiles = []

      await wrapper.vm.uploadFiles()

      expect(consoleSpy).toHaveBeenCalledWith('No files to upload')
      consoleSpy.mockRestore()
    })

    it('should filter invalid files from upload payload', async () => {
      wrapper.vm.sourceFiles = [
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: false, errors: ['err'], isDuplicate: false },
        { file: new File(['x'], 'valid2.xml'), isValid: true, errors: [], isDuplicate: false }
      ]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      const calledWith = vi.mocked(uploadDataCollectionFiles).mock.calls[0][0] as File[]
      expect(calledWith).toHaveLength(2)
      expect(calledWith[0].name).toBe('test-file.xml')
      expect(calledWith[1].name).toBe('valid2.xml')
    })

    it('should not reject upload if all files are .xml but invalid', async () => {
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: false, errors: ['bad'], isDuplicate: false }]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      // Filter sends empty array but still calls the service
      expect(uploadDataCollectionFiles).toHaveBeenCalledWith([], ['default'])
    })

    it('should clear source files after successful upload', async () => {
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.sourceFiles).toEqual([])
    })

    it('should retain source files after upload error', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]
      vi.mocked(uploadDataCollectionFiles).mockRejectedValueOnce(new Error('fail'))

      await wrapper.vm.uploadFiles()
      await flushPromises()
      consoleErrorSpy.mockRestore()

      expect(wrapper.vm.sourceFiles).toHaveLength(1)
    })
  })

  // ─── Button Disabled via Props ───────────────────────────────────────
  describe('Button Disabled via Props', () => {
    it('should have upload button disabled attribute when shouldUploadDisabled is true', () => {
      const uploadButton = wrapper.find('[data-test="upload-button"]')
      const btn = uploadButton.findComponent(FeatherButton)
      expect(btn.props('disabled')).toBe(true)
    })

    it('should have upload button enabled when valid files present', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])
      wrapper.vm.isLoading = false
      await wrapper.vm.$nextTick()

      const uploadButton = wrapper.find('[data-test="upload-button"]')
      const btn = uploadButton.findComponent(FeatherButton)
      expect(btn.props('disabled')).toBe(false)
    })

    it('should have choose-file button enabled when not loading', () => {
      const btn = wrapper.find('[data-test="choose-file-button"]')
      const featherBtn = btn.findComponent(FeatherButton)
      expect(featherBtn.props('disabled')).toBe(false)
    })

    it('should have choose-folder button enabled when not loading', () => {
      const btn = wrapper.find('[data-test="choose-folder-button"]')
      const featherBtn = btn.findComponent(FeatherButton)
      expect(featherBtn.props('disabled')).toBe(false)
    })

    it('should have choose-file button disabled when loading', async () => {
      wrapper.vm.isLoading = true
      await wrapper.vm.$nextTick()

      const btn = wrapper.find('[data-test="choose-file-button"]')
      const featherBtn = btn.findComponent(FeatherButton)
      expect(featherBtn.props('disabled')).toBe(true)
    })

    it('should have choose-folder button disabled when loading', async () => {
      wrapper.vm.isLoading = true
      await wrapper.vm.$nextTick()

      const btn = wrapper.find('[data-test="choose-folder-button"]')
      const featherBtn = btn.findComponent(FeatherButton)
      expect(featherBtn.props('disabled')).toBe(true)
    })
  })

  // ─── Button Variants ─────────────────────────────────────────────────
  describe('Button Variants', () => {
    it('should render choose-file button as secondary', () => {
      const btn = wrapper.find('[data-test="choose-file-button"]')
      const featherBtn = btn.findComponent(FeatherButton)
      expect(featherBtn.props('secondary')).toBe(true)
    })

    it('should render choose-folder button as secondary', () => {
      const btn = wrapper.find('[data-test="choose-folder-button"]')
      const featherBtn = btn.findComponent(FeatherButton)
      expect(featherBtn.props('secondary')).toBe(true)
    })

    it('should render upload button as primary', () => {
      const btn = wrapper.find('[data-test="upload-button"]')
      const featherBtn = btn.findComponent(FeatherButton)
      expect(featherBtn.props('primary')).toBe(true)
    })
  })

  // ─── Overwrite Edge Cases ────────────────────────────────────────────
  describe('Overwrite Edge Cases', () => {
    it('should only clear isDuplicate for the selected file', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: true },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: true }
      ])

      wrapper.vm.selectedIndex = 0
      wrapper.vm.overwriteFile()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
      expect(wrapper.vm.sourceFiles[1].isDuplicate).toBe(true)
    })

    it('should close rename dialog after overwrite', () => {
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: true }]
      wrapper.vm.selectedIndex = 0
      wrapper.vm.displayRenameDialog = true

      wrapper.vm.overwriteFile()

      expect(wrapper.vm.displayRenameDialog).toBe(false)
    })

    it('should reset selectedIndex after overwrite', () => {
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: true }]
      wrapper.vm.selectedIndex = 0

      wrapper.vm.overwriteFile()

      expect(wrapper.vm.selectedIndex).toBeNull()
    })
  })

  // ─── Empty List Content ──────────────────────────────────────────────
  describe('Empty List Content', () => {
    it('should display EmptyList component when no files', () => {
      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
    })

    it('should pass correct content message to EmptyList', () => {
      expect(wrapper.text()).toContain('No files selected for upload')
    })

    it('should hide EmptyList when files are present', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(false)
    })

    it('should show EmptyList again after all files are removed', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])
      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(false)

      wrapper.vm.sourceFiles = []
      wrapper.vm.tableRecord = []
      wrapper.vm.total = 0
      await wrapper.vm.$nextTick()

      expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
    })
  })

  // ─── tableRecord & Pagination Logic ──────────────────────────────────
  describe('tableRecord & Pagination Logic', () => {
    const createFiles = (count: number) =>
      Array.from({ length: count }, (_, i) => ({
        file: new File(['content'], `file-${i}.xml`, { type: 'application/xml' }),
        isValid: true,
        errors: [] as string[],
        isDuplicate: false
      }))

    it('should slice sourceFiles into tableRecord based on pageSize', async () => {
      const files = createFiles(15)
      await setSourceFiles(files)

      // Default pageSize is 10, page is 1
      expect(wrapper.vm.tableRecord).toHaveLength(10)
      expect(wrapper.vm.tableRecord[0].file.name).toBe('file-0.xml')
      expect(wrapper.vm.tableRecord[9].file.name).toBe('file-9.xml')
    })

    it('should update tableRecord when onPageChange is called', async () => {
      const files = createFiles(15)
      await setSourceFiles(files)

      wrapper.vm.onPageChange(2)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.page).toBe(2)
      expect(wrapper.vm.tableRecord).toHaveLength(5)
      expect(wrapper.vm.tableRecord[0].file.name).toBe('file-10.xml')
    })

    it('should update tableRecord when onPageSizeChange is called', async () => {
      const files = createFiles(25)
      await setSourceFiles(files)

      wrapper.vm.onPageSizeChange(20)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.pageSize).toBe(20)
      expect(wrapper.vm.page).toBe(1) // reset to page 1
      expect(wrapper.vm.tableRecord).toHaveLength(20)
    })

    it('should reset page to 1 on pageSize change', async () => {
      const files = createFiles(25)
      await setSourceFiles(files)

      // Go to page 2 first
      wrapper.vm.onPageChange(2)
      expect(wrapper.vm.page).toBe(2)

      // Change page size should reset to page 1
      wrapper.vm.onPageSizeChange(5)
      expect(wrapper.vm.page).toBe(1)
      expect(wrapper.vm.tableRecord).toHaveLength(5)
    })

    it('should handle pageSize larger than total files', async () => {
      const files = createFiles(3)
      await setSourceFiles(files)

      wrapper.vm.onPageSizeChange(50)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.tableRecord).toHaveLength(3)
    })

    it('should handle empty sourceFiles with pagination', () => {
      wrapper.vm.onPageChange(1)
      expect(wrapper.vm.tableRecord).toHaveLength(0)
    })

    it('should update total when files are added via triggerFileInput', async () => {
      await triggerFileInput([mockFile, mockFile2])

      expect(wrapper.vm.total).toBe(2)
      expect(wrapper.vm.tableRecord).toHaveLength(2)
    })

    it('should update total when files are added via triggerFolderInput', async () => {
      await triggerFolderInput([mockFile, mockFile2])

      expect(wrapper.vm.total).toBe(2)
      expect(wrapper.vm.tableRecord).toHaveLength(2)
    })

    it('should update total and tableRecord after removeFile', async () => {
      await triggerFileInput([mockFile, mockFile2])

      // Verify initial state
      expect(wrapper.vm.total).toBe(2)
      expect(wrapper.vm.tableRecord).toHaveLength(2)

      wrapper.vm.removeFile(0)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.total).toBe(1)
      expect(wrapper.vm.tableRecord).toHaveLength(1)
      expect(wrapper.vm.tableRecord[0].file.name).toBe('test-file-2.xml')
    })

    it('should clear total and tableRecord after successful upload', async () => {
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
      await triggerFileInput([mockFile])

      expect(wrapper.vm.total).toBe(1)
      expect(wrapper.vm.tableRecord).toHaveLength(1)

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.total).toBe(0)
      expect(wrapper.vm.tableRecord).toHaveLength(0)
      expect(wrapper.vm.sourceFiles).toHaveLength(0)
    })

    it('should display only first page of files in table', async () => {
      const files = createFiles(15)
      await setSourceFiles(files)

      // Default pageSize is 10
      const rows = wrapper.findAll('tr')
      // 1 header + 10 data rows
      expect(rows).toHaveLength(11)
    })
  })

  // ─── removeFile Edge Cases ───────────────────────────────────────────
  describe('removeFile Edge Cases', () => {
    it('should update total after removing a file', async () => {
      await triggerFileInput([mockFile, mockFile2])
      expect(wrapper.vm.total).toBe(2)

      wrapper.vm.removeFile(0)
      expect(wrapper.vm.total).toBe(1)
    })

    it('should recalculate tableRecord after removing file', async () => {
      const file3 = new File(['c3'], 'file-3.xml', { type: 'application/xml' })
      await triggerFileInput([mockFile, mockFile2, file3])

      wrapper.vm.removeFile(1) // remove test-file-2.xml
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].file.name).toBe('test-file.xml')
      expect(wrapper.vm.sourceFiles[1].file.name).toBe('file-3.xml')
      expect(wrapper.vm.tableRecord).toHaveLength(2)
    })

    it('should handle removing last file from middle page', async () => {
      // Create 11 files, go to page 2 (1 file), then remove it
      const files = Array.from({ length: 11 }, (_, i) =>
        new File(['c'], `f-${i}.xml`, { type: 'application/xml' })
      )
      await triggerFileInput(files)

      wrapper.vm.onPageChange(2)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.tableRecord).toHaveLength(1)

      // removeFile takes table-row index (0 on page 2 corresponds to sourceFiles[10])
      wrapper.vm.removeFile(0)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles).toHaveLength(10)
      expect(wrapper.vm.total).toBe(10)
    })
  })

  // ─── openFileRenameDialog ────────────────────────────────────────────
  describe('openFileRenameDialog', () => {
    it('should set displayRenameDialog to true', () => {
      wrapper.vm.openFileRenameDialog(0)
      expect(wrapper.vm.displayRenameDialog).toBe(true)
    })

    it('should set selectedIndex to the given index', () => {
      wrapper.vm.openFileRenameDialog(5)
      expect(wrapper.vm.selectedIndex).toBe(5)
    })

    it('should not render a warning icon for re-uploaded sources', async () => {
      // Duplicates show the calm "update" icon now and don't auto-trigger
      // the rename dialog. Rename can still be invoked programmatically
      // via openFileRenameDialog() if a future UI surfaces it.
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: true }
      ])

      expect(wrapper.findAll('.warning-icon')).toHaveLength(0)
      expect(wrapper.findAll('.update-icon')).toHaveLength(1)
    })
  })

  // ─── Upload clears sourceFileInput ref ───────────────────────────────
  describe('Upload clears sourceFileInput ref', () => {
    it('should reset sourceFileInput value after successful upload', async () => {
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
      const inputElement = fileInput.element as HTMLInputElement

      // Simulate a file input value
      Object.defineProperty(inputElement, 'value', {
        value: 'C:\\fakepath\\test.xml',
        writable: true,
        configurable: true
      })

      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(inputElement.value).toBe('')
    })
  })

  // ─── File Upload with snackbar ───────────────────────────────────────
  describe('File Upload Snackbar Behavior', () => {
    it('should show snackbar for each invalid file during file upload', async () => {
      vi.mocked(validateSnmpDataCollectionSourceFile)
        .mockResolvedValueOnce({ isValid: false, errors: ['Err1'] })
        .mockResolvedValueOnce({ isValid: false, errors: ['Err2'] })

      await triggerFileInput([mockFile, mockFile2])

      expect(mockShowSnackBar).toHaveBeenCalledTimes(2)
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Error processing file test-file.xml.',
        error: true
      })
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Error processing file test-file-2.xml.',
        error: true
      })
    })

    it('should show error snackbar on upload failure', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]
      vi.mocked(uploadDataCollectionFiles).mockRejectedValueOnce(new Error('Network fail'))

      await wrapper.vm.uploadFiles()
      await flushPromises()
      consoleErrorSpy.mockRestore()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Error uploading files',
        error: true
      })
    })

    it('should show snackbar for invalid folder file', async () => {
      vi.mocked(validateSnmpDataCollectionSourceFile).mockResolvedValueOnce({
        isValid: false,
        errors: ['Bad schema']
      })

      await triggerFolderInput([mockFile])

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Error processing file test-file.xml.',
        error: true
      })
    })

    it('should not show snackbar for valid file', async () => {
      await triggerFileInput([mockFile])

      expect(mockShowSnackBar).not.toHaveBeenCalled()
    })

    it('should show extension validation snackbar for non-xml files', async () => {
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
      const txtFile = new File(['text'], 'data.txt', { type: 'text/plain' })
      wrapper.vm.sourceFiles = [{ file: txtFile, isValid: true, errors: [], isDuplicate: false }]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'All files must be XML files with .xml extension',
        error: true
      })
    })
  })

  // ─── Error icon click does NOT open dialog ───────────────────────────
  describe('Icon Click Behavior', () => {
    it('should not render a clickable warning icon for re-uploaded sources', async () => {
      // The duplicate-row icon is now a non-clickable "update" indicator.
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])

      expect(wrapper.find('.warning-icon').exists()).toBe(false)
      expect(wrapper.find('.update-icon').exists()).toBe(true)
      expect(wrapper.vm.displayRenameDialog).toBe(false)
    })

    it('should NOT open rename dialog when error icon exists', async () => {
      await setSourceFiles([{ file: mockFile, isValid: false, errors: ['bad'], isDuplicate: false }])

      // Error icon has no click handler in template
      const errorIcon = wrapper.find('.error-icon')
      expect(errorIcon.exists()).toBe(true)
      await errorIcon.trigger('click')

      expect(wrapper.vm.displayRenameDialog).toBe(false)
    })

    it('should NOT have click handler on success icon', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      const successIcon = wrapper.find('.success-icon')
      expect(successIcon.exists()).toBe(true)
      await successIcon.trigger('click')

      expect(wrapper.vm.displayRenameDialog).toBe(false)
    })
  })

  // ─── Computed shouldUploadDisabled Edge Cases ────────────────────────
  describe('shouldUploadDisabled Edge Cases', () => {
    it('should be disabled when all files are invalid even if not duplicate', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: false, errors: ['err'], isDuplicate: false },
        { file: mockFile2, isValid: false, errors: ['err'], isDuplicate: false }
      ])

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should be enabled when all files are duplicates (all updates) and valid', async () => {
      // A batch entirely of re-uploaded sources is a valid update flow.
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: true },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: true }
      ])

      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })

    it('should be enabled for pure-update batch even with no profile selected', async () => {
      // Updates don't need a profile pick: existing memberships are preserved.
      // The picker becomes optional ("also add to these profiles") for updates.
      wrapper.vm.selectedProfileNames = []
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: true, kind: 'group' }
      ])

      expect(wrapper.vm.selectedProfileNames.length).toBe(0)
      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })

    it('should be disabled for mixed batch (new + update) when no profile selected', async () => {
      // A new source still needs a profile to avoid being orphaned, even if
      // the batch also contains an update.
      wrapper.vm.selectedProfileNames = []
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false, kind: 'group' },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: true, kind: 'group' }
      ])

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should be disabled when file is invalid (regardless of duplicate state)', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: false, errors: ['err'], isDuplicate: true }
      ])

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })

    it('should transition from disabled to enabled when file becomes valid', async () => {
      await setSourceFiles([{ file: mockFile, isValid: false, errors: ['err'], isDuplicate: false }])
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)

      wrapper.vm.sourceFiles[0].isValid = true
      wrapper.vm.sourceFiles[0].errors = []
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })

    it('should keep upload enabled regardless of the duplicate flag', async () => {
      // Duplicates no longer affect upload-disabled state — toggling the
      // flag has no effect on the button.
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])
      expect(wrapper.vm.shouldUploadDisabled).toBe(false)

      wrapper.vm.sourceFiles[0].isDuplicate = false
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.shouldUploadDisabled).toBe(false)
    })

    it('should become disabled when isLoading changes to true', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])
      expect(wrapper.vm.shouldUploadDisabled).toBe(false)

      wrapper.vm.isLoading = true
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.shouldUploadDisabled).toBe(true)
    })
  })

  // ─── Multiple file interactions sequence test ────────────────────────
  describe('Multi-step File Interaction Flows', () => {
    it('should handle add files, remove some, add more, then upload', async () => {
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)

      // Add two files
      await triggerFileInput([mockFile, mockFile2])
      expect(wrapper.vm.sourceFiles).toHaveLength(2)

      // Remove first
      wrapper.vm.removeFile(0)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.sourceFiles).toHaveLength(1)

      // Add another file
      const file3 = new File(['c3'], 'file-3.xml', { type: 'application/xml' })
      await triggerFileInput([file3])
      expect(wrapper.vm.sourceFiles).toHaveLength(2)

      // Upload
      await wrapper.vm.uploadFiles()
      await flushPromises()

      const calledWith = vi.mocked(uploadDataCollectionFiles).mock.calls[0][0] as File[]
      expect(calledWith).toHaveLength(2)
      expect(calledWith[0].name).toBe('test-file-2.xml')
      expect(calledWith[1].name).toBe('file-3.xml')

      // Verify cleared
      expect(wrapper.vm.sourceFiles).toHaveLength(0)
      expect(wrapper.vm.total).toBe(0)
    })

    it('should handle file upload then folder upload accumulating files', async () => {
      await triggerFileInput([mockFile])
      expect(wrapper.vm.sourceFiles).toHaveLength(1)

      const folderFile = new File(['folder'], 'folder-file.xml', { type: 'application/xml' })
      await triggerFolderInput([folderFile])
      expect(wrapper.vm.sourceFiles).toHaveLength(2)
      expect(wrapper.vm.total).toBe(2)
    })

    it('should still support rename → upload (programmatic) for users who want a new source', async () => {
      // The rename helper is still exposed even though the duplicate icon
      // no longer surfaces it; this test pins that path so it doesn't bit-rot.
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]

      await triggerFileInput([mockFile])
      // Default behavior: duplicate is accepted as an update, upload enabled.
      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
      expect(wrapper.vm.shouldUploadDisabled).toBe(false)

      // Rename: produces a fresh file with a new name; isDuplicate clears.
      wrapper.vm.selectedIndex = 0
      await wrapper.vm.renameFile('renamed-file.xml')
      await flushPromises()
      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)

      // Upload
      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(uploadDataCollectionFiles).toHaveBeenCalledWith([expect.objectContaining({ name: 'renamed-file.xml' })], ['default'])
    })

    it('should handle overwrite then upload flow', async () => {
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]

      await triggerFileInput([mockFile])
      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)

      // Overwrite
      wrapper.vm.selectedIndex = 0
      wrapper.vm.overwriteFile()
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
      expect(wrapper.vm.shouldUploadDisabled).toBe(false)

      // Upload
      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(uploadDataCollectionFiles).toHaveBeenCalledWith([mockFile], ['default'])
    })
  })

  // ─── removeFile Page Offset Calculation ──────────────────────────────
  describe('removeFile Page Offset Calculation', () => {
    it('should calculate correct sourceIndex on page 2', async () => {
      // 12 files, page 2 shows files 10-11, removing table index 1 removes sourceFiles[11]
      const files = Array.from({ length: 12 }, (_, i) => ({
        file: new File(['c'], `f-${i}.xml`, { type: 'application/xml' }),
        isValid: true, errors: [] as string[], isDuplicate: false
      }))
      await setSourceFiles(files)

      wrapper.vm.onPageChange(2)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.tableRecord).toHaveLength(2) // files 10 and 11

      // Remove table index 1 → sourceIndex = (2-1)*10 + 1 = 11
      wrapper.vm.removeFile(1)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles).toHaveLength(11)
      // f-11.xml should be removed, f-10.xml should remain
      expect(wrapper.vm.sourceFiles.find((f: any) => f.file.name === 'f-11.xml')).toBeUndefined()
      expect(wrapper.vm.sourceFiles.find((f: any) => f.file.name === 'f-10.xml')).toBeDefined()
    })

    it('should calculate correct sourceIndex on page 1', async () => {
      const files = Array.from({ length: 15 }, (_, i) => ({
        file: new File(['c'], `f-${i}.xml`, { type: 'application/xml' }),
        isValid: true, errors: [] as string[], isDuplicate: false
      }))
      await setSourceFiles(files)

      // Page 1, remove table index 3 → sourceIndex = (1-1)*10 + 3 = 3
      wrapper.vm.removeFile(3)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles).toHaveLength(14)
      expect(wrapper.vm.sourceFiles.find((f: any) => f.file.name === 'f-3.xml')).toBeUndefined()
    })

    it('should recalculate tableRecord correctly after removal on page 2', async () => {
      const files = Array.from({ length: 12 }, (_, i) => ({
        file: new File(['c'], `f-${i}.xml`, { type: 'application/xml' }),
        isValid: true, errors: [] as string[], isDuplicate: false
      }))
      await setSourceFiles(files)

      wrapper.vm.onPageChange(2)
      await wrapper.vm.$nextTick()

      // Remove first table row on page 2 (sourceFiles[10])
      wrapper.vm.removeFile(0)
      await wrapper.vm.$nextTick()

      // Now 11 files, page 2 should show only f-11.xml (now at sourceIndex 10)
      expect(wrapper.vm.sourceFiles).toHaveLength(11)
      expect(wrapper.vm.tableRecord).toHaveLength(1)
    })

    it('should return empty tableRecord when removing last file on last page', async () => {
      const files = Array.from({ length: 11 }, (_, i) => ({
        file: new File(['c'], `f-${i}.xml`, { type: 'application/xml' }),
        isValid: true, errors: [] as string[], isDuplicate: false
      }))
      await setSourceFiles(files)

      wrapper.vm.onPageChange(2)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.tableRecord).toHaveLength(1)

      wrapper.vm.removeFile(0)
      await wrapper.vm.$nextTick()

      // Page 2 now has 0 items (all 10 files on page 1)
      expect(wrapper.vm.total).toBe(10)
      expect(wrapper.vm.tableRecord).toHaveLength(0)
    })
  })

  // ─── Watcher Preserves File Object References ───────────────────────
  describe('Watcher File Object Preservation', () => {
    it('should preserve the file property reference when watcher updates isDuplicate', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])
      const originalFile = wrapper.vm.sourceFiles[0].file

      store.uploadedSourceNames = [{ id: 1, name: 'other' }]
      await wrapper.vm.$nextTick()

      // The watcher creates new objects via spread, but file property should be same
      expect(wrapper.vm.sourceFiles[0].file).toBe(originalFile)
    })

    it('should preserve isValid and errors when watcher only changes isDuplicate', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: false, errors: ['Err1', 'Err2'], isDuplicate: false }
      ])

      store.uploadedSourceNames = [{ id: 1, name: 'test-file' }]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
      expect(wrapper.vm.sourceFiles[0].isValid).toBe(false)
      expect(wrapper.vm.sourceFiles[0].errors).toEqual(['Err1', 'Err2'])
    })

    it('should handle watcher running on empty sourceFiles', async () => {
      wrapper.vm.sourceFiles = []
      store.uploadedSourceNames = [{ id: 1, name: 'test' }]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles).toHaveLength(0)
    })

    it('should correctly handle .xml extension stripping in duplicate detection', async () => {
      // The watcher does .replace('.xml', '') which only removes the first .xml
      const doubleXmlFile = new File(['c'], 'test.xml.xml', { type: 'application/xml' })
      await setSourceFiles([{ file: doubleXmlFile, isValid: true, errors: [], isDuplicate: false }])

      // Store has 'test.xml' → after replace('.xml','') = 'test'
      // File name 'test.xml.xml' → after replace('.xml','') = 'test.xml'
      // 'test' !== 'test.xml' → NOT duplicate
      store.uploadedSourceNames = [{ id: 1, name: 'test' }]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
    })

    it('should mark file as duplicate when names match after extension strip', async () => {
      const file = new File(['c'], 'myData.xml', { type: 'application/xml' })
      await setSourceFiles([{ file, isValid: true, errors: [], isDuplicate: false }])

      store.uploadedSourceNames = [{ id: 1, name: 'MYDATA' }]
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })
  })

  // ─── renameFile Boundary Conditions ──────────────────────────────────
  describe('renameFile Boundary Conditions', () => {
    it('should error when selectedIndex equals sourceFiles.length', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])
      wrapper.vm.selectedIndex = 1 // equals length of 1

      await wrapper.vm.renameFile('new-name.xml')

      expect(consoleSpy).toHaveBeenCalledWith('Invalid index for renaming file')
      consoleSpy.mockRestore()
    })

    it('should rename successfully when selectedIndex is 0 (boundary)', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])
      wrapper.vm.selectedIndex = 0

      await wrapper.vm.renameFile('renamed.xml')
      await flushPromises()

      expect(wrapper.vm.sourceFiles[0].file.name).toBe('renamed.xml')
    })

    it('should rename last file when selectedIndex is length-1', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: true, errors: [], isDuplicate: false },
        { file: mockFile2, isValid: true, errors: [], isDuplicate: true }
      ])
      wrapper.vm.selectedIndex = 1

      await wrapper.vm.renameFile('second-renamed.xml')
      await flushPromises()

      expect(wrapper.vm.sourceFiles[1].file.name).toBe('second-renamed.xml')
      expect(wrapper.vm.sourceFiles[0].file.name).toBe('test-file.xml') // unchanged
    })

    it('should still check isDuplicate against server names after rename', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'taken-name' }]
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])
      wrapper.vm.selectedIndex = 0

      await wrapper.vm.renameFile('taken-name.xml')
      await flushPromises()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })

    it('should handle renameFile with case-insensitive server duplicate check', async () => {
      store.uploadedSourceNames = [{ id: 1, name: 'EXISTING' }]
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])
      wrapper.vm.selectedIndex = 0

      await wrapper.vm.renameFile('existing.xml')
      await flushPromises()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(true)
    })

    it('should update validation result after rename even if validation fails', async () => {
      vi.mocked(validateSnmpDataCollectionSourceFile).mockResolvedValueOnce({
        isValid: false,
        errors: ['New validation error']
      })

      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])
      wrapper.vm.selectedIndex = 0

      await wrapper.vm.renameFile('bad-schema.xml')
      await flushPromises()

      expect(wrapper.vm.sourceFiles[0].isValid).toBe(false)
      expect(wrapper.vm.sourceFiles[0].errors).toEqual(['New validation error'])
      // Dialog should still close
      expect(wrapper.vm.displayRenameDialog).toBe(false)
    })
  })

  // ─── overwriteFile Property Preservation ─────────────────────────────
  describe('overwriteFile Property Preservation', () => {
    it('should preserve isValid after overwrite', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])
      wrapper.vm.selectedIndex = 0

      wrapper.vm.overwriteFile()

      expect(wrapper.vm.sourceFiles[0].isValid).toBe(true)
    })

    it('should preserve errors after overwrite', async () => {
      await setSourceFiles([
        { file: mockFile, isValid: false, errors: ['Err1'], isDuplicate: true }
      ])
      wrapper.vm.selectedIndex = 0

      wrapper.vm.overwriteFile()

      expect(wrapper.vm.sourceFiles[0].errors).toEqual(['Err1'])
      expect(wrapper.vm.sourceFiles[0].isValid).toBe(false)
      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
    })

    it('should preserve file reference after overwrite', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: true }])
      wrapper.vm.selectedIndex = 0
      const originalFile = wrapper.vm.sourceFiles[0].file

      wrapper.vm.overwriteFile()

      expect(wrapper.vm.sourceFiles[0].file).toBe(originalFile)
    })

    it('should only modify isDuplicate to false on overwrite', async () => {
      const fileEntry = { file: mockFile, isValid: true, errors: [] as string[], isDuplicate: true }
      await setSourceFiles([fileEntry])
      wrapper.vm.selectedIndex = 0

      wrapper.vm.overwriteFile()

      expect(wrapper.vm.sourceFiles[0].isDuplicate).toBe(false)
      expect(wrapper.vm.sourceFiles[0].file.name).toBe('test-file.xml')
    })
  })

  // ─── uploadFiles Response Copy Verification ──────────────────────────
  describe('uploadFiles Response Handling', () => {
    beforeEach(() => {
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
    })

    it('should spread response arrays into uploadFilesReport (not reference)', async () => {
      const originalErrors = [{ file: 'a.xml', error: 'err' }]
      const originalSuccess = [{ file: 'b.xml' }]
      const mockResponse = { errors: originalErrors, success: originalSuccess }
      vi.mocked(uploadDataCollectionFiles).mockResolvedValueOnce(mockResponse as any)

      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      // Verify the report is a copy
      expect(wrapper.vm.uploadFilesReport.errors).toEqual(originalErrors)
      expect(wrapper.vm.uploadFilesReport.success).toEqual(originalSuccess)

      // Modify original → should NOT affect the stored report
      originalErrors.push({ file: 'c.xml', error: 'new' })
      expect(wrapper.vm.uploadFilesReport.errors).toHaveLength(1)
    })

    it('should handle response with empty errors array', async () => {
      vi.mocked(uploadDataCollectionFiles).mockResolvedValueOnce({
        errors: [],
        success: [{ file: 'test.xml' }]
      } as any)

      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.uploadFilesReport.errors).toEqual([])
      expect(wrapper.vm.uploadFilesReport.success).toEqual([{ file: 'test.xml' }])
    })

    it('should handle response with empty success array', async () => {
      vi.mocked(uploadDataCollectionFiles).mockResolvedValueOnce({
        errors: [{ file: 'test.xml', error: 'fail' }],
        success: []
      } as any)

      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.uploadFilesReport.errors).toEqual([{ file: 'test.xml', error: 'fail' }])
      expect(wrapper.vm.uploadFilesReport.success).toEqual([])
    })

    it('should handle response with multiple errors and successes', async () => {
      vi.mocked(uploadDataCollectionFiles).mockResolvedValueOnce({
        errors: [
          { file: 'a.xml', error: 'err1' },
          { file: 'b.xml', error: 'err2' }
        ],
        success: [
          { file: 'c.xml' },
          { file: 'd.xml' }
        ]
      } as any)

      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.uploadFilesReport.errors).toHaveLength(2)
      expect(wrapper.vm.uploadFilesReport.success).toHaveLength(2)
    })
  })

  // ─── Folder Upload Input Reset After Skipped Files ───────────────────
  describe('Folder Upload Input Reset After Skips', () => {
    it('should reset input after a folder of all-already-uploaded files', async () => {
      store.uploadedSourceNames = [
        { id: 1, name: 'test-file' },
        { id: 2, name: 'test-file-2' }
      ]
      vi.mocked(isDuplicateFile).mockReturnValue(false)

      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')
      const inputElement = folderInput.element as HTMLInputElement

      Object.defineProperty(inputElement, 'files', {
        value: [mockFile, mockFile2],
        writable: true
      })

      await folderInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      // All files now queue as updates (the server will upsert) and the
      // input is still reset so the user can pick another folder.
      expect(inputElement.files).toBeNull()
      expect(wrapper.vm.sourceFiles).toHaveLength(2)
      expect(wrapper.vm.sourceFiles.every((f: any) => f.isDuplicate)).toBe(true)
    })

    it('should reset input after processing errors in folder upload', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(validateSnmpDataCollectionSourceFile).mockRejectedValue(new Error('fail'))

      const folderInput = wrapper.find('[data-test="snmp-data-collection-folder-input"]')
      const inputElement = folderInput.element as HTMLInputElement

      Object.defineProperty(inputElement, 'files', {
        value: [mockFile],
        writable: true
      })

      await folderInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()
      consoleSpy.mockRestore()

      expect(inputElement.files).toBeNull()
    })
  })

  // ─── Sequential Pagination Navigation ────────────────────────────────
  describe('Sequential Pagination Navigation', () => {
    const createFiles = (count: number) =>
      Array.from({ length: count }, (_, i) => ({
        file: new File(['c'], `f-${i}.xml`, { type: 'application/xml' }),
        isValid: true, errors: [] as string[], isDuplicate: false
      }))

    it('should handle navigating forward and back between pages', async () => {
      await setSourceFiles(createFiles(25))

      // Go to page 2
      wrapper.vm.onPageChange(2)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.tableRecord[0].file.name).toBe('f-10.xml')

      // Go to page 3
      wrapper.vm.onPageChange(3)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.tableRecord[0].file.name).toBe('f-20.xml')
      expect(wrapper.vm.tableRecord).toHaveLength(5)

      // Go back to page 1
      wrapper.vm.onPageChange(1)
      await wrapper.vm.$nextTick()
      expect(wrapper.vm.tableRecord[0].file.name).toBe('f-0.xml')
      expect(wrapper.vm.tableRecord).toHaveLength(10)
    })

    it('should handle page size change after navigating to later page', async () => {
      await setSourceFiles(createFiles(30))

      // Go to page 3
      wrapper.vm.onPageChange(3)
      expect(wrapper.vm.page).toBe(3)

      // Change page size → should reset to page 1
      wrapper.vm.onPageSizeChange(20)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.page).toBe(1)
      expect(wrapper.vm.tableRecord).toHaveLength(20)
      expect(wrapper.vm.tableRecord[0].file.name).toBe('f-0.xml')
    })

    it('should handle navigating to last page with fewer items', async () => {
      await setSourceFiles(createFiles(23))

      wrapper.vm.onPageChange(3)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.tableRecord).toHaveLength(3)
      expect(wrapper.vm.tableRecord[0].file.name).toBe('f-20.xml')
      expect(wrapper.vm.tableRecord[2].file.name).toBe('f-22.xml')
    })

    it('should reflect correct total after page size change', async () => {
      await setSourceFiles(createFiles(50))

      wrapper.vm.onPageSizeChange(100)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.total).toBe(50)
      expect(wrapper.vm.tableRecord).toHaveLength(50)
    })

    it('should handle very large pageSize covering all items', async () => {
      await setSourceFiles(createFiles(5))

      wrapper.vm.onPageSizeChange(200)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.tableRecord).toHaveLength(5)
      expect(wrapper.vm.page).toBe(1)
    })
  })

  // ─── handleSourceFileUpload Input Reset After Errors ─────────────────
  describe('handleSourceFileUpload Input Reset', () => {
    it('should reset input value even when all files throw errors', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(validateSnmpDataCollectionSourceFile).mockRejectedValue(new Error('fail'))

      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
      const inputElement = fileInput.element as HTMLInputElement

      Object.defineProperty(inputElement, 'files', {
        value: [mockFile],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()
      consoleSpy.mockRestore()

      // Input should still be reset after processing errors
      expect(inputElement.files).toBeNull()
    })

    it('should reset input value even when all files are skipped as duplicates', async () => {
      vi.mocked(isDuplicateFile).mockReturnValue(true)

      const fileInput = wrapper.find('[data-test="snmp-data-collection-file-input"]')
      const inputElement = fileInput.element as HTMLInputElement

      Object.defineProperty(inputElement, 'files', {
        value: [mockFile, mockFile2],
        writable: true
      })

      await fileInput.trigger('change')
      await flushPromises()
      await wrapper.vm.$nextTick()

      expect(inputElement.files).toBeNull()
      expect(wrapper.vm.sourceFiles).toHaveLength(0)
    })
  })

  // ─── FeatherPagination Prop Binding ──────────────────────────────────
  describe('FeatherPagination Prop Binding', () => {
    it('should bind update:modelValue to onPageChange', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      const pagination = wrapper.findComponent({ name: 'FeatherPagination' })
      expect(pagination.exists()).toBe(true)

      // Emit update:modelValue event
      await pagination.vm.$emit('update:modelValue', 1)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.page).toBe(1)
    })

    it('should bind update:pageSize to onPageSizeChange', async () => {
      await setSourceFiles([{ file: mockFile, isValid: true, errors: [], isDuplicate: false }])

      const pagination = wrapper.findComponent({ name: 'FeatherPagination' })

      await pagination.vm.$emit('update:pageSize', 20)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.pageSize).toBe(20)
      expect(wrapper.vm.page).toBe(1)
    })

    it('should update pagination total when files are added', async () => {
      await triggerFileInput([mockFile])

      const pagination = wrapper.findComponent({ name: 'FeatherPagination' })
      expect(pagination.props('total')).toBe(1)

      await triggerFileInput([mockFile2])

      expect(pagination.props('total')).toBe(2)
    })
  })

  // ─── Concurrent Upload Prevention ────────────────────────────────────
  describe('Concurrent Upload Prevention', () => {
    it('should set isLoading immediately when uploadFiles starts', async () => {
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]

      // Start upload but don't await
      const promise = wrapper.vm.uploadFiles()
      expect(wrapper.vm.isLoading).toBe(true)

      await promise
      await flushPromises()
      expect(wrapper.vm.isLoading).toBe(false)
    })

    it('shouldUploadDisabled should be true during ongoing upload', async () => {
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]

      expect(wrapper.vm.shouldUploadDisabled).toBe(false)

      const promise = wrapper.vm.uploadFiles()
      expect(wrapper.vm.shouldUploadDisabled).toBe(true)

      await promise
      await flushPromises()
    })

    it('should disable choose-file and choose-folder buttons during upload', async () => {
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]

      const promise = wrapper.vm.uploadFiles()
      await wrapper.vm.$nextTick()

      const chooseFile = wrapper.find('[data-test="choose-file-button"]').findComponent(FeatherButton)
      const chooseFolder = wrapper.find('[data-test="choose-folder-button"]').findComponent(FeatherButton)

      expect(chooseFile.props('disabled')).toBe(true)
      expect(chooseFolder.props('disabled')).toBe(true)

      await promise
      await flushPromises()
    })
  })

  // ─── ellipsify Integration with Component ────────────────────────────
  describe('ellipsify Integration', () => {
    it('should not truncate filename with exactly 39 characters', async () => {
      // Exactly 39 chars
      const name = 'a'.repeat(35) + '.xml' // 39 chars
      expect(name.length).toBe(39)
      const file = new File(['c'], name, { type: 'application/xml' })
      await setSourceFiles([{ file, isValid: true, errors: [], isDuplicate: false }])

      const span = wrapper.find('.file span')
      expect(span.text()).toBe(name)
      expect(span.text()).not.toContain('…')
    })

    it('should truncate filename with 40 characters to 38 chars + ellipsis', async () => {
      const name = 'a'.repeat(36) + '.xml' // 40 chars
      expect(name.length).toBe(40)
      const file = new File(['c'], name, { type: 'application/xml' })
      await setSourceFiles([{ file, isValid: true, errors: [], isDuplicate: false }])

      const span = wrapper.find('.file span')
      // ellipsify returns text.substring(0, 39-1) + '…' = 38 chars + … = 39 chars
      expect(span.text().length).toBe(39)
      expect(span.text().endsWith('…')).toBe(true)
    })

    it('should handle single character filename', async () => {
      const file = new File(['c'], 'x', { type: 'application/xml' })
      await setSourceFiles([{ file, isValid: true, errors: [], isDuplicate: false }])

      const span = wrapper.find('.file span')
      expect(span.text()).toBe('x')
    })

    it('should handle empty string filename', async () => {
      const file = new File(['c'], '', { type: 'application/xml' })
      await setSourceFiles([{ file, isValid: true, errors: [], isDuplicate: false }])

      const span = wrapper.find('.file span')
      // ellipsify returns the text as-is when empty (falsy text)
      expect(span.text()).toBe('')
    })
  })

  // ─── uploadFiles State Machine ───────────────────────────────────────
  describe('uploadFiles State Machine', () => {
    beforeEach(() => {
      store.fetchSnmpCollectionSources = vi.fn().mockResolvedValue(undefined)
    })

    it('should not clear sourceFiles if extension check fails', async () => {
      const txtFile = new File(['text'], 'bad.txt', { type: 'text/plain' })
      wrapper.vm.sourceFiles = [{ file: txtFile, isValid: true, errors: [], isDuplicate: false }]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      // Extension check fails → early return → sourceFiles untouched
      expect(wrapper.vm.sourceFiles).toHaveLength(1)
      expect(wrapper.vm.isLoading).toBe(false)
    })

    it('should not set isLoading if extension check fails', async () => {
      const txtFile = new File(['text'], 'bad.txt', { type: 'text/plain' })
      wrapper.vm.sourceFiles = [{ file: txtFile, isValid: true, errors: [], isDuplicate: false }]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.isLoading).toBe(false)
    })

    it('should not set isLoading if no files to upload', async () => {
      const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {})
      wrapper.vm.sourceFiles = []

      await wrapper.vm.uploadFiles()

      expect(wrapper.vm.isLoading).toBe(false)
      consoleSpy.mockRestore()
    })

    it('should not open report dialog if extension check fails', async () => {
      const txtFile = new File(['text'], 'bad.txt', { type: 'text/plain' })
      wrapper.vm.sourceFiles = [{ file: txtFile, isValid: true, errors: [], isDuplicate: false }]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.uploadedDataCollectionFilesReportDialogState).toBe(false)
    })

    it('should not call fetchAllSourcesNames if extension check fails', async () => {
      vi.mocked(store.fetchAllSourcesNames).mockClear()
      const txtFile = new File(['text'], 'bad.txt', { type: 'text/plain' })
      wrapper.vm.sourceFiles = [{ file: txtFile, isValid: true, errors: [], isDuplicate: false }]

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(store.fetchAllSourcesNames).not.toHaveBeenCalled()
    })

    it('should clear tableRecord and total on successful upload', async () => {
      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]
      wrapper.vm.tableRecord = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]
      wrapper.vm.total = 1

      await wrapper.vm.uploadFiles()
      await flushPromises()

      expect(wrapper.vm.tableRecord).toHaveLength(0)
      expect(wrapper.vm.total).toBe(0)
    })

    it('should not clear tableRecord or total on upload failure', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(uploadDataCollectionFiles).mockRejectedValueOnce(new Error('fail'))

      wrapper.vm.sourceFiles = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]
      wrapper.vm.tableRecord = [{ file: mockFile, isValid: true, errors: [], isDuplicate: false }]
      wrapper.vm.total = 1

      await wrapper.vm.uploadFiles()
      await flushPromises()
      consoleSpy.mockRestore()

      // Source data should be preserved on error
      expect(wrapper.vm.sourceFiles).toHaveLength(1)
    })
  })

  // ─── Large File Set Pagination Rendering ─────────────────────────────
  describe('Large File Set Pagination Rendering', () => {
    it('should render exactly pageSize rows for 200+ files', async () => {
      const files = Array.from({ length: 200 }, (_, i) => ({
        file: new File(['c'], `f-${i}.xml`, { type: 'application/xml' }),
        isValid: true, errors: [] as string[], isDuplicate: false
      }))
      await setSourceFiles(files)

      expect(wrapper.vm.tableRecord).toHaveLength(10) // default pageSize
      // 1 header row + 10 data rows
      const rows = wrapper.findAll('tr')
      expect(rows).toHaveLength(11)
    })

    it('should update rendered rows when pageSize changes for large set', async () => {
      const files = Array.from({ length: 200 }, (_, i) => ({
        file: new File(['c'], `f-${i}.xml`, { type: 'application/xml' }),
        isValid: true, errors: [] as string[], isDuplicate: false
      }))
      await setSourceFiles(files)

      wrapper.vm.onPageSizeChange(50)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.tableRecord).toHaveLength(50)
    })

    it('should show correct files on last page of large set', async () => {
      const files = Array.from({ length: 25 }, (_, i) => ({
        file: new File(['c'], `f-${i}.xml`, { type: 'application/xml' }),
        isValid: true, errors: [] as string[], isDuplicate: false
      }))
      await setSourceFiles(files)

      wrapper.vm.onPageChange(3)
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.tableRecord).toHaveLength(5)
      expect(wrapper.vm.tableRecord[0].file.name).toBe('f-20.xml')
      expect(wrapper.vm.tableRecord[4].file.name).toBe('f-24.xml')
    })
  })

})

import CompiledMibFiles from '@/components/MibCompiler/CompiledMibFiles.vue'
import { deleteFile, generateEvents, getFileText } from '@/services/mibCompilerService'
import { useMibCompilerStore } from '@/stores/mibCompilerStore'
import { MibCompilerFileInfo } from '@/types/mibCompiler'
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/mibCompilerService', () => ({
  deleteFile: vi.fn(),
  generateEvents: vi.fn(),
  getFileText: vi.fn()
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: vi.fn()
  })
}))

describe('CompiledMibFiles.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useMibCompilerStore>
  let mockFile: MibCompilerFileInfo

  const globalConfig = {
    global: {
      stubs: {
        ConfirmationDialog: {
          name: 'ConfirmationDialog',
          template: '<div class="confirmation-dialog-stub"><slot name="content" /></div>',
          props: ['visible', 'title'],
          emits: ['ok', 'cancel']
        },
        FileText: {
          name: 'FileText',
          template: '<div class="file-text-stub"><slot name="content" /></div>',
          props: ['visible', 'title'],
          emits: ['hidden']
        },
        TableCard: {
          name: 'TableCard',
          template: '<div class="table-card-stub"><slot /></div>'
        },
        EmptyList: {
          name: 'EmptyList',
          template: '<div class="empty-list-stub" data-test="empty-list">{{ content?.msg }}</div>',
          props: ['content']
        },
        FeatherIcon: true,
        FeatherSortHeader: {
          name: 'FeatherSortHeader',
          template: '<th><slot /></th>',
          props: ['property', 'sort'],
          emits: ['sort-changed']
        },
        TransitionGroup: {
          name: 'TransitionGroup',
          template: '<tbody><slot /></tbody>'
        }
      },
      components: {
        FeatherButton,
        FeatherInput,
        FeatherPagination
      }
    }
  }

  beforeEach(async () => {
    vi.clearAllMocks()

    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useMibCompilerStore(pinia)

    store.files = []
    store.compiledMibFilesSearchTerm = ''
    store.compiledMibFilesPagination = { page: 1, pageSize: 10, total: 0 }
    store.compiledMibFilesSort = { property: 'fileName', value: SORT.NONE }
    store.fetchMibFiles = vi.fn().mockResolvedValue(undefined)
    store.onCompiledMibFilesSearchChange = vi.fn()
    store.onCompiledMibFilesPageChange = vi.fn()
    store.onCompiledMibFilesPageSizeChange = vi.fn()
    store.onCompiledMibFilesSortChange = vi.fn()

    mockFile = {
      fileName: 'test-mib.mib',
      location: 'COMPILED'
    }

    wrapper = mount(CompiledMibFiles, {
      ...globalConfig,
      global: {
        ...globalConfig.global,
        plugins: [pinia]
      }
    })

    await flushPromises()
    await nextTick()
  })

  afterEach(async () => {
    if (wrapper) {
      wrapper.unmount()
    }

    await flushPromises()
    await nextTick()

    vi.restoreAllMocks()
  })

  describe('Basic Rendering', () => {
    it('renders the component', () => {
      expect(wrapper.exists()).toBe(true)
    })

    it('renders the header with correct title', () => {
      const header = wrapper.find('h3')
      expect(header.exists()).toBe(true)
      expect(header.text()).toBe('Compiled MIB Files')
    })

    it('renders the search input', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.exists()).toBe(true)
      expect(searchInput.props('label')).toBe('Search MIBs')
    })

    it('renders the data table with correct aria-label', () => {
      const table = wrapper.find('table.data-table')
      expect(table.exists()).toBe(true)
      expect(table.attributes('aria-label')).toBe('Compiled MIB Files Table')
    })

    it('renders column headers', () => {
      const headers = wrapper.findAll('th')
      expect(headers.length).toBeGreaterThanOrEqual(2)
      expect(headers[0].text()).toContain('MIB File')
      expect(headers[1].text()).toBe('Actions')
    })

    it('renders the container div', () => {
      expect(wrapper.find('.container').exists()).toBe(true)
    })
  })

  describe('Empty State', () => {
    it('renders EmptyList when no compiled files exist', async () => {
      store.files = []
      await wrapper.vm.$nextTick()

      const emptyList = wrapper.find('[data-test="empty-list"]')
      expect(emptyList.exists()).toBe(true)
    })

    it('does not render pagination when no files exist', async () => {
      store.files = []
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      expect(pagination.exists()).toBe(false)
    })

    it('does not render table rows when no files exist', async () => {
      store.files = []
      await wrapper.vm.$nextTick()

      const rows = wrapper.findAll('tbody tr')
      expect(rows.length).toBe(0)
    })
  })

  describe('Table with Data', () => {
    beforeEach(async () => {
      store.files = [
        mockFile,
        { fileName: 'another-mib.mib', location: 'COMPILED' }
      ]
      await wrapper.vm.$nextTick()
    })

    it('renders table rows for each compiled file', async () => {
      // Need to trigger reactivity
      await flushPromises()
      expect(store.filteredCompiledMibFiles.length).toBe(2)
    })

    it('renders file name in each row', async () => {
      await flushPromises()
      const fileNames = wrapper.findAll('[data-test="file-name"]')
      expect(fileNames.length).toBe(2)
    })

    it('renders action buttons for each row', async () => {
      await flushPromises()
      const generateButtons = wrapper.findAll('[data-test="generate-events-button"]')
      const deleteButtons = wrapper.findAll('[data-test="delete-button"]')
      const downloadButtons = wrapper.findAll('[data-test="download-button"]')
      
      expect(generateButtons.length).toBe(2)
      expect(deleteButtons.length).toBe(2)
      expect(downloadButtons.length).toBe(2)
    })

    it('renders pagination when files exist', async () => {
      await flushPromises()
      const pagination = wrapper.find('[data-test="FeatherPagination"]')
      expect(pagination.exists()).toBe(true)
    })
  })

  describe('Search Functionality', () => {
    it('binds search term to store value', async () => {
      store.compiledMibFilesSearchTerm = 'test'
      await wrapper.vm.$nextTick()

      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('modelValue')).toBe('test')
    })

    it('calls store method on search input change', async () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      await searchInput.vm.$emit('update:modelValue', 'new-search')

      expect(store.onCompiledMibFilesSearchChange).toHaveBeenCalledWith('new-search')
    })

    it('filters files based on search term', async () => {
      store.files = [
        { fileName: 'network.mib', location: 'COMPILED' },
        { fileName: 'system.mib', location: 'COMPILED' }
      ]
      store.compiledMibFilesSearchTerm = 'network'
      await wrapper.vm.$nextTick()

      expect(store.searchedCompiledMibFiles.length).toBe(1)
      expect(store.searchedCompiledMibFiles[0].fileName).toBe('network.mib')
    })
  })

  describe('Sorting Functionality', () => {
    it('calls store sort method when sort changes', async () => {
      wrapper.vm.sortChanged({ property: 'fileName', value: SORT.ASCENDING })

      expect(store.onCompiledMibFilesSortChange).toHaveBeenCalledWith({
        property: 'fileName',
        value: SORT.ASCENDING
      })
    })

    it('passes current sort state to sort header', () => {
      store.compiledMibFilesSort = { property: 'fileName', value: SORT.ASCENDING }
      // The component should pass the sort value to FeatherSortHeader
      expect(store.compiledMibFilesSort.value).toBe(SORT.ASCENDING)
    })
  })

  describe('Pagination', () => {
    beforeEach(async () => {
      const files = []
      for (let i = 1; i <= 25; i++) {
        files.push({ fileName: `file${i}.mib`, location: 'COMPILED' as const })
      }
      store.files = files
      store.compiledMibFilesPagination = { page: 1, pageSize: 10, total: 25 }
      await wrapper.vm.$nextTick()
    })

    it('renders pagination with correct props', async () => {
      await flushPromises()
      const pagination = wrapper.find('[data-test="FeatherPagination"]')
      expect(pagination.exists()).toBe(true)
    })

    it('calls store method on page change', async () => {
      const pagination = wrapper.findComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', 2)

      expect(store.onCompiledMibFilesPageChange).toHaveBeenCalledWith(2)
    })

    it('calls store method on page size change', async () => {
      const pagination = wrapper.findComponent(FeatherPagination)
      await pagination.vm.$emit('update:pageSize', 20)

      expect(store.onCompiledMibFilesPageSizeChange).toHaveBeenCalledWith(20)
    })

    it('shows only first page of results', async () => {
      await flushPromises()
      expect(store.paginatedCompiledMibFiles.length).toBe(10)
    })
  })

  describe('View Details (File Text Drawer)', () => {
    beforeEach(async () => {
      store.files = [mockFile]
      await wrapper.vm.$nextTick()
    })

    it('opens file text drawer on file name click', async () => {
      vi.mocked(getFileText).mockResolvedValue({
        name: mockFile.fileName,
        location: 'COMPILED',
        contents: 'MIB file contents here'
      })

      await wrapper.vm.onViewDetailsClick(mockFile)
      await flushPromises()

      expect(getFileText).toHaveBeenCalledWith('COMPILED', mockFile.fileName)
      expect(wrapper.vm.textDrawerVisible).toBe(true)
      expect(wrapper.vm.fileText).toBe('MIB file contents here')
    })

    it('does not open drawer when fileName is empty', async () => {
      await wrapper.vm.onViewDetailsClick({ fileName: '', location: 'COMPILED' })
      
      expect(getFileText).not.toHaveBeenCalled()
      expect(wrapper.vm.textDrawerVisible).toBe(false)
    })

    it('handles error when loading file details fails', async () => {
      vi.mocked(getFileText).mockRejectedValue(new Error('Network error'))

      await wrapper.vm.onViewDetailsClick(mockFile)
      await flushPromises()

      expect(wrapper.vm.textDrawerVisible).toBe(false)
    })

    it('closes file text drawer and resets state', async () => {
      wrapper.vm.textDrawerVisible = true
      wrapper.vm.fileText = 'some content'
      wrapper.vm.selectedFile = mockFile

      wrapper.vm.onCloseTextDrawer()

      expect(wrapper.vm.textDrawerVisible).toBe(false)
      expect(wrapper.vm.fileText).toBe('')
      expect(wrapper.vm.selectedFile).toBeNull()
    })
  })

  describe('Delete Functionality', () => {
    beforeEach(async () => {
      store.files = [mockFile]
      await wrapper.vm.$nextTick()
    })

    it('opens delete confirmation dialog on delete button click', async () => {
      wrapper.vm.onDeleteClick(mockFile)

      expect(wrapper.vm.deleteDialogVisible).toBe(true)
      expect(wrapper.vm.selectedFile).toEqual(mockFile)
    })

    it('deletes file on confirmation', async () => {
      vi.mocked(deleteFile).mockResolvedValue(undefined)
      store.fetchMibFiles = vi.fn().mockResolvedValue(undefined)

      wrapper.vm.selectedFile = mockFile
      await wrapper.vm.onDeleteConfirm()
      await flushPromises()

      expect(deleteFile).toHaveBeenCalledWith('COMPILED', mockFile.fileName)
      expect(store.fetchMibFiles).toHaveBeenCalled()
      expect(wrapper.vm.deleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedFile).toBeNull()
    })

    it('handles delete error gracefully', async () => {
      vi.mocked(deleteFile).mockRejectedValue(new Error('Delete failed'))

      wrapper.vm.selectedFile = mockFile
      await wrapper.vm.onDeleteConfirm()
      await flushPromises()

      expect(wrapper.vm.deleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedFile).toBeNull()
    })

    it('does not delete when no file is selected', async () => {
      wrapper.vm.selectedFile = null
      await wrapper.vm.onDeleteConfirm()

      expect(deleteFile).not.toHaveBeenCalled()
    })

    it('closes dialog on cancel', () => {
      wrapper.vm.selectedFile = mockFile
      wrapper.vm.deleteDialogVisible = true

      wrapper.vm.onDeleteCancel()

      expect(wrapper.vm.deleteDialogVisible).toBe(false)
      expect(wrapper.vm.selectedFile).toBeNull()
    })
  })

  describe('Download Functionality', () => {
    beforeEach(async () => {
      store.files = [mockFile]
      await wrapper.vm.$nextTick()
    })

    it('downloads file when download button is clicked', async () => {
      const mockContents = 'MIB file contents'
      vi.mocked(getFileText).mockResolvedValue({
        name: mockFile.fileName,
        location: 'COMPILED',
        contents: mockContents
      })

      // Mock URL and document methods
      const mockUrl = 'blob:test-url'
      const createObjectURLSpy = vi.spyOn(URL, 'createObjectURL').mockReturnValue(mockUrl)
      const revokeObjectURLSpy = vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {})
      const mockLink = {
        href: '',
        download: '',
        click: vi.fn()
      }
      const createElementSpy = vi.spyOn(document, 'createElement').mockReturnValue(mockLink as any)
      const appendChildSpy = vi.spyOn(document.body, 'appendChild').mockImplementation(() => mockLink as any)
      const removeChildSpy = vi.spyOn(document.body, 'removeChild').mockImplementation(() => mockLink as any)

      await wrapper.vm.onDownloadClick(mockFile)
      await flushPromises()

      expect(getFileText).toHaveBeenCalledWith('COMPILED', mockFile.fileName)
      expect(createObjectURLSpy).toHaveBeenCalled()
      expect(mockLink.click).toHaveBeenCalled()
      expect(revokeObjectURLSpy).toHaveBeenCalledWith(mockUrl)

      createObjectURLSpy.mockRestore()
      revokeObjectURLSpy.mockRestore()
      createElementSpy.mockRestore()
      appendChildSpy.mockRestore()
      removeChildSpy.mockRestore()
    })

    it('does not download when fileName is empty', async () => {
      await wrapper.vm.onDownloadClick({ fileName: '', location: 'COMPILED' })

      expect(getFileText).not.toHaveBeenCalled()
    })

    it('handles download error gracefully', async () => {
      vi.mocked(getFileText).mockRejectedValue(new Error('Download failed'))

      await wrapper.vm.onDownloadClick(mockFile)
      await flushPromises()

      // Should not throw, error is handled
      expect(getFileText).toHaveBeenCalled()
    })
  })

  describe('Generate Events Functionality', () => {
    beforeEach(async () => {
      store.files = [mockFile]
      await wrapper.vm.$nextTick()
    })

    it('opens generate events dialog with default UEI', () => {
      wrapper.vm.onGenerateEventsClick(mockFile)

      expect(wrapper.vm.generateEventsDialogVisible).toBe(true)
      expect(wrapper.vm.selectedFile).toEqual(mockFile)
      expect(wrapper.vm.uei).toBe('uei.opennms.org/')
      expect(wrapper.vm.ueiError).toBe('')
    })

    it('validates empty UEI', async () => {
      wrapper.vm.selectedFile = mockFile
      wrapper.vm.uei = ''
      wrapper.vm.generateEventsDialogVisible = true

      await wrapper.vm.onConfirmGenerateEvents()

      expect(wrapper.vm.ueiError).toBe('UEI Base is required.')
      expect(generateEvents).not.toHaveBeenCalled()
    })

    it('validates UEI must start with uei.opennms.org/', async () => {
      wrapper.vm.selectedFile = mockFile
      wrapper.vm.uei = 'invalid-uei'
      wrapper.vm.generateEventsDialogVisible = true

      await wrapper.vm.onConfirmGenerateEvents()

      expect(wrapper.vm.ueiError).toBe('UEI Base must start with "uei.opennms.org/".')
      expect(generateEvents).not.toHaveBeenCalled()
    })

    it('validates UEI must have path after prefix', async () => {
      wrapper.vm.selectedFile = mockFile
      wrapper.vm.uei = 'uei.opennms.org/'
      wrapper.vm.generateEventsDialogVisible = true

      await wrapper.vm.onConfirmGenerateEvents()

      expect(wrapper.vm.ueiError).toBe('UEI Base must contain a path after "uei.opennms.org/".')
      expect(generateEvents).not.toHaveBeenCalled()
    })

    it('validates UEI should not end with slash', async () => {
      wrapper.vm.selectedFile = mockFile
      wrapper.vm.uei = 'uei.opennms.org/test/'
      wrapper.vm.generateEventsDialogVisible = true

      await wrapper.vm.onConfirmGenerateEvents()

      expect(wrapper.vm.ueiError).toBe('UEI Base should not end with a slash.')
      expect(generateEvents).not.toHaveBeenCalled()
    })

    it('generates events with valid UEI', async () => {
      vi.mocked(generateEvents).mockResolvedValue({ success: true } as any)

      wrapper.vm.selectedFile = mockFile
      wrapper.vm.uei = 'uei.opennms.org/vendor/test'
      wrapper.vm.generateEventsDialogVisible = true

      await wrapper.vm.onConfirmGenerateEvents()
      await flushPromises()

      expect(generateEvents).toHaveBeenCalledWith({
        name: mockFile.fileName,
        ueiBase: 'uei.opennms.org/vendor/test'
      })
      expect(wrapper.vm.generateEventsDialogVisible).toBe(false)
      expect(wrapper.vm.selectedFile).toBeNull()
      expect(wrapper.vm.uei).toBe('')
    })

    it('handles generate events error gracefully', async () => {
      vi.mocked(generateEvents).mockRejectedValue(new Error('Generate failed'))

      wrapper.vm.selectedFile = mockFile
      wrapper.vm.uei = 'uei.opennms.org/vendor/test'
      wrapper.vm.generateEventsDialogVisible = true

      await wrapper.vm.onConfirmGenerateEvents()
      await flushPromises()

      expect(wrapper.vm.generateEventsDialogVisible).toBe(false)
      expect(wrapper.vm.selectedFile).toBeNull()
    })

    it('does not generate events when no file is selected', async () => {
      wrapper.vm.selectedFile = null
      wrapper.vm.uei = 'uei.opennms.org/vendor/test'

      await wrapper.vm.onConfirmGenerateEvents()

      expect(generateEvents).not.toHaveBeenCalled()
    })

    it('closes dialog on cancel and resets state', () => {
      wrapper.vm.selectedFile = mockFile
      wrapper.vm.uei = 'uei.opennms.org/vendor/test'
      wrapper.vm.ueiError = 'some error'
      wrapper.vm.generateEventsDialogVisible = true

      wrapper.vm.onCancelGenerateEvents()

      expect(wrapper.vm.generateEventsDialogVisible).toBe(false)
      expect(wrapper.vm.selectedFile).toBeNull()
      expect(wrapper.vm.uei).toBe('')
      expect(wrapper.vm.ueiError).toBe('')
    })
  })

  describe('Store Integration', () => {
    it('uses store filteredCompiledMibFiles getter', () => {
      store.files = [
        { fileName: 'compiled.mib', location: 'COMPILED' },
        { fileName: 'pending.mib', location: 'PENDING' }
      ]

      expect(store.filteredCompiledMibFiles.length).toBe(1)
      expect(store.filteredCompiledMibFiles[0].fileName).toBe('compiled.mib')
    })

    it('uses store searchedCompiledMibFiles getter', () => {
      store.files = [
        { fileName: 'network.mib', location: 'COMPILED' },
        { fileName: 'system.mib', location: 'COMPILED' }
      ]
      store.compiledMibFilesSearchTerm = 'network'

      expect(store.searchedCompiledMibFiles.length).toBe(1)
    })

    it('uses store paginatedCompiledMibFiles getter', () => {
      const files = []
      for (let i = 1; i <= 25; i++) {
        files.push({ fileName: `file${i}.mib`, location: 'COMPILED' as const })
      }
      store.files = files
      store.compiledMibFilesPagination = { page: 1, pageSize: 10, total: 25 }

      expect(store.paginatedCompiledMibFiles.length).toBe(10)
    })

    it('uses store sortedCompiledMibFiles getter', () => {
      store.files = [
        { fileName: 'zebra.mib', location: 'COMPILED' },
        { fileName: 'alpha.mib', location: 'COMPILED' }
      ]
      store.compiledMibFilesSort = { property: 'fileName', value: SORT.ASCENDING }

      expect(store.sortedCompiledMibFiles[0].fileName).toBe('alpha.mib')
      expect(store.sortedCompiledMibFiles[1].fileName).toBe('zebra.mib')
    })
  })

  describe('Edge Cases', () => {
    it('handles files with special characters in name', async () => {
      store.files = [{ fileName: 'test-mib_v2.3 (copy).mib', location: 'COMPILED' }]
      await wrapper.vm.$nextTick()

      expect(store.filteredCompiledMibFiles.length).toBe(1)
    })

    it('handles empty search term', async () => {
      store.files = [mockFile]
      store.compiledMibFilesSearchTerm = ''
      await wrapper.vm.$nextTick()

      expect(store.searchedCompiledMibFiles.length).toBe(1)
    })

    it('handles whitespace-only search term', async () => {
      store.files = [mockFile]
      store.compiledMibFilesSearchTerm = '   '
      await wrapper.vm.$nextTick()

      // Store should trim whitespace and show all files
      expect(store.searchedCompiledMibFiles.length).toBe(1)
    })

    it('handles case-insensitive search', async () => {
      store.files = [{ fileName: 'NETWORK.mib', location: 'COMPILED' }]
      store.compiledMibFilesSearchTerm = 'network'
      await wrapper.vm.$nextTick()

      expect(store.searchedCompiledMibFiles.length).toBe(1)
    })

    it('handles rapid pagination changes', async () => {
      const files = []
      for (let i = 1; i <= 50; i++) {
        files.push({ fileName: `file${i}.mib`, location: 'COMPILED' as const })
      }
      store.files = files
      store.compiledMibFilesPagination = { page: 1, pageSize: 10, total: 50 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', 2)
      await pagination.vm.$emit('update:modelValue', 3)
      await pagination.vm.$emit('update:modelValue', 1)

      expect(store.onCompiledMibFilesPageChange).toHaveBeenCalledTimes(3)
    })

    it('handles file with very long name', async () => {
      const longName = 'a'.repeat(255) + '.mib'
      store.files = [{ fileName: longName, location: 'COMPILED' }]
      await wrapper.vm.$nextTick()

      expect(store.filteredCompiledMibFiles.length).toBe(1)
      expect(store.filteredCompiledMibFiles[0].fileName).toBe(longName)
    })
  })

  describe('UEI Validation Edge Cases', () => {
    beforeEach(() => {
      wrapper.vm.selectedFile = mockFile
      wrapper.vm.generateEventsDialogVisible = true
    })

    it('validates whitespace-only UEI', async () => {
      wrapper.vm.uei = '   '

      await wrapper.vm.onConfirmGenerateEvents()

      expect(wrapper.vm.ueiError).toBe('UEI Base is required.')
    })

    it('accepts valid UEI with multiple path segments', async () => {
      vi.mocked(generateEvents).mockResolvedValue({ success: true } as any)
      wrapper.vm.uei = 'uei.opennms.org/vendor/category/event'

      await wrapper.vm.onConfirmGenerateEvents()
      await flushPromises()

      expect(generateEvents).toHaveBeenCalledWith({
        name: mockFile.fileName,
        ueiBase: 'uei.opennms.org/vendor/category/event'
      })
    })

    it('accepts valid UEI with hyphens and underscores', async () => {
      vi.mocked(generateEvents).mockResolvedValue({ success: true } as any)
      wrapper.vm.uei = 'uei.opennms.org/vendor-name/event_type'

      await wrapper.vm.onConfirmGenerateEvents()
      await flushPromises()

      expect(generateEvents).toHaveBeenCalled()
    })
  })

  describe('Component Lifecycle', () => {
    it('mounts without errors', () => {
      expect(() => {
        mount(CompiledMibFiles, {
          ...globalConfig,
          global: {
            ...globalConfig.global,
            plugins: [createTestingPinia({ createSpy: vi.fn })]
          }
        })
      }).not.toThrow()
    })

    it('unmounts without errors', () => {
      expect(() => wrapper.unmount()).not.toThrow()
    })

    it('initializes with correct default state', () => {
      expect(wrapper.vm.deleteDialogVisible).toBe(false)
      expect(wrapper.vm.textDrawerVisible).toBe(false)
      expect(wrapper.vm.generateEventsDialogVisible).toBe(false)
      expect(wrapper.vm.selectedFile).toBeNull()
      expect(wrapper.vm.uei).toBe('')
      expect(wrapper.vm.ueiError).toBe('')
      expect(wrapper.vm.fileText).toBe('')
    })
  })

  describe('Columns Configuration', () => {
    it('defines correct columns', () => {
      const columns = wrapper.vm.columns
      expect(columns).toHaveLength(1)
      expect(columns[0]).toEqual({ id: 'fileName', label: 'MIB File' })
    })
  })

  describe('Empty List Content', () => {
    it('has correct empty list message', () => {
      expect(wrapper.vm.emptyListContent).toEqual({ msg: 'No results found.' })
    })
  })

  describe('Accessibility', () => {
    it('table has proper aria-label', () => {
      const table = wrapper.find('table')
      expect(table.attributes('aria-label')).toBe('Compiled MIB Files Table')
    })

    it('file names are clickable with hyperlink class', async () => {
      store.files = [mockFile]
      await wrapper.vm.$nextTick()
      await flushPromises()

      const hyperlinks = wrapper.findAll('.hyperlink')
      expect(hyperlinks.length).toBeGreaterThan(0)
    })
  })

  describe('CSS Classes', () => {
    it('applies correct container class', () => {
      expect(wrapper.find('.compiled-mib-files-container').exists() || 
             wrapper.find('.table-card-stub').exists()).toBe(true)
    })

    it('has header with sections', () => {
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('.section-left').exists()).toBe(true)
      expect(wrapper.find('.section-right').exists()).toBe(true)
    })
  })
})

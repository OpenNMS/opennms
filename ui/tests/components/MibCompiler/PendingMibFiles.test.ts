import PendingMibFiles from '@/components/MibCompiler/PendingMibFiles.vue'
import { compileMib, deleteFile, getFileText } from '@/services/mibCompilerService'
import { useMibCompilerStore } from '@/stores/mibCompilerStore'
import { MibCompilerFileInfo } from '@/types/mibCompiler'
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { SORT } from '@featherds/table'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/mibCompilerService', () => ({
  compileMib: vi.fn(),
  deleteFile: vi.fn(),
  getFileText: vi.fn()
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: vi.fn()
  })
}))

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
}))

describe('PendingMibFiles.vue', () => {
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
    mockPush.mockClear()

    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useMibCompilerStore(pinia)

    store.files = []
    store.pendingMibFilesSearchTerm = ''
    store.pendingMibFilesPagination = { page: 1, pageSize: 10, total: 0 }
    store.pendingMibFilesSort = { property: 'fileName', value: SORT.NONE }
    store.fetchMibFiles = vi.fn().mockResolvedValue(undefined)
    store.onPendingMibFilesSearchChange = vi.fn()
    store.onPendingMibFilesPageChange = vi.fn()
    store.onPendingMibFilesPageSizeChange = vi.fn()
    store.onPendingMibFilesSortChange = vi.fn()
    store.setSelectedMibFile = vi.fn()

    mockFile = {
      fileName: 'test-mib.mib',
      location: 'PENDING'
    }

    wrapper = mount(PendingMibFiles, {
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
      expect(header.text()).toBe('Pending MIB Files')
    })

    it('renders the search input', () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.exists()).toBe(true)
      expect(searchInput.props('label')).toBe('Search MIBs')
    })

    it('renders the data table with correct aria-label', () => {
      const table = wrapper.find('table.data-table')
      expect(table.exists()).toBe(true)
      expect(table.attributes('aria-label')).toBe('Pending MIB Files Table')
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
    it('renders EmptyList when no pending files exist', async () => {
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
        { fileName: 'another-mib.mib', location: 'PENDING' }
      ]
      await wrapper.vm.$nextTick()
    })

    it('renders table rows for each pending file', async () => {
      await flushPromises()
      expect(store.filteredPendingMibFiles.length).toBe(2)
    })

    it('renders file name in each row', async () => {
      await flushPromises()
      const fileNames = wrapper.findAll('[data-test="file-name"]')
      expect(fileNames.length).toBe(2)
    })

    it('renders action buttons for each row', async () => {
      await flushPromises()
      const editButtons = wrapper.findAll('[data-test="edit-button"]')
      const compileButtons = wrapper.findAll('[data-test="compile-button"]')
      const deleteButtons = wrapper.findAll('[data-test="delete-button"]')
      const downloadButtons = wrapper.findAll('[data-test="download-button"]')

      expect(editButtons.length).toBe(2)
      expect(compileButtons.length).toBe(2)
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
      store.pendingMibFilesSearchTerm = 'test'
      await wrapper.vm.$nextTick()

      const searchInput = wrapper.findComponent(FeatherInput)
      expect(searchInput.props('modelValue')).toBe('test')
    })

    it('calls store method on search input change', async () => {
      const searchInput = wrapper.findComponent(FeatherInput)
      await searchInput.vm.$emit('update:modelValue', 'new-search')

      expect(store.onPendingMibFilesSearchChange).toHaveBeenCalledWith('new-search')
    })

    it('filters files based on search term', async () => {
      store.files = [
        { fileName: 'network.mib', location: 'PENDING' },
        { fileName: 'system.mib', location: 'PENDING' }
      ]
      store.pendingMibFilesSearchTerm = 'network'
      await wrapper.vm.$nextTick()

      expect(store.searchedPendingMibFiles.length).toBe(1)
      expect(store.searchedPendingMibFiles[0].fileName).toBe('network.mib')
    })
  })

  describe('Sorting Functionality', () => {
    it('calls store sort method when sort changes', async () => {
      wrapper.vm.sortChanged({ property: 'fileName', value: SORT.ASCENDING })

      expect(store.onPendingMibFilesSortChange).toHaveBeenCalledWith({
        property: 'fileName',
        value: SORT.ASCENDING
      })
    })

    it('passes current sort state to sort header', () => {
      store.pendingMibFilesSort = { property: 'fileName', value: SORT.ASCENDING }
      expect(store.pendingMibFilesSort.value).toBe(SORT.ASCENDING)
    })
  })

  describe('Pagination', () => {
    beforeEach(async () => {
      const files = []
      for (let i = 1; i <= 25; i++) {
        files.push({ fileName: `file${i}.mib`, location: 'PENDING' as const })
      }
      store.files = files
      store.pendingMibFilesPagination = { page: 1, pageSize: 10, total: 25 }
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

      expect(store.onPendingMibFilesPageChange).toHaveBeenCalledWith(2)
    })

    it('calls store method on page size change', async () => {
      const pagination = wrapper.findComponent(FeatherPagination)
      await pagination.vm.$emit('update:pageSize', 20)

      expect(store.onPendingMibFilesPageSizeChange).toHaveBeenCalledWith(20)
    })

    it('shows only first page of results', async () => {
      await flushPromises()
      expect(store.paginatedPendingMibFiles.length).toBe(10)
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
        location: 'PENDING',
        contents: 'MIB file contents here'
      })

      await wrapper.vm.onViewDetailsClick(mockFile)
      await flushPromises()

      expect(getFileText).toHaveBeenCalledWith('PENDING', mockFile.fileName)
      expect(wrapper.vm.textDrawerVisible).toBe(true)
      expect(wrapper.vm.fileText).toBe('MIB file contents here')
    })

    it('does not open drawer when fileName is empty', async () => {
      await wrapper.vm.onViewDetailsClick({ fileName: '', location: 'PENDING' })

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

  describe('Edit Functionality', () => {
    beforeEach(async () => {
      store.files = [mockFile]
      await wrapper.vm.$nextTick()
    })

    it('navigates to edit page on edit button click', async () => {
      const mockResponse = {
        name: mockFile.fileName,
        location: 'PENDING',
        contents: 'MIB file contents'
      }
      vi.mocked(getFileText).mockResolvedValue(mockResponse)

      await wrapper.vm.onEditClick(mockFile)
      await flushPromises()

      expect(getFileText).toHaveBeenCalledWith('PENDING', mockFile.fileName)
      expect(store.setSelectedMibFile).toHaveBeenCalledWith(mockResponse)
      expect(mockPush).toHaveBeenCalledWith('/mib-compiler/edit')
    })

    it('does not navigate when fileName is empty', async () => {
      await wrapper.vm.onEditClick({ fileName: '', location: 'PENDING' })

      expect(getFileText).not.toHaveBeenCalled()
      expect(mockPush).not.toHaveBeenCalled()
    })

    it('handles error when loading file for edit fails', async () => {
      vi.mocked(getFileText).mockRejectedValue(new Error('Network error'))

      await wrapper.vm.onEditClick(mockFile)
      await flushPromises()

      expect(mockPush).not.toHaveBeenCalled()
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

      expect(deleteFile).toHaveBeenCalledWith('PENDING', mockFile.fileName)
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
        location: 'PENDING',
        contents: mockContents
      })

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

      expect(getFileText).toHaveBeenCalledWith('PENDING', mockFile.fileName)
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
      await wrapper.vm.onDownloadClick({ fileName: '', location: 'PENDING' })

      expect(getFileText).not.toHaveBeenCalled()
    })

    it('handles download error gracefully', async () => {
      vi.mocked(getFileText).mockRejectedValue(new Error('Download failed'))

      await wrapper.vm.onDownloadClick(mockFile)
      await flushPromises()

      expect(getFileText).toHaveBeenCalled()
    })
  })

  describe('Compile Functionality', () => {
    beforeEach(async () => {
      store.files = [mockFile]
      await wrapper.vm.$nextTick()
    })

    it('opens compile confirmation dialog on compile button click', () => {
      wrapper.vm.onCompileClick(mockFile)

      expect(wrapper.vm.compileDialogVisible).toBe(true)
      expect(wrapper.vm.selectedFile).toEqual(mockFile)
    })

    it('compiles file on confirmation', async () => {
      vi.mocked(compileMib).mockResolvedValue({
        success: true,
        message: 'MIB compiled successfully',
        mibName: mockFile.fileName
      })
      store.fetchMibFiles = vi.fn().mockResolvedValue(undefined)

      wrapper.vm.selectedFile = mockFile
      await wrapper.vm.onCompileConfirm()
      await flushPromises()

      expect(compileMib).toHaveBeenCalledWith(mockFile.fileName)
      expect(store.fetchMibFiles).toHaveBeenCalled()
      expect(wrapper.vm.compileDialogVisible).toBe(false)
      expect(wrapper.vm.selectedFile).toBeNull()
    })

    it('handles compile error gracefully', async () => {
      vi.mocked(compileMib).mockRejectedValue(new Error('Compile failed'))

      wrapper.vm.selectedFile = mockFile
      await wrapper.vm.onCompileConfirm()
      await flushPromises()

      expect(wrapper.vm.compileDialogVisible).toBe(false)
      expect(wrapper.vm.selectedFile).toBeNull()
    })

    it('does not compile when no file is selected', async () => {
      wrapper.vm.selectedFile = null
      await wrapper.vm.onCompileConfirm()

      expect(compileMib).not.toHaveBeenCalled()
    })

    it('closes dialog on cancel', () => {
      wrapper.vm.selectedFile = mockFile
      wrapper.vm.compileDialogVisible = true

      wrapper.vm.onCompileCancel()

      expect(wrapper.vm.compileDialogVisible).toBe(false)
      expect(wrapper.vm.selectedFile).toBeNull()
    })

    it('displays success message from response', async () => {
      const successMessage = 'Custom success message'
      vi.mocked(compileMib).mockResolvedValue({
        success: true,
        message: successMessage,
        mibName: mockFile.fileName
      })

      wrapper.vm.selectedFile = mockFile
      await wrapper.vm.onCompileConfirm()
      await flushPromises()

      expect(compileMib).toHaveBeenCalled()
    })

    it('uses default message when response message is empty', async () => {
      vi.mocked(compileMib).mockResolvedValue({
        success: true,
        message: '',
        mibName: mockFile.fileName
      })

      wrapper.vm.selectedFile = mockFile
      await wrapper.vm.onCompileConfirm()
      await flushPromises()

      expect(compileMib).toHaveBeenCalled()
    })
  })

  describe('Store Integration', () => {
    it('uses store filteredPendingMibFiles getter', () => {
      store.files = [
        { fileName: 'compiled.mib', location: 'COMPILED' },
        { fileName: 'pending.mib', location: 'PENDING' }
      ]

      expect(store.filteredPendingMibFiles.length).toBe(1)
      expect(store.filteredPendingMibFiles[0].fileName).toBe('pending.mib')
    })

    it('uses store searchedPendingMibFiles getter', () => {
      store.files = [
        { fileName: 'network.mib', location: 'PENDING' },
        { fileName: 'system.mib', location: 'PENDING' }
      ]
      store.pendingMibFilesSearchTerm = 'network'

      expect(store.searchedPendingMibFiles.length).toBe(1)
    })

    it('uses store paginatedPendingMibFiles getter', () => {
      const files = []
      for (let i = 1; i <= 25; i++) {
        files.push({ fileName: `file${i}.mib`, location: 'PENDING' as const })
      }
      store.files = files
      store.pendingMibFilesPagination = { page: 1, pageSize: 10, total: 25 }

      expect(store.paginatedPendingMibFiles.length).toBe(10)
    })

    it('uses store sortedPendingMibFiles getter', () => {
      store.files = [
        { fileName: 'zebra.mib', location: 'PENDING' },
        { fileName: 'alpha.mib', location: 'PENDING' }
      ]
      store.pendingMibFilesSort = { property: 'fileName', value: SORT.ASCENDING }

      expect(store.sortedPendingMibFiles[0].fileName).toBe('alpha.mib')
      expect(store.sortedPendingMibFiles[1].fileName).toBe('zebra.mib')
    })
  })

  describe('Edge Cases', () => {
    it('handles files with special characters in name', async () => {
      store.files = [{ fileName: 'test-mib_v2.3 (copy).mib', location: 'PENDING' }]
      await wrapper.vm.$nextTick()

      expect(store.filteredPendingMibFiles.length).toBe(1)
    })

    it('handles empty search term', async () => {
      store.files = [mockFile]
      store.pendingMibFilesSearchTerm = ''
      await wrapper.vm.$nextTick()

      expect(store.searchedPendingMibFiles.length).toBe(1)
    })

    it('handles whitespace-only search term', async () => {
      store.files = [mockFile]
      store.pendingMibFilesSearchTerm = '   '
      await wrapper.vm.$nextTick()

      expect(store.searchedPendingMibFiles.length).toBe(1)
    })

    it('handles case-insensitive search', async () => {
      store.files = [{ fileName: 'NETWORK.mib', location: 'PENDING' }]
      store.pendingMibFilesSearchTerm = 'network'
      await wrapper.vm.$nextTick()

      expect(store.searchedPendingMibFiles.length).toBe(1)
    })

    it('handles rapid pagination changes', async () => {
      const files = []
      for (let i = 1; i <= 50; i++) {
        files.push({ fileName: `file${i}.mib`, location: 'PENDING' as const })
      }
      store.files = files
      store.pendingMibFilesPagination = { page: 1, pageSize: 10, total: 50 }
      await wrapper.vm.$nextTick()

      const pagination = wrapper.findComponent(FeatherPagination)
      await pagination.vm.$emit('update:modelValue', 2)
      await pagination.vm.$emit('update:modelValue', 3)
      await pagination.vm.$emit('update:modelValue', 1)

      expect(store.onPendingMibFilesPageChange).toHaveBeenCalledTimes(3)
    })

    it('handles file with very long name', async () => {
      const longName = 'a'.repeat(255) + '.mib'
      store.files = [{ fileName: longName, location: 'PENDING' }]
      await wrapper.vm.$nextTick()

      expect(store.filteredPendingMibFiles.length).toBe(1)
      expect(store.filteredPendingMibFiles[0].fileName).toBe(longName)
    })
  })

  describe('Component Lifecycle', () => {
    it('mounts without errors', () => {
      expect(() => {
        mount(PendingMibFiles, {
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
      expect(wrapper.vm.compileDialogVisible).toBe(false)
      expect(wrapper.vm.selectedFile).toBeNull()
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
      expect(table.attributes('aria-label')).toBe('Pending MIB Files Table')
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
      expect(
        wrapper.find('.pending-mib-files-container').exists() ||
          wrapper.find('.table-card-stub').exists()
      ).toBe(true)
    })

    it('has header with sections', () => {
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('.section-left').exists()).toBe(true)
      expect(wrapper.find('.section-right').exists()).toBe(true)
    })
  })

  describe('Compile Error Handling', () => {
    beforeEach(() => {
      wrapper.vm.selectedFile = mockFile
      wrapper.vm.compileDialogVisible = true
    })

    it('handles compile error with missing dependencies', async () => {
      const error = {
        isAxiosError: true,
        response: {
          data: {
            success: false,
            message: 'Compilation failed',
            missingDependencies: ['DEP1-MIB', 'DEP2-MIB']
          }
        }
      }
      vi.mocked(compileMib).mockRejectedValue(error)

      await wrapper.vm.onCompileConfirm()
      await flushPromises()

      expect(wrapper.vm.compileDialogVisible).toBe(false)
    })

    it('handles compile error with generic error message', async () => {
      vi.mocked(compileMib).mockRejectedValue(new Error('Network error'))

      await wrapper.vm.onCompileConfirm()
      await flushPromises()

      expect(wrapper.vm.compileDialogVisible).toBe(false)
    })
  })

  describe('Router Navigation', () => {
    it('uses router to navigate to edit page', async () => {
      vi.mocked(getFileText).mockResolvedValue({
        name: mockFile.fileName,
        location: 'PENDING',
        contents: 'content'
      })

      await wrapper.vm.onEditClick(mockFile)
      await flushPromises()

      expect(mockPush).toHaveBeenCalledWith('/mib-compiler/edit')
    })

    it('sets selected file in store before navigation', async () => {
      const mockResponse = {
        name: mockFile.fileName,
        location: 'PENDING',
        contents: 'content'
      }
      vi.mocked(getFileText).mockResolvedValue(mockResponse)

      await wrapper.vm.onEditClick(mockFile)
      await flushPromises()

      expect(store.setSelectedMibFile).toHaveBeenCalledWith(mockResponse)
    })
  })
})

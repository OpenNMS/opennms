import { listPendingAndCompiledFiles } from '@/services/mibCompilerService'
import { MibCompilerFileInfo, MibCompilerFileInfoWithContent, MibCompilerStoreState } from '@/types/mibCompiler'
import { SORT } from '@featherds/table'
import { defineStore } from 'pinia'

export const useMibCompilerStore = defineStore('useMibCompilerStore', {
  state: (): MibCompilerStoreState => ({
    files: [],
    isLoading: false,
    selectedMibFile: null,
    compiledMibFilesSearchTerm: '',
    pendingMibFilesSearchTerm: '',
    compiledMibFilesSort: {
      property: 'fileName',
      value: SORT.NONE
    },
    pendingMibFilesSort: {
      property: 'fileName',
      value: SORT.NONE
    },
    compiledMibFilesPagination: {
      page: 1,
      pageSize: 10,
      total: 0
    },
    pendingMibFilesPagination: {
      page: 1,
      pageSize: 10,
      total: 0
    }
  }),
  getters: {
    filteredCompiledMibFiles(): MibCompilerFileInfo[] {
      return this.files.filter((file: MibCompilerFileInfo) => file.location === 'COMPILED')
    },
    filteredPendingMibFiles(): MibCompilerFileInfo[] {
      return this.files.filter((file: MibCompilerFileInfo) => file.location === 'PENDING')
    },
    searchedCompiledMibFiles(): MibCompilerFileInfo[] {
      const query = this.compiledMibFilesSearchTerm.trim().toLowerCase()

      if (!query) {
        return this.filteredCompiledMibFiles
      }

      return this.filteredCompiledMibFiles.filter((file) => file.fileName.toLowerCase().includes(query))
    },
    searchedPendingMibFiles(): MibCompilerFileInfo[] {
      const query = this.pendingMibFilesSearchTerm.trim().toLowerCase()

      if (!query) {
        return this.filteredPendingMibFiles
      }

      return this.filteredPendingMibFiles.filter((file) => file.fileName.toLowerCase().includes(query))
    },
    sortedCompiledMibFiles(): MibCompilerFileInfo[] {
      const { property, value } = this.compiledMibFilesSort

      if (value === SORT.NONE) {
        return this.searchedCompiledMibFiles
      }

      return [...this.searchedCompiledMibFiles].sort((a, b) => {
        const aValue = String(a[property] ?? '')
        const bValue = String(b[property] ?? '')
        const compareResult = aValue.localeCompare(bValue, undefined, {
          numeric: true,
          sensitivity: 'base'
        })

        return value === SORT.ASCENDING ? compareResult : -compareResult
      })
    },
    sortedPendingMibFiles(): MibCompilerFileInfo[] {
      const { property, value } = this.pendingMibFilesSort

      if (value === SORT.NONE) {
        return this.searchedPendingMibFiles
      }

      return [...this.searchedPendingMibFiles].sort((a, b) => {
        const aValue = String(a[property] ?? '')
        const bValue = String(b[property] ?? '')
        const compareResult = aValue.localeCompare(bValue, undefined, {
          numeric: true,
          sensitivity: 'base'
        })

        return value === SORT.ASCENDING ? compareResult : -compareResult
      })
    },
    paginatedCompiledMibFiles(): MibCompilerFileInfo[] {
      const start = (this.compiledMibFilesPagination.page - 1) * this.compiledMibFilesPagination.pageSize
      const end = start + this.compiledMibFilesPagination.pageSize
      return this.sortedCompiledMibFiles.slice(start, end)
    },
    paginatedPendingMibFiles(): MibCompilerFileInfo[] {
      const start = (this.pendingMibFilesPagination.page - 1) * this.pendingMibFilesPagination.pageSize
      const end = start + this.pendingMibFilesPagination.pageSize
      return this.sortedPendingMibFiles.slice(start, end)
    }
  },
  actions: {
    async fetchMibFiles() {
      this.isLoading = true
      try {
        const response = await listPendingAndCompiledFiles()
        this.files = response

        const compiledCount = this.searchedCompiledMibFiles.length
        const compiledMaxPage = Math.max(1, Math.ceil(compiledCount / this.compiledMibFilesPagination.pageSize))
        if (this.compiledMibFilesPagination.page > compiledMaxPage) {
          this.compiledMibFilesPagination.page = compiledMaxPage
        }

        const pendingCount = this.searchedPendingMibFiles.length
        const pendingMaxPage = Math.max(1, Math.ceil(pendingCount / this.pendingMibFilesPagination.pageSize))
        if (this.pendingMibFilesPagination.page > pendingMaxPage) {
          this.pendingMibFilesPagination.page = pendingMaxPage
        }

        this.isLoading = false
      } catch (error) {
        console.error('Error fetching compiled MIB files:', error)
        this.isLoading = false
      }
    },
    // Frontend pagination actions - only update page state, do not re-fetch
    onCompiledMibFilesPageChange(newPage: number) {
      this.compiledMibFilesPagination.page = newPage
    },
    onCompiledMibFilesSearchChange(searchTerm: string | number | undefined) {
      this.compiledMibFilesSearchTerm = String(searchTerm ?? '')
      this.compiledMibFilesPagination.page = 1
    },
    onCompiledMibFilesSortChange(sortObj: { property: keyof MibCompilerFileInfo; value: SORT }) {
      this.compiledMibFilesSort = sortObj
      this.compiledMibFilesPagination.page = 1
    },
    onCompiledMibFilesPageSizeChange(newPageSize: number) {
      this.compiledMibFilesPagination.pageSize = newPageSize
      this.compiledMibFilesPagination.page = 1 // Reset to first page when page size changes
    },
    onPendingMibFilesPageChange(newPage: number) {
      this.pendingMibFilesPagination.page = newPage
    },
    onPendingMibFilesSearchChange(searchTerm: string | number | undefined) {
      this.pendingMibFilesSearchTerm = String(searchTerm ?? '')
      this.pendingMibFilesPagination.page = 1
    },
    onPendingMibFilesSortChange(sortObj: { property: keyof MibCompilerFileInfo; value: SORT }) {
      this.pendingMibFilesSort = sortObj
      this.pendingMibFilesPagination.page = 1
    },
    onPendingMibFilesPageSizeChange(newPageSize: number) {
      this.pendingMibFilesPagination.pageSize = newPageSize
      this.pendingMibFilesPagination.page = 1 // Reset to first page when page size changes
    },
    setSelectedMibFile(file: MibCompilerFileInfoWithContent | null) {
      this.selectedMibFile = file
    }
  }
})


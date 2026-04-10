import { listPendingAndCompiledFiles } from '@/services/mibCompilerService'
import { MibCompilerFileInfo, MibCompilerStoreState } from '@/types/mibCompiler'
import { SORT } from '@featherds/table'
import { defineStore } from 'pinia'

export const useMibCompilerStore = defineStore('useMibCompilerStore', {
  state: (): MibCompilerStoreState => ({
    compiledMibFiles: [],
    pendingMibFiles: [],
    files: [],
    isLoading: false,
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
    sortedCompiledMibFiles(): MibCompilerFileInfo[] {
      const { property, value } = this.compiledMibFilesSort

      if (value === SORT.NONE) {
        return this.filteredCompiledMibFiles
      }

      return [...this.filteredCompiledMibFiles].sort((a, b) => {
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
        return this.filteredPendingMibFiles
      }

      return [...this.filteredPendingMibFiles].sort((a, b) => {
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
        this.compiledMibFilesPagination.total = this.filteredCompiledMibFiles.length
        this.pendingMibFilesPagination.total = this.filteredPendingMibFiles.length
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
    onPendingMibFilesSortChange(sortObj: { property: keyof MibCompilerFileInfo; value: SORT }) {
      this.pendingMibFilesSort = sortObj
      this.pendingMibFilesPagination.page = 1
    },
    onPendingMibFilesPageSizeChange(newPageSize: number) {
      this.pendingMibFilesPagination.pageSize = newPageSize
      this.pendingMibFilesPagination.page = 1 // Reset to first page when page size changes
    }
  }
})


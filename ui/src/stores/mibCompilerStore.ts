import { listMibFiles } from '@/services/mibCompilerService'
import { MibCompilerStoreState } from '@/types/mibCompiler'
import { defineStore } from 'pinia'

export const useMibCompilerStore = defineStore('useMibCompilerStore', {
  state: (): MibCompilerStoreState => ({
    pendingFiles: [],
    compiledFiles: [],
    isLoading: false
  }),
  actions: {
    async fetchMibFiles() {
      this.isLoading = true
      try {
        const response = await listMibFiles()
        this.pendingFiles = response.pending
        this.compiledFiles = response.compiled
      } catch (error) {
        console.error('Error fetching MIB files:', error)
      } finally {
        this.isLoading = false
      }
    }
  }
})

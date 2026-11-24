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
    async fetchMibFiles(): Promise<boolean> {
      this.isLoading = true
      try {
        const response = await listMibFiles()
        this.pendingFiles = response.pending
        this.compiledFiles = response.compiled
        return true
      } catch (error) {
        console.error('Error fetching MIB files:', error)
        // the caller says what happened: an empty table would read as "no MIB files"
        return false
      } finally {
        this.isLoading = false
      }
    }
  }
})

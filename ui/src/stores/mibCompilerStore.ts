import { listMibFiles } from '@/services/mibCompilerService'
import { MibCompilerStoreState } from '@/types/mibCompiler'
import { createFailureResult, createSuccessResponse, ValidationResult } from '@/types/validation'
import { defineStore } from 'pinia'

export const useMibCompilerStore = defineStore('useMibCompilerStore', {
  state: (): MibCompilerStoreState => ({
    pendingFiles: [],
    compiledFiles: [],
    isLoading: false
  }),
  actions: {
    async fetchMibFiles(): Promise<ValidationResult> {
      this.isLoading = true
      try {
        const response = await listMibFiles()
        this.pendingFiles = response.pending
        this.compiledFiles = response.compiled
        return createSuccessResponse()
      } catch (error) {
        console.error('Error fetching MIB files:', error)
        // the caller reports it: an empty table would read as "no MIB files"
        return createFailureResult('Failed to load the MIB files. Try again.')
      } finally {
        this.isLoading = false
      }
    }
  }
})

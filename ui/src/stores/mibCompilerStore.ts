import useSnackbar from '@/composables/useSnackbar'
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
        // an empty table after a failed load reads as "no MIB files"; say what happened
        useSnackbar().showSnackBar({ msg: 'Failed to load the MIB files. Try again.', error: true })
      } finally {
        this.isLoading = false
      }
    }
  }
})

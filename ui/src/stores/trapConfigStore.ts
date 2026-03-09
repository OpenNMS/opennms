import { TrapConfigStoreState } from '@/types/trapConfig'
import { defineStore } from 'pinia'

export const useTrapConfigStore = defineStore('useTrapConfigStore', {
  state: (): TrapConfigStoreState => ({
    isLoading: false,
    activeTab: 0
  }),
  actions: {
    // async fetchAllTrapConfigsNames() {
    //   this.isLoading = true
    //   try {
    //     const response = await getAllTrapConfigNames()
    //     this.uploadedTrapConfigs = response
    //     this.isLoading = false
    //   } catch (error) {
    //     console.error('Error fetching all trap configuration names:', error)
    //     this.isLoading = false
    //   }
    // },
    // async fetchTrapConfigs() {
    //   this.isLoading = true
    //   try {
    //     const response = await getTrapConfigs()
    //     await this.fetchAllTrapConfigsNames()
    //     this.trapConfigs = response
    //     this.isLoading = false
    //   } catch (error) {
    //     console.error('Error fetching trap configurations:', error)
    //     this.isLoading = false
    //   }
    // }
  }
})


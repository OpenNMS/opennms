import { TrapConfigStoreState } from '@/types/trapConfig'
import { defineStore } from 'pinia'

export const useTrapConfigStore = defineStore('useTrapConfigStore', {
  state: (): TrapConfigStoreState => ({
    isLoading: false,
    activeTab: 0
  }),
  actions: {}
})


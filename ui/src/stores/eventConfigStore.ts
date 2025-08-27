import { eventConfigSources } from '@/components/EventConfiguration/data'
import { EventConfigStoreState } from '@/types/eventConfig'
import { defineStore } from 'pinia'

const defaultPagination = {
  page: 1,
  pageSize: 10,
  total: 0
}

export const useEventConfigStore = defineStore('useEventConfigStore', {
  state: (): EventConfigStoreState => ({
    sources: [],
    sourcesPagination: { ...defaultPagination },
    isLoading: false,
    activeTab: 0,
    uploadedFilesReportModalState: {
      visible: false
    }
  }),
  actions: {
    async fetchEventConfigs() {
      this.isLoading = true
      try {
        this.sources = eventConfigSources // Using static data for now
        this.sourcesPagination.total = this.sources.length
      } catch (error) {
        console.error('Error fetching event configurations:', error)
      } finally {
        this.isLoading = false
      }
    },
    onSourcePageChange(page: number) {
      this.sourcesPagination.page = page
    },
    onSourcePageSizeChange(pageSize: number) {
      this.sourcesPagination.pageSize = pageSize
    },
    resetActiveTab(){
      this.activeTab = 0
    }
  }
})

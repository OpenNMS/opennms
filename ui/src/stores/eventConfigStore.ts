import { eventConfigSources } from '@/components/EventConfiguration/data'
import { EventConfigStoreState, EventConfSourceMetadata } from '@/types/eventConfig'
import { cloneDeep } from 'lodash'
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
    },
    deleteEventConfigSourceModalState: {
      visible: false,
      eventConfigSource: null
    }
  }),
  actions: {
    async fetchEventConfigs() {
      this.isLoading = true
      try {
        this.sources = cloneDeep(eventConfigSources) // Using static data for now
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
    },
    showDeleteEventConfigSourceModal(eventConfigSource: EventConfSourceMetadata) {
      this.deleteEventConfigSourceModalState.visible = true
      this.deleteEventConfigSourceModalState.eventConfigSource = eventConfigSource
    },
    hideDeleteEventConfigSourceModal() {
      this.deleteEventConfigSourceModalState.visible = false
      this.deleteEventConfigSourceModalState.eventConfigSource = null
    }
  }
})

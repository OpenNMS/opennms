import { data } from '@/components/EventConfiguration/data'
import { EventConfigState, EventConfSourceMetadata } from '@/types/eventConfig'
import { defineStore } from 'pinia'

export const useEventConfigStore = defineStore('eventConfigStore', {
  state: (): EventConfigState => ({
    eventConfigs: [],
    eventConfigPagination: {
      page: 1,
      pageSize: 10,
      total: 0
    },
    selectedEventConfig: null,
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
        this.eventConfigs = data // Using static data for now
        this.eventConfigPagination.total = this.eventConfigs.length
      } catch (error) {
        console.error('Error fetching event configurations:', error)
      } finally {
        this.isLoading = false
      }
    },
    selectEventConfig(eventConfig: EventConfSourceMetadata) {
      this.selectedEventConfig = eventConfig
    },
    onEventConfigPageChange(page: number) {
      this.eventConfigPagination.page = page
    },
    onEventConfigPageSizeChange(pageSize: number) {
      this.eventConfigPagination.pageSize = pageSize
    },
    resetActiveTab(){
      this.activeTab = 0
    },
    showDeleteEventConfigSourceModal(eventConfigSource: EventConfSourceMetadata) {
      this.deleteEventConfigSourceModalState.visible = true
      this.deleteEventConfigSourceModalState.eventConfigSource = eventConfigSource
    },
    hideDeleteEventConfigSourceModal() {
      console.log('hiding modal')
      this.deleteEventConfigSourceModalState.visible = false
      this.deleteEventConfigSourceModalState.eventConfigSource = null
    }
  }
})

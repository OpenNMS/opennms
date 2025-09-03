import { eventConfigSources } from '@/components/EventConfiguration/data'
import { changeEventConfigSourceStatus, filterEventConfigSources } from '@/services/eventConfigService'
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
    uploadedEventConfigFilesReportDialogState: {
      visible: false
    },
    deleteEventConfigSourceDialogState: {
      visible: false,
      eventConfigSource: null
    },
    changeEventConfigSourceStatusDialogState: {
      visible: false,
      eventConfigSource: null
    }
  }),
  actions: {
    async fetchEventConfigs() {
      this.isLoading = true
      try {
        const response = await filterEventConfigSources(
          (this.sourcesPagination.page - 1) * this.sourcesPagination.pageSize,
          this.sourcesPagination.pageSize,
          this.sourcesPagination.total,
          ''
        )
        console.log('response', response)
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
    resetActiveTab() {
      this.activeTab = 0
    },
    showDeleteEventConfigSourceModal(eventConfigSource: EventConfSourceMetadata) {
      this.deleteEventConfigSourceDialogState.visible = true
      this.deleteEventConfigSourceDialogState.eventConfigSource = eventConfigSource
    },
    hideDeleteEventConfigSourceModal() {
      this.deleteEventConfigSourceDialogState.visible = false
      this.deleteEventConfigSourceDialogState.eventConfigSource = null
    },
    resetSourcesPagination() {
      this.sourcesPagination = { ...defaultPagination }
    },
    showChangeEventConfigSourceStatusDialog(eventConfigSource: EventConfSourceMetadata) {
      this.changeEventConfigSourceStatusDialogState.visible = true
      this.changeEventConfigSourceStatusDialogState.eventConfigSource = eventConfigSource
    },
    hideChangeEventConfigSourceStatusDialog() {
      this.changeEventConfigSourceStatusDialogState.visible = false
      this.changeEventConfigSourceStatusDialogState.eventConfigSource = null
    },
    async disableEventConfigSource(sourceId: number) {
      if (sourceId) {
        const response = await changeEventConfigSourceStatus(sourceId, false)
        if (response) {
          await this.fetchEventConfigs()
        }
      } else {
        console.error('No source selected')
      }
    },
    async enableEventConfigSource(sourceId: number) {
      if (sourceId) {
        const response = await changeEventConfigSourceStatus(sourceId, true)
        if (response) {
          await this.fetchEventConfigs()
        }
      } else {
        console.error('No source selected')
      }
    }
  }
})


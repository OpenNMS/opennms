import { changeEventConfigSourceStatus, filterEventConfigSources } from '@/services/eventConfigService'
import { EventConfigSource, EventConfigStoreState } from '@/types/eventConfig'
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
    sourcesSearchTerm: '',
    sourcesSorting: {
      sortOrder: 'desc',
      sortKey: 'createdTime'
    },
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
          this.sourcesSearchTerm,
          this.sourcesSorting.sortKey,
          this.sourcesSorting.sortOrder
        )
        this.sources = response.sources
        this.sourcesPagination.total = response.totalRecords
        this.isLoading = false
      } catch (error) {
        console.error('Error fetching event configurations:', error)
        this.isLoading = false
      }
    },
    async onSourcePageChange(page: number) {
      this.sourcesPagination.page = page
      await this.fetchEventConfigs()
    },
    async onSourcePageSizeChange(pageSize: number) {
      this.sourcesPagination.page = 1
      this.sourcesPagination.pageSize = pageSize
      await this.fetchEventConfigs()
    },
    async onChangeSourcesSearchTerm(value: string) {
      this.sourcesSearchTerm = value ?? ''
      this.sourcesPagination.page = 1
      await this.fetchEventConfigs()
    },
    async onSourcesSortChange(sortKey: string, sortOrder: string) {
      this.sourcesSorting.sortKey = sortKey
      this.sourcesSorting.sortOrder = sortOrder
      await this.fetchEventConfigs()
    },
    resetActiveTab() {
      this.activeTab = 0
    },
    showDeleteEventConfigSourceModal(eventConfigSource: EventConfigSource) {
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
    async refreshEventsSources() {
      this.resetSourcesPagination()
      this.sourcesSearchTerm = ''
      this.sourcesSorting.sortKey = 'createdTime'
      this.sourcesSorting.sortOrder = 'desc'
      await this.fetchEventConfigs()
    },
    showChangeEventConfigSourceStatusDialog(eventConfigSource: EventConfigSource) {
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


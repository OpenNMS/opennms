import { EVENT_CONF_CATCH_ALL_SOURCE } from '@/lib/utils'
import {
  changeEventConfigSourceStatus,
  filterEventConfigSources,
  getAllSourceNames,
  getOrderedEventConfigSources,
  updateEventConfigSourcesOrder
} from '@/services/eventConfigService'
import { EventConfigMutationResult, EventConfigSource, EventConfigStoreState } from '@/types/eventConfig'
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
      sortOrder: 'asc',
      sortKey: 'evaluationOrder'
    },
    isLoading: false,
    activeTab: 0,
    uploadedSources: [],
    orderedSources: [],
    catchAllSource: null,
    isSavingSourceOrder: false,
    reorderSourcesDrawerState: {
      visible: false
    },
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
    },
    createEventConfigSourceDialogState: {
      visible: false
    }
  }),
  actions: {
    async fetchAllSourcesNames() {
      this.isLoading = true
      try {
        const response = await getAllSourceNames()
        this.uploadedSources = response
        this.isLoading = false
      } catch (error) {
        console.error('Error fetching all event configuration source names:', error)
        this.isLoading = false
      }
    },
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
        await this.fetchAllSourcesNames()
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
    async refreshSourcesFilters() {
      this.resetSourcesPagination()
      this.sourcesSearchTerm = ''
      this.sourcesSorting.sortKey = 'evaluationOrder'
      this.sourcesSorting.sortOrder = 'asc'
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
        throw new Error('No source selected')
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
        throw new Error('No source selected')
      }
    },
    showCreateEventConfigSourceDialog() {
      this.createEventConfigSourceDialogState.visible = true
    },
    hideCreateEventConfigSourceDialog() {
      this.createEventConfigSourceDialogState.visible = false
    },
    async showReorderSourcesDrawer() {
      this.reorderSourcesDrawerState.visible = true
      await this.fetchOrderedSources()
    },
    hideReorderSourcesDrawer() {
      this.reorderSourcesDrawerState.visible = false
    },
    async fetchOrderedSources() {
      this.isLoading = true
      try {
        const sources = await getOrderedEventConfigSources()
        this.orderedSources = sources.filter(source => source.name !== EVENT_CONF_CATCH_ALL_SOURCE)
        this.catchAllSource = sources.find(source => source.name === EVENT_CONF_CATCH_ALL_SOURCE) ?? null
      } catch (error) {
        console.error('Error fetching ordered event configuration sources:', error)
        this.orderedSources = []
        this.catchAllSource = null
      } finally {
        this.isLoading = false
      }
    },
    async saveSourcesOrder(sourceIds: number[]): Promise<EventConfigMutationResult> {
      this.isSavingSourceOrder = true
      try {
        const result = await updateEventConfigSourcesOrder(sourceIds)
        if (result.ok) {
          await this.fetchEventConfigs()
        }
        return result
      } finally {
        this.isSavingSourceOrder = false
      }
    }
  }
})

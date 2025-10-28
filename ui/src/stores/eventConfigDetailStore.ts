import { Severity } from '@/components/EventConfigEventCreate/constants'
import {
  changeEventConfigEventStatus,
  changeEventConfigSourceStatus,
  filterEventConfigEvents
} from '@/services/eventConfigService'
import {
  EventConfigDetailStoreState,
  EventConfigEvent,
  EventConfigSource
} from '@/types/eventConfig'
import { defineStore } from 'pinia'

const defaultPagination = {
  page: 1,
  pageSize: 10,
  total: 0
}

export const getDefaultEventConfigEvent = (): EventConfigEvent => ({
  id: new Date().getTime(), // Temporary ID for new events
  uei: '',
  eventLabel: '',
  description: '',
  severity: Severity.Normal,
  enabled: true,
  xmlContent: '',
  createdTime: new Date(),
  lastModified: new Date(),
  modifiedBy: '',
  sourceName: '',
  vendor: '',
  fileOrder: 0
})

export const useEventConfigDetailStore = defineStore('useEventConfigDetailStore', {
  state: (): EventConfigDetailStoreState => ({
    events: [],
    eventsPagination: { ...defaultPagination },
    eventsSearchTerm: '',
    eventsSorting: {
      sortOrder: 'desc',
      sortKey: 'createdTime'
    },
    selectedSource: null,
    isLoading: false,
    deleteEventConfigEventDialogState: {
      visible: false,
      eventConfigEvent: null
    },
    changeEventConfigEventStatusDialogState: {
      visible: false,
      eventConfigEvent: null
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
    async fetchEventsBySourceId() {
      if (!this.selectedSource) {
        console.error('No source selected')
        return
      }
      this.isLoading = true
      const id = this.selectedSource.id
      try {
        const response = await filterEventConfigEvents(
          id,
          (this.eventsPagination.page - 1) * this.eventsPagination.pageSize,
          this.eventsPagination.pageSize,
          this.eventsSearchTerm,
          this.eventsSorting.sortKey,
          this.eventsSorting.sortOrder
        )
        this.events = response.events
        this.eventsPagination.total = response.totalRecords
        this.isLoading = false
      } catch (error) {
        console.error('Error fetching events for source ID:', id, error)
        this.isLoading = false
      }
    },
    setSelectedEventConfigSource(eventConfigSource: EventConfigSource) {
      this.selectedSource = eventConfigSource
    },
    async onEventsPageChange(page: number) {
      this.eventsPagination.page = page
      await this.fetchEventsBySourceId()
    },
    async onEventsPageSizeChange(pageSize: number) {
      this.eventsPagination.page = 1
      this.eventsPagination.pageSize = pageSize
      await this.fetchEventsBySourceId()
    },
    async onChangeEventsSearchTerm(value: string) {
      this.eventsSearchTerm = value ?? ''
      this.eventsPagination.page = 1
      await this.fetchEventsBySourceId()
    },
    async onEventsSortChange(sortKey: string, sortOrder: string) {
      this.eventsSorting.sortKey = sortKey
      this.eventsSorting.sortOrder = sortOrder
      await this.fetchEventsBySourceId()
    },
    async refreshEventConfigEvents() {
      this.resetEventsPagination()
      this.eventsSearchTerm = ''
      this.eventsSorting.sortKey = 'createdTime'
      this.eventsSorting.sortOrder = 'desc'
      await this.fetchEventsBySourceId()
    },
    showDeleteEventConfigEventDialog(eventConfigSource: EventConfigEvent) {
      this.deleteEventConfigEventDialogState.visible = true
      this.deleteEventConfigEventDialogState.eventConfigEvent = eventConfigSource
    },
    hideDeleteEventConfigEventDialog() {
      this.deleteEventConfigEventDialogState.visible = false
      this.deleteEventConfigEventDialogState.eventConfigEvent = null
    },
    async disableEventConfigEvent(eventId: number) {
      if (this.selectedSource) {
        const response = await changeEventConfigEventStatus(eventId, this.selectedSource.id, false)
        if (response) {
          await this.fetchEventsBySourceId()
        }
      } else {
        console.error('No source selected')
      }
    },
    async enableEventConfigEvent(eventId: number) {
      if (this.selectedSource) {
        const response = await changeEventConfigEventStatus(eventId, this.selectedSource.id, true)
        if (response) {
          await this.fetchEventsBySourceId()
        }
      } else {
        console.error('No source selected')
      }
    },
    showChangeEventConfigEventStatusDialog(eventConfigEvent: EventConfigEvent) {
      this.changeEventConfigEventStatusDialogState.eventConfigEvent = eventConfigEvent
      this.changeEventConfigEventStatusDialogState.visible = true
    },
    async hideChangeEventConfigEventStatusDialog() {
      this.changeEventConfigEventStatusDialogState.visible = false
      this.changeEventConfigEventStatusDialogState.eventConfigEvent = null
      await this.fetchEventsBySourceId()
    },
    resetEventsPagination() {
      this.eventsPagination = { ...defaultPagination }
    },
    showDeleteEventConfigSourceDialog(eventConfigSource: EventConfigSource) {
      this.deleteEventConfigSourceDialogState.visible = true
      this.deleteEventConfigSourceDialogState.eventConfigSource = eventConfigSource
    },
    async hideDeleteEventConfigSourceDialog() {
      this.deleteEventConfigSourceDialogState.eventConfigSource = null
      this.deleteEventConfigSourceDialogState.visible = false
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
      if (sourceId && this.selectedSource) {
        const response = await changeEventConfigSourceStatus(sourceId, false)
        if (response) {
          this.selectedSource.enabled = false
          await this.fetchEventsBySourceId()
        }
      } else {
        console.error('No source selected')
        throw new Error('No source selected')
      }
    },
    async enableEventConfigSource(sourceId: number) {
      if (sourceId && this.selectedSource) {
        const response = await changeEventConfigSourceStatus(sourceId, true)
        if (response) {
          this.selectedSource.enabled = true
          await this.fetchEventsBySourceId()
        }
      } else {
        console.error('No source selected')
        throw new Error('No source selected')
      }
    }
  }
})


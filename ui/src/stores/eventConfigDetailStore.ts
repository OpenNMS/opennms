import { eventConfigEvents } from '@/components/EventConfiguration/data'
import { changeEventConfigEventStatus, changeEventConfigSourceStatus } from '@/services/eventConfigService'
import { DrawerState, EventConfigDetailStoreState, EventConfigEvent, EventConfigSource } from '@/types/eventConfig'
import { cloneDeep } from 'lodash'
import { defineStore } from 'pinia'

const defaultPagination = {
  page: 1,
  pageSize: 10,
  total: 0
}

const getDefaultDrawerState = (): DrawerState => {
  return {
    visible: false,
    isEventEditorModal: false
  }
}

export const useEventConfigDetailStore = defineStore('useEventConfigDetailStore', {
  state: (): EventConfigDetailStoreState => ({
    events: [],
    eventsPagination: { ...defaultPagination },
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
    },
    drawerState: getDefaultDrawerState()
  }),
  actions: {
    async fetchEventsBySourceId() {
      this.isLoading = true
      const id = this.selectedSource?.id
      try {
        console.log('Fetching events for source ID:', this.events)

        this.events = cloneDeep(eventConfigEvents) // Using static data for now
        this.eventsPagination.total = this.events.length
      } catch (error) {
        console.error('Error fetching events for source ID:', id, error)
      } finally {
        this.isLoading = false
      }
    },
    setSelectedEventConfigSource(eventConfigSource: EventConfigSource) {
      this.selectedSource = eventConfigSource
    },
    onEventsPageChange(page: number) {
      this.eventsPagination.page = page
    },
    onEventsPageSizeChange(pageSize: number) {
      this.eventsPagination.pageSize = pageSize
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
    hideDeleteEventConfigSourceDialog() {
      this.deleteEventConfigSourceDialogState.visible = false
      this.deleteEventConfigSourceDialogState.eventConfigSource = null
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
        }
      } else {
        console.error('No source selected')
      }
    },
    async enableEventConfigSource(sourceId: number) {
      if (sourceId && this.selectedSource) {
        const response = await changeEventConfigSourceStatus(sourceId, true)
        if (response) {
          this.selectedSource.enabled = true
        }
      } else {
        console.error('No source selected')
      }
    },
    openEventDrawerModal() {
      this.drawerState.visible  = true      
    },
    closeEventDrawerModal() {
      this.drawerState.visible  = false
    }
  }
})


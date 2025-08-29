import { eventConfigEvents } from '@/components/EventConfiguration/data'
import { changeEventConfigEventStatus } from '@/services/eventConfigService'
import { EventConfigDetailStoreState, EventConfigEvent, EventConfSourceMetadata } from '@/types/eventConfig'
import { defineStore } from 'pinia'

const defaultPagination = {
  page: 1,
  pageSize: 10,
  total: 0
}

export const useEventConfigDetailStore = defineStore('useEventConfigDetailStore', {
  state: (): EventConfigDetailStoreState => ({
    events: [],
    eventsPagination: { ...defaultPagination },
    selectedSource: null,
    isLoading: false,
    deleteEventConfigEventModalState: {
      visible: false,
      eventConfigEvent: null
    }
  }),
  actions: {
    async fetchEventsBySourceId() {
      this.isLoading = true
      const id = this.selectedSource?.id
      try {
        this.events = eventConfigEvents // Using static data for now
        this.eventsPagination.total = this.events.length
      } catch (error) {
        console.error('Error fetching events for source ID:', id, error)
      } finally {
        this.isLoading = false
      }
    },
    setSelectedEventConfigSource(eventConfigSource: EventConfSourceMetadata) {
      this.selectedSource = eventConfigSource
    },
    onEventsPageChange(page: number) {
      this.eventsPagination.page = page
    },
    onEventsPageSizeChange(pageSize: number) {
      this.eventsPagination.pageSize = pageSize
    },
    showDeleteEventConfigEventModal(eventConfigSource: EventConfigEvent) {
      this.deleteEventConfigEventModalState.visible = true
      this.deleteEventConfigEventModalState.eventConfigEvent = eventConfigSource
    },
    hideDeleteEventConfigEventModal() {
      this.deleteEventConfigEventModalState.visible = false
      this.deleteEventConfigEventModalState.eventConfigEvent = null
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
    }
  }
})


import { eventConfigEvents } from '@/components/EventConfiguration/data'
import { EventConfigDetailStoreState, EventConfSourceMetadata } from '@/types/eventConfig'
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
    isLoading: false
  }),
  actions: {
    async fetchEventsBySourceId(id: number) {
      this.isLoading = true
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
    }
  }
})
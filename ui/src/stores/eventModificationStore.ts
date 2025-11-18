import { getEventConfSourceById } from '@/services/eventConfigService'
import { CreateEditMode } from '@/types'
import { EventConfigEvent, EventConfigSource, EventModificationStoreState } from '@/types/eventConfig'
import { defineStore } from 'pinia'

export const useEventModificationStore = defineStore('useEventModificationStore', {
  state: (): EventModificationStoreState => ({
    selectedSource: null,
    eventModificationState: {
      isEditMode: CreateEditMode.None,
      eventConfigEvent: null
    }
  }),
  actions: {
    async fetchSourceById(id: string) {
      try {
        const response = await getEventConfSourceById(id)
        this.selectedSource = response
      } catch (error) {
        console.error('Error fetching source by ID:', id, error)
      }
    },
    setSelectedEventConfigSource(
      eventConfigSource: EventConfigSource,
      isEditMode: CreateEditMode,
      eventConfigEvent: EventConfigEvent | null
    ) {
      this.selectedSource = eventConfigSource
      this.eventModificationState.isEditMode = isEditMode
      this.eventModificationState.eventConfigEvent = eventConfigEvent
    },
    resetEventModificationState() {
      this.selectedSource = null
      this.eventModificationState.isEditMode = CreateEditMode.None
      this.eventModificationState.eventConfigEvent = null
    }
  }
})


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
    setSelectedEventConfigSource(eventConfigSource: EventConfigSource, isEditMode: CreateEditMode, eventConfigEvent: EventConfigEvent | null) {
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


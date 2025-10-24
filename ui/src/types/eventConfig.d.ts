import { CreateEditMode, Pagination, Sorting } from '.'

export type EventConfigSource = {
  id: number
  name: string
  vendor: string
  description: string
  enabled: boolean
  eventCount: number
  fileOrder: number
  uploadedBy: string
  createdTime: Date
  lastModified: Date
}

export type EventConfigEvent = {
  id: number
  uei: string
  eventLabel: string
  description: string
  severity: string
  enabled: boolean
  xmlContent: string
  createdTime: Date
  lastModified: Date
  modifiedBy: string
  sourceName: string
  vendor: string
  fileOrder: number
}

export type EventConfigEventRequest = {
  uei: string
  ['event-label']: string
  descr: string
  severity: string
}

export type EventConfigStoreState = {
  sources: EventConfigSource[]
  sourcesPagination: Pagination
  sourcesSearchTerm: string
  sourcesSorting: Sorting
  isLoading: boolean
  activeTab: number
  uploadedSourceNames: string[]
  uploadedEventConfigFilesReportDialogState: {
    visible: boolean
  }
  deleteEventConfigSourceDialogState: {
    visible: boolean
    eventConfigSource: EventConfigSource | null
  }
  changeEventConfigSourceStatusDialogState: {
    visible: boolean
    eventConfigSource: EventConfigSource | null
  }
}

export type EventConfigDetailStoreState = {
  events: EventConfigEvent[]
  eventsPagination: Pagination
  eventsSearchTerm: string
  eventsSorting: Sorting
  selectedSource: EventConfigSource | null
  eventModificationDrawerState: {
    visible: boolean
    isEditMode: CreateEditMode
    eventConfigEvent: EventConfigEvent | null
  }
  isLoading: boolean
  deleteEventConfigEventDialogState: {
    visible: boolean
    eventConfigEvent: EventConfigEvent | null
  }
  changeEventConfigEventStatusDialogState: {
    visible: boolean
    eventConfigEvent: EventConfigEvent | null
  }
  deleteEventConfigSourceDialogState: {
    visible: boolean
    eventConfigSource: EventConfigSource | null
  }
  changeEventConfigSourceStatusDialogState: {
    visible: boolean
    eventConfigSource: EventConfigSource | null
  }
}

export type EventConfigFilesUploadResponse = {
  errors: [
    {
      file: string
      error: string
    }
  ]
  success: [
    {
      file: string
    }
  ]
}

export type EventConfigSourcesResponse = {
  sources: EventConfigSource[]
  totalRecords: number
}

export type EventConfigEventsResponse = {
  events: EventConfigEvent[]
  totalRecords: number
}

export interface DrawerState {
  visible: boolean
  isEventEditorModal: boolean
}

export type UploadEventFileType = {
  file: File
  isValid: boolean
  errors: string[]
  isDuplicate: boolean
}

export type EventModificationStoreState = {
  selectedSource: EventConfigSource | null
  eventModificationState: {
    visible: boolean
    isEditMode: CreateEditMode
    eventConfigEvent: EventConfigEvent | null
  }
}

export type EventFormErrors = {
  uei?: string
  eventLabel?: string
  description?: string
  severity?: string
  logmsg?: string
  dest?: string
  reductionKey?: string
  alarmType?: string
  clearKey?: string
}

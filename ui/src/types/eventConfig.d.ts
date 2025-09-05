import { Pagination, Sorting } from '.'

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
  enabled: boolean
  xmlContent: string
  createdTime: Date
  lastModified: Date
  modifiedBy: string
  sourceName: string
  vendor: string
  fileOrder: number
}

export type EventConfigStoreState = {
  sources: EventConfigSource[]
  sourcesPagination: Pagination
  sourcesSearchTerm: string
  sourcesSorting: Sorting
  isLoading: boolean
  activeTab: number
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
  selectedSource: EventConfigSource | null
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
  drawerState: DrawerState
}

export type EventConfigFilesUploadReponse = {
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
  invalid?: {
    file: string
    reason: string
  }[]
}

export type EventConfigSourcesResponse = {
  sources: EventConfigSource[]
  totalRecords: number
}
export interface DrawerState {
  visible: boolean
  isEventEditorModal: boolean
}

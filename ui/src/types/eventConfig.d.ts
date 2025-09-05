import { Pagination, Sorting } from '.'

export type EventConfSource = {
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

export type EventConfEvent = {
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
  sources: EventConfSource[]
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
    eventConfigSource: EventConfSource | null
  }
  changeEventConfigSourceStatusDialogState: {
    visible: boolean
    eventConfigSource: EventConfSource | null
  }
}

export type EventConfigDetailStoreState = {
  events: EventConfigEvent[]
  eventsPagination: Pagination
  selectedSource: EventConfSource | null
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
    eventConfigSource: EventConfSource | null
  }
  changeEventConfigSourceStatusDialogState: {
    visible: boolean
    eventConfigSource: EventConfSource | null
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

export type EventConfSourcesResponse = {
  sources: EventConfSource[]
  totalRecords: number
}
export interface DrawerState {
  visible: boolean
  isEventEditorModal: boolean
}
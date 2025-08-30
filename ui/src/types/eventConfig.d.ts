import { Pagination } from '.'

export type EventConfSourceMetadata = {
  filename: string
  eventCount: number
  fileOrder: number
  username: string
  now: Date
  vendor: string
  description: string
  id: number
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
  sources: EventConfSourceMetadata[]
  sourcesPagination: Pagination
  isLoading: boolean
  activeTab: number
  uploadedEventConfigFilesReportDialogState: {
    visible: boolean
  },
  deleteEventConfigSourceDialogState: {
    visible: boolean,
    eventConfigSource: EventConfSourceMetadata | null
  }
}

export type EventConfigDetailStoreState = {
  events: EventConfigEvent[]
  eventsPagination: Pagination
  selectedSource: EventConfSourceMetadata | null
  isLoading: boolean,
  deleteEventConfigEventDialogState: {
    visible: boolean,
    eventConfigEvent: EventConfigEvent | null
  },
  changeEventConfigEventStatusDialogState: {
    visible: boolean,
    eventConfigEvent: EventConfigEvent | null,
    
  }
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
  ],
   invalid?: {
    file: string;
    reason: string;
  }[];
}


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

export type EventConfigState = {
  eventConfigs: EventConfSourceMetadata[]
  eventConfigPagination: Pagination
  selectedEventConfig: EventConfSourceMetadata | null
  isLoading: boolean
  activeTab: number
  uploadedFilesReportModalState: {
    visible: boolean
  },
  deleteEventConfigSourceModalState: {
    visible: boolean,
    eventConfigSource: EventConfSourceMetadata | null
  }
}

export type EventConfigFilesUploadReponse = {
  errors: [
    {
      file: string,
      error: string
    }
  ]
  success: [
    {
      file: string
    }
  ]
}

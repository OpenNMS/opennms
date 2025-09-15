import {
  EventConfigEvent,
  EventConfigEventsResponse,
  EventConfigFilesUploadResponse,
  EventConfigSource,
  EventConfigSourcesResponse
} from '@/types/eventConfig'

export const mapUploadedEventConfigFilesResponseFromServer = (response: any): EventConfigFilesUploadResponse => {
  return {
    errors: response.errors.map((err: any) => ({
      file: err.file,
      error: err.error
    })),
    success: response.success.map((success: any) => ({
      file: success.file
    }))
  }
}

export const mapEventConfigSourceFromServer = (source: any): EventConfigSource => {
  return {
    id: source.id,
    name: source.name,
    description: source.description,
    enabled: source.enabled,
    eventCount: source.eventCount,
    fileOrder: source.fileOrder,
    vendor: source.vendor,
    uploadedBy: source.uploadedBy,
    createdTime: new Date(source.createdTime),
    lastModified: new Date(source.lastModified)
  }
}

export const mapEventConfSourceResponseFromServer = (response: any): EventConfigSourcesResponse => {
  return {
    sources: response.eventConfSourceList.map((source: any) => mapEventConfigSourceFromServer(source)),
    totalRecords: response.totalRecords
  }
}

export const mapEventConfigEventFromServer = (event: any): EventConfigEvent => {
  return {
    id: event.id,
    uei: event.uei,
    eventLabel: event.eventLabel,
    description: event.description,
    enabled: event.enabled,
    xmlContent: event.xmlContent,
    createdTime: new Date(event.createdTime),
    lastModified: new Date(event.lastModified),
    modifiedBy: event.modifiedBy,
    sourceName: event.sourceName,
    vendor: event.vendor,
    fileOrder: event.fileOrder
  }
}

export const mapEventConfigEventsResponseFromServer = (response: any): EventConfigEventsResponse => {
  return {
    events: response.eventConfEventList.map((event: any) => mapEventConfigEventFromServer(event)),
    totalRecords: response.totalRecords
  }
}

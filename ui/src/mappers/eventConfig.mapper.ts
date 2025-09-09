import { EventConfigFilesUploadReponse, EventConfigSource, EventConfigSourcesResponse } from '@/types/eventConfig'

export const mapUploadedEventConfigFilesResponseFromServer = (response: any): EventConfigFilesUploadReponse => {
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


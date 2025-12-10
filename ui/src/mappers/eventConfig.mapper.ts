import {
  EventConfigEvent,
  EventConfigEventsResponse,
  EventConfigFilesUploadResponse,
  EventConfigSource,
  EventConfigSourcesResponse
} from '@/types/eventConfig'
import vkbeautify from 'vkbeautify'

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

const extractSeverity = (xmlContent: string): string | null => {
  let severity: string | null

  if (xmlContent) {
    try {
      const parser = new DOMParser()
      const xmlDoc = parser.parseFromString(xmlContent, 'application/xml')
      const severityElement = xmlDoc.getElementsByTagName('severity')[0]
      severity = severityElement ? severityElement.textContent : null
      return severity
    } catch (e) {
      severity = null
      return severity
    }
  } else {
    severity = null
    return severity
  }
}

export const mapEventConfigEventFromServer = (event: any): EventConfigEvent => {
  return {
    id: event.id,
    uei: event.uei,
    eventLabel: event.eventLabel,
    description: event.description,
    severity: extractSeverity(event.xmlContent) || '',
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
    events: response.eventConfSourceList.map((event: any) => mapEventConfigEventFromServer(event)),
    totalRecords: response.totalRecords
  }
}

export const mapEventConfEventEditRequest = (content: any, status: boolean): string => {
  return vkbeautify.xml(`<eventEdit><enabled>${status}</enabled>${content as string}</eventEdit>`)
}


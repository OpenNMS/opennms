import {
  EventConfigEvent,
  EventConfigEventJsonStructure,
  EventConfigEventsResponse,
  EventConfigFilesUploadResponse,
  EventConfigSource,
  EventConfigSourcesResponse
} from '@/types/eventConfig'
import vkbeautify from 'vkbeautify'

const mapEventConfEventFromServer = (event: any): EventConfigEventJsonStructure => {
  const payload = {} as EventConfigEventJsonStructure
  payload.uei = event.uei
  payload.eventLabel = event.eventLabel
  payload.descr = event.descr
  payload.operinstruct = event.operinstruct

  if (Object.keys(event).includes('alarmData')) {
    payload.alarmData = {
      alarmType: event.alarmData.alarmType || 1,
      reductionKey: event.alarmData.reductionKey || '',
      autoClean: event.alarmData.autoClean || false,
      clearKey: event.alarmData.clearKey || ''
    }
  }

  payload.logmsg = {
    dest: event.logmsg?.dest || '',
    content: event.logmsg?.content || ''
  }

  payload.severity = event.severity

  if (Object.keys(event).includes('mask')) {
    if (Object.keys(event.mask).includes('maskelements')) {
      payload.mask = {
        maskelements: event.mask?.maskelements?.map((me: any) => ({
          mename: me.mename,
          mevalue: me.mevalues?.[0] || ''
        }))
      }
    }
    if (Object.keys(event.mask).includes('varbinds')) {
      payload.mask = {
        ...payload.mask,
        varbinds: event.mask?.varbinds
          ?.map((vb: any) => {
            if (Object.keys(vb).includes('vbnumber')) {
              return {
                vbnumber: vb.vbnumber,
                vbvalues: vb.vbvalues[0] || ''
              }
            }
            if (Object.keys(vb).includes('vboid')) {
              return {
                vboid: vb.vboid,
                vbvalues: vb.vbvalues[0] || ''
              }
            }
            return undefined
          })
          ?.filter((vbMapped: any) => vbMapped !== undefined)
      }
    }
  }

  if (Object.keys(event).includes('varbindsdecodes')) {
    payload.varbindsdecodes = event.varbindsdecodes?.map((vbd: any) => ({
      parmId: vbd.parmid,
      decode: vbd.decodes?.map((dec: any) => ({
        varbindvalue: dec.varbindvalue,
        varbinddecodedstring: dec.varbinddecodedstring
      }))
    }))
  }

  return payload
}

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
    jsonContent: mapEventConfEventFromServer(JSON.parse(event.content)),
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


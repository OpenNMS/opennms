import {
  EventConfigEvent,
  EventConfigEventJsonStructure,
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
    fileOrder: event.fileOrder,
    jsonContent: mapToEventConfEventJsonStructureFromServer(event.jsonContent)
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

export const mapToEventConfEventJsonStructureFromServer = (input: any): EventConfigEventJsonStructure => {
  return {
    mask: input?.mask
      ? {
        maskelement: Array.isArray(input.mask.maskelement)
          ? input.mask.maskelement.map((me: any) => ({
            mename: String(me?.mename || ''),
            mevalue: String(me?.mevalue || '')
          }))
          : [],
        varbind: Array.isArray(input.mask.varbind)
          ? input.mask.varbind.map((vb: any) => ({
            vbnumber: Number(vb?.vbnumber || 0),
            vbvalue: String(vb?.vbvalue || '')
          }))
          : []
      }
      : undefined,
    varbindsdecode: Array.isArray(input.varbindsdecode)
      ? input.varbindsdecode.map((vb: any) => ({
        parmid: vb?.parmid || '',
        decode: Array.isArray(vb?.decode)
          ? vb.decode.map((d: any) => ({
            key: String(d?.key || ''),
            value: String(d?.value || '')
          }))
          : []
      }))
      : [],
    uei: String(input?.uei || ''),
    'event-label': String(input?.['event-label'] || ''),
    descr: String(input?.descr || ''),
    operinstruct: String(input?.operinstruct || ''),
    logmsg: {
      dest: String(input?.logmsg?.dest || ''),
      content: String(input?.logmsg?.content || '')
    },
    severity: String(input?.severity || ''),
    'alarm-data': input?.['alarm-data']
      ? {
        'reduction-key': String(input['alarm-data']['reduction-key'] || ''),
        'alarm-type': input['alarm-data']['alarm-type'] || '',
        'auto-clean': Boolean(input['alarm-data']['auto-clean']),
        'clear-key': input['alarm-data']['clear-key'] ? String(input['alarm-data']['clear-key']) : undefined
      }
      : undefined
  }
}

export const mapToEventConfEventJsonStructureFromClient = (
  uei: string,
  eventLabel: string,
  description: string,
  operatorInstructions: string,
  severity: string,
  dest: string,
  logmsg: string,
  addAlarmData: boolean,
  reductionKey: string,
  alarmType: string,
  autoClean: boolean,
  clearKey: string,
  maskElements: Array<{ name?: { _text?: string; _value?: string }; value?: string }>,
  varbinds: Array<{ index: string; value: string }>,
  varbindsDecode: Array<{ parmId: string; decode: Array<{ key: string; value: string }> }>
): EventConfigEventJsonStructure => {
  const eventXml: EventConfigEventJsonStructure = {
    uei,
    'event-label': eventLabel,
    descr: description,
    operinstruct: operatorInstructions,
    logmsg: {
      dest: dest.toUpperCase(),
      content: logmsg
    },
    severity
  }

  if (maskElements && maskElements.length > 0) {
    eventXml.mask = {
      maskelement: maskElements.map((me) => ({
        mename: me.name?._value || me.name?._text || '',
        mevalue: me.value || ''
      }))
    }
  }

  if (varbinds && varbinds.length > 0) {
    eventXml.mask = {
      ...(eventXml.mask || {}),
      varbind: varbinds.map((vb) => ({
        vbnumber: Number(vb.index || 0),
        vbvalue: vb.value || ''
      }))
    }
  }

  if (varbindsDecode && varbindsDecode.length > 0) {
    eventXml.varbindsdecode = varbindsDecode.map((vb) => ({
      parmid: vb.parmId,
      decode: vb.decode.map((d) => ({
        key: d.key,
        value: d.value
      }))
    }))
  }

  if (addAlarmData) {
    eventXml['alarm-data'] = {
      'reduction-key': reductionKey,
      'alarm-type': alarmType,
      'auto-clean': autoClean,
      ...(clearKey ? { 'clear-key': clearKey } : {})
    }
  }

  return eventXml
}


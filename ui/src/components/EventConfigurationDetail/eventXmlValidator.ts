import { EventConfigEvent } from '@/types/eventConfig'
import { XMLValidator } from 'fast-xml-parser'

export const validateEventDetailsJson = (
  event: EventConfigEvent,
  eventUei: any,
  eventLabel: any,
  eventDescription: any,
  selectedEventSeverity: any
): { isValid: boolean; error: string } => {
  let isValid = true
  let error = ''
  if (!event) {
    isValid = false
    error = 'No event selected'
  }
  if (!eventUei) {
    isValid = false
    error = 'Event UEI is required'
  }
  if (!eventLabel) {
    isValid = false
    error = 'Event label is required'
  }
  if (!eventDescription) {
    isValid = false
    error = 'Event description is required'
  }
  if (!selectedEventSeverity) {
    isValid = false
    error = 'Event severity is required'
  }

  return {
    isValid,
    error
  }
}

export const validateEventDetailsXml = (
  event: EventConfigEvent,
  xmlContent: string
): { isValid: boolean; error: string } => {
  let isValid = true
  let error = ''
  if (!event) {
    isValid = false
    error = 'No event selected'
  }
  if (!xmlContent) {
    return {
      isValid: false,
      error: 'Event XML is required'
    }
  }
  const parser = new DOMParser()
  const xmlDoc = parser.parseFromString(xmlContent, 'application/xml')
  if (xmlDoc.querySelector('parsererror')) {
    return {
      isValid: false,
      error: 'Invalid XML format - file contains syntax errors'
    }
  }
  const result = XMLValidator.validate(xmlContent)
  if (!result) {
    return {
      isValid: false,
      error: 'Invalid XML format - file contains syntax errors'
    }
  }

  console.log(xmlDoc)
  

  const eventElement = xmlDoc.querySelector('event')
  if (!eventElement) {
    return {
      isValid: false,
      error: 'Missing <event> root element'
    }
  }
  if (eventElement) {
    const xmlns = eventElement.getAttribute('xmlns') || ''
    if (xmlns !== 'http://xmlns.opennms.org/xsd/eventconf') {
      return {
        isValid: false,
        error: 'Missing or invalid OpenNMS namespace in <event> element'
      }
    }
  }
  const eventChildren = eventElement?.children
  if (!eventChildren || eventChildren.length === 0) {
    return {
      isValid: false,
      error: 'Empty <event> element'
    }
  } else {
    const uei = eventElement.querySelector('uei')?.textContent?.trim()
    const label = eventElement.querySelector('event-label')?.textContent?.trim()
    const severity = eventElement.querySelector('severity')?.textContent?.trim()
    const description = eventElement.querySelector('descr')?.textContent?.trim()

    if (!uei) {
      return {
        isValid: false,
        error: 'Missing <uei>'
      }
    }
    if (!label) {
      return {
        isValid: false,
        error: 'Missing <event-label>'
      }
    }
    if (!severity) {
      return {
        isValid: false,
        error: 'Missing <severity>'
      }
    }
    if (!description) {
      return {
        isValid: false,
        error: 'Missing <descr>'
      }
    }
  }

  return {
    isValid,
    error
  }
}


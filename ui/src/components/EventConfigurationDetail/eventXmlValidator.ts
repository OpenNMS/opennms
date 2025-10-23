import { EventConfigEvent } from '@/types/eventConfig'
import { XMLValidator } from 'fast-xml-parser'
import { Severity } from '../EventConfigEventCreate/constants'

export const validateEventDetailsJson = (
  event: EventConfigEvent,
  eventUei: any,
  eventLabel: any,
  eventDescription: any,
  selectedEventSeverity: any
): { isValid: boolean; error: string[] } => {
  let isValid = true
  const error = []
  if (!event) {
    isValid = true
    error.push('No event selected')
  }
  if (!eventUei) {
    isValid = true
    error.push('Event UEI is required')
  }
  if (!eventLabel) {
    isValid = true
    error.push('Event label is required')
  }
  if (!eventDescription) {
    isValid = true
    error.push('Event description is required')
  }
  if (!selectedEventSeverity) {
    isValid = true
    error.push('Event severity is required')
  }

  return {
    isValid,
    error
  }
}

export const validateEventDetailsXml = (
  event: EventConfigEvent,
  xmlContent: string
): { isValid: boolean; error: string[] } => {
  let isValid = true
  const error = []
  if (!event) {
    isValid = false
    error.push('No event selected')
  }
  if (!xmlContent) {
    isValid = false
    error.push('Event XML is required')
  }
  const parser = new DOMParser()
  const xmlDoc = parser.parseFromString(xmlContent, 'application/xml')
  if (xmlDoc.querySelector('parsererror')) {
    isValid = false
    error.push('Invalid XML format - file contains syntax errors')
  }
  const result = XMLValidator.validate(xmlContent)
  if (!result) {
    isValid = false
    error.push('Invalid XML format - file contains syntax errors')
  }
  const eventElement = xmlDoc.querySelector('event')
  if (!eventElement) {
    isValid = false
    error.push('Missing <event> root element')
  }
  const xmlns = eventElement?.getAttribute('xmlns') || ''
  if (xmlns !== 'http://xmlns.opennms.org/xsd/eventconf') {
    isValid = false
    error.push('Missing or invalid OpenNMS namespace in <event> element')
  }
  const eventChildren = eventElement?.children
  if (!eventChildren || eventChildren.length === 0) {
    isValid = false
    error.push('Empty <event> element')
  } else {
    const uei = eventElement.querySelector('uei')?.textContent?.trim()
    const label = eventElement.querySelector('event-label')?.textContent?.trim()
    const severity = eventElement.querySelector('severity')?.textContent?.trim()
    const description = eventElement.querySelector('descr')?.textContent?.trim()
    if (!uei) {
      isValid = false
      error.push('Missing <uei>')
    }
    if (!label) {
      isValid = false
      error.push('Missing <event-label>')
    }
    if (!severity) {
      isValid = false
      error.push('Missing <severity>')
    } else if (Severity[severity as keyof typeof Severity] === undefined) {
      isValid = false
      error.push('Invalid <severity> value')
    }
    if (!description) {
      isValid = false
      error.push('Missing <descr>')
    }
  }

  return {
    isValid,
    error
  }
}


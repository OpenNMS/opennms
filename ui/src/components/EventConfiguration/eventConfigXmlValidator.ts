import { UploadEventFileType } from '@/types/eventConfig'

export const MAX_FILES_UPLOAD = 10

export const validateEventConfigFile = async (file: File) => {
  const validationErrors: string[] = []

  try {
    const text = await file.text()

    if (text.trim().length === 0) {
      validationErrors.push('File is empty')
      return { isValid: false, errors: validationErrors }
    }

    if (!file.name.endsWith('.events.xml') && !file.name.includes('event')) {
      validationErrors.push('File does not appear to be an event configuration file (expected .events.xml extension)')
      return { isValid: false, errors: validationErrors }
    }

    if (validationErrors.length === 0) {
      const parser = new DOMParser()
      const xmlDoc = parser.parseFromString(text, 'application/xml')
      if (xmlDoc.querySelector('parsererror')) {
        validationErrors.push('Invalid XML format - file contains syntax errors')
        return { isValid: false, errors: validationErrors }
      }

      const eventsElement = xmlDoc.querySelector('events')
      if (!eventsElement) {
        validationErrors.push('Missing <events> root element')
        return { isValid: false, errors: validationErrors }
      } else {
        const xmlns = eventsElement.getAttribute('xmlns') || ''
        if (xmlns !== 'http://xmlns.opennms.org/xsd/eventconf') {
          validationErrors.push('Missing or invalid OpenNMS namespace in <events> element')
          return { isValid: false, errors: validationErrors }
        }
      }

      const eventElements = eventsElement.querySelectorAll('event')
      const childElements = eventsElement.children
      if (childElements.length && eventElements.length === 0) {
        const childNames = Array.from(childElements)
          .map((el) => `<${el.tagName.toLowerCase()}>`)
          .join(', ')
        validationErrors.push(`<events> element contains ${childNames} but no <event> elements`)
        return { isValid: false, errors: validationErrors }
      } else if (eventElements.length === 0) {
        validationErrors.push('No <event> entries found within <events> element')
        return { isValid: false, errors: validationErrors }
      } else {
        for (const [idx, event] of Array.from(eventElements).entries()) {
          const eventErrors = validateEventElement(event, idx + 1)
          if (eventErrors) {
            validationErrors.push(eventErrors)
            return { isValid: false, errors: validationErrors }
          }
        }
      }
    }
  } catch (error) {
    validationErrors.push(`Error reading file content: ${error instanceof Error ? error.message : 'Unknown error'}`)
    return { isValid: false, errors: validationErrors }
  }
  return {
    isValid: validationErrors.length === 0,
    errors: validationErrors
  }
}

export const validateEventElement = (event: Element, eventNumber: number): string => {
  const uei = event.querySelector('uei')?.textContent?.trim()
  const label = event.querySelector('event-label')?.textContent?.trim()
  const severity = event.querySelector('severity')?.textContent?.trim()

  if (!uei) {
    return `Event ${eventNumber}: missing <uei>`
  }
  if (!label) {
    return `Event ${eventNumber}: missing <event-label>`
  }
  if (!severity) {
    return `Event ${eventNumber}: missing <severity>`
  }
  return ''
}

export const isDuplicateFile = (fileName: string, existingFiles: UploadEventFileType[]): boolean => {
  return !!existingFiles?.some((element) => element.file.name.toLowerCase() === fileName.toLowerCase())
}


export const validateEventConfigFile = async (file: File) => {
  const validationErrors: string[] = []

  try {
    const text = await file.text()

    if (text.trim().length === 0) {
      validationErrors.push('File is empty')
      return { isValid: false, errors: validationErrors }
    }

    const parser = new DOMParser()
    const xmlDoc = parser.parseFromString(text, 'application/xml')

    if (xmlDoc.querySelector('parsererror')) {
      validationErrors.push('Invalid XML format - file contains syntax errors')
      return { isValid: false, errors: validationErrors }
    }

    const eventsElement = xmlDoc.querySelector('events')
    if (!eventsElement) {
      validationErrors.push('Missing <events> root element')
    } else {
      const xmlns = eventsElement.getAttribute('xmlns') || ''
      if (!xmlns.includes('opennms.org')) {
        validationErrors.push('Missing or invalid OpenNMS namespace in <events> element')
      }
    }

    const eventElements = xmlDoc.querySelectorAll('event')
    if (eventElements.length === 0) {
      validationErrors.push('No <event> entries found within <events> element')

      if (eventsElement && eventElements.length === 0) {
        const childElements = eventsElement.children
        if (childElements.length === 0) {
          validationErrors.push('Empty <events> element - no content found')
        } else {
          const childNames = Array.from(childElements)
            .map((el) => `<${el.tagName}>`)
            .join(', ')
          validationErrors.push(`<events> element contains ${childNames} but no <event> elements`)
        }
      }
    } else {
      eventElements.forEach((event, idx) => {
        const eventErrors = validateEventElement(event, idx + 1)
        if (eventErrors.length > 0) {
          validationErrors.push(...eventErrors)
        }
      })
    }

    if (!file.name.endsWith('.events.xml') && !file.name.includes('event')) {
      validationErrors.push('File does not appear to be an event configuration file (expected .events.xml extension)')
    }
  } catch (error) {
    validationErrors.push(`Error reading file content: ${error instanceof Error ? error.message : 'Unknown error'}`)
  }
  return {
    isValid: validationErrors.length === 0,
    errors: validationErrors
  }
}

const validateEventElement = (event: Element, eventNumber: number): string[] => {
  const eventErrors: string[] = []
  const missingFields: string[] = []
  const uei = event.querySelector('uei')?.textContent?.trim()
  const label = event.querySelector('event-label')?.textContent?.trim()
  const severity = event.querySelector('severity')?.textContent?.trim()

  if (!uei) {
    missingFields.push('missing <uei>')
  }
  if (!label) {
    missingFields.push('missing <event-label>')
  }
  if (!severity) {
    missingFields.push('missing <severity>')
  }

  if (missingFields.length > 0) {
    eventErrors.push(`Event ${eventNumber}: ${missingFields.join(', ')}`)
  }

  return eventErrors
}

export const isDuplicateFile = (
  fileName: string,
  existingFiles: File[],
  existingInvalidFiles: { name: string; reason: string }[]
): boolean => {
  return existingFiles.some((f) => f.name === fileName) || existingInvalidFiles.some((f) => f.name === fileName)
}


import { EventFormErrors } from '@/types/eventConfig'

export const validateEvent = (
  uei: string,
  eventLabel: string,
  description: string,
  severity: string,
  dest: string,
  logmsg: string,
  addAlarmData: boolean,
  reductionKey: string,
  alarmType: string,
  autoClean: boolean,
  clearKey: string,
  maskElements: Array<{ name?: { _text?: string; _value?: string }; value?: string }>
): EventFormErrors => {
  const errors: EventFormErrors = {}

  if (!uei || uei.trim() === '') {
    errors.uei = 'UEI is required.'
  }

  if (!eventLabel || eventLabel.trim() === '') {
    errors.eventLabel = 'Event Label is required.'
  }

  if (!description || description.trim() === '') {
    errors.description = 'Description is required.'
  }

  if (!logmsg || logmsg.trim() === '') {
    errors.logmsg = 'Log Message is required.'
  }

  if (!dest || dest.trim() === '') {
    errors.dest = 'Destination is required.'
  }

  if (!severity || severity.trim() === '') {
    errors.severity = 'Severity is required.'
  }

  if (addAlarmData) {
    if (!reductionKey || reductionKey.trim() === '') {
      errors.reductionKey = 'Reduction Key is required when Alarm Data is added.'
    }

    if (!alarmType || alarmType.trim() === '') {
      errors.alarmType = 'Alarm Type is required when Alarm Data is added.'
    }

    if (autoClean) {
      if (!clearKey || clearKey.trim() === '') {
        errors.clearKey = 'Clear Key is required when Auto Clean is enabled.'
      }
    }
  }

  const maskElementErrors: Array<{ name?: string; value?: string }> = []
  const namesSet = new Set<string>()
  maskElements.forEach((element, index) => {
    const elementErrors: { name?: string; value?: string } = {}
    if (!element.name?._value || element.name?._value.trim() === '') {
      elementErrors.name = 'Mask Element Name is required.'
    } else if (namesSet.has(element.name._value)) {
      elementErrors.name = 'Mask Element Name must be unique.'
    } else {
      namesSet.add(element.name._value)
    }

    if (!element.value || element.value.trim() === '') {
      elementErrors.value = 'Mask Element Value is required.'
    }

    maskElementErrors[index] = elementErrors
  })

  if (maskElementErrors.some(err => Object.keys(err).length > 0)) {
    errors.maskElements = maskElementErrors
  }

  return errors
}


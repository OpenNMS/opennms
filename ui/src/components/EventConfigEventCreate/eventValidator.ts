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
  clearKey: string
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

  return errors
}


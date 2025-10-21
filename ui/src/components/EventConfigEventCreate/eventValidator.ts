import { EventFormErrors } from '@/types/eventConfig'

export const validateEvent = (
  uei: string,
  eventLabel: string,
  description: string,
  severity: string,
  dest: string,
  logmsg: string
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

  return errors
}


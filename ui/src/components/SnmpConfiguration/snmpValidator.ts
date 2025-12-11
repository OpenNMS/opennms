import { SnmpDefinitionFormErrors, SnmpProfileFormErrors } from '@/types/snmpConfig'

export const validateDefinition = (
  snmpVersion: string,
  firstIpAddress: string,
  secondIpAddress: string
): SnmpDefinitionFormErrors => {
  const errors: SnmpDefinitionFormErrors = {}

  if (!snmpVersion) {
    errors.snmpVersion = 'SNMP Version is required'
  }

  if (!firstIpAddress) {
    errors.firstIpAddress = 'First IP Address is required'
  }

  if (!secondIpAddress) {
    errors.secondIpAddress = 'Second IP Address is required'
  }

  return errors
}

export const validateProfile = (
  label: string,
  filterExpression: string
): SnmpProfileFormErrors => {
  const errors: SnmpProfileFormErrors = {}

  if (!label) {
    errors.label = 'SNMP Profile label is required'
  }

  if (!filterExpression) {
    errors.filterExpression = 'FilterExpression is required'
  }

  return errors
}

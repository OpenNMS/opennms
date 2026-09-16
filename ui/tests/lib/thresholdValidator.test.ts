import { describe, expect, test } from 'vitest'
import {
  FILTER_OPERATOR_OPTIONS,
  FilterOperator,
  MAX_DS_NAME_LENGTH,
  SERVICE_STATUS_OPTIONS,
  THRESHOLDING_GROUP_PARAMETER,
  THRESHOLD_TYPE_OPTIONS,
  ThresholdDefinitionKind,
  ThresholdType,
  hasErrors,
  validateAddressRange,
  validateResourceFilter,
  validateThreshdPackage,
  validateThreshdService,
  validateThresholdDefinition,
  validateThresholdGroup,
  validateThresholder
} from '@/lib/thresholdValidator'
import type { Expression, Threshold, ThreshdPackage, ThreshdService, ThresholdGroup } from '@/types/thresholdConfig'

const threshold = (overrides: Partial<Threshold> = {}): Threshold => ({
  type: ThresholdType.High,
  dsType: 'node',
  value: '90',
  rearm: '70',
  trigger: '3',
  filterOperator: FilterOperator.Or,
  resourceFilters: [],
  dsName: 'cpuUtilization',
  ...overrides
})

const expression = (overrides: Partial<Expression> = {}): Expression => ({
  type: ThresholdType.High,
  dsType: 'node',
  value: '90',
  rearm: '70',
  trigger: '3',
  filterOperator: FilterOperator.Or,
  resourceFilters: [],
  expression: 'a + b',
  ...overrides
})

const group = (overrides: Partial<ThresholdGroup> = {}): ThresholdGroup => ({
  name: 'mib2',
  rrdRepository: '/opt/opennms/share/rrd/snmp/',
  thresholds: [],
  expressions: [],
  ...overrides
})

const threshdPackage = (overrides: Partial<ThreshdPackage> = {}): ThreshdPackage => ({
  name: 'example1',
  filter: 'IPADDR != \'0.0.0.0\'',
  specifics: [],
  includeRanges: [],
  excludeRanges: [],
  includeUrls: [],
  services: [],
  outageCalendars: [],
  ...overrides
})

const service = (overrides: Partial<ThreshdService> = {}): ThreshdService => ({
  name: 'SNMP',
  interval: 300000,
  parameters: [{ key: THRESHOLDING_GROUP_PARAMETER, value: 'mib2' }],
  ...overrides
})

describe('threshold definition validation', () => {
  test.each(['0', '90', '-3.5', '+.5', '1e3', '1E-3', '${scv:key:value}'])('accepts %s as a value', (value) => {
    const errors = validateThresholdDefinition(threshold({ value }), ThresholdDefinitionKind.Threshold)
    expect(errors.value).toBeUndefined()
  })

  test.each(['abc', '1,5', '1.2.3', '${nocolon}', ''])('rejects %s as a value', (value) => {
    const errors = validateThresholdDefinition(threshold({ value }), ThresholdDefinitionKind.Threshold)
    expect(errors.value).toBeDefined()
  })

  test('anchors the patterns so a trailing suffix is not ignored', () => {
    // The XSD patterns are anchored; an unanchored copy would accept these and fail server-side instead.
    expect(validateThresholdDefinition(threshold({ trigger: '1abc' }), ThresholdDefinitionKind.Threshold).trigger)
      .toBeDefined()
    expect(validateThresholdDefinition(threshold({ value: '90abc' }), ThresholdDefinitionKind.Threshold).value)
      .toBeDefined()
  })

  test.each(['1', '10', '${scv:a:b}'])('accepts %s as a trigger', (trigger) => {
    expect(validateThresholdDefinition(threshold({ trigger }), ThresholdDefinitionKind.Threshold).trigger)
      .toBeUndefined()
  })

  test.each(['0', '-1', '1.5', ''])('rejects %s as a trigger', (trigger) => {
    expect(validateThresholdDefinition(threshold({ trigger }), ThresholdDefinitionKind.Threshold).trigger)
      .toBeDefined()
  })

  test('still requires rearm and trigger for the change thresholds', () => {
    // The daemon ignores them, but the schema declares them mandatory, so blanking them produces a
    // configuration the server refuses.
    const errors = validateThresholdDefinition(
      threshold({ type: ThresholdType.RelativeChange, rearm: '', trigger: '' }),
      ThresholdDefinitionKind.Threshold
    )
    expect(errors.rearm).toBeDefined()
    expect(errors.trigger).toBeDefined()
  })

  test('rejects a datasource name RRDtool cannot store', () => {
    const atLimit = 'a'.repeat(MAX_DS_NAME_LENGTH)
    const overLimit = 'a'.repeat(MAX_DS_NAME_LENGTH + 1)

    expect(validateThresholdDefinition(threshold({ dsName: atLimit }), ThresholdDefinitionKind.Threshold).dsName)
      .toBeUndefined()
    expect(validateThresholdDefinition(threshold({ dsName: overLimit }), ThresholdDefinitionKind.Threshold).dsName)
      .toContain(String(MAX_DS_NAME_LENGTH))
  })

  test('requires dsName only for thresholds and expression only for expressions', () => {
    expect(validateThresholdDefinition(threshold({ dsName: '' }), ThresholdDefinitionKind.Threshold).dsName)
      .toBeDefined()
    expect(validateThresholdDefinition(expression({ expression: '  ' }), ThresholdDefinitionKind.Expression).expression)
      .toBeDefined()

    // ...and neither field is demanded of the other kind.
    expect(validateThresholdDefinition(expression(), ThresholdDefinitionKind.Expression).dsName).toBeUndefined()
    expect(validateThresholdDefinition(threshold(), ThresholdDefinitionKind.Threshold).expression).toBeUndefined()
  })

  test('rejects an unknown type, datasource type or filter operator', () => {
    expect(validateThresholdDefinition(threshold({ type: 'sideways' }), ThresholdDefinitionKind.Threshold).type)
      .toBeDefined()
    expect(validateThresholdDefinition(threshold({ dsType: '' }), ThresholdDefinitionKind.Threshold).dsType)
      .toBeDefined()
    expect(
      validateThresholdDefinition(threshold({ filterOperator: 'xor' }), ThresholdDefinitionKind.Threshold)
        .filterOperator
    ).toBeDefined()
  })
})

describe('resource filter validation', () => {
  test('requires a field name', () => {
    expect(validateResourceFilter({ field: '', content: '.*' }).field).toBeDefined()
    expect(validateResourceFilter({ field: 'ifDescr', content: '.*' }).field).toBeUndefined()
  })

  test('flags a pattern that cannot be compiled, without blocking it', () => {
    // Java accepts constructs JavaScript does not, so this is advisory rather than a hard error.
    const errors = validateResourceFilter({ field: 'ifDescr', content: '[' })
    expect(errors.content).toBeDefined()
    expect(errors.field).toBeUndefined()
  })
})

describe('group validation', () => {
  test('requires a name and an RRD repository', () => {
    expect(validateThresholdGroup(group({ name: '  ' }), [], true).name).toBeDefined()
    expect(validateThresholdGroup(group({ rrdRepository: '' }), [], true).rrdRepository).toBeDefined()
  })

  test('rejects a name already in use', () => {
    expect(validateThresholdGroup(group(), ['mib2'], true).name).toBeDefined()
    expect(validateThresholdGroup(group(), ['cisco'], true).name).toBeUndefined()
  })

  test('rejects surrounding whitespace, which the server would silently trim away', () => {
    expect(validateThresholdGroup(group({ name: ' mib2 ' }), [], true).name).toBeDefined()
  })
})

describe('threshd validation', () => {
  test('requires a package name and filter', () => {
    expect(validateThreshdPackage(threshdPackage({ name: '' }), [], true).name).toBeDefined()
    expect(validateThreshdPackage(threshdPackage({ filter: '' }), [], true).filter).toBeDefined()
    expect(hasErrors(validateThreshdPackage(threshdPackage(), [], true))).toBe(false)
  })

  test('requires a positive whole-millisecond interval', () => {
    expect(validateThreshdService(service({ interval: 0 }), ['mib2']).interval).toBeDefined()
    expect(validateThreshdService(service({ interval: -1 }), ['mib2']).interval).toBeDefined()
    expect(validateThreshdService(service({ interval: 1.5 }), ['mib2']).interval).toBeDefined()
    expect(validateThreshdService(service({ interval: Number.NaN }), ['mib2']).interval).toBeDefined()
    expect(validateThreshdService(service(), ['mib2']).interval).toBeUndefined()
  })

  test('rejects a thresholding-group parameter naming a group that does not exist', () => {
    expect(validateThreshdService(service(), ['cisco']).thresholdingGroup).toBeDefined()
    expect(validateThreshdService(service(), ['mib2']).thresholdingGroup).toBeUndefined()

    // An empty value means "not bound yet", which is a legitimate intermediate state.
    const unbound = service({ parameters: [{ key: THRESHOLDING_GROUP_PARAMETER, value: '' }] })
    expect(validateThreshdService(unbound, ['mib2']).thresholdingGroup).toBeUndefined()
  })

  test('requires a thresholder service and class name', () => {
    expect(validateThresholder({ service: '', className: '', parameters: [] }).service).toBeDefined()
    expect(validateThresholder({ service: 'SNMP', className: '', parameters: [] }).className).toBeDefined()
    expect(hasErrors(validateThresholder({ service: 'SNMP', className: 'x.Y', parameters: [] }))).toBe(false)
  })

  test('requires both halves of an address range', () => {
    expect(validateAddressRange('1.1.1.1', '')).toBeDefined()
    expect(validateAddressRange('', '2.2.2.2')).toBeDefined()
    expect(validateAddressRange('', '')).toBeUndefined()
    expect(validateAddressRange('1.1.1.1', '2.2.2.2')).toBeUndefined()
  })
})

describe('select options', () => {
  test('offers every threshold type in schema order', () => {
    expect(THRESHOLD_TYPE_OPTIONS.map(option => option._value)).toEqual([
      'high',
      'low',
      'relativeChange',
      'absoluteChange',
      'rearmingAbsoluteChange'
    ])
  })

  test('offers both filter operators and both service statuses', () => {
    expect(FILTER_OPERATOR_OPTIONS.map(option => option._value).sort()).toEqual(['and', 'or'])
    expect(SERVICE_STATUS_OPTIONS.map(option => option._value)).toEqual(['on', 'off'])
  })
})

describe('hasErrors', () => {
  test('ignores undefined fields', () => {
    expect(hasErrors({ name: undefined })).toBe(false)
    expect(hasErrors({})).toBe(false)
    expect(hasErrors({ name: 'Name is required.' })).toBe(true)
  })
})

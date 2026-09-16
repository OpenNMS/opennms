import { describe, expect, test } from 'vitest'
import { GROUP_HELP, PACKAGE_HELP, RESOURCE_FILTER_HELP, THRESHOLD_DEFINITION_HELP, THRESHOLD_FIELD_HINTS, THRESHOLD_TYPE_HELP } from '@/lib/thresholdHelpText'
import { ThresholdType } from '@/lib/thresholdValidator'

describe('thresholdHelpText', () => {
  test('has help for every threshold type', () => {
    // Guards against adding a type to the enum and forgetting the copy, which would leave the Type field
    // with no hint at all.
    for (const type of Object.values(ThresholdType)) {
      expect(THRESHOLD_TYPE_HELP[type], `missing help for ${type}`).toBeTruthy()
    }
  })

  test('carries over the parts of the legacy help that users relied on', () => {
    expect(THRESHOLD_FIELD_HINTS.dsName).toContain('19')
    expect(THRESHOLD_FIELD_HINTS.dsType).toContain('ignores resource filters')
    expect(THRESHOLD_FIELD_HINTS.triggeredUEI).toContain('uei.opennms.org/<category>/<name>')
    expect(RESOURCE_FILTER_HELP).toContain('in the order listed')
    expect(THRESHOLD_DEFINITION_HELP).toContain('relativeChange')
    expect(GROUP_HELP).toContain('thresholding-group')
    expect(PACKAGE_HELP).toContain('Outage calendars')
  })
})

import { ONMS_UI_VERSION } from '@opennms/onms-ui'
import { describe, expect, it } from 'vitest'

describe('@opennms/onms-ui package wiring', () => {
  it('resolves the workspace package', () => {
    expect(ONMS_UI_VERSION).toBe('0.3.0')
  })
})

import { ONMS_UI_VERSION } from '@opennms/onms-ui'
import { describe, expect, it } from 'vitest'
import packageInfo from '../../packages/onms-ui/package.json'

describe('@opennms/onms-ui package wiring', () => {
  it('resolves the workspace package', () => {
    expect(ONMS_UI_VERSION).toBe(packageInfo.version)
    expect(ONMS_UI_VERSION).toMatch(/^\d+\.\d+\.\d+$/)
  })
})

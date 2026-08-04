import * as OnmsUI from '@opennms/onms-ui'
import { describe, expect, it } from 'vitest'

// window.OnmsUI (the externals target plugins compile against) is exactly
// this namespace: a missing runtime export breaks externalized plugin
// imports at load time. Additions are fine but must be conscious — update
// this list in the same commit as the barrel change.
const EXPECTED_RUNTIME_EXPORTS = [
  'ONMS_TOAST_GROUP_CENTER',
  'ONMS_TOAST_GROUP_START',
  'ONMS_UI_VERSION',
  'OnmsAutoComplete',
  'OnmsButton',
  'OnmsCard',
  'OnmsCheckbox',
  'OnmsChip',
  'OnmsColumn',
  'OnmsConfirmationDialog',
  'OnmsDatePicker',
  'OnmsDialog',
  'OnmsDrawer',
  'OnmsIcon',
  'OnmsIconButton',
  'OnmsInputNumber',
  'OnmsInputText',
  'OnmsListbox',
  'OnmsMenu',
  'OnmsMessageDialog',
  'OnmsMultiSelect',
  'OnmsPanel',
  'OnmsPassword',
  'OnmsPopover',
  'OnmsRadioButton',
  'OnmsSearchInput',
  'OnmsSelect',
  'OnmsSpinner',
  'OnmsTab',
  'OnmsTabList',
  'OnmsTabPanel',
  'OnmsTabPanels',
  'OnmsTable',
  'OnmsTabs',
  'OnmsTag',
  'OnmsTextarea',
  'OnmsToastHost',
  'OnmsToggleSwitch',
  'OnmsTooltip',
  'releaseActiveToast',
  'useOnmsToast'
]

describe('@opennms/onms-ui runtime export surface', () => {
  it('matches the window.OnmsUI contract exactly', () => {
    expect(Object.keys(OnmsUI).sort()).toEqual(EXPECTED_RUNTIME_EXPORTS)
  })

  it('carries the version marker', () => {
    expect(OnmsUI.ONMS_UI_VERSION).toMatch(/^\d+\.\d+\.\d+$/)
  })
})

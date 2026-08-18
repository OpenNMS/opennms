///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

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

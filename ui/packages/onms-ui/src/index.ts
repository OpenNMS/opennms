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

export const ONMS_UI_VERSION = '0.1.0'

export { default as OnmsAutoComplete } from './components/OnmsAutoComplete.vue'
export { default as OnmsButton } from './components/OnmsButton.vue'
export { default as OnmsCheckbox } from './components/OnmsCheckbox.vue'
export { default as OnmsChip } from './components/OnmsChip.vue'
export { default as OnmsDialog } from './components/OnmsDialog.vue'
export { default as OnmsDrawer } from './components/OnmsDrawer.vue'
export { default as OnmsIcon } from './components/OnmsIcon.vue'
export { default as OnmsIconButton } from './components/OnmsIconButton.vue'
export { default as OnmsInputText } from './components/OnmsInputText.vue'
export { default as OnmsMenu } from './components/OnmsMenu.vue'
export { default as OnmsPassword } from './components/OnmsPassword.vue'
export { default as OnmsRadioButton } from './components/OnmsRadioButton.vue'
export { default as OnmsSelect } from './components/OnmsSelect.vue'
export { default as OnmsSpinner } from './components/OnmsSpinner.vue'
export { default as OnmsTab } from './components/OnmsTab.vue'
export { default as OnmsTabList } from './components/OnmsTabList.vue'
export { default as OnmsTabPanel } from './components/OnmsTabPanel.vue'
export { default as OnmsTabPanels } from './components/OnmsTabPanels.vue'
export { default as OnmsTabs } from './components/OnmsTabs.vue'
export { default as OnmsTag } from './components/OnmsTag.vue'
export { default as OnmsTextarea } from './components/OnmsTextarea.vue'
export { default as OnmsToastHost } from './components/OnmsToastHost.vue'
export { default as OnmsToggleSwitch } from './components/OnmsToggleSwitch.vue'

export { useOnmsToast, ONMS_TOAST_GROUP_CENTER, ONMS_TOAST_GROUP_START } from './composables/useOnmsToast'

export type { OnmsMenuItem, OnmsTagSeverity } from './types'
export type { OnmsToastOptions, OnmsToastSeverity } from './composables/useOnmsToast'

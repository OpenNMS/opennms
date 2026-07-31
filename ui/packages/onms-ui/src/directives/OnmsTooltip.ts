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

import Tooltip from 'primevue/tooltip'

// Seam re-export (NMS-20054) of PrimeVue's Tooltip directive. The host app
// registers it as `v-onms-tooltip` (ui/src/theme/primevue-setup.ts), so app
// and plugin templates carry OpenNMS vocabulary instead of PrimeVue's.
// PrimeVue keys the directive's internals (pt name, data-pc-name, z-index
// bucket) off BaseTooltip.extend('tooltip', ...), not the registration name,
// so the rename is behavior-neutral (verified against primevue@4.5.5).
export default Tooltip

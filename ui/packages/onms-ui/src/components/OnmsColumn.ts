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

import type { VNode } from 'vue'
import Column from 'primevue/column'

// Seam "wrapper" (NMS-20081) for PrimeVue Column — a COMPILE-TIME re-export,
// not a component wrapper. PrimeVue's DataTable discovers its columns by
// walking its default-slot vnode tree for `type.name === 'Column'`
// (see @primevue/core HelperSet._recursive); a Column nested inside a real
// wrapper component is only discovered when the wrapper vnode carries an
// explicit `key`, and a forgotten key silently drops the column. So the
// runtime component IS PrimeVue Column (identical discovery, ordering and
// slot behavior), and the seam narrowing lives entirely in this type cast,
// enforced by vue-tsc in consumer templates. A future framework swap
// replaces this file with a real column-collection component under the same
// OnmsColumn tag.
//
// Because this is a re-export, the passthrough escape hatch keeps PrimeVue's
// `pt` prop name (a cast cannot rename a runtime prop) — the one deliberate
// naming exception to the package's `unsafePt` convention, documented in the
// README.

export interface OnmsColumnProps {
  /** Row property rendered in body cells (and used as the sort key) */
  field?: string
  /** Header cell text */
  header?: string
  /** Enables click-to-sort on this column's header */
  sortable?: boolean
  style?: string | Record<string, string>
  bodyStyle?: string | Record<string, string>
  class?: string
  /** Pins the column while the table scrolls horizontally */
  frozen?: boolean
  alignFrozen?: 'left' | 'right'
  /** Renders the row-expansion toggle cell (pair with OnmsTable's expandedRows/#expansion) */
  expander?: boolean
  /** Renders the row-edit init/save/cancel controls (pair with OnmsTable's editMode="row") */
  rowEditor?: boolean
  /** Escape hatch: PrimeVue passthrough. Named `pt` (not unsafePt) — see header comment. */
  pt?: unknown
}

export interface OnmsColumnSlots {
  body?: (scope: { data: any, index: number }) => VNode[]
  header?: (scope: { column: any }) => VNode[]
  editor?: (scope: { data: any, field: string, index: number }) => VNode[]
}

declare class _OnmsColumnComponent {
  $props: OnmsColumnProps
  $slots: OnmsColumnSlots
}

export default Column as unknown as typeof _OnmsColumnComponent
